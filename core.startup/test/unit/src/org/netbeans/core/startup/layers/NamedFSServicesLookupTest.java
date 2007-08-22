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

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.startup.MainLookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
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
    private Logger LOG;
    
    public NamedFSServicesLookupTest(String name) {
        super(name);
    }

    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("Test." + getName());
        
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
        LOG.info("creating instances");
        
        FileObject inst = FileUtil.createData(root, "inst/ordering/X.instance");
        inst.setAttribute("instanceCreate", Long.valueOf(1000));
        FileObject inst2 = FileUtil.createData(root, "inst/ordering/A.instance");
        inst2.setAttribute("instanceCreate", Long.valueOf(500));
        FileObject inst3 = FileUtil.createData(root, "inst/ordering/B.instance");
        inst3.setAttribute("instanceCreate", Long.valueOf(1500));
        FileObject inst4 = FileUtil.createData(root, "inst/ordering/C.instance");
        inst4.setAttribute("instanceCreate", Long.valueOf(700));
        
        LOG.info("Adding attributes to parrent");
        FileObject parent = inst.getParent();
        parent.setAttribute("A.instance/C.instance", Boolean.TRUE);
        parent.setAttribute("C.instance/X.instance", Boolean.TRUE);
        parent.setAttribute("X.instance/B.instance", Boolean.TRUE);
        
        
        LOG.info("About to create lookup");
        Lookup l = Lookups.forPath("inst/ordering");
        LOG.info("querying lookup");
        Iterator<? extends Long> lng = l.lookupAll(Long.class).iterator();
        LOG.info("checking results");
        
        assertEquals(Long.valueOf(500), lng.next());
        assertEquals(Long.valueOf(700), lng.next());
        assertEquals(Long.valueOf(1000), lng.next());
        assertEquals(Long.valueOf(1500), lng.next());
        
        LOG.info("Order is correct");

        Iterator<? extends Lookup.Item<Long>> items = l.lookupResult(Long.class).allItems().iterator();
        
        LOG.info("Checking IDs");
        assertEquals("inst/ordering/A", items.next().getId());
        assertEquals("inst/ordering/C", items.next().getId());
        assertEquals("inst/ordering/X", items.next().getId());
        assertEquals("inst/ordering/B", items.next().getId());
        
        LOG.info("Ids ok");
    }

    public void testNumericOrdering() throws Exception {
        class Tst implements FileSystem.AtomicAction {
            public void run() throws IOException {
                init();
            }
            
            void init() throws IOException {
                FileObject inst = FileUtil.createData(root, "inst/positional/X.instance");
                inst.setAttribute("instanceCreate", Long.valueOf(1000));
                inst.setAttribute("position", 3);
                FileObject inst2 = FileUtil.createData(root, "inst/positional/A.instance");
                inst2.setAttribute("instanceCreate", Long.valueOf(500));
                inst2.setAttribute("position", 1);
                FileObject inst3 = FileUtil.createData(root, "inst/positional/B.instance");
                inst3.setAttribute("instanceCreate", Long.valueOf(1500));
                inst3.setAttribute("position", 4);
                FileObject inst4 = FileUtil.createData(root, "inst/positional/C.instance");
                inst4.setAttribute("instanceCreate", Long.valueOf(700));
                inst4.setAttribute("position", 2);
            }
            
            void verify() {
                Lookup l = Lookups.forPath("inst/positional");
                Iterator<? extends Long> lng = l.lookupAll(Long.class).iterator();
                assertEquals(Long.valueOf(500), lng.next());
                assertEquals(Long.valueOf(700), lng.next());
                assertEquals(Long.valueOf(1000), lng.next());
                assertEquals(Long.valueOf(1500), lng.next());
                Iterator<? extends Lookup.Item<Long>> items = l.lookupResult(Long.class).allItems().iterator();
                assertEquals("inst/positional/A", items.next().getId());
                assertEquals("inst/positional/C", items.next().getId());
                assertEquals("inst/positional/X", items.next().getId());
                assertEquals("inst/positional/B", items.next().getId());
            }
        }
        
        Tst tst = new Tst();
        root.getFileSystem().runAtomicAction(tst);
        tst.verify();
    }

}
