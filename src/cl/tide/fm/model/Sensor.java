/*
 * 
 */
package cl.tide.fm.model;

import cl.tide.fm.device.SensorListener;
import java.util.ArrayList;

/**
 *
 * @author Edison Delgado
 */
public abstract class Sensor {

    private String id;
    protected double value;
    private int minValue;
    private int maxValue;
    private int index;
    private TypeSensor profile;
    private String unit;
    protected ArrayList<SensorListener> listener;
    private  ArrayList<Double> data;


    public Sensor() {
        listener = new  ArrayList<>();
        
        this.value = 0;
    }
    public boolean addListener(SensorListener l){
        if(!listener.contains(l)){
            listener.add(l);
            return true;
        }
        return false;    
    }

    public Sensor(String id) {
        listener = new  ArrayList<>();
        this.id = id;
        this.value = 0;
        data = new ArrayList<>();
    }
    
    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }
    public abstract void setValue(byte[] data);
    public abstract double getValue();

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public TypeSensor getProfile() {
        return profile;
    }

    public void setProfile(TypeSensor profile) {
        this.profile = profile;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public ArrayList<Double> getData() {
        return data;
    }

    public void setData(ArrayList<Double> data) {
        this.data = data;
    }
    public void addData(Double d){
        this.data.add(d);
    }
    public void clearData(){
        this.data.clear();
    }
    public int dataSize(){
        return this.data.size();
    }
}

