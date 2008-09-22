/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR parent HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of parent file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use parent file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include parent License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates parent
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied parent code. If applicable, add the following below the
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
 * If you wish your version of parent file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include parent software in parent distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of parent file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.dataview.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * A better-looking table than JTable.
 *
 * In particular, on Mac OS this looks more like a Cocoa table than the default Aqua LAF manages.
 * Likewise Linux and the GTK+ LAF. We also fill the entirety of any enclosing JScrollPane by default.
 */
public class ExtendedJTable extends JTable {

    private static final Color MAC_FOCUSED_SELECTED_CELL_HORIZONTAL_LINE_COLOR = new Color(0x7daaea);
    private static final Color MAC_UNFOCUSED_SELECTED_CELL_HORIZONTAL_LINE_COLOR = new Color(0xe0e0e0);
    private static final Color MAC_FOCUSED_UNSELECTED_VERTICAL_LINE_COLOR = new Color(0xd9d9d9);
    private static final Color MAC_FOCUSED_SELECTED_VERTICAL_LINE_COLOR = new Color(0x346dbe);
    private static final Color MAC_UNFOCUSED_UNSELECTED_VERTICAL_LINE_COLOR = new Color(0xd9d9d9);
    private static final Color MAC_UNFOCUSED_SELECTED_VERTICAL_LINE_COLOR = new Color(0xacacac);

    public ExtendedJTable() {
        // Although it's the JTable default, most systems' tables don't draw a grid by default.
        // The Aqua and GTK LAFs ignore the grid settings anyway, so this causes no change there.
        setShowGrid(false);

        // Tighten the cells up, and enable the manual painting of the vertical grid lines.
        setIntercellSpacing(new Dimension());

        // Table column re-ordering is too badly implemented to enable.
        getTableHeader().setReorderingAllowed(false);

        if (isMacOs()) {
            // Work around Apple 4352937 (fixed in 10.5).
            if (System.getProperty("os.version").startsWith("10.4")) {
                ((JLabel) getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEADING);
            }
        }
        // Use an iTunes-style vertical-only "grid".
        setShowHorizontalLines(false);
        setShowVerticalLines(true);

    }

    /**
     * Paints empty rows too, after letting the UI delegate do
     * its painting.
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintEmptyRows(g);
    }

    /**
     * Paints the backgrounds of the implied empty rows when the
     * table model is insufficient to fill all the visible area
     * available to us. We don't involve cell renderers, because
     * we have no data.
     */
    protected void paintEmptyRows(Graphics g) {
        final int rowCount = getRowCount();
        final Rectangle clip = g.getClipBounds();
        final int height = clip.y + clip.height;
        if (rowCount * rowHeight < height) {
            for (int i = rowCount; i <= height / rowHeight; ++i) {
                g.setColor(backgroundColorForRow(i));
                g.fillRect(clip.x, i * rowHeight, clip.width, rowHeight);
            }

            // Mac OS' Aqua LAF never draws vertical grid lines, so we have to draw them ourselves.
            if (getShowVerticalLines()) {
                g.setColor(MAC_UNFOCUSED_UNSELECTED_VERTICAL_LINE_COLOR);
                TableColumnModel colModel = getColumnModel();
                int x = 0;
                for (int i = 0; i < colModel.getColumnCount(); ++i) {
                    TableColumn column = colModel.getColumn(i);
                    x += column.getWidth();
                    g.drawLine(x - 1, rowCount * rowHeight, x - 1, height);
                }
            }
        }
    }
    private static final Color MAC_OS_ALTERNATE_ROW_COLOR = new Color(0.92f, 0.95f, 0.99f);

    /**
     * Returns the appropriate background color for the given row index.
     */
    public static Color backgroundColorForRow(int row) {
        if (UIManager.getLookAndFeel().getClass().getName().contains("GTK")) {
            return (row % 2 == 0) ? Color.WHITE : UIManager.getColor("Table.background");
        } else if (System.getProperty("os.name").contains("Mac")) {
            return (row % 2 == 0) ? Color.WHITE : MAC_OS_ALTERNATE_ROW_COLOR;
        } else if (System.getProperty("os.name").contains("Win")) {
            return (row % 2 == 0) ? Color.WHITE : MAC_OS_ALTERNATE_ROW_COLOR;
        }
        return UIManager.getColor("Table.background");
    }

