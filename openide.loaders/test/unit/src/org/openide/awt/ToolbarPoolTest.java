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

package org.openide.awt;

import java.io.*;
import junit.framework.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;


/** Mostly to test the correct behaviour of AWTTask.waitFinished.
 *
 * @author Jaroslav Tulach
 */
public class ToolbarPoolTest extends TestCase {
    org.openide.filesystems.FileObject toolbars;
    org.openide.loaders.DataFolder toolbarsFolder;
    
    public ToolbarPoolTest (java.lang.String testName) {
        super (testName);
    }
    
    public static Test suite () {
        TestSuite suite = new TestSuite (ToolbarPoolTest.class);
        return suite;
    }

    protected void setUp() throws java.lang.Exception {
        org.openide.filesystems.FileObject root = org.openide.filesystems.Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        toolbars = org.openide.filesystems.FileUtil.createFolder (root, "Toolbars");
        toolbarsFolder = org.openide.loaders.DataFolder.findFolder (toolbars);
        org.openide.filesystems.FileObject[] arr = toolbars.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        
        ToolbarPool tp = ToolbarPool.getDefault ();
        tp.waitFinished ();
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testGetConf () throws Exception {
        ToolbarPool tp = ToolbarPool.getDefault ();
        
        String conf = tp.getConfiguration ();
        assertEquals ("By default there is no config", "", conf);
        
    }
    

    
    public void testCreateConf () throws Exception {
        javax.swing.JLabel conf = new javax.swing.JLabel ();
        conf.setName ("testCreateConf");
        
        conf = (javax.swing.JLabel)writeInstance (toolbars, "conf1.ser", conf);
        
        ToolbarPool tp = ToolbarPool.getDefault ();
        
        tp.waitFinished ();
        String[] myConfs = tp.getConfigurations ();
        assertEquals ("One", 1, myConfs.length);
        assertEquals ("By default there is the one", "testCreateConf", myConfs[0]);
        
    }

    public void testCreateFolderTlbs () throws Exception {
        FileUtil.createFolder (toolbars, "tlb2");
        
        ToolbarPool tp = ToolbarPool.getDefault ();
        
        tp.waitFinished ();
        Toolbar[] myTlbs = tp.getToolbars ();
        assertEquals ("One", 1, myTlbs.length);
        assertEquals ("By default there is the one", "tlb2", myTlbs[0].getName ());
        
    }
    
    public void testWaitsForToolbars () throws Exception {
        FileObject tlb = FileUtil.createFolder (toolbars, "tlbx");
        DataFolder f = DataFolder.findFolder (tlb);
        InstanceDataObject.create (f, "test1", javax.swing.JLabel.class);
        
        ToolbarPool tp = ToolbarPool.getDefault ();
        
        tp.waitFinished ();
        Toolbar[] myTlbs = tp.getToolbars ();
        assertEquals ("One", 1, myTlbs.length);
        assertEquals ("By default there is the one", "tlbx", myTlbs[0].getName ());
        
        assertLabels ("One subcomponent", 1, myTlbs[0]);
        
        InstanceDataObject.create (f, "test2", javax.swing.JLabel.class);
        
        tp.waitFinished ();
        
        assertLabels ("Now there are two", 2, myTlbs[0]);
    }
    
    private static Object writeInstance (final FileObject folder, final String name, final Object inst) throws IOException {
        class W implements FileSystem.AtomicAction {
            public Object create;
            
            public void run () throws IOException {
                org.openide.filesystems.FileObject fo = FileUtil.createData (folder, name);
                org.openide.filesystems.FileLock lock = fo.lock ();
                ObjectOutputStream oos = new ObjectOutputStream (fo.getOutputStream (lock));
                oos.writeObject (inst);
                oos.close ();
                lock.releaseLock ();
                
                DataObject obj = DataObject.find (fo);
                org.openide.cookies.InstanceCookie ic =     
                    (org.openide.cookies.InstanceCookie)
                    obj.getCookie (org.openide.cookies.InstanceCookie.class);
                
                assertNotNull ("Cookie created", ic);
                try {
                    create = ic.instanceCreate ();
                    assertEquals ("The same instance class", inst.getClass(), create.getClass ());
                } catch (ClassNotFoundException ex) {
                    fail (ex.getMessage ());
                }
            }
        }
        W w = new W ();
        folder.getFileSystem ().runAtomicAction (w);
        return w.create;
    }
    
    private static void assertLabels (String msg, int cnt, java.awt.Component c) {
        int real = countLabels (c);
        assertEquals (msg, cnt, real);
    }
    
    private static int countLabels (java.awt.Component c) {
        if (c instanceof javax.swing.JLabel) return 1;
        if (! (c instanceof javax.swing.JComponent)) return 0;
        int cnt = 0;
        java.awt.Component[] arr = ((javax.swing.JComponent)c).getComponents ();
        for (int i = 0; i < arr.length; i++) {
            cnt += countLabels (arr[i]);
        }
        return cnt;
    }
}
