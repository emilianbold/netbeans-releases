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
package org.netbeans.modules.visualweb.web.ui.dt.component.vforms;

import java.awt.Color;
import java.util.Map;
import javax.faces.component.ActionSource;
import javax.faces.component.EditableValueHolder;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.ext.componentgroup.ColorWrapper;
import com.sun.rave.web.ui.component.Form;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class VirtualFormsHelper {

    public static DisplayAction getContextItem(DesignContext context) {
        DesignBean formBean = null;
        DesignBean rootBean = context.getRootContainer();
        if (rootBean.getInstance() instanceof Form) {   //just in case
            formBean = rootBean;
        }
        else {
            formBean = findFormBeanFromRoot(rootBean);
        }
        if (formBean != null) {
            return new VirtualFormsCustomizerAction(formBean);
        }
        return null;
    }

    public static DisplayAction getContextItem(DesignBean bean) {
        if (findFormBean(bean) == null) {
            return null;
        }
        if (bean.getInstance() instanceof EditableValueHolder ||
            bean.getInstance() instanceof ActionSource) {
            return new EditVirtualFormsCustomizerAction(bean);
        }
        return null;
    }

    public static DisplayAction getContextItem(DesignBean[] beans) {
        if (findFormBean(beans) == null) {
            return null;
        }
        for (int i = 0; beans != null && i < beans.length; i++) {
            if (beans[i].getInstance() instanceof EditableValueHolder ||
                beans[i].getInstance() instanceof ActionSource) {
                return new EditVirtualFormsCustomizerAction(beans);
            }
        }
        return null;
    }

    public static DesignBean findFormBean(DesignBean[] beans) {
        if (beans == null) {
            return null;
        }
        for (int i = 0; i < beans.length; i++) {
            DesignBean formBean = findFormBean(beans[i]);
            if (formBean != null) {
                return formBean;
            }
        }
        return null;
    }

    public static DesignBean findFormBean(DesignBean bean) {
        if (bean == null) {
            return null;
        }
        if (bean.getInstance() instanceof Form) {
            return bean;
        }
        return findFormBean(bean.getBeanParent());
    }

    private static DesignBean findFormBeanFromRoot(DesignBean parent) {
        if (parent == null) {
            return null;
        }
        DesignBean[] childBeans = parent.getChildBeans();
        for (int i = 0; childBeans != null && i < childBeans.length; i++) {
            DesignBean bean = childBeans[i];
            if (bean.getInstance() instanceof Form) {
                return bean;
            }
            DesignBean formBean = findFormBeanFromRoot(bean);
            if (formBean != null) {
                return formBean;
            }
        }
        return null;
    }

    public static String getNewVirtualFormName(List vformsList) {
        List nameList = new ArrayList();
        for (int i = 0; vformsList != null && i < vformsList.size(); i++) {
            nameList.add(((Form.VirtualFormDescriptor)vformsList.get(i)).getName());
        }
        
        String name = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("newVirtualForm"); // NOI18N
        
        for (int i = 1; i < 999; i++) {
            if (!nameList.contains(name + i)) {
                name = name + i;
                break;
            }
        }
        
        return name;
    }
}
