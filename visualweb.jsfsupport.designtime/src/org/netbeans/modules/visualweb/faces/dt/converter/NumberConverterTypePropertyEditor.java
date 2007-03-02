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
package org.netbeans.modules.visualweb.faces.dt.converter;

import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;
import java.beans.PropertyEditorSupport;

/**
 * For editing the NumberConverter property - type. The possible type can be number, currency, percent
 *
 * @author cao
 * @deprecated
 */
public class NumberConverterTypePropertyEditor extends PropertyEditorSupport implements PropertyEditor2 {
    
    private String[] types = new String[] {"number", "currency", "percent"};
    private DesignProperty designProperty;
    private int selected = 0;
    
    /** Creates a new instance of NumberConverterTypePropertyEditor */
    public NumberConverterTypePropertyEditor() {
    }
    
    /**
     * Implementation of design time API - PropertyEditor2
     */
    public void setDesignProperty( DesignProperty designProperty ) {
        this.designProperty = designProperty;
    }
    
   /** 
    * Allows the bean builder to tell the property editor the value
    * that the user has entered/selected
    */
    public void setAsText(String text) throws IllegalArgumentException {
        
        // If the passed in type is invalid, then default back to what was before
        for( selected = 0; selected < types.length && !types[selected].equals(text); selected++);
        
        if (selected == types.length)
            selected = 0;
        
        super.setValue( types[selected] );
    }
    
    /** 
     * Tells the bean builder current selected type
     */
    public String getAsText() {
        return types[selected];
    }
    
    /** 
     * Allows the bean builder to pass the current property
     * value to the property editor
     */
    public void setValue(Object value) {
        selected = 0;
        if( value != null ) {
            for( int i=0;  i < types.length; i++ ) {
                if (value.equals(types[i])) {
                    selected = i;
                    break;
                }
            }
        }
        
        super.setValue( value );
    }
    
    /** 
     * Tells the bean builder the value for the property 
     */
    public Object getValue() {
        return types[selected];
    }
    
    /**
     * The possible values for this property
     */
    public String[] getTags() {
        return types;
    }
    
    /** 
     * Get the initialization code for the property 
     */
    public String getJavaInitializationString() {
	return "\"" + types[selected] + "\"";
    }
}
