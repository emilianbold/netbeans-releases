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
package org.netbeans.core;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.*;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

public class ShortcutsFolderTest extends NbTestCase {
    private ErrorManager err;
    private Keymap keymap;
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public ShortcutsFolderTest(String s) {
        super(s);
    }
    
    protected Level logLevel() {
        return Level.ALL;
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(ENV.class);

        Main.initializeURLFactory ();
        keymap = Lookup.getDefault().lookup(Keymap.class);
        
        assertNotNull("There is a keymap", keymap);
        ShortcutsFolder.initShortcuts ();
        
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
    }
    
    public void testApplyChangeToFactoryActionIssue49597 () throws Exception {
        final FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        final FileObject shortcuts = fs.getRoot ().getFileObject ("Shortcuts");
        FileObject inst = FileUtil.createData (fs.getRoot (), "/Actions/Tools/TestAction.instance");
        TestAction action = new TestAction ();
        inst.setAttribute ("instanceCreate", action);
        
        WeakReference ref = new WeakReference (inst);
        inst = null;
        assertGC ("File can disappear", ref);

//        ShortcutsFolder.waitFinished ();

        assertEquals ("Nothing registered", Collections.EMPTY_LIST, Arrays.asList (keymap.getBoundActions ()));
        
        final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F9, KeyEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

        class R implements FileSystem.AtomicAction {
            FileObject inst2;
            
            public void run() throws IOException {
                inst2 = FileUtil.createData (fs.getRoot (), "/Shortcuts/CA-F9.shadow");
                inst2.setAttribute ("originalFile", "/Actions/Tools/TestAction.instance");
            }
        }
        R run = new R();
        fs.runAtomicAction(run);

        ShortcutsFolder.waitFinished ();
        err.log("ShortcutsFolder.waitFinished");

        FileObject[] arr = shortcuts.getChildren ();
        err.log("children are here");
        
        assertEquals ("One element is there", 1, arr.length);
        org.openide.loaders.DataObject obj = org.openide.loaders.DataObject.find (arr[0]);
        err.log("Object is here" + obj);
        
        assertEquals ("It is DataShadow", org.openide.loaders.DataShadow.class, obj.getClass ());

        Object a = keymap.getAction (stroke);
        assertNotNull ("There is an action", a);
        assertEquals ("It is test action", TestAction.class, a.getClass ());
    }
    
    public void testShortcutsForDifferentFilesThanInstanceOrShadows () throws Exception {
        FileSystem fs = Repository.getDefault ().getDefaultFileSystem ();
        FileObject shortcuts = fs.getRoot ().getFileObject ("Shortcuts");
        FileObject inst = FileUtil.createData (fs.getRoot (), "/Shortcuts/C-F11.xml");

        FileLock lock = inst.lock ();
        java.io.PrintStream ps = new java.io.PrintStream (inst.getOutputStream (lock));
        ps.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ps.println ("<project default=\"run\" name=\"Build everything.\">");
        ps.println ("<target name=\"run\">");
        ps.println ("<ant antfile=\"SampleProject.xml\" inheritall=\"false\" target=\"all\"/>");
        ps.println ("</target>");
        ps.println ("</project>");
        ps.close();
        lock.releaseLock ();
        
        DataObject obj = DataObject.find (inst);
        assertEquals ("XML Data object", org.openide.loaders.XMLDataObject.class, obj.getClass());
        org.openide.cookies.InstanceCookie ic = (org.openide.cookies.InstanceCookie)obj.getCookie(org.openide.cookies.InstanceCookie.class);
        assertNotNull ("Has cookie", ic);

        final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F11, KeyEvent.CTRL_MASK);
        
        ShortcutsFolder.waitFinished ();

        Action action = keymap.getAction(stroke);
        if (action == null) {
            fail("There should be some action for " + stroke + " in:\n" + keymap);
        }
        
        inst.delete ();
        ShortcutsFolder.waitFinished ();
        action = keymap.getAction (stroke);
        assertNull ("Action removed", action);
    }
    
    public static class TestAction extends AbstractAction {
        public void actionPerformed (ActionEvent ae) {}
    }
    
    public static class ENV extends Object implements org.openide.loaders.Environment.Provider {
        public Lookup getEnvironment(DataObject obj) {
            if (obj instanceof org.openide.loaders.XMLDataObject) {
                try {
                    org.w3c.dom.Document doc = ((org.openide.loaders.XMLDataObject)obj).getDocument();
                    if (doc.getDocumentElement().getNodeName().equals ("project")) {
                        return org.openide.util.lookup.Lookups.singleton (
                            new org.openide.loaders.InstanceSupport.Instance (
                                new TestAction ()
                            )
                        );
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    fail ("No exception: " + ex.getMessage());
                }
            }
            return org.openide.util.Lookup.EMPTY;
        }
    }
    
}
