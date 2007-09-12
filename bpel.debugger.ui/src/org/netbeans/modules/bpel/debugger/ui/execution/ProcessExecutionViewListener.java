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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.bpel.debugger.ui.execution;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;

/**
 *
 * @author Alexander Zgursky
 */
public class ProcessExecutionViewListener extends DebuggerManagerAdapter {
    
    private String myViewType;
    private JComponent myView;
    
    ProcessExecutionViewListener(String viewType, JComponent view) {
        myViewType = viewType;
        myView = view;
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE, this);
        updateModel();
    }

    void destroy() {
        DebuggerManager.getDebuggerManager().removeDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE, this);
        Models.setModelsToView(myView, Models.EMPTY_MODEL);
    }

    public void propertyChange(PropertyChangeEvent e) {
        updateModel();
    }
    
    private void updateModel() {
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        DebuggerEngine engine = manager.getCurrentEngine();
        List<List> models = new ArrayList<List>();
        
        if (engine == null) {
            models.add(manager.lookup(myViewType, TreeModel.class));
            models.add(manager.lookup(myViewType, TreeModelFilter.class));
            models.add(manager.lookup(myViewType, TreeExpansionModel.class));
            models.add(manager.lookup(myViewType, NodeModel.class));
            models.add(manager.lookup(myViewType, NodeModelFilter.class));
            models.add(manager.lookup(myViewType, TableModel.class));
            models.add(manager.lookup(myViewType, TableModelFilter.class));
            models.add(manager.lookup(myViewType, NodeActionsProvider.class));
            models.add(manager.lookup(myViewType, NodeActionsProviderFilter.class));
            models.add(manager.lookup(myViewType, ColumnModel.class));
            models.add(manager.lookup(myViewType, Model.class));
        }
        else {
            models.add(lookup(engine, manager, TreeModel.class));
            models.add(lookup(engine, manager, TreeModelFilter.class));
            models.add(lookup(engine, manager, TreeExpansionModel.class));
            models.add(lookup(engine, manager, NodeModel.class));
            models.add(lookup(engine, manager, NodeModelFilter.class));
            models.add(lookup(engine, manager, TableModel.class));
            models.add(lookup(engine, manager, TableModelFilter.class));
            models.add(lookup(engine, manager, NodeActionsProvider.class));
            models.add(lookup(engine, manager, NodeActionsProviderFilter.class));
            models.add(lookup(engine, manager, ColumnModel.class));
            models.add(lookup(engine, manager, Model.class));
        }
        Models.setModelsToView(myView, Models.createCompoundModel(models));
    }

    private List lookup(
            DebuggerEngine engine,
            DebuggerManager manager,
            Class service)
    {
        List engineService = engine.lookup(myViewType, service);
        List managerService = manager.lookup(myViewType, service);
        List<Object> joined = new ArrayList<Object>();

        add(joined, engineService);
        add(joined, managerService);

        return joined;
    }

    private void add(List<Object> source, List collection) {
      Object[] elements = collection.toArray();

      for (Object element : elements) {
          if ( !source.contains(element)) {
              source.add(element);
          }
      }
    }
}
