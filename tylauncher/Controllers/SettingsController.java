package tylauncher.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import jdk.nashorn.internal.objects.Global;
import tylauncher.Main;
import tylauncher.Utilites.*;
import tylauncher.Utilites.Managers.ManagerAnimations;
import tylauncher.Utilites.Managers.ManagerForJSON;
import tylauncher.Utilites.Managers.ManagerStart;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;

import static tylauncher.Controllers.AccountAuthController.accountController;
import static tylauncher.Main.user;

public class SettingsController {
    public static Settings settings = new Settings();
    @FXML
    protected Text infoText;
    @FXML
    protected Pane infoTextPane;
    @FXML
    private AnchorPane A1;
    @FXML
    private ImageView Account_Img;
    @FXML
    private ImageView Forum_Img;
    @FXML
    private CheckBox Fullscrean_Checkbox;
    @FXML
    private ImageView Message_Img;
    @FXML
    private ImageView News_Img;
    /*@FXML
    private Text OzuCount_Text;*/
    @FXML
    private Slider Ozu_Slider;
    @FXML
    private ImageView Play_Img;
    @FXML
    private Button Save_Button;
    @FXML
    private ImageView Settings_Img;
    @FXML
    private TextField X_Label;
    @FXML
    private TextField Y_Label;
    @FXML
    private TextField OzuCount_Label;

    @FXML
    private Pane updateAvailablePane;

    @FXML
    private Button updateButton;

    @FXML
    private Text SettingsSaved_Text;

    @FXML
    private Pane SettingsPane;

    @FXML

    private final ManagerForJSON settingsJson = new ManagerForJSON(4);

    private final File settingsFile = new File(Main.getLauncherDir() + File.separator + "settings.json");

    private static boolean hellishTheme = false;

