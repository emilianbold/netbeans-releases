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

import javax.swing.Action;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * @author   Jan Jancura
 */
public class SessionsActionsProvider implements NodeActionsProvider,
Models.ActionPerformer {
    
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [0];
        if (node instanceof Session)
            return new Action [] {
                Models.createAction (
                    "Make Current", 
                    node, 
                    this,
                    DebuggerManager.getDebuggerManager ().getCurrentSession ()
                        != node
                ),
                Models.createAction ("Kill", node, this)
//                Models.createAction ("Properties", node, this, false)
            };
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof Session) {
            DebuggerManager.getDebuggerManager ().setCurrentSession (
                (Session) node
            );
            return;
        }
        throw new UnknownTypeException (node);
    }

    public void addTreeModelListener (TreeModelListener l) {
    }

    public void removeTreeModelListener (TreeModelListener l) {
    }
    
    public void perform (String action, Object node) {
        if ("Make Current".equals (action)) {
            DebuggerManager.getDebuggerManager ().setCurrentSession (
                (Session) node
            );
        } else
        if ("Kill".equals (action)) {
            ((Session) node).kill ();
        } else
        if ("Properties".equals (action)) {
        }
    }    

    
    // innerclasses ............................................................
    
//    private static class CustomizeAction extends AbstractAction {
//        
//        private Session s;
//        
//        
//        CustomizeAction (Session s) {
//            super ("Customize");
//            this.s = s;
//            setEnabled (false);
//        }
//        
//        public void actionPerformed (ActionEvent e) {
//        }
//    }
//    
//    private static class MakeCurrentAction extends AbstractAction {
//        
//        private Session s;
//        
//        
//        MakeCurrentAction (Session s) {
//            super ("Make Current");
//            this.s = s;
//            setEnabled (
//                DebuggerManager.getDebuggerManager ().getCurrentSession ()
//                != s
//            );
//        }
//        
//        public void actionPerformed (ActionEvent e) {
//            DebuggerManager.getDebuggerManager ().setCurrentSession (s);
//        }
//    }
//    
//    private static class KillAction extends AbstractAction {
//        
//        private Session s;
//        
//        
//        KillAction (Session s) {
//            super ("Kill");
//            this.s = s;
//        }
//        
//        public void actionPerformed (ActionEvent e) {
//            s.kill ();
//        }
//    }
}
