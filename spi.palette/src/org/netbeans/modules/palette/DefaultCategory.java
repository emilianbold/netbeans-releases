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

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Action;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.CategoryListener;
import org.netbeans.modules.palette.Item;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.modules.palette.ui.DnDSupport;
import org.openide.ErrorManager;
import org.openide.nodes.*;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.PasteType;


/**
 * PaletteCategory implementation based on Nodes.
 *
 * @author S. Aubrecht
 */
public class DefaultCategory implements Category, NodeListener {
    
    private Node categoryNode;
    private ArrayList categoryListeners = new ArrayList( 3 );
    private Item[] items;
    
    /** 
     * Creates a new instance of DefaultPaletteCategory 
     *
     * @param categoryNode Node representing the category.
     */
    public DefaultCategory( Node categoryNode ) {
        this.categoryNode = categoryNode;
        this.categoryNode.addNodeListener( this );
        this.categoryNode.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                notifyListeners();
            }
        });
    }

    public Image getIcon(int type) {
        return categoryNode.getIcon( type );
    }

    public void addCategoryListener( CategoryListener listener ) {
        synchronized( categoryListeners ) {
            categoryListeners.add( listener );
        }
    }

    public void removeCategoryListener( CategoryListener listener ) {
        synchronized( categoryListeners ) {
            categoryListeners.remove( listener );
        }
    }

    public Action[] getActions() {
        return categoryNode.getActions( false );
    }

    public String getShortDescription() {
        return categoryNode.getShortDescription();
    }

    public Item[] getItems() {
        if( null == items ) {
            Node[] children = categoryNode.getChildren().getNodes( DefaultModel.canBlock() );
            items = new Item[children.length];
            for( int i=0; i<children.length; i++ ) {
                items[i] = new DefaultItem( children[i] );
            }
        }
        return items;
    }

    public String getName() {
        return categoryNode.getName();
    }

    public String getDisplayName() {
        return categoryNode.getDisplayName();
    }

    protected void notifyListeners() {
        CategoryListener[] listeners;
        synchronized( categoryListeners ) {
            listeners = new CategoryListener[categoryListeners.size()];
            listeners = (CategoryListener[])categoryListeners.toArray( (Object[])listeners );
        }
        for( int i=0; i<listeners.length; i++ ) {
            listeners[i].categoryModified( this );
        }
    }
    
    /** Fired when a set of new children is added.
    * @param ev event describing the action
    */
    public void childrenAdded(NodeMemberEvent ev) {
        items = null;
        notifyListeners();
    }

    /** Fired when a set of children is removed.
    * @param ev event describing the action
    */
    public void childrenRemoved(NodeMemberEvent ev) {
        items = null;
        notifyListeners();
    }

    /** Fired when the order of children is changed.
    * @param ev event describing the change
    */
    public void childrenReordered(NodeReorderEvent ev) {
        items = null;
        notifyListeners();
    }

    /** Fired when the node is deleted.
    * @param ev event describing the node
    */
    public void nodeDestroyed(NodeEvent ev) {
        categoryNode.removeNodeListener( this );
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if( Node.PROP_DISPLAY_NAME.equals( evt.getPropertyName() ) ) {
            notifyListeners();
        }
    }

    public boolean equals(Object obj) {
        if( !(obj instanceof DefaultCategory) )
            return false;
        
        return categoryNode.equals( ((DefaultCategory) obj).categoryNode );
    }

    public Transferable getTransferable() {
        try {
            return categoryNode.drag();
        } catch( IOException ioE ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, ioE );
        }
        return null;
    }
    
    public Lookup getLookup() {
        return categoryNode.getLookup();
    }
    
    private int itemToIndex( Item item ) {
        if( null == item ) {
            return -1;
        }
        Node node = (Node)item.getLookup().lookup( Node.class );
        if( null != node ) {
            Node[] nodes = categoryNode.getChildren().getNodes( DefaultModel.canBlock() );
            for( int i=0; i<nodes.length; i++ ) {
                if( nodes[i].equals( node ) ) {
                    return i;
                }
            }
        }
//        categoryNode.getChildren().getNodes( DefaultModel.canBlock() );
//        Node node = (Node)item.getLookup().lookup( Node.class );
//        if( null != node ) {
//            Index order = (Index)categoryNode.getCookie( Index.class );
//            if( null != order ) {
//                return order.indexOf( node );
//            }
//        }
        return -1;
    }
    
    public boolean moveItem( Item source, Item target, boolean moveBefore ) {
        int sourceIndex = itemToIndex( source );
        int targetIndex = itemToIndex( target );
        if( !moveBefore ) {
            targetIndex++;
        }
        if( sourceIndex >= 0 && targetIndex >= 0 && sourceIndex != targetIndex ) {
            if( sourceIndex < targetIndex ) {
                targetIndex--;
            }
            Index order = (Index)categoryNode.getCookie( Index.class );
            if( null == order ) {
                return false;
            }
            order.move( sourceIndex, targetIndex );
            return true;
        }
        
        return false;
    }

    public boolean dragOver( DropTargetDragEvent e ) {
        boolean res = e.isDataFlavorSupported( PaletteController.ITEM_DATA_FLAVOR );
        DragAndDropHandler handler = getDragAndDropHandler();
        if( null != handler ) {
            res |= handler.canDrop( getLookup(), e.getCurrentDataFlavors(), e.getDropAction() );
        }
        return res;
    }

    public boolean dropItem( Transferable dropItem, int dndAction, Item target, boolean dropBefore ) {
        int targetIndex = itemToIndex( target );
        if( !dropBefore ) {
            targetIndex++;
        }
        DragAndDropHandler handler = getDragAndDropHandler();
        boolean res;
        if( null != handler ) {
            res = handler.doDrop( getLookup(), dropItem, dndAction, targetIndex );
        } else {
            res = performDefaultDrop( dropItem, dndAction, target, dropBefore, targetIndex );
        }
        return res;
    }
    
    private DragAndDropHandler getDragAndDropHandler() {
        return (DragAndDropHandler)categoryNode.getLookup().lookup( DragAndDropHandler.class );
    }
    
    private boolean performDefaultDrop( Transferable dropItem, int dndAction, Item target, boolean dropBefore, int targetIndex ) {
        try {
            PasteType paste = categoryNode.getDropType( dropItem, dndAction, targetIndex );
            if( null != paste ) {
                Item[] itemsBefore = getItems();
                paste.paste();
                items = null;
                Item[] itemsAfter = getItems();
                
                if( itemsAfter.length == itemsBefore.length+1 ) {
                    Item newItem = null;
                    for( int i=itemsAfter.length-1; i>=0; i-- ) {
                        newItem = itemsAfter[i];
                        for( int j=0; j<itemsBefore.length; j++ ) {
                            if( newItem.equals( itemsBefore[j] ) ) {
                                newItem = null;
                                break;
                            }
                        }
                        if( null != newItem ) {
                            break;
                        }
                    }
                    if( null != newItem && targetIndex >= 0 ) {
                        moveItem( newItem, target, dropBefore );
                    }
                }
                return true;
            }
        } catch( IOException ioE ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, ioE );
        }
        return false;
    }
}
