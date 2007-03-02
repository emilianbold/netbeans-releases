/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.propertyeditors.css.model;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author  Winston Prakash
 */
public class ColorModel {
    
    private Color color = Color.BLACK;
    
    private Map colorNameHexMap = new HashMap();
    
    /** Creates a new instance of ColorModel */
    public ColorModel() {
        colorNameHexMap.put("black","#000000"); //NOI18N
        colorNameHexMap.put("gray","#808080"); //NOI18N
        colorNameHexMap.put("white","#FFFFFF"); //NOI18N
        colorNameHexMap.put("maroon","#800000"); //NOI18N
        colorNameHexMap.put("red","#FF0000"); //NOI18N
        colorNameHexMap.put("purple","#800080"); //NOI18N
        colorNameHexMap.put("fuchsia","#FF00FF"); //NOI18N
        colorNameHexMap.put("green","#008000"); //NOI18N
        colorNameHexMap.put("lime","#00FF00"); //NOI18N
        colorNameHexMap.put("olive","#808000"); //NOI18N
        colorNameHexMap.put("orange","#FFA500"); //NOI18N
        colorNameHexMap.put("yellow","#FFFF00"); //NOI18N
        colorNameHexMap.put("navy","#000080"); //NOI18N
        colorNameHexMap.put("blue","#0000FF"); //NOI18N
        colorNameHexMap.put("teal","#008080"); //NOI18N
        colorNameHexMap.put("aqua","#00FFFF"); //NOI18N
    }
    
    public DefaultComboBoxModel getColorList(){
        return new ColorList();
    }
    
    public Color getColor(){
        return color;
    }
    
    public void setColor(Color newColor){
        color = newColor;
    }
    
    public void setColor(String newColor){
        if(newColor.startsWith(CssStyleData.NOT_SET)){
            setColor(Color.BLACK);
        }else if(newColor.startsWith("#")){ //NOI18N
            // Hex Color - #FFFFFF
            setHexColor(newColor);
        }else if (newColor.startsWith("rgb")){ //NOI18N
            // RGB Color - rgb(255,255,255);
            String rgbString = newColor.substring(newColor.indexOf("(")+1,newColor.indexOf(")")); //NOI18N
            try{
                StringTokenizer st = new StringTokenizer(rgbString,","); //NOI18N
                while(st.hasMoreTokens()){
                    int red = Integer.parseInt(st.nextToken().trim());
                    int green = Integer.parseInt(st.nextToken().trim());
                    int blue = Integer.parseInt(st.nextToken().trim());
                    color = new Color(red,green,blue);
                }
            }catch(Exception exc){
                color = Color.BLACK;
            }
        }else {
            String hexValue =  (String) colorNameHexMap.get(newColor);
            if(hexValue != null){
                setHexColor(hexValue);
            }
        }
    }
    
    public void setHexColor(String hexColor){
        int start = 0;
        if(hexColor.startsWith("#")){
            start = 1;
        }
        try{
            int red = Integer.parseInt(hexColor.substring(start,start+2),16);
            int green = Integer.parseInt(hexColor.substring(start+2, start+4),16);
            int blue = Integer.parseInt(hexColor.substring(start+4, start+6),16);
            color = new Color(red,green,blue);
        }catch(Exception exc){
            color = Color.BLACK;
        }
    }
    
    public String getHexColor(){
        String redHex = Integer.toHexString(color.getRed());
        if(redHex.length()<2) redHex = "0" + redHex; //NOI18N
        String greenHex = Integer.toHexString(color.getGreen());
        if(greenHex.length()<2) greenHex = "0" + greenHex; //NOI18N
        String blueHex = Integer.toHexString(color.getBlue());
        if(blueHex.length()<2) blueHex = "0" + blueHex; //NOI18N
        
        String hexString = "#" + redHex + greenHex + blueHex; //NOI18N
        
        return hexString;
    }
    
    public class ColorList extends DefaultComboBoxModel{
        public ColorList(){
            addElement(CssStyleData.NOT_SET);
            for(Iterator iter=colorNameHexMap.keySet().iterator(); iter.hasNext();){
                addElement(iter.next());
            }
        }
    }
    
}
