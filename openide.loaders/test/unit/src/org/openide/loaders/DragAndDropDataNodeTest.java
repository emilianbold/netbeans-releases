/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.netbeans.junit.*;
import org.openide.filesystems.LocalFileSystem;

/** Test things about node delegates.
 * Note: if you mess with file status changes in this test, you may effectively
 * break the testLeakAfterStatusChange test.
 *
 * @author Jesse Glick
 */
public class DragAndDropDataNodeTest extends NbTestCase {
    
    private LocalFileSystem testFileSystem;
    
    public DragAndDropDataNodeTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        testFileSystem = new LocalFileSystem();
        testFileSystem.setRootDirectory( getWorkDir() );
    }

    public void testClipboardCopy() throws IOException, ClassNotFoundException, UnsupportedFlavorException {
        DataFlavor uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");

        FileObject fo = FileUtil.createData( testFileSystem.getRoot(), "dndtest.txt" );
        File tmpFile = FileUtil.toFile( fo );

        DataObject dob = DataObject.find( fo );
        DataNode node = new DataNode( dob, Children.LEAF );

        Transferable t = node.clipboardCopy();
        assertTrue( t.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) );
        List fileList = (List) t.getTransferData( DataFlavor.javaFileListFlavor );
        assertNotNull( fileList );
        assertEquals( 1, fileList.size() );
        assertTrue( fileList.contains( tmpFile ) );

        assertTrue( t.isDataFlavorSupported( uriListFlavor ) );
        String uriList = (String) t.getTransferData( uriListFlavor );
        assertEquals( tmpFile.toURI()+"\r\n", uriList );
    }

    public void testClipboardCut() throws ClassNotFoundException, IOException, UnsupportedFlavorException {
        DataFlavor uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");

        FileObject fo = FileUtil.createData( testFileSystem.getRoot(), "dndtest.txt" );
        File tmpFile = FileUtil.toFile( fo );

        DataObject dob = DataObject.find( fo );
        DataNode node = new DataNode( dob, Children.LEAF );

        Transferable t = node.clipboardCopy();
        assertTrue( t.isDataFlavorSupported( DataFlavor.javaFileListFlavor ) );
        List fileList = (List) t.getTransferData( DataFlavor.javaFileListFlavor );
        assertNotNull( fileList );
        assertEquals( 1, fileList.size() );
        assertTrue( fileList.contains( tmpFile ) );

        assertTrue( t.isDataFlavorSupported( uriListFlavor ) );
        String uriList = (String) t.getTransferData( uriListFlavor );
        assertEquals( tmpFile.toURI()+"\r\n", uriList );
    }
}
