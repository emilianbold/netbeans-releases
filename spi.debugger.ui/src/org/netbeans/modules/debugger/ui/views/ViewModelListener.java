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

package org.netbeans.modules.debugger.ui.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.JComponent;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.TreeModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * This delegating CompoundModelImpl loads all models from DebuggerManager.
 * getDefault ().getCurrentEngine ().lookup (viewType, ..) lookup.
 *
 * @author   Jan Jancura
 */
public class ViewModelListener extends DebuggerManagerAdapter {
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.models") != null;

    private String          viewType;
    private JComponent      view;
    
    
    ViewModelListener (
        String viewType,
        JComponent view
    ) {
        this.viewType = viewType;
        this.view = view;
        DebuggerManager.getDebuggerManager ().addDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
        updateModel ();
    }

    void destroy () {
        DebuggerManager.getDebuggerManager ().removeDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
        Models.setModelsToView (
            view, 
            Models.EMPTY_TREE_MODEL, 
            Models.EMPTY_NODE_MODEL, 
            Models.EMPTY_TABLE_MODEL, 
            Models.EMPTY_NODE_ACTIONS_PROVIDER, 
            Collections.EMPTY_LIST
        );
    }

    public void propertyChange (PropertyChangeEvent e) {
        updateModel ();
    }
    
    private void updateModel () {
        TreeModel tm = (TreeModel) loadModel (TreeModel.class);
        if (tm == null)
            tm = Models.EMPTY_TREE_MODEL;
        List l = loadModels (NodeModel.class);
        l.add (new EmptyModel ());
        
        if (verbose) {
            System.out.println ("");
            System.out.println (viewType + " models:");
        }
        
        Models.setModelsToView (
            view, 
            Models.createCompoundTreeModel (
                tm, 
                loadModels (TreeModelFilter.class)
            ),
            Models.createCompoundNodeModel (
                Models.createCompoundNodeModel (
                    l
                ),
                loadModels (NodeModelFilter.class)
            ),
            Models.createCompoundTableModel (
                Models.createCompoundTableModel (
                    loadModels (TableModel.class)
                ),
                loadModels (TableModelFilter.class)
            ),
            Models.createCompoundNodeActionsProvider (
                Models.createCompoundNodeActionsProvider (
                    loadModels (NodeActionsProvider.class)
                ),
                loadModels (NodeActionsProviderFilter.class)
            ),
            loadModels (ColumnModel.class)
        );
                    
    }
    
    private Object loadModel (Class modelType) {
        DebuggerEngine e = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        Object m = null;
        if (e != null)
            m = e.lookupFirst (viewType, modelType);
        if (m == null)
            m = DebuggerManager.getDebuggerManager ().
                lookupFirst (viewType, modelType);
        return m;
    }

    private List loadModels (Class modelType) {
        DebuggerEngine e = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        List l = new ArrayList ();
        if (e != null)
            l.addAll (e.lookup (viewType, modelType));
        l.addAll (DebuggerManager.getDebuggerManager ().
            lookup (viewType, modelType));
        return l;
    }

    
    // innerclasses ............................................................

    private static class EmptyModel implements NodeModel {
        
        public String getDisplayName (Object node) throws UnknownTypeException {
            if (node == TreeModel.ROOT) {
                return "Name";
            }
            throw new UnknownTypeException (node);
        }
        
        public String getIconBase (Object node) throws UnknownTypeException {
            if (node == TreeModel.ROOT) {
                return "org/netbeans/modules/debugger/resources/DebuggerTab";
            }
            throw new UnknownTypeException (node);
        }
        
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }
        
        public void addTreeModelListener (TreeModelListener l) {}
        public void removeTreeModelListener (TreeModelListener l) {}
    }
}
