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

package org.netbeans.modules.palette;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import javax.swing.Action;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteController;
import org.openide.nodes.*;
import org.openide.util.*;
import org.netbeans.modules.palette.ui.Customizer;
import org.netbeans.spi.palette.PaletteActions;

/**
 * Default implementation of PaletteModel interface based on Nodes.
 *
 * @author S. Aubrecht
 */
public class DefaultModel implements Model, NodeListener {
    
    /**
     * Palette's root node. Its subnodes are palette categories.
     */
    private RootNode rootNode;
    
    /**
     * Item currently selected in the palette or null.
     */
    private Item selectedItem;
    /**
     * Category that owns the selected item or null.
     */
    private Category selectedCategory;
    private PropertyChangeSupport propertySupport;
    private ArrayList<ModelListener> modelListeners = new ArrayList<ModelListener>( 3 );
    private boolean categoriesNeedRefresh = true;
    /**
     * Cached categories
     */
    private Category[] categories;
    
    /** 
     * Creates a new instance of DefaultPaletteModel 
     *
     * @param rootNode Palette's root node.
     */
    public DefaultModel( RootNode rootNode ) {
        this.rootNode = rootNode;
        
        propertySupport = new PropertyChangeSupport( this );
        this.rootNode.addNodeListener( this );
    }

    public void setSelectedItem( Lookup category, Lookup item ) {
        Category cat = null;
        Item it = null;
        if( null != category ) {
            Node catNode = category.lookup( Node.class );
            if( null != catNode ) {
                cat = findCategory( catNode );
            }
        }
        if( null != item && null != cat ) {
            Node itNode = item.lookup( Node.class );
            if( null != itNode ) {
                it = findItem( cat, itNode );//new DefaultItem( itNode );
            }
        }
        
        Item oldValue = selectedItem;
        this.selectedItem = it;
        this.selectedCategory = cat;
        propertySupport.firePropertyChange( Model.PROP_SELECTED_ITEM, oldValue, selectedItem );
    }
    
    public void clearSelection() {
        setSelectedItem( null, null );
    }
    public Action[] getActions() {
        return rootNode.getActions( false );
    }

    public Item getSelectedItem() {
        return selectedItem;
    }
    
    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public void addModelListener( ModelListener listener ) {
        synchronized( modelListeners ) {
            modelListeners.add( listener );
            propertySupport.addPropertyChangeListener( listener );
        }
    }

    public void removeModelListener( ModelListener listener ) {
        synchronized( modelListeners ) {
            modelListeners.remove( listener );
            propertySupport.removePropertyChangeListener( listener );
        }
    }

    
    public synchronized Category[] getCategories() {
        if( null == categories || categoriesNeedRefresh ) {
            Node[] nodes = rootNode.getChildren().getNodes( canBlock() );
            categories = nodes2categories( nodes );
            categoriesNeedRefresh = false;
        }
        return categories;
    }
    
    public static boolean canBlock() {
        return !Children.MUTEX.isReadAccess() && !Children.MUTEX.isWriteAccess ();
    }

    /** Fired when a set of new children is added.
    * @param ev event describing the action
    */
    public void childrenAdded(NodeMemberEvent ev) {
        categoriesNeedRefresh = true;
        if( isRefreshingChildren )
            return;
        getCategories();
        Category[] addedCategories = findCategories( ev.getDelta() );
        fireCategoriesChanged( addedCategories, true );
    }

    /** Fired when a set of children is removed.
    * @param ev event describing the action
    */
    public void childrenRemoved(NodeMemberEvent ev) {
        Category[] removedCategories = findCategories( ev.getDelta() );
        categoriesNeedRefresh = true;
        fireCategoriesChanged( removedCategories, false );
    }

    /** Fired when the order of children is changed.
    * @param ev event describing the change
    */
    public void childrenReordered(NodeReorderEvent ev) {
        categoriesNeedRefresh = true;
        fireCategoriesChanged( null, false );
    }

    /** Fired when the node is deleted.
    * @param ev event describing the node
    */
    public synchronized void nodeDestroyed(NodeEvent ev) {
        this.rootNode.removeNodeListener( this );
    }

