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
public class ClipData extends PropertyData{
    
    PropertyWithUnitData topValue = new PropertyWithUnitData();
    PropertyWithUnitData bottomValue = new PropertyWithUnitData();
    PropertyWithUnitData leftValue = new PropertyWithUnitData();
    PropertyWithUnitData rightValue = new PropertyWithUnitData();
    
    private boolean hasErrors = false;
    
    public void setClip(String cip){
        String cipString = cip.substring(cip.indexOf("(")+1,cip.indexOf(")"));
        StringTokenizer st = new StringTokenizer(cipString,",");
        
        if(st.hasMoreTokens()){
            setTop(st.nextToken());
        }
        if(st.hasMoreTokens()){
            setRight(st.nextToken());
        }
        if(st.hasMoreTokens()){
            setBottom(st.nextToken());
        }
        if(st.hasMoreTokens()){
            setLeft(st.nextToken());
        }
    }
    
    public void setTop(String clipTopStr){
        topValue.setUnit(getUnit(clipTopStr));
        topValue.setValue(clipTopStr.replaceAll(topValue.getUnit(),"").trim());
    }
    
    public void setBottom(String clipBottomStr){
        bottomValue.setUnit(getUnit(clipBottomStr));
        bottomValue.setValue(clipBottomStr.replaceAll(bottomValue.getUnit(),"").trim());
    }
    
    public void setLeft(String clipLeftStr){
        leftValue.setUnit(getUnit(clipLeftStr));
        leftValue.setValue(clipLeftStr.replaceAll(leftValue.getUnit(),"").trim());
    }
    
    public void setRight(String clipRightStr){
        rightValue.setUnit(getUnit(clipRightStr));
        rightValue.setValue(clipRightStr.replaceAll(rightValue.getUnit(),"").trim());
    }
    
    private String getUnit(String clipStr){
        DefaultComboBoxModel unitList = new ClipModel().getClipUnitList();
        for(int i=0; i< unitList.getSize(); i++){
            String unit = (String)unitList.getElementAt(i);
            if(clipStr.trim().endsWith(unit)){
                return unit;
            }
        }
        return "";
    }
    
    public void setTopValue(String top){
        topValue.setValue(top);
    }
    
    public void setTopUnit(String topUnit){
        topValue.setUnit(topUnit);
    }
    
    public void setBottomValue(String bottom){
        bottomValue.setValue(bottom);
    }
    
    public void setBottomUnit(String bottomUnit){
        bottomValue.setUnit(bottomUnit);
    }
    
    public void setLeftValue(String left){
        leftValue.setValue(left);
    }
    
    public void setLeftUnit(String leftUnit){
        leftValue.setUnit(leftUnit);
    }
    
    public void setRightValue(String right){
        rightValue.setValue(right);
    }
    
    public void setRightUnit(String rightUnit){
        rightValue.setUnit(rightUnit);
    }
    
    public String getTopValue(){
        return topValue.getValue();
    }
    
    public String getTopUnit(){
        return topValue.getUnit();
    }
    
    public String getBottomValue(){
        return bottomValue.getValue();
    }
    
    public String getBottomUnit(){
        return bottomValue.getUnit();
    }
    
    public String getLeftValue(){
        return leftValue.getValue();
    }
    
    public String getLeftUnit(){
        return leftValue.getUnit();
    }
    
    public String getRightValue(){
        return rightValue.getValue();
    }
    
    public String getRightUnit(){
        return rightValue.getUnit();
    }
    
    public boolean isTopValueInteger(){
        return topValue.isValueInteger();
    }
    
    public boolean isBottomValueInteger(){
        return bottomValue.isValueInteger();
    }
    
    public boolean isLeftValueInteger(){
        return leftValue.isValueInteger();
    }
    
    public boolean isRightValueInteger(){
        return rightValue.isValueInteger();
    }
    
    public boolean hasErros(){
        if (topValue.hasValue() || bottomValue.hasValue() ||
        rightValue.hasValue() || leftValue.hasValue()){
            return hasErrors;
        }else{
            return false;
        }
    }
    
    public String toString(){
        String clipString = "";
        if (!topValue.toString().equals("")){
            clipString +=  " " + topValue.toString();
        }else{
            clipString = "";
            hasErrors = true;
        }
        if (!rightValue.toString().equals("")){
            clipString +=  ", " + rightValue.toString();
        }else{
            clipString = "";
            hasErrors = true;
        }
        if (!bottomValue.toString().equals("")){
            clipString +=  ", " + bottomValue.toString();
        }else{
            clipString = "";
            hasErrors = true;
        }
        
        if (!leftValue.toString().equals("")){
            clipString +=  ", " + leftValue.toString();
        }else{
            clipString = "";
            hasErrors = true;
        }
        if(clipString.equals("") || hasErrors){
            clipString = "";
        }else{
            clipString = "rect(" + clipString.trim() + ")";
        }
        return clipString.trim();
    }
    
    
}
