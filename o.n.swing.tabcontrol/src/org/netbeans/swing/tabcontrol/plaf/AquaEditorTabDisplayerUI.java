/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * MetalEditorTabDisplayerUI.java
 *
 * Created on December 2, 2003, 9:40 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import org.netbeans.swing.tabcontrol.TabListPopupAction;

/**
 * A provisional look and feel for OS-X, round 2, using Java2d to emulate the
 * aqua look.
 *
 * @author Tim Boudreau
 */
public class AquaEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {
    private Insets taInsets = new Insets(0, 0, 2, 80);

    /** Color used in drawing the line behind the tabs */
    private Color lineMiddleColor = null;
    /** Color used in drawing the line behind the tabs */
    private Color lineHlColor = null;


    public AquaEditorTabDisplayerUI (TabDisplayer displayer) {
        super (displayer);
    }

    public void install() {
        super.install();
        scroll().setMinimumXposition(10);
    }

    protected TabCellRenderer createDefaultRenderer() {
        return new AquaEditorTabCellRenderer();
    }

    protected LayoutManager createLayout() {
        return new OSXTabLayout();
    }

    public Insets getTabAreaInsets() {
        return taInsets;
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new AquaEditorTabDisplayerUI ((TabDisplayer) c);
    }

    protected boolean isAntialiased() {
        return true;
    }
    
    protected Font createFont() {
        return UIManager.getFont("Label.font"); //NOI18N
    }    
    
    protected int createRepaintPolicy () {
        return TabState.REPAINT_SELECTION_ON_ACTIVATION_CHANGE
                | TabState.REPAINT_ON_SELECTION_CHANGE
                | TabState.REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON
                | TabState.REPAINT_ALL_ON_MOUSE_ENTER_TABS_AREA
                | TabState.REPAINT_ON_MOUSE_PRESSED;
    }
    

