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
 * GtkEditorTabCellRenderer.java
 *
 * Created on December 28, 2003, 12:04 AM
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import java.awt.*;

/**
 * A tab cell renderer for OS-X.  Basically does its work by subclassing JButton
 * and doing some tricks to use it as a cell renderer, so the Aqua borders do
 * all the heavy eye-candy lifting (how's that for mixed metaphors?)
 *
 * @author Tim Boudreau
 */
final class GtkEditorTabCellRenderer extends AbstractTabCellRenderer {
    private static final GtkTabPainter GtkTabPainter = new GtkTabPainter();

    static final int TOP_INSET = 1;
    static final int LEFT_INSET = 12;
    static final int RIGHT_INSET = 6;
    static final int BOTTOM_INSET = 2;

    private static final GtkChiclet chiclet = GtkChiclet.INSTANCE1;

    public GtkEditorTabCellRenderer() {
        super(GtkTabPainter, GtkTabPainter, GtkTabPainter,
              new Dimension(33, 8));
    }

    protected int getCaptionYAdjustment() {
        return 1;
    }

    protected int getIconYAdjustment() {
        return -1;
    }
    
    protected int stateChanged(int oldState, int newState) {
        return newState;
    }    
    
    public Color getForeground() {
        return chiclet.getTextForeground();
    }

    private static class GtkTabPainter implements TabPainter {
        private static Insets insets = new Insets(TOP_INSET, LEFT_INSET,
                                                  BOTTOM_INSET, RIGHT_INSET);

        public GtkTabPainter() {
        }

        public Insets getBorderInsets(Component c) {
            boolean leftmost = ((GtkEditorTabCellRenderer) c).isLeftmost();

            if (leftmost) {
                return new Insets(TOP_INSET, LEFT_INSET + 1, BOTTOM_INSET,
                                  RIGHT_INSET);
            } else {
                return insets;
            }
        }

        public void getCloseButtonRectangle(JComponent jc, Rectangle rect,
                                            Rectangle bounds) {
            boolean rightClip = ((GtkEditorTabCellRenderer) jc).isClipRight();
            boolean leftClip = ((GtkEditorTabCellRenderer) jc).isClipLeft();
            if (leftClip || rightClip) {
                rect.x = -100;
                rect.y = -100;
                rect.width = 0;
                rect.height = 0;
            } else {
                int centerY = ((bounds.height - (TOP_INSET + BOTTOM_INSET))
                        / 2) + TOP_INSET - 1;
                rect.x = bounds.x + bounds.width - 11;
                rect.y = centerY;
                rect.height = 10;
                rect.width = 10;
                if (((GtkEditorTabCellRenderer) jc).isRightmost()) {
                    rect.x -= 2;
                }
            }
        }

        private void paintCloseButton(Graphics g, JComponent c) {
            Rectangle r = new Rectangle(0, 0, c.getWidth(), c.getHeight());
            Rectangle cbRect = new Rectangle();
            getCloseButtonRectangle((JComponent) c, cbRect, r);

            //We return larger bounds than we want to paint to compensate
            //for antialiasing and the fact that lines are inclusive not
            //exclusive
            cbRect.width -= 5;
            cbRect.height -= 5;

            g.setColor(chiclet.getLight()); //XXX don't hardcode
            g.drawLine(cbRect.x, cbRect.y, cbRect.x + cbRect.width,
                       cbRect.y + cbRect.height);
            g.drawLine(cbRect.x, cbRect.y + cbRect.height,
                       cbRect.x + cbRect.width, cbRect.y);

            cbRect.translate(-1,-1);
            g.setColor(chiclet.getDark()); //XXX don't hardcode
            g.drawLine(cbRect.x, cbRect.y, cbRect.x + cbRect.width,
                       cbRect.y + cbRect.height);
            g.drawLine(cbRect.x, cbRect.y + cbRect.height,
                       cbRect.x + cbRect.width, cbRect.y);

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
            if ((((GtkEditorTabCellRenderer) c).getState()
                    & TabState.ARMED) != 0) {
                paintCloseButton(g, (JComponent) c);
            }
            
            boolean leftClip = ((GtkEditorTabCellRenderer) c).isClipLeft();
            boolean leftmost = ((GtkEditorTabCellRenderer) c).isLeftmost();
            boolean active = ((GtkEditorTabCellRenderer) c).isActive();
            boolean rightmost = ((GtkEditorTabCellRenderer) c).isRightmost();
            boolean rightClip = ((GtkEditorTabCellRenderer) c).isClipRight();
            
            if (!active && (leftmost || rightmost)) {
                GradientPaint gp = ColorUtil.getGradientPaint(x, 
                    y + (c.getHeight() / 2), UIManager.getColor("control"), //NOI18N
                    x, y + c.getHeight(), UIManager.getColor("controlShadow")); //NOI18N
                ((Graphics2D) g).setPaint (gp);
                if (leftmost && !leftClip) {
                    g.drawLine (x, c.getHeight() / 2, x, c.getHeight());
                }
                if (rightmost && !rightClip) {
                    g.drawLine (x + width, c.getHeight() / 2, x + width, c.getHeight());
                }
            }
        }


        public void paintInterior(Graphics g, Component c) {
            if (true) {
                Rectangle bds = c.getBounds();

                boolean rightmost = ((GtkEditorTabCellRenderer) c).isRightmost();
                boolean rightClip = ((GtkEditorTabCellRenderer) c).isClipRight();
                boolean sel = ((GtkEditorTabCellRenderer) c).isSelected();
                boolean active = ((GtkEditorTabCellRenderer) c).isActive();
                boolean pressed = ((GtkEditorTabCellRenderer) c).isPressed();
                boolean leftClip = ((GtkEditorTabCellRenderer) c).isClipLeft();
                boolean leftmost = ((GtkEditorTabCellRenderer) c).isLeftmost();
                boolean closing = pressed
                        && ((GtkEditorTabCellRenderer) c).inCloseButton();

                //add in a pixel for rightmost/leftmost so we don't clip off
                //antialiasing of the curve
                chiclet.setBounds(0, 0, bds.width, bds.height);

                chiclet.setNotch(rightClip, leftClip);
                int state = 0;
                float leftarc = leftmost && !leftClip ? 0.5f : 0f;
                float rightarc = rightmost && !rightClip ? 0.5f : 0f;

                if (pressed) {
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
                chiclet.setArcs(leftarc, rightarc, 0f, 0f);
                chiclet.setInverted(false);

                chiclet.setState (state);
                chiclet.setDrawOutline(true);
                chiclet.draw((Graphics2D) g);

                if (sel) {
                    chiclet.setBounds (3, 3, bds.width-5, bds.height-5);
                    chiclet.setNotch (false, false);
                    chiclet.setArcs (0.5f, 0.5f, 0.5f, 0.5f);
                    chiclet.setInverted (true);
                    chiclet.draw((Graphics2D) g);
                }

                return;
            }
        }

        public boolean supportsCloseButton(JComponent c) {
            boolean leftClip = ((GtkEditorTabCellRenderer) c).isClipLeft();
            boolean rightClip = ((GtkEditorTabCellRenderer) c).isClipRight();

            return !leftClip && !rightClip;
        }

    }
}
