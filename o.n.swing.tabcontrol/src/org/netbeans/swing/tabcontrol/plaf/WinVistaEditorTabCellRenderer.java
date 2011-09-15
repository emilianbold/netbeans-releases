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
 * WinVistaEditorTabCellRenderer.java
 *
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import java.awt.*;
import org.netbeans.swing.tabcontrol.TabDisplayer;

/**
 * Windows Vista implementation of tab renderer
 *
 * @author S. Aubrecht
 */
final class WinVistaEditorTabCellRenderer extends AbstractTabCellRenderer {
    //Default insets values for Vista look and feel
    private static final int TOP_INSET = 0;
    private static final int LEFT_INSET = 3;
    private static final int RIGHT_INSET = 0;
    static final int BOTTOM_INSET = 0;

    //Painters which will be used for the various states, to pass to superclass
    //constructor
    private static final TabPainter leftClip = new WinVistaLeftClipPainter();
    private static final TabPainter rightClip = new WinVistaRightClipPainter();
    private static final TabPainter normal = new WinVistaPainter();
    
    /**
     * Creates a new instance of WinVistaEditorTabCellRenderer
     */
    public WinVistaEditorTabCellRenderer() {
        super(leftClip, normal, rightClip, new Dimension(32, 42));
    }

    /**
     * Vista look and feel makes selected tab wider by 2 pixels on each side
     */
    public int getPixelsToAddToSelection() {
        return 0;
    }

    public Dimension getPadding() {
        Dimension d = super.getPadding();
        d.width = isShowCloseButton() && !Boolean.getBoolean("nb.tabs.suppressCloseButton") ? 32 : 16;
        return d;
    }

    private static final Color getUnselFillBrightUpperColor() {
        Color result = UIManager.getColor("tab_unsel_fill_bright_upper"); //NOI18N
        if (result == null) {
            result = new Color(235,235,235);
        }
        return result;
    }
    
    private static final Color getUnselFillDarkUpperColor() {
        Color result = UIManager.getColor("tab_unsel_fill_dark_upper"); //NOI18N
        if (result == null) {
            result = new Color(229, 229, 229);
        }
        return result;
    }
    
    private static final Color getUnselFillBrightLowerColor() {
        Color result = UIManager.getColor("tab_unsel_fill_bright_lower"); //NOI18N
        if (result == null) {
            result = new Color(214,214,214);
        }
        return result;
    }
    
    private static final Color getUnselFillDarkLowerColor() {
        Color result = UIManager.getColor("tab_unsel_fill_dark_lower"); //NOI18N
        if (result == null) {
            result = new Color(203, 203, 203);
        }
        return result;
    }
    
    private static final Color getSelFillColor() {
        Color result = UIManager.getColor("tab_sel_fill"); //NOI18N
        if (result == null) {
            result = new Color(244,244,244);
        }
        return result;
    }
    
    private static final Color getFocusFillUpperColor() {
        Color result = UIManager.getColor("tab_focus_fill_upper"); //NOI18N
        if (result == null) {
            result = new Color(242, 249, 252);
        }
        return result;
    }
    
    private static final Color getFocusFillBrightLowerColor() {
        Color result = UIManager.getColor("tab_focus_fill_bright_lower"); //NOI18N
        if (result == null) {
            result = new Color(225, 241, 249);
        }
        return result;
    }
    
    private static final Color getFocusFillDarkLowerColor() {
        Color result = UIManager.getColor("tab_focus_fill_dark_lower"); //NOI18N
        if (result == null) {
            result = new Color(216, 236, 246);
        }
        return result;
    }
    
    private static final Color getMouseOverFillBrightUpperColor() {
        Color result = UIManager.getColor("tab_mouse_over_fill_bright_upper"); //NOI18N
        if (result == null) {
            result = new Color(223,242,252);
        }
        return result;
    }
    
    private static final Color getMouseOverFillDarkUpperColor() {
        Color result = UIManager.getColor("tab_mouse_over_fill_dark_upper"); //NOI18N
        if (result == null) {
            result = new Color(214,239,252);
        }
        return result;
    }
    
    private static final Color getMouseOverFillBrightLowerColor() {
        Color result = UIManager.getColor("tab_mouse_over_fill_bright_lower"); //NOI18N
        if (result == null) {
            result = new Color(189,228,250);
        }
        return result;
    }
    
    private static final Color getMouseOverFillDarkLowerColor() {
        Color result = UIManager.getColor("tab_mouse_over_fill_dark_lower"); //NOI18N
        if (result == null) {
            result = new Color(171,221,248);
        }
        return result;
    }
    
    private static final Color getTxtColor() {
        Color result = UIManager.getColor("TabbedPane.foreground"); //NOI18N
        if (result == null) {
            result = new Color(0, 0, 0);
        }
        return result;
    }
    
