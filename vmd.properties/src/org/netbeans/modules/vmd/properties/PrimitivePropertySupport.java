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

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import org.netbeans.modules.vmd.api.properties.GroupValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.api.properties.DesignPropertyDescriptor;

/**
 *
 * @author Karol Harezlak
 */
public final class PrimitivePropertySupport extends DefaultPropertySupport {
    
    //private DesignPropertyDescriptor designerPropertyDescriptor;
    private String displayName;
    private Object value;
    
    public PrimitivePropertySupport(DesignPropertyDescriptor designerPropertyDescriptor, Class type) {
        super(designerPropertyDescriptor, type);
    }
    
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }
    
    public void setValue(final Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (getPropertyEditor() instanceof DesignPropertyEditor) {
            DesignPropertyEditor propertyEditor = (DesignPropertyEditor) getPropertyEditor();
            if (propertyEditor.canEditAsText() != null)
                setValue("canEditAsText", propertyEditor.canEditAsText()); //NOI18N
        }
        String propertyName = getDesignPropertyDescriptor().getPropertyNames().iterator().next();
        final GroupValue tempValue = new GroupValue(Collections.singletonList(propertyName));
        tempValue.putValue(propertyName, value);
        this.value = value;
        if (getDesignPropertyDescriptor().getComponent() == null)
            throw new IllegalStateException("No DesignComponent for getDesignerPropertyDescriptor() : " + getDesignPropertyDescriptor().getPropertyDisplayName()); //NOI18N
        if (getPropertyEditor() instanceof DesignPropertyEditor)
            SaveToModelSupport.saveToModel(getDesignPropertyDescriptor().getComponent(), tempValue, (DesignPropertyEditor) getPropertyEditor());
        else
            SaveToModelSupport.saveToModel(getDesignPropertyDescriptor().getComponent(), tempValue, null);
        
    }
    
    public String getHtmlDisplayName() {
        if (getDesignPropertyDescriptor().getPropertyNames().isEmpty())
            return getDesignPropertyDescriptor().getPropertyDisplayName();
        getDesignPropertyDescriptor().getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                if (getDesignPropertyDescriptor().getComponent().isDefaultValue(getDesignPropertyDescriptor().getPropertyNames().iterator().next()))
                    displayName = getDesignPropertyDescriptor().getPropertyDisplayName();
                else
                    displayName = "<b>" + getDesignPropertyDescriptor().getPropertyDisplayName()+"</b>";  // NOI18N
            }
        });
        
        return displayName;
    }

    protected void update() {
        if (getDesignPropertyDescriptor().getPropertyNames() != null &&! getDesignPropertyDescriptor().getPropertyNames().isEmpty())
                this.value = readPropertyValue(getDesignPropertyDescriptor().getComponent(), getDesignPropertyDescriptor().getPropertyNames().iterator().next());
        if (getPropertyEditor() instanceof DesignPropertyEditor) {
            DesignPropertyEditor propertyEditor = (DesignPropertyEditor)getPropertyEditor();
            propertyEditor.resolve(
                    getDesignPropertyDescriptor().getComponent(),
                    getDesignPropertyDescriptor().getPropertyNames(),
                    this.value,
                    this,
                    getDesignPropertyDescriptor().getPropertyDisplayName()
            );
            propertyEditor.resolveInplaceEditor(propertyEditor.getInplaceEditor());
            String title = propertyEditor.getCustomEditorTitle();
            if ( title != null)
                setValue(PROPERTY_CUSTOM_EDITOR_TITLE, title);
        }
    }
    
}
