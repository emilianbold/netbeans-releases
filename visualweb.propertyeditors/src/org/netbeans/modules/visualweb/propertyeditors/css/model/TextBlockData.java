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
public class TextBlockData {
    /**
     * Holds value of property verticalAlignUnit.
     */
    private String verticalAlignUnit ="px"; //NOI18N
    
    /**
     * Holds value of property verticalAlignValue.
     */
    private String verticalAlignValue ="";
    
    /**
     * Holds value of property indentationUnit.
     */
    private String indentationUnit ="px"; //NOI18N
    
    /**
     * Holds value of property wordSpacingUnit.
     */
    private String wordSpacingUnit ="px"; //NOI18N
    
    /**
     * Holds value of property indentationValue.
     */
    private String indentationValue ="";
    
    /**
     * Holds value of property wordSpacingValue.
     */
    private String wordSpacingValue ="";
    
    /**
     * Holds value of property letterSpacingUnit.
     */
    private String letterSpacingUnit="px"; //NOI18N
    
    /**
     * Holds value of property letterSpacingValue.
     */
    private String letterSpacingValue="";
    
    /**
     * Holds value of property lineHeightUnit.
     */
    private String lineHeightUnit="px"; //NOI18N
    
    /**
     * Holds value of property lineHeightValue.
     */
    private String lineHeightValue;
    
    public void setVerticalAlign(String verticalAlignStr){
        verticalAlignUnit = getUnit(verticalAlignStr);
        verticalAlignValue = verticalAlignStr.replaceAll(verticalAlignUnit,"").trim();
    }
    
    public void setIndentation(String indentationStr){
        indentationUnit = getUnit(indentationStr);
        indentationValue = indentationStr.replaceAll(indentationUnit,"").trim();
    }
    
    public void setWordSpacing(String wordSpacingStr){
        wordSpacingUnit = getUnit(wordSpacingStr);
        wordSpacingValue = wordSpacingStr.replaceAll(wordSpacingUnit,"").trim();
    }
    
    public void setLetterSpacing(String letterSpacingStr){
        letterSpacingUnit = getUnit(letterSpacingStr);
        letterSpacingValue = letterSpacingStr.replaceAll(letterSpacingUnit,"").trim();
    }
    
    public void setLineHeight(String lineHeightStr){
        lineHeightUnit = getUnit(lineHeightStr);
        lineHeightValue = lineHeightStr.replaceAll(lineHeightUnit,"").trim();
    }
    
    private String getUnit(String textBlockStr){
        DefaultComboBoxModel unitList = new TextBlockModel().getTextBlockUnitList();
        for(int i=0; i< unitList.getSize(); i++){
            String unit = (String)unitList.getElementAt(i);
            if(textBlockStr.trim().endsWith(unit)){
                return unit;
            }
        }
        return "";
    }
    
    /**
     * Getter for property verticalAlignUnit.
     * @return Value of property verticalAlignUnit.
     */
    public String getVerticalAlignUnit() {
        
        return this.verticalAlignUnit;
    }
    
    /**
     * Setter for property verticalAlignUnit.
     * @param verticalAlignUnit New value of property verticalAlignUnit.
     */
    public void setVerticalAlignUnit(String verticalAlignUnit) {
        
        this.verticalAlignUnit = verticalAlignUnit;
    }
    
    /**
     * Getter for property verticalAlignValue.
     * @return Value of property verticalAlignValue.
     */
    public String getVerticalAlignValue() {
        
        return this.verticalAlignValue;
    }
    
    /**
     * Setter for property verticalAlignValue.
     * @param verticalAlignValue New value of property verticalAlignValue.
     */
    public void setVerticalAlignValue(String verticalAlignValue) {
        
        this.verticalAlignValue = verticalAlignValue;
    }
    
    /**
     * Getter for property indentationUnit.
     * @return Value of property indentationUnit.
     */
    public String getIndentationUnit()  {
        
        return this.indentationUnit;
    }
    
    /**
     * Setter for property indentationUnit.
     * @param indentationUnit New value of property indentationUnit.
     */
    public void setIndentationUnit(java.lang.String indentationUnit)  {
        
        this.indentationUnit = indentationUnit;
    }
    
    /**
     * Getter for property wordSpacingUnit.
     * @return Value of property wordSpacingUnit.
     */
    public String getWordSpacingUnit() {
        
        return this.wordSpacingUnit;
    }
    
    /**
     * Setter for property wordSpacingUnit.
     * @param wordSpacingUnit New value of property wordSpacingUnit.
     */
    public void setWordSpacingUnit(String wordSpacingUnit) {
        
        this.wordSpacingUnit = wordSpacingUnit;
    }
    
    /**
     * Getter for property indentationValue.
     * @return Value of property indentationValue.
     */
    public String getIndentationValue() {
        
        return this.indentationValue;
    }
    
    /**
     * Setter for property indentationValue.
     * @param indentationValue New value of property indentationValue.
     */
    public void setIndentationValue(String indentationValue) {
        
        this.indentationValue = indentationValue;
    }
    
    /**
     * Getter for property wordSpacingValue.
     * @return Value of property wordSpacingValue.
     */
    public String getWordSpacingValue() {
        
        return this.wordSpacingValue;
    }
    
    /**
     * Setter for property wordSpacingValue.
     * @param wordSpacingValue New value of property wordSpacingValue.
     */
    public void setWordSpacingValue(String wordSpacingValue) {
        
        this.wordSpacingValue = wordSpacingValue;
    }
    
    /**
     * Getter for property letterSpacingUnit.
     * @return Value of property letterSpacingUnit.
     */
    public String getLetterSpacingUnit() {
        
        return this.letterSpacingUnit;
    }
    
    /**
     * Setter for property letterSpacingUnit.
     * @param letterSpacingUnit New value of property letterSpacingUnit.
     */
    public void setLetterSpacingUnit(String letterSpacingUnit) {
        
        this.letterSpacingUnit = letterSpacingUnit;
    }
    
    /**
     * Getter for property letterSpacingValue.
     * @return Value of property letterSpacingValue.
     */
    public String getLetterSpacingValue() {
        
        return this.letterSpacingValue;
    }
    
    /**
     * Setter for property letterSpacingValue.
     * @param letterSpacingValue New value of property letterSpacingValue.
     */
    public void setLetterSpacingValue(String letterSpacingValue) {
        
        this.letterSpacingValue = letterSpacingValue;
    }
    
    /**
     * Getter for property lineHeightUnit.
     * @return Value of property lineHeightUnit.
     */
    public String getLineHeightUnit() {
        
        return this.lineHeightUnit;
    }
    
    /**
     * Setter for property lineHeightUnit.
     * @param lineHeightUnit New value of property lineHeightUnit.
     */
    public void setLineHeightUnit(String lineHeightUnit) {
        
        this.lineHeightUnit = lineHeightUnit;
    }
    
    /**
     * Getter for property lineHeightValue.
     * @return Value of property lineHeightValue.
     */
    public String getLineHeightValue() {
        
        return this.lineHeightValue;
    }
    
    /**
     * Setter for property lineHeightValue.
     * @param lineHeightValue New value of property lineHeightValue.
     */
    public void setLineHeightValue(String lineHeightValue) {
        
        this.lineHeightValue = lineHeightValue;
    }
    
    
}
