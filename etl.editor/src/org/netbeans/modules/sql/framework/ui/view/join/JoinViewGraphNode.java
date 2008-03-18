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
package org.netbeans.modules.sql.framework.ui.view.join;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCanvasArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.TableArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.TitleArea;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.JoinBuilderSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.impl.JoinBuilderSQLUIModelImpl;
import org.netbeans.modules.sql.framework.ui.view.TableColumnNode;
import org.netbeans.modules.sql.framework.ui.view.TableColumnTreePanel;
import org.netbeans.modules.sql.framework.ui.view.graph.MetaTableModel;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLTableArea;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.WindowManager;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoLink;
import com.sun.sql.framework.exception.BaseException;
import net.java.hulp.i18n.Logger;
import javax.swing.table.TableModel;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.ui.graph.impl.GradientBrush;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class JoinViewGraphNode extends BasicCanvasArea {

    /* log4j logger category */
    private static final String LOG_CATEGORY = JoinViewGraphNode.class.getName();
    private static URL showSqlUrl = JoinViewGraphNode.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/Show_Sql.png");
    private static URL joinViewUrl = JoinViewGraphNode.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/join_view.png");
    private static URL editJoinViewUrl = JoinViewGraphNode.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/edit_join.png");
    protected static URL selectColumnsUrl = JoinViewGraphNode.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/ColumnSelection.png");
    private static URL removeUrl = JoinViewGraphNode.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/remove.png");
    private static URL showJoinDataUrl = JoinViewGraphNode.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/showOutput.png");
    //private static final Color DEFAULT_TITLE_COLOR = new Color(221, 235, 246);
    private static final Color DEFAULT_BG_COLOR = new Color(204, 213, 241);
    private static final Color DEFAULT_BG_COLOR_DARK = new Color(165, 193, 249);
    private static final JGoBrush DEFAULT_TITLE_BRUSH = new GradientBrush(DEFAULT_BG_COLOR_DARK, DEFAULT_BG_COLOR);
    private JMenuItem showSqlItem;
    private JMenuItem editItem;
    private JMenuItem showDataItem;
    private JMenuItem selectColumnsItem;
    protected JMenuItem removeItem;
    private JoinViewActionListener aListener;
    private ArrayList<SQLJoinTableArea> tableAreas = new ArrayList<SQLJoinTableArea>();
    private static final int PREFERRED_JOIN_VIEW_WIDTH = 140;
    private static transient final Logger mLogger = Logger.getLogger(JoinViewGraphNode.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /** Creates a new instance of JoinViewGraphNode */
    public JoinViewGraphNode(SQLJoinView jView) {
        initGUI(jView);
    }

    private void initGUI(SQLJoinView jView) {
        this.setSelectable(true);
        this.setResizable(true);
        this.setPickableBackground(false);

        //add join view title
        String lblTitleArea = jView.getQualifiedName();
        titleArea = new TitleArea(lblTitleArea);
        titleArea.setBrush(DEFAULT_TITLE_BRUSH);

        ImageIcon joinTitleIcon = new ImageIcon(joinViewUrl);
        titleArea.setTitleImage(joinTitleIcon);
        addObjectAtTail(titleArea);

        //go through each table in join view and add table header and
        //all table columns
        Collection joinTables = jView.getSQLJoinTables();

        Iterator it = joinTables.iterator();
        while (it.hasNext()) {
            SQLJoinTable jTable = (SQLJoinTable) it.next();
            SourceTable sTable = jTable.getSourceTable();
            addTable(sTable);
        }

        if (this.getSize().getWidth() == PREFERRED_JOIN_VIEW_WIDTH) {
            // Force layoutChildren().
            this.setSize(PREFERRED_JOIN_VIEW_WIDTH - 1, this.getMaximumHeight());
        } else {
            this.setSize(PREFERRED_JOIN_VIEW_WIDTH, this.getMaximumHeight());
        }

        initializePopUpMenu();
    }

    private void initializePopUpMenu() {
        this.popUpMenu = new JPopupMenu();
        aListener = new JoinViewActionListener();

        // edit join
        String nbBundle1 = mLoc.t("BUND473: Edit JoinView...");
        String lblEditJoin = nbBundle1.substring(15);
        editItem = new JMenuItem(lblEditJoin, new ImageIcon(editJoinViewUrl));
        editItem.addActionListener(aListener);
        popUpMenu.add(editItem);

        // show sql
        // NOTE: Use SQLBasicTableArea.class as superclass Bundle already contains this
        // resource
        String nbBundle2 = mLoc.t("BUND365: Show SQL");
        String lblShowSql = nbBundle2.substring(15);
        showSqlItem = new JMenuItem(lblShowSql, new ImageIcon(showSqlUrl));
        showSqlItem.addActionListener(aListener);
        popUpMenu.add(showSqlItem);

        // show join data
        String nbBundle3 = mLoc.t("BUND453: Show Data");
        String lblShowData = nbBundle3.substring(15);
        showDataItem = new JMenuItem(lblShowData, new ImageIcon(showJoinDataUrl));
        showDataItem.addActionListener(aListener);
        popUpMenu.add(showDataItem);

        // select visible columns
        // NOTE: Use SQLBasicTableArea.class as superclass Bundle already contains this
        // resource
        String nbBundle4 = mLoc.t("BUND426: Select Columns...");
        String lblSelectColumns = nbBundle4.substring(15);
        selectColumnsItem = new JMenuItem(lblSelectColumns, new ImageIcon(selectColumnsUrl));
        selectColumnsItem.addActionListener(aListener);
        popUpMenu.add(selectColumnsItem);

        popUpMenu.addSeparator();
        // NOTE: Use SQLBasicTableArea.class as superclass Bundle already contains this
        // resource
        String nbBundle5 = mLoc.t("BUND152: Remove");
        String lbl = nbBundle5.substring(15);
        removeItem = new JMenuItem(lbl, new ImageIcon(removeUrl));
        removeItem.addActionListener(aListener);
        popUpMenu.add(removeItem);
    }

    @Override
    public void setGraphView(IGraphView view) {
        super.setGraphView(view);
        Iterator it = tableAreas.iterator();

        while (it.hasNext()) {
            SQLBasicTableArea tableArea = (SQLBasicTableArea) it.next();
            tableArea.setGraphView(view);
        }
    }

    /**
     * Gets the minimum height of the area.
     *
     * @return minimum height
     */
    @Override
    public int getMaximumHeight() {
        int maxHeight = 0;

        maxHeight = getInsets().top + getInsets().bottom;

        maxHeight += titleArea.getMinimumHeight();

        Iterator it = tableAreas.iterator();
        while (it.hasNext()) {
            SQLBasicTableArea tableArea = (SQLBasicTableArea) it.next();
            maxHeight += tableArea.getMaximumHeight();
        }

        return maxHeight;
    }

    /**
     * Gets the minimum width of the area.
     *
     * @return minimum width
     */
    @Override
    public int getMaximumWidth() {
        int maxWidth = 0;

        maxWidth = getInsets().left + getInsets().right;

        int width = 0;

        if (titleArea.getMinimumWidth() > width) {
            width = titleArea.getMinimumWidth();
        }

        Iterator it = tableAreas.iterator();
        while (it.hasNext()) {
            SQLBasicTableArea tableArea = (SQLBasicTableArea) it.next();
            if (tableArea.getMaximumWidth() > width) {
                width = tableArea.getMaximumWidth();
            }
        }

        maxWidth += width;

        return maxWidth;
    }

    /**
     * Gets the minimum height.
     *
     * @return minimum height
     */
    @Override
    public int getMinimumHeight() {
        int minHeight = getInsets().top + getInsets().bottom;

        //take min height of title into account this much height
        //we want to show always
        if (titleArea != null) {
            minHeight += titleArea.getMinimumHeight();
        }

        return minHeight;
    }

    /**
     * Gets the minimum width.
     *
     * @return minimum width
     */
    @Override
    public int getMinimumWidth() {
        int minWidth = 0;

        //take min width of title into account this much
        //wide we want to show always
        if (titleArea != null) {
            minWidth = titleArea.getMinimumWidth();
        }

        // Account for width of all table areas - use maximum width to allow for
        // visibility of all column names.
        Iterator it = tableAreas.iterator();
        while (it.hasNext()) {
            SQLBasicTableArea tableArea = (SQLBasicTableArea) it.next();
            minWidth = Math.max(tableArea.getMaximumWidth(), minWidth);
        }

        // Always account for horizontal insets.
        minWidth += getInsets().left + getInsets().right;
        return minWidth;
    }

    /**
     * Lays out the children of this cell area.
     */
    @Override
    public void layoutChildren() {
        int rectleft = getLeft();
        int recttop = getTop();
        int rectwidth = getWidth();
        int rectheight = getHeight();

        int left = rectleft + insets.left;
        int top = recttop + insets.top;
        int width = rectwidth - insets.left - insets.right;
        int height = rectheight - insets.top - insets.bottom;

        titleArea.setBoundingRect(left, top, width, titleArea.getMinimumHeight());

        Iterator it = tableAreas.iterator();

        int topSoFar = top + titleArea.getHeight();

        while (it.hasNext()) {
            SQLBasicTableArea tableArea = (SQLBasicTableArea) it.next();
            if (height - titleArea.getHeight() > 0) {
                tableArea.setVisible(true);
                tableArea.setBoundingRect(left, topSoFar, width, tableArea.getMaximumHeight());
                topSoFar += tableArea.getHeight();
            } else {
                tableArea.setVisible(false);
                tableArea.setBoundingRect(titleArea.getBoundingRect());
            }
        }
    }

    private class JoinViewActionListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == editItem) {
                edit_ActionPerformed(e);
            } else if (source == showSqlItem) {
                ShowSql_ActionPerformed(e);
            } else if (source == selectColumnsItem) {
                selectVisibleColumnsActionPerformed(e);
            } else if (source == removeItem) {
                Remove_ActionPerformed(e);
            } else if (source == showDataItem) {
                showJoinData_ActionPerformed(e);
            }
        }
    }

    private void edit_ActionPerformed(ActionEvent e) {
        SQLJoinView joinView = (SQLJoinView) JoinViewGraphNode.this.getDataObject();
        Object[] args = new Object[]{joinView};
        this.getGraphView().execute(ICommand.EDIT_JOINVIEW, args);
    }

    private void ShowSql_ActionPerformed(ActionEvent e) {
        SQLJoinView joinView = (SQLJoinView) JoinViewGraphNode.this.getDataObject();
        SQLJoinOperator op = joinView.getRootJoin();
        op.setDisplayName(joinView.getAliasName());
        Object[] args = new Object[]{op};
        this.getGraphView().execute(ICommand.SHOW_SQL_CMD, args);
    }

    private void showJoinData_ActionPerformed(ActionEvent e) {
        SQLJoinView joinView = (SQLJoinView) JoinViewGraphNode.this.getDataObject();
        Object[] args = new Object[]{joinView};
        this.getGraphView().execute(ICommand.SHOW_DATA_CMD, args);
    }

    private void selectVisibleColumnsActionPerformed(ActionEvent e) {
        List<SourceTable> tables = new ArrayList<SourceTable>(tableAreas.size());
        List<DBColumn> columns = new ArrayList<DBColumn>(tableAreas.size() * 5);
        Map<SourceTable, TableModel> tableToModelMap = new HashMap<SourceTable, TableModel>();

        Iterator tableIter = tableAreas.iterator();
        if (tableIter.hasNext()) {
            do {
                SQLJoinTableArea joinTblArea = (SQLJoinTableArea) tableIter.next();
                TableArea tblArea = joinTblArea.getTableArea();
                SourceTable srcTable = (SourceTable) joinTblArea.getDataObject();

                if (tblArea instanceof SQLTableArea && srcTable != null) {
                    tableToModelMap.put(srcTable, ((SQLTableArea) tblArea).getModel());
                    tables.add(srcTable);
                    columns.addAll(srcTable.getColumnList());
                }
            } while (tableIter.hasNext());
        }

        if (tables.isEmpty()) {
            return;
        }

        TableColumnTreePanel columnPanel = new TableColumnTreePanel(tables, true);
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;

        // NOTE: Use SQLBasicTableArea.class as superclass Bundle already contains this
        // resource
        String nbBundle6 = mLoc.t("BUND428: Select columns to display for this table.");
        String dlgLabel = nbBundle6.substring(15);
        JLabel lbl = new JLabel(dlgLabel);
        lbl.getAccessibleContext().setAccessibleName(dlgLabel);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.bottom = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        panel.add(new JSeparator(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel.add(columnPanel, gbc);

        // NOTE: Use SQLBasicTableArea.class as superclass Bundle already contains this
        // resource
        String nbBundle7 = mLoc.t("BUND429: Select Columns");
        String dlgTitle = nbBundle7.substring(15);
        int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), panel, dlgTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (JOptionPane.OK_OPTION == response) {
            List tableNodes = columnPanel.getTableColumnNodes();

            // Toggle visibility of columns as directed by user.
            Iterator iter = columns.iterator();
            while (iter.hasNext()) {
                SQLDBColumn column = (SQLDBColumn) iter.next();
                boolean userWantsVisible = TableColumnNode.isColumnVisible(column, tableNodes);
                if (column.isVisible() && !userWantsVisible) {
                    column.setVisible(false);
                    try {
                        removeColumn(column);
                    } catch (BaseException ex) {
                    }
                } else if (!column.isVisible() && userWantsVisible) {
                    column.setVisible(true);
                    MetaTableModel model = (MetaTableModel) tableToModelMap.get(column.getParentObject());

                    if ((model != null) && !model.containsColumn(column)) {
                        model.addColumn(column);
                    } else {
                        makeColumnVisible(column);
                    }
                }
            }

            // Trigger repaint of all affected GUI objects.
            iter = tableAreas.iterator();
            while (iter.hasNext()) {
                SQLJoinTableArea tblArea = (SQLJoinTableArea) iter.next();
                tblArea.setHeight(tblArea.getMaximumHeight());
                tblArea.layoutChildren();
            }

            setHeight(getMaximumHeight());
            layoutChildren();

            // Mark collab as needing to be persisted.
            Object graphModel = getGraphView().getGraphModel();
            if (graphModel instanceof CollabSQLUIModel) {
                ((CollabSQLUIModel) graphModel).setDirty(true);
            }
        }
    }

    private void Remove_ActionPerformed(ActionEvent e) {
        this.getGraphView().deleteNode(this);
    }

    /**
     * get a list of all input and output links
     *
     * @return list of input links
     */
    @Override
    public List getAllLinks() {
        ArrayList<JGoLink> links = new ArrayList<JGoLink>();

        Iterator it = this.tableAreas.iterator();

        while (it.hasNext()) {
            SQLBasicTableArea tableArea = (SQLBasicTableArea) it.next();
            links.addAll(tableArea.getAllLinks());
        }

        return links;
    }

    /**
     * get a list of all table areas in this join view
     *
     * @return list of input links
     */
    public List getAllTableAreas() {
        return this.tableAreas;
    }

    public boolean isColumnMapped(SQLDBColumn column) {
        Iterator it = this.tableAreas.iterator();

        while (it.hasNext()) {
            SQLBasicTableArea tableArea = (SQLBasicTableArea) it.next();
            if (tableArea.isColumnMapped(column)) {
                return true;
            }
        }

        return false;
    }

    public void removeColumnReference(SQLDBColumn column) throws BaseException {
        DBTable table = column.getParent();

        Iterator it = this.tableAreas.iterator();

        while (it.hasNext()) {
            SQLBasicTableArea tableArea = (SQLBasicTableArea) it.next();
            Object tableObj = tableArea.getDataObject();
            if (tableObj.equals(table)) {
                tableArea.removeColumnReference(column);
                return;
            }
        }
    }

    public void removeColumn(SQLDBColumn column) throws BaseException {
        DBTable table = column.getParent();

        Iterator it = this.tableAreas.iterator();

        while (it.hasNext()) {
            SQLBasicTableArea tableArea = (SQLBasicTableArea) it.next();
            Object tableObj = tableArea.getDataObject();
            if (tableObj.equals(table)) {
                tableArea.removeColumn(column);
                this.layoutChildren();
                return;
            }
        }
    }

    public void makeColumnInVisible(SQLDBColumn column) throws BaseException {
        DBTable table = column.getParent();

        Iterator it = this.tableAreas.iterator();

        while (it.hasNext()) {
            SQLBasicTableArea tableArea = (SQLBasicTableArea) it.next();
            Object tableObj = tableArea.getDataObject();
            if (tableObj.equals(table)) {
                tableArea.makeColumnInVisible(column);
                return;
            }
        }
    }

    public void makeColumnVisible(SQLDBColumn column) {
        DBTable table = column.getParent();

        Iterator it = this.tableAreas.iterator();

        while (it.hasNext()) {
            SQLBasicTableArea tableArea = (SQLBasicTableArea) it.next();
            Object tableObj = tableArea.getDataObject();
            if (tableObj.equals(table)) {
                tableArea.makeColumnVisible(column);
                return;
            }
        }
    }

    public void addColumn(SQLDBColumn column) {
        SQLDBTable table = (SQLDBTable) column.getParent();

        Iterator it = this.tableAreas.iterator();

        while (it.hasNext()) {
            SQLBasicTableArea tableArea = (SQLBasicTableArea) it.next();
            SQLDBTable tableObj = (SQLDBTable) tableArea.getDataObject();
            if (tableObj.getParent().getFullyQualifiedTableName(tableObj).equals(table.getParent().getFullyQualifiedTableName(table))) {
                ArrayList<SQLDBColumn> columns = new ArrayList<SQLDBColumn>();
                columns.add(column);
                tableArea.addColumns(columns);
                this.layoutChildren();
                return;
            }
        }
    }

    /**
     * set the bounding rectangle and make it not resize beyond a certain wisth
     *
     * @param left left
     * @param top top
     * @param width width
     * @param height
     */
    @Override
    public void setBoundingRect(int left, int top, int width, int height) {
        if (this.isExpandedState()) {
            super.setBoundingRect(left, top, Math.max(width, 100), this.getMaximumHeight());
        } else {
            super.setBoundingRect(left, top, Math.max(width, 100), height);
        }
    }
    //
    //    public void removeColumnReference(SQLDBTable table, SQLDBColumn column) {
    //        Iterator it = this.tableAreas.iterator();
    //        while(it.hasNext()) {
    //            SQLJoinTableArea tableArea = (SQLJoinTableArea) it.next();
    //            if(tableArea.getDataObject().equals(table)) {
    //                tableArea.removeColumnReference(column);
    //            }
    //        }
    //    }
    public void addTable(SourceTable sTable) {
        SQLJoinTableArea tableArea = new SQLJoinTableArea(sTable);
        tableArea.setDataObject(sTable);
        tableArea.setGraphView(this.getGraphView());
        tableArea.setSelectable(true);
        tableArea.setResizable(false);
        tableArea.setPickableBackground(false);
        tableArea.showExpansionImage(false);
        //tableArea.setShowHeader(false);
        addObjectAtTail(tableArea);
        tableAreas.add(tableArea);
    }

    public boolean containsTable(SourceTable sTable) {
        Iterator it = tableAreas.iterator();

        while (it.hasNext()) {
            SQLJoinTableArea tableArea = (SQLJoinTableArea) it.next();
            if (sTable.equals(tableArea.getDataObject())) {
                return true;
            }
        }

        return false;
    }

    public void removeTable(SourceTable sTable) throws BaseException {
        try {
            SQLJoinView joinView = (SQLJoinView) this.getDataObject();
            SQLJoinTable jTable = joinView.getJoinTable(sTable);

            NotifyDescriptor nd = new NotifyDescriptor.Confirmation("You may lose some user defined conditions in some joins, Do you really want to remove the table?", NotifyDescriptor.WARNING_MESSAGE);

            Object response = DialogDisplayer.getDefault().notify(nd);
            if (response.equals(NotifyDescriptor.CANCEL_OPTION)) {
                return;
            }

            SQLUIModel model = (SQLUIModel) this.getGraphView().getGraphModel();
            if (model == null) {
                throw new BaseException("can not delete table " + sTable.getName() + " from join view, graph model is null");
            }

            Iterator it = tableAreas.iterator();

            while (it.hasNext()) {
                SQLJoinTableArea tableArea = (SQLJoinTableArea) it.next();
                if (sTable.equals(tableArea.getDataObject())) {
                    SQLJoinOperator op = joinView.getRootJoin();
                    List joinTableList = null;
                    if (op != null) {
                        joinTableList = JoinUtility.getJoinTables(op);
                    }

                    if (joinTableList == null) {
                        return;
                    }

                    //remove table from join view this will also remove
                    //dangling references to jTable
                    joinView.removeObject(jTable);

                    JoinBuilderSQLUIModel joinModel = new JoinBuilderSQLUIModelImpl(joinView);
                    int index = joinTableList.indexOf(jTable);

                    if (index != 0) {
                        for (int i = index + 1; i < joinTableList.size(); i++) {
                            JoinUtility.handleAutoJoins((SQLJoinTable) joinTableList.get(i), false, joinModel);
                        }
                    } else {
                        //now once table is remove join relationship will be in disorder
                        //so we need to create auto joins again, for this create JoinBuilderSQLUIModelImpl
                        //first get the list of join tables
                        ArrayList<SQLJoinTable> joinTables = new ArrayList<SQLJoinTable>(joinView.getSQLJoinTables());
                        //now remove all objects from join view
                        joinModel.removeAll();
                        //populate join view again with auto joins
                        JoinUtility.handleAutoJoins(joinTables, joinModel);
                    }

                    removeColumnReference(sTable);
                    model.removeObject(sTable);
                    tableAreas.remove(tableArea);
                    this.removeObject(tableArea);

                    break;
                }
            }

            this.setSize(140, this.getMaximumHeight());
            //set size is not calling layoutChildren so need to call it again
            //so that join view can refresh
            this.layoutChildren();
        } catch (BaseException ex) {
            mLogger.errorNoloc(mLoc.t("EDIT188: Error removing source table {0}from join view", sTable.getName()), ex);
            throw ex;
        }
    }

    //remove a table without re adjusting joins in join view
    public void removeJoinTable(SourceTable sTable) throws BaseException {
        SQLJoinView joinView = (SQLJoinView) this.getDataObject();
        SQLJoinTable jTable = joinView.getJoinTable(sTable);

        SQLUIModel model = (SQLUIModel) this.getGraphView().getGraphModel();
        if (model == null) {
            throw new BaseException("can not delete table " + sTable.getName() + " from join view, graph model is null");
        }

        Iterator it = tableAreas.iterator();

        while (it.hasNext()) {
            SQLJoinTableArea tableArea = (SQLJoinTableArea) it.next();
            if (sTable.equals(tableArea.getDataObject())) {

                //remove table from join view this will also remove
                //dangling references to jTable
                joinView.removeObject(jTable);

                removeColumnReference(sTable);
                model.removeObject(sTable);
                tableAreas.remove(tableArea);
                this.removeObject(tableArea);
                break;
            }
        }

        this.setSize(140, this.getMaximumHeight());
        //set size is not calling layoutChildren so need to call it again
        //so that join view can refresh
        this.layoutChildren();
    }

    public boolean isTableColumnMapped(SourceTable sTable) {
        Iterator it = sTable.getColumnList().iterator();
        while (it.hasNext()) {
            SQLDBColumn column = (SQLDBColumn) it.next();
            if (this.isColumnMapped(column)) {
                return true;
            }
        }

        return false;
    }

    private void removeColumnReference(SourceTable sTable) throws BaseException {
        Iterator it = sTable.getColumnList().iterator();
        while (it.hasNext()) {
            SQLDBColumn column = (SQLDBColumn) it.next();
            this.removeColumnReference(column);
        }
    }

    @Override
    public void removeChildNode(IGraphNode child) {
        SourceTable sTable = (SourceTable) child.getDataObject();
        SQLJoinView joinView = (SQLJoinView) this.getDataObject();

        SQLJoinTable jTable = joinView.getJoinTable(sTable);

        try {
            SQLUIModel model = (SQLUIModel) this.getGraphView().getGraphModel();
            if (model == null) {
                throw new BaseException("Cannot delete table " + sTable.getName() + " from join view, graph model is null");
            }

            if (joinView.getSourceTables().size() <= 2) {
                NotifyDescriptor d = new NotifyDescriptor.Message("Cannot remove table " + sTable.getName() + " from join view, join view always requires atleast two tables", NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }

            this.removeTable(jTable.getSourceTable());
        } catch (BaseException ex) {
            mLogger.errorNoloc(mLoc.t("EDIT188: Error removing source table {0}from join view", sTable.getName()), ex);
        }
    }

    /**
     * set the state
     *
     * @param sExpanded whether table is expanded
     */
    @Override
    public void setExpanded(boolean sExpanded) {
        //make this table resizeable only in expanded mode
        this.setResizable(sExpanded);
        Iterator it = tableAreas.iterator();
        while (it.hasNext()) {
            SQLJoinTableArea tableArea = (SQLJoinTableArea) it.next();
            tableArea.setExpandedState(sExpanded);
        }
        super.setExpanded(sExpanded);
    }
}
