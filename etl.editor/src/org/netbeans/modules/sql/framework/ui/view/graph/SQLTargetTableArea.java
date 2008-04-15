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
package org.netbeans.modules.sql.framework.ui.view.graph;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.exception.DBSQLException;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.graph.ICommand;
import org.netbeans.modules.sql.framework.ui.graph.impl.GradientBrush;
import org.openide.util.Exceptions;


import com.nwoods.jgo.JGoBrush;
import java.awt.event.InputEvent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBMetaDataFactory;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.impl.SQLDBModelImpl;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.model.visitors.SQLDBSynchronizationVisitor;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.BasicTopView;
import org.openide.awt.StatusDisplayer;
import org.openide.windows.WindowManager;

/**
 * @author Ritesh Adval
 */
public class SQLTargetTableArea extends SQLBasicTableArea {

    private static URL targetTableImgUrl = SQLTargetTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/TargetTable.png");
    private static URL showDataUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/SourceTable.png");
    private static URL showSqlUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/Show_Sql.png");
    private static URL propertiesUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/properties.png");
    private static URL showRejectionDataImgUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/showRejectedData.png");
    private static URL synchroniseImgUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/refresh.png");
    private static URL targetTableConditionImgUrl = SQLBasicTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/targetTableCondition.png");
    private static URL dataFilterImgUrl = SQLSourceTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/filter16.gif");
    private static URL remountImgUrl = SQLTargetTableArea.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/redo.png");
    private static final Color DEFAULT_BG_COLOR = new Color(219, 207, 219);//(204, 213, 241);
    private static final Color DEFAULT_BG_COLOR_DARK = new Color(221, 235, 246);//(165, 193, 249);
    private static final JGoBrush DEFAULT_TITLE_BRUSH = new GradientBrush(DEFAULT_BG_COLOR, DEFAULT_BG_COLOR_DARK);
    private JMenuItem showSqlItem;
    private JMenuItem showDataItem;
    private JMenuItem showRejectionDataItem;
    private JMenuItem editJoinConditionItem;
    private JMenuItem editFilterConditionItem;
    private JMenuItem propertiesItem;
    private JMenuItem synchroniseItem;
    private JMenuItem remountItem;
    private static transient final Logger mLogger = Logger.getLogger(SQLTargetTableArea.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    
    /**
     * Creates a new instance of SQLTargetTableArea
     */
    public SQLTargetTableArea() {
        super();
    }

    /**
     * Creates a new instance of SQLTargetTableArea
     *
     * @param tgttable
     *
     */
    public SQLTargetTableArea(TargetTable tgttable) {
        super();
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea#initializePopUpMenu()
     */
    protected void initializePopUpMenu() {
        try {
            ActionListener aListener = new TableActionListener();
            // Show SQL
            String nbBundle1 = mLoc.t("BUND365: Show SQL");
            String lblShowSql =  nbBundle1.substring(15);
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

            // Show rejection data
            // TODO: Add listener framework to enable/disable this action depending on whether
            // a validation condition exists
            String nbBundle3 = mLoc.t("BUND461: Show Rejected Data...");
            String lblRjtShowData = nbBundle3.substring(15);
            showRejectionDataItem = new JMenuItem(lblRjtShowData, new ImageIcon(showRejectionDataImgUrl));
            showRejectionDataItem.setMnemonic(nbBundle3.substring(15).charAt(0));
            showRejectionDataItem.addActionListener(aListener);
            popUpMenu.add(showRejectionDataItem);


            addSelectVisibleColumnsPopUpMenu(aListener);
            String nbBundle4 = mLoc.t("BUND024: Refresh Metadata");
            synchroniseItem = new JMenuItem(nbBundle4.substring(15), new ImageIcon(synchroniseImgUrl));
            synchroniseItem.setMnemonic(nbBundle4.substring(15).charAt(9));
            synchroniseItem.addActionListener(aListener);
            popUpMenu.add(synchroniseItem);

            String nbBundle5 = mLoc.t("BUND027: Remount");
            String lblRemount = nbBundle5.substring(15);
            remountItem = new JMenuItem(lblRemount, new ImageIcon(remountImgUrl));
            remountItem.setMnemonic(nbBundle5.substring(15).charAt(0));
            remountItem.addActionListener(aListener);
            SQLObject tbl = (SQLObject) SQLTargetTableArea.this.getDataObject();
            SQLDBModelImpl impl = (SQLDBModelImpl) tbl.getParentObject();
            if (impl.getETLDBConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) ||
                impl.getETLDBConnectionDefinition().getDBType().equalsIgnoreCase("Internal")) {
                popUpMenu.add(remountItem);
            }
            popUpMenu.addSeparator();

            // TODO: show join condition only if source table exist (Delete, Static Insert/Update does not require Join Condition)
            // Target Join Condition
            String nbBundle6 = mLoc.t("BUND462: Target Join Condition...");
            String lblTargetCondition = nbBundle6.substring(15);
            editJoinConditionItem = new JMenuItem(lblTargetCondition, new ImageIcon(targetTableConditionImgUrl));
            editJoinConditionItem.setMnemonic(nbBundle6.substring(15).charAt(0));
            editJoinConditionItem.addActionListener(aListener);
            popUpMenu.add(editJoinConditionItem);

            // Target Filter Condition
            String nbBundle7 = mLoc.t("BUND463: Outer Filter Condition...");
            String lblTargetFilterCondition = nbBundle7.substring(15);
            editFilterConditionItem = new JMenuItem(lblTargetFilterCondition, new ImageIcon(dataFilterImgUrl));
            editFilterConditionItem.setMnemonic(nbBundle7.substring(15).charAt(0));
            editFilterConditionItem.addActionListener(aListener);
            popUpMenu.add(editFilterConditionItem);
            popUpMenu.addSeparator();

            // Remove
            addRemovePopUpMenu(aListener);

            // Properties
            popUpMenu.addSeparator();
            String nbBundle8 = mLoc.t("BUND443: Properties");
            String lblProps =  nbBundle8.substring(15);
            propertiesItem = new JMenuItem(lblProps, new ImageIcon(propertiesUrl));
            propertiesItem.setAccelerator(KeyStroke.getKeyStroke('7',InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_MASK));
            propertiesItem.addActionListener(aListener);
            popUpMenu.add(propertiesItem);
        } catch (BaseException ex) {
            Exceptions.printStackTrace(ex);
        }
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
            } else if (source == remountItem) {
                Remount_ActionPerformed(e);
            } else if (source == showDataItem) {
                ShowData_ActionPerformed(e);
            } else if (source == showRejectionDataItem) {
                ShowRejectionData_ActionPerformed(e);
            } else if (source == editJoinConditionItem) {
                ShowTargetJoinCondition_ActionPerformed(e);
            } else if (source == editFilterConditionItem) {
                ShowTargetFilterCondition_ActionPerformed(e);
            } else if (source == synchroniseItem) {
                synchroniseItem_ActionPerformed(e);
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

    private void ShowSql_ActionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLTargetTableArea.this.getDataObject();
        Object[] args = new Object[]{sqlObject};
        this.getGraphView().execute(ICommand.SHOW_SQL_CMD, args);
    }

    private void Remount_ActionPerformed(ActionEvent e) {
        try {
            SQLObject sqlObject = (SQLObject) SQLTargetTableArea.this.getDataObject();
            SQLObjectUtil.dropTable(table, (SQLDBModel) table.getParent());
            SQLObjectUtil.createTable(table, (SQLDBModel) table.getParent());
            SQLObjectUtil.setOrgProperties(table);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            StatusDisplayer.getDefault().setStatusText("Unable to remount :" + ex.getMessage());
        }
    }

    private void ShowData_ActionPerformed(ActionEvent e) {
        SQLObject tbl = (SQLObject) SQLTargetTableArea.this.getDataObject();
        this.getGraphView().execute(ICommand.SHOW_DATA_CMD, new Object[]{tbl});
    }

    private void ShowRejectionData_ActionPerformed(ActionEvent e) {
        SQLObject tbl = (SQLObject) SQLTargetTableArea.this.getDataObject();
        this.getGraphView().execute(ICommand.SHOW_REJECTION_DATA_CMD, new Object[]{tbl});
    }

    private void ShowTargetJoinCondition_ActionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLTargetTableArea.this.getDataObject();
        Object[] args = new Object[]{SQLTargetTableArea.this, sqlObject};
        this.getGraphView().execute(ICommand.SHOW_TARGET_JOIN_CONDITION_CMD, args);
    }

