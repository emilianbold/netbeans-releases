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
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Open the Source editor for the selected DataObject.
 *
 * @author Jeri Lockhart
 */
public class PlanSourceViewOpenAction extends OpenAction {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;

    @Override
	public String getName() {
        return NbBundle.getMessage(PlanSourceViewOpenAction.class,
                "PlanSourceViewOpenAction_Name");
    }

    @Override
	protected void performAction(Node[] node) {
        if (node == null || node[0] == null) {
            return;
        }
        PlanDataObject dobj = node[0].getLookup().lookup(
                PlanDataObject.class);
        if (dobj != null) {
            ViewComponentCookie svc = dobj.getCookie(
                    ViewComponentCookie.class);
            if (svc != null) {
            	svc.view(ViewComponentCookie.View.SOURCE,
            			dobj.getPlanEditorSupport().getModel().getPlanComponent());
            	return;
            }
        }
        // default to open cookie
        OpenCookie oc = node[0].getCookie(OpenCookie.class);
        if (oc != null) {
            oc.open();
        }
    }
}
