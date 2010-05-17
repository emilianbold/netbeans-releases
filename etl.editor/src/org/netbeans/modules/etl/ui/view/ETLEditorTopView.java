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
package org.netbeans.modules.etl.ui.view;

import com.sun.etl.exception.BaseException;
import net.java.hulp.i18n.Logger;
import com.sun.etl.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.etl.codegen.ETLProcessFlowGeneratorFactory;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilder;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilderContext;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.view.graph.actions.CollabPropertiesAction;
import org.netbeans.modules.etl.ui.view.graph.actions.EditDbModelAction;
import org.netbeans.modules.etl.ui.view.graph.actions.JoinAction;
import org.netbeans.modules.etl.ui.view.graph.actions.RuntimeInputAction;
import org.netbeans.modules.etl.ui.view.graph.actions.RuntimeOutputAction;
import org.netbeans.modules.etl.ui.view.graph.actions.SelectTableAction;
import org.netbeans.modules.etl.ui.view.graph.actions.TestRunAction;
import org.netbeans.modules.etl.ui.view.graph.actions.ToggleOutputAction;
import org.netbeans.modules.etl.ui.view.graph.actions.ValidationAction;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.actions.AutoLayoutAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.CollapseAllAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.ExpandAllAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.ZoomAction;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.BasicTopView;
import org.netbeans.modules.etl.ui.view.graph.actions.RefreshMetadataAction;
import org.netbeans.modules.etl.ui.view.graph.actions.RemountCollaborationAction;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.graph.actions.ZoomInAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.ZoomOutAction;
import org.netbeans.modules.sql.framework.ui.output.SQLStatementPanel;
import org.openide.util.NbBundle;

/**
 * ETL Editor top view. This class just provides ETL specfic actions in toolbar and graph
 * right click.
 *
 * @author Ritesh Adval
 */
public class ETLEditorTopView extends BasicTopView {

