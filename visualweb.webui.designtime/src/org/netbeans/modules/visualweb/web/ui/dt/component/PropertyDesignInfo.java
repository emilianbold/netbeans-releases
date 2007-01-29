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
import com.sun.rave.web.ui.component.Property;
import com.sun.rave.web.ui.component.PropertySheetSection;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignUtil;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;

/** DesignInfo class for components that extend the {@link
 * org.netbeans.modules.visualweb.web.ui.dt.component.Propety} component.
 *
 * @author gjmurphy
 */
public class PropertyDesignInfo extends AbstractDesignInfo {

    public PropertyDesignInfo () {
        super(Property.class);
    }

    public Result beanCreatedSetup(DesignBean bean) {
        super.beanCreatedSetup(bean);
        DesignProperty textProperty = bean.getProperty("label"); //NOI18N
        String suffix = DesignUtil.getNumericalSuffix(bean.getInstanceName());
        textProperty.setValue(
                bean.getBeanInfo().getBeanDescriptor().getDisplayName() + " " + suffix);
        return Result.SUCCESS;
    }

    public boolean acceptParent(DesignBean parentBean, DesignBean childBean, Class childClass) {
        if (parentBean.getInstance().getClass().equals(PropertySheetSection.class))
            return true;
        return false;
    }

}
