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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openide.nodes.Node;

import org.netbeans.modules.soa.mapper.common.ui.palette.IPaletteItem;


/**
 * Tabbed panels of the functoid palette.
 *
 * @author Tientien Li
 */
public class PalettePanel extends javax.swing.JPanel {

    /** The manager of Palette that serves functionality*/
    protected PaletteManager mManager = null;

    /** Root palette node - provides palette content (categories and items) */
    protected Node mPaletteNode;

    /** Palette tabs component. */
    protected javax.swing.JTabbedPane mTabbedPane;

    /** List of aligned lists. Used for displaying beans. */
    protected java.util.ArrayList mAlignedLists = new java.util.ArrayList();

    /** Listener for various types of events */
    private Listener mListener;

    /** Set of categories - to avoid duplicates */
    private java.util.Set mCategories = new java.util.HashSet();

    /** List of aligned lists. Used for displaying beans. */
    protected Node[] mCategoryNodes = null;

    /** List of aligned lists. Used for displaying beans. */
    protected boolean[] mCategoryStatus = null;

    /** Field SHOW_NAMES           */
    private static final String SHOW_NAMES = "SHOW_NAMES";    //NOI18N

    /** Field mTabbedDone           */
    private boolean mTabbedDone = false;

    /** Field mInitTabbed           */
    private boolean mInitTabbed = false;

    /** Field mNodeListener */
//    private PaletteNodeListener mNodeListener = new PaletteNodeListener();

    /** Field listRenderer           */
    private PaletteListRenderer listRenderer;

    /**
     * The log4j logger
     */
    private static Logger mLogger =
            Logger.getLogger(PalettePanel.class.getName());

    //:::::::::::::::::::::::::::::::::::::

    /**
     * Create new instance (implementation is in superclass) of the palette
     * panel
     *
     * @param pm the palette manager
     */
    public PalettePanel(PaletteManager pm) {

        mManager     = pm;
        mPaletteNode = mManager.getRootNode();
        mTabbedPane  = new javax.swing.JTabbedPane();

        mTabbedPane.setPreferredSize(new Dimension(410, 50));

//        mPaletteNode.addNodeListener(mNodeListener);
//        mPaletteNode.getChildren().getNodes(); // force subnodes creation
        fillTabbedPane();
        updateTabbedPane();
        
        /*
        mTabbedPane.getAccessibleContext().setAccessibleName(
            PaletteManager.getBundle().getString("ACS_PaletteTabbedPane"));
        mTabbedPane.getAccessibleContext().setAccessibleDescription(
            PaletteManager.getBundle().getString("ACSD_PaletteTabbedPane"));
        */

        add(mTabbedPane);
        setLayout(new PaletteLayout(mTabbedPane));
    }

    // IPaletteManager Interface Support methods....
    //-------------------------------------------------------------------------
    /**
     * Method isInitialized
     *
     *
     * @return true if initialization complete
     *
     */
    public boolean isInitialized() {
        return mTabbedDone;
    }

    /**
     * get the selected palette category
     *
     *
     * @return the selected category
     *
     */
    public PaletteCategoryNode getSelectedCategory() {

        int i = mTabbedPane.getSelectedIndex();

        if (i < 0) {
            return null;
        }

        String selectedTab = mTabbedPane.getTitleAt(i);
        PaletteCategoryNode n = null;

        for (java.util.Iterator it = mCategories.iterator(); it.hasNext();) {
            n = (PaletteCategoryNode) it.next();

            if (selectedTab.equals(n.getDisplayName())) {
                return n;
            }
        }

        return null;
    }

    /**
     * set the selected palette category
     *
     *
     * @param selectedTab the selected tab name
     *
     */
    public void setSelectedCategory(String selectedTab) {

        for (int i = 0, n = mTabbedPane.getTabCount(); i < n; i++) {
            if (selectedTab.equals(mTabbedPane.getTitleAt(i))) {
                mTabbedPane.setSelectedIndex(i);

                break;
            }
        }
    }

