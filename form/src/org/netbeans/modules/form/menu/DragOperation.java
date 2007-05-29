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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Point;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.modules.form.HandleLayer;
import org.netbeans.modules.form.MetaComponentCreator;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;

/**
 * DragOperation handles all drag operations whether they are drag and drop or pick and plop. It
 * also deals with new components from the palette and rearranging existing menu items within the menu.
 * It does *not* handle the actual adding and removing of components. Instead that is delegated back
 * to the MenuEditLayer.
 * @author joshua.marinacci@sun.com
 */
class DragOperation {
    private static final boolean DEBUG = false;
    private MenuEditLayer menuEditLayer;
    private JMenuItem dragComponent;
    private boolean started = false;
    private JComponent targetComponent;
    private enum Op { PICK_AND_PLOP_FROM_PALETTE, INTER_MENU_DRAG, NO_MENUBAR };
    private Op op = Op.PICK_AND_PLOP_FROM_PALETTE;
    private JMenuItem payloadComponent;
    
    public DragOperation(MenuEditLayer menuEditLayer) {
        this.menuEditLayer = menuEditLayer;
        this.started = false;
    }
    
    // start a drag from one menu item to another
    void start(JMenuItem item, Point pt) {
        op = Op.INTER_MENU_DRAG;
        p("starting an inner menu drag for: " + item + " at " + pt);
        started = true;
        dragComponent = new JMenuItem();
        dragComponent.setText(item.getText());
        dragComponent.setIcon(item.getIcon());
        dragComponent.setAccelerator(item.getAccelerator());
        dragComponent.setBorder(MenuEditLayer.DRAG_MENU_BORDER);
        dragComponent.setMargin(new Insets(1,1,1,1));
        dragComponent.setBorderPainted(true);
        dragComponent.setSize(dragComponent.getPreferredSize());
        dragComponent.setLocation(pt);
        menuEditLayer.layers.add(dragComponent, JLayeredPane.DRAG_LAYER);
        menuEditLayer.repaint();
        payloadComponent = item;
    }
    
    // start a pick and plop from the palette operation
    void start(PaletteItem item, Point pt) {
        if(!menuEditLayer.doesFormContainMenuBar()) {
            op = Op.NO_MENUBAR;
            //josh: use the invalid drop target cursor instead
            menuEditLayer.glassLayer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            menuEditLayer.showMenubarWarning = true;
            menuEditLayer.repaint();
            return;
        }
        
        op = Op.PICK_AND_PLOP_FROM_PALETTE;
        p("starting drag op for : " + item.getComponentClassName() + " at " + pt);
        started = true;
        dragComponent = new JMenuItem();
        dragComponent.setText("a new menu item");
        dragComponent.setBorder(MenuEditLayer.DRAG_MENU_BORDER);
        dragComponent.setMargin(new Insets(1,1,1,1));
        dragComponent.setBorderPainted(true);
        dragComponent.setSize(dragComponent.getPreferredSize());
        dragComponent.setLocation(pt);
        menuEditLayer.layers.add(dragComponent, JLayeredPane.DRAG_LAYER);
        menuEditLayer.repaint();
    }
    
    void move(Point pt) {
        if(dragComponent != null) {
            // move the drag component
            dragComponent.setLocation(pt);
            
            // open any relevant top-level menus
            RADComponent rad = menuEditLayer.formDesigner.getHandleLayer().getMetaComponentAt(pt, HandleLayer.COMP_DEEPEST);
            if(rad != null && JMenu.class.isAssignableFrom(rad.getBeanClass())) {
                //p("over a menu");
                targetComponent = (JComponent) menuEditLayer.formDesigner.getComponent(rad);
                menuEditLayer.openMenu(rad, targetComponent);
                return;
            }
            
            //show any drop target markers
            Component child = getDeepestComponentInPopups(pt);
            //p("child = " + child);
            if(child == null) {
                if(targetComponent != null) {
                    targetComponent.setBorder(MenuEditLayer.UNSELECTED_BORDER);
                }
            }
            
            
            if(child instanceof JMenuItem && /*child != payloadComponent &&*/ child != dragComponent) {
                if(targetComponent != null) {
                    targetComponent.setBorder(MenuEditLayer.UNSELECTED_BORDER);
                }
                targetComponent = (JComponent)child;
                if(targetComponent != null) {
                    targetComponent.setBorder(MenuEditLayer.INSERTION_BORDER);
                }
                menuEditLayer.repaint();
            }
            
            if(child instanceof JMenu) {
                Point pt2 = SwingUtilities.convertPoint(menuEditLayer.glassLayer, pt, child);
                styleMenu((JMenu)child,pt2);
            }
            
        } else {
            p("DragOperation: dragComponent shouldn't be null when moving");
        }
    }
    
    private void styleMenu(JMenu menu, Point point) {
        menu.setBorderPainted(true);
        // if on the right side: 
        p("point = " + point + "  widt = " + menu.getWidth());
        if(point.x > menu.getWidth()-30) {
            p("doing menu right");
            menu.setBorder(MenuEditLayer.INSERTION_BORDER_MENU_RIGHT);
            menu.repaint();
        } else {
            menu.setBorder(MenuEditLayer.INSERTION_BORDER);
        }
        menuEditLayer.showMenuPopup(menu);
    }
    
