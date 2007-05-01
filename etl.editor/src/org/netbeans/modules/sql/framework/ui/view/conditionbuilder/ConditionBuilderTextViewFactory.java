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
package org.netbeans.modules.sql.framework.ui.view.conditionbuilder;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.sql.framework.ui.graph.IGraphController;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoModel;
import org.netbeans.modules.sql.framework.ui.graph.IToolBar;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.graph.impl.OperatorXmlInfoModel;
import org.netbeans.modules.sql.framework.ui.graph.view.impl.SQLToolBar;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.actions.ShowTableTreeAction;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.actions.ValidateSQLAction;
import org.netbeans.modules.sql.framework.ui.view.graph.AbstractSQLViewFactory;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ConditionBuilderTextViewFactory extends AbstractSQLViewFactory {
    private IGraphView gView;

    private IGraphViewContainer gViewContainer;
    private OperatorXmlInfoModel operatorModel;
    private IToolBar toolBar;

    public ConditionBuilderTextViewFactory(IGraphViewContainer gContainer, int toolBarType) {
        this.gViewContainer = gContainer;

        // Operator Model has to be created only once
        this.operatorModel = OperatorXmlInfoModel.getInstance(this.gViewContainer.getOperatorFolder());
        this.toolbarType = toolBarType;
        super.setUp();
    }

    /**
     * create a graph controller
     *
     * @return
     */
    public IGraphController createGraphController() {
        return null;
    }

    /**
     * create a graph view
     *
     * @return graph view
     */
    public IGraphView createGraphView() {
        return null;
    }

    /**
     * create a tool bar
     *
     * @return tool bar
     */
    public IToolBar createToolBar() {
        toolBar = new SQLToolBar(this);
        return toolBar;
    }

    /**
     * get graph view pop up actions
     *
     * @return actions
     */
    public List getGraphActions() {
        return null;
    }

    /**
     * Gets the current graph controller
     *
     * @return graph controller
     */
    public IGraphController getGraphController() {
        return null;
    }

    /**
     * Gets graph view currently associated with this manager.
     *
     * @return current instance of IGraphView
     */
    public IGraphView getGraphView() {
        return this.gView;
    }

    /**
     * get graph view container
     *
     * @return graph view container
     */
    public Object getGraphViewContainer() {
        return this.gViewContainer;
    }

    /**
     * Gets operator view (toolbar) currently associated with this manager.
     *
     * @return current instance of IToolBar
     */
    public IToolBar getOperatorView() {
        return toolBar;
    }

    /**
     * get operator xml info model which is defined in netbeans layer.xml file
     *
     * @return operator xml info model
     */
    public IOperatorXmlInfoModel getOperatorXmlInfoModel() {
        return operatorModel;
    }

    /**
     * get sql model
     *
     * @return sql model
     */
    public SQLUIModel getSQLModel() {
        return null;
    }

    /**
     * return toolbar actions
     *
     * @return toolbar actions
     */
    public List getToolBarActions() {
        ArrayList actions = new ArrayList();
        // Use seperate instance as ShowTableTreeAction is used in other toolbar also.
        actions.add(new ShowTableTreeAction());
        actions.add(GraphAction.getAction(ValidateSQLAction.class));

        return actions;
    }

}
