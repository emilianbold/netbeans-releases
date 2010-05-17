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
