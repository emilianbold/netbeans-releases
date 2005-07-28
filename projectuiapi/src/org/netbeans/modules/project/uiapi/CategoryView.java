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

package org.netbeans.modules.project.uiapi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.project.uiapi.Utilities;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hrebejk
 */
public class CategoryView extends JPanel implements ExplorerManager.Provider, PropertyChangeListener {
                
    private ExplorerManager manager;
    private BeanTreeView btv;
    private CategoryModel categoryModel;

    public CategoryView( CategoryModel categoryModel ) {

        this.categoryModel = categoryModel;

        // See #36315
        manager = new ExplorerManager();

        setLayout( new BorderLayout() );

        Dimension size = new Dimension( 220, 4 );
        btv = new BeanTreeView();    // Add the BeanTreeView
        btv.setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        btv.setPopupAllowed( false );
        btv.setRootVisible( false );
        btv.setDefaultActionAllowed( false );            
        btv.setMinimumSize( size );
        btv.setPreferredSize( size );
        btv.setMaximumSize( size );
        btv.setDragSource (false);
        this.add( btv, BorderLayout.CENTER );                        
        manager.setRootContext( createRootNode( categoryModel ) );
        manager.addPropertyChangeListener( this );
        categoryModel.addPropertyChangeListener( this );
        btv.expandAll();
        selectNode( categoryModel.getCurrentCategory() );

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CategoryView.class,"AN_CatgoryView"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CategoryView.class,"AD_CategoryView"));

    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }


    public void addNotify() {
        super.addNotify();
        btv.expandAll();
    }


    // Private methods -----------------------------------------------------

    private void selectNode( ProjectCustomizer.Category category ) {

        Node node = findNode( category, manager.getRootContext() );

        if ( node != null ) {                
            try {
                manager.setSelectedNodes( new Node[] { node } );
            }
            catch ( PropertyVetoException e ) {
                // No node will be selected
            }                
        }

    }   

    private Node findNode( ProjectCustomizer.Category category, Node node ) {

        Children ch = node.getChildren();;

        if ( ch != null && ch != Children.LEAF ) {
            Node nodes[] = ch.getNodes( true );

            if ( nodes != null ) {                    
                for( int i = 0; i < nodes.length; i++ ) {
                    ProjectCustomizer.Category cc = (ProjectCustomizer.Category)nodes[i].getLookup().lookup( ProjectCustomizer.Category.class );

                    if ( cc == category ) {
                        return nodes[i];
                    }
                    else {
                        Node n = findNode( category, nodes[i] );
                        if ( n != null ) {
                            return n;
                        }
                    }                                                
                }
            }
        }

        return null;
    }


    private Node createRootNode( CategoryModel categoryModel ) {            
        ProjectCustomizer.Category rootCategory = ProjectCustomizer.Category.create( "root", "root", null, categoryModel.getCategories() ); // NOI18N           
        return new CategoryNode( rootCategory );
    }

    // Implementation of property change listener --------------------------

    public void propertyChange(PropertyChangeEvent evt) {

        Object source = evt.getSource();
        String propertyName = evt.getPropertyName();

        if ( source== manager && ExplorerManager.PROP_SELECTED_NODES.equals( propertyName ) ) {
            Node nodes[] = manager.getSelectedNodes(); 
            if ( nodes == null || nodes.length <= 0 ) {
                return;
            }
            Node node = nodes[0];

            ProjectCustomizer.Category category = (ProjectCustomizer.Category) node.getLookup().lookup( ProjectCustomizer.Category.class );
            if ( category != categoryModel.getCurrentCategory() ) {
                categoryModel.setCurrentCategory( category );
            }
        }

        if ( source == categoryModel && CategoryModel.PROP_CURRENT_CATEGORY.equals( propertyName ) ) {
            selectNode( (ProjectCustomizer.Category)evt.getNewValue() );
        }

    }


    // Private Inner classes -----------------------------------------------

    /** Node to be used for configuration
     */
    private static class CategoryNode extends AbstractNode implements PropertyChangeListener {

        private Image icon = org.openide.util.Utilities.loadImage( "org/netbeans/modules/project/uiapi/defaultCategory.gif" ); // NOI18N    

        private ProjectCustomizer.Category category;

        public CategoryNode( ProjectCustomizer.Category category ) {
            super( ( category.getSubcategories() == null || category.getSubcategories().length == 0 ) ? 
                        Children.LEAF : new CategoryChildren( category.getSubcategories() ), 
                   Lookups.fixed( new Object[] { category } ) );
            setName( category.getName() );
            this.category = category;
            setDisplayName( category.getDisplayName() );
            
            if ( category.getIcon() != null ) {
                this.icon = category.getIcon(); 
            }
            Utilities.getCategoryChangeSupport(category).addPropertyChangeListener(this);
        }

        public String getHtmlDisplayName() {
            return category.isValid() ? null :
                "<html><font color=\"!nb.errorForeground\">" + // NOI18N
                    category.getDisplayName() + "</font></html>"; // NOI18N
        }
        
        public Image getIcon( int type ) {
            return this.icon;
        }

        public Image getOpenedIcon( int type ) {
            return getIcon( type );
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == CategoryChangeSupport.VALID_PROPERTY) {
                fireDisplayNameChange(null, null);
            }
        }
        
    }

    /** Children used for configuration
     */
    private static class CategoryChildren extends Children.Keys {

        private Collection descriptions;

        public CategoryChildren( ProjectCustomizer.Category[] descriptions ) {
            this.descriptions = Arrays.asList( descriptions );
        }

        // Children.Keys impl --------------------------------------------------

        public void addNotify() {
            setKeys( descriptions );
        }

        public void removeNotify() {
            setKeys( Collections.EMPTY_LIST );
        }

        protected Node[] createNodes( Object key ) {
            return new Node[] { new CategoryNode( (ProjectCustomizer.Category)key ) };
        }
    }        

}
            
 
