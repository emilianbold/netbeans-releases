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

package org.netbeans.modules.xml.multiview.test;

import java.io.File;
import java.io.IOException;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.AssertionFailedErrorException;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;

import org.netbeans.modules.xml.multiview.test.util.Helper;
import org.netbeans.modules.xml.multiview.test.bookmodel.*;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;

/**
 *
 * @author Milan Kuchtiak
 */
public class XmlMultiViewEditorTest extends NbTestCase {
    private DataLoaderPool pool;
    private DataLoader loader;
    private static BookDataObject bookDO;
    
    public XmlMultiViewEditorTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(XmlMultiViewEditorTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        pool = DataLoaderPool.getDefault ();
        assertNotNull (pool);
        loader = DataLoader.getLoader(BookDataLoader.class);
    }
   

    /** Tet if sample.book was correctly recognized by BookDataLoader and
     * if sample.book was open in editor (XML view) 
     */
    public void testBookDataObject() throws IOException {
        //assertTrue(java.util.Arrays.asList(pool.toArray()).contains(loader));
        File f = Helper.getBookFile(getDataDir());
        FileObject fo = FileUtil.toFileObject(f);
        assertNotNull(fo);
        
        doSetPreferredLoader (fo, loader);
        DataObject dObj = DataObject.find (fo);
        assertEquals (BookDataObject.class, dObj.getClass ());
        
        bookDO = (BookDataObject)dObj;
        ((EditCookie)bookDO.getCookie(EditCookie.class)).edit();
        
        // wait to see the changes in Design view
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex){}
        
        XmlMultiViewEditorSupport editor  = (XmlMultiViewEditorSupport)bookDO.getCookie(EditorCookie.class);
        javax.swing.text.Document doc = editor.getDocument();
        assertTrue("The document is empty :",doc.getLength()>0);
    }
    
    public void testChangeModel() throws IOException {
        assertNotNull("Book DataObject not found",bookDO);
        
        try {
            Book book = bookDO.getBook();
            book.setAttributeValue("chapter", 0, "length", "110");
            bookDO.modelChanged();
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to change book model",ex);
        }
        // wait to see the changes
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex){}
        
        // test if data object was modified
        SaveCookie cookie = (SaveCookie)bookDO.getCookie(SaveCookie.class);
        assertNotNull("Data Object Not Modified",cookie);
        cookie.save();
        
        // test to golden file
        File original = Helper.getBookFile(getDataDir());
        String golden = "ChangedChapterLength.pass";
        assertFile(original, getGoldenFile(golden), getWorkDir());
    }
    
    public void testChangeModelInDesignView() throws IOException {
        assertNotNull("Book DataObject not found",bookDO);
        try {
            bookDO.showElement(bookDO.getBook().getChapter()[1]);
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to open Chapter section",ex);
        }
        // wait for saving file
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex){}
        
        try {
            javax.swing.JTextField titleTF = Helper.getChapterTitleTF(bookDO,bookDO.getBook().getChapter()[1]);
            titleTF.requestFocus();
            titleTF.getDocument().remove(0, titleTF.getDocument().getLength());
            Thread.sleep(300);
            titleTF.getDocument().insertString(0,"The garden full of beans",null);
            Thread.sleep(300);
        } catch (Exception ex) {
            System.out.println("ex="+ex);
            throw new AssertionFailedErrorException("Failed to set the title for Chapter: ",ex);
        }
        // open XML View
        ((EditCookie)bookDO.getCookie(EditCookie.class)).edit();
        // wait for see the changes
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex){}
        
        // test if data object was modified
        SaveCookie cookie = (SaveCookie)bookDO.getCookie(SaveCookie.class);
        assertNotNull("Data Object Not Modified",cookie);
        cookie.save();
        
        // test to golden file
        File original = Helper.getBookFile(getDataDir());
        String golden = "ChangedChapterTitle.pass";
        assertFile(original, getGoldenFile(golden), getWorkDir());
    }
    /**
     * Used for running test from inside the IDE by internal execution.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    private void doSetPreferredLoader (FileObject fo, DataLoader loader) throws IOException {
        pool.setPreferredLoader (fo, loader);
    }
}