    static final Color getBorderColor() {
        Color result = UIManager.getColor("tab_border"); //NOI18N
        if (result == null) {
            result = new Color(137,140,149);
        }
        return result;
    }
    
    private static final Color getSelBorderColor() {
        Color result = UIManager.getColor("tab_sel_border"); //NOI18N
        if (result == null) {
            result = new Color(60,127,177);
        }
        return result;
    }
    
    private static final Color getBorderInnerColor() {
        Color result = UIManager.getColor("tab_border_inner"); //NOI18N
        if (result == null) {
            result = new Color(255,255,255);
        }
        return result;
    }
    
    
    
    
    
            

    public Color getSelectedActivatedForeground() {
        return getTxtColor();
    }

    public Color getSelectedForeground() {
        return getTxtColor();
    }


    
    private static void paintTabGradient( Graphics g, WinVistaEditorTabCellRenderer ren, Polygon poly ) {
        Rectangle rect = poly.getBounds(); 
        boolean selected = ren.isSelected();
        boolean focused = selected && ren.isActive();
        boolean attention = ren.isAttention();
        boolean mouseOver = ren.isArmed();
        if (focused && !attention) {
            rect.height++;
            ColorUtil.vistaFillRectGradient((Graphics2D) g, rect,
                                         getFocusFillUpperColor(),  
                                         getFocusFillBrightLowerColor(), getFocusFillDarkLowerColor() );
        } else if (selected && !attention) {
            rect.height++;
            g.setColor(getSelFillColor());
            g.fillPolygon( poly );
        } else if (mouseOver && !attention) {
            ColorUtil.vistaFillRectGradient((Graphics2D) g, rect,
                                         getMouseOverFillBrightUpperColor(), getMouseOverFillDarkUpperColor(), 
                                         getMouseOverFillBrightLowerColor(), getMouseOverFillDarkLowerColor() );
        } else if (attention) {
            Color a = new Color (255, 255, 128);
            Color b = new Color (230, 200, 64);
            ColorUtil.xpFillRectGradient((Graphics2D) g, rect,
                                         a, b);         
        } else {
            ColorUtil.vistaFillRectGradient((Graphics2D) g, rect,
                                         getUnselFillBrightUpperColor(), getUnselFillDarkUpperColor(), 
                                         getUnselFillBrightLowerColor(), getUnselFillDarkLowerColor() );
        }
        
    }
    
    protected int getCaptionYAdjustment() {
        return 0;
    }

    protected int getIconYAdjustment() {
        return -2;
    }
    
