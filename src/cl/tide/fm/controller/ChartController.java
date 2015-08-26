/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.controller;

import cl.tide.fm.components.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.chart.*;
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
public class ChartController {

    private LineChart lChart;
    private ArrayList<SensorView> externalSensor;
    private long interval;
    private Timer timer;
    private boolean running = false;
    private ArrayList<SensorView> internalSensor;
    private ContextMenu cMenu;


    ///Constructor
    public ChartController(LineChart chart) {

        this.lChart = chart;
        this.interval = 5000;
        lChart.setTitle("Datos de Sensores");
        lChart.setCreateSymbols(false);//remove symbols 
        lChart.setLegendVisible(false);
        //lChart.setLegendSide(Side.TOP);
        //lChart.getXAxis().setAutoRanging(false);
        lChart.getXAxis().setAutoRanging(true);
        lChart.getXAxis().setTickMarkVisible(false);
        lChart.setAnimated(false);
        cMenu = setContextMenu();

        lChart.setOnMouseClicked((MouseEvent event) -> {
            if (MouseButton.SECONDARY.equals(event.getButton())) {
                cMenu.show(lChart.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });

    }

    private ContextMenu setContextMenu() {

        MenuItem clearChart = new MenuItem("Borrar gráfico");
        //GlyphsDude.setIcon(clearChart, FontAwesomeIcon.TRASH, "1.5em");
        MenuItem chartManager = new MenuItem("Detener gráfico");
        //GlyphsDude.setIcon(chartManager, FontAwesomeIcon.STOP, "1.5em");
        
        CheckMenuItem fastUpdate = new CheckMenuItem("Graficar cada 1 segundo");
        CheckMenuItem normalUpdate = new CheckMenuItem("Graficar cada 5 segundos");
        CheckMenuItem slowUpdate = new CheckMenuItem("Graficar cada 10 segundos");
        normalUpdate.setSelected(true);
        clearChart.setOnAction(e -> {
            clear();           
        });
        
        chartManager.setOnAction(e ->{
            if(isRunning()){
                stop();
                chartManager.setText("Iniciar gráfico");
                //GlyphsDude.setIcon(chartManager, FontAwesomeIcon.PLAY, "1.5em");
            }
            else{
                start();
                chartManager.setText("Detener gráfico");
                //GlyphsDude.setIcon(chartManager, FontAwesomeIcon.STOP, "1.5em");
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
        timer.cancel();
        timer = null;

    }

    public synchronized void removeSerie(XYChart.Series series) {
        if(series == null)
            return;
        lChart.getData().remove(series);
        System.out.println("serie removed " + series.toString());
       
    }

    public synchronized void addSerie(XYChart.Series serie){
        boolean b = lChart.getData().contains(serie);
        if(!b)
            lChart.getData().add(serie);   
        System.out.println(b + " serie added "+ serie.toString());
    }

    public int getViewCount() {
        return externalSensor.size();
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public LineChart getlChart() {
        return lChart;
    }

    public void setlChart(LineChart lChart) {
        this.lChart = lChart;
    }

    public ArrayList<SensorView> getViews() {
        return externalSensor;
    }

    public void setExternalSensors(ArrayList<SensorView> views) {
        this.externalSensor = views;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public synchronized void plot() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Total views " + externalSensor.size());
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                Date d = new Date();
                String date = dateFormat.format(d);
                externalSensor.stream().forEach((view) -> {                 
                    Double data = view.getSensor().getValue();
                    final XYChart.Data<String, Double> value = new XYChart.Data<>(date, data);
                    Platform.runLater(() -> {
                        if (view.getCustomSerie().getSerie().getData().size() > 50) {
                            view.getCustomSerie().getSerie().getData().remove(0);
                        }
                        view.getCustomSerie().addValue(value);
                    });
                });
                internalSensor.stream().forEach((SensorView s) -> {
                    Double data = s.getSensor().getValue();
                    final XYChart.Data<String, Double> value = new XYChart.Data<>(date, data);
                    Platform.runLater(() -> {
                        if (s.getCustomSerie().getSerie().getData().size() > 50) {
                            s.getCustomSerie().getSerie().getData().remove(0);
                        }
                        s.getCustomSerie().addValue(value);
                    });
                });
            }
        }, 3000, interval);
    }
    
    public void clear(){
        internalSensor.stream().forEach((s) -> {
            s.getCustomSerie().getSerie().getData().clear();
        });
        externalSensor.stream().forEach((v) -> {
            v.getCustomSerie().getSerie().getData().clear();
        });
    }

    public ArrayList<SensorView> getInternalSensor() {
        return internalSensor;
    }

    public void setInternalSensor(ArrayList<SensorView> internalSensor) {
        this.internalSensor = internalSensor;
    }

}
