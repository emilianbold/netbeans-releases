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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.web.ui.component.Link;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;

/**
 * Design time for Link component.
 *
 * @author Edwin Goei
 */
public class LinkDesignInfo extends AbstractDesignInfo {

    public LinkDesignInfo() {
        super(Link.class);
    }

    /**
     * <p>
     * Called when a new <code>Link</code> is dropped.
     * </p>
     *
     * @param bean
     *            <code>DesignBean</code> for the newly created instance
     */
    public Result beanCreatedSetup(DesignBean bean) {
        Link alert = (Link) bean.getInstance();
        DesignProperty prop = bean.getProperty("url"); //NOI18N
        // TODO what should this value be.
        // To prevent NPE set it to something that works for now
        prop.setValue("/resources/stylesheet.css");
        return Result.SUCCESS;
    }

}
