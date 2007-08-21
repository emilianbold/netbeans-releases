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

package org.openide.awt;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;

/** Mostly to test the correct behaviour of AWTTask.waitFinished.
 *
 * @author Jaroslav Tulach
 */
public class ToolbarPoolTest extends NbTestCase {
    FileObject toolbars;
    DataFolder toolbarsFolder;
    
    public ToolbarPoolTest (String testName) {
        super (testName);
    }

    @Override
    protected int timeOut() {
        return 30000;
    }
    
    protected Level logLevel() {
        return Level.FINE;
    }
    
    protected void setUp() throws Exception {
        FileObject root = Repository.getDefault ().getDefaultFileSystem ().getRoot ();
        toolbars = FileUtil.createFolder (root, "Toolbars");
        toolbarsFolder = DataFolder.findFolder (toolbars);
        FileObject[] arr = toolbars.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete ();
        }
        
        ToolbarPool tp = ToolbarPool.getDefault ();
        tp.waitFinished ();
    }

    public void testGetConf () throws Exception {
        ToolbarPool tp = ToolbarPool.getDefault ();
        
        String conf = tp.getConfiguration ();
        assertEquals ("By default there is no config", "", conf);
        
    }
    

    
    public void testCreateConf () throws Exception {
        JLabel conf = new JLabel ();
        conf.setName ("testCreateConf");
        
        conf = (JLabel)writeInstance (toolbars, "conf1.ser", conf);
        
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
        InstanceDataObject.create (f, "test1", JLabel.class);
        
        ToolbarPool tp = ToolbarPool.getDefault ();
        
        tp.waitFinished ();
        Toolbar[] myTlbs = tp.getToolbars ();
        assertEquals ("One", 1, myTlbs.length);
        assertEquals ("By default there is the one", "tlbx", myTlbs[0].getName ());
        
        assertLabels ("One subcomponent", 1, myTlbs[0]);
        
        InstanceDataObject.create (f, "test2", JLabel.class);
        
        tp.waitFinished ();
        
        assertLabels ("Now there are two", 2, myTlbs[0]);
    }
    
    private static Object writeInstance (final FileObject folder, final String name, final Object inst) throws IOException {
        class W implements FileSystem.AtomicAction {
            public Object create;
            
            public void run () throws IOException {
                FileObject fo = FileUtil.createData (folder, name);
                FileLock lock = fo.lock ();
                ObjectOutputStream oos = new ObjectOutputStream (fo.getOutputStream (lock));
                oos.writeObject (inst);
                oos.close ();
                lock.releaseLock ();
                
                DataObject obj = DataObject.find (fo);
                InstanceCookie ic =     
                    (InstanceCookie)
                    obj.getCookie (InstanceCookie.class);
                
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
    
    private static void assertLabels (String msg, int cnt, Component c) {
        int real = countLabels (c);
        assertEquals (msg, cnt, real);
    }
    
    private static int countLabels (Component c) {
        if (c instanceof JLabel) return 1;
        if (! (c instanceof JComponent)) return 0;
        int cnt = 0;
        Component[] arr = ((JComponent)c).getComponents ();
        for (int i = 0; i < arr.length; i++) {
            cnt += countLabels (arr[i]);
        }
        return cnt;
    }
}
