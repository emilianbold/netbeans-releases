/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.palette.ui;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.ModelListener;
import org.netbeans.modules.palette.Settings;
import org.netbeans.modules.palette.Utils;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.Utilities;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;



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

        if (!GraphicsEnvironment.isHeadless()) {
            dndSupport = new DnDSupport(this);
        }

        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) )
            setBackground( UIManager.getColor("NbExplorerView.background") ); //NOI18N
        else
            setBackground( UIManager.getColor ("Panel.background") );
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
            scrollPane.setBorder( BorderFactory.createEmptyBorder() );
            scrollPane.addMouseListener( mouseListener() );
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) )
                scrollPane.getViewport().setBackground( UIManager.getColor("NbExplorerView.background") ); //NOI18N
            else
                scrollPane.getViewport().setBackground( UIManager.getColor ("Panel.background") );
            // GTK L&F paints extra border around viewport, get rid of it
            scrollPane.setViewportBorder(null);
        }
        return scrollPane;
    }

    CategoryDescriptor getCategoryDescriptor( Category category ) {
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
                descriptor.setShowNames( getSettings().getShowItemNames() );
                descriptor.setIconSize( getSettings().getIconSize() );
            } else {
                descriptor.refresh();
            }
            descriptor.setWidth( getWidth() );
            descriptors[i] = descriptor;
        }
        return descriptors;
    }
    
    private Category[] getVisibleCategories( Category[] cats ) {
        ArrayList<Category> tmp = new ArrayList<Category>( cats.length );
        for( int i=0; i<cats.length; i++ ) {
            if( settings.isVisible( cats[i] ) ) {
                tmp.add( cats[i] );
            }
        }
        return tmp.toArray( new Category[tmp.size()] );
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
                if (dndSupport != null) {
                    dndSupport.remove(descriptor);
                }
            }
        }
        for( int i=0; i<paletteCategoryDescriptors.length; i++ ) {
            CategoryDescriptor paletteCategoryDescriptor = paletteCategoryDescriptors[i];
            if( !arrayContains( descriptors, paletteCategoryDescriptor ) ) {
                add( paletteCategoryDescriptor.getComponent() );
                if (dndSupport != null) {
                    dndSupport.add(paletteCategoryDescriptor);
                }
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
                        setIconSize( settings.getIconSize() );
                        setShowItemNames( settings.getShowItemNames() );
                        setItemWidth( settings.getShowItemNames() ? settings.getItemWidth() : -1 );
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
        descriptor.setSelectedItem( item );
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
            selectedCategory = null;
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
                        Utils.addCustomizationMenuItems( popup, getController(), getSettings() );
                        popup.show( (Component)event.getSource(), event.getX(), event.getY() );
                    }
                }
            };
        }
        return mouseListener;
    }

    private void setShowItemNames( boolean showNames ) {
        for( int i=0; i<descriptors.length; i++ ) {
            descriptors[i].setShowNames( showNames );
        }
        repaint();
    }

    private void setIconSize(int iconSize) {
        for( int i=0; i<descriptors.length; i++ ) {
            descriptors[i].setIconSize( iconSize );
        }
        repaint();
    }
    
    private void setItemWidth(int itemWidth) {
        for( int i=0; i<descriptors.length; i++ ) {
            descriptors[i].setItemWidth( itemWidth );
        }
        repaint();
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
    
    public HelpCtx getHelpCtx() {
        HelpCtx ctx = null;
        if( null != getModel() ) {
            Item selItem = getModel().getSelectedItem();
            if( null != selItem ) {
                Node selNode = (Node) selItem.getLookup().lookup( Node.class );
                if( null != selNode )
                    ctx = selNode.getHelpCtx();
            } 
            if( null == ctx || HelpCtx.DEFAULT_HELP.equals( ctx ) ) {
                //find the selected category
                CategoryDescriptor selCategory = null;
                for( int i=0; i<descriptors.length; i++ ) {
                    if( descriptors[i].isSelected() ) {
                        selCategory = descriptors[i];
                        break;
                    }
                }
                if( null != selCategory ) {
                    Node selNode = (Node) selCategory.getCategory().getLookup().lookup( Node.class );
                    if( null != selNode )
                        ctx = selNode.getHelpCtx();
                }
            }
            if( null == ctx || HelpCtx.DEFAULT_HELP.equals( ctx ) ) {
                Node selNode = (Node) getModel().getRoot().lookup( Node.class );
                if( null != selNode )
                    ctx = selNode.getHelpCtx();
            }
        }
        if( null == ctx || HelpCtx.DEFAULT_HELP.equals( ctx ) ) {
            ctx = new HelpCtx("CommonPalette"); // NOI18N
        }
        return ctx;
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
                    if( PaletteController.ATTR_IS_VISIBLE.equals( evt.getPropertyName() ) ) {
                        PalettePanel.this.refresh();
                        for( int i=0; null != descriptors && i<descriptors.length; i++ ) {
                            descriptors[i].computeItems();
                        }
                    } else if( PaletteController.ATTR_ICON_SIZE.equals( evt.getPropertyName() ) ) {
                        
                        setIconSize( getSettings().getIconSize() );
                        
                    } else if( PaletteController.ATTR_SHOW_ITEM_NAMES.equals( evt.getPropertyName() ) ) {
                        
                        setShowItemNames( getSettings().getShowItemNames() );
                        setItemWidth( getSettings().getShowItemNames() ? getSettings().getItemWidth() : -1 );
                        
                    }
                }

            };
        } 
        return settingsListener;
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

    public void updateUI() {
        super.updateUI();
        if( null != model )
            model.refresh();
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
            int width = getWidth();
            for( int i=0; i<descriptors.length; i++ ) {
                CategoryDescriptor descriptor = descriptors[i];
                height += descriptor.getPreferredHeight( width )+1;
            }
            return new Dimension( 10 /* not used - tracks viewports width*/, height );
        }
        
        public void removeLayoutComponent(Component comp) {
        }
    }
}
