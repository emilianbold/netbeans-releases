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
package org.netbeans.modules.sql.framework.ui.view;

import org.netbeans.modules.sql.framework.ui.output.dataview.DataOutputPanel;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.util.List;
import javax.swing.Action;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.editor.property.impl.PropertyViewManager;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderView;
import org.netbeans.modules.sql.framework.ui.view.graph.BasicSQLViewFactory;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLCollaborationView;
import org.netbeans.modules.sql.framework.ui.view.join.JoinMainDialog;
import org.netbeans.modules.sql.framework.ui.view.join.JoinUtility;
import org.netbeans.modules.sql.framework.ui.view.property.FFSourceTableProperties;
import org.netbeans.modules.sql.framework.ui.view.property.FFTargetTableProperties;
import org.netbeans.modules.sql.framework.ui.view.property.SourceTableProperties;
import org.netbeans.modules.sql.framework.ui.view.property.TargetTableProperties;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Attribute;
import net.java.hulp.i18n.Logger;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.view.ETLOutputWindowTopComponent;
import org.netbeans.modules.sql.framework.model.DBMetaDataFactory;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.impl.RuntimeInputImpl;
import org.netbeans.modules.sql.framework.model.impl.RuntimeOutputImpl;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.editor.property.impl.PropertyNode;
import org.netbeans.modules.sql.framework.ui.editor.property.impl.TemplateFactory;
import org.netbeans.modules.sql.framework.ui.output.SQLStatementPanel;
import org.netbeans.modules.sql.framework.ui.output.dataview.JoinOperatorDataPanel;
import org.netbeans.modules.sql.framework.ui.output.dataview.JoinViewDataPanel;
import org.netbeans.modules.sql.framework.ui.output.dataview.RejectedRowsDataPanel;
import org.netbeans.modules.sql.framework.ui.output.dataview.SourceTableDataPanel;
import org.netbeans.modules.sql.framework.ui.output.dataview.TargetTableDataPanel;
import org.netbeans.modules.sql.framework.ui.view.property.RuntimeInputProperties;
import org.netbeans.modules.sql.framework.ui.view.property.RuntimeOutputProperties;
import org.netbeans.modules.sql.framework.ui.view.property.SQLCollaborationProperties;
import org.netbeans.modules.sql.framework.ui.view.validation.SQLValidationView;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 * Main view of SQL Framework
 *
 * @author Wei Han
 * @version $Revision$
 */
public abstract class BasicTopView extends JPanel implements IGraphViewContainer {

