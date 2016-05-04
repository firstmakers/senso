/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.sensonet;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/**
 *
 * @author Edison Delgado
 */
public class User {
    public long id;
    public String name;
    public String email;
    public String gravatarUrl;
    public List<Project> projects;
    

    public User(JSONObject user) {
        //System.out.println(user.toString());
        projects = new ArrayList<>();
        id = user.getLong("id");
        name = user.getString("name");
        email = user.getString("email");
        gravatarUrl = user.getString("gravatarUrl");
    }
    
    public void addProject(Project p){
        projects.add(p);
    }
    
    
    
}