    /**
     * get the list of selected item indices for a category
     *
     *
     * @param selectedTab the palette category tab name
     *
     * @return the list of selected item indices
     *
     */
    public int[] getCategorySelectedItemIndices(String selectedTab) {

        int sel = -1;

        for (int i = 0, n = mTabbedPane.getTabCount(); i < n; i++) {
            if (selectedTab.equals(mTabbedPane.getTitleAt(i))) {
                sel = i;

                break;
            }
        }

        if (sel < 0) {
            return null;
        }

        JList list = (JList) mAlignedLists.get(sel);

        return list.getSelectedIndices();
    }

    /**
     * get the Category Index of a selected palette category
     *
     *
     * @param selectedTab the palette category tab name
     *
     * @return the palette category index
     *
     */
    public int getCategoryIndex(String selectedTab) {

        int sel = -1;

        for (int i = 0, n = mTabbedPane.getTabCount(); i < n; i++) {
            if (selectedTab.equals(mTabbedPane.getTitleAt(i))) {
                sel = i;

                break;
            }
        }

        return sel;
    }

    /**
     * select all items within a category
     *
     *
     * @param selectedTab the palette category tab name
     *
     */
    public void selectAll(String selectedTab) {

        for (int i = 0, n = mTabbedPane.getTabCount(); i < n; i++) {
            if (selectedTab.equals(mTabbedPane.getTitleAt(i))) {
                PaletteList list = (PaletteList) mAlignedLists.get(i);

                list.addSelectionInterval(0, (list.getModel().getSize() - 1));

                return;
            }
        }
    }

    /**
     * clear all selections within a category
     *
     *
     * @param selectedTab the palette category tab name
     *
     */
    public void clearAll(String selectedTab) {

        for (int i = 0, n = mTabbedPane.getTabCount(); i < n; i++) {
            if (selectedTab.equals(mTabbedPane.getTitleAt(i))) {
                ((PaletteList) mAlignedLists.get(i)).clearSelection();

                return;
            }
        }
    }

    /**
     * select an item within a category
     *
     *
     * @param selectedTab the palette category tab name
     * @param item the selected item
     *
     */
    public void selectItem(String selectedTab, PaletteItemNode item) {

        for (int i = 0, n = mTabbedPane.getTabCount(); i < n; i++) {
            if (selectedTab.equals(mTabbedPane.getTitleAt(i))) {
                PaletteList list  = (PaletteList) mAlignedLists.get(i);
                javax.swing.ListModel   model = list.getModel();

                for (int j = 0, m = model.getSize(); j < m; j++) {
                    if (item == (PaletteItemNode) model.getElementAt(j)) {
                        list.addSelectionInterval(j, j);

                        return;
                    }
                }
            }
        }
    }

    /**
     * clear a selected item within a category
     *
     *
     * @param selectedTab the palette category tab name
     * @param item the selected item
     *
     */
    public void clearItem(String selectedTab, PaletteItemNode item) {

        for (int i = 0, n = mTabbedPane.getTabCount(); i < n; i++) {
            if (selectedTab.equals(mTabbedPane.getTitleAt(i))) {
                PaletteList list  = (PaletteList) mAlignedLists.get(i);
                javax.swing.ListModel   model = list.getModel();

                for (int j = 0, m = model.getSize(); j < m; j++) {
                    if (item == (PaletteItemNode) model.getElementAt(j)) {
                        list.removeSelectionInterval(j, j);

                        return;
                    }
                }
            }
        }
    }

    /**
     * Notify when this component is added into a container
     *
     *
     */
    public void addNotify() {

        // t("addNotify: "+mInitTabbed+", "+mTabbedDone);
        super.addNotify();

        if (mListener == null) {
            mListener = new Listener();

            mManager.addPropertyChangeListener(mListener);
//            mManager.addManagerListener(mListener);
            mTabbedPane.addChangeListener(mListener);
            mTabbedPane.addMouseListener(mListener);
        }

        // temp fix of a UI bug.. First selection must be 0
        mTabbedPane.setSelectedIndex(0);
    }

