/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.components;

import cl.tide.fm.device.FmSensoEvent;
import cl.tide.fm.device.SensorListener;
import cl.tide.fm.model.Sensor;
import cl.tide.fm.model.SensorProfile;
import cl.tide.fm.model.UnknownSensor;
import cl.tide.fm.utilities.Utilities;
import javafx.scene.image.Image;

/**
 *
 * @author eDelgado
 */
public class UnknownView extends SensorView {

    UnknownSensor sensor;
    public UnknownView() {
        setIcon(new Image("/images/question_128.png"));
        setCssStyle(Utilities.StyleUnknown);
    }

    public UnknownView(Sensor s) {
        setIcon(new Image("/images/question_128.png"));
        setCssStyle(Utilities.StyleUnknown);
        setColor(SensorProfile.UNKNOWN_DEFAULT);
        this.sensor = (UnknownSensor)s;
        sensor.addListener(new SensorListener() {
           
            @Override
            public void onSensorChangeValue(FmSensoEvent value) {
                setValue(value.getInteger()+"");
                setDecimal(value.getDecimal()+"");
            }
        });
    }

    
}
