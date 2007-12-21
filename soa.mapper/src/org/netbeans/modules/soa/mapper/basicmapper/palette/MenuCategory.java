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

package org.netbeans.modules.soa.mapper.basicmapper.palette;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;

import org.openide.util.NbBundle;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteCategory;
import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteManager;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteViewCategory;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteViewItem;

/**
 *
 *
 * @Created on Jun 8, 2004
 * @author sleong
 * @version 1.0
 */
public class MenuCategory
        extends JToggleButton
        implements IPaletteViewCategory {
    
    private static Font FONT_MENUBAR;
    private static Font FONT_MENUITEM;
    private static Color COLOR_MENUBAR_SHADOW;
    private static Color COLOR_MENUITEM_FG;
    private static Color COLOR_MENUITEM_BG;
    private static Color COLOR_MENUITEM_SELECTION_FG;
    private static Color COLOR_MENUITEM_SELECTION_BG;
    
    static {
        FONT_MENUBAR         = UIManager.getFont("MenuBar.font");
        FONT_MENUITEM        = UIManager.getFont("MenuItem.font");
        COLOR_MENUBAR_SHADOW = UIManager.getColor("MenuBar.shadow");
        COLOR_MENUITEM_FG    = UIManager.getColor("MenuItem.foreground");
        COLOR_MENUITEM_BG    = UIManager.getColor("MenuItem.background");
        COLOR_MENUITEM_SELECTION_FG = UIManager.getColor("MenuItem.selectionForeground");
        COLOR_MENUITEM_SELECTION_BG = UIManager.getColor("MenuItem.selectionBackground");
        
        COLOR_MENUBAR_SHADOW = (COLOR_MENUBAR_SHADOW == null) ? Color.GRAY : COLOR_MENUBAR_SHADOW;
        COLOR_MENUITEM_FG = (COLOR_MENUITEM_FG == null) ? Color.BLACK : COLOR_MENUITEM_FG;
        COLOR_MENUITEM_BG = (COLOR_MENUITEM_BG == null) ? Color.WHITE : COLOR_MENUITEM_BG;
        COLOR_MENUITEM_SELECTION_FG = (COLOR_MENUITEM_SELECTION_FG == null) ? Color.WHITE : COLOR_MENUITEM_SELECTION_FG;
        COLOR_MENUITEM_SELECTION_BG = (COLOR_MENUITEM_SELECTION_BG == null) ? Color.LIGHT_GRAY : COLOR_MENUITEM_SELECTION_BG;
    }
    
    private MenuGroup mGroup;
    
    // a flag indicates there is a spearactor in the menu item list;
    private static final Object _SEPARACTOR = new Object();
    
    /**
     * the categroy for this button
     */
    private IPaletteCategory mCategory;
    
    /**
     * the palette manager, the model
     */
    private IPaletteManager mModel;
    
    private List mViewItemList;
    
    private List mMenuItemList;
    
    private ButtonPopupMenu mButtonMenu;
    
    private Component mCurrentFocusedComponent;
    
    private AWTEventListener mFocusLostClickListener;

    private static final Logger LOGGER = Logger.getLogger(BasicMapperPalette.class.getName());
    
    
    public MenuCategory(IPaletteCategory category, IPaletteManager model, Icon icon, ResourceBundle bundle, Class bundleLoader) {
        super(category.getName());
        mCategory = category;
        mModel = model;
        mViewItemList = new LinkedList();
        mMenuItemList = new LinkedList();
        Icon paletteIcon = null;
        String iconRes = (String)category.getCategoryAttribute("Icon");     // NOI18N
        if (iconRes != null) {
            String iconURI = bundle.getString(iconRes);
            URL url = bundleLoader.getResource(iconURI);
            if (url != null) {
                paletteIcon = new ImageIcon(url);
            }
        }
        if (paletteIcon == null) {
            setIconTextGap(1);
            setHorizontalAlignment(AbstractButton.LEFT);
            setHorizontalTextPosition(AbstractButton.LEFT);
            if (icon != null) {
                setIcon(icon);
            } else {
                try {
                    setIcon(new ImageIcon(getClass().getResource("category_10x16.png")));
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Unable to find the default category icon file: category_10x16.png", e);
                }
            }
        } else {
            setIcon(paletteIcon);
        }
        
        // Set accessibility (a11y) stuff
        String a11yDesc = (String) category.getCategoryAttribute("A11yDesc");   // NOI18N
        if (!MapperUtilities.isEmpty(a11yDesc)) {
            a11yDesc = bundle.getString(a11yDesc);
        }
        if (MapperUtilities.isEmpty(a11yDesc)) {
            a11yDesc = MapperUtilities.cutAmpersand(category.getName());
        }
        String a11yName = (String) category.getCategoryAttribute("A11yName");    // NOI18N
        if (!MapperUtilities.isEmpty(a11yName)) {
            a11yName = bundle.getString(a11yName);
        }
        if (MapperUtilities.isEmpty(a11yName)) {
            a11yName = MapperUtilities.cutAmpersand(category.getName());
        }
        getAccessibleContext().setAccessibleDescription(a11yDesc);
        getAccessibleContext().setAccessibleName(a11yName);
        
        addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (isSelected()) {
                    showButtonMenu();
                } else {
                    mButtonMenu.hide();
                }
            }
        });
        setFont(FONT_MENUBAR);
        setContentAreaFilled(false);
        setRolloverEnabled(false);
        setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 12));
        setIconTextGap(4);
    }
    
    public void setGroup(MenuGroup group) {
        mGroup = group;
        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (mGroup != null) {
                    MenuCategory active = mGroup.getActiveMenu();
                    if (active != null && active != MenuCategory.this){
                        setSelected(true);
                    }
                }
            }
        });
    }
    
    public void addNotify() {
        super.addNotify();
        Window parent = SwingUtilities.getWindowAncestor(this);
        if (mButtonMenu == null) {
            createNewButtonPopupMenu(parent);
        } else if (mButtonMenu.getOwner() != parent) {
            createNewButtonPopupMenu(parent);
        }
    }
    
    private void createNewButtonPopupMenu(Window parent) {
        if (mButtonMenu != null) {
            mButtonMenu.hide();
            mButtonMenu.cleanup();
            mButtonMenu.dispose();
        }
        mButtonMenu = new ButtonPopupMenu(parent);
        Iterator menuItemIter = mMenuItemList.iterator();
        while (menuItemIter.hasNext()) {
            Object menuItem = menuItemIter.next();
            if (menuItem == _SEPARACTOR) {
                mButtonMenu.addSeparator();
            } else {
                mButtonMenu.addButton((AbstractButton) menuItem);
            }
        }
    }
    
    public IPaletteCategory getPaletteCategory() {
        return mCategory;
    }
    
    public void showButtonMenu() {
        if (mButtonMenu == null) {
            throw new IllegalStateException(
                    "Can't show the menu yet because the MenuButton has not been displayed to the screen yet");
        }
        
        mButtonMenu.pack();
        
        // set the location of mButtonMenu
        setMenuLocation();
        
        Toolkit.getDefaultToolkit().addAWTEventListener(getFocusLostClickListener(), AWTEvent.MOUSE_EVENT_MASK);
        
        mButtonMenu.show();
    }
    
    // When the user clicks the mouse on a different window (including the main
    // parent window), our menu should close.
    private AWTEventListener getFocusLostClickListener() {
        if (mFocusLostClickListener == null) {
            mFocusLostClickListener = new AWTEventListener() {
                public void eventDispatched(AWTEvent event) {
                    if (mButtonMenu == null || !mButtonMenu.isVisible()) {
                        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                        return;
                    }
                    Object source = event.getSource();
                    if (
                            ((MouseEvent) event).getClickCount() > 0 &&
                            source instanceof Component && 
                            ! (source instanceof MenuCategory)) {
                        Window w = SwingUtilities.windowForComponent((Component) source);
                        if (w != mButtonMenu) {
                            MenuCategory.this.setSelected(false);
                        }
                    }
                }
            };
        }
        return mFocusLostClickListener;
    }
    
    // Set the location of the menu such that it opens up above the
    // category if there is not enough room to open below the category.
    private void setMenuLocation() {
        Dimension windowSize = mButtonMenu.getSize();
        
        // When obtaining the screen size, account for any screen insets,
        // such as the Windows Taskbar.
        GraphicsConfiguration gc =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRect = new Rectangle(screenSize.width, screenSize.height - insets.bottom);
        
        Point pt = getLocationOnScreen();
        Rectangle underRect = new Rectangle(pt.x, pt.y + getSize().height, windowSize.width, windowSize.height);
        if (screenRect.contains(underRect)) {
            mButtonMenu.setLocation(underRect.x, underRect.y);
        } else {
            mButtonMenu.setLocation(pt.x, pt.y - windowSize.height);
        }
    }
    
    public void hideButtonMenu() {
        setSelected(false);
    }
    
    /**
     * Return the Java AWT component as the viewiable object of this palette
     * view item.
     *
     * @return   the Java AWT component as the viewiable object of this palette
     *      view item.
     */
    public Component getViewComponent() {
        return this;
    }
    
    /**
     * Return the palette item in another form of object repersentation
     *
     * @return   the palette item in another form of object repersentation
     */
    public Object getItemObject() {
        return mCategory;
    }
    
    /**
     * Return null.
     *
     * @return   no drag and drop operation, alwayas return null.
     */
    public Object getTransferableObject() {
        return null;
    }
    
    /**
     * Set the transferable object for drag and drop opertaion, this methoid is
     * not applicable to this button.
     *
     * @param obj  the transferable object
     */
    public void setTransferableObject(Object obj) {
    }
    
    /**
     * Add a palette view item to this category
     *
     * @param item the new palette view item to add to this category
     */
    public void addViewItem(IPaletteViewItem item) {
        if (mViewItemList.contains(item)) {
            return;
        }
        mViewItemList.add(item);
        mMenuItemList.add(item.getViewComponent());
        if (mButtonMenu != null) {
            mButtonMenu.addButton((AbstractButton)item);
        }
    }
    
    /**
     * Insert a palette view item to this category.
     *
     * @param item  the new palette view item to add to this category
     * @param index the position of the new palette view item.
     */
    public void insertViewItem(IPaletteViewItem item, int index) {
        if (mViewItemList.contains(item)) {
            return;
        }
        mViewItemList.add(index, item);
        mMenuItemList.add(index, item.getViewComponent());
        if (mButtonMenu != null) {
            mButtonMenu.addButton((AbstractButton) item.getViewComponent(), index);
        }
    }
    
    /**
     * Remove the palette view item from the specified index.
     *
     * @param index the index of the palette view item to be removed.
     */
    public void removeViewItem(int index) {
        mViewItemList.remove(index);
        mMenuItemList.remove(index);
        if (mButtonMenu != null) {
            mButtonMenu.removeButton(index);
        }
    }
    
    /**
     * Remove the specified palette view item from this category.
     *
     * @param item  the palette view item to be removed from this category.
     */
    public void removeViewItem(IPaletteViewItem item) {
        mViewItemList.remove(item);
        mMenuItemList.remove(item.getViewComponent());
        if (mButtonMenu != null) {
            mButtonMenu.removeButton((AbstractButton) item.getViewComponent());
        }
    }
    
    /**
     * Return a collection of all the view items from this category.
     *
     * @return a collection of all the view items from this category.
     */
    public Collection getViewItems() {
        return Collections.unmodifiableCollection(mViewItemList);
    }
    
    /**
     * Return the palette view item from the specified index.
     *
     * @param i the index to be search
     *
     * @return the palette view item from the specified index.
     */
    public IPaletteViewItem getViewItem(int i) {
        return (IPaletteViewItem)mViewItemList.get(i);
    }
    
    /**
     * Return the position of the specified palette view item of a category.
     *
     * @param item the palette view item to be search on.
     *
     * @return  the position of the specified palette view item of a category.
     */
    public int getViewItemIndex(IPaletteViewItem item) {
        return mViewItemList.indexOf(item);
    }
    
    /**
     * Return the number of view items in this category.
     *
     * @return the number of view items in this category.
     */
    public int getViewItemCount() {
        return mViewItemList.size();
    }
    
    public void addSeparator() {
        mMenuItemList.add(_SEPARACTOR);
        if (mButtonMenu != null) {
            mButtonMenu.addSeparator();
        }
    }
    
    public void setEnabled(boolean enable) {
        if (this.isEnabled() == enable) {
            return;
        }
        super.setEnabled(enable);
        for (int i = 0; i < getComponentCount(); i++) {
            Component comp = getComponent(i);
            comp.setEnabled(enable);
        }
    }
    
    private void init() {
        setHorizontalAlignment(AbstractButton.LEFT);
        setVerticalAlignment(AbstractButton.CENTER);
        setHorizontalTextPosition(AbstractButton.RIGHT);
    }
    
    
    
    private class MenuButtonList extends JPanel {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (mCurrentFocusedComponent != null) {
                g.setColor(COLOR_MENUITEM_SELECTION_BG);
                g.fillRect(0, mCurrentFocusedComponent.getY(), getWidth(), mCurrentFocusedComponent.getHeight());
            }
        }
    }
    
    
    
    private class ButtonPopupMenu extends JWindow {
        private JPanel mButtonList;
        private int mButtonHeight;
        private ComponentListener mComponentListener;
        
        public ButtonPopupMenu(Window parent) {
            super(parent);
            mButtonList = new MenuButtonList();
            mButtonList.setBackground(COLOR_MENUITEM_BG);
            mButtonList.setBorder(BorderFactory.createLineBorder(COLOR_MENUBAR_SHADOW));
            mButtonList.setLayout(new GridBagLayout());
            
            // The rollover border does not display correctly. It overlaps the
            // button text, which looks bad. So, we expand the button size by
            // giving it a large enough border. This border has a background color
            // to match the button background color. This allows the rollover rect
            // to size around the text correctly.
            Color background = mButtonList.getBackground();
            
            mButtonList.addFocusListener(mFocusListener);
            addFocusListener(mFocusListener);
            
            getContentPane().add(mButtonList);
            registerDefaultKeyActionBindings();
            
            mComponentListener = new ComponentListener() {
                public void componentHidden(ComponentEvent e) {
                    MenuCategory.this.setSelected(false);
                }
                public void componentMoved(ComponentEvent e) {
                    MenuCategory.this.setSelected(false);
                }
                public void componentResized(ComponentEvent e) {
                    MenuCategory.this.setSelected(false);
                }
                public void componentShown(ComponentEvent e) {
                    MenuCategory.this.setSelected(false);
                }
            };
            getOwner().addComponentListener(mComponentListener);
            
            // removed setup button, all buttons to be visible by default
//            addSeparator();
            AbstractButton setupButton = new JButton(NbBundle.getMessage(
                    this.getClass(), "LBL_Category_Configuration_Item"));
//            addButton(1, setupButton);
            mButtonHeight = setupButton.getPreferredSize().height + 2;
//            setupButton.addActionListener(new ActionListener () {
//                public void actionPerformed (ActionEvent e) {
//                    mModel.showDialog(mCategory);
//                }
//            });
        }
        
        public void hide() {
            super.hide();
            if ((mGroup != null)
                    && (mGroup.getActiveMenu() == MenuCategory.this)) {
                mGroup.setActiveMenu(null);
            }
        }
        
        private ActionListener mActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MenuCategory.this.setSelected(false);
            }
        };
        
        private FocusListener mFocusListener = new FocusListener() {
            
            public void focusGained(FocusEvent e) {
                Object source = e.getSource();
                KeyboardFocusManager fMgr = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                Component c = fMgr.getFocusOwner();
                if (c != null && c instanceof JButton) {
                    if (mCurrentFocusedComponent != c && mCurrentFocusedComponent != null) {
                        mCurrentFocusedComponent.setForeground(COLOR_MENUITEM_FG);
                    }
                    mCurrentFocusedComponent = c;
                    mCurrentFocusedComponent.setForeground(COLOR_MENUITEM_SELECTION_FG);
                }
                repaint();
            }
            
            public void focusLost(FocusEvent e) {
                repaint();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Component owner =
                                KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                        
                        Component win = SwingUtilities.getRoot(owner);
                        if (win != ButtonPopupMenu.this) {
                            MenuCategory.this.setSelected(false);
                        }
                    }
                });
            }
        };
        
        private Action mNextFocusAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager fMgr = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                Component owner = fMgr.getFocusOwner();
                if (owner == null) {
                    repaint();
                    return;
                }
                for (int i = 0; i < mButtonList.getComponentCount(); i++) {
                    if (owner == mButtonList.getComponent(i)) {
                        fMgr.focusNextComponent();
                        return;
                    }
                }
            }
        };
        
        private Action mPrevFocusAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager fMgr = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                Component owner = fMgr.getFocusOwner();
                if (owner == null) {
                    repaint();
                    return;
                }
                for (int i = 0; i < mButtonList.getComponentCount(); i++) {
                    if (owner == mButtonList.getComponent(i)) {
                        fMgr.focusPreviousComponent();
                        return;
                    }
                }
            }
        };
        
        private Action mNextMenuAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Component parent = MenuCategory.this.getParent();
                if (parent instanceof MenuGroup) {
                    MenuGroup parentGroup = (MenuGroup) parent;
                    MenuCategory next = parentGroup.getNextMenu(MenuCategory.this);
                    if (next != null) {
                        setSelected(false);
                        next.setSelected(true);
                    }
                }
            }
        };
        
        private Action mPrevMenuAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Component parent = MenuCategory.this.getParent();
                if (parent instanceof MenuGroup) {
                    MenuGroup parentGroup = (MenuGroup) parent;
                    MenuCategory prev = parentGroup.getPreviousMenu(MenuCategory.this);
                    if (prev != null) {
                        setSelected(false);
                        prev.setSelected(true);
                    }
                }
            }
        };
        
        private Action mHideAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                MenuCategory.this.setSelected(false);
            }
        };
        
        private Action mDefaultButtonAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager fMgr =
                        KeyboardFocusManager.getCurrentKeyboardFocusManager();
                Component owner = fMgr.getFocusOwner();
                if (owner == null) {
                    return;
                }
                for (int i = 0; i < mButtonList.getComponentCount(); i++) {
                    Component c = mButtonList.getComponent(i);;
                    if (owner == c) {
                        ((AbstractButton)c).doClick(0);
                        return;
                    }
                }
            }
        };
        
        private MouseMotionListener mButtonFocusListener = new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                if (e.getSource() instanceof JButton) {
                    Component c = (Component) e.getSource();
                    if (mCurrentFocusedComponent != c && mCurrentFocusedComponent != null) {
                        mCurrentFocusedComponent.setForeground(COLOR_MENUITEM_FG);
                    }
                    mCurrentFocusedComponent = c;
                    mCurrentFocusedComponent.setForeground(COLOR_MENUITEM_SELECTION_FG);
                    c.requestFocus();
                }
            }
        };
        
        public void show() {
            super.show();
            if (mButtonList.getComponentCount() > 0) {
                mButtonList.getComponent(0).requestFocus();
            }
            if (mGroup != null) {
                mGroup.setActiveMenu(MenuCategory.this);
            }
        }
        
        public void addButton(AbstractButton b) {
            addButton(b, mButtonList.getComponentCount() - 2);
        }
        
        public void addButton(AbstractButton b, int index) {
            if (index < 0) {
                index = 0;
            } else if (index > mButtonList.getComponentCount() - 2) {
                throw new IndexOutOfBoundsException(
                        "component count: " + (mButtonList.getComponentCount() - 2) + " insert index: " + index);
            }
            addButton(index, b);
        }
        
        private void addButton(int y, AbstractButton b) {
            initButton(b);
            addComponent(y, b);
            if (mButtonHeight > 0) {
                // We size all buttons to have the same height.
                // We also add an extra amount to the width so that the
                // rollover border has enough room to draw itself.
                Dimension bSize = new Dimension(b.getPreferredSize().width + 2, mButtonHeight);
                b.setPreferredSize(bSize);
                b.setMinimumSize(bSize);
            }
        }
        
        public void removeButton(AbstractButton b) {
            mButtonList.remove(b);
            unInitButton(b);
        }
        
        public void removeButton(int index) {
            AbstractButton b = (AbstractButton) mButtonList.getComponent(index);
            mButtonList.remove(index);
            unInitButton(b);
        }
        
        public void addSeparator() {
            Component sepa = new JSeparator(JSeparator.HORIZONTAL);
            addComponent(mButtonList.getComponentCount() - 1, sepa);
        }
        
        public void removeAll() {
            int lastComponentIndex = mButtonList.getComponentCount() - 1;
            while (lastComponentIndex >= 0) {
                if (mButtonList.getComponent(lastComponentIndex) instanceof AbstractButton) {
                    removeButton(lastComponentIndex--);
                } else {
                    mButtonList.remove(lastComponentIndex--);
                }
            }
        }
        
        private void addComponent(int index, Component b) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = GridBagConstraints.RELATIVE;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.BOTH;
            mButtonList.add(b, c, index);
        }
        
        private void initButton(AbstractButton b) {
            b.setFont(FONT_MENUITEM);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.addFocusListener(mFocusListener);
            b.addMouseMotionListener(mButtonFocusListener);
            b.addActionListener(mActionListener);
        }
        
        private void unInitButton(AbstractButton b) {
            b.removeFocusListener(mFocusListener);
            b.removeMouseMotionListener(mButtonFocusListener);
            b.removeActionListener(mActionListener);
        }
        
        private void registerDefaultKeyActionBindings() {
            KeyStroke arrowUp = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
            getRootPane().getActionMap().put("up", mPrevFocusAction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(arrowUp,"up");
            
            KeyStroke arrowDown = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
            getRootPane().getActionMap().put("down", mNextFocusAction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(arrowDown,"down");
            
            KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            getRootPane().getActionMap().put("esc", mHideAction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc,"esc");
            
            KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
            getRootPane().getActionMap().put("enter", mDefaultButtonAction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter,"enter");
            
            KeyStroke arrowRight = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
            getRootPane().getActionMap().put("right", mNextMenuAction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(arrowRight, "right");
            
            KeyStroke arrowLeft = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
            getRootPane().getActionMap().put("left", mPrevMenuAction);
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(arrowLeft, "left");
            
        }
        
        public void cleanup() {
            if (mButtonList != null) {
                removeAll();
                getOwner().removeComponentListener(mComponentListener);
                mButtonList.removeFocusListener(mFocusListener);
                mButtonList = null;
                mPrevFocusAction = null;
                mNextFocusAction = null;
                mHideAction = null;
                mDefaultButtonAction = null;
                mNextMenuAction = null;
                mPrevMenuAction = null;
            }
        }
    }
    
    public void hide() {
        super.hide();
        Toolkit.getDefaultToolkit().removeAWTEventListener(getFocusLostClickListener());
    }
    
    public void close() {
        if (mButtonMenu != null) {
            mButtonMenu.hide();
            mButtonMenu.cleanup();
            mButtonMenu.dispose();
            mButtonMenu = null;
            mGroup = null;
        }
    }
    
    Color mDisabledTextColor = null;
    
    // Draw a small down arrow over the right margin of the button
    public void paint(Graphics g) {
        super.paint(g);
        AbstractButton b = (AbstractButton) this;
        Dimension size = b.getSize();
        Rectangle viewRect = new Rectangle(size);
        /*
         * XXXXX
         *  XXX
         *   X
         */
        
        int px = viewRect.x + viewRect.width - 10;
        int py = viewRect.y + (viewRect.height)/2;
        Polygon p = new Polygon();
        int cx = 4;
        while (cx >= 0) {
            p.addPoint(px, py);
            p.addPoint(px + cx, py);
            py++;
            px++;
            cx -= 2;
        }
        Color color = Color.black;
        if (!b.isEnabled()) {
            if (mDisabledTextColor == null) {
                mDisabledTextColor = UIManager.getColor("Button.disabledText");
            }
            color = mDisabledTextColor;
        }
        g.setColor(color);
        g.drawPolygon(p);
        g.fillPolygon(p);
        
    }
}
