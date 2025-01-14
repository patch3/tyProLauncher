package tylauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Window;
import tylauncher.Controllers.SettingsController;
import tylauncher.Managers.ManagerDirs;
import tylauncher.Managers.ManagerWindow;
import tylauncher.Utilites.AdminConsole.AdminConsole;
import tylauncher.Utilites.Constants.FXMLS;
import tylauncher.Utilites.Constants.Images;
import tylauncher.Utilites.Constants.Titles;
import tylauncher.Utilites.*;
import tylauncher.Utilites.Tasks.CheckUpdTask;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tylauncher.Utilites.Utils.*;

public class Main extends Application {
    public static List<String> filesToDeleteOnExit = new ArrayList<>();
    public static final String launcher_version = "0.4.6";
    public static final User user = new User();
    private static final Logger logger = new Logger(Main.class);
    private static final ManagerDirs _launcherDir = new ManagerDirs("TyPro");
    private static final ManagerDirs runtimeDir = new ManagerDirs(String.format("TyPro%sruntime%sjre8%s", File.separator,File.separator,File .separator));
    public static Stage mainStage = null;
    private static TrayIcon trayIcon;
    private static ManagerDirs _clientDir = new ManagerDirs(String.format("TyPro%sclients%s", File.separator, File.separator));

    public static File getRuntimeDir() {
        return runtimeDir.getWorkDir();
    }

    public static File getLauncherDir() {
        return _launcherDir.getWorkDir();
    }

    public static File getClientDir() {
        return _clientDir.getWorkDir();
    }

    public static void setClientDir(File f) {
        _clientDir.setWorkDir(f);
    }

    public static void resetClientDir() {
        _clientDir = new ManagerDirs(String.format("TyPro%sclients%s", File.separator, File.separator));
    }

    static void test() {}



    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        logger.logInfo("START LAUNCHER");
        Thread adminConsole = new Thread(new AdminConsole());
        adminConsole.start();

        logger.logInfo(UserPC.getPCinfo());
        test();

        System.setProperty("user.dir", getLauncherDir().getAbsolutePath());

        if (args.length > 0 && args[0].equalsIgnoreCase("love")) {
            logger.logInfo("Пасхалка");
            easter();
            System.exit(0);
        }

        if (new File(getClientDir() + File.separator + "TyUpdaterLauncher.jar").exists())
            Utils.DeleteFile(new File(getClientDir() + File.separator + "TyUpdaterLauncher.jar"));

        splitOut();

        try {
            SettingsController.readSettingsFromFileToSettings();
        } catch (Exception e) {
            logger.logError(e);
        }

        launch();
    }

    /**
     * полностью закрывает всё, удаляет иконку из трея.. Да блять, сам посмотри что оно делает..
     */
    public static void exit() {
        logger.logInfo("Вызван метод выхода");
        SystemTray.getSystemTray().remove(trayIcon);
        filesToDeleteOnExit.forEach(s ->{
            File delFile = new File(s);
            if (delFile.exists()){
                Utils.DeleteFile(new File(s));
            }else {
                System.err.println("Файл не найден: " + delFile.getAbsolutePath());
            }
        });
        Platform.exit();
        System.exit(0);
    }

    private static void trayIconCreate(Window window) throws IOException, FontFormatException {
        java.awt.Image img = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("assets/ico.png"));


        trayIcon = new TrayIcon(img, "TypicalLauncher", poppupMenuCreate());
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(mouseEvent -> Platform.runLater(() -> {
            Stage st = (Stage) window;
            st.setIconified(!st.isIconified());
        }));


        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            logger.logInfo(e);
        }
    }

    private static PopupMenu poppupMenuCreate() throws IOException, FontFormatException {
        //Настройка выпоадающего списка при нажатии пкм на иконку трея
        java.awt.Font font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, Objects.requireNonNull
                (Main.class.getResourceAsStream("StyleSheets/Minecraft.ttf")));

        PopupMenu popupMenu = new PopupMenu();


        MenuItem exit = new MenuItem("Выход");
        exit.addActionListener(e -> exit());

        MenuItem showUnShow = new MenuItem("Показать/скрыть");
        showUnShow.addActionListener(e -> Platform.runLater(() -> mainStage.setIconified(!mainStage.isIconified())));

        popupMenu.add(showUnShow);
        popupMenu.addSeparator();
        popupMenu.add(exit);

        popupMenu.setFont(font.deriveFont(14f));
        return popupMenu;
    }

    private static File setLogs() throws IOException {
        File dir_logs = new File(getLauncherDir() + File.separator + "logs");
        if (!dir_logs.exists()) dir_logs.mkdirs();

        // время для логов
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

        // очищаем от говна
        String logfile = now.toString()
                .replace(":", "-")
                .replace("]", "")
                .replace("[", "")
                .replace(".", "")
                .replace("/", "-")
                + ".log";

        // лог файл
        File log = new File(dir_logs + File.separator + "LogFile_" + logfile);
        if (!log.exists()) log.createNewFile();
        CheckLogs();
        return log;
    }

    private static void splitOut() throws IOException {
        // запись в файл и вывод в консоль
        PrintStream out = new PrintStream(Files.newOutputStream(setLogs().toPath()));

        PrintStream dual = new DualStream(System.out, out);
        System.setOut(dual);

        dual = new DualStream(System.err, out);
        System.setErr(dual);
    }

    @Override
    public void start(Stage primaryStage) throws IOException, FontFormatException {
        Font.loadFont(getClass().getResourceAsStream("Minecraft.ttf"), 16);
        Parent root = null;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource(FXMLS.UPDATER)));
        } catch (IOException e) {
            logger.logError(e);
            exit();
        }
        Scene scene = new Scene(Objects.requireNonNull(root), 350, 100);
        primaryStage.setTitle(Titles.BASE_LAUNCHER_TITLE);
        primaryStage.getIcons().add(Images.ICON_DEFAULT);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.getScene().getWindow().centerOnScreen();
        primaryStage.show();
        mainStage = primaryStage;
        ManagerWindow.initWindows();
        Window window = scene.getWindow();
        window.setOnCloseRequest(event -> Main.exit());

        try {
            trayIconCreate(window);
        } catch (IOException | FontFormatException e) {
            logger.logError(e);
        }
        UpdaterLauncher.checkUpdate();

        new CheckUpdTask();
    }
}
