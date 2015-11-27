package cl.tide.fm.controller;

import cl.tide.fm.components.*;
import cl.tide.fm.device.*;
import cl.tide.fm.model.*;
import cl.tide.fm.tour.Tour;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.*;
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
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import org.hid4java.HidDevice;

import org.hid4java.HidException;

public class FXMLController implements Initializable, FmSensoListener {

    FmSenso fmSenso;
    @FXML Node AnchorPane;
    @FXML protected VBox sensorContainer;
    @FXML protected TabPane tabPane;
    @FXML private ImageView status, logo; 
    @FXML protected Text firmware;
    @FXML Button btnHelp;
    private ArrayList<SensorView> sensors;
   
    protected Control control;
    
    
    
    //private ChartController mChart;
    private Tabcontroller mTab;
    private Timer mTimer;
    private boolean pause = true;
    private int totalSamples = 0;

    FileManager fm;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
       
        //deviceInfo = new DeviceInfo();
        control = new Control();
        control.samples.setNumber(new BigDecimal(300));
        control.interval.setNumber(BigDecimal.ONE);    
        
        control.btnStart.setOnAction((ActionEvent event) -> {
            clickStart();
        });
        control.btnPause.setOnAction((ActionEvent event) -> {
            clickPause();
        });
        control.btnStop.setOnAction((ActionEvent event) -> {
            clickStop();
        });
        control.btnSave.setOnAction((ActionEvent event) -> {
            clickSave(((Node) event.getTarget()).getScene().getWindow());
        });
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
        
        btnHelp.setOnAction((ActionEvent event)->{
            showTour(btnHelp.getScene().getWindow());
        });
        if (fmSenso.isConnected()) {
            configChart();
            fmSenso.start();
        }
      
    }
    /*
     *Agrega los sensores internos de la tarjeta senso
     */
    private void configChart(){
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
        if(mTab != null)
            mTab.stop();
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

    @Override
    public void onAttachedDevice(HidDevice device) {
        setStatusDevice(device);
        addInternalSensor();
        System.out.println("New device attached");
        if (!fmSenso.isRunning()) {
            configChart();
            fmSenso.start();  
        }
    }

    @Override
    public void onDetachedDevice(HidDevice device) {
        setStatusDevice(device);
        Platform.runLater(() -> {
            mTab.closeAllTab();
            sensorContainer.getChildren().removeAll(sensors);
            sensors.clear();
            System.out.println("Current device Detached ");
        });
    }

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

    private void setChanges(SensorView view, boolean status) {
        if (status) {
            mTab.addSensorview(view);
        } else {
            mTab.removeSensorview(view);
        }
    }

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

    private void clickStart() {
        System.out.println("Clicked Start");
        if (totalSamples > 0) {
            showDialog();
            return;
        }
        long userInterval = control.interval.getNumber().intValue() * 1000;
        int smp = control.samples.getNumber().intValue();
        fm.setHeader(getHeader());
        if (fmSenso.isRunning() && userInterval > 999 && smp > 0) {
            mTab.start();
            control.btnSave.setDisable(true);
            control.btnStart.setDisable(true);
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    if (totalSamples >= control.samples.getNumber().intValue()) {
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

    private void showDialog() {

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Senso");
        alert.setHeaderText("Atención, hay datos de una medición disponible");
        alert.setContentText("¿Desea guardar?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            System.out.println("calling save method");
            clickSave(control.btnSave.getScene().getWindow());
        } else {
            System.out.println("calling clear method");
            clear();
        }
    }

    private String[] getHeader() {
        String[] header = new String[sensors.size() + 1];
        header[0] = "Hora";
      
        for (int i = 1; i < header.length; i++) {
            header[i] = sensors.get(i - 1).getName();
        }
        return header;
    }

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

    private void updateUI() {

        Platform.runLater(() -> {
            double n = control.samples.getNumber().intValue();
            // System.out.println("updating ui "+totalSamples+"/"+n  +(totalSamples/n)+ " %");
            control.sampleText.setText(totalSamples + "/" + (int) n);
            control.progressBar.setProgress(totalSamples / n);
        });
    }

    private void clickPause() {
        System.out.println("Clicked Pause");
        control.btnStart.setDisable(false);
        control.btnSave.setDisable(false);
        mTab.stop();
        if (mTimer != null) {
            mTimer.cancel();
            this.pause = true;
            mTimer = null;
        }
    }

    private void clickStop() {
        System.out.println("Clicked Stop");
        control.btnSave.setDisable(false);
        control.btnStart.setDisable(false);
        mTab.stop();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

    }

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

    private void clear() {
        totalSamples = 0;
        Platform.runLater(() -> {
            control.sampleText.setText("0/0");
            control.progressBar.setProgress(0.0);
        });
    }

    @Override
    public void onFirmwareChange(String firm) {
        Platform.runLater(()->{
            firmware.setText("versión: "+firm);
        });   
        
    }
    
    public void showTour(Window win){
        Tour t = new Tour(win);
        t.showTour();
    }

}
