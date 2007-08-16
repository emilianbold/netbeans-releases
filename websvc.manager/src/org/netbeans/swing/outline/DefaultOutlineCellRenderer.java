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
 * DefaultOutlineTreeCellRenderer.java
 *
 * Created on January 28, 2004, 7:49 PM
 */

package org.netbeans.swing.outline;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreePath;

/** An outline-aware TableCellRenderer which knows how to paint expansion
 * handles and indent child nodes an appropriate amount. 
 *
 * @author  Tim Boudreau
 */
public class DefaultOutlineCellRenderer extends DefaultTableCellRenderer {
    private boolean expanded = false;
    private boolean leaf = true;
    private boolean showHandle = true;
    private int nestingDepth = 0;
    private static final Border expansionBorder = new ExpansionHandleBorder();
    
    /** Creates a new instance of DefaultOutlineTreeCellRenderer */
    public DefaultOutlineCellRenderer() {
    }
    
    /** Overridden to combine the expansion border (whose insets determine how
     * much a child tree node is shifted to the right relative to the ancestor
     * root node) with whatever border is set, as a CompoundBorder.  The expansion
     * border is also responsible for drawing the expansion icon.  */
    public final void setBorder (Border b) {
        if (b == expansionBorder) {
            super.setBorder(b);
        } else {
            super.setBorder(BorderFactory.createCompoundBorder (b, expansionBorder));
        }
    }
    
    private static Icon getDefaultOpenIcon() {
	return UIManager.getIcon("Tree.openIcon"); //NOI18N
    }

    private static Icon getDefaultClosedIcon() {
	return UIManager.getIcon("Tree.closedIcon"); //NOI18N
    }

    private static Icon getDefaultLeafIcon() {
	return UIManager.getIcon("Tree.leafIcon"); //NOI18N
    }
    
    private static Icon getExpandedIcon() {
        return UIManager.getIcon ("Tree.collapsedIcon"); //NOI18N
    }
    
    private static Icon getCollapsedIcon() {
        return UIManager.getIcon ("Tree.expandedIcon"); //NOI18N
    }
    
    static int getNestingWidth() {
        return getExpansionHandleWidth();
    }
    
    static int getExpansionHandleWidth() {
        return getExpandedIcon().getIconWidth();
    }
    
    static int getExpansionHandleHeight() {
        return getExpandedIcon().getIconHeight();
    }
    
    private void setNestingDepth (int i) {
        nestingDepth = i;
    }
    
    private void setExpanded (boolean val) {
        expanded = val;
    }
    
    private void setLeaf (boolean val) {
        leaf = val;
    }
    
    private void setShowHandle (boolean val) {
        showHandle = val;
    }
    
    private boolean isLeaf () {
        return leaf;
    }
    
    private boolean isExpanded () {
        return expanded;
    }
    
    private boolean isShowHandle() {
        return showHandle;
    }
    
    /** Set the nesting depth - the number of path elements below the root.
     * This is set in getTableCellEditorComponent(), and retrieved by the
     * expansion border to determine how far to the right to indent the current
     * node. */
    private int getNestingDepth() {
        return nestingDepth;
    }
    
    /** Get a component that can render cells in an Outline.  If 
     * <code>((Outline) table).isTreeColumnIndex(column)</code> is true,
     * it will paint as indented and with an expansion handle if the 
     * Outline's model returns false from <code>isLeaf</code> for the
     * passed value. 
     * <p>
     * If the column is not the tree column, its behavior is the same as
     * DefaultTableCellRenderer.
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, 
                          int column) {
    
        Component c = (DefaultOutlineCellRenderer) super.getTableCellRendererComponent(
              table, value, isSelected, hasFocus, row, column);
        Outline tbl = (Outline) table;
        if (tbl.isTreeColumnIndex(column)) {
            AbstractLayoutCache layout = tbl.getLayoutCache();
            
            boolean leaf = tbl.getOutlineModel().isLeaf(value);
            setLeaf(leaf);
            setShowHandle(true);
            TreePath path = layout.getPathForRow(row);
            boolean expanded = !layout.isExpanded(path);
            setExpanded (expanded);
            setNestingDepth (path.getPathCount() - 1);
            RenderDataProvider rendata = tbl.getRenderDataProvider();
            Icon icon = null;
            if (rendata != null) {
                String displayName = rendata.getDisplayName(value);
                if (displayName != null) {
                    setText (displayName);
                }
                setToolTipText (rendata.getTooltipText(value));
                Color bg = rendata.getBackground(value);
                Color fg = rendata.getForeground(value);
                if (bg != null && !isSelected) {
                    setBackground (bg);
                } else {
                    setBackground (isSelected ? 
                        tbl.getSelectionBackground() : tbl.getBackground());
                }
                if (fg != null && !isSelected) {
                    setForeground (fg);
                } else {
                    setForeground (isSelected ? 
                        tbl.getSelectionForeground() : tbl.getForeground());
                }
                icon = rendata.getIcon(value);
            } 
            if (icon == null) {
                if (!leaf) {
                    if (expanded) {
                        setIcon (getDefaultClosedIcon());
                    } else {
                        setIcon (getDefaultOpenIcon());
                    }
                } else {
                    setIcon (getDefaultLeafIcon());
                }
            }
        
        } else {
            setIcon(null);
            setShowHandle(false);
        }
        return this;
    }
    
    private static class ExpansionHandleBorder implements Border {
        private Insets insets = new Insets(0,0,0,0);
        public Insets getBorderInsets(Component c) {
            DefaultOutlineCellRenderer ren = (DefaultOutlineCellRenderer) c;
            if (ren.isShowHandle()) {
                insets.left = getExpansionHandleWidth() + (ren.getNestingDepth() *
                    getNestingWidth());
                //Defensively adjust all the insets fields
                insets.top = 1;
                insets.right = 1;
                insets.bottom = 1;
            } else {
                //Defensively adjust all the insets fields
                insets.left = 1;
                insets.top = 1;
                insets.right = 1;
                insets.bottom = 1;
            }
            return insets;
        }
        
        public boolean isBorderOpaque() {
            return false;
        }
        
        public void paintBorder(Component c, java.awt.Graphics g, int x, int y, int width, int height) {
            DefaultOutlineCellRenderer ren = (DefaultOutlineCellRenderer) c;
            if (ren.isShowHandle() && !ren.isLeaf()) {
                Icon icon = ren.isExpanded() ? getExpandedIcon() : getCollapsedIcon();
                int iconY;
                int iconX = ren.getNestingDepth() * getNestingWidth();
                if (icon.getIconHeight() < height) {
                    iconY = (height / 2) - (icon.getIconHeight() / 2);
                } else {
                    iconY = 0;
                }
                icon.paintIcon(c, g, iconX, iconY);
            }
        }
    }
}
