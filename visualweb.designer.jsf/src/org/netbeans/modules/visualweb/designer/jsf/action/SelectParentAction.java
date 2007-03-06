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


package org.netbeans.modules.visualweb.designer.jsf.action;

import org.netbeans.modules.visualweb.api.designer.Designer;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.designer.jsf.JsfSupportUtilities;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.spi.designtime.idebridge.action.AbstractDesignBeanAction;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;

/**
 * Action selecting the parent bean.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old functionality implementation -> isEnabled, performAction impl)
 */
public class SelectParentAction  extends AbstractDesignBeanAction {

    /** Creates a new instance of SelectParentAction */
    public SelectParentAction() {
    }

    protected String getDisplayName(DesignBean[] designBeans) {
        return NbBundle.getMessage(SelectParentAction.class, "LBL_SelectParentAction");
    }

    protected String getIconBase(com.sun.rave.designtime.DesignBean[] designBeans) {
        return null;
    }

    protected boolean isEnabled(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return false;
        }

        DesignBean designBean = designBeans[0];
        return canSelectParent(designBean);
    }

    protected void performAction(DesignBean[] designBeans) {
        if (designBeans.length == 0) {
            return;
        }

        DesignBean designBean = designBeans[0];
        selectParent(designBean);
    }

    private static boolean canSelectParent(DesignBean designBean) {
        if (designBean == null) {
            return false;
        }
        
        DesignBean parent = designBean.getBeanParent();
        if (parent == null) {
            return false;
        }
        
        if (parent == parent.getDesignContext().getRootContainer()) {
            return false;
        }
        
        if (Util.isSpecialBean(parent)) {
            return false;
        }
        
        return true;
    }

    private static void selectParent(DesignBean designBean) {
        if (designBean == null) {
            return;
        }

        DesignBean parent = designBean.getBeanParent();
        if (parent == null) {
            return;
        }

        Element componentRootElement = JsfSupportUtilities.getComponentRootElementForDesignBean(parent);
        if (componentRootElement == null) {
            return;
        }
        
        Designer designer = JsfSupportUtilities.getDesignerForDesignContext(designBean.getDesignContext());
        designer.selectComponent(componentRootElement);
    }
}
