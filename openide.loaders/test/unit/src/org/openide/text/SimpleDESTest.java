/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.text;

import java.io.PrintStream;
import javax.swing.Action;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import org.openide.DialogDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.util.actions.SystemAction;

/** DefaultDataObject is supposed to have open operation that shows the text
 * editor or invokes a dialog with questions.
 *
 * @author  Jaroslav Tulach
 */
public final class SimpleDESTest extends NbTestCase {
    
    private FileSystem lfs;
    private DataObject obj;
    
    /** Creates a new instance of DefaultSettingsContextTest */
    public SimpleDESTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(SimpleDESTest.class));
        System.exit (0);
    }
    
    protected void setUp() throws java.lang.Exception {
        clearWorkDir ();
        
        System.setProperty("org.openide.util.Lookup", "org.openide.text.SimpleDESTest$Lkp");
        super.setUp();
        
        LocalFileSystem l = new LocalFileSystem ();
        l.setRootDirectory (getWorkDir ());
        lfs = l;
        
        FileObject fo = FileUtil.createData (lfs.getRoot (), "AA/" + getName () + ".test");
        assertNotNull("file not found", fo);
        obj = DataObject.find(fo);
        
        assertEquals ("The right class", obj.getClass (), SO.class);
    }
    
    protected void tearDown() throws java.lang.Exception {
        super.tearDown();
    }
    
    public void testHasEditorCookieForResonableContentOfFiles () throws Exception {
        doCookieCheck (true);
    }
    
    private void doCookieCheck (boolean hasEditCookie) throws Exception {
        EditorCookie c = tryToOpen (
            "Ahoj Jardo," +
            "how are you" +
            "\t\n\rBye"
        );
        assertNotNull (c);
        
        assertEquals (
            "Next questions results in the same cookie", 
            c, 
            obj.getCookie(EditorCookie.class)
        );
        assertEquals (
            "Print cookie is provided",
            c,
            obj.getCookie(org.openide.cookies.PrintCookie.class)
        );
        assertEquals (
            "CloseCookie as well",
            c,
            obj.getCookie(org.openide.cookies.CloseCookie.class)
        );
        
        if (hasEditCookie) {
            assertEquals (
                "EditCookie as well",
                c,
                obj.getCookie(org.openide.cookies.EditCookie.class)
            );
        } else {
            assertNull (
                "No EditCookie",
                obj.getCookie(org.openide.cookies.EditCookie.class)
            );
            
        }
        
        OpenCookie open = (OpenCookie)obj.getCookie (OpenCookie.class);
        open.open ();
        
        javax.swing.text.Document d = c.getDocument();
        assertNotNull (d);
        
        d.insertString(0, "Kuk", null);
        
        assertNotNull (
            "Now there is a save cookie", 
            obj.getCookie (org.openide.cookies.SaveCookie.class)
        );
    }
    
    public void testItIsPossibleToMaskEditCookie () throws Exception {
        doCookieCheck (false);
    }
    
    private EditorCookie tryToOpen (String content) throws Exception {
        FileObject fo = obj.getPrimaryFile();
        FileLock lock = fo.lock();
        PrintStream os = new PrintStream (fo.getOutputStream(lock));
        os.print (content);
        os.close ();
        lock.releaseLock();
        
        return (EditorCookie)obj.getCookie (EditorCookie.class);
    }
    
    //
    // Our fake lookup
    //
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new DLP ());
        }
    }
    
    private static final class SL extends org.openide.loaders.UniFileLoader {
        public SL () {
            super (SO.class.getName ());
            getExtensions().addExtension("test");
        }
        protected org.openide.loaders.MultiDataObject createMultiObject(FileObject primaryFile) throws org.openide.loaders.DataObjectExistsException, java.io.IOException {
            return new SO (primaryFile);
        }
    } // end of SL
    
    private static final class SO extends org.openide.loaders.MultiDataObject implements org.openide.nodes.CookieSet.Factory {
        private org.openide.nodes.Node.Cookie cookie = (org.openide.nodes.Node.Cookie)DataEditorSupport.create(this, getPrimaryEntry(), getCookieSet ());
        
        
        public SO (FileObject fo) throws org.openide.loaders.DataObjectExistsException {
            super (fo, (SL)SL.getLoader(SL.class));
            
            if (fo.getNameExt().indexOf ("MaskEdit") == -1) {
                getCookieSet ().add (cookie);
            } else {
                getCookieSet ().add (new Class[] { 
                    OpenCookie.class, 
                    org.openide.cookies.CloseCookie.class, EditorCookie.class, 
                    org.openide.cookies.PrintCookie.class
                }, this); 
            }
        }
        
        
        public org.openide.nodes.Node.Cookie createCookie (Class c) {
            return cookie;
        }
    } // end of SO

    private static final class DLP extends org.openide.loaders.DataLoaderPool {
        protected java.util.Enumeration loaders() {
            return java.util.Collections.enumeration(
                java.util.Collections.singleton(
                    SL.getLoader (SL.class)
                )
            );
        }
    } // end of DataLoaderPool
}
