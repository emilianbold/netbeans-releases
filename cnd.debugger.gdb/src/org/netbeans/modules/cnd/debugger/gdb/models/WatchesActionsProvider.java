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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.KeyStroke;
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
                        ((GdbWatchVariable) nodes[i]).remove();
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
            return;
        }
        throw new UnknownTypeException(node);
    }

    public void addModelListener(ModelListener l) {
    }

    public void removeModelListener(ModelListener l) {
    }

    private static void customize(GdbWatchVariable w) {
        WatchPanel wp = new WatchPanel(w.getExpression());
        JComponent panel = wp.getPanel();

        ResourceBundle bundle = NbBundle.getBundle(WatchesActionsProvider.class);
        DialogDescriptor dd = new org.openide.DialogDescriptor(
                panel, MessageFormat.format(bundle.getString("CTL_Edit_Watch_Dialog_Title"), // NOI18N
                new Object [] { w.getExpression() })
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
        w.setExpression(wp.getExpression());
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
        DebuggerManager.getDebuggerManager().createWatch(wp.getExpression());
    }
}
