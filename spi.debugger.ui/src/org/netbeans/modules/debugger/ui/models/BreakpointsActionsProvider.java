/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.ui.models;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.debugger.ui.actions.AddBreakpointAction;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
public class BreakpointsActionsProvider implements NodeActionsProvider {
    
    
    private static final Action NEW_BREEAKPOINT_ACTION = new AbstractAction 
        (NbBundle.getBundle (BreakpointsActionsProvider.class).getString
            ("CTL_BreakpointAction_New_Label")) {
            public void actionPerformed (ActionEvent e) {
                new AddBreakpointAction ().actionPerformed (null);
            }
    };
    private static final Action ENABLE_ALL_ACTION = new AbstractAction 
        (NbBundle.getBundle (BreakpointsActionsProvider.class).getString
            ("CTL_BreakpointAction_EnableAll_Label")) {
            public void actionPerformed (ActionEvent e) {
                DebuggerManager dm = DebuggerManager.getDebuggerManager ();
                Breakpoint[] bs = dm.getBreakpoints ();
                int i, k = bs.length;
                for (i = 0; i < k; i++)
                    bs [i].enable ();
            }
    };
    private static final Action DISABLE_ALL_ACTION = new AbstractAction 
        (NbBundle.getBundle (BreakpointsActionsProvider.class).getString
            ("CTL_BreakpointAction_DisableAll_Label")) {
            public void actionPerformed (ActionEvent e) {
                DebuggerManager dm = DebuggerManager.getDebuggerManager ();
                Breakpoint[] bs = dm.getBreakpoints ();
                int i, k = bs.length;
                for (i = 0; i < k; i++)
                    bs [i].disable ();
            }
    };
    private static final Action DELETE_ALL_ACTION = new AbstractAction 
        (NbBundle.getBundle (BreakpointsActionsProvider.class).getString
            ("CTL_BreakpointAction_DeleteAll_Label")) {
            public void actionPerformed (ActionEvent e) {
                DebuggerManager dm = DebuggerManager.getDebuggerManager ();
                Breakpoint[] bs = dm.getBreakpoints ();
                int i, k = bs.length;
                for (i = 0; i < k; i++)
                    dm.removeBreakpoint (bs [i]);
            }
    };
    private static final Action ENABLE_ACTION = Models.createAction (
        NbBundle.getBundle (BreakpointsActionsProvider.class).getString
            ("CTL_BreakpointAction_Enable_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    ((Breakpoint) nodes [i]).enable ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    private static final Action DISABLE_ACTION = Models.createAction (
        NbBundle.getBundle (BreakpointsActionsProvider.class).getString
            ("CTL_BreakpointAction_Disable_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    ((Breakpoint) nodes [i]).disable ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    private static final Action DELETE_ACTION = Models.createAction (
        NbBundle.getBundle (BreakpointsActionsProvider.class).getString
            ("CTL_BreakpointAction_Delete_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                DebuggerManager dm = DebuggerManager.getDebuggerManager ();
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    dm.removeBreakpoint ((Breakpoint) nodes [i]);
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
    private static final Action SET_GROUP_NAME_ACTION = Models.createAction (
        NbBundle.getBundle (BreakpointsActionsProvider.class).getString
            ("CTL_BreakpointAction_SetGroupName_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                setGroupName (nodes);
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );
    private static final Action DELETE_ALL_ACTION_S = Models.createAction (
        NbBundle.getBundle (BreakpointsActionsProvider.class).getString
            ("CTL_BreakpointAction_DeleteAll_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                String groupName = (String) nodes [0];
                DebuggerManager dm = DebuggerManager.getDebuggerManager ();
                Breakpoint[] bs = dm.getBreakpoints ();
                int i, k = bs.length;
                for (i = 0; i < k; i++)
                    if (bs [i].getGroupName ().equals (groupName))
                        dm.removeBreakpoint (bs [i]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    private static final Action ENABLE_ALL_ACTION_S = Models.createAction (
        NbBundle.getBundle (BreakpointsActionsProvider.class).getString
            ("CTL_BreakpointAction_EnableAll_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                String groupName = (String) nodes [0];
                Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
                    getBreakpoints ();
                int i, k = bs.length;
                for (i = 0; i < k; i++)
                    if (bs [i].getGroupName ().equals (groupName))
                        bs [i].enable ();
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    private static final Action DISABLE_ALL_ACTION_S = Models.createAction (
        NbBundle.getBundle (BreakpointsActionsProvider.class).getString
            ("CTL_BreakpointAction_DisableAll_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                String groupName = (String) nodes [0];
                Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
                    getBreakpoints ();
                int i, k = bs.length;
                for (i = 0; i < k; i++)
                    if (bs [i].getGroupName ().equals (groupName))
                        bs [i].disable ();
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    
    
    //private Vector listeners = new Vector ();
    
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [] {
                NEW_BREEAKPOINT_ACTION,
                null,
                ENABLE_ALL_ACTION,
                DISABLE_ALL_ACTION,
                DELETE_ALL_ACTION,
                null
            };
        if (node instanceof String)
            return new Action [] {
                SET_GROUP_NAME_ACTION,
                null,
                ENABLE_ALL_ACTION_S,
                DISABLE_ALL_ACTION_S,
                DELETE_ALL_ACTION_S,
                null
            };
        if (node instanceof Breakpoint)
            if (((Breakpoint) node).isEnabled ())
                return new Action [] {
                    DISABLE_ACTION,
                    DELETE_ACTION,
                    SET_GROUP_NAME_ACTION,
                    null,
                    NEW_BREEAKPOINT_ACTION,
                    null,
                    ENABLE_ALL_ACTION,
                    DISABLE_ALL_ACTION,
                    DELETE_ALL_ACTION,
                    null
                };
            else
                return new Action [] {
                    ENABLE_ACTION,
                    DELETE_ACTION,
                    SET_GROUP_NAME_ACTION,
                    null,
                    NEW_BREEAKPOINT_ACTION,
                    null,
                    ENABLE_ALL_ACTION,
                    DISABLE_ALL_ACTION,
                    DELETE_ALL_ACTION,
                    null
                };
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof String) 
            return;
        if (node instanceof Breakpoint) 
            return;
        throw new UnknownTypeException (node);
    }

    public void addModelListener (ModelListener l) {
        //listeners.add (l);
    }

    public void removeModelListener (ModelListener l) {
        //listeners.remove (l);
    }
    
//    public void fireTreeChanged () {
//        Vector v = (Vector) listeners.clone ();
//        int i, k = v.size ();
//        for (i = 0; i < k; i++)
//            ((TreeModelListener) v.get (i)).treeChanged ();
//    }

    private static void setGroupName (Object[] nodes) {
        NotifyDescriptor.InputLine descriptor = new NotifyDescriptor.InputLine (
            NbBundle.getBundle (BreakpointsActionsProvider.class).getString
                ("CTL_BreakpointAction_GroupDialog_NameLabel"),
            NbBundle.getBundle (BreakpointsActionsProvider.class).getString
                ("CTL_BreakpointAction_GroupDialog_Title")
        );
        if (DialogDisplayer.getDefault ().notify (descriptor) == 
            NotifyDescriptor.OK_OPTION
        ) {
            if (nodes [0] instanceof String) {
                String oldName = (String) nodes [0];
                Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
                    getBreakpoints ();
                int j, jj = bs.length;
                for (j = 0; j < jj; j++)
                    if ( ((Breakpoint) bs [j]).getGroupName ().
                         equals (oldName)
                    )
                        ((Breakpoint) bs [j]).setGroupName (
                            descriptor.getInputText ()
                        );
                return;
            }
            int i, k = nodes.length;
            for (i = 0; i < k; i++)
                ((Breakpoint) nodes [i]).setGroupName (
                    descriptor.getInputText ()
                );
           // fireTreeChanged ();
        }
    }
}
