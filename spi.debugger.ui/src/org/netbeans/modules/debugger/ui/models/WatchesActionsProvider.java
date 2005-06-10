/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.ui.models;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.KeyStroke;

import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.debugger.ui.actions.AddWatchAction;
import org.netbeans.modules.debugger.ui.WatchPanel;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;


/**
 * @author   Jan Jancura
 */
public class WatchesActionsProvider implements NodeActionsProvider {
    
    private static final Action NEW_WATCH_ACTION = new AbstractAction
        (NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_AddNew")) {
            public void actionPerformed (ActionEvent e) {
                ((AddWatchAction) AddWatchAction.findObject(AddWatchAction.class, true)).actionPerformed(null);
            }
    };
    private static final Action DELETE_ALL_ACTION = new AbstractAction 
        (NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_DeleteAll")) {
            public void actionPerformed (ActionEvent e) {
                DebuggerManager.getDebuggerManager ().removeAllWatches ();
            }
    };
    private static final Action DELETE_ACTION = Models.createAction (
        NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_Delete"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    ((Watch) nodes [i]).remove ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    static { 
        DELETE_ACTION.putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE")
        );
    };
    private static final Action CUSTOMIZE_ACTION = Models.createAction (
        NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_Customize"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                customize ((Watch) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [] {
                NEW_WATCH_ACTION,
                null,
                DELETE_ALL_ACTION
            };
        if (node instanceof Watch)
            return new Action [] {
                NEW_WATCH_ACTION,
                null,
                DELETE_ACTION,
                DELETE_ALL_ACTION,
                null,
                CUSTOMIZE_ACTION
            };
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof Watch) {
            customize ((Watch) node);
            return;
        }
        throw new UnknownTypeException (node);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }

    private static void customize (Watch w) {

        WatchPanel wp = new WatchPanel(w.getExpression());
        JComponent panel = wp.getPanel();

        ResourceBundle bundle = NbBundle.getBundle(WatchesActionsProvider.class);
        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor(
            panel,
            java.text.MessageFormat.format(bundle.getString("CTL_WatchDialog_Title"), // NOI18N 
                                           new Object [] { w.getExpression() })
        );
        dd.setHelpCtx(new HelpCtx("debug.add.watch"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        dialog.dispose();

        if (dd.getValue() != org.openide.DialogDescriptor.OK_OPTION) return;
        w.setExpression(wp.getExpression());
    }
}
