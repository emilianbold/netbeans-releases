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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import javax.swing.Action;
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
import javax.swing.border.CompoundBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.PopupMenuUI;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.actions.PropertyAction;
import org.netbeans.modules.form.assistant.AssistantMessages;
import org.netbeans.modules.form.editors.IconEditor.NbImageIcon;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;

/**
 *
 * @author joshua.marinacci@sun.com
 */
public class MenuEditLayer extends JPanel {
    
    /* === public constants === */
    

    /* === constants for the look of the designer === */
    public static final Border DRAG_MENU_BORDER = BorderFactory.createLineBorder(Color.BLACK,1);
    public static final Border DRAG_SEPARATOR_BORDER = BorderFactory.createLineBorder(Color.RED,1);
    public static final Color SELECTED_MENU_BACKGROUND = new Color(0xA5A6A9);
    public static final Color EMPTY_ICON_COLOR = new Color(0xDDDDDD);
    public static final int EMPTY_ICON_BORDER_WIDTH = 2;
    
    /* === private constants === */
    private static final boolean DEBUG = false;
    
    /* === public and package level fields. these should probably become getters and setters  ===*/
    public VisualDesignerPopupFactory hackedPopupFactory = null;
    public FormDesigner formDesigner;
    JLayeredPane layers;
    JComponent glassLayer;
    DropTargetLayer dropTargetLayer;
    boolean showMenubarWarning = false;
    
    /* === private fields === */
    private Map<JMenu, PopupMenuUI> menuPopupUIMap;
    
    public enum SelectedPortion { Icon, Text, Accelerator, All, None };
    private SelectedPortion selectedPortion = SelectedPortion.None;
    
    private JMenu currentMenu;
    private KeyboardMenuNavigator keyboardMenuNavigator;
    private Map<RADVisualContainer,FormModelListener> formModelListeners;
    private DragOperation dragop;
    private FormModelListener menuBarFormListener;
    private PropertyChangeListener selectionListener;
    private boolean isAlive = true;

    /** Creates a new instance of MenuEditLayer */
    public MenuEditLayer(final FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
        menuPopupUIMap = new HashMap<JMenu, PopupMenuUI>();
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
        this.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                glassLayer.setSize(MenuEditLayer.this.getSize());
                dropTargetLayer.setSize(MenuEditLayer.this.getSize());
            }

            public void componentMoved(ComponentEvent arg0) {
            }

            public void componentShown(ComponentEvent arg0) {
            }

