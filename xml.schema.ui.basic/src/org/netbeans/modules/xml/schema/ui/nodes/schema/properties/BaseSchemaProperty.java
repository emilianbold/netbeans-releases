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
 * BooleanProperty.java
 *
 * Created on January 5, 2006, 3:21 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.nodes.schema.properties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.PropertySupport;

/**
 * The Base class for schema component properties.
 * It extends PropertySupport.Reflection
 * In general all schema properties allow restoring to deafult, 
 * so this class provides skeleton impl of the required methods.
 * Subclasses can override these methods for different behaviour.
 * The canWrite method is overridden to mark properties as uneditable,
 * if schema component belongs to read only model.
 * This class provides constructors which create properties with localized user 
 * friendly names and description and customized editor classes.
 * They can be changed after creation, using the setter methods.
 * This class can be instantiated, but will be subclassed for specific type of properties.
 * For example the BooleanProperty class subclasses this class to support
 * boolean properties.
 * Example usage: 
 * <CODE>        
 * Property myProp = new BaseSchemaProperty(
 * myComp, // schema component
 * String.class // value type
 * "myProperty", // property name
 * "My property", // display name
 * "My property Description",	// descr
 * MyPropertyEditor.class // if default value is false
 * );
</CODE>
 * @author Ajit Bhate
 */
public class BaseSchemaProperty extends PropertySupport.Reflection {
    
    /**
     * Creates a new instance of BaseSchemaProperty.
     * @param component The schema component which property belongs to.
     * @param valueType The class type of the property.
     * @param property The property name.
     * @param propDispName The display name of the property.
     * @param propDesc Short description about the property.
     * @param propEditorClass The property editor class 
     *    if the property needs special property editor.
     *    If no property editor class is provided default editor 
     *    (for type of property) will be used.
     *    The property editor class must provide a default constructor.
     *    Subclasses can also override 
     *    getPropertyEditor method to provide property editor.
     * @throws java.lang.NoSuchMethodException If no getter and setter for the property are found
     */
    public BaseSchemaProperty(SchemaComponent component, 
            Class valueType,
            String property,
            String propDispName, 
            String propDesc, 
            Class propEditorClass)
            throws NoSuchMethodException {
        super(component,valueType,property);
        super.setName(property);
        super.setDisplayName(propDispName);
        super.setShortDescription(propDesc);
        if(propEditorClass!=null)
            super.setPropertyEditorClass(propEditorClass);
    }
    
    /**
     * Creates a new instance of BaseSchemaProperty.
     * @param component The schema component which property belongs to.
     * @param valueType The class type of the property.
     * @param getter The property getter method.
     * @param setter The property setter method.
     * @param property The property name.
     * @param propDispName The display name of the property.
     * @param propDesc Short description about the property.
     * @param propEditorClass The property editor class if the property needs 
     *                        special property editor. If no property editor 
     *                        class is provided default editor 
     *                        (for type of property) will be used.
     *                        The property editor class must provide a 
     *                        default constructor. Subclasses can also override 
     *                        getPropertyEditor method to provide property editor.
     */
    public BaseSchemaProperty(SchemaComponent component, 
            Class valueType,
            Method getter,
            Method setter,
            String property,
            String propDispName, 
            String propDesc, 
            Class propEditorClass) {
        super(component,valueType,getter,setter);
        super.setName(property);
        super.setDisplayName(propDispName);
        super.setShortDescription(propDesc);
        if(propEditorClass!=null)
            super.setPropertyEditorClass(propEditorClass);
    }
    
    /**
     * This api determines if this property is editable
     * @return Returns true if the property is editable, false otherwise.
     */
    @Override
    public boolean canWrite() {
        // Check for null model since component may have been removed.
        SchemaModel model = getComponent().getModel();
        return XAMUtils.isWritable(model);
    }
    
    // Methods to support restore to default
    /**
     * This api determines if this property has default value.
     * If the property value is null, its considered as default value.
     * Subclasses can override if different behaviour expected.
     * @return Returns true if the property is default value, false otherwise.
     */
    @Override
    public boolean isDefaultValue () {
        try {
            return getValue()==null;
        } catch (IllegalArgumentException ex) {
        } catch (InvocationTargetException ex) {
        } catch (IllegalAccessException ex) {
        }
        return false;
    }

    /**
     * This api determines if this property supports resetting default value.
     * This returns true always.
     * Subclasses can override if different behaviour expected.
     */
    @Override
    public boolean supportsDefaultValue () {
        return true;
    }

    /**
     * This api resets the property to its default value.
     * It sets property value to null which is considered as default value.
     * Subclasses can override if different behaviour expected.
     */
    @Override
    public void restoreDefaultValue () throws IllegalAccessException, InvocationTargetException {
        setValue(null);
    }

    protected SchemaComponent getComponent() {
        return (SchemaComponent)instance;
    }

    protected void setComponent(SchemaComponent sc) {
        instance = sc;
    }

    /** 
     * Helper method to convert the first letter of a string to uppercase.
     * And prefix the string with some next string.
     */
    public static String firstLetterToUpperCase(String s, String pref) {
        switch (s.length()) {
            case 0:
                return pref;
                
            case 1:
                return pref + Character.toUpperCase(s.charAt(0));
                
            default:
                return pref + Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }
    
}