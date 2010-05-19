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
package org.netbeans.modules.sql.framework.ui.view.graph;

import com.sun.etl.exception.BaseException;
import com.sun.etl.exception.DBSQLException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.ListSelectionModel;

import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.IGraphController;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.impl.GradientBrush;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.TableSelectionPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;


import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoView;
import java.awt.event.InputEvent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopPanel;
import org.netbeans.modules.sql.framework.model.DBMetaDataFactory;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.impl.SQLDBModelImpl;
import org.netbeans.modules.sql.framework.model.visitors.SQLDBSynchronizationVisitor;
import org.netbeans.modules.sql.framework.ui.view.BasicTopView;
import org.netbeans.modules.sql.framework.ui.view.join.JoinViewGraphNode;
import org.openide.awt.StatusDisplayer;
import org.openide.windows.WindowManager;

/**
 * @author Ritesh Adval
 * @author Jonathan Giron
 */
public class SQLSourceTableArea extends SQLBasicTableArea {

    private static URL sourceTableImgUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/SourceTable.png");
    private static URL showDataUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/SourceTable.png");
    private static URL showSqlUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/Show_Sql.png");
    private static URL autoMapImgUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/AutoMapToTarget.png");
    private static URL defineValidationImgUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/validateMenu.png");
    private static URL dataFilterImgUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/filter16.gif");
    private static URL propertiesUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/properties.png");
    private static URL synchroniseImgUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/refresh.png");
    private static URL remountImgUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/redo.png");
    private static final Color DEFAULT_BG_COLOR = new Color(204, 213, 241);
    private static final Color DEFAULT_BG_COLOR_DARK = new Color(165, 193, 249); // new Color(249, 224, 127);
    private static final JGoBrush DEFAULT_TITLE_BRUSH = new GradientBrush(DEFAULT_BG_COLOR_DARK, DEFAULT_BG_COLOR);
    private JMenuItem showSqlItem;
    private JMenuItem showDataItem;
    private JMenuItem propertiesItem;
    private JMenuItem autoMapItem;
    private JMenuItem dataValidationMapItem;
    private JMenuItem dataFilterMapItem;
    private JMenuItem synchroniseItem;
    private JMenuItem remountItem;
    private transient ETLCollaborationTopPanel designView;
    private static transient final Logger mLogger = Logger.getLogger(SQLSourceTableArea.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    
    /**
     * Creates a new instance of SQLSourceTableArea
     */
    public SQLSourceTableArea() {
        super();
    }

    /**
     * Creates a new instance of SQLSourceTableArea
     *
     * @param table the table to render
     */
    public SQLSourceTableArea(SourceTable table) {
        super(table);
    }

    /**
     *
     */
    protected void initializePopUpMenu() {
        try {
            ActionListener aListener = new TableActionListener();
            // Show SQL
            String nbBundle1 = mLoc.t("BUND365: Show SQL");
            String lblShowSql = nbBundle1.substring(15);
            showSqlItem = new JMenuItem(lblShowSql, new ImageIcon(showSqlUrl));
            showSqlItem.setAccelerator(KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK));
            showSqlItem.addActionListener(aListener);
            popUpMenu.add(showSqlItem);

            // Show data
            String nbBundle2 = mLoc.t("BUND453: Show Data");
            String lblShowData = nbBundle2.substring(15);
            showDataItem = new JMenuItem(lblShowData, new ImageIcon(showDataUrl));
            showDataItem.setAccelerator(KeyStroke.getKeyStroke('D',InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK));
            showDataItem.addActionListener(aListener);
            popUpMenu.add(showDataItem);


            addSelectVisibleColumnsPopUpMenu(aListener);
            String nbBundle24 = mLoc.t("BUND024: Refresh Metadata");
            synchroniseItem = new JMenuItem(nbBundle24.substring(15), new ImageIcon(synchroniseImgUrl));
            synchroniseItem.setMnemonic(nbBundle24.substring(15).charAt(9));
            synchroniseItem.addActionListener(aListener);
            popUpMenu.add(synchroniseItem);

            String nbBundle3 = mLoc.t("BUND027: Remount");
            String lblRemount = nbBundle3.substring(15);
            remountItem = new JMenuItem(lblRemount, new ImageIcon(remountImgUrl));
            remountItem.setMnemonic(nbBundle3.substring(15).charAt(0));
            remountItem.addActionListener(aListener);
            SQLObject tbl = (SQLObject) SQLSourceTableArea.this.getDataObject();
            SQLDBModelImpl impl = (SQLDBModelImpl) tbl.getParentObject();
            if (impl.getETLDBConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) ||
                impl.getETLDBConnectionDefinition().getDBType().equalsIgnoreCase("Internal")) {
                popUpMenu.add(remountItem);
            }

            // Define data filtering action
            String nbBundle4 = mLoc.t("BUND455: Extraction Condition...");
            popUpMenu.addSeparator();
            dataFilterMapItem = new JMenuItem(nbBundle4.substring(15), new ImageIcon(dataFilterImgUrl));
            dataFilterMapItem.setAccelerator(KeyStroke.getKeyStroke('F',InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK));
            dataFilterMapItem.setMnemonic(nbBundle4.substring(15).charAt(0));
            dataFilterMapItem.addActionListener(aListener);
            popUpMenu.add(dataFilterMapItem);

            // Define data validation action
            String nbBundle5 = mLoc.t("BUND456: Data Validation...");
            dataValidationMapItem = new JMenuItem(nbBundle5.substring(15), new ImageIcon(defineValidationImgUrl));
            dataValidationMapItem.setMnemonic(nbBundle5.substring(15).charAt(0));
            dataValidationMapItem.addActionListener(aListener);
            popUpMenu.add(dataValidationMapItem);

            // Remove
            popUpMenu.addSeparator();
            addRemovePopUpMenu(aListener);

            // Show properties
            popUpMenu.addSeparator();
            String nbBundle6 = mLoc.t("BUND443: Properties");
            String lblProps = nbBundle6.substring(15);
            propertiesItem = new JMenuItem(lblProps, new ImageIcon(propertiesUrl));
            propertiesItem.setAccelerator(KeyStroke.getKeyStroke('7',InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK));
            propertiesItem.addActionListener(aListener);
            popUpMenu.add(propertiesItem);

            // Auto map action
            String nbBundle7 = mLoc.t("BUND458: Auto Map");
            autoMapItem = new JMenuItem(nbBundle7.substring(15), new ImageIcon(autoMapImgUrl));
            autoMapItem.setAccelerator(KeyStroke.getKeyStroke('A',InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK));
            autoMapItem.addActionListener(aListener);

            popUpMenu.addSeparator();
            popUpMenu.add(autoMapItem);
        } catch (BaseException ex) {
             mLogger.infoNoloc(ex.getMessage());
        }

    }

    Icon createIcon() {
        return new ImageIcon(sourceTableImgUrl);
    }

    private class TableActionListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         *
         * @param e ActionEvent to handle
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == propertiesItem) {
                Properties_ActionPerformed(e);
            } else if (source == showSqlItem) {
                ShowSql_ActionPerformed(e);
            } else if (source == remountItem) {
                Remount_ActionPerformed(e);
            } else if (source == showDataItem) {
                ShowData_ActionPerformed(e);
            } else if (source == autoMapItem) {
                performAutoMap(e);
            } else if (source == dataValidationMapItem) {
                DataValidation_ActionPerformed(e);
            } else if (source == synchroniseItem) {
                synchroniseItem_ActionPerformed(e);
            } else if (source == dataFilterMapItem) {
                showDataFilter_ActionPerformed(e);
            } else {
                handleCommonActions(e);
            }
        }
    }

    private void Properties_ActionPerformed(ActionEvent e) {
        if (!WindowManager.getDefault().findTopComponent("properties").isShowing()) {
            WindowManager.getDefault().findTopComponent("properties").open();
        }
    }

    private void Remount_ActionPerformed(ActionEvent e) {
        try {
            SQLDBTable table = (SQLDBTable) SQLSourceTableArea.this.getDataObject();
            SQLObjectUtil.dropTable(table, (SQLDBModel) table.getParent());
            SQLObjectUtil.createTable(table, (SQLDBModel) table.getParent());
            SQLObjectUtil.setOrgProperties(table);
        } catch (Exception ex) {
             mLogger.infoNoloc(ex.getMessage());
            StatusDisplayer.getDefault().setStatusText("Unable to remount :" + ex.getMessage());
        }
    }

    private void ShowSql_ActionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLSourceTableArea.this.getDataObject();
        Object[] args = new Object[]{sqlObject};
        this.getGraphView().execute(ICommand.SHOW_SQL_CMD, args);
    }

    private void ShowData_ActionPerformed(ActionEvent e) {
        SQLObject tbl = (SQLObject) SQLSourceTableArea.this.getDataObject();
        this.getGraphView().execute(ICommand.SHOW_DATA_CMD, new Object[]{tbl});
    }

    private void showDataFilter_ActionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLSourceTableArea.this.getDataObject();
        Object[] args = new Object[]{SQLSourceTableArea.this, sqlObject};
        this.getGraphView().execute(ICommand.DATA_EXTRACTION, args);
    }

    private void synchroniseItem_ActionPerformed(ActionEvent e) {
        IGraphView gView = this.getGraphView();
        CollabSQLUIModel sqlModel = (CollabSQLUIModel) gView.getGraphModel();

        String nbBundle8 = mLoc.t("BUND026: If columns are deleted or renamed you may lose existing mappings.");
        String dlgMsg = nbBundle8.substring(15);
        String nbBundle9 = mLoc.t("BUND024: Refresh Metadata");
        String dlgTitle = nbBundle9.substring(15);
        int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), dlgMsg, dlgTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        if (JOptionPane.OK_OPTION == response) {
            try {
                SQLDBTable tbl = (SQLDBTable) SQLSourceTableArea.this.getDataObject();
                SQLDBSynchronizationVisitor visitView = new SQLDBSynchronizationVisitor();

                SQLTableArea tableArea1 = (SQLTableArea) this.getTableArea();
                MetaTableModel model = (MetaTableModel) tableArea1.getModel();
                visitView.mergeCollabTableWithDatabaseTable(tbl, model);
                if (!visitView.infoList.isEmpty()) {
                    tableArea1.layoutChildren();
                    SQLJoinView jView = sqlModel.getJoinView((SourceTable) tbl);
                    if (jView != null) {
                        JoinViewGraphNode jViewGraph = (JoinViewGraphNode) gView.findGraphNode(jView);
                        jViewGraph.layoutChildren();
                        jViewGraph.setHeight(jViewGraph.getMaximumHeight());
                    }
                    // Mark collab as needing to be persisted.
                    DataObjectProvider.getProvider().getActiveDataObject().setModified(true);
                    sqlModel.setDirty(true);
                    
                }
                BasicTopView gvMgr = (BasicTopView) gView.getGraphViewContainer();
                gvMgr.showRefreshMetadataInfo(visitView.infoList);
            } catch (DBSQLException ex) {
                mLogger.infoNoloc(ex.getMessage());
            } catch (Exception ex) {
                mLogger.infoNoloc(ex.getMessage());
            }
        }
    }

    private void performAutoMap(ActionEvent e) {
        IGraphView gView = this.getGraphView();
        CollabSQLUIModel sqlModel = (CollabSQLUIModel) gView.getGraphModel();

        if (sqlModel != null) {
            List tTables = sqlModel.getSQLDefinition().getTargetTables();
            //if there is only one target table
            if (tTables.size() == 1) {
                createLinksToTarget((TargetTable) tTables.get(0));
                return;
            }

            TargetTable tt = SQLObjectUtil.getMappedTargetTable((SQLObject) this.getDataObject(), tTables);
            if (tt != null) {
                createLinksToTarget(tt);
                return;
            }

            //otherwise we have multiple target tables so need to ask user to select a
            // target
            //table
            TableSelectionPanel tableSelectionPanel = new TableSelectionPanel(tTables);
            tableSelectionPanel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tableSelectionPanel.setPreferredSize(new Dimension(200, 160));

            DialogDescriptor dd = new DialogDescriptor(tableSelectionPanel, "Select Target Table to Auto Map", true,
                    NotifyDescriptor.OK_CANCEL_OPTION, null, null);

            if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
                List selectedTables = tableSelectionPanel.getSelectedTables();
                if (selectedTables.size() == 1) {
                    createLinksToTarget((TargetTable) selectedTables.get(0));
                }
            }
        }
        updateActions();
    }

    private void createLinksToTarget(TargetTable tTable) {
        IGraphView gView = this.getGraphView();
        if (!(gView instanceof SQLGraphView)) {
            return;
        }

        IGraphController graphController = gView.getGraphController();

        IGraphNode targetNode = gView.findGraphNode(tTable);
        if (targetNode == null) {
            return;
        }

        SQLDBTable sTable = (SQLDBTable) this.getDataObject();
        Iterator it = sTable.getColumnList().iterator();

        while (it.hasNext()) {
            SQLDBColumn column = (SQLDBColumn) it.next();
            SQLDBColumn tColumn = getColumnIgnoreCase(tTable, column.getName());
            if (tColumn != null && tColumn.getJdbcType() == column.getJdbcType()) {

                IGraphPort from = this.getOutputGraphPort(column.getName());
                IGraphPort to = targetNode.getInputGraphPort(tColumn.getName());
                if (from != null && to != null) {
                    graphController.handleLinkAdded(from, to);
                }
            }
        }
        updateActions();
    }

    private SQLDBColumn getColumnIgnoreCase(TargetTable tt, String columnName) {
        Iterator it = tt.getColumnList().iterator();

        while (it.hasNext()) {
            SQLDBColumn column = (SQLDBColumn) it.next();
            if (column.getName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }

        return null;
    }

    private void DataValidation_ActionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLSourceTableArea.this.getDataObject();
        Object[] args = new Object[]{SQLSourceTableArea.this, sqlObject};
        this.getGraphView().execute(ICommand.DATA_VALIDATION, args);
    }

    public boolean doMouseClick(int modifiers, Point dc, Point vc, JGoView view1) {
        IGraphView gView = this.getGraphView();
        CollabSQLUIModel sqlModel = (CollabSQLUIModel) gView.getGraphModel();
        if (sqlModel != null) {
            List tTables = sqlModel.getSQLDefinition().getTargetTables();
            //if there are no target tables then we need not show automap menu item
            if (tTables.size() == 0) {
                this.autoMapItem.setEnabled(false);
                return super.doMouseClick(modifiers, dc, vc, view1);
            }

            this.autoMapItem.setEnabled(true);

            //if there is only one target table then we do not need to show a dialog to
            // the user
            //to select a target table
            if (tTables.size() == 1) {
                this.autoMapItem.setText("Auto Map");
                return super.doMouseClick(modifiers, dc, vc, view1);
            }

            TargetTable tt = SQLObjectUtil.getMappedTargetTable((SQLObject) this.getDataObject(), tTables);
            //if this source table is already mapped to an existing target table
            //then auto map should map to that target table and also we do not need to
            // show dialog to the user
            if (tt != null) {
                this.autoMapItem.setText("Auto Map");
                return super.doMouseClick(modifiers, dc, vc, view1);
            }

            //otherwise there are multiple target tables so we need to show a dialog
            //to the user
            this.autoMapItem.setText("Auto Map...");
            return super.doMouseClick(modifiers, dc, vc, view1);
        }

        return super.doMouseClick(modifiers, dc, vc, view1);
    }

    /**
     * Sets the data object
     *
     * @param obj - then object to be represented by this node
     */
    public void setDataObject(Object obj) {
        super.setDataObject(obj);
        setConditionIcons();
    }

    /**
     * Sets Data extraction and Validation icons.
     *
     * @see org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea#setConditionIcons()
     */
    public void setConditionIcons() {
        SQLCondition extractionCondition = null;
        SQLCondition validationCondition = null;

        SourceTable tbl = (SourceTable) this.getDataObject();
        if (tbl != null) {
            extractionCondition = tbl.getExtractionCondition();
            validationCondition = tbl.getDataValidationCondition();

            setTableConditionIcons(extractionCondition, validationCondition);
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea#getDefaultTitleBrush()
     */
    protected JGoBrush getDefaultTitleBrush() {
        return DEFAULT_TITLE_BRUSH;
    }

    /**
     * @return
     * @see org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea#getDefaultBackgroundColor()
     */
    protected Color getDefaultBackgroundColor() {
        return DEFAULT_BG_COLOR;
    }

    private void updateActions() {
        try {
            this.getView().updateUI();
            ETLDataObject etlDataObject = DataObjectProvider.getProvider().getActiveDataObject();
            ETLEditorSupport editor = etlDataObject.getETLEditorSupport();
            editor.synchDocument();
        } catch (Exception ex) {
        //ignore
        }
    }
}

