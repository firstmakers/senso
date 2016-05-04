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
 * @author Edison Delgado
 */
public class Datasource {

    String name;
    long id;
    long owner = -1;
    List<Variable> variables;

    public Datasource(JSONObject ds) {
       variables = new ArrayList<>();
       id = ds.getLong("id");
        try {
            JSONObject tmp = ds.getJSONObject("owner");
            owner = tmp.getLong("id");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }finally{
            if(owner < 0)
                owner = ds.getLong("owner");
        }
        name = ds.getString("name");
     
        try {
            JSONArray vrs = ds.getJSONArray("variables");
            if (vrs.length() > 0) {
                for (int i = 0; i < vrs.length(); i++) {
                    variables.add(new Variable(vrs.getJSONObject(i)));
                }
            }
              //owner = new User(ds.getJSONObject("owner"));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
    
    public void addVariable(Variable v){
        this.variables.add(v);
    }
    public List<Variable> getVariables(){
        return this.variables;
    }
    
    
}
