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

import javax.swing.Action;

import org.netbeans.api.debugger.jpda.*;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.ui.SourcePath;


/**
 * @author   Jan Jancura
 */
public class VariablesActionsProvider implements NodeActionsProvider {
    
    
    private final Action GO_TO_SOURCE_ACTION = Models.createAction (
        "Go to Source", 
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                goToSource ((Field) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
        
    private ContextProvider lookupProvider;

    
    public VariablesActionsProvider (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [0];
        if (node instanceof Field)
            return new Action [] {
                GO_TO_SOURCE_ACTION
            };
        if (node instanceof Variable)
            return new Action [] {
            };
        if (node.equals ("More")) // NOI18N
            return new Action [] {
            };
        if (node.equals ("NoInfo")) // NOI18N
            return new Action [] {
            };
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof Field) {
            goToSource ((Field) node);
            return;
        }
        if (node.equals ("More")) // NOI18N
            return;
        if (node.equals ("NoInfo")) // NOI18N
            return;
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
    }
    
    public void goToSource (Field variable) {
        SourcePath ectx = (SourcePath) lookupProvider.lookupFirst
            (null, SourcePath.class);
        ectx.showSource (variable);
    }

    private boolean isSourceAvailable (Field v) {
        SourcePath ectx = (SourcePath) lookupProvider.lookupFirst 
            (null, SourcePath.class);
        return ectx.sourceAvailable (v);
    }
}
