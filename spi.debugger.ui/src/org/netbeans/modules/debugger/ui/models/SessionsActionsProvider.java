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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
public class SessionsActionsProvider implements NodeActionsProvider {
    
    private static final Action FINISH_ALL_ACTION = new AbstractAction 
        (NbBundle.getBundle(SessionsActionsProvider.class).getString("CTL_SessionAction_FinishAll_Label")) {
            public void actionPerformed (ActionEvent e) {
                Session[] ss = DebuggerManager.getDebuggerManager ().
                    getSessions ();
                int i, k = ss.length;
                for (i = 0; i < k; i++)
                    ss [i].kill ();
            }
    };
    private Action MAKE_CURRENT_ACTION = Models.createAction (
        NbBundle.getBundle(SessionsActionsProvider.class).getString("CTL_SessionAction_MakeCurrent_Label"), new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return DebuggerManager.getDebuggerManager ().
                    getCurrentSession () != node;
            }
            
            public void perform (Object[] nodes) {
                DebuggerManager.getDebuggerManager ().setCurrentSession (
                    (Session) nodes [0]
                );
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    private static final Action FINISH_ACTION = Models.createAction (
        NbBundle.getBundle(SessionsActionsProvider.class).getString("CTL_SessionAction_Finish_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    ((Session) nodes [i]).kill ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [] {
                FINISH_ALL_ACTION
            };
        if (node instanceof Session)
            return new Action [] {
                MAKE_CURRENT_ACTION,
                FINISH_ACTION,
                null,
                FINISH_ALL_ACTION
            };
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof Session) {
            if (DebuggerManager.getDebuggerManager ().getCurrentSession () == 
                node
            ) return;
            DebuggerManager.getDebuggerManager ().setCurrentSession (
                (Session) node
            );
            return;
        }
        throw new UnknownTypeException (node);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }
}
