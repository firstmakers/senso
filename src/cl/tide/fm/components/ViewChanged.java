/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.components;

import cl.tide.fm.model.CustomSeries;
import cl.tide.fm.model.Sensor;

/**
 *
 * @author eDelgado
 */
public interface ViewChanged {
 
    public void changed(boolean visible, CustomSeries customSerie, Boolean newValue);
 
}
