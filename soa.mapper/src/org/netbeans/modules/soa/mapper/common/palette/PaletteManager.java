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

package org.netbeans.modules.soa.mapper.common.palette;

import java.awt.Frame;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;



import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteCategory;
import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteItem;
import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteManager;

/**
 * The PaletteManager manages Funcotid Palette content.
 *
 * @author Tientien Li
 */
public class PaletteManager
    implements IPaletteManager {

    /** The default Palette Floder name */
    private String mPaletteFolderName = "Palette";        // NOI18N

    // IDs for tasks running in AWT event-queue

//    /** ID of task for adding new categories of palette. */
//    private static final int CATEGORIES_ADDED = 1;
//
//    /** ID of task for removing some categories of palette. */
//    private static final int CATEGORIES_REMOVED = 2;
//
//    /** ID of task for reordering categories of palette. */
//    private static final int CATEGORIES_REORDERED = 3;
//
//    /** ID of task for updating items of specified palette category. */
//    private static final int ITEMS_CHANGED = 4;
//
//    /** ID of task for reordering items of specified palette category. */
//    private static final int ITEMS_REORDERED = 5;
//
//    /** ID of task for refreshing of new switched UI. */
//    private static final int SWITCH_UI = 6;

    //:::::::::::::::::::::::::::::::::::::

    /** PaletteItem which is currently selected (in Add mode). */
    private PaletteItemNode mSelectedPaletteItem = null;

    /** The support for firing property changes */
    private java.beans.PropertyChangeSupport mPropertySupport;

//    /** The PaletteManager listeners */
//    private transient Vector mListeners;

    /** Set of category-nodes that are to be updated (cache) */
//    private java.util.Set mUpdateNodes = new java.util.HashSet();

    /** Listener of each category in the palette */
//    private org.openide.nodes.NodeListener mCategoryNodeListener;

    /** Root palette node - provides palette content (categories and items) */
    private PaletteNode mPaletteNode;

    /** The base component in that component palette is plugged */
    private javax.swing.JComponent mPalette = new javax.swing.JPanel();

    /** palette Panel that is instantiated from this palette class */
    private PalettePanel mPalettePane;

    /** palette Dialog that is instantiated from this palette class */
    private PaletteDialog mPaletteDialog;

    /** All categories that are in Component Palette  */
    private PaletteCategoryNode[] mCategories = null;

    /** All items that are in Component Palette  */
    private PaletteItemNode[][] mItems = null;

    //:::::::::::::::::::::::::::::::::::::

    /** Field Main Frame           */
    private Frame mFrame = null;

    /** Field Main Component           */
    private java.awt.Component mComponent = null;

    /** Field InitState           */
    private boolean mInitState = false;

    /** Field number of categories           */
    private int mCatNumber = -1;

    /** Field showComponentsNames           */
    private static boolean showComponentsNames = true;    // false;

    /** Property name of the showComponentsNames property */
    public static final String PROP_SHOW_COMPONENTS_NAMES =
        "showComponentsNames";                            // NOI18N
    /**
     * The log4j logger
     */
    private static Logger mLogger =
            Logger.getLogger(PaletteManager.class.getName());

    /**
     * Create new instance of PaletteManager
     *
     * @param folderName the name of palette folder
     * @param palNode the root node the the palette folder
     */
    public PaletteManager() {
    }

    /**
     * Set the folder of this PaletteManager. Everything begins here.
     *
     * @param folderName the name of palette folder
     */
    public void setFolder (String folderName) {
        t("<init>-BEG");

        mPaletteFolderName = folderName;
        mPropertySupport    = new java.beans.PropertyChangeSupport(this);
        mPaletteNode        = new PaletteNode(folderName);
        Integer cn = (Integer) mPaletteNode.getNodeAttribute("TotalCategories");
        if (cn != null) {
            mCatNumber = cn.intValue();
        }
//        createListeners4PaletteNodes();
//        mPaletteNode.getChildren().getNodes(true);
        mPalettePane = new PalettePanel(this);
        switchUI();
        
//        runInEventQueue(SWITCH_UI, null);
        t("<init>-END");
    }

    /**
     * get the Palette Root Node
     *
     *
     * @return the palette root node
     *
     */
    public PaletteNode getPaletteRootNode() {
        return mPaletteNode;
    }

    // IPaletteManager Interface methods....
    //-------------------------------------------------------------------------

    /**
     * is the palette Initialized
     *
     *
     * @return true if initialization complete
     *
     */
    public boolean isInitialized() {
        return mInitState;
    }

    /**
     * get palette Categories from the palette folder
     *
     *
     */
    private void getCategories() {
        t("GetCategory: "+mCategories);

        if (mCategories != null) {
            return;
        }

        int     runs = 0;
        boolean done = true;

        Node[] categories = mPaletteNode.getCategoryNodes();
        t(runs+" GetCategory: got "+categories.length+", Total "+mCatNumber);

        if ((mCatNumber > 0) && (categories.length < mCatNumber)) {
            return;
        }

        if (categories.length == 0) {
            done = false;
        } else {

            ArrayList list = new ArrayList(categories.length);
            mItems = new PaletteItemNode[categories.length][];
            
            for (int i = 0; i < categories.length; i++) {
                if (categories[i] instanceof PaletteCategoryNode) {
                    list.add(categories[i]);
                    
                    //load paletteItemNode
                    PaletteCategoryNode pcn = (PaletteCategoryNode) categories[i];
                    mItems[i] = (PaletteItemNode[]) pcn.getItemNodes();
                }
            }

            mCategories =
                (PaletteCategoryNode[]) list
                    .toArray(new PaletteCategoryNode[list.size()]);
            
        }

    }

    /**
     * update the selected palette category Items
     *
     *
     * @param sel the selected category
     *
     */
//    public void updateItems(int sel) {
//        Node[] items = mCategories[sel].getChildren().getNodes();
//        ArrayList list2 = new ArrayList(items.length);
//
//        for (int i = 0; i < items.length; i++) {
//            if (items[i] instanceof PaletteItemNode) {
//                list2.add(items[i]);
//            }
//        }
//
//        t("updateCategoriesNodes[" + sel + "]: "
//              + items.length + " -> " + list2.size());
//
//        mItems[sel] =
//            (PaletteItemNode[]) list2
//                .toArray(new PaletteItemNode[list2.size()]);
//    }

    /**
     * get All palette Categories
     *
     *
     * @return the list of all palette categories
     *
     */
    public IPaletteCategory[] getAllCategories() {

        if (mCategories == null) {
            getCategories();
        }

        return (IPaletteCategory[]) mCategories;
    }

    /**
     * get a Selected palette Category
     *
     *
     * @return a selected palette category
     *
     */
    public IPaletteCategory getSelectedCategory() {
        return (IPaletteCategory) mPalettePane.getSelectedCategory();
    }

    /**
     * get palette items for a selected Category
     *
     *
     * @param category the selected category
     *
     * @return the list of all palette items in the category
     *
     */
    public IPaletteItem[] getCategoryItems(IPaletteCategory category) {

        if (category == null) {
            return null;
        }

        int sel = mPalettePane.getCategoryIndex(category.getName());

        if (sel < 0) {
            return null;
        }

        if (mItems[sel] == null) {
            return null;
        }

        return (IPaletteItem[]) mItems[sel];
    }

    /**
     * get the Selected Item Indices from a palette category
     *
     *
     * @param category the palette category
     *
     * @return the list of selected item indices
     *
     */
    public int[] getCategorySelectedItemIndices(IPaletteCategory category) {
        return mPalettePane
            .getCategorySelectedItemIndices(category.getName());
    }

    /**
     *  show the palette pop up Dialog
     *
     *
     */
    public void showDialog() {
        if (!isInitialized()) {
            // mPalettePane.buildTabbedPane();
            mLogger.fine("PaletteManager is initializing... Please wait. ");
            return;
        }
        if (mPaletteDialog == null) {
            if (mFrame == null) {
                mFrame = setFrame();
            }
            mPaletteDialog = new PaletteDialog(mFrame, mPalettePane);
        }
        if (mPaletteDialog != null) {
            mPaletteDialog.showDialog();
        }
    }

    /**
     *  show the palette pop up Dialog
     *
     *
     * @param comp the associated component
     *
     */
    public void showDialog(java.awt.Component comp) {
        if (!isInitialized()) {
            // mPalettePane.buildTabbedPane();
            mLogger.fine("PaletteManager is initializing... Please wait. ");
            return;
        }
        if (mPaletteDialog == null) {
            if (mFrame == null) {
                mFrame = setFrame();
            }
            mPaletteDialog = new PaletteDialog(mFrame, mPalettePane);
        }
        if (mPaletteDialog != null) {
            mPaletteDialog.showDialog(comp);
        }
    }

    /**
     *  show the palette pop up Dialog
     *
     *
     * @param category the selected category
     *
     */
    public void showDialog(IPaletteCategory category) {
        if (!isInitialized()) {
            // mPalettePane.buildTabbedPane();
            mLogger.fine("PaletteManager is initializing... Please wait. ");
            return;
        }
        if (mPaletteDialog == null) {
            if (mFrame == null) {
                mFrame = setFrame();
            }
            mPaletteDialog = new PaletteDialog(mFrame, mPalettePane);
        }
        if (mPaletteDialog != null) {
            mPalettePane.setSelectedCategory(category.getName());
            mPaletteDialog.showDialog();
        }
    }

    /**
     *  set the current application Frame.
     *
     * @param c   the application component
     */
    public void setFrame(java.awt.Component c) {
        mComponent = c;
        mFrame = null;
        mPaletteDialog = null;
    }

    /**
     * Find and return the current application frame.
     *
     * @return the current applicaiton frame
     */
    private Frame setFrame() {
        java.awt.Component component = mComponent;
        Frame aFrame =
                org.openide.windows.WindowManager.getDefault().getMainWindow();

        if (mComponent == null) {
            return aFrame;
        }
        while (!(component instanceof Frame)) {
            component = component.getParent();
            if (component == null) {
                return aFrame;
            }
        }
        return (Frame) component;
    }

    /**
     * select All items in a category
     *
     *
     * @param category the selected category
     *
     */
    public void selectAll(IPaletteCategory category) {
        mPalettePane.selectAll(category.getName());
    }

    /**
     * clear All selected items within a category
     *
     *
     * @param category the category
     *
     */
    public void clearAll(IPaletteCategory category) {
        mPalettePane.clearAll(category.getName());
    }

    /**
     * select an item within a category
     *
     *
     * @param category the category
     * @param item the selected item
     *
     */
    public void selectItem(IPaletteCategory category, IPaletteItem item) {
        mPalettePane.selectItem(category.getName(), (PaletteItemNode) item);
    }

    /**
     * clear a selcted Item within a category
     *
     *
     * @param category the selected category
     * @param item the selected item
     *
     */
    public void clearItem(IPaletteCategory category, IPaletteItem item) {
        mPalettePane.clearItem(category.getName(), (PaletteItemNode) item);
    }

    //-------------------------------------------------------------------------

    /**
     * Sets if UI should display names of beans with beanicons
     *
     * @param value the show compoent name flag
     */
    public void setShowComponentsNames(boolean value) {

        if (value == showComponentsNames) {
            return;
        }

        showComponentsNames = value;

        /*
        firePropertyChange(PROP_SHOW_COMPONENTS_NAMES,
        new Boolean(!value), new Boolean(value));
         */
    }

    /**
     * Gets if UI should display names of beans with beanicons
     *
     * @return the show component name flag
     */
    public boolean getShowComponentsNames() {

        /* return FormEditor.getFormSettings().getShowComponentsNames(); */
        return showComponentsNames;
    }

    /**
     * Get the root node of the palette.
     *
     * @return the root node
     */
    public Node getRootNode() {
        return mPaletteNode;
    }

    /**
     * Get nodes representing palette categories.
     *
     * @return the list of palette category nodes
     */
    public Node[] getCategoryNodes() {

        if (mCategories == null) {
            getCategories();
        }

        return mCategories;
    }

    /**
     * Get nodes representing palette itmes of given category.
     *
     * @param categoryNode the selected category
     * @return the list of palette items in the category
     */
    public Node[] getItemNodes(Node categoryNode) {

        if (!(categoryNode instanceof PaletteCategoryNode)) {
            return new Node[]{};
        }

        t("getItemNodes: "
          + ((PaletteCategoryNode) categoryNode).getValidItemNodes().length);

        for (int i = 0; i < mCategories.length; i++) {
            if (categoryNode == mCategories[i]) {
                return mItems[i];
            }
        }

        return new Node[]{};
    }

    /**
     * Get the base component in that component palette is plugged
     *
     * @return the base component
     */
    public javax.swing.JComponent getComponent() {

        return mPalette;
    }

    /** Returns the currently selected PaletteNode(that represents a JavaBean to
     * be added to the form) or null for selection mode.
     *
     * @return the currently selected PaletteNode or null for selection mode
     */
    public PaletteItemNode getSelectedItem() {
        return mSelectedPaletteItem;
    }

    /**
     * Sets the current Selected Item.
     *
     * @param newItem The new selected item
     */
    public void setSelectedItem(PaletteItemNode newItem) {

        t("PM:setSelectedItem: " + ((newItem == null) ? "NULL"  // NOI18N
                : MapperUtilities.cutAmpersand(newItem.getName())));

        if (newItem == null) {
            return;
        }

        if (newItem == mSelectedPaletteItem) {
            return;
        }

        PaletteItemNode oldItem = mSelectedPaletteItem;

        mSelectedPaletteItem = newItem;

        mPropertySupport.firePropertyChange(PROP_SELECTEDITEM, oldItem,
                                            newItem);
    }

    /**
     * Sets the current checked or unchecked item
     *
     * @param newItem The new checked item
     * @param ck ture if checked otherwise unchecked
     */
    public void setCheckedItem(PaletteItemNode newItem, boolean ck) {

        t("PM:setCheckedItem: " + ((newItem == null) ? "NULL"   // NOI18N
                : MapperUtilities.cutAmpersand(newItem.getName())));

        if (newItem == null) {
            return;
        }
        PaletteItemNode oldItem = null;
        mPropertySupport.firePropertyChange(
            (ck ? PROP_CHECKEDITEM : PROP_UNCHECKEDITEM), oldItem, newItem);
    }

    /**
     * Sets the current add component or selection mode.
     * @param newState The new add component or null for selection mode
     */
    public void setInitialized(boolean newState) {
        mLogger.fine(">>> PM-initialized: " + newState);
        boolean oldState = mInitState;
        mInitState = newState;
        mPropertySupport.firePropertyChange(
                PROP_INITIALIZED, oldState, newState);
    }

    //:::::::::::::::::::::::::::::::::::::

    /**
     * Get the bundle of whole Component Palette
     *
     * @return the palette resource bundle
     */
    public static java.util.ResourceBundle getBundle() {
        return org.openide.util.NbBundle.getBundle(PaletteManager.class);
    }

    /**
     *  Create palette item from node
     *
     * @param itemNode the palette node
     * @return the palette item created from the node
     */
    public static PaletteItem createPaletteItem(Node itemNode) {

        try {
            return new PaletteItem(itemNode);
        } catch (InstantiationException ex) {
            mLogger.log(Level.FINEST, "createPaletteItem-InstantiationException: ", ex);
        }

        return null;
    }

    /**
     * Switch the UI of Component Palette
     *
     */
    private void switchUI() {

        t("switchUI-BEG");             // NOI18N
        setSelectedItem(null);

        // setMode(PaletteAction.MODE_SELECTION);
        mPalette.removeAll();

        // palette.setLayout(new BoxLayout(palette, BoxLayout.X_AXIS));
        mPalette.setLayout(new java.awt.BorderLayout());
        mPalette.add(mPalettePane);    // component);

        mPalette.revalidate();
        mPalette.repaint();
        t("switchUI-END");             // NOI18N
    }

    //:::::::::::::::::::::::::::::::::::::

//    /**
//     * Create listeners for palette nodes
//     *
//     */
//    private void createListeners4PaletteNodes() {
//
//        t("createListeners4PaletteNodes-BEG");    // NOI18N
//
//        mCategoryNodeListener = new NodeAdapter() {
//
//            public void childrenAdded(NodeMemberEvent evt) {
//                Node node = (Node) evt.getSource();
//                if (node instanceof PaletteCategoryNode) {
//                    updateCategoryInEventQueue(node);
//                }
//            }
//
//            public void childrenRemoved(NodeMemberEvent evt) {
//
//                Node node = (Node) evt.getSource();
//                if (node instanceof PaletteCategoryNode) {
//                    updateCategoryInEventQueue(node);
//                }
//            }
//
//            public void childrenReordered(NodeReorderEvent evt) {
//
//                Node node = (Node) evt.getSource();
//                if (node instanceof PaletteCategoryNode) {
//                    updateCategoryInEventQueue(node);
//                }
//            }
//
//            public void propertyChange(java.beans.PropertyChangeEvent evt) {
//
//                if (Node.PROP_NAME.equals(evt.getPropertyName())
//                    || Node.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
//                    Node node = (Node) evt.getSource();
//                    if (node instanceof PaletteCategoryNode) {
//                        updateCategoryInEventQueue(node);
//                    }
//                }
//            }
//        };
//
//        mPaletteNode.addNodeListener(new NodeAdapter() {
//
//            public void childrenAdded(NodeMemberEvent evt) {
//
//                Node[] nodes = evt.getDelta();
//                evt.getDeltaIndices();    // to compute the indices now
//                for (int i = 0; i < nodes.length; i++) {
//                    if (nodes[i] instanceof PaletteCategoryNode) {
//                        nodes[i].addNodeListener(mCategoryNodeListener);
//                    }
//                }
//
//                runInEventQueue(CATEGORIES_ADDED, evt);
//            }
//
//            public void childrenRemoved(NodeMemberEvent evt) {
//
//                Node[] nodes = evt.getDelta();
//                evt.getDeltaIndices();    // to compute the indices now
//                for (int i = 0; i < nodes.length; i++) {
//                    nodes[i].removeNodeListener(mCategoryNodeListener);
//                }
//
//                runInEventQueue(CATEGORIES_REMOVED, evt);
//            }
//
//            public void childrenReordered(NodeReorderEvent evt) {
//                runInEventQueue(CATEGORIES_REORDERED, null);
//            }
//        });
//
//    }

    //:::::::::::::::::::::::::::::::::::::

    /**
     * Add a property listener
     *
     * @param l the property change listener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        mPropertySupport.addPropertyChangeListener(l);
    }

    /**
     * Remove a property listener
     *
     * @param l the property change listener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        mPropertySupport.removePropertyChangeListener(l);
    }

//    /**
//     * Add a palette manager listener
//     *
//     * @param l the palette manager listener to be added
//     */
//    public void addManagerListener(PaletteManagerListener l) {
//
//        if (mListeners == null) {
//            mListeners = new Vector();
//        }
//
//        mListeners.addElement(l);
//    }

//    /**
//     * Remove a palette manager listener
//     *
//     * @param l the palette manager listener to be removed
//     */
//    public void removeManagerListener(PaletteManagerListener l) {
//
//        if (mListeners != null) {
//            mListeners.removeElement(l);
//        }
//    }

    //:::::::::::::::::::::::::::::::::::::

//    /**
//     * update the palette Category InEvent Queue
//     *
//     *
//     * @param node the palette category node
//     *
//     */
//    private void updateCategoryInEventQueue(Node node) {
//
//        java.util.Set     nodes         = mUpdateNodes;
//        boolean noRefreshTask = mUpdateNodes.size() == 0;
//
//        mUpdateNodes.add(node);
//        t("added category to update: " + node.getName());    // NOI18N
//
//        if (noRefreshTask || (nodes != mUpdateNodes)) {
//            runInEventQueue(ITEMS_CHANGED, null);
//        }
//        // allItems = null;
//    }

//    /**
//     * run a task in the event queue
//     *
//     *
//     * @param toDoTask the task to be performed
//     * @param param task parameters
//     *
//     */
//    private void runInEventQueue(int toDoTask, Object param) {
//        mLogger.fine("placing task, id: " + toDoTask);
////        java.awt.EventQueue.invokeLater(new PaletteTask(toDoTask, param));
//        new PaletteTask(toDoTask, param).run();
//    }

//    /**
//     * The class manages a Palette Task queue
//     *
//     */
//    private class PaletteTask
//        implements Runnable {
//
//        /** Field whatToDo           */
//        private int whatToDo;
//
//        /** Field parameter           */
//        private Object parameter;
//
//        /**
//         * Constructor for a new Palette Task
//         *
//         *
//         * @param toDoTask the task id
//         * @param param parameters
//         *
//         */
//        PaletteTask(int toDoTask, Object param) {
//            whatToDo  = toDoTask;
//            parameter = param;
//        }
//
//        /**
//         * run a queued palette task
//         *
//         *
//         */
//        public void run() {
//
//            t("performing task, id: " + whatToDo);    // NOI18N
//
//            if (whatToDo == SWITCH_UI) {
//                switchUI();
//                return;
//            }
//
//            java.util.Set changedNodes;
//
//            if (whatToDo == ITEMS_CHANGED) {
//                changedNodes = mUpdateNodes;
//                mUpdateNodes = new java.util.HashSet();
//            } else {
//                changedNodes = null;
//            }
//
//            if (mListeners == null) {
//                return;
//            }
//
//            Vector l;
//
//            synchronized (this) {
//                l = (Vector) mListeners.clone();
//            }
//
//            for (java.util.Iterator it = l.iterator(); it.hasNext();) {
//                PaletteManagerListener pml =
//                    (PaletteManagerListener) it.next();
//
//                if (whatToDo == ITEMS_CHANGED) {
//                    java.util.Iterator itr = changedNodes.iterator();
//
//                    while (itr.hasNext()) {
//                        Node node = (Node) itr.next();
//
//                        pml.categoryChanged(node);
//                    }
//                } else if (parameter instanceof NodeMemberEvent) {
//
//                    // extract nodes from NodeMemberEvent
//                    Node[] nodes   = ((NodeMemberEvent) parameter).getDelta();
//                    int[]  indices =
//                        ((NodeMemberEvent) parameter).getDeltaIndices();
//
//                    if (whatToDo == CATEGORIES_ADDED) {
//                        pml.categoriesAdded(nodes, indices);
//                    } else if (whatToDo == CATEGORIES_REMOVED) {
//                        pml.categoriesRemoved(nodes, indices);
//                    }
//                } else if (whatToDo == CATEGORIES_REORDERED) {
//                    pml.categoriesReordered();
//                }
//            }
//        }
//    }

    //:::::::::::::::::::::::::::::::::::::

    //For debugging purposes only. Original NB Code...
    static private final boolean TRACE = true;
    static private void t(String str) {
        if (TRACE) {
            if (str != null) {
                mLogger.fine("-----=====> PaletteManager: [" + Thread.currentThread().getName() + "] " + str);
            } else {
                mLogger.fine("");
            }
        }
    }

    //:::::::::::::::::::::::::::::::::::::
}
