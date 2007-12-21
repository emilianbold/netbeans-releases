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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.openide.util.NbBundle;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteCategory;
import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteItem;
import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteManager;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicAccumulatingMethoidNode;
import org.netbeans.modules.soa.mapper.basicmapper.methoid.BasicMethoidNode;
import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewManager;
import org.netbeans.modules.soa.mapper.common.basicmapper.literal.ILiteralUpdaterFactory;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteView;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteViewCategory;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteViewItem;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteViewObjectFactory;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;

/**
 * <p>
 *
 * Title: </p> BasicMapperPalette <p>
 *
 * Description: </p> BasicMapperPalette provides implemenation of supporting
 * palette manager as its model, and displaying the palette item as an component
 * on its toolbar.<p>
 *
 * @author    Un Seng Leong
 * @created   January 6, 2003
 */

public class BasicMapperPalette
implements IPaletteView, IPaletteViewObjectFactory {

    /**
     * Mapper view manager
     */
    private IBasicViewManager mViewManager;

    /**
     * Palette Manager, the model
     */
    private IPaletteManager mModel;

    /**
     * the palette view object factory of this mapper palette.
     */
    private IPaletteViewObjectFactory mFactory;

    /**
     * the resource bundle for the view items.
     */
    private ResourceBundle mBundle;

    /**
     * the loader to load bundle resources.
     */
    private Class mBundleLoader = BasicMapperPalette.class;

    /**
     * palette category list 
     */
    private List mCategoryList;
    
    private List mCategoryViewList;
    
    /**
     * palette item storage.
     */
    private List mItemList;

    /**
     * icon for popup dialog button.
     */
    private Icon mCategroyIcon;

    /**
     * Flag indicates if request a new methoid to the mapper if user selected an
     * item from the config dialog.
     */
    private boolean mSelectedOnRequest = true;

    /**
     * the log instance
     */
    private static final Logger LOGGER = Logger.getLogger(BasicMapperPalette.class.getName());

    private PropertyChangeListener mPManagerListener = new PaletteManagerListener();
    
    private MenuGroup mMenuGroup = new MenuGroup();
    
    /**
     * Sets up literal info on new field objects created from palette.
     */
    private ILiteralUpdaterFactory mLiteralUpdaterFactory;
    
    private JPanel mPanel;
    private JToolBar mToolBar;
    private Map mPeripheryComponents = new HashMap();
    
    
    /**
     * Constructor for the BasicMapperPalette object
     *
     * @param viewManager  Description of the Parameter
     */
    public BasicMapperPalette(IBasicViewManager viewManager) {
        mToolBar = new JToolBar();
        mToolBar.setFloatable(false);
        
        mViewManager = viewManager;
        mCategoryList = new Vector();
        mCategoryViewList = new ArrayList();
        mItemList = new Vector();
        setFactory(this);
        
        mToolBar.add(mMenuGroup);
        
        mPanel = new JPanel(new BorderLayout(0, 0));
        mPanel.add(mToolBar, BorderLayout.CENTER);
    }

    /**
     * Returns the 0 width and heigh as the minimum size of this toolbar so that
     * mapper can rezie in a spliter.
     *
     * @return   The minimumSize value
     */
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }

    /**
     * Return the view manager of the mapper.
     *
     * @return   the view manager of the mapper.
     */
    public IBasicViewManager getViewManager() {
        return mViewManager;
    }

    /**
     * Set the palette manager for this palette.
     *
     * @param paletteMgr  the palette manager for this palette.
     */
    public void setPaletteManager(IPaletteManager paletteMgr) {
        mModel = paletteMgr;
        if (mModel.isInitialized()) {
            initPaletteItems();
        }

        mModel.addPropertyChangeListener(mPManagerListener);
    }

    /**
     * Return the palette manager for this palette.
     *
     * @return   the palette manager for this palette.
     */
    public IPaletteManager getPaletteManager() {
        return mModel;
    }

    /**
     * Return the resource bundle to be used for the palette items.
     *
     * @return   the resource bundle to be used for the palette items.
     */
    public ResourceBundle getBundle() {
        if (mBundle == null) {
            mBundle = NbBundle.getBundle(this.getClass());
        }

        return mBundle;
    }

    /**
     * Set the resource bundle, and the loader, to be used for the palette item.
     *
     * @param bundle  the resource bundle to be used for the palette item.
     * @param loader  The new bundle loader, if null the default one is used.
     */
    public void setBundle(ResourceBundle bundle, Class loader) {
        mBundle = bundle;
        if (loader != null) {
            mBundleLoader = loader;
        }
    }

    public Component getViewComponent() {
        return mPanel;
    }
    
    public Component getPaletteComponent() {
        return mToolBar;
    }
    
    public Component getPeripheryComponent(Object constraints) {
        return (Component) mPeripheryComponents.get(constraints);
    }
    
    public void setPeripheryComponent(Component component, Object constraints) {
        Component previousComponent = getPeripheryComponent(constraints);
        if (previousComponent != null) {
            mPanel.remove(previousComponent);
        }
        mPeripheryComponents.put(constraints, component);
        mPanel.add(component, constraints);
    }
    
    public Component add(Component c) {
        return mToolBar.add(c, mToolBar.getComponentCount() - 1);
    }
    
    public Component add(Component c, int index) {
        if (index == mToolBar.getComponentCount()) {
            return mToolBar.add(c, index - 1);
        }
        return mToolBar.add(c, index);
    }
    
    public void add (Component c, Object constrains, int index) {
        add(c, index);
    }
    
    /**
     * Add a IPaletteViewItem to this palette view.
     *
     * @param item  the IPaletteViewItem to be added.
     */
    public void addItem(IPaletteViewItem item) {
        synchronized (mItemList) {
            Component comp = item.getViewComponent();
            if (item instanceof MenuCategory) {
                mMenuGroup.addMenu((MenuCategory) item); 
            } else {
                add(comp);
            }
            mItemList.add(item);
        }
    }

    /**
     * Remove a IPaletteViewItem from this palette view.
     *
     * @param item  the IPaletteViewItem to be removed.
     */
    public void removeItem(IPaletteViewItem item) {
        synchronized (mItemList) {
            mItemList.remove(item);
            Component comp = item.getViewComponent();
            if (comp instanceof MenuCategory) {
                mMenuGroup.removeMenu((MenuCategory) comp);
            } else {
                mToolBar.remove(comp);
            }
        }
    }

    /**
     * Return the number of item in this palette view.
     *
     * @return   the number of item in this palette view.
     */
    public int getItemCount() {
        return getItemCount (mItemList);
    }
    
    /**
     * Return the number of item in this palette view. It checks
     * if an element in the collection is a IPaletteViewCategory, then it
     * recusively calls itself to acumentive the total number of items.   
     * 
     * @param items
     * @return
     */
    private int getItemCount (Collection items) {
        int total = 0;
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            IPaletteViewItem item = (IPaletteViewItem) iter.next();
            if (item instanceof IPaletteViewCategory) {
                total += getItemCount (((IPaletteViewCategory) item).getViewItems());     
            } else {
                total++;   
            }
        }
        return total;        
    }

    /**
     * Return all the items in a collection object. This method recusively 
     * finds all the palette view item within all the category and return 
     * them. 
     *
     * @return   all the items in a collection object.
     */
    public Collection getAllItems() {
        return mItemList;
    }
    
    private Collection getAllItems (Collection items) {
        Set rslt = new HashSet(); 
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            IPaletteViewItem item = (IPaletteViewItem) iter.next();
            if (item instanceof IPaletteViewCategory) {
                rslt.addAll(getAllItems (((IPaletteViewCategory) item).getViewItems()));     
            } else {
                rslt.add(item);   
            }
        }
        return rslt;
    }
    
    /**
     * Return the palette items of this palette. This method will
     * only return the first level of palette view items, and the 
     * category view items. It will not recusively return the 
     * sub-items from the category.
     * 
     * @return  the palette items of this palette. 
     */
    public Collection getItems() {
        return mItemList;
    }

    /**
     * Find and return the palette view item repersenting the specified palette
     * item.
     *
     * @param item  the palette item to be matched.
     * @return      the palette view item repersenting the specified palette
     *      item.
     */
    public IPaletteViewItem findPaletteViewItem(IPaletteItem item) {
        return findPaletteViewItem (mItemList, item);
    }
    
    /**
     * Find and return the palette view item repersenting the specified palette
     * item.
     *
     * @param item  the palette item to be matched.
     * @return      the palette view item repersenting the specified palette
     *      item.
     */
    public IMethoid findMethoid(IPaletteItem item) {
            IPaletteViewItem vItem =  createViewablePaletteItem(item);
            return (IMethoid) vItem.getTransferableObject();
}

    /**
     * Find and return the palette view item repersenting the specified palette
     * item in the specified collections of view items. If a view item in the 
     * collection is a IPaletteViewCategory, it recusively find to the category.
     * 
     * @param viewItems the collection of the view items to be search on.
     * @param item      the item to be find
     * @return  the palette view item repersenting the specified palette
     * item 
     */
    private IPaletteViewItem findPaletteViewItem (Collection viewItems, IPaletteItem item) {
        Iterator iter = viewItems.iterator();
        while (iter.hasNext()) {
            IPaletteViewItem viewItem = (IPaletteViewItem) iter.next(); 
            if (viewItem.getItemObject() == item) {
                return viewItem;
            }

            if (viewItem instanceof IPaletteViewCategory) {
                viewItem = findPaletteViewItem (
                    ((IPaletteViewCategory) viewItem).getViewItems(), item);
                if (viewItem != null) {
                    return viewItem;
                }
            }
        }
        return null;
    }

    /**
     * Return a newly create Palette view item by the specified palette item
     * from Palette manager.
     *
     * @param item  the palette item model
     * @return      a newly create Palette view item by the specified palette
     *      item from Palette manager.
     */
    public IPaletteViewItem createViewablePaletteItem(IPaletteItem item) {
        return new BasicViewItem(item, getBundle(), mLiteralUpdaterFactory);
    }

    /**
     * Return a newly create palette category view item by the specified palette
     * category item from Palette manager.
     *
     * @param category  Description of the Parameter
     * @return          a newly create palette category view item by the
     *      specified palette category item from Palette manager.
     */
    public IPaletteViewItem createViewablePaletteCategoryItem(IPaletteCategory category) {
        if (mCategroyIcon == null) {
            String iconURI = "down.png";
            try {
                mCategroyIcon = new ImageIcon(BasicMapperPalette.class.getResource(iconURI));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unable to load category button icon: " + iconURI, e);
            }
        }
        return new MenuCategory(
                category, 
                mModel, 
                mCategroyIcon, 
                getBundle(), 
                mBundleLoader);
    }

    /**
     * Return a palette view item that performs auto layout for this mapper.
     *
     * @return   a palette view item that performs auto layout for this mapper.
     */
    public IPaletteViewItem createAutoLayoutItem() {
        return new AutoLayoutButton(this);
    }

    /**
     * Return a palette view item that performs expand all group nodes for this
     * mapper.
     *
     * @return   a palette view item that performs expand all group nodes for
     *      this mapper.
     */
    public IPaletteViewItem createExpandAllNodesItem() {
        return new ExpandAllButton(this);
    }

    /**
     * Return a palette view item that performs collapse all group nodes for
     * this mapper.
     *
     * @return   a palette view item that performs collapse all group nodes for
     *      this mapper.
     */
    public IPaletteViewItem createCollapseAllNodesItem() {
        return new CollapseAllButton(this);
    }

    /**
     * Return a palette view item that deletes the selected canvas nodes.
     *
     * @return   a palette view item that performs expand all group nodes for
     *      this mapper.
     */
    public IPaletteViewItem createDeleteSelectedNodesItem() {
        return new DeleteSelectionButton(this);
    }
    
    /**
     * Add a sparator in between palette items.
     */
    public void addItemSeparator() {
        mToolBar.addSeparator();
    }

    /**
     * Set the palette view object factory to create palette view item.
     *
     * @param factory  the factory of this palette view.
     */
    public void setFactory(IPaletteViewObjectFactory factory) {
        mFactory = factory;
    }

    /**
     * Return the palette view object factory to create palette view item.
     *
     * @return   the palette view object factory to create palette view item.
     */
    public IPaletteViewObjectFactory getFactory() {
        return mFactory;
    }

    /**
     * Initialize palette items available from palette maanger. Create component
     * for each palette item and added to this toolbar.
     */
    public void initPaletteItems() {
        IPaletteManager paletteManager = getPaletteManager();
        IPaletteCategory categories[] = paletteManager.getAllCategories();

        IPaletteItem paletteItems[] = null;
        boolean sep = false;
        for (int i = 0; i < categories.length; i++) {
            // skip the category that has been initialized
            if (mCategoryList.contains(categories[i])) {
                continue;
            }

            IPaletteViewItem categoryView =
                getFactory().createViewablePaletteCategoryItem(categories[i]);
            if (categoryView instanceof IPaletteViewCategory) {
                addItem(categoryView);
                mCategoryViewList.add(categoryView);
            }
            categoryView.getViewComponent().setEnabled(mToolBar.isEnabled());
            paletteItems = paletteManager.getCategoryItems(categories[i]);
            if (paletteItems == null) {
                continue;
            }
            int[] selectedIndex = paletteManager.getCategorySelectedItemIndices(categories[i]);
            for (int j = 0; j < paletteItems.length; j++) {
                IPaletteViewItem viewItem = getFactory().
                    createViewablePaletteItem(paletteItems[j]);
                if (categoryView instanceof IPaletteViewCategory) {
                    ((IPaletteViewCategory) categoryView).addViewItem(viewItem);
                } else {
                    addItem(viewItem);
                }
                viewItem.getViewComponent().setVisible(false);
                // check if the viewItem is selected.
                for (int k = 0; k < selectedIndex.length; k++) {
                    if (selectedIndex[k] == j) {
                        viewItem.getViewComponent().setVisible(true);
                    }
                }
                ((Component) viewItem).setEnabled(mToolBar.isEnabled());
                if (viewItem instanceof AbstractButton) {
                    ((AbstractButton) viewItem).addActionListener (
                        new RequestNewMethoidAction((IMethoid) viewItem.getTransferableObject()));  
                }
            }
            if (!(categoryView instanceof IPaletteViewCategory)) {
                addItem(categoryView);
            }
        }
        
        // Activate mnemonics in components
        MapperUtilities.activateInlineMnemonics(mToolBar);
    }

    /**
     * Return true if the user selected a palette item from dialog should
     * request the coorspending new Methoid in the mapper, false otherwise.
     *
     * @return   true if the user selected a palette item from dialog should
     *      request the coorspending new Methoid in the mapper, false otherwise.
     */
    public boolean getSelectedOnRequest() {
        return mSelectedOnRequest;
    }

    /**
     * Sets if the user selected a palette item from dialog should request the
     * coorspending new Methoid in the mapper.
     *
     * @param selectedOnRequest  set to true if the user selected a palette item
     *      from dialog should request the coorspending new Methoid in the
     *      mapper, false otherwise.
     */
    public void setSelectedOnRequest(boolean selectedOnRequest) {
        mSelectedOnRequest = selectedOnRequest;
    }

    /**
     * Seleted the specified palette item. This method will send a new group
     * node event to mapper view manager to create the methoid of that palette
     * item.
     *
     * @param item  the palette item to be selected.
     */
    public void selectedItem(IPaletteItem item) {
        if (!mToolBar.isVisible() || !mToolBar.isEnabled() || !getSelectedOnRequest()) {
            return;
        }

        IPaletteViewItem viewItem = findPaletteViewItem(item);

        if (viewItem == null) {
            return;
        }
        /*
         * PENDING -- this causing thread dead lock in the PaletteDialog box when
         * the seleted methoid requires another dialog.
         * requestNewMethoidNode(
         * (IMethoid) ((BasicViewItem) viewItem).getTransferData(
         * ((BasicViewItem) viewItem).getDefaultDataFlavor()));
         */
    }

    /**
     * Uncheck the specified a palette item. This method will hide the palette
     * component of the palette item.
     *
     * @param item  the item has been uncheck in the palette manager config
     *      dialog
     */
    public void uncheckItem(IPaletteItem item) {
        final IPaletteViewItem viewItem = findPaletteViewItem(item);

        if (viewItem == null) {
            return;
        }
        viewItem.getViewComponent().setVisible(false);
    }

    /**
     * Check on a specified palette item. This method will set the visible
     * palette component to be visible.
     *
     * @param item  the specified palette item to be checked.
     */
    public void checkItem(IPaletteItem item) {
        final IPaletteViewItem viewItem = findPaletteViewItem(item);

        if (viewItem == null) {
            return;
        }
        viewItem.getViewComponent().setVisible(true);
    }

    /**
     * Requesting a new methoid node to be create in the mapper. This method
     * sends a mapper event with the specified methoid node to mapper view
     * manager to creat the methoid on the canvas.
     *
     * @param methoid  the specified methoid to be created
     */
    public void requestNewMethoidNode(IMethoid methoid) {
        BasicMethoidNode newMethoidNode = null;
        if (methoid.isAccumulative()) {
            newMethoidNode = new BasicAccumulatingMethoidNode(methoid);
        } else {
            newMethoidNode = new BasicMethoidNode(methoid);
        }
        
        Point defaultLocation = mViewManager.getCanvasView().getCanvas()
            .getDefaultLocationForNewNode(newMethoidNode);

        newMethoidNode.setX(defaultLocation.x);
        newMethoidNode.setY(defaultLocation.y);

        getViewManager().postMapperEvent(
            MapperUtilities.getMapperEvent(
            this,
            newMethoidNode,
            IMapperEvent.REQ_NEW_NODE,
            "PaletteToolBar requesting new methoid: " + newMethoidNode));
    }

    /**
     * Set the toolbar and all the buttons the enable value.
     *
     * @param enable  the enable boolean value
     */
    public void setEnabled(boolean enable) {
        mToolBar.setEnabled(enable);
        for (int i = 0; i < mToolBar.getComponentCount(); i++) {
            mToolBar.getComponent(i).setEnabled(enable);
        }
    }

    public void close() {
       mModel.removePropertyChangeListener(mPManagerListener);
       for (Iterator iter=mCategoryViewList.iterator(); iter.hasNext();) {
           IPaletteViewCategory category = (IPaletteViewCategory) iter.next();
           category.close();
       }
    }
    
    /**
     * Set the factory which handles setting up literal info on
     * field objects created from the palette.
     */
    public void setLiteralUpdaterFactory(ILiteralUpdaterFactory literalUpdaterFactory) {
        mLiteralUpdaterFactory = literalUpdaterFactory;
    }
    

    /**
     * This class listens on the changes of PaletteManager and modified the
     * toolbar and palette items properties accrodingly.
     *
     * @author    sleong
     * @created   December 4, 2002
     */
    private class PaletteManagerListener
         implements PropertyChangeListener {
        /**
         * Listens on palette manager property change event, and provides method
         * calling for each specified event.
         *
         * @param event  the property change event.
         */
        public void propertyChange(PropertyChangeEvent event) {
            LOGGER.log(Level.FINEST, "BasicMapperPalette:" + BasicMapperPalette.this + " hashcode:"
                + BasicMapperPalette.this.hashCode());
            LOGGER.log(Level.FINEST, "IPaletteManager new event: " + event.getPropertyName());

            if (event.getPropertyName().equals(IPaletteManager.PROP_INITIALIZED)) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        // intialize palette items in an invoke-later to allow
                        // time for previous invoke-laters to update our enablement
                        // state before we set our enablement state on each button. - josh
                        initPaletteItems();
                    }
                });

            } else if (event.getPropertyName().equals(IPaletteManager.PROP_SELECTEDITEM)) {
                if (event.getNewValue() instanceof IPaletteItem) {
                    selectedItem((IPaletteItem) event.getNewValue());
                }
            } else if (event.getPropertyName().equals(IPaletteManager.PROP_CHECKEDITEM)) {
                if (event.getNewValue() instanceof IPaletteItem) {
                    checkItem((IPaletteItem) event.getNewValue());
                }
            } else if (event.getPropertyName().equals(IPaletteManager.PROP_UNCHECKEDITEM)) {
                if (event.getNewValue() instanceof IPaletteItem) {
                    uncheckItem((IPaletteItem) event.getNewValue());
                }
            }
        }
    }
    
    private class RequestNewMethoidAction implements ActionListener {
        
        private IMethoid mMethoid;
        
        public RequestNewMethoidAction (IMethoid methoid) {
            mMethoid = methoid;
        }
        
        public void actionPerformed (ActionEvent e) {
            if (mToolBar.isEnabled()) {
                requestNewMethoidNode (mMethoid);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }
}
