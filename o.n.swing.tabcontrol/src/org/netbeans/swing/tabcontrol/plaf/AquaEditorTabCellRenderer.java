/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
/*
 * AquaEditorTabCellRenderer.java
 *
 * Created on December 28, 2003, 12:04 AM
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * A tab cell renderer for OS-X.  Basically does its work by subclassing JButton
 * and doing some tricks to use it as a cell renderer, so the Aqua borders do
 * all the heavy eye-candy lifting (how's that for mixed metaphors?)
 *
 * @author Tim Boudreau
 */
final class AquaEditorTabCellRenderer extends AbstractTabCellRenderer {
    private static final AquaTabPainter AquaTabPainter = new AquaTabPainter();
    
    static final int TOP_INSET = 0;
    static final int LEFT_INSET = 3;
    static final int RIGHT_INSET = 6;
    static final int BOTTOM_INSET = 2;
    
    
    private static final ChicletWrapper chiclet = new ChicletWrapper();
    
    public AquaEditorTabCellRenderer() {
        super(AquaTabPainter, AquaTabPainter, AquaTabPainter,
                new Dimension(23, 8));
    }
    
    protected int getCaptionYAdjustment() {
        return 0;
    }
    
    protected int getIconYAdjustment() {
        return -1;
    }
    
    public Dimension getPadding() {
        Dimension d = super.getPadding();
        d.width = isShowCloseButton() && !Boolean.getBoolean("nb.tabs.suppressCloseButton") ? 26 : 18;
        return d;
    }
    
    private static class AquaTabPainter implements TabPainter {
        private static Insets insets = new Insets(TOP_INSET, LEFT_INSET,
                BOTTOM_INSET, RIGHT_INSET);
        
        public AquaTabPainter() {
        }
        
        public Insets getBorderInsets(Component c) {
            boolean leftmost = ((AquaEditorTabCellRenderer) c).isLeftmost();
            
            if (leftmost) {
                return new Insets(TOP_INSET, LEFT_INSET + 4, BOTTOM_INSET,
                        RIGHT_INSET);
            } else {
                return insets;
            }
        }
        
        public void getCloseButtonRectangle(JComponent jc, Rectangle rect,
                Rectangle bounds) {
            boolean rightClip = ((AquaEditorTabCellRenderer) jc).isClipRight();
            boolean leftClip = ((AquaEditorTabCellRenderer) jc).isClipLeft();
            boolean notSupported = !((AbstractTabCellRenderer) jc).isShowCloseButton();
            if (leftClip || rightClip || notSupported) {
                rect.x = -100;
                rect.y = -100;
                rect.width = 0;
                rect.height = 0;
            } else {
                String iconPath = findIconPath((AquaEditorTabCellRenderer) jc);
                Icon icon = TabControlButtonFactory.getIcon(iconPath);
                int iconWidth = icon.getIconWidth();
                int iconHeight = icon.getIconHeight();
                rect.x = bounds.x + bounds.width - iconWidth - 2;
                rect.y = bounds.y + (Math.max(0, bounds.height / 2 - iconHeight / 2));
                rect.width = iconWidth;
                rect.height = iconHeight;
            }
        }
        
        
        /**
         * Returns path of icon which is correct for currect state of tab at given
         * index
         */
        private String findIconPath( AquaEditorTabCellRenderer renderer ) {
            if( renderer.inCloseButton() && renderer.isPressed() ) {
                return "org/netbeans/swing/tabcontrol/resources/mac_close_pressed.png"; // NOI18N
            }
            if( renderer.inCloseButton() ) {
                return "org/netbeans/swing/tabcontrol/resources/mac_close_rollover.png"; // NOI18N
            }
            return "org/netbeans/swing/tabcontrol/resources/mac_close_enabled.png"; // NOI18N
        }
        
        private void paintCloseButton(Graphics g, JComponent c) {
            if (((AbstractTabCellRenderer) c).isShowCloseButton()) {
                
                Rectangle r = new Rectangle(0, 0, c.getWidth(), c.getHeight());
                Rectangle cbRect = new Rectangle();
                getCloseButtonRectangle((JComponent) c, cbRect, r);
                
                //paint close button
                String iconPath = findIconPath( (AquaEditorTabCellRenderer)c );
                Icon icon = TabControlButtonFactory.getIcon( iconPath );
                icon.paintIcon(c, g, cbRect.x, cbRect.y);
            }
        }
        
        public Polygon getInteriorPolygon(Component c) {
            return new Polygon(new int[]{0, c.getWidth(), c.getWidth(), 0}, new int[]{
                0, 0, c.getHeight(), c.getHeight()}, 4);
        }
        
        public boolean isBorderOpaque() {
            return false;
        }
        
        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height) {
            paintCloseButton(g, (JComponent) c);
        }
        
        
        public void paintInterior(Graphics g, Component c) {
            if (true) {
                Rectangle bds = c.getBounds();
                
                boolean rightmost = ((AquaEditorTabCellRenderer) c).isRightmost();
                boolean rightClip = ((AquaEditorTabCellRenderer) c).isClipRight();
                boolean sel = ((AquaEditorTabCellRenderer) c).isSelected();
                boolean active = ((AquaEditorTabCellRenderer) c).isActive();
                boolean pressed = ((AquaEditorTabCellRenderer) c).isPressed();
                boolean leftClip = ((AquaEditorTabCellRenderer) c).isClipLeft();
                boolean leftmost = ((AquaEditorTabCellRenderer) c).isLeftmost();
                boolean closing = pressed
                        && ((AquaEditorTabCellRenderer) c).inCloseButton();
                boolean attention = !pressed && !closing
                        && ((AquaEditorTabCellRenderer) c).isAttention();
                
                //add in a pixel for rightmost/leftmost so we don't clip off
                //antialiasing of the curve
                chiclet.setBounds(0, 0, bds.width, bds.height);
                
                chiclet.setNotch(rightClip, leftClip);
                int state = 0;
                float leftarc = leftmost && !leftClip ? 0.5f : 0f;
                float rightarc = rightmost && !rightClip ? 0.5f : 0f;
                
                if (pressed && (rightClip || leftClip)) {
                    state |= GenericGlowingChiclet.STATE_PRESSED;
                }
                if (active) {
                    state |= GenericGlowingChiclet.STATE_ACTIVE;
                }
                if (sel) {
                    state |= GenericGlowingChiclet.STATE_SELECTED;
                }
                if (closing) {
                    state |= GenericGlowingChiclet.STATE_CLOSING;
                }
                if (attention) {
                    state |= GenericGlowingChiclet.STATE_ATTENTION;
                }
                chiclet.setArcs(leftarc, rightarc, leftarc, rightarc);
                
                chiclet.setState(state);
                chiclet.draw((Graphics2D) g);
                return;
            }
        }
        
        public boolean supportsCloseButton(JComponent c) {
            boolean leftClip = ((AquaEditorTabCellRenderer) c).isClipLeft();
            boolean rightClip = ((AquaEditorTabCellRenderer) c).isClipRight();
            boolean supported = ((AquaEditorTabCellRenderer) c).isShowCloseButton();
            return !leftClip && !rightClip && supported;
        }
        
    }
}
