
package cl.tide.fm.components;

import cl.tide.fm.device.FmSensoEvent;
import cl.tide.fm.device.SensorListener;
import cl.tide.fm.model.Sensor;
import cl.tide.fm.model.SensorProfile;
import javafx.scene.image.Image;

/**
 *
 * @author Edison Delgado
 */
public class TemperatureView extends SensorView {
   
    boolean firstime = true;
 

    public TemperatureView(Sensor s) {
        super();
        setSensor(s);
        setIcon(new Image("/images/thermometer_128.png"));
        setColor(SensorProfile.TEMPERATURA_DEFAULT);
        setBackgroundColor(getColor());
        setName("Temperatura");
        //data = new float[100000000];
        
        FmSensoEvent event = new FmSensoEvent(s.getValue(), 0);
        setValue(event.getInteger()+"");
        setDecimal(event.getDecimal()+"");
        //setSerieColor();
        s.addListener((FmSensoEvent value) -> {
            System.err.println(value.getOldValue()+ " "+ value.getValue());
            if (firstime) {
                setValue(value.getInteger() + "");
                setDecimal(value.getDecimal() + "");
                firstime = false;
            } else {
                if (value.getOldInteger() != value.getInteger()) {
                    setValue(value.getInteger() + "");
                }
                if (value.getOldDecimal() != value.getInteger()) {
                    setDecimal(value.getDecimal() + "");
                }
            }
        });
    }
}
