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
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/** It must be possible to create lookup anytime, if there is no deadlock,
 * even if the recognition of FolderLookup takes really long time.
 *
 * @author Jaroslav Tulach
 */
public class CanYouCreateFolderLookupFromHandleFindSlowVersionTest extends NbTestCase {
    
    /** Creates a new instance of CanYouQueryFolderLookupFromHandleFindTest */
    public CanYouCreateFolderLookupFromHandleFindSlowVersionTest(String s) {
        super(s);
    }
    
    protected void setUp() {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        assertEquals("Lookup registered", Lkp.class, Lookup.getDefault().getClass());
    }
    
    public void testCreateAndImmediatellyQueryWhenThereIsALotfSlowDataObjectsTheLookup() throws Exception {
        MyLoader m = (MyLoader)MyLoader.getLoader(MyLoader.class);
        m.button = FileUtil.createFolder(Repository.getDefault().getDefaultFileSystem().getRoot(), "FolderLookup");
        DataObject instance = InstanceDataObject.create(DataFolder.findFolder(m.button), "SomeName", JButton.class);
        m.instanceFile = instance.getPrimaryFile();
        for (int i = 0; i < 15; i++) {
            m.button.createData("slow" + i + ".slow");
        }
        
        
        WeakReference ref = new WeakReference(instance);
        instance = null;
        assertGC("Object must disappear first", ref);
        
        FileObject any = Repository.getDefault().getDefaultFileSystem().getRoot().createData("Ahoj.txt");
        DataObject obj = DataObject.find(any);
        
        assertEquals("The right object found", m, obj.getLoader());
        assertNotNull("Value found", m.v);
        assertEquals("Button", JButton.class, m.v.getClass());
        assertNotNull("Lookup created", m.lookup);
        assertEquals("All slow files recognized", 15, m.slowCnt);
    }
    
    
    public static final class Lkp extends AbstractLookup {
        public Lkp() {
            this(new InstanceContent());
        }
        
        private Lkp(InstanceContent ic) {
            super(ic);
            ic.add(new Pool());
        }
    }
    
    public static final class MyLoader extends UniFileLoader {
        public FileObject button;
        public Object v;
        public Lookup lookup;
        
        public InstanceDataObject created;
        
        private FileObject instanceFile;
        
        private DataObject middleCreation;

        private int slowCnt;
        
        public MyLoader() throws IOException {
            super("org.openide.loaders.MultiDataObject");
        }
        
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt("slow")) {
                slowCnt++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    fail("No failures, please");
                }
                return null;
            }
            
            if (!fo.hasExt("txt")) {
                return null;
            }
            
            assertNull("First invocation", lookup);
            
            FolderLookup l = new FolderLookup(DataFolder.findFolder(button));
            lookup = l.getLookup();
            v = lookup.lookup(JButton.class);
            assertNotNull("The instance computed", v);
            
            return fo;
        }
        
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MultiDataObject(primaryFile, this);
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        static List loaders;
        
        public Pool() {
        }
        
        public Enumeration loaders() {
            return Enumerations.singleton(DataLoader.getLoader(MyLoader.class));
        }
    }
    
}
