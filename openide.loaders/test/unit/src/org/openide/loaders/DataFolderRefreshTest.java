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

import org.openide.filesystems.*;

import java.beans.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.netbeans.junit.*;

/** Testing that a change in a pool triggers notification of a change in DataFolder's
 * children.
 *
 * @author  Jaroslav Tulach
 */
public class DataFolderRefreshTest extends LoggingTestCaseHid {
    private ArrayList hold = new ArrayList();
    private org.openide.ErrorManager err;

    private FileObject root;

    /** Creates new DataFolderTest */
    public DataFolderRefreshTest (String name) {
        super (name);
    }
    
    protected void setUp () throws Exception {
        err = org.openide.ErrorManager.getDefault().getInstance("TEST-" + getName());

        registerIntoLookup(new FolderInstanceTest.Pool());
        
        DataLoaderPool pool = DataLoaderPool.getDefault ();
        assertNotNull (pool);
        assertEquals (FolderInstanceTest.Pool.class, pool.getClass ());
        
        clearWorkDir ();
        
        root = FileUtil.createFolder(
            Repository.getDefault().getDefaultFileSystem().getRoot(),
            "dir"
        );
        
        FileUtil.createData(root, "s1.simple");
        FileUtil.createData(root, "s2.simple");
    }

    public void testIsChangeFired() throws Exception {
        DataLoader l = DataLoader.getLoader(DataLoaderOrigTest.SimpleUniFileLoader.class);
        err.log("Add loader: " + l);
        FolderInstanceTest.Pool.setExtra(l);
        err.log("Loader added");
        
        DataFolder f = DataFolder.findFolder(root);
        class C implements PropertyChangeListener {
            PropertyChangeEvent ev;
            
            public void propertyChange(PropertyChangeEvent evt) {
                assertNull("Only one event", this.ev);
                this.ev = evt;
            }
        }
        
        C c = new C();
        f.addPropertyChangeListener(c);
        
        DataObject[] arr = f.getChildren();
        
        assertEquals("Two objects", 2, arr.length);
        assertEquals("Loader1", arr[0].getLoader(), l);
        assertEquals("Loader2", arr[1].getLoader(), l);
        
        FolderInstanceTest.Pool.setExtra(null);
        
        arr = f.getChildren();
        
        assertNotNull("A change event delivered", c.ev);
        assertEquals("children", DataFolder.PROP_CHILDREN, c.ev.getPropertyName());
        
        
        assertEquals("Two objects", 2, arr.length);
        assertEquals("Loader1", arr[0].getLoader(), DataLoaderPool.getDefaultFileLoader());
        assertEquals("Loader2", arr[1].getLoader(), DataLoaderPool.getDefaultFileLoader());
    }
}
