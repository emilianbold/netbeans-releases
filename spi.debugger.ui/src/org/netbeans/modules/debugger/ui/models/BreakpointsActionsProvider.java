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
import javax.swing.Action;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.debugger.ui.actions.AddBreakpointAction;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * @author   Jan Jancura
 */
public class BreakpointsActionsProvider implements NodeActionsProvider, 
Models.ActionPerformer {
    
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [] {
                Models.createAction ("New Breakpoint ...", null, this),
                null,
                Models.createAction ("Enable All", null, this),
                Models.createAction ("Disable All", null, this),
                Models.createAction ("Delete All", null, this),
                null
            };
        if (node instanceof Breakpoint)
            if (((Breakpoint) node).isEnabled ())
                return new Action [] {
                    Models.createAction ("Disable", (Breakpoint) node, this),
                    Models.createAction ("Delete", (Breakpoint) node, this),
                    null,
                    Models.createAction ("New Breakpoint ...", null, this),
                    null,
                    Models.createAction ("Enable All", null, this),
                    Models.createAction ("Disable All", null, this),
                    Models.createAction ("Delete All", null, this),
                    null
                };
            else
                return new Action [] {
                    Models.createAction ("Enable", (Breakpoint) node, this),
                    Models.createAction ("Delete", (Breakpoint) node, this),
                    null,
                    Models.createAction ("New Breakpoint ...", null, this),
                    null,
                    Models.createAction ("Enable All", null, this),
                    Models.createAction ("Disable All", null, this),
                    Models.createAction ("Delete All", null, this),
                    null
                };
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof Breakpoint) {
            return;
        }
        throw new UnknownTypeException (node);
    }

    public void addTreeModelListener (TreeModelListener l) {
    }

    public void removeTreeModelListener (TreeModelListener l) {
    }
    
    public void perform (String action, Object node) {
        if (action.equals ("New Breakpoint ...")) {
            new AddBreakpointAction ().actionPerformed (null);
        } else
        if (action.equals ("Enable All")) {
            DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            Breakpoint[] bs = dm.getBreakpoints ();
            int i, k = bs.length;
            for (i = 0; i < k; i++)
                bs [i].enable ();
        } else
        if (action.equals ("Disable All")) {
            DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            Breakpoint[] bs = dm.getBreakpoints ();
            int i, k = bs.length;
            for (i = 0; i < k; i++)
                bs [i].disable ();
        } else
        if (action.equals ("Delete All")) {
            DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            Breakpoint[] bs = dm.getBreakpoints ();
            int i, k = bs.length;
            for (i = 0; i < k; i++)
                dm.removeBreakpoint (bs [i]);
        } else
        if (action.equals ("Enable")) {
            ((Breakpoint) node).enable ();
        } else
        if (action.equals ("Disable")) {
            ((Breakpoint) node).disable ();
        } else
        if (action.equals ("Delete")) {
            DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            dm.removeBreakpoint ((Breakpoint) node);
        }
    }
}
