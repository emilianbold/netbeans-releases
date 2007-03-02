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

import java.util.StringTokenizer;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author  Winston Prakash
 */
public class BackgroundPositionData {
    
    /**
     * Holds value of property horizontalUnit.
     */
    private String horizontalUnit = "px"; //NOI18N
    
    /**
     * Holds value of property verticalUnit.
     */
    private String verticalUnit = "px"; //NOI18N
    
    /**
     * Holds value of property verticalValue.
     */
    private String verticalValue = ""; //NOI18N
    
    /**
     * Holds value of property horizontalValue.
     */
    private String horizontalValue = ""; //NOI18N
    
    /** Creates a new instance of BackgroundPositionData */
    public BackgroundPositionData() {
    }
    
    public void setBackgroundPosition(String bgPositionStr){
        StringTokenizer st = new StringTokenizer(bgPositionStr);
        if(bgPositionStr.indexOf(",") != -1){
            st = new StringTokenizer(bgPositionStr, ",");
        }else{
            st = new StringTokenizer(bgPositionStr);
        }
        // Horizontal Postion
        if(st.hasMoreTokens()){
            String token = st.nextToken();
            horizontalUnit = getUnit(token);
            horizontalValue = token.replaceAll(horizontalUnit,"");
        }
        if(st.hasMoreTokens()){
            String token = st.nextToken();
            verticalUnit = getUnit(token);
            verticalValue = token.replaceAll(verticalUnit,"");
        }
    }
    
    private String getUnit(String postionStr){
        DefaultComboBoxModel unitList = new BackgroundModel().getBackgroundPositionUnitList();
        for(int i=0; i< unitList.getSize(); i++){
            String unit = (String)unitList.getElementAt(i);
            if(postionStr.endsWith(unit)){
                return unit;
            }
        }
        return "";
    }
    
    /**
     * Setter for property horizontalUnit.
     * @param horizontalUnit New value of property horizontalUnit.
     */
    public void setHorizontalUnit(String horizontalUnit) {
        this.horizontalUnit = horizontalUnit;
    }
    
    public String getHorizontalUnit() {
        return this.horizontalUnit;
    }
    
    /**
     * Setter for property verticalUnit.
     * @param verticalUnit New value of property verticalUnit.
     */
    public void setVerticalUnit(String verticalUnit) {
        this.verticalUnit = verticalUnit;
    }
    
    public String getVerticalUnit() {
        return this.verticalUnit;
    }
    
    /**
     * Setter for property verticalValue.
     * @param verticalValue New value of property verticalValue.
     */
    public void setVerticalValue(String verticalValue) {
        this.verticalValue = verticalValue;
    }
    
    public String getVerticalValue() {
        return this.verticalValue;
    }
    
    /**
     * Setter for property horizontalValue.
     * @param horizontalValue New value of property horizontalValue.
     */
    public void setHorizontalValue(String horizontalValue) {
        this.horizontalValue = horizontalValue;
    }
    
    public String getHorizontalValue() {
        return this.horizontalValue;
    }
    
    public String toString(){
        String bgPosition = "";
        if (!(horizontalValue.equals("") || horizontalValue.startsWith(CssStyleData.NOT_SET))){
            bgPosition += horizontalValue;
            if(Utils.isInteger(horizontalValue)){
                bgPosition += horizontalUnit;
            }
        }
        
        if (!(verticalValue.equals("") || verticalValue.startsWith(CssStyleData.NOT_SET))){
            bgPosition = bgPosition + " " + verticalValue;
            if(Utils.isInteger(verticalValue)){
                bgPosition += verticalUnit;
            }
        }
        
        return bgPosition;
    }
}
