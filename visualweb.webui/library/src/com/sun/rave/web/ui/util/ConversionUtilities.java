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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.el.ValueBinding;

/**
 * The ConversionUtilities class provides utility method for
 * converting values to and from Strings. Use this class if
 * your component processes input from the user, or displays
 * a converted value. 
 */

public class ConversionUtilities {

    private static final String RENDERED_NULL_VALUE = "_RENDERED_NULL_VALUE_";
    private static final boolean DEBUG = false;
    
    /**
     * <p>Convert the values of a component with a 
     * single (non-list, non-array) value. Use this 
     * method if<p>
     * <ul>
     * <li>the component always binds the user input to 
     * a single object (e.g. a textfield component); 
     * or </li> 
     * <li>to handle the single object case when the 
     * component may bind the user input to a single 
     * object <em>or</em> to a collection of 
     * objects (e.g. a list component). Use a 
     * ValueTypeEvaluator to evaluate the value 
     * binding type. </li> 
     * </ul>
     * @param component The component whose value is getting converted
     * @param rawValue The submitted value of the component
     * @param context The FacesContext of the request
     * @throws ConverterException if the conversion fails
     * @return An Object representing the converted value. If rawValue ==
     * <code>null</code> return null.
     * @see ValueTypeEvaluator
     */
    public static Object convertValueToObject(UIComponent component,
                                              String rawValue,
                                              FacesContext context)
        throws ConverterException {
        
        if(DEBUG) log("convertValueToObject()");
        
	// Optimization based on 
	// javax.faces.convert.Converter getAsObject.
	// It says:
	// return null if the value to convert is
	// null otherwise the result of the conversion
	//
        if(rawValue == null || !(component instanceof ValueHolder)) {
	    return rawValue; 
        }
        
        ValueHolder valueHolder = ((ValueHolder)component);
        
        Converter converter = valueHolder.getConverter();
        
        if(converter == null) {
            
            Class clazz = null;
            // Determine the type of the component's value object
            ValueBinding valueBinding =
                    component.getValueBinding("value"); //NOI18
            if(valueBinding == null) {
                Object value = valueHolder.getValue();
                if(value == null) {
                    return rawValue;
                }
                clazz = value.getClass();
            } else {
                clazz = valueBinding.getType(context);
            }
            
            // You can't register a default converter for
            // String/Object for the whole app (as opposed to for the
            // individual component). In this case we just
            // return the String.
            if(clazz == null || clazz.equals(String.class) || clazz.equals(Object.class)) {
                return rawValue;
            }
            
            // Try to get a converter
            converter = getConverterForClass(clazz);
            
            if(converter == null) {
               return rawValue;
            }
        }
        if(DEBUG) {
            log("Raw value was: " + rawValue);
            log("Converted value is: " + // NOI18N
		converter.getAsObject(context, component, rawValue));
        }
        return converter.getAsObject(context, component, rawValue);
    }


