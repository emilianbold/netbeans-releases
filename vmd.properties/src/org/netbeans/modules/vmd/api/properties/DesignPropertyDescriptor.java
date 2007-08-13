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

package org.netbeans.modules.vmd.api.properties;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.DesignComponent;

/**
 *
 * @author Karol Harezlak
 */

public abstract class DesignPropertyDescriptor {

    /** Default implementation of DesignPropertyDescriptor
     *
     */
    public static final DesignPropertyDescriptor create(String displayName, String toolTip, String category, DesignPropertyEditor propertyEditor, Class propertyEditorType, String... propertyNames) {
        return new DefaultPropertyDescriptor(displayName, toolTip, category, propertyEditor, propertyEditorType, propertyNames);
    }

    //First name of the list is always name which will be return when Property.getName
    public abstract List<String> getPropertyNames();

    public abstract String getPropertyDisplayName();

    public abstract String getPropertyToolTip();

    public abstract String getPropertyCategory();

    public abstract DesignPropertyEditor getPropertyEditor();

    public abstract DesignComponent getComponent();

    @Deprecated
    public abstract Class getPropertyEditorType();

    public abstract void init(DesignComponent component);

    private static class DefaultPropertyDescriptor extends DesignPropertyDescriptor {

        private List<String> propertyNames;
        private String displayName;
        private String toolTip;
        private String category;
        private DesignPropertyEditor propertyEditor;
        private WeakReference<DesignComponent> component;
        private Class propertyEditorType;

        private DefaultPropertyDescriptor(String displayName, String toolTip, String category, DesignPropertyEditor propertyEditor, Class propertyEditorType, String... propertyNames) {
            if (category == null) {
                throw new IllegalArgumentException("Empty category"); // NOI18N
            }
            this.propertyNames = Arrays.asList(propertyNames);
            this.displayName = displayName;
            this.toolTip = toolTip;
            this.category = category;
            this.propertyEditor = propertyEditor;
            this.propertyEditorType = propertyEditorType;
        }

        public List<String> getPropertyNames() {
            return propertyNames;
        }

        public String getPropertyDisplayName() {
            return displayName;
        }

        public String getPropertyToolTip() {
            return toolTip;
        }

        public String getPropertyCategory() {
            return category;
        }

        public DesignPropertyEditor getPropertyEditor() {
            return propertyEditor;
        }

        public DesignComponent getComponent() {
            if (component == null) {
                return null;
            }
            return component.get();
        }

        public Class getPropertyEditorType() {
            if (propertyEditor != null) {
                return propertyEditor.getClass();
            } else {
                return propertyEditorType;
            }
        }

        public void init(DesignComponent component) {
            this.component = new WeakReference<DesignComponent>(component);
        }
    }
}
