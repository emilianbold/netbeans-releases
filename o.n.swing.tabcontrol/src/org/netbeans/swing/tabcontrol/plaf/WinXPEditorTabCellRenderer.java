/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * WinXPEditorTabCellRenderer.java
 *
 * Created on 09 December 2003, 16:54
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import org.netbeans.swing.tabcontrol.TabDisplayer;

/**
 * Windows xp implementation of tab renderer
 *
 * @author Tim Boudreau
 */
final class WinXPEditorTabCellRenderer extends AbstractTabCellRenderer {
    //Default insets values for XP look and feel
    private static final int TOP_INSET = 0;
    private static final int LEFT_INSET = 3;
    private static final int RIGHT_INSET = 0;
    static final int BOTTOM_INSET = 3;

    //Painters which will be used for the various states, to pass to superclass
    //constructor
    private static final TabPainter leftClip = new WinXPLeftClipPainter();
    private static final TabPainter rightClip = new WinXPRightClipPainter();
    private static final TabPainter normal = new WinXPPainter();

    /**
     * Creates a new instance of WinXPEditorTabCellRenderer
     */
    public WinXPEditorTabCellRenderer() {
        super(leftClip, normal, rightClip, new Dimension(32, 42));
    }

    /**
     * XP look and feel makes selected tab wider by 2 pixels on each side
     */
    public int getPixelsToAddToSelection() {
        return 4;
    }

    public Dimension getPadding() {
        Dimension d = super.getPadding();
        return d;
    }

    private static final Color getTopInactiveSelectedColor() {
        Color result = UIManager.getColor("tab_sel_fill_bright"); //NOI18N
        if (result == null) {
            result = new Color(252, 250, 244);
        }
        return result;
    }

    private static final Color getBottomInactiveSelectedColor() {
        Color result = UIManager.getColor("tab_sel_fill_dark"); //NOI18N
        if (result == null) {
            result = new Color(243, 241, 224);
        }
        return result;
    }

    private static final Color getTopActiveSelectedColor() {
        Color result = UIManager.getColor("tab_focus_fill_bright"); //NOI18N
        if (result == null) {
            result = new Color(210, 220, 243);
        }
        return result;
    }

    private static final Color getBottomActiveSelectedColor() {
        Color result = UIManager.getColor("tab_focus_fill_dark"); //NOI18N
        if (result == null) {
            result = new Color(238, 242, 253);
        }
        return result;
    }

    private static final Color getTopUnselectedColor() {
        Color result = UIManager.getColor("tab_unsel_fill_bright"); //NOI18N
        if (result == null) {
            result = Color.white;
        }
        return result;
    }

    private static final Color getBottomUnselectedColor() {
        Color result = UIManager.getColor("tab_unsel_fill_dark"); //NOI18N
        if (result == null) {
            result = new Color(236, 235, 229);
        }
        return result;
    }

    static final Color getBorderColor() {
        Color result = UIManager.getColor("tab_bottom_border"); //NOI18N
        if (result == null) {
            result = new Color(127, 187, 185);
        }
        return result;
    }

    private static final Color getCloseButtonColor(
            WinXPEditorTabCellRenderer ren) {
        String key = ren.inCloseButton() ?
                "close_button_highlight" : "close_button"; //NOI18N
        Color result = UIManager.getColor(key);
        if (result == null) {
            result = ren.inCloseButton() ?
                    new Color(172, 57, 28) : Color.black;
        }
        return result;
    }

    static final Color getSelectedTabBottomLineColor() {
        Color result = UIManager.getColor("tab_sel_bottom_border"); //NOI18N
        if (result == null) {
            result = new Color(238, 235, 218);
        }
        return result;
    }

    private static final Color getShadowBorderColor(
            WinXPEditorTabCellRenderer ren) {
        //Preserving in case open issue about borders is decided that
        //there should be some difference between shadow/hl borders
        return getBorderColor();
    }

    private static final Color getHighlightBorderColor(
            WinXPEditorTabCellRenderer ren) {
        //Preserving in case open issue about borders is decided that
        //there should be some difference between shadow/hl borders
        return getBorderColor();
    }

    private static final Color getHighlightColor() {
        Color result = UIManager.getColor("TabbedPane.selectionIndicator"); //NOI18N
        if (result == null) {
            result = new Color(255, 199, 60); //XXX derive from a system color
        }
        return result;
    }

