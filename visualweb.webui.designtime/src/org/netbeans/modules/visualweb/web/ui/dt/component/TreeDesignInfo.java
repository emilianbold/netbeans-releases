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

import com.sun.data.provider.DataProvider;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.web.ui.component.Tree;
import com.sun.rave.web.ui.component.TreeNode;
import org.netbeans.modules.visualweb.web.ui.dt.AbstractDesignInfo;

/**
 * Design time behavior for a <code>Tree</code> component.
 *
 * @author gjmurphy
 * @author Edwin Goei
 */
public class TreeDesignInfo extends AbstractDesignInfo {

    public TreeDesignInfo() {
        super(Tree.class);
    }

    public Result beanCreatedSetup(DesignBean bean) {
        DesignProperty prop;

        DesignProperty textProperty = bean.getProperty("text"); //NOI18N
        textProperty.setValue(bean.getBeanInfo().getBeanDescriptor().getDisplayName());

        DesignContext context = bean.getDesignContext();
        if (context.canCreateBean(TreeNode.class.getName(), bean, null)) {
            DesignBean treeNodeBean = context.createBean(TreeNode.class.getName(), bean, null);
            // Use same DT property state as if user added TreeNode
            treeNodeBean.getDesignInfo().beanCreatedSetup(treeNodeBean);
        }

        return Result.SUCCESS;
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean,
            Class childClass) {
        if (TreeNode.class.equals(childClass))
            return true;
        return false;
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        if (DataProvider.class.isAssignableFrom(sourceClass)) {
            if (this.getDefaultBindingProperty(targetBean) != null)
                return true;
        }
        return false;
    }

}
