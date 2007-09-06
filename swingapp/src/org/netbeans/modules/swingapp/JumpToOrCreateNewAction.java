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

package org.netbeans.modules.swingapp;

import org.netbeans.modules.form.Event;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormProperty;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADComponentCookie;
import org.netbeans.modules.form.RADProperty;
import org.netbeans.modules.form.actions.PropertyAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 * Jumps to source of the action set to the currently selected component.
 * If no action is set (but the component has action property), the property
 * editor is opened. If no action is set, but there is already an event handler
 * assigned as ActionListener, then this action is disabled.
 */
public class JumpToOrCreateNewAction extends NodeAction {

    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
            RADComponent metacomp = (radCookie != null) ? radCookie.getRADComponent() : null;
            if (metacomp != null) {
                DataObject dobj = FormEditor.getFormDataObject(metacomp.getFormModel());
                if (dobj != null && AppFrameworkSupport.isFrameworkEnabledProject(dobj.getPrimaryFile())) {
                    FormProperty actionProp = metacomp.getBeanProperty("action"); // NOI18N
                    if (actionProp != null) {
                        try {
                            Object value = actionProp.getValue();
                            if (value instanceof ProxyAction) {
                                return true;
                            }
                            if (value != null) {
                                return false;
                            }
                        } catch (Exception ex) {}

                        // if no action set yet, check if there's no action event handler either
                        boolean actionEventAssigned = false;
                        for (Event e : metacomp.getKnownEvents()) {
                            if ("actionPerformed".equals(e.getListenerMethod().getName()) // NOI18N
                                    && e.getEventHandlers().length > 0) {
                                actionEventAssigned = true;
                                break;
                            }
                        }
                        return !actionEventAssigned;
                    }
                }
            }
        }
        return false;
    }

    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
            RADComponent metacomp = (radCookie != null) ? radCookie.getRADComponent() : null;
            if (metacomp != null) {
                DataObject dobj = FormEditor.getFormDataObject(metacomp.getFormModel());
                FileObject srcFile = (dobj != null) ? dobj.getPrimaryFile() : null;
                if (srcFile != null && AppFrameworkSupport.isFrameworkEnabledProject(srcFile)) {
                    RADProperty actionProp = metacomp.getBeanProperty("action"); // NOI18N
                    if (actionProp != null) {
                        ActionManager am = ActionManager.getActionManager(srcFile);
                        try {
                            Object value = actionProp.getValue();
                            if (value instanceof ProxyAction) {
                                am.jumpToActionSource(((ProxyAction)value));
                            } else if (value == null) { // invoke action property editor,
                                // let the user select or create a new action
                                new PropertyAction(actionProp).actionPerformed(null);
                                value = actionProp.getValue();
                                if (value instanceof ProxyAction) {
                                    am.jumpToActionSource(((ProxyAction)value));
                                }
                            }
                        } catch (Exception ex) {}
                    }
                }
            }
        }
    }

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
