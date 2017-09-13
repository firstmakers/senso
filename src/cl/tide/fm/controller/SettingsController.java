/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.controller;

import cl.tide.fm.model.SettingsEvent;
import cl.tide.fm.sensonet.SensonetClient;
import cl.tide.fm.sensonet.User;
import cl.tide.fm.utilities.IntegerField;
import cl.tide.fm.utilities.IntegerFieldSample;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


/**
 *
 * @author Edison Delgado
 */
public class SettingsController extends AnchorPane {


    private FXMLLoader fxmlLoader;
    private boolean  running = false;
    public static final String PACKAGE = "cl.tide.senso";
    @FXML PasswordField sensonetPassword;
    @FXML Text passwordError, emailError, infoConnect;
    @FXML
    public Tab tabSetting, tabSample, tabInterval, tabProgram;
    @FXML
    public TabPane tabPaneSetting;
    @FXML
    public TextArea sensonetToken;
    @FXML
    public TextField workspace, apikey, sensonetEmail;
    @FXML
    Hyperlink /*linkaccount,*/ linkSensonet;
    @FXML
    Button btnworkspace, btnOk, testConnect;
    @FXML
    Label lbtimer;
    @FXML
    CheckBox cbxsaveall, cbxanimation, cbxfuturesamples, /*cbxubidots,*/ autoconnect;
    public static int GENERAL_SETTINGS = 0;
    public static int SAMPLE_SETTINGS = 1;
    public static int INTERVAL_SETTINGS = 2;
    public static int PROGRAM_SETTINGS = 3;
    private static final Boolean USE_ANIMATION_DEFAULT = false;
    //private static final String UBIDOTS_ID_DATASOURCE = "ubidots_id_data_source";
    private static final String USE_ANIMATION = "use_animation";
    private static final String SAVE_ALL_FILES = "save_all_files";
    private static final Boolean SAVE_ALL_FILES_DEFAULT = false;
    private static final String WORKSPACE = "workspace_path";
    private static final String WORKSPACE_DEFAULT = System.getProperties().getProperty("user.home");
    private static final String USER_INTERVAL = "user_interval";
    private static final String USER_SAMPLES = "user_samples";
    private static final String USER_FUTURE_SAMPLES = "user_future_samples";
    private static final String USER_FUTURE_SAMPLES_MS = "user_samples_in_ms";
    private static final Boolean USER_FUTURE_SAMPLES_DEFAULT = false;
    private static final long USER_FUTURE_SAMPLES_MS_DEFAULT = 0;
    private static final long USER_INTERVAL_DEFAULT = 1000;
    private static final int USER_SAMPLES_DEFAULT = 1000;
    //private static final String USER_APIKEY = "ubidots_apikey";
    private static final String USER_APIKEY_DEFAULT = "";
    //private static final String USER_ALLOW_UBIDOTS = "allow_ubidots";
    //private static final Boolean USER_ALLOW_UBIDOTS_DEFAULT = false;

    private static final String SENSONET_KEY_PROJECT = "sensonet_key_project";
    private static final String SENSONET_KEY_PROJECT_DEFAULT = "";
    private static final String USER_SENSONET = "user_sensonet";
    private static final String USER_SENSONET_DEFAULT = "";
    private static final String PASS_SENSONET = "sensonet_password";
    private static final String PASS_SENSONET_DEFAULT = "";
    private static final String SENSONET_AUTOCONNECT = "sensonet_autoconnect";
    private static final Boolean SENSONET_AUTOCONNECT_DEFAULT = true; 
    
    private final Stage owner;
    private final Stage dialog;
    private List<SettingsChange> listener;
    private Timer timer;

    private static Preferences preferences;

    @FXML
    public IntegerField hour, minute, programSecond, programHour, programMinute, second;
    @FXML
    public IntegerFieldSample sample;

