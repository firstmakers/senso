package cl.tide.fm.controller;

import cl.tide.fm.components.*;
import static cl.tide.fm.controller.ProgramController.getHourToMillisecond;
import static cl.tide.fm.controller.ProgramController.getMsToHMS;
import cl.tide.fm.device.*;
import cl.tide.fm.model.*;
import cl.tide.fm.tour.Tour;
import com.ubidots.*;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.*;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.hid4java.HidDevice;

import org.hid4java.HidException;

public class FXMLController implements Initializable, FmSensoListener, SettingsController.SettingsChange {

    FmSenso fmSenso;
    @FXML
    Node AnchorPane;
    @FXML
    protected VBox sensorContainer;
    @FXML
    protected TabPane tabPane;
    @FXML
    private ImageView status, logo;
    @FXML
    protected Text firmware;
    @FXML
    protected Text info;
    @FXML
    Button clearChart;
    private ArrayList<SensorView> sensors;
    private Timer programTimer;
    protected Control control;
    private Stage stage;
    private Boolean sampling = false;
    private SettingsController settings;
    private Boolean animation = true;

    //private ChartController mChart;
    private Tabcontroller mTab;
    private Timer mTimer;
    private boolean pause = true;
    private int totalSamples = 0;
    private long userInterval = 1000;
    private int userSamples = 100;
    private long userProgram = 0;
    private Boolean futureSampling;
    private Boolean closing = false;
    private UbidotsClient ubidots;

    FileManager fm;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        control = new Control();
        control.btnStart.setOnAction((ActionEvent event) -> {
            clickStart();
        });
        control.btnPause.setOnAction((ActionEvent event) -> {
            clickPause();
        });
        control.btnStop.setOnAction((ActionEvent event) -> {
            clickStop();
        });
        try {
            fmSenso = new FmSenso();
            fmSenso.addFmSensoListener(this);
            sensors = new ArrayList<>(6);
        } catch (HidException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        sensorContainer.getChildren().add(control);

        if (sensors.isEmpty() && fmSenso.isConnected()) {
            addInternalSensor();
        }
        setStatusDevice(fmSenso.getCurrentDevice());

        clearChart.setOnAction((ActionEvent event) -> {
            if (mTab != null) {
                mTab.clear();
            }
        });

        if (fmSenso.isConnected()) {
            configChart();
            fmSenso.start();
        }
        settings = new SettingsController(getStage());
        fm = new FileManager(sensors, this);
        settings.addListener(this);

        futureSampling = SettingsController.getFutureSample();

        configUbibots();

    }


    /*
     *Agrega los sensores internos de la tarjeta senso
     */

    private void configChart() {
        mTab = new Tabcontroller(tabPane, sensors);
    }

    public void addInternalSensor() {
        //add internal sensor
        Sensor s = fmSenso.getSensorManager().getInternalSensors().get(0);
        LigthView view = new LigthView(s);
        view.setID(s.getId());
        view.setCustomSerie(new CustomSeries(view.getName()));
        sensors.add(view);
        addView(view);
    }

    /*
     */
    public void close() {
        closing = true;
        fmSenso.stop();
        if (mTab != null) {
            mTab.stop();
        }
        cancelTimer();
    }

    @Override
    public void onClose() {
        //mChart.stop();
        clickStop();
        if(ubidots!=null)
            ubidots.close();
        firmware.setText("");
        System.out.println("Closed");
    }

    @Override
    public void onStart() {
        System.out.println("Start");
        Platform.runLater(() -> {
            if (fmSenso.isRunning()) {
                fmSenso.addFmSensoListener(this);
            }
        });
 
    }

    /**
     * Evento que comunica la conexión de la tarjeta.
     *
     * @param device Dispositivo.
     */
    @Override
    public void onAttachedDevice(HidDevice device) {
        setStatusDevice(device);
        addInternalSensor();
        System.out.println("New device attached");
        info("Se conectó un dispositivo", 2000);
        if (!fmSenso.isRunning()) {
            configChart();
            fmSenso.start();
        }
    }

    /**
     * Evento que comunica la desconexión del dispositivo
     *
     * @param device Dispositivo
     */
    @Override
    public void onDetachedDevice(HidDevice device) {
        setStatusDevice(device);
        Platform.runLater(() -> {
            mTab.closeAllTab();
            sensorContainer.getChildren().removeAll(sensors);
            sensors.clear();
            System.out.println("Current device Detached ");
            clear();
        });
        info("Se desconectó el dispositivo ", 2000);
    }