            public void componentHidden(ComponentEvent arg0) {
            }
        });
        
        MouseInputAdapter mia = new GlassLayerMouseListener();
        glassLayer.addMouseListener(mia);
        glassLayer.addMouseMotionListener(mia);
        configureSelectionListener();
        
        if (!assistantInitialized) {
            initAssistant();
        }
    }
    
    private boolean assistantInitialized = false;
    private void initAssistant() {
        String missingMenubarMsg = "You cannot add a menu component to a form without a menubar";
        AssistantMessages messages = AssistantMessages.getDefault();
        messages.setMessages("missingMenubar", missingMenubarMsg); // NOI18N
        assistantInitialized = true;
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
        if(JPopupMenu.class.isAssignableFrom(clas)) return true;
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
        /* Using the assistant now instead of text ontop of the glasspane.
         * josh: I need to delete all of the previous code related to the showMenubarWarning boolean
        if(showMenubarWarning) {
            g2.drawString("You cannot add a menu component to a form without a menubar.", 5, getHeight()-30);
        }*/
        g2.dispose();
    }
    
    
    // the public method for non-menu parts of the form editor to
    // start menu editing
    public void openAndShowMenu(RADComponent metacomp, Component comp) {
        //p("making sure the menu is open: " + metacomp +  " " + metacomp.getName());
        if(hackedPopupFactory == null) {
            this.hackedPopupFactory = new VisualDesignerPopupFactory(this);
        }
        openMenu(metacomp, comp);
        glassLayer.requestFocusInWindow();
    }
    
    void openMenu(RADComponent metacomp, Component comp) {
        getPopupFactory();
        configureGlassLayer();
        registerKeyListeners();
        configureFormListeners();
        configureSelectionListener();
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
        unconfigureSelectionListener();
        for(JMenu m : menuPopupUIMap.keySet()) {
            unconfigureMenu(m);
        }
        menuPopupUIMap.clear();
        if(hackedPopupFactory != null) {
            hackedPopupFactory.containerMap.clear();
            hackedPopupFactory = null;
        }
        if(dragop.isStarted()) {
            dragop.fastEnd();
        }
        // close all popup frames
        this.setVisible(false);
        if(keyboardMenuNavigator != null) {
            glassLayer.removeKeyListener(keyboardMenuNavigator);
            keyboardMenuNavigator.unconfigure();
            keyboardMenuNavigator = null;
        }
        backgroundMap.clear();
        //hackedPopupFactory.containerMap.clear();
    }
    
    //josh: all this key listener stuff should go into a separate class
    private synchronized void registerKeyListeners() {
        if(keyboardMenuNavigator == null) {
            keyboardMenuNavigator = new KeyboardMenuNavigator(this);
            glassLayer.addKeyListener(keyboardMenuNavigator);
            glassLayer.addKeyListener(new KeyListener() {

                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                    if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        dragop.fastEnd();
                    }
                }

                public void keyReleased(KeyEvent e) {
                }
                
            });
        }
    }
    
    private VisualDesignerPopupFactory getPopupFactory() {
        if(hackedPopupFactory == null) {
            hackedPopupFactory = new VisualDesignerPopupFactory(this);
        }
        return hackedPopupFactory;
    }
    
    private void configureGlassLayer() {
        try {
            glassLayer.setDropTarget(new DropTarget());
            glassLayer.getDropTarget().addDropTargetListener(new GlassLayerDropTargetListener());
        } catch (TooManyListenersException ex) {
            ex.printStackTrace();
        }
    }
    
    PropertyChangeListener paletteListener = null;
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
                            if(evt.getChangeType() == evt.FORM_TO_BE_CLOSED) {
                                hideMenuLayer();
                                isAlive = false;
                            }
                            if(evt.getChangeType() == evt.COMPONENT_ADDED) {
                                if(evt.getCreatedDeleted()) {
                                    configureNewComponent(evt.getComponent());
                                }
                            }
                            
                        }
                    }
                }                
            };
            formDesigner.getFormModel().addFormModelListener(menuBarFormListener);
        }
        if(paletteListener == null) {
            paletteListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if(PaletteUtils.getSelectedItem() == null || 
                            !isMenuRelatedComponentClass(PaletteUtils.getSelectedItem().getComponentClass())) {
                        if(dragop != null && dragop.isStarted()) {
                            dragop.fastEnd();
                        }
                    }     
                }
            };
            paletteContext = formDesigner.getFormEditor().getFormDataObject().getFormFile();
            PaletteUtils.addPaletteListener(paletteListener, paletteContext);
        }
    }    
    
    FileObject paletteContext = null;
    private void unconfigureFormListeners() {
        if(menuBarFormListener != null) {
            if(formDesigner != null && formDesigner.getFormModel() != null) {
                formDesigner.getFormModel().removeFormModelListener(menuBarFormListener);
            }
        }
        if(paletteListener != null) {
            PaletteUtils.removePaletteListener(paletteListener, paletteContext);
            paletteContext = null;
            paletteListener = null;
        }
        menuBarFormListener = null;
    }    

    private void configureSelectionListener() {
        if(selectionListener == null) {
            selectionListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if(!isAlive) return;
                    Node[] oldNodes = (Node[])evt.getOldValue();
                    Node[] newNodes = (Node[])evt.getNewValue();
                    List<RADComponent> selectedNodes = new ArrayList<RADComponent>();

                    for(Node n : newNodes) {
                        if(n instanceof RADComponentNode) {
                            RADComponentNode radn = (RADComponentNode) n;
                            selectedNodes.add(radn.getRADComponent());
                        }
                    }
                    
                    setSelectedRADComponents(selectedNodes);
                    
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
        getPopupFactory();
        // if already created then just make it visible
        if(hackedPopupFactory.containerMap.containsKey(menu)) {
            JPanel view = hackedPopupFactory.containerMap.get(menu);
            view.setVisible(true);
        } else {
            if(!isConfigured(menu)) {
                configureMenu(null, menu);
            }
            final JPopupMenu popup = menu.getPopupMenu();
            
            if(!(popup.getUI() instanceof VisualDesignerPopupMenuUI)) {
                popup.setUI(new VisualDesignerPopupMenuUI(this, popup.getUI()));
            }
            if(menu.isVisible()) {
                //force popup view creation
                hackedPopupFactory.getPopup(menu, null, 0, 0);
                
                // do later so that the component will definitely be on screen by then
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            popup.show(menu,0,menu.getHeight());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            //ignore anyexceptions caused by showing the popups
                        }
                        
                    }
                });
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
        //p("configuring the menu: " + menu.getText());
        // make sure it will draw it's border so we can have rollovers and selection
        menu.setBorderPainted(true);
        //install the wrapper icon if not a toplevel JMenu
        if(!isTopLevelMenu(menu)) {
            if(!(menu.getIcon() instanceof WrapperIcon)) {
                menu.setIcon(new WrapperIcon(menu.getIcon()));
            }
        }
        
        // configure the maps and popups
        JPopupMenu popup = menu.getPopupMenu();
        //p("got a popup: " + popup);
        menuPopupUIMap.put(menu, popup.getUI());
        popup.setUI(new VisualDesignerPopupMenuUI(this, popup.getUI()));
        
        // get all of the components in this menu
        Component[] subComps = menu.getMenuComponents();
        //p("the current subcomps of the menu are:");
        for(Component c : subComps) {
            //p("   comp: " + c);
        }
        // if this isn't the first time this menu has been opened then the sub components
        // will have been moved to the popupPanel already, so we will find them there instead.
        JPanel popupPanel = getPopupFactory().containerMap.get(menu);
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
        VisualDesignerJPanelPopup pop = hackedPopupFactory.getPopup(menu);
        if(pop != null) {
            pop.hide();
        }
        if(popup != null) {
            popup.setVisible(false);
        }
        menu.setPopupMenuVisible(false);
        hackedPopupFactory.containerMap.remove(menu);
    }
    
    private boolean isConfigured(JComponent c) {
        return menuPopupUIMap.containsKey(c);
    }
    
    
    void configureMenuItem(final JMenu parent, final JComponent c) {
        if(c instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) c;
            if(!(item.getIcon() instanceof WrapperIcon)) {
                item.setIcon(new WrapperIcon(item.getIcon()));
            }
            installAcceleratorPreview(item);
            item.setBorderPainted(true);
        }
    }
    
    static final int ACCEL_PREVIEW_WIDTH = 80;
    private static final Border accel_border = new Border() {

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(Color.WHITE);
            int offset = 5;
            g.fillRect(width-ACCEL_PREVIEW_WIDTH+offset,   1,ACCEL_PREVIEW_WIDTH, height);
            g.setColor(EMPTY_ICON_COLOR);
            g.drawRect(width-ACCEL_PREVIEW_WIDTH+offset,   1,ACCEL_PREVIEW_WIDTH-1, height);
            g.drawRect(width-ACCEL_PREVIEW_WIDTH+offset+1, 2,ACCEL_PREVIEW_WIDTH-3, height-2);
            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font("SansSerif",Font.PLAIN,10));
            g.drawString("shortcut", width-ACCEL_PREVIEW_WIDTH+15,height-3);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0,0,0,ACCEL_PREVIEW_WIDTH);
        }

        public boolean isBorderOpaque() {
            return true;
        }
        
    };
    
    
    //installs a special border to represent the accelerator preview
    //if the menu item already has an accelerator, then it will
    //remove the preview if necessary.
    private static void installAcceleratorPreview(JMenuItem item) {
        //detect accelerator key
        boolean already_has_accel = false;
        if(item.getAccelerator() != null) already_has_accel = true;
        if(item.getAction() != null && item.getAction().getValue(Action.ACCELERATOR_KEY) != null) already_has_accel = true;

        
        
        boolean already_has_accel_border = false;
        if(item.getBorder() == accel_border) {
            already_has_accel_border = true;
            //uninstall if needed
            if(already_has_accel) {
                item.setBorder(null);
                return;
            }
        }
        
        if(item.getBorder() instanceof CompoundBorder) {
            CompoundBorder comp = (CompoundBorder)item.getBorder();
            if(comp.getInsideBorder() == accel_border) {
                already_has_accel_border = true;
                //uninstall if needed
                if(already_has_accel) {
                    item.setBorder(comp.getOutsideBorder());
                    return;
                }
            }
        }
        
        if(already_has_accel_border) return;
        if(already_has_accel) return;
        
        
        if(item.getBorder() == null) {
            item.setBorder(accel_border);
            return;
        }
        
        item.setBorder(BorderFactory.createCompoundBorder(
                    item.getBorder(),accel_border));
    }
    
    
    void unconfigureMenuItem(JComponent c) {
    }
    
    
    
    //override JComponent.isOpaque to always return false
    public boolean isOpaque() {
        return false;
    }
    
    
    // returns true if parent really is an ancestor of target
    boolean isAncestor(JComponent target, JComponent parent) {
        if(!(parent instanceof JMenu)) {
            return false;
        }
        RADComponent targetRad = formDesigner.getMetaComponent(target);
        RADComponent parentRad = targetRad.getParentComponent();
        if(parentRad == null) return false;
        Object possibleParent = formDesigner.getComponent(parentRad);
        RADComponent realParentRad = formDesigner.getMetaComponent(parent);
        if(parentRad == realParentRad) {
            return true;
        }
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
        RADComponent targetRad = formDesigner.getMetaComponent(menu);
        RADComponent parentRad = targetRad.getParentComponent();
        Object possibleParent = formDesigner.getComponent(parentRad);
        if(possibleParent instanceof JComponent) {
            return (JComponent) possibleParent;
        } else {
            return null;
        }
    }
    
   
    List<RADComponent> getSelectedRADComponents() {
        return Collections.unmodifiableList(selectedComponents);
    }
    
    RADComponent getSingleSelectedComponent() {
        if(selectedComponents.isEmpty()) {
            return null;
        }
        if(selectedComponents.size() > 1) {
            setSelectedRADComponent(selectedComponents.get(0));
        }
        return selectedComponents.get(0);
    }
    
    
    private List<RADComponent> selectedComponents = new ArrayList<RADComponent>();
    
    
    boolean isComponentSelected() {
        return !selectedComponents.isEmpty();
    }
    
    void setSelectedRADComponent(RADComponent comp) {
        List<RADComponent> comps = new ArrayList<RADComponent>();
        comps.add(comp);
        setSelectedRADComponents(comps);
        formDesigner.setSelectedComponent(comp);
    }
    
    void setSelectedRADComponents(List<RADComponent> comps) {
        try {
            //clear old bgs first
            for(RADComponent rad : selectedComponents) {
                if(isMenuRelatedRADComponent(rad) && !isMenuBarContainer(rad)) { // don't mess w/ the menubar's background
                    JComponent c = (JComponent) formDesigner.getComponent(rad);
                    if(c != null) { // could be null if comp was just deleted
                        c.setBackground(getNormalBackground(c));
                    }
                }
            }

            selectedComponents.clear();
            selectedComponents.addAll(comps);

            //check for non-menu comps
            for(RADComponent c : selectedComponents) {
                if (!isMenuRelatedRADComponent(c)) {
                    setVisible(false);
                    return;
                }
            }

            registerKeyListeners();

            //josh: what do I do about this?
            //formDesigner.setSelectedComponent(selectedRADComponent);

            for(RADComponent rad : selectedComponents) {
                JComponent c = (JComponent) formDesigner.getComponent(rad);
                if(c != null) {
                    if(!isMenuBarContainer(rad)) { // don't mess w/ the menubar's background
                        c.setBackground(getSelectedBackground(c));
                    }
                    makeSureShowingOnScreen(rad, c);
                    if (c instanceof JMenu) {
                        showMenuPopup((JMenu) c);
                    }
                }
            }

            repaint();
        
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getComponentDefaultsPrefix(JComponent c) {
        if(c instanceof JMenuBar) {
            return "MenuBar";
        }
        if(c instanceof JMenu) {
            return "Menu";
        }
        if(c instanceof JCheckBoxMenuItem) {
            return "CheckBoxMenuItem";
        }
        if(c instanceof JRadioButtonMenuItem) {
            return "RadioButtonMenuItem";
        }
        return "MenuItem";
    }
    
    private Color getNormalBackground(JComponent c) {
        String prefix = getComponentDefaultsPrefix(c);
        Color color = UIManager.getDefaults().getColor(prefix+".background");
        color = backgroundMap.get(c);
        if(color == null) {
            color = Color.WHITE;
        }
        return color;
    }
    
    private Map<JComponent, Color> backgroundMap = new HashMap<JComponent,Color>();
    private Color getSelectedBackground(JComponent c) {
        backgroundMap.put(c,c.getBackground());
        return SELECTED_MENU_BACKGROUND;
    }
    
    private Color getNormalForeground(JComponent c) {
        String prefix = getComponentDefaultsPrefix(c);
        Color color = UIManager.getDefaults().getColor(prefix+".foreground");
        if(color == null) {
            color = Color.BLACK;
        }
        return color;
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
        Node[] selectedNodes = inspector.getSelectedNodes();
        JPopupMenu popup = NodeOp.findContextMenu(selectedNodes);
        if(!this.isVisible()) {
            this.setVisible(true);
        }
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
        addRadComponentTo(payloadRad, target, +0);
    }
    
    void addRadComponentToAfter(RADVisualComponent payloadRad, JComponent target) {
        addRadComponentTo(payloadRad, target, +1);
    }
    private void addRadComponentTo(RADVisualComponent payloadRad, JComponent target, int offset) {
        try {
            JComponent targetParent = getMenuParent(target);
            if(target.getParent() instanceof JMenuBar) {
                targetParent = (JComponent) target.getParent();
            }
            //p("target parent = " + targetParent);
            RADVisualComponent targetRad = (RADVisualComponent) formDesigner.getMetaComponent(target);
            //p("target rad = " + targetRad);
            RADVisualContainer targetParentRad = (RADVisualContainer) formDesigner.getMetaComponent(targetParent);
            //p("target parent rad = " + targetParentRad);
            
            assert targetParentRad != null;
            
            //p("=== inserting before drop target component ===");
            int index2 = targetParentRad.getIndexOf(targetRad) + offset;
            //p("inserting at index: " + index2);
            FormModelEvent fme2 = formDesigner.getFormModel().fireComponentAdded(payloadRad, false);
            formDesigner.getFormModel().addVisualComponent(payloadRad, targetParentRad, new Integer(index2), true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
    boolean addRadComponentToEnd(JComponent targetComponent, MetaComponentCreator creator) {
        RADVisualContainer targetContainer = (RADVisualContainer) formDesigner.getMetaComponent(targetComponent);
        //p("target container = " + targetContainer);
        Object constraints = null;
        boolean added = creator.addPrecreatedComponent(targetContainer, constraints);
        //p("added comp: " + creator.getPrecreatedMetaComponent());
        return added;
    }
    
    void moveRadComponentInto(JComponent payload, JComponent targetMenu) {
        try {
            
            //check if dragging onto self
            if(payload == targetMenu) {
                return;
            }
            
            //check if dragging to a descendant node
            if(isAncestor(targetMenu, payload)) {
                return;
            }
            
            JComponent payloadParent = getMenuParent(payload);
            if(payloadParent == null) {
                payloadParent = (JComponent) payload.getParent();
            }
            //p("payload parent = " + payloadParent);
            RADVisualComponent payloadRad = (RADVisualComponent) formDesigner.getMetaComponent(payload);
            //p("payload rad = " + payloadRad);
            RADVisualContainer payloadParentRad = (RADVisualContainer) formDesigner.getMetaComponent(payloadParent);
            //p("payload parent rad = " + payloadParentRad);
            
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
            //p("target menu rad = " + targetMenuRad);
            //add inside the target menu
            //p("=== inserting at end of a menu ===");
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
        moveRadComponentTo(payload, target, 0);
    }
    void moveRadComponentToAfter(JComponent payload, JComponent target) {
        moveRadComponentTo(payload, target, 1);
    }
    private void moveRadComponentTo(JComponent payload, JComponent target, int offset) {
        try {
            if(payload == target) {
                return;
            }
            //check if dragging to a descendant node
            if(isAncestor(target, payload)) {
                return;
            }
            JComponent payloadParent = getMenuParent(payload);
            /*
            if(payloadParent == null) {
                payloadParent = (JComponent) payload.getParent();
            }*/
            //p("payload parent = " + payloadParent);
            
            JComponent targetParent = getMenuParent(target);
            
            if(targetParent == null) {
                targetParent = (JComponent) target.getParent();
            }
            //p("target parent = " + targetParent);
            
            RADVisualComponent payloadRad = (RADVisualComponent) formDesigner.getMetaComponent(payload);
            //p("payload rad = " + payloadRad);
            RADVisualComponent targetRad = (RADVisualComponent) formDesigner.getMetaComponent(target);
            //p("target rad = " + targetRad);
            RADVisualContainer payloadParentRad = (RADVisualContainer) formDesigner.getMetaComponent(payloadParent);
            //p("payload parent rad = " + payloadParentRad);
            RADVisualContainer targetParentRad = (RADVisualContainer) formDesigner.getMetaComponent(targetParent);
            //p("target parent rad = " + targetParentRad);
            

            //if a toplevel menu dragged next to another toplevel menu
            if(payload instanceof JMenu && payload.getParent() instanceof JMenuBar 
                    && target instanceof JMenu && target.getParent() instanceof JMenuBar) {
                //remove from old spot
                targetParent = (JComponent) target.getParent();
                payloadParent = (JComponent) payload.getParent();
                payloadParentRad = (RADVisualContainer) formDesigner.getMetaComponent(payloadParent);
                targetParentRad = (RADVisualContainer) formDesigner.getMetaComponent(targetParent);
                //p("new payload parent rad = " + payloadParentRad);
            }
            
            //p("=== removing ===");
            //skip if no payload rad, which probably means this is a new component from the palette
            if(payloadRad != null && payloadParentRad != null) {
                //p("=== did a remove ===");
                int index = payloadParentRad.getIndexOf(payloadRad);
                payloadParentRad.remove(payloadRad);
                FormModelEvent fme = formDesigner.getFormModel().fireComponentRemoved(payloadRad, payloadParentRad, index, false);
            }

                
            //if dragged component into a toplevel menu
            if(targetParent == null && target instanceof JMenu && target.getParent() instanceof JMenuBar) {
                //p("=== inserting at end of a toplevel menu ===");
                targetParentRad = (RADVisualContainer) targetRad;
                //add to end of the toplevel menu
                targetParentRad.add(payloadRad, -1);
                targetParentRad.getLayoutSupport().addComponents(new RADVisualComponent[] { payloadRad }, null, -1);
                FormModelEvent fme2 = formDesigner.getFormModel().fireComponentAdded(payloadRad, false);
                return;
            }
            
            // insert if target exists, else the item was removed by dragging out of the menu
            if(targetParentRad != null) {
                //p("=== inserting before drop target component ===");
                int index2 = targetParentRad.getIndexOf(targetRad) + offset;
                //p("index of target = " + index2);
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
        boolean added = creator.addPrecreatedComponent(targetContainer, null);
        return added;
    }
    
    
    // change the look of the component to reflect the newly added state.
    // this mainly means making the foreground color light gray.
    void configureNewComponent(RADComponent item) {
        if(item != null) {
            JComponent c = (JComponent) formDesigner.getComponent(item);
            if(c != null) {
                c.setForeground(Color.LIGHT_GRAY);
            }
        }
    }
    
    
    // change the look of the component to reflect the fully edited state
    private void configureEditedComponent(JComponent c) {
        //p("configuring an edited component");
        if(c.getForeground() == Color.LIGHT_GRAY) {
            c.setForeground(getNormalForeground(c));
        }
    }
    
    void configureEditedComponent(RADComponent c) {
        if(c != null) {
            configureEditedComponent((JComponent)formDesigner.getComponent(c));
        }
    }

    //listens to see if this particular menu has been changed
    private void registerForm(final RADVisualContainer metacomp, final JMenu menu) {
        // don't double register
        if(!formModelListeners.containsKey(metacomp)) {
            FormModelListener fml = new FormModelListener() {
                public void formChanged(FormModelEvent[] events) {
                    p("form changed starting");
                    if (events != null) {
                        for(FormModelEvent evt : events) {
                            
                            p("form model update: " + evt.getChangeType() + " = ");
                            switch(evt.getChangeType()) {
                            case FormModelEvent.COMPONENT_PROPERTY_CHANGED: p("COMPONENT_PROPERTY_CHANGED");break;
                            case FormModelEvent.COMPONENT_ADDED: p("COMPONENT_ADDED");break;
                            case FormModelEvent.COMPONENT_REMOVED: p("COMPONENT_REMOVED");break;
                            case FormModelEvent.COMPONENTS_REORDERED: p("COMPONENTS_REORDERED");break;
                            case FormModelEvent.BINDING_PROPERTY_CHANGED: p("BINDING_PROPERTY_CHANGED");break;
                            case FormModelEvent.FORM_LOADED: p("FORM_LOADED"); break;
                            case FormModelEvent.FORM_TO_BE_CLOSED: p("FORM_TO_BE_CLOSED"); break;
                            default: p("unknown type");
                            }
                            if(evt.getComponent() != null) {
                                p(" " + evt.getComponent().getName());
                            };
                            
                            if(evt.getChangeType() == evt.FORM_TO_BE_CLOSED) {
                                formModelListeners.remove(metacomp);
                                metacomp.getFormModel().addFormModelListener(this);
                                continue;
                            }
                            
                            if(evt.getChangeType() == evt.COMPONENT_PROPERTY_CHANGED) {
                                if("action".equals(evt.getPropertyName())) {
                                    configureEditedComponent(evt.getComponent());
                                }
                            }
                            if(evt.getChangeType() == evt.COMPONENT_PROPERTY_CHANGED || evt.getChangeType() == evt.BINDING_PROPERTY_CHANGED) {
                                if(evt.getContainer() == metacomp || evt.getComponent() == metacomp) {
                                    rebuildOnScreenMenu(metacomp);
                                }
                                updateIcon(evt.getComponent());
                            }
                            
                            if(evt.getChangeType() == evt.COMPONENT_ADDED) {
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
                    p("form changed ending");
                }
            };
            formModelListeners.put(metacomp,fml);
            metacomp.getFormModel().addFormModelListener(fml);
        }
    }
    
    private void rebuildOnScreenMenu(RADVisualContainer menuRAD) {
        if(menuRAD == null) return;
        if(hackedPopupFactory == null) return;
        p("** rebuildOnScreenMenu() called on: " + menuRAD.getName());
        JMenu menu = (JMenu) formDesigner.getComponent(menuRAD);
        if(hackedPopupFactory.containerMap.containsKey(menu)) {
            JPanel popupContainer = hackedPopupFactory.containerMap.get(menu);
            //p("looking over components left");
            for(Component c : popupContainer.getComponents()) {
                //p("found comp: " + c);
                if(c instanceof JMenu) {
                    unconfigureMenu((JMenu)c);
                } else {
                    unconfigureMenuItem((JComponent)c);
                }
            }
            //p("removing all from container");;
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
            p("menu popup not built yet: " + menu.getName());
        }
    }
    
    private void updateIcon(RADComponent rad) {
        p("updating icon for rad: " + rad);
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
                if(!(item.getIcon() instanceof WrapperIcon) && !isTopLevelMenu(item)) {
                    item.setIcon(new WrapperIcon(item.getIcon()));
                }
                
                if(item.getIcon() instanceof WrapperIcon) {
                    ((WrapperIcon)item.getIcon()).setIcon(icon);
                } else { // we should never get here
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
    
    public static boolean isTopLevelMenu(JComponent comp) {
        if(comp == null) return false;
        if(comp instanceof JMenu) {
            if(comp.getParent() instanceof JMenuBar) return true;
        }
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
                if(c != null && isTopLevelMenu(c)) {
                    if(e.getClickCount() > 1) {
                        isEditing = true;
                        configureEditedComponent(c);
                        formDesigner.startInPlaceEditing(rad);
                    } else {
                        openMenu(rad, c);
                        glassLayer.requestFocusInWindow();
                        setSelectedRADComponent(rad);
                        if(e.isPopupTrigger()) {
                            showContextMenu(e.getPoint());
                            return;
                        }
                        if(!dragop.isStarted()) {
                            pressPoint = e.getPoint();
                            pressComp = c;
                            return;
                        }
                    }
                    return;
                }
                if(c instanceof JMenuBar) {
                     setSelectedRADComponent(rad);
                     if(e.isPopupTrigger()) {
                         showContextMenu(e.getPoint());
                         return;
                     }
                     return;
                }
            }

            JComponent c = dragop.getDeepestComponentInPopups(e.getPoint());

            
            if(c == null && !isMenuRelatedRADComponent(rad)) {
                p("not a menu component. going back to handle layer");
                PaletteUtils.clearPaletteSelection();
                hideMenuLayer();
                formDesigner.getHandleLayer().mousePressed(e);
                return;
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
                    configureEditedComponent(c);
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
                        setSelectedRADComponent((RADComponent)formDesigner.getMetaComponent(c));
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
