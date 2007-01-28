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

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public abstract class HtmlBeanInfoBase extends SimpleBeanInfo {

    // The bean class
    protected Class beanClass;
    // The default property name
    protected String defaultPropertyName;
    // The 16x16 color icon
    protected String iconFileName_C16;
    // The 32x32 color icon
    protected String iconFileName_C32;
    // The 16x16 monochrome icon
    protected String iconFileName_M16;
    // The 32x32 monochrome icon
    protected String iconFileName_M32;

    private PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[0];

    public PropertyDescriptor[] getPropertyDescriptors() {
        return this.propertyDescriptors;
    }


    private EventSetDescriptor[] eventSetDescriptors = new EventSetDescriptor[0];

    public EventSetDescriptor[] getEventSetDescriptors() {
        return this.eventSetDescriptors;
    }


    private int defaultPropertyIndex = -2;

    public int getDefaultPropertyIndex() {
        if (defaultPropertyIndex == -2) {
            if (defaultPropertyName == null) {
                defaultPropertyIndex = -1;
            } else {
                PropertyDescriptor[] propertyDescriptors = this.getPropertyDescriptors();
                for (int i = 0; i < propertyDescriptors.length && defaultPropertyIndex == -2; i++) {
                    if (defaultPropertyName.equals(propertyDescriptors[i].getName()))
                        defaultPropertyIndex = i;
                }
            }
        }
        return defaultPropertyIndex;
    }

    public Image getIcon(int iconKind) {
        String name;
        switch (iconKind) {
            case ICON_COLOR_16x16:
                name = iconFileName_C16;
                break;
            case ICON_COLOR_32x32:
                name = iconFileName_C32;
                break;
            case ICON_MONO_16x16:
                name = iconFileName_M16;
                break;
            case ICON_MONO_32x32:
                name = iconFileName_M32;
                break;
            default:
                name = null;
        }
        if (name != null) {
            Image im = loadImage(name + ".png"); //NOI18N
            if (im == null) {
                im = loadImage(name + ".gif"); //NOI18N
            }
            return im;
        }
        return null;
    }

    /**
     * Returns the property descriptor for the property name specified, or null
     * if this bean has no such property.
     */
    protected PropertyDescriptor getPropertyDescriptor(String propName) {
        PropertyDescriptor[] propertyDescriptors = this.getPropertyDescriptors();
        if (propertyDescriptors != null) {
            for (int i = 0; i < propertyDescriptors.length; i++) {
                if (propertyDescriptors[i].getName().equals(propName)) {
                    return propertyDescriptors[i];
                }
            }
        }
        return null;
    }

    /**
     * Loads and returns a class instance corresponding to the fully-qualified
     * name specified, using the class loader used to load this class.
     */
    protected Class loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