    /**
     * <p>Convert a String array of submitted values to the appropriate
     * type of Array for the value Object. This method assumes that
     * the value binding for the value of the component has been
     * determined to be an array (and as a consequence that the
     * component implements ValueHolder).</p>
     * 
     * <p>To evaluate the valueBinding, use the ValueTypeEvaluator 
     * class.</p>
     * @param component The component whose submitted values are to be
     * converted
     * @param rawValues The submitted value of the component
     * @param context The FacesContext of the request
     * @see ValueTypeEvaluator
     * @throws ConverterException if the conversion fails
     * @return An array of converted values
     */
    public static Object convertValueToArray(UIComponent component, 
					     String[] rawValues, 
					     FacesContext context)
        throws ConverterException {

	if(DEBUG) log("::convertValueToArray()"); 

	// By definition Converter returns null if the value to
	// convert is null. Do so here.
	//
	if (rawValues == null) {
	    return null;
	}
        
	// Get the class of the array members. We expect that the
	// component's value binding for its value has been determined
	// to be an array, as this is a condition of invoking this
	// method.     
	Class clazz = null; 
        
        // Get any converter specified by the page author
        Converter converter = ((ValueHolder)component).getConverter(); 
        
        try { 
            // <RAVE>
            // clazz = component.getValueBinding("value").
            //             getType(context).getComponentType(); //NOI18N
            ValueBinding vb = component.getValueBinding("value"); //NOI18N
            Class valueClass = vb.getType(context);
            if (Object.class.equals(valueClass)) {
                Object value = vb.getValue(context);
                if (value != null)
                    valueClass = value.getClass();
            }
            clazz = valueClass.getComponentType();
            // </RAVE>
        } 
        catch(Exception ex) { 
            // This may fail because we don't have a valuebinding (the 
            // developer may have used the binding attribute) 
            
            Object value = ((ValueHolder)component).getValue(); 
            if(value == null) { 
                // Now we're on thin ice. If there is a converter, we'll 
                // try to set this as an object array; if not, we'll just 
                // go for String.
                if(converter != null) { 
                    if(DEBUG) log("\tNo class info, converter present - using object...");
                    clazz = Object.class; 
                }
                else { 
                     if(DEBUG) log("\tNo class info, no converter - using String...");
                    clazz = String.class;
                }
                
            }
            else { 
                clazz = value.getClass().getComponentType();
                if(DEBUG) log("\tClass is " + clazz.getName());
            }        
        }

        
	// If the array members are Strings, no conversion is
	// necessary
	if (clazz.equals(String.class)) {
	    
	    if(DEBUG) { 
		log("\tArray class is String, no conversion necessary"); 
		log("\tValues are "); 
		for(int counter = 0; counter < rawValues.length; ++counter) { 
		    log("\t" + rawValues[counter]); 
		} 
	    }            
	    
	    return rawValues; 
        }

	// We know rawValues is not null
	//
	int arraySize = 0; 
	arraySize = rawValues.length; 
	if(DEBUG) {
	    log("\tNumber of values is " + //NOI18N
		String.valueOf(arraySize)); 
	} 

        Object valueArray = Array.newInstance(clazz, arraySize);

	// If there are no new values, return an empty array
	if(arraySize == 0) { 
	    if(DEBUG) { 
		log("\tEmpty value array, return new empty array"); 
		log("\tof type " + valueArray.toString()); 
	    } 
	    return valueArray; 
	} 

	// Populate the array by converting each of the raw values

	// If there is no converter, look for the default converter
	if(converter == null) { 
	    if(DEBUG) log("\tAttempt to get a default converter"); 
	    converter = getConverterForClass(clazz); 
	} 
	else if(DEBUG) log("\tRetrieved converter attached to component"); 

        int counter; 
	if(converter == null) { 

	    if(DEBUG) log("\tNo converter found");

	    if(clazz.equals(Object.class)) {
		if(DEBUG) { 
		    log("\tArray class is object, return the String array"); 
		    log("\tValues are\n"); 
		    for(counter = 0; counter < rawValues.length; ++counter) { 
			log("\n" + rawValues[counter]); 
		    } 
		} 
		return rawValues;
	    }

	    // Failed to deal with submitted data. Throw an
	    // exception. 
	    String valueString = "";
	    for (counter  = 0; counter < rawValues.length; counter++) {
		valueString = valueString + " " + rawValues[counter]; //NOI18N
	    }
	    Object [] params = {
		valueString,
		"null Converter"
	    };

	    String message = "Could not find converter for " + valueString;
	    throw new ConverterException(message); 
	}
        

        if(clazz.isPrimitive()) {
            for(counter = 0; counter < arraySize; ++counter) {
                addPrimitiveToArray(component, 
				    context, 
                                    converter,
				    clazz, 
				    valueArray, 
				    counter, 
				    rawValues[counter]);
	    } 
	} 

	else {
            for(counter = 0; counter < arraySize; ++counter) {
                Array.set(valueArray,  counter, converter.getAsObject
			  (context, (UIComponent)component, rawValues[counter]));
            }
        }

        return valueArray;
    }

