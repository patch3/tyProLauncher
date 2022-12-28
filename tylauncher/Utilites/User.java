package tylauncher.Utilites;

import javafx.scene.image.Image;
import tylauncher.Main;
import tylauncher.Utilites.Managers.ManagerWeb;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;

public class User {
    private String _email;
    private long _id;
    private String _password;
    private String _login;
    private String _session;
    private String _balance;
    private String _group;
    private Image _image;
    public boolean wasAuth = false;

    public User() {
        this._id = -1;
        this._email = "";
        this._password = "";
        this._login = "";
        this._session = "";
        this._balance = "0";
        this._group = "[Игрок]";
        this._image = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("assets/picked/steve.png")));
    }

    public boolean Auth() throws Exception {
        WebAnswer.Reset();

        if (_login.isEmpty() || _password.isEmpty()) throw new Exception("Логин или пароль не введены");

        ManagerWeb authManagerWeb = new ManagerWeb("auth");
        authManagerWeb.setUrl("https://typro.space/vendor/launcher/login_launcher.php");
        authManagerWeb.putParam("login", _login);
        authManagerWeb.putParam("password", _password);
        authManagerWeb.request();

        System.err.println(authManagerWeb);

        String[] answer = authManagerWeb.getAnswerMass();

        if (answer[0].equals("status")) WebAnswer.setStatus(answer[1]);

        if (answer[2].equals("type")) WebAnswer.setType(answer[3]);
        if (answer[4].equals("message")) WebAnswer.setMessage(answer[5]);
        if (answer[2].equals("user") && answer[3].equals("id")) this._id = Integer.parseInt(answer[4]);
        if (answer[5].equals("login")) this._login = answer[6];
        if (answer.length > 7) {
            if (answer[7].equals("email")) this._email = answer[8];
            if (answer[11].equals("coin")) this._balance = answer[12];
            if(wasAuth && !WebAnswer.getStatus()){
                System.err.println(this);
                throw new Exception("Данные авторизации поломались. Авторизуйтесь заново.");
            }
            if (!wasAuth && WebAnswer.getStatus()){
                if (answer[9].equals("session")) this._session = answer[10];
                System.err.println(this);
                wasAuth = true;
                return WebAnswer.getStatus();
            }
        }
        System.err.println(this);
        return WebAnswer.getStatus();

    }


    public String GetBalance() {
        return _balance;
    }

    public String GetGroup() {
        return _group;
    }

    public String GetLogin() {
        return _login;
    }

    public Image GetImage() {
        return this._image;
    }

    public String GetPassword() {
        return this._password;
    }

    public String getSession() {
        return _session;
    }
    public void setSession(String _session) {
        this._session = _session;
    }
    public String getEmail() {
        return _email;
    }

    public void setPassword(String _password) {
        this._password = _password;
    }

    public void setLogin(String _login) {
        this._login = _login;
    }

    public void setImage(Image _image) {
        this._image = _image;
    }

    public void Reset() {
        this._password = "";
        this._session = "";
        this._login = "";
        this._group = "[Игрок]";
        this._balance = "0$";
        this._image = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("assets/picked/steve.png")));
    }

    @Override
    public String toString() {
        return "User{" + "_email='" + _email + '\'' + ", _id=" + _id + ", _password='" + _password + '\'' + ", _login='" + _login + '\'' + ", _session='" + _session + '\'' + ", _balance='" + _balance + '\'' + ", _group='" + _group + '\'' + ", _image=" + _image.toString() + '}';
    }
}
