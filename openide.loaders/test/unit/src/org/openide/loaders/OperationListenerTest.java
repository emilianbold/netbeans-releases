/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import junit.framework.AssertionFailedError;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import java.beans.*;
import java.io.IOException;
import java.util.*;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import org.openide.util.Enumerations;
import org.openide.util.RequestProcessor;

/* 
 * Checks whether a during a modify operation (copy, move) some
 * other thread can get a grip on unfinished and uncostructed 
 * content on filesystem.
 *
 * @author Jaroslav Tulach
 */
public class OperationListenerTest extends NbTestCase 
implements OperationListener {
    private ArrayList events = new ArrayList ();
    private FileSystem fs;
    private DataLoaderPool pool;
    
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.loaders.OperationListenerTest$Lkp");
    }

    /** Creates the test */
    public OperationListenerTest(String name) {
        super(name);
    }
    
    // For each test setup a FileSystem and DataObjects
    protected void setUp() throws Exception {
        String fsstruct [] = new String [] {
            "source/A.attr", 
            "B.attr",
            "dir/",
            "fake/A.instance"
        };
        TestUtilHid.destroyLocalFileSystem (getName());
        fs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        
        assertNotNull("ErrManager has to be in lookup", org.openide.util.Lookup.getDefault().lookup(ErrManager.class));
        ErrManager.resetMessages();
        
        pool = DataLoaderPool.getDefault ();
        assertNotNull (pool);
        assertEquals (Pool.class, pool.getClass ());
        
        Pool.extra = null;
    }
    
    //Clear all stuff when the test finish
    protected void tearDown() throws Exception {
        pool.removeOperationListener(this);
        
//        AddLoaderManuallyHid.addRemoveLoader (ALoader.getLoader (ALoader.class), false);
//        AddLoaderManuallyHid.addRemoveLoader (BLoader.getLoader (BLoader.class), false);
    }
    
    protected void runTest () throws Throwable {
        try {
            super.runTest ();
        } catch (AssertionFailedError err) {
            AssertionFailedError n = new AssertionFailedError (err.getMessage () + "\n" + ErrManager.messages);
            n.initCause (err);
            throw n;
        }
    }
    
    //
    // Tests
    //
    
    public void testRecognizeFolder () {
        pool.addOperationListener(this);
        DataFolder df = DataFolder.findFolder (fs.findResource ("fake"));
        DataObject[] arr = df.getChildren ();
        
        assertEquals ("One child", 1, arr.length);
        
        assertEvents ("Recognized well", new OperationEvent[] {
            new OperationEvent (df),
            new OperationEvent (arr[0])
        });
    }

    public void testCopyFile() throws Exception {
        pool.addOperationListener(this);
        DataObject obj = DataObject.find (fs.findResource ("fake/A.instance"));
        DataFolder df = DataFolder.findFolder (fs.findResource ("dir"));
        DataObject n = obj.copy (df);
        assertEquals ("Copy successfull", n.getFolder(), df);
        
        assertEvents ("All well", new OperationEvent[] {
            new OperationEvent (obj),
            new OperationEvent (df),
            new OperationEvent (n),
            new OperationEvent.Copy (n, obj)
        });
    }
    
    public void testBrokenLoader () throws Exception {
        BrokenLoader loader = (BrokenLoader)DataLoader.getLoader(BrokenLoader.class);
        
        try {
            Pool.extra = loader;
            
            pool.addOperationListener(this);
            
            loader.acceptableFO = fs.findResource ("source/A.attr");
            try {
                DataObject obj = DataObject.find (fs.findResource ("source/A.attr"));
                fail ("The broken loader throws exception and cannot be created");
            } catch (IOException ex) {
                // ok
            }
            assertEquals ("Loader created an object", loader, loader.obj.getLoader());
            
            // and the task can be finished
            loader.recognize.waitFinished ();
            
            assertEvents ("One creation notified even if the object is broken", new OperationEvent[] {
                new OperationEvent (loader.obj),
            });
        } finally {
            Pool.extra = null;
        }
    }
    
    //
    // helper methods
    //
    
    private void assertEvents (String txt, OperationEvent[] expected) {
        boolean failure = false;
        if (expected.length != events.size ()) {
            failure = true;
        } else {
            for (int i = 0; i < expected.length; i++) {
                OperationEvent e = expected[i];
                OperationEvent r = (OperationEvent)events.get (i);
                if (e.getClass  () != r.getClass ()) {
                    failure = true;
                    break;
                }
                if (e.getObject () != r.getObject()) {
                    failure = true;
                    break;
                }
            }
        }
        
        
        if (failure) {
            StringBuffer sb = new StringBuffer ();
            
            int till = Math.max (expected.length, events.size ());
            sb.append ("Expected events: " + expected.length + " was: " + events.size () + "\n");
            for (int i = 0; i < till; i++) {
                sb.append ("  Expected: ");
                if (i < expected.length) {
                    sb.append (expected[i].getClass () + " source: " + expected[i].getObject ());
                }
                sb.append ('\n');
                sb.append ("  Was     : ");
                if (i < events.size ()) {
                    OperationEvent ev = (OperationEvent)events.get (i);
                    sb.append (ev.getClass () + " source: " + ev.getObject ());
                }
                sb.append ('\n');
            }
            
            fail (sb.toString ());
        }
        
        events.clear();
    }
    
    //
    // Listener implementation
    //
    
    public void operationCopy(org.openide.loaders.OperationEvent.Copy ev) {
        events.add (ev);
        org.openide.ErrorManager.getDefault().log ("  operationCopy: " + ev);
    }
    
    public void operationCreateFromTemplate(org.openide.loaders.OperationEvent.Copy ev) {
        events.add (ev);
        org.openide.ErrorManager.getDefault().log ("  operationCreateFromTemplate: " + ev);
    }
    
    public void operationCreateShadow(org.openide.loaders.OperationEvent.Copy ev) {
        events.add (ev);
        org.openide.ErrorManager.getDefault().log ("  operationCreateShadow: " + ev);
    }
    
    public void operationDelete(OperationEvent ev) {
        events.add (ev);
        org.openide.ErrorManager.getDefault().log ("  operationDelete: " + ev);
    }
    
    public void operationMove(org.openide.loaders.OperationEvent.Move ev) {
        events.add (ev);
        org.openide.ErrorManager.getDefault().log ("  operationMove: " + ev);
    }
    
    public void operationPostCreate(OperationEvent ev) {
        events.add (ev);
        org.openide.ErrorManager.getDefault().log ("  operationPostCreate: " + ev);
    }
    
    public void operationRename(org.openide.loaders.OperationEvent.Rename ev) {
        events.add (ev);
        org.openide.ErrorManager.getDefault().log ("  operationRename: " + ev);
    }

    
    //
    // Own loader
    //
    public static final class BrokenLoader extends UniFileLoader {
        public FileObject acceptableFO;
        public RequestProcessor.Task recognize;
        public MultiDataObject obj;
        
        public BrokenLoader() {
            super(MultiDataObject.class.getName ());
        }
        protected String displayName() {
            return "BrokenLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (acceptableFO != null && acceptableFO.equals(fo)) {
                return fo;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(final FileObject primaryFile) throws DataObjectExistsException, IOException {
            obj = new MultiDataObject (primaryFile, this);
            
            assertNull ("Only one invocation of this code allowed", recognize);
            
            class R implements Runnable {
                public DataObject found;
                public void run () {
                    synchronized (this) {
                        notify ();
                    }
                    // this basicly means another call to createMultiObject method
                    // of this loader again, but the new MultiDataObject will throw
                    // DataObjectExistsException and will block in its
                    // getDataObject method
                    try {
                        found = DataObject.find (primaryFile);
                    } catch (IOException ex) {
                        fail ("Unexepcted exception: " + ex);
                    }
                    
                    assertEquals ("DataObjects are the same", found, obj);
                }
            }
            R run = new R ();
            synchronized (run) {
                recognize = RequestProcessor.getDefault ().post (run);
                try {
                    run.wait ();
                } catch (InterruptedException ex) {
                    fail ("Unexepcted ex: " + ex);
                }
            }
                
            
            throw new IOException ("I am broken!");
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }
        
        public void run() {
        }
        
    }
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }
        
        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
            ic.add(new ErrManager());
            ic.add(new Pool ());
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        public static DataLoader extra;
        
        
        protected Enumeration loaders () {
            if (extra == null) {
                return Enumerations.empty ();
            } else {
                return Enumerations.singleton (extra);
            }
        }
    }

    private static final class ErrManager extends org.openide.ErrorManager {
        static final StringBuffer messages = new StringBuffer();
        static int nOfMessages;
        static final String DELIMITER = ": ";
        static final String WARNING_MESSAGE_START = WARNING + DELIMITER;
        
        static void resetMessages() {
            messages.delete(0, ErrManager.messages.length());
            nOfMessages = 0;
        }
        
        public void log(int severity, String s) {
            nOfMessages++;
            messages.append(severity + DELIMITER + s);
            messages.append('\n');
        }
        
        public Throwable annotate(Throwable t, int severity,
                String message, String localizedMessage,
                Throwable stackTrace, Date date) {
            return t;
        }
        
        public Throwable attachAnnotations(Throwable t, Annotation[] arr) {
            return t;
        }
        
        public org.openide.ErrorManager.Annotation[] findAnnotations(Throwable t) {
            return null;
        }
        
        public org.openide.ErrorManager getInstance(String name) {
            return this;
        }
        
        public void notify(int severity, Throwable t) {}
    }
    
}
