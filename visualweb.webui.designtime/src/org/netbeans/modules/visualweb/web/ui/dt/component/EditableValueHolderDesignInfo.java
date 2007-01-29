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
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;

/**
 * DesignInfo for all input components (extensions of UIInput).
 *
 * @author gjmurphy
 */
public class EditableValueHolderDesignInfo extends AbstractDesignInfo {

    /** Creates a new instance of EditableValueHolderDesignInfo */
    public EditableValueHolderDesignInfo(Class clazz) {
        super(clazz);
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean,
            Class childClass) {
        return false;
    }

    public void propertyChanged(DesignProperty property, Object oldValue) {
        if (property.getPropertyDescriptor().getName().equals("required")) { //NOI18N
            DesignBean bean = property.getDesignBean();
            String id = bean.getInstanceName();
            DesignContext context = bean.getDesignContext();
            DesignBean[] designBeans = context.getBeans();
            for (int i = 0; i < designBeans.length; i++) {
                DesignProperty p = designBeans[i].getProperty("for"); //NOI18N
                if (p != null && id.equals(p.getValue())) {
                    p.setValue(id);
                }
            }
        }
        super.propertyChanged(property, oldValue);
    }

}