    public static final String OPERATOR_FOLDER = "ETLOperators";
    private static transient final Logger mLogger = Logger.getLogger(ETLEditorTopView.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private ETLCollaborationTopPanel topPanel;
    //private UndoAction undoAction;
    //private RedoAction redoAction;
    /**
     * Creates a new instance of ETLEditorTopView.
     *
     * @param model CollabSQLUIModelImpl containing collab model info
     * @param etlTopComp ETLCollaborationTopPanel which will host this view
     */
    public ETLEditorTopView(CollabSQLUIModel model, ETLCollaborationTopPanel etlTopComp) {
        super(model);
        this.topPanel = etlTopComp;
    }

    /**
     * Creates a new instance of ETLEditorTopView.
     *
     * @param model CollabSQLUIModelImpl containing collab model info
     */
    public ETLEditorTopView(CollabSQLUIModel model) {
        super(model);
    }

    /**
     * Indicates whether this view is editable.
     *
     * @return boolean - true/false
     */
    @Override
    public boolean canEdit() {
        return topPanel.canEdit();
    }

    /**
     * Execute a command
     *
     * @param command - command
     * @param args - arguments
     */
    @Override
    public Object[] execute(String command, Object[] args) {
        if (command.equals(ICommand.ADD_RUNTIME_CMD)) {
            Integer tableType = (Integer) args[0];
            TablePanel tPanel = new TablePanel(tableType.intValue());
            tPanel.showTablePanel();
        } else {
            return super.execute(command, args);
        }
        return null;
    }

    /**
     * Gets List of Actions associated with the graph canvas.
     *
     * @return List of graph canvas actions
     */
    public List getGraphActions() {
        ArrayList actions = new ArrayList();
        //        // undo action are not static (because they are used in condition builder also) so
        //        // we create it using constructor 
        //          if ((undoAction == null) || (redoAction == null) ) {
        //            synchronized (this) {
        //                if ((undoAction == null) || (redoAction == null) ) {
        //                    undoAction = new UndoAction();
        //                    redoAction = new RedoAction();
        //                }
        //            }
        //        }
        //
        //        //FOR RIGHT CLICK......
        //        if ((undoAction == null) || (redoAction == null)) {
        //            synchronized (this) {
        //                if ((undoAction == null) || (redoAction == null) ) {
        //                    undoAction.setEnabled(true);
        //                    redoAction.setEnabled(true);
        //                }
        //            }
        //        }
        //        actions.add(undoAction);
        //        actions.add(redoAction);
        //        //null is used for seperator
        //        actions.add(null);

        actions.add(GraphAction.getAction(ExpandAllAction.class));
        actions.add(GraphAction.getAction(CollapseAllAction.class));
        actions.add(GraphAction.getAction(ToggleOutputAction.class));
        actions.add(GraphAction.getAction(RefreshMetadataAction.class));
        actions.add(GraphAction.getAction(RemountCollaborationAction.class));
        actions.add(GraphAction.getAction(SelectTableAction.class));

        //null is used for seperator
        actions.add(null);

        actions.add(GraphAction.getAction(JoinAction.class));
        actions.add(GraphAction.getAction(EditDbModelAction.class));
        actions.add(GraphAction.getAction(RuntimeInputAction.class));
        actions.add(GraphAction.getAction(RuntimeOutputAction.class));

        //null is used for seperator
        actions.add(null);

        actions.add(GraphAction.getAction(ZoomInAction.class));
        actions.add(GraphAction.getAction(ZoomOutAction.class));
        //actions.add(GraphAction.getAction(ZoomAction.class));
        // null is used for seperator
        actions.add(null);

        actions.add(GraphAction.getAction(AutoLayoutAction.class));
        actions.add(GraphAction.getAction(ValidationAction.class));
        actions.add(GraphAction.getAction(TestRunAction.class));
        // actions.add(GraphAction.getAction(PrintAction.class));
        //null is used for seperator
        actions.add(null);

        actions.add(GraphAction.getAction(CollabPropertiesAction.class));

        return actions;
    }

    /**
     * Gets name of operator folder.
     *
     * @return name of operator folder
     */
    public String getOperatorFolder() {
        return OPERATOR_FOLDER;
    }

    /**
     * Gets List of Actions associated with the editor toolbar.
     *
     * @return List of toolbar Actions
     */
    public List getToolBarActions() {
        ArrayList actions = new ArrayList();
        //        // undo action are not static (because they are used in condition builder also) so
        //        // we create it using constructor 
        //        if ((undoAction == null) || (redoAction == null) ) {
        //            synchronized (this) {
        //                if ((undoAction == null) || (redoAction == null) ) {
        //                    undoAction = new UndoAction();
        //                    redoAction = new RedoAction();
        //                }
        //            }
        //        }
        //         
        //        actions.add(undoAction);
        //        actions.add(redoAction);
        //        actions.add(null);

        actions.add(GraphAction.getAction(ExpandAllAction.class));
        actions.add(GraphAction.getAction(CollapseAllAction.class));
        actions.add(GraphAction.getAction(ToggleOutputAction.class));
        actions.add(GraphAction.getAction(RefreshMetadataAction.class));
        actions.add(GraphAction.getAction(RemountCollaborationAction.class));
        actions.add(GraphAction.getAction(SelectTableAction.class));

        // null is used for seperator
        actions.add(null);
        actions.add(GraphAction.getAction(JoinAction.class));
        actions.add(GraphAction.getAction(EditDbModelAction.class));
        actions.add(GraphAction.getAction(RuntimeInputAction.class));
        actions.add(GraphAction.getAction(RuntimeOutputAction.class));
        // null is used for seperator
        actions.add(null);

        actions.add(GraphAction.getAction(ZoomInAction.class));
        actions.add(GraphAction.getAction(ZoomOutAction.class));
        actions.add(GraphAction.getAction(ZoomAction.class));
        // null is used for seperator
        actions.add(null);
        actions.add(GraphAction.getAction(AutoLayoutAction.class));
        // actions.add(GraphAction.getAction(PrintAction.class));
        actions.add(GraphAction.getAction(ValidationAction.class));
        actions.add(GraphAction.getAction(TestRunAction.class));
        return actions;
    }

    /**
     * Generates and displays associated SQL statement for the given SQLObject.
     *
     * @param obj SQLObject whose SQL statement is to be displayed
     */
    @Override
    protected void showSql(SQLObject obj) {
        if (obj.getObjectType() == SQLConstants.TARGET_TABLE) {
            SQLStatementPanel statementPanel = super.getOrCreateSQLStatementPanel(obj);
            SQLStatementPanel.ShowSQLWorkerThread showSqlThread =  statementPanel 

                   
                       .new ShowSQLWorkerThread() {
                @Override
                public Object construct() {
                    TargetTable targetTable = null;
                    try {
                        startProgressBar();
                        if (sqlObjectLocalRef.getObjectType() == SQLConstants.TARGET_TABLE) {
                            targetTable = (TargetTable) sqlObjectLocalRef;
                            // Show SQL which will be executed during run time
                            SQLDefinition sqlDefn = sqlModel.getSQLDefinition();

                            ETLStrategyBuilderContext context = new ETLStrategyBuilderContext(sqlDefn, targetTable);
                            ETLStrategyBuilder tableScriptBuilder = ETLProcessFlowGeneratorFactory.getTargetTableScriptBuilder(context);

                            if (!hasValidationErrors()) {
                                this.sqlText = tableScriptBuilder.getScriptToDisplay(context);
                            }
                        }
                    } catch (BaseException be) {
                        mLogger.errorNoloc(mLoc.t("EDIT048: Failed to generate Core SQL{0}", ETLEditorTopView.class.getName()), be);
                        StringBuilder msg = new StringBuilder();
                        if (targetTable != null) {
                            msg.append(targetTable.getQualifiedName()).append(": ");
                        }

                        if (StringUtil.isNullString(be.getMessage())) {
                            msg.append("Unknown error occurred while generating SQL.");
                        } else {
                            msg.append(be.getMessage());
                        }
                        String nbBundle1 = mLoc.t("BUND368: Cannot evaluate SQL:{0}",msg);
                        this.sqlText =  nbBundle1.substring(15);
                    } catch (Exception exp) {
                        String nbBundle2 = mLoc.t("BUND368: Cannot evaluate SQL:{0}",exp.getMessage());
                        mLogger.errorNoloc(mLoc.t("EDIT048: Failed to generate Core SQL{0}", ETLEditorTopView.class.getName()), ex);
                        this.sqlText = nbBundle2.substring(15);
                    }
                    return "";
                }
            };
            showSqlThread.start();
            showSplitPaneView(statementPanel);

        } else {
            super.showSql(obj);
        }
    }
}
