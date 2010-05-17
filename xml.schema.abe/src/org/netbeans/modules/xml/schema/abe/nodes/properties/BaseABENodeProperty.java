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

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.abe.nodes.ABEAbstractNode;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.PropertySupport;

/**
 * The Base class for schema component properties.
 * It extends PropertySupport.Reflection
 * In general all ABE node properties allow restoring to default,
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
 * Property myProp = new BaseABENodeProperty(
 * myComp, // schema component
 * String.class // value type
 * "myProperty", // property name
 * "My property", // display name
 * "My property Description",	// descr
 * MyPropertyEditor.class // if default value is false
 * );
 * </CODE>
 * 
 * @author Ayub Khan
 */
public class BaseABENodeProperty extends PropertySupport.Reflection {
    
    /**
	 * Creates a new instance of BaseABENodeProperty.
	 * 
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
    public BaseABENodeProperty(Object component, 
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
	 * Creates a new instance of BaseABENodeProperty.
	 * 
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
    public BaseABENodeProperty(Object component, 
            Class valueType,
            String property,
            String propDispName, 
            String propDesc)
            throws NoSuchMethodException {
        this(component, valueType, property, propDispName, propDesc, null);
    }
	
    /**
	 * Creates a new instance of BaseABENodeProperty.
	 * 
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
    public BaseABENodeProperty(Object component, 
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
        AXIComponent c = getComponent();
        if(c == null || (c.getModel() == null))
            return false;
        
        AXIComponent o = c.getOriginal();
        if(c != o && c.isReadOnly()) {
            return false;
        }
        
        SchemaModel model = c.getModel().getSchemaModel();
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

    protected AXIComponent getComponent() {
        if(instance instanceof ABEAbstractNode){
            //this is a node so return the axi component associated with the node
            return ((ABEAbstractNode)instance).getAXIComponent();
        }
        return (AXIComponent) instance;
    }

    protected void setComponent(AXIComponent sc) {
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
