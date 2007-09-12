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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.favorites;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public class IndexTest extends NbTestCase {

    public IndexTest(String name) {
        super (name);
    }
    
    @Override
    protected void setUp () throws Exception {
        super.setUp ();
        
        // initialize module system with all modules
        Lookup.getDefault().lookup (
            ModuleInfo.class
        );
    }
    
    /**
     * Test basic functionality of Index on Favorites node.
     */
    public void testReorder () throws Exception {
        FileObject folder = FileUtil.createFolder (
            Repository.getDefault().getDefaultFileSystem().getRoot(), 
            "FavoritesTest"
        );
        FileObject fo1 = FileUtil.createData(folder,"Test1");
        FileObject fo2 = FileUtil.createData(folder,"Test2");
        
        DataObject dObj1 = DataObject.find(fo1);
        DataObject dObj2 = DataObject.find(fo2);
        
        DataFolder favorites = Favorites.getFolder();
        
        dObj1.createShadow(favorites);
        dObj2.createShadow(favorites);
        
        Node n = Favorites.getNode();
        
        Node n1 = n.getChildren().findChild("Test1");
        assertNotNull("Node must exist", n1);
        Node n2 = n.getChildren().findChild("Test2");
        assertNotNull("Node must exist", n2);
        
        Index ind = n.getCookie(Index.class);
        assertNotNull("Index must exist", ind);
        
        int i;
        i = ind.indexOf(n1);
        assertEquals("Node index must be 1", i, 1);
        i = ind.indexOf(n2);
        assertEquals("Node index must be 2", i, 2);
        
        ind.reorder(new int [] {0,2,1});
        
        i = ind.indexOf(n1);
        assertEquals("Node index must be 2", i, 2);
        i = ind.indexOf(n2);
        assertEquals("Node index must be 1", i, 1);
    }
    
}
