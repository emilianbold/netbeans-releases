/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.form.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormModel;
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
    private JComponent dragComponent;
    private boolean started = false;
    private JComponent targetComponent;
    private enum Op { PICK_AND_PLOP_FROM_PALETTE, INTER_MENU_DRAG, NO_MENUBAR };
    private Op op = Op.PICK_AND_PLOP_FROM_PALETTE;
    private JMenuItem payloadComponent;
    private List<JMenuItem> payloadComponents;
    private PaletteItem currentItem;
    
    public boolean isPickAndPlop() {
        return op == Op.PICK_AND_PLOP_FROM_PALETTE;
    }
    
    public DragOperation(MenuEditLayer menuEditLayer) {
        this.menuEditLayer = menuEditLayer;
        this.started = false;
    }
    
    public JComponent getDragComponent() {
        return dragComponent;
    }
    
    // start a drag from one menu item to another
    void start(JMenuItem item, Point pt) {
        op = Op.INTER_MENU_DRAG;
        p("starting an inner menu drag for: " + item + " at " + pt);
        started = true;
        
        
        //josh: intial support for dragging multiple items
        //in the future we should use the payloadComponents variable
        //for dragging components instead of the payloadComponent variable.
        List<RADComponent> rads = menuEditLayer.getSelectedRADComponents();
        
        payloadComponents = new ArrayList<JMenuItem>();
        if(rads.size() > 1) {
            for(RADComponent rad : rads) {
                Object comp = menuEditLayer.formDesigner.getComponent(rad);
                if(comp instanceof JMenuItem) {
                    payloadComponents.add((JMenuItem)comp);
                } else {
                    fastEnd();
                    return;
                }
            }
        } else {
            payloadComponents.add(item);
        }
        
        dragComponent = (JMenuItem) createDragFeedbackComponent(item, null);
        dragComponent.setSize(dragComponent.getPreferredSize());
        dragComponent.setLocation(pt);
        menuEditLayer.layers.add(dragComponent, JLayeredPane.DRAG_LAYER);
        menuEditLayer.repaint();
        payloadComponent = item;
    }

    void setTargetVisible(boolean vis) {
        if(!vis) {
            menuEditLayer.layers.remove(dragComponent);
        } else {
            menuEditLayer.layers.add(dragComponent, JLayeredPane.DRAG_LAYER);
        }
        menuEditLayer.repaint();
    }
    
    private JComponent createDragFeedbackComponent(JMenuItem item, Class type) {
        // get the pre-created component for use as drag feedback
        PaletteItem paletteItem = PaletteUtils.getSelectedItem();
        if(paletteItem != null) {
            MetaComponentCreator creator = menuEditLayer.formDesigner.getFormModel().getComponentCreator();
            RADVisualComponent precreated = creator.precreateVisualComponent(
                    paletteItem.getComponentClassSource());
            if(precreated != null) {
                p("precreated: " + precreated.getBeanClass());
                Object comp = precreated.getBeanInstance();
                if(comp instanceof JComponent) {
                    JComponent jcomp = (JComponent) comp;
                    p("it's a jcomponent");
                    if(comp instanceof JMenuItem) {
                        JMenuItem mcomp = (JMenuItem) comp;
                        mcomp.setBorder(MenuEditLayer.DRAG_MENU_BORDER);
                        mcomp.setIcon(new MenuEditLayer.WrapperIcon());
                        mcomp.setMargin(new Insets(1,1,1,1));
                        mcomp.setBorderPainted(true);
                    }
                    if(comp instanceof JSeparator) {
                        p("it's a separator");
                        jcomp.setBorder(BorderFactory.createLineBorder(new Color(0xFFA400), 1));//, thickness)MenuEditLayer.DRAG_SEPARATOR_BORDER);
                        jcomp.setPreferredSize(new Dimension(80,5));
                        p("border = " + jcomp.getBorder());
                    }
                    return jcomp;
                }
            }
        }
        
        JComponent dragComponent = null;
        dragComponent = new JMenuItem();
        
        if(item == null && type != null && JComponent.class.isAssignableFrom(type)) {
            try {
                dragComponent = (JComponent)type.newInstance();
                p("created a drag component here: " + dragComponent);
            } catch (Exception ex) {
                System.out.println("exception: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        if(item instanceof JMenu) { 
            dragComponent = new JMenu(); 
        }
        if(item instanceof JCheckBoxMenuItem) { 
            dragComponent = new JCheckBoxMenuItem(); 
            ((JCheckBoxMenuItem)dragComponent).setSelected(true);
        }
        if(item instanceof JRadioButtonMenuItem) { 
            dragComponent = new JRadioButtonMenuItem(); 
            ((JRadioButtonMenuItem)dragComponent).setSelected(true);
        }
        if(dragComponent instanceof JMenuItem) {
            JMenuItem dragItem = (JMenuItem) dragComponent;
            if(item != null) {
                dragItem.setText(item.getText());
                dragItem.setIcon(item.getIcon());
                if(! (item instanceof JMenu)) {
                    if(!DropTargetLayer.isMetal()) {
                        dragItem.setAccelerator(item.getAccelerator());
                    }
                }
            } else {
                dragItem.setText("a new menu item");
            }
            dragItem.setMargin(new Insets(1,1,1,1));
            dragItem.setBorderPainted(true);
        }
        dragComponent.setBorder(MenuEditLayer.DRAG_MENU_BORDER);
        return dragComponent;

    }
    
    // start a pick and plop from the palette operation
    void start(PaletteItem item, Point pt) {
        // clean up prev is necessary
        if(dragComponent != null) {
            menuEditLayer.layers.remove(dragComponent);
            dragComponent = null;
        }
        
        if(!menuEditLayer.doesFormContainMenuBar()) {
            op = Op.NO_MENUBAR;
            //josh: use the invalid drop target cursor instead
            //menuEditLayer.glassLayer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            menuEditLayer.showMenubarWarning = true;
            FormEditor.getAssistantModel(menuEditLayer.formDesigner.getFormModel()).setContext("missingMenubar"); // NOI18N
            menuEditLayer.repaint();
            return;
        }
        
        op = Op.PICK_AND_PLOP_FROM_PALETTE;
        p("starting drag op for : " + item.getComponentClassName() + " at " + pt);
        started = true;
        dragComponent = createDragFeedbackComponent(null, item.getComponentClass());
        dragComponent.setSize(dragComponent.getPreferredSize());
        p("created drag component = " + dragComponent);
        dragComponent.setLocation(pt);
        menuEditLayer.layers.add(dragComponent, JLayeredPane.DRAG_LAYER);
        menuEditLayer.repaint();
        currentItem = item;
        menuEditLayer.glassLayer.requestFocusInWindow();
    }
    
    void move(Point pt) {
        if(dragComponent != null) {
            // move the drag component
            dragComponent.setLocation(pt);
            
            
            // look at the rad component under the cursor before checking the popups
            RADComponent rad = menuEditLayer.formDesigner.getHandleLayer().getMetaComponentAt(pt, HandleLayer.COMP_DEEPEST);
            
            // if dragging a JMenu over an open spot in the menu bar
            if(rad != null && JMenuBar.class.isAssignableFrom(rad.getBeanClass()) && JMenu.class.isAssignableFrom(dragComponent.getClass())) {
                //p("over the menu bar");
                menuEditLayer.dropTargetLayer.setDropTarget(rad, pt);
                targetComponent = (JComponent) menuEditLayer.formDesigner.getComponent(rad);
            }
            
            // open any relevant top-level menus
            if(rad != null && JMenu.class.isAssignableFrom(rad.getBeanClass())) {
                //p("over a menu: " + rad);
                targetComponent = (JComponent) menuEditLayer.formDesigner.getComponent(rad);
                menuEditLayer.openMenu(rad, targetComponent);
                if(JMenu.class.isAssignableFrom(dragComponent.getClass())) {
                    menuEditLayer.dropTargetLayer.setDropTarget(rad, pt);
                } else {
                    menuEditLayer.dropTargetLayer.setDropTarget(rad, pt, DropTargetLayer.DropTargetType.INTO_SUBMENU);
                }
                return;
            }
            
            //show any drop target markers
            Component child = getDeepestComponentInPopups(pt);
            
            
            if(child instanceof JMenuItem && child != dragComponent) {
                targetComponent = (JComponent)child;
                if(targetComponent != null) {
                    menuEditLayer.dropTargetLayer.setDropTarget(targetComponent, pt, DropTargetLayer.DropTargetType.INTER_MENU);
                }
                menuEditLayer.repaint();
            }
            
            if(child instanceof JMenu) {
                Point pt2 = SwingUtilities.convertPoint(menuEditLayer.glassLayer, pt, child);
                JMenu menu = (JMenu) child;
                Point point = pt2;
                menu.setBorderPainted(true);
                if(DropTargetLayer.isSubMenuRightEdge(point, menu)) {
                    menuEditLayer.dropTargetLayer.setDropTarget(menu, point, DropTargetLayer.DropTargetType.INTO_SUBMENU);
                    menu.repaint();
                } else {
                    menuEditLayer.dropTargetLayer.setDropTarget(menu, pt, DropTargetLayer.DropTargetType.INTER_MENU);
                }
                menuEditLayer.showMenuPopup(menu);
            }
            
        } else {
            p("DragOperation: dragComponent shouldn't be null when moving");
        }
    }
    
    
    
    void end(Point pt) {
        started = false;
        currentItem = null;
        if(dragComponent == null) return;
        p("ending an operation at: " + pt);
        menuEditLayer.layers.remove(dragComponent);
        menuEditLayer.dropTargetLayer.clearDropTarget();
        
        p("op = " + op);
        switch (op) {
        case PICK_AND_PLOP_FROM_PALETTE: completePickAndPlopFromPalette(pt); break;
        case INTER_MENU_DRAG: completeInterMenuDrag(pt); break ;
        }
        
        menuEditLayer.glassLayer.requestFocusInWindow();
        payloadComponent = null;
        targetComponent = null;
        menuEditLayer.repaint();
        
    }
    
    void fastEnd() {
        started = false;
        if(dragComponent != null) {
            menuEditLayer.layers.remove(dragComponent);
            menuEditLayer.repaint();
        }
        menuEditLayer.dropTargetLayer.clearDropTarget();
    }
    
    // only looks at JMenu and JMenubar RADComponents as well as anything in the popups
    JComponent getDeepestComponent(Point pt) {
        RADComponent rad = menuEditLayer.formDesigner.getHandleLayer().getMetaComponentAt(pt, HandleLayer.COMP_DEEPEST);
        if(rad != null && (JMenu.class.isAssignableFrom(rad.getBeanClass()) ||
                JMenuBar.class.isAssignableFrom(rad.getBeanClass()))) {
           return (JComponent) menuEditLayer.formDesigner.getComponent(rad);
        } else {
            return (JComponent) getDeepestComponentInPopups(pt);
        }
    }
    
    public JComponent getTargetComponent() {
        return targetComponent;
    }
    
    private void completeInterMenuDrag(Point pt) {
        p("================\n\n\n\n==========\n\n========");
        p("complete inter menu drag: target comp = " + targetComponent);
        if(targetComponent == null) return;
        
        //check if it's still a valid target
        JComponent tcomp = (JComponent) getDeepestComponent(pt);
        p("target = " + targetComponent);
        p("tcomp = " + tcomp);
        if(targetComponent != tcomp) {
            p("no longer over a valid target. bailing");
            menuEditLayer.formDesigner.toggleSelectionMode();
            return;
        }
        
        // conver to target component's coords.
        Point pt2 = SwingUtilities.convertPoint(menuEditLayer.glassLayer, pt, tcomp);
        if(tcomp instanceof JMenu) {
            JMenu menu = (JMenu) tcomp;
            
            
            // if dragging a jmenu onto a toplevel jmenu
            if(menu.getParent() instanceof JMenuBar && isOnlyJMenus(payloadComponents)) { //payloadComponent instanceof JMenu) {
                p("dropping into a toplevel menu");
                if(DropTargetLayer.isMenuLeftEdge(pt2, menu)) {
                    p("doing a left drop");
                    menuEditLayer.moveRadComponentToBefore(payloadComponent, menu);
                    return;
                } else if (DropTargetLayer.isMenuRightEdge(pt2, menu)) {
                    p("doing a right drop");
                    menuEditLayer.moveRadComponentToAfter(payloadComponent, menu);
                    return;
                } else {  // else must be in the center so just add to the menu instead of next to
                    p("doing a center drop");
                    menuEditLayer.moveRadComponentInto(payloadComponent, menu);
                    return;
                }
            }
            p("on a jmenu. could be in or above");
            p("converted point = " + pt2);
            if(DropTargetLayer.isSubMenuRightEdge(pt2, menu)) {
                p("doing 'in' menu drop");
                menuEditLayer.moveRadComponentInto(payloadComponent, menu);
            } else {
                p("doing above/below menu drop");
                if(DropTargetLayer.isBelowItem(pt2,menu)) {
                    menuEditLayer.moveRadComponentToAfter(payloadComponent, targetComponent);
                } else {
                    menuEditLayer.moveRadComponentToBefore(payloadComponent, targetComponent);
                }
            }
            return;
        }
        
        if(tcomp instanceof JMenuBar) {
            p("dragging to the menubar itself");
            if(payloadComponent instanceof JMenu) {
                p("adding menu to the end of the menubar");
                menuEditLayer.moveRadComponentInto(payloadComponent, targetComponent);
                return;
            } else {
                p("can't drag a non-JMenu to the menubar");
                return;
            }
        }
        
        //if after or before the current item
        if(DropTargetLayer.isBelowItem(pt2,tcomp)) {
            menuEditLayer.moveRadComponentToAfter(payloadComponent, targetComponent);
        } else {
            menuEditLayer.moveRadComponentToBefore(payloadComponent, targetComponent);
        }
    }
    
    private void completePickAndPlopFromPalette(Point pt) {
        p("complete pick and plop from palette: target comp = " + targetComponent);
        PaletteItem paletteItem = PaletteUtils.getSelectedItem();
        
        if(targetComponent == null) return;
        
        
        //check if it's still a valid target
        JComponent tcomp = (JComponent) getDeepestComponent(pt);
        p("target = " + targetComponent);
        p("tcomp = " + tcomp);
        if(targetComponent != tcomp) {
            p("no longer over a valid target. bailing");
            menuEditLayer.formDesigner.toggleSelectionMode();
            return;
        }
        
        JComponent newComponent = null;
        // get the pre-created component
        MetaComponentCreator creator = menuEditLayer.formDesigner.getFormModel().getComponentCreator();
        if(creator != null) {
            RADVisualComponent precreated = creator.precreateVisualComponent(
                    paletteItem.getComponentClassSource());
            if(precreated != null) {
                newComponent = (JComponent) precreated.getBeanInstance();
            }
        }
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
        // i can probably remove both of these variables
        LayoutComponent layoutComponent = creator.getPrecreatedLayoutComponent();
        Object constraints = null;
        
        
        Point pt2 = SwingUtilities.convertPoint(menuEditLayer.glassLayer, pt, targetComponent);
        // dragged to a menu, add inside the menu instead of next to it
        if(targetComponent instanceof JMenu) {
            p("============== doing a new comp to a jmenu");
            
            //toplevel menus
            if(targetComponent.getParent() instanceof JMenuBar) {
                p("on top of a toplevel menu");
                
                if(DropTargetLayer.isMenuLeftEdge(pt2, targetComponent) && isMenuPayload(creator)) {
                    p("on the left edge");
                    RADVisualComponent newRad = creator.getPrecreatedMetaComponent();
                    menuEditLayer.addRadComponentToBefore(newRad, targetComponent);
                } else if(DropTargetLayer.isMenuRightEdge(pt2, targetComponent) && isMenuPayload(creator)) {
                    p("on the right edge");
                    RADVisualComponent newRad = creator.getPrecreatedMetaComponent();
                    menuEditLayer.addRadComponentToAfter(newRad, targetComponent);
                } else {
                    p("else in the center");
                    menuEditLayer.addRadComponentToEnd(targetComponent, creator);
                }
            } else {
                if(DropTargetLayer.isSubMenuRightEdge(pt2, targetComponent)) {
                    p("doing in menu drop");
                    menuEditLayer.addRadComponentToEnd(targetComponent, creator);
                } else {
                    p("doing above/below menu drop");
                    RADVisualComponent newRad = creator.getPrecreatedMetaComponent();
                    p("new rad = " + newRad);
                    if(DropTargetLayer.isBelowItem(pt2, targetComponent)) {
                        menuEditLayer.addRadComponentToAfter(newRad, targetComponent);
                    } else {
                        menuEditLayer.addRadComponentToBefore(newRad, targetComponent);
                    }
                }
            }
        } else {
            if(targetComponent instanceof JMenuBar) {
                p("======= doing a new comp directly to the jmenubar");
                menuEditLayer.addRadComponentToEnd(targetComponent, creator);
            } else {
                // add the new component to the target's containing menu
                p("doing the new kind of add");
                RADVisualComponent newRad = creator.getPrecreatedMetaComponent();
                p("new rad = " + newRad);
                //menuEditLayer.addRadComponentToBefore(newRad, targetComponent);
                if(DropTargetLayer.isBelowItem(pt2, targetComponent)) {
                    menuEditLayer.addRadComponentToAfter(newRad, targetComponent);
                } else {
                    menuEditLayer.addRadComponentToBefore(newRad, targetComponent);
                }
            }
        }
        
        menuEditLayer.formDesigner.toggleSelectionMode();
        
    }
    
    private boolean isOnlyJMenus(List<JMenuItem> items) {
        for(JMenuItem item : items) {
            if(item instanceof JMenu) continue;
            return false;
        }
        return true;
    }
    
    private boolean isMenuPayload(MetaComponentCreator creator) {
        if(JMenu.class.isAssignableFrom(creator.getPrecreatedMetaComponent().getBeanClass())) {
            return true;
        }
        return false;
    }
    
    //josh: this is a very slow way to find the component under the mouse cursor.
    //there must be a faster way to do it
    public JComponent getDeepestComponentInPopups(Point pt) {
        Component[] popups = menuEditLayer.layers.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER);
        for(Component popup : popups) {
            //p("looking at popup: " + popup);
            if(popup.isVisible()) {
                Point pt2 = SwingUtilities.convertPoint(menuEditLayer, pt, popup);
                JComponent child = (JComponent) javax.swing.SwingUtilities.getDeepestComponentAt(popup, pt2.x, pt2.y);
                if(child != null) return child;
            }
        }
        return null;
    }
    
    
    public boolean isStarted() {
        return started;
    }
    
    public PaletteItem getCurrentItem() {
        return currentItem;
    }
    
    private static void p(String s) {
        if(DEBUG) {
            System.out.println(s);
        }
    }
}
