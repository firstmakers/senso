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
    private double oldValue;
    private int oldInteger;
    private int oldDecimal;

    public FmSensoEvent(double newValue, double oldValue) {
        this.value = newValue;
        this.oldValue = oldValue;
        this.integer = (int) value;
        this.oldInteger = (int) oldValue;
        if(value > 0)
            this.decimal = (int) Sensor.round(((value - integer)*10), 1);  
        else if(value < 0)
            this.decimal = (int) Sensor.round(((value + integer)*-10), 1);
        else
            this.decimal = 0;
        
        if(oldValue > 0)
            this.oldDecimal = (int) Sensor.round(((oldValue - oldInteger)*10), 1);  
        else if(oldValue < 0)
            this.oldDecimal = (int) Sensor.round(((oldValue + oldInteger)*-10), 1);
        else
            this.oldDecimal = 0;
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

    public double getOldValue() {
        return oldValue;
    }

    public void setOldValue(double oldValue) {
        this.oldValue = oldValue;
    }

    public int getOldInteger() {
        return oldInteger;
    }

    public void setOldInteger(int oldInteger) {
        this.oldInteger = oldInteger;
    }

    public int getOldDecimal() {
        return oldDecimal;
    }

    public void setOldDecimal(int oldDecimal) {
        this.oldDecimal = oldDecimal;
    }

}