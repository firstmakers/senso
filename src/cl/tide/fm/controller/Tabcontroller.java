/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.controller;

import cl.tide.fm.components.ChartTab;
import cl.tide.fm.components.SensorView;
import cl.tide.fm.model.TypeSensor;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

/**
 *
 * @author Edison Delgado
 */
public class Tabcontroller {

    private List<Tab> tabs;
    private TabPane tabPane;
    private List<SensorView> sensors;
    private ChartTab ligthChart;
    private ChartTab TemperatureChart;
    private SimpleIntegerProperty numTempSensor;
    private SimpleIntegerProperty numLigthSensor;
    private int numtemp;
    private int numligth;
    private Tab lightTab;
    private Tab tempTab;

    /**
     * @param tabpane
     */
    public Tabcontroller(TabPane tabpane, List<SensorView> sensor) {
        this.tabPane = tabpane;
        numtemp = 0;
        numligth = 0;

        numLigthSensor= new SimpleIntegerProperty(numligth);
        numTempSensor = new SimpleIntegerProperty(numtemp);
        tabs = new ArrayList<>();
        sensors = sensor;
        ligthChart = new ChartTab("Luminosidad");
        ligthChart.setYAxisLabel("Lux");
        TemperatureChart = new ChartTab("Temperatura");
        TemperatureChart.setYAxisLabel("Celsius");
        lightTab = getNewTab("Luminosidad", ligthChart);
        tempTab = getNewTab("Temperatura", TemperatureChart);

        numLigthSensor.addListener(new ChangeListener(){
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                System.out.println("Sensores de luz : "+ (int) newValue );
                if((int)newValue==0){
                    deleteTab(lightTab);
                }
                else if((int)oldValue==0 && (int)newValue==1){
                    addTab(lightTab);
                }
            }
        });
        numTempSensor.addListener(new ChangeListener(){
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                System.out.println("Sensores de Temperatura : "+ (int) newValue );
                if((int)newValue==0){
                    deleteTab(tempTab);
                }
                else if((int)oldValue==0 && (int)newValue==1){
                    addTab(tempTab);
                }
            }
        });
        if (sensors.size() > 0) {
            for (SensorView s : sensors) {
                if (s.getSensor().getProfile().equals(TypeSensor.LIGTH)) {
                    numligth++;
                    numLigthSensor.set(numligth);
                    ligthChart.addSensorview(s);
                    

                } else if (s.getSensor().getProfile().equals(TypeSensor.TEMPERATURE)) {
                   numtemp++;
                   numTempSensor.set(numtemp);
                   TemperatureChart.addSensorview(s);
                   
                } else {
                    //desconocido.
                }
            }
        }
        
    }

    public void closeAllTab() {
        stop();
        ligthChart.clear();
        TemperatureChart.clear();
        tabPane.getTabs().clear();
        numLigthSensor.set(0);
        numLigthSensor.set(0);
    }

    public void addAllTab() {
        
        lightTab = getNewTab("Luminosidad", ligthChart);
        addTab(lightTab);
        //ligthChart.start();
        tempTab = getNewTab("Temperatura", TemperatureChart);
        addTab(tempTab);
        //TemperatureChart.start();
    }

    public void stop() {
        TemperatureChart.stop();
        ligthChart.stop();
    }
    public void start(){
        TemperatureChart.start();
        ligthChart.start();
    }

    /**
     * agrega un tab
     * @param tab
     */
    public void addTab(Tab tab) {
        Platform.runLater(()->{if(!tabPane.getTabs().contains(tab))tabPane.getTabs().add(tab);});  
    }

    /**
     * borra un tab *
     * @param tab
     */
    public void deleteTab(Tab tab) {
        Platform.runLater(()->{tabPane.getTabs().remove(tab);});
   
    }

    public void addSensorview(SensorView s) {
        if (s.getSensor().getProfile().equals(TypeSensor.LIGTH)) {
            numligth++;
            numLigthSensor.set(numligth);
            ligthChart.addSensorview(s); 
        } else if (s.getSensor().getProfile().equals(TypeSensor.TEMPERATURE)) {
            numtemp++;
            numTempSensor.set(numtemp);
            TemperatureChart.addSensorview(s);
            //System.out.println("TABCONTROLLER temp = "+ numTempSensor.get());
        }
    }

    public void removeSensorview(SensorView s) {
        if (s.getSensor().getProfile().equals(TypeSensor.LIGTH)) {
            ligthChart.removeSensorview(s);
            numligth--;
            numLigthSensor.set(numligth);
        } else if (s.getSensor().getProfile().equals(TypeSensor.TEMPERATURE)) {
            numtemp--;
            TemperatureChart.removeSensorview(s);
            numTempSensor.set(numtemp);
            //System.out.println("TABCONTROLLER temp = "+ numTempSensor.get());
        }

    }

    public Tab getNewTab(String title, ChartTab chartTab) {
        AnchorPane pane = new AnchorPane();
        AnchorPane.setBottomAnchor(chartTab.lineChart, 0.0);
        AnchorPane.setTopAnchor(chartTab.lineChart, 0.0);
        AnchorPane.setRightAnchor(chartTab.lineChart, 0.0);
        AnchorPane.setLeftAnchor(chartTab.lineChart, 0.0);
        pane.getChildren().add(chartTab.lineChart);
        Tab tabSensor = new Tab(title, pane);
        return tabSensor;
    }

}