    private static transient final Logger mLogger = Logger.getLogger(BasicTopView.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    protected static abstract class ConditionValidator implements ActionListener {

        static final class DataValidation extends ConditionValidator {

            private SourceTable mTable;

            public DataValidation(SQLBasicTableArea gNode, SourceTable table, ConditionBuilderView view, Dialog dlg, CollabSQLUIModel sqlModel) {
                super(gNode, view, dlg, sqlModel);
                mTable = table;
            }

            protected void setCondition(SQLCondition cond) {
                SQLCondition oldCondition = mTable.getExtractionCondition();
                if (cond != null) {
                    if (!cond.equals(oldCondition)) {
                        mTable.setDataValidationCondition(cond);
                        mSqlModel.setDirty(true);
                    }
                }
            }
        }

        static final class ExtractionFilter extends ConditionValidator {

            private SourceTable mTable;

            public ExtractionFilter(SQLBasicTableArea gNode, SourceTable table, ConditionBuilderView view, Dialog dlg, CollabSQLUIModel sqlModel) {
                super(gNode, view, dlg, sqlModel);
                mTable = table;
            }

            protected void setCondition(SQLCondition cond) {
                SQLCondition oldCondition = mTable.getExtractionCondition();
                if (cond != null) {
                    if (!cond.equals(oldCondition)) {
                        mTable.setExtractionCondition(cond);
                        mSqlModel.setDirty(true);
                    }
                }
            }
        }

        static final class TargetJoinConditioon extends ConditionValidator {

            private TargetTable mTable;

            public TargetJoinConditioon(SQLBasicTableArea gNode, TargetTable table, ConditionBuilderView view, Dialog dlg, CollabSQLUIModel sqlModel) {
                super(gNode, view, dlg, sqlModel);
                mTable = table;
            }

            protected void setCondition(SQLCondition cond) {
                SQLCondition oldCondition = mTable.getJoinCondition();
                if (cond != null) {
                    if (!cond.equals(oldCondition)) {
                        mTable.setJoinCondition(cond);
                        mSqlModel.setDirty(true);
                    }
                }
            }
        }

        static final class TargetFilterCondition extends ConditionValidator {

            private TargetTable mTable;

            public TargetFilterCondition(SQLBasicTableArea gNode, TargetTable table, ConditionBuilderView view, Dialog dlg, CollabSQLUIModel sqlModel) {
                super(gNode, view, dlg, sqlModel);
                mTable = table;
            }

            protected void setCondition(SQLCondition cond) {
                SQLCondition oldCondition = mTable.getFilterCondition();
                if (cond != null) {
                    if (!cond.equals(oldCondition)) {
                        mTable.setFilterCondition(cond);
                        mSqlModel.setDirty(true);
                    }
                }
            }
        }
        protected Dialog mDialog;
        protected SQLBasicTableArea mTableNode;
        protected ConditionBuilderView mView;
        protected CollabSQLUIModel mSqlModel;

        protected ConditionValidator(SQLBasicTableArea gNode, ConditionBuilderView view, Dialog dialog, CollabSQLUIModel sqlModel) {
            mTableNode = gNode;
            mView = view;
            mDialog = dialog;
            mSqlModel = sqlModel;
        }

        public void actionPerformed(ActionEvent e) {
            if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                if (!mView.isConditionValid()) {
                    String nbBundle1 = mLoc.t("BUND482: Current condition is invalid.Are you sure you want to keep it and close this builder?");
                    NotifyDescriptor confirmDlg = new NotifyDescriptor.Confirmation(nbBundle1.substring(15), mDialog.getTitle(), NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(confirmDlg);
                    if (confirmDlg.getValue() != NotifyDescriptor.YES_OPTION) {
                        return;
                    }
                }

                setCondition((SQLCondition) mView.getPropertyValue());
                if (mTableNode != null) {
                    mTableNode.setConditionIcons();
                }
            }

            mDialog.dispose();
        }

        protected abstract void setCondition(SQLCondition cond);
    }
    private static final String LOG_CATEGORY = BasicTopView.class.getName();
    protected SQLCollaborationView collabView;
    protected CollabSQLUIModel sqlModel;
    private HashMap<String, DataOutputPanel> outputDataViewMap = new HashMap<String, DataOutputPanel>();
    private SQLValidationView refreshMetaView;
    private HashMap<String, DataOutputPanel> rejectionDataViewMap = new HashMap<String, DataOutputPanel>();
    private HashMap<String, SQLStatementPanel> sqlViewMap = new HashMap<String, SQLStatementPanel>();

    /**
     * New instance
     *
     * @param model - CollabSQLUIModelImpl
     */
    public BasicTopView(CollabSQLUIModel model) {
        this.sqlModel = model;
        initGui();
    }

    /**
     * Is editable
     *
     * @return boolean - true/false
     */
    public boolean canEdit() {
        return true;
    }

    public void enableToolBarActions(boolean b) {
        List actions = this.getToolBarActions();
        Iterator it = actions.iterator();
        while (it.hasNext()) {
            Action action = (Action) it.next();
            if (action != null) {
                action.setEnabled(b);
            }
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
        } else if (command.equals(ICommand.SHOW_DATA_CMD)) {
            showDataOutputView((SQLObject) args[0]);
        } else if (command.equals(ICommand.SHOW_REJECTION_DATA_CMD)) {
            showRejectionDataOutputView((SQLObject) args[0]);
        } else if (command.equals(ICommand.SHOW_PROPERTY_CMD)) {
            IGraphNode graphNode = (IGraphNode) args[0];
            this.showProperties(graphNode);
        } else if (command.equals(ICommand.CONFIG_CMD)) {
            // Integer tableType = (Integer) args[0];
        } else if (command.equals(ICommand.EDIT_JOINVIEW)) {
            editJoinView((SQLJoinView) args[0]);
        } else if (command.equals(ICommand.DATA_VALIDATION)) {
            SQLBasicTableArea graphNode = (SQLBasicTableArea) args[0];
            doDataValidation(graphNode, (SourceTable) args[1]);
        } else if (command.equals(ICommand.DATA_EXTRACTION)) {
            SQLBasicTableArea graphNode = (SQLBasicTableArea) args[0];
            showDataExtraction(graphNode, (SourceTable) args[1]);
        } else if (command.equals(ICommand.SHOW_TARGET_JOIN_CONDITION_CMD)) {
            SQLBasicTableArea graphNode = (SQLBasicTableArea) args[0];
            showTargetJoinCondition(graphNode, (TargetTable) args[1]);
        } else if (command.equals(ICommand.SHOW_TARGET_FILTER_CONDITION_CMD)) {
            SQLBasicTableArea graphNode = (SQLBasicTableArea) args[0];
            showTargetFilterCondition(graphNode, (TargetTable) args[1]);
        }

        return null;
    }

    /**
     * Document this
     *
     * @param dataObj - data object
     * @return - IGraphNode
     */
    public IGraphNode findGraphNode(Object dataObj) {
        return this.collabView.findGraphNode(dataObj);
    }

    /**
     * Return SQLCollaborationView
     *
     * @return SQLCollaborationView
     */
    public SQLCollaborationView getCollaborationView() {
        return this.collabView;
    }

    /**
     * Return actions for popup menu of graph area
     *
     * @return a list of actions
     */
    public abstract List getGraphActions();

    /**
     * Return SQLGraphView
     *
     * @return SQLGraphView
     */
    public IGraphView getGraphView() {
        return this.collabView.getGraphView();
    }

    /**
     * Return the operator folder name
     *
     * @return operator folder name
     */
    public abstract String getOperatorFolder();

    /**
     * Return actions for toolbar
     *
     * @return a list of actions
     */
    public abstract List getToolBarActions();

    /**
     * get initial zoom factor
     *
     * @return initial zoom factor
     */
    public double getZoomFactor() {
        return this.collabView.getZoomFactor();
    }

    public void setModifiable(boolean b) {
        this.collabView.getGraphView().setModifiable(b);
        enableToolBarActions(b);
    }

    /**
     * set the zoom factor
     *
     * @param factor zoom factor
     */
    public void setZoomFactor(double factor) {
        this.collabView.setZoomFactor(factor);
    }

    /**
     * Shows output view in bottom portion of a split pane.
     *
     * @param c - component
     */
    public void showSplitPaneView(Component c) {
        // add to output.
        ETLOutputWindowTopComponent topComp = ETLOutputWindowTopComponent.findInstance();
        if (!topComp.isOpened()) {
            topComp.open();
        }
        topComp.setVisible(true);
        topComp.addPanel(c);
        topComp.requestActive();
    }

    public void setDirty(boolean dirty) {
        sqlModel.setDirty(dirty);
        SQLUIModel model = (SQLUIModel) getGraphView().getGraphModel();
        model.setDirty(dirty);
    }

    protected SQLStatementPanel getOrCreateSQLStatementPanel(SQLObject obj) {
        SQLStatementPanel c = sqlViewMap.get(obj.getId());
        if (c == null) {
            c = new SQLStatementPanel(this, obj);
            sqlViewMap.put(obj.getId(), c);
        } else {
            c.updateSQLObject(obj);
        }
        return c;
    }

    private String getTemplateName(SQLObject bean) {
        String template = null;
        Attribute attr = bean.getAttribute("ORGPROP_LOADTYPE");
        if (attr == null) {
            try {
                SQLObjectUtil.setOrgProperties((SQLDBTable) bean);
                attr = bean.getAttribute("ORGPROP_LOADTYPE");
            } catch (BaseException ex) {
                StatusDisplayer.getDefault().setStatusText(ex.getMessage());
                if (bean.getObjectType() == SQLConstants.SOURCE_TABLE) {
                    template = "FFSourceTable";
                } else if (bean.getObjectType() == SQLConstants.TARGET_TABLE) {
                    template = "FFTargetTable";
                }
                return template;
            }
        }

        if (bean.getObjectType() == SQLConstants.SOURCE_TABLE) {
            if (attr == null) {
                template = "FFSourceTable";
                return template;
            }
            if (((String) attr.getAttributeValue()).equals("RSS")) {
                template = "RSSSourceTable";
            } else if (((String) attr.getAttributeValue()).equalsIgnoreCase("WEB")) {
                template = "WebSourceTable";
            } else if (((String) attr.getAttributeValue()).equalsIgnoreCase("WEBROWSET")) {
                template = "WebrowsetSourceTable";
            } else if (((String) attr.getAttributeValue()).equalsIgnoreCase("DELIMITED") ||
                    ((String) attr.getAttributeValue()).equalsIgnoreCase("FIXEDWIDTH")) {
                template = "FFSourceTable";
            } else {
                template = "FFSourceTable";
            }
        } else if (bean.getObjectType() == SQLConstants.TARGET_TABLE) {
            if (attr == null) {
                template = "FFTargetTable";
                return template;
            }
            if (((String) attr.getAttributeValue()).equals("RSS")) {
                template = "RSSTargetTable";
            } else if (((String) attr.getAttributeValue()).equalsIgnoreCase("WEB")) {
                template = "WebTargetTable";
            } else if (((String) attr.getAttributeValue()).equalsIgnoreCase("WEBROWSET")) {
                template = "WebrowsetTargetTable";
            } else if (((String) attr.getAttributeValue()).equalsIgnoreCase("DELIMITED") ||
                    ((String) attr.getAttributeValue()).equalsIgnoreCase("FIXEDWIDTH")) {
                template = "FFTargetTable";
            } else {
                template = "FFTargetTable";
            }

        }
        return template;
    }

    /**
     * show properties dialog
     */
    public void showProperties(Object selectedObj) {
        SQLObject bean = null;
        String template = "Collaboration";
        Object pBean = new SQLCollaborationProperties(sqlModel.getSQLDefinition(), this);

        if (selectedObj != null && (selectedObj instanceof IGraphNode)) {
            IGraphNode gNode = (IGraphNode) selectedObj;
            bean = (SQLObject) gNode.getDataObject();
            if (bean == null) {
                return;
            }
            if (bean.getObjectType() == SQLConstants.SOURCE_TABLE) {
                SourceTableProperties srcTableBaen = new SourceTableProperties(this, (SQLBasicTableArea) gNode, (SourceTable) bean);
                if (((SourceTable) bean).getParent().getConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) ||
                        ((SourceTable) bean).getParent().getConnectionDefinition().getDBType().equalsIgnoreCase("Internal")) {
                    template = getTemplateName(bean);
                    pBean = new FFSourceTableProperties(srcTableBaen);
                } else {
                    template = "SourceTable";
                    pBean = srcTableBaen;
                }
            } else if (bean.getObjectType() == SQLConstants.TARGET_TABLE) {
                TargetTableProperties trgtTableBaen = new TargetTableProperties(this, (SQLBasicTableArea) gNode, (TargetTable) bean);
                if (((TargetTable) bean).getParent().getConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) ||
                        ((TargetTable) bean).getParent().getConnectionDefinition().getDBType().equalsIgnoreCase("Internal")) {
                    template = getTemplateName(bean);
                    pBean = new FFTargetTableProperties(trgtTableBaen);
                } else {
                    template = "TargetTable";
                    pBean = trgtTableBaen;
                }
            } else if (bean.getObjectType() == SQLConstants.RUNTIME_INPUT) {
                pBean = new RuntimeInputProperties((RuntimeInputImpl) bean, sqlModel.getSQLDefinition(), this);
                template = "RuntimeInput";
            } else if (bean.getObjectType() == SQLConstants.RUNTIME_OUTPUT) {
                pBean = new RuntimeOutputProperties((RuntimeOutputImpl) bean, sqlModel.getSQLDefinition(), this);
                template = "RuntimeOutput";
            }
        }

