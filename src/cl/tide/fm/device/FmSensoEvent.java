/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.device;

import cl.tide.fm.model.Sensor;

/**
 *
 * @author eDelgado
 */
public class FmSensoEvent {

    private double value;
    private int integer;
    private int decimal;
   
    public FmSensoEvent(double value) {
        this.value = value;
        this.integer = (int) value;
        if(value > 0)
            this.decimal = (int) Sensor.round(((value - integer)*10), 1);  
        else if(value < 0)
            this.decimal = (int) Sensor.round(((value + integer)*-10), 1);
        else
            this.decimal = 0;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getInteger() {
        return integer;
    }

    public void setInteger(int integer) {
        this.integer = integer;
    }

    public int getDecimal() {
        return decimal;
    }

    public void setDecimal(int decimal) {
        this.decimal = decimal;
    }


 
}