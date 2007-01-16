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
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.api.properties.DesignPropertyDescriptor;
import org.netbeans.modules.vmd.api.properties.GroupValue;

/**
 *
 * @author Karol Harezlak
 */
final class AdvancedPropertySupport extends DefaultPropertySupport {

    private DesignPropertyDescriptor designerPropertyDescriptor;
    private String displayName;
    private GroupValue value;

    public AdvancedPropertySupport(final DesignPropertyDescriptor designerPropertyDescriptor, Class type) {
        super(designerPropertyDescriptor, type);
        
        this.value = new GroupValue(designerPropertyDescriptor.getPropertyNames());
        if (designerPropertyDescriptor.getPropertyNames() !=null &&! designerPropertyDescriptor.getPropertyNames().isEmpty()) {
            for (String propertyName : designerPropertyDescriptor.getPropertyNames()) {
                value.putValue(propertyName, readPropertyValue(designerPropertyDescriptor.getComponent(), propertyName));
            }
        }
        this.designerPropertyDescriptor = designerPropertyDescriptor;
        
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
    }
    
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }
    
    public void setValue(final Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (value instanceof GroupValue) {
            this.value = (GroupValue) value;
            if (getPropertyEditor() instanceof DesignPropertyEditor)
                SaveToModelSupport.saveToModel(designerPropertyDescriptor.getComponent(), this.value, (DesignPropertyEditor) getPropertyEditor());
            else
                SaveToModelSupport.saveToModel(designerPropertyDescriptor.getComponent(), this.value, null);
        } else
            throw new IllegalArgumentException("Wrong type"); //NOI18N
    }
    
    public String getHtmlDisplayName() {
        if (designerPropertyDescriptor.getPropertyNames().isEmpty())
            return designerPropertyDescriptor.getPropertyDisplayName();
        
        designerPropertyDescriptor.getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                boolean isDefault = false;
                
                for (String propertyName : designerPropertyDescriptor.getPropertyNames()) {
                    if (designerPropertyDescriptor.getComponent().isDefaultValue(propertyName)) {
                        isDefault = false;
                        return;
                    }
                }
                if (isDefault)
                    displayName = designerPropertyDescriptor.getPropertyDisplayName();
                else
                    displayName = "<b>" + designerPropertyDescriptor.getPropertyDisplayName()+"</b>";  // NOI18N
            }
        });
        
        return displayName;
    }
    
}
