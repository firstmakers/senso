/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.components;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Edison Delgado
 */
public class ChartTab {
    
    private final CategoryAxis xAxis = new CategoryAxis();
    private final NumberAxis yAxis = new NumberAxis();
    public final LineChart<String,Number> lineChart;
    private long interval;
    private Timer timer;
    private boolean running = false;
    private ContextMenu cMenu;
    
    public List<SensorView> sensors;
    
    
    public ChartTab(String title){
        sensors = new ArrayList<>();
        this.interval = 5000;
        xAxis.setLabel("Hora");
        lineChart = new LineChart<>(xAxis,yAxis);
        lineChart.setTitle(title);
        lineChart.setCreateSymbols(false);//remove symbols 
        lineChart.setLegendVisible(false);
        lineChart.getXAxis().setAutoRanging(true);
        lineChart.getXAxis().setTickMarkVisible(false);
        lineChart.setAnimated(false);
        cMenu = setContextMenu();

        lineChart.setOnMouseClicked((MouseEvent event) -> {
            if (MouseButton.SECONDARY.equals(event.getButton())) {
                cMenu.show(lineChart.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });
    }
    
    
    public void addSensorview(SensorView s){
        System.out.println("Añadido sensor de : " + s.getSensor().getProfile().toString());
        Platform.runLater(()->{
            addSerie(s.getCustomSerie().getSerie());
            sensors.add(s);
            s.setSerieColor();
        });

    }
    
    public void removeSensorview(SensorView s){
        removeSerie(s.getCustomSerie().getSerie());
        sensors.remove(s);
    }
    public void setYAxisLabel(String label){
        yAxis.setLabel(label);
    }
    public void setXAxisLabel(String label){
        xAxis.setLabel(label);
    }
    public void addSerie(XYChart.Series serie){
        lineChart.getData().add(serie);
    }
    public void removeSerie(XYChart.Series serie){
        lineChart.getData().remove(serie);
    }
    
    
    public synchronized void plot() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (sensors.size() > 0) {
                    System.out.println("Total views " + sensors.size());
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    Date d = new Date();
                    String date = dateFormat.format(d);
                    sensors.stream().forEach((view) -> {
                        Double data = view.getSensor().getValue();
                        final XYChart.Data<String, Double> value = new XYChart.Data<>(date, data);
                        Platform.runLater(() -> {
                            if (view.getCustomSerie().getSerie().getData().size() > 50) {
                                view.getCustomSerie().getSerie().getData().remove(0);
                            }
                            view.getCustomSerie().addValue(value);
                        });
                    });
                }
            }
        }, 0, interval);
    }
    private ContextMenu setContextMenu() {
        MenuItem chartManager = new MenuItem("Iniciar gráfico");
        MenuItem clearChart = new MenuItem("Borrar gráfico");
        CheckMenuItem fastUpdate = new CheckMenuItem("Graficar cada 1 segundo");
        CheckMenuItem normalUpdate = new CheckMenuItem("Graficar cada 5 segundos");
        CheckMenuItem slowUpdate = new CheckMenuItem("Graficar cada 10 segundos");
        normalUpdate.setSelected(true);
        clearChart.setOnAction(e -> {
            clear();           
        });
        
        chartManager.setOnAction(e -> {
            if (sensors.size() > 0) {
                if (isRunning()) {
                    stop();
                    chartManager.setText("Iniciar gráfico");
                    //GlyphsDude.setIcon(chartManager, FontAwesomeIcon.PLAY, "1.5em");
                } else {
                    start();
                    chartManager.setText("Detener gráfico");
                    //GlyphsDude.setIcon(chartManager, FontAwesomeIcon.STOP, "1.5em");
                }
            }
        });

        fastUpdate.setOnAction(e -> {
            if (fastUpdate.isSelected()) {
                stop();
                slowUpdate.setSelected(false);
                normalUpdate.setSelected(false);
                setInterval(1000);
                start();              
            }
        });
        normalUpdate.setOnAction(e-> {
            if(normalUpdate.isSelected()){
                stop();
                slowUpdate.setSelected(false);
                fastUpdate.setSelected(false);
                setInterval(5000);
                start();
            }
        });

        slowUpdate.setOnAction(e -> {
            if (slowUpdate.isSelected()) {
                stop();
                fastUpdate.setSelected(false);
                normalUpdate.setSelected(false);
                setInterval(10000);
                start();
            }
        });
        
        ContextMenu menu = new ContextMenu(
                chartManager,
                clearChart,
                new SeparatorMenuItem(),
                fastUpdate,
                normalUpdate,
                slowUpdate
        );      
        return menu;
    }
    public void start() {
        running = true;
        timer = new Timer();
        plot();
    }
    public void stop() {
        running = false;
        if(timer!=null)
            timer.cancel();
        timer = null;

    }
    public boolean isRunning() {
        return running;
    }
    public void setRunning(boolean running) {
        this.running = running;
    }
    public void clear(){
        sensors.stream().forEach((s) -> {
            s.getCustomSerie().getSerie().getData().clear();
        });
    }
    public long getInterval() {
        return interval;
    }
    public void setInterval(long interval) {
        this.interval = interval;
    }

}
