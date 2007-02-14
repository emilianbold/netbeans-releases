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

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.util.Lookup;

/**
 *
 * @author Stanislav Aubrecht
 */
public class ModelTest extends AbstractPaletteTestHid {

    public ModelTest(String testName) {
        super(testName);
    }

    /**
     * Test of getName method, of class org.netbeans.modules.palette.Model.
     */
    public void testGetName() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( PALETTE_ROOT_FOLDER_NAME, new DummyActions() );
        Model model = pc.getModel();
        assertEquals( PALETTE_ROOT_FOLDER_NAME, model.getName() );
    }

    /**
     * Test of getCategories method, of class org.netbeans.modules.palette.Model.
     */
    public void testGetCategories() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( PALETTE_ROOT_FOLDER_NAME, new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        assertEquals( categoryNames.length, categories.length );
        for( int i=0; i<categories.length; i++ ) {
            assertEquals( categoryNames[i], categories[i].getName() );
        }
    }

    /**
     * Test of getActions method, of class org.netbeans.modules.palette.Model.
     */
    public void testGetActions() throws IOException {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( PALETTE_ROOT_FOLDER_NAME, actions );
        Model model = pc.getModel();
        Action[] modelActions = model.getActions();
        Action[] rootActions = actions.getCustomPaletteActions();
        for( int i=0; i<rootActions.length; i++ ) {
            if( null == rootActions[i] )
                continue;
            boolean found = false;
            for( int j=0; j<modelActions.length; j++ ) {
                if( null == modelActions[j] )
                    continue;
                if( modelActions[j].equals( rootActions[i] ) ) {
                    found = true;
                    break;
                }
            }
            assertTrue( "Action " + rootActions[i].getValue( Action.NAME ) + " not found in palette actions.", found );
        }
    }

    /**
     * Test of getSelectedItem method, of class org.netbeans.modules.palette.Model.
     */
    public void testSelectedItemAndCategory() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( PALETTE_ROOT_FOLDER_NAME, new DummyActions() );
        Model model = pc.getModel();

        assertNull( "No item is selected by default", model.getSelectedItem() );
        assertNull( "No category is selected by default", model.getSelectedCategory() );
        
        Category[] categories = model.getCategories();
        Category catToSelect = categories[3];
        Item itemToSelect = catToSelect.getItems()[4];
        
        model.setSelectedItem( catToSelect.getLookup(), itemToSelect.getLookup() );
        
        assertEquals( catToSelect, model.getSelectedCategory() );
        assertEquals( itemToSelect, model.getSelectedItem() );
        
        model.clearSelection();
        
        assertNull( "No item is selected after clearSelection()", model.getSelectedItem() );
        assertNull( "No category is selected after clearSelection()", model.getSelectedCategory() );
    }

    /**
     * Test of getRoot method, of class org.netbeans.modules.palette.Model.
     */
    public void testGetRoot() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( PALETTE_ROOT_FOLDER_NAME, new DummyActions() );
        Model model = pc.getModel();
        Lookup rootLookup = model.getRoot();
        
        DataFolder df = (DataFolder)rootLookup.lookup( DataFolder.class );
        assertNotNull( df );
        
        FileObject fo = df.getPrimaryFile();
        assertNotNull( fo );
        
        assertEquals( PALETTE_ROOT_FOLDER_NAME, fo.getName() );
    }

    /**
     * Test of moveCategory method, of class org.netbeans.modules.palette.Model.
     */
    public void testMoveCategoryBefore() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( PALETTE_ROOT_FOLDER_NAME, new DummyActions() );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        Category source = categories[0];
        Category target = categories[categories.length-1];
        
        model.moveCategory( source, target, true );
        
        pc.refresh();
        
        Category[] movedCategories = model.getCategories();
        assertEquals( categories.length, movedCategories.length );
        assertEquals( target.getName(), movedCategories[movedCategories.length-1].getName() );
        assertEquals( source.getName(), movedCategories[movedCategories.length-1-1].getName() );
    }

    /**
     * Test of moveCategory method, of class org.netbeans.modules.palette.Model.
     */
    public void testMoveCategoryAfter() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( PALETTE_ROOT_FOLDER_NAME, new DummyActions() );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        Category source = categories[0];
        Category target = categories[categories.length-1];
        
        model.moveCategory( source, target, false );
        
        pc.refresh();
        
        Category[] movedCategories = model.getCategories();
        assertEquals( categories.length, movedCategories.length );
        assertEquals( target.getName(), movedCategories[movedCategories.length-1-1].getName() );
        assertEquals( source.getName(), movedCategories[movedCategories.length-1].getName() );
    }

    /**
     * Test of moveCategory method, of class org.netbeans.modules.palette.Model.
     */
    public void testMoveCategorySamePosition() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( PALETTE_ROOT_FOLDER_NAME, new DummyActions() );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        Category source = categories[0];
        Category target = categories[0];
        
        model.moveCategory( source, target, false );
        
        Category[] movedCategories = model.getCategories();
        assertEquals( categories.length, movedCategories.length );
        assertEquals( target, movedCategories[0] );
        assertEquals( source, movedCategories[0] );
    }

}
