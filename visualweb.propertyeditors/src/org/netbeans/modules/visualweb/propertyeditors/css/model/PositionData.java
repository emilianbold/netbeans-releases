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

import javax.swing.DefaultComboBoxModel;

/**
 * Position data to initialize the Style Editor GUI
 * based on value and unit
 * @author  Winston Prakash
 */
public class PositionData {
    /**
     * Holds value of property topUnit.
     */
    private String topUnit="px"; //NOI18N
    
    /**
     * Holds value of property rightUnit.
     */
    private String rightUnit="px"; //NOI18N
    
    /**
     * Holds value of property leftUnit.
     */
    private String leftUnit="px"; //NOI18N
    
    /**
     * Holds value of property bottomUnit.
     */
    private String bottomUnit="px"; //NOI18N
    
    /**
     * Holds value of property widthUnit.
     */
    private String widthUnit="px"; //NOI18N
    
    /**
     * Holds value of property heightUnit.
     */
    private String heightUnit="px"; //NOI18N
    
    /**
     * Holds value of property bottomValue.
     */
    private String bottomValue="";
    
    /**
     * Holds value of property heightValue.
     */
    private String heightValue="";
    
    /**
     * Holds value of property leftValue.
     */
    private String leftValue="";
    
    /**
     * Holds value of property rightValue.
     */
    private String rightValue="";
    
    /**
     * Holds value of property topValue.
     */
    private String topValue="";
    
    /**
     * Holds value of property widthValue.
     */
    private String widthValue="";
    
    public void setTop(String topStr){
        topUnit = getUnit(topStr);
        topValue = topStr.replaceAll(topUnit,"").trim();
    }
    
    public void setBottom(String bottomStr){
        bottomUnit = getUnit(bottomStr);
        bottomValue = bottomStr.replaceAll(bottomUnit,"").trim();
    }
    
    public void setLeft(String leftStr){
        leftUnit = getUnit(leftStr);
        leftValue = leftStr.replaceAll(leftUnit,"").trim();
    }
    
    public void setRight(String rightStr){
        rightUnit = getUnit(rightStr);
        rightValue = rightStr.replaceAll(rightUnit,"").trim();
    }
    
    public void setWidth(String widthStr){
        widthUnit = getUnit(widthStr);
        widthValue = widthStr.replaceAll(widthUnit,"").trim();
    }
    
    public void setHeight(String heightStr){
        heightUnit = getUnit(heightStr);
        heightValue = heightStr.replaceAll(heightUnit,"").trim();
    }
    
    private String getUnit(String positionStr){
        DefaultComboBoxModel unitList = new PositionModel().getPositionUnitList();
        for(int i=0; i< unitList.getSize(); i++){
            String unit = (String)unitList.getElementAt(i);
            if(positionStr.trim().endsWith(unit)){
                return unit;
            }
        }
        return "";
    }
    
    /**
     * Getter for property topUnit.
     * @return Value of property topUnit.
     */
    public String getTopUnit()  {
        
        return this.topUnit;
    }
    
    /**
     * Setter for property topUnit.
     * @param topUnit New value of property topUnit.
     */
    public void setTopUnit(java.lang.String topUnit)  {
        
        this.topUnit = topUnit;
    }
    
    /**
     * Getter for property rightUnit.
     * @return Value of property rightUnit.
     */
    public String getRightUnit()  {
        
        return this.rightUnit;
    }
    
    /**
     * Setter for property rightUnit.
     * @param rightUnit New value of property rightUnit.
     */
    public void setRightUnit(java.lang.String rightUnit)  {
        
        this.rightUnit = rightUnit;
    }
    
    /**
     * Getter for property leftUnit.
     * @return Value of property leftUnit.
     */
    public String getLeftUnit()  {
        
        return this.leftUnit;
    }
    
    /**
     * Setter for property leftUnit.
     * @param leftUnit New value of property leftUnit.
     */
    public void setLeftUnit(java.lang.String leftUnit)  {
        
        this.leftUnit = leftUnit;
    }
    
    /**
     * Getter for property bottomUnit.
     * @return Value of property bottomUnit.
     */
    public String getBottomUnit()   {
        
        return this.bottomUnit;
    }
    
    /**
     * Setter for property bottomUnit.
     * @param bottomUnit New value of property bottomUnit.
     */
    public void setBottomUnit(java.lang.String bottom) {
    }
    
    
    /**
     * Getter for property widthUnit.
     * @return Value of property widthUnit.
     */
    public String getWidthUnit()  {
        
        return this.widthUnit;
    }
    
    /**
     * Setter for property widthUnit.
     * @param widthUnit New value of property widthUnit.
     */
    public void setWidthUnit(java.lang.String widthUnit)  {
        
        this.widthUnit = widthUnit;
    }
    
    /**
     * Getter for property heightUnit.
     * @return Value of property heightUnit.
     */
    public String getHeightUnit()  {
        
        return this.heightUnit;
    }
    
    /**
     * Setter for property heightUnit.
     * @param heightUnit New value of property heightUnit.
     */
    public void setHeightUnit(java.lang.String heightUnit)  {
        
        this.heightUnit = heightUnit;
    }
    
    /**
     * Getter for property bottomValue.
     * @return Value of property bottomValue.
     */
    public String getBottomValue() {
        
        return this.bottomValue;
    }
    
    /**
     * Setter for property bottomValue.
     * @param bottomValue New value of property bottomValue.
     */
    public void setBottomValue(String bottomValue) {
        
        this.bottomValue = bottomValue;
    }
    
    /**
     * Getter for property heightValue.
     * @return Value of property heightValue.
     */
    public String getHeightValue() {
        
        return this.heightValue;
    }
    
    /**
     * Setter for property heightValue.
     * @param heightValue New value of property heightValue.
     */
    public void setHeightValue(String heightValue) {
        
        this.heightValue = heightValue;
    }
    
    /**
     * Getter for property leftValue.
     * @return Value of property leftValue.
     */
    public String getLeftValue() {
        
        return this.leftValue;
    }
    
    /**
     * Setter for property leftValue.
     * @param leftValue New value of property leftValue.
     */
    public void setLeftValue(String leftValue) {
        
        this.leftValue = leftValue;
    }
    
    /**
     * Getter for property rightValue.
     * @return Value of property rightValue.
     */
    public String getRightValue() {
        
        return this.rightValue;
    }
    
    /**
     * Setter for property rightValue.
     * @param rightValue New value of property rightValue.
     */
    public void setRightValue(String rightValue) {
        
        this.rightValue = rightValue;
    }
    
    /**
     * Getter for property topValue.
     * @return Value of property topValue.
     */
    public String getTopValue() {
        
        return this.topValue;
    }
    
    /**
     * Setter for property topValue.
     * @param topValue New value of property topValue.
     */
    public void setTopValue(String topValue) {
        
        this.topValue = topValue;
    }
    
    /**
     * Getter for property widthValue.
     * @return Value of property widthValue.
     */
    public String getWidthValue() {
        
        return this.widthValue;
    }
    
    /**
     * Setter for property widthValue.
     * @param widthValue New value of property widthValue.
     */
    public void setWidthValue(String widthValue) {
        
        this.widthValue = widthValue;
    }
}
