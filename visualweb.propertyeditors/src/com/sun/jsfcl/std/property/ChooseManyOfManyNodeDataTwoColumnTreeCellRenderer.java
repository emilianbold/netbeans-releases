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
package com.sun.jsfcl.std.property;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import com.sun.jsfcl.std.reference.ReferenceDataItem;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChooseManyOfManyNodeDataTwoColumnTreeCellRenderer extends
    ReferenceDataTwoColumnListCellRenderer implements TreeCellRenderer {
    protected static Icon FOLDER_OPEN_ICON = new ImageIcon(
        ChooseManyOfManyNodeDataTwoColumnTreeCellRenderer.class.getResource("folder-open.gif")); //NOI18N
    protected static Icon FOLDER_CLOSED_ICON = new ImageIcon(
        ChooseManyOfManyNodeDataTwoColumnTreeCellRenderer.class.getResource("folder-closed.gif")); //NOI18N

    protected String listNodePrefix;
    Color textSelectionColor;
    Color textNonSelectionColor;
    Color backgroundSelectionColor;
    Color backgroundNonSelectionColor;
    Color borderSelectionColor;
    boolean drawsFocusBorderAroundIcon;

    public ChooseManyOfManyNodeDataTwoColumnTreeCellRenderer(String listNodePrefix) {

        super();
        this.listNodePrefix = listNodePrefix;
        textSelectionColor = UIManager.getColor("Tree.selectionForeground");
        textNonSelectionColor = UIManager.getColor("Tree.textForeground");
        backgroundSelectionColor = UIManager.getColor("Tree.selectionBackground");
        backgroundNonSelectionColor = UIManager.getColor("Tree.textBackground");
        borderSelectionColor = UIManager.getColor("Tree.selectionBorderColor");
        Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");
        drawsFocusBorderAroundIcon = (value != null && ((Boolean)value).
            booleanValue());
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean sel, boolean expanded, boolean leaf, int row,
        boolean hasFocus) {
        DefaultMutableTreeNode node;

        if (sel) {
            setForeground(textSelectionColor);
            leftLabel.setForeground(textSelectionColor);
            rightLabel.setForeground(textSelectionColor);
            setBackground(backgroundSelectionColor);
            leftLabel.setBackground(backgroundSelectionColor);
            rightLabel.setBackground(backgroundSelectionColor);
        } else {
            setForeground(textNonSelectionColor);
            leftLabel.setForeground(textNonSelectionColor);
            rightLabel.setForeground(textNonSelectionColor);
            setBackground(backgroundNonSelectionColor);
            leftLabel.setBackground(backgroundNonSelectionColor);
            rightLabel.setBackground(backgroundNonSelectionColor);
        }
        setComponentOrientation(tree.getComponentOrientation());
        node = (DefaultMutableTreeNode)value;
        String[] labels;
        if (node.getParent() == null) {
            // dealing with root node which never shows up
            labels = new String[] {
                "Root", ""}; // NOI18N
            leftLabel.setIcon(expanded ? FOLDER_OPEN_ICON : FOLDER_CLOSED_ICON);
            wantsSecondLabel = false;
        } else if (node.getParent().getParent() == null) {
            // dealing with child of root
            labels = new String[] {
                listNodePrefix + (node.getParent().getIndex(node) + 1), ""};
            leftLabel.setIcon(expanded ? FOLDER_OPEN_ICON : FOLDER_CLOSED_ICON);
            wantsSecondLabel = false;
        } else {
            // dealing with any other node
            ChooseManyOfManyNodeData data = (ChooseManyOfManyNodeData)node.getUserObject();
            ReferenceDataItem item = (ReferenceDataItem)data.getData();
            labels = getLabels(item);
            leftLabel.setIcon(null);
            wantsSecondLabel = true;
        }
        leftLabel.setText(labels[0]);
        rightLabel.setText(labels[1]);
        invalidate();
        return this;
    }

}
