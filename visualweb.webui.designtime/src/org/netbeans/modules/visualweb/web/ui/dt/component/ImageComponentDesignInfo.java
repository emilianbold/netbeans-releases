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
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;
import com.sun.rave.web.ui.component.ImageComponent;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;
import org.netbeans.modules.visualweb.web.ui.dt.component.customizers.ImageCustomizerAction;

/**
 * Design time behavior for an <code>Image</code> component.
 *
 * @author gjmurphy
 */
public class ImageComponentDesignInfo extends AbstractDesignInfo {

    public ImageComponentDesignInfo() {
        super(ImageComponent.class);
    }

    public Result beanCreatedSetup(DesignBean bean) {
        ImageComponent image = (ImageComponent)bean.getInstance();
        return Result.SUCCESS;
    }

    public DisplayAction[] getContextItems(DesignBean bean) {
        DisplayAction[] superActions = super.getContextItems(bean);
        if (superActions == null)
            return new DisplayAction[] {new ImageCustomizerAction(bean)};
        DisplayAction[] actions = new DisplayAction[superActions.length + 1];
        int i = 0;
        while (i < superActions.length) {
            actions[i] = superActions[i];
            i++;
        }
        actions[i] = new ImageCustomizerAction(bean);
        return actions;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        return false;
    }

    protected DesignProperty getDefaultBindingProperty(DesignBean targetBean) {
        return targetBean.getProperty("url"); //NOI18N
    }

}
