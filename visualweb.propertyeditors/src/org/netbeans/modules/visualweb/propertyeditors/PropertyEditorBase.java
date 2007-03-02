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
package org.netbeans.modules.visualweb.propertyeditors;

import java.beans.FeatureDescriptor;
import java.beans.PropertyEditorSupport;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;
import com.sun.rave.propertyeditors.util.JavaInitializer;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

/**
 * An abstract base class for property editors. Provides support for getting
 * and setting of {@link com.sun.rave.designtime.DesignProperty} information
 * about the property to be edited.
 *
 * @author gjmurphy
 */
public abstract class PropertyEditorBase extends PropertyEditorSupport implements
        PropertyEditor2, ExPropertyEditor {

    /**
     * A static {@link com.sun.rave.propertyeditors.util.Bundle} for use by
     * implementing classes in this package.
     */
    protected static final Bundle bundle = Bundle.getBundle(PropertyEditorBase.class);

    /**
     * The property's "unset" value. This value will be null until the editor's
     * DesignProperty has been set.
     */
    protected Object unsetValue;

    PropertyEnv propertyEnv;
    DesignProperty designProperty;

    /**
     * Set the design property for this editor.
     */
    public void setDesignProperty(DesignProperty designProperty) {
        this.designProperty = designProperty;
        unsetValue = designProperty.getUnsetValue();
    }

    /**
     * Get this editor's design property.
     */
    public DesignProperty getDesignProperty() {
        return this.designProperty;
    }

    /**
     * Returns <code>true</code> is this editor's value can be edited in-line.
     * If this method returns <code>false</code>, the implementing editor must
     * provide a custom pop-up editor.
     */
    public boolean isEditableAsText() {
        return true;
    }

    /**
     * Returns the unique property help id that maps to the help topic for this
     * property editor. By default, returns null. Extending classes that provide
     * help should override this method.
     */
    protected String getPropertyHelpId() {
        return null;
    }

    /**
     * Used by the NetBeans IDE to pass an object that represents the property's
     * environment, and that editors may use to send and receive notification of
     * editing state.
     */
    public final void attachEnv(PropertyEnv propertyEnv) {
        this.propertyEnv = propertyEnv;
        if (!this.isEditableAsText())
            propertyEnv.getFeatureDescriptor().setValue("canEditAsText", Boolean.FALSE);
        String propertyHelpId = this.getPropertyHelpId();
        if (propertyHelpId != null) {
            FeatureDescriptor descriptor = propertyEnv.getFeatureDescriptor();
            descriptor.setValue(ExPropertyEditor.PROPERTY_HELP_ID, propertyHelpId);
        }
    }

    PropertyEnv getEnv() {
        return this.propertyEnv;
    }

    /**
     * Returns a string that contains Java code for initializing the property
     * with this editor's current value. This method defers to {@link
     * com.sun.rave.propertyeditors.util.JavaInitializer#toJavaInitializationString(Object)}.
     */
    public String getJavaInitializationString() {
        return JavaInitializer.toJavaInitializationString(getValue());
    }

}
