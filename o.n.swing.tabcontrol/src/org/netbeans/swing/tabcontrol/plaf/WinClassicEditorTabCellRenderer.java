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
 * WinClassicEditorTabCellRenderer.java
 *
 * Created on 09 December 2003, 16:54
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import java.awt.*;
import org.netbeans.swing.tabcontrol.TabDisplayer;

/**
 * Windows classic implementation of tab renderer
 *
 * @author Tim Boudreau
 */
final class WinClassicEditorTabCellRenderer extends AbstractTabCellRenderer {
    private static final TabPainter leftClip = new WinClassicLeftClipPainter();
    private static final TabPainter rightClip = new WinClassicRightClipPainter();
    private static final TabPainter normal = new WinClassicPainter();
    
    static final Color ATTENTION_COLOR = new Color(255, 238, 120);    
    
    private static boolean isGenericUI = !"Windows".equals(
        UIManager.getLookAndFeel().getID());

    /**
     * Creates a new instance of WinClassicEditorTabCellRenderer
     */
    public WinClassicEditorTabCellRenderer() {
          super(leftClip, normal, rightClip, new Dimension (28, 32));
      }

    public Color getSelectedForeground() {
        return UIManager.getColor("textText"); //NOI18N
    }

    public Color getForeground() {
        return getSelectedForeground();
    }
    
    /**
     * #56245 - need more space between icon and edge on classic for the case
     * of full 16x16 icons.
     */
    public int getPixelsToAddToSelection() {
        return 4;
    }    

    private static final Insets INSETS = new Insets(0, 2, 0, 10);

    private static class WinClassicPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
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

            rect.y = bounds.y + ins.top;

            rect.height = bounds.height - rect.y;
            rect.x = bounds.x + bounds.width - 10;
            rect.width = 5;

