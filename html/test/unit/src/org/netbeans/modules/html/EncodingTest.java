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

package org.netbeans.modules.html;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import javax.swing.text.Document;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

public class EncodingTest extends NbTestCase {
    /** the fs to work on */
    private LocalFileSystem fs;
    
    public EncodingTest(String name) {
        super(name);
    }
    
    /**/
    protected void setUp() throws Exception {
        Utils.setUp();

        File f = File.createTempFile (this.getName (), "");
        f.delete ();
        f.mkdirs ();
        
        fs = new LocalFileSystem ();
        fs.setRootDirectory(f);
        
        // to help the loader to recognize our files
        FileUtil.setMIMEType("html", "text/html");
        
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
    /*package*/ static void compareStream (InputStream i1, InputStream i2) throws Exception {
        for (int i = 0; true; i++) {
            int c1 = i1.read ();
            int c2 = i2.read ();

            assertEquals (i + "th bytes are different", c1, c2);
            
            if (c1 == -1) return;
        }
    }
    
}
