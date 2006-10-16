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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.palette.ui;

import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.CategoryListener;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Settings;
import org.netbeans.modules.palette.Utils;
import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

/**
 * A visual component for a single palette category. Contains expand/collapse button
 * and a list of palette items.
 *
 * @author David Kaspar, Jan Stola, S. Aubrecht
 */
class CategoryDescriptor implements CategoryListener {
    private PalettePanel palettePanel;
    private Category category;
    private JPanel wholePanel;
    private CategoryButton categoryButton;
    private CategoryList itemsList;
    private DefaultListModel itemsListModel;
    private boolean opened;
    private boolean resetItems = true;
    private Settings settings;

    CategoryDescriptor( PalettePanel palettePanel, Category category ) {
        assert palettePanel != null : "No palette panel"; // NOI18N
        assert category != null : "No category node"; // NOI18N
        this.palettePanel = palettePanel;
        this.category = category;
        this.settings = palettePanel.getSettings();
        wholePanel = new JPanel() {
            public void addNotify() {
                super.addNotify();
                CategoryDescriptor.this.category.addCategoryListener( CategoryDescriptor.this );
            }
            public void removeNotify() {
                super.removeNotify();
                CategoryDescriptor.this.category.removeCategoryListener( CategoryDescriptor.this );
            }
        };
        

        wholePanel.setLayout (new GridBagLayout ());
        wholePanel.setBorder (new EmptyBorder (0, 0, 0, 0));

        MouseListener listener = createMouseListener();

        categoryButton = new CategoryButton( this, category );
        categoryButton.addMouseListener (listener);
        GridBagConstraints gbc = new GridBagConstraints (0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets (1, 0, 0, 0), 0, 0);
        wholePanel.add (categoryButton, gbc);

        itemsList = new CategoryList( category, palettePanel );
        itemsList.setModel (itemsListModel = new DefaultListModel ());
        itemsList.setShowNames(palettePanel.getSettings().getShowItemNames());
        itemsList.setIconSize(palettePanel.getSettings().getIconSize());
        itemsList.addMouseListener (listener);
        itemsList.addListSelectionListener (new ListSelectionListener () {
            public void valueChanged (ListSelectionEvent e) {
                CategoryDescriptor.this.palettePanel.select( CategoryDescriptor.this.category, (Item)itemsList.getSelectedValue ());
            }
        });
        gbc = new GridBagConstraints (0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets (0, 0, 0, 0), 0, 0);
        wholePanel.add (itemsList, gbc);
        wholePanel.setFocusTraversalPolicy( new MyFocusTraversal( this ) );
        doSetOpened( settings.isExpanded( category ) );
    }

    private MouseListener createMouseListener() {
        return new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    JComponent comp = (JComponent)event.getSource();
                    Item item = null;
                    if (comp instanceof JList) {
                        JList list = (JList)comp;
                        Point p = event.getPoint();
                        int index = list.locationToIndex(p);
                        if (index >= 0 && !list.getCellBounds(index, index).contains(p)) {
                            index = -1;
                        }
                        if (index >= 0) {
                            item = (Item)list.getModel().getElementAt(index);
                        }
                    }
                    Action[] actions = null == item ? category.getActions() : item.getActions();
                    JPopupMenu popup = Utilities.actionsToPopup( actions, getComponent() );
                    Utils.addCustomizationMenuItems( popup, getPalettePanel().getController(), getPalettePanel().getSettings() );
                    popup.show(comp, event.getX(), event.getY());
                }
            }
        };
    }

    void refresh() {
        categoryButton.updateProperties();
        categoryButton.repaint ();
        if( isOpened() && resetItems ) {
            computeItems();
        }
    }
    
    void computeItems() {
        DefaultListModel newModel = new DefaultListModel();
        Item[] items = category.getItems();
        if( items != null ) {
            for( int i=0; i<items.length; i++ ) {
                if( settings.isVisible( items[i] ) ) {
                    newModel.addElement( items[i] );
                }
            }
        }
        itemsListModel = newModel;
        itemsList.setModel( newModel );
        resetItems = false;
    }
        
    void resetItems() {
        resetItems = true;
    }

    Category getCategory () {
        return category;
    }

    boolean isOpened() {
        return opened;
    }
    
    boolean isSelected() {
        return categoryButton.isFocusOwner() || itemsList.getSelectedIndex() >= 0;
    }

    void setSelectedItem( Item item ) {
        if( itemsList.getSelectedValue () == item ) {
            return;
        }
        if( item == null ) {
            int selectedIndex = itemsList.getSelectedIndex ();
            itemsList.removeSelectionInterval( selectedIndex, selectedIndex );
        } else {
            itemsList.setSelectedValue( item, true );
        }
    }

    void setOpened( boolean b ) {
        if( opened == b ) {
            return;
        }
        doSetOpened( b );
        settings.setExpanded( category, b );
    }
    
    private void doSetOpened( boolean b ) {
        opened = b;
        if( opened ) {
            if( resetItems ) {
                computeItems();
            }
        } else {
            palettePanel.select( category, null );
        }
        itemsList.setVisible( opened );
        categoryButton.setSelected( opened );
        if( opened ) {
            itemsList.ensureIndexIsVisible( 0 );
        }
    }

    void setPositionY( int yPosition ) {
        wholePanel.setLocation( 0, yPosition );
    }

    JComponent getComponent () {
        return wholePanel;
    }
    
    int getPreferredHeight( int width ) {
        return isOpened() ?
            itemsList.getPreferredHeight( width ) + categoryButton.getPreferredSize().height :
            categoryButton.getPreferredSize().height;
    }

    void setWidth( int width ) {
        wholePanel.setSize( width, wholePanel.getHeight() );
    }

    void setShowNames( boolean showNames ) {
        itemsList.setShowNames( showNames );
    }

    void setIconSize( int iconSize ) {
        itemsList.setIconSize( iconSize );
    }

    void setItemWidth( int itemWidth ) {
        itemsList.setFixedCellWidth( itemWidth );
    }

    PalettePanel getPalettePanel () {
        return palettePanel;
    }

    public void categoryModified( Category category ) {
        resetItems();
        palettePanel.refresh ();
    }
    
    CategoryList getList() {
        return itemsList;
    }
    
    CategoryButton getButton() {
        return categoryButton;
    }

    private static class MyFocusTraversal extends FocusTraversalPolicy {
        private CategoryDescriptor descriptor;
        public MyFocusTraversal( CategoryDescriptor descriptor ) {
            this.descriptor = descriptor;
        }
        
        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            if( focusCycleRoot == descriptor.wholePanel && aComponent == descriptor.categoryButton )
                return descriptor.itemsList;
            return null;
        }

        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            if( focusCycleRoot == descriptor.wholePanel && aComponent == descriptor.itemsList )
                return descriptor.categoryButton;
            return null;
        }

        public Component getLastComponent(Container focusCycleRoot) {
            if( focusCycleRoot == descriptor.wholePanel )
                return descriptor.itemsList;
            return null;
        }

        public Component getFirstComponent(Container focusCycleRoot) {
            if( focusCycleRoot == descriptor.wholePanel )
                return descriptor.categoryButton;
            return null;
        }

        public Component getDefaultComponent(Container focusCycleRoot) {
            return getFirstComponent( focusCycleRoot );
        }
    }
}
