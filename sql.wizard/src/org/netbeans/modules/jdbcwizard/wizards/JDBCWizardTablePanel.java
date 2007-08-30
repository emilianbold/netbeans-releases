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

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.wizards;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.openide.util.NbBundle;

import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBTable;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBColumn;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.impl.DBColumnImpl;
import org.netbeans.modules.jdbcwizard.builder.dbmodel.impl.DBTableImpl;

// import javax.swing.AbstractAction;
import javax.swing.table.TableCellEditor;

import java.util.EventObject;

/**
 * This class represents table for meta data. This holds a JTable for showing table meta data.
 * 
 * @author
 */
public class JDBCWizardTablePanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    class MetaTableComponent extends JTable {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        protected MyTableModelCellRenderer mytabmod;

        protected MyBooleanRenderer mybool;
		
		public MetaTableComponent() {
            // Need to revisit whether should use abstract model here??
			mytabmod = new MyTableModelCellRenderer();
			mybool = new MyBooleanRenderer();
            this.setDefaultRenderer(DBTableImpl.class, this.mytabmod);
            this.setDefaultRenderer(Boolean.class, this.mybool);
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
            this.myPanel.setOpaque(true);
            this.myPanel.setBorder(MyBooleanRenderer.noFocusBorder);
			this.setSelected(true);
        }

