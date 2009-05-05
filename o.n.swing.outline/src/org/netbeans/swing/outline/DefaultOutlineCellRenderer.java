/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.swing.outline;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
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
    private static int expansionHandleWidth = 0;
    private static int expansionHandleHeight = 0;
    private boolean expanded = false;
    private boolean leaf = true;
    private boolean showHandle = true;
    private int nestingDepth = 0;
    private final JCheckBox theCheckBox;
    private final CellRendererPane fakeCellRendererPane;
    private JCheckBox checkBox;
    private Reference<RenderDataProvider> lastRendererRef = new WeakReference<RenderDataProvider>(null); // Used by lazy tooltip
    private Reference<Object> lastRenderedValueRef = new WeakReference<Object>(null);                    // Used by lazy tooltip
    private static final Border expansionBorder = new ExpansionHandleBorder();
    
    /** Creates a new instance of DefaultOutlineTreeCellRenderer */
    public DefaultOutlineCellRenderer() {
        theCheckBox = new JCheckBox();
        theCheckBox.setSize(theCheckBox.getPreferredSize());
        theCheckBox.setBorderPainted(false);
        theCheckBox.setOpaque(false);
        // In order to paint the check-box correctly, following condition must be true:
        // SwingUtilities.getAncestorOfClass(CellRendererPane.class, theCheckBox) != null
        // (See e.g.: paintSkin() method in com/sun/java/swing/plaf/windows/XPStyle.java)
        fakeCellRendererPane = new CellRendererPane();
        fakeCellRendererPane.add(theCheckBox);
    }
    
    /** Overridden to combine the expansion border (whose insets determine how
     * much a child tree node is shifted to the right relative to the ancestor
     * root node) with whatever border is set, as a CompoundBorder.  The expansion
     * border is also responsible for drawing the expansion icon.
     * @param b the border to be rendered for this component
     */
    @Override
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
    
    static Icon getExpandedIcon() {
        return UIManager.getIcon ("Tree.expandedIcon"); //NOI18N
    }
    
    static Icon getCollapsedIcon() {
        return UIManager.getIcon ("Tree.collapsedIcon"); //NOI18N
    }
    
    static int getNestingWidth() {
        return getExpansionHandleWidth();
    }

    static int getExpansionHandleWidth() {
        if (expansionHandleWidth == 0) {
            expansionHandleWidth = getExpandedIcon ().getIconWidth ();
        }
        return expansionHandleWidth;
    }

    static int getExpansionHandleHeight() {
        if (expansionHandleHeight == 0) {
            expansionHandleHeight = getExpandedIcon ().getIconHeight ();
        }
        return expansionHandleHeight;
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

    private void setCheckBox(JCheckBox checkBox) {
        this.checkBox = checkBox;
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

    private JCheckBox getCheckBox() {
        return checkBox;
    }

    int getTheCheckBoxWidth() {
        return theCheckBox.getSize().width;
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
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, 
                          int column) {
    
        Component c = (DefaultOutlineCellRenderer) super.getTableCellRendererComponent(
              table, value, isSelected, hasFocus, row, column);
        Outline tbl = (Outline) table;
        if (tbl.isTreeColumnIndex(column)) {
            AbstractLayoutCache layout = tbl.getLayoutCache();
            row = tbl.convertRowIndexToModel(row);
            boolean isleaf = tbl.getOutlineModel().isLeaf(value);
            setLeaf(isleaf);
            setShowHandle(true);
            TreePath path = layout.getPathForRow(row);
            boolean isExpanded = layout.isExpanded(path);
            setExpanded (isExpanded);
            int nd = path.getPathCount() - (tbl.isRootVisible() ? 1 : 2);
            if (nd < 0) {
                nd = 0;
            }
            setNestingDepth (nd );
            RenderDataProvider rendata = tbl.getRenderDataProvider();
            Icon icon = null;
            if (rendata != null && value != null) {
                String displayName = rendata.getDisplayName(value);
                if (displayName != null) {
                    setText (displayName);
                }
                lastRendererRef = new WeakReference<RenderDataProvider>(rendata);
                lastRenderedValueRef = new WeakReference<Object>(value);
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

                JCheckBox cb = null;
                if (rendata instanceof CheckRenderDataProvider) {
                    CheckRenderDataProvider crendata = (CheckRenderDataProvider) rendata;
                    if (crendata.isCheckable(value)) {
                        cb = theCheckBox;
                        Boolean chSelected = crendata.isSelected(value);
                        cb.setSelected(!Boolean.FALSE.equals(chSelected));
                        // Third state is "selected armed" to be consistent with org.openide.explorer.propertysheet.ButtonModel3Way
                        cb.getModel().setArmed(chSelected == null);
                        cb.getModel().setPressed(chSelected == null);
                        cb.setEnabled(crendata.isCheckEnabled(value));
                        cb.setBackground(getBackground());
                    }
                }
                setCheckBox(cb);
            } 
            if (icon == null) {
                if (!isleaf) {
                    if (isExpanded) {
                        setIcon (getDefaultOpenIcon());
                    } else { // ! expanded
                        setIcon (getDefaultClosedIcon());
                    }
                } else { // leaf
                    setIcon (getDefaultLeafIcon());
                }
            } else { // icon != null
                setIcon(icon);
            }
        
        } else { // ! tbl.isTreeColumnIndex(column)
            setIcon(null);
            setShowHandle(false);
            }
        return this;
    }

    @Override
    public String getToolTipText() {
        // Retrieve the tooltip only when someone asks for it...
        RenderDataProvider rendata = lastRendererRef.get();
        Object value = lastRenderedValueRef.get();
        if (rendata != null && value != null) {
            String toolT = rendata.getTooltipText(value);
            if (toolT != null && (toolT = toolT.trim ()).length () > 0) {
                return toolT;
            }
        }
        return super.getToolTipText();
    }

    private static class ExpansionHandleBorder implements Border {

        private static final boolean isGtk = "GTK".equals (UIManager.getLookAndFeel ().getID ()); //NOI18N

        private Insets insets = new Insets(0,0,0,0);
        private static JLabel lExpandedIcon = null;
        private static JLabel lCollapsedIcon = null;

        {
            if (isGtk) {
                lExpandedIcon = new JLabel (getExpandedIcon (), SwingUtilities.TRAILING);
                lCollapsedIcon = new JLabel (getCollapsedIcon (), SwingUtilities.TRAILING);
            }
        }

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
            if (ren.getCheckBox() != null) {
                insets.left += ren.getCheckBox().getSize().width;
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
                if (isGtk) {
                    JLabel lbl = ren.isExpanded () ? lExpandedIcon : lCollapsedIcon;
                    lbl.setSize (Math.max (getExpansionHandleWidth (), iconX + getExpansionHandleWidth ()), height);
                    lbl.paint (g);
                } else {
                    icon.paintIcon(c, g, iconX, iconY);
                }
            }
            JCheckBox chBox = ren.getCheckBox();
            if (chBox != null) {
                int chBoxX = getExpansionHandleWidth() + ren.getNestingDepth() * getNestingWidth();
                Dimension chDim = chBox.getSize();
                java.awt.Graphics gch = g.create(chBoxX, 0, chDim.width, chDim.height);
                chBox.paint(gch);
            }
        }
    }
}
