/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
        return new HelpCtx("gui.components.designing"); // NOI18N
    }

}
