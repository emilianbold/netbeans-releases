/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.edm.editor.graph;

import java.awt.Frame;
import java.util.List;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphController;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphView;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorManager;
import org.netbeans.modules.edm.editor.graph.jgo.IToolBar;
import org.netbeans.modules.edm.editor.graph.components.SQLToolBar;
import org.netbeans.modules.edm.editor.graph.components.OperatorPaletteDialog;
import org.netbeans.modules.edm.editor.graph.components.OperatorSelectionPanel;
import org.netbeans.modules.edm.editor.ui.model.SQLUIModel;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;


/**
 * Abstract factory for creating SQL graph view objects.
 *
 * @author Ritesh Adval
 * @author Jonathan Giron
 * @version $Revision$
 */
public abstract class AbstractSQLViewFactory implements IOperatorManager {

    private OperatorSelectionPanel panel;
    protected int toolbarType;

    /**
     * create a graph view
     *
     * @return graph view
     */
    public abstract IGraphView createGraphView();

    /**
     * create a tool bar
     *
     * @return tool bar
     */
    public abstract IToolBar createToolBar();

    /**
     * create a graph controller
     *
     * @return
     */
    public abstract IGraphController createGraphController();

    /**
     * get SQL model
     *
     * @return SQL model
     */
    public abstract SQLUIModel getSQLModel();


    /**
     * get graph view pop up actions
     *
     * @return actions
     */
    public abstract List getGraphActions();

    /**
     * return toolbar actions
     *
     * @return toolbar actions
     */
    public abstract List getToolBarActions();

    /**
     * call this to setup relation between graph view, graph view container, toolbar,
     * controller and model
     */

    IGraphView graphView = null;

     public void setUp() {
        IGraphController controller = createGraphController();
        SQLUIModel model = getSQLModel();

        //set up graph view
        graphView = createGraphView();
        if (graphView != null) {
            graphView.setGraphController(controller);
            graphView.setGraphModel(getSQLModel());

            graphView.setGraphActions(getGraphActions());
        }
        //set up controller
        if (controller != null) {
            controller.setDataModel(model);
            controller.setView(graphView);
        }
    }

    public void setToolBar() {
        setUp();
        //set up toolbar
        IToolBar toolBar = createToolBar();
        if (toolBar != null) {
            toolBar.setGraphView(graphView);
            toolBar.setActions(getToolBarActions());
            toolBar.initializeToolBar();
            //set toolbar on graph
            if (graphView != null) {
                graphView.setToolBar(toolBar);
            }
        }
    }

    public void setSQLToolBar() {
        setUp();
        //set up toolbar
        IToolBar toolBar = createToolBar();
        if (toolBar != null) {
            toolBar.setGraphView(graphView);
            toolBar.setActions(getToolBarActions());
            toolBar.initializeToolBar();
            SQLToolBar sqlToolBar = (SQLToolBar) toolBar;
            sqlToolBar.initializeSQLToolBar();
            //set toolbar on graph
            if (graphView != null) {
                graphView.setToolBar(toolBar);
                graphView.setToolBar(sqlToolBar);
            }
        }
    }


    /**
     * Show the operator palette dialog, initially displaying the category panel
     * associated with the given node.
     *
     * @param node operator node whose category panel will be initially displayed in the
     *        selection dialog.
     */
    public void show(Node node) {
        if (panel == null) {
            panel = new OperatorSelectionPanel(this.getGraphView(), getOperatorXmlInfoModel(), toolbarType);
        }

        panel.selectCategory(node.getDisplayName());

        Frame f = WindowManager.getDefault().getMainWindow();

        OperatorPaletteDialog dlg = new OperatorPaletteDialog(f, panel);
        dlg.pack();

        dlg.showDialog(f);
    }

    /**
     * Returns toolbar type.
     *
     * @return toolbar type
     */
    public int getToolbarType() {
        return this.toolbarType;
    }
}
