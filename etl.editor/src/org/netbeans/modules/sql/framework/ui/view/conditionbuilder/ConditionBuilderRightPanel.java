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
package org.netbeans.modules.sql.framework.ui.view.conditionbuilder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;

import java.util.logging.Level;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.utils.ConditionUtil;
import org.netbeans.modules.sql.framework.ui.SwingWorker;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.graph.view.impl.BasicToolBar;
import org.netbeans.modules.sql.framework.ui.model.ConditionBuilderSQLUiModel;
import org.netbeans.modules.sql.framework.ui.model.impl.ConditionBuilderSQLUIModelImpl;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.netbeans.modules.sql.framework.ui.output.SQLStatementPanel;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.actions.ValidateGraphAction;
import org.netbeans.modules.sql.framework.ui.view.validation.SQLValidationView;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.ui.output.SQLOutputConditionView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ConditionBuilderRightPanel extends JPanel implements IConditionGraphViewContainer {

    //private static transient final Logger mLogger = Logger.getLogger(ConditionBuilderRightPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ConditionBuilderRightPanel.class.getName());

    private class ValidationThread extends SwingWorker {

        private SQLCondition execModel;
        private List list;

        public ValidationThread(SQLCondition execModel) {
            this.execModel = execModel;
        }

        /**
         * Compute the value to be returned by the <code>get</code> method.
         * 
         * @return object
         */
        public Object construct() {
            list = execModel.validate();
            list = ConditionBuilderUtil.filterValidations(list);

            return "";
        }

        // Runs on the event-dispatching thread.
        public void finished() {
            if (execModel.getAllObjects().size() == 0) {
                String nbBundle1 = mLoc.t("BUND384: No expression to be validated in the Graph.");
                String msg = nbBundle1.substring(15);
                validationView.appendToView(msg);
            } else if (list.size() == 0) {
                String nbBundle2 = mLoc.t("BUND379: Condition is valid.");
                String msg = nbBundle2.substring(15);
                validationView.appendToView(msg);
            } else {
                validationView.setValidationInfos(list);
            }

            showSplitPaneView(validationView);
        }
    }
    private static final String LOG_CATEGORY = ConditionBuilderRightPanel.class.getName();
    private SQLCondition cond;
    private IGraphViewContainer editor;
    private ConditionGraphView graphView;
    private JSplitPane hSplitPane;
    private ConditionBuilderSQLUiModel model;
    private SQLOutputConditionView outputView;
    private JSplitPane splitPane;
    private HashMap sqlViewMap = new HashMap();
    private TableTreeView tableTreeView;
    private SQLValidationView validationView;
    private int toolbarType;

    /** Creates a new instance of ConditionBuilderRightPanel */
    public ConditionBuilderRightPanel(IGraphViewContainer editor, SQLCondition cond, List tables, int toolBarType) {
        this.editor = editor;
        this.cond = cond;
        this.toolbarType = toolBarType;

        initGui(cond, tables);
    }

    public void autoLayout() {
        graphView.autoLayout();
    }

    /**
     * Is editable
     * 
     * @return boolean - true/false
     */
    public boolean canEdit() {
        return editor.canEdit();
    }

    public void clearView() {
        this.graphView.clearAll();
        cond.removeAllObjects();
    }

    public void deleteNode(IGraphNode node) {
        graphView.deleteNode(node);
    }

    /**
     * validate the collaboration
     */
    public void doValidation() {
        try {
            if (model != null) {
                validationView.clearView();
                String nbBundle3 = mLoc.t("BUND385: Performing validation...");
                String msg = nbBundle3.substring(15);
                validationView.appendToView(msg);

                SQLCondition cond1 = model.getSQLCondition();
                ValidationThread vThread = new ValidationThread(cond1);
                vThread.start();
            }
        } catch (Exception ex) {
            String nbBundle4 = mLoc.t("BUND386: Error occurred during validation: {0}", ex.getMessage());
            String msg = nbBundle4.substring(15);
            logger.log(Level.SEVERE, mLoc.t("EDIT154: error doing condition validation{0}", LOG_CATEGORY)+ex.getMessage());
            validationView.appendToView(msg);
        }
    }

    /**
     * Execute a command
     * 
     * @param command - command
     * @param args - arguments
     */
    public Object[] execute(String command, Object[] args) {
        if (command.equals(ICommand.SHOW_SQL_CMD)) {
            showSql((SQLObject) args[0]);
        }
        return null;
    }

    public SQLObject getConditionRootPredicate() {
        return cond.getRootPredicate();
    }

    public IGraphView getGraphView() {
        return this.graphView;
    }

    public ConditionBuilderSQLUiModel getModel() {
        return this.model;
    }

    public String getOperatorFolder() {
        return editor.getOperatorFolder();
    }

    public boolean isDirty() {
        return model.isDirty();
    }

    public void refresh(SQLObject rootObj) {
        try {
            graphView.clearAll();
            SQLCondition cond1 = model.getSQLCondition();
            cond1.removeAllObjects();
            ConditionUtil.populateCondition(cond1, rootObj);
            model.restoreUIState();
            Runnable layout = new Runnable() {

                public void run() {
                    graphView.autoLayout();
                }
            };
            SwingUtilities.invokeLater(layout);
        } catch (Exception ex) {
            // Safely ignore this exception
            logger.log(Level.SEVERE, mLoc.t("EDIT513: Can't refresh condition graph view{0}", LOG_CATEGORY)+ex.getMessage());            
        }
    }

    public void setDirty(boolean d) {
        model.setDirty(d);
    }

    public void setModifiable(boolean edit) {
        IGraphView gView = graphView;
        gView.setModifiable(edit);
        GraphAction action = GraphAction.getAction(ValidateGraphAction.class);
        if (action != null) {
            action.setEnabled(edit);
        }
    }

    /**
     * Shows output view in bottom portion of a split pane.
     * 
     * @param c - component
     */
    public void showSplitPaneView(Component c) {
        splitPane.setBottomComponent(outputView);
        splitPane.setOneTouchExpandable(true);
        Dimension d = this.getSize();
        int divLocation = d.height * 3 / 5;
        splitPane.setDividerLocation(divLocation);
        outputView.addComponent(c);
    }

    /**
     * Shows the condition SQL
     */
    public void showSql() {
        SQLObject obj = cond.getRootPredicate();
        if (obj != null) {
            showSql(obj);
        } else {
            doValidation();
        }
    }

    /**
     * Generates and displays associated SQL statement for the given SQLObject.
     * 
     * @param obj SQLObject whose SQL statement is to be displayed
     */
    public void showSql(SQLObject obj) {
        SQLStatementPanel c = (SQLStatementPanel) sqlViewMap.get(obj.getId());
        if (c == null) {
            c = new SQLStatementPanel(this, obj);
            sqlViewMap.put(obj.getId(), c);
        } else {
            c.updateSQLObject(obj);
        }
        c.refreshSql();
        showSplitPaneView(c);
    }

    /**
     * Shows the SQL
     */
    public void showSQL() {
        if (splitPane.getBottomComponent() == null) {
            showSql();
        }
    }

    public void showTableTree() {
        if (hSplitPane.getLeftComponent() == null) {
            hSplitPane.setLeftComponent(tableTreeView);
            hSplitPane.setOneTouchExpandable(true);
        } else {
            hSplitPane.setOneTouchExpandable(false);
            hSplitPane.setLeftComponent(null);
        }
    }

    private void initGui(SQLCondition cond1, List tables) {
        this.setLayout(new BorderLayout());

        // Create table tree view
        tableTreeView = new TableTreeView(tables);
        tableTreeView.setMinimumSize(new Dimension(200, 100));

        // Create a horizontal split pane which has left and right side
        // Left side holds tree tabbed view
        hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        hSplitPane.setOneTouchExpandable(true);
        hSplitPane.setDividerLocation(200);

        // Let the tree tabbed pane as left component
        hSplitPane.setLeftComponent(tableTreeView);
        outputView = new SQLOutputConditionView(this);
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        model = new ConditionBuilderSQLUIModelImpl(cond);
        ConditionBuilderViewFactory viewFactory = new ConditionBuilderViewFactory(model, this, toolbarType);
        model.restoreUIState();
        graphView = (ConditionGraphView) viewFactory.getGraphView();

        splitPane.setTopComponent(graphView);

        hSplitPane.setRightComponent(splitPane);

        this.add(hSplitPane, BorderLayout.CENTER);

        validationView = new SQLValidationView(graphView);
        String nbBundle5 = mLoc.t("BUND383: Validation");
        String name = nbBundle5.substring(15);
        validationView.setName(name);

        BasicToolBar tlBar = (BasicToolBar) viewFactory.getOperatorView();
        this.add(tlBar, BorderLayout.NORTH);
    }
}
