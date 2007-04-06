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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
    
    private RADVisualContainer currentMenuRAD;
    private JMenu currentMenu;
    private RADVisualComponent selectedRADComponent;
    private RADVisualContainer menuBarRAD;
    private KeyListener keyListener;
    private Map<RADVisualContainer,FormModelListener> formModelListeners;
    
    
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
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.RED);
        g.drawString("Menu Edit Layer",5,getHeight()-5);
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
        //p("MenuEditLayer.openMenu on: " + metacomp.getName());
        JMenu menu = (JMenu) comp;
        currentMenu = menu;
        registerKeyListeners();
        configureMenu(null,menu);
        showMenuPopup(menu);
        if(metacomp instanceof RADVisualContainer) {
            currentMenuRAD = (RADVisualContainer)metacomp;
            menuBarRAD = currentMenuRAD.getParentContainer();
            registerForm((RADVisualContainer)metacomp);
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
        
        
        // recurse for sub-menus
        for(Component c : subComps) {
            if(c instanceof JMenu) {
                configureMenu(menu, (JMenu)c);
                RADComponent rad = formDesigner.getMetaComponent(c);
                registerForm((RADVisualContainer)rad);
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
            //ex.printStackTrace();
        }
        //c.setTransferHandler(null);
        c.removeMouseListener(menuitemListenerMap.get(c));
        c.removeMouseMotionListener(menuitemListenerMap.get(c));
        menuParentMap.remove(c);
    }
    
    private void configMouseListener(final JComponent c) {
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
        
        public MenuItemMouseHandler(JComponent c) {
            super();
            this.c = c;
        }
        
        public void mousePressed(MouseEvent e) {
            started = false;
            if (!started && e.isPopupTrigger()) {
                //globalPopup.show(c, 40, 0);
            }
            start = e.getPoint();
        }
        
        public void mouseReleased(MouseEvent e) {
            setSelectedComponent(c);
        }
        
        
        private Point start;
        
        private boolean started = false;
        
        public void mouseDragged(MouseEvent e) {
            if (started)
                return;
            if (e.getPoint().distance(start) > 10) {
                started = true;
                JComponent comp = (JComponent) e.getSource();
                TransferHandler handler = comp.getTransferHandler();
                handler.exportAsDrag(comp, e, TransferHandler.MOVE);
                return;
            }
        }
    }
    
    //listens to see if this particular menu has been changed
    private void registerForm(final RADVisualContainer metacomp) {
        // don't double register
        if(!formModelListeners.containsKey(metacomp)) {
            FormModelListener fml = new FormModelListener() {
                public void formChanged(FormModelEvent[] events) {
                    for(FormModelEvent evt : events) {
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
            
            // rebuild it
            popupContainer.removeAll();
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
    
}