            rect.y += (rect.height / 2) - 2;
            rect.height = 5;
        }

        public Polygon getInteriorPolygon(Component c) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = ren.isLeftmost() ? 1 : 0;
            int y = isGenericUI ? 0 : 1;

            int width = ren.isLeftmost() ? c.getWidth() - 1 : c.getWidth();
            int height = ren.isSelected() ?
                    c.getHeight() + 2 : c.getHeight() - 1;
                    
            p.addPoint(x, y + ins.top + 2);
            p.addPoint(x + 2, y + ins.top);
            p.addPoint(x + width - 3, y + ins.top);
            p.addPoint(x + width - 1, y + ins.top + 2);
            p.addPoint(x + width - 1, y + height - 2);
            p.addPoint(x, y + height - 2);
            return p;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            g.setColor(ren.isSelected() ?
                       UIManager.getColor("controlLtHighlight") :
                       UIManager.getColor("controlHighlight")); //NOI18N

            int[] xpoints = p.xpoints;
            int[] ypoints = p.ypoints;

            g.drawLine(xpoints[0], ypoints[0], xpoints[p.npoints - 1],
                       ypoints[p.npoints - 1]);

            for (int i = 0; i < p.npoints - 1; i++) {
                g.drawLine(xpoints[i], ypoints[i], xpoints[i + 1],
                           ypoints[i + 1]);
                if (i == p.npoints - 4) {
                    g.setColor(ren.isSelected() ?
                               UIManager.getColor("controlDkShadow") :
                               UIManager.getColor("controlShadow")); //NOI18N
                    g.drawLine(xpoints[i] + 1, ypoints[i] + 1,
                               xpoints[i] + 2, ypoints[i] + 2);
                }
            }
        }

        public void paintInterior(Graphics g, Component c) {

            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;
            boolean wantGradient = ren.isSelected() && ren.isActive() || ((ren.isClipLeft()
                    || ren.isClipRight())
                    && ren.isPressed());

            if (wantGradient) {
                ((Graphics2D) g).setPaint(ColorUtil.getGradientPaint(0, 0, getSelGradientColor(), ren.getWidth(), 0, UIManager.getColor(
                        "TabbedPane.background")));//NOI18N
            } else {
                if (!ren.isAttention()) {
                    g.setColor(ren.isSelected() ?
                               UIManager.getColor("TabbedPane.background") :
                               UIManager.getColor("tab_unsel_fill")); //NOI18N
                } else {
                    g.setColor(ATTENTION_COLOR);
                }
            }
            Polygon p = getInteriorPolygon(c);
            g.fillPolygon(p);

            if (!supportsCloseButton((JComponent)c)) {
                return;
            }
            
            Rectangle r = new Rectangle();
            getCloseButtonRectangle(ren, r, new Rectangle(0, 0,
                                                          ren.getWidth(),
                                                          ren.getHeight()));

            if (ren.inCloseButton()) {
                g.setColor(UIManager.getColor("control")); //NOI18N
                g.fillRect(r.x - 1, r.y - 1, r.width + 3, r.height + 3);
                g.setColor(UIManager.getColor("textText")); //NOI18N
            } else {
                g.setColor(ren.getForeground());
            }

            g.setColor(ren.getForeground());
            g.drawLine(r.x, r.y, r.x + r.width - 1, r.y + r.height - 1);
            g.drawLine(r.x, r.y + r.height - 1, r.x + r.width - 1, r.y);

            if (ren.isSelected()) {
                g.setColor(UIManager.getColor("controlShadow"));
            } else if (!ren.isSelected()) {
                g.setColor(UIManager.getColor("controlDkShadow"));
            }

            if (ren.inCloseButton()) {
                r.x -= 2;
                r.y -= 2;
                r.width += 4;
                r.height += 4;
                if (!ren.isPressed()) {
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                } else {
                    g.setColor(
                            UIManager.getColor("controlShadow").darker());
                }
                g.drawLine(r.x, r.y, r.x + r.width, r.y);
                g.drawLine(r.x, r.y, r.x, r.y + r.height);
                if (!ren.isPressed()) {
                    g.setColor(
                            UIManager.getColor("controlShadow").darker());
                } else {
                    g.setColor(UIManager.getColor("controlLtHighlight"));
                }
                g.drawLine(r.x, r.y + r.height, r.x + r.width, r.y + r.height);
                g.drawLine(r.x + r.width, r.y + r.height, r.x + r.width, r.y);
            }
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return 
                ((AbstractTabCellRenderer) renderer).isShowCloseButton();
        }

    }


    private static class WinClassicLeftClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public Polygon getInteriorPolygon(Component c) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = -3;
            int y = isGenericUI ? 0 : 1;

            int width = c.getWidth() + 3;
            int height = ren.isSelected() ?
                    c.getHeight() + 2 : c.getHeight() - 1;

            p.addPoint(x, y + ins.top + 2);
            p.addPoint(x + 2, y + ins.top);
            p.addPoint(x + width - 3, y + ins.top);
            p.addPoint(x + width - 1, y + ins.top + 2);
            p.addPoint(x + width - 1, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            g.setColor(ren.isSelected() ?
                       UIManager.getColor("controlLtHighlight") :
                       UIManager.getColor("controlHighlight")); //NOI18N

            int[] xpoints = p.xpoints;
            int[] ypoints = p.ypoints;

            g.drawLine(xpoints[0], ypoints[0], xpoints[p.npoints - 1],
                       ypoints[p.npoints - 1]);

            for (int i = 0; i < p.npoints - 1; i++) {
                g.drawLine(xpoints[i], ypoints[i], xpoints[i + 1],
                           ypoints[i + 1]);
                if (i == p.npoints - 4) {
                    g.setColor(ren.isSelected() ?
                               UIManager.getColor("controlDkShadow") :
                               UIManager.getColor("controlShadow")); //NOI18N
                    g.drawLine(xpoints[i] + 1, ypoints[i] + 1,
                               xpoints[i] + 2, ypoints[i] + 2);
                }
            }
        }

        public void paintInterior(Graphics g, Component c) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;
            boolean wantGradient = ren.isSelected() && ren.isActive() || ((ren.isClipLeft()
                    || ren.isClipRight())
                    && ren.isPressed());

            if (wantGradient) {
                ((Graphics2D) g).setPaint(ColorUtil.getGradientPaint(0, 0, getSelGradientColor(), ren.getWidth(), 0, UIManager.getColor(
                        "TabbedPane.background")));//NOI18N
            } else {
                if (!ren.isAttention()) {
                    g.setColor(ren.isSelected() ?
                           UIManager.getColor("TabbedPane.background") :
                           UIManager.getColor("tab_unsel_fill")); //NOI18N
                } else {
                    g.setColor(ATTENTION_COLOR);
                }
            }
            Polygon p = getInteriorPolygon(c);
            g.fillPolygon(p);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {
            rect.setBounds(-20, -20, 0, 0);
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return false;
        }
    }

    private static class WinClassicRightClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Polygon getInteriorPolygon(Component c) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = 0;
            int y = isGenericUI ? 0 : 1;

            int width = c.getWidth();
            int height = ren.isSelected() ?
                    c.getHeight() + 2 : c.getHeight() - 1;

            p.addPoint(x, y + ins.top + 2);
            p.addPoint(x + 2, y + ins.top);
            p.addPoint(x + width - 1, y + ins.top);
            p.addPoint(x + width - 1, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            g.setColor(ren.isSelected() ?
                       UIManager.getColor("controlLtHighlight") :
                       UIManager.getColor("controlHighlight")); //NOI18N

            int[] xpoints = p.xpoints;
            int[] ypoints = p.ypoints;

            g.drawLine(xpoints[0], ypoints[0], xpoints[p.npoints - 1],
                       ypoints[p.npoints - 1]);

            for (int i = 0; i < p.npoints - 1; i++) {
                g.drawLine(xpoints[i], ypoints[i], xpoints[i + 1],
                           ypoints[i + 1]);
                if (ren.isSelected() && i == p.npoints - 4) {
                    g.setColor(ren.isActive() ?
                               UIManager.getColor("Table.selectionBackground") :
                               UIManager.getColor("control")); //NOI18n
                } else if (i == p.npoints - 4) {
                    break;
                }
                if (i == p.npoints - 3) {
                    break;
                }
            }
        }

        public void paintInterior(Graphics g, Component c) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;
            boolean wantGradient = ren.isSelected() && ren.isActive() || ((ren.isClipLeft()
                    || ren.isClipRight())
                    && ren.isPressed());

            if (wantGradient) {
                ((Graphics2D) g).setPaint(ColorUtil.getGradientPaint(0, 0, getSelGradientColor(), ren.getWidth(), 0, UIManager.getColor(
                        "TabbedPane.background")));//NOI18N
            } else {
                if (!ren.isAttention()) {
                    g.setColor(ren.isSelected() ?
                           UIManager.getColor("TabbedPane.background") : //NOI18N
                           UIManager.getColor("tab_unsel_fill")); //NOI18N
                } else {
                    g.setColor(ATTENTION_COLOR);
                }
            }

            Polygon p = getInteriorPolygon(c);
            g.fillPolygon(p);
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

    private static final Color getSelGradientColor() {
        //XXX delete this method
        return UIManager.getColor("winclassic_tab_sel_gradient");
    }
}