    private static void addPrimitiveToArray(UIComponent component, 
					    FacesContext context, 
					    Converter converter, 
					    Class clazz, 
					    Object valueArray, 
					    int arrayIndex, 
					    String rawValue) { 

	Object valueObject = 
                converter.getAsObject(context, component, rawValue); 
	if(clazz.equals(Boolean.TYPE)) { 
	    boolean value = ((Boolean)valueObject).booleanValue();
	    Array.setBoolean(valueArray, arrayIndex, value);
	} 
	else if(clazz.equals(Byte.TYPE)) {
	    byte value = ((Byte)valueObject).byteValue();
	    Array.setByte(valueArray, arrayIndex, value); 
	}
	else if(clazz.equals(Double.TYPE)) {
	    double value = ((Double)valueObject).doubleValue();
	    Array.setDouble(valueArray, arrayIndex, value); 
	} 
	else if(clazz.equals(Float.TYPE)) {
	    float value = ((Float)valueObject).floatValue();
	    Array.setFloat(valueArray, arrayIndex, value); 	
	} 
	else if(clazz.equals(Integer.TYPE)) {
	    int value = ((Integer)valueObject).intValue();
	    Array.setInt(valueArray, arrayIndex, value); 	
	} 
	else if(clazz.equals(Character.TYPE)) {
	    char value = ((Character)valueObject).charValue();
	    Array.setChar(valueArray, arrayIndex, value); 
        }
	else if(clazz.equals(Short.TYPE)) {
	    short value = ((Short)valueObject).shortValue();
	    Array.setShort(valueArray, arrayIndex, value);         
	} 
	else if(clazz.equals(Long.TYPE)) {
	    long value  = ((Long)valueObject).longValue();
	    Array.setLong(valueArray, arrayIndex, value);         
        } 
    } 

    /**
     * <p>Convert a String array of submitted values to the appropriate
     * type of List for the value Object. This method assumes that
     * the value binding for the value of the component has been
     * determined to be a subclass of java.util.List, and as a
     * consequence, that the component implements ValueHolder.</p>
     * 
     * <p>To evaluate the valueBinding, use the ValueTypeEvaluator 
     * class.</p>
     * @param component The component whose submitted values are to be
     * converted
     * @param rawValues The submitted value of the component
     * @param context The FacesContext of the request
     * @see ValueTypeEvaluator
     * @throws ConverterException if the conversion fails
     * 
     * @return A List of converted values
     */
    public static Object convertValueToList(UIComponent component, 
				            String[] rawValues, 
				            FacesContext context)
        throws ConverterException {

	if(DEBUG) { 
	    log("::convertValueToList()"); 
	} 

	// By definition Converter returns null if the value to
	// convert is null. Do so here.
	//
	if (rawValues == null) {
	    return null;
	}

	// Get the class of the array members. We expect that the
	// component's value binding for its value has been determined
	// to be an array, as this is a condition of invoking this
	// method.     
	Class clazz = null; 
        
        // Get any converter specified by the page author
        Converter converter = ((ValueHolder)component).getConverter(); 
        
        try { 
            clazz = component.getValueBinding("value").
                        getType(context).getComponentType(); //NOI18N
        } 
        catch(Exception ex) { 
            // This may fail because we don't have a valuebinding (the 
            // developer may have used the binding attribute) 
            
            Object value = ((ValueHolder)component).getValue(); 
            if(value == null) { 
                // Now we're on thin ice. If there is a converter, we'll 
                // try to set this as an object array; if not, we'll just 
                // go for String.
                if(converter != null) { 
                    if(DEBUG) log("\tNo class info, converter present - using object...");
                    clazz = Object.class; 
                }
                else { 
                     if(DEBUG) log("\tNo class info, no converter - using String...");
                    clazz = String.class;
                }
                
            }
            else { 
                clazz = value.getClass().getComponentType();
                if(DEBUG) log("\tClass is " + clazz.getName());
            }        
        }

	java.util.List list = null; 
	try { 
            list = (java.util.List)(clazz.newInstance());
        }
        catch(Throwable problem) { 
	    // clazz is either abstract or an interface.
	    // we'll try a couple of reasonable List implementations 
	    if(clazz.isAssignableFrom(ArrayList.class)) { 
		list = new ArrayList(); 
	    } 
	    else if(clazz.isAssignableFrom(LinkedList.class)) { 
		list = new LinkedList(); 
	    } 
	    else if(clazz.isAssignableFrom(Vector.class)) { 
		list = new Vector(); 
	    } 
	    else { 
		String message = 
		    "Unable to convert the value of component " + //NOI18N
		    component.toString() + ". The type of the " + //NOI18N
		    "value object must be a class that can be " + //NOI18N
		    "instantiated, or it must be assignable " +   //NOI18N
		    "from ArrayList, LinkedList or Vector.";      //NOI18N
		throw new ConverterException(message, problem);
	    } 
        }

	// We know rawValues is not null
	//
	int listSize = 0; 
	listSize = rawValues.length; 
	// If there are no new values, return an empty array
	if(listSize == 0) { 
	    if(DEBUG) log("\tEmpty value array, return new empty list"); 
	    return list; 
	} 

	// Populate the list by converting each of the raw values
   
	int arrayIndex; 

	if(converter == null) { 
	    if(DEBUG) log("No converter, add the values as Strings"); 
	    for(arrayIndex = 0; arrayIndex <listSize; ++arrayIndex) { 
		list.add(rawValues[arrayIndex]); 
	    } 
	}
	else { 
	    if(DEBUG) 
		log("Using converter " + converter.getClass().getName()); 

	    for(arrayIndex = 0; arrayIndex < listSize; ++arrayIndex) { 

                if(DEBUG) { 
                    Object converted = 
			converter.getAsObject(context, component,
					      rawValues[arrayIndex]);
                    log("String value: " + rawValues[arrayIndex] +    //NOI18N
			" converts to : " + converted.toString()); //NOI18N
                }
		list.add(converter.getAsObject(context, component,
					       rawValues[arrayIndex]));
            
	    } 
	}
	return list; 
    }