    private static class WinVistaPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
        }

        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {
              
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) jc;
            
            if (!ren.isShowCloseButton()) {
                rect.x = -100;
                rect.y = -100;
                rect.width = 0;
                rect.height = 0;
                return;
            }
            String iconPath = findIconPath(ren);
            Icon icon = TabControlButtonFactory.getIcon(iconPath);
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            rect.x = bounds.x + bounds.width - iconWidth - 2;
            rect.y = bounds.y + (Math.max(0, bounds.height / 2 - iconHeight / 2));
            rect.width = iconWidth;
            rect.height = iconHeight;
        }


        /**
         * Returns path of icon which is correct for currect state of tab at given
         * index
         */
        private String findIconPath( WinVistaEditorTabCellRenderer renderer ) {
            if( renderer.inCloseButton() && renderer.isPressed() ) {
                return "org/openide/awt/resources/vista_close_pressed.png"; // NOI18N
            }
            if( renderer.inCloseButton() ) {
                return "org/openide/awt/resources/vista_close_rollover.png"; // NOI18N
            }
            return "org/openide/awt/resources/vista_close_enabled.png"; // NOI18N       
        }
    
        public Polygon getInteriorPolygon(Component c) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = 0;
            int y = 0;

            int width = ren.isRightmost() ? c.getWidth() - 1 : c.getWidth();
            int height = c.getHeight() - ins.bottom;

            //just a plain rectangle
            p.addPoint(x, y + ins.top);
            p.addPoint(x + width, y + ins.top);
            p.addPoint(x + width, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            
            g.translate(x, y);

            Color borderColor = ((ren.isActive() && ren.isSelected())
                    || ren.isArmed()) ? getSelBorderColor() : getBorderColor();
            g.setColor(borderColor);
            int left = 0;
            //left
            if (ren.isLeftmost() )
                g.drawLine(0, 0, 0, height - 1);
            //top
            g.drawLine(0, 0, width - 1, 0);
            //right
            if( (ren.isActive() && ren.isNextTabSelected()) || ren.isNextTabArmed() )
                g.setColor( getSelBorderColor() );
            g.drawLine(width - 1, 0, width - 1, height - 2);
            //bottom
            g.setColor(getBorderColor());
            if( !ren.isSelected() ) {
                g.drawLine(0, height - 1, width - 1, height - 1);
            } else {
                g.drawLine(width - 1, height-1, width - 1, height - 1);
            }

            //inner white border
            g.setColor(getBorderInnerColor());
            //top
            g.drawLine(1, 1, width-2, 1);
            if( ren.isSelected() )
                height++;
            //left
            if (ren.isLeftmost())
                g.drawLine(1, 1, 1, height - 2);
            else
                g.drawLine(0, 1, 0, height - 2);
            //right
            g.drawLine(width-2, 1, width-2, height - 2);

            g.translate(-x, -y);
        }


        public void paintInterior(Graphics g, Component c) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            Polygon poly = getInteriorPolygon(ren);
            paintTabGradient( g, ren, poly );
            
            //Get the close button bounds, more or less
            Rectangle r = new Rectangle();
            getCloseButtonRectangle(ren, r, new Rectangle(0, 0,
                                                          ren.getWidth(),
                                                          ren.getHeight()));

            if (!g.hitClip(r.x, r.y, r.width, r.height)) {
                return;
            }
            
            //paint close button
            String iconPath = findIconPath( ren );
            Icon icon = TabControlButtonFactory.getIcon( iconPath );
            icon.paintIcon(ren, g, r.x, r.y);
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return renderer instanceof TabDisplayer ? 
                ((TabDisplayer) renderer).isShowCloseButton() : true;
        }

    }

    private static class WinVistaLeftClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
        }

        public Polygon getInteriorPolygon(Component c) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            
            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = 0;
            int y = 0;

            int width = ren.isRightmost() ? c.getWidth() - 1 : c.getWidth();
            int height = c.getHeight() - ins.bottom;

            //just a plain rectangle
            p.addPoint(x, y + ins.top);
            p.addPoint(x + width, y + ins.top);
            p.addPoint(x + width, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            g.translate(x, y);

            Color borderColor = ((ren.isActive() && ren.isSelected())
                    || ren.isArmed()) ? getSelBorderColor() : getBorderColor();
            g.setColor(borderColor);
            int left = 0;
            //left
            //no line
            //top
            g.drawLine(0, 0, width - 1, 0);
            //right
            if( (ren.isActive() && ren.isNextTabSelected()) || ren.isNextTabArmed() )
                g.setColor( getSelBorderColor() );
            g.drawLine(width - 1, 0, width - 1, height - 2);
            //bottom
            g.setColor(getBorderColor());
            if( !ren.isSelected() ) {
                g.drawLine(0, height - 1, width - 1, height - 1);
            } else {
                g.drawLine(width - 1, height-1, width - 1, height - 1);
            }

            //inner white border
            g.setColor(getBorderInnerColor());
            //top
            g.drawLine(0, 1, width-2, 1);
            if( ren.isSelected() )
                height++;
            //left
            //no line
            //right
            g.drawLine(width-2, 1, width-2, height - 2);

            g.translate(-x, -y);
        }

        public void paintInterior(Graphics g, Component c) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            
            Polygon poly = getInteriorPolygon(ren);
            paintTabGradient( g, ren, poly );
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return false;
        }

        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {
            rect.setBounds(-20, -20, 0, 0);
        }

    }

    private static class WinVistaRightClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Polygon getInteriorPolygon(Component c) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = 0;
            int y = 0;

            int width = c.getWidth() + 1;
            int height = c.getHeight() - ins.bottom;

            //just a plain rectangle
            p.addPoint(x, y + ins.top);
            p.addPoint(x + width, y + ins.top);
            p.addPoint(x + width, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            
            g.translate(x, y);

            Color borderColor = ((ren.isActive() && ren.isSelected())
                    || ren.isArmed()) ? getSelBorderColor() : getBorderColor();
            g.setColor(borderColor);
            int left = 0;
            //left
            //no line
            //top
            g.drawLine(0, 0, width, 0);
            //right
            //no line
            //bottom
            g.setColor(getBorderColor());
            if( !ren.isSelected() ) {
                g.drawLine(0, height - 1, width - 1, height - 1);
            } else {
                g.drawLine(width - 1, height-1, width - 1, height - 1);
            }

            //inner white border
            g.setColor(getBorderInnerColor());
            //top
            g.drawLine(1, 1, width, 1);
            if( ren.isSelected() )
                height++;
            //left
            g.drawLine(0, 1, 0, height - 2);
            //right
            //no line

            g.translate(-x, -y);
        }

        public void paintInterior(Graphics g, Component c) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            
            Polygon poly = getInteriorPolygon(ren);
            paintTabGradient( g, ren, poly );
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return false;
        }

        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {
            rect.setBounds(-20, -20, 0, 0);
        }
    }
}
