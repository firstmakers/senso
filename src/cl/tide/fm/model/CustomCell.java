/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.model;

import javafx.beans.property.SimpleDoubleProperty;

/**
 *
 * @author Edison Delgado
 */
public class CustomCell {
    public SimpleDoubleProperty value = new SimpleDoubleProperty();

    public CustomCell(SimpleDoubleProperty value) {
        this.value = value;
    }

    public CustomCell() {       
    }
    
    
    public Double getValue(){
        return value.get();
    }
    public void setValue(Double v) {
        value.set(v);
    }
    
}