    /**
     * Fill the content of palette TabbedPane
     *
     *
     */
    private void fillTabbedPane() {

        synchronized (getTreeLock()) {
            // t("fillTabbedPanel... initTab: "+mInitTabbed);
            if (mInitTabbed) {
                return;
            }
            mCategoryNodes = mManager.getCategoryNodes();

            if (mCategoryNodes == null) {
                return;
            }

            // t("fillTabbedPanel... got: "+mCategoryNodes.length);
            if (mCategoryNodes.length < 1) {
                return;
            }

            mCategoryStatus = new boolean[mCategoryNodes.length];

            mTabbedPane.removeAll();
            mAlignedLists.clear();
            mCategories.clear();

            for (int i = 0; i < mCategoryNodes.length; i++) {
                Node categoryNode = mCategoryNodes[i];
                mCategories.add(categoryNode);
                Component tab = new JPanel(); // createPaletteTab(i, categoryNode);
                mTabbedPane.addTab(categoryNode.getDisplayName(), null, tab);

                mCategoryStatus[i] = false;
            }
        }

        /*
        for (int i = 0; i < mTabbedPane.getComponentCount(); i++) {
            t("Tab[" + i + "]: " + mTabbedPane.getTitleAt(i));
        }
        */

        mTabbedPane.revalidate();
        mTabbedPane.repaint();

//        for (int i = 0; i < mCategoryNodes.length; i++) {
//            Node categoryNode = mCategoryNodes[i];
//            categoryNode.addNodeListener(mNodeListener);
//            categoryNode.getChildren().getNodes(); // force subnodes creation
//        }

        mInitTabbed = true;
        updateShowNames(mManager.getShowComponentsNames());

    }

    /**
     * update the content of palette TabbedPane
     *
     *
     */
    private void updateTabbedPane() {

        synchronized (getTreeLock()) {
            //t("updateTabbedPane... initTab:"+mInitTabbed);
            if (!mInitTabbed) {
                fillTabbedPane();
            }
            int cSize = mCategoryNodes.length;
            for (int i = 0; i < mCategoryNodes.length; i++) {
                Node categoryNode = mCategoryNodes[i];
                mCategories.add(categoryNode);
                Component tab = createPaletteTab(i, categoryNode);
                mTabbedPane.setComponentAt(i, tab);
                //t(cSize+"-fillTabbedPane.list[" + i + "] "
                //   + ((PaletteList) mAlignedLists.get(i)).getModel().getSize());
            }
        }

        mTabbedPane.revalidate();
        mTabbedPane.repaint();

        mTabbedDone = true;
        mManager.setInitialized(mTabbedDone);
    }


    /**
     * Create palette tab for category node
     *
     * @param index the category index
     * @param node the category node
     * @return the palette tab component for the category
     */
    private Component createPaletteTab(int index, Node node) {

        // t("creating palette tab");    // NOI18N
        PaletteList list = createCategoryList(node);

        mAlignedLists.add(index, list);

        javax.swing.JScrollPane scrollList = new javax.swing.JScrollPane() {
            public void setBorder(javax.swing.border.Border border) {
                // keep the border null
            }
        };

        scrollList.setViewportView(list);

        /* set default checked items...  */
        javax.swing.ListModel   model = list.getModel();

        for (int j = 0, m = model.getSize(); j < m; j++) {
            PaletteItemNode item = (PaletteItemNode) model.getElementAt(j);
            Boolean checked =
                    (Boolean) item.getItemAttribute(IPaletteItem.ATTR_CHECKED);
            if ((checked != null) && checked.booleanValue()) {
                list.addSelectionInterval(j, j);
            }
        }

        return scrollList;
    }

