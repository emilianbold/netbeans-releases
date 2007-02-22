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

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Union;
import org.netbeans.modules.xml.schema.ui.basic.editors.MemberTypesEditor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This class provides property support for properties having boolean values.
 * It provides support for properties which default to null or to false
 * when not specified.
 * @author Ajit Bhate
 */
public class MemberTypesProperty extends BaseSchemaProperty {
    
    /**
     * Creates a new instance of MemberTypesProperty.
     * 
     * @param component The schema component which property belongs to.
     * @param property The property name.
     * @param propDispName The display name of the property.
     * @param propDesc Short description about the property.
     * @param isDefaultFalse If the default value for this property is false.
     *     In such case BooleanDefaultFalseEditor will be used as propertyeditor, 
     *     BooleanEditor otherwise which supports null as default values.
     * @throws java.lang.NoSuchMethodException If no getter and setter for the property are found
     */
    public MemberTypesProperty(SchemaComponent component, String property,
            String dispName, String desc)
            throws NoSuchMethodException {
        super(component,
                Collection.class,
                // The getter method for schema properties start with is
                component.getClass().getMethod(BaseSchemaProperty.
                firstLetterToUpperCase(property, "get"), new Class[0]),
//                component.getClass().getMethod(firstLetterToUpperCase(
//                property, "set"), new Class[]{Boolean.class}),
                null,
                property,
                dispName,
                desc,
                MemberTypesEditor.class
                );
    }
    
    @SuppressWarnings("unchecked")
    public void setValue(Object o) throws IllegalAccessException, InvocationTargetException {
        List<NamedComponentReference<GlobalSimpleType>> oldSelectionRef = 
                (List<NamedComponentReference<GlobalSimpleType>>) getValue();
        List<NamedComponentReference<GlobalSimpleType>> newSelectionRef = 
                (List<NamedComponentReference<GlobalSimpleType>>) o;
        int oIdx = 0;
        int nIdx = 0;
        Union unoin = (Union)super.getComponent();
        // try to minimize updates
        if(oldSelectionRef!= null && newSelectionRef != null) {
            for (;oIdx<oldSelectionRef.size()&&nIdx<newSelectionRef.size();oIdx++) {
                if(!oldSelectionRef.get(oIdx).equals(newSelectionRef.get(nIdx))) {
                    unoin.removeMemberType(oldSelectionRef.get(oIdx));
                } else {
                    nIdx++;
                }
            }
        }
        if(oldSelectionRef!=null) {
            for(int i=oIdx;i<oldSelectionRef.size();i++)
                unoin.removeMemberType(oldSelectionRef.get(i));
        }
        if(newSelectionRef!=null) {
            for(int i=nIdx;i<newSelectionRef.size();i++)
                unoin.addMemberType(newSelectionRef.get(i));
        }
    }

    public PropertyEditor getPropertyEditor() {
        return new MemberTypesEditor(super.getComponent());
    }
    
}
