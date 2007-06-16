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

package org.netbeans.core.startup.layers;

import java.util.Iterator;
import org.netbeans.core.startup.MainLookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.NamedServicesLookupTest;


/** Test finding services from manifest and .instance files.
 * @author Jaroslav Tulach
 */
public class NamedFSServicesLookupTest extends NamedServicesLookupTest{
    private FileObject root;
    
    public NamedFSServicesLookupTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);
        assertEquals(MainLookup.class, Lookup.getDefault().getClass());

        root = Repository.getDefault().getDefaultFileSystem().getRoot();
        
        super.setUp();
    }
    
    public void testLoadFromTheSFS() throws Exception {
        doLoad("inst/sub");
    }
    public void testLoadFromSubdirTheSFS() throws Exception {
        doLoad("inst");
    }
    
    private void doLoad(String lkpName) throws Exception {
        FileObject inst = FileUtil.createData(root, "inst/sub/X.instance");
        inst.setAttribute("instanceCreate", Long.valueOf(1000));
        
        Lookup l = Lookups.forPath(lkpName);
        Long lng = l.lookup(Long.class);
        assertNotNull("A value found", lng);
        
        inst.delete();
        
        assertNull("Now it is null", l.lookup(Long.class));
    }
    
    public void testOrderingAttributes() throws Exception {
        FileObject inst = FileUtil.createData(root, "inst/ordering/X.instance");
        inst.setAttribute("instanceCreate", Long.valueOf(1000));
        FileObject inst2 = FileUtil.createData(root, "inst/ordering/A.instance");
        inst2.setAttribute("instanceCreate", Long.valueOf(500));
        FileObject inst3 = FileUtil.createData(root, "inst/ordering/B.instance");
        inst3.setAttribute("instanceCreate", Long.valueOf(1500));
        FileObject inst4 = FileUtil.createData(root, "inst/ordering/C.instance");
        inst4.setAttribute("instanceCreate", Long.valueOf(700));
        
        inst.getParent().setAttribute("A.instance/C.instance", Boolean.TRUE);
        inst.getParent().setAttribute("C.instance/X.instance", Boolean.TRUE);
        inst.getParent().setAttribute("X.instance/B.instance", Boolean.TRUE);
        
        
        Lookup l = Lookups.forPath("inst/ordering");
        Iterator<? extends Long> lng = l.lookupAll(Long.class).iterator();
        assertEquals(Long.valueOf(500), lng.next());
        assertEquals(Long.valueOf(700), lng.next());
        assertEquals(Long.valueOf(1000), lng.next());
        assertEquals(Long.valueOf(1500), lng.next());

        Iterator<? extends Lookup.Item<Long>> items = l.lookupResult(Long.class).allItems().iterator();
        assertEquals("inst/ordering/A", items.next().getId());
        assertEquals("inst/ordering/C", items.next().getId());
        assertEquals("inst/ordering/X", items.next().getId());
        assertEquals("inst/ordering/B", items.next().getId());
    }

    public void testNumericOrdering() throws Exception {
        FileObject inst = FileUtil.createData(root, "inst/ordering/X.instance");
        inst.setAttribute("instanceCreate", Long.valueOf(1000));
        inst.setAttribute("position", 3);
        FileObject inst2 = FileUtil.createData(root, "inst/ordering/A.instance");
        inst2.setAttribute("instanceCreate", Long.valueOf(500));
        inst2.setAttribute("position", 1);
        FileObject inst3 = FileUtil.createData(root, "inst/ordering/B.instance");
        inst3.setAttribute("instanceCreate", Long.valueOf(1500));
        inst3.setAttribute("position", 4);
        FileObject inst4 = FileUtil.createData(root, "inst/ordering/C.instance");
        inst4.setAttribute("instanceCreate", Long.valueOf(700));
        inst4.setAttribute("position", 2);
        Lookup l = Lookups.forPath("inst/ordering");
        Iterator<? extends Long> lng = l.lookupAll(Long.class).iterator();
        assertEquals(Long.valueOf(500), lng.next());
        assertEquals(Long.valueOf(700), lng.next());
        assertEquals(Long.valueOf(1000), lng.next());
        assertEquals(Long.valueOf(1500), lng.next());
        Iterator<? extends Lookup.Item<Long>> items = l.lookupResult(Long.class).allItems().iterator();
        assertEquals("inst/ordering/A", items.next().getId());
        assertEquals("inst/ordering/C", items.next().getId());
        assertEquals("inst/ordering/X", items.next().getId());
        assertEquals("inst/ordering/B", items.next().getId());
    }

}
