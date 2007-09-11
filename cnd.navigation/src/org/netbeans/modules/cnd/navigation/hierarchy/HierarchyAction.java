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

package org.netbeans.modules.cnd.navigation.hierarchy;

import java.awt.event.ActionEvent;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

/**
 * Action which shows Hierarchy component.
 */
public class HierarchyAction extends CallableSystemAction {

    public HierarchyAction() {
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        performAction();
    }

    @Override
    protected String iconResource() {
        return HierarchyTopComponent.ICON_PATH;
    }

    public void performAction() {
        TopComponent win = HierarchyTopComponent.findInstance();
        win.open();
        win.requestActive();
    }

    public String getName() {
        return NbBundle.getMessage(HierarchyAction.class, "CTL_HierarchyAction"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isEnabled() {
        return CsmModelAccessor.getModel().projects().size()>0;
    }
    
    @Override
    protected boolean asynchronous () {
        return false;
    }
}