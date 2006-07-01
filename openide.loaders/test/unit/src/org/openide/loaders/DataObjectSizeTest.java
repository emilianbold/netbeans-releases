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

import java.lang.ref.WeakReference;
import java.util.HashSet;
import junit.textui.TestRunner;
import org.openide.filesystems.*;
import java.util.Enumeration;
import org.openide.nodes.Node;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.Repository;
import org.netbeans.junit.*;

/** How big is default data object?
 * @author Jaroslav Tulach
 */
public class DataObjectSizeTest extends NbTestCase {
    static FileSystem lfs;
    static DataObject original;

    public DataObjectSizeTest(String name) {
        super(name);
    }
    
    protected void setUp() throws java.lang.Exception {
        if (original == null) {
            String fsstruct [] = new String [] {
                "folder/original.txt", 
            };
            TestUtilHid.destroyLocalFileSystem (getName());
            lfs = TestUtilHid.createLocalFileSystem (getWorkDir (), fsstruct);
            FileObject fo = FileUtil.createData (lfs.getRoot (), "/folder/original.txt");
            assertNotNull(fo);
            original = DataObject.find (fo);

            assertFalse ("Not a folder", original instanceof DataFolder);
        }
    }
    
    public void testThatThereIsJustOneItemIssue42857 () throws Exception {
        Object[] exclude = {
            original.getLoader (),
            original.getPrimaryFile (),
            org.openide.util.Utilities.activeReferenceQueue (),
        };
        
        assertSize ("If we exclude all the static things, like loader and " +
            " reference queue and things we do not have control upon like file object" +
            " we should get some reasonable size for the data object. ", 
            java.util.Collections.singleton (original), 280, exclude
        );
    }
    
    public void testNumberOfDataObjectPoolItemsIssue42857 () throws Exception {
        class CountItems implements MemoryFilter {
            HashSet items = new HashSet ();
            
            public boolean reject(java.lang.Object obj) {
                if (obj instanceof DataObjectPool.Item) {
                    items.add (obj);
                }
                
                return false;
            }
        }
        CountItems cnt = new CountItems ();
        assertSize (
            "Just iterate thru all the objects available and count Items", 
            java.util.Collections.singleton (DataObjectPool.getPOOL ()), 
            Integer.MAX_VALUE,
            cnt
        );
        
        if (cnt.items.size () != 1) {
            fail ("There should be one item, but was " + cnt.items.size () + "\n" + cnt.items);
        }
    }
}
