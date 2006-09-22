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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.actions;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.CodeCustomizer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

public class CustomCodeAction extends NodeAction {

    protected void performAction(Node[] activatedNodes) {
        RADComponent metacomp = getComponent(activatedNodes);
        if (metacomp != null)
            CodeCustomizer.show(metacomp);
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        return getComponent(activatedNodes) != null;
    }

    private static RADComponent getComponent(Node[] nodes) {
        if (nodes != null && nodes.length == 1) {
            RADComponentCookie radCookie = (RADComponentCookie)
                    nodes[0].getCookie(RADComponentCookie.class);
            if (radCookie != null) {
                RADComponent metacomp = radCookie.getRADComponent();
                if (metacomp != null && metacomp != metacomp.getFormModel().getTopRADComponent())
                    return metacomp;
            }
        }
        return null;
    }

    public String getName() {
        return org.openide.util.NbBundle.getMessage(CustomCodeAction.class, "ACT_CustomCode"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP; // "gui.custom_code" ?
    }
    
}