    /**
     * Create Aligned list for category node
     *
     * @param node the category node
     * @return the palette list for the category
     */
    private PaletteList createCategoryList(Node node) {

        PaletteList list = new PaletteList(mManager);

        list.setListData(mManager.getItemNodes(node));
        list.setOpaque(false);
        list.setAlignStyle(PaletteList.HORIZONTAL_ALIGN_STYLE);
        list.setVisibleRowCount(2);

        // list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(mListener);
        list.addMouseListener(mListener);

        if (listRenderer == null) {
            listRenderer = new PaletteListRenderer();

            listRenderer.setShowNames(mManager.getShowComponentsNames());
        }

        list.setCellRenderer(listRenderer);
        javax.swing.ToolTipManager.sharedInstance().registerComponent(list);
        list.getAccessibleContext()
            .setAccessibleName(java.text.MessageFormat
                .format(PaletteManager.getBundle()
                    .getString("ACS_PaletteBeansList"),    // NOI18N
                new Object[]{ node.getDisplayName() }));
        list.getAccessibleContext()
            .setAccessibleDescription(PaletteManager.getBundle()
                .getString("ACSD_PaletteBeansList"));      // NOI18N

        return list;
    }

    /**
     * Clear selection in every aligned list
     */
    private void clearSelection() {

        for (int i = 0; i < mAlignedLists.size(); i++) {
            ((PaletteList) mAlignedLists.get(i)).clearSelection();
        }
    }

    /*
    ///////////////////////////////////////////////////
    void showMenuOnPalette(Component comp, java.awt.Point pos) {
        new PalettePopupMenu(mManager.getPaletteRootNode()).show(comp, pos.x,
                             pos.y);
    }
    void showMenuOnItem(Node itemNode, Component comp, Point pos) {
        JPopupMenu popup = new ItemPopupMenu(itemNode);
        popup.add(new JSeparator());
        JCheckBoxMenuItem menuItem =
            new JCheckBoxMenuItem(PaletteManager.getBundle()
            .getString("CTL_ShowNames")); // NOI18N
        menuItem.setSelected(listRenderer.isShowNames());
        menuItem.setActionCommand(SHOW_NAMES);
        menuItem.addActionListener(listener);
        popup.add(menuItem);
        popup.show(comp, pos.x, pos.y);
    }
    //////////////////////////////////////////////////
    */

    /**
     * re-validate all palette Lists
     *
     *
     */
    private void revalidateLists() {

        // hack to force the aligned list to relayout list items
        for (int i = 0; i < mAlignedLists.size(); i++) {
            PaletteList list = (PaletteList) mAlignedLists.get(i);

            list.setSize(0, 0);

            if (i == mTabbedPane.getSelectedIndex()) {
                list.revalidate();
                list.repaint();
            }
        }
/*
        int         index = mTabbedPane.getSelectedIndex();
        PaletteList list  = (PaletteList) mAlignedLists.get(index);

        list.revalidate();
        list.repaint();
 */
    }

    /**
     * update the palette list ShowNames flag
     *
     *
     * @param show true to show palette item names
     *
     */
    public void updateShowNames(boolean show) {

        if (listRenderer != null) {
            listRenderer.setShowNames(show);
            revalidateLists();
        }
    }

    //:::::::::::::::::::::::::::::::::::::

    /**
     * This class creates a palette list item with checkbox
     *
     */
    private static class DoCheckBox extends javax.swing.JPanel {

        /** the palette item check box           */
        private javax.swing.JCheckBox jc;

        /** the palette item label           */
        private javax.swing.JLabel jb;

        /** the palette item listener           */
        private java.awt.event.ItemListener iListener;

        /**
         * Constructor to create a palette item with checkbox
         *
         *
         */
        public DoCheckBox() {

            super();

            this.setLayout(new BorderLayout(0, 1));

            jc = new javax.swing.JCheckBox();
            jb = new javax.swing.JLabel();

            jb.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            this.add(jc, BorderLayout.WEST);
            this.add(jb, BorderLayout.CENTER);
            this.add(new javax.swing.JLabel("   "), BorderLayout.EAST);
        }

        /**
         * set the palette item Margin
         *
         *
         * @param m the margin insets
         *
         */
        public void setMargin(Insets m) {
            // set it to null
        }