        public Component getTableCellRendererComponent(final JTable table,
                                                       final Object value,
                                                       final boolean isSelected,
                                                       final boolean hasFocus,
                                                       final int row,
                                                       final int column) {
            final RowDataWrapper rowDW = ((MyTableModel) table.getModel()).getRowDataWrapper(row);
            if (rowDW != null && !rowDW.isEditable().booleanValue()) {
                this.setEnabled(false);
                this.setFocusable(false);
                this.setBackground(Color.LIGHT_GRAY);
                final Object obj = rowDW.getTable();
                if (obj instanceof DBTable) {
                    final DBTable st = (DBTable) obj;// SourceTable modified to
                    if (!st.isSelected()) {
                        this.setToolTipText(NbBundle.getMessage(JDBCWizardTablePanel.class,
                                "TOOLTIP_source_table_disabled_unselected", rowDW.getTable()));
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
                this.setEnabled(true);
                this.setFocusable(true);
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
		
		public void setSelected(boolean flag){
			super.setSelected(flag);
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

    class MyButtonRenderer extends JButton implements TableCellRenderer, TableCellEditor {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private JPanel myButPanel;

        MyButtonRenderer(final String disp) {
            super();
            this.setText(disp);
            this.setOpaque(true);
            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.myButPanel = new JPanel();
            this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            this.myButPanel.setLayout(new BorderLayout());
            this.myButPanel.add(this, BorderLayout.CENTER);
            this.myButPanel.setOpaque(true);
            this.myButPanel.setEnabled(false);
        }

        public Component getTableCellRendererComponent(final JTable table,
                                                       final Object value,
                                                       final boolean isSelected,
                                                       final boolean hasFocus,
                                                       final int row,
                                                       final int column) {
            final RowDataWrapper rowDW = ((MyTableModel) table.getModel()).getRowDataWrapper(row);
            if (rowDW != null && !rowDW.isEditable().booleanValue()) {
                this.setEnabled(false);
                this.setFocusable(false);
                this.setBackground(Color.LIGHT_GRAY);
                final Object obj = rowDW.getTable();
                if (obj instanceof DBTable) {
                    final DBTable st = (DBTable) obj;// SourceTable modified to
                    // DBTable
                    if (!st.isSelected()) {
                        this.setToolTipText(NbBundle.getMessage(JDBCWizardTablePanel.class,
                                "TOOLTIP_source_table_disabled_unselected", rowDW.getTable()));
                    }
                }
            } else {
                if (isSelected) {
                    // myButPanel.setBorder(noFocusBorder);
                    this.myButPanel.setBackground(Color.LIGHT_GRAY);
                    this.setForeground(table.getSelectionForeground());
                    this.setBackground(table.getSelectionBackground());
                    this.myButPanel.setForeground(table.getSelectionForeground());
                    this.myButPanel.setBackground(table.getSelectionBackground());
                } else {
                    this.setForeground(table.getForeground());
                    this.setBackground(table.getBackground());
                    this.myButPanel.setForeground(table.getForeground());
                    this.myButPanel.setBackground(table.getBackground());
                }
                if (hasFocus) { // NOI18N this scope block
                    this.myButPanel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                    if (table.isCellEditable(row, column)) {
                        this.setForeground(UIManager.getColor("Table.focusCellForeground"));
                        this.setBackground(UIManager.getColor("Table.focusCellBackground"));
                    }
                    this.myButPanel.setForeground(UIManager.getColor("Table.focusCellForeground"));
                    this.myButPanel.setBackground(UIManager.getColor("Table.focusCellBackground"));
                }

                this.setEnabled(true);
                this.setFocusable(true);
                this.setText("Advanced..");
            }
            return this.myButPanel;
        }

        public Component getTableCellEditorComponent(final JTable table,
				final Object value, final boolean isSelected, final int row,
				final int column) {
			final RowDataWrapper rowDW = ((MyTableModel) table.getModel())
					.getRowDataWrapper(row);
			this.addActionListener(new ColumnAction(rowDW));
			if (rowDW != null && !rowDW.isEditable().booleanValue()) {
				this.setEnabled(false);
				this.setFocusable(false);
				this.setBackground(Color.LIGHT_GRAY);
				final Object obj = rowDW.getTable();
				if (obj instanceof DBTable) {
					final DBTable st = (DBTable) obj;
					if (!st.isSelected()) {
						this.setToolTipText(NbBundle.getMessage(
								JDBCWizardTablePanel.class,
								"TOOLTIP_source_table_disabled_unselected",
								rowDW.getTable()));
					}
				}
			} else {

				if (isSelected) {
					this.myButPanel.setBackground(Color.LIGHT_GRAY);
					this.myButPanel.add(this, BorderLayout.CENTER);
					this.setEnabled(true);
					this.myButPanel.setEnabled(true);
					this.myButPanel.setOpaque(true);
					//this.addActionListener(new ColumnAction(rowDW));
					this.setForeground(table.getSelectionForeground());
					this.setBackground(table.getSelectionBackground());
					this.myButPanel.setForeground(table
							.getSelectionForeground());
					this.myButPanel.setBackground(table
							.getSelectionBackground());
				} else {
					this.setForeground(table.getForeground());
					this.setBackground(table.getBackground());
					this.myButPanel.setForeground(table.getForeground());
					this.myButPanel.setBackground(table.getBackground());
				}
				this.setFocusable(true);
				this.setText("Advanced..");
			}
			return this.myButPanel;
		}

        /**
		 * 
		 */
        public Object getCellEditorValue() {
            return this.getCellEditorValue();
        }

        /**
		 * 
		 */
        public boolean isCellEditable(final EventObject anEvent) {
            return true;
        }

        /**
		 * 
		 */
        public boolean shouldSelectCell(final EventObject anEvent) {
            return true;
        }

        /**
         * 
         */
        public boolean stopCellEditing() {
            return true;
        }

        /**
         * 
         */
        public void cancelCellEditing() {
            this.cancelCellEditing();
        }

        /**
         * 
         */
        public void addCellEditorListener(final CellEditorListener l) {
        }

        /**
         * 
         */
        public void removeCellEditorListener(final CellEditorListener l) {
        }

    }

    class ColumnAction implements ActionListener {
        protected JButton okbutton = new JButton(NbBundle.getMessage( JDBCWizardTablePanel.class, "BTN_OK"));

        protected JButton cancelbutton = new JButton(NbBundle.getMessage( JDBCWizardTablePanel.class, "BTN_Cancel"));
        
        protected JButton selectallbutton = new JButton(NbBundle.getMessage( JDBCWizardTablePanel.class, "BTN_SelectAll"));
        
        protected JButton clearallbutton = new JButton(NbBundle.getMessage( JDBCWizardTablePanel.class, "BTN_ClearAll"));

        protected JPanel buttonpanel = new JPanel();

        protected InsertColumnPanel correspInsertCol;

        protected UpdateColumnPanel correspUpdateCol;

        protected ChosenColumnPanel correspChosenCol;

        protected PolledColumnPanel correspPolledCol;

        protected JTabbedPane myTabpane;

        // protected JDialog columndisplay = new JDialog();
        protected class ColumnDialog extends JDialog {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            ColumnDialog(final JDialog parent, final boolean modal) {
                super(parent, modal);
            }
        }

        ColumnDialog columnDisplayDialog = null;

        protected String title = "Columns";
        
        RowDataWrapper takes;
        public ColumnAction(final RowDataWrapper takes) {
        	this.takes = takes;
        	columnDisplayDialog = new ColumnDialog(new JDialog(), true);
        	this.columnDisplayDialog.setAlwaysOnTop(true);
            this.columnDisplayDialog.setResizable(false);
            this.columnDisplayDialog.setEnabled(true);
            this.columnDisplayDialog.setFocusable(true);
        }

        // this is the default action when the button corresponding to the
        // created table is clicked
        /**
         * 
         */
        public void actionPerformed(final ActionEvent anAct) {
        	if(this.takes != null && this.takes.isSelected().booleanValue()){
        		this.correspInsertCol = new InsertColumnPanel();
				this.correspInsertCol.addColumnTable(((DBTable) this.takes
						.getTable()).getColumnList());

				this.correspUpdateCol = new UpdateColumnPanel();
				this.correspUpdateCol.addColumnTable(((DBTable) this.takes
						.getTable()).getColumnList());

				this.correspChosenCol = new ChosenColumnPanel();
				this.correspChosenCol.addColumnTable(((DBTable) this.takes
						.getTable()).getColumnList());

				this.correspPolledCol = new PolledColumnPanel();
				this.correspPolledCol.addColumnTable(((DBTable) this.takes
						.getTable()).getColumnList());
				this.title = this.title.concat(JDBCWizardTablePanel.SEPARATOR
						+ ((DBTable) this.takes.getTable()).getName());
        		this.initializeColumn();
        	}
        	else
        		JOptionPane.showMessageDialog(null, "Please select the table to select additional properties", "Column Selection", JOptionPane.WARNING_MESSAGE);
        }	

        /**
         *
         */
        public void initializeColumn() {
            this.columnDisplayDialog.setTitle(this.title);
            this.correspInsertCol.setName(NbBundle.getMessage( JDBCWizardTablePanel.class, "LBL_Col_Insert"));
            this.correspUpdateCol.setName(NbBundle.getMessage( JDBCWizardTablePanel.class, "LBL_Col_Update"));
            this.correspChosenCol.setName(NbBundle.getMessage( JDBCWizardTablePanel.class, "LBL_Col_Find"));//
            this.correspPolledCol.setName(NbBundle.getMessage( JDBCWizardTablePanel.class, "LBL_Col_PollRecords"));
            
            this.myTabpane = new JTabbedPane();
            this.myTabpane.add(this.correspInsertCol);
            this.myTabpane.add(this.correspUpdateCol);
            this.myTabpane.add(this.correspChosenCol);
            this.myTabpane.add(this.correspPolledCol);
            this.columnDisplayDialog.add(this.myTabpane, BorderLayout.CENTER);
            this.cancelbutton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    ColumnAction.this.cleanUp();
                }
            });
            this.okbutton.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    ColumnAction.this.okAction();
                }
            });
            this.selectallbutton.addActionListener(new ActionListener(){
            	public void actionPerformed(final ActionEvent e){
            		ColumnAction.this.selectAllAction(ColumnAction.this.myTabpane.getSelectedIndex());
            	}
            });
            this.clearallbutton.addActionListener(new ActionListener(){
            	public void actionPerformed(final ActionEvent e){
            		ColumnAction.this.clearAllAction(ColumnAction.this.myTabpane.getSelectedIndex());
            	}
            });
            
            this.buttonpanel.add(this.selectallbutton, BorderLayout.LINE_START);
            this.buttonpanel.add(this.clearallbutton, BorderLayout.WEST);
            this.buttonpanel.add(this.okbutton, BorderLayout.EAST);
            this.buttonpanel.add(this.cancelbutton, BorderLayout.LINE_END);
            this.columnDisplayDialog.add(this.buttonpanel, BorderLayout.SOUTH);
            this.centerWindowOnScreen(this.columnDisplayDialog);
            this.columnDisplayDialog.setSize(new Dimension(400, 300));
            this.columnDisplayDialog.setVisible(true);
         }

