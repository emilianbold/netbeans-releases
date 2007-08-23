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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form.menu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.menu.DropTargetLayer.DropTargetType;
import org.netbeans.modules.form.menu.MenuEditLayer.SelectedPortion;

/**
 *  This component handles drawing all drop targets for the menu bar and
 * the selection rects around selected menu item.
 *
 * Also note that this class attempts to calculate the position of the icon
 * and accelerator within the menu item. It cannot do this completely accurately
 * since the API for JMenuItem does not expose such metrics. This class therefore
 * contains some hard coded values and attempts to use L&F specific information to
 * fine tune the accelerator positioning.
 *
 * @author joshy
 */
class DropTargetLayer extends JComponent {


    public enum DropTargetType { INTER_MENU, NONE, INTO_SUBMENU }
    
    private static final boolean DEBUG = false;
    private MenuEditLayer canvas;
    private Point currentTargetPoint;
    private DropTargetType currentTargetType;
    private JComponent currentTargetComponent;
    private JComponent selectedComponent;
    
    //private static BasicStroke DROP_TARGET_LINE_STROKE = new BasicStroke(2,
    //        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f,  new float[] {5f, 5f}, 0f);
    private static BasicStroke DROP_TARGET_LINE_STROKE = new BasicStroke(
          3.0f, BasicStroke.CAP_ROUND, 1, 1.0f,
          new float[] {6.0f,6.0f}, 0.0f);

    private static Color DROP_TARGET_COLOR = new Color(0xFFA400);
    private static Color SELECTION_COLOR = DROP_TARGET_COLOR;
    private static BasicStroke SELECTION_STROKE = new BasicStroke(1);
    
    public DropTargetLayer(MenuEditLayer canvas) {
        this.canvas = canvas;
    }
    
    
    public void setSelectedComponent(JComponent selectedComponent) {
        this.selectedComponent = selectedComponent;
        repaint();
    }
    
    public void setDropTarget(JComponent comp, Point pt, DropTargetLayer.DropTargetType type) {
        currentTargetComponent = comp;
        currentTargetPoint = pt;
        currentTargetType = type;
        repaint();
    }
    
    public void setDropTarget(RADComponent comp, Point pt) {
        currentTargetComponent = (JComponent) canvas.formDesigner.getComponent(comp);
        currentTargetPoint = pt;
        currentTargetType = DropTargetType.NONE;
        repaint();
    }
    
    public void setDropTarget(RADComponent comp, Point pt, DropTargetLayer.DropTargetType type) {
        currentTargetComponent = (JComponent) canvas.formDesigner.getComponent(comp);
        currentTargetPoint = pt;
        currentTargetType = type;
        repaint();
    }
    
    public void clearDropTarget() {
        currentTargetComponent = null;
        currentTargetType = DropTargetType.NONE;
        currentTargetPoint = null;
        repaint();
    }
    
    
    
    public static boolean isMenuRightEdge(Point pt, JComponent menu) {
        return pt.x > menu.getWidth()-8;
    }
    public static boolean isMenuLeftEdge(Point pt, JComponent menu) {
        return pt.x < 8;
    }
    public static boolean isSubMenuRightEdge(Point pt, JComponent menu) {
        return pt.x > menu.getWidth()-30;
    }
    
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        if(DEBUG) {
            g.setColor(Color.GREEN);
            g.drawString("DropTarget Layer ", 30,100); //NOI18N
        }
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // drag effects that go in the toplevel of the menubar
        if(currentTargetComponent != null) {
            RADComponent comp = canvas.getFormMenuBar();
            if(comp != null && canvas.formDesigner != null) {
                g2.setColor(DROP_TARGET_COLOR);
                g2.setStroke(DROP_TARGET_LINE_STROKE);
                
                // if over the menubar itself, rather than a toplevel JMenu
                if(canvas.formDesigner.getMetaComponent(currentTargetComponent) == comp) { 
                    //draw orange rect where you could drop a new menu item
                    drawOpenSpotAtEndOfMenuBar(g2,currentTargetComponent);
                } else {
                    
                    // if this is a toplevel JMenu, draw between them
                    if(currentTargetComponent.getParent() instanceof JMenuBar) {
                        JComponent menu = currentTargetComponent;
                        JComponent menubar = (JComponent) currentTargetComponent.getParent();
                        Point mblocation = SwingUtilities.convertPoint(menu, new Point(0, 0), this);
                        Point cursorLocation = SwingUtilities.convertPoint(this, currentTargetPoint, menu);

                        g2.setColor(DROP_TARGET_COLOR);
                        if(currentTargetType != DropTargetType.INTO_SUBMENU) {
                            if (isMenuLeftEdge(cursorLocation, menu)) {
                                drawVerticalTargetLine(g2, mblocation.x - 1, mblocation.y, 50);
                            } else if (isMenuRightEdge(cursorLocation, menu)) {
                                //if last toplevel menu, draw the rect target instead of a linef
                                if(isLastChild(menu,menubar)) {
                                    drawOpenSpotAtEndOfMenuBar(g2, menubar);
                                } else {
                                    drawVerticalTargetLine(g2, mblocation.x + menu.getWidth(), mblocation.y, 50);
                                }
                            } else {
                                // center drop
                                g2.drawRect(mblocation.x, mblocation.y, menu.getWidth(), menu.getHeight());
                            }
                        }
                    }
                }
            }
        }
        
