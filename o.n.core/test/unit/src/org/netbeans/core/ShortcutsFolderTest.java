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
import org.netbeans.junit.*;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import junit.textui.TestRunner;

/**
 * Tests shortcuts folder to ensure it handles wildcard keystrokes correctly. 
 */
public class ShortcutsFolderTest extends NbTestCase {
    static {
        // register lookup
        System.setProperty("org.openide.util.Lookup", "org.netbeans.core.ShortcutsFolderTest$LKP");
        Main.initializeURLFactory ();
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public ShortcutsFolderTest(String s) {
        super(s);
    }
    
    ShortcutsFolder sf = null;
    FileSystem fs = null;
    FileObject fld = null;
    FileObject fo = null;
    
    static Repository repository = null;
    protected void setUp () {
        fs = createTestingFilesystem();
        fo = getFolderForShortcuts(fs);
        sf = createShortcutsFolder(fs);
        sf.waitShortcutsFinished();
    }
    
    protected void tearDown() {
        try {
            FileLock lock = fo.lock();
            fo.delete(lock);
            lock.releaseLock();
        } catch (Exception ioe) {
        }
        
        ShortcutsFolder.shortcutsFolder = null;
        sf = null;
        fs = null;
        fld = null;
        fo = null;
    }

    public void testHyphenation (String s) {
        System.out.println("testHyphenation");
        HashSet set = new HashSet();
        char[] c = new String("ABCD").toCharArray();
        ShortcutsFolder.createHyphenatedPermutation (c, set, "-F5");
        
        assertTrue (set.contains("A-B-C-D-F5"));
    }
   
    public void testPermutations () {
        System.out.println("testPermutations");
        HashSet set = new HashSet();
        
        ShortcutsFolder.getAllPossibleOrderings("BANG", "-F5", set);
        String[] permutations = new String[] {
            "BNAG", "BNGA", "BGNA", "BAGN", "BGAN", 
            "ANBG", "ABGN", "AGBN", "ANGB", "ABNG", "AGNB",
            "NBGA", "NGBA", "NABG", "NGAB", "NBAG", "NAGB",
            "GNAB", "GBAN", "GBNA", "GNBA", "GANB", "GABN",
        };
        
        for (int i=0; i < permutations.length; i++) {
            assertTrue ("Permutation of BANG not generated: " + permutations[i], 
                set.contains(permutations[i] + "-F5"));
        }
    }
    
    public void testPermutationsIncludeHyphenatedVariants() {
        System.out.println("testPermutationsIncludeHyphenatedVariants");
        HashSet set = new HashSet();
        
        ShortcutsFolder.getAllPossibleOrderings("BANG", "-F5", set);
        String[] permutations = new String[] {
            "B-N-A-G", "B-N-G-A", "B-G-N-A", "B-A-G-N", "B-G-A-N", 
            "A-N-B-G", "A-B-G-N", "A-G-B-N", "A-N-G-B", "A-B-N-G", "A-G-N-B",
            "N-B-G-A", "N-G-B-A", "N-A-B-G", "N-G-A-B", "N-B-A-G", "N-A-G-B",
            "G-N-A-B", "G-B-A-N", "G-B-N-A", "G-N-B-A", "G-A-N-B", "G-A-B-N",
        };
        
        for (int i=0; i < permutations.length; i++) {
            assertTrue ("Permutation of BANG not generated: " + permutations[i], 
                set.contains(permutations[i] + "-F5"));
        }
    }
    
    public void testPermutationsContainConvertedWildcard () {
        doPermutationsContainConvertedWildcard (true);
        doPermutationsContainConvertedWildcard (false);
    }
    
    public void doPermutationsContainConvertedWildcard (boolean mac) {
        System.out.println("testPermutationsContainConvertedWildcard mac=" + mac);
        String targetChar = mac
            ? "M" : "C";
        
        String[] s = ShortcutsFolder.getPermutations("DA-F5", mac);
        HashSet set = new HashSet (Arrays.asList(s));
        set.add ("DA-F5"); //Permutations will not contain the passed value
        
        String[] permutations = new String[] {
            targetChar+"A-F5", "A" + targetChar + "-F5",
            targetChar+"-A-F5", "A-" + targetChar + "-F5",
            "AD-F5", "A-D-F5", "D-A-F5"
        };
        
        for (int i=0; i < permutations.length; i++) {
            assertTrue ("Permutation of DA-F5 not generated:" 
                + permutations[i] + "-(generated:" + set + ")",
                set.contains(permutations[i]));
        }
    }
    
    public void testPermutationsIncludeWildcardIfSpecifiedKeyIsToolkitAccelerator () {
        doTestPermutationsIncludeWildcardIfSpecifiedKeyIsToolkitAccelerator(true);
        doTestPermutationsIncludeWildcardIfSpecifiedKeyIsToolkitAccelerator(false);
    }

    public void doTestPermutationsIncludeWildcardIfSpecifiedKeyIsToolkitAccelerator (boolean macintosh) {
        System.out.println("testPermutationsIncludeWildcardIfSpecifiedKeyIsToolkitAccelerator - mac=" + macintosh);
        String targetChar = macintosh
            ? "M" : "C";
        
        String[] s = ShortcutsFolder.getPermutations(targetChar + "A-F5", macintosh);
        
        String[] permutations = new String[] {
            "A" + targetChar + "-F5", "A-" + targetChar + "-F5",
            "DA-F5", "D-A-F5"
        };
        
        HashSet set = new HashSet (Arrays.asList(s));
        set.add (targetChar + "A-F5"); //Permutations will not contain the passed value
        
        for (int i=0; i < permutations.length; i++) {
            assertTrue ("Permutation of " + targetChar + "A-F5 not generated:" 
                + permutations[i] + "-(generated:" + set + ")", 
                set.contains(permutations[i].intern()));
        }
    } 
    
    public void testPermutationsContainConvertedAltWildcard () {
        doTestPermutationsContainConvertedAltWildcard (true);
        doTestPermutationsContainConvertedAltWildcard (false);
    }
    
    public void doTestPermutationsContainConvertedAltWildcard (boolean macintosh) {
        System.out.println("testPermutationsContainConvertedAltWildcard mac = " + macintosh);
        String cmdChar = macintosh
            ? "M" : "C";        
        
        String altKey = macintosh ?
            "C" : "A"; //NOI18N        
        
        String[] s = ShortcutsFolder.getPermutations("DO-F5", macintosh);
        HashSet set = new HashSet (Arrays.asList(s));
        set.add ("DO-F5"); //Permutations will not contain the passed value
        
        String[] permutations = new String[] {
            "OD-F5", 
            "O" + cmdChar + "-F5", 
            "DO-F5", 
            "D-O-F5",
            "O-D-F5", 
            altKey + "-D-F5", 
            altKey + "-" + cmdChar + "-F5", 
            altKey + "D-F5",
        };
        
        for (int i=0; i < permutations.length; i++) {
            assertTrue ("Permutation of D"+ altKey + "-F5 not generated:" 
                + permutations[i] + "; (generated:" + set + ")",
                set.contains(permutations[i]));
        }
    } 

    public void testDualWildcardPermutations() {
        doTestDualWildcardPermutations(true);
        doTestDualWildcardPermutations(false);
    }   
    
    public void doTestDualWildcardPermutations(boolean macintosh) {
        System.out.println("testDualWildcardPermutations mac=" + macintosh);
        String cmdChar = macintosh
            ? "M" : "C";        
        
        String altKey = macintosh ?
            "C" : "A"; //NOI18N        
        
        String[] s = ShortcutsFolder.getPermutations("OD-F5", macintosh);
        HashSet set = new HashSet (Arrays.asList(s));
        set.add ("OD-F5"); //Permutations will not contain the passed value
        
        String[] permutations = new String[] {
            "OD-F5", "O" + cmdChar + "-F5", "DO-F5", "D-O-F5",
            "O-D-F5", altKey + "-D-F5", altKey + "-" + cmdChar + "-F5", altKey + "D-F5",
            altKey + cmdChar + "-F5"
        };
        
        for (int i=0; i < permutations.length; i++) {
            assertTrue ("Permutation of OD-F5 not generated:" 
                + permutations[i] + "; (generated:" + set + ")",
                set.contains(permutations[i]));
        }
    }
    
    public void testOPermutationOfAlt () throws Exception {
        System.out.println("testOPermutationOfAlt");
//        FileSystem fs = createTestingFilesystem();
//        FileObject fo = getFolderForShortcuts(fs);
        
//        assertEquals (lastDir, repository.getDefaultFileSystem().getRoot().getPath());
        
        FileObject data1 = fo.createData("OD-F6.instance");
        assertNotNull(data1);
        data1.setAttribute("instanceClass", "org.netbeans.core.ShortcutsFolderTest$TestAction");
        
        FileObject data2 = fo.createData("OS-F6.instance");
        assertNotNull(data2);
        data2.setAttribute("instanceClass", "org.netbeans.core.ShortcutsFolderTest$TestAction");

        sf.refreshGlobalMap();
        
        DataObject ob = DataObject.find (data1);
        assertNotNull("Data object not found: " + data1.getPath(), ob);
        
        InstanceCookie ck = (InstanceCookie) ob.getCookie(InstanceCookie.class);
        Object obj = ck.instanceCreate();
        
        assertTrue ("InstanceCookie was not an instanceof TestAction - " + obj, obj instanceof TestAction);
        
        int mask = System.getProperty("mrj.version") == null ? KeyEvent.ALT_MASK :
            KeyEvent.CTRL_MASK;
        
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F6, mask | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        Action action = (Action) obj;
        
        ShortcutsFolder.applyChanges(Arrays.asList(new Object[] {new ShortcutsFolder.ChangeRequest (stroke, action, false)}));
        
        ShortcutsFolder.refreshGlobalMap();

        FileObject now = fo.getFileObject ("OD-F6.instance");
        //XXX WTF??
        assertNull ("File object should be deleted - but is " + (now == null ? " null " : now.getPath()), now);
    }  

  
    private FileSystem createTestingFilesystem () {
        FileSystem result = Repository.getDefault ().getDefaultFileSystem ();
        FileObject root = result.getRoot ();
        try {
            FileObject[] arr = root.getChildren ();
            for (int i = 0; i < arr.length; i++) {
                arr[i].delete ();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail (e.getMessage());
        }
        return result;
    }
    
    private FileObject getFolderForShortcuts(FileSystem fs) {
        FileObject result = null;
        try {
            result = fs.getRoot().getFileObject("Shortcuts");
            if (result == null) {
                result = fs.getRoot().createFolder("Shortcuts");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return result;
    }
    
    private String folderName = null;
    private ShortcutsFolder createShortcutsFolder(FileSystem fs) {
        try {
            DataObject dob = DataObject.find(getFolderForShortcuts(fs));
            ShortcutsFolder result = new ShortcutsFolder ((DataFolder)dob);
            ShortcutsFolder.shortcutsFolder = result;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail ("Exception creating shortcuts folder on " + fs);
            return null;
        }
    }
    
    public void testShortcutsFolderDeletesShortcutWhenNameIsMisordered() throws Exception {
        FileSystem fs = createTestingFilesystem();
        FileObject fo = getFolderForShortcuts(fs);
        
        FileObject data1 = fo.createData("AD-F5.instance");
        assertNotNull(data1);
        data1.setAttribute("instanceClass", "org.netbeans.core.ShortcutsFolderTest$TestAction");
        
        FileObject data2 = fo.createData("AS-F5.instance");
        assertNotNull(data2);
        data2.setAttribute("instanceClass", "org.netbeans.core.ShortcutsFolderTest$TestAction");
        
        ShortcutsFolder sf = createShortcutsFolder(fs);
        
        sf.waitShortcutsFinished();
        
        DataObject ob = DataObject.find (data1);
        assertNotNull("Data object not found: " + data1.getPath(), ob);
        
        InstanceCookie ck = (InstanceCookie) ob.getCookie(InstanceCookie.class);
        Object obj = ck.instanceCreate();
        
        assertTrue ("InstanceCookie was not an instanceof TestAction - " + obj, obj instanceof TestAction);
        
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        Action action = (Action) obj;
        
        ShortcutsFolder.applyChanges(Arrays.asList(new Object[] {new ShortcutsFolder.ChangeRequest (stroke, action, false)}));
        
        ShortcutsFolder.refreshGlobalMap();
        FileObject now = fo.getFileObject ("AD-F5.instance");
        assertNull ("File object should be deleted - ", now);
        
        assertNotNull ("File should not have been deleted: " + fo, fo.getFileObject ("AS-F5.instance"));
    }
    
    public void testApplyChangeToFactoryActionIssue49597 () throws Exception {
        FileSystem fs = createTestingFilesystem();
        FileObject shortcuts = getFolderForShortcuts (fs);
        FileObject inst = org.openide.filesystems.FileUtil.createData (fs.getRoot (), "/Actions/Tools/TestAction.instance");
        TestAction action = new TestAction ();
        inst.setAttribute ("instanceCreate", action);
        
        WeakReference ref = new WeakReference (inst);
        inst = null;
        assertGC ("File can disappear", ref);
        
        ShortcutsFolder sf = createShortcutsFolder(fs);
        sf.waitShortcutsFinished();

        javax.swing.text.Keymap map = (javax.swing.text.Keymap)Lookup.getDefault ().lookup (javax.swing.text.Keymap.class);
        
        assertEquals ("Nothing registered", java.util.Collections.EMPTY_LIST, java.util.Arrays.asList (map.getBoundActions ()));
        
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F9, KeyEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        ShortcutsFolder.applyChanges(Arrays.asList(new Object[] {new ShortcutsFolder.ChangeRequest (stroke, action, true)}));
        sf.waitShortcutsFinished ();

        FileObject[] arr = shortcuts.getChildren ();
        assertEquals ("One element is there", 1, arr.length);
        org.openide.loaders.DataObject obj = org.openide.loaders.DataObject.find (arr[0]);
        assertEquals ("It is DataShadow", org.openide.loaders.DataShadow.class, obj.getClass ());
        
        Object a = map.getAction (stroke);
        assertNotNull ("There is an action", a);
        assertEquals ("It is test action", TestAction.class, a.getClass ());
    }
    
    public void testShortcutsForDifferentFilesThanInstanceOrShadows () throws Exception {
        FileSystem fs = createTestingFilesystem();
        FileObject shortcuts = getFolderForShortcuts (fs);
        FileObject inst = org.openide.filesystems.FileUtil.createData (fs.getRoot (), "/Shortcuts/C-F11.xml");

        java.io.PrintStream ps = new java.io.PrintStream (inst.getOutputStream (inst.lock ()));
        ps.println ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ps.println ("<project default=\"run\" name=\"Build everything.\">");
        ps.println ("<target name=\"run\">");
        ps.println ("<ant antfile=\"SampleProject.xml\" inheritall=\"false\" target=\"all\"/>");
        ps.println ("</target>");
        ps.println ("</project>");
        ps.close();
        DataObject obj = DataObject.find (inst);
        assertEquals ("XML Data object", org.openide.loaders.XMLDataObject.class, obj.getClass());
        org.openide.cookies.InstanceCookie ic = (org.openide.cookies.InstanceCookie)obj.getCookie(org.openide.cookies.InstanceCookie.class);
        assertNotNull ("Has cookie", ic);

        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F11, KeyEvent.CTRL_MASK);
        
        ShortcutsFolder sf = createShortcutsFolder(fs);
        sf.waitShortcutsFinished();

        javax.swing.text.Keymap map = (javax.swing.text.Keymap)Lookup.getDefault ().lookup (javax.swing.text.Keymap.class);
        
        Action action = map.getAction(stroke);
        assertNotNull ("There is some action", action);
        
        ShortcutsFolder.applyChanges(Arrays.asList(new Object[] {new ShortcutsFolder.ChangeRequest (stroke, action, false)}));
        sf.waitShortcutsFinished ();
        
        action = map.getAction(stroke);
        assertNull ("Action removed", action);
        

    }
    
    public static class TestAction extends AbstractAction {
        public void actionPerformed (ActionEvent ae) {}
    }
    
    public static class LKP extends org.openide.util.lookup.AbstractLookup implements org.openide.loaders.Environment.Provider {
        public LKP () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private LKP (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new NbKeymap ());
            //ic.add (new EM ());
            ic.add (this);
        }
        
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
    
    public static class EM extends ErrorManager {
        public Throwable attachAnnotations (Throwable t, Annotation[] arr) {
            return t;
        }

        public void log (int i, String s) {
            System.err.println(s);
        }
        
        public Annotation[] findAnnotations (Throwable t) {
            return new Annotation[0];
        }

        public Throwable annotate (
            Throwable t, int severity,
            String message, String localizedMessage,
            Throwable stackTrace, java.util.Date date
        ) {
            System.err.println(message);
            t.printStackTrace();
            return t;
        }

        public void notify (int severity, Throwable t) {
            t.printStackTrace();
        }

        public ErrorManager getInstance(String name) {
            return this;
        }
    }
    
}