    /**
     * Maneja el estado del sensor, asigna indicadores visuales según el estado
     * del sensor.
     *
     * @param device Dispositivo
     */
    public void setStatusDevice(HidDevice device) {
        if (device != null) {
            status.setImage(new Image("/images/usb_detach.png"));
        }
        if (fmSenso.isConnected()) {
            status.setImage(new Image("/images/usb_attach.png"));
            //deviceInfo.setStatus("Conectado");
        } else {
            status.setImage(new Image("/images/usb_detach.png"));
            //deviceInfo.setStatus("Desconectado");
        }
    }

    /**
     * Interface del dispositivo que notifica cada vez que se desconecta un
     * sensor externo.
     *
     * @param removedList listado con los sensores desconectados.
     */
    @Override
    public void onSensorDetach(final ArrayList<Sensor> removedList) {
        Platform.runLater(()->{
        String id;
        for (Sensor s : removedList) {
            id = s.getId();
            for (int i = 0; i < sensors.size(); i++) {
                if (sensors.get(i).getID().equals(id)) {
                    removeView(sensors.get(i));
                    mTab.removeSensorview(sensors.get(i));
                    sensors.remove(i);
                }
            }
        }
        
        
        });
        
    }

    /**
     * Interface del dispositivo que notifica cada vez que se conecta un sensor
     * externo.
     *
     * @param addedList listado con los nuevos sensores conectados.
     */
    @Override
    public void onSensorAttach(ArrayList<Sensor> addedList) {
       Platform.runLater(()->{
        SensorView sensor;
        for (Sensor s : addedList) {
            if (s.getProfile() == (TypeSensor.TEMPERATURE)) {
                sensor = new TemperatureView(s);
            } else if (s.getProfile() == (TypeSensor.LIGTH)) {
                sensor = new LigthView(s);
            } else if (s.getProfile() == (TypeSensor.HUMIDITY)) {
                sensor = new HumidityView(s);
            } else {
                sensor = new UnknownView(s);
            }
            sensor.setID(s.getId());
            sensor.setAnimation(animation);
            sensor.setCustomSerie(new CustomSeries(sensor.getName()));
            sensors.add(sensor);
            addView(sensor);
            mTab.addSensorview(sensor);
        }
       });
        
    }

    /**
     * Muestra o Elimina un sensor del gráfico.
     *
     * @param view Sensor
     * @param status Estado del sensor true / Visible, false / No visible
     */
    private void setChanges(SensorView view, boolean status) {
        if (status) {
            mTab.addSensorview(view);
        } else {
            mTab.removeSensorview(view);
        }
    }

