/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.model;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

/**
 *
 * @author Edison Delgado
 */
public class CustomSeries {

    private XYChart.Series serie;
    private String name;
    private List<XYChart.Data<String, Double>> data;
    private ObservableList<XYChart.Data<String, Double>> values;

    public CustomSeries(String name) {
        this.name = name;
        data = new ArrayList<>();
        values = FXCollections.observableList(data);
        this.serie = new XYChart.Series<>(name, values);
    }

    public void addValue(XYChart.Data<String, Double> value) {
        this.values.add(value);
    }

    public XYChart.Series getSerie() {
        return serie;
    }

    public void setSerie(XYChart.Series serie) {
        this.serie = serie;
    }


    public ObservableList<XYChart.Data<String, Double>> getValues() {
        return values;
    }

    public void setValues(ObservableList<XYChart.Data<String, Double>> values) {
        this.values = values;
    }

    public String getName() {
        return serie.getName();
    }
    
    public void setName(String name) {
        this.serie.setName(name);
  
    }

    public List<XYChart.Data<String, Double>> getData() {
        return data;
    }

    public void setData(List<XYChart.Data<String, Double>> data) {
        this.data = data;
    }


}
