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

package com.sun.rave.designtime.impl;

import java.awt.Image;
import com.sun.rave.designtime.BeanCreateInfoSet;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Result;

/**
 * A basic implementation of BeanCreateInfoSet to use for convenience.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see BeanCreateInfoSet
 */
public class BasicBeanCreateInfoSet implements BeanCreateInfoSet {

    protected String[] classNames = null;
    protected String displayName;
    protected String description;
    protected Image smallIcon;
    protected Image largeIcon;

    public BasicBeanCreateInfoSet() {}

    public BasicBeanCreateInfoSet(String[] beanClassNames) {
        this.classNames = beanClassNames;
    }

    public BasicBeanCreateInfoSet(String[] beanClassNames, String displayName) {
        this.classNames = beanClassNames;
        this.displayName = displayName;
    }

    public BasicBeanCreateInfoSet(String[] beanClassNames, Image smallIcon) {
        this.classNames = beanClassNames;
        this.smallIcon = smallIcon;
    }

    public BasicBeanCreateInfoSet(String[] beanClassNames, String displayName, Image smallIcon) {
        this.classNames = beanClassNames;
        this.displayName = displayName;
        this.smallIcon = smallIcon;
    }

    public void setBeanClassNames(String[] beanClassNames) {
        this.classNames = beanClassNames;
    }

    public String[] getBeanClassNames() {
        return classNames;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : makeDisplayName();
    }

    protected String makeDisplayName() {
        if (classNames != null && classNames.length > 0) {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < classNames.length; i++) {
                buf.append(classNames[i]);
                if (i < classNames.length - 1) {
                    buf.append(","); // NOI18N
                }
            }
            return buf.toString();
        }
        return null;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setSmallIcon(Image smallIcon) {
        this.smallIcon = smallIcon;
    }

    public Image getSmallIcon() {
        return smallIcon;
    }

    public void setLargeIcon(Image largeIcon) {
        this.largeIcon = largeIcon;
    }

    public Image getLargeIcon() {
        return largeIcon;
    }

    public String getHelpKey() {
        return null;
    }

    public Result beansCreatedSetup(DesignBean[] beans) {
        return null;
    }
}
