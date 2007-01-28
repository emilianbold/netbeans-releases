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
package org.netbeans.modules.visualweb.faces.dt;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Locale;

public class PropertyDescriptorBase extends PropertyDescriptor {

    public PropertyDescriptorBase(String propertyName,
        Class beanClass) throws IntrospectionException {
        super(propertyName, beanClass);
    }

    public PropertyDescriptorBase(String propertyName, Class beanClass,
        String getterName, String setterName) throws IntrospectionException {
        super(propertyName, beanClass, getterName, setterName);
    }

    public PropertyDescriptorBase(String propertyName, Method getter,
        Method setter) throws IntrospectionException {
        super(propertyName, getter, setter);
    }

    public String getDisplayName() {
        String displayName =
            (String)getValue("displayName." + Locale.getDefault().getLanguage()); // NOI18N
        if (displayName != null) {
            return (displayName);
        } else {
            return super.getDisplayName();
        }
    }

    public String getShortDescription() {
        String shortDescription =
            (String)getValue("description." + Locale.getDefault().getLanguage()); // NOI18N
        if (shortDescription != null) {
            return (shortDescription);
        } else {
            return super.getShortDescription();
        }
    }

}
