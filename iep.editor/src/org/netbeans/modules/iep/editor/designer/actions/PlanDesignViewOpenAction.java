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

package org.netbeans.modules.iep.editor.designer.actions;

import org.netbeans.modules.iep.editor.PlanDataObject;
import org.netbeans.modules.iep.editor.PlanEditorSupport;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/**
 * Opens the PlanDataObject in the IEP editor design view.
 *
 * 
 */
public class PlanDesignViewOpenAction extends NodeAction{
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;

    @Override
    protected void performAction(Node[] node) {
        if (node == null || node[0] == null) {
            return;
        }
        PlanDataObject sdo = node[0].getLookup().lookup(
                PlanDataObject.class);
        if (sdo != null) {
            PlanEditorSupport wes = sdo.getPlanEditorSupport();
            ViewComponentCookie svc = sdo.getCookie(
                    ViewComponentCookie.class);
            if (svc != null) {
                IEPModel model= wes.getModel();
                PlanComponent plan = null;
                ViewComponentCookie.View view = ViewComponentCookie.View.DESIGN;
                if (model.getState() ==  Model.State.VALID) {
                    plan = model.getPlanComponent();
                }
                if (plan == null) {
                    view = ViewComponentCookie.View.SOURCE;
                }
                
                if (wes.getOpenedPanes() == null ||
                        wes.getOpenedPanes().length == 0) {
                    svc.view(view, plan);
                } else {
                    wes.open();
                }                
                return;
            }
        }
        // default to open cookie
        OpenCookie oc = node[0].getCookie(OpenCookie.class);
        if (oc != null) {
            oc.open();
        }
    }

    @Override
    protected boolean enable(Node[] node) {
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(PlanDesignViewOpenAction.class,
                "PlanDesignViewOpenAction_Name");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