        /**
         * set the palette item Icon
         *
         *
         * @param ic the item icon
         *
         */
        public void setIcon(javax.swing.Icon ic) {
            jb.setIcon(ic);
        }

        /**
         * set the palette item label Text
         *
         *
         * @param text the item label text
         *
         */
        public void setText(String text) {
            jb.setText(text);
        }

        /**
         * set the Selected flag of palette item
         *
         *
         * @param b true if it is to be selected
         *
         */
        public void setSelected(boolean b) {
            jc.setSelected(b);
        }
    }

    /**
     * This class implements a rollover button like list renderer
     */
    private static class PaletteListRenderer
            implements javax.swing.ListCellRenderer {

        /** Field button           */
        private DoCheckBox button; // JToggleButton button;

        /** Field buttonBorder           */
        private javax.swing.border.Border buttonBorder;

        /** Field emptyBorder           */
        private javax.swing.border.Border emptyBorder;

        /** Field iconSize           */
        private int iconSize;

        /** Field showNames           */
        private boolean showNames = true;    // false;

        /** button default Color */
        private Color defaultColor;

        /**
         * Constructor to create the Palette List Renderer
         *
         *
         */
        public PaletteListRenderer() {

            button = new DoCheckBox();    // JToggleButton();
            button.setMargin(new Insets(1, 1, 1, 1));
            buttonBorder = button.getBorder();
            defaultColor = button.getBackground();

            if (buttonBorder != null) {
                emptyBorder =
                    new javax.swing.border.EmptyBorder(
                            buttonBorder.getBorderInsets(button));
            }

            setIconSize(java.beans.BeanInfo.ICON_COLOR_16x16);
            setShowNames(false);
        }

        /**
         * Return a component that has been configured to display
         * the specified value.
         *
         *
         * @param list The JList we're painting
         * @param value The value of the list element.
         * @param index The cells index
         * @param isSelected True if the specified cell was selected.
         * @param cellHasFocus True if the specified cell has the focus
         *
         * @return A component whose paint() method will render the specified
         * value.
         *
         */
        public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            Node node = (Node) value;

            if (showNames) {
                button.setText(node.getDisplayName());
            }

            button.setIcon(new javax.swing.ImageIcon(node.getIcon(iconSize)));
            button.setSelected(isSelected);
            button.setBackground((index
                                  == ((PaletteList) list)
                                      .getCurSelectedIndex())
                                 ? java.awt.Color.YELLOW
                                 : defaultColor);
            button.setToolTipText(node.getShortDescription().replace('-',
                    '.'));

            return button;
        }

        /**
         * set the palette item Icon Size
         *
         *
         * @param size the icon size
         *
         */
        public void setIconSize(int size) {

            /*
            if (size == BeanInfo.ICON_COLOR_16x16) {
                button.setHorizontalAlignment(
                  isShowNames() ? SwingConstants.LEFT : SwingConstants.CENTER);
                button.setVerticalTextPosition(SwingConstants.CENTER);
                button.setHorizontalTextPosition(SwingConstants.RIGHT);
            } else {
                button.setHorizontalAlignment(SwingConstants.CENTER);
                button.setVerticalTextPosition(SwingConstants.BOTTOM);
                button.setHorizontalTextPosition(SwingConstants.CENTER);
            }
             */
            iconSize = size;
        }

        /**
         * get the palette item Icon Size
         *
         *
         * @return the icon size
         *
         */
        public int getIconSize() {
            return iconSize;
        }

        /**
         * set the palette item ShowNames flag
         *
         *
         * @param show true to show the palette item label text
         *
         */
        public void setShowNames(boolean show) {

            if (!show) {
                button.setText("     "); // null);
            }

            showNames = show;

            setIconSize(getIconSize());
        }

