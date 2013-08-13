/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.commons.treelist;

import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Wrapper for node renderers. Defines appropriate foreground/background colors,
 * borders. Provides expansion button.
 *
 * @author S. Aubrecht
 */
final class RendererPanel extends JPanel {

    private static final ColorManager colorManager = ColorManager.getDefault();
    private static final Border NO_FOCUS_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ColorManager.getDefault().getDefaultBackground()),
            BorderFactory.createEmptyBorder(1, 1, 0, 1));
    private static Border INNER_BORDER;
    private static Color expandableRootBackground = null;
    private static Color expandableRootForeground = null;
    private static Color expandableRootSelectedBackground = null;
    private static Color expandableRootSelectedForeground = null;
    private static final Icon EMPTY_ICON = new EmptyIcon();
    private final boolean isRoot;
    private final TreeListNode node;
    ;
    private JButton expander;
    private int depth = 0;

    public RendererPanel(final TreeListNode node) {
        super(new BorderLayout());

        if (null == expandableRootBackground) {
            deriveColorsAndMargin();
        }

        this.node = node;
        isRoot = node.getParent() == null;
        setOpaque(!isRoot || !colorManager.isAqua() || !node.isExpandable() || node.getType().equals(TreeListNode.Type.TITLE) );
        if (node.isExpandable()) {
            expander = new LinkButton(EMPTY_ICON, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    node.setExpanded(!node.isExpanded());
                }
            });

            add(expander, BorderLayout.WEST);
        } else if (!isRoot || node.getType().equals(TreeListNode.Type.CLOSED)) {
            add(new JLabel(new EmptyIcon()), BorderLayout.WEST);
        }
        depth = getDepth();
    }
    
    private int getDepth() {
        int d = 1;
        TreeListNode parent = node;
        while (parent.getParent() != null) {
            parent = parent.getParent();
            d++;
        }

        return d;
    }

    public void configure(Color foreground, Color background, boolean isSelected, boolean hasFocus, int nestingDepth, int rowHeight, int rowWidth) {
        if (isRoot && node.isExpandable() || node.getType().equals(TreeListNode.Type.CLOSED)) {
            foreground = isSelected ? expandableRootSelectedForeground : expandableRootForeground;
            background = isSelected ? expandableRootSelectedBackground : expandableRootBackground;
        } else if (node.getType().equals(TreeListNode.Type.TITLE)) {
            foreground = isSelected ? expandableRootSelectedForeground : ColorManager.getDefault().getDefaultBackground();
            background = isSelected ? expandableRootSelectedBackground : ColorManager.getDefault().getDisabledColor();
        }
        int maxWidth = rowWidth - depth * EMPTY_ICON.getIconWidth() - (TreeList.INSETS_LEFT + nestingDepth * rowHeight / 2) - TreeList.INSETS_RIGHT;
        JComponent inner = node.getComponent(foreground, background, isSelected, hasFocus, maxWidth > 0 ? maxWidth : 0);
        if (node.isExpandable() || !isRoot || node.getType().equals(TreeListNode.Type.CLOSED)) {
            inner.setBorder(INNER_BORDER);
        }
        add(inner, BorderLayout.CENTER);

        setBackground(background);
        setForeground(foreground);
        
        if (null != expander) {
            expander.setIcon(node.isExpanded() ? getExpandedIcon() : getCollapsedIcon());
            expander.setPressedIcon(expander.getIcon());
        }
        Border border = null;
        if (hasFocus) {
            if (isSelected) {
                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder"); // NOI18N
            }
            if (border == null) {
                border = UIManager.getBorder("List.focusCellHighlightBorder"); // NOI18N
            }
        }
        if (null == border) {
            border = NO_FOCUS_BORDER;
        }
        border = BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(TreeList.INSETS_TOP, TreeList.INSETS_LEFT + nestingDepth * rowHeight / 2,
                TreeList.INSETS_BOTTOM, TreeList.INSETS_RIGHT));

        try {
            setBorder(border);
        } catch (NullPointerException npe) {
            //workaround for 175940
            Logger.getLogger(RendererPanel.class.getName()).log(Level.INFO, "Bug #175940", npe);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if (isRoot && colorManager.isAqua() && node.isExpandable() && node.isRenderedWithGradient()) {
            Graphics2D g2d = (Graphics2D) g;
            Paint oldPaint = g2d.getPaint();
            g2d.setPaint(new GradientPaint(0, 0, Color.white, 0, getHeight() / 2, getBackground()));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setPaint(oldPaint);
        } else {
            super.paintComponent(g);
        }
    }
    
    @Override
    public String getToolTipText(MouseEvent event) {
        Component c = SwingUtilities.getDeepestComponentAt(this, event.getX(), event.getY());
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            String tooltip = jc.getToolTipText();
            if (null != tooltip) {
                return tooltip;
            }
        }
        return super.getToolTipText(event);
    }

    /**
     * Initialize the various colors we will be using. (copied from
     * org.openide.explorer.propertysheet.PropUtils)
     */
    private static void deriveColorsAndMargin() {
        expandableRootBackground = colorManager.getExpandableRootBackground();
        expandableRootForeground = colorManager.getExpandableRootForeground();
        expandableRootSelectedBackground = colorManager.getExpandableRootSelectedBackground();
        expandableRootSelectedForeground = colorManager.getExpandableRootSelectedForeground();

        Integer i = (Integer) UIManager.get("netbeans.ps.iconmargin"); //NOI18N

        int iconMargin = 0;
        if (i != null) {
            iconMargin = i.intValue();
        } else {
            if (colorManager.isWindows()) {
                iconMargin = 4;
            } else {
                iconMargin = 0;
            }
        }
        INNER_BORDER = BorderFactory.createEmptyBorder(0, iconMargin, 0, 0);
    }

    /**
     * Get the icon displayed by an expanded set. Typically this is just the
     * same icon the look and feel supplies for trees
     */
    static Icon getExpandedIcon() {
        Icon expandedIcon = UIManager.getIcon(colorManager.isGtk() ? "Tree.gtk_expandedIcon" : "Tree.expandedIcon"); //NOI18N
        assert expandedIcon != null : "no Tree.expandedIcon found"; //NOI18N
        return expandedIcon;
    }

    /**
     * Get the icon displayed by a collapsed set. Typically this is just the
     * icon the look and feel supplies for trees
     */
    static Icon getCollapsedIcon() {
        Icon collapsedIcon = UIManager.getIcon(colorManager.isGtk() ? "Tree.gtk_collapsedIcon" : "Tree.collapsedIcon"); //NOI18N
        assert collapsedIcon != null : "no Tree.collapsedIcon found"; //NOI18N
        return collapsedIcon;
    }

    private static class EmptyIcon implements Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        @Override
        public int getIconWidth() {
            return getExpandedIcon().getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return getExpandedIcon().getIconHeight();
        }
    }
}
