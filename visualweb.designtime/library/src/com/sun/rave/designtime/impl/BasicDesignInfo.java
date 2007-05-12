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

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;

/**
 * A basic implementation of DesignInfo to subclass and use for convenience.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see DesignInfo
 */
public class BasicDesignInfo implements DesignInfo {

    protected Class beanClass = null;

    public BasicDesignInfo(Class beanClass) {
        this.beanClass = beanClass;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return true;
    }

    public Result beanCreatedSetup(DesignBean designBean) {
        return null;
    }

    public Result beanPastedSetup(DesignBean designBean) {
        return null;
    }

    public Result beanDeletedCleanup(DesignBean designBean) {
        return null;
    }

    public DisplayAction[] getContextItems(DesignBean designBean) {
        return null;
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        return false;
    }

    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        return null;
    }

    public void beanContextActivated(DesignBean designBean) {}

    public void beanContextDeactivated(DesignBean designBean) {}

    public void instanceNameChanged(DesignBean designBean, String oldInstanceName) {}

    public void beanChanged(DesignBean designBean) {}

    public void propertyChanged(DesignProperty prop, Object oldValue) {}

    public void eventChanged(DesignEvent event) {}
}
