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
