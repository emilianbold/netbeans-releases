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
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import java.util.logging.Logger;
import org.netbeans.modules.edm.model.SQLDBColumn;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.openide.util.NbBundle;


/**
 * @author Jonathan Giron
 * @version $Revision$
 */
public class TableColumnTreeCellEditor extends JComponent implements TreeCellEditor {
 private static transient final Logger mLogger = Logger.getLogger(TableColumnTreeCellEditor.class.getName());
    /**
     * Class CheckBoxItemListener listens for checkbox changes.
     */
    private class CheckBoxItemListener implements ItemListener {

        private void selectTableAndColumn(boolean isSelected) {
            Enumeration e = node.children();
            while (e.hasMoreElements()) {
                TableColumnNode child = (TableColumnNode) e.nextElement();
                if (child.isEnabled()) {
                    child.setSelected(isSelected);
                }
            }
            node.setSelected(isSelected);
        }

        /**
         * Invoked when an item has been selected or deselected by the user. The code
         * written for this method performs the operations that need to occur when an item
         * is selected (or deselected).
         *
         * @param e ItemEvent to handle
         */
        public void itemStateChanged(ItemEvent e) {
            boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);

            if (node.getUserObject() instanceof SQLDBTable) {
                if (!isSelected) {
                    String message = NbBundle.getMessage(TableColumnTreeCellEditor.class, "MSG_deselect_all_table_columns");
                    String header = NbBundle.getMessage(TableColumnTreeCellEditor.class, "TITLE_Confirm_deselect_all");
                    int response = JOptionPane.showConfirmDialog(parent, message, header, JOptionPane.YES_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        selectTableAndColumn(isSelected);
                    }
                }else {
                    selectTableAndColumn(isSelected);
                }
            } else if (node.getUserObject() instanceof SQLDBColumn) {
                node.setSelected(isSelected);
                TableColumnNode tableNode = (TableColumnNode) node.getParent();
                if (tableNode != null) {
                    tableNode.setSelectedBasedOnChildren();
                }
            }

            if (tree != null) {
                tree.repaint();
            }
        }

    }

    /** Parent dialog **/
    private JComponent parent = null;

    /** Color to use for the background when the node isn't selected. */
    protected Color backgroundNonSelectionColor;

    /** Color to use for the background when a node is selected. */
    protected Color backgroundSelectionColor;

    /** Color to use for the focus indicator when the node has focus. */
    protected Color borderSelectionColor;

    /** True if has focus. */
    protected boolean hasFocus;

    /**
     * Used in editing. Indicates x position to place <code>editingComponent</code>.
     */
    protected transient int offset;

    /** Is the value currently selected. */
    protected boolean selected;

    /** Color to use for the foreground for non-selected nodes. */
    protected Color textNonSelectionColor;

    // Colors
    /** Color to use for the foreground for selected nodes. */
    protected Color textSelectionColor;

    /** Checkbox indicating selection state */
    //private JCheckBox checkBox;

    private transient int lastRow;

    private List listeners = new ArrayList();

    private TableColumnNode node;

    private JTree tree;


    /**
     * Constructor TableColumnTreeCellEditor constructs this object.
     */
    public TableColumnTreeCellEditor(JComponent prnt) {
        this.parent = prnt;
    }

    /**
     * @see javax.swing.CellEditor#addCellEditorListener(CellEditorListener)
     */
    public void addCellEditorListener(CellEditorListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * @see javax.swing.CellEditor#cancelCellEditing()
     */
    public void cancelCellEditing() {
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
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    public Object getCellEditorValue() {
        return (node != null) ? node.getUserObject() : null;
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
     * @see javax.swing.tree.TreeCellEditor#getTreeCellEditorComponent(JTree, Object,
     *      boolean, boolean, boolean, int)
     */
    public Component getTreeCellEditorComponent(JTree aTree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        setTree(aTree);

        DefaultTreeCellRenderer dtc = new DefaultTreeCellRenderer();
        dtc.setComponentOrientation(tree.getComponentOrientation());

        JLabel label = (JLabel) dtc.getTreeCellRendererComponent(aTree, value, selected, expanded, leaf, row, hasFocus);

        node = null;
        if (value instanceof TableColumnNode) {
            node = (TableColumnNode) value;
        }
        if (node == null) {
            return label;
        }

        this.removeAll();
        this.setLayout(new GridBagLayout());
        this.setBackground(label.getBackground());
        JCheckBox checkBox = new JCheckBox();
        checkBox.setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 2));
        checkBox.addItemListener(new CheckBoxItemListener());
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
        return this;
    }

    /**
     * @see javax.swing.CellEditor#isCellEditable(EventObject)
     */
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent != null && anEvent instanceof MouseEvent && (((MouseEvent) anEvent).getClickCount() == 1)
            && isMouseClickOnCheckBox(((MouseEvent) anEvent))) {
            return true;
        }
        return false;
    }

    /**
     * method isMouseClickOnCheckBox returns true if the mouse event is clicked on a
     * checkbox.
     *
     * @param event is the mouse event to be used.
     * @return boolean true if the mouse is checked.
     */
    public boolean isMouseClickOnCheckBox(MouseEvent event) {
        if (event != null) {
            if (event.getSource() instanceof JTree) {
                setTree((JTree) event.getSource());
                TreeCellRenderer renderer = tree.getCellRenderer();
                if (renderer instanceof TableColumnTreeCellRenderer) {
                    if (lastRow != -1 && tree != null) {
                        TreePath path = tree.getUI().getClosestPathForLocation(tree, event.getX(), event.getY());
                        Rectangle bounds = tree.getUI().getPathBounds(tree, path);
                        Point p = event.getPoint();

                        if (bounds.contains(p)) {
                            p.x = p.x - bounds.x;
                            p.y = p.y - bounds.y;
                        }

                        return ((TableColumnTreeCellRenderer) renderer).isMouseClickOnCheckBox(p);
                    }
                }
            }
        }
        return true;
    }

    /**
     * method isMouseClickOnCheckBox is used to determine if the user has clicked the
     * mouse on the checkbox.
     *
     * @param p is the point to check
     * @return boolean true if the user has clicked on the checkbox.
     */
    //public boolean isMouseClickOnCheckBox(Point p) {
    //    return checkBox.contains(p);
    //}

    /**
     * @see javax.swing.CellEditor#removeCellEditorListener(CellEditorListener)
     */
    public void removeCellEditorListener(CellEditorListener l) {
        listeners.remove(l);
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

    /**
     * @see javax.swing.CellEditor#shouldSelectCell(EventObject)
     */
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    /**
     * @see javax.swing.CellEditor#stopCellEditing()
     */
    public boolean stopCellEditing() {
        return true;
    }

    /**
     * Returns true if the passed in location is a valid mouse location to start editing
     * from. This is implemented to return false if <code>x</code> is <= the width of
     * the icon and icon gap displayed by the renderer. In other words this returns true
     * if the user clicks over the text part displayed by the renderer, and false
     * otherwise.
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return true if the passed in location is a valid mouse location
     * @todo implement this correctly
     */
    protected boolean inHitRegion(int x, int y) {
        if (lastRow != -1 && tree != null) {
            //Rectangle bounds = tree.getRowBounds(lastRow);
            // if (bounds != null && x <= (bounds.x + offset)
            // && offset < (bounds.width -5))
            // { return false; }
        }
        return true;
    }

    private void setTree(JTree tree) {
        this.tree = tree;
    }
}

