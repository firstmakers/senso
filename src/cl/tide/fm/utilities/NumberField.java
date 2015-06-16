/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.utilities;

import javafx.geometry.Pos;
import javafx.scene.control.TextField;

/**
 *
 * @author eDelgado
 */
public class NumberField extends TextField {
       private int minValue = 1;
       private int maxValue = 100000000;
       private int value;

    public NumberField() {
        setAlignment(Pos.TOP_RIGHT);
        setText(minValue+"");
    }

       
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
    }
              
       @Override
    public void replaceText(int start, int end, String text) {
        String oldValue = getText();

        if (!text.matches("[A-Za-z]") && !text.matches("[\\\\!\"#$%&()*+,./:;<=>?@\\[\\]^_{|}~]+")|| text.isEmpty()) {
            super.replaceText(start, end, text);
        
        }
        if (getText().length() > 9) {
            setText(oldValue);
        }
      
    }
    

    @Override
    public void replaceSelection(String text) {

        if (!text.matches("[A-Za-z]") && !text.matches("[\\\\!\"#$%&()*+,./:;<=>?@\\[\\]^_{|}~]+")|| text.isEmpty()) {
          
           super.replaceSelection(text);     
        }  
        
    }
    

}
