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

import org.openide.nodes.Node;
import org.openide.util.actions.*;
import org.openide.util.*;

import org.netbeans.modules.form.*;


/**
 * Makes the entire form selected in designer - if the current designed
 * container is a sub-container and also the current selected node.
 * This action is not presented visually anywhere, it is used as one of the
 * component default actions ensuring that when a designed container is double
 * clicked, the whole form is brought back to design.
 * 
 * @author Tomas Pavek
 */
public class EditFormAction extends NodeAction {

    protected boolean enable(Node[] nodes) {
        boolean ret = false;
        if (nodes != null && nodes.length == 1) {
            RADComponentCookie radCookie = nodes[0].getCookie(RADComponentCookie.class);
            RADComponent comp = (radCookie != null) ? radCookie.getRADComponent() : null;
            if (comp != null) {
                RADComponent topComp = comp.getFormModel().getTopRADComponent();
                if (comp != topComp && EditContainerAction.isEditableComponent(topComp)) {
                    FormDesigner designer = getDesigner(comp);
                    if (designer != null && comp == designer.getTopDesignComponent()) {
                        ret = true;
                    }
                }
            }
        }
        return ret;
    }

    static void reenable(Node[] nodes) {
        SystemAction.get(EditFormAction.class).reenable0(nodes);
    }

    private void reenable0(Node[] nodes) {
        setEnabled(enable(nodes));
    }

    protected void performAction(Node[] nodes) {
        if (nodes != null && nodes.length == 1) {
            RADComponentCookie radCookie = nodes[0].getCookie(RADComponentCookie.class);
            RADComponent comp = (radCookie != null) ? radCookie.getRADComponent() : null;
            if (comp != null) {
                RADComponent topComp = comp.getFormModel().getTopRADComponent();
                if (topComp != comp && EditContainerAction.isEditableComponent(topComp)) {
                    FormDesigner designer = getDesigner(topComp);
                    if (designer != null && topComp != designer.getTopDesignComponent()) {
                        designer.setTopDesignComponent((RADVisualComponent)topComp, true);
                        designer.requestActive();

                        // NodeAction is quite unreliable in enabling, do it ourselves for sure
                        Node[] n = new Node[] { topComp.getNodeReference() };
                        if (n[0] != null) {
                            EditContainerAction.reenable(n);
                            DesignParentAction.reenable(n);
                            EditFormAction.reenable(n);
                        }
                    }
                }
            }
        }
    }

    private static FormDesigner getDesigner(RADComponent comp) {
        return FormEditor.getFormDesigner(comp.getFormModel());
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return ""; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
