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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.netbeans.installer.utils.helper.swing;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JTable;
import javax.swing.JTree; 
import javax.swing.table.TableCellRenderer;
import org.netbeans.installer.utils.LogManager;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiTreeTableColumnRenderer extends JTree implements TableCellRenderer {
    private NbiTreeTable treeTable;
    
    private int visibleRow = 0;
    
    private NbiTreeTableColumnCellRenderer cellRenderer;
    
    public NbiTreeTableColumnRenderer(final NbiTreeTable treeTable) {
        this.treeTable = treeTable;
        
        setModel(treeTable.getModel().getTreeModel());
        
        setRootVisible(false);
        setShowsRootHandles(true);
        
        setTreeColumnCellRenderer(new NbiTreeTableColumnCellRenderer(treeTable));
        
        setRowHeight(treeTable.getRowHeight());
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        visibleRow = row;
        
        if (selected) {
            setOpaque(true);
            setBackground(treeTable.getSelectionBackground());
            setForeground(treeTable.getSelectionForeground());
        } else {
            setOpaque(false);
            setBackground(treeTable.getBackground());
            setForeground(treeTable.getForeground());
        }
        
        return this;
    }
    
    public void setBounds(int x, int y, int w, int h) {
        if (treeTable != null) {
            super.setBounds(x, 0, w, treeTable.getHeight());
        } else {
            super.setBounds(x, y, w, h);
        }
    }
    
    public void paint(Graphics g) {
        g.translate(0, -visibleRow * getRowHeight());
        super.paint(g);
    }
    
    public NbiTreeTableColumnCellRenderer getTreeColumnCellRenderer() {
        return cellRenderer;
    }
    
    public void setTreeColumnCellRenderer(final NbiTreeTableColumnCellRenderer renderer) {
        cellRenderer = renderer;
        setCellRenderer(renderer);
    }
}
