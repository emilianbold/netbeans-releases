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
