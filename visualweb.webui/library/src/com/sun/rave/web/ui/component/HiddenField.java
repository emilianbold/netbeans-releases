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
package com.sun.rave.web.ui.component;

import javax.faces.context.FacesContext;
import com.sun.rave.web.ui.util.ConversionUtilities;

/**
 *
 * @author avk
 */
public class HiddenField extends HiddenFieldBase {

    private final static boolean DEBUG = false;

    /** Creates a new instance of HiddenField */
    public HiddenField() {
    }


    /**
     * <p>Return the value to be rendered as a string when the
     * component is readOnly. The default behaviour is to
     * invoke getValueAsString(). Override this method in case
     * a component needs specialized behaviour.</p>
     * @param context FacesContext for the current request
     * @return A String value of the component
     */
    public String getReadOnlyValueString(FacesContext context) {
        return getValueAsString(context);
    }

     /**
     * <p>Return the value to be rendered, as a String (converted
     * if necessary), or <code>null</code> if the value is null.</p>
     * @param context FacesContext for the current request
     * @return A String value of the component
     */
    public String getValueAsString(FacesContext context) { 

        if(DEBUG) log("getValueAsString()");
        
	// This is done in case the RENDER_RESPONSE is occuring
	// prematurely due to some error or an immediate condition
	// on a button. submittedValue is set to null when the
	// component has been validated. 
	// If the component has not passed through the PROCESS_VALIDATORS
	// phase then submittedValue will be non null if a value
	// was submitted for this component.
	// 
        Object submittedValue = getSubmittedValue();
        if (submittedValue != null) {
            if(DEBUG) { 
                log("Submitted value is not null " + //NOI18N
                    submittedValue.toString());
            }
            return (String) submittedValue;
        }
              
        String value = ConversionUtilities.convertValueToString(this, getValue());
        if(value == null) {
            value = new String();
        }
        if(DEBUG) log("Component value is " + value); 
        return value;
    } 

    /**
     * Return the converted value of newValue.
     * If newValue is null, return null.
     * If newValue is "", check the rendered value. If the
     * the value that was rendered was null, return null
     * else continue to convert.
     */
    protected Object getConvertedValue(FacesContext context, 
                                       Object newValue) 
        throws javax.faces.convert.ConverterException {

        if(DEBUG) log("getConvertedValue()");
            
        Object value = ConversionUtilities.convertRenderedValue(context, 
                                                                newValue, this);
        
        if(DEBUG) log("\tComponent is valid " + String.valueOf(isValid())); 
        if(DEBUG) log("\tValue is " + String.valueOf(value)); 
        return value;
    }
    
    protected void log(String s) { 
        System.out.println(this.getClass().getName() + "::" + s); 
    }
}
