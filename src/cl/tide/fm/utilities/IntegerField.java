/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.utilities;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author edisondelgado
 */
public class IntegerField extends TextField {
    private IntegerProperty value;

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        this.maxLength = Integer.toString(maxValue).length(); 
    }
    
     private int minValue;
     private int maxValue;
     private int maxLength;
     protected ValueChange listener;

    public void addListener(ValueChange l) {
        listener = l;
    }

    // expose an integer value property for the text field.
    public int getValue() {
        return value.getValue();
    }

    public void setValue(int newValue) {
        this.setText(String.format("%02d", newValue));
        if (newValue != getValue()) {
            value.setValue(newValue);
            if (listener != null) {
                listener.onChange(newValue);
            }
        }
    }
    
    public IntegerProperty valueProperty() { return value; }

    public IntegerField() {
        this.IntegerField(0,100000,0);
    }
    
    public void IntegerField(int minValue, int maxValue, int initialValue) {
      if (minValue > maxValue) 
        throw new IllegalArgumentException(
          "IntField min value " + minValue + " greater than max value " + maxValue
        );
      if (maxValue < minValue) 
        throw new IllegalArgumentException(
          "IntField max value " + minValue + " less than min value " + maxValue
        );
      if (!((minValue <= initialValue) && (initialValue <= maxValue))) 
        throw new IllegalArgumentException(
          "IntField initialValue " + initialValue + " not between " + minValue + " and " + maxValue
        );
      this.getStyleClass().add("IntegerField");
      // initialize the field values.
      this.minValue = minValue;
      this.maxValue = maxValue;
      value = new SimpleIntegerProperty(initialValue);
      setText(initialValue + "");
 
      final IntegerField intField = this;
 
      // make sure the value property is clamped to the required range
      // and update the field's text to be in sync with the value.
      value.addListener((ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) -> {
          if (newValue == null) {
              intField.setText("00");
          } else {
              int mValue = newValue.intValue();
              if (mValue < intField.minValue) {
                  setValue(intField.minValue);
                  return;
              }
              
              if (mValue > intField.maxValue) {
                  setValue(intField.maxValue);
                  return;
              }
              
              if (mValue == 0 && (textProperty().get() == null || "".equals(textProperty().get()))) {
                  // no action required, text property is already blank, we don't need to set it to 0.
              } else {
                  setValue(mValue);
              }
          }
      });
 
      // restrict key input to numerals.
      this.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent keyEvent) -> {
          //Permite sólo números y la tecla backspace (Borrar)
          if (!"0123456789".contains(keyEvent.getCharacter()) && keyEvent.getCode()!= KeyCode.BACK_SPACE) {
              keyEvent.consume();
          }
      });
      
      // ensure any entered values lie inside the required range.
      this.textProperty().addListener((ObservableValue<? extends String> observableValue, String oldValue, String newValue) -> {
    
          String regex = "^[0-9]+$"; //solo numeros
          
          if( !newValue.matches(regex) || newValue.isEmpty() || newValue.length() > maxLength+1){
              setValue( getValue());
              return;
          }
          final int intValue = Integer.parseInt(newValue);
 
          if (intField.minValue > intValue || intValue > intField.maxValue) {
              setValue(getValue());
              return;
          }
          setValue(intValue);
      });
      //reemplaza el menu contextual por uno vacio
      this.setContextMenu(new ContextMenu());

    }
    
    public interface ValueChange{
        public void onChange(int value);
    }
    
}
