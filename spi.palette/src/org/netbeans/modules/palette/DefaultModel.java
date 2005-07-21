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

package org.netbeans.modules.palette;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import javax.swing.Action;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteFilter;
import org.openide.nodes.*;
import org.openide.util.*;
import org.netbeans.modules.palette.ui.Customizer;
import org.openide.util.lookup.ProxyLookup;

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
    private ArrayList modelListeners = new ArrayList( 3 );
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
            Node catNode = (Node)category.lookup( Node.class );
            if( null != catNode ) {
                cat = new DefaultCategory( catNode );
            }
        }
        if( null != item ) {
            Node itNode = (Node)item.lookup( Node.class );
            if( null != itNode ) {
                it = new DefaultItem( itNode );
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

    
    public Category[] getCategories() {
        if( null == categories ) {
            Node[] nodes = rootNode.getChildren().getNodes( canBlock() );
            categories = nodes2categories( nodes );
        }
        return categories;
    }
    
    static boolean canBlock() {
        return !Children.MUTEX.isReadAccess() && !Children.MUTEX.isWriteAccess ();
    }

    /** Fired when a set of new children is added.
    * @param ev event describing the action
    */
    public void childrenAdded(NodeMemberEvent ev) {
        categories = null;
        Category[] addedCategories = nodes2categories( ev.getDelta() );
        fireCategoriesChanged( addedCategories, true );
    }

    /** Fired when a set of children is removed.
    * @param ev event describing the action
    */
    public void childrenRemoved(NodeMemberEvent ev) {
        categories = null;
        Category[] removedCategories = nodes2categories( ev.getDelta() );
        fireCategoriesChanged( removedCategories, false );
    }

    /** Fired when the order of children is changed.
    * @param ev event describing the change
    */
    public void childrenReordered(NodeReorderEvent ev) {
        categories = null;
        fireCategoriesChanged( null, false );
    }

    /** Fired when the node is deleted.
    * @param ev event describing the node
    */
    public void nodeDestroyed(NodeEvent ev) {
        this.rootNode.removeNodeListener( this );
    }

    public void propertyChange(PropertyChangeEvent evt) {
    }

    private void fireCategoriesChanged( Category[] changedCategories, boolean added ) {
        ModelListener[] listeners;
        synchronized( modelListeners ) {
            listeners = new ModelListener[modelListeners.size()];
            listeners = (ModelListener[])modelListeners.toArray( (Object[])listeners );
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

    public void refresh() {
        clearSelection();
        categories = null;
        rootNode.refreshChildren();
    }
    
    public void showCustomizer( Settings settings ) {
        Customizer.show( rootNode, settings );
    }
    
    public Lookup getRoot() {
        return rootNode.getLookup();
    }
    
    public boolean moveCategory( Category source, Category target, boolean moveBefore ) {
        int sourceIndex = categoryToIndex( source );
        int targetIndex = categoryToIndex( target );
        if( !moveBefore ) {
            targetIndex++;
        }
        if( sourceIndex >= 0 && targetIndex >= 0 && sourceIndex != targetIndex ) {
            if( sourceIndex < targetIndex ) {
                targetIndex--;
            }
            Index order = (Index)rootNode.getCookie( Index.class );
            if( null == order ) {
                return false;
            }
            order.move( sourceIndex, targetIndex );
            return true;
        }
        
        return false;
    }

    private int categoryToIndex( Category category ) {
        Node node = (Node)category.getLookup().lookup( Node.class );
        if( null != node ) {
            Index order = (Index)rootNode.getCookie( Index.class );
            if( null != order ) {
                return order.indexOf( node );
            }
        }
        return -1;
    }

    public String getName() {
        return rootNode.getName();
    }
}
