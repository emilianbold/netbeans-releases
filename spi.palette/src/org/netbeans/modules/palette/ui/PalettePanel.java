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
import java.util.ArrayList;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.ModelListener;
import org.netbeans.modules.palette.Settings;
import org.netbeans.modules.palette.Utils;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;



/**
 * Palette's visual component implementation.
 *
 * @author S. Aubrecht
 */
public class PalettePanel extends JPanel implements Scrollable {

    private static PalettePanel theInstance;
    
    private PaletteController controller;
    private Model model;
    private Settings settings;
    
    private ModelListener modelListener;
    private PropertyChangeListener settingsListener;
    
    private CategoryDescriptor[] descriptors = new CategoryDescriptor[0];
    private Category selectedCategory;
    
    private Object lock = new Object ();
    private MouseListener mouseListener;
    
    private JScrollPane scrollPane;
    
    private DnDSupport dndSupport;

    private PalettePanel () {
        setLayout( new PaletteLayoutManager() );
        addMouseListener( mouseListener() );
        
        dndSupport = new DnDSupport( this );
    }
    
    public static synchronized PalettePanel getDefault() {
        if( null == theInstance ) {
            theInstance = new PalettePanel();
        }
        return theInstance;
    }
    
    public JScrollPane getScrollPane() {
        if( null == scrollPane ) {
            scrollPane = new JScrollPane( this );
            scrollPane.setBorder(null);
            scrollPane.addMouseListener( mouseListener() );
        }
        return scrollPane;
    }
    
    private CategoryDescriptor getCategoryDescriptor( Category category ) {
        for( int i=0; i<descriptors.length; i++ ) {
            CategoryDescriptor descriptor = descriptors[i];
            if( descriptor.getCategory () == category )
                return descriptor;
        }
        return null;
    }

    private CategoryDescriptor[] computeDescriptors( Category[] categories ) {
        if( null == categories ) {
            return new CategoryDescriptor[0];
        }
        categories = getVisibleCategories( categories );
        CategoryDescriptor[] descriptors = new CategoryDescriptor[categories.length];
        for( int i=0; i<categories.length; i++) {
            Category category = categories[i];
            CategoryDescriptor descriptor = getCategoryDescriptor( category );
            if( descriptor == null ) {
                descriptor = new CategoryDescriptor( this, category );
                descriptor.setShowNames( getShowItemNames() );
                descriptor.setIconSize( getIconSize() );
            } else {
                descriptor.refresh();
            }
            descriptor.setWidth( getWidth() );
            descriptors[i] = descriptor;
        }
        return descriptors;
    }
    
    private Category[] getVisibleCategories( Category[] cats ) {
        ArrayList tmp = new ArrayList( cats.length );
        for( int i=0; i<cats.length; i++ ) {
            if( settings.isVisible( cats[i] ) ) {
                tmp.add( cats[i] );
            }
        }
        return (Category[])tmp.toArray( new Category[tmp.size()] );
    }

    void computeHeights( Category openedCategory ) {
        computeHeights( descriptors, openedCategory );
    }

    private void computeHeights( CategoryDescriptor[] paletteCategoryDescriptors, 
                                 Category openedCategory) {
        if( paletteCategoryDescriptors == null || paletteCategoryDescriptors.length <= 0 ) {
            return;
        }
        revalidate();
    }

    private static boolean arrayContains( Object[] objects, Object object ) {
        if( objects == null || object == null )
            return false;
        for( int i=0; i<objects.length; i++ ) {
            if( objects[i] == object )
                return true;
        }
        return false;
    }

    private void setDescriptors( CategoryDescriptor[] paletteCategoryDescriptors ) {
        for( int i=0; i<descriptors.length; i++ ) {
            CategoryDescriptor descriptor = descriptors[i];
            if( !arrayContains( paletteCategoryDescriptors, descriptor ) ) {
                remove( descriptor.getComponent() );
                dndSupport.remove( descriptor );
            }
        }
        for( int i=0; i<paletteCategoryDescriptors.length; i++ ) {
            CategoryDescriptor paletteCategoryDescriptor = paletteCategoryDescriptors[i];
            if( !arrayContains( descriptors, paletteCategoryDescriptor ) ) {
                add( paletteCategoryDescriptor.getComponent() );
                dndSupport.add( paletteCategoryDescriptor );
            }
        }
        if( descriptors.length == 0 && paletteCategoryDescriptors.length > 0 ) {
            boolean isAnyCategoryOpened = false;
            for( int i=0; i<paletteCategoryDescriptors.length; i++ ) {
                if( paletteCategoryDescriptors[i].isOpened() ) {
                    isAnyCategoryOpened = true;
                    break;
                }
            }
            if( !isAnyCategoryOpened ) {
                paletteCategoryDescriptors[0].setOpened( true );
            }
        }
        descriptors = paletteCategoryDescriptors;        
        revalidate();
    }
    
