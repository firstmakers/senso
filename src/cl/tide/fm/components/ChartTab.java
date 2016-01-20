/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.components;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Muestra y controla la vista del gráfico que se asigna a un Tab,
 * 
 * @author Edison Delgado
 */
public class ChartTab {
    
    private final CategoryAxis xAxis = new CategoryAxis();
    private final NumberAxis yAxis = new NumberAxis();
    public final LineChart<String,Number> lineChart;
    private long interval;
    private Timer timer;
    private boolean running = false;
    private ContextMenu cMenu;
    private MenuItem chartManager;
    private int maxPoint = 50;
    public List<SensorView> sensors;
    
    
    public ChartTab(String title){
        sensors = new ArrayList<>();
        this.interval = 5000;
        xAxis.setLabel("Hora");
        lineChart = new LineChart<>(xAxis,yAxis);
        lineChart.setTitle(title);
        lineChart.setCreateSymbols(false);//remove symbols 
        lineChart.setLegendVisible(false);
        lineChart.getXAxis().setAutoRanging(true);
        lineChart.getXAxis().setTickMarkVisible(false);
        lineChart.setAnimated(false);
        cMenu = setContextMenu();
         
        //Manejador del clic derecho sobre el gráfico
        lineChart.setOnMouseClicked((MouseEvent event) -> {
            if (MouseButton.SECONDARY.equals(event.getButton())) {
                //muestra el menú en las coordenadas donde se hace clic derecho
                cMenu.show(lineChart.getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });
    }
    
    /*Agrega un nuevo sensor*/
    public void addSensorview(SensorView s){
        Platform.runLater(()->{
            addSerie(s.getCustomSerie().getSerie());
            sensors.add(s);
            s.setSerieColor();
        });

    }
    
    /*Elimina un sensor del gráfico*/
    public void removeSensorview(SensorView s){
        Platform.runLater(()->{
            removeSerie(s.getCustomSerie().getSerie());
            sensors.remove(s);
        });
    }
    
    /*Asigna una etiqueta al eje Y del gráfico*/
    public void setYAxisLabel(String label){
        yAxis.setLabel(label);
    }
    /*Asigna una etiqueta al eje X del gráfico*/
    public void setXAxisLabel(String label){
        xAxis.setLabel(label);
    }
    /*Agrega una serie al gráfico*/
    public void addSerie(XYChart.Series serie){
        lineChart.getData().add(serie);
    }
    /*Elimina una serie del gráfico*/
    public void removeSerie(XYChart.Series serie){
        lineChart.getData().remove(serie);
    }
    
    /*
        Grafica según el intervalo definido por el usuario 
        todos los sensores que están agregados. Por defecto se muestran los últimos 
        50 registros
    */
    public synchronized void plot() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (sensors.size() > 0) {
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    Date d = new Date();
                    String date = dateFormat.format(d);
                    sensors.stream().forEach((view) -> {
                        Double data = view.getSensor().getValue();
                        final XYChart.Data<String, Double> value = new XYChart.Data<>(date, data);
                        Platform.runLater(() -> {
                            if (view.getCustomSerie().getSerie().getData().size() > maxPoint) {
                                view.getCustomSerie().getSerie().getData().remove(0);
                            }
                            view.getCustomSerie().addValue(value);
                        });
                    });
                }
            }
        }, 0, interval);
    }
    
    /*
        Menú contextual que se despliega haciendo clic derecho sobre el gráfico
    */
    private ContextMenu setContextMenu() {
        chartManager = new MenuItem("Iniciar gráfico");
        MenuItem clearChart = new MenuItem("Borrar gráfico");
        clearChart.setOnAction(e -> {
            clear();           
        });
        chartManager.setOnAction(e -> {
            if (sensors.size() > 0) {
                if (isRunning()) {
                    stop();
                    //GlyphsDude.setIcon(chartManager, FontAwesomeIcon.PLAY, "1.5em");
                } else {
                    start(interval);             
                    //GlyphsDude.setIcon(chartManager, FontAwesomeIcon.STOP, "1.5em");
                }
            }
        });        
        ContextMenu menu = new ContextMenu(
                chartManager,
                clearChart
        );      
        return menu;
    }
    
    /*
        Inicia el gráfico
    */
    public void start(long interval) {
        chartManager.setText("Detener gráfico");
        System.out.println(interval);
        setInterval(interval);
        running = true;
        timer = new Timer();
        plot();
    }
    
    /*
        Detiene el gráfico
    */
    public void stop() {
        chartManager.setText("Iniciar gráfico");
        running = false;
        if(timer!=null)
            timer.cancel();
        timer = null;

    }
    /*
        Retorna verdadero cuando el gráfico está corriendo
    */
    public boolean isRunning() {
        return running;
    }

    /*
        Limpia los datos que contienen las series del gráfico
    */
    public void clear(){
        sensors.stream().forEach((s) -> {
            s.getCustomSerie().getSerie().getData().clear();
        });
    }
    /*
        Obtiene el intervalo en milisegundos
    */
    public long getInterval() {
        return interval;
    }
    /*
        Asigna un intervalo en milisegundos
    */
    public void setInterval(long interval) {
        this.interval = interval;
    }
    
    /*
        Obtiene el máximo número de puntos que se pueden mostrar en el gráfico
    */
    public int getMaxPoint() {
        return maxPoint;
    }

    /*
        Asigna el máximo número de puntos que se pueden mostrar en el gráfico
    */
    public void setMaxPoint(int maxPoint) {
        this.maxPoint = maxPoint;
    }

}
