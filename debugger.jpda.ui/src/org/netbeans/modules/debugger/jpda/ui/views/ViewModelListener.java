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

package org.netbeans.modules.debugger.jpda.ui.views;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import javax.swing.JComponent;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
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
        Models.setModelsToView (view, Models.EMPTY_MODEL);
    }

    public void propertyChange (PropertyChangeEvent e) {
        updateModel ();
    }
    
    private void updateModel () {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        DebuggerEngine e = dm.getCurrentEngine ();
        Set s = new HashSet ();
        if (e != null) {
            s.addAll (e.lookup (viewType, TreeModel.class));
            s.addAll (e.lookup (viewType, TreeModelFilter.class));
            s.addAll (e.lookup (viewType, TreeExpansionModel.class));
            s.addAll (e.lookup (viewType, NodeModel.class));
            s.addAll (e.lookup (viewType, NodeModelFilter.class));
            s.addAll (e.lookup (viewType, TableModel.class));
            s.addAll (e.lookup (viewType, TableModelFilter.class));
            s.addAll (e.lookup (viewType, NodeActionsProvider.class));
            s.addAll (e.lookup (viewType, NodeActionsProviderFilter.class));
            s.addAll (e.lookup (viewType, ColumnModel.class));
            s.addAll (e.lookup (viewType, Model.class));
        }
        s.addAll (dm.lookup (viewType, TreeModel.class));
        s.addAll (dm.lookup (viewType, TreeModelFilter.class));
        s.addAll (dm.lookup (viewType, TreeExpansionModel.class));
        s.addAll (dm.lookup (viewType, NodeModel.class));
        s.addAll (dm.lookup (viewType, NodeModelFilter.class));
        s.addAll (dm.lookup (viewType, TableModel.class));
        s.addAll (dm.lookup (viewType, TableModelFilter.class));
        s.addAll (dm.lookup (viewType, NodeActionsProvider.class));
        s.addAll (dm.lookup (viewType, NodeActionsProviderFilter.class));
        s.addAll (dm.lookup (viewType, ColumnModel.class));
        s.addAll (dm.lookup (viewType, Model.class));
        
        Models.setModelsToView (
            view, 
            Models.createCompoundModel (new ArrayList (s))
        );
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