    void end(Point pt) {
        started = false;
        if(dragComponent == null) return;
        p("ending an operation at: " + pt);
        menuEditLayer.layers.remove(dragComponent);
        
        switch (op) {
        case PICK_AND_PLOP_FROM_PALETTE: completePickAndPlopFromPalette(pt); break;
        case INTER_MENU_DRAG: completeInterMenuDrag(pt); break ;
        }
        
    }
    
    void fastEnd() {
        started = false;
        if(dragComponent != null) {
            menuEditLayer.layers.remove(dragComponent);
        }
        if(targetComponent != null) {
            targetComponent.setBorder(MenuEditLayer.UNSELECTED_BORDER);
        }
    }
    
    JComponent getDeepestComponent(Point pt) {
        RADComponent rad = menuEditLayer.formDesigner.getHandleLayer().getMetaComponentAt(pt, HandleLayer.COMP_DEEPEST);
        if(rad != null && JMenu.class.isAssignableFrom(rad.getBeanClass())) {
           return (JComponent) menuEditLayer.formDesigner.getComponent(rad);
        } else {
            return (JComponent) getDeepestComponentInPopups(pt);
        }
    }
    
    private void completeInterMenuDrag(Point pt) {
        if(targetComponent == null) return;
        targetComponent.setBorder(MenuEditLayer.UNSELECTED_BORDER);
        
        //check if it's still a valid target
        JComponent tcomp = (JComponent) getDeepestComponent(pt);
        p("target = " + targetComponent);
        p("tcomp = " + tcomp);
        if(targetComponent != tcomp) {
            p("no longer over a valid target. bailing");
            menuEditLayer.formDesigner.toggleSelectionMode();
            return;
        }
        
        
        menuEditLayer.moveRadComponentToBefore(payloadComponent, targetComponent);
        payloadComponent = null;
        targetComponent = null;
        
    }
    
    private void completePickAndPlopFromPalette(Point pt) {
        PaletteItem paletteItem = PaletteUtils.getSelectedItem();
        
        if(targetComponent == null) return;
        
        targetComponent.setBorder(MenuEditLayer.UNSELECTED_BORDER);
        
        //check if it's still a valid target
        JComponent tcomp = (JComponent) getDeepestComponent(pt);
        p("target = " + targetComponent);
        p("tcomp = " + tcomp);
        if(targetComponent != tcomp) {
            p("no longer over a valid target. bailing");
            menuEditLayer.formDesigner.toggleSelectionMode();
            return;
        }
        
        // get the pre-created component
        MetaComponentCreator creator = menuEditLayer.formDesigner.getFormModel().getComponentCreator();
        RADVisualComponent precreated = creator.precreateVisualComponent(
                paletteItem.getComponentClassSource());
        
        JComponent newComponent = (JComponent) precreated.getBeanInstance();
        // if pre-creation failed then make new component manually
        if(newComponent == null) {
            try {
                newComponent = (JComponent)paletteItem.getComponentClass().newInstance();
            } catch (Exception ex) {
                p("couldn't create new component!");
                ex.printStackTrace();
                return;
            }
        }
        
        // add new component reference to the form
        LayoutComponent layoutComponent = creator.getPrecreatedLayoutComponent();
        Object constraints = null;
        
        
        // dragged to a menu, add inside the menu instead of next to it
        if(targetComponent instanceof JMenu) {
            p("============== doing a new comp to a jmenu");
            RADVisualContainer targetContainer = (RADVisualContainer) menuEditLayer.formDesigner.getMetaComponent(targetComponent);
            p("target container = " + targetContainer);
            boolean added = creator.addPrecreatedComponent(targetContainer, constraints);
        } else {
            /*
            // add the new component to the target's containing menu
            JComponent menuParent = menuEditLayer.getMenuParent(targetComponent);
            RADVisualContainer targetContainer = (RADVisualContainer) menuEditLayer.formDesigner.getMetaComponent(menuParent);
            boolean added = creator.addPrecreatedComponent(targetContainer, constraints);
            */
            p("doing the new kind of add");
            RADVisualComponent newRad = creator.getPrecreatedMetaComponent();
            p("new rad = " + newRad);
            menuEditLayer.addRadComponentToBefore(newRad, targetComponent);
        }
        
        menuEditLayer.formDesigner.toggleSelectionMode();
        
    }
    
    //josh: this is a very slow way to find the component under the mouse cursor.
    //there must be a faster way to do it
    public JComponent getDeepestComponentInPopups(Point pt) {
        Component[] popups = menuEditLayer.layers.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER);
        for(Component popup : popups) {
            //p("looking at popup: " + popup);
            Point pt2 = SwingUtilities.convertPoint(menuEditLayer, pt, popup);
            JComponent child = (JComponent) javax.swing.SwingUtilities.getDeepestComponentAt(popup, pt2.x, pt2.y);
            if(child != null) return child;
        }
        return null;
    }
    
    
    public boolean isStarted() {
        return started;
    }
    
    private static void p(String s) {
        if(DEBUG) {
            System.out.println(s);
        }
    }
}
