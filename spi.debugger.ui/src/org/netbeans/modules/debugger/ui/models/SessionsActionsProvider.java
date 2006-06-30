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
