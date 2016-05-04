/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.sensonet;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author edisondelgado
 */
public class Project {
    long id;
    String name;
    String token;
    String description;
    User owner;
    String status;
    List<Datasource> datasources;
    //List<Participant>
    
    public Project(JSONObject p){
        //System.out.println(p.toString());
        datasources = new ArrayList<>();
        id = p.getLong("id");
        name = p.getString("name");
        token = p.getString("token");
        description = p.getString("description");
        status = p.getString("status");
        owner = new User( p.getJSONObject("owner"));
        JSONArray dts = p.getJSONArray("datasources");
        int length = dts.length();
        if(length>0){
            for(int i =0;i<length;i++){
                datasources.add(new Datasource(dts.getJSONObject(i)));
            }
        }
        
        
    }
    
}
