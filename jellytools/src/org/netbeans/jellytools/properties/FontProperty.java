/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.properties;

/*
 * FontArrayProperty.java
 *
 * Created on June 18, 2002, 11:53 AM
 */

import java.io.File;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.properties.editors.FontCustomEditorOperator;
import org.netbeans.jemmy.operators.ContainerOperator;

/** Operator serving property of type Font
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class FontProperty extends Property {

    /** String constant for plain font style */    
    public static final String STYLE_PLAIN = FontCustomEditorOperator.STYLE_PLAIN; 
    /** String constant for bold font style */    
    public static final String STYLE_BOLD = FontCustomEditorOperator.STYLE_BOLD; 
    /** String constant for italic font style */    
    public static final String STYLE_ITALIC = FontCustomEditorOperator.STYLE_ITALIC; 
    /** String constant for bold italic font style */    
    public static final String STYLE_BOLDITALIC = FontCustomEditorOperator.STYLE_BOLDITALIC;
   
    /** Creates a new instance of FontProperty
     * @param contOper ContainerOperator of parent container to search property in
     * @param name String property name 
     * @deprecated Use {@link #FontProperty(PropertySheetOperator, String)} instead
     */
    public FontProperty(ContainerOperator contOper, String name) {
        super(contOper, name);
    }
    
    /** Creates a new instance of FontProperty
     * @param propertySheetOper PropertySheetOperator where to find property.
     * @param name String property name 
     */
    public FontProperty(PropertySheetOperator propertySheetOper, String name) {
        super(propertySheetOper, name);
    }
    
    /** invokes custom property editor and returns proper custom editor operator
     * @return FontCustomEditorOperator */    
    public FontCustomEditorOperator invokeCustomizer() {
        openEditor();
        return new FontCustomEditorOperator(getName());
    }
    
    /** setter for Font value through Custom Editor
     * @param fontName String font name
     * @param fontStyle String font style
     * @param fontSize String font size */    
    public void setFontValue(String fontName, String fontStyle, String fontSize) {
        FontCustomEditorOperator customizer=invokeCustomizer();
        if (fontName!=null)
            customizer.setFontName(fontName);
        if (fontStyle!=null)
            customizer.setFontStyle(fontStyle);
        if (fontSize!=null)
            customizer.setFontSize(fontSize);
        customizer.ok();
    }        
    
    /** getter for Font value through Custom Editor
     * @return String[3] font name, font style and font size */    
    public String[] getFontValue() {
        String[] value=new String[3];
        FontCustomEditorOperator customizer=invokeCustomizer();
        value[0]=customizer.getFontName();
        value[1]=customizer.getFontStyle();
        value[2]=customizer.getFontSize();
        customizer.close();
        return value;
    }

    /** Performs verification by accessing all sub-components */    
    public void verify() {
        invokeCustomizer().verify();
        new NbDialogOperator(getName()).close();
    }        
}
