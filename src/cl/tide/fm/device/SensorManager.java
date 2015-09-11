/*
 */
package cl.tide.fm.device;

import cl.tide.fm.model.Humidity;
import cl.tide.fm.model.Ligth;
import cl.tide.fm.model.Sensor;
import cl.tide.fm.model.SensorProfile;
import cl.tide.fm.model.Temperature;
import cl.tide.fm.model.UnknownSensor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Edison Delgado
 */
public class SensorManager {
    
    private final List<Sensor> externalSensors;
    private final List<Sensor> internalSensors;


    private final int maxSensor = 5;
    private final Sensor light;
    private final byte[] lightID = new byte[1];
    
    public SensorManager() {
        internalSensors = new ArrayList<>();
        externalSensors = new ArrayList<>();
        lightID[0] = SensorProfile.LIGHT;
        light = SensorManager.Instance(Arrays.toString(lightID),SensorProfile.LIGHT);
        internalSensors.add(light);
    }
    
    public static Sensor Instance(String id, int profile){
        System.out.println("Creating new Sensor " +id);
        switch(profile){
            case SensorProfile.TEMPERATURE:
                return new Temperature(id);
            case SensorProfile.HUMIDITY:
                return  new Humidity(id);
            case SensorProfile.LIGHT:
                return new Ligth(id);
            default:
                return new UnknownSensor(id);     
        }       
    }
    public List<Sensor> getExternalSensors() {
        return externalSensors;
    }

    public List<Sensor> getInternalSensors() {
        return internalSensors;
    }
      
    public int getSensorCount(){
        return externalSensors.size();
    }
    
    public void addExternalSensor(Sensor sensor){
        externalSensors.add(sensor);
    }
    
    public boolean findSensorByID(String id){      
        return externalSensors.stream().anyMatch((s) -> (s.getId().equals(id)));      
    }
    
    public Sensor getSensorByID(String id){
        for(Sensor s: externalSensors){
                if(s.getId().equals(id)){ 
                     return s;
                }
            }      
        return null;
    }
    
    public void removeSensorByID(String id){
        Sensor s = getSensorByID(id);
        externalSensors.remove(s);
    }

    public List<Sensor> getAttachedSensor(byte[] data) {
        List<Sensor> sensors = new ArrayList<>(maxSensor);
        byte[] id = new byte[8];
        int i, j;
        int aux = 1;       
        int length = (int) data[0];
        Sensor sensor;
        for (i = 1; i < length+1; i++) {           
            for (j = 0; j < 8; j++) {
                id[j] = data[aux];
                aux++;
            }
            aux ++;   
            String mId = getID(id);
            if(!findSensorByID(mId)){
                // return a new Sensor instance by id
                sensor = SensorManager.Instance(mId, data[1]);
                System.out.println(" New Sensor Attached "+ sensor.getId());
                sensors.add(sensor);
            }
        }
        return sensors;
    }
    
    private String getID(byte[] id){
        return Arrays.toString(id);
    } 
    
    public void removeSensor(List sensors){
        externalSensors.removeAll(sensors);
    }
    public void addSensor(List sensors){
        externalSensors.addAll(sensors);
    }
    public void addSensor(byte[] data){
        externalSensors.addAll(getAttachedSensor(data));
    }
    
    public void clear(){
        externalSensors.clear();
    }
    public List<Sensor> getDetachedSensor(byte[] data) {
        List<Sensor> removedSensor = new ArrayList<>(externalSensors);        
        byte[] id = new byte[8];
        int i, j;
        int aux = 1;       
        int length = (int) data[0];
        Sensor sensor;
        for (i = 1; i < length+1; i++) {           
            for (j = 0; j < 8; j++) {
                id[j] = data[aux];
                aux++;
            }
            aux ++;    
            sensor = getSensorByID(getID(id));
            if(sensor != null){
                removedSensor.remove(sensor);
            }
        }   
        removedSensor.stream().forEach((k) -> {
            System.out.println(" New Sensor detached "+ k.getId());
        });
        return removedSensor;
    }

    public List<Sensor> getSensors() {
        return this.externalSensors;
    }
    
}
