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
import javafx.scene.chart.*;

/**
 *
 * @author Edison Delgado
 */
public class ChartController {

    private LineChart lChart;
    private ArrayList<SensorView> views;
    private long interval;
    private Timer timer;
    private boolean running = false;
    private ArrayList<SensorView> internalSensor;

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
        return views.size();
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
        return views;
    }

    public void setExternalSensors(ArrayList<SensorView> views) {
        this.views = views;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public synchronized void plot() {
        timer.schedule(new TimerTask() {
            public void run() {
                System.out.println("Total views " + views.size());
                for (final SensorView view : views) {
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    Date d = new Date();
                    String date = dateFormat.format(d);
                    Double data = view.getSensor().getValue();
                    final XYChart.Data<String, Double> value = new XYChart.Data<>(date, data);
                    Platform.runLater(new Runnable() {
                        public void run() {
                            if (view.getCustomSerie().getSerie().getData().size() > 50) {
                                view.getCustomSerie().getSerie().getData().remove(0);
                            }
                            view.getCustomSerie().addValue(value);
                        }
                    });
                }
                for (final SensorView s : internalSensor) {
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    Date d = new Date();
                    String date = dateFormat.format(d);
                    Double data = s.getSensor().getValue();
                    final XYChart.Data<String, Double> value = new XYChart.Data<>(date, data);
                    Platform.runLater(new Runnable() {
                        public void run() {
                            if (s.getCustomSerie().getSerie().getData().size() > 50) {
                                s.getCustomSerie().getSerie().getData().remove(0);
                            }
                            s.getCustomSerie().addValue(value);
                        }
                    });
                }
            }
        }, 5000, interval);
    }

    public ArrayList<SensorView> getInternalSensor() {
        return internalSensor;
    }

    public void setInternalSensor(ArrayList<SensorView> internalSensor) {
        this.internalSensor = internalSensor;
    }

}
