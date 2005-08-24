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

package org.openide.loaders;

import java.io.IOException;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;

/**
 * Tests Index cookio of DataFolder (when uses DataFilter).
 *
 * @author Jiri Rechtacek
 */
public class DataFolderIndexTest extends NbTestCase {
    DataFolder df;
    FileObject fo;

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public DataFolderIndexTest(String s) {
        super(s);
    }
    
    protected void setUp () {
        try {
            fo = Repository.getDefault ().getDefaultFileSystem ().getRoot ().createFolder ("TestTemplates");
            df = DataFolder.findFolder (fo);
            assertNotNull ("DataFolder found for AA", df);
            
            df.getPrimaryFile ().createData ("marie");
            df.getPrimaryFile ().createData ("jakub");
            df.getPrimaryFile ().createData ("eva");
            df.getPrimaryFile ().createData ("adam");
            
            assertNotNull ("Folder " + df + " has a children.", df.getChildren ());
            assertEquals ("Folder " + df + " has 4 childs.", 4, df.getChildren ().length);
            
        } catch (Exception x) {
            fail (x.getMessage ());
        }
    }
    
    protected void tearDown() {
        try {
            FileLock l = fo.lock ();
            fo.delete (l);
            l.releaseLock ();
        } catch (IOException ioe) {
            fail (ioe.getMessage ());
        }
    }
    
    public void testIndexWithoutInitialization() throws Exception {
        Node n = df.getNodeDelegate();
        
        Index fromNode = (Index) n.getLookup ().lookup (Index.class);
        assertNotNull ("DataFolderNode has Index.", fromNode);

        int x = fromNode.getNodesCount();
        assertEquals("The same number of nodes like folder children", df.getChildren().length, x);
    }

    public void testIndexNodesWithoutInitialization() throws Exception {
        Node n = df.getNodeDelegate();
        
        Index fromNode = (Index) n.getLookup ().lookup (Index.class);
        assertNotNull ("DataFolderNode has Index.", fromNode);

        int x = fromNode.getNodes().length;
        assertEquals("The same number of nodes like folder children", df.getChildren().length, x);
    }
    
    public void testWithoutFilter () throws Exception {
        testMatchingIndexes (df, df.getNodeDelegate ());
    }
    
    public void testIndexCookieOfferedOnlyWhenAppropriate() throws Exception {
        Node n = df.getNodeDelegate();
        assertNotNull("have an index cookie on SFS", n.getLookup().lookup(Index.class));
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        n = DataFolder.findFolder(lfs.getRoot()).getNodeDelegate();
        assertNull("have no index cookie on a local folder", n.getLookup().lookup(Index.class));
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject x = fs.getRoot().createFolder("x");
        FileObject y = fs.getRoot().createFolder("y");
        y.setAttribute("DataFolder.Index.reorderable", Boolean.TRUE);
        n = DataFolder.findFolder(x).getNodeDelegate();
        assertNull("have no index cookie on a random folder in a random FS", n.getLookup().lookup(Index.class));
        n = DataFolder.findFolder(y).getNodeDelegate();
        assertNotNull("do have index cookie if magic attr is set", n.getLookup().lookup(Index.class));
    }
    
    private void testMatchingIndexes (DataFolder f, Node n) {
        Node [] arr = n.getChildren ().getNodes (true);
        
        Index fromNode = (Index) n.getLookup ().lookup (Index.class);
        assertNotNull ("DataFolderNode has Index.", fromNode);
        
        Index fromFolder = new DataFolder.Index (f, n);
        assertNotNull ("DataFolderNode has Index.", fromFolder);
        
        assertTrue ("Index contains some items", fromNode.getNodesCount () > 0);
        assertTrue ("Index contains some items", fromFolder.getNodesCount () > 0);
        
        log ("Node's index: " + Arrays.asList (fromNode.getNodes ()));
        log ("Folder's index: " + Arrays.asList (fromFolder.getNodes ()));
        
        for (int i = 0; i < arr.length; i++) {
            fromNode.indexOf (arr [i]);
            assertEquals ("Node " + arr [0] + " has as same position in Node's Index [" + Arrays.asList (fromNode.getNodes ()) + "]" +
                    "as in folder's Index [" + Arrays.asList (fromFolder.getNodes ()) + "].",
                    fromFolder.indexOf (arr [i]), fromNode.indexOf (arr [i]));
        }
    }
    
}
