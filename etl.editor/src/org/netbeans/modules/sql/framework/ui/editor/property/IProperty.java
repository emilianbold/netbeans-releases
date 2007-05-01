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

package org.netbeans.modules.sql.framework.ui.editor.property;

import java.beans.PropertyEditor;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public interface IProperty extends INode {

    /**
     * get the group to which this property belongs
     * 
     * @return group to which property belongs
     */
    public String getGroup();

    /**
     * get the position where this property should appear in the property sheet gui
     * 
     * @return position
     */
    public int getPosition();

    /**
     * get the property customizer
     * 
     * @return property customizer
     */
    public IPropertyCustomizer getPropertyCustomizer();

    /**
     * Get the optional property editor which can be used to edit this property
     * 
     * @return property editor
     */
    public PropertyEditor getPropertyEditor();

    /**
     * get the gui type of this property
     * 
     * @return gui type
     */
    public String getType();

    public Object getValue() throws java.lang.IllegalAccessException, java.lang.reflect.InvocationTargetException;

    /**
     * get the type of value of this property
     * 
     * @return property value type
     */
    public Class getValueType();

    /**
     * does this property has a custom editor
     * 
     * @return whether property has a custom editor
     */
    public boolean isCustomEditor();

    /**
     * Does this property has a default value
     * 
     * @returrn whether property has a default value
     */
    public boolean isDefault();

    /**
     * Is this property read only, if readonly then gui can not edit this property
     * 
     * @return whether this property is read only
     */
    public boolean isReadOnly();

    /**
     * Is this property require if yes then this property should always have a vaule
     * 
     * @return whether this property is required
     */
    public boolean isRequired();

    /**
     * is this value valid
     * 
     * @return value if valid
     */
    public boolean isValid();

    /**
     * deos this property has a custom editor
     * 
     * @param editor whether property has a custom editor
     */
    public void setCustomEditor(String editor);

    /**
     * Does this property has a default value
     * 
     * @returrn whether property has a default value
     */
    public void setDefault(boolean def);

    /**
     * Does this property has a default value
     * 
     * @returrn whether property has a default value
     */
    public void setDefault(String def);

    /**
     * set the optional property editor which can be used to edit this property
     * 
     * @return property editor
     */
    public void setEditorClass(String editorClass);

    /**
     * set the group to which this property belongs
     * 
     * @return group to which property belongs
     */
    public void setGroup(String group);

    /**
     * set the owner object which has this property
     */
    public void setOwnerObject(Object obj);

    /**
     * set the position where this property should appear in the property sheet gui
     * 
     * @return position
     */
    public void setPosition(String position);

    /**
     * set the property customizer
     * 
     * @param customizer customizer
     */
    public void setPropertyCustomizer(IPropertyCustomizer customizer);

    /**
     * set the optional property editor which can be used to edit this property
     * 
     * @return property editor
     */
    public void setPropertyEditor(PropertyEditor editor);

    /**
     * Is this property read only, if readonly then gui can not edit this property
     * 
     * @return whether this property is read only
     */
    public void setReadOnly(boolean readOnly);

    /**
     * Is this property read only, if readonly then gui can not edit this property
     * 
     * @return whether this property is read only
     */
    public void setReadOnly(String readOnly);

    /**
     * Is this property require if yes then this property should always have a vaule
     * 
     * @return whether this property is required
     */
    public void setRequired(boolean required);

    /**
     * Is this property require if yes then this property should always have a vaule
     * 
     * @return whether this property is required
     */
    public void setRequired(String required);

    /**
     * get the gui type of this property
     * 
     * @return gui type
     */
    public void setType(String type);

    public void setValue(Object obj) throws java.lang.IllegalAccessException, java.lang.IllegalArgumentException,
            java.lang.reflect.InvocationTargetException;

    /**
     * set the type of value of this property
     * 
     * @return property value type
     */
    public void setValueType(String valueType);

}

