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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.impl.GradientBrush;
import org.openide.util.NbBundle;

import com.nwoods.jgo.JGoBrush;

/**
 * @author Ritesh Adval
 */
public class SQLTargetTableArea extends SQLBasicTableArea {

    private static URL targetTableImgUrl = SQLTargetTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/TargetTable.png");

    private static URL showDataUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/SourceTable.png");

    private static URL showSqlUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/Show_Sql.png");

    private static URL propertiesUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/properties.png");

    private static URL showRejectionDataImgUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/showRejectedData.png");

    private static URL targetTableConditionImgUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/targetTableCondition.png");
    private static URL dataFilterImgUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/filter16.gif");
    
    private static final Color DEFAULT_BG_COLOR = new Color(234, 236, 240);
    
    private static final Color DEFAULT_BG_COLOR_DARK = new Color(183, 190, 204);
    
    private static final JGoBrush DEFAULT_TITLE_BRUSH = new GradientBrush(DEFAULT_BG_COLOR, DEFAULT_BG_COLOR_DARK);  

    private JMenuItem showSqlItem;
    private JMenuItem showDataItem;
    private JMenuItem showRejectionDataItem;
    private JMenuItem editJoinConditionItem;
    private JMenuItem editFilterConditionItem;
    private JMenuItem propertiesItem;

    /**
     * Creates a new instance of SQLTargetTableArea
     */
    public SQLTargetTableArea() {
        super();
    }

    /**
     * Creates a new instance of SQLTargetTableArea
     * 
     * @param table the table to render
     */
    public SQLTargetTableArea(SQLDBTable table) {
        super(table);
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea#initializePopUpMenu()
     */
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

        // Show rejection data
        // TODO: Add listener framework to enable/disable this action depending on whether
        // a validation condition exists
        String lblRjtShowData = NbBundle.getMessage(SQLBasicTableArea.class, "LBL_show_rejection_data");
        showRejectionDataItem = new JMenuItem(lblRjtShowData, new ImageIcon(showRejectionDataImgUrl));

        showRejectionDataItem.addActionListener(aListener);
        popUpMenu.add(showRejectionDataItem);

        // Select Columns
        addSelectVisibleColumnsPopUpMenu(aListener);
        popUpMenu.addSeparator();

        // TODO: show join condition only if source table exist (Delete, Static Insert/Update does not require Join Condition)
        // Target Join Condition
        String lblTargetCondition = NbBundle.getMessage(SQLBasicTableArea.class, "LBL_edit_target_join_condition");
        editJoinConditionItem = new JMenuItem(lblTargetCondition, new ImageIcon(targetTableConditionImgUrl));
        editJoinConditionItem.addActionListener(aListener);
        popUpMenu.add(editJoinConditionItem);
                
        // Target Filter Condition
        String lblTargetFilterCondition = NbBundle.getMessage(SQLBasicTableArea.class, "LBL_edit_target_filter_condition");
        editFilterConditionItem = new JMenuItem(lblTargetFilterCondition, new ImageIcon(dataFilterImgUrl));
        editFilterConditionItem.addActionListener(aListener);
        popUpMenu.add(editFilterConditionItem);
        popUpMenu.addSeparator();

        // Remove
        addRemovePopUpMenu(aListener);

        // Properties
        popUpMenu.addSeparator();
        String lblProps = NbBundle.getMessage(SQLBasicTableArea.class, "LBL_properties");
        propertiesItem = new JMenuItem(lblProps, new ImageIcon(propertiesUrl));
        propertiesItem.addActionListener(aListener);
        popUpMenu.add(propertiesItem);
    }

    Icon createIcon() {
        return new ImageIcon(targetTableImgUrl);
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
            } else if (source == showRejectionDataItem) {
                ShowRejectionData_ActionPerformed(e);
            } else if (source == editJoinConditionItem) {
                ShowTargetJoinCondition_ActionPerformed(e);
            }else if (source == editFilterConditionItem) {
                ShowTargetFilterCondition_ActionPerformed(e);
            }  else {
                handleCommonActions(e);
            }
        }
    }

    private void Properties_ActionPerformed(ActionEvent e) {
        Object[] args = new Object[] { SQLTargetTableArea.this, Boolean.TRUE};
        this.getGraphView().execute(ICommand.SHOW_PROPERTY_CMD, args);
    }

    private void ShowSql_ActionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLTargetTableArea.this.getDataObject();
        Object[] args = new Object[] { sqlObject};
        this.getGraphView().execute(ICommand.SHOW_SQL_CMD, args);
    }

    private void ShowData_ActionPerformed(ActionEvent e) {
        SQLObject tbl = (SQLObject) SQLTargetTableArea.this.getDataObject();
        this.getGraphView().execute(ICommand.SHOW_DATA_CMD, new Object[] { tbl});
    }

    private void ShowRejectionData_ActionPerformed(ActionEvent e) {
        SQLObject tbl = (SQLObject) SQLTargetTableArea.this.getDataObject();
        this.getGraphView().execute(ICommand.SHOW_REJECTION_DATA_CMD, new Object[] { tbl});
    }

    private void ShowTargetJoinCondition_ActionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLTargetTableArea.this.getDataObject();
        Object[] args = new Object[] { SQLTargetTableArea.this, sqlObject};
        this.getGraphView().execute(ICommand.SHOW_TARGET_JOIN_CONDITION_CMD, args);
    }
    
    private void ShowTargetFilterCondition_ActionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLTargetTableArea.this.getDataObject();
        Object[] args = new Object[] { SQLTargetTableArea.this, sqlObject};
        this.getGraphView().execute(ICommand.SHOW_TARGET_FILTER_CONDITION_CMD, args);
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

    public void setConditionIcons() {
        TargetTable tbl = (TargetTable) this.getDataObject();
        if (tbl != null) {
        	SQLCondition c1 = tbl.getJoinCondition();
            setTableConditionIcons(c1);
            
            SQLCondition c2 = tbl.getFilterCondition();
            setTableConditionIcons(c2);
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
}
