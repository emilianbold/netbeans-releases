/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import junit.textui.TestRunner;

import org.openide.filesystems.*;
import org.openide.util.Lookup;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;
import java.beans.PropertyChangeListener;

/** Simulates the deadlock from issue 35847.
 * @author Jaroslav Tulach
 */
public class Deadlock35847Test extends NbTestCase {
    
    public Deadlock35847Test(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(Deadlock35847Test.class));
    }
    
    protected void setUp() throws Exception {
    }
    /*
    protected void tearDown() throws Exception {
    }
     */
    
    public void testLoaderThatStopsToRecognizeWhatItHasRecognized () throws Exception {
        ForgetableLoader l = (ForgetableLoader)DataLoader.getLoader(ForgetableLoader.class);
        AddLoaderManuallyHid.addRemoveLoader(l, true);
        try {
            FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), new String[] {
                "folder/f.forget",
                "folder/f.keep"
            });
            
            // do not recognize anything
            l.forget = true;
            
            FileObject fo = lfs.findResource("folder");
            DataFolder f = DataFolder.findFolder(fo);
            
            
            DataObject[] arr = f.getChildren ();
            assertEquals ("Two child there", 2, arr.length);
            
            DataObject keep;
            java.lang.ref.WeakReference forget;
            if (arr[0].getPrimaryFile().hasExt ("keep")) {
                keep = arr[0];
                forget = new java.lang.ref.WeakReference (arr[1]);
            } else {
                keep = arr[1];
                forget = new java.lang.ref.WeakReference (arr[0]);
            }
            
            org.openide.nodes.Node theDelegate = new org.openide.nodes.FilterNode (keep.getNodeDelegate());
            
            arr = null;
            assertGC ("Forgetable object can be forgeted", forget);
            
            class P extends org.openide.nodes.NodeAdapter
            implements java.beans.PropertyChangeListener {
                int cnt;
                String name;
                
                public void propertyChange (java.beans.PropertyChangeEvent ev) {
                    name = ev.getPropertyName();
                    cnt++;
                }
            }
            P listener = new P ();
            keep.addPropertyChangeListener (listener);
            // in order to trigger listening on the original node and cause deadlock
            theDelegate.addNodeListener(listener);
            
            // now recognize
            l.forget = false;
            
            // this will trigger invalidation of keep from Folder Recognizer Thread
            DataObject[] newArr = f.getChildren ();
            
            assertEquals ("Keep is Invalidated", 1, listener.cnt);
            assertEquals ("Property is PROP_VALID", DataObject.PROP_VALID, listener.name);
        } finally {
            AddLoaderManuallyHid.addRemoveLoader(l, false);
            // back to previous state
            l.forget = false;
        }
        TestUtilHid.destroyLocalFileSystem(getName());
    }
    
    public void testLoaderThatStopsToRecognizeWhatItHasRecognizedAndDoesItWhileHoldingChildrenMutex () throws Exception {
        org.openide.nodes.Children.MUTEX.readAccess (new org.openide.util.Mutex.ExceptionAction () {
            public Object run () throws Exception {
                testLoaderThatStopsToRecognizeWhatItHasRecognized ();
                return null;
            }
        });
    }
    

    public static final class ForgetableLoader extends MultiFileLoader {
        public boolean forget;
        
        public ForgetableLoader () {
            super(MultiDataObject.class);
        }
        protected String displayName() {
            return "ForgetableLoader";
        }
        /** Recognizes just two files - .forget and .keep at once, only in non-forgetable mode 
         */
        protected FileObject findPrimaryFile(FileObject fo) {
            if (forget) {
                return null;
            }
            if (fo.hasExt ("forget")) {
                return FileUtil.findBrother (fo, "keep");
            }
            if (fo.hasExt ("keep")) {
                return fo;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MultiDataObject (primaryFile, this);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry (obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
}
