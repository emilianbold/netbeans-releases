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
 * AquaEditorTabCellRenderer.java
 *
 * Created on December 28, 2003, 12:04 AM
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import java.awt.*;
import org.netbeans.swing.tabcontrol.TabDisplayer;

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
                int centerY = ((bounds.height - (TOP_INSET + BOTTOM_INSET))
                        / 2) + TOP_INSET - 1;
                rect.x = bounds.x + bounds.width - 9;
                rect.y = centerY - 2;
                rect.height = 10;
                rect.width = 10;
                if (((AquaEditorTabCellRenderer) jc).isRightmost()) {
                    rect.x -= 2;
                }
            }
        }

        private void paintCloseButton(Graphics g, JComponent c) {
            if (((AbstractTabCellRenderer) c).isShowCloseButton()) {
                Rectangle r = new Rectangle(0, 0, c.getWidth(), c.getHeight());
                Rectangle cbRect = new Rectangle();
                getCloseButtonRectangle((JComponent) c, cbRect, r);
                //We return larger bounds than we want to paint to compensate
                //for antialiasing and the fact that lines are inclusive not
                //exclusive
                cbRect.width -= 5;
                cbRect.height -= 5;

                g.setColor(Color.darkGray);
                g.drawLine(cbRect.x, cbRect.y, cbRect.x + cbRect.width,
                           cbRect.y + cbRect.height);
                g.drawLine(cbRect.x, cbRect.y + cbRect.height,
                           cbRect.x + cbRect.width, cbRect.y);
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
            if ((((AquaEditorTabCellRenderer) c).getState()
                    & TabState.MOUSE_IN_TABS_AREA) != 0) {
                paintCloseButton(g, (JComponent) c);
            }
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
