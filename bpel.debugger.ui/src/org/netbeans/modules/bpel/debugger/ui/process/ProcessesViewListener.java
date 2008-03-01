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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.process;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
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

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.11.30
 */
class ProcessesViewListener extends DebuggerManagerAdapter {
    
    ProcessesViewListener (String viewType, JComponent view) {
        myViewType = viewType;
        myView = view;
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
        Models.setModelsToView (myView, Models.EMPTY_MODEL);
    }

    public void propertyChange (PropertyChangeEvent e) {
        updateModel ();
    }
    
    private void updateModel () {
        DebuggerManager manager = DebuggerManager.getDebuggerManager ();
        DebuggerEngine engine = manager.getCurrentEngine ();
        List<List> models = new ArrayList<List>();
        
        if (engine == null) {
          models.add (manager.lookup (myViewType, TreeModel.class));
          models.add (manager.lookup (myViewType, TreeModelFilter.class));
          models.add (manager.lookup (myViewType, TreeExpansionModel.class));
          models.add (manager.lookup (myViewType, NodeModel.class));
          models.add (manager.lookup (myViewType, NodeModelFilter.class));
          models.add (manager.lookup (myViewType, TableModel.class));
          models.add (manager.lookup (myViewType, TableModelFilter.class));
          models.add (manager.lookup (myViewType, NodeActionsProvider.class));
          models.add (manager.lookup (myViewType, NodeActionsProviderFilter.class));
          models.add (manager.lookup (myViewType, ColumnModel.class));
          models.add (manager.lookup (myViewType, Model.class));
        }
        else {
          models.add (lookup (engine, manager, TreeModel.class));
          models.add (lookup (engine, manager, TreeModelFilter.class));
          models.add (lookup (engine, manager, TreeExpansionModel.class));
          models.add (lookup (engine, manager, NodeModel.class));
          models.add (lookup (engine, manager, NodeModelFilter.class));
          models.add (lookup (engine, manager, TableModel.class));
          models.add (lookup (engine, manager, TableModelFilter.class));
          models.add (lookup (engine, manager, NodeActionsProvider.class));
          models.add (lookup (engine, manager, NodeActionsProviderFilter.class));
          models.add (lookup (engine, manager, ColumnModel.class));
          models.add (lookup (engine, manager, Model.class));
        }
        Models.setModelsToView (myView, Models.createCompoundModel (models));
    }

    // XXX copy-paste programming!
    private <T> List<? extends T> lookup(
            final DebuggerEngine engine,
            final DebuggerManager manager,
            final Class<T> service) {
        final List<? extends T> engineService = engine.lookup(myViewType, service);
        final List<? extends T> managerService = manager.lookup(myViewType, service);
        final List<T> joined = new ArrayList<T>();
        
        add(joined, engineService);
        add(joined, managerService);
        
        return joined;
    }
    
    private <T> void add(
            final List<T> source, 
            final List<? extends T> collection) {
        for (T element : collection) {
          if (!source.contains(element)) {
              source.add(element);
          }
        }
    }

    private String myViewType;
    private JComponent myView;
}
