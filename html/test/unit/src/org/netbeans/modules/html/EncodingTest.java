/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.html;

import junit.framework.*;
import junit.textui.TestRunner;
import org.openide.filesystems.*;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import java.util.Enumeration;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.InstanceDataObject;

import org.netbeans.junit.*;
import java.io.*;
import javax.swing.text.Document;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.util.Lookup;

public class EncodingTest extends NbTestCase {
    /** the fs to work on */
    private LocalFileSystem fs;
    
    public EncodingTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new TestSuite(EncodingTest.class));
        // In case it fails to shut itself down afterwards:
        System.exit(0);
    }
    
    /**/
    protected void setUp() throws Exception {
        System.setProperty ("org.openide.util.Lookup", "org.netbeans.modules.html.EncodingTest$Lkp");
        assertEquals ("Our lookup is installed", Lookup.getDefault ().getClass (), Lkp.class);

        File f = File.createTempFile (this.getName (), "");
        f.delete ();
        f.mkdirs ();
        
        fs = new LocalFileSystem ();
        fs.setRootDirectory(f);
        
        Repository.getDefault ().addFileSystem(fs);
    }
    
    protected void tearDown() throws Exception {
        Repository.getDefault ().removeFileSystem(fs);
        
        fs.getRootDirectory().deleteOnExit ();
    }
    
    /** Loads an empty file.
     */
    public void testLoadEmptyFile () throws Exception {
        checkEncoding (null, "empty.html", true);
    }
    
    /** Loades a file that does not specify an encoding.
     */
    public void testLoadOfNoEncoding () throws Exception {
        checkEncoding (null, "sample.html", true);
    }
    
    /** Loades a file that does not specify an encoding.
     */
    public void testLoadOfWrongEncoding () throws Exception {
        checkEncoding (null, "wrongencoding.html", false);
    }
    
    /** Test load of UTF-8 encoding.
     */
    public void testEncodingUTF8 () throws Exception {
        checkEncoding ("UTF-8", "UTF8.html", true);
    }
    /** Test load of UTF-8 encoding specified in ' ' instead of " "
     */
    public void testEncodingApostrof () throws Exception {
        checkEncoding ("UTF-8", "apostrof.html", true);
    }
    
    /** Test load of UTF-8 encoding specified in ' ' instead of " "
     * with a text that is followed with "
     */
    public void testEncodingApostrofWithQuote () throws Exception {
        checkEncoding ("UTF-8", "apostrofwithoutquote.html", true);
    }
    
    /** @param enc expected encoding
     *  @param res resource path
     *  @param withCmp should also document content be compared?
     */
    private void checkEncoding (String enc, String res, boolean withCmp) throws Exception {    
        InputStream is = getClass ().getResourceAsStream ("data/"+res);
        assertNotNull (res+" should exist", is);
        
        FileObject data = FileUtil.createData (fs.getRoot (), res);
        FileLock lock = data.lock();
        OutputStream os = data.getOutputStream (lock);
        FileUtil.copy (is, os);
        is.close ();
        os.close ();
        lock.releaseLock ();
        
        DataObject obj = DataObject.find (data);
        
        assertEquals ("Must be HtmlDataObject", HtmlDataObject.class, obj.getClass ());
        
        OpenCookie open = (OpenCookie)obj.getCookie (OpenCookie.class);
        assertNotNull("There is an open cookie", open);
        
        open.open ();
        
        EditorCookie ec = (EditorCookie)obj.getCookie (EditorCookie.class);
        assertNotNull ("There is an editor cookie", ec);
        
        Document doc = ec.openDocument();
        assertNotNull ("Need a document", doc);
        
        
        Reader r;
        if (enc == null) {
            r = new InputStreamReader (getClass ().getResourceAsStream ("data/"+res));
        } else {
            r = new InputStreamReader (getClass ().getResourceAsStream ("data/"+res), enc);
        }
           
        if (!withCmp)
            return;
        
        compareDoc (r, doc);
        r.close ();
        
        doc.insertString (0, "X", null);
        doc.remove (0, 1);
        
        SaveCookie sc = (SaveCookie)obj.getCookie(SaveCookie.class);
        assertNotNull ("Document is modified", sc);
        sc.save ();
       
        InputStream i1 = getClass ().getResourceAsStream ("data/"+res);
        InputStream i2 = obj.getPrimaryFile().getInputStream();
        compareStream (i1, i2);
        i2.close ();
        i1.close ();
        
    }
    
    /** Compares content of document and reader
     */
    private static void compareDoc (Reader r, Document doc) throws Exception {
        for (int i = 0; i < doc.getLength(); i++) {
            String ch = doc.getText (i, 1);
            assertEquals ("Really one char", 1, ch.length());
            
            char fromStream = (char)r.read ();
            if (fromStream != ch.charAt (0) && fromStream == (char)13 && ch.charAt (0) == (char)10) {
                // new line in document is always represented by 13, read next character
                fromStream = (char)r.read ();
            }
            
            
            assertEquals ("Stream and doc should be the same on index " + i, (int)fromStream, (int)ch.charAt (0));
        }
    }
    
    /** Compares content of two streams. 
     */
    private static void compareStream (InputStream i1, InputStream i2) throws Exception {
        for (int i = 0; true; i++) {
            int c1 = i1.read ();
            int c2 = i2.read ();

            assertEquals (i + "th bytes are different", c1, c2);
            
            if (c1 == -1) return;
        }
    }
    
    //
    // Our fake lookup
    //
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () throws Exception {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) throws Exception {
            super (ic);
            
            ic.add (new Pool ());
//            ic.add (new EM ());
        }
    }
    
    
    private static final class Pool extends DataLoaderPool {
        
        protected java.util.Enumeration loaders () {
            return new org.openide.util.enum.SingletonEnumeration (
                DataLoader.getLoader(HtmlLoader.class)
            );
        }
        
    } // end of Pool

    
}
