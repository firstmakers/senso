/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Edison Delgado
 */
public class CustomCellDate {

    public SimpleStringProperty date = new SimpleStringProperty();
    
    
    public CustomCellDate() {  
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date d = new Date();
        String fecha = dateFormat.format(d);
        this.date.set(fecha);
    }
    
    public String getValue() {
        return date.get();
    }

    public void setValue(Date d) {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String date = dateFormat.format(d);
        this.date.set(date);
    }
}
