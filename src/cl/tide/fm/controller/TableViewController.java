/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.controller;

import cl.tide.fm.components.SensorView;
import cl.tide.fm.model.CustomCell;
import cl.tide.fm.model.CustomCellDate;
import cl.tide.fm.model.CustomColumn;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

/**
 *
 * @author Edison Delgado
 */
public class TableViewController {
    private TableView table;
    private ArrayList<SensorView> views;
    private int interval;
    private Timer timer;
    private List<CustomCellDate> list;
    private ObservableList<CustomCellDate> date;
    TableColumn dateColumn;
    
    
    public TableViewController(TableView table) {
        list = new ArrayList<>();
        date = FXCollections.observableList(list);
        this.table = table;       
        dateColumn = new TableColumn<>("Fecha");
        dateColumn.setCellValueFactory(
                new PropertyValueFactory<CustomCellDate, String>("date")
        );
        table.getColumns().add(dateColumn);
        setInterval(2000);
    }
    
    public void addColumn(String name){
        TableColumn col = new TableColumn();
        col.setText(name);
        table.getColumns().add(col);
    }
    public void removeColumn(){}

    public TableView getTable() {
        return table;
    }

    public void setTable(TableView table) {
        this.table = table;
    }

    public ArrayList<SensorView> getViews() {
        return views;
    }

    public void setViews(ArrayList<SensorView> views) {
        this.views = views;
    }

    void addColumn(CustomColumn column, ObservableList<CustomCell> data) {
        table.getColumns().add(column.getTableColumn());
        
    }

    void removeColumn(CustomColumn column) {
        table.getColumns().remove(column.getTableColumn());
    }
    public void startCapture() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                for (final SensorView s : views) {
                    final Double data = s.getSensor().getValue();
                    System.out.println(">> Current Value " + data);
                    Platform.runLater(new Runnable() {
                        public void run() {                       
                            date.add(new CustomCellDate());
                           
                        }
                    });
                }
            }
        }, 1000, interval);
    }
    
    public void stopCapture(){
        if(timer!= null)
            timer.cancel();
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
