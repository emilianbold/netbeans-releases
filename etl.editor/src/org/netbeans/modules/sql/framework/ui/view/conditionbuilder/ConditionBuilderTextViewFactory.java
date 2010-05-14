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
        // TODO: We need to restore operator category.
        super.setSQLToolBar();
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
