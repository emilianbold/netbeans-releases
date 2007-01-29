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
import com.sun.rave.web.ui.component.Button;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;

/**
 * <p>Design time behavior for a <code>Button</code> component.</p>
 * <ul>
 * <li>When dropped, set the <code>text</code> property to <code>Submit</code>
 * (localized).</li>
 * <li>If an image component is linked to this component, set our <code>imageURL</code>
 * property to the image's <code>url</code> property, and delete the image.</li>
 * </ul>
 */

public class ButtonDesignInfo extends AbstractDesignInfo {

    public ButtonDesignInfo() {
        super(Button.class);
    }

    public Result beanCreatedSetup(DesignBean bean) {
        super.beanCreatedSetup(bean);
        DesignProperty textProperty = bean.getProperty("text"); //NOI18N
        textProperty.setValue(
                bean.getBeanInfo().getBeanDescriptor().getDisplayName());
        return Result.SUCCESS;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return false;
    }

}
