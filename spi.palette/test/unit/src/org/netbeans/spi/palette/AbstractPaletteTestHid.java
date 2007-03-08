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
import junit.framework.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;


/**
 *
 * @author Stanislav Aubrecht
 */
public abstract class AbstractPaletteTestHid extends TestCase {

    protected FileObject paletteRootFolder;
    private static final String PALETTE_ROOT_FOLDER_NAME = "test_palette_folder";
    private String rootFolderName;
    
    protected String[] categoryNames;
    protected String[][] itemNames;
    
    private static DataLoader myDummyLoader;

    public AbstractPaletteTestHid( String name ) {
        super( name );
    }
    
    protected void setUp() throws Exception {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
//        paletteRootFolder = fs.findResource( PALETTE_ROOT_FOLDER_NAME );
//        if( null != paletteRootFolder )
//            paletteRootFolder.delete();
        rootFolderName = PALETTE_ROOT_FOLDER_NAME+System.currentTimeMillis();
        paletteRootFolder = fs.getRoot().createFolder( rootFolderName );
        
//        NbPreferences.forModule( DefaultSettings.class ).node( "CommonPaletteSettings" ).removeNode();
        
        if( null == myDummyLoader )
            myDummyLoader = new DummyItemLoader();
        
        createDefaultPaletteContentInFolder( paletteRootFolder );
            }

    protected void tearDown() throws Exception {
        if( null != paletteRootFolder ) {
            FileLock lock = null;
            try {
                if( paletteRootFolder.isValid() ) {
                    lock = paletteRootFolder.lock();
                    paletteRootFolder.delete( lock );
                }
            } finally {
                if( null != lock )
                    lock.releaseLock();
            }
        }
    }
    
    protected void createDefaultPaletteContentInFolder( FileObject rootFolder ) throws IOException {
        categoryNames = new String[10];
        itemNames = new String[categoryNames.length][10];
        for( int i=0; i<categoryNames.length; i++ ) {
            categoryNames[i] = "Category_" + i;
            
            FileObject catFolder = rootFolder.createFolder( categoryNames[i] );
            
            for( int j=0; j<itemNames[i].length; j++ ) {
                itemNames[i][j] = categoryNames[i] + "_Item_" + j;
                
                FileObject itemFile = catFolder.createData( itemNames[i][j], DummyItemLoader.ITEM_EXT );
                DataLoaderPool.setPreferredLoader( itemFile, myDummyLoader );
            }
        }
    }
    
    protected String getRootFolderName() {
        return rootFolderName;
    }
    
    protected FileObject getCategoryFile( String catName ) throws DataObjectNotFoundException {
        FileObject fo = paletteRootFolder.getFileObject( catName );
        if( null == fo ) {
            fail( "Category folder '" + catName + "' not found." );
        }
        return fo;
    }
    
    protected Node getCategoryNode( String catName ) throws DataObjectNotFoundException {
        FileObject fo = getCategoryFile( catName );
        DataObject dobj = DataObject.find( fo );
        if( null == dobj ) {
            fail( "Category data object '" + catName + "' not found." );
        }
        return dobj.getNodeDelegate();
    }
    
    protected FileObject getItemFile( String catName, String itemName ) throws DataObjectNotFoundException {
        FileObject fo = getCategoryFile( catName );
        FileObject itemFO = fo.getFileObject( itemName, DummyItemLoader.ITEM_EXT );
        if( null == itemFO ) {
            fail( "Item file '" + itemName + "' not found." );
        }
        return itemFO;
    }
    
    protected Node getItemNode( String catName, String itemName ) throws DataObjectNotFoundException {
        FileObject fo = getItemFile( catName, itemName );
        DataObject dobj = DataObject.find( fo );
        if( null == dobj ) {
            fail( "Item data object '" + itemName + "' not found." );
        }
        return dobj.getNodeDelegate();
    }
}
