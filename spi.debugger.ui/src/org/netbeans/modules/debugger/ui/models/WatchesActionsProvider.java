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
import java.util.*;
import javax.swing.*;

import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.debugger.ui.actions.AddWatchAction;
import org.netbeans.modules.debugger.ui.WatchPanel;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;


/**
 * @author   Jan Jancura
 */
public class WatchesActionsProvider implements NodeActionsProvider, 
Models.ActionPerformer {
    
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [] {
                Models.createAction ("New Watch ...", null, this),
                Models.createAction ("Delete All", null, this)
            };
        if (node instanceof Watch)
            return new Action [] {
                Models.createAction ("Delete", (Watch) node, this),
                null,
                Models.createAction ("New Watch ...", null, this),
                Models.createAction ("Delete All", null, this),
                null,
                Models.createAction ("Customize", (Watch) node, this)
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

    public void addTreeModelListener (TreeModelListener l) {
    }

    public void removeTreeModelListener (TreeModelListener l) {
    }
    
    public void perform (String action, Object node) {
        if (action.equals ("Delete")) {
            ((Watch) node).remove ();
        } else
        if (action.equals ("Customize")) {
            customize ((Watch) node);
        } else
        if (action.equals ("Delete All")) {
            DebuggerManager.getDebuggerManager ().removeAllWatches ();
        } else
        if (action.equals ("New Watch ...")) {
            new AddWatchAction ().actionPerformed (null);
        }
    }

    private static void customize (Watch w) {

        WatchPanel wp = new WatchPanel(w.getExpression());
        JComponent panel = wp.getPanel();

        ResourceBundle bundle = NbBundle.getBundle(WatchesActionsProvider.class);
        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor(
            panel,
            bundle.getString ("CTL_WatchDialog_Title") // NOI18N
        );
        dd.setHelpCtx(new HelpCtx("debug.add.watch"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        dialog.dispose();

        if (dd.getValue() != org.openide.DialogDescriptor.OK_OPTION) return;
        w.setExpression(wp.getExpression());
    }
}
