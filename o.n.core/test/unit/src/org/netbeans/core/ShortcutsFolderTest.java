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
package org.netbeans.core;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import junit.framework.*;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.junit.*;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import junit.textui.TestRunner;

public class ShortcutsFolderTest extends LoggingTestCaseHid {
    private ErrorManager err;
    private Keymap keymap;
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public ShortcutsFolderTest(String s) {
        super(s);
    }
    
    protected void setUp() throws Exception {
        registerIntoLookup(new ENV());

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
        assertNotNull ("There is some action", action);
        
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
