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

package org.netbeans.modules.visualweb.faces.dt_1_1.component.html;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import org.netbeans.modules.visualweb.faces.dt_1_1.component.UISelectItemsDesignInfo;
import javax.faces.component.UISelectMany;
import javax.faces.component.UISelectOne;

/**
 * DesignInfo base class for HtmlSelect* DesignInfo classes.
 *
 * @author gjmurphy
 */
public abstract class HtmlSelectDesignInfoBase extends HtmlDesignInfoBase {

    public void propertyChanged(DesignProperty prop, Object oldValue) {
        super.propertyChanged(prop, oldValue);
        if ("value".equals(prop.getPropertyDescriptor().getName())) { //NOI18N
            DesignBean bean = prop.getDesignBean();
            Object underlyingInstance = bean.getInstance();
            if (underlyingInstance instanceof UISelectOne || underlyingInstance instanceof UISelectMany) {
                int kidCount = bean.getChildBeanCount();
                if (kidCount > 0) {
                    DesignBean siBean = bean.getChildBean(0);
                    UISelectItemsDesignInfo.maybeSetupConverter(siBean);
                }
            }
        }
    }

}
