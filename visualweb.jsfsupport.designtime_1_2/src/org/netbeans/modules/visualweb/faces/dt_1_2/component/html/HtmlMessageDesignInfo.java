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
package org.netbeans.modules.visualweb.faces.dt_1_2.component.html;

import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import javax.faces.component.UIComponent;
import com.sun.rave.designtime.*;
import javax.faces.component.html.HtmlMessage;

public class HtmlMessageDesignInfo extends HtmlDesignInfoBase {

    public Class getBeanClass() {
        return HtmlMessage.class;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        bean.getProperty("for").setValue(""); //NOI18N
        bean.getProperty("showDetail").setValue(Boolean.FALSE); //NOI18N
        bean.getProperty("showSummary").setValue(Boolean.TRUE); //NOI18N
        bean.getProperty("infoClass").setValue("infoMessage"); // NOI18N
        bean.getProperty("warnClass").setValue("warnMessage"); // NOI18N
        bean.getProperty("errorClass").setValue("errorMessage"); // NOI18N
        bean.getProperty("fatalClass").setValue("fatalMessage"); // NOI18N
        return Result.SUCCESS;
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        return UIComponent.class.isAssignableFrom(sourceClass);
    }

    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        if (sourceBean.getInstance() instanceof UIComponent &&
            sourceBean != targetBean) {
            DesignProperty prop = targetBean.getProperty("for"); // NOI18N
            if (prop != null) {
                prop.setValue(sourceBean.getInstanceName());
            }
        }
        return Result.SUCCESS;
    }
}
