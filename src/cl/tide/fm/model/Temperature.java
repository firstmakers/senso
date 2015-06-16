/*
 * 
 */
package cl.tide.fm.model;

import cl.tide.fm.device.FmSensoEvent;
import cl.tide.fm.device.SensorListener;

/**
 *
 * @author Edison Delgado
 */
public class Temperature extends Sensor{

    public Temperature(String id) {
        super(id);
        setProfile(TypeSensor.TEMPERATURE);
        setUnit("ºC");
    }

    
    public double getCelsius(){       
        return value;
    }
    public  double getFahrenheit(){        
        return ((value*9)/5)+32;
    }
    public double getKelvin(){
        return value + 273.15;
    }
    public double getCelsius(int decimal){       
        return round(value, decimal);
    }
    public double getFahrenheit(int decimal){        
        return round(((value*9)/5)+32, decimal);
    }
    public double getKelvin(int decimal){
        return round(value +273.15, decimal);
    }

    @Override
    public void setValue(byte[] data) {
        double oldvalue = value;
        double val;
        int celsius = ((data[1] & 0xFF) << 8) + (data[0] & 0xFF);
        //Below Zero
        if(celsius > 32767){
            String s = Integer.toBinaryString(~celsius).substring(16);
            int d = - (Integer.parseInt(s,2) + 1);
            val = (d/16);
        }else{
            val = celsius*0.0625;
        }
        //when value change assign new value and notify changes
        if(oldvalue != val){
            value = val;
            for(SensorListener l :listener)
                l.onSensorChangeValue(new FmSensoEvent(value));
        }

    }

    @Override
    public double getValue() {
        return getCelsius(1);
    }
    
}
    
    


    
