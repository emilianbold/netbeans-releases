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

package org.openide.loaders;


import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import org.openide.filesystems.*;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import javax.swing.Action;
import junit.framework.Test;
import org.openide.actions.EditAction;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/** Check what can be done when registering loaders in layer.
 * @author Jaroslav Tulach
 */
@RandomlyFails
public class DataLoaderInLayerTest extends NbTestCase {

    public DataLoaderInLayerTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        Test t = null;
        t = new NbTestSuite(DataLoaderInLayerTest.class);
        if (t == null) {
            t = new DataLoaderInLayerTest("testFactoryInstanceRegistrationWorksAsWell");
        }
        return t;
    }
    
    protected FileSystem createFS(String... resources) throws IOException {
        return TestUtilHid.createLocalFileSystem(getWorkDir(), resources);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        FileUtil.setMIMEType("simple", "text/plain");
        FileUtil.setMIMEType("ant", "text/ant+xml");
    }
    
    private static void addRemoveLoader(DataLoader l, boolean add) throws IOException {
        addRemoveLoader("text/plain", l, add);
    }
    private static void addRemoveLoader(String mime, DataLoader l, boolean add) throws IOException {
        addRemove(mime, l.getClass(), add);
    }
    private static <F extends DataObject.Factory> void addRemove(String mime, Class<F> clazz, boolean add) throws IOException {
        String res = "Loaders/" + mime + "/Factories/" + clazz.getSimpleName().replace('.', '-') + ".instance";
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        if (add) {
            FileObject fo = FileUtil.createData(root, res);
            fo.setAttribute("instanceClass", clazz.getName());
        } else {
            FileObject fo = root.getFileObject(res);
            if (fo != null) {
                fo.delete();
            }
        }
        for (;;) {
            Object f = Lookups.forPath("Loaders/" + mime + "/Factories").lookup(clazz);
            FolderLookup.ProxyLkp.DISPATCH.waitFinished();
            if (add == (f != null)) {
                break;
            }
        }
    }
    private static <F extends DataObject.Factory> void addRemove(String mime, F factory, boolean add) throws IOException {
        String res = "Loaders/" + mime + "/Factories/" + factory.getClass().getSimpleName().replace('.', '-') + ".instance";
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        if (add) {
            FileObject fo = FileUtil.createData(root, res);
            fo.setAttribute("instanceCreate", factory);
            assertSame("No serialization, just memory fs is used", factory, fo.getAttribute("instanceCreate"));
        } else {
            FileObject fo = root.getFileObject(res);
            if (fo != null) {
                fo.delete();
            }
        }
        for (;;) {
            Object f = Lookups.forPath("Loaders/" + mime + "/Factories").lookup(factory.getClass());
            FolderLookup.ProxyLkp.DISPATCH.waitFinished();
            if (add == (f != null)) {
                break;
            }
        }
    }
    
    public void testSimpleGetChildren() throws Exception {
        DataLoader l = DataLoader.getLoader(SimpleUniFileLoader.class);
        addRemoveLoader(l, true);
        try {
            FileSystem lfs = createFS("folder/file.simple");
            FileObject fo = lfs.findResource("folder");
            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] arr = df.getChildren();
            assertEquals("One object", 1, arr.length);
            DataObject dob = arr[0];
            assertEquals(SimpleDataObject.class, dob.getClass());

            DataObject copied = dob.copy(df);
            assertEquals(SimpleDataObject.class, copied.getClass());

            DataObject templ = dob.createFromTemplate(df, "ahoj");
            assertEquals(SimpleDataObject.class, templ.getClass());
            assertEquals("ahoj", templ.getName());

            DataObject ren = dob.copyRename(df, "kuk", "simple");
            assertEquals(SimpleDataObject.class, ren.getClass());
            assertEquals("kuk", ren.getName());
        } finally {
            addRemoveLoader(l, false);
        }
    }

    public void testFactoryRegistrationWorksAsWell() throws Exception {
        addRemove("text/plain", SimpleFactory.class, true);
        try {
            FileSystem lfs = createFS("folderF/file.simple");
            FileObject fo = lfs.findResource("folderF");
            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] arr = df.getChildren();
            assertEquals("One object", 1, arr.length);
            DataObject dob = arr[0];
            assertEquals(SimpleDataObject.class, dob.getClass());

            DataObject copied = dob.copy(df);
            assertEquals(SimpleDataObject.class, copied.getClass());

            DataObject templ = dob.createFromTemplate(df, "ahoj");
            assertEquals(SimpleDataObject.class, templ.getClass());
            assertEquals("ahoj", templ.getName());

            DataObject ren = dob.copyRename(df, "kuk", "simple");
            assertEquals(SimpleDataObject.class, ren.getClass());
            assertEquals("kuk", ren.getName());
        } finally {
            addRemove("text/plain", SimpleFactory.class, false);
        }
    }

    public void testFactoryInstanceRegistrationWorksAsWell() throws Exception {
        URL u = DataLoaderInLayerTest.class.getResource("/org/openide/loaders/saveAll.gif");
        Image img = Toolkit.getDefaultToolkit().createImage(u);
        
        DataObject.Factory f = DataLoaderPool.factory(SimpleDataObject.class, "text/simplefactory", img);
        
        addRemove("text/plain", f, true);
        try {
            FileSystem lfs = createFS("folderF/file.simple");
            FileObject fo = lfs.findResource("folderF");
            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] arr = df.getChildren();
            assertEquals("One object", 1, arr.length);
            DataObject dob = arr[0];
            assertEquals(SimpleDataObject.class, dob.getClass());
            
            FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
            FileObject edit = FileUtil.createData(root, "/Loaders/text/simplefactory/Actions/org-openide-actions-EditAction.instance");
            
            Node node = dob.getNodeDelegate();
            Action[] actions = node.getActions(true);
            assertEquals("One action is present: " + Arrays.asList(actions), 1, actions.length);
            assertEquals("It is the edit one", EditAction.class, actions[0].getClass());
            
            assertSame("Icon is propagated for open", img, node.getOpenedIcon(0));
            assertSame("Icon is propagated", img, node.getIcon(0));
            
            Reference<DataFolder> ref = new WeakReference<DataFolder>(df);
            df = null;
            assertGC("Folder can go away", ref);
            
            df = DataFolder.findFolder(fo);
            arr = df.getChildren();
            assertEquals("One object", 1, arr.length);
            assertEquals("Object is the same", dob, arr[0]);

            DataObject copied = dob.copy(df);
            assertEquals(SimpleDataObject.class, copied.getClass());

            DataObject templ = dob.createFromTemplate(df, "ahoj");
            assertEquals(SimpleDataObject.class, templ.getClass());
            assertEquals("ahoj", templ.getName());

            DataObject ren = dob.copyRename(df, "kuk", "simple");
            assertEquals(SimpleDataObject.class, ren.getClass());
            assertEquals("kuk", ren.getName());
        } finally {
            addRemove("text/plain", f, false);
        }
    }
    
    public void testFactoryInstanceRegistrationWorksAsWellNowFromLayer() throws Exception {
        URL u = DataLoaderInLayerTest.class.getResource("/org/openide/loaders/saveAll.gif");
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject instance = FileUtil.createData(root, "TestLoaders/text/L.instance");
        instance.setAttribute("dataObjectClass", SimpleDataObject.class.getName());
        instance.setAttribute("mimeType", "text/simplefactory");
        instance.setAttribute("SystemFileSystem.icon", u);
        
        
        Image img = ImageUtilities.loadImage("org/openide/loaders/saveAll.gif");
        
        DataObject.Factory f = DataLoaderPool.factory(instance);
        
        addRemove("text/plain", f, true);
        try {
            FileSystem lfs = createFS("folderQ/file.simple");
            FileObject fo = lfs.findResource("folderQ");
            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] arr = df.getChildren();
            assertEquals("One object", 1, arr.length);
            DataObject dob = arr[0];
            assertEquals(SimpleDataObject.class, dob.getClass());
            
            FileObject edit = FileUtil.createData(root, "/Loaders/text/simplefactory/Actions/org-openide-actions-EditAction.instance");
            
            Node node = dob.getNodeDelegate();
            Action[] actions = node.getActions(true);
            assertEquals("One action is present: " + Arrays.asList(actions), 1, actions.length);
            assertEquals("It is the edit one", EditAction.class, actions[0].getClass());
            
            assertImage("Icon is propagated for open", img, node.getOpenedIcon(0));
            assertImage("Icon is propagated", img, node.getIcon(0));
            
            Reference<DataFolder> ref = new WeakReference<DataFolder>(df);
            df = null;
            assertGC("Folder can go away", ref);
            
            df = DataFolder.findFolder(fo);
            arr = df.getChildren();
            assertEquals("One object", 1, arr.length);
            assertEquals("Object is the same", dob, arr[0]);
        } finally {
            addRemove("text/plain", f, false);
        }
    }

    public void testSimpleLoader() throws Exception {
        DataLoader l = DataLoader.getLoader(SimpleUniFileLoader.class);
        addRemoveLoader(l, true);
        try {
            FileSystem lfs = createFS("folder/file.simple");
            FileObject fo = lfs.findResource("folder/file.simple");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            assertEquals(SimpleDataObject.class, dob.getClass());
        } finally {
            addRemoveLoader(l, false);
        }
    }

    public void testDataObjectFind() throws Exception {
        DataLoader l = DataLoader.getLoader(SimpleUniFileLoader.class);
        addRemoveLoader(l, true);
        try {
            FileSystem lfs = createFS("folder/file.simple");
            FileObject fo = lfs.findResource("folder/file.simple");
            assertNotNull(fo);
            
            DataObject jdo = DataObject.find(fo);
            for (int i = 0; i < 5000; i++) {
                FileObject primary = jdo.getPrimaryFile();
                jdo.setValid(false);
                jdo = DataObject.find(primary);
                assertNotNull(jdo);
                assertTrue(jdo.isValid());
            }
            
        } finally {
            addRemoveLoader(l, false);
        }
    }

    public void testAntAsAntSimpleLoader() throws Exception {
        DataLoader l1 = DataLoader.getLoader(SimpleUniFileLoader.class);
        DataLoader l2 = DataLoader.getLoader(AntUniFileLoader.class);
        DataLoader l3 = DataLoader.getLoader(XMLUniFileLoader.class);
        addRemoveLoader(l1, true);
        addRemoveLoader("text/ant+xml", l2, true);
        addRemoveLoader("text/xml", l3, true);
        try {
            FileSystem lfs = createFS(new String[] {
                "folder/file.ant",
            });
            FileObject fo = lfs.findResource("folder/file.ant");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            assertEquals(l2, dob.getLoader());
        } finally {
        addRemoveLoader(l1, false);
        addRemoveLoader("text/ant+xml", l2, false);
        addRemoveLoader("text/xml", l3, false);
        }
    }
    public void testAntWithoutAntSimpleLoader() throws Exception {
        DataLoader l1 = DataLoader.getLoader(SimpleUniFileLoader.class);
        //DataLoader l2 = DataLoader.getLoader(AntUniFileLoader.class);
        DataLoader l3 = DataLoader.getLoader(XMLUniFileLoader.class);
        addRemoveLoader(l1, true);
        //addRemoveLoader("text/ant+xml", l2, true);
        addRemoveLoader("text/xml", l3, true);
        try {
            FileSystem lfs = createFS("folder2/file.ant");
            FileObject fo = lfs.findResource("folder2/file.ant");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            MultiFileLoader xmlL = DataLoader.getLoader(XMLDataObject.Loader.class);
            assertEquals("No special handling for XML", xmlL, dob.getLoader());
        } finally {
        addRemoveLoader(l1, false);
        //addRemoveLoader("text/ant+xml", l2, false);
        addRemoveLoader("text/xml", l3, false);
        }
    }

    public void testAntAsUnknownSimpleLoader() throws Exception {
        DataLoader l1 = DataLoader.getLoader(SimpleUniFileLoader.class);
        //DataLoader l2 = DataLoader.getLoader(AntUniFileLoader.class);
        DataLoader l3 = DataLoader.getLoader(XMLUniFileLoader.class);
        addRemoveLoader(l1, true);
        //addRemoveLoader("text/ant+xml", l2, true);
        addRemoveLoader("content/unknown", l3, true);
        try {
            FileSystem lfs = createFS("folder3/file.ant");
            FileObject fo = lfs.findResource("folder3/file.ant");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            assertEquals(l3, dob.getLoader());
        } finally {
        addRemoveLoader(l1, false);
        //addRemoveLoader("text/ant+xml", l2, false);
        addRemoveLoader("content/unknown", l3, false);
        }
    }

    public void testManifestRegistrationsTakePreceedence() throws Exception {
        DataLoader l1 = DataLoader.getLoader(SimpleUniFileLoader.class);
        DataLoader l2 = DataLoader.getLoader(AntUniFileLoader.class);
        DataLoader l3 = DataLoader.getLoader(XMLUniFileLoader.class);
        addRemoveLoader(l1, true);
        addRemoveLoader("text/ant+xml", l2, true);
        AddLoaderManuallyHid.addRemoveLoader(l3, true);
        try {
            FileSystem lfs = createFS("folder4/file.ant");
            FileObject fo = lfs.findResource("folder4/file.ant");
            assertNotNull(fo);
            DataObject dob = DataObject.find(fo);
            assertEquals("Old registration of l3 takes preceedence", l3, dob.getLoader());
        } finally {
            addRemoveLoader(l1, false);
            addRemoveLoader("text/ant+xml", l2, false);
            AddLoaderManuallyHid.addRemoveLoader(l3, false);
        }
    }
    
    public static final class XMLUniFileLoader extends SimpleUniFileLoader {
        @Override
        protected void initialize() {
            getExtensions().addMimeType("text/xml");
            getExtensions().addMimeType("text/ant+xml");
        }
    }
    public static final class AntUniFileLoader extends SimpleUniFileLoader {
        @Override
        protected void initialize() {
            getExtensions().addMimeType("text/xml");
            getExtensions().addMimeType("text/ant+xml");
        }
    }
    public static class SimpleUniFileLoader extends UniFileLoader {
        public SimpleUniFileLoader() {
            super(SimpleDataObject.class.getName());
        }
        @Override
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("simple");
        }
        protected String displayName() {
            return "Simple";
        }
        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            return new SimpleDataObject(pf, this);
        }
    }
    public static final class SimpleFactory implements DataObject.Factory {
        public DataObject findDataObject(FileObject fo, Set<? super FileObject> recognized) throws IOException {
            return SimpleUniFileLoader.findObject(SimpleUniFileLoader.class).findDataObject(fo, recognized);
        }
    }
    
    public static final class SimpleDataObject extends MultiDataObject {
        private ArrayList supp = new ArrayList ();
        
        public SimpleDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
            super(pf, loader);
        }
        
        /** Access method to modify cookies 
         * @return cookie set of this data object
         */
        public final org.openide.nodes.CookieSet cookieSet () {
            return getCookieSet ();
        }
        
        /** Getter for list of listeners attached to the data object.
         */
        public final Enumeration listeners () {
            return Collections.enumeration (supp);
        }
        
        @Override
        public void addPropertyChangeListener (PropertyChangeListener l) {
            super.addPropertyChangeListener (l);
            supp.add (l);
        }

        @Override
        public void removePropertyChangeListener (PropertyChangeListener l) {
            super.removePropertyChangeListener (l);
            supp.remove (l);
        }        
    }

    private static void assertImage(String msg, Image img1, Image img2) {
        ImageObserver obs = new ImageObserver() {
            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                fail("Already updated, hopefully");
                return true;
            }
        };
        
        int h, w;
        assertEquals("Width: " + msg, w = img1.getWidth(obs), img2.getWidth(obs));
        assertEquals("Height: " + msg, h = img1.getHeight(obs), img2.getHeight(obs));
        
        
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                //assertEquals("Pixel " + i + ", " + j + " same: " + msg, img1.get)
            }
        }
        
    }
}