        /**
         * check to show the palette item label text or not
         *
         *
         * @return true to show the palette item label text
         *
         */
        public boolean isShowNames() {
            return showNames;
        }
    }

    /**
     * Listener for events fired by PalettePanel members
     */
    private class Listener
            extends java.awt.event.MouseAdapter
            implements /*PaletteManagerListener,*/
            java.awt.event.ActionListener,
            java.beans.PropertyChangeListener,
            javax.swing.event.ChangeListener,
            javax.swing.event.ListSelectionListener {

        /**
         * ListSelectionListener on PaletteList. Handles selection changes.
         *
         * @param evt the list selection event
         */
        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {

            JList list = (JList) evt.getSource();

            /*
            int index = list.getSelectedIndex();
            if (index == -1) {
                if (tabbedPane.getSelectedIndex()
                    == alignedLists.indexOf(list)) {
                    manager.setSelectedItem(null);
                    //manager.setMode(PaletteAction.MODE_SELECTION);
                }
            } else if (!evt.getValueIsAdjusting()) {
                PaletteItemNode node =
                (PaletteItemNode) list.getModel().getElementAt(index);
                // PaletteItem item = PaletteManager.createPaletteItem(node);
                manager.setSelectedItem(node); // item);
                //manager.setMode(PaletteAction.MODE_ADD);
            }
             */
        }

        /**
         * Handles mouse button release on tabbed pane. Invokes popup menus.
         *
         * @param e a mouse event
         */
        public void mouseReleased(java.awt.event.MouseEvent e) {

            if (!SwingUtilities.isRightMouseButton(e)) {
                return;
            }

            /* disable the popup for now... 08/26/02, T. Li */
            //showMenuOnPalette(e.getComponent(), e.getPoint());
        }

        /**
         * Handles 'Big icons' or 'Show names' menu item selection.
         *
         * @param evt an action event
         */
        public void actionPerformed(java.awt.event.ActionEvent evt) {

            JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) evt.getSource();
            /*
            if (evt.getActionCommand().equals(BIG_ICONS)) {
                if (menuItem.isSelected()) {
                    listRenderer.setIconSize(BeanInfo.ICON_COLOR_32x32);
                } else {
                    listRenderer.setIconSize(BeanInfo.ICON_COLOR_16x16);
                }
            } else
             */
            if (evt.getActionCommand().equals(SHOW_NAMES)) {
                listRenderer.setShowNames(menuItem.isSelected());
                mManager.setShowComponentsNames(menuItem.isSelected());
            }

            revalidateLists();
        }

        /**
         * ChangeListener on tabbed pane. Handles selected tab change.
         *
         * @param evt a tab change event
         */
        public void stateChanged(javax.swing.event.ChangeEvent evt) {

            // manager.setSelectedItem(null);
            // manager.setMode(PaletteAction.MODE_SELECTION);
            int index = mTabbedPane.getSelectedIndex();

            if ((index >= 0) && (index < mAlignedLists.size())) {
                PaletteList list = (PaletteList) mAlignedLists.get(index);

                if (list != null) {
                    int sel = list.getCurSelectedIndex();

                    mManager.setSelectedItem((sel < 0)
                                             ? null
                                             : ((PaletteItemNode) (list
                                                 .getModel()
                                                     .getElementAt(sel))));
                }

                // list.setListData(manager.getItemNodes(
                // manager.getCategoryNodes()[index]));
            }

        }

        /**
         * PropertyChangeListener on palette manager.
         *
         * @param evt a property change event
         */
        public void propertyChange(java.beans.PropertyChangeEvent evt) {

            if (PaletteManager.PROP_SELECTEDITEM
                    .equals(evt.getPropertyName())) {
            }
        }