        /**
         * 
         *
         */
        public void cleanUp() {
            int cnt = 0;
            while (cnt < this.correspInsertCol.getColumnTable().getModel().getRowCount()) {
                ((DBColumn) (this.correspInsertCol.getColumnTables()).get(cnt)).setSelected(true);
                cnt++;
            }
            this.columnDisplayDialog.dispose();
            this.title = "Columns";
        }

        public void okAction() {
            this.columnDisplayDialog.dispose();
            this.title = "Columns";
        }
        
        public void selectAllAction(int index){
        	int cnt = 0;
            switch (index) {
            case 0:
            	while( cnt < this.correspInsertCol.getColumnTable().getModel().getRowCount()){
                    DBColumn db = (DBColumn) (this.correspInsertCol.getColumnTables()).get(cnt);
                    db.setInsertSelected(true);
                    cnt++;
                }
            	this.correspInsertCol.repaint();
            	break;
            case 1:
            	while( cnt < this.correspUpdateCol.getColumnTable().getModel().getRowCount()){
                    DBColumn db = (DBColumn) (this.correspUpdateCol.getColumnTables()).get(cnt);
                    db.setUpdateSelected(true);
                    cnt++;
                }
            	this.correspUpdateCol.repaint();
            	break;
            case 2:
            	while( cnt < this.correspChosenCol.getColumnTable().getModel().getRowCount()){
                    DBColumn db = (DBColumn) (this.correspChosenCol.getColumnTables()).get(cnt);
                    db.setChooseSelected(true);
                    cnt++;
                }
            	this.correspChosenCol.repaint();
            	break;  
            case 3:
            	while( cnt < this.correspPolledCol.getColumnTable().getModel().getRowCount()){
                    DBColumn db = (DBColumn) (this.correspPolledCol.getColumnTables()).get(cnt);
                    db.setPollSelected(true);
                    cnt++;
                }
            	this.correspPolledCol.repaint();
            	break;
            }        	
        }
        
