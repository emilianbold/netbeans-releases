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
import java.util.Collections;
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

import org.netbeans.modules.model.database.DBTable;
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
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

import com.nwoods.jgo.JGoBrush;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Logger;
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
    
    private static final Color DEFAULT_TITLE_COLOR = new Color(221, 235, 246);
    
    private static final Color DEFAULT_BG_COLOR = new Color(204, 213, 241);
    
    private static final Color DEFAULT_BG_COLOR_DARK = new Color(165, 193, 249);
    
    private static final JGoBrush DEFAULT_TITLE_BRUSH = new GradientBrush(DEFAULT_BG_COLOR_DARK, DEFAULT_BG_COLOR);    
    
    private JMenuItem showSqlItem;
    
    private JMenuItem editItem;
    
    private JMenuItem showDataItem;
    
    private JMenuItem selectColumnsItem;
    
    protected JMenuItem removeItem;
    
    private JoinViewActionListener aListener;
    
    private ArrayList tableAreas = new ArrayList();
    
    private static final int PREFERRED_JOIN_VIEW_WIDTH = 140;
    
    /** Creates a new instance of JoinViewGraphNode */
    public JoinViewGraphNode(SQLJoinView jView) {
        initGUI(jView);
    }
    
    private void initGUI(SQLJoinView jView) {
        this.setSelectable(false);
        this.setResizable(true);
        this.setGrabChildSelection(true);
        
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
        String lblEditJoin = NbBundle.getMessage(JoinViewGraphNode.class, "LBL_edit_joinview");
        editItem = new JMenuItem(lblEditJoin, new ImageIcon(editJoinViewUrl));
        editItem.addActionListener(aListener);
        popUpMenu.add(editItem);
        
        // show sql
        // NOTE: Use SQLBasicTableArea.class as superclass Bundle already contains this
        // resource
        String lblShowSql = NbBundle.getMessage(SQLBasicTableArea.class, "LBL_show_sql");
        showSqlItem = new JMenuItem(lblShowSql, new ImageIcon(showSqlUrl));
        showSqlItem.addActionListener(aListener);
        popUpMenu.add(showSqlItem);
        
        // show join data
        String lblShowData = NbBundle.getMessage(JoinViewGraphNode.class, "LBL_show_data");
        showDataItem = new JMenuItem(lblShowData, new ImageIcon(showJoinDataUrl));
        showDataItem.addActionListener(aListener);
        popUpMenu.add(showDataItem);
        
        // select visible columns
        // NOTE: Use SQLBasicTableArea.class as superclass Bundle already contains this
        // resource
        String lblSelectColumns = NbBundle.getMessage(SQLBasicTableArea.class, "LBL_select_columns");
        selectColumnsItem = new JMenuItem(lblSelectColumns, new ImageIcon(selectColumnsUrl));
        selectColumnsItem.addActionListener(aListener);
        popUpMenu.add(selectColumnsItem);
        
        popUpMenu.addSeparator();
        // NOTE: Use SQLBasicTableArea.class as superclass Bundle already contains this
        // resource
        String lbl = NbBundle.getMessage(SQLBasicTableArea.class, "LBL_remove");
        removeItem = new JMenuItem(lbl, new ImageIcon(removeUrl));
        removeItem.addActionListener(aListener);
        popUpMenu.add(removeItem);
    }
    
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
            } else if(source == showDataItem) {
                showJoinData_ActionPerformed(e);
            }
        }
    }
    
    private void edit_ActionPerformed(ActionEvent e) {
        SQLJoinView joinView = (SQLJoinView) JoinViewGraphNode.this.getDataObject();
        Object[] args = new Object[] { joinView};
        this.getGraphView().execute(ICommand.EDIT_JOINVIEW, args);
    }
    
    private void ShowSql_ActionPerformed(ActionEvent e) {
        SQLJoinView joinView = (SQLJoinView) JoinViewGraphNode.this.getDataObject();
        SQLJoinOperator op = joinView.getRootJoin();
        op.setDisplayName(joinView.getAliasName());
        Object[] args = new Object[] { op};
        this.getGraphView().execute(ICommand.SHOW_SQL_CMD, args);
    }
    
    private void showJoinData_ActionPerformed(ActionEvent e) {
        SQLJoinView joinView = (SQLJoinView) JoinViewGraphNode.this.getDataObject();
        Object[] args = new Object[] {joinView};
        this.getGraphView().execute(ICommand.SHOW_DATA_CMD, args);
    }
    
    private void selectVisibleColumnsActionPerformed(ActionEvent e) {
        List tables = Collections.EMPTY_LIST;
        List columns = Collections.EMPTY_LIST;
        Map tableToModelMap = Collections.EMPTY_MAP;
        
        Iterator tableIter = tableAreas.iterator();
        if (tableIter.hasNext()) {
            tables = new ArrayList(tableAreas.size());
            columns = new ArrayList(tableAreas.size() * 5); // Assume 5 cols / table
            tableToModelMap = new HashMap(tables.size());
            
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
        String dlgLabel = NbBundle.getMessage(SQLBasicTableArea.class, "MSG_dlg_select_columns");
        JLabel lbl = new JLabel(dlgLabel);
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
        String dlgTitle = NbBundle.getMessage(SQLBasicTableArea.class, "TITLE_dlg_select_columns");
        int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), panel, dlgTitle, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        
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
    public List getAllLinks() {
        ArrayList links = new ArrayList();
        
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
                ArrayList columns = new ArrayList();
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
    public void setBoundingRect(int left, int top, int width, int height) {
        if (this.isExpandedState()) {
            super.setBoundingRect(left, top, Math.max(width, 100), this.getMaximumHeight());
        } else {
            super.setBoundingRect(left, top, Math.max(width, 100), height);
        }
    }
    
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
        tableArea.setSelectable(false);
        tableArea.setResizable(false);
        tableArea.setGrabChildSelection(true);
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
            
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    "You may lose some user defined conditions in some joins, Do you really want to remove the table?", NotifyDescriptor.WARNING_MESSAGE);
            
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
                        //so we need to create auto joins again, for this create
                        // JoinBuilderSQLUIModelImpl
                        //first get the list of join tables
                        ArrayList joinTables = new ArrayList(joinView.getSQLJoinTables());
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
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "removeTable", "Error removing source table " + sTable.getName() + " from join view",
                    ex);
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
                NotifyDescriptor d = new NotifyDescriptor.Message("Cannot remove table " + sTable.getName()
                + " from join view, join view always requires atleast two tables", NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }
            
            this.removeTable(jTable.getSourceTable());
            
        } catch (BaseException ex) {
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "removeChildNode", "Error removing source table " + sTable.getName()
            + " from join view", ex);
        }
    }
    
    /**
     * set the state
     *
     * @param sExpanded whether table is expanded
     */
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