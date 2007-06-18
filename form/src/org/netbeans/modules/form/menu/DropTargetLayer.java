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
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.menu.DropTargetLayer.DropTargetType;
import org.netbeans.modules.form.menu.MenuEditLayer.SelectedPortion;

/**
 *  This component handles drawing all drop targets for the menu bar and
 * the selection rects around selected menu item. It does not currently
 * handle the other selection rects (separators or menus) and it does not
 * currently handle the drop target for dragging between menu items or within
 * menus. This class should be updated to handle all drop target drawing instead
 * of using the border hacks.
 *
 * Also note that this class attempts to calculate the position of the icon
 * and accelerator within the menu item. It cannot do this completely accurately
 * since the API for JMenuItem does not expose such metrics. This class therefore
 * contains some hard coded values and attempts to use L&F specific information to
 * fine tune the accelerator positioning.
 *
 * @author joshy
 */
public class DropTargetLayer extends JComponent {
    public enum DropTargetType { INTER_MENU, NONE, INTO_SUBMENU }
    
    private static final boolean DEBUG = false;
    private MenuEditLayer canvas;
    public RADComponent menuBarDropTargetComponent = null;
    public Point menuBarDropTargetPoint;
    private Point currentTargetPoint;
    private DropTargetType currentTargetType;
    private JComponent currentTargetComponent;
    
    //private static BasicStroke DROP_TARGET_LINE_STROKE = new BasicStroke(2,
    //        BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f,  new float[] {5f, 5f}, 0f);
    private static BasicStroke DROP_TARGET_LINE_STROKE = new BasicStroke(
          3.0f, BasicStroke.CAP_ROUND, (int)1, 1.0f,
          new float[] {6.0f,6.0f}, 0.0f);

    private static Color DROP_TARGET_COLOR = new Color(0xFFA400);
    private static Color SELECTION_COLOR = DROP_TARGET_COLOR;
    private static BasicStroke SELECTION_STROKE = new BasicStroke(1);
    
    public DropTargetLayer(MenuEditLayer canvas) {
        this.canvas = canvas;
    }
    
    
    public void setDropTargetComponent(JComponent targetComponent, DropTargetLayer.DropTargetType type, Point pt) {
        currentTargetComponent = targetComponent;
        currentTargetType = type;
        currentTargetPoint = pt;
        menuBarDropTargetComponent = null;
        menuBarDropTargetPoint = null;
        //josh: can i combine the menubar and normal targets into a single one?
    }
    
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        if(DEBUG) {
            g.setColor(Color.GREEN);
            g.drawString("DropTarget Layer ", 30,100);
        }
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if(menuBarDropTargetComponent != null) {
            RADComponent comp = canvas.getFormMenuBar();
            if(comp != null && canvas.formDesigner != null) {
                g2.setColor(DROP_TARGET_COLOR);
                g2.setStroke(DROP_TARGET_LINE_STROKE);
                if(comp == menuBarDropTargetComponent) { // over the menu bar
                    JComponent mb = (JComponent) canvas.formDesigner.getComponent(comp);
                    Point mblocation = SwingUtilities.convertPoint(mb, new Point(0,0), this);
                    if(mb.getComponentCount() > 0) {
                        Component lastComp = mb.getComponent(mb.getComponentCount()-1);
                        mblocation.x += lastComp.getX() + lastComp.getWidth();
                    }
                    g2.drawRect(mblocation.x+2, mblocation.y+2, mb.getHeight()-4, mb.getHeight()-4);
                } else { // over a toplevel menu. draw between them
                    
                    
                    JComponent menu = (JComponent) canvas.formDesigner.getComponent(menuBarDropTargetComponent);
                    Point mblocation = SwingUtilities.convertPoint(menu, new Point(0,0), this);
                    Point cursorLocation = SwingUtilities.convertPoint(this, menuBarDropTargetPoint, menu);
                    int size = menu.getHeight()-4;
                    
                    g2.setColor(DROP_TARGET_COLOR);
                    
                    if(cursorLocation.x < 15) { // left edge
                        drawVerticalTargetLine(g2, mblocation.x-1, mblocation.y, 50);
                    } else if(cursorLocation.x > menu.getWidth()-15) { // right edge
                        drawVerticalTargetLine(g2, mblocation.x+menu.getWidth(), mblocation.y, 50);
                        //g2.drawRect(mblocation.x+menu.getWidth()-size/2, mblocation.y+2, size, size);
                    } else { // center drop
                        g2.drawRect(mblocation.x, mblocation.y, menu.getWidth(), menu.getHeight());
                    }
                }
            }
        }
        
