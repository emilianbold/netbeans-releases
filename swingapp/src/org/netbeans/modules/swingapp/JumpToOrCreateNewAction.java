/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

    @Override
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
            RADComponent metacomp = (radCookie != null) ? radCookie.getRADComponent() : null;
            if (metacomp != null) {
                DataObject dobj = FormEditor.getFormDataObject(metacomp.getFormModel());
                if (dobj != null && ActionManager.canHaveActions(dobj.getPrimaryFile())) {
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

    @Override
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            RADComponentCookie radCookie = activatedNodes[0].getCookie(RADComponentCookie.class);
            RADComponent metacomp = (radCookie != null) ? radCookie.getRADComponent() : null;
            if (metacomp != null) {
                DataObject dobj = FormEditor.getFormDataObject(metacomp.getFormModel());
                FileObject srcFile = (dobj != null) ? dobj.getPrimaryFile() : null;
                if (srcFile != null && ActionManager.canHaveActions(srcFile)) {
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

    @Override
    protected boolean asynchronous() {
        return false;
    }
    @Override
    public String getName() {
        return ""; // NOI18N
    }
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
