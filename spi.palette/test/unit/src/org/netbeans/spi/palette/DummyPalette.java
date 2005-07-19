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

package org.netbeans.spi.palette;

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Utilities;


/**
 *
 * @author Stanislav Aubrecht
 */
class DummyPalette {
    
    private static final int CATEGORY_COUNT = 9;
    private static final int ITEM_COUNT = 9;
    
    /** Creates a new instance of DummyPalette */
    private DummyPalette() {
    }
    
    public static Node createPaletteRoot() {
        Children categories = new Children.Array();
        categories.add( createCategories() );
        return new RootNode( categories );
    }
    
    private static Node[] createCategories() {
        Node[] categories = new Node[ CATEGORY_COUNT ];
        
        for( int i=0; i<categories.length; i++ ) {
            Children items = new Children.Array();
            items.add( createItems() );
            categories[i] = new CategoryNode( items, i );
        }
        return categories;
    }
    
    private static Node[] createItems() {
        Node[] items = new Node[ ITEM_COUNT ];
        
        for( int i=0; i<items.length; i++ ) {
            items[i] = new ItemNode( i );
        }
        return items;
    }

    private static class RootNode extends AbstractNode {
        public RootNode( Children children ) {
            super( children );
            setName( "DummyPalette" );
        }
    }
    
    private static class CategoryNode extends AbstractNode {
        public CategoryNode( Children children, int index ) {
            super( children );
            setName( "Category_" + index );
            setDisplayName( "CategoryName_" + index );
            setShortDescription( "Short category description " + index );
        }

        public Image getIcon(int type) {

            Image icon = null;
            try {
                URL url = new URL("nbres:/javax/swing/beaninfo/images/JTabbedPaneColor16.gif");
                icon = java.awt.Toolkit.getDefaultToolkit().getImage(url);
            } catch( MalformedURLException murlE ) {
            }
            return icon;
        }
    }
    
    private static class ItemNode extends AbstractNode {
        
        public ItemNode( int index ) {
            super( Children.LEAF );
            setName( "Item_" + index );
            setDisplayName( "ItemName_" + index );
            setShortDescription( "Short item description " + index );
        }

        public Image getIcon(int type) {

            Image icon = null;
            try {
                URL url = new URL("nbres:/javax/swing/beaninfo/images/JTabbedPaneColor16.gif");
                icon = java.awt.Toolkit.getDefaultToolkit().getImage(url);
            } catch( MalformedURLException murlE ) {
            }
            return icon;
        }
    }
}