    /**
     * Converts an Object (which may or may not be the value of the
     * component) to a String using the converter associated
     * with the component. This method can be used to convert the
     * value of the component, or the value of an Object associated
     * with the component, such as the objects representing the
     * options for a listbox or a checkboxgroup.
     * @param component The component that needs to display the value
     * as a String
     * @param realValue The object that the component is to display
     * @throws ConverterException if the conversion fails
     * 
     * @return If converting the Object to a String fails
     */
    public static String convertValueToString(UIComponent component,
                                              Object realValue) 
            throws ConverterException {
              
	if(DEBUG) log("convertValueToString(UIComponent, Object)"); 

        // The way the RI algorithm is written, it ends up returning
        // and empty string if the realValue is null and there is no 
        // converter, and null if there is a converter (the converter
        // is never applied). I don't think that's right, but I'm 
        // not sure what the right thing to do is. I return an empty
        // string for now. 
        
        if(realValue == null) { 
            return new String();  
        }
        
        if(realValue instanceof String) { 
            return (String)realValue; 
        } 

	if(!(component instanceof ValueHolder)) { 
	    return String.valueOf(realValue); 
	} 

        Converter converter = ((ValueHolder)component).getConverter(); 
        
        // Case 1: no converter specified for the component. Try 
        // getting a default converter, and if that fails, invoke 
        // the .toString() method. 
        if (converter == null) {
               
            // if converter attribute set, try to acquire a converter
            // using its class type. (avk note: this is the comment from 
            // the RI - not sure what it's supposed to mean)
            
            converter =  getConverterForClass(realValue.getClass());
            
            // if there is no default converter available for this identifier,
            // assume the model type to be String. Otherwise proceed to case 2.
            if (converter == null) {
                return String.valueOf(realValue); 
            }
        }
        
        // Case 2: we have found a converter.
        FacesContext context = FacesContext.getCurrentInstance(); 
        return converter.getAsString(context, component, realValue); 
    }


    /**
     * This method retrieves an appropriate converter based on the
     * type of an object. 
     * @param converterClass The name of the converter class
     * @return An instance of the appropriate converter type
     */
    public static Converter getConverterForClass(Class converterClass) {

        if (converterClass == null) {
            return null;
        }
        try {
            ApplicationFactory aFactory =
                    (ApplicationFactory) FactoryFinder.getFactory(
                    FactoryFinder.APPLICATION_FACTORY);
            Application application = aFactory.getApplication();
            return (application.createConverter(converterClass));
        } catch (Exception e) {
            return (null);
        }
    }
    
    static void log(String s) { 
        System.out.println("ConversionUtilities::" + s); 
    }

