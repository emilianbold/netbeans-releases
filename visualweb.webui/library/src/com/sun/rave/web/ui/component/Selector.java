/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.sun.rave.web.ui.component;

import java.lang.reflect.Array;
import java.util.Iterator;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.event.ValueChangeEvent;

import com.sun.rave.web.ui.model.OptionTitle;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.MessageUtil;
import com.sun.rave.web.ui.util.ValueType;
import com.sun.rave.web.ui.util.ValueTypeEvaluator;

/**
 *
 * @author avk
 */

public class Selector extends SelectorBase implements SelectorManager {

    // If true, debugging statements are printed to stdout
    private static final boolean DEBUG = false;

    /**
     * Read only separator string
     */
    private static final String READ_ONLY_SEPARATOR = ", "; //NOI18N

    private boolean multiple;

   // Holds the ValueType of this component
    protected ValueTypeEvaluator valueTypeEvaluator = null; 
    
    public Selector() { 
        valueTypeEvaluator = new ValueTypeEvaluator(this); 
    } 
    
    /**
     *
     * <p>Return a flag indicating whether this component is responsible
     * for rendering its child components.  The default implementation
     * in {@link UIComponentBase#getRendersChildren} tries to find the
     * renderer for this component.  If it does, it calls {@link
     * Renderer#getRendersChildren} and returns the result.  If it
     * doesn't, it returns false.  As of version 1.2 of the JavaServer
     * Faces Specification, component authors are encouraged to return
     * <code>true</code> from this method and rely on {@link
     * UIComponentBase#encodeChildren}.</p>
     */
    public boolean getRendersChildren() {
        return true;
    }
        
    /**
     * Retrieve the value of this component (the "selected" property) as an  
     * object. This method is invoked by the JSF engine during the validation 
     * phase. The JSF default behaviour is for components to defer the 
     * conversion and validation to the renderer, but for the Selector based
     * components, the renderers do not share as much functionality as the 
     * components do, so it is more efficient to do it here. 
     * @param context The FacesContext of the request
     * @param submittedValue The submitted value of the component
     */
    
    public Object getConvertedValue(FacesContext context, 
                                    Object submittedValue)
        throws ConverterException {    
        return getConvertedValue(this, valueTypeEvaluator, context, submittedValue); 
    } 
    
        
   /**
     * Retrieve the value of this component (the "selected" property) as an  
     * object. This method is invoked by the JSF engine during the validation 
     * phase. The JSF default behaviour is for components to defer the 
     * conversion and validation to the renderer, but for the Selector based
     * components, the renderers do not share as much functionality as the 
     * components do, so it is more efficient to do it here. 
     * @param component The component whose value to convert
     * @param context The FacesContext of the request
     * @param submittedValue The submitted value of the component
     */
    private Object getConvertedValue(UIComponent component, 
				  ValueTypeEvaluator valueTypeEvaluator,
				  FacesContext context, 
				  Object submittedValue)

        throws ConverterException {
  
        if(DEBUG) log("getConvertedValue()", component); 

	if(!(submittedValue instanceof String[])) { 
            Object[] args = { component.getClass().getName() }; 
	    String msg = MessageUtil.getMessage
                    ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
		     "Selector.invalidSubmittedValue", args); //NOI18N
                          
	    throw new ConverterException(msg);
	} 

	String[] rawValues = (String[])submittedValue; 

	// This should never happen
	//
	if(rawValues.length == 1 &&
		OptionTitle.NONESELECTED.equals(rawValues[0])) { 
            Object[] args = { OptionTitle.NONESELECTED }; 
	    String msg = MessageUtil.getMessage
                    ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
		     "Selector.invalidSubmittedValue", args); //NOI18N
                          