    protected void processMouseWheelEvent(MouseWheelEvent e) {
        //overridden to repaint the arrow buttons if the selected tab moves into
        //or out of view
        boolean wasShowing = selectionModel.getSelectedIndex()
                >= getFirstVisibleTab() && selectionModel.getSelectedIndex()
                <= getLastVisibleTab();

        super.processMouseWheelEvent(e);

        boolean stillShowing = selectionModel.getSelectedIndex()
                >= getFirstVisibleTab() && selectionModel.getSelectedIndex()
                <= getLastVisibleTab();

        if (wasShowing != stillShowing) {
            Component[] c = displayer.getComponents();
            for (int i = 0; i < c.length; i++) {
                c[i].repaint();
            }
        }
    }

    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 28;
        //Never call getGraphics() on the control, it resets in-process
        //painting on OS-X 1.4.1 and triggers gratuitous repaints
        Graphics g = BasicScrollingTabDisplayerUI.getOffscreenGraphics();
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom + 7;
        }
        if (prefHeight % 2 == 0) {
            prefHeight += 1;
        }
        return new Dimension(displayer.getWidth(), prefHeight);
    }

    private static final int BUTTON_SIZE = 19;

    protected AbstractButton[] createControlButtons() {
        JButton[] result = new JButton[3];
        result[0] = new TimerButton(scroll().getBackwardAction());
        result[1] = new TimerButton(scroll().getForwardAction());
        result[2] = new OnPressButton(new TabListPopupAction(displayer));
        configureButton(result[0], new LeftIcon());
        configureButton(result[1], new RightIcon());
        configureButton(result[2], new DownIcon());
        result[0].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        //This button draws no left/right side border, so make it wider
        result[1].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        result[2].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));


        result[0].setBorder(cborder);
        result[1].setBorder(cborder);
        result[2].setBorder(cborder);


        scroll().getBackwardAction().putValue("control", displayer); //NOI18N
        scroll().getForwardAction().putValue("control", displayer); //NOI18N

        return result;
    }

    private final Border cborder = new RadioButtonPseudoBorder();

    private static void configureButton(JButton button, Icon icon) {
        button.setIcon(icon);
        //        button.setMargin(null);
        button.setText(null);
        //undocumented (?) call to hide action text - see JButton line 234
        button.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
        button.setFocusable(false);
        button.setRolloverEnabled(false);
        button.setOpaque(false);
    }

    protected void paintAfterTabs(Graphics g) {
        //Draw the continuation of the rounded border behind the buttons
        //and tabs
        
        int centerY = (((displayer.getHeight() - 
            (AquaEditorTabCellRenderer.TOP_INSET + AquaEditorTabCellRenderer.BOTTOM_INSET)) / 2) 
            + AquaEditorTabCellRenderer.TOP_INSET - 1) + getTabAreaInsets().top;
        
        if (lineMiddleColor == null) {
            lineMiddleColor = ColorUtil.getMiddle(UIManager.getColor("controlShadow"), 
            UIManager.getColor("control")); //NOI18N
        }
        g.setColor (lineMiddleColor);
        
        int rightLineStart = getTabsAreaWidth() - 4;
        int rightLineEnd = displayer.getWidth() - 7;
        
        if (displayer.getModel().size() > 0 && !scroll().isLastTabClipped()) {
            //Extend the line out to the edge of the last visible tab
            //if none are clipped
            int idx = scroll().getLastVisibleTab(displayer.getWidth());
            rightLineStart = scroll().getX(idx) + scroll().getW(idx) + 1;
        } else if (displayer.getModel().size() == 0) {
            rightLineStart = 6;
        }
        
        if (scroll().getOffset() >= 0) {
            //fill the left edge notch
            g.drawLine(6, centerY, 13, centerY);
        }
        g.drawLine(rightLineStart, centerY, rightLineEnd, centerY);
        
        if (lineHlColor == null) {
            lineHlColor = ColorUtil.getMiddle (lineMiddleColor, 
            UIManager.getColor("control"));
        }
        
        g.setColor (lineHlColor); //NOI18N
        g.drawLine(rightLineStart, centerY+1, rightLineEnd, centerY+1);
        if (scroll().getOffset() > 0) {
            //fill the left edge notch
            g.drawLine(6, centerY+1, 10, centerY+1);
        }
    }
    
    static boolean aqua14204offByOneError = System.getProperty("java.version") != null &&
        System.getProperty("java.version").indexOf ("1.4.2_04") > -1;
    
    private class OSXTabLayout implements LayoutManager {

        public void addLayoutComponent(String name, java.awt.Component comp) {
        }

        public void layoutContainer(java.awt.Container parent) {
            Insets in = getTabAreaInsets();
            Component[] c = parent.getComponents();
            int x = parent.getWidth() - in.right + 3;
            int y = 0;
            Dimension psize;

            int centerY = (((displayer.getHeight() - (AquaEditorTabCellRenderer.TOP_INSET
                    + AquaEditorTabCellRenderer.BOTTOM_INSET)) / 2) + AquaEditorTabCellRenderer.TOP_INSET)
                    + getTabAreaInsets().top;

            for (int i = 0; i < c.length; i++) {
                psize = c[i].getPreferredSize();
//                y = in.top + 3; //hardcoded to spec
                y = centerY - (psize.height / 2);
                int w = Math.min(psize.width, parent.getWidth() - x);
                c[i].setBounds(x, y, w, Math.min(psize.height,
                                                 parent.getHeight()));
                x += psize.width;
            }
        }

        public Dimension minimumLayoutSize(java.awt.Container parent) {
            return getPreferredSize((JComponent) parent);
        }

        public Dimension preferredLayoutSize(java.awt.Container parent) {
            return getPreferredSize((JComponent) parent);
        }

        public void removeLayoutComponent(java.awt.Component comp) {
        }

    }

    private static final int ICON_WIDTH = 11;
    private static final int ICON_HEIGHT = 8;
    private static final int[] xpoints = new int[20];
    private static final int[] ypoints = new int[20];

    private static class LeftIcon implements Icon {
        protected boolean dontpaint = true;

        public int getIconHeight() {
            return ICON_HEIGHT;
        }

        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (dontpaint) {
                return;
            }
            if (!aqua14204offByOneError) {
                x -= 1;
            }
            y -= 3;
            g.setColor(c.isEnabled() ?
                       c.getForeground() :
                       UIManager.getColor("controlShadow")); //NOI18N
            int wid = getIconWidth();
            int hi = getIconHeight() + 1;
            xpoints[0] = x + (wid - 4);
            ypoints[0] = y + 1;

            xpoints[1] = xpoints[0];
            ypoints[1] = y + hi + 1;

            xpoints[2] = x + 2;
            ypoints[2] = y + (hi / 2) + 1;

            g.fillPolygon(xpoints, ypoints, 3);

        }
    }

    private static class RightIcon extends LeftIcon {
        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public int getIconHeight() {
            return ICON_HEIGHT - 1;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (dontpaint) {
                return;
            }
            y -= 2;
            g.setColor(c.isEnabled() ?
                       c.getForeground() :
                       UIManager.getColor("controlShadow")); //NOI18N
            int wid = getIconWidth();
            int hi = getIconHeight() + 1;
            xpoints[0] = x + 3; //x + (wid-4);
            ypoints[0] = y;

            xpoints[1] = x + 3;
            ypoints[1] = y + hi + 1;

            xpoints[2] = x + (wid - 4) + 1;//x+2;
            ypoints[2] = y + (hi / 2);

            g.fillPolygon(xpoints, ypoints, 3);

        }
    }

    private static class DownIcon extends LeftIcon {
        public int getIconHeight() {
            return ICON_HEIGHT + 2;
        }

        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (dontpaint) {
                return;
            }
            y -= 5;
            if (aqua14204offByOneError) {
                x+=1;
            }

            int wid = getIconWidth();
            if (wid % 2 == 0)
                wid--; //guarantee an odd number so lines are smooth
            int hi = getIconHeight();
            xpoints[0] = x-1;
            ypoints[0] = y + (hi / 2);

            xpoints[1] = (x + wid) - 2;
            ypoints[1] = y + (hi / 2);


            xpoints[2] = x + (wid / 2) - 1;
            ypoints[2] = (y + hi);
            g.setColor(c.isEnabled() ?
                       c.getForeground() :
                       UIManager.getColor("controlShadow")); //NOI18N
            g.fillPolygon(xpoints, ypoints, 3);

        }
    }

    private static BufferedImage img = null;
    private static final void createImages() {
        img = new BufferedImage (60, 20, BufferedImage.TYPE_INT_ARGB_PRE);
        JRadioButton jrb = new JRadioButton();
        CellRendererPane pane = new CellRendererPane();
        pane.add (jrb);
        jrb.setBounds (0, 0, 80, 20);
        jrb.setBackground(null);
        jrb.setOpaque(false);
        
        jrb.getModel().setRollover(false);
        jrb.setRolloverEnabled(false);
        jrb.getModel().setPressed(true);
        jrb.setSelected (false);
        
        Graphics g = img.getGraphics();
        jrb.paint (g);
        
        g.translate (20, 0);
        jrb.setSelected(true);
        jrb.paint (g);
        g.translate (20, 0);
        
        jrb.setEnabled(false);
        jrb.paint (g);
    }
    
    
    private class RadioButtonPseudoBorder implements Border {
        public RadioButtonPseudoBorder() {
            if (img == null) {
                createImages();
            }
        }
        
        public Insets getBorderInsets(Component c) {
            Insets ins = new Insets(3, 3, 3, 3);
            return ins;
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {

            
            Icon ic = ((JButton) c).getIcon();
            
            int pos = 0;
            if (!c.isEnabled()) {
                pos = 40;
            } else {
                boolean shouldSelect = selectionModel.getSelectedIndex() != -1
                    && displayer.isActive() && ((ic.getClass()
                    == LeftIcon.class && selectionModel.getSelectedIndex()
                    < getFirstVisibleTab()) || (ic.getClass()
                    == RightIcon.class && selectionModel.getSelectedIndex()
                    > getLastVisibleTab()));
                if (shouldSelect) {
                    pos = 20;
                }
                if (shouldSelect) {
                    c.setForeground(new Color(0, 64, 128));
                } else {
                    c.setForeground(UIManager.getColor("controlDkShadow")); //NOI18N
                }            
            }
            Graphics2D g2d = (Graphics2D) g;
            AffineTransform at = AffineTransform.getTranslateInstance(-pos, -1);
            g2d.drawRenderedImage(img, at);

            ((LeftIcon) ic).dontpaint = false;
            ic.paintIcon(c, g, x + (width / 4) + 2, y + (width / 4) + 3);
            ((LeftIcon) ic).dontpaint = true;

        }
    }
}
