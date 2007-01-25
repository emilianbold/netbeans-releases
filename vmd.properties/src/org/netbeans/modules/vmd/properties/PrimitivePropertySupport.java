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
final class PrimitivePropertySupport extends DefaultPropertySupport {

    private DesignPropertyDescriptor designerPropertyDescriptor;
    private String displayName;
    private Object value;
    
    public PrimitivePropertySupport(DesignPropertyDescriptor designerPropertyDescriptor, Class type) {
        super(designerPropertyDescriptor, type);
        
        this.designerPropertyDescriptor = designerPropertyDescriptor;
        if (designerPropertyDescriptor.getPropertyNames() !=null &&! designerPropertyDescriptor.getPropertyNames().isEmpty()) {
            if (designerPropertyDescriptor.getPropertyEditorType().equals(Boolean.class) ||
                designerPropertyDescriptor.getPropertyEditorType().equals(Integer.class)) {
                this.value = readPropertyValue(designerPropertyDescriptor.getComponent(), designerPropertyDescriptor.getPropertyNames().iterator().next()).getPrimitiveValue ();
            } else
                this.value = readPropertyValue(designerPropertyDescriptor.getComponent(), designerPropertyDescriptor.getPropertyNames().iterator().next());
        }
        
        if (getPropertyEditor() instanceof DesignPropertyEditor) {
            ((DesignPropertyEditor)getPropertyEditor()).resolve(
                designerPropertyDescriptor.getComponent(),
                designerPropertyDescriptor.getPropertyNames(),
                this.value,
                this,
                ((DesignPropertyEditor) getPropertyEditor()).getInplaceEditor(),
                designerPropertyDescriptor.getPropertyDisplayName()
                );
        }
        if (getPropertyEditor() instanceof DesignPropertyEditor) {
            String title = ((DesignPropertyEditor) getPropertyEditor()).getCustomEditorTitle();
            if ( title != null)
                setValue(PROPERTY_CUSTOM_EDITOR_TITLE, title);
        }
    }
    
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }
    
    public void setValue(final Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String propertyName = designerPropertyDescriptor.getPropertyNames().iterator().next();
        final GroupValue tempValue = new GroupValue(Collections.singletonList(propertyName));
        tempValue.putValue(propertyName, value);
        this.value = value;
        if (designerPropertyDescriptor.getComponent() == null)
            throw new IllegalStateException("No DesignComponent for designerPropertyDescriptor : " + designerPropertyDescriptor.getPropertyDisplayName()); //NOI18N
        
        if (getPropertyEditor() instanceof DesignPropertyEditor)
            SaveToModelSupport.saveToModel(designerPropertyDescriptor.getComponent(), tempValue, (DesignPropertyEditor) getPropertyEditor());
        else
            SaveToModelSupport.saveToModel(designerPropertyDescriptor.getComponent(), tempValue, null);
        
    }
    
    public String getHtmlDisplayName() {
        if (designerPropertyDescriptor.getPropertyNames().isEmpty())
            return designerPropertyDescriptor.getPropertyDisplayName();
        designerPropertyDescriptor.getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                if (designerPropertyDescriptor.getComponent().isDefaultValue(designerPropertyDescriptor.getPropertyNames().iterator().next()))
                    displayName = designerPropertyDescriptor.getPropertyDisplayName();
                else
                    displayName = "<b>" + designerPropertyDescriptor.getPropertyDisplayName()+"</b>";  // NOI18N
            }
        });
        
        return displayName;
    }
    
}
