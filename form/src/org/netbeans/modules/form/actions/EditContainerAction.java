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


/** Action that focuses selected container to be edited in FormDesigner.
 */
public class EditContainerAction extends NodeAction {

    private static EditFormAction editFormAction = (EditFormAction)
                       SharedClassObject.findObject(EditFormAction.class, true);

    private static String name;

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            RADComponentCookie radCookie = (RADComponentCookie)activatedNodes[0]
                                            .getCookie(RADComponentCookie.class);
            RADComponent metacomp = radCookie == null ? null :
                                      radCookie.getRADComponent();
            if (metacomp instanceof RADVisualContainer) {
                FormDesigner designer = FormEditor.getFormDesigner(metacomp.getFormModel());
                if (designer != null) {
                    designer.setTopDesignComponent((RADVisualComponent)metacomp, true);
                    designer.requestActive();
                }

                editFormAction.setEnabled(
                    metacomp.getFormModel().getTopRADComponent() != metacomp);
            }
        }
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            RADComponentCookie radCookie = (RADComponentCookie)activatedNodes[0]
                                            .getCookie(RADComponentCookie.class);
            RADComponent metacomp = radCookie == null ? null :
                                      radCookie.getRADComponent();
            return metacomp instanceof RADVisualContainer
                   && FormEditor.getFormDesigner(metacomp.getFormModel()) != null;
        }
        return false;
    }

    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(EditContainerAction.class)
                     .getString("ACT_EditContainer"); // NOI18N
        return name;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.containers.designing"); // NOI18N
    }

}
