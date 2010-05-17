/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
