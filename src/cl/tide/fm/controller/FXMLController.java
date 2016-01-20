package cl.tide.fm.controller;

import cl.tide.fm.components.*;
import static cl.tide.fm.controller.ProgramController.getHourToMillisecond;
import static cl.tide.fm.controller.ProgramController.getMsToHMS;
import cl.tide.fm.device.*;
import cl.tide.fm.model.*;
import cl.tide.fm.tour.Tour;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.hid4java.HidDevice;

import org.hid4java.HidException;

public class FXMLController implements Initializable, FmSensoListener {

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
    @FXML Button clearChart;
    private ArrayList<SensorView> sensors;
    private Timer programTimer;
    protected Control control;
    private Stage stage;
    private boolean sampling = false;

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    //private ChartController mChart;
    private Tabcontroller mTab;
    private Timer mTimer;
    private boolean pause = true;
    private int totalSamples = 0;
    private long userInterval = 1000;
    private int userSamples = 100;
    private long userProgram = -1;

    FileManager fm;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //deviceInfo = new DeviceInfo();
        control = new Control();
        /*control.samples.setNumber(new BigDecimal(300));
         control.interval.setNumber(BigDecimal.ONE);    */

        control.btnStart.setOnAction((ActionEvent event) -> {
            clickStart();
        });
        control.btnPause.setOnAction((ActionEvent event) -> {
            clickPause();
        });
        control.btnStop.setOnAction((ActionEvent event) -> {
            clickStop();
        });
        /*control.btnSave.setOnAction((ActionEvent event) -> {
         clickSave(((Node) event.getTarget()).getScene().getWindow());
         });*/
        try {
            fmSenso = new FmSenso();
            fmSenso.addFmSensoListener(this);
            sensors = new ArrayList<>();
        } catch (HidException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        sensorContainer.getChildren().add(control);

        if (sensors.isEmpty() && fmSenso.isConnected()) {
            addInternalSensor();
        }
        setStatusDevice(fmSenso.getCurrentDevice());
        fm = new FileManager(sensors);

        clearChart.setOnAction((ActionEvent event) -> {
            if(mTab!= null)
                mTab.clear();
        });
        
        if (fmSenso.isConnected()) {
            configChart();
            fmSenso.start();
        }
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
        fmSenso.stop();
        if (mTab != null) {
            mTab.stop();
        }
        if (programTimer != null) {
            programTimer.cancel();
        }
    }

    @Override
    public void onClose() {
        System.out.println("Closed");
        //mChart.stop();
        clickStop();
        firmware.setText("");
        //mTab.stopCapture();
    }

    @Override
    public void onStart() {
        System.out.println("Start");
        Platform.runLater(() -> {

            if (fmSenso.isRunning()) {
                fmSenso.addFmSensoListener(this);

            }
        });
        //mTab.startCapture();
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
        System.out.println(removedList.size() + " sensor removed");
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
    }