        // draw the menu item subselection rectangles
        //JComponent selected = null;//canvas.getSelectedComponent();
        for(RADComponent rad : canvas.getSelectedRADComponents()) {
            if(rad != null) {
                JComponent selected = (JComponent) canvas.formDesigner.getComponent(rad);
                drawSelectedComponent(g2, selected);
            }
        }
        
        g2.dispose();
    }
    
    
    private boolean isLastChild(JComponent child, JComponent parent) {
        if(parent == null) return false;
        if(parent.getComponentCount() < 1) return false;
        return (child == parent.getComponent(parent.getComponentCount()-1));
    }
    //draw orange rect where you could drop a new menu item
    private  void drawOpenSpotAtEndOfMenuBar(Graphics2D g2, JComponent mb) {
        Point mblocation = SwingUtilities.convertPoint(mb, new Point(0,0), this);
        if(mb.getComponentCount() > 0) {
            Component lastComp = mb.getComponent(mb.getComponentCount()-1);
            mblocation.x += lastComp.getX() + lastComp.getWidth();
        }
        g2.drawRect(mblocation.x+2, mblocation.y+2, mb.getHeight()-4, mb.getHeight()-4);
    }
    private void drawSelectedComponent(Graphics2D g2, JComponent selected) {
        // draw normal border around toplevel menus
        if (selected instanceof JMenu && selected.getParent() instanceof JMenuBar) {
            JMenuItem menu = (JMenuItem) selected;
            Point location = SwingUtilities.convertPoint(menu, new Point(0, 0), this);
            g2.translate(location.x, location.y);
            g2.setStroke(SELECTION_STROKE);
            g2.setColor(SELECTION_COLOR);
            g2.drawRect(0, 0, menu.getWidth() - 1, menu.getHeight() - 1);
            g2.translate(-location.x, -location.y);
        }

        // style only menuitems and menus that aren't also toplevel menus
        // don't do subrect drawing if doing a drag
        if (selected instanceof JMenuItem && !(selected.getParent() instanceof JMenuBar) && currentTargetComponent == null) {
            JMenuItem item = (JMenuItem) selected;
            drawSubselectedItem(g2, item);
        }

        // draw the drop target
        if (currentTargetComponent != null) {
            if (currentTargetType == DropTargetType.INTER_MENU) {
                Point loc = SwingUtilities.convertPoint(currentTargetComponent, new Point(0, 0), this);
                int x = loc.x;
                int y = loc.y;
                g2.translate(x, y);
                drawHorizontalTargetLine(g2, -10, 0, currentTargetComponent.getWidth() + 20);
                g2.translate(-x, -y);
            }

            if (currentTargetType == DropTargetType.INTO_SUBMENU) {
                Point loc = SwingUtilities.convertPoint(currentTargetComponent, new Point(0, 0), this);
                int x = loc.x;
                int y = loc.y;
                int w = currentTargetComponent.getWidth();
                int h = currentTargetComponent.getHeight();
                g2.translate(x, y);
                g2.drawRect(0, 0, w, h);
                //drawVerticalTargetLine(g2, currentTargetComponent.getWidth(), -10, currentTargetComponent.getHeight()+20);
                g2.translate(-x, -y);
            }
        }
    }


    private void drawSubselectedItem(Graphics2D g2, JMenuItem item) {
        Point location = SwingUtilities.convertPoint(item, new java.awt.Point(0, 0), this);
        g2.translate(location.x, location.y);

        int iconGap = item.getIconTextGap();
        int iconLeft = getIconLeft(item);
        int iconWidth = getIconWidth(item);
        int iconHeight = getIconHeight(item);
        int iconTop = (item.getHeight() - iconHeight) / 2;
        int accelWidth = getAcceleratorWidth(item);

        int textWidth = item.getWidth() - iconLeft - iconWidth - iconGap - accelWidth;
        int textLeft = iconLeft + iconWidth + iconGap;
        int accelLeft = item.getWidth() - accelWidth;

        // draw bounding boxes
        g2.setColor(Color.LIGHT_GRAY);
        //g2.drawRect(iconLeft, 0, iconWidth-1, item.getHeight()-1);
        //g2.drawRect(textLeft, 0, textWidth-1, item.getHeight()-1);
        //draw the accelerator areaa
        //g2.drawRect(accelLeft, 0, accelWidth - 1, item.getHeight() - 1);

        // draw the selection rectangles
        g2.setStroke(SELECTION_STROKE);
        g2.setColor(SELECTION_COLOR);
        switch (canvas.getCurrentSelectedPortion()) {
            case Icon:
                {
                    if (item.getIcon() != null) {
                        g2.drawRect(iconLeft - 1, iconTop - 1, iconWidth + 1, iconHeight + 1);
                    }
                    break;
                }
            case Text:
                {
                    g2.drawRect(iconLeft + iconWidth + iconGap - 1, -1, textWidth + 1, item.getHeight() + 1);
                    break;
                }
            case Accelerator:
                {
                    if (item instanceof javax.swing.JMenu) {
                        break;
                    }
                    g2.drawRect(accelLeft - 1, -1, accelWidth + 1, item.getHeight() + 1);
                    break;
                }
            case All:
                {
                    g2.drawRect(0, 0, item.getWidth() - 1, item.getHeight() - 1);
                }
        }
        g2.translate(-location.x, -location.y);
    }
    
    
    private static void drawHorizontalTargetLine(Graphics2D g, int x, int y, int len) {
        g.setColor(DROP_TARGET_COLOR);
        g.setStroke(DROP_TARGET_LINE_STROKE);
        g.drawLine(x, y-1, x-1+len, y-1);
    }
    
    private static void drawVerticalTargetLine(Graphics2D g, int x, int y, int len) {
        g.setColor(DROP_TARGET_COLOR);
        g.setStroke(DROP_TARGET_LINE_STROKE);
        g.drawLine(x-1, y, x-1, y+len);
    }
    
    // josh: hard coded. must calculate in the future
    private static int getAcceleratorWidth(JMenuItem item) {
        if(item instanceof JMenu) return 0;
        if(item.getAccelerator() != null) return 50;
        return MenuEditLayer.ACCEL_PREVIEW_WIDTH; // gutter space that we can click on to add an accelerator
    }
    
    private static int getAcceleratorLeft(JMenuItem item) {
        return item.getWidth() - getAcceleratorWidth(item);
    }
    
    public static MenuEditLayer.SelectedPortion calculateSelectedPortion(JMenuItem item, Point localPt) {
        if(localPt.x <= getIconRight(item)) {
            return SelectedPortion.Icon;
        }
        
        if(localPt.x > getAcceleratorLeft(item)) {
            return SelectedPortion.Accelerator;
        }
        
        return SelectedPortion.Text;
    }
    
    private static int getIconWidth(JMenuItem item) {
        int iconWidth = item.getIcon() != null ? item.getIcon().getIconWidth() : 0;
        return iconWidth;
    }
    
    private static int getIconHeight(JMenuItem item) {
        int iconHeight = item.getIcon() != null ? item.getIcon().getIconHeight() : 0;
        return iconHeight;
    }
    
    //josh: hard coded to account for the checkbox gutter. replace in the future
    // with a calculated value
    private static int getIconLeft(JMenuItem item) {
        if(isWindows()) {
            if(hasRadioOrCheckSibling(item)) {
                return 22;
            } else {
                return 20;
            }
        }
        if(item instanceof JRadioButtonMenuItem) {
            return 11;
        }
        return 14;
    }
    
    
    private static boolean hasRadioOrCheckSibling(JMenuItem item) {
        if(item.getParent() == null) return false;
        for(Component c : item.getParent().getComponents()) {
            if(c instanceof JRadioButtonMenuItem) return true;
            if(c instanceof JCheckBoxMenuItem) return true;
        }
        return false;
    }
    
    private static int getIconRight(JMenuItem item) {
        return getIconLeft(item) + getIconWidth(item);
    }
    
 
    
    private static boolean isWindows() {
        if(System.getProperty("os.name").startsWith("Windows")) {
            return true;
        }
        return false;
    }
    private static void p(String s) {
        if(DEBUG) {
            System.out.println(s);
        }
    }
}