        public void clearAllAction(int index){
        	int cnt = 0;
            switch (index) {
            case 0:
            	while( cnt < this.correspInsertCol.getColumnTable().getModel().getRowCount()){
                    DBColumn db = (DBColumn) (this.correspInsertCol.getColumnTables()).get(cnt);
                    if(db.isNullable()){
                        db.setInsertSelected(false);
                    }
                    cnt++;
                }
            	this.correspInsertCol.repaint();
            	break;
            case 1:
            	while( cnt < this.correspUpdateCol.getColumnTable().getModel().getRowCount()){
                    DBColumn db = (DBColumn) (this.correspUpdateCol.getColumnTables()).get(cnt);
                    db.setUpdateSelected(false);
                    cnt++;
                }
            	this.correspUpdateCol.repaint();
            	break;
            case 2:
            	while( cnt < this.correspChosenCol.getColumnTable().getModel().getRowCount()){
                    DBColumn db = (DBColumn) (this.correspChosenCol.getColumnTables()).get(cnt);
                    db.setChooseSelected(false);
                    cnt++;
                }
            	this.correspChosenCol.repaint();
            	break;  
            case 3:
            	while( cnt < this.correspPolledCol.getColumnTable().getModel().getRowCount()){
                    DBColumn db = (DBColumn) (this.correspPolledCol.getColumnTables()).get(cnt);
                    db.setPollSelected(false);
                    cnt++;
                }
            	this.correspPolledCol.repaint();
            	break;             	
            }

        }