    public void propertyChange(PropertyChangeEvent evt) {
    }

    private void fireCategoriesChanged( Category[] changedCategories, boolean added ) {
        ModelListener[] listeners;
        synchronized( modelListeners ) {
            listeners = new ModelListener[modelListeners.size()];
            listeners = modelListeners.toArray( listeners );
        }
        for( int i=0; i<listeners.length; i++ ) {
            if( null != changedCategories ) {
                if( added ) {
                    listeners[i].categoriesAdded( changedCategories );
                } else {
                    listeners[i].categoriesRemoved( changedCategories );
                }
            } else {
                listeners[i].categoriesReordered();
            }
        }
    }

    /**
     * Wrap the given Nodes in PaletteCategory instances.
     */
    private Category[] nodes2categories( Node[] nodes ) {
        Category[] res = new Category[ nodes.length ];
        
        for( int i=0; i<res.length; i++ ) {
            res[i] = new DefaultCategory( nodes[i] );
        }
        
        return res;
    }
    
    private Category[] findCategories( Node[] nodes ) {
        Category[] res = new Category[ nodes.length ];

        Category[] current = getCategories();
        for( int i=0; i<res.length; i++ ) {
            boolean found = false;
            for( int j=0; !found && null != current && j<current.length; j++ ) {
                Node catNode = current[j].getLookup().lookup( Node.class );
                if( nodes[i].equals( catNode ) ) {
                    res[i] = current[j];
                    found = true;
                }
            }
            if( !found ) {
                res[i] = new DefaultCategory( nodes[i] );
            }
        }

        return res;
    }

    private boolean isRefreshingChildren = false;
    public void refresh() {
        synchronized( rootNode ) {
            PaletteActions customActions = rootNode.getLookup().lookup( PaletteActions.class );
            Action customRefreshAction = customActions.getRefreshAction();
            if( null != customRefreshAction ) {
                customRefreshAction.actionPerformed( new ActionEvent( getRoot(), 0, "refresh" ) ); //NOI18N
            }
            clearSelection();
            categoriesNeedRefresh = true;
            isRefreshingChildren = true;
            try {
                rootNode.refreshChildren();
            } finally {
                isRefreshingChildren = false;
            }
            fireCategoriesChanged( null, false );
        }
    }
    
    public void showCustomizer( PaletteController controller, Settings settings ) {
        Customizer.show( rootNode, controller, settings );
    }
    
    public Lookup getRoot() {
        return rootNode.getLookup();
    }
    
    public boolean moveCategory( Category source, Category target, boolean moveBefore ) {
        int targetIndex = categoryToIndex( target );
        if( !moveBefore ) {
            targetIndex++;
        }
        DragAndDropHandler handler = getDragAndDropHandler();
        return handler.moveCategory( source.getLookup(), targetIndex );
    }

    private int categoryToIndex( Category category ) {
        Node node = category.getLookup().lookup( Node.class );
        if( null != node ) {
            Index order = rootNode.getCookie( Index.class );
            if( null != order ) {
                return order.indexOf( node );
            }
        }
        return -1;
    }

    public String getName() {
        return rootNode.getName();
    }

    private Category findCategory( Node node ) {
        Category[] cats = getCategories();
        for( int i=0; i<cats.length; i++ ) {
            Node catNode = cats[i].getLookup().lookup( Node.class );
            if( null != catNode && catNode.equals( node ) )
                return cats[i];
        }
        return null;
    }
    
    private Item findItem( Category category, Node node ) {
        Item[] items = category.getItems();
        for( int i=0; i<items.length; i++ ) {
            Node itNode = items[i].getLookup().lookup( Node.class );
            if( null != itNode && itNode.equals( node ) )
                return items[i];
        }
        return null;
    }

    public boolean canReorderCategories() {
        return getDragAndDropHandler().canReorderCategories( rootNode.getLookup() );
    }
    
    private DragAndDropHandler getDragAndDropHandler() {
        return rootNode.getLookup().lookup( DragAndDropHandler.class );
    }
}
