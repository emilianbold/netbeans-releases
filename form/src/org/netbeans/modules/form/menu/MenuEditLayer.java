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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.PopupMenuUI;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.actions.PropertyAction;
import org.netbeans.modules.form.editors.IconEditor.NbImageIcon;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;

/**
 *
 * @author joshua.marinacci@sun.com
 */
public class MenuEditLayer extends JPanel {
    
    /* === public constants === */
    
    
    public static final Border DRAG_MENU_BORDER = BorderFactory.createLineBorder(Color.BLACK,1);
    public static final Border DRAG_SEPARATOR_BORDER = BorderFactory.createLineBorder(Color.RED,1);
    public static final Color SELECTED_MENU_BACKGROUND = new Color(0xA5A6A9);
    public static final Color EMPTY_ICON_COLOR = new Color(0xDDDDDD);
    public static final int EMPTY_ICON_BORDER_WIDTH = 2;
    /* === private constants === */
    private static final boolean DEBUG = false;
    
    /* === public and package level fields. these should probably become getters and setters  ===*/
    public VisualDesignerPopupFactory hackedPopupFactory = new VisualDesignerPopupFactory(this);
    public FormDesigner formDesigner;
    JLayeredPane layers;
    JComponent glassLayer;
    DropTargetLayer dropTargetLayer;
    boolean showMenubarWarning = false;
    
    /* === private fields === */
    private Map<JComponent,MouseInputAdapter> menuitemListenerMap;
    private Map<JMenu, PopupMenuUI> menuPopupUIMap;
    private Map<JComponent,JComponent> menuParentMap;
    
    private RADComponent selectedRADComponent = null;
    private JComponent selectedComponent;
    public enum SelectedPortion { Icon, Text, Accelerator, All, None };
    private SelectedPortion selectedPortion = SelectedPortion.None;
    
    private JMenu currentMenu;
    private KeyboardMenuNavigator keyboardMenuNavigator;
    private Map<RADVisualContainer,FormModelListener> formModelListeners;
    private DragOperation dragop;
    private FormModelListener menuBarFormListener;
    private PropertyChangeListener selectionListener;

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
        glassLayer.setSize(400,400); //josh: do i need this line? probably can delete it.
        
        dropTargetLayer = new DropTargetLayer(this);
        layers.add(dropTargetLayer, new Integer(JLayeredPane.DRAG_LAYER-5)); // put the drop target layer just above the drag layer
        
