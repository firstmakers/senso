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
import javafx.scene.image.Image;

/**
 *
 * @author eDelgado
 */
public class LigthView extends SensorView {

    public LigthView(Sensor s) {
        setIcon(new Image("/images/sunny_128.png"));
        setSensor(s);
        setName("Luminosidad");
        setUnit(s.getUnit());
        setColor(SensorProfile.LIGHT_DEFAULT);
        setBackgroundColor(getColor());
        //data = new float[100000000];
        s.addListener(new SensorListener() {

            @Override
            public void onSensorChangeValue(FmSensoEvent value) {
                setValue(value.getInteger() + "");
                setDecimal(value.getDecimal() + "");
            }
        });
    }

}
