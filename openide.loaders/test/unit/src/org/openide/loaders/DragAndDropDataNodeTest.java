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
