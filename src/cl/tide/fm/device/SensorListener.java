/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.device;

/**
 *
 * @author eDelgado
 */
public interface SensorListener {
    
    public void onSensorChangeValue(FmSensoEvent value);
}
