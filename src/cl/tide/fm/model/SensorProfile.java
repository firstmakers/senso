/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.model;

import javafx.scene.paint.Color;

/**
 *
 * @author eDelgado
 */
public class SensorProfile {
    
    public static final int LIGHT =  0x01;
    public static final int HUMIDITY = 0x02;
    public static final int TEMPERATURE = 0x28;
    public static final int UNKNOWN = 0x00;
    public static final Color TEMPERATURA_DEFAULT = Color.rgb(230, 146, 39);
    public static final Color LIGHT_DEFAULT = Color.rgb(124, 164, 60);
    public static final Color HUMIDITY_DEFAULT = Color.rgb(44, 143, 203);   
    public static final Color UNKNOWN_DEFAULT = Color.rgb(99, 94, 152);
  
         
}
