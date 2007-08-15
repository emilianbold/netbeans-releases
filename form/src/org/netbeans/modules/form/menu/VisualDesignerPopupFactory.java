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
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADVisualContainer;

/** A PopupFactory which returns VisualDesignerJPanelPopup's
 *
 * @author joshua.marinacci@sun.com
 */
class VisualDesignerPopupFactory extends PopupFactory {
    private static final boolean DEBUG = false;
    
    public Map<JMenu, JPanel> containerMap;
    private Map<JMenu, VisualDesignerJPanelPopup> popupMap;
    
    private MenuEditLayer canvas;
    
    public VisualDesignerPopupFactory(MenuEditLayer canvas) {
        containerMap = new HashMap<JMenu, JPanel>();
        popupMap = new HashMap<JMenu, VisualDesignerJPanelPopup>();
        this.canvas = canvas;
    }
    
    public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
        
        final JMenu menu = (JMenu) owner;
        p("creating a popup for: " + menu.getText());
        JComponent parent = canvas.getMenuParent(menu);
        //p("menu's parent = " + parent);
        JPanel cont = containerMap.get(menu);
        
        if (cont == null) {
            cont = new VisualDesignerJPanelContainer(menu,this);
            cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));//GridLayout(10,1));
            
            
            RADVisualContainer menuRAD = (RADVisualContainer) canvas.formDesigner.getMetaComponent(menu);
            for(RADComponent c : menuRAD.getSubBeans()) {
                JComponent comp = (JComponent) canvas.formDesigner.getComponent(c);
                cont.add(comp);
            }
            
            cont.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            containerMap.put(menu,cont);
            canvas.layers.add(cont, JLayeredPane.DEFAULT_LAYER);
        }
        
        // if the parent is a menu then this is a submenu
        //if(parent instanceof JMenu) {
            /*
        } else {
            // x/y are in abs screen coords. must convert to local
            Point pt = new Point(x,y);
            p("original point = " + pt);
            SwingUtilities.convertPointFromScreen(pt,canvas);
            p("converted poitn = " + pt);
            cont.setLocation(pt);
        }*/
        
        
        //p("size = " + cont.getSize());
        //p("min = " + cont.getLayout().preferredLayoutSize(cont));
        cont.setSize(cont.getLayout().preferredLayoutSize(cont));
        //p("size = " + cont.getSize());
        canvas.validate();
        canvas.setVisible(true);
        final JPanel fcont = cont;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setLocationFromMenu(menu, fcont);
            }
        });
        
        canvas.validate();
        canvas.repaint();
        VisualDesignerJPanelPopup popup = new VisualDesignerJPanelPopup(cont, menu, this);
        popupMap.put(menu,popup);
        return popup;
    }
    
    VisualDesignerJPanelPopup getPopup(JMenu menu) {
        return popupMap.get(menu);
    }
    
    private void setLocationFromMenu(final JMenu menu, final JPanel cont) {
        Point pt = SwingUtilities.convertPoint(menu, new Point(0,0), canvas);
        
        JComponent parent = canvas.getMenuParent(menu);
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
    
    private static void p(String string) {
        if(DEBUG) {
            System.out.println(string);
        }
    }
    
    void hideOtherMenus(JMenu menu) {
        for(JMenu m : containerMap.keySet()) {
            if(m != menu) {
                // hide if not an ancestor of this menu
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
    
    private static class VisualDesignerJPanelContainer extends JPanel {
        private JMenu menu;
        private VisualDesignerPopupFactory fact;
        VisualDesignerJPanelContainer(JMenu menu, VisualDesignerPopupFactory fact) {
            this.menu = menu;
            this.fact = fact;
        }
        public void setVisible(boolean visible) {
            // if making visible
            if(visible) {
                // make sure the other menus are hidden
                fact.hideOtherMenus(menu);
                // make sure this menu popup is at the right place
                fact.setLocationFromMenu(menu,this);
                // repack?
            }
            super.setVisible(visible);
        }
    }
    
}