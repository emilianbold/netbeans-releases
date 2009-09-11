/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.kenai.ui.treelist;

import java.awt.event.MouseEvent;
import org.netbeans.modules.kenai.ui.dashboard.LinkButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
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
import org.netbeans.modules.kenai.ui.dashboard.ColorManager;
import org.netbeans.modules.kenai.ui.dashboard.MyProjectNode;
import org.netbeans.modules.kenai.ui.dashboard.CategoryNode;
import org.openide.explorer.propertysheet.PropertySheet;

/**
 * Wrapper for node renderers. Defines appropriate foreground/background colors,
 * borders. Provides expansion button.
 *
 * @author S. Aubrecht
 */
final class RendererPanel extends JPanel {

    static final boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    static final boolean isGtk = "GTK".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    private static final Border NO_FOCUS_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0, 1, 0, ColorManager.getDefault().getDefaultBackground()),
            BorderFactory.createEmptyBorder(1, 1, 0, 1) );
    private static Border INNER_BORDER;

    private static Color expandableRootBackground = null;
    private static Color expandableRootForeground = null;
    private static Color expandableRootSelectedBackground = null;
    private static Color expandableRootSelectedForeground = null;

    private static final Icon EMPTY_ICON = new EmptyIcon();

    private final boolean isRoot;
    private final TreeListNode node;;
    private JButton expander;

    public RendererPanel( final TreeListNode node ) {
        super( new BorderLayout() );

        if( null == expandableRootBackground )
            deriveColorsAndMargin();

        this.node = node;
        isRoot = node.getParent() == null;
        setOpaque(!isRoot || !isAqua || !node.isExpandable());
        if( node.isExpandable() ) {
            expander = new LinkButton(EMPTY_ICON, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    node.setExpanded( !node.isExpanded() );
                }
            });

            add( expander, BorderLayout.WEST );
        } else if( !isRoot ) {
            add( new JLabel( new EmptyIcon() ), BorderLayout.WEST );
        }
    }

    public void configure( Color foreground, Color background, boolean isSelected, boolean hasFocus, int nestingDepth, int rowHeight ) {
        if( isRoot && node.isExpandable() || node instanceof MyProjectNode) {
            foreground = isSelected ? expandableRootSelectedForeground : expandableRootForeground;
            background = isSelected ? expandableRootSelectedBackground : expandableRootBackground;
        } else if (node instanceof CategoryNode) {
            background = isSelected ? expandableRootSelectedBackground : ColorManager.getDefault().getDisabledColor();
        }

        JComponent inner = node.getComponent(foreground, background, isSelected, hasFocus);
        if( node.isExpandable() || !isRoot )
            inner.setBorder(INNER_BORDER);
        add(inner, BorderLayout.CENTER);

        setBackground(background);
        setForeground(foreground);

        if( null != expander ) {
            expander.setIcon( node.isExpanded() ? getExpandedIcon() : getCollapsedIcon() );
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
        if( null == border )
            border = NO_FOCUS_BORDER;
        border = BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(TreeList.INSETS_TOP, TreeList.INSETS_LEFT+nestingDepth*rowHeight/2,
                TreeList.INSETS_BOTTOM, TreeList.INSETS_RIGHT));

        setBorder(border);
    }

    @Override
    public void paintComponent( Graphics g ) {
        if( isRoot && isAqua && node.isExpandable() ) {
            Graphics2D g2d = (Graphics2D) g;
            Paint oldPaint = g2d.getPaint();
            g2d.setPaint( new GradientPaint(0,0, Color.white, 0, getHeight()/2, getBackground()) );
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setPaint(oldPaint);
        } else {
            super.paintComponent(g);
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Component c = SwingUtilities.getDeepestComponentAt(this, event.getX(), event.getY());
        if( c instanceof JComponent ) {
            JComponent jc = (JComponent) c;
            String tooltip = jc.getToolTipText();
            if( null != tooltip )
                return tooltip;
        }
        return super.getToolTipText(event);
    }

    /**
     * Initialize the various colors we will be using.
     * (copied from org.openide.explorer.propertysheet.PropUtils)
     */
    private static void deriveColorsAndMargin() {
        //make sure UIManager constants for property sheet are initialized
        new PropertySheet();

        Color controlColor = UIManager.getColor("control"); //NOI18N

        if (controlColor == null) {
            controlColor = Color.LIGHT_GRAY;
        }

        int red;
        int green;
        int blue;

        boolean windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel".equals( //NOI18N
                UIManager.getLookAndFeel().getClass().getName()
            );

        boolean nimbus = "Nimbus".equals(UIManager.getLookAndFeel().getID()); // NOI18N

        expandableRootBackground = UIManager.getColor("PropSheet.setBackground"); //NOI18N
        expandableRootSelectedBackground = UIManager.getColor("PropSheet.selectedSetBackground"); //NOI18N

        if( nimbus || isGtk ) {
            expandableRootBackground = UIManager.getColor( "Menu.background" );//NOI18N
            expandableRootSelectedBackground = UIManager.getColor("Tree.selectionBackground"); //NOI18N
        }

        if (expandableRootBackground == null) {
            if (expandableRootBackground == null) {
                red = adjustColorComponent(controlColor.getRed(), -25, -25);
                green = adjustColorComponent(controlColor.getGreen(), -25, -25);
                blue = adjustColorComponent(controlColor.getBlue(), -25, -25);
                expandableRootBackground = new Color(red, green, blue);
            }
        }
        if( isAqua )
            expandableRootBackground = new Color( (int)Math.max(0.0, expandableRootBackground.getRed()*0.85)
                    , (int)Math.max(0.0, expandableRootBackground.getGreen()*0.85)
                    , (int)Math.max(0.0, expandableRootBackground.getBlue()*0.85));

        if (expandableRootSelectedBackground == null) {
            Color col = windows ? UIManager.getColor("Table.selectionBackground") //NOI18N
                                : UIManager.getColor("activeCaptionBorder"); //NOI18N

            if (col == null) {
                col = Color.BLUE;
            }

            red = adjustColorComponent(col.getRed(), -25, -25);
            green = adjustColorComponent(col.getGreen(), -25, -25);
            blue = adjustColorComponent(col.getBlue(), -25, -25);
            expandableRootSelectedBackground = new Color(red, green, blue);
        }

        expandableRootForeground = UIManager.getColor("PropSheet.setForeground"); //NOI18N

        if( nimbus || isGtk )
            expandableRootForeground = new Color( UIManager.getColor( "Menu.foreground" ).getRGB() ); //NOI18N

        if (expandableRootForeground == null) {
            expandableRootForeground = UIManager.getColor("Table.foreground"); //NOI18N

            if (expandableRootForeground == null) {
                expandableRootForeground = UIManager.getColor("textText"); // NOI18N

                if (expandableRootForeground == null) {
                    expandableRootForeground = Color.BLACK;
                }
            }
        }

        expandableRootSelectedForeground = UIManager.getColor("PropSheet.selectedSetForeground"); //NOI18N

        if (expandableRootSelectedForeground == null) {
            expandableRootSelectedForeground = UIManager.getColor("Table.selectionForeground"); //NOI18N

            if (expandableRootSelectedForeground == null) {
                expandableRootSelectedForeground = Color.WHITE;
            }
        }
        if( isAqua )
            expandableRootSelectedForeground = Color.black;

        Integer i = (Integer) UIManager.get("netbeans.ps.iconmargin"); //NOI18N

        int iconMargin = 0;
        if (i != null) {
            iconMargin = i.intValue();
        } else {
            if ( windows ) {
                iconMargin = 4;
            } else {
                iconMargin = 0;
            }
        }
        INNER_BORDER = BorderFactory.createEmptyBorder(0, iconMargin, 0, 0);
    }

    /** Adjust an rgb color component.
     * @param base the color, an RGB value 0-255
     * @param adjBright the amount to subtract if base > 128
     * @param adjDark the amount to add if base <=128  */
    private static int adjustColorComponent(int base, int adjBright, int adjDark) {
        if (base > 128) {
            base -= adjBright;
        } else {
            base += adjDark;
        }

        if (base < 0) {
            base = 0;
        }

        if (base > 255) {
            base = 255;
        }

        return base;
    }

    /** Get the icon displayed by an expanded set.  Typically this is just the
     * same icon the look and feel supplies for trees */
    static Icon getExpandedIcon() {
        Icon expandedIcon = UIManager.getIcon(isGtk ? "Tree.gtk_expandedIcon" : "Tree.expandedIcon"); //NOI18N
        assert expandedIcon != null: "no Tree.expandedIcon found"; //NOI18N
        return expandedIcon;
    }

    /** Get the icon displayed by a collapsed set. Typically this is just the
     * icon the look and feel supplies for trees */
    static Icon getCollapsedIcon() {
        Icon collapsedIcon = UIManager.getIcon(isGtk ? "Tree.gtk_collapsedIcon" : "Tree.collapsedIcon"); //NOI18N
        assert collapsedIcon != null: "no Tree.collapsedIcon found"; //NOI18N
        return collapsedIcon;
    }

    private static class EmptyIcon implements Icon {

        public void paintIcon(Component c, Graphics g, int x, int y) {
        }

        public int getIconWidth() {
            return getExpandedIcon().getIconWidth();
        }

        public int getIconHeight() {
            return getExpandedIcon().getIconHeight();
        }
    }
}
