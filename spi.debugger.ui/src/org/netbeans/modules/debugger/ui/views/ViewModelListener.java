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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.Action;
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
import org.netbeans.spi.viewmodel.ModelListener;
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
            Models.EMPTY_MODEL
        );
    }

    public void propertyChange (PropertyChangeEvent e) {
        updateModel ();
    }
    
    private void updateModel () {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        DebuggerEngine e = dm.getCurrentEngine ();
        List l = new ArrayList ();
        if (e != null) {
            l.addAll (e.lookup (viewType, TreeModel.class));
            l.addAll (e.lookup (viewType, TreeModelFilter.class));
            l.addAll (e.lookup (viewType, TreeExpansionModel.class));
            l.addAll (e.lookup (viewType, NodeModel.class));
            l.addAll (e.lookup (viewType, NodeModelFilter.class));
            l.addAll (e.lookup (viewType, TableModel.class));
            l.addAll (e.lookup (viewType, TableModelFilter.class));
            l.addAll (e.lookup (viewType, NodeActionsProvider.class));
            l.addAll (e.lookup (viewType, NodeActionsProviderFilter.class));
            l.addAll (e.lookup (viewType, ColumnModel.class));
            l.addAll (e.lookup (viewType, Model.class));
        }
        l.addAll (dm.lookup (viewType, TreeModel.class));
        l.addAll (dm.lookup (viewType, TreeModelFilter.class));
        l.addAll (dm.lookup (viewType, TreeExpansionModel.class));
        l.addAll (dm.lookup (viewType, NodeModel.class));
        l.addAll (dm.lookup (viewType, NodeModelFilter.class));
        l.addAll (dm.lookup (viewType, TableModel.class));
        l.addAll (dm.lookup (viewType, TableModelFilter.class));
        l.addAll (dm.lookup (viewType, NodeActionsProvider.class));
        l.addAll (dm.lookup (viewType, NodeActionsProviderFilter.class));
        l.addAll (dm.lookup (viewType, ColumnModel.class));
        l.addAll (dm.lookup (viewType, Model.class));
        
        Set s = new HashSet ();
        Iterator it = l.iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (s.contains (o))
                it.remove ();
            else
                s.add (o);
        }
        
        Models.setModelsToView (
            view, 
            Models.createCompoundModel (new ArrayList (l))
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
        
        public void addModelListener (ModelListener l) {}
        public void removeModelListener (ModelListener l) {}
    }
}
