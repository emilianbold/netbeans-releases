/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem
import org.openide.loaders.*;
import org.openide.cookies.*;
import org.openide.util.*;

import java.beans.*;
import java.io.*;
import java.util.*;

import org.netbeans.junit.*;

/** Simulate deadlock from issue 47707.
 *
 * @author Radek Matous, Jaroslav Tulach
 */
public class InstanceDataObjecIssue47707Test extends NbTestCase {
    /** folder to create instances in */
    private DataObject inst;
    /** filesystem containing created instances */
    private FileSystem lfs;
    
    /** Creates new DataFolderTest */
    public InstanceDataObjecIssue47707Test(String name) {
        super (name);
    }
    
    public static void main (String[] args) throws Exception {
        junit.textui.TestRunner.run(new NbTestSuite (InstanceDataObjecIssue47707Test.class));
    }
    
    /** Setups variables.
     */
    protected void setUp () throws Exception {
        System.setProperty ("org.openide.util.Lookup", "org.openide.loaders.InstanceDataObjecIssue47707Test$Lkp");
        
        String fsstruct [] = new String [] {
            "A.settings",
        };
        
        TestUtilHid.destroyLocalFileSystem (getName());
        lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);

        FileObject bb = lfs.findResource("A.settings");
        
        inst = DataObject.find (bb);
    }

    public void testGetCookieCanBeCalledTwice () throws Exception {
        Object cookie = inst.getCookie (org.openide.cookies.InstanceCookie.class);
        
        assertNotNull ("There is at least data object", cookie);
        assertEquals ("Of right type", LkpForDO.class, cookie.getClass ());
        
    }
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup 
    implements Environment.Provider {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (this);
        }
        
        public org.openide.util.Lookup getEnvironment (DataObject obj) {
            return new LkpForDO (new org.openide.util.lookup.InstanceContent (), obj);
        }
    } // end of Lkp
    
    public static final class LkpForDO extends org.openide.util.lookup.AbstractLookup 
    implements org.openide.cookies.InstanceCookie, Runnable {
        private boolean triedToDeadlock;
        private DataObject obj;
        
        private LkpForDO (org.openide.util.lookup.InstanceContent ic, DataObject obj) {
            super (ic);
            ic.add (this);
            this.obj = obj;
        }
        
        public void run () {
            // tries to query instance data object from other thread
            Object o = obj.getCookie (InstanceCookie.class);
            assertNotNull ("Cookie is there", o);
        }

        protected void beforeLookup(Template template) {
            if (!triedToDeadlock) {
                triedToDeadlock = true;
                org.openide.util.RequestProcessor.getDefault ().post (this).waitFinished ();
            }
        }
        
        
        public String instanceName () {
            return getClass ().getName ();
        }

        public Class instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            return getClass ();
        }

        public Object instanceCreate ()
        throws java.io.IOException, ClassNotFoundException {
            return this;
        }
        
    } // end LkpForDO
    
}
