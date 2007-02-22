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

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.nodes.PropertySupport;

/**
 * This class provides property support for properties having global references.
 * @author Ajit Bhate
 */
public class GlobalReferenceProperty extends PropertySupport.Reflection {
    
    private List<Class> filterTypes;
    private String referenceTypeDisplayName;
    private String typeDisplayName;

    /**
     * Creates a new instance of GlobalReferenceProperty.
     * 
     * 
     * @param component The schema component which property belongs to.
     * @param property The property name.
     * @param propDispName The display name of the property.
     * @param propDesc Short description about the property.
     * @param isPrimitive distinguish between int and Integer. temporary property
     * Assumes that the property editor is default editor for Integer.
     * If special editor needed, subclasses and instances must set it explicitly.
     * @throws java.lang.NoSuchMethodException If no getter and setter for the property are found
     */
    public GlobalReferenceProperty(AXIComponent component, 
            String property, String dispName, String desc, 
            String typeDisplayName, String referenceTypeDisplayName,
            Class referenceType, List<Class> filterTypes) 
            throws NoSuchMethodException {
        super(component,referenceType,property);
        super.setName(property);
        super.setDisplayName(dispName);
        super.setShortDescription(desc);
        this.filterTypes = filterTypes;
        this.referenceTypeDisplayName = referenceTypeDisplayName;
        this.typeDisplayName = typeDisplayName;
    }

    protected AXIComponent getComponent() {
        return (AXIComponent) instance;
    }

    protected void setComponent(AXIComponent sc) {
        instance = sc;
    }
	
    
    /**
     * This api determines if this property is editable
     * @return Returns true if the property is editable, false otherwise.
     */
    @Override
    public boolean canWrite() {
		// Check for null model since component may have been removed.
		if(getComponent()== null || 
				(getComponent().getModel() == null))
			return false;
		SchemaModel model = getComponent().getModel().getSchemaModel();
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
     * Overriden to return false always.
     * Subclasses can override if different behaviour expected.
     */
    @Override
    public boolean supportsDefaultValue () {
        return false;
    }

    /**
     * This method returns the property editor.
     * Overridden to return special editor.
     */
    @Override
    public java.beans.PropertyEditor getPropertyEditor() {
        return new GlobalReferenceEditor(
				getComponent(), typeDisplayName, 
                getDisplayName(), referenceTypeDisplayName, filterTypes);
    }
    
}
