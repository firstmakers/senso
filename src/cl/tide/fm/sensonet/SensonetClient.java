/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.sensonet;

import cl.tide.fm.components.SensorView;
import cl.tide.fm.controller.SettingsController;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;


/**
 *
 * @author edisondelgado
 */
public class SensonetClient {
    private boolean connected;
    private boolean isLogged;
    private boolean isOnline = false;
    private String loginStatus;
    private User user;
    private String token;
    private SensonetEvents listener;
    private Project project;
    CloseableHttpClient client;
    Datasource myDatasource ;
    List<SensorView> sensors;
    List<Variable> variables;
    private Timer netTimer;
    private  Timer timer;
    private boolean stop;

    public static final String baseUrl = "http://uranio.tide.cl:1337/";
    
    public SensonetClient(){
        sensors = new ArrayList<>();
        variables = new ArrayList<>();
        connected = false;
        isLogged = false;
        stop = true;
        listener = null; 
        client = HttpClients.createDefault();
        checkingNetwork();
    }
    
    public void addListener(SensonetEvents event){
        listener = event;
    }
    
    public void removeListener(){
        listener = null;
    }

    public void start(List<SensorView> s) {
        long interval = SettingsController.getInterval();
        interval = interval < 10000 ? 10000 : interval;
        System.err.println("interval update " + interval);

        stop = false;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!isLogged)
                    login(SettingsController.getSensonetUser(), SettingsController.getSensonetPassword());
                System.out.println("\n Sensonet Status "+ isOnline +" " + !stop + " "+ isLogged);
                if (isOnline && !stop) {
                    if(myDatasource == null) {
                        findOrCreateDataSource("Senso");
                        if(variables.isEmpty())
                            findOrCreateVariables(s);
                    }
                    if (variables.size() > 0) {
                        variables.forEach((va) -> {
                            try {
                                BasicNameValuePair param = new BasicNameValuePair("value", va.sensor.getValue() + "");
                                List<NameValuePair> params = new ArrayList<>();
                                params.add(param);
                                HttpPost post = new HttpPost(baseUrl + "project/datasource/variable/value/" + va.id);
                                
                                post.setEntity(new UrlEncodedFormEntity(params));
                                CloseableHttpResponse result = client.execute(post);
                                int statusCode = result.getStatusLine().getStatusCode();
                                if (statusCode == 200) {
                                    System.out.println(va.name + " enviado correctamente ");
                                } else {
                                    System.err.println(statusCode + " Ocurrio un error al enviar " + va.name);
                                }
                                result.close();

                            } catch (IOException ex) {
                                Logger.getLogger(SensonetClient.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        });
                    } else {
                        //almacenar los datos  y envialos luego?
                        System.err.println("No hay variables");
                        findOrCreateVariables(s);
                    }
                }else{
                     System.err.println("sensonet is offline");
                }
            }
        }, 0, interval);
    }

    public void stop(){
      stop = true;
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }
    
    
    public User login(String email, String password)  {
        HttpPut login = new HttpPut(baseUrl.concat("login"));
        try {
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("email", email));
            nvps.add(new BasicNameValuePair("password", password));
            login.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = client.execute(login);
            int statusCode = response.getStatusLine().getStatusCode();
            
            if(statusCode == 200){
                isLogged = true;
                String entity = EntityUtils.toString( response.getEntity());
                JSONObject result = new JSONObject(entity);
                user = new User(result);
                if(listener != null) listener.onUserLogged(user);
                return user;
            }
            else{
                System.err.println(statusCode + " Error de autenticación");
            }
            response.close();
        } catch (IOException io) {
            System.err.println(io.getMessage());
        }
        return user;
    }

    public Project getProjectByToken() {

        String url = baseUrl.concat("project/").concat(token);
        HttpGet get = new HttpGet(url);
        try {
            CloseableHttpResponse result = client.execute(get);

            int statusCode = result.getStatusLine().getStatusCode();
            System.out.println(result.getStatusLine());
            String response = EntityUtils.toString(result.getEntity());
            //System.out.println(response);
            if(statusCode == 200 & !response.isEmpty()){
            Project p = new Project(
                    new JSONObject(response));
            return p;
            }
        } catch (IOException ex) {
            Logger.getLogger(SensonetClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Datasource findOrCreateDataSource(String name) {
        if (project != null) {
            for (Datasource dts : project.datasources) {
                if (dts.owner == user.id) {
                    myDatasource = dts;
                }
            }

            if (myDatasource == null) {

                String url = baseUrl.concat("project/datasources/") + project.id;
                HttpPost post = new HttpPost(url);
                BasicNameValuePair param = new BasicNameValuePair("name", name);
                List<NameValuePair> params = new ArrayList<>();
                params.add(param);
                try {
                    post.setEntity(new UrlEncodedFormEntity(params));
                    CloseableHttpResponse result = client.execute(post);

                    int statusCode = result.getStatusLine().getStatusCode();
                    System.out.println(result.getStatusLine());
                    if (statusCode == 200) {
                        JSONObject json = new JSONObject(EntityUtils.toString(result.getEntity()));
                        System.out.println(result.getEntity().toString());
                        myDatasource = new Datasource(json);
                        return myDatasource;
                    } else {
                        System.out.println(statusCode);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SensonetClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println("Ya existe un dts");
                return myDatasource;
            }
        }
        return null;
    }
    
    private void ready(){
        if(variables.size()>0 && listener!=null){
            listener.ready();
        }
    }
    
    public List<Variable> findOrCreateVariables(List<SensorView> sensors) {
        //int count = myDatasources.get(0).getVariables().size();
        try {
            for (SensorView sv : sensors) {
                List<NameValuePair> nvps = new ArrayList<>();
                nvps.add(new BasicNameValuePair("name", sv.getName()));
                nvps.add(new BasicNameValuePair("unit", sv.getSensor().getUnit()));
                nvps.add(new BasicNameValuePair("idSensor", sv.getSensor().getId()));

                HttpPost post = new HttpPost(baseUrl.concat("/project/datasources/variable/")
                         + myDatasource.id);
                post.setEntity(new UrlEncodedFormEntity(nvps));

                CloseableHttpResponse result = client.execute(post);
                int statusCode = result.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    JSONObject json = new JSONObject(EntityUtils.toString(result.getEntity()));
                    Variable v = new Variable(json);
                    v.addSensor(sv.getSensor());
                    variables.add(v);
                     System.err.println(statusCode + " Nueva variable " + v.name);
                } else {
                    System.err.println(statusCode + " No se pudo crear la variable ");
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(SensonetClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        ready();
        return variables;

    }
    
    private void checkingNetwork() {
        netTimer = new Timer();
        netTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                boolean status = netIsAvailable();
                if (status != isOnline) {
                    isOnline = status;
                    System.out.println("Network " + status);
                    if (listener != null) {
                        listener.isOnline(isOnline);
                    }
                }
            }
        }, 0, 5000);
    }
 
    public static boolean netIsAvailable() {
        try {
            final URL url = new URL(baseUrl);
            final URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }

    private void stopCheckingNetwork() {
        System.out.println("Closing checking network");
        if(netTimer != null){
            netTimer.cancel();
            netTimer=null;
        }
    }
    
    public void close() {
        stop();
        stopCheckingNetwork();
        isLogged = false;
        if (client != null) {
            try {
                client.close();
            } catch (IOException ex) {
                Logger.getLogger(SensonetClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    
    //
    public void logout(){
        try {
            HttpGet get = new HttpGet(baseUrl.concat("logout"));
            CloseableHttpResponse result = client.execute(get);
            int statusCode = result.getStatusLine().getStatusCode();
            if(statusCode == 200)
                System.out.println("sensonet logout");
            else
                System.err.println("error al desconectar de sensonet");
            isLogged = false;
        } catch (IOException ex) {
            Logger.getLogger(SensonetClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isIsLogged() {
        return isLogged;
    }

    public void setIsLogged(boolean isLogged) {
        this.isLogged = isLogged;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }
    
    public void addSensor(SensorView s){
       
        if(!stop &&  !sensors.contains(s) && netIsAvailable()){
            try {
                List<NameValuePair> nvps = new ArrayList<>();
                nvps.add(new BasicNameValuePair("name", s.getName()));
                nvps.add(new BasicNameValuePair("unit", s.getSensor().getUnit()));
                nvps.add(new BasicNameValuePair("idSensor", s.getSensor().getId()));
                String url = baseUrl.concat("/project/datasources/variable/")
                         + myDatasource.id;
                HttpPost post = new HttpPost(url);
                post.setEntity(new UrlEncodedFormEntity(nvps));
                System.out.println("execute "+url);
                CloseableHttpResponse result = client.execute(post);
                int statusCode = result.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    JSONObject json = new JSONObject(EntityUtils.toString(result.getEntity()));
                    Variable v = new Variable(json);
                    v.addSensor(s.getSensor());
                    variables.add(v);
                    System.err.println(statusCode + "Nueva variable " + v.name);
                } else {
                    System.err.println(statusCode + " No se pudo crear la variable ");
                }
            } catch (Exception ex) {
                Logger.getLogger(SensonetClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            System.out.println( s.getName() + " este sensor se listará cuando comience la medición");
        }
    }
    
    public void removeSensor(SensorView s){
        for(Variable v : variables){
            if(v.sensor.equals(s.getSensor())){
                variables.remove(v);
                sensors.remove(s);
                return;
            }
        }
    }

    public Project setToken(String token) {
        this.token = token;
        this.project = getProjectByToken();
        if(project!= null) System.out.println(project.name);
        else System.err.println("no se encontró el projecto");
        return this.project;
    }

    
}
