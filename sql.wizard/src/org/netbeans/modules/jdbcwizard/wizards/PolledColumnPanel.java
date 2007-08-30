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

package org.netbeans.modules.jdbcwizard.wizards;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.JTableHeader;
import org.openide.util.NbBundle;

import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBColumn;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.impl.DBColumnImpl;

/**
 * @author Administrator
 */
public class PolledColumnPanel extends JPanel implements JDBCTableColumnDisplayable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    class MetaTColumnComponent extends JTable {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public MetaTColumnComponent() {
            // Need to revisit whether should use abstract model here??
            this.setDefaultRenderer(DBColumnImpl.class, new MyTColumnModelCellRenderer());
            this.setDefaultRenderer(Boolean.class, new MyBooleanRenderer());
            final JTableHeader header = this.getTableHeader();
            header.setReorderingAllowed(false);
            header.setResizingAllowed(false);
        }
    }

    static class MyBooleanRenderer extends JCheckBox implements TableCellRenderer {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        protected static Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        private JPanel myPanel;

        /**
         * Creates a default MyBooleanRenderer.
         */
        public MyBooleanRenderer() {
            super();
            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            this.myPanel = new JPanel();
            this.myPanel.setLayout(new BorderLayout());
            this.myPanel.add(this, BorderLayout.CENTER);
            this.setEnabled(true);
            this.myPanel.setOpaque(true);
            this.myPanel.setBorder(MyBooleanRenderer.noFocusBorder);
        }

        /**
         * 
         */
        public Component getTableCellRendererComponent(final JTable table,
                                                       final Object value,
                                                       final boolean isSelected,
                                                       final boolean hasFocus,
                                                       final int row,
                                                       final int column) {
            final RowDataWrapper rowDW = ((MyTColumnModel) table.getModel()).getRowDataWrapper(row);
            if (rowDW != null && !rowDW.isEditable().booleanValue()) {
                this.setEnabled(false);
                this.setFocusable(false);
                this.setBackground(Color.LIGHT_GRAY);
                final Object obj = rowDW.getTColumn();
                if (obj instanceof DBColumn) {
                    final DBColumn st = (DBColumn) obj; // SourceTable modified to
                    // DBColumn
                    if (!st.isPollSelected()) {
                        this.setToolTipText(NbBundle.getMessage(JDBCWizardTablePanel.class,
                                "TOOLTIP_source_table_disabled_unselected", rowDW.getTColumn()));
                    }
                }
                this.myPanel.setBorder(MyBooleanRenderer.noFocusBorder);
                this.myPanel.setBackground(Color.LIGHT_GRAY);
            } else {
                if (isSelected) {
                    this.setForeground(table.getSelectionForeground());
                    this.setBackground(table.getSelectionBackground());
                    this.myPanel.setForeground(table.getSelectionForeground());
                    this.myPanel.setBackground(table.getSelectionBackground());
                } else {
                    this.setForeground(table.getForeground());
                    this.setBackground(table.getBackground());
                    this.myPanel.setForeground(table.getForeground());
                    this.myPanel.setBackground(table.getBackground());
                }
                if (hasFocus) { // NOI18N this scope block
                    this.myPanel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                    if (table.isCellEditable(row, column)) {
                        this.setForeground(UIManager.getColor("Table.focusCellForeground"));
                        this.setBackground(UIManager.getColor("Table.focusCellBackground"));
                    }
                    this.myPanel.setForeground(UIManager.getColor("Table.focusCellForeground"));
                    this.myPanel.setBackground(UIManager.getColor("Table.focusCellBackground"));
                } else {
                    this.myPanel.setBorder(MyBooleanRenderer.noFocusBorder);
                }
                this.setFocusable(true);
                this.setSelected(true);
                this.setToolTipText("");
            }
            this.setSelected((value != null && ((Boolean) value).booleanValue()));
            return this.myPanel;
        }

        /**
         * Overrides <code>JComponent.setBackground</code> to assign the unselected-background
         * color to the specified color.
         * 
         * @param c set the background color to this value
         */
        public void setBackground(final Color c) {
            super.setBackground(c);
        }

        /**
         * Overrides <code>JComponent.setForeground</code> to assign the unselected-foreground
         * color to the specified color.
         * 
         * @param c set the foreground color to this value
         */
        public void setForeground(final Color c) {
            super.setForeground(c);
        }
    }

    class MyTColumnModel extends AbstractTableModel {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private final String[] tcolumnNames = { NbBundle.getMessage( PolledColumnPanel.class, "LBL_Columns_Select"), NbBundle.getMessage( PolledColumnPanel.class, "LBL_Columns_ColumnName")};

        private List rowList;

        public MyTColumnModel(final List testList) {
            this.rowList = new ArrayList();
            for (int i = 0; i < testList.size(); i++) {
                final RowDataWrapper rowData = new RowDataWrapper((DBColumn) testList.get(i));
                this.rowList.add(rowData);
            }
        }

        /*
         * JTable uses this method to determine the default renderer/ editor for each cell. If we
         * didn't implement this method, then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        /**
         * 
         */
        public Class getColumnClass(final int c) {
            return this.getValueAt(0, c).getClass();
        }

        /**
         * 
         */
        public int getColumnCount() {
            return this.tcolumnNames.length;
        }

        /**
         * 
         */
        public String getColumnName(final int col) {
            return this.tcolumnNames[col];
        }

        /**
         * 
         */
        public int getRowCount() {
            return this.rowList.size();
        }

        /**
         * @param row
         * @return
         */
        public RowDataWrapper getRowDataWrapper(final int row) {
            if (row < this.rowList.size()) {
                return (RowDataWrapper) this.rowList.get(row);
            }
            return null;
        }

        /**
         * @return
         */
        public ArrayList getTColumns() {
            final ArrayList columnList = new ArrayList();
            for (int i = 0; i < this.rowList.size(); i++) {
                final RowDataWrapper rowData = (RowDataWrapper) this.rowList.get(i);
                columnList.add(rowData.getTColumn());
            }
            return columnList;
        }

        /**
         * 
         */
        public Object getValueAt(final int row, final int col) {
            final RowDataWrapper rowData = (RowDataWrapper) this.rowList.get(row);
            switch (col) {
            case 0:
                return rowData.isSelected();
            case 1:
                return rowData.getTColumn();
            }
            return String.valueOf(col + "?");
        }

        /*
         * Don't need to implement this method unless your table's editable.
         */
        /**
         * 
         */
        public boolean isCellEditable(final int row, final int col) {
            // Note that the data/cell address is constant,
            // no matter where the cell appears onscreen.
            final Object rowObj = this.rowList.get(row);
            return rowObj != null ? ((RowDataWrapper) rowObj).isEditable().booleanValue() && col == 0 : false;
        }

        /**
         * @param row
         * @param col
         * @param flag
         */
        public void setCellEditable(final int row, final int col, final boolean flag) {
            final Object rowObj = this.rowList.get(row);
            if (rowObj != null) {
                ((RowDataWrapper) rowObj).setEditable(flag ? Boolean.TRUE : Boolean.FALSE);
            }
        }

        /*
         * Don't need to implement this method unless your table's data can change.
         */
        /**
         * 
         */
        public void setValueAt(final Object value, final int row, final int col) {
            final RowDataWrapper rowData = (RowDataWrapper) this.rowList.get(row);
            switch (col) {
            case 0:
                rowData.setSelected((Boolean) value);
                this.fireTableRowsUpdated(row, row);
                break;
            }
        }
    }

    static class MyTColumnModelCellRenderer extends DefaultTableCellRenderer {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        protected static Border noFocusBorder1 = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        /**
         * Creates a default MyBooleanRenderer.
         */
        public MyTColumnModelCellRenderer() {
            super();
        }

        /**
         * 
         */
        public Component getTableCellRendererComponent(final JTable table,
                                                       final Object value,
                                                       final boolean isSelected,
                                                       final boolean hasFocus,
                                                       final int row,
                                                       final int column) {
            final JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            final MyTColumnModel model = (MyTColumnModel) table.getModel();
            final RowDataWrapper rowDW = model.getRowDataWrapper(row);
            if (rowDW != null && !rowDW.isEditable().booleanValue()) {
                renderer.setEnabled(false);
                renderer.setBackground(Color.lightGray);
                final Object obj = rowDW.getTColumn();
                final DBColumn st = (DBColumn) obj;
                if (!st.isPollSelected()) {
                    renderer.setToolTipText(NbBundle.getMessage(JDBCWizardTablePanel.class,
                            "TOOLTIP_source_table_disabled_unselected", rowDW.getTColumn()));
                }
                renderer.setBorder(MyTColumnModelCellRenderer.noFocusBorder1);
                renderer.setFocusable(false);
            } else {
                if (isSelected) {
                    renderer.setForeground(table.getSelectionForeground());
                    renderer.setBackground(table.getSelectionBackground());
                } else {
                    renderer.setForeground(table.getForeground());
                    renderer.setBackground(table.getBackground());
                }
                if (value instanceof DBColumn) {
                    final DBColumn dbModleTbl = (DBColumn) value;
                    if (dbModleTbl.getName() != null) {
                        this.setText(dbModleTbl.getName());
                    }
                }
                renderer.setToolTipText("");
                renderer.setEnabled(true);
                renderer.setFocusable(true);
            }
            return renderer;
        }
    }

    class RowDataWrapper {
        private DBColumn tcolumn;

        /**
         * @param mTColumn
         */
        public RowDataWrapper(final DBColumn mTColumn) {
            this.tcolumn = mTColumn;
        }

        /**
         * @return
         */
        public Object getTColumn() {
            return this.tcolumn;
        }

        /**
         * @return
         */
        public Boolean isEditable() {
            return this.tcolumn.isEditable() ? Boolean.TRUE : Boolean.FALSE;
            // return Boolean.TRUE;
        }

        /**
         * @return
         */
        public Boolean isSelected() {
            return this.tcolumn.isPollSelected() ? Boolean.TRUE : Boolean.FALSE;
        }

        /**
         * @param isEditable
         */
        public void setEditable(final Boolean isEditable) {
            this.tcolumn.setEditable(isEditable.booleanValue());
        }

        /**
         * @param isSelected
         */
        public void setSelected(final Boolean isSelected) {
            this.tcolumn.setPollSelected(isSelected.booleanValue());
        }
    }

    private JPanel headerPnl;

    /* table to display meta data */
    private MetaTColumnComponent metaDataTColumn;

    /* scrollpane for columns JTable */
    private JScrollPane tableScroll;

    /** Creates a default instance of JDBCWizardTablePanel */

    /**
     * Creates a new instance of JDBCWizardTablePanel to render the selection of tables
     * participating in an JDBC collaboration.
     * 
     * @param testList List of tables
     */
    public PolledColumnPanel() {
        final JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        this.headerPnl = new JPanel();
        this.headerPnl.setLayout(new BorderLayout());
        this.headerPnl.setOpaque(false);
        this.headerPnl.add(p, BorderLayout.NORTH);
        // addColumnTable(testList);
    }

    /**
     * Gets associated JTable.
     * 
     * @return JTable
     */
    public JTable getColumnTable() {
        return this.metaDataTColumn;
    }

    /**
     * Gets list of selected tables.
     * 
     * @return List of selected tables
     */
    public List getColumnTables() {
        final MyTColumnModel tcolumnModel = (MyTColumnModel) this.metaDataTColumn.getModel();
        return tcolumnModel.getTColumns();
    }

    public List getSelectedColumnTables() {
        final List allctabs = this.getColumnTables();
        final List selctabs = new ArrayList();
        for (int cnt = 0; cnt < allctabs.size(); cnt++) {
            if (((DBColumn) allctabs.get(cnt)).isPollSelected()) {
                selctabs.add(allctabs.get(cnt));
            }
        }
        return selctabs;
    }

    /**
     * Paints this component
     * 
     * @param g graphics context
     */
    public void paint(final Graphics g) {
        super.paint(g);
    }

    /**
     * Populates selected tables using items contained in the given List.
     * 
     * @param tableNameList List of tables to use in repopulating set of selected tables
     */
    public void resetColumnTable(final List tcolumnNameList) {
        final MyTColumnModel myMod = new MyTColumnModel(tcolumnNameList);
        this.metaDataTColumn.setModel(myMod);
        // set checkbox column size
        final TableColumn column = this.metaDataTColumn.getColumnModel().getColumn(0);
        column.setResizable(false);
        column.setMinWidth(40);
        column.setPreferredWidth(80);
        column.setMaxWidth(120);
    }

    public void addColumnTable(final List testList) {
        this.metaDataTColumn = new MetaTColumnComponent();
        this.metaDataTColumn.setFont(JDBCTableColumnDisplayable.FONT_TABLE_COLUMNS);
        this.metaDataTColumn.getTableHeader().setFont(JDBCTableColumnDisplayable.FONT_TABLE_HEADER);
        final MyTColumnModel myModel = new MyTColumnModel(testList);
        this.metaDataTColumn.setModel(myModel);
        this.setLayout(new BorderLayout());
        // add(headerPnl, BorderLayout.NORTH);
        this.setPreferredSize(new Dimension(100, 100));
        this.setMaximumSize(new Dimension(150, 150));
        // set checkbox column size
        final TableColumn column = this.metaDataTColumn.getColumnModel().getColumn(0);
        column.setResizable(false);
        column.setMinWidth(40);
        column.setPreferredWidth(80);
        column.setMaxWidth(120);
        this.tableScroll = new JScrollPane(this.metaDataTColumn);
        final javax.swing.border.Border inside = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3,
                3), BorderFactory.createLineBorder(Color.GRAY));
        this.tableScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), inside));
        this.add(this.tableScroll, BorderLayout.CENTER);
    }
}
