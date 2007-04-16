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

package org.netbeans.modules.autoupdate.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class UnitTable extends JTable {
    private UnitCategoryTableModel model = null;
    private PluginColumn pluginColumn;
    private InstalledPluginColumn installedPluginColumn;
    private LocalPluginColumn localPluginColumn;
    private int defaultHeight = new JCheckBox ("", true).getPreferredSize().height;
    
    /** Creates a new instance of UpdateTable */
    public UnitTable (TableModel model) {
        super (model);
        this.model = (UnitCategoryTableModel) model;
        setDefaultRenderer (Boolean.class, new BooleanRenderer (getDefaultRenderer (Boolean.class)));
        
        if (UnitCategoryTableModel.Type.AVAILABLE == this.model.getType ()) {
            this.pluginColumn = new PluginColumn (this, 1, this.model);
        }
        if (UnitCategoryTableModel.Type.UPDATE == this.model.getType ()) {
            this.pluginColumn = new PluginColumn (this, 1, this.model);
        }
//        if (UnitCategoryTableModel.Type.LOCAL == this.model.getType ()) {
//            this.pluginColumn = new PluginColumn (this, 1, this.model);
//        }        
        if (UnitCategoryTableModel.Type.LOCAL == this.model.getType ()) {
            this.localPluginColumn = new LocalPluginColumn (this, 1, this.model);
        }        
        if (UnitCategoryTableModel.Type.INSTALLED == this.model.getType ()) {
            this.installedPluginColumn = new InstalledPluginColumn (this, 1, this.model);
        }
        
        // set sizes XXX
//        getColumnModel ().getColumn (0).setPreferredWidth ((int) (getWidth () * .1));
//        getColumnModel ().getColumn (1).setPreferredWidth ((int) (getWidth () * .5));
//        getColumnModel ().getColumn (2).setPreferredWidth ((int) (getWidth () * .15));
//        getColumnModel ().getColumn (3).setPreferredWidth ((int) (getWidth () * .15));
//        getColumnModel ().getColumn (4).setPreferredWidth ((int) (getWidth () * .1));
        getColumnModel ().getColumn (0).setPreferredWidth (50);
        getColumnModel ().getColumn (1).setPreferredWidth (250);
        getColumnModel ().getColumn (2).setPreferredWidth (75);
        getColumnModel ().getColumn (3).setPreferredWidth (75);
        getColumnModel ().getColumn (4).setPreferredWidth (50);
        
        SortColumnHeaderRenderer scRenderer = new SortColumnHeaderRenderer(this.model,getColumnModel ().getColumn(0).getHeaderRenderer());
        getColumnModel().getColumn(1).setHeaderRenderer(scRenderer);        
        initTable ();
        initActions ();
        revalidate ();
    }

    private void initActions () {
        final String collapseKey = "CollapseAll", expandKey="ExpandAll";//NOI18N
        
        if (getActionMap ().get(collapseKey) == null) {            
            InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), collapseKey);
            inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), expandKey);
            
            getActionMap().put(collapseKey,new AbstractAction(){
                public void actionPerformed(ActionEvent e) {
                    model.collapseAll();
                    model.fireTableDataChanged();
                }
            });
            getActionMap().put(expandKey,new AbstractAction(){
                public void actionPerformed(ActionEvent e) {
                    model.expandAll();
                    model.fireTableDataChanged();
                }
            });            
        }
    }
    
    private void initTable () {
        setSurrendersFocusOnKeystroke (true);
        setUI (new UnitTableUI (model));
        setRowHeight (defaultHeight);
        
        addMouseListener (new MouseAdapter () {
            public void mouseClicked (MouseEvent e) {
                //handle group collapse/expand
                int row = rowAtPoint (e.getPoint ());
                if (model.isCategoryAtRow (row)) {
                    if (e.getClickCount () == 2) {
                        model.toggleCategoryExpanded (row);
                    } else if (e.getClickCount () == 1) {
                        int col = columnAtPoint (e.getPoint ());
                        if (col == 0) {
                            model.toggleCategoryExpanded (row);
                        }
                    }
                }
            }
        });
        
        getColumnModel().addColumnModelListener (new TableColumnModelListener () {
            public void columnAdded(TableColumnModelEvent arg0) {}
            public void columnRemoved(TableColumnModelEvent arg0) {}
            public void columnMoved(TableColumnModelEvent arg0) {}
            public void columnMarginChanged(ChangeEvent arg0) {}

            public void columnSelectionChanged(ListSelectionEvent arg0) {
                modifyBooleanEditor (getSelectedRow ());
            }
        });
        
        getSelectionModel ().addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent evt) {
                final int row = getSelectedRow ();
                setRowHeight (evt.getFirstIndex (), defaultHeight);
                setRowHeight (evt.getLastIndex (), defaultHeight);
                if (getSelectedColumn() == 0) {
                    modifyBooleanEditor (row);
                }                
                
                if (! (model instanceof InstalledTableModel || model instanceof LocallyDownloadedTableModel)
                        && row != -1 && ! model.isCategoryAtRow (row)) {
                    Component c = null;               
                    if (! UnitTable.this.isEditing () || UnitTable.this.getEditingColumn () != 1) {
                        c = pluginColumn.getTableCellRendererComponent(UnitTable.this, 0, true, true, row, 1);
                    } else {
                        c = pluginColumn.getTableCellEditorComponent(UnitTable.this, 0, true, row, 1);
                    }
                    setRowHeight (row, defaultHeight < c.getPreferredSize().height ? c.getPreferredSize().height : defaultHeight);
                }
                if (model instanceof InstalledTableModel
                        && row != -1 && ! model.isCategoryAtRow (row)) {
                    Component c = installedPluginColumn.getTableCellRendererComponent (UnitTable.this, 0, true, true, row, 1);
                    setRowHeight (row, defaultHeight < c.getPreferredSize().height ? c.getPreferredSize().height : defaultHeight);
                }
                if (model instanceof LocallyDownloadedTableModel
                        && row != -1 && ! model.isCategoryAtRow (row)) {
                    Component c = localPluginColumn.getTableCellRendererComponent (UnitTable.this, 0, true, true, row, 1);
                    setRowHeight (row, defaultHeight < c.getPreferredSize().height ? c.getPreferredSize().height : defaultHeight);
                }
            }
        });

    }
    
    private void modifyBooleanEditor (int row) {
        Component cmp = getDefaultEditor(Boolean.class).getTableCellEditorComponent (this, 0, true,  row, 0);
        if (cmp != null) {
            if (cmp instanceof JCheckBox) {
                JCheckBox cb = (JCheckBox) cmp;
                cb.setBorderPaintedFlat (true);
                cb.setVerticalAlignment (SwingConstants.TOP);
            }
            cmp.setBackground (UIManager.getColor ("Table.selectionBackground"));
        }
    }
    
    @Override
    public TableCellRenderer getCellRenderer (int row, int column) {
        if (model.isCategoryAtRow (row)) {
            return new CategoryTableRenderer ();
        } else {
            return super.getCellRenderer (row, column);
        }
    }
    
    private static final Icon openedIcon = UIManager.getIcon ("Tree.expandedIcon");
    private static final Icon closedIcon = UIManager.getIcon ("Tree.collapsedIcon");
        
    private class CategoryTableRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent (JTable table, Object value,
                              boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component res = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
            
            if (res == null || value == null) {
                return res;
            }
            
            assert value instanceof UnitCategory : value + " must be instanceof UnitCategory.";
            UnitCategory c = (UnitCategory) value;
            JLabel renderer = (JLabel) res;
            if (isSelected) {
                renderer.setBackground (UIManager.getColor ("Table.selectionBackground"));
            } else {
                Color categoryBackground = UIManager.getColor ("TabbedPane.tabAreaBackground");
                renderer.setBackground (categoryBackground == null ? UIManager.getColor ("Table.gridColor") : categoryBackground);
            }
            renderer.setText (c.getCategoryName ());
            Icon treeIcon = c.isExpanded () ? openedIcon : closedIcon;
            renderer.setIcon (treeIcon);
            renderer.setHorizontalAlignment (JLabel.LEFT);
            renderer.setOpaque (true);
            res = renderer;
            
            return res;
        }
    }
    
    private class BooleanRenderer extends DefaultTableCellRenderer {
        private TableCellRenderer defaultRenderer = null;
        public BooleanRenderer (TableCellRenderer original) {
            defaultRenderer = original;
        }
        
        @Override
        public Component getTableCellRendererComponent (JTable table, Object value,
                              boolean isSelected, boolean hasFocus, int row, int column) {            
            Component comp = defaultRenderer.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
            
            if (comp == null || value == null) {
                return comp;
            }

            if (comp instanceof JCheckBox) {
                JCheckBox cb = (JCheckBox) comp;
                cb.setBorderPaintedFlat (true);
                cb.setVerticalAlignment (TOP);
            }
            
            Color c = isSelected ? UIManager.getColor("Table.selectionBackground") :
                UIManager.getColor("Table.background");
            comp.setBackground(c);
            
            assert value instanceof Boolean : value + " must be instanceof Boolean.";
            comp.setEnabled (model.isCellEditable (row, 0));
            
            return comp;
        }
    }
           
    private String getBundle (String key) {
        return NbBundle.getMessage (UnitTable.class, key);
    }    
}
