/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.openide.filesystems;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.NamedServicesLookupTest;

/** Test finding services from manifest and .instance files.
 * @author Jaroslav Tulach
 */
public class RecognizeInstanceFilesTest extends NamedServicesLookupTest{
    private FileObject root;
    private Logger LOG;
    
    public RecognizeInstanceFilesTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void setUp() throws Exception {
        if (System.getProperty("netbeans.user") == null) {
            System.setProperty("netbeans.user", new File(getWorkDir(), "ud").getPath());
        }
        
        LOG = Logger.getLogger("Test." + getName());
        
        root = FileUtil.getConfigRoot();
        for (FileObject fo : root.getChildren()) {
            fo.delete();
        }
        
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
        Collection<? extends Long> lngAll = l.lookupAll(Long.class);
        assertEquals(4, lngAll.size());
        Iterator<? extends Long> lng = lngAll.iterator();
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

    public void testSupportsClassNameEncodedInAFileName() throws Exception {
        FileObject inst = FileUtil.createData(root, "inst/class/" + Inst.class.getName().replace('.', '-') + ".instance");
        Lookup l = Lookups.forPath("inst/class");
        assertNotNull("Instance created", l.lookup(Inst.class));
    }

    public void testNoItemsForSillyPairs() throws Exception {
        FileObject inst = FileUtil.createData(root, "silly/class/File.silly");
        CharSequence log = Log.enable("org.openide.filesystems", Level.WARNING);
        Lookup l = Lookups.forPath("silly/class");
        Collection<?> c = l.lookupResult(Object.class).allItems();
        assertTrue("No items created: " + c, c.isEmpty());
        assertEquals("No warnings:\n" + log, 0, log.length());
    }
    
    public static final class Inst extends Object {
    }

    public void testSharedClassObject() throws Exception {
        Shared instance = SharedClassObject.findObject(Shared.class, true);
        FileUtil.createData(root, "dir/" + Shared.class.getName().replace('.', '-') + ".instance");
        Lookup l = Lookups.forPath("dir");
        assertSame(instance, l.lookup(Shared.class));
    }

    public static final class Shared extends SharedClassObject {}

}
