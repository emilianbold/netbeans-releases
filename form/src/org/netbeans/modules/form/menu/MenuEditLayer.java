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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.PopupMenuUI;
import org.netbeans.modules.form.*;
import org.netbeans.spi.palette.PaletteController;

/**
 *
 * @author joshua.marinacci@sun.com
 */
public class MenuEditLayer extends JPanel {
    private static final boolean DEBUG = false;
    private static final boolean newDnD = true;
    JPopupMenu globalPopup;
    public VisualDesignerPopupFactory hackedPopupFactory = new VisualDesignerPopupFactory(this);
    MenuItemReceiverAndSender menuItemTransferHandler;
    VisualDesignerItemDropTargetListener globalDropListener = new VisualDesignerItemDropTargetListener(this);
    private Map<JComponent,MouseInputAdapter> menuitemListenerMap;
    private Map<JMenu, PopupMenuUI> menuPopupUIMap;
    public FormDesigner formDesigner;
    private Map<JComponent,JComponent> menuParentMap;
    
    private JComponent selectedComponent;
    
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
    private RADVisualContainer currentMenuRAD;
    private JMenu currentMenu;
    private RADVisualComponent selectedRADComponent;
    private RADVisualContainer menuBarRAD;
    private KeyListener keyListener;
    private Map<RADVisualContainer,FormModelListener> formModelListeners;
    public JLayeredPane layers;
    
    
    /** Creates a new instance of MenuEditLayer */
    public MenuEditLayer(FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
        menuitemListenerMap = new HashMap<JComponent,MouseInputAdapter>();
        menuPopupUIMap = new HashMap<JMenu, PopupMenuUI>();
        menuItemTransferHandler = new MenuItemReceiverAndSender(this);
        menuParentMap = new HashMap<JComponent, JComponent>();
        formModelListeners = new HashMap<RADVisualContainer,FormModelListener>();
        
        Action editActionAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                openActionEditor();
            }
        };
        editActionAction.putValue(Action.NAME,"Edit Action");
        globalPopup = new JPopupMenu("Edit Menu");
        globalPopup.add(new JMenuItem(editActionAction));
        globalPopup.add(new JMenuItem("Insert Menu Item"));
        globalPopup.add(new JMenuItem("Insert Radio Button Menu Item"));
        globalPopup.add(new JMenuItem("Insert Checkbox Menu Item"));
        globalPopup.add(new JMenuItem("Insert Separator"));
        globalPopup.add(new JMenuItem("Delete"));
        
        setLayout(null);
        layers = new JLayeredPane();
        this.setLayout(new BorderLayout());
        this.add(layers,"Center");
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(DEBUG) {
            g.setColor(Color.RED);
            g.drawString("the MenuEditLayer is visible",5,getHeight()-5);
        }
    }
    
    
    
    private void openActionEditor() {
        //p("selected components:");
        for(RADComponent comp : (List<RADComponent>)formDesigner.getSelectedComponents()) {
            //np("selected comp = " + comp);
        }
    }
    
    public void closeMenu(RADComponent menuComp, Component comp) {
    }
    
    public void openMenu(RADComponent metacomp, Component comp) {
        //reset the layers
        
        //p("MenuEditLayer.openMenu on: " + metacomp.getName());
        JMenu menu = (JMenu) comp;
        currentMenu = menu;
        registerKeyListeners();
        configureMenu(null,menu);
        showMenuPopup(menu);
        if(metacomp instanceof RADVisualContainer) {
            currentMenuRAD = (RADVisualContainer)metacomp;
            menuBarRAD = currentMenuRAD.getParentContainer();
        }
    }
    
    void showMenuPopup(final JMenu menu) {
        // if already created then just make it visible
        if(hackedPopupFactory.containerMap.containsKey(menu)) {
            hackedPopupFactory.containerMap.get(menu).setVisible(true);
        } else {
            JPopupMenu popup = menu.getPopupMenu();
            popup.show(menu,0,menu.getHeight());
            //popup.setVisible(true);
        }
        //menu.getPopupMenu().setVisible(true);
        //menu.doClick();
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
    
    public void hideMenuLayer() {
        //p("hiding the menu layer");
        // tear down each menu and menu item
        for(JMenu m : menuPopupUIMap.keySet()) {
            unconfigureMenu(m);
        }
        menuPopupUIMap.clear();
        menuitemListenerMap.clear();
        // close all popup frames
        this.setVisible(false);
        removeKeyListener(keyListener);
        keyListener = null;
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
        
        // menus can transfer themselves
        menuItemTransferHandler.registerTransferComponent(menu,menu);
        menu.setTransferHandler(menuItemTransferHandler);
        
        // set up drag and drop stuff
        try {
            menu.getDropTarget().addDropTargetListener(globalDropListener);
        } catch (TooManyListenersException ex) {
            ex.printStackTrace();
        }
        
        configMouseListener(menu);
        
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
        //p("unconfiguring menu: " + menu.getText());
        // restore the UI
        menu.getPopupMenu().setUI(menuPopupUIMap.get(menu));
        
        // un-setup drag and drop stuff
        try {
            menu.getDropTarget().removeDropTargetListener(globalDropListener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // restore all children
        JPanel popup = hackedPopupFactory.containerMap.get(menu);
        //        p("got the popup " + popup);
        if(popup != null) {
            for(Component c : popup.getComponents()) {
                if(c instanceof JMenu) {
                    //unconfigureMenu((JMenu)c);
                } else {
                    unconfigureMenuItem((JComponent) c);
                }
            }
            
            //hide the popup(s) if it's still visible
            if(menu.getPopupMenu() != null) {
                menu.getPopupMenu().setVisible(false);
            }
            popup.setVisible(false);
        }
    }
    
    private boolean isConfigured(JComponent c) {
        return menuParentMap.containsKey(c);
    }
    
    
    
    
    void configureMenuItem(final JMenu parent, final JComponent c) {
        menuParentMap.put(c,parent);
        // menu items tranfer themselves
        menuItemTransferHandler.registerTransferComponent(c,c);
        c.setTransferHandler(menuItemTransferHandler);
        try {
            c.getDropTarget().addDropTargetListener(globalDropListener);
        } catch (TooManyListenersException ex) {
            ex.printStackTrace();
        }
        
        configMouseListener(c);
    }
    
    void unconfigureMenuItem(JComponent c) {
        if(c instanceof JMenuItem) {
            //p("un-configuring menu item: " + ((JMenuItem)c).getText());
        }
        try {
            c.getDropTarget().removeDropTargetListener(globalDropListener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        c.setTransferHandler(null);
        c.removeMouseListener(menuitemListenerMap.get(c));
        c.removeMouseMotionListener(menuitemListenerMap.get(c));
        menuParentMap.remove(c);
    }
    
    
    
    private void configMouseListener(final JComponent c) {
        //remove existing listeners
        
        for(MouseListener l : c.getMouseListeners()) {
            c.removeMouseListener(l);
        }
        for(MouseMotionListener l : c.getMouseMotionListeners()) {
            c.removeMouseMotionListener(l);
        }
        // a listener for the context menu and for starting drags
        MouseInputAdapter mia = new MenuItemMouseHandler(c);
        c.addMouseListener(mia);
        c.addMouseMotionListener(mia);
        menuitemListenerMap.put(c,mia);
    }
    
    
    //override JComponent.isOpaque to always return false
    public boolean isOpaque() {
        return false;
    }
    
    
    // returns true if parent really is an ancestor of target
    boolean isAncestor(JMenu target, JMenu parent) {
        Object possibleParent = menuParentMap.get(target);
        if(parent == possibleParent) {
            return true;
        }
        return false;
    }
    
    JComponent getMenuParent(JComponent menu) {
        return menuParentMap.get(menu);
    }
    
    private void setSelectedComponent(JComponent c) {
        this.requestFocusInWindow(false);
        try {
            if(selectedComponent != null) {
                selectedComponent.setBorder(UNSELECTED_BORDER);
            }
            
            selectedComponent = c;
            
            if(c == null) {
                return;
            }
            
            RADVisualComponent rad = (RADVisualComponent) formDesigner.getMetaComponent(c);
            selectedRADComponent = rad;
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
    
    // a mouse listener to let menu items be selected and init drags
    private class MenuItemMouseHandler extends MouseInputAdapter {
        
        private JComponent c;
        private boolean isEditing = false;
        private Point start;
        private boolean started = false;
        
        public MenuItemMouseHandler(JComponent c) {
            super();
            this.c = c;
        }
        
        public void mousePressed(MouseEvent e) {
            started = false;
            if (!started && e.isPopupTrigger()) {
                //globalPopup.show(c, 40, 0);
            }
            if(e.getClickCount() > 1) {
                if(c instanceof JMenuItem) {
                    JMenuItem item = (JMenuItem) c;
                    RADComponent radcomp = formDesigner.getMetaComponent(item);
                    isEditing = true;
                    formDesigner.startInPlaceEditing(radcomp);
                }
            }
            start = e.getPoint();
        }
        
        public void mouseReleased(MouseEvent e) {
            if(!isEditing) {
                setSelectedComponent(c);
            }
            isEditing = false;
            if(started) {
                finishDragOperation(e);
            }
            started = false;
            start = null;
        }
        
        
        
        public void mouseDragged(MouseEvent e) {
            if (started) {
                updateDragOperation(e);
                return;
            }
            if (e.getPoint().distance(start) > 10) {
                started = true;
                JComponent comp = (JComponent) e.getSource();
                if(newDnD) {
                    startDragOperation(start, comp);
                } else {
                    TransferHandler handler = comp.getTransferHandler();
                    p("starting the drag operation");
                    handler.exportAsDrag(comp, e, TransferHandler.MOVE);
                }
                return;
            }
        }
        
        private void startDragOperation(Point pt, JComponent comp) {
            //p("starting drag at: " + e.getPoint());
            //Point pt = e.getPoint();
            pt = SwingUtilities.convertPoint(comp, pt, MenuEditLayer.this);
            Component child = SwingUtilities.getDeepestComponentAt(layers, pt.x, pt.y);
            p("payload = " + child);
            if(child instanceof JMenuItem) {
                payloadComponent = (JComponent) child;
                JMenuItem payload = (JMenuItem) child;
                JMenuItem item = new JMenuItem();
                item.setText(payload.getText());
                item.setIcon(payload.getIcon());
                item.setBorder(MenuEditLayer.DRAG_MENU_BORDER);
                item.setMargin(new Insets(1,1,1,1));
                item.setBorderPainted(true);
                dragComponent = item;
                //Dimension dim = new Dimension(80,40);
                //dragComponent.setPreferredSize(dim);
                dragComponent.setSize(dragComponent.getPreferredSize());
                dragComponent.setLocation(pt);
                layers.add(dragComponent, JLayeredPane.DRAG_LAYER);
                repaint();
            } else {
                started = false;
            }
        }
        
        private void updateDragOperation(MouseEvent e) {
            Point pt = SwingUtilities.convertPoint((JComponent)e.getSource(), e.getPoint(), MenuEditLayer.this);
            //Component child = layers.getComponentAt(pt);
            Component child = SwingUtilities.getDeepestComponentAt(layers, pt.x, pt.y);
            p("child = " + child);
            if(child instanceof JMenuItem && child != payloadComponent && child != dragComponent) {
                if(targetComponent != null) {
                    targetComponent.setBorder(MenuEditLayer.UNSELECTED_BORDER);
                }
                targetComponent = (JComponent)child;
                if(targetComponent != null) {
                    targetComponent.setBorder(MenuEditLayer.INSERTION_BORDER);
                }
                repaint();
            }
            pt.translate(5,5);
            dragComponent.setLocation(pt);
            repaint();
        }
        private void finishDragOperation(MouseEvent e) {
            moveRadComponentToBefore(payloadComponent, targetComponent);
            layers.remove(dragComponent);
            if(targetComponent != null) {
                targetComponent.setBorder(MenuEditLayer.UNSELECTED_BORDER);
            }
            dragComponent = null;
        }
        private JComponent dragComponent = null;
        private JComponent targetComponent = null;
        private JComponent payloadComponent = null;
        
        private void moveRadComponentToBefore(JComponent payload, JComponent target) {
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
                int index = payloadParentRad.getIndexOf(payloadRad);
                payloadParentRad.remove(payloadRad);
                FormModelEvent fme = formDesigner.getFormModel().fireComponentRemoved(payloadRad, payloadParentRad, index, false);

                p("=== inserting ===");
                // insert if target exists, else the item was removed by dragging out of the menu
                if(targetParentRad != null) {
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
        p("rebuild called");
        JMenu menu = (JMenu) formDesigner.getComponent(menuRAD);
        if(hackedPopupFactory.containerMap.containsKey(menu)) {
            JPanel popupContainer = hackedPopupFactory.containerMap.get(menu);
            
            popupContainer.removeAll();
            for(Component c : popupContainer.getComponents()) {
                if(c instanceof JMenu) {
                    unconfigureMenu((JMenu)c);
                } else {
                    unconfigureMenuItem((JComponent)c);
                }
            }
            // rebuild it
            for(RADVisualComponent child : menuRAD.getSubComponents()) {
                JComponent jchild = (JComponent) formDesigner.getComponent(child);
                p("checking: " + jchild);
                if(!isConfigured(jchild)) {
                    p("not configured!");
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
    
    //josh: all this key listener stuff should go into a separate class
    private void registerKeyListeners() {
        this.requestFocusInWindow(false);
        if(keyListener == null) {
            keyListener = new KeyListener() {
                public void keyPressed(KeyEvent e) {
                    //p("pressed: ");
                    if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                        selectOffsetMenuItem(+1);
                    }
                    if(e.getKeyCode() == KeyEvent.VK_UP) {
                        selectOffsetMenuItem(-1);
                    }
                    if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                        selectOffsetMenu(-1);
                    }
                    if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        selectOffsetMenu(+1);
                    }
                }
                public void keyReleased(KeyEvent e) {
                }
                public void keyTyped(KeyEvent e) {
                }
            };
            addKeyListener(keyListener);
        }
    }
    
    // select the next menu item offset from the current one.
    // pass in -1 and +1 to do prev and next menu items
    private void selectOffsetMenuItem(int offset) {
        if(currentMenuRAD.getSubComponents().length == 0) {
            setSelectedComponent(null);
            return;
        }
        if(selectedRADComponent == null) {
            selectedRADComponent = currentMenuRAD.getSubComponent(0);
        }
        int index = currentMenuRAD.getIndexOf(selectedRADComponent);
        if(index+offset >=0 && index+offset < currentMenuRAD.getSubComponents().length) {
            selectedRADComponent = currentMenuRAD.getSubComponent(index+offset);
        } else {
            selectedRADComponent = currentMenuRAD.getSubComponent(index);
        }
        
        JComponent item = (JComponent) formDesigner.getComponent(selectedRADComponent);
        setSelectedComponent(item);
    }
    
    private void selectOffsetMenu(int offset) {
        setSelectedComponent(null);
        if(formDesigner.getComponent(selectedRADComponent) instanceof JMenu) {
            showMenuPopup((JMenu)formDesigner.getComponent(selectedRADComponent));
            currentMenuRAD = (RADVisualContainer) selectedRADComponent;
            RADVisualComponent firstItemRad = currentMenuRAD.getSubComponent(0);
            if(firstItemRad != null) {
                setSelectedComponent((JComponent) formDesigner.getComponent(firstItemRad));
            }
            return;
        }
        int index = menuBarRAD.getIndexOf(currentMenuRAD);
        
        // if not a toplevel menu
        if(index < 0) {
            // if left then head back up the heirarchy
            if(offset < 0) {
                setSelectedComponent((JComponent) formDesigner.getComponent(currentMenuRAD));
                currentMenuRAD = currentMenuRAD.getParentContainer();
                return;
            }
            // if right then switch to the next a full toplevel menu
            if(offset > 0) {
                currentMenuRAD = getTopLevelMenu(currentMenuRAD);
                index = menuBarRAD.getIndexOf(currentMenuRAD);
                // now continue on as normal
            }
        }
        
        // set the current to the new one
        index = index+offset;
        // wrap around if necessary
        if(index <0) {
            index = menuBarRAD.getSubComponents().length-1;
        }
        if(index >= menuBarRAD.getSubComponents().length) {
            index = 0;
        }
        currentMenuRAD = (RADVisualContainer) menuBarRAD.getSubComponent(index);
        
        // show the new current menu
        JMenu menu = (JMenu) formDesigner.getComponent(currentMenuRAD);
        openMenu(currentMenuRAD,menu);
        
        // set the first item as selected
        if(currentMenuRAD.getSubComponents().length > 0) {
            selectedRADComponent = currentMenuRAD.getSubComponents()[0];
            setSelectedComponent((JComponent) formDesigner.getComponent(selectedRADComponent));
        }
    }
    private static void p(String string) {
        //turn off for now
        if(DEBUG) {
            System.out.println(string);
        }
    }
    
    private RADVisualContainer getTopLevelMenu(RADVisualContainer currentMenuRAD) {
        if(menuBarRAD.getIndexOf(currentMenuRAD) >= 0) {
            return currentMenuRAD;
        }
        return getTopLevelMenu(currentMenuRAD.getParentContainer());
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
}
