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
package org.netbeans.modules.edm.editor.ui.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 * @author Jonathan Giron
 * @version $Revision$
 */
public class TableColumnTreeCellRenderer extends JPanel implements TreeCellRenderer {

    /** Color to use for the background when the node isn't selected. */
    protected Color backgroundNonSelectionColor;

    /** Color to use for the background when a node is selected. */
    protected Color backgroundSelectionColor;

    /** Color to use for the focus indicator when the node has focus. */
    protected Color borderSelectionColor;

    /** True if has focus. */
    protected boolean hasFocus;

    /** Is the value currently selected. */
    protected boolean selected;

    /** Color to use for the foreground for non-selected nodes. */
    protected Color textNonSelectionColor;

    /** Color to use for the foreground for selected nodes. */
    protected Color textSelectionColor;

    /** Checkbox indicating selection state */
    private JCheckBox checkBox;

    private JTree tree;

    /**
     * Constructor TableColumnTreeCellRenderer constructs this object.
     */
    public TableColumnTreeCellRenderer() {
        super();

        setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
        setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
        setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
        setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
        setBorderSelectionColor(UIManager.getColor("Tree.selectionBorderColor"));

        checkBox = new JCheckBox();
        checkBox.setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 2));
    }

    /**
     * Returns the background color to be used for non selected nodes.
     * 
     * @return Color for the nonselection color
     */
    public Color getBackgroundNonSelectionColor() {
        return backgroundNonSelectionColor;
    }

    /**
     * Returns the color to use for the background if node is selected.
     * 
     * @return Color is the new background selection color.
     */
    public Color getBackgroundSelectionColor() {
        return backgroundSelectionColor;
    }

    /**
     * Returns the color the border is drawn.
     * 
     * @return Color of the border selection
     */
    public Color getBorderSelectionColor() {
        return borderSelectionColor;
    }

    /**
     * Returns the color the text is drawn with when the node isn't selected.
     * 
     * @return Color of the text non selection.
     */
    public Color getTextNonSelectionColor() {
        return textNonSelectionColor;
    }

    /**
     * Returns the color the text is drawn with when the node is selected.
     * 
     * @return Color of text selection
     */
    public Color getTextSelectionColor() {
        return textSelectionColor;
    }

    /**
     * Sets the value of the current tree cell to <code>value</code>. If
     * <code>selected</code> is true, the cell will be drawn as if selected. If
     * <code>expanded</code> is true the node is currently expanded and if
     * <code>leaf</code> is true the node represets a leaf and if <code>hasFocus</code>
     * is true the node currently has focus. <code>tree</code> is the <code>JTree</code>
     * the receiver is being configured for. Returns the <code>Component</code> that the
     * renderer uses to draw the value.
     * 
     * @param theTree is the tree whose renderer component is requested
     * @param value is the object that is to be in focus
     * @param selected1 is true if the object is selected
     * @param expanded is true if the object is expanded
     * @param leaf is true if the object is a leaf node
     * @param row is the row of the object
     * @param hasFocus1 is true if the object has focus
     * @return the <code>Component</code> that the renderer uses to draw the value
     */
    public Component getTreeCellRendererComponent(JTree theTree, Object value, boolean selected1, boolean expanded, boolean leaf, int row,
            boolean hasFocus1) {
        setTree(theTree);
        this.selected = selected1;
        this.hasFocus = hasFocus1;

        DefaultTreeCellRenderer dtc = new DefaultTreeCellRenderer();
        dtc.setComponentOrientation(tree.getComponentOrientation());

        JLabel label = (JLabel) dtc.getTreeCellRendererComponent(theTree, value, selected1, expanded, leaf, row, hasFocus1);

        TableColumnNode node = null;
        if (value instanceof TableColumnNode) {
            node = (TableColumnNode) value;
        }
        if (node == null) {
            return label;
        }

        this.removeAll();
        this.setLayout(new GridBagLayout());
        this.setBackground(label.getBackground());
        checkBox.setBackground(label.getBackground());

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 0.0;
        gc.weighty = 0.0;
        gc.anchor = GridBagConstraints.LINE_START;
        gc.fill = GridBagConstraints.NONE;
        gc.gridwidth = 1;
        this.add(checkBox, gc);

        gc = new GridBagConstraints();
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        gc.anchor = GridBagConstraints.LINE_START;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = 1;
        this.add(label, gc);

        label.setText(node.getName());
        label.setIcon(new ImageIcon(node.getIcon(0)));
        checkBox.setSelected(node.isSelected());

        if (!node.isEnabled()) {
            checkBox.setEnabled(false);
            checkBox.setFocusable(false);
            label.setEnabled(false);
            label.setFocusable(false);
        } else {
            checkBox.setEnabled(true);
            checkBox.setFocusable(true);
        }

        this.setToolTipText(node.getToolTip());
        return this;
    }

    /**
     * method isMouseClickOnCheckBox is used to determine if the user has clicked the
     * mouse on the checkbox.
     * 
     * @param p is the point to check
     * @return boolean true if the user has clicked on the checkbox.
     */
    public boolean isMouseClickOnCheckBox(Point p) {
        return checkBox.contains(p);
    }

    /**
     * Sets the background color to be used for non selected nodes.
     * 
     * @param newColor is the new color for background nonselection
     */
    public void setBackgroundNonSelectionColor(Color newColor) {
        backgroundNonSelectionColor = newColor;
    }

    /**
     * Sets the color to use for the background if node is selected.
     * 
     * @param newColor is the new background selection color to use.
     */
    public void setBackgroundSelectionColor(Color newColor) {
        backgroundSelectionColor = newColor;
    }

    /**
     * Sets the color to use for the border.
     * 
     * @param newColor is the new color for border selection
     */
    public void setBorderSelectionColor(Color newColor) {
        borderSelectionColor = newColor;
    }

    /**
     * Sets the color the text is drawn with when the node isn't selected.
     * 
     * @param newColor is the new color for text nonselection
     */
    public void setTextNonSelectionColor(Color newColor) {
        textNonSelectionColor = newColor;
    }

    /**
     * Sets the color the text is drawn with when the node is selected.
     * 
     * @param newColor is the new color for text selection
     */
    public void setTextSelectionColor(Color newColor) {
        textSelectionColor = newColor;
    }

    private void setTree(JTree tree) {
        this.tree = tree;
    }
}

