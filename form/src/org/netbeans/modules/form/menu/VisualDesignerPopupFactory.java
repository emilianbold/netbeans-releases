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

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import org.netbeans.modules.form.menu.VisualDesignerJPanelPopup;

/** A PopupFactory which returns VisualDesignerJPanelPopup's
 *
 * @author joshua.marinacci@sun.com
 */
class VisualDesignerPopupFactory extends PopupFactory {
    private static final boolean DEBUG = false;
    
    public Map<JMenu, JPanel> containerMap;
    public Map<JMenu, Popup> popupMap;
    
    public MenuEditLayer canvas;
    
    public VisualDesignerPopupFactory(MenuEditLayer canvas) {
        containerMap = new HashMap<JMenu, JPanel>();
        popupMap = new HashMap<JMenu, Popup>();
        this.canvas = canvas;
    }
    
    public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
        //p("getting a popup factory: " + owner + " " + contents + " " + x + " " + y);
        
        JMenu menu = (JMenu) owner;
        //p("creating a popup for: " + menu.getText());
        JComponent parent = canvas.getMenuParent(menu);
        //p("menu's parent = " + parent);
        JPanel cont = containerMap.get(menu);
        
        if (cont == null) {
            cont = new VisualDesignerJPanelContainer(menu);
            cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));//GridLayout(10,1));
            for(Component c : menu.getMenuComponents()) {
                cont.add(c);
            }
            cont.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            containerMap.put(menu,cont);
            canvas.add(cont);
        }
        
        // if the parent is a menu then this is a submenu
        if(parent instanceof JMenu) {
            setLocationFromMenu(menu, cont);
        } else {
            // x/y are in abs screen coords. must convert to local
            Point pt = new Point(x,y);
            //        p("original point = " + pt);
            SwingUtilities.convertPointFromScreen(pt,canvas);
            //        p("converted poitn = " + pt);
            cont.setLocation(pt);
        }
        
        
        //p("size = " + cont.getSize());
        //p("min = " + cont.getLayout().preferredLayoutSize(cont));
        cont.setSize(cont.getLayout().preferredLayoutSize(cont));
        //p("size = " + cont.getSize());
        canvas.validate();
        canvas.repaint();
        //canvas.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        //        p("returning a new jpanel popup at: " + cont.getLocation());
        Popup popup = new VisualDesignerJPanelPopup(cont, menu, this);
        popupMap.put(menu,popup);
        return popup;
        //return PopupFactory.getSharedInstance().getPopup(owner, contents, x, y);
    }
    
    private void setLocationFromMenu(final JMenu menu, final JPanel cont) {
        Point pt = menu.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(pt,canvas);
        
        JComponent parent = canvas.getMenuParent(menu);
        p("parent = " + parent);
        if(parent instanceof JMenu) {
            // get this menu's location in local coords
            // move to the right edge of the menu
            pt = new Point(pt.x + menu.getWidth(), pt.y);
        } else {
            // if parent isn't a jmenu the this must be a toplevel,
            // so we must position below the menu instead of next to it
            pt = new Point(pt.x, pt.y + menu.getHeight());
        }
        cont.setLocation(pt);
    }
    
    private void p(String string) {
        if(DEBUG) {
            System.out.println(string);
        }
    }
    
    void hideOtherMenus(JMenu menu) {
        p("hiding other menus");
        for(JMenu m : containerMap.keySet()) {
            if(m != menu) {
                // hide if not an ancestor of this menu
                p("checking for ancestor");
                if(!isAncestor(m,menu)) {
                    JPanel popup = containerMap.get(m);
                    popup.setVisible(false);
                }
            }
        }
    }
    private boolean isAncestor(JMenu m, JMenu menu) {
        return canvas.isAncestor(menu, m);
    }
    
    private class VisualDesignerJPanelContainer extends JPanel {
        JMenu menu;
        VisualDesignerJPanelContainer(JMenu menu) {
            this.menu = menu;
        }
        public void setVisible(boolean visible) {
            p("setting vis : " + visible);
            // if making visible
            if(visible) {
                // make sure the other menus are hidden
                hideOtherMenus(menu);
                // make sure this menu popup is at the right place
                setLocationFromMenu(menu,this);
                // repack?
            }
            super.setVisible(visible);
        }
    }
    
}