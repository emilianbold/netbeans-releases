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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.PopupMenuUI;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;

/**
 *
 * @author joshua.marinacci@sun.com
 */
public class MenuEditLayer extends JPanel {
    
    /* === public constants === */
    public static final Border INSERTION_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2,0,0,0,Color.RED),
            BorderFactory.createEmptyBorder(0,2,2,2)
            );
    
    public static final Border INSERTION_BORDER_MENU_RIGHT = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,0,2,Color.RED),
            BorderFactory.createEmptyBorder(2,2,2,0)
            );
    public static final Border UNSELECTED_BORDER = BorderFactory.createEmptyBorder(2,2,2,2);
    public static final Border SELECTED_BORDER = BorderFactory.createLineBorder(Color.ORANGE,2);
    public static final Border DRAG_MENU_BORDER = BorderFactory.createLineBorder(Color.BLACK,1);
    
    /* === private constants === */
    private static final boolean DEBUG = false;
    
    /* === public and package level fields. these should probably become getters and setters  ===*/
    public VisualDesignerPopupFactory hackedPopupFactory = new VisualDesignerPopupFactory(this);
    public FormDesigner formDesigner;
    JLayeredPane layers;
    JComponent glassLayer;
    boolean showMenubarWarning = false;
    
    /* === private fields === */
    private Map<JComponent,MouseInputAdapter> menuitemListenerMap;
    private Map<JMenu, PopupMenuUI> menuPopupUIMap;
    private Map<JComponent,JComponent> menuParentMap;
    private JComponent selectedComponent;
    private JMenu currentMenu;
    private KeyboardMenuNavigator keyboardMenuNavigator;
    private Map<RADVisualContainer,FormModelListener> formModelListeners;
    private DragOperation dragop;
    private FormModelListener menuBarFormListener;
    
    
    /** Creates a new instance of MenuEditLayer */
    public MenuEditLayer(final FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
        menuitemListenerMap = new HashMap<JComponent,MouseInputAdapter>();
        menuPopupUIMap = new HashMap<JMenu, PopupMenuUI>();
        menuParentMap = new HashMap<JComponent, JComponent>();
        formModelListeners = new HashMap<RADVisualContainer,FormModelListener>();
        
        
        setLayout(null);
        layers = new JLayeredPane();
        this.setLayout(new BorderLayout());
        this.add(layers,"Center");
        
        dragop = new DragOperation(this);
        
        glassLayer = new JComponent() {
            public void paintComponent(Graphics g) {
                if(DEBUG) {
                    g.setColor(Color.GREEN);
                    g.drawString("Glass Layer", 30,30);
                    g.drawRect(0,0,getWidth()-1,getHeight()-1);
                }
            }
        };
        
        layers.add(glassLayer, new Integer(500)); // put the glass layer over the drag layer
        glassLayer.setSize(400,400);
        
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                glassLayer.setSize(MenuEditLayer.this.getSize());
            }
        });
        
        MouseInputAdapter mia = new GlassLayerMouseListener();
        glassLayer.addMouseListener(mia);
        glassLayer.addMouseMotionListener(mia);
    }
    
    
    public static boolean isMenuRelatedRADComponent(RADComponent comp) {
        if(comp == null) return false;
        return isMenuRelatedComponentClass(comp.getBeanClass());
    }
    
    public static boolean isMenuRelatedComponentClass(Class clas) {
        if(clas == null) return false;
        if(JMenuItem.class.isAssignableFrom(clas)) return true;
        if(JMenu.class.isAssignableFrom(clas)) return true;
        if(JSeparator.class.isAssignableFrom(clas)) return true;
        if(JMenuBar.class.isAssignableFrom(clas)) return true;
        return false;
    }
    
    public boolean isPossibleNewMenuComponent(PaletteItem item) {
        if(item == null) return false;
        
        if(JMenuItem.class.isAssignableFrom(item.getComponentClass())) {
            return true;
        }
        return false;
    }
    
    
    public void startNewMenuComponentPickAndPlop(PaletteItem item, Point pt) {
        p("starting a new menu component pick and plop");
        this.setVisible(true);
        this.requestFocus();
        dragop = new DragOperation(this);
        dragop.start(item, pt);
    }
    
    public void startNewMenuComponentDragAndDrop(PaletteItem item) {
        this.setVisible(true);
        this.requestFocus();
        configureGlassLayer();
        configureFormListeners();
        //dragop = new DragOperation(this);
        //dragop.start(item,)
    }
    
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(DEBUG) {
            g.setColor(Color.RED);
            g.drawString("the MenuEditLayer is visible",5,getHeight()-5);
        }
        g.setColor(Color.BLACK);
        if(showMenubarWarning) {
            g.drawString("TEMP: You cannot add a menu component to a form without a menubar.", 5, getHeight()-30);
        }
    }
    
    
    
    public void openMenu(RADComponent metacomp, Component comp) {
        configureGlassLayer();
        configureFormListeners();
        //reset the layers
        JMenu menu = (JMenu) comp;
        currentMenu = menu;
        registerKeyListeners();
        configureMenu(null,menu);
        showMenuPopup(menu);
        if(metacomp instanceof RADVisualContainer) {
            keyboardMenuNavigator.currentMenuRAD = (RADVisualContainer)metacomp;
            keyboardMenuNavigator.menuBarRAD = keyboardMenuNavigator.currentMenuRAD.getParentContainer();
        }
    }
    
    
    public void hideMenuLayer() {
        // tear down each menu and menu item
        unconfigureFormListeners();
        for(JMenu m : menuPopupUIMap.keySet()) {
            unconfigureMenu(m);
        }
        if(dragop.isStarted()) {
            dragop.fastEnd();
        }
        menuPopupUIMap.clear();
        menuitemListenerMap.clear();
        // close all popup frames
        this.setVisible(false);
        if(keyboardMenuNavigator != null) {
            glassLayer.removeKeyListener(keyboardMenuNavigator);
            keyboardMenuNavigator.unconfigure();
            keyboardMenuNavigator = null;
        }
    }
    
    //josh: all this key listener stuff should go into a separate class
    private void registerKeyListeners() {
        glassLayer.requestFocusInWindow();
        if(keyboardMenuNavigator == null) {
            keyboardMenuNavigator = new KeyboardMenuNavigator(this);
            glassLayer.addKeyListener(keyboardMenuNavigator);
        }
    }
    
    private void configureGlassLayer() {
        try {
            glassLayer.setDropTarget(new DropTarget());
            glassLayer.getDropTarget().addDropTargetListener(new GlassLayerDropTargetListener());
        } catch (TooManyListenersException ex) {
            ex.printStackTrace();
        }
    }
    
    private void configureFormListeners() {
        
        if(menuBarFormListener == null) {
            menuBarFormListener = new FormModelListener() {
                public void formChanged(FormModelEvent[] events) {
                    if(events != null) {
                        for(FormModelEvent evt : events) {
                            // if this is a menubar delete event
                            if(evt.getChangeType() == evt.COMPONENT_REMOVED) {
                                if(evt.getComponent() != null && 
                                        JMenuBar.class.isAssignableFrom(evt.getComponent().getBeanClass())) {
                                    hideMenuLayer();
                                }
                            }
                        }
                    }
                }                
            };
            formDesigner.getFormModel().addFormModelListener(menuBarFormListener);
        }
    }    
    
    private void unconfigureFormListeners() {
        if(menuBarFormListener != null) {
            formDesigner.getFormModel().removeFormModelListener(menuBarFormListener);
        }
        menuBarFormListener = null;
    }    


    void showMenuPopup(final JMenu menu) {
        // if already created then just make it visible
        if(hackedPopupFactory.containerMap.containsKey(menu)) {
            hackedPopupFactory.containerMap.get(menu).setVisible(true);
        } else {
            JPopupMenu popup = menu.getPopupMenu();
            popup.show(menu,0,menu.getHeight());
        }
        this.validate();
    }
    
    public boolean isMenuLayerComponent(RADComponent metacomp) {
        if(metacomp == null) {
            return false;
        }
        if(metacomp.getBeanClass().equals(JMenuItem.class)) {
            return true;
        }
        return false;
    }
    
    
    
    void configureMenu(final JComponent parent, final JMenu menu) {
        // make sure it will draw it's border so we can have rollovers and selection
        menu.setBorderPainted(true);
        
        // configure the maps and popups
        //p("configuring menu: " + menu.getText());
        menuParentMap.put(menu,parent);
        JPopupMenu popup = menu.getPopupMenu();
        menuPopupUIMap.put(menu,popup.getUI());
        popup.setUI(new VisualDesignerPopupMenuUI(this, popup.getUI()));
        
        // get all of the components in this menu
        Component[] subComps = menu.getMenuComponents();
        // if this isn't the first time this menu has been opened then the sub components
        // will have been moved to the popupPanel already, so we will find them there instead.
        JPanel popupPanel = hackedPopupFactory.containerMap.get(menu);
        if(popupPanel != null) {
            subComps = popupPanel.getComponents();
        }
        
        
        RADVisualContainer menuRAD = (RADVisualContainer) formDesigner.getMetaComponent(menu);
        registerForm(menuRAD,menu);
        
        // recurse for sub-menus
        for(Component c : subComps) {
            if(c instanceof JMenu) {
                configureMenu(menu, (JMenu)c);
                RADComponent rad = formDesigner.getMetaComponent(c);
                registerForm((RADVisualContainer)rad,(JMenu)c);
            } else {
                configureMenuItem(menu, (JComponent) c);
            }
        }
    }
    
    private void unconfigureMenu(final JMenu menu) {
        // restore the UI
        menu.getPopupMenu().setUI(menuPopupUIMap.get(menu));
        
        
        // restore all children
        JPanel popup = hackedPopupFactory.containerMap.get(menu);
        if(popup != null) {
            for(Component c : popup.getComponents()) {
                if(c instanceof JMenu) {
                    unconfigureMenu((JMenu)c);
                } else {
                    unconfigureMenuItem((JComponent) c);
                }
            }
            
            //hide the popup(s) if it's still visible
            if(menu.getPopupMenu() != null) {
                menu.getPopupMenu().setVisible(false);
            }
            popup.setVisible(false);
            //layers.remove(popup);
        }
    }
    
    private boolean isConfigured(JComponent c) {
        return menuParentMap.containsKey(c);
    }
    
    
    
    
    void configureMenuItem(final JMenu parent, final JComponent c) {
        menuParentMap.put(c,parent);
    }
    
    void unconfigureMenuItem(JComponent c) {
        menuParentMap.remove(c);
    }
    
    
    
    //override JComponent.isOpaque to always return false
    public boolean isOpaque() {
        return false;
    }
    
    
    // returns true if parent really is an ancestor of target
    boolean isAncestor(JMenu target, JMenu parent) {
        Object possibleParent = menuParentMap.get(target);
        //check if this is the target's parent from the map
        if(parent == possibleParent) {
            return true;
        } else {
            // recursively check up the chain to see if this is a further ancestor
            if(possibleParent instanceof JMenu) {
                return isAncestor((JMenu)possibleParent, parent);
            }
        }
        return false;
    }
    
    JComponent getMenuParent(JComponent menu) {
        return menuParentMap.get(menu);
    }
    
    void setSelectedComponent(JComponent c) {
        glassLayer.requestFocusInWindow();
        try {
            if(selectedComponent != null) {
                selectedComponent.setBorder(UNSELECTED_BORDER);
            }
            
            selectedComponent = c;
            
            if(c == null) {
                return;
            }
            
            RADVisualComponent rad = (RADVisualComponent) formDesigner.getMetaComponent(c);
            keyboardMenuNavigator.selectedRADComponent = rad;
            formDesigner.setSelectedComponent(rad);
            
            selectedComponent.setBorder(SELECTED_BORDER);
            if(selectedComponent instanceof JMenu) {
                JMenu menu = (JMenu) selectedComponent;
                showMenuPopup(menu);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    // returns true if this is a menu container that should be highlighted if the component
    // tcomp is dragged over it.
    public boolean canHighlightContainer(RADVisualContainer targetContainer, RADVisualComponent tcomp) {
        Class beanclass = tcomp.getBeanClass();
        if(targetContainer != null && targetContainer.isMenuComponent() && targetContainer.canAddComponent(beanclass)) {
            return true;
        }
        return false;
    }
    
    // is this rollover code still being used?
    // this turns on and off the rollover highlight as well as auto-opening the menu
    // if it is a menu
    private JComponent prevRollover = null;
    public void rolloverContainer(RADVisualContainer targetContainer) {
        if(targetContainer == null && prevRollover != null) {
            clearRollover();
        }
        if(targetContainer != null) {
            JComponent rollover = (JComponent) formDesigner.getComponent(targetContainer);
            if(rollover != prevRollover){
                clearRollover();
            }
            prevRollover = rollover;
            prevRollover.setBorder(BorderFactory.createLineBorder(Color.ORANGE,2));
            prevRollover.repaint();
            if(rollover instanceof JMenu) {
                formDesigner.openMenu(targetContainer);
                //openMenu(targetContainer,rollover);
            }
        }
    }
    
    public void clearRollover() {
        if(prevRollover==null) return;
        prevRollover.setBorder(BorderFactory.createEmptyBorder());
        prevRollover.repaint();
        prevRollover = null;
    }
    
    
    void addRadComponentToBefore(RADVisualComponent payloadRad, JComponent target) {
        try {
            JComponent targetParent = getMenuParent(target);
            p("target parent = " + targetParent);
            RADVisualComponent targetRad = (RADVisualComponent) formDesigner.getMetaComponent(target);
            p("target rad = " + targetRad);
            RADVisualContainer targetParentRad = (RADVisualContainer) formDesigner.getMetaComponent(targetParent);
            p("target parent rad = " + targetParentRad);
            
            assert targetParentRad != null;
            
            p("=== inserting before drop target component ===");
            int index2 = targetParentRad.getIndexOf(targetRad);
            p("inserting at index: " + index2);
            FormModelEvent fme2 = formDesigner.getFormModel().fireComponentAdded(payloadRad, false);
            formDesigner.getFormModel().addVisualComponent(payloadRad, targetParentRad, new Integer(index2), true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
    void moveRadComponentInto(JComponent payload, JMenu targetMenu) {
        try {
            if(payload == targetMenu) {
                p("can't move onto self");
                return;
            }
            JComponent payloadParent = getMenuParent(payload);
            p("payload parent = " + payloadParent);
            RADVisualComponent payloadRad = (RADVisualComponent) formDesigner.getMetaComponent(payload);
            p("payload rad = " + payloadRad);
            RADVisualContainer payloadParentRad = (RADVisualContainer) formDesigner.getMetaComponent(payloadParent);
            p("payload parent rad = " + payloadParentRad);
            //skip if no payload rad, which probably means this is a new component from the palette
            if(payloadRad != null) {
                int index = payloadParentRad.getIndexOf(payloadRad);
                payloadParentRad.remove(payloadRad);
                FormModelEvent fme = formDesigner.getFormModel().fireComponentRemoved(payloadRad, payloadParentRad, index, false);
            }
            RADVisualContainer targetMenuRad = (RADVisualContainer) formDesigner.getMetaComponent(targetMenu);
            p("target menu rad = " + targetMenuRad);
            //add inside the target menu
            p("=== inserting at end of a menu ===");
            //add to end of the toplevel menu
            targetMenuRad.add(payloadRad, -1);
            targetMenuRad.getLayoutSupport().addComponents(new RADVisualComponent[] { payloadRad }, null, -1);
            FormModelEvent fme2 = formDesigner.getFormModel().fireComponentAdded(payloadRad, false);
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
        }        
    }
    
    void moveRadComponentToBefore(JComponent payload, JComponent target) {
        try {
            if(payload == target) {
                p("can't move onto self");
                return;
            }
            JComponent payloadParent = getMenuParent(payload);
            p("payload parent = " + payloadParent);
            JComponent targetParent = getMenuParent(target);
            p("target parent = " + targetParent);
            RADVisualComponent payloadRad = (RADVisualComponent) formDesigner.getMetaComponent(payload);
            p("payload rad = " + payloadRad);
            RADVisualComponent targetRad = (RADVisualComponent) formDesigner.getMetaComponent(target);
            p("target rad = " + targetRad);
            RADVisualContainer payloadParentRad = (RADVisualContainer) formDesigner.getMetaComponent(payloadParent);
            p("payload parent rad = " + payloadParentRad);
            RADVisualContainer targetParentRad = (RADVisualContainer) formDesigner.getMetaComponent(targetParent);
            p("target parent rad = " + targetParentRad);
            

            p("=== removing ===");
            //skip if no payload rad, which probably means this is a new component from the palette
            if(payloadRad != null && payloadParentRad != null) {
                int index = payloadParentRad.getIndexOf(payloadRad);
                payloadParentRad.remove(payloadRad);
                FormModelEvent fme = formDesigner.getFormModel().fireComponentRemoved(payloadRad, payloadParentRad, index, false);
            }

            //if dragged component into a toplevel menu
            if(targetParent == null && target instanceof JMenu && target.getParent() instanceof JMenuBar) {
                p("=== inserting at end of a toplevel menu ===");
                targetParentRad = (RADVisualContainer) targetRad;
                //add to end of the toplevel menu
                targetParentRad.add(payloadRad, -1);
                targetParentRad.getLayoutSupport().addComponents(new RADVisualComponent[] { payloadRad }, null, -1);
                FormModelEvent fme2 = formDesigner.getFormModel().fireComponentAdded(payloadRad, false);
                return;
            }
            
            // insert if target exists, else the item was removed by dragging out of the menu
            if(targetParentRad != null) {
                p("=== inserting before drop target component ===");
                int index2 = targetParentRad.getIndexOf(targetRad);
                targetParentRad.add(payloadRad, index2);
                targetParentRad.getLayoutSupport().addComponents(new RADVisualComponent[] { payloadRad }, 
                        null, index2);
                FormModelEvent fme2 = formDesigner.getFormModel().fireComponentAdded(payloadRad, false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //listens to see if this particular menu has been changed
    private void registerForm(final RADVisualContainer metacomp, final JMenu menu) {
        // don't double register
        if(!formModelListeners.containsKey(metacomp)) {
            FormModelListener fml = new FormModelListener() {
                public void formChanged(FormModelEvent[] events) {
                    if (events != null) {
                        for(FormModelEvent evt : events) {
                            p("event = " + evt);
                            p("type = " + evt.getChangeType());
                            p("prop changed = " + evt.COMPONENT_PROPERTY_CHANGED);
                            p("comp added = " + evt.COMPONENT_ADDED);
                            p("comp removed = " + evt.COMPONENT_REMOVED);
                            p("comp reordered = " + evt.COMPONENTS_REORDERED);
                            if(evt.getComponent() != null) {
                                p(" " + evt.getComponent().getName());
                            };
                            if(evt.getChangeType() == evt.COMPONENT_PROPERTY_CHANGED) {
                                rebuildOnScreenMenu(metacomp);
                            }
                            // if this menu was deleted then make sure it's popup is hidden and removed
                            if(evt.getChangeType() == evt.COMPONENT_REMOVED) {
                                if(evt.getComponent() == metacomp) {
                                    p("this component was removed");
                                    unconfigureMenu(menu);
                                    continue;
                                }
                            }
                            // if something added to the menu we monitor
                            if(evt.getChangeType() == evt.COMPONENT_ADDED ||
                                    evt.getChangeType() == evt.COMPONENTS_REORDERED ||
                                    evt.getChangeType() == evt.COMPONENT_REMOVED) {
                                if(evt.getContainer() == metacomp) {
                                    // then rebuild the menu*/
                                    rebuildOnScreenMenu(metacomp);
                                }
                            }
                        }
                    }
                }
            };
            formModelListeners.put(metacomp,fml);
            metacomp.getFormModel().addFormModelListener(fml);
        }
    }
    
    private void rebuildOnScreenMenu(RADVisualContainer menuRAD) {
        p("** rebuildOnScreenMenu() called");
        JMenu menu = (JMenu) formDesigner.getComponent(menuRAD);
        if(hackedPopupFactory.containerMap.containsKey(menu)) {
            JPanel popupContainer = hackedPopupFactory.containerMap.get(menu);
            p("looking over components left");
            for(Component c : popupContainer.getComponents()) {
                p("found comp: " + c);
                if(c instanceof JMenu) {
                    unconfigureMenu((JMenu)c);
                } else {
                    unconfigureMenuItem((JComponent)c);
                }
            }
            p("removing all from container");
            popupContainer.removeAll();
            // rebuild it
            for(RADVisualComponent child : menuRAD.getSubComponents()) {
                JComponent jchild = (JComponent) formDesigner.getComponent(child);
                //p("checking: " + jchild);
                if(!isConfigured(jchild)) {
                    //p("not configured!");
                    if(jchild instanceof JMenu) {
                        configureMenu(menu, (JMenu)jchild);
                    } else {
                        configureMenuItem(menu,jchild);
                    }
                }
                popupContainer.add(jchild);
            }
            
            // repack it
            popupContainer.setSize(popupContainer.getLayout().preferredLayoutSize(popupContainer));
            validate();
            popupContainer.repaint();
        } else {
            p("menu popup not built yet");
        }
    }
    
    private static void p(String string) {
        //turn off for now
        if(DEBUG) {
            System.out.println(string);
        }
    }
    
    
    
    //returns true if this array contains a menu component
    public static boolean containsMenuTypeComponent(RADVisualComponent[] comps) {
        if(comps == null) return false;
        if(comps.length < 1) return false;
        for(RADVisualComponent c : comps) {
            if(JMenuItem.class.isAssignableFrom(c.getBeanClass())) return true;
            if(JMenuBar.class.isAssignableFrom(c.getBeanClass())) return true;
            if(JMenu.class.isAssignableFrom(c.getBeanClass())) return true;
        }
        return false;
    }
    
    // returns true if this container is a menubar or menu, else false
    public static boolean isValidMenuContainer(RADVisualContainer cont) {
        if(cont == null) return false;
        if(JMenuBar.class.isAssignableFrom(cont.getBeanClass())) return true;
        if(JMenu.class.isAssignableFrom(cont.getBeanClass())) return true;
        return false;
    }
    
    public boolean doesFormContainMenuBar() {
        p("testing comps");
        for(RADComponent comp : formDesigner.getFormModel().getAllComponents()) {
            p("testing comp: " + comp);
            if(JMenuBar.class.isAssignableFrom(comp.getBeanClass())) {
                return true;
            }
        }
        return false;
    }


    private class GlassLayerMouseListener extends MouseInputAdapter {
        Point pressPoint = null;
        JComponent pressComp = null;
        private boolean isEditing = false;
        
        public void mouseClicked(MouseEvent e) {
            p("mouse clicked: " + e);
        }
        
        public void mousePressed(MouseEvent e) {
            p("mouse pressed: " + e);
            // drag drag ops
            if(dragop.isStarted()) {
                dragop.end(e.getPoint());
                return;
            }

            // open top level menus when clicking them
            RADComponent rad = formDesigner.getHandleLayer().getMetaComponentAt(e.getPoint(), HandleLayer.COMP_DEEPEST);
            if(rad != null) {
                JComponent c = (JComponent) formDesigner.getComponent(rad);
                if(c != null && c instanceof JMenu && c.getParent() instanceof JMenuBar) {
                    p("this is a top menu bar component");
                    openMenu(rad, c);
                    setSelectedComponent(c);
                    return;
                }
            }

            JComponent c = dragop.getDeepestComponentInPopups(e.getPoint());
            p("pressed component: " + c);

            p("pressed rad = " + rad);
            if(c == null && !isMenuRelatedRADComponent(rad)) {
                p("not a menu component. going back to handle layer");
                hideMenuLayer();
            }

            // start editing
            if(e.getClickCount() > 1) {
                p("double click");
                if(c instanceof JMenuItem) {
                    JMenuItem item = (JMenuItem) c;
                    RADComponent radcomp = formDesigner.getMetaComponent(item);
                    isEditing = true;
                    formDesigner.startInPlaceEditing(radcomp);
                }
            }

            //prep for drag motion for menuitem to menuitem drags
            if(!dragop.isStarted() && c instanceof JMenuItem) {
                pressPoint = e.getPoint();
                pressComp = c;
            }
        }
        
        public void mouseReleased(MouseEvent e) {
            //p("mouse released: " + e);
            if(dragop.isStarted()) {
                dragop.end(e.getPoint());
            } else {
                if(!isEditing) {
                    JComponent c = (JComponent) dragop.getDeepestComponentInPopups(e.getPoint());
                    if(c != null) { 
                        setSelectedComponent(c);
                    }
                }
                isEditing = false;
            }
        }
        
        public void mouseEntered(MouseEvent e) {
            p("mouse entered: " + e);
            if(!dragop.isStarted()) {
                p("not started");
                PaletteItem item = PaletteUtils.getSelectedItem();
                p("designer mode = " + formDesigner.getDesignerMode());
                p("add = " + FormDesigner.MODE_ADD);
                p("connect = " + FormDesigner.MODE_CONNECT);
                p("select = " + FormDesigner.MODE_SELECT);
                
                // if not menu related at all, then jump back to handle layer
                if(item != null && !isMenuRelatedComponentClass(item.getComponentClass())) {
                    p("not a menu component. going back to the handle layer");
                    hideMenuLayer();
                    return;
                }
                
                if(formDesigner.getDesignerMode() == FormDesigner.MODE_ADD && item != null) {
                    if(JMenuBar.class.isAssignableFrom(item.getComponentClass())) {
                        p("dragging in a menu bar. go back to handle layer");
                        hideMenuLayer();
                        return;
                    }
                    dragop.start(item,e.getPoint());
                }
                
                if(formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT && showMenubarWarning) {
                    p("still in bad mode");
                    glassLayer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    showMenubarWarning = false;
                    repaint();
                }
            }
        }
        
        public void mouseExited(MouseEvent e) {
            //p("mouse exited: " + e);
        }
        
        public void mouseDragged(MouseEvent e) {
            if(!dragop.isStarted() && pressPoint != null && pressComp instanceof JMenuItem
                    && e.getPoint().distance(pressPoint) > 10) {
                dragop.start((JMenuItem)pressComp, e.getPoint());
                pressPoint = null;
                pressComp = null;
            }
            if(dragop.isStarted()) {
                dragop.move(e.getPoint());
            }
            return;
        }
        
        public void mouseMoved(MouseEvent e) {
            //p("mouse moved: " + e);
            if(dragop.isStarted()) {
                dragop.move(e.getPoint());
            }                
        }
                
    }
    
    
    private class GlassLayerDropTargetListener implements DropTargetListener {
        public void dragEnter(DropTargetDragEvent dtde) {
            p("new drag enter");
            if(!dragop.isStarted()) {
                PaletteItem item = PaletteUtils.getSelectedItem();
                
                
                if(item != null && !isMenuRelatedComponentClass(item.getComponentClass())) {
                    p("not a menu component. going back to the handle layer");
                    hideMenuLayer();
                    return;
                }
                
                if(formDesigner.getDesignerMode() == FormDesigner.MODE_ADD && item != null) {
                    if(JMenuBar.class.isAssignableFrom(item.getComponentClass())) {
                        p("dragging in a menu bar. go back to handle layer");
                        hideMenuLayer();
                        return;
                    }
                    dragop.start(item,dtde.getLocation());
                }
            }
        }

        public void dragOver(DropTargetDragEvent dtde) {
            p("new drag over");
            if(dragop.isStarted()) {
                dragop.move(dtde.getLocation());
            }
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        public void dragExit(DropTargetEvent dte) {
        }

        public void drop(DropTargetDropEvent dtde) {
            p("new drop");
            if(dragop.isStarted()) {
                dragop.end(dtde.getLocation());
                return;
            }
        }
        
    }
    

}