    /**
     * Agrega un sensor a la interfaz gráfica.
     */
    private void addView(final Node node) {
        Platform.runLater(() -> {
            final SensorView view = (SensorView) node;
            view.addListener((boolean visible, CustomSeries customSerie, Boolean status) -> {
                setChanges(view, status);
            });
            synchronized (sensorContainer) {
                sensorContainer.getChildren().add(node);
                //mChart.addSerie(view.getCustomSerie().getSerie());
                if (animation) {
                    Timeline fadein = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 0.0)),
                            new KeyFrame(new Duration(100), new KeyValue(node.opacityProperty(), 1.0)));
                    fadein.play();
                }
                view.setSerieColor();
            }
        });
    }

    /**
     * Elimina un sensor de la interfaz gráfica.
     */
    private void removeView(final Node node) {
        Platform.runLater(() -> {
            SensorView view = (SensorView) node;
            synchronized (sensorContainer) {
                //mChart.removeSerie(view.getCustomSerie().getSerie());
                if(animation){
                Timeline fade = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 1.0)),
                        new KeyFrame(new Duration(200), e -> {
                            sensorContainer.getChildren().remove(node);
                        }, new KeyValue(node.opacityProperty(), 0.0)));
                fade.play();
                }else{
                    sensorContainer.getChildren().remove(node);
                }
            }
        });
    }

    /**
     * Inicia una medición, si está pausada la continúa. Si la medición anterior
     * no ha sido guardada borra todo su contenido.
     */
    private void clickStart() {  
        if (totalSamples > 0 && !pause) {
            clear();
        }
        sampling = true;
        pause = false;
        fm.newWorkbook(getHeader());
        if(ubidots!= null && !pause)
            ubidots.tryToConnect(sensors);
        else 
            System.err.println(ubidots);
        if (fmSenso.isRunning() && userInterval > 999 && userSamples > 0) {
            mTab.start(userInterval);
            /*control.btnSave.setDisable(true);*/
            control.btnStart.setDisable(true);
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (totalSamples >= userSamples) {
                        clickStop();
                        return;
                    }
                    totalSamples++;
                    updateUI();
                    fm.streamRow(getSamples(), null);
                   
                }
            }, 0, userInterval);
        }
        System.gc();
    }
    
     /**
     * Pausa una medición que está en curso.
     */
    private void clickPause() {
        control.btnStart.setDisable(false);
        sampling = false;
        //control.btnSave.setDisable(false);
        mTab.stop();
        if (mTimer != null) {
            mTimer.cancel();
            this.pause = true;
            mTimer = null;
        }
        if(totalSamples > 0)
            info("La medición está pausada, haga clic en iniciar para reanudar", 4000);
    }

    /**
     * Detiene una medición que está en curso.
     */
    private void clickStop() {
        sampling = false;
        control.btnStart.setDisable(false);
        pause = false;
        mTab.stop();
        fm.automaticSave();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if(totalSamples>0)
            info("La medición ha finalizado, haga clic en archivo y luego en guardar.", 5000);
        if(ubidots != null)
            ubidots.stopUpdate();
    }


    /**
     * Muestra un diálogo cuando existe una medición sin guardar.
     */
    private void showDialog() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Senso");
        alert.setHeaderText("Atención, hay datos de una medición disponible");
        alert.setContentText("¿Desea guardar?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            System.out.println("calling save method");
            clickSave(stage);
        } else {
            System.out.println("calling clear method");
            clear();
        }
    }

    /**
     * Obtiene la cabecera con los nombres de los sensores conectados.
     *
     * @return Devuelve una arreglo con los nombres de los sensores.
     */
    private String[] getHeader() {
        String[] header = new String[sensors.size() + 1];
        header[0] = "Hora";

        for (int i = 1; i < header.length; i++) {
            header[i] = sensors.get(i - 1).getName();
        }
        return header;
    }

    /**
     * Obtiene un fila con los datos actuales de los sensores conectados.
     *
     * @return Arreglo con la hora en que se tomó la muestra y los datos de los
     * sensores conectados.
     */
    private Object[] getSamples() {
        final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date d = new Date();
        String date = dateFormat.format(d);
        Object[] ob = new Object[sensors.size() + 1];

        int count = ob.length;
        for (int i = 0; i < count; i++) {
            ob[i] = date;
            int cellIndex = 1;
            for (SensorView v : sensors) {
                ob[cellIndex] = v.getSensor().getValue();
                cellIndex++;
            }
        }
        return ob;
    }

    /**
     * Cambia los texto que indican la medición actual y la barra de progreso.
     */
    private void updateUI() {
        Platform.runLater(() -> {
            double n = userSamples;
            // System.out.println("updating ui "+totalSamples+"/"+n  +(totalSamples/n)+ " %");
            control.sampleText.setText(totalSamples + "/" + (int) n);
            control.progressBar.setProgress(totalSamples / n);
            info("Muestra "+totalSamples + " de " + (int) n, 0);
        });
    }


    /**
     * Abre una ventana para guardar una medición.
     *
     * @param win Ventana principal que contiene la app.
     */
    private void clickSave(Window win) {
        System.out.println("Clicked save");
        if (totalSamples > 0) {
            //save & clear
            FileChooser fileChooser = new FileChooser();
            //Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel file (*.xlsx)", "*.xlsx");
            fileChooser.getExtensionFilters().add(extFilter);
            //Show save file dialog
            File file = fileChooser.showSaveDialog(win);
            if (file != null) {
                fm.save(file.getAbsolutePath());
                clear();
            }
        }
        System.gc();
    }

    /**
     * Reestablece la barra de progreso y los textos que muestra el avance de
     * las mediciones.
     */
    private void clear() {
        totalSamples = 0;
        mTab.clear();
        Platform.runLater(() -> {
            control.sampleText.setText("0/0");
            control.progressBar.setProgress(0.0);
        });
    }
    
    
    public Boolean getAnimation() {
        return animation;
    }

    public void setAnimation(Boolean animation) {
        this.animation = animation;
        sensors.forEach((s)->{s.setAnimation(animation);});
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }


    /**
     * Asigna la versión del firmware de la tarjeta conectada.
     *
     * @param firm número de versión del firmaware.
     */
    @Override
    public void onFirmwareChange(String firm) {
        Platform.runLater(() -> {
            firmware.setText("versión: " + firm);
        });

    }

    /**
     * Abre una ventana que muestra la ayuda.
     *
     * @param win Contenedor principal
     */
    public void showTour(Window win) {
        Tour t = new Tour(win);
        t.showTour();
    }

    ///------ACCIONES DEL MENÚ PRINCIPAL----///
    /**
     * Items del menú muestras.
     */
    @FXML
    private CheckMenuItem menuOneSecond,
            menuTenSeconds,
            menuThirtySeconds,
            menuOneMin,
            menuTenThousandSamples,
            menuOneThousandSamples,
            menuHundredSamples;

    @FXML
    private MenuItem menuSettings;
    
    /**
     * Abre La ventana de configuración.
     * @param event evento del menú configuración.
     */
    public void openSettings(ActionEvent event){
       settings.show();
       settings.setSelected(SettingsController.GENERAL_SETTINGS);
       /*settings.setCurrentInterval(userInterval);
       settings.setCurrentFutureSampling();
       settings.setCurrentSamples();*/

    }

    /**
     * Abre una venta donde se obtiene una ruta para guardar una medición.
     * @param event Evento onAction del menú guardar
     */
    public void handleMenuSave(ActionEvent event) {
        clickSave(stage);
    }

    /**
     * Muestra una ventana con la ayuda de la app.
     *
     * @param event Evento onAction del menú ayuda.
     */
    public void handleMenuHelp(ActionEvent event) {
        showTour(stage);
    }

    /**
     * Maneja los checkbox del menú de los intervalos.
     *
     * @param event Evento
     */
    public void handleMenuInterval(ActionEvent event) {
        Object menuItem = event.getSource();
        if (menuItem instanceof CheckMenuItem) {
            switch (((CheckMenuItem) menuItem).getId()) {
                case "menuOneSecond":
                    settings.setInterval(1000);
                    menuOneMin.setSelected(false);
                    menuTenSeconds.setSelected(false);
                    menuThirtySeconds.setSelected(false);
                    break;
                case "menuTenSeconds":
                    settings.setInterval(10000);
                    menuOneMin.setSelected(false);
                    menuOneSecond.setSelected(false);
                    menuThirtySeconds.setSelected(false);
                    break;
                case "menuThirtySeconds":
                    settings.setInterval(30000);
                    menuOneMin.setSelected(false);
                    menuTenSeconds.setSelected(false);
                    menuOneSecond.setSelected(false);
                    break;

                case "menuOneMin":
                    settings.setInterval(60000);
                    menuOneSecond.setSelected(false);
                    menuTenSeconds.setSelected(false);
                    menuThirtySeconds.setSelected(false);
                    break;

                default:
                    break;
            }
        } else if (menuItem instanceof MenuItem) {
            setUnchecked(false);
            //abrir configuración personalizada
            showSettingInterval();
        }

    }

    /**
     * Maneja los checkbox del menú de muestras.
     *
     * @param event Evento
     */
    public void handleMenuSample(ActionEvent event) {
        Object menuItem = event.getSource();
        if (menuItem instanceof CheckMenuItem) {
            switch (((CheckMenuItem) menuItem).getId()) {
                case "menuHundredSamples":
                    menuTenThousandSamples.setSelected(false);
                    menuOneThousandSamples.setSelected(false);
                    settings.setSamples(100);

                    break;
                case "menuOneThousandSamples":
                    menuTenThousandSamples.setSelected(false);
                    menuHundredSamples.setSelected(false);
                    settings.setSamples(1000);
                    break;
                case "menuTenThousandSamples":
                    menuOneThousandSamples.setSelected(false);
                    menuHundredSamples.setSelected(false);
                    settings.setSamples(10000);
                    break;
                default:
                    break;
            }
        } else if (menuItem instanceof MenuItem) {
            setUnchecked(true);
            //abrir configuración personalizada
            showSettingSampling();
        }
    }

    /**
     * Asigna no seleccionado al menú de muestras.
     */
    public void setUnchecked(boolean b) {
        if (b) {
            menuHundredSamples.setSelected(false);
            menuTenThousandSamples.setSelected(false);
            menuOneThousandSamples.setSelected(false);
        }
        menuOneSecond.setSelected(false);
        menuTenSeconds.setSelected(false);
        menuThirtySeconds.setSelected(false);
        menuOneMin.setSelected(false);
    }

    /**
     * Abre una ventana donde se puede configurar el intervalo de medición y el
     * número de muestras.
     */
    private void showSettingSampling() {
        settings.show();
        settings.setSelected(SettingsController.SAMPLE_SETTINGS);
        
    }

    /**
     * Abre una ventana donde se puede configurar el intervalo de medición y el
     * número de muestras.
     */
    private void showSettingInterval() {
        settings.show();
        settings.setSelected(SettingsController.INTERVAL_SETTINGS);
        
    }

    /**
     * Abre la ventana para programar una medición futura.
     *
     * @param event Evento
     */
    public void openProgram(ActionEvent event) {
        settings.show();
        settings.setSelected(SettingsController.PROGRAM_SETTINGS);
    }
    private  void cancelTimer(){
        if (programTimer != null) {
            programTimer.purge();
            programTimer.cancel();
            programTimer = null;
        }
    }
    private void initTimerFutureSmapling() {
        if (programTimer != null) {
            programTimer.cancel();
            programTimer = null;
        }
        programTimer = new Timer();
        programTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (fmSenso.isConnected()) {
                    long current = getHourToMillisecond(LocalTime.now().getHour(),
                            LocalTime.now().getMinute(),
                            LocalTime.now().getSecond());
                    int[] remaining = getMsToHMS(userProgram - current);
                    String hora = remaining[0] < 10 ? "0" + remaining[0] : remaining[0] + "";
                    String minutos = remaining[1] < 10 ? "0" + remaining[1] : remaining[1] + "";
                    String segundos = remaining[2] < 10 ? "0" + remaining[2] : remaining[2] + "";
                    info("La medición comenzará en " + hora + ":" + minutos + ":" + segundos, 0);
                    if (userProgram <= current) {
                        //
                        clickStart();
                        settings.setFutureSample(false);
                    }
                }
            }
        }, 0, 1000);
    }

    /**
     * Muestra un mensaje.
     * @param msg mensaje
     * @param duration duración del mensaje en milisegundos
     */
    public void info(String msg, long duration) {
        if(!closing){
        Platform.runLater(() -> {
            info.setText(msg);
            if(info.getOpacity()== 0.0) {
                if(animation){
                    FadeTransition fadein = new FadeTransition(new Duration(300), info);
                    fadein.setFromValue(0.0);
                    fadein.setToValue(1.0);
                    fadein.play();
                }
                else
                    info.setOpacity(1.0);
            }
            if (duration > 10) {
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (animation) {
                            FadeTransition fade = new FadeTransition(new Duration(300), info);
                            fade.setFromValue(1.0);
                            fade.setToValue(0.0);
                            fade.play();   
                        }
                        t.purge();
                        t.cancel();
                        info.setText("");
                    }
                }, duration);
            }
        });
        }
        else{
            System.out.print("closing "+ true);
        }
    }

    /**
     * Asigna el interavlo de tiempo en que se toman las muestras.
     */
    private void setInterval(long i) {
        userInterval = i;
        if (mTab != null) {
            mTab.setInterval(i);
        }
        if (mTimer != null && !pause) {
            clickPause();
            clickStart();
        }
    }

    /**
     * Asigna la cantidad de muestras definidas por el usuario.
     */
    private void setUserSample(int s) {
        userSamples = s;
    }

    /**
     * @param event evento que contiene los cambios
     */
    @Override
    public void onSettingsChanged(SettingsEvent event) {

        if (event.interval != userInterval) {
            setInterval(event.interval);
        }//

        if (event.samples != userSamples) {
            setUserSample(event.samples);
        }//

        if (!Objects.equals(event.isAnimationAvailable, animation)) {
            setAnimation(event.isAnimationAvailable);

        }//

        if (!Objects.equals(event.isSaveAllAvailable, fm.saveAll)) {
            fm.saveAll = event.isAnimationAvailable;
            if (event.isSaveAllAvailable) {
                if (!event.currentWorkspace.equals(fm.workspace)) {
                    fm.workspace = event.currentWorkspace;
                }
            }
        }//

        futureSampling = event.isAvailableFutureSampling;
        if (futureSampling) {
            userProgram = event.futureSamplingMs-1000;
            initTimerFutureSmapling();
        } 
        else{
            if(programTimer != null){
                cancelTimer();
                info("Se ha cancelado la programación de la medición", 2000);
            }
        }
        
        if(event.isAllowUbidots && ubidots == null){
            configUbibots();
        }

    }

    private void configUbibots() {
        if(ubidots!=null)
            ubidots.close();
        ubidots = null;
        if (SettingsController.getAllowUbidots()) {
            ubidots = new UbidotsClient(SettingsController.getUbidotsApiKey());
            ubidots.addListener(new UbidotsClient.UbidotsEvent() {
                @Override
                public void isOnline(boolean status) {
                    System.out.println("Ubidots online...");
                    if(status)
                        info("El servidor ubidots está online", 1000);
                    else
                        info(UbidotsClient.NET_ERROR, 1000);
                }

                @Override
                public void onErrorOcurred(String error) {
                    System.out.println(error);
                    if(error.equals(UbidotsClient.NET_ERROR)){
                        info(error,0);
                        ubidots.stopUpdate();
                    }       
                }

                @Override
                public void onStop() {
                    System.out.println("onStop");
                }
            });
        }
    }
}
