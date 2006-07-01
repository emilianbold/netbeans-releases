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

import java.io.File;
import java.io.IOException;
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.Set;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.ErrorManager;

import org.openide.filesystems.*;
import org.openide.nodes.*;

// XXX to do:
// - loaders are never asked to recognize an invalid file object (#13926)

/** Test invalidation of objects: getting of node delegate,
 * whether folder instances include them, whether loaders are
 * asked to recognize invalid objects, etc.
 * @author Jesse Glick
 */
public class DataObjectInvalidationOneFileTest extends LoggingTestCaseHid {
    Logger log;
    
    public DataObjectInvalidationOneFileTest(String name) {
        super(name);
    }

    protected Level logLevel() {
        return Level.FINE;
    }
    
    protected void setUp() throws IOException {
        clearWorkDir();

        log = Logger.getLogger("TEST-" + getName());
        registerIntoLookup(new Pool());
    }
    
    protected void tearDown() throws Exception {
        WeakReference ref = new WeakReference(DataLoader.getLoader(SlowDataLoader.class));
        Pool.setExtra(null);
        assertGC("Let's cleanup all nodes, data objects created in previous test", ref);
    }
    
    /** Tests that the loader pool does not
     * try to create a DataObject for a given file object more
     * than once.
     * Refer to #15898.
     */
    public void testDataObjectsCreatedOncePerFile() throws Exception {
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
            "folder/file.slow",
        });
        FileObject folder = lfs.findResource("folder");
        DataLoader l = DataLoader.getLoader(SlowDataLoader.class);
        log.info("Registering the slow loader");
        Pool.setExtra(l);
        
        log.info("Clearing the counts");
        SlowDataLoader.createCount = 0;
        SlowDataObject.createCount = 0;
        log.info("Counts cleared");
        
        DataFolder f = DataFolder.findFolder(folder);
        
        log.info("Folder created: " + f);
        
        Node foldernode = f.getNodeDelegate();
        
        log.info("Node created: " + foldernode);
        
        Children folderkids = foldernode.getChildren();
        
        log.info("Children are here");
        assertEquals("Getting a folder node does not start automatically scanning children", 0, SlowDataLoader.createCount);
        assertEquals("Getting a folder node does not finish automatically scanning children", 0, SlowDataObject.createCount);
        
        Node[] keep = folderkids.getNodes(true);
        
        log.info("Nodes for children are computed: " + keep.length);
        
        assertEquals("After getting folder node children, a data object is not started to be created >1 time", 1, SlowDataLoader.createCount);
        assertEquals("After getting folder node children, a data object is not successfully created >1 time", 1, SlowDataObject.createCount);
    }

    private static final class ExpectingListener implements PropertyChangeListener {
        private final Set changes = new HashSet(); // Set<String>
        public synchronized void propertyChange(PropertyChangeEvent ev) {
            changes.add(ev.getPropertyName());
            //System.err.println("got: " + ev.getSource() + " " + ev.getPropertyName() + " " + ev.getOldValue() + " " + ev.getNewValue());//XXX
            notifyAll();
        }
        public synchronized boolean gotSomething(String prop) throws InterruptedException {
            if (changes.contains(prop)) return true;
            wait(3000);
            return changes.contains(prop);
        }
    }
    
    public static final class SlowDataLoader extends UniFileLoader {
        public static int createCount = 0;
        private static Logger ERR = Logger.getLogger("SlowDataLoader");
        public SlowDataLoader() {
            super(SlowDataObject.class.getName());
        }
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("slow");
        }
        protected String displayName() {
            return "Slow";
        }
        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            ERR.info("in createMultiObject for: " + pf);
            SlowDataObject o = new SlowDataObject(pf, this);
            ERR.info("created object : " + o);
            //new Exception("creating for: " + pf + " count=" + createCount).printStackTrace();
            return o;
        }
    }
    public static final class SlowDataObject extends MultiDataObject {
        public Thread ok;
        public static int createCount = 0;
        public SlowDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
            super(pf, loader);
            synchronized (loader) {
                SlowDataLoader.ERR.info("Incrementing SlowDataObject count to " + ++createCount);
                SlowDataLoader.ERR.info("Incrementing SlowDataLoader count to " + ++SlowDataLoader.createCount);
                
                // in case somebody is listening on the loader for our creation
                // let him wake up
                SlowDataLoader.ERR.info("Wake up sleepers");
                loader.notifyAll ();
            }
            
            int cnt = 1;
            
            while (cnt-- > 0) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    throw new IOException(ie.toString());
                }
            }
            
            
            ok = Thread.currentThread();
            SlowDataLoader.ERR.info("End of constructor");
        }
        protected Node createNodeDelegate() {
            return new SlowDataNode(this);
        }
        
        protected DataObject handleCopy (DataFolder df) throws java.io.IOException {
            FileObject fo = this.getPrimaryEntry().copy (df.getPrimaryFile(), "slow");
            return new SlowDataObject (fo, (MultiFileLoader)this.getLoader());
        }
        protected DataObject handleCreateFromTemplate (DataFolder df, String s) throws java.io.IOException {
            FileObject fo = this.getPrimaryEntry().createFromTemplate (df.getPrimaryFile(), null);
            return new SlowDataObject (fo, (MultiFileLoader)this.getLoader());
        }
    }
    public static final class SlowDataNode extends DataNode {
        public SlowDataNode(SlowDataObject o) {
            super(o, Children.LEAF);
            if (o.ok == null) throw new IllegalStateException("getDataNode called too early");
            // Serve as a marker that this is the correct data node kind
            // (instanceof will not work because of filter nodes):
            setShortDescription("slownode");
        }
    }

    private static final class Pool extends DataLoaderPool {
        private static DataLoader extra;
        
        
        protected java.util.Enumeration loaders () {
            if (extra == null) {
                return org.openide.util.Enumerations.empty ();
            } else {
                return org.openide.util.Enumerations.singleton (extra);
            }
        }

        public static void setExtra(DataLoader aExtra) {
            if (aExtra != null && extra != null) {
                fail("Cannot set extra loader while one is already there. 1: " + extra + " 2: " + aExtra);
            }
            extra = aExtra;
            Pool p = (Pool)DataLoaderPool.getDefault();
            p.fireChangeEvent(new javax.swing.event.ChangeEvent(p));
        }
    }
}
