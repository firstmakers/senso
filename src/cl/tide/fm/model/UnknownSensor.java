/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.model;

/**
 *
 * @author eDelgado
 */
public class UnknownSensor extends Sensor{

    public UnknownSensor() {
    }

    public UnknownSensor(String id) {
        super(id);
        setProfile(TypeSensor.UNKNOWN);
      
    }

    @Override
    public void setValue(byte[] data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getValue() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
