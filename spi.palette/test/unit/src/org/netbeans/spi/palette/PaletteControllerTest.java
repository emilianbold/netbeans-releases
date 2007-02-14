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

package org.netbeans.spi.palette;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import junit.framework.TestCase;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.Settings;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Stanislav Aubrecht
 */
public class PaletteControllerTest extends TestCase {

    private PaletteController controller;
    private Node rootNode;
    private DummyActions actions;
    private Model model;
    private Settings settings;
    
    public PaletteControllerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        actions = new DummyActions();
        rootNode = DummyPalette.createPaletteRoot();
        controller = PaletteFactory.createPalette( rootNode, actions );
        model = controller.getModel();
        settings = controller.getSettings();
    }

    /**
     * Test of addPropertyChangeListener method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testAddPropertyChangeListener() {
        //make sure nothing is selected by default
        model.clearSelection();
        
        MyPropertyChangeListener myListener = new MyPropertyChangeListener();
        controller.addPropertyChangeListener( myListener );
        
        Category cat = model.getCategories()[0];
        Item item = cat.getItems()[0];
        
        model.setSelectedItem( cat.getLookup(), item.getLookup() );
        
        assertEquals( PaletteController.PROP_SELECTED_ITEM, myListener.getPropertyName() );
        assertEquals( item.getLookup(), myListener.getValue() );
        
        myListener.clear();
        model.clearSelection();

        assertEquals( PaletteController.PROP_SELECTED_ITEM, myListener.getPropertyName() );
        assertEquals( Lookup.EMPTY, myListener.getValue() );
    }

    /**
     * Test of removePropertyChangeListener method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testRemovePropertyChangeListener() {
        //make sure nothing is selected by default
        model.clearSelection();
        
        MyPropertyChangeListener myListener = new MyPropertyChangeListener();
        controller.addPropertyChangeListener( myListener );
        
        Category cat = model.getCategories()[0];
        Item item = cat.getItems()[0];
        
        model.setSelectedItem( cat.getLookup(), item.getLookup() );
        
        assertEquals( PaletteController.PROP_SELECTED_ITEM, myListener.getPropertyName() );
        assertEquals( item.getLookup(), myListener.getValue() );
        
        controller.removePropertyChangeListener( myListener );
        myListener.clear();
        model.clearSelection();

        assertEquals( null, myListener.getPropertyName() );
        assertEquals( null, myListener.getValue() );
    }

    /**
     * Test of getSelectedItem method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testGetSelectedItem() {
        //make sure nothing is selected by default
        model.clearSelection();
        
        assertEquals( Lookup.EMPTY, controller.getSelectedItem() );
        
        Category cat = model.getCategories()[0];
        Item item = cat.getItems()[0];
        model.setSelectedItem( cat.getLookup(), item.getLookup() );
        
        assertEquals( item.getLookup(), controller.getSelectedItem() );
        
        cat = model.getCategories()[3];
        item = cat.getItems()[5];
        model.setSelectedItem( cat.getLookup(), item.getLookup() );

        assertEquals( item.getLookup(), controller.getSelectedItem() );

        model.clearSelection();

        assertEquals( Lookup.EMPTY, controller.getSelectedItem() );
    }

    /**
     * Test of getSelectedCategory method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testGetSelectedCategory() {
        //make sure nothing is selected by default
        model.clearSelection();
        
        assertEquals( Lookup.EMPTY, controller.getSelectedItem() );
        
        Category cat = model.getCategories()[0];
        Item item = cat.getItems()[0];
        model.setSelectedItem( cat.getLookup(), item.getLookup() );
        
        assertEquals( cat.getLookup(), controller.getSelectedCategory() );
        
        cat = model.getCategories()[0];
        item = cat.getItems()[5];
        model.setSelectedItem( cat.getLookup(), item.getLookup() );

        assertEquals( cat.getLookup(), controller.getSelectedCategory() );

        cat = model.getCategories()[4];
        item = cat.getItems()[6];
        model.setSelectedItem( cat.getLookup(), item.getLookup() );

        assertEquals( cat.getLookup(), controller.getSelectedCategory() );

        model.clearSelection();

        assertEquals( Lookup.EMPTY, controller.getSelectedCategory() );
    }

    /**
     * Test of clearSelection method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testClearSelection() {
        //make sure nothing is selected by default
        model.clearSelection();
        
        assertEquals( Lookup.EMPTY, controller.getSelectedItem() );
        
        Category cat = model.getCategories()[0];
        Item item = cat.getItems()[0];
        model.setSelectedItem( cat.getLookup(), item.getLookup() );
        
        assertEquals( cat.getLookup(), controller.getSelectedCategory() );
        
        controller.clearSelection();
        assertEquals( Lookup.EMPTY, controller.getSelectedCategory() );
        
        controller.clearSelection();
        assertEquals( Lookup.EMPTY, controller.getSelectedCategory() );
    }

    /**
     * Test of resetPalette method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testResetPalette() {
//        System.out.println("testResetPalette");
//        
//        PaletteController instance = null;
//        
//        instance.resetPalette();
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
    }

    /**
     * Test of setPaletteFilter method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testRefresh() throws IOException {
        MyPaletteFilter filter = new MyPaletteFilter( false );
        
        PaletteController myController = PaletteFactory.createPalette( DummyPalette.createPaletteRoot(), new DummyActions(), filter, null );
        
        Model myModel = myController.getModel();
        
        
        Category[] categories = myModel.getCategories();
        assertEquals( 9, categories.length );
        for( int i=0; i<categories.length; i++ ) {
            assertEquals( 9, categories[i].getItems().length );
        }
        
        filter.setEnabled( true );
        myController.refresh();
        
        categories = myModel.getCategories();
        for( int i=0; i<categories.length; i++ ) {
            //System.out.println( categories[i].getName() );
            assertTrue( filter.isValidName( categories[i].getName() ) );
            
            Item[] items = categories[i].getItems();
            for( int j=0; j<items.length; j++ ) {
                //System.out.println( items[j].getName() );
                assertTrue( filter.isValidName( items[j].getName() ) );
            }
        }
        
        filter.setEnabled( false );
        myController.refresh();
        
        categories = myModel.getCategories();
        assertEquals( 9, categories.length );
        for( int i=0; i<categories.length; i++ ) {
            assertEquals( 9, categories[i].getItems().length );
        }
    }

    /**
     * Test of showCustomizer method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testShowCustomizer() {
        ProxyModel myModel = new ProxyModel( model );
        controller.setModel( myModel );
        
        controller.showCustomizer();
        
        assertTrue( myModel.showCustomizerCalled );
    }

    /**
     * Test of getRoot method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testGetRoot() {
        assertEquals( rootNode.getName(), controller.getRoot().lookup( Node.class ).getName() );
    }

    private static class MyPropertyChangeListener implements PropertyChangeListener {
        private String propertyName;
        private Object newValue;
        
        public void propertyChange( PropertyChangeEvent evt ) {
            propertyName = evt.getPropertyName();
            newValue = evt.getNewValue();
        }
        
        public String getPropertyName() {
            return propertyName;
        }
        
        public Object getValue() {
            return newValue;
        }
        
        public void clear() {
            propertyName = null;
            newValue = null;
        }
    }
    
    private static class MyPaletteFilter extends PaletteFilter {
        
        private boolean isEnabled;
        
        public MyPaletteFilter( boolean enabled ) {
            this.isEnabled = enabled;
        }
        
        public boolean isValidItem(Lookup lkp) {
            if( !isEnabled )
                return true;
            
            Node node = (Node)lkp.lookup( Node.class );
            
            return nodeNameEndsWith1or2or3( node );
        }

        public boolean isValidCategory(Lookup lkp) {
            if( !isEnabled )
                return true;
            
            Node node = (Node)lkp.lookup( Node.class );
            
            return nodeNameEndsWith1or2or3( node );
        }
        
        private boolean nodeNameEndsWith1or2or3( Node node ) {
            if( null == node )
                return false;
            
            return isValidName( node.getName() );
        }
        
        public boolean isValidName( String name ) {
            if( null == name )
                return false;
            
            return name.endsWith( "1" ) || name.endsWith( "2" ) || name.endsWith( "3" );
        }
        
        public void setEnabled( boolean enable ) {
            this.isEnabled = enable;
        }
    }
}
