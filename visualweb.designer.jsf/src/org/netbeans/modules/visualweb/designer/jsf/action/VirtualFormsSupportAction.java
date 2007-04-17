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


import java.awt.Component;
import javax.swing.Action;
import javax.swing.JToggleButton;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action switching on/off the virtual forms support.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old functionality implementation -> performAction impl)
 */
public class VirtualFormsSupportAction extends AbstractJsfFormAction {

    /** Creates a new instance of RefreshAction */
    public VirtualFormsSupportAction() {
    }

    protected String getDisplayName(JsfForm jsfForm) {
        return NbBundle.getMessage(VirtualFormsSupportAction.class, "LBL_VirtualFormsSupportAction");
    }

    protected String getIconBase(JsfForm jsfForm) {
        return "org/netbeans/modules/visualweb/designer/jsf/resources/virtualForms.png"; // NOI18N
    }

    protected boolean isEnabled(JsfForm jsfForm) {
        return jsfForm != null;
    }

    protected void performAction(JsfForm jsfForm) {
        if (jsfForm == null) {
            return;
        }

        jsfForm.setVirtualFormsSupportEnabled(!jsfForm.isVirtualFormsSupportEnabled());

//        // XXX FIXME Why is this here? Move it to more appropriate place.
////        webform.getPane().repaint();
//        DesignerPane designerPane = webForm.getPane();
//        // XXX #6486455 Possible NPE.
//        if (designerPane == null) {
////            webForm.getTopComponent().repaint();
//            webForm.tcRepaint();
//        } else {
//            designerPane.repaint();
//        }
    }

    /** Overriding to get the toggle button as the toolbar presenter. */
    protected Component getToolbarPresenter(Action contextAwareAction, Lookup.Result<Node> result) {
        JsfForm jsfForm = getJsfForm(result);
        boolean selected;
        if (jsfForm == null) {
            selected = false;
        } else {
            selected = jsfForm.isVirtualFormsSupportEnabled();
        }

        JToggleButton toggleButton = new JToggleButton();
        Actions.connect(toggleButton, contextAwareAction);
        toggleButton.setSelected(selected);

        return toggleButton;
    }
}
