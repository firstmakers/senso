package cl.tide.fm.controller;

import cl.tide.fm.components.*;
import cl.tide.fm.device.*;
import cl.tide.fm.model.*;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import org.hid4java.HidDevice;

import org.hid4java.HidException;

public class FXMLController implements Initializable, FmSensoListener {

    FmSenso fmSenso;
    @FXML
    Node AnchorPane;
    @FXML
    private VBox sensorContainer;
    private ArrayList<SensorView> ExternalSensorView;
    private ArrayList<SensorView> InternalSensorView;
    private DeviceInfo deviceInfo;
    private Control control;
    @FXML
    private LineChart lineChart;
    //@FXML private TableView tableView; 
    private ChartController mChart;
    //private TableViewController mTab;
    private Timer mTimer;
    private boolean pause = true;
    private int totalSamples = 0;

    FileManager fm;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        deviceInfo = new DeviceInfo();
        control = new Control();
        control.samples.setNumber(new BigDecimal(300));
        control.interval.setNumber(BigDecimal.ONE);
        control.btnStart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clickStart();
            }
        });
        control.btnPause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clickPause();
            }
        });
        control.btnStop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clickStop();
            }
        });
        control.btnSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clickSave(((Node) event.getTarget()).getScene().getWindow());
            }
        });
        try {
            fmSenso = new FmSenso();
            fmSenso.addFmSensoListener(this);
            ExternalSensorView = new ArrayList<>();
            InternalSensorView = new ArrayList<>();

        } catch (HidException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        //sensorContainer.getChildren().add(deviceInfo);
        sensorContainer.getChildren().add(control);

        mChart = new ChartController(lineChart);
        mChart.setExternalSensors(ExternalSensorView);
        mChart.setInternalSensor(InternalSensorView);
        //mTab = new TableViewController(tableView);
        //mTab.setViews(sensorView);

        if (fmSenso.isConnected()) {
            fmSenso.start();
        }
        if (InternalSensorView.isEmpty() && fmSenso.isConnected()) {
            addInternalSensor();
        }
        setStatusDevice(fmSenso.getCurrentDevice());
        fm = new FileManager(ExternalSensorView);
    }
    /*
     *Agrega los sensores internos de la tarjeta senso
     */

    public void addInternalSensor() {
        //add internal sensor
        Sensor s = fmSenso.getSensorManager().getInternalSensors().get(0);
        LightView view = new LightView(s);
        view.setID(s.getId());
        view.setCustomSerie(new CustomSeries(view.getName()));
        InternalSensorView.add(view);
        addView(view);
    }

    /*
     */
    public void close() {
        fmSenso.stop();
    }

    @Override
    public void onClose() {
        System.out.println("Closed");
        mChart.stop();
        clickStop();
        //mTab.stopCapture();
    }

    @Override
    public void onStart() {
        System.out.println("Start ");
        mChart.start();
        //mTab.startCapture();
    }

    @Override
    public void onAttachedDevice(HidDevice device) {
        setStatusDevice(device);
        addInternalSensor();
        System.out.println("New device attached");
        if (!fmSenso.isRunning()) {
            fmSenso.start();
        }
    }

    @Override
    public void onDetachedDevice(HidDevice device) {
        setStatusDevice(device);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                sensorContainer.getChildren().removeAll(ExternalSensorView);
                sensorContainer.getChildren().removeAll(InternalSensorView);
                ExternalSensorView.clear();
                InternalSensorView.clear();
                System.out.println("Current device Detached ");
            }
        });
    }

    public void setStatusDevice(HidDevice device) {
        if (device != null) {
            deviceInfo.setName(device.getProduct());
            deviceInfo.setFirmware(device.getManufacturer());
        }
        if (fmSenso.isConnected()) {
            deviceInfo.setIcon(new Image("/images/usb_attach.png"));
            deviceInfo.setStatus("Conectado");
        } else {
            deviceInfo.setIcon(new Image("/images/usb_detach.png"));
            deviceInfo.setStatus("Desconectado");
        }
    }

    @Override
    public void onSensorDetach(final ArrayList<Sensor> removedList) {
        System.out.println(removedList.size() + " sensor removed");
        String id;
        for (Sensor s : removedList) {
            id = s.getId();
            for (int i = 0; i < ExternalSensorView.size(); i++) {
                if (ExternalSensorView.get(i).getID().equals(id)) {
                    removeView(ExternalSensorView.get(i));
                    ExternalSensorView.remove(i);
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
                sensor = new LightView(s);
            } else if (s.getProfile() == (TypeSensor.HUMIDITY)) {
                sensor = new HumidityView(s);
            } else {
                sensor = new UnknownView(s);
            }
            sensor.setID(s.getId());
            sensor.setCustomSerie(new CustomSeries(sensor.getName()));
            ExternalSensorView.add(sensor);
            addView(sensor);

        }
    }

    private void setChanges(SensorView view, boolean status) {
        if (status) {
            mChart.addSerie(view.getCustomSerie().getSerie());
        } else {
            mChart.removeSerie(view.getCustomSerie().getSerie());
        }
    }

    private void addView(final Node node) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                final SensorView view = (SensorView) node;
                view.addListener(new ViewChanged() {
                    @Override
                    public void changed(boolean visible, CustomSeries customSerie, Boolean status) {
                        setChanges(view, status);
                    }
                });
                synchronized (FXMLController.this) {
                    sensorContainer.getChildren().add(node);
                    mChart.addSerie(view.getCustomSerie().getSerie());
                    Timeline fadein = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 0.0)),
                            new KeyFrame(new Duration(300), new KeyValue(node.opacityProperty(), 1.0)));
                    view.setSerieColor();
                    fadein.play();
                }
            }
        });
    }

    private void removeView(final Node node) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                SensorView view = (SensorView) node;
                synchronized (FXMLController.this) {
                    mChart.removeSerie(view.getCustomSerie().getSerie());
                    Timeline fade = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 1.0)),
                            new KeyFrame(new Duration(600), new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            sensorContainer.getChildren().remove(node);
                        }
                    }, new KeyValue(node.opacityProperty(), 0.0)));
                    fade.play();               
                }
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
        alert.setHeaderText("Atención, hay una medición disponible");
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
        String[] header = new String[ExternalSensorView.size() + InternalSensorView.size() + 1];
        header[0] = "Hora";
        header[1] = InternalSensorView.get(0).getName();
        for (int i = 2; i < header.length; i++) {
            header[i] = ExternalSensorView.get(i - 2).getName();
        }
        return header;
    }

    private Object[] getSamples() {
        final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date d = new Date();
        String date = dateFormat.format(d);
        Object[] ob = new Object[ExternalSensorView.size() + InternalSensorView.size() + 1];

        for (int i = 0; i < ob.length; i++) {
            ob[i] = date;
            int cellIndex = 1;
            for (SensorView v : InternalSensorView) {
                ob[cellIndex] = v.getSensor().getValue();
                cellIndex++;
            }
            for (SensorView v : ExternalSensorView) {
                ob[cellIndex] = v.getSensor().getValue();
                cellIndex++;
            }
        }
        return ob;
    }

    private void updateUI() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                double n = control.samples.getNumber().intValue();
                // System.out.println("updating ui "+totalSamples+"/"+n  +(totalSamples/n)+ " %");
                control.sampleText.setText(totalSamples + "/" + (int) n);
                control.progressBar.setProgress(totalSamples / n);
            }
        });
    }

    private void clickPause() {
        System.out.println("Clicked Pause");
        control.btnStart.setDisable(false);
        control.btnSave.setDisable(false);
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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                control.sampleText.setText("0/0");
                control.progressBar.setProgress(0.0);
            }
        });
    }

}
