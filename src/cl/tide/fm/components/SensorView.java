/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.components;

import cl.tide.fm.controller.SettingsController;
import cl.tide.fm.model.CustomSeries;
import cl.tide.fm.model.Sensor;
import com.ubidots.Variable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 *
 * @author Edison Delgado
 */
public class SensorView extends VBox {
    
    @FXML public Text decimal;
    @FXML public Text integer;
    @FXML public TextField name;
    @FXML public Text unit;
    @FXML public CheckBox cbx;
    @FXML public ImageView icon;
    @FXML private Rectangle background;
    @FXML public ColorPicker colorPicker;
    @FXML private MenuButton sensorMenu;
    private FXMLLoader fxmlLoader;
    private String ID;
    private Color color;
    private CustomSeries customSerie;
    private Sensor sensor;
    private List<ViewChanged> listener;
    private boolean serieVisibility;
    private boolean animation;
    private List<ViewNameChanged> nameListener;
    protected float[] data;
    private String UbidotsID;
    private Variable  ubidotsVariable;


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
        
       colorPicker.setOnAction((ActionEvent event) -> {
           Color oldColor = getColor();
           setColor(colorPicker.getValue());
           Color newColor = getColor();
           if(animation) animateBackground(background, oldColor, newColor);
           setBackgroundColor(newColor);
           setSerieColor();
           if(nameListener!=null && nameListener.size()>0){
               nameListener.forEach((l)->{l.onNameSensorChange(getName(), "");});
           }
        });
       nameListener = new ArrayList<>();
       listener = new ArrayList<>();
       cbx.setSelected(true);
       serieVisibility = true;
       cbx.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
           //System.out.println("oldvalue " +oldValue+" newValue "+ newValue);
           for(ViewChanged list : listener){
               list.changed(serieVisibility, customSerie, newValue);
               if(newValue)
                   setSerieColor();
           }          
        });       
       
       name.textProperty().addListener((observable, oldValue, newValue)->{
           if(customSerie!= null){
               customSerie.setName(newValue);
               System.out.println("nuevo nombre "+ newValue);
               if(nameListener!=null)
                   nameListener.forEach((l)->{l.onNameSensorChange(newValue, oldValue); });   
           }
       });
       //UbidotsID = SettingsController.getUbidotsVariable(getID());     
    }

    /*
    * Anima el color de una figura, se utiliza para animar el 
    * fondo de los sensores cuando cambian de color.
    * @param shape: figura que se animará.
    * @param from: color actual
    * @param to : próximo color.
     */
    private void animateBackground(Shape shape, Color from, Color to ){
        FillTransition ft = new FillTransition(new Duration(400));
        ft.setShape(shape);
        ft.setFromValue(from);
        ft.setToValue(to);
        ft.play();
    }
    public void animateText(Text text ,double from, double to){
        FadeTransition ft = new FadeTransition(new Duration(400));
        ft.setNode(text);
        ft.setFromValue(from);
        ft.setToValue(to);
        ft.play(); 
    }
    
    public void addListener(ViewChanged listener){
        if(!this.listener.contains(listener))
            this.listener.add(listener);
    }
    
    public void addListener(ViewNameChanged listener){
        if(!this.nameListener.contains(listener))
            this.nameListener.add(listener);
    }
    
    public void removeListener(ViewChanged listener){
        this.listener.remove(listener);
    }
    
    public void removeListener(ViewNameChanged listener){
         boolean x = this.nameListener.remove(listener);
         System.err.println(x==true? "Removido "+listener :"No existe "+ listener);
    }
    /*
     * Aplica el color actual del sensor a la serie del gráfico
     */
    public void setSerieColor() {
        String rgb = String.format("%d, %d, %d",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
        try {

            Node node = getCustomSerie().getSerie().getNode();
            if (node != null) {
                node.setStyle("-fx-stroke: rgb(" + rgb + ", 1.0); -fx-background-color:rgb(" + rgb + ", 1.0); ;");
            }
        } catch (Exception e) {
            System.out.println("exception " + e.getMessage());
        }
    }

    public void setValue(String value) {
        if(animation)  
            animateText(integer, 0.0, 1.0);  
        integer.setText(value.concat("."));
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
    public void setDecimal(String t) {
        if (animation) 
            animateText(decimal, 0.0, 1.0);
        decimal.setText(t);
    }
    
    public void setUnit(String t){
        this.unit.setText(t);
    }

    public String getName() {
        return name.getText();
    }

    public void setName(String name) {
        System.err.println("name changed "+ name);
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

    public boolean isAnimation() {
        return animation;
    }

    public void setAnimation(boolean animation) {
        this.animation = animation;
    }
    public String getUbidotsID() {
        return UbidotsID;
    }

    public void setUbidotsID(String UbidotsID) {
        this.UbidotsID = UbidotsID;
    }
    
    public interface ViewNameChanged{
        public void onNameSensorChange(String newValue, String oldValue);
    }
    public Variable getUbidotsVariable() {
        return ubidotsVariable;
    }

    public void setUbidotsVariable(Variable ubidotsVariable) {
        this.ubidotsVariable = ubidotsVariable;
    }
   
}
