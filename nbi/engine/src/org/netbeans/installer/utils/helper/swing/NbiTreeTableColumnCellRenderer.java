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
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

public class NbiTreeTableColumnCellRenderer extends JLabel implements TreeCellRenderer {
    protected NbiTreeTable treeTable;
    
    public NbiTreeTableColumnCellRenderer(final NbiTreeTable treeTable) {
        this.treeTable = treeTable;
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        // we base our assumption on which node is selected on whether this row is 
        // selected in the table, since the selection is not propagated to the tree
        if (row == treeTable.getSelectedRow()) {
            setOpaque(true);
            setForeground(treeTable.getSelectionForeground());
            setBackground(treeTable.getSelectionBackground());
        } else {
            setOpaque(false);
            setForeground(treeTable.getForeground());
            setBackground(treeTable.getBackground());
        }
        
        setText(value.toString());
        
        return this;
    }
}