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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import java.awt.*;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;

/**
 * Gtk implementation of tab renderer
 *
 * @author Marek Slama
 */
final class GtkEditorTabCellRenderer extends AbstractTabCellRenderer {

    private static final TabPainter leftClip = new GtkLeftClipPainter();
    private static final TabPainter rightClip = new GtkRightClipPainter();
    private static final TabPainter normal = new GtkPainter();
    
    private static JTabbedPane dummyTab;

    static final Color ATTENTION_COLOR = new Color(255, 238, 120);
    
    /**
     * Creates a new instance of GtkEditorTabCellRenderer
     */
    public GtkEditorTabCellRenderer() {
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

    protected int getCaptionYAdjustment() {
        return 2;
    }
    
    protected int getIconYAdjustment() {
        return 0;
    }

    public Dimension getPadding() {
        Dimension d = super.getPadding();
        d.width = isShowCloseButton() && !Boolean.getBoolean("nb.tabs.suppressCloseButton") ? 28 : 14;
        return d;
    }
    
    private static final Insets INSETS = new Insets(0, 2, 0, 10);
    
    private static void paintTabBackground (Graphics g, int index, int state,
    int x, int y, int w, int h) {
        if (dummyTab == null) {
            dummyTab = new JTabbedPane();
        }
        Region region = Region.TABBED_PANE_TAB;
        SynthLookAndFeel laf = (SynthLookAndFeel) UIManager.getLookAndFeel();
        SynthStyleFactory sf = laf.getStyleFactory();
        SynthStyle style = sf.getStyle(dummyTab, region);
        SynthContext context =
            new SynthContext(dummyTab, region, style, state);
        SynthPainter painter = style.getPainter(context);
        painter.paintTabbedPaneTabBackground(context, g, x, y, w, h, index);
    }
    
    private static class GtkPainter implements TabPainter {

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

            rect.y += (rect.height / 2);
            rect.height = 5;
        }

        public Polygon getInteriorPolygon(Component c) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = ren.isLeftmost() ? 1 : 0;
            int y = 1;

            int width = ren.isLeftmost() ? c.getWidth() - 1 : c.getWidth();
            int height = ren.isSelected() ?
                    c.getHeight() + 2 : c.getHeight() - 1;
                    
            //Modified to return rectangle
            p.addPoint(x, y);
            p.addPoint(x + width, y);
            p.addPoint(x + width, y + height);
            p.addPoint(x, y + height);
            return p;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            return;
        }
        

        public void paintInterior(Graphics g, Component c) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            if (ren.isSelected()) {
                paintTabBackground(g, 0, SynthConstants.SELECTED,
                p.getBounds().x, p.getBounds().y, p.getBounds().width, p.getBounds().height);
            } else {
                paintTabBackground(g, 0, 0,
                p.getBounds().x, p.getBounds().y + 1, p.getBounds().width, p.getBounds().height - 1);
            }
            
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


    private static class GtkLeftClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public Polygon getInteriorPolygon(Component c) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = -3;
            int y = 1;

            int width = c.getWidth() + 3;
            int height = ren.isSelected() ?
                    c.getHeight() + 2 : c.getHeight() - 1;

            //Modified to return rectangle
            p.addPoint(x, y);
            p.addPoint(x + width, y);
            p.addPoint(x + width, y + height);
            p.addPoint(x, y + height);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            return;
        }

        public void paintInterior(Graphics g, Component c) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            if (ren.isSelected()) {
                paintTabBackground(g, 0, SynthConstants.SELECTED,
                p.getBounds().x, p.getBounds().y, p.getBounds().width, p.getBounds().height);
            } else {
                paintTabBackground(g, 0, 0,
                p.getBounds().x, p.getBounds().y + 1, p.getBounds().width, p.getBounds().height - 1);
            }
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

    private static class GtkRightClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Polygon getInteriorPolygon(Component c) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = 0;
            int y = 1;

            int width = c.getWidth() + 10;
            int height = ren.isSelected() ?
                    c.getHeight() + 2 : c.getHeight() - 1;

            //Modified to return rectangle
            p.addPoint(x, y);
            p.addPoint(x + width, y);
            p.addPoint(x + width, y + height);
            p.addPoint(x, y + height);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
        }

        public void paintInterior(Graphics g, Component c) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;
            
            Polygon p = getInteriorPolygon(c);
            if (ren.isSelected()) {
                paintTabBackground(g, 0, SynthConstants.SELECTED,
                p.getBounds().x, p.getBounds().y, p.getBounds().width, p.getBounds().height);
            } else {
                paintTabBackground(g, 0, 0,
                p.getBounds().x, p.getBounds().y + 1, p.getBounds().width, p.getBounds().height - 1);
            }
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
