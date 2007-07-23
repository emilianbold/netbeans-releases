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

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.api.properties.DesignPropertyDescriptor;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author Karol Harezlak
 */
public abstract class DefaultPropertySupport extends PropertySupport {

    public static final String PROPERTY_VALUE_NULL = "PROPERTY_NULL_VALUE_FOR_FEATURE_DESCRIPTOR"; //NOI18N //work around for hashmap which cant accept null
    public static final String PROPERYT_INPLACE_EDITOR = "inplaceEditor"; //NOI18N
    public static final String PROPERTY_CUSTOM_EDITOR_TITLE = "title"; //NOI18N
    
    private PropertyValue propertyValue;
    private PropertyEditor propertyEditor;
    private List<String> propertyNames;
    private DesignPropertyDescriptor designPropertyDescriptor;

    @SuppressWarnings("unchecked")
    DefaultPropertySupport(DesignPropertyDescriptor designerPropertyDescriptor, Class type) {
        super(designerPropertyDescriptor.getPropertyNames().iterator().next(), type, designerPropertyDescriptor.getPropertyDisplayName(), designerPropertyDescriptor.getPropertyToolTip(), true, true);
        this.designPropertyDescriptor = designerPropertyDescriptor;
        propertyEditor = designerPropertyDescriptor.getPropertyEditor();
        propertyNames = designerPropertyDescriptor.getPropertyNames();
        update();
        if (getPropertyEditor() instanceof DesignPropertyEditor && ((DesignPropertyEditor) getPropertyEditor()).getInplaceEditor() != null) {
            setValue(PROPERYT_INPLACE_EDITOR, ((DesignPropertyEditor) getPropertyEditor()).getInplaceEditor());
        }
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (propertyEditor != null) {
            return propertyEditor;
        }
        return super.getPropertyEditor();
    }

    protected PropertyValue readPropertyValue(final DesignComponent component, final String propertyName) {
        component.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                propertyValue = component.readProperty(propertyName);
            }
        });
        return propertyValue;
    }

    public boolean canWrite() {
        if (propertyEditor instanceof DesignPropertyEditor) {
            return ((DesignPropertyEditor) propertyEditor).canWrite();
        }
        return super.canWrite();
    }

    @SuppressWarnings(value = "unchecked")
    public void restoreDefaultValue() throws IllegalAccessException, InvocationTargetException {
        if (propertyEditor instanceof DesignPropertyEditor && propertyNames != null && (!propertyNames.isEmpty())) {
            setValue(((DesignPropertyEditor) propertyEditor).getDefaultValue());
        } else {
            super.restoreDefaultValue();
        }
    }

    public boolean isDefaultValue() {
        if (propertyEditor instanceof DesignPropertyEditor) {
            return ((DesignPropertyEditor) propertyEditor).isDefaultValue();
        }
        return super.isDefaultValue();
    }

    public boolean supportsDefaultValue() {
        if (propertyEditor instanceof DesignPropertyEditor) {
            return ((DesignPropertyEditor) propertyEditor).supportsDefaultValue();
        }
        return super.supportsDefaultValue();
    }

    protected DesignPropertyDescriptor getDesignPropertyDescriptor() {
        return designPropertyDescriptor;
    }

    protected abstract void update();
}
