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

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import junit.framework.Test;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.*;

import org.openide.loaders.DefaultDataObjectTest.JspLoader;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.ChangeSupport;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class FolderChildrenTest extends NbTestCase {
    private Logger LOG;
    public FolderChildrenTest() {
        super("");
    }

    public FolderChildrenTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 65000;
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    public static Test suite() {
        Test t = null;
//        t = new FolderChildrenTest("testALotOfHiddenEntries");
        if (t == null) {
            t = new NbTestSuite(FolderChildrenTest.class);
        }
        return t;
    }
    protected void assertChildrenType(Children ch) {
        assertEquals("Use lazy children by default", FolderChildren.class, ch.getClass());
    }

    private static void setSystemProp(String key, String value) {
        java.util.Properties prop = System.getProperties();
        if (prop.get(key) != null) return;
        prop.put(key, value);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();

        LOG = Logger.getLogger("test." + getName());
        MockServices.setServices(Pool.class);
        Pool.setLoader(null);
        assertEquals("The right pool initialized", Pool.class, DataLoaderPool.getDefault().getClass());
        setSystemProp("netbeans.security.nocheck","true");

        FileObject[] arr = FileUtil.getConfigRoot().getChildren();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete();
        }
    }

    public void testCorrectLoggerName() throws Exception {
        FileObject fo = FileUtil.getConfigRoot();
        Node n = DataFolder.findFolder(fo).getNodeDelegate();
        Enumeration<String> en = java.util.logging.LogManager.getLogManager().getLoggerNames();
        while(en.hasMoreElements()) {
            String log = en.nextElement();
            if (log.startsWith("org.openide.loaders.FolderChildren")) {
                assertEquals("org.openide.loaders.FolderChildren", log);
            }
        }
    }

    @RandomlyFails // NB-Core-Build #2858
    public void testSimulateADeadlockThatWillBeFixedByIssue49459 () throws Exception {
        FileObject a = FileUtil.createData (FileUtil.getConfigRoot (), "XYZ49459/org-openide-loaders-FolderChildrenTest$N1.instance");
        FileObject bb = FileUtil.getConfigFile("XYZ49459");
        assertNotNull (bb);

        class Run implements Runnable {
            private boolean read;
            private DataFolder folder;

            public Node[] children;

            public Run (DataFolder folder) {
                this.folder = folder;
            }

            public void run () {
                if (!read) {
                    read = true;
                    Children.MUTEX.readAccess (this);
                    return;
                }


                // this will deadlock without fix #49459
                children = folder.getNodeDelegate ().getChildren ().getNodes (true);

            }
        }

        Run r = new Run (DataFolder.findFolder (bb));
        Children.MUTEX.writeAccess (r);

        assertNotNull ("Children filled", r.children);
        assertEquals ("But are empty as cannot wait under getNodes", 0, r.children.length);

        // try once more without the locks
        r.children = null;
        r.run ();
        assertNotNull ("But running without mutexs works better - children filled", r.children);
        assertEquals ("One child", 1, r.children.length);
        DataObject obj = (DataObject)r.children[0].getCookie (DataObject.class);
        assertNotNull ("There is data object", obj);
        assertEquals ("It belongs to our file", a, obj.getPrimaryFile ());
    }

    public void testAdditionOfNewFileDoesNotInfluenceAlreadyExistingLoaders ()
    throws Exception {
        FileUtil.createData (FileUtil.getConfigRoot (), "AA/org-openide-loaders-FolderChildrenTest$N1.instance");
        FileUtil.createData (FileUtil.getConfigRoot (), "AA/org-openide-loaders-FolderChildrenTest$N2.instance");

        FileObject bb = FileUtil.getConfigFile("AA");

        DataFolder folder = DataFolder.findFolder (bb);
        Node node = folder.getNodeDelegate();

        Node[] arr = node.getChildren ().getNodes (true);
        assertEquals ("There is a nodes for both", 2, arr.length);
        assertNotNull ("First one is our node", arr[0].getCookie (N1.class));

        FileObject n = bb.createData ("A.txt");
        Node[] newarr = node.getChildren ().getNodes (true);
        assertEquals ("There is new node", 3, newarr.length);

        n.delete ();

        Node[] last = node.getChildren ().getNodes (true);
        assertEquals ("Again they are two", 2, last.length);

        assertEquals ("First one is the same", last[0], arr[0]);
        assertEquals ("Second one is the same", last[1], arr[1]);

    }

    @RandomlyFails // NB-Core-Build #1058 (in FolderChildrenLazyTest)
    public void testChangeableDataFilter() throws Exception {
        String pref = getName() + "/";
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/A.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/B.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/AA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/BA.txt");


        FileObject bb = FileUtil.getConfigFile(pref + "/BB");

        Filter filter = new Filter();
        DataFolder folder = DataFolder.findFolder (bb);

        Children ch = folder.createNodeChildren( filter );
        Node[] arr = ch.getNodes (true);

        assertNodes( arr, new String[] { "A.txt", "AA.txt" } );
        filter.fire();
        arr = ch.getNodes (true);
        assertNodes( arr, new String[] { "B.txt", "BA.txt" } );
    }

    @RandomlyFails // NB-Core-Build #1049 (in FolderChildrenLazyTest), #1051 (in this)
    public void testChangeableDataFilterOnNodeDelegate() throws Exception {
        String pref = getName() + "/";
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/A.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/B.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/AA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/BA.txt");


        FileObject bb = FileUtil.getConfigFile(pref + "BB");

        Filter filter = new Filter();
        DataFolder folder = DataFolder.findFolder (bb);


        Node n = folder.getClonedNodeDelegate(filter);
        Children ch = n.getChildren();
        Node[] arr = ch.getNodes (true);

        assertNodes( arr, new String[] { "A.txt", "AA.txt" } );
        filter.fire();
        arr = ch.getNodes (true);
        assertNodes( arr, new String[] { "B.txt", "BA.txt" } );
    }


    public void testOrderAttributesAreReflected() throws Exception {
        FileObject root = FileUtil.createFolder(FileUtil.getConfigRoot(), "order");

        for (int i = 0; i < 256; i++) {
            FileUtil.createData(root, "file" + i + ".txt");
        }

        FileObject[] arr = root.getChildren();
        assertEquals(256, arr.length);

        for (int i = 0; i < 256; i++) {
            arr[i].setAttribute("position", i ^ 0x6B);
        }

        DataFolder folder = DataFolder.findFolder (root);
        Node n = folder.getNodeDelegate();
        Children ch = n.getChildren();
        Node[] nodes = ch.getNodes (true);
        assertEquals(256, nodes.length);

        for (int i = 0; i < 256; i++) {
            FileObject fo = nodes[i].getLookup().lookup(FileObject.class);
            assertNotNull(i + " Has file object: " + nodes[i], fo);
            assertEquals(i + " It is the correct one: ", arr[i ^ 0x6B], fo);
        }
    }

    private static Object holder;
    @RandomlyFails // NB-Core-Build #2838 (in FolderChildrenEagerTest)
    public void testChildrenCanGC () throws Exception {
        Filter filter = new Filter();
        holder = filter;

        String pref = getName() + '/';
        FileObject bb = FileUtil.createFolder(FileUtil.getConfigRoot(), pref + "/BB");
        bb.createData("Ahoj.txt");
        bb.createData("Hi.txt");
        DataFolder folder = DataFolder.findFolder(bb);

        Children ch = folder.createNodeChildren(filter);
        LOG.info("children created: " + ch);
        Node[] arr = ch.getNodes(true);
        LOG.info("nodes obtained" + arr);
        assertEquals("Accepts only Ahoj", 1, arr.length);
        LOG.info("The one node" + arr[0]);

        WeakReference ref = new WeakReference(ch);
        ch = null;
        arr = null;

        assertGC("Children can disappear even we hold the filter", ref);
    }

    @RandomlyFails // NB-Core-Build #1043 (in FolderChildrenEagerTest)
    public void testSeemsLikeTheAbilityToRefreshIsBroken() throws Exception {
        String pref = getName() + '/';
        FileObject bb = FileUtil.createFolder(FileUtil.getConfigRoot(), pref + "/BB");
	bb.createData("Ahoj.txt");
	bb.createData("Hi.txt");

        DataFolder folder = DataFolder.findFolder (bb);

	Node n = folder.getNodeDelegate();
	Node[] arr = n.getChildren().getNodes(true);
	assertEquals("Both are visible", 2, arr.length);

	WeakReference ref = new WeakReference(arr[0]);
	arr = null;
	assertGC("Nodes can disappear", ref);


	bb.createData("Third.3rd");

	arr = n.getChildren().getNodes(true);
	assertEquals("All are visbile ", 3, arr.length);
    }

    @RandomlyFails // NB-Core-Build #1868
    public void testReorderAfterRename() throws Exception {
        String pref = getName() + '/';
        FileObject bb = FileUtil.createFolder(FileUtil.getConfigRoot(), pref + "/BB");
        FileObject ahoj = bb.createData("Ahoj.txt");
        bb.createData("Hi.txt");

        DataFolder folder = DataFolder.findFolder (bb);

        Node n = folder.getNodeDelegate();
        Node[] arr = n.getChildren().getNodes(true);
        assertEquals("Both are visible", 2, arr.length);
        assertEquals("Ahoj is 1st", "Ahoj.txt", arr[0].getName());
        assertEquals("Hi is 2nd", "Hi.txt", arr[1].getName());


        DataObject obj = DataObject.find(ahoj);
        obj.rename("xyz.txt");

        arr = n.getChildren().getNodes(true);
        assertEquals("All are visbile ", 2, arr.length);
        assertEquals("Hi is 1st", "Hi.txt", arr[0].getName());
        assertEquals("xyz is 2nd", "xyz.txt", arr[1].getName());
    }


    public static class N1 extends org.openide.nodes.AbstractNode
    implements Node.Cookie {
        public N1 () {
            this (true);
        }

        private N1 (boolean doGc) {
            super (org.openide.nodes.Children.LEAF);

            if (doGc) {
                for (int i = 0; i < 5; i++) {
                    System.gc ();
                }
            }
        }

        @Override
        public Node cloneNode () {
            return new N1 (false);
        }

        @Override
        public Node.Cookie getCookie (Class c) {
            if (c == getClass ()) {
                return this;
            }
            return null;
        }
    }

    public static final class N2 extends N1 {
    }


    private void assertNodes( Node[] nodes, String names[] ) {
        Object t = Arrays.asList(nodes);
        assertEquals( "Wrong number of nodes: " + t, names.length, nodes.length );

        for( int i = 0; i < nodes.length; i++ ) {
            assertEquals( "Wrong name at index " + i + ": " + t, names[i], nodes[i].getName() );
        }

    }

    private static class Filter implements ChangeableDataFilter  {

        private boolean selectA = true;

        private final ChangeSupport cs = new ChangeSupport(this);

        public boolean acceptDataObject (DataObject obj) {
            String fileName = obj.getPrimaryFile().getName();
            boolean select = fileName.startsWith( "A" );
            select = selectA ? select : !select;
            return select;
        }

        public void addChangeListener( ChangeListener listener ) {
            cs.addChangeListener(listener);
        }

        public void removeChangeListener( ChangeListener listener ) {
            cs.removeChangeListener(listener);
        }

        public void fire( ) {

            selectA = !selectA;

            cs.fireChange();
        }

    }

    public void testChildrenListenToFilesystemByABadea () throws Exception {
        doChildrenListenToFilesystem (false);
    }
    public void testChildrenListenToFileByABadea () throws Exception {
        doChildrenListenToFilesystem (true);
    }

    private void doChildrenListenToFilesystem (boolean useFileObject) throws Exception {

        final Object waitObj = new Object();

        class MyFileChangeListener implements FileChangeListener {
            boolean created;

            public void fileFolderCreated(FileEvent fe) {}
            public void fileChanged(FileEvent fe) {}
            public void fileDeleted(FileEvent fe) {}
            public void fileRenamed(FileRenameEvent fe) {}
            public void fileAttributeChanged(FileAttributeEvent fe) {}
            public void fileDataCreated(FileEvent e) {
                synchronized (waitObj) {
                    created = true;
                    waitObj.notify();
                }
            }
        }

        final String FILE_NAME = "C.txt";

        MyFileChangeListener fcl = new MyFileChangeListener();



        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        Repository.getDefault().addFileSystem(fs);
        final FileObject workDir = FileUtil.createFolder (FileUtil.getConfigRoot(), "workFolder");
        final FileObject sibling = FileUtil.createFolder (FileUtil.getConfigRoot(), "unimportantSibling");

        workDir.addFileChangeListener(fcl);

        DataFolder workDirDo = DataFolder.findFolder(workDir);
        FolderChildren fc = new FolderChildren(workDirDo);

        // init the FolderChildren
        fc.getNodes();

        File newFile;

        if (useFileObject) {
            FileObject newFo = FileUtil.createData (workDir, FILE_NAME);
            newFile = FileUtil.toFile(newFo);
        } else {
            newFile = new File(FileUtil.toFile(workDir), FILE_NAME);
            new FileOutputStream(newFile).close();
        }

        // first or second run (second run is after caling workDir.refresh())
        boolean firstRun = true;

        synchronized (waitObj) {

            for(;;) {
                // wait for create notification
                if (!fcl.created)
                    waitObj.wait(5000);

                if (!fcl.created) {
                    System.out.println("Not received file create notification, can't test.");
                    if (firstRun) {
                        // didn't get a notification, we should get one by calling refresh()
                        firstRun = false;
                        workDir.refresh();
                        continue;
                    }
                    else {
                        // didn't get a notification even after second run
                        // FolderChildren probably didn't get a notification neither
                        // so it doesn't know anything about the new file => nothing to test
                        return;
                    }
                } else {
                    break;
                }
            }

            // wait for FolderChildren to receive and process the create notification
            int cnt = 10;
            while (cnt-- > 0 && fc.getNodes ().length < 1) {
                try {
                    Thread.sleep(300);
                }
                catch (InterruptedException e) {}
            }

            assertEquals("FolderChildren doesn't contain " + newFile, 1, fc.getNodes().length);
        }
    }

    @RandomlyFails // in FolderChildrenLazyTest in NB-Core-Build #1478
    public void testRenameOpenComponent() throws Exception {
        JspLoader.cnt = 0;
        Pool.setLoader(JspLoader.class);

        String fsstruct [] = new String [] {
            "AA/a.test"
        };

        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);
        Repository.getDefault().addFileSystem(lfs);

        FileObject fo = lfs.findResource("AA/a.test");
        assertNotNull("file not found", fo);
        DataObject obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), DefaultDataObject.class);

        Node[] origNodes = obj.getFolder().getNodeDelegate().getChildren().getNodes(true);
        assertEquals("One node", 1, origNodes.length);
        assertEquals("the obj", obj, origNodes[0].getLookup().lookup(DataObject.class));

        obj.rename("ToSomeStrangeName.jsp");
        assertFalse("Invalid now", obj.isValid());

        DataObject newObj = DataObject.find(obj.getPrimaryFile());
        if (newObj == obj) {
            fail("They should be different now: " + obj + ", " + newObj);
        }

        Node[] newNodes = obj.getFolder().getNodeDelegate().getChildren().getNodes(true);
        assertEquals("One new node", 1, newNodes.length);
        assertEquals("the new obj", newObj, newNodes[0].getLookup().lookup(DataObject.class));

    }

    public void testRefreshInvalidDO() throws Exception {
        String fsstruct [] = new String [] {
            "AA/a.test"
        };

        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);
        Repository.getDefault().addFileSystem(lfs);

        FileObject fo = lfs.findResource("AA/a.test");
        assertNotNull("file not found", fo);
        DataObject obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), DefaultDataObject.class);

        Node folderNode = obj.getFolder().getNodeDelegate();

        Node[] origNodes = folderNode.getChildren().getNodes(true);
        assertEquals("One node", 1, origNodes.length);
        assertEquals("the obj", obj, origNodes[0].getLookup().lookup(DataObject.class));

        LOG.info("before setValid");
        obj.setValid(false);
        LOG.info("end of setValid");
        assertFalse("Invalid now", obj.isValid());

        DataObject newObj = DataObject.find(obj.getPrimaryFile());
        assertNotSame(newObj, obj);

        LOG.info("before getNodes: " + Arrays.asList(origNodes));
        Node[] newNodes = folderNode.getChildren().getNodes(true);
        LOG.info("end    getNodes: " + Arrays.asList(newNodes));
        assertEquals("One new node", 1, newNodes.length);
        assertEquals("the new obj", newObj, newNodes[0].getLookup().lookup(DataObject.class));

    }

    public void testCheckType() {
        DataFolder folder = DataFolder.findFolder(FileUtil.createMemoryFileSystem().getRoot());
        Children ch = folder.getNodeDelegate().getChildren();
        assertChildrenType(ch);
    }
    
    public void testDeadlockWithChildrenMutex() throws Exception {
        class R implements Runnable, NodeListener {
            private RequestProcessor RP = new RequestProcessor("testDeadlockWithChildrenMutex");
            private Node node;
            private FileObject folderAA;
            private FileObject fileATXTInFolderAA;
            private DataObject[] arr;
            private DataObject[] newarr;
            private DataFolder folder;
            private Node[] nodes;
            private int changes;
            public void init() throws IOException {
                FileUtil.createData(FileUtil.getConfigRoot(), "AA/org-openide-loaders-FolderChildrenTest$N1.instance");
                FileUtil.createData(FileUtil.getConfigRoot(), "AA/org-openide-loaders-FolderChildrenTest$N2.instance");

                folderAA = FileUtil.getConfigFile("/AA");

                folder = DataFolder.findFolder(folderAA);
                node = folder.getNodeDelegate();
                node.getChildren().getNodes(true);
                node.addNodeListener(this);
            }

            int state;
            public void run() {
                try {
                    switch (state++) {
                        case 0: clean(); return;
                        case 1: createATxt(); return;
                        default: throw new IllegalStateException("state: " + (state - 1));
                    }
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            private void createATxt() throws IOException {
                fileATXTInFolderAA = folderAA.createData("A.txt");
            }

            private void clean() throws IOException {
                arr = folder.getChildren();
                assertEquals("There is a obj for both", 2, arr.length);
                // calls createATxt in different thread
                RP.post(this).waitFinished();
                newarr = folder.getChildren();
                assertEquals("There is new node", 3, newarr.length);
                fileATXTInFolderAA.delete ();
            }

            public void finish() {
                Node[] last = node.getChildren ().getNodes (true);
                assertEquals ("Again they are two", 2, last.length);
            }

            private void ch() {
                nodes = node.getChildren().getNodes();
                changes++;
            }

            public void childrenAdded(NodeMemberEvent ev) {
                ch();
            }

            public void childrenRemoved(NodeMemberEvent ev) {
                ch();
            }

            public void childrenReordered(NodeReorderEvent ev) {
                ch();
            }

            public void nodeDestroyed(NodeEvent ev) {
                ch();
            }

            public void propertyChange(PropertyChangeEvent evt) {
                // oK
            }
        }


        R run = new R();
        run.init();
        CharSequence seq = Log.enable(FolderChildren.class.getName(), Level.WARNING);
        Children.MUTEX.readAccess(run);
        if (seq.length() > 0) {
            fail("No warnings please:\n" + seq);
        }
        run.finish();
    }

    @RandomlyFails // NB-Core-Build #985
    public void testCountNumberOfNodesWhenUsingFormLikeLoader() throws Exception {
        FileUtil.createData (FileUtil.getConfigRoot(), "FK/A.java");
        FileUtil.createData (FileUtil.getConfigRoot(), "FK/A.formKit");

        Pool.setLoader(FormKitDataLoader.class);

        FileObject bb = FileUtil.getConfigFile("/FK");

        DataFolder folder = DataFolder.findFolder (bb);

        Node[] arr = folder.getNodeDelegate().getChildren().getNodes(true);

        assertNodes( arr, new String[] { "A" } );
    }

    public void testALotOfHiddenEntries() throws Exception {
        FileObject folder = FileUtil.createFolder(FileUtil.getConfigRoot(), "aLotOf");
        List<FileObject> arr = new ArrayList<FileObject>();
        final int FILES = 1000;
        for (int i = 0; i < FILES; i++) {
            arr.add(FileUtil.createData(folder, "" + i + ".dat"));
        }

        DataFolder df = DataFolder.findFolder(folder);

        VisQ visq = new VisQ();

        FilterNode fn = new FilterNode(new FilterNode(new AbstractNode(df.createNodeChildren(visq))));
        class L implements NodeListener {
            int cnt;

            public void childrenAdded(NodeMemberEvent ev) {
                cnt++;
            }

            public void childrenRemoved(NodeMemberEvent ev) {
                cnt++;
            }

            public void childrenReordered(NodeReorderEvent ev) {
                cnt++;
            }

            public void nodeDestroyed(NodeEvent ev) {
                cnt++;
            }

            public void propertyChange(PropertyChangeEvent evt) {
                cnt++;
            }
        }
        L listener = new L();
        fn.addNodeListener(listener);

        List<Node> nodes = new ArrayList<Node>();
        int cnt = fn.getChildren().getNodesCount(true);
        List<Node> snapshot = fn.getChildren().snapshot();
        assertEquals("Count as expected", cnt, snapshot.size());
        for (int i = 0; i < cnt; i++) {
            nodes.add(snapshot.get(i));
        }
        assertEquals("No events delivered", 0, listener.cnt);
        assertEquals("Size is half cut", FILES / 2, fn.getChildren().getNodesCount(true));
    }

    public static final class VisQ implements VisibilityQueryImplementation, DataFilter.FileBased {
        public boolean isVisible(FileObject file) {
            try {
                int number = Integer.parseInt(file.getName());
                return number % 2 == 0;
            } catch (NumberFormatException numberFormatException) {
                return true;
            }
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public boolean acceptDataObject(DataObject obj) {
            return isVisible(obj.getPrimaryFile());
        }

        public boolean acceptFileObject(FileObject obj) {
            return isVisible(obj);
        }

    }


    public static final class Pool extends DataLoaderPool {
        private static Class<? extends DataLoader> loader;

        /**
         * @return the loader
         */
        private static Class<? extends DataLoader> getLoader() {
            return loader;
        }

        /**
         * @param aLoader the loader to set
         */
        static void setLoader(Class<? extends DataLoader> aLoader) {
            loader = aLoader;
            ((Pool)getDefault()).fireChangeEvent(new ChangeEvent(getDefault()));
        }

        @Override
        protected Enumeration<? extends DataLoader> loaders() {
            Class<? extends DataLoader> l = getLoader();
            return l == null ? Enumerations.<DataLoader>empty() : Enumerations.singleton(DataLoader.getLoader(l));
        }
    }

    public static class FormKitDataLoader extends MultiFileLoader {
        public static final String FORM_EXTENSION = "formKit"; // NOI18N
        private static final String JAVA_EXTENSION = "java"; // NOI18N

        private static final long serialVersionUID = 1L;
        static int cnt;

        public FormKitDataLoader() {
            super(FormKitDataObject.class.getName());
        }

        @Override
        protected String defaultDisplayName() {
            return NbBundle.getMessage(FormKitDataLoader.class, "LBL_FormKit_loader_name");
        }

        protected FileObject findPrimaryFile(FileObject fo)
        {
            cnt++;

            String ext = fo.getExt();
            if (ext.equals(FORM_EXTENSION))
            {
                return FileUtil.findBrother(fo, JAVA_EXTENSION);
            }
            if (ext.equals(JAVA_EXTENSION) && FileUtil.findBrother(fo, FORM_EXTENSION) != null)
            {
                return fo;
            }
            return null;
        }

        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, java.io.IOException
        {
            return new FormKitDataObject(FileUtil.findBrother(primaryFile, FORM_EXTENSION),
                    primaryFile,
                    this);
        }

        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject multiDataObject, FileObject fileObject)
        {
            FileEntry formEntry = new FileEntry(multiDataObject, fileObject);
            return formEntry;
        }

        @Override
        protected Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }

        public final class FormKitDataObject extends MultiDataObject {
            FileEntry formEntry;

            public FormKitDataObject(FileObject ffo, FileObject jfo, FormKitDataLoader loader) throws DataObjectExistsException, IOException
            {
                super(jfo, loader);
                formEntry = (FileEntry)registerEntry(ffo);
            }


        }
    }

}