    private static final Color getTopHighlightColor() {
        Color result = UIManager.getColor("tab_highlight_header"); //NOI18N
        if (result == null) {
            result = new Color(230, 139, 44);
        }
        return result;
    }

    public Color getSelectedActivatedForeground() {
        Color result = UIManager.getColor("textText"); //NOI18N
        if (result == null) {
            result = Color.BLACK;
        }
        return result;
    }

    private static final Color getRightEdgeSelectedShadow() {
        Color result = UIManager.getColor("close_button_border_focus"); //NOI18N
        if (result == null) {
            result = new Color(181, 201, 243);
        }
        return result;
    }

    public Color getSelectedActivatedBackground() {
        Color top = UIManager.getColor("tab_focus_fill_bright"); //NOI18N
        Color bot = UIManager.getColor("tab_focus_fill_dark"); //NOI18N
        if (top == null) {
            top = new Color(238, 242, 253);
        }
        if (bot == null) {
            bot = new Color(210, 220, 243);
        }
        Color result = ColorUtil.getMiddle(top, bot);
        return result;
    }

    public static Color getCloseButtonAAColor(WinXPEditorTabCellRenderer ren) {
        Color towards = ren.getBackground();
        Color base = getCloseButtonColor(ren);

        Color result = org.netbeans.swing.tabcontrol.plaf.ColorUtil.getMiddle(
                base, towards);
        int factor = ren.inCloseButton() ? 35 : 74;
        factor *= ColorUtil.isBrighter(towards, base) ? 1 : -1;
        result = ColorUtil.adjustBy(result, factor);
        return result;
    }

    private static Color getCloseButtonBorderColor(
            WinXPEditorTabCellRenderer ren) {
        String key = ren.isActive() && ren.isSelected() ? "close_button_border_focus" : ren.isSelected() ?
                "close_button_border_selected" : "close_button_border_unsel"; //NOI18N
        Color result = UIManager.getColor(key);
        if (result == null) {
            result = ren.isActive() && ren.isSelected() ? new Color(181, 201,
                                                                    243) : ren.isSelected() ?
                    new Color(203, 202, 187) : new Color(200, 201, 192);
        }
        return result;
    }

    public static Color getCloseButtonHighlight(WinXPEditorTabCellRenderer ren) {
        Color result = ren.isPressed() && ren.inCloseButton() ? getCloseButtonBorderColor(
                ren) : ren.isActive() && ren.isSelected() ?
                UIManager.getColor("tab_sel_fill_dark") :
                UIManager.getColor("tab_sel_fill_bright");
        if (result == null) {
            result = Color.white;
        }
        return result;
    }

    public static Color getCloseButtonShadow(WinXPEditorTabCellRenderer ren) {
        return ren.isPressed() && ren.inCloseButton() ?
                Color.WHITE : getCloseButtonBorderColor(ren);
    }


    private static final Paint getPaint(WinXPEditorTabCellRenderer ren,
                                        TabPainter p) {
        Insets ins = p.getBorderInsets(ren);
        int xTop = ins.left;
        int yTop = ins.top;
        int xBot = ins.left;
        int yBot = ren.getHeight() - (ins.top + ins.bottom + 1);
        if (ren.isSelected() || ren.isArmed()) {
            yTop += 3;
        }
        return getPaint(xTop, yTop, xBot, yBot, ren);
    }

    private static final Paint getPaint(int xTop, int yTop, int xBot, int yBot,
                                        WinXPEditorTabCellRenderer ren) {
        if (ren.isSelected() || (ren.isPressed() && !ren.inCloseButton())) {
            if (ren.isActive()) {
                return ColorUtil.getGradientPaint(xTop, yTop,
                                                  getTopActiveSelectedColor(),
                                                  xBot, yBot,
                                                  getBottomActiveSelectedColor());
            } else {
                Color a = getTopInactiveSelectedColor();
                Color b = getBottomInactiveSelectedColor();
                if (a == b) {
                    return a;
                } else {
                    return ColorUtil.getGradientPaint(xTop, yTop, a, xBot,
                                                      yBot, b);
                }
            }
        } else {
            return ColorUtil.getGradientPaint(xTop, yTop,
                                              getTopUnselectedColor(), xBot,
                                              yBot,
                                              getBottomUnselectedColor());
        }
    }

