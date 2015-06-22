
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
   

 

    public TemperatureView(Sensor s) {
        super();
        setSensor(s);
        setIcon(new Image("/images/thermometer_128.png"));
        setColor(SensorProfile.TEMPERATURA_DEFAULT);
        setBackgroundColor(getColor());
        setName("Temperatura");
        //setSerieColor();
        s.addListener(new SensorListener() {
 
            @Override
            public void onSensorChangeValue(FmSensoEvent value) {  
               if(value.getOldInteger()!= value.getInteger())
                    setValue(value.getInteger()+"");
               if(value.getOldDecimal()!= value.getInteger())
                    setDecimal(value.getDecimal()+"");
            }
        });
    } 
}
