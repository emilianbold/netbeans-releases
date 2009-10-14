/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.etl.ui.view.wizards;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.impl.AbstractDBTable;



/**
 * This class represents table for meta data. This holds a JTable for showing table meta
 * data.
 * 
 * @author Sanjeeth Duvuru
 * @version $Revision$
 */
public class ETLCollaborationWizardTablePanel extends JPanel {

    private static transient final Logger mLogger = Logger.getLogger(ETLCollaborationWizardTablePanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    
    class MetaTableComponent extends JTable {
        public MetaTableComponent() {
            setDefaultRenderer(AbstractDBTable.class, new MyTableModelCellRenderer());
            setDefaultRenderer(Boolean.class, new MyBooleanRenderer());

            JTableHeader header = this.getTableHeader();
            header.setReorderingAllowed(false);
            header.setResizingAllowed(false);
        }
    }

    static class MyBooleanRenderer extends JCheckBox implements TableCellRenderer {
        protected static Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        private JPanel myPanel;
        /**
         * Creates a default MyBooleanRenderer.
         */
        public MyBooleanRenderer() {
            super();
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            myPanel = new JPanel();
            myPanel.setLayout(new BorderLayout());
            myPanel.add(this, BorderLayout.CENTER);
            myPanel.setOpaque(true);
            myPanel.setBorder(noFocusBorder);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            RowDataWrapper rowDW = ((MyTableModel) table.getModel()).getRowDataWrapper(row);

            if (rowDW != null && !rowDW.isEditable().booleanValue()) {
                setEnabled(false);
                setFocusable(false);

                setBackground(Color.LIGHT_GRAY);

                Object obj = rowDW.getTable();
                    String nbBundle1 = mLoc.t("BUND063: Table {0} already exists as target in this collaboration.",rowDW.getTable());
                    String nbBundle2 = mLoc.t("BUND064: Table {0} already exists as source table in this collaboration.",rowDW.getTable());
                    String nbBundle3 = mLoc.t("BUND064: Table {0} already exists as source table in this collaboration.", rowDW.getTable());
                    String nbBundle4 = mLoc.t("BUND066: Table {0} already exists as target table in this collaboration.",rowDW.getTable());
                if (obj instanceof TargetTable) {
                    TargetTable tt = (TargetTable) obj;
                    if (tt.isSelected()) {
                        setToolTipText(nbBundle1.substring(15));
                    } else {
                        setToolTipText(nbBundle2.substring(15));
                    }
                }

                if (obj instanceof SourceTable) {
                    SourceTable st = (SourceTable) obj;
                    if (!st.isSelected()) {
                        setToolTipText(nbBundle4.substring(15));
                    }
                }
                myPanel.setBorder(noFocusBorder);
                myPanel.setBackground(Color.LIGHT_GRAY);
            } else {
                if (isSelected) {
                    setForeground(table.getSelectionForeground());
                    setBackground(table.getSelectionBackground());

                    myPanel.setForeground(table.getSelectionForeground());
                    myPanel.setBackground(table.getSelectionBackground());
                } else {
                    setForeground(table.getForeground());
                    setBackground(table.getBackground());

                    myPanel.setForeground(table.getForeground());
                    myPanel.setBackground(table.getBackground());
                }

                if (hasFocus) { // NOI18N this scope block
                    myPanel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                    if (table.isCellEditable(row, column)) {
                        setForeground(UIManager.getColor("Table.focusCellForeground"));
                        setBackground(UIManager.getColor("Table.focusCellBackground"));
                    }
                    myPanel.setForeground(UIManager.getColor("Table.focusCellForeground"));
                    myPanel.setBackground(UIManager.getColor("Table.focusCellBackground"));
                } else {
                    myPanel.setBorder(noFocusBorder);
                }

                setEnabled(true);
                setFocusable(true);
                setToolTipText("");
            }

            setSelected((value != null && ((Boolean) value).booleanValue()));
            return myPanel;
        }

        /**
         * Overrides <code>JComponent.setBackground</code> to assign the
         * unselected-background color to the specified color.
         * 
         * @param c set the background color to this value
         */
        public void setBackground(Color c) {
            super.setBackground(c);
        }

        /**
         * Overrides <code>JComponent.setForeground</code> to assign the
         * unselected-foreground color to the specified color.
         * 
         * @param c set the foreground color to this value
         */
        public void setForeground(Color c) {
            super.setForeground(c);
        }
    }

    class MyTableModel extends AbstractTableModel {
        private String[] columnNames = { "Select", "Table Name"};

        private List rowList;

        public MyTableModel(List testList) {
            rowList = new ArrayList();

            for (int i = 0; i < testList.size(); i++) {
                RowDataWrapper rowData = new RowDataWrapper((SQLDBTable) testList.get(i));
                rowList.add(rowData);
            }
        }

        /*
         * JTable uses this method to determine the default renderer/ editor for each
         * cell. If we didn't implement this method, then the last column would contain
         * text ("true"/"false"), rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public int getRowCount() {
            return rowList.size();
        }

        public RowDataWrapper getRowDataWrapper(int row) {
            if (row < rowList.size()) {
                return (RowDataWrapper) rowList.get(row);
            }

            return null;
        }

        public ArrayList getTables() {
            ArrayList tableList = new ArrayList();

            for (int i = 0; i < rowList.size(); i++) {
                RowDataWrapper rowData = (RowDataWrapper) rowList.get(i);
                tableList.add(rowData.getTable());
            }

            return tableList;
        }

        public Object getValueAt(int row, int col) {
            RowDataWrapper rowData = (RowDataWrapper) rowList.get(row);
            switch (col) {
                case 0:
                    return rowData.isSelected();
                case 1:
                    return rowData.getTable();

            }

            return String.valueOf(col + "?");
        }

        /*
         * Don't need to implement this method unless your table's editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            Object rowObj = rowList.get(row);
            return (rowObj != null) ? ((RowDataWrapper) rowObj).isEditable().booleanValue() && (col == 0) : false;
        }

        public void setCellEditable(int row, int col, boolean flag) {
            Object rowObj = rowList.get(row);
            if (rowObj != null) {
                ((RowDataWrapper) rowObj).setEditable(flag ? Boolean.TRUE : Boolean.FALSE);
            }
        }

        /*
         * Don't need to implement this method unless your table's data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            RowDataWrapper rowData = (RowDataWrapper) rowList.get(row);
            switch (col) {
                case 0:
                    rowData.setSelected((Boolean) value);
                    fireTableRowsUpdated(row, row);
                    break;
            }
        }
    }

    static class MyTableModelCellRenderer extends DefaultTableCellRenderer {
        protected static Border noFocusBorder1 = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            MyTableModel model = (MyTableModel) table.getModel();

            RowDataWrapper rowDW = model.getRowDataWrapper(row);
            if (rowDW != null && !rowDW.isEditable().booleanValue()) {
                renderer.setEnabled(false);
                renderer.setBackground(Color.lightGray);

                Object obj = rowDW.getTable();

                    String nbBundle1 = mLoc.t("BUND063: Table {0} already exists as target in this collaboration.",rowDW.getTable());
                    String nbBundle2 = mLoc.t("BUND064: Table {0} already exists as source table in this collaboration.",rowDW.getTable());
                    String nbBundle3 = mLoc.t("BUND064: Table {0} already exists as source table in this collaboration.", rowDW.getTable());
                    String nbBundle4 = mLoc.t("BUND066: Table {0} already exists as target table in this collaboration.",rowDW.getTable());
                if (obj instanceof TargetTable) {
                    TargetTable tt = (TargetTable) obj;
                    if (tt.isSelected()) {
                        setToolTipText(nbBundle1.substring(15));
                    } else {
                        renderer.setToolTipText(nbBundle2.substring(15));
                    }
                }

                if (obj instanceof SourceTable) {
                    SourceTable st = (SourceTable) obj;
                    if (!st.isSelected()) {
                        renderer.setToolTipText(nbBundle4.substring(15));
                    }
                }

                renderer.setBorder(noFocusBorder1);
                renderer.setFocusable(false);
            } else {
                if (isSelected) {
                    renderer.setForeground(table.getSelectionForeground());
                    renderer.setBackground(table.getSelectionBackground());
                } else {
                    renderer.setForeground(table.getForeground());
                    renderer.setBackground(table.getBackground());
                }

                renderer.setToolTipText("");
                renderer.setEnabled(true);
                renderer.setFocusable(true);
            }

            return renderer;
        }
    }

    class RowDataWrapper {
        private SQLDBTable table;

        public RowDataWrapper(SQLDBTable mTable) {
            table = mTable;
        }

        public Object getTable() {
            return table;
        }

        public Boolean isEditable() {
            return table.isEditable() ? Boolean.TRUE : Boolean.FALSE;
        }

        public Boolean isSelected() {
            return table.isSelected() ? Boolean.TRUE : Boolean.FALSE;
        }

        public void setEditable(Boolean isEditable) {
            table.setEditable(isEditable.booleanValue());
        }

        public void setSelected(Boolean isSelected) {
            table.setSelected(isSelected.booleanValue());
        }
    }

    /* font selection for column data in table body */
    private static final Font FONT_TABLE_COLUMNS = new Font("Dialog", Font.PLAIN, 10);

    /* font selection for column headers in table body */
    private static final Font FONT_TABLE_HEADER = new Font("Dialog", Font.BOLD, 10);

    private JPanel headerPnl;

    /* table to display meta data */
    private MetaTableComponent metaDataTable;

    /* scrollpane for columns JTable */
    private JScrollPane tableScroll;

    /** Creates a default instance of ETLCollaborationWizardTablePanel */
    public ETLCollaborationWizardTablePanel() {
    }

    /**
     * Creates a new instance of ETLCollaborationWizardTablePanel to render the selection
     * of tables participating in an ETL collaboration.
     * 
     * @param testList List of tables
     */
    public ETLCollaborationWizardTablePanel(List testList) {
        setOpaque(false);

        JPanel p = new JPanel();

        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);

        headerPnl = new JPanel();
        headerPnl.setLayout(new BorderLayout());
        headerPnl.setOpaque(false);
        headerPnl.add(p, BorderLayout.NORTH);

        addTable(testList);
    }

    /**
     * Gets associated JTable.
     * 
     * @return JTable
     */
    public JTable getTable() {
        return this.metaDataTable;
    }

    /**
     * Gets list of selected tables.
     * 
     * @return List of selected tables
     */
    public List getTables() {
        MyTableModel tableModel = (MyTableModel) metaDataTable.getModel();
        return tableModel.getTables();
    }

    /**
     * Paints this component
     * 
     * @param g graphics context
     */
    public void paint(Graphics g) {
        super.paint(g);
    }

    /**
     * Populates selected tables using items contained in the given List.
     * 
     * @param tableNameList List of tables to use in repopulating set of selected tables
     */
    public void resetTable(List tableNameList) {
        MyTableModel myMod = new MyTableModel(tableNameList);
        metaDataTable.setModel(myMod);

        //set checkbox column size
        TableColumn column = metaDataTable.getColumnModel().getColumn(0);
        column.setResizable(false);
        column.setMinWidth(40);
        column.setPreferredWidth(40);
        column.setMaxWidth(80);

    }

    private void addTable(List testList) {
        metaDataTable = new MetaTableComponent();

        metaDataTable.setFont(FONT_TABLE_COLUMNS);
        metaDataTable.getTableHeader().setFont(FONT_TABLE_HEADER);

        MyTableModel myModel = new MyTableModel(testList);
        metaDataTable.setModel(myModel);

        setLayout(new BorderLayout());
        add(headerPnl, BorderLayout.NORTH);

        //set checkbox column size
        TableColumn column = metaDataTable.getColumnModel().getColumn(0);
        column.setResizable(false);
        column.setMinWidth(40);
        column.setPreferredWidth(40);
        column.setMaxWidth(80);

        tableScroll = new JScrollPane(metaDataTable);

        javax.swing.border.Border inside = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3),
            BorderFactory.createLineBorder(Color.GRAY));

        tableScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), inside));
        add(tableScroll, BorderLayout.CENTER);
    }
}