    private static final void paintGradient(Graphics g,
                                            WinXPEditorTabCellRenderer ren,
                                            TabPainter p) {
        Graphics2D g2d = (Graphics2D) g;
        //draw gradient
        Insets ins = p.getBorderInsets(ren);
        Paint gp = getPaint(ren, p);
        g2d.setPaint(gp);
        Polygon poly = p.getInteriorPolygon(ren);
        g.fillPolygon(poly);
        if (ren.isArmed() || ren.isSelected()) {
            paintTopLine(g, ren, p);
        }
    }

    private static final void paintTopLine(Graphics g,
                                           WinXPEditorTabCellRenderer ren,
                                           TabPainter p) {
        Polygon poly = p.getInteriorPolygon(ren);
        ((Graphics2D) g).setPaint(getHighlightColor());
        g.setColor(getHighlightColor());
        Shape clip = g.getClip();
        Insets ins = p.getBorderInsets(ren);
        try {
            if (clip != null) {
                Area a = new Area(clip);
                a.intersect(new Area(poly));
                g.setClip(a);
            } else {
                g.setClip(poly);
            }
            g.fillRect(0, ins.top, ren.getWidth(), 3);
        } finally {
            g.setClip(clip);
        }
    }

    private static class WinXPPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
        }

        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {
                                                
            if (!((AbstractTabCellRenderer) jc).isShowCloseButton()) {
                rect.x = -100;
                rect.y = -100;
                rect.width = 0;
                rect.height = 0;
                return;
            }
            Insets ins = getBorderInsets(jc);

            rect.y = bounds.y + ins.top - 3;

            rect.height = bounds.height - rect.y;
            rect.x = bounds.x + bounds.width - 12;
            rect.width = 6;

            rect.y += (rect.height / 2) - 3;
            rect.height = 6;
        }

        public Polygon getInteriorPolygon(Component c) {
            WinXPEditorTabCellRenderer ren = (WinXPEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
//            int x = ren.isLeftmost() ? 1 : 0;
            int x = 0;
            int y = 0;

            int h = c.getHeight() - ins.bottom;

            int width = ren.isRightmost() ? c.getWidth() - 1 : c.getWidth();
            int height = ren.isSelected() ? h + 1 : h;

            p.addPoint(x, y + ins.top + 2);
            p.addPoint(x + 2, y + ins.top);
            p.addPoint(x + width - 2, y + ins.top);
            p.addPoint(x + width, y + ins.top + 2);
            p.addPoint(x + width, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinXPEditorTabCellRenderer ren = (WinXPEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            g.setColor(getHighlightBorderColor(ren));

            int[] xpoints = p.xpoints;
            int[] ypoints = p.ypoints;

            g.drawLine(xpoints[0], ypoints[0], xpoints[p.npoints - 1],
                       ypoints[p.npoints - 1]);

            Color prev = null;
            boolean topColor = ren.isArmed() || ren.isSelected();
            for (int i = 0; i < p.npoints - 1; i++) {
                if (i == 0 && topColor) {
                    prev = g.getColor();
                    g.setColor(getTopHighlightColor());
                } else if (i == 3 && topColor) {
                    g.setColor(prev);
                }
                g.drawLine(xpoints[i], ypoints[i], xpoints[i + 1],
                           ypoints[i + 1]);
            }

            //Find a color to antialias the top with the tab area background
            g.setColor(ColorUtil.adjustComponentsTowards(topColor ?
                                                         getTopHighlightColor() :
                                                         getHighlightBorderColor(
                                                                 ren),
                                                         UIManager.getColor(
                                                                 "control"))); //NOI18N
            
            //Antialias the corners of the polygon
            g.drawLine(xpoints[0] - 1, ypoints[0], xpoints[1] - 1,
                       ypoints[1]);
            g.drawLine(xpoints[2] + 1, ypoints[2], xpoints[3] + 1,
                       ypoints[3]);

            if (ren.isSelected()) {
                g.setColor(getRightEdgeSelectedShadow());
                Insets ins = getBorderInsets(c);
                g.drawLine(ren.getWidth() - (ren.isRightmost() ? 2 : 1),
                           ins.top + 3, width - (ren.isRightmost() ? 2 : 1),
                           height - ins.bottom);

                GradientPaint paint = (GradientPaint) getPaint(ins.top + 3, 0, height - (ins.top
                                                                                         + ins.bottom
                                                                                         + 2), 1,
                                                               ren);
                ((Graphics2D) g).setPaint(paint);
                //Flip the gradient
                Point2D p1 = paint.getPoint1();
                Point2D p2 = paint.getPoint2();
                paint =
                        ColorUtil.getGradientPaint(Math.round(p2.getX()),
                                                   Math.round(p2.getY()),
                                                   paint.getColor1(),
                                                   Math.round(p1.getX()),
                                                   Math.round(p1.getY()),
                                                   paint.getColor2(), false);
                //Paint the left edge inverse gradient line
                int rpos = ren.isLeftmost() ? x + 2 : x + 1;
                g.fillRect(rpos, y + ins.top + 3, rpos,
                           (height - (ins.top + ins.bottom + 3)));

                g.setColor(getSelectedTabBottomLineColor());
                g.drawLine(1, height - ins.bottom, width - 2,
                           height - ins.bottom);
            }
        }


        public void paintInterior(Graphics g, Component c) {
            WinXPEditorTabCellRenderer ren = (WinXPEditorTabCellRenderer) c;
            //Use the utility method to paint the interior gradient
            paintGradient(g, ren, this);

            Rectangle r = new Rectangle();
            //Get the close button bounds, more or less
            getCloseButtonRectangle(ren, r, new Rectangle(0, 0,
                                                          ren.getWidth(),
                                                          ren.getHeight()));

            if (!g.hitClip(r.x, r.y, r.width, r.height)) {
                return;
            }
            
            //Draw the close button gradient background if armed
            if (ren.inCloseButton()) {
                //Draw a small gradient background to the close button
                Color bottom = getCloseButtonBorderColor(ren);

                Color top = Color.WHITE;
                if (ren.inCloseButton() && ren.isPressed()) {
                    Color q = top;
                    top = bottom;
                    bottom = q;
                }

                GradientPaint gp = ColorUtil.getGradientPaint(r.x - 2, r.y - 2, top, r.x
                                                                                     + r.width
                                                                                     - 4, r.y
                                                                                          + r.height
                                                                                          + 4, bottom);
                ((Graphics2D) g).setPaint(gp);

                g.fillRect(r.x - 2, r.y - 2, r.width + 4, r.height + 4);
            }
            //Draw the close button itself
            
            //Derive a middle color from the foreground and background to antialias
            //the X against its background
            g.setColor(getCloseButtonAAColor(ren));
            //Draw the antialiasing behind the X as some boxes in the form:
            //  XX    XX
            //  XX    XX
            //    XXXX 
            //    XXXX
            //    XXXX
            //    XXXX
            //  XX    XX
            //  XX    XX
            
            g.fillRect(r.x + (r.width / 2) - 2, r.y + (r.height / 2) - 2,
                       4, 4);
            g.fillRect(r.x, r.y, 2, 2);
            g.fillRect(r.x + r.width - 2, r.y, 2, 2);
            g.fillRect(r.x, r.y + r.height - 2, 2, 2);
            g.fillRect(r.x + r.width - 2, r.y + r.height - 2, 2, 2);
            
            //Get the real close button color
            g.setColor(getCloseButtonColor(ren));
            //And draw the X itself
            g.drawLine(r.x, r.y, r.x + r.width - 1, r.y + r.height - 1);
            g.drawLine(r.x, r.y + r.height - 1, r.x + r.width - 1, r.y);

            //Now draw the shadow of the X, or the highlight if pressed - at any rate,
            //the lines down and to the right below the X, inverting the highlight/shadow
            //colors if we're in the "pushed" state
            g.setColor(ren.isPressed() && ren.inCloseButton() ?
                       getCloseButtonHighlight(ren) :
                       getCloseButtonShadow(ren));
            
            //the southeast leg right shadow
            g.drawLine(r.x + r.width, r.y + r.height - 2, r.x + r.width,
                       r.y + r.height - 1);
            //the southeast leg bottom shadow
            g.drawLine(r.x + r.width - 2, r.y + r.height, r.x + r.width - 1,
                       r.y + r.height);
            //the south shadow under the square in the middle
            g.drawLine(r.x + r.width - 4, r.y + r.height - 1,
                       r.x + r.width - 3, r.y + r.height - 1);
            //the southwest leg bottom shadow
            g.drawLine(r.x, r.y + r.height, r.x + 1, r.y + r.height);
            //the right edge middle shadow
            g.drawLine(r.x + r.width - 1, r.y + r.height - 4,
                       r.x + r.width - 1, r.y + r.height - 3);
            //the northeast leg left shadow
            g.drawLine(r.x + r.width, r.y, r.x + r.width, r.y + 1);
            
            //Now draw the highlight of the X, or the shadow if pressed - at any rate,
            //the lines up and to the left above the X
            g.setColor(ren.isPressed() && ren.inCloseButton() ?
                       getCloseButtonShadow(ren) :
                       getCloseButtonHighlight(ren));
            
            //draw the northwest leg top highlight
            g.drawLine(r.x, r.y - 1, r.x + 1, r.y - 1);
            //draw the northwest leg left highlight
            g.drawLine(r.x - 1, r.y, r.x - 1, r.y + 1);
            //draw the left edge middle highlight1
            g.drawLine(r.x, r.y + 2, r.x, r.y + 3);
            //draw the southwest leg left highlight
            g.drawLine(r.x - 1, r.y + r.height - 2, r.x - 1,
                       r.y + r.height - 1);
            //draw the top middle highlight
            g.drawLine(r.x + 2, r.y, r.x + 3, r.y);
            //draw the northeast leg top highlight
            g.drawLine(r.x + r.width - 2, r.y - 1, r.x + r.width - 1, r.y - 1);

            //Draw the close button border if armed
            if (ren.inCloseButton()) {
                Color cbColor = getCloseButtonBorderColor(ren);
                g.setColor(cbColor);
                //Grow the rectangle to its bounds
                r.x -= 3;
                r.y -= 3;
                r.width += 5;
                r.height += 5;
                //Draw a rounded rectangle for it
                g.drawRoundRect(r.x, r.y, r.width, r.height, 6, 6);

                //Now draw the antialiasing lines at the corners.
                //XXX may be smarter to leave it up to the look and feel
                //to turn on antialiasing - then drawRoundRect will be
                //antialiased automatically
                g.setColor(
                        ColorUtil.getMiddle(ren.getBackground(), cbColor));
                //upper left corner
                g.drawLine(r.x, r.y + 1, r.x + 1, r.y);
                //upper right corner
                g.drawLine(r.x + r.width - 1, r.y, r.x + r.width, r.y + 1);
                //bottom left corner
                g.drawLine(r.x, r.y + r.height - 1, r.x + 1, r.y + r.height);
                //bottom right corner
                g.drawLine(r.x + r.width, r.y + r.height - 1,
                           r.x + r.width - 1, r.y + r.height);

            }
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return renderer instanceof TabDisplayer ? 
                ((TabDisplayer) renderer).isShowCloseButton() : true;
        }

    }

    private static class WinXPLeftClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
        }

        public Polygon getInteriorPolygon(Component c) {
            WinXPEditorTabCellRenderer ren = (WinXPEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = -3;
            int y = 0;

            int h = c.getHeight() - ins.bottom;

            int width = c.getWidth() + 3;
            int height = ren.isSelected() ? h + 1 : h;

            p.addPoint(x, y + ins.top + 2);
            p.addPoint(x + 2, y + ins.top);
            p.addPoint(x + width - 2, y + ins.top);
            p.addPoint(x + width, y + ins.top + 2);
            p.addPoint(x + width, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinXPEditorTabCellRenderer ren = (WinXPEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            g.setColor(getHighlightBorderColor(ren));

            int[] xpoints = p.xpoints;
            int[] ypoints = p.ypoints;

            g.drawLine(xpoints[0], ypoints[0], xpoints[p.npoints - 1],
                       ypoints[p.npoints - 1]);
            Color prev = null;
            boolean topColor = ren.isArmed() || ren.isSelected();
            for (int i = 0; i < p.npoints - 1; i++) {
                if (i == 1 && topColor) {
                    prev = g.getColor();
                    g.setColor(getTopHighlightColor());
                } else if (i == 2 && topColor) {
                    g.setColor(prev);
                }
                g.drawLine(xpoints[i], ypoints[i], xpoints[i + 1],
                           ypoints[i + 1]);
                if (i == p.npoints - 4) {
                    g.setColor(getShadowBorderColor(ren));
                    g.drawLine(xpoints[i] + 1, ypoints[i] + 1,
                               xpoints[i] + 2, ypoints[i] + 2);
                }
            }
            //Find a color to antialias the top with the tab area background
            g.setColor(ColorUtil.adjustComponentsTowards(topColor ?
                                                         getTopHighlightColor() :
                                                         getHighlightBorderColor(
                                                                 ren),
                                                         UIManager.getColor(
                                                                 "control"))); //NOI18N
                
            //Antialias the corners of the polygon
            g.drawLine(xpoints[0] - 1, ypoints[0], xpoints[1] - 1,
                       ypoints[1]);
            g.drawLine(xpoints[2] + 1, ypoints[2], xpoints[3] + 1,
                       ypoints[3]);

            if (ren.isSelected()) {
                g.setColor(getRightEdgeSelectedShadow());
                Insets ins = getBorderInsets(c);
                g.drawLine(ren.getWidth() - 1, ins.top + 3, width - 1,
                           height - ins.bottom);
                g.setColor(getSelectedTabBottomLineColor());
                g.drawLine(0, height - ins.bottom, width - 2,
                           height - ins.bottom);

            }
        }

        public void paintInterior(Graphics g, Component c) {
            WinXPEditorTabCellRenderer ren = (WinXPEditorTabCellRenderer) c;
            paintGradient(g, ren, this);
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

    private static class WinXPRightClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Polygon getInteriorPolygon(Component c) {
            WinXPEditorTabCellRenderer ren = (WinXPEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = 0;
            int y = 0;

            int h = c.getHeight() - ins.bottom;

            int width = c.getWidth() + 3;
            int height = ren.isSelected() ? h + 1 : h;//ren.isSelected() ? h + 2 : h - 1;

            p.addPoint(x, y + ins.top + 2);
            p.addPoint(x + 2, y + ins.top);
            p.addPoint(x + width, y + ins.top);
            p.addPoint(x + width, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinXPEditorTabCellRenderer ren = (WinXPEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            g.setColor(getHighlightBorderColor(ren));

            int[] xpoints = p.xpoints;
            int[] ypoints = p.ypoints;

            g.drawLine(xpoints[0], ypoints[0], xpoints[p.npoints - 1],
                       ypoints[p.npoints - 1]);

            Color prev = null;
            boolean topColor = ren.isArmed() || ren.isSelected();
            for (int i = 0; i < p.npoints - 1; i++) {
                if (i == 1 && topColor) {
                    prev = g.getColor();
                    g.setColor(getTopHighlightColor());
                } else if (i == 2 && topColor) {
                    g.setColor(prev);
                }
                g.drawLine(xpoints[i], ypoints[i], xpoints[i + 1],
                           ypoints[i + 1]);
            }
            
            //Find a color to antialias the top with the tab area background
            g.setColor(ColorUtil.adjustComponentsTowards(topColor ?
                                                         getTopHighlightColor() :
                                                         getHighlightBorderColor(
                                                                 ren),
                                                         UIManager.getColor(
                                                                 "control"))); //NOI18N
                
            //Antialias the corners of the polygon
            g.drawLine(xpoints[0] - 1, ypoints[0], xpoints[1] - 1,
                       ypoints[1]);
//            g.drawLine(xpoints[2]+1, ypoints[2], xpoints[3]+1, ypoints[3]);
            
            
            if (ren.isSelected()) {
                Insets ins = getBorderInsets(c);

                GradientPaint paint = (GradientPaint) getPaint(ins.top + 3, 0, height - (ins.top
                                                                                         + ins.bottom
                                                                                         + 2), 1,
                                                               ren);
                ((Graphics2D) g).setPaint(paint);
                //Flip the gradient
                Point2D p1 = paint.getPoint1();
                Point2D p2 = paint.getPoint2();
                paint =
                        ColorUtil.getGradientPaint(Math.round(p2.getX()),
                                                   Math.round(p2.getY()),
                                                   paint.getColor1(),
                                                   Math.round(p1.getX()),
                                                   Math.round(p1.getY()),
                                                   paint.getColor2(), false);
                //Paint the left edge inverse gradient line
                g.fillRect(x + 1, y + ins.top + 3, x + 1,
                           (height - (ins.top + ins.bottom + 3)));

                g.setColor(getSelectedTabBottomLineColor());
                g.drawLine(1, height - ins.bottom, width - 1,
                           height - ins.bottom);
            }
        }

        public void paintInterior(Graphics g, Component c) {
            WinXPEditorTabCellRenderer ren = (WinXPEditorTabCellRenderer) c;
            paintGradient(g, ren, this);
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
