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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import org.openide.filesystems.*;
import org.netbeans.junit.*;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/*
 */
public class FileObjectInLookupTest extends NbTestCase {
    FileObject root;
    
    public FileObjectInLookupTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        MockServices.setServices(OwnDataLoaderPool.class);
        clearWorkDir ();
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir (), new String[] {
            "adir/",
            "adir/file.txt",
            "adir/file.own"
        });
        
        root = FileUtil.toFileObject(FileUtil.toFile(lfs.getRoot()));
        
        Enumeration<?> en = DataLoaderPool.getDefault().allLoaders();
        while (en.hasMoreElements()) {
            if (en.nextElement() instanceof OwnDataLoader) {
                return;
            }
        }
        fail("OwnDataLoader shall be registered");
    }
    
    protected void tearDown() throws Exception {
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    public void testFOInsideFolder() throws Exception {
        DataFolder f = DataFolder.findFolder(root.getFileObject("adir"));
        assertFileObjects(f);
        f.rename("bdir");
        assertFileObjects(f);
    }
    
    public void testFOInsideADefaultDataObject() throws Exception {
        DataObject obj = DataObject.find(root.getFileObject("adir/file.txt"));
        assertFileObjects(obj);
        obj.rename("kuk");
        assertFileObjects(obj);
        obj.move(obj.getFolder().getFolder());
        assertFileObjects(obj);
    }

    public void testOwnLoader() throws Exception {
        DataObject obj = DataObject.find(root.getFileObject("adir/file.own"));
        assertEquals(OwnDataLoader.class, obj.getLoader().getClass());
        assertFileObjects(obj);
        obj.rename("kuk");
        assertFileObjects(obj);
        obj.move(obj.getFolder().getFolder());
        assertFileObjects(obj);
    }

    public void testShadow() throws Exception {
        DataObject obj = DataObject.find(root.getFileObject("adir/file.own"));
        DataShadow shadow = obj.createShadow(obj.getFolder().getFolder());
        assertEquals(OwnDataLoader.class, obj.getLoader().getClass());
        
        assertEquals("DataObject for the shadow is the shadow", shadow, shadow.getCookie(DataObject.class));
        
        assertFileObjects(obj);
        assertFileObjects("However FileObject of a shadow are delegated to the original", shadow, obj.files());
        obj.rename("kuk");
        assertFileObjects(obj);
        assertFileObjects("However FileObject of a shadow are delegated to the original", shadow, obj.files());
        obj.move(obj.getFolder().getFolder());
        assertFileObjects(obj);
        assertFileObjects("However FileObject of a shadow are delegated to the original", shadow, obj.files());
        shadow.rename("somenewshadow");
        assertFileObjects(obj);
        assertFileObjects("However FileObject of a shadow are delegated to the original", shadow, obj.files());
        obj.delete();
        /*
        DataObject broken = DataObject.find(shadow.getPrimaryFile());
        if (shadow == broken) {
            fail("They should be different: " + shadow + " != " + broken);
        }
        assertEquals("DataObject for the shadow is now the shadow", broken, broken.getCookie(DataObject.class));
        assertFileObjects(broken);
         */
    }
    
    private static void assertFileObjects(DataObject obj) {
        assertFileObjects("", obj, obj.files());
    }
    
    private static void assertFileObjects(String msg, DataObject obj, Collection<? extends FileObject> expect) {
        Collection<? extends FileObject> allcol = obj.getNodeDelegate().getLookup().lookupAll(FileObject.class);
        List<FileObject> all = new ArrayList<FileObject>(allcol);
        Enumeration<? extends FileObject> files = Collections.enumeration(expect);
        int i = 0;
        while (files.hasMoreElements()) {
            FileObject fo = files.nextElement();
            if (i >= all.size()) {
                fail(msg + "\nThere should be more elements, but there is only " + all.size() + "\nAll: " + all + "\nCurrent: " + fo);
            }
            
            if (fo.equals(all.get(i))) {
                i++;
                continue;
            }
            fail(msg + "\nError at position " + i + " expected: " + fo + " but was: " + all.get(i) + "\nAll: " + all);
        }
    }
    
    public static final class OwnDataLoaderPool extends DataLoaderPool {
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(OwnDataLoader.getLoader(OwnDataLoader.class));
        }
    }


    public static class OwnDataLoader extends UniFileLoader {
        private static final long serialVersionUID = 1L;

        public OwnDataLoader() {
            super("org.openide.loaders.OwnDataObject");
        }

        protected String defaultDisplayName() {
            return NbBundle.getMessage(OwnDataLoader.class, "LBL_Own_loader_name");
        }

        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("own");
        }

        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new OwnDataObject(primaryFile, this);
        }
    }
    static class OwnDataObject extends MultiDataObject implements Lookup.Provider {

        public OwnDataObject(FileObject pf, OwnDataLoader loader) throws DataObjectExistsException, IOException {
            super(pf, loader);
            CookieSet cookies = getCookieSet();
            cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        }

        protected Node createNodeDelegate() {
            return new OwnDataNode(this, getLookup());
        }

        public Lookup getLookup() {
            return getCookieSet().getLookup();
        }
    }
    
    static class OwnDataNode extends DataNode {
        private static final String IMAGE_ICON_BASE = "SET/PATH/TO/ICON/HERE";

        public OwnDataNode(OwnDataObject obj, Lookup lookup) {
            super(obj, Children.LEAF, lookup);
            //        setIconBaseWithExtension(IMAGE_ICON_BASE);
        }

    }

}