    public void doRefresh() {
        if( null != controller )
            controller.refresh();
    }
    
    public void refresh () {
        Runnable runnable = new Runnable() {
            public void run() {
                synchronized( lock ) {
                    setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
                    CategoryDescriptor[] paletteCategoryDescriptors = computeDescriptors( null != model ? model.getCategories() : null );
                    setDescriptors (paletteCategoryDescriptors);
                    if( null != settings ) {
                        doSetIconSize( settings.getIconSize() );
                        doSetShowItemNames( settings.getShowItemNames() );
                    }
                    if( null != model ) {
                        Item item = model.getSelectedItem();
                        Category category = model.getSelectedCategory();
                        setSelectedItemFromModel( category, item );
                    }
                    setCursor( Cursor.getDefaultCursor() );
                }
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater( runnable );
        }
    }
    
    public void propertyChange( PropertyChangeEvent evt ) {
        refresh ();
    }

    void select( Category category, Item item ) {
        if( category != selectedCategory ) {
            CategoryDescriptor selectedDescriptor = findDescriptorFor( selectedCategory );
            if( selectedDescriptor != null ) {
                selectedDescriptor.setSelectedItem( null );
            }
        }
        selectedCategory = category;
        if( null != model ) {
            if( null == category || null == item )
                model.clearSelection();
            else
                model.setSelectedItem( category.getLookup(), item.getLookup() );
        }
    }
    
    private void setSelectedItemFromModel( Category category, Item item ) {
        if( null != selectedCategory && !selectedCategory.equals( category ) ) {
            CategoryDescriptor selectedDescriptor = findDescriptorFor( selectedCategory );
            if( selectedDescriptor != null ) {
                selectedDescriptor.setSelectedItem( null );
            }
        }
        CategoryDescriptor descriptor = findDescriptorFor( category );
        if( descriptor == null ) {
            return;
        }
        if( item != null ) {
            selectedCategory = category;
         }
        if( descriptor != null ) {
            descriptor.setSelectedItem( item );
        }
    }

    private CategoryDescriptor findDescriptorFor( Category category ) {
        if( null != descriptors ) {
            for( int i= 0; i<descriptors.length; i++ ) {
                CategoryDescriptor descriptor = descriptors[i];
                if( descriptor.getCategory().equals( category ) )
                    return descriptor;
            }
        }
        return null;
    }
    
    private void scrollToCategory( final Category category ) {
        Runnable runnable = new Runnable() {
            public void run() {
                synchronized( lock ) {
                    CategoryDescriptor descriptor = findDescriptorFor( category );
                    if( null != descriptor ) {
                        scrollPane.validate();
                        Point loc = descriptor.getComponent().getLocation();
                        scrollPane.getViewport().setViewPosition( loc );
                    }
                }
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater( runnable );
        }
    }

    /**
     * Set new palette model and settings.
     */
    public void setContent( PaletteController newController, Model newModel, Settings newSettings ) {
        synchronized (lock ) {
            if( newModel == model && newSettings == settings ) {
                return;
            }
            
            Model old = model;
            if( model != null && null != modelListener ) {
                model.removeModelListener( modelListener );
            }
            if( settings != null && null != settingsListener ) {
                settings.removePropertyChangeListener( settingsListener );
            }
            
            model = newModel;
            settings = newSettings;
            controller = newController;
            if( model != null ) {
                model.addModelListener( getModelListener() );
            }
            if( null != settings ) {
                settings.addPropertyChangeListener( getSettingsListener() );
            }
            refresh();
        }
    }
    
    private MouseListener mouseListener() {
        if( null == mouseListener ) {
            mouseListener = new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    if( SwingUtilities.isRightMouseButton( event ) && null != model ) {
                        JPopupMenu popup = Utilities.actionsToPopup( model.getActions(), PalettePanel.this );
                        Utils.addCustomizerMenuItem( popup, controller );
                        popup.show( (Component)event.getSource(), event.getX(), event.getY() );
                    }
                }
            };
        }
        return mouseListener;
    }

    public void setShowItemNames( boolean showNames ) {
        if( null != settings ) {
            settings.setShowItemNames( showNames );
        }
        doSetShowItemNames( showNames );
    }

