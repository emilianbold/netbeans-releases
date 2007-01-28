/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
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