        /**
         * @param window
         */
        public void centerWindowOnScreen(final Window window) {
            window.pack();
            final Rectangle rect = window.getBounds();
            final Dimension scrnDim = Toolkit.getDefaultToolkit().getScreenSize();
            rect.x = Math.max(0, (scrnDim.width - rect.width) / 3);
            rect.y = Math.max(0, (scrnDim.height - rect.height) / 3);
            window.setBounds(rect);
        }
    }

    class MyTableModel extends AbstractTableModel {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private final String[] columnNames = { NbBundle.getMessage( JDBCWizardTablePanel.class, "LBL_select_jLabell" ),NbBundle.getMessage( JDBCWizardTablePanel.class, "LBL_tablename_jLabell" ),NbBundle.getMessage( JDBCWizardTablePanel.class, "LBL_properties_jLabell" ) };

        private List rowList;

        public MyTableModel(final List testList) {
            this.rowList = new ArrayList();
            for (int i = 0; i < testList.size(); i++) {
                final RowDataWrapper rowData = new RowDataWrapper((DBTable) testList.get(i));
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
            return this.columnNames.length;
        }

        /**
         * 
         */
        public String getColumnName(final int col) {
            return this.columnNames[col];
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
        public ArrayList getTables() {
            final ArrayList tableList = new ArrayList();
            for (int i = 0; i < this.rowList.size(); i++) {
                final RowDataWrapper rowData = (RowDataWrapper) this.rowList.get(i);
                tableList.add(rowData.getTable());
            }
            return tableList;
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
                return rowData.getTable();
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
            return true;
            // return (rowObj != null) ? ((RowDataWrapper) rowObj).isEditable().booleanValue() &&
            // (col == 0) : false;
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

    static class MyTableModelCellRenderer extends DefaultTableCellRenderer {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        protected static Border noFocusBorder1 = BorderFactory.createEmptyBorder(1, 1, 1, 1);

        /**
         * Creates a default MyBooleanRenderer.
         */
        public MyTableModelCellRenderer() {
            super();
        }

        public Component getTableCellRendererComponent(final JTable table,
                                                       final Object value,
                                                       final boolean isSelected,
                                                       final boolean hasFocus,
                                                       final int row,
                                                       final int column) {
            final JLabel renderer = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            final MyTableModel model = (MyTableModel) table.getModel();
            final RowDataWrapper rowDW = model.getRowDataWrapper(row);
            if (rowDW != null && !rowDW.isEditable().booleanValue()) {
                renderer.setEnabled(false);
                renderer.setBackground(Color.lightGray);
                final Object obj = rowDW.getTable();
                final DBTable st = (DBTable) obj;
                if (!st.isSelected()) {
                    renderer.setToolTipText(NbBundle.getMessage(JDBCWizardTablePanel.class,
                            "TOOLTIP_source_table_disabled_unselected", rowDW.getTable()));
                }
                renderer.setBorder(MyTableModelCellRenderer.noFocusBorder1);
                renderer.setFocusable(false);
            } else {
                if (isSelected) {
                    renderer.setForeground(table.getSelectionForeground());
                    renderer.setBackground(table.getSelectionBackground());
                } else {
                    renderer.setForeground(table.getForeground());
                    renderer.setBackground(table.getBackground());
                }
                if (value instanceof DBTable) {
                    final DBTable dbModleTbl = (DBTable) value;
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
        private DBTable table;

        public RowDataWrapper(final DBTable mTable) {
            this.table = mTable;
        }

        /**
         * @return
         */
        public Object getTable() {
            return this.table;
        }

        /**
         * @return
         */
        public Boolean isEditable() {
            return this.table.isEditable() ? Boolean.TRUE : Boolean.FALSE;
        }

        /**
         * @return
         */
        public Boolean isSelected() {
            return this.table.isSelected() ? Boolean.TRUE : Boolean.FALSE;
        }

        /**
         * @param isEditable
         */
        public void setEditable(final Boolean isEditable) {
            this.table.setEditable(isEditable.booleanValue());
        }

        /**
         * @param isSelected
         */
        public void setSelected(final Boolean isSelected) {
            this.table.setSelected(isSelected.booleanValue());
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

    /** Creates a default instance of JDBCWizardTablePanel */
    private static final String SEPARATOR = " - ";

    public JDBCWizardTablePanel() {
    }

    /**
     * Creates a new instance of JDBCWizardTablePanel to render the selection of tables
     * participating in an JDBC collaboration.
     * 
     * @param testList List of tables
     */
    public JDBCWizardTablePanel(final List testList) {
        this.setOpaque(false);
        final JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        this.headerPnl = new JPanel();
        this.headerPnl.setLayout(new BorderLayout());
        this.headerPnl.setOpaque(false);
        this.headerPnl.add(p, BorderLayout.NORTH);
        this.addTable(testList);
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
        final MyTableModel tableModel = (MyTableModel) this.metaDataTable.getModel();
        return tableModel.getTables();
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
    public void resetTable(final List tableNameList) {
        final MyTableModel myMod = new MyTableModel(tableNameList);
        this.metaDataTable.setModel(myMod);
        this.metaDataTable.getColumn("Properties").setCellRenderer(new MyButtonRenderer(NbBundle.getMessage( JDBCWizardTablePanel.class, "BTN_Advanced")));
        this.metaDataTable.getColumn("Properties").setCellEditor(new MyButtonRenderer(NbBundle.getMessage( JDBCWizardTablePanel.class, "BTN_Advanced")));
        // set checkbox column size
        final TableColumn column = this.metaDataTable.getColumnModel().getColumn(0);
        column.setResizable(false);
        column.setMinWidth(40);
        column.setPreferredWidth(80);
        column.setMaxWidth(120);
        
        final TableColumn columnAdv = this.metaDataTable.getColumnModel().getColumn(2);
        columnAdv.setResizable(false);
        columnAdv.setMinWidth(60);
        columnAdv.setPreferredWidth(80);
        columnAdv.setMaxWidth(100);
    }

    /**
     * @param testList
     */
    public void addTable(final List testList) {
        this.metaDataTable = new MetaTableComponent();
        this.metaDataTable.setFont(JDBCWizardTablePanel.FONT_TABLE_COLUMNS);
        this.metaDataTable.getTableHeader().setFont(JDBCWizardTablePanel.FONT_TABLE_HEADER);
        final MyTableModel myModel = new MyTableModel(testList);
        this.metaDataTable.setModel(myModel);
        this.metaDataTable.getColumn("Properties").setCellRenderer(new MyButtonRenderer(NbBundle.getMessage( JDBCWizardTablePanel.class, "BTN_Advanced")));
        this.metaDataTable.getColumn("Properties").setCellEditor(new MyButtonRenderer(NbBundle.getMessage( JDBCWizardTablePanel.class, "BTN_Advanced")));
        this.setLayout(new BorderLayout());
        // add(headerPnl, BorderLayout.NORTH);
        this.setPreferredSize(new Dimension(100, 100));
        this.setMaximumSize(new Dimension(150, 150));
        // set checkbox column size
        final TableColumn column = this.metaDataTable.getColumnModel().getColumn(0);
        column.setResizable(false);
        column.setMinWidth(40);
        column.setPreferredWidth(40);
        column.setMaxWidth(80);
        this.tableScroll = new JScrollPane(this.metaDataTable);
        final javax.swing.border.Border inside = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3,
                3), BorderFactory.createLineBorder(Color.GRAY));
        this.tableScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), inside));
        this.add(this.tableScroll, BorderLayout.CENTER);
    }
}