    @FXML
    void initialize() {
        ButtonPage.reset();
        ButtonPage.setPressedNum(5);
        BooleanPageController.addButton(Account_Img);
        BooleanPageController.addButton(News_Img);
        BooleanPageController.addButton(Forum_Img);
        BooleanPageController.addButton(Message_Img);
        BooleanPageController.addButton(Settings_Img);
        BooleanPageController.addButton(Play_Img);

        ErrorInterp.settingsController = this;
        if(hellishTheme) SettingsPane.setStyle("-fx-background-color:  ff0000;");


        if (settingsFile.exists()) {
            try {
                settingsJson.ReadJSONFile(settingsFile.getAbsolutePath());
                settings.setOzu(Integer.parseInt(settingsJson.GetOfIndex(0,1)));
                settings.setX(Integer.parseInt(settingsJson.GetOfIndex(1,1)));
                settings.setY(Integer.parseInt(settingsJson.GetOfIndex(2,1)));
                settings.setFsc(Boolean.parseBoolean(settingsJson.GetOfIndex(3,1)));
            } catch (Exception e) {
                setInfoText(e.toString());
                e.printStackTrace();
            }
        }
        if(UpdaterController.updateAvailable){
            updateAvailablePane.setDisable(false);
            updateAvailablePane.setVisible(true);
        }
        updateButton.setOnMouseClicked(mouseEvent ->{
            UpdaterLauncher.UpdateLauncher();
        });

        Ozu_Slider.setMax((int)(UserPC.getOzu()/512) * 512);

        Ozu_Slider.setValue(settings.getOzu());
        OzuCount_Label.setText(String.valueOf(settings.getOzu()));

        try {
            WriteSettingToFile();
            UpdateSettings();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Ozu_Slider.valueProperty().addListener((obs, oldval, newval) -> {
            int value = Math.round(newval.floatValue() / 512) * 512;
            OzuCount_Label.setText(String.valueOf(value));
            Ozu_Slider.setOnMouseReleased(mouseEvent -> {
                Ozu_Slider.setValue(value);
                OzuCount_Label.setText(String.valueOf(value));
            });
        });
        OzuCount_Label.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.trim().equalsIgnoreCase(String.valueOf(666))){
                SettingsPane.setStyle("-fx-background-color:  ff0000;");
                OzuCount_Label.setStyle("-fx-background-color: ff0000;");
                ErrorInterp.setMessageError("Зря..", "settings");
                A1.getScene().setCursor(Cursor.cursor(String.valueOf(Main.class.getResource("assets/HellTyMasunya.png"))));
                hellishTheme = true;
            }
            if(newValue.trim().equalsIgnoreCase("999")){
                SettingsPane.setStyle("-fx-background-color: #363636;");
                OzuCount_Label.setStyle("-fx-background-color: #363636;");
                ErrorInterp.setMessageError("Умничка :)", "settings");
                A1.getScene().setCursor(Cursor.DEFAULT);
                hellishTheme = false;
            }
            if (!newValue.matches("\\d*")) OzuCount_Label.setText(oldValue);
        });
        X_Label.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) X_Label.setText(oldValue);
        });
        Y_Label.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) Y_Label.setText(oldValue);
        });
        Save_Button.setOnMouseClicked(mouseEvent -> {
            settings.setFsc(Fullscrean_Checkbox.isSelected());
            try {
                settings.setX(Integer.parseInt(X_Label.getText()));
                settings.setY(Integer.parseInt(Y_Label.getText()));
                settings.setOzu(Integer.parseInt(OzuCount_Label.getText()));
                WriteSettingToFile();
                UpdateSettings();
                infoTextPane.setVisible(false);
            } catch (Exception e) {
                SettingsSaved_Text.setVisible(false);
                ErrorInterp.setMessageError(e.toString(), "settings");
                UpdateSettings();
                System.err.println(settings.toString());
                return;
            }
            SettingsSaved_Text.setVisible(true);
            ManagerAnimations.StartFadeAnim(SettingsSaved_Text);
        });

        News_Img.setOnMouseClicked(mouseEvent -> Main.OpenNew("News.fxml", A1));
        Forum_Img.setOnMouseClicked(mouseEvent -> Main.OpenNew("Forum.fxml", A1));
        Message_Img.setOnMouseClicked(mouseEvent -> Main.OpenNew("Message.fxml", A1));
        Play_Img.setOnMouseClicked(mouseEvent -> Main.OpenNew("Play.fxml", A1));

        Account_Img.setOnMouseClicked(mouseEvent -> {
            try {
                if (user.Auth()) {
                    Main.OpenNew("Account.fxml", A1);
                    accountController.UpdateData();
                } else Main.OpenNew("AccountAuth.fxml", A1);
            } catch (Exception e) {
                Main.OpenNew("AccountAuth.fxml", A1);
            }
        });
    }
    void UpdateSettings() {
        OzuCount_Label.setText(String.valueOf(settings.getOzu()));
        Ozu_Slider.setValue(settings.getOzu());
        Y_Label.setText(String.valueOf(settings.getY()));
        X_Label.setText(String.valueOf(settings.getX()));
        Fullscrean_Checkbox.setSelected(settings.getFsc());
    }

    public void WriteSettingToFile() throws Exception {
        settingsJson.setOfIndex("ozu", 0, 0);
        settingsJson.setOfIndex(String.valueOf(settings.getOzu()), 0, 1);
        settingsJson.setOfIndex("x", 1, 0);
        settingsJson.setOfIndex(String.valueOf(settings.getX()), 1, 1);
        settingsJson.setOfIndex("y", 2, 0);
        settingsJson.setOfIndex(String.valueOf(settings.getY()), 2, 1);
        settingsJson.setOfIndex("FullScreenMode", 3, 0);
        settingsJson.setOfIndex(String.valueOf(settings.getFsc()), 3, 1);
        settingsJson.WritingFile(Main.getLauncherDir() + File.separator + "settings.json");
    }

    public void setInfoText(String info) {
        infoTextPane.setVisible(true);
        infoText.setText(info);
        ManagerAnimations.StartFadeAnim(infoTextPane);
    }

}