    public SettingsController(Stage st) {
        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        listener = new ArrayList<>();
        
        //createRegistry();
        preferences = Preferences.userRoot().node(PACKAGE);
        this.owner = st;
        this.dialog = new Stage();
        //ventana modal
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Configuración");
        Scene dialogScene = new Scene(this);
        dialogScene.getStylesheets().add("/styles/Styles.css");
        dialog.setScene(dialogScene);
        dialog.setMinWidth(620);
        dialog.setMinHeight(320);
        dialog.setMaxWidth(620);
        dialog.setMaxHeight(340);
        dialog.setOnCloseRequest((WindowEvent event) -> {
            stopTimer();
            System.err.println("cerrando configuración");
        });
        /*testConnect.focusedProperty().addListener((change)->{
            infoConnect.setText("");
        });
        sensonetPassword.focusedProperty().addListener(new ChangeListener<Boolean>(){
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                   passwordError.setText("");
                }
            });
        sensonetEmail.focusedProperty().addListener(new ChangeListener<Boolean>(){
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                   emailError.setText("");
                }
            });
        autoconnect.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            setSensonetAutoConnect(newValue);
        });
        */
        /*cbxubidots.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            setAllowAUbidots(newValue);
        });*/
        cbxanimation.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            setAnimation(newValue);
        });
        cbxsaveall.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {

            setSaveAllFiles(newValue);
        });
        cbxfuturesamples.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            setFutureSample(newValue);
            cbxfuturesamples.setSelected(newValue);
            if (newValue) {
                startTimer();
            } else {
                stopTimer();
            }

        });

        sample.addListener(samplesChange);

        hour.addListener(intervalChange);
        minute.addListener(intervalChange);
        second.addListener(intervalChange);

        programHour.addListener(futureSampleChange);
        programMinute.addListener(futureSampleChange);
        programSecond.addListener(futureSampleChange);

        btnOk.setOnAction((e) -> {
            dialog.close();
        });
        /*sensonetToken.textProperty().addListener((obs,oldv,newv)->{
            setSensonetProject(newv);
        });
        
        sensonetEmail.textProperty().addListener((obs,oldv, newv)->{
            setSensonetUser(newv);
        });
        
        sensonetPassword.textProperty().addListener((obs,oldv,newv)->{
            setSensonetPassword(newv);
        });
        /*apikey.textProperty().addListener((obs,oldv,newv)->{
            setUbidotsApiKey(newv);
        });*/
        init();
    }

    IntegerField.ValueChange samplesChange = (int value) -> {
        if (value != getSamples()) {
            setSamples(value);
        }
    };

    IntegerField.ValueChange futureSampleChange = (int value) -> {
        System.err.println("Cambios en la programacion");
        long i = getHourToMillisecond(
                programHour.getValue(),
                programMinute.getValue(),
                programSecond.getValue());
        setFutureSampleInMS(i);
    };

    IntegerField.ValueChange intervalChange = (int value) -> {
        System.err.println("Cambios en intervalo");
        long i = getHourToMillisecond(
                hour.getValue(),
                minute.getValue(),
                second.getValue());
        final long interval = i == 0 ? 86400000 : i;//  0 == 24 horas
        setInterval(interval);
    };
    
    /*private void createRegistry(){
        if(PlatformUtil.isWindows()){
            try {
                Reg reg = new Reg();
                Reg.Key key = reg.add("HKEY_CURRENT_USER\\SOFTWARE\\JavaSoft\\Prefs");
                reg.write();
            } catch (IOException ex) {
                Logger.getLogger(SettingsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }*/

    private void startTimer() {
        if (timer != null) {
            stopTimer();
            timer = null;
        }
        running = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long current = getHourToMillisecond(LocalTime.now().getHour(),
                        LocalTime.now().getMinute(),
                        LocalTime.now().getSecond());
                int[] remaining = getMsToHMS(getFutureSampleInMS() - current);
                String hora = remaining[0] < 10 ? "0" + remaining[0] : remaining[0] + "";
                String minutos = remaining[1] < 10 ? "0" + remaining[1] : remaining[1] + "";
                String segundos = remaining[2] < 10 ? "0" + remaining[2] : remaining[2] + "";
                Platform.runLater(() -> {
                    if(running)
                        lbtimer.setText("La medición comenzará en " + hora + ":" + minutos + ":" + segundos);
                });
                if (getFutureSampleInMS() <= current) {               
                    stopTimer();
                }
            }
        }, 0, 1000);
    }

    private void stopTimer() {
         running = false;
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }
        if(lbtimer != null)
            Platform.runLater(()->{
                lbtimer.setText("");
            });

    }

    //////**** PUBLIC METHODS****\\\\\\
    public void show() {
        dialog.show();
        init();
    }

    public void selectWorkspace(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Elegir espacio de trabajo");
        File file = directoryChooser.showDialog(null);
        if (file != null) {
            setWorkspace(file.getPath());
        }
    }

    public void setSelected(int index) {
        tabPaneSetting.getSelectionModel().select(index);
    }
 /*   
    public static String getUbidotsVariable(String idSensor){
        return SettingsController.preferences.get(idSensor,"");
    }
    
    public static void setUbidotsVariable(String idSensor, String idVariableUbidots){
        SettingsController.preferences.put(idSensor, sanitize(idVariableUbidots));
    }
    
    public static String getUbidotsDataSource() {
        return SettingsController.preferences.get(UBIDOTS_ID_DATASOURCE, "");
    }

    public static void setUbidotsDataSource(String id) {
        SettingsController.preferences.put(UBIDOTS_ID_DATASOURCE, sanitize(id));
    }
*/
    private void setAnimation(Boolean value) {
        SettingsController.preferences.putBoolean(USE_ANIMATION, value);
        notifyEvent();
    }
   /* public static Boolean getAllowUbidots(){
        return SettingsController.preferences.getBoolean(USER_ALLOW_UBIDOTS, USER_ALLOW_UBIDOTS_DEFAULT);
    }
    public static String getUbidotsApiKey(){
        return SettingsController.preferences.get(USER_APIKEY, USER_APIKEY_DEFAULT);
    }
*/
    public static boolean getAnimation() {
        return SettingsController.preferences.getBoolean(USE_ANIMATION, USE_ANIMATION_DEFAULT);
    }

    public static boolean getSaveAllFiles() {
        return SettingsController.preferences.getBoolean(SAVE_ALL_FILES, SAVE_ALL_FILES_DEFAULT);
    }

    public static String getWorkspace() {
        return SettingsController.preferences.get(WORKSPACE, WORKSPACE_DEFAULT);
    }

    public static long getInterval() {
        return SettingsController.preferences.getLong(USER_INTERVAL, USER_INTERVAL_DEFAULT);
    }

    public static int getSamples() {
        return SettingsController.preferences.getInt(USER_SAMPLES, USER_SAMPLES_DEFAULT);
    }

    public static boolean getFutureSample() {
        return SettingsController.preferences.getBoolean(USER_FUTURE_SAMPLES, USER_FUTURE_SAMPLES_DEFAULT);
    }

    public static long getFutureSampleInMS() {
        return SettingsController.preferences.getLong(USER_FUTURE_SAMPLES_MS, USER_FUTURE_SAMPLES_MS_DEFAULT);
    }

    public static long getHourToMillisecond(int hour, int min, int sec) {
        return hour * 3600000
                + min * 60000
                + sec * 1000;
    }
    
    public static String getSensonetUser(){
        return SettingsController.preferences.get(USER_SENSONET, USER_SENSONET_DEFAULT);
    }
    public static void setSensonetUser(String user){
        SettingsController.preferences.put(USER_SENSONET, sanitize(user));
    }
    public static String getSensonetPassword(){
        return SettingsController.preferences.get(PASS_SENSONET, PASS_SENSONET_DEFAULT);
    }
    public static void setSensonetPassword(String pass){
        SettingsController.preferences.put(PASS_SENSONET, sanitize(pass));
    }
    
    public static String getSensonetProject(){
    return SettingsController.preferences.get(SENSONET_KEY_PROJECT, SENSONET_KEY_PROJECT_DEFAULT);
    }
    public  void setSensonetProject(String key){
        SettingsController.preferences.put(SENSONET_KEY_PROJECT, sanitize(key));
        notifyEvent();
    }
    public static Boolean getSensonetAutoConnect(){
        return SettingsController.preferences.getBoolean(SENSONET_AUTOCONNECT, SENSONET_AUTOCONNECT_DEFAULT);
    }
    public static void setSensonetAutoConnect(Boolean key){
        SettingsController.preferences.putBoolean(SENSONET_AUTOCONNECT, key);
    }

    public static int[] getMsToHMS(long ms) {
        int[] hms = new int[3];
        hms[0] = (int) (ms / 3600000);
        long restoHora = ms % 3600000;
        hms[1] = (int) (restoHora / 60000);
        long restoMinutos = restoHora % 60000;
        hms[2] = (int) (restoMinutos / 1000);
        return hms;
    }

    /*public void openLink(ActionEvent e) {
        linkaccount.setVisited(false);
        try {
            Desktop.getDesktop().browse(new URI("https://app.ubidots.com/accounts/signup"));
        } catch (IOException | URISyntaxException e1) {
        }
    }*/
    
    /**
     * Abre el navegador para crear una cuenta sensonet
     */
    public void openSensonetAccount(ActionEvent e){
        linkSensonet.setVisited(false);
        try {
            Desktop.getDesktop()
                    .browse(new URI(SensonetClient.baseUrl.concat("signup")));
        } catch (IOException | URISyntaxException e1) {
        }
    }
    
    public void testLogin(ActionEvent e){
        infoConnect.setText("");
        String email = sensonetEmail.getText();
        String password = sensonetPassword.getText();
     
        if(password.isEmpty()){
            passwordError.setText("Debes ingresar la contraseña");
            return;
        }else if(email.isEmpty()){
            emailError.setText("Debes ingresar u correo electrónico");
            return;
        }
        else{
            if(SensonetClient.netIsAvailable()){
            infoConnect.setText("Accediendo...");
            testConnect.setDisable(true);
            SensonetClient sensonet = new SensonetClient();
           
             User user = sensonet.login(email, password);
             if(user != null){
                 infoConnect.setText("Bienvenido "+ user.name);
             }else{
                 infoConnect.setText("Ocurrio un error, revisa tu usuario y contraseña ");
             }
             sensonet.close();
             testConnect.setDisable(false);
            }else{
                infoConnect.setText("Comprueba tu conexión de internet");
            }
        }
    }


    ///////**** PRIVATE METHODS *****\\\\\\\
    private void init() {
        /*sensonetEmail.setText(getSensonetUser());
        sensonetPassword.setText(getSensonetPassword());
        sensonetToken.setText(getSensonetProject());*/
       /* apikey.setText(getUbidotsApiKey());
        setAllowAUbidots(getAllowUbidots());*/
        setSaveAllFiles(getSaveAllFiles());
        setAnimation(getAnimation());
        cbxanimation.setSelected(getAnimation());
        cbxsaveall.setSelected(getSaveAllFiles());
       // cbxubidots.setSelected(getAllowUbidots());
        workspace.setText(getWorkspace());

        sample.setMaxValue(99999999);
        sample.setMinValue(1);
        sample.setValue(getSamples());

        hour.setMaxValue(23);
        minute.setMaxValue(59);
        second.setMaxValue(59);

        hour.setMinValue(0);
        minute.setMinValue(0);
        second.setMinValue(0);

        int[] interval = getMsToHMS(getInterval());

        hour.setValue(interval[0]);
        minute.setValue(interval[1]);
        second.setValue(interval[2]);

        programHour.setMaxValue(23);
        programMinute.setMaxValue(59);
        programSecond.setMaxValue(59);

        programHour.setMinValue(0);
        programMinute.setMinValue(0);
        second.setMinValue(0);

        boolean isProgram = getFutureSample();
        cbxfuturesamples.setSelected(isProgram);
        setFutureSampleInMS(getFutureSampleInMS());
        int[] x = getMsToHMS(getFutureSampleInMS());
        programHour.setValue(x[0]);
        programMinute.setValue(x[1]);
        programSecond.setValue(x[2]);
        if (isProgram) {
            startTimer();
        }

    }

    public void setInterval(long ms) {

        final long interval = ms == 0 ? 86400000 : ms;//  0 == 24 horas
        SettingsController.preferences.putLong(USER_INTERVAL, interval);
        notifyEvent();
        setCurrentInterval(interval);
    }

    public void setSamples(int samples) {
        SettingsController.preferences.putInt(USER_SAMPLES, samples);
        notifyEvent();
        setCurrentSamples();
    }

    public void setFutureSampleInMS(long ms) {
        long currentTime = getHourToMillisecond(LocalTime.now().getHour(),
                LocalTime.now().getMinute(),
                LocalTime.now().getSecond());
        long ftsample = ms == 0 ? 86400000 : ms;//  0 == 24 horas
        System.out.println(ftsample);
        if (ftsample < currentTime) {
            ftsample = 86400000 + ftsample;
        }
        SettingsController.preferences.putLong(USER_FUTURE_SAMPLES_MS, ftsample);
        notifyEvent();  
    }

    public void setFutureSample(Boolean value) {
        SettingsController.preferences.putBoolean(USER_FUTURE_SAMPLES, value);
        notifyEvent();
    }
    
    
   /* private void setUbidotsApiKey(String key){
        
        SettingsController.preferences.put(USER_APIKEY, sanitize(key));
        notifyEvent();
    }*/

    private void setSaveAllFiles(Boolean value) {
        if (value) {
            workspace.setDisable(false);
            btnworkspace.setDisable(false);
        } else {
            workspace.setDisable(true);
            btnworkspace.setDisable(true);
        }
        SettingsController.preferences.putBoolean(SAVE_ALL_FILES, value);
        notifyEvent();
    }

    private void setWorkspace(String wspace) {
        SettingsController.preferences.put(WORKSPACE, wspace);
        this.workspace.setText(wspace);
        notifyEvent();
    }

    private SettingsEvent getChange() {
        return new SettingsEvent(
                /*getAllowUbidots(),
                getUbidotsApiKey(),*/
                getAnimation(),
                getSaveAllFiles(),
                getWorkspace(),
                getInterval(),
                getSamples(),
                getFutureSample(),
                getFutureSampleInMS()
                //getSensonetProject()
        );
    }

    private void notifyEvent() {
        if (listener != null && listener.size() > 0) {
            listener.forEach((l) -> {
                l.onSettingsChanged(getChange());
            });
        }
    }

    public void addListener(SettingsChange st) {
        if (listener != null) {
            listener.add(st);
        }
        notifyEvent();
    }

    public void setCurrentFutureSampling() {
        boolean program = getFutureSample();
        cbxfuturesamples.setSelected(program);

        int[] x = getMsToHMS(getFutureSampleInMS());
        programHour.setValue(x[0]);
        programMinute.setValue(x[1]);
        programSecond.setValue(x[2]);
        if (program) {
            startTimer();
        }

    }

    public void setCurrentInterval(long interval) {

        int hora = (int) (interval / 3600000);
        long restoHora = interval % 3600000;
        int minutos = (int) (restoHora / 60000);
        long restoMinutos = restoHora % 60000;
        int segundos = (int) (restoMinutos / 1000);
        hour.setValue(hora);
        minute.setValue(minutos);
        second.setValue(segundos);
    }

    public void setCurrentSamples() {
        sample.setValue(getSamples());
    }

    /*private void setAllowAUbidots(Boolean newValue) {
        if (newValue) {
            apikey.setDisable(false);
        } else {
            apikey.setDisable(true);
        }
        SettingsController.preferences.putBoolean(USER_ALLOW_UBIDOTS, newValue);
        notifyEvent();
    }*/
    /**
     * Elimina espacios vacios y saltos de linea.
     */
    private static String sanitize(String string){
        return  string.replaceAll("\n", "").replaceAll(" ", "").trim();
    }

    public interface SettingsChange {

        public void onSettingsChanged(SettingsEvent event);
    }
    


}
