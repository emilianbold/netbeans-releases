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

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChooseManyOfManyNodeDataTreeCellRenderer extends DefaultTreeCellRenderer {
    protected static Icon FOLDER_OPEN_ICON = new ImageIcon(ChooseManyOfManyNodeDataTreeCellRenderer.class.
        getResource("folder-open.gif")); //NOI18N
    protected static Icon FOLDER_CLOSED_ICON = new ImageIcon(
        ChooseManyOfManyNodeDataTreeCellRenderer.class.getResource("folder-closed.gif")); //NOI18N

    protected String listNodePrefix;

    public ChooseManyOfManyNodeDataTreeCellRenderer(String listNodePrefix) {

        super();
        this.listNodePrefix = listNodePrefix;
        setOpenIcon(FOLDER_OPEN_ICON);
        setClosedIcon(FOLDER_CLOSED_ICON);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean sel, boolean expanded, boolean leaf, int row,
        boolean hasFocus) {
        DefaultMutableTreeNode node;

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (value == null) {
            return this;
        }
        node = (DefaultMutableTreeNode)value;
        String text;
        if (node.getParent() == null) {
            // dealing with root node which never shows up
            text = "Root"; // NOI18N
        } else if (node.getParent().getParent() == null) {
            // dealing with child of root
            text = listNodePrefix + (node.getParent().getIndex(node) + 1);
            setIcon(expanded ? getOpenIcon() : getClosedIcon());
// If I want to look at using the same ones as in Project Navigator
// Not doing it since I dont want to deal with different color depth issues
//            org.openide.util.Utilities.loadImage ("org/openide/loaders/defaultFolder.gif")
        } else {
            // dealing with any other node
            ChooseManyOfManyNodeData data = (ChooseManyOfManyNodeData)node.getUserObject();
            text = data.getLabel();
            setIcon(null);
        }
        setText(text);
        return this;
    }

}
