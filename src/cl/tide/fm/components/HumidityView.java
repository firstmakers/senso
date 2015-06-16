/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.components;

import cl.tide.fm.device.FmSensoEvent;
import cl.tide.fm.device.SensorListener;
import cl.tide.fm.model.Humidity;
import cl.tide.fm.model.Sensor;
import cl.tide.fm.model.SensorProfile;
import cl.tide.fm.utilities.Utilities;
import javafx.scene.image.Image;

/**
 *
 * @author Edison Delgado
 */
public class HumidityView extends SensorView{
    Humidity sensor;
    
    public HumidityView() {
        
        setIcon(new Image("/images/humidity.png"));
        setCssStyle(Utilities.StyleHumidity);
    }

    public HumidityView(Sensor s) {
        setIcon(new Image("/images/humidity.png"));
        setColor(SensorProfile.HUMIDITY_DEFAULT);
        setBackgroundColor(getColor());
        setName("Humedad");
        setUnit(s.getUnit());
        this.sensor = (Humidity) s;
        this.sensor.addListener(new SensorListener() {

               @Override
               public void onSensorChangeValue(FmSensoEvent value) {
                      setValue(value.getInteger()+"");
                setDecimal(value.getDecimal()+"");
               }
           }); 
    }   
    
}
