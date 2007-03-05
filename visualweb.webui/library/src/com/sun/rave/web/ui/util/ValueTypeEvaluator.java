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
package com.sun.rave.web.ui.util;

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.el.ValueBinding;

import com.sun.rave.web.ui.component.Selector;

/**
 * <p>
 * The ValueTypeEvaluator and the ValueType (@see ValueType) classes
 * are helper classes for UIComponents which accept value bindings that
 * can be either single objects or a collection of
 * objects (for example, an array). Typically, these components have
 * to process input differently depending on the type of the value
 * object. </p>
 *<p>
 * Usage: create a ValueTypeEvaluator member class in the UIComponent's
 * constructor. Invoke getValueType() to get the type of the component's
 * value bindinding. ValueTypeEvaluator caches the value type after the 
 * first invocation, so the component should not cache the value itself.
 *
 */
public class ValueTypeEvaluator {
    
    private ValueType valueType = null; 
    private UIComponent component = null; 
    
    private static final boolean DEBUG = false;
    
    /**
     * Creates a new instance of ValueTypeEvaluator.
     * @param component The UIComponent for which the ValueTypeEvaluator is created
     */
    public ValueTypeEvaluator(UIComponent component) {
        this.component = component;
        if(!(component instanceof ValueHolder)) {
            if(DEBUG) log("\tComponent is not a value holder");
            valueType = ValueType.NONE;
        }
    }
      /**
     * Determine the type of the valuebinding of this object. This
     * method returns the cached ValueType if it has already been 
     * set. If it was not set, it retrieves the current FacesContext 
     * and invokes getValueType(FacesContext context). 
     * @return valueType the ValueType of this object
     */
    public ValueType getValueType() {
        if(DEBUG) log("getValueType()"); //NOI18N
        if(valueType == null) { 
	    valueType = getValueType(FacesContext.getCurrentInstance());         
        }
        
        else if(DEBUG) { 
            if(DEBUG) log("\tValueType already known " + valueType.toString()); //NOI18N
        }
	return valueType; 
    } 

    /** 
     * Determine the type of the valuebinding of this object. This
     * method returns the cached ValueType if it has already been 
     * set or otherwise determines the ValueType by evaluating the 
     * value binding for the component's value object. 
     *
     * @param context The FacesContext
     * @return valueType the ValueType of this object
     */
    public ValueType getValueType(FacesContext context) { 

	if(DEBUG) log("getValueType(context)"); 

        if(valueType == null) { 
	    valueType = evaluateValueType(context); 
            if(DEBUG) log("\tEvaluated ValueType to " + valueType.toString()); //NOI18N
        }
        else if(DEBUG) { 
            if(DEBUG) log("\tValueType already known " + valueType.toString()); //NOI18N
        }
	return valueType; 
    } 

    public void reset() { 
        valueType = null; 
    } 
    
    private ValueType evaluateValueType(FacesContext context) { 

	// Determine the type of the component's value object
        ValueBinding valueBinding =
                component.getValueBinding("value"); //NOI18N
        
        if(valueBinding != null) {
            if(DEBUG) log("\tFound value binding for ");
            return evaluateValueBinding(valueBinding, context);
	} 

	if(DEBUG) log("No valuebinding...");
            
	Object valueObject = ((ValueHolder)component).getValue();
	if(valueObject == null) {
	    if(DEBUG) log("No initial value either...");
	    if(component instanceof Selector) {
		boolean isMultiple = ((Selector)component).isMultiple();
		if(isMultiple) {
		    if(DEBUG) log("Guessing array...");
		    return  ValueType.ARRAY;
		} 
		if(DEBUG) log("Guessing object...");
		return ValueType.OBJECT;
	    }
	    
	    if(DEBUG) log("Guessing object...");
	    return ValueType.OBJECT;
	} 
	return evaluateClass(valueObject.getClass());
    }
    
    private ValueType evaluateValueBinding(ValueBinding valueBinding, 
                                           FacesContext context) {
        // We have found a valuebinding.
        Class clazz = valueBinding.getType(context);
        
        // Null class
        if(clazz == null) {
            String msg = "\tComponent has invalid value binding for \"value\".";
            throw new ConverterException(msg);
        }
        
        // <RAVE>
        // If compile-time type is Object, it is possible that at run-time the
        // value's actual type is something else, like Object[]. So check to be
        // sure.
        if (Object.class.equals(clazz)) {
            Object value = valueBinding.getValue(context);
            if (value != null)
                clazz = value.getClass();
        }
        // </RAVE>
        
        return evaluateClass(clazz); 
    } 
    
    private ValueType evaluateClass(Class clazz) { 
        
        // The value is an array 
        if(clazz.isArray()) {
            return ValueType.ARRAY;
        }
        // The value is a List (or an ArrayList)
        if(java.util.List.class.isAssignableFrom(clazz)) {
            return ValueType.LIST;
        }
      
        // Neither array nor list, assume single object
        return valueType.OBJECT;
    }
       
     
    private void log(String s) { 
        System.out.println(this.getClass().getName() + "::" + s); 
    }
}