    private void ShowTargetFilterCondition_ActionPerformed(ActionEvent e) {
        SQLObject sqlObject = (SQLObject) SQLTargetTableArea.this.getDataObject();
        Object[] args = new Object[]{SQLTargetTableArea.this, sqlObject};
        this.getGraphView().execute(ICommand.SHOW_TARGET_FILTER_CONDITION_CMD, args);
    }

    private void synchroniseItem_ActionPerformed(ActionEvent e) {
        IGraphView gView = this.getGraphView();

        String nbBundle9 = mLoc.t("BUND026: If columns are deleted or renamed you may lose existing mappings.");
        String dlgMsg = nbBundle9.substring(15);
        String nbBundle10 = mLoc.t("BUND024: Refresh Metadata");
        String dlgTitle = nbBundle10.substring(15);
        int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), dlgMsg, dlgTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        if (JOptionPane.OK_OPTION == response) {
            try {

                SQLDBTable tbl = (SQLDBTable) SQLTargetTableArea.this.getDataObject();
                SQLDBSynchronizationVisitor visitView = new SQLDBSynchronizationVisitor();

                SQLTableArea tableArea1 = (SQLTableArea) this.getTableArea();
                MetaTableModel model = (MetaTableModel) tableArea1.getModel();
                visitView.mergeCollabTableWithDatabaseTable(tbl, model);
                if (!visitView.infoList.isEmpty()) {
                    tableArea1.layoutChildren();
                    tableArea1.setHeight(tableArea1.getMaximumHeight());
                    // Mark collab as needing to be persisted.
                    Object graphModel = getGraphView().getGraphModel();
                    if (graphModel instanceof CollabSQLUIModel) {
                        ((CollabSQLUIModel) graphModel).setDirty(true);
                    }
                }
                BasicTopView gvMgr = (BasicTopView) gView.getGraphViewContainer();
                gvMgr.showRefreshMetadataInfo(visitView.infoList);
            } catch (DBSQLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
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
     * @return 
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
}
