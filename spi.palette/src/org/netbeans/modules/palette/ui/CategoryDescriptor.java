/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.palette.ui;

import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.CategoryListener;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Settings;
import org.netbeans.modules.palette.Utils;
import org.openide.nodes.*;
import org.openide.util.WeakListeners;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.EventListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


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
        category.addCategoryListener( this );
        wholePanel = new JPanel ();

        wholePanel.setLayout (new GridBagLayout ());
        wholePanel.setBorder (new EmptyBorder (0, 0, 0, 0));

        MouseListener listener = createMouseListener();

        categoryButton = new CategoryButton( this, category );
        categoryButton.addMouseListener (listener);
        GridBagConstraints gbc = new GridBagConstraints (0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets (1, 0, 0, 0), 0, 0);
        wholePanel.add (categoryButton, gbc);

        itemsList = new CategoryList( category );
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
    }

    void setPositionY( int yPosition ) {
        wholePanel.setLocation( 0, yPosition );
    }

    JComponent getComponent () {
        return wholePanel;
    }
    
    int getPreferredHeight() {
        return isOpened() ?
            getComponent().getPreferredSize().height :
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
}