	    throw new ConverterException(msg);
	}
	
	// If there are no elements in rawValue nothing was submitted.
	// If null was rendered, return null
	//
	if(rawValues.length == 0) {
	    if(DEBUG) log("\t no values submitted, we return null", component); 
	    if (ConversionUtilities.renderedNull(component)) {
		return null; 
	    }
	}

        // Why does getAttributes.get("multiple") not work? 
	if(((SelectorManager)component).isMultiple()) { 
	    if(DEBUG) log("\tComponent accepts multiple values", component); 

	    if(valueTypeEvaluator.getValueType() == ValueType.ARRAY) { 
		if(DEBUG) log("\tComponent value is an array", component); 
		return ConversionUtilities.convertValueToArray
		    (component, rawValues, context); 
	    } 
	    // This case is not supported yet!
	    else if(valueTypeEvaluator.getValueType() == ValueType.LIST) { 
		if(DEBUG) log("\tComponent value is a list", component); 
		return ConversionUtilities.convertValueToList
		    (component, rawValues, context); 
	    } 
	    else {
                if(DEBUG) log("\tMultiple selection enabled for non-array value", 
                              component);
                Object[] params = { component.getClass().getName() };
                String msg = MessageUtil.getMessage
                        ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
                         "Selector.multipleError",               //NOI18N
                         params); 
                throw new ConverterException(msg);
	    }
	}

	if(DEBUG) log("\tComponent value is an object", component); 

	// Not sure if this case is taken care of consistently
	// Need to formulate the possible states for the
	// submitted value and what they mean.
	//
	// This can overwrite an unchanged value property
	// with null when it was originally empty string.
	/*
	if(rawValues[0].length() == 0) { 
	    if(DEBUG) log("\t empty string submitted, return null", component); 
	    return null; 
	} 
	*/

	String cv = rawValues.length == 0 ? "" : rawValues[0];

	if(valueTypeEvaluator.getValueType() == ValueType.NONE) { 
            if(DEBUG) log("\t valuetype == none, return rawValue", component); 
	    return cv;
	} 

        if(DEBUG) log("\t Convert the thing...", component); 
	return ConversionUtilities.convertValueToObject
	    (component, cv, context);
    } 

    /**
     * Return a string suitable for displaying the value in read only mode.
     * The default is to separate the list values with a comma.
     *
     * @param context The FacesContext
     * @throws javax.faces.FacesException If the list items cannot be processed
     */
    // AVK - instead of doing this here, I think we 
    // should set the value to be displayed when we get the readOnly 
    // child component. It would be a good idea to separate the listItems 
    // processing for the renderer - where we have to reprocess the items
    // every time, from other times, when this may not be necessary.
    // I note that although this code has been refactored by Rick, my 
    // original code already did this so the fault is wtih me. 
    protected String getValueAsReadOnly(FacesContext context) {
        
	// The comma format READ_ONLY_SEPARATOR should be part of the theme
	// and/or configurable by the application
	//
	return getValueAsString(context, READ_ONLY_SEPARATOR, true);
    }

    /**
     * Get the value (the object representing the selection(s)) of this 
     * component as a String. If the component allows multiple selections,
     * the strings corresponding to the individual options are separated by 
     * spaces. 
     * @param context The FacesContext of the request
     * @param separator A String separator between the values
    
    public String getValueAsString(FacesContext context, String separator) { 
	 return getValueAsString(context, separator, false);
    }
 */
    /**
     * Get the value (the object representing the selection(s)) of this 
     * component as a String. If the component allows multiple selections,
     * the strings corresponding to the individual options are separated
     * by the separator argument. If readOnly is true, leading and
     * and trailing separators are omitted.
     * If readOnly is false the formatted String is suitable for decoding
     * by ListRendererBase.decode.
     *
     * @param context The FacesContext of the request
     * @param separator A String separator between the values
     * @param readOnly A readonly formatted String, no leading or trailing
     * separator string.
     */
    private String getValueAsString(FacesContext context, String separator,
		boolean readOnly) {

	// Need to distinguish null value from an empty string
	// value. See the end of this method for empty string
	// value formatting
	//
	Object value = getValue(); 
	if(value == null) { 
	    return new String(); 
	} 

	if(valueTypeEvaluator.getValueType() == ValueType.NONE) { 
	    return new String(); 
	} 
        
	if(valueTypeEvaluator.getValueType() == ValueType.INVALID) { 
	    return new String(); 
	} 

	// Multiple selections
	//
	// The format should be the same as that returned
	// from the javascript which always has a leading
	// and terminating separator. And suitable for decoding
	// by ListRendererBase.decode
	//
	if(valueTypeEvaluator.getValueType() == ValueType.LIST) { 

	    StringBuffer valueBuffer = new StringBuffer(256); 

	    java.util.List list = (java.util.List)value; 
	    Iterator valueIterator = ((java.util.List)value).iterator();
	    String valueString = null; 

	    // Leading delimiter
	    //
	    if (!readOnly && valueIterator.hasNext()) {
		valueBuffer.append(separator);
	    }

	    while(valueIterator.hasNext()) {
		valueString = ConversionUtilities.convertValueToString
			(this, valueIterator.next());
		valueBuffer.append(valueString); 
		// Add terminating delimiter
		//
                if(!readOnly || (readOnly && valueIterator.hasNext())) {
                    valueBuffer.append(separator);
                }
	    } 
	    return valueBuffer.toString(); 
	}

	if(valueTypeEvaluator.getValueType() == ValueType.ARRAY) {
	    
	    StringBuffer valueBuffer = new StringBuffer(256); 

	    int length = Array.getLength(value); 
	    Object valueObject = null;
	    String valueString = null; 
	    
	    if (!readOnly && length != 0) {
		valueBuffer.append(separator);
	    }
	    for(int counter = 0; counter < length; ++counter) { 
		valueObject = Array.get(value,counter); 
		valueString = 
		    ConversionUtilities.convertValueToString
		    (this, valueObject); 
		valueBuffer.append(valueString); 
		// Add terminating delimiter
		//
                if(!readOnly || (readOnly && counter < length - 1)) {
                    valueBuffer.append(separator);
                }
	    } 
	    return valueBuffer.toString(); 
	} 

	// Empty string looks like '<sep><sep>' or if separator == "|"
	// it'll be "||"
	//
	String cv = ConversionUtilities.convertValueToString(this, value);
	if (readOnly) {
	    return cv;
	} else {
	    StringBuffer sb = new StringBuffer(64);
	    return sb.append(separator).append(cv).append(separator).toString();
	}
    } 

    public int getLabelLevel() {

        int labelLevel = super.getLabelLevel();
        if(labelLevel < 1 || labelLevel > 3) { 
            labelLevel = 2; 
            super.setLabelLevel(labelLevel);
        }
        return labelLevel;
    }

    /**
     * Getter for property multiple.
     * @return Value of property multiple.
     */
    public boolean isMultiple() {
        
        return this.multiple;
    }

    /**
     * Setter for property multiple.
     * @param multiple New value of property multiple.
     */
    public void setMultiple(boolean multiple) {
        if(this.multiple != multiple) {
            valueTypeEvaluator.reset();
            this.multiple = multiple;
        }
    }

    /**
     * Public method toString() 
     * @return A String representation of this component
     */
    public String toString() {
	String string = this.getClass().getName(); 
	return string; 
    }

     /**
     * private method for development time error detecting
     */
    static void log(String s, Object o) {
        System.out.println(o.getClass().getName() + "::" + s); //NOI18N
    }

    /**
     * private method for development time error detecting
     */
    void log(String s) {
        System.out.println(this.getClass().getName() + "::" + s); //NOI18N
    }

    /**
     * <p>Return <code>true</code> if the new value is different from the
     * previous value.</p>
     *
     * This only implements a compareValues for value if it is an Array.
     * If value is not an Array, defer to super.compareValues.
     * The assumption is that the ordering of the elements
     * between the previous value and the new value is determined
     * in the same manner.
     *
     * Another assumption is that the two object arguments
     * are of the same type, both arrays of both not arrays.
     *
     * @param previous old value of this component (if any)
     * @param value new value of this component (if any)
     */
    protected boolean compareValues(Object previous, Object value) {

	// Let super take care of null cases
	//
	if (previous == null || value == null) {
	    return super.compareValues(previous, value);
	}
	if (value instanceof Object[]) {
	    // If the lengths aren't equal return true
	    //
	    int length = Array.getLength(value);
	    if (Array.getLength(previous) != length) {
		return true;
	    }
	    // Each element at index "i" in previous must be equal to the
	    // elementa at index "i" in value.
	    //
	    for (int i = 0; i < length; ++i) {

		Object newValue = Array.get(value, i);
		Object prevValue = Array.get(previous, i);

		// This is probably not necessary since
		// an Option's value cannot be null
		//
		if (newValue == null) {
		    if (prevValue == null) {
			continue;
		    } else {
			return true;
		    }
		}
		if (prevValue == null) {
		    return true;
		}

		if (!prevValue.equals(newValue)) {
		    return true;
		}
	    }
	    return false;
        }
	return super.compareValues(previous, value);
    }

    public void setSelected(Object selected) {
        super.setSelected(selected);
        valueTypeEvaluator.reset(); 
    }
}
