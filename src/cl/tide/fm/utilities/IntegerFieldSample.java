/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.tide.fm.utilities;

import java.text.DecimalFormat;

/**
 *
 * @author edisondelgado
 */
public class IntegerFieldSample extends  IntegerField
{

    public IntegerFieldSample() {
        super();
    }
    
    @Override
    public void setValue(int v){
        this.valueProperty().setValue(v);
        DecimalFormat df = new DecimalFormat("###.###");
        this.setText(df.format(v));
        if(listener != null){
            listener.onChange(v);
        }
    }
    
}