//        /**
//         * Listen for category added event on palette manager.
//         *
//         * @param nodes the list of new category nodes
//         * @param indices the list of new category indices
//         */
//        public void categoriesAdded(Node[] nodes, int[] indices) {
//            if (!mTabbedDone) {
//                return;
//            }
//
//            mManager.setSelectedItem(null);
//
//            for (int i = 0; i < nodes.length; i++) {
//                Node categoryNode = nodes[i];
//
//                if (!mCategories.contains(categoryNode)) {
//                    int index = indices[i];
//
//                    if (index > mTabbedPane.getTabCount()) {
//                        index = mTabbedPane.getTabCount();
//                    }
//
//                    Component tab = createPaletteTab(index, categoryNode);
//
//                    mTabbedPane.insertTab(categoryNode.getDisplayName(),
//                                          null, tab, null, index);
//                }
//            }
//
//            revalidate();
//            repaint();
//        }
//
//        /**
//         * Listen for category remove events on palette manager.
//         *
//         * @param nodes the list of removed category nodes
//         * @param indices the list of removed category indices
//         */
//        public void categoriesRemoved(Node[] nodes, int[] indices) {
//
//            if (!mTabbedDone) {
//                return;
//            }
//
//            mManager.setSelectedItem(null);
//
//            for (int i = nodes.length - 1; i >= 0; i--) {
//                int index = indices[i];
//
//                mTabbedPane.removeTabAt(index);
//                mAlignedLists.remove(index);
//                mCategories.remove(nodes[i]);
//            }
//
//            revalidate();
//            repaint();
//        }
//
//        /**
//         * Listen for category reordered events on palette manager.
//         */
//        public void categoriesReordered() {
//
//            if (!mTabbedDone) {
//                return;
//            }
//
//            mManager.setSelectedItem(null);
//
//            int    sel         = mTabbedPane.getSelectedIndex();
//            String selectedTab = (sel >= 0)
//                                 ? mTabbedPane.getTitleAt(sel)
//                                 : null;
//
//            fillTabbedPane();
//
//            if (selectedTab != null) {
//                for (int i = 0, n = mTabbedPane.getTabCount(); i < n; i++) {
//                    if (selectedTab.equals(mTabbedPane.getTitleAt(i))) {
//                        mTabbedPane.setSelectedIndex(i);
//
//                        break;
//                    }
//                }
//            }
//
//            mTabbedPane.revalidate();
//            mTabbedPane.repaint();
//        }
//
//        /**
//         * Listen for category content change events on palette manager.
//         *
//         * @param catNode the category with changes
//         */
//        public void categoryChanged(Node catNode) {
//
//            if (!mTabbedDone) {
//                return;
//            }
//        }
    }

    /**
     * Layout of buttons and tabed pane - (top and bottom) or (left and right).
     */
    private static class PaletteLayout
            implements java.awt.LayoutManager {

        /** Field buttons           */
        private Component buttons = null;

        /** Field tabs           */
        private Component tabs;

        /**
         * Constructor the create a new PaletteLayout manager
         *
         *
         * @param tabs the list of tabs
         *
         */
        public PaletteLayout(Component tabs) {
            this.tabs = tabs;
        }

        /**
         * Lays out the container in the specified panel.
         *
         * @param parent the component which needs to be laid out
         */
        public void layoutContainer(java.awt.Container parent) {

            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                int    top    = insets.top;
                int    bottom = parent.getHeight() - insets.bottom;
                int    left   = insets.left;
                int    right  = parent.getWidth() - insets.right;
                int    height = bottom - top;
                int    width  = right - left;

                if (buttons != null) {
                    Dimension buttonSize = buttons.getPreferredSize();

                    if (buttonSize.width * height
                            >= buttonSize.height * width) {
                        buttons.setBounds(left, top,
                                          width /*buttonSize.width*/,
                                          buttonSize.height);

                        top    += buttonSize.height;
                        height -= buttonSize.height;
                    } else {
                        buttons.setBounds(left, top + 5, buttonSize.width,
                                          height - 5 /*buttonSize.height*/);

                        left  += buttonSize.width;
                        width -= buttonSize.width;
                    }
                }

                if (tabs != null) {
                    tabs.setBounds(left, top, width, height);
                }
            }
        }

        /**
         * get the preferred palette list Layout Size
         *
         *
         * @param parent the parent container
         *
         * @return the preferred layout size
         *
         */
        public Dimension preferredLayoutSize(java.awt.Container parent) {

            synchronized (parent.getTreeLock()) {
                Dimension dim = new Dimension();

                if (buttons != null) {
                    Dimension buttonSize = buttons.getPreferredSize();

                    dim.width += buttonSize.width;
                }

                if (tabs != null) {
                    Dimension tabSize = tabs.getPreferredSize();

                    dim.width  += tabSize.width;
                    dim.height = tabSize.height;
                }

                Insets insets = parent.getInsets();

                dim.width  += insets.left + insets.right;
                dim.height += insets.top + insets.bottom;

                return dim;
            }
        }

        /**
         * add a component to the Layout
         *
         *
         * @param name the name of the component
         * @param comp the component
         *
         */
        public void addLayoutComponent(String name, Component comp) {
            // we do not add/remove
        }

        /**
         * get the minimum Layout Size
         *
         *
         * @param parent the parent container
         *
         * @return the minimum layout size
         *
         */
        public Dimension minimumLayoutSize(java.awt.Container parent) {
            return preferredLayoutSize(parent);
        }

        /**
         * remove a component form the Layout
         *
         *
         * @param comp the component to be removed
         *
         */
        public void removeLayoutComponent(Component comp) {
           // we do not add/remove
        }
    }

