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
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.ui.EngineContext;


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
                goToSource ((Variable) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
        
    private LookupProvider lookupProvider;

    
    public VariablesActionsProvider (LookupProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [0];
        if (node instanceof Variable)
            return new Action [] {
                GO_TO_SOURCE_ACTION
            };
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof Variable) {
            goToSource ((Variable) node);
            return;
        }
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addTreeModelListener (TreeModelListener l) {
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeTreeModelListener (TreeModelListener l) {
    }
    
    public void goToSource (Variable variable) {
        EngineContext ectx = (EngineContext) lookupProvider.lookupFirst
            (EngineContext.class);
        ectx.showSource (variable);
    }

    private boolean isSourceAvailable (Variable v) {
        EngineContext ectx = (EngineContext) lookupProvider.lookupFirst 
            (EngineContext.class);
        return ectx.sourceAvailable (v);
    }
}
