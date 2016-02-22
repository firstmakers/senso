/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.controller;

import cl.tide.fm.components.SensorView;
import com.ubidots.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 *
 * @author edisondelgado
 */
public class UbidotsClient {

    public static final String NET_ERROR = "No se puede establecer la conección con el servidor";
    public static final String TIME_OUT = "Se agotó el tiempo de respuesta";
    public static final String UNKNOWN = "Error desconocido";

    private ApiClient client;
    private static boolean isOnline = false;
    private UbidotsEvent event;
    private final String apikey;
    DataSource dataSource;
    public static Variable[] variables;

    private final long timeout = 10000;
    private Timer timer;
    private Timer netTimer;
    private List<SensorView> sensors;
    
    public UbidotsClient(String apikey) {
        this.apikey = apikey;
        checkingNetwork();
    }

    public void tryToConnect(List<SensorView> sensors) {
        this.sensors = sensors;
        System.err.println("Attempt connectUbibots....");
        Thread attemptConnection = new Thread(new Task<DataSource>() {
        @Override
        protected DataSource call() throws Exception {
            DataSource dataSource = null;
            if (netIsAvailable()) {
                client = new ApiClient(apikey);
                dataSource = client.createDataSource("Senso");
                System.out.println("creando nuevo ds ubidots.");

                if (dataSource != null) {

                    int length = sensors.size();
                    variables = new Variable[length];
                    for (int count = 0; length > count; count++) {
                        SensorView s = sensors.get(count);
                        System.out.println(s.getName());
                        variables[count] = dataSource.createVariable(s.getName());
                        s.setUbidotsVariable(variables[count]);
                        System.out.println("variable " +variables[count].getName());
                    }
                    isOnline = true;
                    if (event != null) {
                        event.isOnline(isOnline);
                    }
                    startUpdate(sensors);//comienza a enviar datos.
                } else {
                    System.out.println("error al crear el datasource");
                }
                return dataSource;
            } else {
                if (event != null) {
                    event.onErrorOcurred(NET_ERROR);
                }
                return null;
            }
        }
    });
        attemptConnection.setDaemon(true);
        attemptConnection.start();
    }


    public void addListener(UbidotsEvent e){
        this.event = e;
    }
    
    public void removeListener(){
        this.event = null;
    }
    
    /*public void mapSensor(SensorView s) {
        if (dataSource != null) {
            Thread vs = new Thread(new Runnable() {
                @Override
                public void run() {
                            Variable var = dataSource.createVariable(s.getName(), s.getSensor().getUnit());
                            s.setUbidotsID(var.getId());
                } 
            });
            vs.setDaemon(true);
            vs.start();
        }
    }
  
    public void mapSensors(ArrayList<SensorView> sensors) {
        List<SensorView> tmplist = new ArrayList<>(sensors.size());
        if (dataSource != null) {
            Thread vs = new Thread(new Runnable() {
                @Override
                public void run() {
                    sensors.forEach((SensorView s) -> {
                        String id = s.getUbidotsID();
                        if (id.isEmpty()) {
                            tmplist.add(s);
                        }
                    });
                    if (tmplist.size() > 0) {
                        tmplist.forEach((s) -> {
                            Variable var = dataSource.createVariable(s.getName(), s.getSensor().getUnit());
                            s.setUbidotsID(var.getId());
                        });
                    }
                }
            });
            vs.setDaemon(true);
            vs.start();
        }
    }*/
    
    public void startUpdate(List<SensorView> sensors) {
        long interval = SettingsController.getInterval();
        interval = interval < 10000 ? 10000 : interval;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isOnline) {
                    sensors.forEach((s) -> {
                        Variable var = s.getUbidotsVariable();
                        System.out.println(var);
                        if (var != null) {
                            var.saveValue(s.getSensor().getValue());
                            System.out.println("");
                        }
                    });
                }else{
                    //almacenar los datos  y envialos luego?
                    System.err.println("ubidots offline");
                }
            }
        }, 0, interval);
    }

    public void stopUpdate(){
        if(timer!=null){
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }
    
    public void close(){
        stopUpdate();
        stopCheckingNetwork();
        System.gc();
    }
  
    private static boolean netIsAvailable() {
        try {
            final URL url = new URL("http://ubidots.com/");
            final URLConnection conn = url.openConnection();
            conn.connect();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }

    private void checkingNetwork() {     
        netTimer = new Timer(); 
        netTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                boolean  status = netIsAvailable();
                   if(status != isOnline){
                       isOnline = status;
                       if(event != null)
                           event.isOnline(isOnline);
                   }
            }
        },0,5000);
    }

    private void stopCheckingNetwork() {
        if(netTimer != null){
            netTimer.cancel();
            netTimer.purge();
            netTimer=null;
        }
    }

    interface UbidotsEvent{
        public void isOnline(boolean status);
        public void onErrorOcurred(String error);
        public void onStop();
    }
    
}