    /**
     * Return the converted value of submittedValue.
     * If submittedValue is null, return null.
     * If submittedValue is "", check the rendered value. If the
     * the value that was rendered was null, return null
     * else continue to convert.
     */
    public static Object convertRenderedValue(FacesContext context,
	    Object submittedValue, UIComponent component)
	    throws ConverterException {

        Converter converter = ((ValueHolder)component).getConverter();

	// If the component has a converter we can't assume that
	// "" should be returned if "" was rendered or "" was rendered
	// for null.
	//
	if (converter == null) {
	    // See if we rendered null.
	    // If we rendered null and the submitted value was ""
	    // return null
	    //
	    if (renderedNull(component) && submittedValue instanceof String &&
		((String)submittedValue).length() == 0) {
		return null;
	    }
	}
	// If submittedValue is null, convertValueToObject returns null
	// as does Converter by definition.
	//
        return ConversionUtilities.convertValueToObject(component,
		(String)submittedValue, context);
   }

    /**
     * Record the value being rendered.
     *
     * @param component The component being rendered.
     * @param value The value being rendered.
     */
    public static void setRenderedValue(UIComponent component, Object value) {

	// First remove the attribute.
	// Need to do this because a null value does nothing.
	// Therefore the last value specified will remain.
	//
	component.getAttributes().remove(
		ConversionUtilities.RENDERED_NULL_VALUE);

	// If the value is null, put barfs.
	// So getRenderedValue will return null, if there is no
	// RENDERED_NULL_VALUE property. Interpret this to mean that
	// "null" was saved. I'd rather not but as long as the
	// explicit property is not sought outside of these methods
	// then it shouldn't be a problem.
	//

	if (value == null) {
	    component.getAttributes().put(
		ConversionUtilities.RENDERED_NULL_VALUE, Boolean.TRUE);
	}
    }

    /**
     * Return true if the stored rendered value on the specified
     * component was null.
     */
    public static boolean renderedNull(UIComponent component) {
	    return (Boolean)component.getAttributes().get(
		    ConversionUtilities.RENDERED_NULL_VALUE) == null ?
			false : true;
    }

    /**
     * Remove the stored rendered value from the specified component.
     */
    public static void removeRenderedValue(UIComponent component) {
	component.getAttributes().remove(RENDERED_NULL_VALUE);
    }

    private final static String RENDERED_TABLE_NULL_VALUES =
	"_RENDERED_TABLE_NULL_VALUES_";

    /**
     * Used to preserve the rendered value when a component is
     * used within a table. Since there is only one component
     * instance when used in a table column the rendered value
     * must be maintained for each "virtual" component instance
     * for the rows in the column.
     *
     * @param context The current FacesContext for this request.
     * @param component The component that is appearing in the table.
     */
    public static void saveRenderedValueState(FacesContext context,
		UIComponent component) {

	boolean renderedNullValue = renderedNull(component);
	HashMap rv = (HashMap)component.getAttributes().get(
		RENDERED_TABLE_NULL_VALUES);

	if (rv == null) {
	    if (renderedNullValue) {
		rv = new HashMap();
		    component.getAttributes().put(
			RENDERED_TABLE_NULL_VALUES, rv);
		rv.put(component.getClientId(context), null);
		component.getAttributes().remove(RENDERED_NULL_VALUE);
	    }
	} else
	if (!renderedNullValue) {
	    rv.remove(component.getClientId(context));
	} else {
	    rv.put(component.getClientId(context), null);
	    removeRenderedValue(component);
	}
    }

    /**
     * Used to restore the rendered value when a component is
     * used within a table. Since there is only one component
     * instance when used in a table column the rendered value
     * must be maintained and restored for each "virtual" component
     * instance for the rows in the column.
     *
     * @param context The current FacesContext for this request.
     * @param component The component that is appearing in the table.
     */
    public static void restoreRenderedValueState(FacesContext context,
		UIComponent component) {
	HashMap rv = (HashMap)component.getAttributes().get(
	    RENDERED_TABLE_NULL_VALUES);
	if (rv != null) {
	    if (rv.containsKey(component.getClientId(context))) {
		setRenderedValue(component, null);
	    }
	}
    }

    /**
     * Remove the storage for the "virtual" for the specified
     * component used to save the rendered value for the "virtual"
     * instances of this component when used in a table. 
     */
    public static void removeSavedRenderedValueState(UIComponent component) {
	component.getAttributes().remove(RENDERED_TABLE_NULL_VALUES);
    }
}
