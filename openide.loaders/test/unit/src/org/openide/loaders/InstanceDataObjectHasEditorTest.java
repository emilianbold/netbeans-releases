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

import java.awt.Button;
import java.util.Date;
import junit.framework.*;
import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.*;
import org.openide.modules.ModuleInfo;
import org.openide.util.*;
import org.openide.util.Utilities;
import org.openide.util.actions.*;
import org.openide.nodes.*;
import java.util.List;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstanceDataObjectHasEditorTest extends org.netbeans.junit.NbTestCase {
    private FileObject fo;
    
    
    public InstanceDataObjectHasEditorTest (String testName) {
        super (testName);
    }
    
    protected void setUp () throws java.lang.Exception {
        clearWorkDir ();

        // initialize modules
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    public void testSettingsFileOnSFSShouldHaveEditor () throws Exception {
        FileObject set = createSettings (Repository.getDefault ().getDefaultFileSystem ().getRoot (), "x.settings");
        DataObject obj = DataObject.find (set);
        assertEquals (InstanceDataObject.class, obj.getClass ());
        assertNull ("It does not have edit cookie", obj.getCookie (EditCookie.class));
        assertNull ("It does not have open cookie", obj.getCookie (OpenCookie.class));
        assertNull ("It does not have editor cookie", obj.getCookie (EditorCookie.class));
        
        Object o = obj.getNodeDelegate ().getPreferredAction ();
        Class c = o == null ? Object.class : o.getClass ();
        
        if (c == org.openide.actions.OpenAction.class) {
            fail ("Default actions should not be open on SFS: " + o);
        }
    }
    
    public void testSettingsFileOnNonSFSShouldHaveEditor () throws Exception {
        clearWorkDir ();
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory (getWorkDir ());
        
        FileObject set = createSettings (lfs.getRoot (), "x.settings");
        DataObject obj = DataObject.find (set);
        assertEquals (InstanceDataObject.class, obj.getClass ());
        assertNotNull ("It has edit cookie", obj.getCookie (EditCookie.class));
        assertNotNull ("It has open cookie", obj.getCookie (OpenCookie.class));
        assertNotNull ("It has editor cookie", obj.getCookie (EditorCookie.class));

        Object o = obj.getNodeDelegate ().getPreferredAction ();
        Class c = o == null ? Object.class : o.getClass ();
        
        assertEquals ("Default actions should be open on non-SFS", org.openide.actions.OpenAction.class, c);
    }
    
    public void testSettingsFileOnNonSFSAfterCopyShouldHaveEditor () throws Exception {
        clearWorkDir ();
        LocalFileSystem lfs = new LocalFileSystem ();
        lfs.setRootDirectory (getWorkDir ());
        
        FileObject set = createSettings (lfs.getRoot (), "x.settings");
        DataObject old = DataObject.find (set);
        Date d = set.lastModified();
        
        /* This code would work only with core/settings, so moving the test there
        InstanceCookie ic = (InstanceCookie)old.getCookie(InstanceCookie.class);
        assertNotNull ("The cookie is there", ic);
        Object instance = ic.instanceCreate();
        assertNotNull ("It produces a result", instance);
        assertEquals ("It is Button", Button.class, instance.getClass ());
         */
        
        FileObject tgt = FileUtil.createFolder(lfs.getRoot (), "moved");
        DataFolder fld = DataFolder.findFolder (tgt);
        
        DataObject obj = old.copy (fld);
        
        assertEquals ("No change in modifications", d, set.lastModified());
        assertEquals ("The same name", obj.getPrimaryFile().getNameExt (), set.getNameExt());
        
        assertEquals (InstanceDataObject.class, obj.getClass ());
        assertNotNull ("It has edit cookie", obj.getCookie (EditCookie.class));
        assertNotNull ("It has open cookie", obj.getCookie (OpenCookie.class));
        assertNotNull ("It has editor cookie", obj.getCookie (EditorCookie.class));

        Object o = obj.getNodeDelegate ().getPreferredAction ();
        Class c = o == null ? Object.class : o.getClass ();
        
        assertEquals ("Default actions should be open on non-SFS", org.openide.actions.OpenAction.class, c);
    }

    private FileObject createSettings (FileObject root, String name) throws IOException {
        FileObject set = FileUtil.createData (root, name);

        FileLock lock = set.lock ();
        PrintStream os = new PrintStream (set.getOutputStream (lock));
        
        os.println ("<?xml version=\"1.0\"?>");
        os.println ("<!DOCTYPE settings PUBLIC \"-//NetBeans//DTD Session settings 1.0//EN\" \"http://www.netbeans.org/dtds/sessionsettings-1_0.dtd\">");
        os.println ("<settings version=\"1.0\">");
//        os.println ("<module name=\"org.apache.tools.ant.module/3\" spec=\"3.15\"/>");
        os.println ("<instanceof class=\"java.io.Serializable\"/>");
        os.println ("<instanceof class=\"java.lang.Object\"/>");
        os.println ("<instanceof class=\"java.awt.Component\"/>");
        os.println ("<instance class=\"java.awt.Button\"/>");
        os.println ("</settings>");
        
        os.close ();
        lock.releaseLock();
        return set;
    }
}
