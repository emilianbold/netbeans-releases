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

package org.netbeans.core.lookup;

import org.netbeans.junit.*;
import junit.textui.TestRunner;

import java.io.File;
import org.netbeans.core.modules.Module;
import org.netbeans.core.modules.ModuleManager;
import org.netbeans.core.NbTopManager;
import org.netbeans.core.modules.ModuleHistory;
import org.openide.util.Lookup;
import javax.swing.Action;
import java.util.Iterator;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Mutex;
import org.openide.cookies.InstanceCookie;
import org.openide.util.MutexException;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;
import java.io.IOException;
import java.util.Properties;
import org.openide.filesystems.FileUtil;

/** A test.
 * @author Jesse Glick
 * @see InstanceDataObjectModuleTestHid
 */
public class InstanceDataObjectModuleTest8 extends InstanceDataObjectModuleTestHid
implements org.openide.filesystems.FileChangeListener {
    private boolean instanceSaved;

    public InstanceDataObjectModuleTest8(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        // Turn on verbose logging while developing tests:
        //System.setProperty("org.netbeans.core.modules", "0");
        TestRunner.run(new NbTestSuite(InstanceDataObjectModuleTest8.class));
    }

    protected void setUp() throws Exception {
        // Use MemoryFileSystem:
        Properties p = System.getProperties();
        p.remove("system.dir");
        System.setProperties(p);
        Repository.getDefault ().getDefaultFileSystem ().addFileChangeListener (this);
        super.setUp();
    }
     
    protected void tearDown () throws java.lang.Exception {
        Repository.getDefault ().getDefaultFileSystem ().removeFileChangeListener (this);
        super.tearDown ();
    }
    
    /** Currently fails.
     * Same as #7, but reloading is done quickly (one write mutex, no pause).
     */
    public void testFixedSettingsChangeInstanceAfterFastReload() throws Exception {
        twiddle(m2, TWIDDLE_ENABLE);
        DataObject obj1;
        try {
            obj1 = findIt("Services/Misc/inst-8.settings");
            assertEquals("No saved state for inst-8.settings", null, FileUtil.toFile(obj1.getPrimaryFile()));
            InstanceCookie inst1 = (InstanceCookie)obj1.getCookie(InstanceCookie.class);
            assertNotNull("Had an instance", inst1);
            Action a1 = (Action)inst1.instanceCreate();
            assertEquals("Correct action class", "test2.SomeAction", a1.getClass().getName());
            assertTrue("Old version of action", a1.isEnabled());
            // Make some change which should cause it to be written to disk:
            synchronized (this) {
                a1.setEnabled(false);
                // Cf. InstanceDataObject.SettingsInstance.SAVE_DELAY = 2000:
                wait (60000);
                assertTrue ("Really was saved", instanceSaved);
            }
            /*
            File saved = new File(new File(new File(systemDir, "Services"), "Misc"), "inst-8.settings");
            assertTrue("Wrote to disk: " + saved, saved.isFile());
             */
            /*
            File saved = FileUtil.toFile(obj1.getPrimaryFile());
            assertNotNull("Wrote to disk; expecting: " + new File(new File(new File(systemDir, "Services"), "Misc"), "inst-8.settings"),
                saved);
             */
            twiddle(m2, TWIDDLE_RELOAD);
            // Make sure there is time for changes to take effect:
            Thread.sleep(2000);
            DataObject obj2 = findIt("Services/Misc/inst-8.settings");
            assertSameDataObject ("same data object", obj1, obj2);
            InstanceCookie inst2 = (InstanceCookie)obj2.getCookie(InstanceCookie.class);
            assertNotNull("Had an instance", inst2);
            assertTrue("InstanceCookie changed", inst1 != inst2);
            Action a2 = (Action)inst2.instanceCreate();
            assertTrue("Action changed", a1 != a2);
            assertTrue("Correct action", "SomeAction".equals(a2.getValue(Action.NAME)));
            assertTrue("New version of action", !a2.isEnabled());
        } finally {
            twiddle(m2, TWIDDLE_DISABLE);
        }
        // Now make sure it has no cookie.
        Thread.sleep(1000);
        DataObject obj3 = findIt("Services/Misc/inst-8.settings");
        assertSameDataObject ("same data object2", obj1, obj3);
        InstanceCookie inst3 = (InstanceCookie)obj3.getCookie(InstanceCookie.class);
        assertNull("Had instance", inst3);
    }
    
    
    public void fileAttributeChanged (org.openide.filesystems.FileAttributeEvent fe) {
    }
    
    public synchronized void fileChanged (org.openide.filesystems.FileEvent fe) {
        if ("inst-8.settings".equals (fe.getFile ().getNameExt ())) {
            instanceSaved = true;
            notifyAll ();
        }
    }
    
    public void fileDataCreated (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileDeleted (org.openide.filesystems.FileEvent fe) {
        if ("inst-8.settings".equals (fe.getFile ().getNameExt ())) {
            FileObject isThere = Repository.getDefault ().getDefaultFileSystem ().findResource (fe.getFile ().getPath ());
            
            fail ("File " + fe.getFile () + " should not be deleted as this will discard the data object. Moreover it is expected that similar file is still there: " + isThere);
        }
    }
    
    public void fileFolderCreated (org.openide.filesystems.FileEvent fe) {
    }
    
    public void fileRenamed (org.openide.filesystems.FileRenameEvent fe) {
    }
}
