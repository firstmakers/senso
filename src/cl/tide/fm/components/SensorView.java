/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.components;

import cl.tide.fm.model.CustomSeries;
import cl.tide.fm.model.Sensor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 *
 * @author Edison Delgado
 */
public class SensorView extends VBox{
    
    @FXML private Text decimal;
    @FXML private Text integer;
    @FXML private TextField name;
    @FXML private Text unit;
    @FXML private CheckBox cbx;
    @FXML private ImageView icon;
    @FXML private Rectangle background;
    @FXML private ColorPicker colorPicker;
    private FXMLLoader fxmlLoader;
    private String ID;
    private Color color;
    private CustomSeries customSerie;
    private Sensor sensor;
    private List<ViewChanged> listener;
    private boolean serieVisibility;

    /**
     *
     */
    public SensorView(){
        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/BaseSensor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
   
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        
       colorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setColor(colorPicker.getValue());
                setBackgroundColor(getColor());
                setSerieColor();
            }
        });
       listener = new ArrayList<>();
       cbx.setSelected(true);
       serieVisibility = true;
       cbx.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue){
                 //System.out.println("oldvalue " +oldValue+" newValue "+ newValue); 
            
                for(ViewChanged list : listener){
                    list.changed(serieVisibility, customSerie, newValue);
                     if(newValue)
                        setSerieColor();
                }            
            }          
        });
    }
    
    public void addListener(ViewChanged listener){
        if(!this.listener.contains(listener))
            this.listener.add(listener);
    }
    
    public void setSerieColor(){
        String rgb = String.format("%d, %d, %d",
        (int) (color.getRed() * 255),
        (int) (color.getGreen() * 255),
        (int) (color.getBlue() * 255));
        
        Node node = getCustomSerie().getSerie().getNode();
        if(node != null)
            node.setStyle("-fx-stroke: rgb("+rgb+", 1.0);");
    }
    public void setValue(String value){
        this.integer.setText(value.concat("."));
    }
    
    public void setIcon(Image img){
        this.icon.setImage(img);
        this.icon.setSmooth(true);
    }
  
    public void setBackgroundColor(Paint value){
        this.background.setFill(value);
    }
    public void setCssStyle (String style){
        this.getStylesheets().add(style);
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
    public void setDecimal(String t){
        this.decimal.setText(t);
    }
    public void setUnit(String t){
        this.unit.setText(t);
    }

    public String getName() {
        return name.getText();
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.colorPicker.setValue(color);
        
    }

    public CustomSeries getCustomSerie() {
        return customSerie;
    }

    public void setCustomSerie(CustomSeries customSerie) {
        this.customSerie = customSerie;
        //setSerieColor();
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public boolean isSerieVisibility() {
        return serieVisibility;
    }

    public void setSerieVisibility(boolean serieVisibility) {
        this.serieVisibility = serieVisibility;
    }
}
