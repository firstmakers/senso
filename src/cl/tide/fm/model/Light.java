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
public class Light extends Sensor{
    
    private final int min = 0;
    private final int max = 65536;       

    public Light(String id) {
        super(id);
        setProfile(TypeSensor.LIGTH);
        setUnit("lux");
        setMaxValue(max);
        setMinValue(min);
    }

    @Override
    public void setValue(byte[] data) {
        double old = value;
        double light = ((data[1] & 0xFF) << 8) + (data[0] & 0xFF);
        value = (light / 1.2);
        if(old!= value){
         for(SensorListener l :listener)
            l.onSensorChangeValue(new FmSensoEvent(getLux(),old));
        }
    }
    public double getLux(){
        return round(value, 1);
    }
    public double getLux(int i){
        return round(value, i);
    }
    public double getPercentaje(){
        return value;
    }
    @Override
    public double getValue() {
        return getLux(1);
    }  
}
