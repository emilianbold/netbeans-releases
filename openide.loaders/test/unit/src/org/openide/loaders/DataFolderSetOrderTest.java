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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.filesystems.FileSystem;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Does a change in order on folder fire the right properties?
 *
 * @author  Jaroslav Tulach
 */
public class DataFolderSetOrderTest extends NbTestCase 
implements PropertyChangeListener {
    private DataFolder aa;
    private DataFolder bb;
    private ArrayList events = new ArrayList ();
    private static Task previous;
    
    public DataFolderSetOrderTest (String name) {
        super (name);
    }

    /** If execution fails we wrap the exception with 
     * new log message.
     */
    protected void runTest () throws Throwable {
        ErrManager.messages.append ("Starting test ");
        ErrManager.messages.append (getName ());
        ErrManager.messages.append ('\n');
        
        try {
            super.runTest ();
        } catch (AssertionFailedError ex) {
            AssertionFailedError ne = new AssertionFailedError (ex.getMessage () + " Log:\n" + ErrManager.messages);
            ne.setStackTrace (ex.getStackTrace ());
            throw ne;
        }
    }
    
    protected void setUp () throws Exception {
        System.setProperty("org.openide.util.Lookup", "org.openide.loaders.DataFolderSetOrderTest$Lkp");
        assertNotNull ("ErrManager has to be in lookup", Lookup.getDefault().lookup(ErrManager.class));
        
        if (previous != null) {
            previous.waitFinished ();
        }
        
        TestUtilHid.destroyLocalFileSystem (getName());
        String fsstruct [] = new String [] {
            "AA/X.txt",
            "AA/Y.txt",
            "BB/X.slow",
        };
        
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir (), fsstruct);

        aa = DataFolder.findFolder (lfs.findResource ("AA"));
        bb = DataFolder.findFolder (lfs.findResource ("BB"));    
        
        aa.addPropertyChangeListener (this);
    }
    
    protected void tearDown () throws Exception {
        final DataLoader l = DataLoader.getLoader(DataObjectInvalidationTest.SlowDataLoader.class);
        
        aa.removePropertyChangeListener (this);
    }
    
    private void makeFolderRecognizerBusy () throws Exception {
        if (getName().indexOf ("Busy") < 0) {
            return;
        }
        
        final DataLoader l = DataLoader.getLoader(DataObjectInvalidationTest.SlowDataLoader.class);
        synchronized (l) {
            // this will trigger bb.getChildren
            previous = RequestProcessor.getDefault().post(new Runnable() {
                public void run () {
                    DataObject[] arr = bb.getChildren ();
                }
            });
            
            // waits till the recognition blocks in the new SlowDataObject
            l.wait ();
        }
        
        // now the folder recognizer is blocked at least for 2s
    }

    private void doTest () throws Exception {
        DataObject[] arr = aa.getChildren ();
        assertEquals ("Two objects", 2, arr.length);
        ArrayList l = new ArrayList (Arrays.asList (arr));
        Collections.reverse (l);
        
        assertEquals ("No changes yet", 0, events.size ());
        makeFolderRecognizerBusy ();
        aa.setOrder ((DataObject[])l.toArray (new DataObject[0]));
        
        DataObject[] narr = aa.getChildren ();
        assertEquals ("Two again", 2, narr.length);
        
        assertSame ("1 == 2", arr[0], narr[1]);
        assertSame ("2 == 1", arr[1], narr[0]);
        
// PENDING-JST: Should be this, but         if (2 != events.size () || !events.contains (DataFolder.PROP_ORDER) || !events.contains (DataFolder.PROP_CHILDREN)) {
// lets test at least for this:
        if (!events.contains (DataFolder.PROP_ORDER) || !events.contains (DataFolder.PROP_CHILDREN)) {
            fail ("Wrong events: " + events);
        }
    }
    
    public void testReorderWithoutChecksWhenFolderReconizerIsBusy () throws Exception {
        doTest ();
    }

    
    public void testReorderWithoutChecks () throws Exception {
        doTest ();
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        events.add (evt.getPropertyName ());
    }

    //
    // Our fake lookup
    //
    public static final class Lkp extends AbstractLookup {
        public Lkp () {
            this (new InstanceContent());
        }
        
        private Lkp(InstanceContent ic) {
            super (ic);
            ic.add (new ErrManager ());
            ic.add (new Pool ());
        }
    }
    //
    // Logging support
    //
    public static final class ErrManager extends ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();
        
        private String prefix;
        
        public ErrManager () {
            this (null);
        }
        public ErrManager (String prefix) {
            this.prefix = prefix;
        }
        
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
            return t;
        }
        
        public Throwable attachAnnotations (Throwable t, ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        public ErrorManager getInstance (String name) {
            if (
                name.startsWith ("org.openide.loaders.FolderList")
//              || name.startsWith ("org.openide.loaders.FolderInstance")
            ) {
                return new ErrManager ('[' + name + ']');
            } else {
                // either new non-logging or myself if I am non-logging
                return new ErrManager ();
            }
        }
        
        public void log (int severity, String s) {
            if (prefix != null) {
                messages.append (prefix);
                messages.append (s);
                messages.append ('\n');
            }
        }
        
        public void notify (int severity, Throwable t) {
            log (severity, t.getMessage ());
        }
        
        public boolean isNotifiable (int severity) {
            return prefix != null;
        }
        
        public boolean isLoggable (int severity) {
            return prefix != null;
        }
        
    } // end of ErrManager
    
    private static final class Pool extends DataLoaderPool {
        
        protected Enumeration loaders() {
            return Enumerations.singleton(DataLoader.getLoader(DataObjectInvalidationTest.SlowDataLoader.class));
        }
        
    } // end of Pool
}
