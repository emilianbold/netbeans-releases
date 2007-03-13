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

/*
 * ClipData.java
 *
 * Created on October 22, 2004, 2:32 PM
 */

package org.netbeans.modules.css.visual.model;

import java.util.StringTokenizer;

/**
 * Data Structure for the Clip data
 * @author  Winston Prakash
 * @version 1.0
 */
public class ClipData extends PropertyData{

    PropertyWithUnitData topValue = new PropertyWithUnitData();
    PropertyWithUnitData bottomValue = new PropertyWithUnitData();
    PropertyWithUnitData leftValue = new PropertyWithUnitData();
    PropertyWithUnitData rightValue = new PropertyWithUnitData();

    public void setClip(String clip){
        topValue.clear();
        bottomValue.clear();
        leftValue.clear();
        rightValue.clear();
        if(clip != null){
            int start = clip.indexOf("(");
            int stop = clip.indexOf(")");
            if((start != -1) && (stop != -1)){
                String clipString = clip.substring(start + 1, stop);
                StringTokenizer st = new StringTokenizer(clipString,",");

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
        }
    }

    public void setTop(String clipTopStr){
        topValue.setData(clipTopStr);
    }

    public void setBottom(String clipBottomStr){
        bottomValue.setData(clipBottomStr);
    }

    public void setLeft(String clipLeftStr){
        leftValue.setData(clipLeftStr);
    }

    public void setRight(String clipRightStr){
        rightValue.setData(clipRightStr);
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

    public String toString(){
        if(!topValue.hasValue() && !rightValue.hasValue() && !bottomValue.hasValue() && !leftValue.hasValue()){
            return "";
        }
        String clipString = "";
        if (topValue.hasValue()){
            clipString +=  " " + topValue.toString();
        }else{
            clipString +=  " 0px"; //NOI18N
        }
        if (rightValue.hasValue()){
            clipString +=  ", " + rightValue.toString();
        }else{
            clipString +=  ", 0px"; //NOI18N
        }
        if (bottomValue.hasValue()){
            clipString +=  ", " + bottomValue.toString();
        }else{
            clipString +=  ", 0px"; //NOI18N
        }
        
        if (leftValue.hasValue()){
            clipString +=  ", " + leftValue.toString();
        }else{
            clipString +=  ", 0px"; //NOI18N
        }
        clipString = "rect(" + clipString.trim() + ")"; //NOI18N
        return clipString.trim();
    }
    
    
}
