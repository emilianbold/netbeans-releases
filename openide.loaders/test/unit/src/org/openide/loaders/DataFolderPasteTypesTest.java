/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.util.datatransfer.PasteType;

public class DataFolderPasteTypesTest extends NbTestCase {

    private File dir;
    private Node folderNode;
    private LocalFileSystem testFileSystem;

    
    public DataFolderPasteTypesTest (String name) {
        super (name);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        testFileSystem = new LocalFileSystem();
        testFileSystem.setRootDirectory( getWorkDir() );
        Repository.getDefault().addFileSystem( testFileSystem );
        
        FileObject fo = FileUtil.createFolder( testFileSystem.getRoot(), "testDir");
        DataObject dob = DataObject.find( fo );
        folderNode = dob.getNodeDelegate();
    }

    protected void tearDown() throws Exception {

        clearWorkDir();
        Repository.getDefault().removeFileSystem( testFileSystem );
    }

    public void testNoPasteTypes() throws ClassNotFoundException {
        DataFlavor flavor = new DataFlavor( "unsupported/flavor;class=java.lang.Object" );

        DataFolder.FolderNode node = (DataFolder.FolderNode)folderNode;
        ArrayList list = new ArrayList();
        node.createPasteTypes( new MockTransferable( new DataFlavor[] {flavor}, null ), list );
        assertEquals( 0, list.size() );
    }

    public void testJavaFileListPasteTypes() throws ClassNotFoundException, IOException {
        FileObject testFO = FileUtil.createData( testFileSystem.getRoot(), "testFile.txt" );
        File testFile = FileUtil.toFile( testFO );
        ArrayList fileList = new ArrayList(1);
        fileList.add( testFile );
        Transferable t = new MockTransferable( new DataFlavor[] {DataFlavor.javaFileListFlavor}, fileList );

        DataFolder.FolderNode node = (DataFolder.FolderNode)folderNode;
        ArrayList list = new ArrayList();
        node.createPasteTypes( t, list );
        assertFalse( list.isEmpty() );
        PasteType paste = (PasteType)list.get( 0 );
        paste.paste();

        FileObject[] children = testFileSystem.getRoot().getFileObject( "testDir" ).getChildren();
        assertEquals( 1, children.length );
        assertEquals( children[0].getNameExt(), "testFile.txt" );
    }

    public void testUriFileListPasteTypes() throws ClassNotFoundException, IOException {
        DataFlavor flavor = new DataFlavor( "unsupported/flavor;class=java.lang.Object" );
        FileObject testFO = FileUtil.createData( testFileSystem.getRoot(), "testFile.txt" );
        File testFile = FileUtil.toFile( testFO );
        String uriList = testFile.toURI() + "\r\n";
        Transferable t = new MockTransferable( new DataFlavor[] {new DataFlavor("text/uri-list;class=java.lang.String")}, uriList );

        DataFolder.FolderNode node = (DataFolder.FolderNode)folderNode;
        ArrayList list = new ArrayList();
        node.createPasteTypes( t, list );
        assertFalse( list.isEmpty() );
        PasteType paste = (PasteType)list.get( 0 );
        paste.paste();

        FileObject[] children = testFileSystem.getRoot().getFileObject( "testDir" ).getChildren();
        assertEquals( 1, children.length );
        assertEquals( children[0].getNameExt(), "testFile.txt" );
    }

    private static class MockTransferable implements Transferable {
        private DataFlavor[] flavors;
        private Object data;
        public MockTransferable( DataFlavor[] flavors, Object data ) {
            this.flavors = flavors;
            this.data = data;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for( int i=0; i<flavors.length; i++ ) {
                if( flavors[i].equals( flavor ) ) {
                    return true;
                }
            }
            return false;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if( !isDataFlavorSupported( flavor ) ) {
                throw new UnsupportedFlavorException( flavor );
            }
            return data;
        }

    }
}
