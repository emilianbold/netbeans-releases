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
import org.openide.util.NbBundle;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoView;

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
    
    private static final Color DEFAULT_BG_COLOR = new Color(204, 213, 241);
    
    private static final Color DEFAULT_BG_COLOR_DARK = new Color(165, 193, 249); // new Color(249, 224, 127);
    
    private static final JGoBrush DEFAULT_TITLE_BRUSH = new GradientBrush(DEFAULT_BG_COLOR_DARK, DEFAULT_BG_COLOR);
    
    private JMenuItem showSqlItem;
    
    private JMenuItem showDataItem;
    
    private JMenuItem propertiesItem;
    
    private JMenuItem autoMapItem;
    
    private JMenuItem dataValidationMapItem;
    
    private JMenuItem dataFilterMapItem;
    
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
    public SQLSourceTableArea(SQLDBTable table) {
        super(table);
    }
    
    protected void initializePopUpMenu() {
        ActionListener aListener = new TableActionListener();
        // Show SQL
        String lblShowSql = NbBundle.getMessage(SQLBasicTableArea.class, "LBL_show_sql");
        showSqlItem = new JMenuItem(lblShowSql, new ImageIcon(showSqlUrl));
        showSqlItem.addActionListener(aListener);
        popUpMenu.add(showSqlItem);
        
        // Show data
        String lblShowData = NbBundle.getMessage(SQLBasicTableArea.class, "LBL_show_data");
        showDataItem = new JMenuItem(lblShowData, new ImageIcon(showDataUrl));
        showDataItem.addActionListener(aListener);
        popUpMenu.add(showDataItem);
        
        // Fit to size
        addSelectVisibleColumnsPopUpMenu(aListener);
        
        // Define data filtering action
        popUpMenu.addSeparator();
        dataFilterMapItem = new JMenuItem("Extraction Condition...", new ImageIcon(dataFilterImgUrl));
        dataFilterMapItem.addActionListener(aListener);
        popUpMenu.add(dataFilterMapItem);
        
        // Define data validation action
        dataValidationMapItem = new JMenuItem("Data Validation...", new ImageIcon(defineValidationImgUrl));
        dataValidationMapItem.addActionListener(aListener);
        popUpMenu.add(dataValidationMapItem);
        
        // Remove
        popUpMenu.addSeparator();
        addRemovePopUpMenu(aListener);
        
        // Show properties
        popUpMenu.addSeparator();
        String lblProps = NbBundle.getMessage(SQLBasicTableArea.class, "LBL_properties");
        propertiesItem = new JMenuItem(lblProps, new ImageIcon(propertiesUrl));
        propertiesItem.addActionListener(aListener);
        popUpMenu.add(propertiesItem);
        
        // Auto map action
        autoMapItem = new JMenuItem("Auto Map", new ImageIcon(autoMapImgUrl));
        autoMapItem.addActionListener(aListener);
        
        popUpMenu.addSeparator();
        popUpMenu.add(autoMapItem);
        
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
            } else if (source == showDataItem) {
                ShowData_ActionPerformed(e);
            } else if (source == autoMapItem) {
                performAutoMap(e);
            } else if (source == dataValidationMapItem) {
                DataValidation_ActionPerformed(e);
            } else if (source == dataFilterMapItem) {
                showDataFilter_ActionPerformed(e);
            } else {
                handleCommonActions(e);
            }
        }
    }
    
    private void Properties_ActionPerformed(ActionEvent e) {
        Object[] args = new Object[] { SQLSourceTableArea.this, Boolean.TRUE};
        this.getGraphView().execute(ICommand.SHOW_PROPERTY_CMD, args);
    }
    
    private void ShowSql_ActionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLSourceTableArea.this.getDataObject();
        Object[] args = new Object[] { sqlObject};
        this.getGraphView().execute(ICommand.SHOW_SQL_CMD, args);
    }
    
    private void ShowData_ActionPerformed(ActionEvent e) {
        SQLObject tbl = (SQLObject) SQLSourceTableArea.this.getDataObject();
        this.getGraphView().execute(ICommand.SHOW_DATA_CMD, new Object[] { tbl});
    }
    
    private void showDataFilter_ActionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLSourceTableArea.this.getDataObject();
        Object[] args = new Object[] { SQLSourceTableArea.this, sqlObject};
        this.getGraphView().execute(ICommand.DATA_EXTRACTION, args);
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
        Object[] args = new Object[] { SQLSourceTableArea.this, sqlObject};
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
     * @see org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea#getDefaultBackgroundColor()
     */
    protected Color getDefaultBackgroundColor() {
        return DEFAULT_BG_COLOR;
    }
    
    private void updateActions() {
        try{
            this.getView().updateUI();
            ETLDataObject etlDataObject = DataObjectProvider.getProvider().getActiveDataObject();
            ETLEditorSupport editor = etlDataObject.getETLEditorSupport();
            editor.synchDocument();            
        } catch(Exception ex){
            //ignore
        }
    }
}

