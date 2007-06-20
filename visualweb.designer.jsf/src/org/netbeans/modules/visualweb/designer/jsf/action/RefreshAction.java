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


import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.netbeans.modules.visualweb.insync.live.DesignBeanNode;
import org.openide.util.NbBundle;


/**
 * Action refreshing the page.
 * XXX This impl shows broken architecture, it shouldn't depend on insync internal impl at all,
 * or better, it shouldn't exist at all, the refresh should be always done automatically.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old functionality implementation -> performAction impl)
 */
public class RefreshAction  extends AbstractJsfFormAction {

    /** Creates a new instance of RefreshAction */
    public RefreshAction() {
        // XXX #94118 Avoiding the action from non page beans. 
        putValue(DesignBeanNode.ACTION_KEY_PAGE_BEAN_ONLY, Boolean.TRUE);
    }

    protected String getDisplayName(JsfForm jsfForm) {
        return NbBundle.getMessage(RefreshAction.class, "LBL_RefreshAction");
    }

    protected String getIconBase(JsfForm jsfForm) {
        return "org/netbeans/modules/visualweb/designer/jsf/resources/refresh.png"; // NOI18N
    }

    protected boolean isEnabled(JsfForm jsfForm) {
        return jsfForm != null;
    }

    protected void performAction(JsfForm jsfForm) {
        if (jsfForm == null) {
            return;
        }

        refresh(jsfForm);
    }


    private static void refresh(JsfForm jsfForm) {
//        webform.refresh(true);
        jsfForm.refreshModelWithExternals(true);
    }

}
