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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.properties;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.properties.GroupValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;

/**
 *
 * @author Karol Harezlak
 */
final class SaveToModelSupport {
    
    private SaveToModelSupport() {
    }
    
    public static void saveToModel(final DesignComponent component,
                                   final GroupValue values,
                                   final DesignPropertyEditor propertyEditor) {
        
        if (component == null || values == null)
            throw new IllegalArgumentException("Null argument exception"); //NOI18N
        
        if (component.getDocument().getTransactionManager().isAccess())
            return;
        
        component.getDocument().getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                if (propertyEditor != null && propertyEditor.isExecuteInsideWriteTransactionUsed()) {    
                    if (! propertyEditor.executeInsideWriteTransaction())
                        return;
                }
                for (String propertyName : values.getPropertyNames()) {
                    if (values.getValue(propertyName) instanceof PropertyValue) {
                        component.writeProperty(propertyName, (PropertyValue) values.getValue(propertyName));
                        continue;
                    }
                    if (values.getValue(propertyName) == null)
                        component.writeProperty(propertyName, PropertyValue.createNull());
                    else if (values.getValue(propertyName) instanceof DesignComponent) {
                        component.writeProperty(propertyName,
                            PropertyValue.createComponentReference((DesignComponent) values.getValue(propertyName)));
                    } else {
                        component.writeProperty(propertyName,
                            PropertyValue.createValue(component.getDocument().getDocumentInterface().getProjectType(),
                            getComponentTypeID(component,propertyName) ,
                            values.getValue(propertyName)));
                    }
                }
            }
        });
    }
    
    private static TypeID getComponentTypeID(DesignComponent component, String propertyName) {
        return  component.getComponentDescriptor().getPropertyDescriptor(propertyName).getType();
    }
    
}
