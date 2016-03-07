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
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 *
 * @author eDelgado
 */
public class CustomColumn {

    private TableColumn tableColumn;
    private List<CustomCell> values;
    private ObservableList<CustomCell> data;
    private String name;

    public CustomColumn(String name) {
        this.name = name;
        values = new ArrayList<>();
        data = FXCollections.observableList(values);
        tableColumn = new TableColumn(name);
        tableColumn.setCellValueFactory(
                new PropertyValueFactory<CustomCell, Double>("value")
        );
        
    }

    public void addValue(double value) {
        CustomCell cel = new CustomCell();
        cel.setValue(value);
        data.add(cel);
    }

    public ObservableList<CustomCell> getData() {
        return data;
    }

    public void setData(ObservableList<CustomCell> data) {
        this.data = data;
    }

    public TableColumn getTableColumn() {
        return tableColumn;
    }

    public void setTableColumn(TableColumn tableColumn) {
        this.tableColumn = tableColumn;
    }

    public void addCustomCell(CustomCell cell) {
        this.data.add(cell);
    }

}
