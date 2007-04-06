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

package org.netbeans.modules.form;

import java.beans.*;

/**
 * An interface representing a context of a FormProperty.
 *
 * @author Tomas Pavek
 */

public interface FormPropertyContext {

    /**
     * Describes whether the FormPropertyEditor can be used for editing properties.
     * This property editor encapsulates multiple property editors which can be used
     * for given property - this feature is not suitable e.g. for event properties,
     * and sometimes not possible beacuase of restrictions in XML storage format
     * (which must stay compatible with previous versions).
     * @return true if multiple property editors can be used (FormPropertyEditor)
     */
    public boolean useMultipleEditors();

    /**
     * Initializes property editor for a property - property editors are usually
     * constructed with no parameters, but often needs some context
     * (e.g. FormAwareEditor needs FormModel and FormProperty).
     */
    public void initPropertyEditor(PropertyEditor prEd, FormProperty property);

    /**
     * Provides the form the property belongs to. The context is needed for loading
     * classes of property editors (from the right classpath).
     * @return FormModel this property belong to
     */
    public FormModel getFormModel();

    /**
     * Returns relative path of the property within the form. Together with the
     * name of the property (which is not included) the path can be used to
     * identify the property. E.g. all bean properties of a component has the
     * component name as their context path. Nested properties also have the
     * parent property name in the path.
     * @return String relative path of the property within the form
     */
    public String getContextPath();

    /**
     * Returns the type of the object the property belongs to (which it is
     * a property of).
     * @return type of the parent object of the property
     */
    public Class getParentObjectType();

    /**
     * Implementation of FormPropertyContext for component properties.
     */
    public static class Component implements FormPropertyContext {
        private RADComponent component;

        public Component(RADComponent metacomp) {
            component = metacomp;
        }

        public boolean useMultipleEditors() {
            return true;
        }

        public void initPropertyEditor(PropertyEditor prEd, FormProperty property) {
            if (prEd instanceof FormAwareEditor)
                ((FormAwareEditor)prEd).setContext(getFormModel(), property);
        }

        public FormModel getFormModel() {
            return component.getFormModel();
        }

        public String getContextPath() {
            return component != getFormModel().getTopRADComponent() ?
                   component.getName() : ""; // NOI18N
        }

        public Class getParentObjectType() {
            return component.getBeanClass();
        }
    }

    /**
     * Implementation of FormPropertyContext for a property that is a
     * "sub-property" of another property (e.g. border support properties).
     */
    public static class SubProperty implements FormPropertyContext {
        private FormProperty parentProperty;

        public SubProperty(FormProperty parentProp) {
            this.parentProperty = parentProp;
        }

        public boolean useMultipleEditors() {
            return parentProperty.getPropertyContext().useMultipleEditors();
        }

        public void initPropertyEditor(PropertyEditor prEd, FormProperty property) {
            parentProperty.getPropertyContext().initPropertyEditor(prEd, property);
        }

        public FormModel getFormModel() {
            return parentProperty.getPropertyContext().getFormModel();
        }

        public String getContextPath() {
            String parentPath = parentProperty.getPropertyContext().getContextPath();
            return parentPath != null && !parentPath.equals("") ? // NOI18N
                   parentPath + "." + parentProperty.getName() : // NOI18N
                   parentProperty.getName();
        }

        public Class getParentObjectType() {
            return parentProperty.getValueType();
        }
    }

    /** "Empty" implementation of FormPropertyContext. */
    public static class EmptyImpl implements FormPropertyContext {

        public boolean useMultipleEditors() {
            return false;
        }

        public void initPropertyEditor(PropertyEditor prEd, FormProperty property) {
            if (prEd instanceof FormAwareEditor) {
                ((FormAwareEditor)prEd).setContext(getFormModel(), property);
            }
        }

        public FormModel getFormModel() {
            return null;
        }

        public String getContextPath() {
            return ""; // NOI18N
        }

        public Class getParentObjectType() {
            return Object.class;
        }

        // ------

        public static EmptyImpl getInstance() {
            if (theInstance == null)
                theInstance = new EmptyImpl();
            return theInstance;
        }

        static private EmptyImpl theInstance = null;
    }
}
