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
package org.netbeans.modules.etl.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.openide.util.NbBundle;

import org.netbeans.modules.etl.codegen.ETLProcessFlowGeneratorFactory;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilder;
import org.netbeans.modules.etl.codegen.ETLStrategyBuilderContext;
import org.netbeans.modules.etl.ui.DataObjectHelper;
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
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.actions.AutoLayoutAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.CollapseAllAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.ExpandAllAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.RedoAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.UndoAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.ZoomAction;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.BasicTopView;
import org.netbeans.modules.sql.framework.ui.view.SQLStatementPanel;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Logger;
import com.sun.sql.framework.utils.StringUtil;
import org.netbeans.modules.sql.framework.ui.graph.actions.FitToHeightAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.FitToPageAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.FitToWidthAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.ZoomInAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.ZoomOutAction;
/**
 * ETL Editor top view. This class just provides ETL specfic actions in toolbar and graph
 * right click.
 *
 * @author Ritesh Adval
 */
public class ETLEditorTopView extends BasicTopView {
    public static final String OPERATOR_FOLDER = "ETLOperators";
    
    private ETLCollaborationTopComponent etlView;
    private UndoAction undoAction;
    private RedoAction redoAction;
    
    /**
     * Creates a new instance of ETLEditorTopView.
     *
     * @param model CollabSQLUIModelImpl containing collab model info
     * @param etlTopComp ETLCollaborationTopComponent which will host this view
     */
    public ETLEditorTopView(CollabSQLUIModel model, ETLCollaborationTopComponent etlTopComp) {
        super(DataObjectHelper.getPropertyViewManager(), model);
        this.etlView = etlTopComp;
    }

    /**
     * Creates a new instance of ETLEditorTopView.
     *
     * @param model CollabSQLUIModelImpl containing collab model info
     */    
    public ETLEditorTopView(CollabSQLUIModel model) {
        super(DataObjectHelper.getPropertyViewManager(), model);
    }
    
    /**
     * Indicates whether this view is editable.
     *
     * @return boolean - true/false
     */
    public boolean canEdit() {
        return etlView.canEdit();
    }
    
    /**
     * Execute a command
     *
     * @param command - command
     * @param args - arguments
     */
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
        //null is used for seperator
//        actions.add(null);
        
        actions.add(GraphAction.getAction(ExpandAllAction.class));
        actions.add(GraphAction.getAction(CollapseAllAction.class));
        actions.add(GraphAction.getAction(ToggleOutputAction.class));
        
        actions.add(GraphAction.getAction(SelectTableAction.class));
        
        //null is used for seperator
        actions.add(null);
        
        actions.add(GraphAction.getAction(JoinAction.class));
        actions.add(GraphAction.getAction(EditDbModelAction.class));
        actions.add(GraphAction.getAction(RuntimeInputAction.class));
        actions.add(GraphAction.getAction(RuntimeOutputAction.class));
       
        //null is used for seperator
        actions.add(null);
       
     /*   actions.add(GraphAction.getAction(FitToHeightAction.class));
        actions.add(GraphAction.getAction(FitToPageAction.class));
        actions.add(GraphAction.getAction(FitToWidthAction.class));
        // null is used for seperator
        actions.add(null);*/
        
        actions.add(GraphAction.getAction(ZoomInAction.class));
        actions.add(GraphAction.getAction(ZoomOutAction.class));
        actions.add(GraphAction.getAction(ZoomAction.class));
        // null is used for seperator
        actions.add(null);
    
        actions.add(GraphAction.getAction(AutoLayoutAction.class));
        actions.add(GraphAction.getAction(ValidationAction.class));
        actions.add(GraphAction.getAction(TestRunAction.class));
//        actions.add(GraphAction.getAction(PrintAction.class));
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
        actions.add(GraphAction.getAction(SelectTableAction.class));
      
        // null is used for seperator
        actions.add(null);
        actions.add(GraphAction.getAction(JoinAction.class));
        actions.add(GraphAction.getAction(EditDbModelAction.class));
        actions.add(GraphAction.getAction(RuntimeInputAction.class));
        actions.add(GraphAction.getAction(RuntimeOutputAction.class));
        // null is used for seperator
        actions.add(null);
       
     /*   actions.add(GraphAction.getAction(FitToHeightAction.class));
        actions.add(GraphAction.getAction(FitToPageAction.class));
        actions.add(GraphAction.getAction(FitToWidthAction.class));
        // null is used for seperator
        actions.add(null);*/
       
        actions.add(GraphAction.getAction(ZoomInAction.class));
        actions.add(GraphAction.getAction(ZoomOutAction.class));
        actions.add(GraphAction.getAction(ZoomAction.class));
        // null is used for seperator
        actions.add(null);
        actions.add(GraphAction.getAction(AutoLayoutAction.class));
//        actions.add(GraphAction.getAction(PrintAction.class));
        actions.add(GraphAction.getAction(ValidationAction.class));
        actions.add(GraphAction.getAction(TestRunAction.class));
        return actions;
    }
    
    /**
     * Generates and displays associated SQL statement for the given SQLObject.
     *
     * @param obj SQLObject whose SQL statement is to be displayed
     */
    protected void showSql(SQLObject obj) {
        if (obj.getObjectType() == SQLConstants.TARGET_TABLE) {
            SQLStatementPanel statementPanel = super.getOrCreateSQLStatementPanel(obj);
            SQLStatementPanel.ShowSQLWorkerThread showSqlThread = statementPanel.new ShowSQLWorkerThread() {
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
                        Logger.printThrowable(Logger.ERROR, ETLEditorTopView.class.getName(), this, "Failed to generate Core SQL", be);
                        
                        StringBuilder msg = new StringBuilder();
                        if (targetTable != null) {
                            msg.append(targetTable.getQualifiedName()).append(": ");
                        }
                        
                        if (StringUtil.isNullString(be.getMessage())) {
                            msg.append("Unknown error occurred while generating SQL.");
                        } else {
                            msg.append(be.getMessage());
                        }
                        this.sqlText = NbBundle.getMessage(SQLStatementPanel.class, "MSG_cant_evaluate_sql", msg);
                    }catch (Exception exp) {
                        Logger.printThrowable(Logger.ERROR, ETLEditorTopView.class.getName(), this, "Failed to generate Core SQL", ex);
                        this.sqlText = NbBundle.getMessage(SQLStatementPanel.class, "MSG_cant_evaluate_sql", exp.getMessage());
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
