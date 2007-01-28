/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.impl;

import java.awt.Image;
import com.sun.rave.designtime.BeanCreateInfo;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Result;

/**
 * A basic implementation of BeanCreateInfo to use for convenience.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see BeanCreateInfo
 */
public class BasicBeanCreateInfo implements BeanCreateInfo {

    protected String beanClassName;
    protected String displayName;
    protected String description;
    protected Image smallIcon;
    protected Image largeIcon;

    public BasicBeanCreateInfo() {}

    public BasicBeanCreateInfo(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public BasicBeanCreateInfo(String beanClassName, String displayName) {
        this(beanClassName);
        this.displayName = displayName;
    }

    public BasicBeanCreateInfo(String beanClassName, String displayName, String description) {
        this(beanClassName, displayName);
        this.description = description;
    }

    public BasicBeanCreateInfo(String beanClassName, Image smallIcon) {
        this(beanClassName);
        this.smallIcon = smallIcon;
    }

    public BasicBeanCreateInfo(String beanClassName, String displayName, Image smallIcon) {
        this(beanClassName, displayName);
        this.smallIcon = smallIcon;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : beanClassName != null ? beanClassName : null;
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

    public Result beanCreatedSetup(DesignBean designBean) {
        return null;
    }

    public String getHelpKey() {
        return null;
    }
}