        PropertyNode pNode = PropertyViewManager.getPropertyViewManager().getPropertyNodeForTemplateName(template, null, pBean);
        final Object pb = pBean;
        pNode.addPropertyChangeSupport(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                // if value is differnt then only set it
                if (evt.getOldValue() != null && !evt.getOldValue().equals(evt.getNewValue())) {
                    try {
                        TemplateFactory.invokeSetter(pb, evt.getPropertyName(), evt.getNewValue());
                        DataObjectProvider.getProvider().getActiveDataObject().setModified(true);
                    } catch (Exception ex) {
                        mLogger.errorNoloc(mLoc.t("EDIT194: Failed to save changes {0}", LOG_CATEGORY), ex);
                    }
                }
            }
        });
        WindowManager.getDefault().getRegistry().getActivated().setActivatedNodes(new Node[]{pNode});
    //TODO: Need to update model for all the modification in the property sheet
    }

    /**
     * Generates and displays associated SQL statement for the given SQLObject.
     *
     * @param obj SQLObject whose SQL statement is to be displayed
     */
    protected void showSql(SQLObject obj) {
        SQLStatementPanel c = getOrCreateSQLStatementPanel(obj);
        c.refreshSql();
        showSplitPaneView(c);
    }

    public void showRefreshMetadataInfo(List valInfo) {
        refreshMetaView.clearView();
        if ((valInfo != null) && (valInfo.size() > 0)) {
            refreshMetaView.setValidationInfos(valInfo);
            showSplitPaneView(refreshMetaView);
        } else {
            StatusDisplayer.getDefault().setStatusText("Collaboration Metadata is up-to-date.");
        }
    }

    private void doDataValidation(SQLBasicTableArea gNode, SourceTable table) {
        String nbBundle2 = mLoc.t("BUND478: Data Validation Condition");
        ConditionBuilderView cView = ConditionBuilderUtil.getValidationConditionBuilderView(table, (IGraphViewContainer) this.getGraphView().getGraphViewContainer());
        String title = nbBundle2.substring(15);

        // Create a Dialog that defers decision-making on whether to close the dialog to
        // an ActionListener.
        DialogDescriptor dd = new DialogDescriptor(cView, title, true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);

        // Pushes closing logic to ActionListener impl
        dd.setClosingOptions(new Object[0]);

        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.getAccessibleContext().setAccessibleDescription("This dialog does Condition Validation");
        ActionListener dlgListener = new ConditionValidator.DataValidation(gNode, table, cView, dlg, sqlModel);
        dd.setButtonListener(dlgListener);

        dlg.setModal(true);
        dlg.setVisible(true);
    }

    private void editJoinView(SQLJoinView jView) {
        JoinMainDialog.showJoinDialog(sqlModel.getSQLDefinition().getJoinSources(), jView, this.getGraphView());
        if (JoinMainDialog.getClosingButtonState() == JoinMainDialog.OK_BUTTON) {
            SQLJoinView modifiedJoinView = JoinMainDialog.getSQLJoinView();
            if (!jView.equals(modifiedJoinView)) {
                sqlModel.setDirty(true);
            }
            List tableNodes = JoinMainDialog.getTableColumnNodes();
            try {
                JoinUtility.editJoinView(jView, modifiedJoinView, modifiedJoinView.getSourceTables(), tableNodes, this.getGraphView());
            } catch (BaseException ex) {

                mLogger.errorNoloc(mLoc.t("EDIT195: Caught Exception while commiting join view edits.{0}", LOG_CATEGORY), ex);
                NotifyDescriptor d = new NotifyDescriptor.Message(ex.toString(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
        updateActions();
    }

    private void initGui() {
        BasicSQLViewFactory viewFactory = new BasicSQLViewFactory(sqlModel, this, this.getGraphActions(), this.getToolBarActions());
        this.collabView = new SQLCollaborationView(viewFactory);
        // create output view
        refreshMetaView = new SQLValidationView(this.getGraphView());
        String nbBundle1 = mLoc.t("BUND483: Refresh Metadata Log");
        refreshMetaView.setName(nbBundle1.substring(15));
        setLayout(new BorderLayout());
        add(this.collabView, BorderLayout.CENTER);
    }

    private void showDataExtraction(SQLBasicTableArea gNode, SourceTable table) {
        ConditionBuilderView cView = ConditionBuilderUtil.getConditionBuilderView(table, (IGraphViewContainer) this.getGraphView().getGraphViewContainer());
        String nbBundle2 = mLoc.t("BUND506: Extraction Condition");
        String title = nbBundle2.substring(15);

        // Create a Dialog that defers decision-making on whether to close the dialog to
        // an ActionListener.
        DialogDescriptor dd = new DialogDescriptor(cView, title, true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);

        // Pushes closing logic to ActionListener impl
        dd.setClosingOptions(new Object[0]);

        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.getAccessibleContext().setAccessibleDescription("This dialog filters data based on condition specified");
        ActionListener dlgListener = new ConditionValidator.ExtractionFilter(gNode, table, cView, dlg, sqlModel);
        dd.setButtonListener(dlgListener);

        dlg.setModal(true);
        dlg.setVisible(true);
        updateActions();
    }

    /**
     * simply show the data of all the rows and column of the given table
     *
     * @param table - table
     */
    private void showDataOutputView(final SQLObject table) {
        SQLUIModel model = (SQLUIModel) getGraphView().getGraphModel();
        if (!(model instanceof CollabSQLUIModel)) {
            return;
        }

        SQLDefinition def = ((CollabSQLUIModel) model).getSQLDefinition();
        DataOutputPanel dataView = outputDataViewMap.get(table.getId());

        if (dataView == null) {
            if (table.getObjectType() == SQLConstants.TARGET_TABLE) {
                dataView = new TargetTableDataPanel((TargetTable) table, def);
            } else if (table.getObjectType() == SQLConstants.SOURCE_TABLE) {
                dataView = new SourceTableDataPanel((SourceTable) table, def);
            } else if (table.getObjectType() == SQLConstants.JOIN_VIEW) {
                dataView = new JoinViewDataPanel((SQLJoinView) table, def);
            } else if (table.getObjectType() == SQLConstants.JOIN) {
                dataView = new JoinOperatorDataPanel((SQLJoinOperator) table, def);
            }

            outputDataViewMap.put(table.getId(), dataView);
        }

        dataView.generateResult(table);
        showSplitPaneView(dataView);
    }

    /**
     * simply show the data of all the rows and column of the given table
     *
     * @param table - table
     */
    private void showRejectionDataOutputView(final SQLObject table) {
        SQLUIModel model = (SQLUIModel) getGraphView().getGraphModel();
        if (!(model instanceof CollabSQLUIModel)) {
            return;
        }

        SQLDefinition def = ((CollabSQLUIModel) model).getSQLDefinition();
        DataOutputPanel view = rejectionDataViewMap.get(table.getId());
        if (view == null) {
            view = new RejectedRowsDataPanel(table, def);
            rejectionDataViewMap.put(table.getId(), view);
        }

        view.generateResult(table);
        showSplitPaneView(view);
    }

    private void showTargetJoinCondition(final SQLBasicTableArea gNode, final TargetTable table) {
        ConditionBuilderView cView = ConditionBuilderUtil.getJoinConditionBuilderView(table, (IGraphViewContainer) this.getGraphView().getGraphViewContainer());
        String nbBundle3 = mLoc.t("BUND507: Target Join Condition");
        String title = nbBundle3.substring(15);

        // Create a Dialog that defers decision-making on whether to close the dialog to
        // an ActionListener.
        DialogDescriptor dd = new DialogDescriptor(cView, title, true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);

        // Pushes closing logic to ActionListener impl
        dd.setClosingOptions(new Object[0]);

        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.getAccessibleContext().setAccessibleDescription("This dialog helps user specify TargetJoinCondition");
        ActionListener dlgListener = new ConditionValidator.TargetJoinConditioon(gNode, table, cView, dlg, sqlModel);
        dd.setButtonListener(dlgListener);

        dlg.setModal(true);
        dlg.setVisible(true);
        updateActions();
    }

    private void showTargetFilterCondition(final SQLBasicTableArea gNode, final TargetTable table) {
        ConditionBuilderView cView = ConditionBuilderUtil.getFilterConditionBuilderView(table, (IGraphViewContainer) this.getGraphView().getGraphViewContainer());
        String nbBundle4 = mLoc.t("BUND508: Outer Filter Condition");
        String title = nbBundle4.substring(15);

        // Create a Dialog that defers decision-making on whether to close the dialog to
        // an ActionListener.
        DialogDescriptor dd = new DialogDescriptor(cView, title, true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);

        // Pushes closing logic to ActionListener impl
        dd.setClosingOptions(new Object[0]);

        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.getAccessibleContext().setAccessibleDescription("This dialog lets user configure outer filter condition for target table");
        ActionListener dlgListener = new ConditionValidator.TargetFilterCondition(gNode, table, cView, dlg, sqlModel);
        dd.setButtonListener(dlgListener);

        dlg.setModal(true);
        dlg.setVisible(true);
        updateActions();
    }

    private boolean isDirty() {
        return sqlModel.isDirty();
    }

    private void updateActions() {
        if (isDirty()) {
            //SQLUIModel model = (SQLUIModel) getGraphView().getGraphModel();
            /*IToolBar toolBar = this.getToolBar();
            if (toolBar == null) {
            return;
            }
            Action undoAction = toolBar.getAction(UndoAction.class);
            Action redoAction = toolBar.getAction(RedoAction.class);
            UndoManager undoManager = model.getUndoManager();
            if (undoManager != null && undoAction != null && redoAction != null) {
            undoAction.setEnabled(undoManager.canUndo());
            redoAction.setEnabled(undoManager.canRedo());
            }*/
            try {
                ETLDataObject etlDataObject = DataObjectProvider.getProvider().getActiveDataObject();
                ETLEditorSupport editor = etlDataObject.getETLEditorSupport();
                editor.synchDocument();
            } catch (Exception e) {
                //ignore
            }
        }
    }
}