//    /**
//     * Listener for the Node chagne events.
//     */
//    private class PaletteNodeListener
//            implements org.openide.nodes.NodeListener {
//
//        /**
//         * Palette node event handler for children added events
//         *
//         *
//         * @param ev the node member event
//         *
//         */
//        public void childrenAdded(org.openide.nodes.NodeMemberEvent ev) {
//            if (ev.isAddEvent()) {
//                Node cat = (Node) ev.getSource();
//
//                if (cat instanceof PaletteCategoryNode) {
//
//                    Node[] ns = ev.getDelta();
//                    int index = mTabbedPane.indexOfTab(cat.getDisplayName());
//                    if (index > -1) {
//
////                        mManager.updateItems(index);
//                        mCategoryStatus[index] = true;
//
//                        Runnable doUpdateTabbedPane = new Runnable() {
//                            public void run() {
//                                updateTabbedPane();
//                             }
//                        };
//
//                        boolean done = true;
//                        for (int i = 0; i < mCategoryStatus.length; i++) {
//                            done = done && mCategoryStatus[i];
//                        }
//                        if (done) {
//                            SwingUtilities.invokeLater(doUpdateTabbedPane);
//                        }
//                    }
//                } else if (cat instanceof PaletteNode) {
//                    Runnable doFillTabbedPane = new Runnable() {
//                        public void run() {
//                            fillTabbedPane();
//                         }
//                    };
//
//                    if (!mInitTabbed) {
//                        SwingUtilities.invokeLater(doFillTabbedPane);
//                    }
//                }
//            }
//        }
//
//        /**
//         * Palette node event handler for children removed events
//         *
//         *
//         * @param ev the node removed event
//         *
//         */
//        public void childrenRemoved(org.openide.nodes.NodeMemberEvent ev) {
//        }
//
//        /**
//         * Palette node event handler for children reordered events
//         *
//         *
//         * @param ev the node reorder event
//         *
//         */
//        public void childrenReordered(org.openide.nodes.NodeReorderEvent ev) {
//        }
//
//        /**
//         * Palette node event handler for children destoryed events
//         *
//         *
//         * @param ev the node destoryed event
//         *
//         */
//        public void nodeDestroyed(org.openide.nodes.NodeEvent ev) {
//        }
//
//        /**
//         * Palette node event handler for property change events
//         *
//         *
//         * @param ev the property change event
//         *
//         */
//        public void propertyChange(java.beans.PropertyChangeEvent ev) {
//        }
//   }

    //:::::::::::::::::::::::::::::::::::::
    //For debugging purposes only... original NB code
    static private final boolean TRACE = true;
    static private void t(String str) {
        if (TRACE) {
            if (str != null) {
                mLogger.fine("-----=====> PalettePanel: [" + Thread.currentThread().getName() + "] " + str);    // NOI18N
            } else {
                mLogger.fine("");                          // NOI18N
            }
        }
    }
    //:::::::::::::::::::::::::::::::::::::
}