    /**
     * Changes the behavior of a table in a JScrollPane to be more like
     * the behavior of JList, which expands to fill the available space.
     * JTable normally restricts its size to just what's needed by its
     * model.
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            JViewport parent = (JViewport) getParent();
            return (parent.getHeight() > getPreferredSize().height);
        }
        return false;
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        return prepareComponent(super.prepareRenderer(renderer, row, column), row, column);
    }

    @Override
    public Component prepareEditor(TableCellEditor editor, int row, int column) {
        return prepareComponent(super.prepareEditor(editor, row, column), row, column);
    }

    private Component prepareComponent(Component c, int row, int column) {
        boolean focused = hasFocus();
        boolean selected = isCellSelected(row, column);
        if (!selected) {
            c.setBackground(backgroundColorForRow(row));
        }

        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;

            if (isGtk() && c instanceof JCheckBox) {
                // The Java 6 GTK LAF JCheckBox doesn't paint its background by default.
                // Sun 5043225 says this is the intended behavior, though presumably not when it's being used as a table cell renderer.
                jc.setOpaque(true);
            } else if (isMacOs() && c instanceof JCheckBox) {
                // There's a similar situation on Mac OS.
                jc.setOpaque(true);
                // Mac OS 10.5 lets us use smaller checkboxes in table cells.
                ((JCheckBox) jc).putClientProperty("JComponent.sizeVariant", "mini");
            }

            if (getCellSelectionEnabled() == false) {
                jc.setBorder(new AquaTableCellBorder(selected, focused, getShowVerticalLines()));
            }
        }
        return c;
    }

    /**
     * Native Mac OS doesn't draw a border on the selected cell, but it does various things that we can emulate with a custom cell border.
     */
    private static class AquaTableCellBorder extends AbstractBorder {

        private boolean selected;
        private boolean focused;
        private boolean verticalLines;

        public AquaTableCellBorder(boolean selected, boolean focused, boolean verticalLines) {
            this.selected = selected;
            this.focused = focused;
            this.verticalLines = verticalLines;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            // Native tables draw a horizontal line under the whole selected row.
            if (selected) {
                g.setColor(focused ? MAC_FOCUSED_SELECTED_CELL_HORIZONTAL_LINE_COLOR : MAC_UNFOCUSED_SELECTED_CELL_HORIZONTAL_LINE_COLOR);
                g.drawLine(x, y + height - 1, x + width, y + height - 1);
            }

            // Mac OS' Aqua LAF never draws vertical grid lines, so we have to draw them ourselves.
            if (verticalLines) {
                if (focused) {
                    g.setColor(selected ? MAC_FOCUSED_SELECTED_VERTICAL_LINE_COLOR : MAC_FOCUSED_UNSELECTED_VERTICAL_LINE_COLOR);
                } else {
                    g.setColor(selected ? MAC_UNFOCUSED_SELECTED_VERTICAL_LINE_COLOR : MAC_UNFOCUSED_UNSELECTED_VERTICAL_LINE_COLOR);
                }
                g.drawLine(x + width - 1, y, x + width - 1, y + height);
            }
        }

        @Override
        public Insets getBorderInsets(Component c) {
            // Defer to getBorderInsets(Component c, Insets insets)...
            Insets result = new Insets(0, 0, 0, 0);
            return getBorderInsets(c, result);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            // FIXME: the whole reason this class exists is because Apple's LAF doesn't like insets other than these, so this might be fragile if they update the LAF.
            insets.left = insets.top = insets.right = insets.bottom = 1;
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }

    /**
     * Improve the appearance of of a table in a JScrollPane on Mac OS, where there's otherwise an unsightly hole.
     */
    @Override
    protected void configureEnclosingScrollPane() {
        super.configureEnclosingScrollPane();

        if (isMacOs() == false) {
            return;
        }

        Container p = getParent();
        if (p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) gp;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null || viewport.getView() != this) {
                    return;
                }

                // JTable copy & paste above this point; our code below.
                // Remove the scroll pane's focus ring.
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                // Put a dummy header in the upper-right corner.
                final Component renderer = new JTableHeader().getDefaultRenderer().getTableCellRendererComponent(null, "", false, false, -1, 0);
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(renderer, BorderLayout.CENTER);
                scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, panel);
            }
        }
    }

    /**
     * Tests whether we're running on Mac OS. 
     */
    private static boolean isMacOs() {
        return System.getProperty("os.name").contains("Mac");
    }

    /**
     * Tests whether we're using the GTK+ LAF (and so are probably on Linux or Solaris).
     */
    private static boolean isGtk() {
        return UIManager.getLookAndFeel().getClass().getName().contains("GTK");
    }
}