        // make the extra layers resize to the main component
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                glassLayer.setSize(MenuEditLayer.this.getSize());
                dropTargetLayer.setSize(MenuEditLayer.this.getSize());
            }
        });
        
        MouseInputAdapter mia = new GlassLayerMouseListener();
        glassLayer.addMouseListener(mia);
        glassLayer.addMouseMotionListener(mia);
        configureSelectionListener();
        //josh: when do we call this? unconfigureSelectionListener();
    }
    
    
    public static boolean isMenuRelatedRADComponent(RADComponent comp) {
        if(comp == null) return false;
        return isMenuRelatedComponentClass(comp.getBeanClass());
    }
    
    public static boolean isMenuBarContainer(RADComponent comp) {
        if(comp == null) return false;
        Class clas = comp.getBeanClass();
        if(clas == null) return false;
        if(JMenuBar.class.isAssignableFrom(clas)) return true;
        return false;
    }
    
    public static boolean isMenuRelatedContainer(RADComponent comp) {
        if(comp == null) return false;
        Class clas = comp.getBeanClass();
        if(clas == null) return false;
        if(JMenu.class.isAssignableFrom(clas)) return true;
        return false;
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
        if(item.getComponentClass() == null) return false;
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
        p("starting a new menu component drag and drop");
        this.setVisible(true);
        this.requestFocus();
        configureGlassLayer();
        configureFormListeners();
    }
    
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(DEBUG) {
            g.setColor(Color.RED);
            g.drawString("the MenuEditLayer is visible",5,getHeight()-5);
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        if(showMenubarWarning) {
            g2.drawString("You cannot add a menu component to a form without a menubar.", 5, getHeight()-30);
        }
        g2.dispose();
    }
    
    
    // the public method for non-menu parts of the form editor to
    // start menu editing
    public void openAndShowMenu(RADComponent metacomp, Component comp) {
        openMenu(metacomp, comp);
        glassLayer.requestFocusInWindow();
    }
    
    void openMenu(RADComponent metacomp, Component comp) {
        configureGlassLayer();
        registerKeyListeners();
        configureFormListeners();
        //reset the layers
        JMenu menu = (JMenu) comp;
        currentMenu = menu;
        configureMenu(null,menu);
        showMenuPopup(menu);
        if(metacomp instanceof RADVisualContainer) {
            keyboardMenuNavigator.setCurrentMenuRAD((RADVisualContainer)metacomp);
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
    private synchronized void registerKeyListeners() {
        if(keyboardMenuNavigator == null) {
            keyboardMenuNavigator = new KeyboardMenuNavigator(this);
            glassLayer.addKeyListener(keyboardMenuNavigator);
            keyboardMenuNavigator.selectedRADComponent = this.selectedRADComponent;
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

    private void configureSelectionListener() {
        if(selectionListener == null) {
            selectionListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    p("activated nodes changed: ");
                    Node[] oldNodes = (Node[])evt.getOldValue();
                    Node[] newNodes = (Node[])evt.getNewValue();
                    for(Node n : newNodes) {
                        if(n instanceof RADComponentNode) {
                            RADComponentNode radn = (RADComponentNode) n;
                            setSelectedRADComponent(radn.getRADComponent());
                        }
                    }
                    
                }
                
            };
            formDesigner.addPropertyChangeListener("activatedNodes",selectionListener);
        };
    }
    
    private void unconfigureSelectionListener() {
        if(selectionListener != null) {
            formDesigner.removePropertyChangeListener(selectionListener);
            selectionListener = null;
        }
    }

    void showMenuPopup(final JMenu menu) {
        // if already created then just make it visible
        if(hackedPopupFactory.containerMap.containsKey(menu)) {
            hackedPopupFactory.containerMap.get(menu).setVisible(true);
        } else {
            if(!isConfigured(menu)) {
                configureMenu(null, menu);
            }
            if(menu.isVisible()) {
                try {
                    JPopupMenu popup = menu.getPopupMenu();
                    popup.show(menu,0,menu.getHeight());
                } catch (Exception ex) {
                    //ignore anyexceptions caused by showing the popups
                }
            }
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
        if(metacomp.getBeanClass().equals(JMenu.class)) {
            return true;
        }
        return false;
    }
    
    
    
    void configureMenu(final JComponent parent, final JMenu menu) {
        // make sure it will draw it's border so we can have rollovers and selection
        menu.setBorderPainted(true);
        //install the wrapper icon if not a toplevel
        if(!(menu.getParent() instanceof JMenuBar)) {
            if(!(menu.getIcon() instanceof WrapperIcon)) {
                menu.setIcon(new WrapperIcon(menu.getIcon()));
            }
        }
        
        // configure the maps and popups
        menuParentMap.put(menu, parent);
        JPopupMenu popup = menu.getPopupMenu();
        menuPopupUIMap.put(menu, popup.getUI());
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
        if(c instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) c;
            if(!(item.getIcon() instanceof WrapperIcon)) {
                item.setIcon(new WrapperIcon(item.getIcon()));
            }
        }
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
    
    
    RADComponent getSelectedRADComponent() {
        return selectedRADComponent;
    }
    
    JComponent getSelectedComponent() {
        return selectedComponent;
    }
    
    void setSelectedRADComponent(RADComponent comp) {
        try {
            if (!this.isMenuRelatedRADComponent(comp)) {
                selectedComponent = null;
                selectedRADComponent = null;
                setVisible(false);
                return;
            }

            if (selectedRADComponent == comp) {
                return;
            }

            selectedRADComponent = comp;
            
            registerKeyListeners();
            keyboardMenuNavigator.selectedRADComponent = selectedRADComponent;
            formDesigner.setSelectedComponent(selectedRADComponent);

            if (selectedComponent != null) {
                selectedComponent.setBackground(getNormalBackground(selectedComponent));
            }

            selectedComponent = (JComponent) formDesigner.getComponent(selectedRADComponent);

            if (selectedComponent != null) {
                selectedComponent.setBackground(SELECTED_MENU_BACKGROUND);
                makeSureShowingOnScreen(comp, selectedComponent);
                if (selectedComponent instanceof JMenu) {
                    JMenu menu = (JMenu) selectedComponent;
                    showMenuPopup(menu);
                }
            }

            repaint();
        
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    private Color getNormalBackground(JComponent c) {
        Color color = null;
        if(c instanceof JMenuBar) {
            color = UIManager.getDefaults().getColor("MenuBar.background");
        }
        if(c instanceof JMenu) {
            color = UIManager.getDefaults().getColor("Menu.background");
        }
        if(c instanceof JCheckBoxMenuItem) {
            color = UIManager.getDefaults().getColor("CheckBoxMenuItem.background");
        }
        if(c instanceof JRadioButtonMenuItem) {
            color = UIManager.getDefaults().getColor("RadioButtonMenuItem.background");
        }
               
        if(color == null) {
            color = UIManager.getDefaults().getColor("MenuItem.background");
        }
        if(color == null) {
            color = Color.WHITE;
        }
        return color;
    }
    
    void setSelectedComponent(JComponent c) {
        setSelectedRADComponent(formDesigner.getMetaComponent(c));
    }
    
    
    private void makeSureShowingOnScreen(RADComponent rad, JComponent comp) {
        if(!this.isVisible()) {
            this.setVisible(true);
            registerKeyListeners();
            if(rad instanceof RADVisualContainer) {
                keyboardMenuNavigator.setCurrentMenuRAD((RADVisualContainer)rad);
            } else {
                keyboardMenuNavigator.setCurrentMenuRAD((RADVisualContainer)rad.getParentComponent());
            }
        }
        
        List<RADComponent> path = new ArrayList<RADComponent>();
        RADComponent temp = rad.getParentComponent();
        while(true) {
            if(temp == null) break;
            path.add(temp);
            temp = temp.getParentComponent();
            if(!isMenuRelatedRADComponent(temp)) {
                break;
            }
        }
        
        // go backwards, top to bottom
        for(int i = path.size()-1; i>=0; i--) {
            RADComponent r = path.get(i);
            JComponent c = (JComponent) formDesigner.getComponent(r);
            if(c instanceof JMenu) {
                showMenuPopup((JMenu)c);
            }
        }
        
    }
    
    

    
    private void showContextMenu(Point popupPos) {
        ComponentInspector inspector = ComponentInspector.getInstance();
        /*josh: i don't know what this does. i copied it from the HandleLayer
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated != formDesigner.multiViewObserver.getTopComponent()
                && activated != inspector)
            return;*/

        //formDesigner.componentActivated(); // just for sure...

        Node[] selectedNodes = inspector.getSelectedNodes();
        JPopupMenu popup = NodeOp.findContextMenu(selectedNodes);
        if (popup != null) {
            popup.show(this, popupPos.x, popupPos.y);
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
            if(target.getParent() instanceof JMenuBar) {
                targetParent = (JComponent) target.getParent();
            }
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
    
    void addRadComponentToAfter(RADVisualComponent payloadRad, JComponent target) {
        try {
            JComponent targetParent = getMenuParent(target);
            if(target.getParent() instanceof JMenuBar) {
                targetParent = (JComponent) target.getParent();
            }
            p("target parent = " + targetParent);
            RADVisualComponent targetRad = (RADVisualComponent) formDesigner.getMetaComponent(target);
            p("target rad = " + targetRad);
            RADVisualContainer targetParentRad = (RADVisualContainer) formDesigner.getMetaComponent(targetParent);
            p("target parent rad = " + targetParentRad);
            
            assert targetParentRad != null;
            
            p("=== inserting before drop target component ===");
            int index2 = targetParentRad.getIndexOf(targetRad) + 1;
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
            if(payloadParent == null) {
                payloadParent = (JComponent) payload.getParent();
            }
            p("payload parent = " + payloadParent);
            RADVisualComponent payloadRad = (RADVisualComponent) formDesigner.getMetaComponent(payload);
            p("payload rad = " + payloadRad);
            RADVisualContainer payloadParentRad = (RADVisualContainer) formDesigner.getMetaComponent(payloadParent);
            p("payload parent rad = " + payloadParentRad);
            
            if(payloadRad != null && payloadParentRad == null) {
                p("MenuDesigner: WARNING! their is a payload rad without a parent! how did we get here!");
            }
            
            // remove the component from it's old location
            // if no payload rad then that probably means this is a new component from the palette
            if(payloadRad != null && payloadParentRad != null) {
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
        moveRadComponentToBefore(payload, target, 0);
    }
    void moveRadComponentToAfter(JComponent payload, JComponent target) {
        moveRadComponentToBefore(payload, target, 1);
    }
    void moveRadComponentToBefore(JComponent payload, JComponent target, int offset) {
        try {
            if(payload == target) {
                p("can't move onto self");
                return;
            }
            JComponent payloadParent = getMenuParent(payload);
            /*
            if(payloadParent == null) {
                payloadParent = (JComponent) payload.getParent();
            }*/
            p("payload parent = " + payloadParent);
            
            JComponent targetParent = getMenuParent(target);
            
            if(targetParent == null) {
                targetParent = (JComponent) target.getParent();
            }
            p("target parent = " + targetParent);
            
            RADVisualComponent payloadRad = (RADVisualComponent) formDesigner.getMetaComponent(payload);
            p("payload rad = " + payloadRad);
            RADVisualComponent targetRad = (RADVisualComponent) formDesigner.getMetaComponent(target);
            p("target rad = " + targetRad);
            RADVisualContainer payloadParentRad = (RADVisualContainer) formDesigner.getMetaComponent(payloadParent);
            p("payload parent rad = " + payloadParentRad);
            RADVisualContainer targetParentRad = (RADVisualContainer) formDesigner.getMetaComponent(targetParent);
            p("target parent rad = " + targetParentRad);
            

            //if a toplevel menu dragged next to another toplevel menu
            if(payload instanceof JMenu && payload.getParent() instanceof JMenuBar 
                    && target instanceof JMenu && target.getParent() instanceof JMenuBar) {
                //remove from old spot
                targetParent = (JComponent) target.getParent();
                payloadParent = (JComponent) payload.getParent();
                payloadParentRad = (RADVisualContainer) formDesigner.getMetaComponent(payloadParent);
                targetParentRad = (RADVisualContainer) formDesigner.getMetaComponent(targetParent);
                p("new payload parent rad = " + payloadParentRad);
            }
            
            p("=== removing ===");
            //skip if no payload rad, which probably means this is a new component from the palette
            if(payloadRad != null && payloadParentRad != null) {
                p("=== did a remove ===");
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
                int index2 = targetParentRad.getIndexOf(targetRad) + offset;
                p("index of target = " + index2);
                targetParentRad.add(payloadRad, index2);
                targetParentRad.getLayoutSupport().addComponents(new RADVisualComponent[] { payloadRad }, 
                        null, index2);
                FormModelEvent fme2 = formDesigner.getFormModel().fireComponentAdded(payloadRad, false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static boolean addComponentToEndOfMenu(RADComponent targetContainer, PaletteItem paletteItem) {
        FormModel model = targetContainer.getFormModel();
        MetaComponentCreator creator = model.getComponentCreator();
        RADVisualComponent precreated = creator.precreateVisualComponent(
                paletteItem.getComponentClassSource());
        JComponent newComponent = (JComponent) precreated.getBeanInstance();
        return creator.addPrecreatedComponent(targetContainer, null);
    }

    //listens to see if this particular menu has been changed
    private void registerForm(final RADVisualContainer metacomp, final JMenu menu) {
        // don't double register
        if(!formModelListeners.containsKey(metacomp)) {
            FormModelListener fml = new FormModelListener() {
                public void formChanged(FormModelEvent[] events) {
                    if (events != null) {
                        for(FormModelEvent evt : events) {
                            
                            p("form model update: " + evt.getChangeType() + " = ");
                            switch(evt.getChangeType()) {
                            case FormModelEvent.COMPONENT_PROPERTY_CHANGED: p("COMPONENT_PROPERTY_CHANGED");break;
                            case FormModelEvent.COMPONENT_ADDED: p("COMPONENT_ADDED");break;
                            case FormModelEvent.COMPONENT_REMOVED: p("COMPONENT_REMOVED");break;
                            case FormModelEvent.COMPONENTS_REORDERED: p("COMPONENTS_REORDERED");break;
                            default: p("unknown type");
                            }
                            if(evt.getComponent() != null) {
                                p(" " + evt.getComponent().getName());
                            };
                            
                            if(evt.getChangeType() == evt.COMPONENT_PROPERTY_CHANGED) {
                                if(evt.getContainer() == metacomp || evt.getComponent() == metacomp) {
                                    rebuildOnScreenMenu(metacomp);
                                }
                                updateIcon(evt.getComponent());
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
                                    return;
                                }
                                if(evt.getContainer() == getFormMenuBar()) {
                                    p("it was a rad though: " + getFormMenuBar());
                                    JComponent comp = (JComponent) formDesigner.getComponent(getFormMenuBar());
                                    p("comp = " + comp);
                                    //comp.revalidate();
                                    RADVisualContainer rad = (RADVisualContainer) getFormMenuBar();
                                    comp.removeAll();
                                    for(RADVisualComponent c : rad.getSubComponents()) {
                                        p("c = " + c);
                                        if(c!=null) {
                                            comp.add((JComponent)formDesigner.getComponent(c));
                                        }
                                    }
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
                if(child != null) {
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
            }
            
            // repack it
            popupContainer.setSize(popupContainer.getLayout().preferredLayoutSize(popupContainer));
            validate();
            popupContainer.repaint();
        } else {
            p("menu popup not built yet");
        }
    }
    
    private void updateIcon(RADComponent rad) {
        try {
            Component comp = (Component) formDesigner.getComponent(rad);
            if(comp instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) comp;
                //p("item rad = " + rad);
                RADProperty icon_prop = rad.getBeanProperty("icon");
                //p("icon prop = " + icon_prop);
                Object value = icon_prop.getValue();
                //p("value = " + value);
                // extract the new value
                Icon icon = null;
                if(value instanceof Icon) {
                    icon = (Icon) value;
                }
                if(value instanceof NbImageIcon) {
                    icon = ((NbImageIcon)value).getIcon();
                }
                if(value instanceof ResourceValue) {
                    ResourceValue rv = (ResourceValue) value;
                    //p("design value = " + rv.getDesignValue());
                    Object designValue = rv.getDesignValue();
                    if(designValue instanceof Icon) {
                        //p("updating icon w/ design value");
                        icon = (Icon) designValue;
                    }
                    if(designValue instanceof NbImageIcon) {
                        icon = ((NbImageIcon)designValue).getIcon();
                    }
                }
                // do the actual update
                //p("updating the real icon with the new value of: "+ icon);
                if(item.getIcon() instanceof WrapperIcon) {
                    ((WrapperIcon)item.getIcon()).setIcon(icon);
                } else {
                    item.setIcon(icon);
                }
            }
        } catch (Throwable thr) {
            thr.printStackTrace();
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
    
    public static boolean containsMenuBar(RADVisualComponent[] comps) {
        if(comps == null) return false;
        if(comps.length < 1) return false;
        for(RADVisualComponent c : comps) {
            if(JMenuBar.class.isAssignableFrom(c.getBeanClass())) return true;
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
        for(RADComponent comp : formDesigner.getFormModel().getAllComponents()) {
            if(JMenuBar.class.isAssignableFrom(comp.getBeanClass())) {
                return true;
            }
        }
        return false;
    }
    
    public RADComponent getFormMenuBar() {
        for(RADComponent comp : formDesigner.getFormModel().getAllComponents()) {
            if(JMenuBar.class.isAssignableFrom(comp.getBeanClass())) {
                return comp;
            }
        }
        return null;
    }


    private class GlassLayerMouseListener extends MouseInputAdapter {
        Point pressPoint = null;
        JComponent pressComp = null;
        private boolean isEditing = false;
        
        public void mouseClicked(MouseEvent e) {
        }
        
        public void mousePressed(MouseEvent e) {
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
                    p("this is a top level JMenu");
                    if(e.getClickCount() > 1) {
                        isEditing = true;
                        formDesigner.startInPlaceEditing(rad);
                    } else {
                        openMenu(rad, c);
                        glassLayer.requestFocusInWindow();
                        setSelectedComponent(c);
                        if(e.isPopupTrigger()) {
                            showContextMenu(e.getPoint());
                            return;
                        }
                        if(!dragop.isStarted()) {
                            p("possibly starting a drag op of a toplevel menu");
                            pressPoint = e.getPoint();
                            pressComp = c;
                            return;
                        }
                    }
                    return;
                }
            }

            JComponent c = dragop.getDeepestComponentInPopups(e.getPoint());

            
            if(c == null && !isMenuRelatedRADComponent(rad)) {
                p("not a menu component. going back to handle layer");
                PaletteUtils.clearPaletteSelection();
                hideMenuLayer();
            }

            // start editing
            if(e.getClickCount() > 1) {
                if(c instanceof JMenuItem) {
                    p("starting the inline editing");
                    JMenuItem item = (JMenuItem) c;
                    Point pt = SwingUtilities.convertPoint(glassLayer, e.getPoint(), item);
                    SelectedPortion portion = DropTargetLayer.calculateSelectedPortion(item, pt);
                    p("selected portion = " + portion);
                    RADComponent radcomp = formDesigner.getMetaComponent(item);
                    if(portion == SelectedPortion.Icon) {
                        p("editing icon");
                        showIconEditor(radcomp);
                    } else if (portion == SelectedPortion.Accelerator) {
                        p("editing accel");
                        showAcceleratorEditor(radcomp);
                    } else {
                        isEditing = true;
                        formDesigner.startInPlaceEditing(radcomp);
                    }
                }
            }
            
            // show context menu
            if(e.isPopupTrigger()) {
                p("showing a context menu");
                showContextMenu(e.getPoint());
                return;
            }

            //prep for drag motion for menuitem to menuitem drags
            if(!dragop.isStarted() && c instanceof JMenuItem) {
                p("possibly starting a drag op");
                pressPoint = e.getPoint();
                pressComp = c;
                return;
            }
        }
        
        public void mouseReleased(MouseEvent e) {
            if(e.isPopupTrigger()) {
                //p("showing a context menu");
                showContextMenu(e.getPoint());
                return;
            }
            
            //p("mouse released: " + e);
            if(dragop.isStarted()) {
                dragop.end(e.getPoint());
            } else {
                if(!isEditing) {
                    JComponent c = (JComponent) dragop.getDeepestComponentInPopups(e.getPoint());
                    if(c != null) { 
                        if(c instanceof JMenuItem) {
                            Point localPt = SwingUtilities.convertPoint(glassLayer, e.getPoint(), c);
                            selectedPortion = DropTargetLayer.calculateSelectedPortion((JMenuItem)c, localPt);
                            dropTargetLayer.repaint();
                        } else {
                            selectedPortion = SelectedPortion.None;
                        }
                        glassLayer.requestFocusInWindow();
                        setSelectedComponent(c);
                        
                    }
                }
                isEditing = false;
            }
        }
        
        private void showIconEditor(RADComponent comp) {
            try {
                RADProperty prop = comp.getBeanProperty("icon");
                new PropertyAction(prop).actionPerformed(null);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        private void showAcceleratorEditor(RADComponent comp) {
            try {
                RADProperty prop = comp.getBeanProperty("accelerator");
                new PropertyAction(prop).actionPerformed(null);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        
        public void mouseEntered(MouseEvent e) {
            if(showMenubarWarning) {
                showMenubarWarning = false;
                repaint();
            }
            if(!dragop.isStarted() || PaletteUtils.getSelectedItem() != dragop.getCurrentItem()) {
                PaletteItem item = PaletteUtils.getSelectedItem();
                
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
                
                /*
                if(formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT && showMenubarWarning) {
                    p("still in bad mode");
                    //glassLayer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    showMenubarWarning = false;
                    repaint();
                }*/
            }
        }
        
        public void mouseExited(MouseEvent e) {
            //p("mouse exited: " + e);
        }
        
        public void mouseDragged(MouseEvent e) {
            if(!dragop.isStarted() && pressPoint != null && pressComp instanceof JMenuItem
                    && e.getPoint().distance(pressPoint) > 10) {
                p("starting the dragop");
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
            p("drag enter: " + dtde);
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
            p("drag over: " + dtde);
            if(dragop.isStarted()) {
                p("moving dragop");
                dragop.move(dtde.getLocation());
            }
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        public void dragExit(DropTargetEvent dte) {
        }

        public void drop(DropTargetDropEvent dtde) {
            if(dragop.isStarted()) {
                dragop.end(dtde.getLocation());
                return;
            }
        }
        
    }
    
    
    public SelectedPortion getCurrentSelectedPortion() {
        return selectedPortion;
    }
     
    
    
    
    class WrapperIcon implements Icon {
        private Icon wrapee;
        public WrapperIcon() {
            this(null);
        }
        public WrapperIcon(Icon icon) {
            wrapee = icon;
        }
        
        public void setIcon(Icon icon) {
            this.wrapee = icon;
        }
        
        public void paintIcon(Component arg0, Graphics g, int x,  int y) {
            if(wrapee != null) {
                wrapee.paintIcon(arg0, g, x, y);
            } else {
                Graphics g2 = g.create();
                g2.setColor(Color.WHITE);
                g2.fillRect(x,y,getIconWidth()-1, getIconHeight()-1);
                g2.setColor(MenuEditLayer.EMPTY_ICON_COLOR);
                g2.drawRect(x,y,getIconWidth()-1, getIconHeight()-1);
                g2.drawRect(x+1,y+1,getIconWidth()-3, getIconHeight()-3);
                g2.dispose();
            }
        }
        
        public int getIconWidth() {
            if(wrapee != null) {
                return wrapee.getIconWidth();
            }
            return 16;
        }
        
        public int getIconHeight() {
            if(wrapee != null) {
                return wrapee.getIconHeight();
            }
            return 16;
        }
        
    }

}
