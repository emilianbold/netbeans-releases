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
import org.openide.nodes.Node;
import org.openide.util.datatransfer.PasteType;

/** Does a change in order on folder fire the right properties?
 *
 * @author  Jaroslav Tulach
 */
public class DataFolderPasteTypesTest extends NbTestCase {

    private File dir;
    private Node folderNode;

    
    public DataFolderPasteTypesTest (String name) {
        super (name);
    }

    protected void setUp() throws Exception {
        dir = new File( getWorkDir(), "testDir" );
        dir.mkdir();
        FileObject fo = FileUtil.toFileObject( dir );
        DataObject dob = DataObject.find( fo );
        folderNode = dob.getNodeDelegate();
    }

    protected void tearDown() throws Exception {

        dir.delete();
        dir.deleteOnExit();
    }

    public void testNoPasteTypes() throws ClassNotFoundException {
        DataFlavor flavor = new DataFlavor( "unsupported/flavor;class=java.lang.Object" );

        DataFolder.FolderNode node = (DataFolder.FolderNode)folderNode;
        ArrayList list = new ArrayList();
        node.createPasteTypes( new MockTransferable( new DataFlavor[] {flavor}, null ), list );
        assertEquals( 0, list.size() );
    }

    public void testJavaFileListPasteTypes() throws ClassNotFoundException, IOException {
        File testFile = File.createTempFile( "testFile", ".txt", getWorkDir() );
        testFile.deleteOnExit();
        ArrayList fileList = new ArrayList(1);
        fileList.add( testFile );
        Transferable t = new MockTransferable( new DataFlavor[] {DataFlavor.javaFileListFlavor}, fileList );

        DataFolder.FolderNode node = (DataFolder.FolderNode)folderNode;
        ArrayList list = new ArrayList();
        node.createPasteTypes( t, list );
        assertFalse( list.isEmpty() );
        PasteType paste = (PasteType)list.get( 0 );
        paste.paste();

        File newFile = new File( dir, testFile.getName() );
        assertTrue( newFile.exists() );
        newFile.delete();
        testFile.delete();
    }

    public void testUriFileListPasteTypes() throws ClassNotFoundException, IOException {
        DataFlavor flavor = new DataFlavor( "unsupported/flavor;class=java.lang.Object" );
        File testFile = File.createTempFile( "testFile", ".txt", getWorkDir() );
        testFile.deleteOnExit();
        String uriList = testFile.toURI() + "\r\n";
        Transferable t = new MockTransferable( new DataFlavor[] {new DataFlavor("text/uri-list;class=java.lang.String")}, uriList );

        DataFolder.FolderNode node = (DataFolder.FolderNode)folderNode;
        ArrayList list = new ArrayList();
        node.createPasteTypes( t, list );
        assertFalse( list.isEmpty() );
        PasteType paste = (PasteType)list.get( 0 );
        paste.paste();

        File newFile = new File( dir, testFile.getName() );
        assertTrue( newFile.exists() );
        newFile.delete();
        testFile.delete();
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
