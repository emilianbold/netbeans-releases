/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.ui.views;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.ui.debugging.DebuggingView;
import org.netbeans.spi.debugger.ContextProvider;
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
import org.netbeans.spi.viewmodel.TreeExpansionModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * This delegating CompoundModelImpl loads all models from DebuggerManager.
 * getDefault ().getCurrentEngine ().lookup (viewType, ..) lookup.
 *
 * <p>
 * This class is identical to org.netbeans.modules.debugger.ui.views.ViewModelListener.
 *
 * @author   Jan Jancura
 */
public class ViewModelListener extends DebuggerManagerAdapter {
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.models") != null;

    private String          viewType;
    private JComponent      view;
    
    
    public ViewModelListener (
        String viewType,
        JComponent view
    ) {
        this.viewType = viewType;
        this.view = view;
        setUp();
    }
    
    public void setUp() {
        DebuggerManager.getDebuggerManager ().addDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
        updateModel ();
    }

    public void destroy () {
        DebuggerManager.getDebuggerManager ().removeDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
        if (view instanceof DebuggingView) {
            ((DebuggingView) view).setRootContext(null, null);
        } else {
            Models.setModelsToView (
                view, 
                Models.EMPTY_MODEL
            );
        }
    }

    public void propertyChange (PropertyChangeEvent e) {
        updateModel ();
    }
    
    private synchronized void updateModel () {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        DebuggerEngine e = dm.getCurrentEngine ();
        
        List treeModels;
        List treeModelFilters;
        List treeExpansionModels;
        List treeExpansionModelFilters;
        List nodeModels;
        List nodeModelFilters;
        List tableModels;
        List tableModelFilters;
        List nodeActionsProviders;
        List nodeActionsProviderFilters;
        List columnModels;
        List mm;
        ContextProvider cp = e != null ? DebuggerManager.join(e, dm) : dm;
        treeModels =            cp.lookup (viewType, TreeModel.class);
        treeModelFilters =      cp.lookup (viewType, TreeModelFilter.class);
        treeExpansionModels =   cp.lookup (viewType, TreeExpansionModel.class);
        treeExpansionModelFilters = cp.lookup (viewType, TreeExpansionModelFilter.class);
        nodeModels =            cp.lookup (viewType, NodeModel.class);
        nodeModelFilters =      cp.lookup (viewType, NodeModelFilter.class);
        tableModels =           cp.lookup (viewType, TableModel.class);
        tableModelFilters =     cp.lookup (viewType, TableModelFilter.class);
        nodeActionsProviders =  cp.lookup (viewType, NodeActionsProvider.class);
        nodeActionsProviderFilters = cp.lookup (viewType, NodeActionsProviderFilter.class);
        columnModels =          cp.lookup (viewType, ColumnModel.class);
        mm =                    cp.lookup (viewType, Model.class);
        
        List models = new ArrayList(11);
        models.add(treeModels);
        models.add(treeModelFilters);
        models.add(treeExpansionModels);
        models.add(nodeModels);
        models.add(nodeModelFilters);
        models.add(tableModels);
        models.add(tableModelFilters);
        models.add(nodeActionsProviders);
        models.add(nodeActionsProviderFilters);
        models.add(columnModels);
        models.add(mm);
        models.add(treeExpansionModelFilters);
        
        if (view instanceof DebuggingView) {
            ((DebuggingView) view).setRootContext(
                    Models.createCompoundModel(models),
                    e);
        } else {
            Models.setModelsToView (
                view, 
                Models.createCompoundModel (models)
            );
        }
    }

    
    // innerclasses ............................................................

    private static class EmptyModel implements NodeModel {
        
        public String getDisplayName (Object node) throws UnknownTypeException {
            if (node == TreeModel.ROOT) {
                return "Name"; // TODO: Localized ???
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