    private void doSetShowItemNames( boolean showNames ) {
        for( int i=0; i<descriptors.length; i++ ) {
            descriptors[i].setShowNames( showNames );
        }
        repaint();
    }
    
    public boolean getShowItemNames() {
        boolean res = true;
        if( null != settings ) {
            res = settings.getShowItemNames();
        }
        return res;
    }

    public void setIconSize(int iconSize) {
        if( null != settings ) {
            settings.setIconSize( iconSize );
        }
        doSetIconSize( iconSize );
    }
    
    private void doSetIconSize(int iconSize) {
        for( int i=0; i<descriptors.length; i++ ) {
            descriptors[i].setIconSize( iconSize );
        }
        repaint();
    }

    public int getIconSize() {
        int res = BeanInfo.ICON_COLOR_16x16;
        if( null != settings ) {
            res = settings.getIconSize();
        }
        return res;
    }

    public boolean getScrollableTracksViewportHeight () {
        return false;
    }

    public boolean getScrollableTracksViewportWidth () {
        return true;
    }

    public Dimension getPreferredScrollableViewportSize () {
        return getPreferredSize ();
    }

    public int getScrollableBlockIncrement (Rectangle visibleRect, int orientation, int direction) {
        return 100;
    }

    public int getScrollableUnitIncrement (Rectangle visibleRect, int orientation, int direction) {
        return 20;
    }
    
    private ModelListener getModelListener() {
        if( null == modelListener ) {
            modelListener = new ModelListener() {
                public void categoriesAdded( Category[] addedCategories ) {
                    PalettePanel.this.refresh();
                    if( null != addedCategories && addedCategories.length > 0 ) {
                        PalettePanel.this.scrollToCategory(addedCategories[0] );
                    }
                }

                public void categoriesRemoved( Category[] removedCategories ) {
                    PalettePanel.this.refresh();
                }

                public void categoriesReordered() {
                    PalettePanel.this.refresh();
                }
                
                public void propertyChange( PropertyChangeEvent evt ) {
                    if( ModelListener.PROP_SELECTED_ITEM.equals( evt.getPropertyName() ) ) {
                        Item selectedItem = model.getSelectedItem();
                        Category selectedCategory = model.getSelectedCategory();
                        setSelectedItemFromModel( selectedCategory, selectedItem );
                    }
                }

            };
        } 
        return modelListener;
    }
    
    private PropertyChangeListener getSettingsListener() {
        if( null == settingsListener ) {
            settingsListener = new PropertyChangeListener() {
                public void propertyChange( PropertyChangeEvent evt ) {
                    if( evt.getPropertyName().equals( PaletteController.ATTR_IS_VISIBLE ) ) {
                        PalettePanel.this.refresh();
                        for( int i=0; null != descriptors && i<descriptors.length; i++ ) {
                            descriptors[i].computeItems();
                        }
                    }
                }

            };
        } 
        return settingsListener;
    }

    private boolean isItemInCategory( Category category, Item item ) {
        Item[] categoryItems = category.getItems();
        for( int i=0; i<categoryItems.length; i++ ) {
            if( item.equals( categoryItems[i] ) )
                return true;
        }
        return false;
    }
    
    Model getModel() {
        return model;
    }
    
    Settings getSettings() {
        return settings;
    }
    
    PaletteController getController() {
        return controller;
    }
    
    private class PaletteLayoutManager implements LayoutManager {
        
        public void addLayoutComponent( String name, Component comp) {
        }
        
        public void layoutContainer( Container parent ) {
            int width = getWidth ();

            int height = 0;
            for( int i=0; i<descriptors.length; i++ ) {
                CategoryDescriptor paletteCategoryDescriptor = descriptors[i];
                paletteCategoryDescriptor.setPositionY( height );
                JComponent comp = paletteCategoryDescriptor.getComponent();
                comp.setSize( width, comp.getPreferredSize().height );
                height += paletteCategoryDescriptor.getComponent().getHeight();
            }
        }
        
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(0, 0);
        }
        
        public Dimension preferredLayoutSize(Container parent) {
            int height = 0;
            for( int i=0; i<descriptors.length; i++ ) {
                CategoryDescriptor descriptor = descriptors[i];
                height += descriptor.getPreferredHeight()+1;
            }
            return new Dimension( 10 /* not used - tracks viewports width*/, height );
        }
        
        public void removeLayoutComponent(Component comp) {
        }
    }
}
