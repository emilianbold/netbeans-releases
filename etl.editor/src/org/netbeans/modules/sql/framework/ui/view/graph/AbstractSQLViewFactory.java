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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.Frame;
import java.util.List;

import org.netbeans.modules.sql.framework.ui.graph.IGraphController;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorManager;
import org.netbeans.modules.sql.framework.ui.graph.IToolBar;
import org.netbeans.modules.sql.framework.ui.graph.view.impl.OperatorPaletteDialog;
import org.netbeans.modules.sql.framework.ui.graph.view.impl.OperatorSelectionPanel;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
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
     * get graph view container
     * 
     * @return graph view container
     */
    public abstract Object getGraphViewContainer();

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
    public void setUp() {
        IGraphController controller = createGraphController();
        SQLUIModel model = getSQLModel();

        //set up graph view
        IGraphView graphView = createGraphView();
        if (graphView != null) {
            graphView.setGraphController(controller);
            graphView.setGraphModel(getSQLModel());

            graphView.setGraphViewContainer(getGraphViewContainer());
            graphView.setGraphActions(getGraphActions());
        }

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

        //set up controller
        if (controller != null) {
            controller.setDataModel(model);
            controller.setView(graphView);
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