        // draw the menu item subselection rectangles
        JComponent selected = canvas.getSelectedComponent();
        // style only menuitems and menus that aren't also toplevel menus
        if(selected instanceof JMenuItem &&
                !(selected.getParent() instanceof JMenuBar)) { // && !(selected instanceof JMenu)) {
            JMenuItem item = (JMenuItem) selected;
            Point location = SwingUtilities.convertPoint(item, new Point(0,0), this);
            g2.translate(location.x,location.y);
            
            int iconGap = item.getIconTextGap();
            int iconLeft = getIconLeft(item);
            int iconWidth = getIconWidth(item);
            int iconHeight = getIconHeight(item);
            int iconTop = (item.getHeight()-iconHeight)/2;
            int accelWidth = getAcceleratorWidth(item);
            
            int textWidth = item.getWidth() - iconLeft - iconWidth - iconGap - accelWidth;
            int textLeft = iconLeft + iconWidth + iconGap;
            int accelLeft = item.getWidth() - accelWidth;
            
            // draw bounding boxes
            g2.setColor(Color.LIGHT_GRAY);
            //g2.drawRect(iconLeft, 0, iconWidth-1, item.getHeight()-1);
            //g2.drawRect(textLeft, 0, textWidth-1, item.getHeight()-1);
            g2.drawRect(accelLeft, 0, accelWidth - 1, item.getHeight() - 1);
            
            // draw the selection rectangles
            g2.setStroke(SELECTION_STROKE);
            g2.setColor(SELECTION_COLOR);
            switch(canvas.getCurrentSelectedPortion()) {
            case Icon: {
                if(item.getIcon() != null) {
                    g2.drawRect(iconLeft-1, iconTop-1, iconWidth+1, iconHeight+1);
                }
                break;
            }
            case Text: {
                g2.drawRect(iconLeft + iconWidth + iconGap -1, -1, textWidth+1, item.getHeight()+1);
                break;
            }
            case Accelerator: {
                if(item instanceof JMenu) break;
                g2.drawRect(accelLeft -1 , -1, accelWidth+1, item.getHeight()+1);
                break;
            }
            case All: {
                g2.drawRect(0,0,item.getWidth()-1, item.getHeight()-1);
            }
            }
            g2.translate(-location.x,-location.y);
        }
        
        // draw the drop target
        if(currentTargetComponent != null) {
            if(currentTargetType == DropTargetType.INTER_MENU) {
                Point loc = SwingUtilities.convertPoint(currentTargetComponent, new Point(0,0), this);
                int x = loc.x;
                int y = loc.y;
                g2.translate(x,y);
                drawHorizontalTargetLine(g2, -10, 0, currentTargetComponent.getWidth()+20);
                g2.translate(-x,-y);
            }
            if(currentTargetType == DropTargetType.INTO_SUBMENU) {
                Point loc = SwingUtilities.convertPoint(currentTargetComponent, new Point(0,0), this);
                int x = loc.x;
                int y = loc.y;
                int w = currentTargetComponent.getWidth();
                int h = currentTargetComponent.getHeight();
                g2.translate(x,y);
                g2.drawRect(0,0,w,h);
                drawVerticalTargetLine(g2, currentTargetComponent.getWidth(), -10, currentTargetComponent.getHeight()+20);
                g2.translate(-x,-y);
            }
        }
        
        g2.dispose();
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
        return 10; // gutter space that we can click on to add an accelerator
    }
    
    private static int getAcceleratorLeft(JMenuItem item) {
        return item.getWidth() - getAcceleratorWidth(item);
    }
    
    public static MenuEditLayer.SelectedPortion calculateSelectedPortion(JMenuItem item, Point localPt) {
        // josh: change these hard coded values to be based on the real component
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
        return 14;
    }
    
    private static int getIconRight(JMenuItem item) {
        return getIconLeft(item) + getIconWidth(item);
    }
    
    
    private static void p(String s) {
        if(DEBUG) {
            System.out.println(s);
        }
    }
}
