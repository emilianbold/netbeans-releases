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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSEditorUtil;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.customizer.NbJSBreakpointCustomizer;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.ActionPerformer;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Provides actions for nodes representing {@link NbJSBreakpoint} in the
 * Breapoint view.
 */
public final class NbJSBreakpointNodeActions implements
        NodeActionsProviderFilter {

    private static final Action GO_TO_SOURCE_ACTION;
    private static final Action CUSTOMIZE_ACTION;

    static {

        GO_TO_SOURCE_ACTION = NbJSEditorUtil.createGoToAction();

        ActionPerformer customize = new ActionPerformer() {
            public boolean isEnabled(Object node) {
                return true;
            }

            public void perform(Object[] nodes) {
                Breakpoint bp = (Breakpoint) nodes[0];
                NbJSBreakpointCustomizer.customize(bp);
            }
        };
        CUSTOMIZE_ACTION = Models.createAction(NbBundle.getMessage(
                NbJSBreakpointNodeActions.class, "CTL_customize"), customize,
                Models.MULTISELECTION_TYPE_EXACTLY_ONE);
    }
   
    
    public void performDefaultAction(NodeActionsProvider original, Object node)
            throws UnknownTypeException {
        if (node instanceof NbJSBreakpoint) {
            NbJSBreakpoint bp = (NbJSBreakpoint) node;
            FileObject fo = bp.getFileObject();
            if (fo != null) {
                NbJSEditorUtil.showLine(NbJSEditorUtil.getLine(fo, bp.getLineNumber()), true);
                return;
            } else {
                DialogDescriptor dialogDescriptor = new DialogDescriptor(null, "Can't open without a Client-Side Debugger Session Running.");
                Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
                dialog.setVisible(true);
            }
        } else {
            original.performDefaultAction(node);
        }
    }

    public Action[] getActions(NodeActionsProvider original, Object node)
            throws UnknownTypeException {
        Action[] origActions = original.getActions(node);
        
        if (node instanceof NbJSBreakpoint && ((NbJSBreakpoint)node).getFileObject() != null) {
            Action[] actions = new Action[origActions.length + 3];
            actions[0] = GO_TO_SOURCE_ACTION;
            actions[1] = null;
            System.arraycopy(origActions, 0, actions, 2, origActions.length);
            actions[actions.length - 1] = CUSTOMIZE_ACTION;
            return actions;
        } else if ( node instanceof NbJSBreakpoint ){ /* In other words no file object was found */
            Action[] actions = new Action[origActions.length + 1];
            System.arraycopy(origActions, 0, actions, 0, origActions.length);
            actions[actions.length - 1] = CUSTOMIZE_ACTION;
            return actions;
        } else {
            return origActions;
        }
    }

}
