/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.device;

import cl.tide.fm.model.Sensor;
import java.util.ArrayList;
import org.hid4java.HidDevice;

/**
 *
 * @author Edison Delgado
 */
public interface FmSensoListener {
    
    public void onFirmwareChange(String firm);
    public void onAttachedDevice(HidDevice device);
    public void onDetachedDevice(HidDevice device);
    public void onClose();
    public void onStart();
    public void onSensorDetach(ArrayList<Sensor> arrayList);
    public void onSensorAttach(ArrayList<Sensor> arrayList);
    
}