    /**
     * Interface del dispositivo que notifica cada vez que se conecta un sensor
     * externo.
     *
     * @param addedList listado con los nuevos sensores conectados.
     */
    @Override
    public void onSensorAttach(ArrayList<Sensor> addedList) {

        System.out.println(addedList.size() + " sensor attached");
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
            sensor.setCustomSerie(new CustomSeries(sensor.getName()));
            sensors.add(sensor);
            addView(sensor);
            mTab.addSensorview(sensor);

        }
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
                Timeline fadein = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 0.0)),
                        new KeyFrame(new Duration(100), new KeyValue(node.opacityProperty(), 1.0)));
                fadein.play();
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
                Timeline fade = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 1.0)),
                        new KeyFrame(new Duration(200), e -> {
                            sensorContainer.getChildren().remove(node);
                        }, new KeyValue(node.opacityProperty(), 0.0)));
                fade.play();
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
        fm.setHeader(getHeader());
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
                    fm.streamRow(getSamples(), null);
                    updateUI();
                }
            }, 0, userInterval);
        }
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

        for (int i = 0; i < ob.length; i++) {
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
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if(totalSamples>0)
            info("La medición ha finalizado, haga clic en archivo y luego en guardar.", 5000);
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
                fm.flush(file.getAbsolutePath());
                clear();
            }
        }
    }

    /**
     * Reestablece la barra de progreso y los textos que muestra el avance de
     * las mediciones.
     */
    private void clear() {
        totalSamples = 0;
        Platform.runLater(() -> {
            control.sampleText.setText("0/0");
            control.progressBar.setProgress(0.0);
        });
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
    private MenuItem menuCustomInterval,
            menuCustomSample;

    /**
     * Abre una venta donde se obtiene una ruta para guardar una medición.
     *
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
                    setInterval(1000);
                    menuOneMin.setSelected(false);
                    menuTenSeconds.setSelected(false);
                    menuThirtySeconds.setSelected(false);
                    break;
                case "menuTenSeconds":
                    setInterval(10000);
                    menuOneMin.setSelected(false);
                    menuOneSecond.setSelected(false);
                    menuThirtySeconds.setSelected(false);
                    break;
                case "menuThirtySeconds":
                    setInterval(30000);
                    menuOneMin.setSelected(false);
                    menuTenSeconds.setSelected(false);
                    menuOneSecond.setSelected(false);
                    break;

                case "menuOneMin":
                    setInterval(60000);
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
                    setUserSample(100);

                    break;
                case "menuOneThousandSamples":
                    menuTenThousandSamples.setSelected(false);
                    menuHundredSamples.setSelected(false);
                    setUserSample(1000);
                    break;
                case "menuTenThousandSamples":
                    menuOneThousandSamples.setSelected(false);
                    menuHundredSamples.setSelected(false);
                    setUserSample(10000);
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
        if(b){
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
        Stage dialog = new Stage();
        //ventana modal
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("configuración de muestras");
        SamplingController spc = new SamplingController();
        spc.setCurrentSetting((int)userSamples);

        Scene dialogScene = new Scene(spc);
        dialogScene.getStylesheets().add("/styles/Styles.css");
        dialog.setScene(dialogScene);
        dialog.setMinWidth(400.0);
        dialog.setMinHeight(180);
        dialog.setMaxWidth(400);
        dialog.setMaxHeight(180);
        dialog.show();
        spc.cancel.setOnAction((ActionEvent event) -> {
            dialog.close();
        });
        spc.addListener((int samples) -> {
            setUserSample(samples);
            dialog.close();
        });
    }
 /**
     * Abre una ventana donde se puede configurar el intervalo de medición y el
     * número de muestras.
     */
    private void showSettingInterval() {
        Stage dialog = new Stage();
        //ventana modal
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("configuración");
        IntervalController stc = new IntervalController();
        stc.setCurrentSetting(userInterval);

        Scene dialogScene = new Scene(stc);
        dialogScene.getStylesheets().add("/styles/Styles.css");
        dialog.setScene(dialogScene);
        dialog.setMinWidth(400.0);
        dialog.setMinHeight(180);
        dialog.setMaxWidth(400);
        dialog.setMaxHeight(180);
        dialog.show();
        stc.cancel.setOnAction((ActionEvent event) -> {
            dialog.close();
        });
        stc.addListener((long interval) -> {
            setInterval(interval);
            dialog.close();
        });
    }
    /**
     * Abre la ventana para programar una medición futura.
     *
     * @param event Evento
     */
    public void openProgram(ActionEvent event) {
        Stage dialog = new Stage();
        //ventana modal
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Programar medición");
        ProgramController pgc = new ProgramController();
        Scene dialogScene = new Scene(pgc);
        dialog.setScene(dialogScene);
        dialogScene.getStylesheets().add("/styles/Styles.css");
        dialog.show();

        if (userProgram > 0) {
            pgc.setCurrentProgram(userProgram);
        }
        pgc.cancel.setOnAction((ActionEvent e) -> {
            if (programTimer != null) {
                programTimer.cancel();
                userProgram = -1;
                info("Se ha cancelado la programación de la medición", 2000);
            }
            pgc.cancelTimer();
            dialog.close();
        });
        pgc.ok.setOnAction((ActionEvent e) -> {
            pgc.cancelTimer();
            dialog.close();
        });
        pgc.addListener(programEvent);
    }

    /**
     * Listener de la ventana de programación, maneja el evento para programar
     * el inicio de cada medición.
     *
     * @param programInMs tiempo en milisegundos.
     */
    ProgramController.ProgramListener programEvent = (long programInMS) -> {
        userProgram = programInMS;
        if (programTimer != null) {
            programTimer.cancel();
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
                    if (current >= userProgram) {
                        //
                        clickStart();
                        userProgram = -1;
                        programTimer.cancel();
                    }
                }
            }
        }, 0, 1000);
    };

    /**
     * Muestra un mensaje.
     *
     * @param msg mensaje
     * @param duration duración del mensaje en milisegundos
     */
    private void info(String msg, long duration) {
        Platform.runLater(() -> {
            info.setText(msg);
            if (info.getOpacity() == 0.0) {
                FadeTransition fadein = new FadeTransition(new Duration(100), info);
                fadein.setFromValue(0.0);
                fadein.setToValue(1.0);
                fadein.play();
            }
            if (duration > 10) {
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        FadeTransition fade = new FadeTransition(new Duration(200), info);
                        fade.setFromValue(1.0);
                        fade.setToValue(0.0);
                        fade.play();
                        t.cancel();
                        t.purge();
                    }
                }, duration);
            }
        });
    }

    /**
     * Asigna el interavlo de tiempo en que se toman las muestras.
     */
    private void setInterval(long i) {
        userInterval = i;
        mTab.setInterval(i);
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
}
