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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.event.ActionEvent;
import java.util.Vector;
import javax.swing.Action;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * @author   Jan Jancura
 */
public class WatchesActionsProvider implements NodeActionsProvider,
Models.ActionPerformer {
    
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [0];
        if (node instanceof JPDAWatch)
            return new Action [] {
                Models.createAction ("Delete", node, this),
                Models.createAction ("Customize", node, this)
            };
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof JPDAWatch) {
            return;
        }
        throw new UnknownTypeException (node);
    }

    public void addTreeModelListener (TreeModelListener l) {
    }

    public void removeTreeModelListener (TreeModelListener l) {
    }
    
    public void perform (String action, Object node) {
        if ("Delete".equals (action)) {
            ((JPDAWatch) node).remove ();
        } else
        if ("Customize".equals (action)) {
        }
    }    

    
    // innerclasses ............................................................
    
//    private static class CustomizeAction extends AbstractAction {
//        
//        private JPDAWatch w;
//        
//        
//        CustomizeAction (JPDAWatch w) {
//            super ("Customize");
//            this.w = w;
//        }
//        
//        public void actionPerformed (ActionEvent e) {
//        }
//    }
//    
//    private static class DeleteAction extends AbstractAction {
//        
//        private JPDAWatch w;
//        
//        
//        DeleteAction (JPDAWatch w) {
//            super ("Delete");
//            this.w = w;
//        }
//        
//        public void actionPerformed (ActionEvent e) {
//            w.remove ();
//        }
//    }
}
