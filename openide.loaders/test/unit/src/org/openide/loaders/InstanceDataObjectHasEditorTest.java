/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.loaders;

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
        
    }

    protected void tearDown () throws java.lang.Exception {
    }

    public void testSettingsFileOnSFSShouldHaveEditor () throws Exception {
        FileObject set = FileUtil.createData (Repository.getDefault ().getDefaultFileSystem ().getRoot (), "x.settings");
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
        
        FileObject set = FileUtil.createData (lfs.getRoot (), "x.settings");
        DataObject obj = DataObject.find (set);
        assertEquals (InstanceDataObject.class, obj.getClass ());
        assertNotNull ("It has edit cookie", obj.getCookie (EditCookie.class));
        assertNotNull ("It has open cookie", obj.getCookie (OpenCookie.class));
        assertNotNull ("It has editor cookie", obj.getCookie (EditorCookie.class));

        Object o = obj.getNodeDelegate ().getPreferredAction ();
        Class c = o == null ? Object.class : o.getClass ();
        
        assertEquals ("Default actions should be open on non-SFS", org.openide.actions.OpenAction.class, c);
    }
}
