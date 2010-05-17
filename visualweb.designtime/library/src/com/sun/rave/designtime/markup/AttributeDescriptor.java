/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package com.sun.rave.designtime.markup;

import java.beans.FeatureDescriptor;

/**
 * <p>An AttributeDescriptor describes a markup attribute that is used to persist the value of a
 * JavaBean property.  The AttributeDescriptor is "stuffed" into the PropertyDescriptor using the
 * name-value pair storage: FeatureDescriptor.setValue(String key, Object value).  The key is
 * defined by Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, or literally "attributeDescriptor".
 * If an AttributeDescriptor is found in a property's PropertyDescriptor, the IDE will persist any
 * property settings in the .jsp file as an attribute.  If no AttributeDescriptor is found, any
 * property settings will be persisted in the .java file as standard JavaBeans properties.  For
 * example, a property called 'background' of type 'java.awt.Color' might be persisted in
 * two ways:</p>
 *
 * <p><ul><li>If it has an AttributeDescriptor, it will be persisted as a markup attribute, like
 * this: <code>background="#FFFFFF"</code>
 * <li>If there is no AttributeDescriptor, it will be persisted as a property setter method call,
 * like this: <code>setBackground(new java.awt.Color(255, 255, 255));</code>
 * </p>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see java.beans.FeatureDescriptor#setValue(String, Object)
 * @see com.sun.rave.designtime.Constants.PropertyDescriptor#ATTRIBUTE_DESCRIPTOR
 */
public class AttributeDescriptor extends FeatureDescriptor {

    /**
     * protected storage for the 'required' property
     */
    protected boolean required;

    /**
     * protected storage for the 'defaultValue' property
     */
    protected String defaultValue;

    /**
     * protected storage for the 'bindable' property
     */
    protected boolean bindable;

    /**
     * Constructs an empty AttributeDescriptor
     */
    public AttributeDescriptor() {}

    /**
     * Constructs an AttributeDescriptor with the specified attributeName
     *
     * @param attributeName The desired attribute name
     */
    public AttributeDescriptor(String attributeName) {
        setName(attributeName);
    }

    /**
     * Constructs an AttributeDescriptor with the specified attributeName, required state, and
     * default value
     *
     * @param attributeName The desired attribute name
     * @param required <code>true</code> if this is a required attribute, <code>false</code> if not
     * @param defaultValue The default value for this attribute (will not be persisted if the
     *        set value matches the default value)
     */
    public AttributeDescriptor(String attributeName, boolean required, String defaultValue) {
        setName(attributeName);
        this.required = required;
        this.defaultValue = defaultValue;
    }

    /**
     * Constructs an AttributeDescriptor with the specified attributeName, required state, default
     * value, and bindable state
     *
     * @param attributeName The desired attribute name
     * @param required <code>true</code> if this is a required attribute, <code>false</code> if not
     * @param defaultValue The default value for this attribute (will not be persisted if the
     *        set value matches the default value)
     * @param bindable <code>true</code> if this property is 'bindable', meaning it can have a value
     *        expression set on it, or <code>false</code> if not
     */
    public AttributeDescriptor(String attributeName, boolean required, String defaultValue,
        boolean bindable) {
        setName(attributeName);
        this.required = required;
        this.defaultValue = defaultValue;
        this.bindable = bindable;
    }

    /**
     * Sets the 'required' property.  A required attribute is one that must have a setting in order
     * to produce valid JSP.
     *
     * @param required <code>true</code> if this attribute should be required, <code>false</code> if
     * not
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Gets the 'required' property.  A required attribute is one that must have a setting in order
     * to produce valid JSP.
     *
     * @return the current state of the 'required' property
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets the 'defaultValue' property. The default value is the value that this property has if
     * it is not set at all - thus when the value is explicitly set to the default, the property
     * setting is removed from the persistence.
     *
     * @param defaultValue The default value for this attribute (will not be persisted if the set
     *        value matches the default value)
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Gets the 'defaultValue' property.  The default value is the value that this property has if
     * it is not set at all - thus when the value is explicitly set to the default, the property
     * setting is removed from the persistence.
     *
     * @return the 'defaultValue' property setting
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the 'bindable' property.  A 'bindable' property is one that may have a value binding
     * expression as its value.
     *
     * @param bindable <code>true</code> if this attribute should be bindable, <code>false</code> if
     *        not
     */
    public void setBindable(boolean bindable) {
        this.bindable = bindable;
    }

    /**
     * Gets the 'bindable' property.  A 'bindable' property is one that may have a value binding
     * expression as its value.
     *
     * @return <code>true</code> if this attribute should be bindable, <code>false</code> if not
     */
    public boolean isBindable() {
        return bindable;
    }

    public boolean equals(Object o) {
        if (o instanceof AttributeDescriptor) {
            AttributeDescriptor ad = (AttributeDescriptor)o;
            return ad == this ||
                ad.required == required &&
                ad.bindable == bindable &&
                (ad.defaultValue == null && defaultValue == null ||
                ad.defaultValue != null && ad.defaultValue.equals(defaultValue));
        }
        return false;
    }
}
