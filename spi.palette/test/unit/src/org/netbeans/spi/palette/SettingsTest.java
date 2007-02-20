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

import java.beans.BeanInfo;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.Settings;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author S. Aubrecht
 */
public class SettingsTest extends AbstractPaletteTestHid {

    public SettingsTest(String testName) {
        super(testName);
    }

    public void testItemVisible() throws Exception {
        FileObject item1 = getItemFile( categoryNames[0], itemNames[0][0] );
        FileObject item2 = getItemFile( categoryNames[0], itemNames[0][1] );
        FileObject item3 = getItemFile( categoryNames[0], itemNames[0][2] );
        
        item2.setAttribute( PaletteController.ATTR_IS_VISIBLE, new Boolean(false) );
        item3.setAttribute( PaletteController.ATTR_IS_VISIBLE, new Boolean(true) );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Settings settings = pc.getSettings();
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        Item[] items = categories[0].getItems();
        
        assertTrue( "Items are visible by default", settings.isVisible( items[0] ) );
        assertTrue( !settings.isVisible( items[1] ) );
        assertTrue( settings.isVisible( items[2] ) );
        
        settings.setVisible( items[0], false );
        settings.setVisible( items[1], true );
        settings.setVisible( items[2], false );

        assertTrue( !settings.isVisible( items[0] ) );
        assertTrue( settings.isVisible( items[1] ) );
        assertTrue( !settings.isVisible( items[2] ) );
    }

    public void testCategoryVisible() throws Exception {
        FileObject cat1 = getCategoryFile( categoryNames[0] );
        FileObject cat2 = getCategoryFile( categoryNames[1] );
        FileObject cat3 = getCategoryFile( categoryNames[2] );
        
        cat2.setAttribute( PaletteController.ATTR_IS_VISIBLE, new Boolean(false) );
        cat3.setAttribute( PaletteController.ATTR_IS_VISIBLE, new Boolean(true) );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Settings settings = pc.getSettings();
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        assertTrue( "Categories are visible by default", settings.isVisible( categories[0] ) );
        assertTrue( !settings.isVisible( categories[1] ) );
        assertTrue( settings.isVisible( categories[2] ) );
        
        settings.setVisible( categories[0], false );
        settings.setVisible( categories[1], true );
        settings.setVisible( categories[2], false );

        assertTrue( !settings.isVisible( categories[0] ) );
        assertTrue( settings.isVisible( categories[1] ) );
        assertTrue( !settings.isVisible( categories[2] ) );
    }

    public void testCategoryReadonly() throws Exception {
        FileObject cat1 = getCategoryFile( categoryNames[0] );
        FileObject cat2 = getCategoryFile( categoryNames[1] );
        FileObject cat3 = getCategoryFile( categoryNames[2] );
        
        cat2.setAttribute( PaletteController.ATTR_IS_READONLY, new Boolean(true) );
        cat3.setAttribute( PaletteController.ATTR_IS_READONLY, new Boolean(false) );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        Node node1 = (Node)categories[0].getLookup().lookup( Node.class );
        Node node2 = (Node)categories[1].getLookup().lookup( Node.class );
        Node node3 = (Node)categories[2].getLookup().lookup( Node.class );
        
        assertTrue( "Categories removable by default", node1.canDestroy() );
        assertTrue( !node2.canDestroy() );
        assertTrue( node3.canDestroy() );
    }

    public void testItemReadonly() throws Exception {
        FileObject item1 = getItemFile( categoryNames[0], itemNames[0][0] );
        FileObject item2 = getItemFile( categoryNames[0], itemNames[0][1] );
        FileObject item3 = getItemFile( categoryNames[0], itemNames[0][2] );
        
        item2.setAttribute( PaletteController.ATTR_IS_READONLY, new Boolean(true) );
        item3.setAttribute( PaletteController.ATTR_IS_READONLY, new Boolean(false) );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Item[] items = model.getCategories()[0].getItems();
        
        Node node1 = (Node)items[0].getLookup().lookup( Node.class );
        Node node2 = (Node)items[1].getLookup().lookup( Node.class );
        Node node3 = (Node)items[2].getLookup().lookup( Node.class );
        
        assertTrue( "Items removable by default", node1.canDestroy() );
        assertTrue( !node2.canDestroy() );
        assertTrue( node3.canDestroy() );
    }

    public void testCategoryExpanded() throws Exception {
        FileObject cat1 = getCategoryFile( categoryNames[0] );
        FileObject cat2 = getCategoryFile( categoryNames[1] );
        FileObject cat3 = getCategoryFile( categoryNames[2] );
        
        cat2.setAttribute( PaletteController.ATTR_IS_EXPANDED, new Boolean(true) );
        cat3.setAttribute( PaletteController.ATTR_IS_EXPANDED, new Boolean(false) );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Settings settings = pc.getSettings();
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        assertTrue( "Categories are collapsed by default", !settings.isExpanded( categories[0] ) );
        assertTrue( settings.isExpanded( categories[1] ) );
        assertTrue( !settings.isExpanded( categories[2] ) );
        
        settings.setExpanded( categories[0], true );
        settings.setExpanded( categories[1], false );
        settings.setExpanded( categories[2], true );

        assertTrue( settings.isExpanded( categories[0] ) );
        assertTrue( !settings.isExpanded( categories[1] ) );
        assertTrue( settings.isExpanded( categories[2] ) );
    }
    
    public void testShowItemNamesTrue() throws Exception {
        paletteRootFolder.setAttribute( PaletteController.ATTR_SHOW_ITEM_NAMES, new Boolean(true) );
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Settings settings = pc.getSettings();

        assertTrue( settings.getShowItemNames() );
        
        settings.setShowItemNames( false );
        
        assertTrue( !settings.getShowItemNames() );
    }
    
    public void testShowItemNamesFalse() throws Exception {
        paletteRootFolder.setAttribute( PaletteController.ATTR_SHOW_ITEM_NAMES, new Boolean(false) );
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Settings settings = pc.getSettings();

        assertTrue( !settings.getShowItemNames() );
        
        settings.setShowItemNames( true );
        
        assertTrue( settings.getShowItemNames() );
    }
    
    public void testShowItemNamesDefault() throws Exception {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Settings settings = pc.getSettings();

        assertTrue( "ShowItemNames is on by default", settings.getShowItemNames() );
    }
    
    public void testIconSizeDefault() throws Exception {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Settings settings = pc.getSettings();

        assertEquals( "Small icons are default", BeanInfo.ICON_COLOR_16x16, settings.getIconSize() );
    }
    
    public void testIconSizeCustom() throws Exception {
        paletteRootFolder.setAttribute( PaletteController.ATTR_ICON_SIZE, new Integer(123) );
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Settings settings = pc.getSettings();

        assertEquals( 123, settings.getIconSize() );
        
        settings.setIconSize( 321 );
        
        assertEquals( 321, settings.getIconSize() );
    }
    
    public void testItemWidthDefault() throws Exception {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Settings settings = pc.getSettings();

        assertEquals( "No item width is specified by default", -1, settings.getItemWidth() );
    }
    
    public void testItemWidthCustom() throws Exception {
        paletteRootFolder.setAttribute( PaletteController.ATTR_ITEM_WIDTH, new Integer(123) );
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Settings settings = pc.getSettings();

        assertEquals( 123, settings.getItemWidth() );
    }
}
