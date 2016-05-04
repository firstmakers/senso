/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.sensonet;

import cl.tide.fm.model.Sensor;
import java.util.List;
import org.json.JSONObject;

/**
 *
 * @author edisondelgado
 */
public class Variable {
    long id ;
    String name;
    String unit;
    String idsensor;
    Sensor sensor;
    //List<Data> data;
    
    public Variable(JSONObject va){   
        id = va.getLong("id");
        unit = va.getString("unit");
        idsensor = va.getString("idsensor");
        name = va.getString("name");
    }
    
    public void addSensor(Sensor s){
        this.sensor = s;
    }
    

}
