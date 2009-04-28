/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.DialogDisplayer;


/**
 * @author   Jan Jancura
 */
public class WatchesActionsProvider implements NodeActionsProvider { 
    
    private static final Action NEW_WATCH_ACTION = new AbstractAction(
            NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_AddNew")) {
        public void actionPerformed(ActionEvent e) {
            newWatch();
        }
    };
    private static final Action DELETE_ALL_ACTION = new AbstractAction(
            NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_DeleteAll")) {
        public void actionPerformed(ActionEvent e) {
            DebuggerManager.getDebuggerManager().removeAllWatches();
        }
    };
    private static final Action DELETE_ACTION = Models.createAction(
            NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_Delete"),
        new Models.ActionPerformer() {
            public boolean isEnabled(Object node) {
                return true;
            }
            public void perform(Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++) {
                    if (nodes[i] instanceof GdbWatchVariable) {
                        ((GdbWatchVariable) nodes[i]).getWatch().remove();
                    }
                }
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
        
    static { 
        DELETE_ACTION.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE")); // NOI18N
    };
    
    private static final Action CUSTOMIZE_ACTION = Models.createAction(
        NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_Customize"), // NOI18N
        new Models.ActionPerformer() {
            public boolean isEnabled(Object node) {
                return true;
            }
            public void perform(Object[] nodes) {
                customize((GdbWatchVariable) nodes[0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            return new Action[] {NEW_WATCH_ACTION, null, DELETE_ALL_ACTION};
        }
        if (node instanceof GdbWatchVariable) {
            return new Action[] {
                NEW_WATCH_ACTION,
                null,
                DELETE_ACTION,
                DELETE_ALL_ACTION,
                null,
                CUSTOMIZE_ACTION
            };
        }
        throw new UnknownTypeException(node);
    }
    
    public void performDefaultAction(Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            return;
        } else if (node instanceof GdbWatchVariable) {
            customize((GdbWatchVariable) node);
            return;
        }
        throw new UnknownTypeException(node);
    }

    public void addModelListener(ModelListener l) {
    }

    public void removeModelListener(ModelListener l) {
    }

    private static void customize(GdbWatchVariable w) {
        WatchPanel wp = new WatchPanel(w.getWatch().getExpression());
        JComponent panel = wp.getPanel();

        ResourceBundle bundle = NbBundle.getBundle(WatchesActionsProvider.class);
        DialogDescriptor dd = new org.openide.DialogDescriptor(
                panel, MessageFormat.format(bundle.getString("CTL_Edit_Watch_Dialog_Title"), // NOI18N
                new Object [] { w.getWatch().getExpression() })
        );
        dd.setHelpCtx (new HelpCtx("debug.customize.watch")); // NOI18N - FIXME (need help topic)
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        dialog.dispose();

        if (dd.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }
        if (panel.getClientProperty("WatchCanceled") != null) { //NOI18N
            return;
        }
        if (wp.getExpression() != null && wp.getExpression().trim().length() > 0) {
            w.getWatch().setExpression(wp.getExpression());
        }
    }

    private static void newWatch() {
        WatchPanel wp = new WatchPanel("");
        JComponent panel = wp.getPanel();

        ResourceBundle bundle = NbBundle.getBundle(WatchesActionsProvider.class);
        DialogDescriptor dd = new DialogDescriptor(
            panel, bundle.getString("CTL_New_Watch_Dialog_Title")); // NOI18N
        
        dd.setHelpCtx(new HelpCtx("debug.new.watch")); // NOI18N - FIXME (need help topic)
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        dialog.dispose();

        if (dd.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }
        if (panel.getClientProperty("WatchCanceled") != null) { //NOI18N
            return;
        }
        if (wp.getExpression() != null && wp.getExpression().trim().length() > 0) {
            DebuggerManager.getDebuggerManager().createWatch(wp.getExpression());
        }
    }
}
