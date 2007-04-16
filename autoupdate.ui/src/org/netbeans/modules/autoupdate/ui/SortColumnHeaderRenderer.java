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
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.autoupdate.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Comparator;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Radek Matous
 */
public class SortColumnHeaderRenderer implements TableCellRenderer{
    private UnitCategoryTableModel model;
    private TableCellRenderer textRenderer;
    private int sortColumn = -1;
    //private boolean sortAscending = true;
    private JTable table;
    private MouseListener mlistener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            JTableHeader header = (JTableHeader)e.getSource();
            int index = header.columnAtPoint(e.getPoint());
            if (index == 1) {
                SortColumnHeaderRenderer.this.columnSelected(index);
                table.setColumnSelectionInterval(index, index);
            }
        }
        
    };
    public SortColumnHeaderRenderer(UnitCategoryTableModel model, TableCellRenderer textRenderer) {
        this.model = model;
        this.textRenderer = textRenderer;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus, int row,
                                                   int column) {
        if (this.table == null) {
            this.table = table;
            this.table.getTableHeader().addMouseListener(mlistener);
        }
        Component text;
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        if (textRenderer != null) {
            text = textRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        } else {
            text = new JLabel((String)value,JLabel.CENTER);
            LookAndFeel.installColorsAndFont((JComponent)text, "TableHeader.background", "TableHeader.foreground", "TableHeader.font");
        }
        panel.add(text,BorderLayout.CENTER);
        if (column == sortColumn) {
            text.setFont(text.getFont().deriveFont(Font.BOLD));                       
            //BasicArrowButton bab = new BasicArrowButton(sortAscending ? SwingConstants.NORTH : SwingConstants.SOUTH);
            //panel.add(bab,BorderLayout.WEST);
        } else {
            text.setFont(text.getFont().deriveFont(Font.PLAIN));
        }
        LookAndFeel.installBorder(panel, "TableHeader.cellBorder");
        return panel;
    }
    

    public void columnSelected(int column) {
        if (column != sortColumn) {
            sortColumn = column;
            //sortAscending = true;
        } else {
            //sortAscending = !sortAscending;
            /*if (sortAscending)*/ {
                sortColumn = -1;
            }
        }
        if (sortColumn != -1) {
            Comparator<Unit> cmp = new Comparator<Unit>(){
                public int compare(Unit o1, Unit o2) {
                    return java.text.Collator.getInstance().compare(o1.getDisplayName(), o2.getDisplayName());
                }
};
            this.model.setUnitComparator(cmp, false);
        } else {
            this.model.setUnitComparator(null, true);
        }
        if (this.table != null) {
            this.model.fireTableDataChanged();
        }
    }
}
