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
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.AssertionFailedErrorException;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;

import org.netbeans.modules.xml.multiview.test.util.Helper;
import org.netbeans.modules.xml.multiview.test.bookmodel.*;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;

/**
 *
 * @author Milan Kuchtiak
 */
public class XmlMultiViewEditorTest extends NbTestCase {
    private DataLoaderPool pool;
    private DataLoader loader;
    private BookDataObject bookDO;

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
        initDataObject();
    }

    public void testChangeModel() throws IOException {
        initDataObject();
        try {
            Book book = bookDO.getBook();
            book.setAttributeValue("chapter", 0, "length", "110");
            bookDO.modelUpdatedFromUI();
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to change book model",ex);
        }
        Helper.waitForDispatchThread();
        // test if data object was modified
        SaveCookie cookie = Helper.getSaveCookie(bookDO);
        assertNotNull("Data Object Not Modified", cookie);
        cookie.save();

        // test to golden file
        File original = Helper.getBookFile(getDataDir());
        assertTrue("File doesn't contain the text : <chapter length=\"110\">",
                    Helper.isTextInFile("<chapter length=\"110\">",original));
    }

    public void testChangeModelInDesignView() throws IOException {
        initDataObject();
        try {
            bookDO.showElement(bookDO.getBook().getChapter()[1]);
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to open Chapter section", ex);
        }
        Helper.waitForDispatchThread();
        try {
            JTextField titleTF = Helper.getChapterTitleTF(bookDO, bookDO.getBook().getChapter()[1]);
            titleTF.requestFocus();
            titleTF.getDocument().remove(0, titleTF.getDocument().getLength());
            titleTF.getDocument().insertString(0, "The garden full of beans", null);
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to set the title for Chapter: ", ex);
        }
        Helper.waitForDispatchThread();
        // open XML View
        ((EditCookie) bookDO.getCookie(EditCookie.class)).edit();
        Helper.waitForDispatchThread();
        // handle consequent calls of SwingUtilities.invokeLater();
        Helper.waitForDispatchThread();

        // test if data object was modified
        SaveCookie cookie = Helper.getSaveCookie(bookDO);
        assertNotNull("Data Object Not Modified", cookie);
        cookie.save();

        // test to golden file
        File original = Helper.getBookFile(getDataDir());
        assertTrue("File doesn't contain the text : <title lang=\"en\">The garden full of beans</title>",
                Helper.isTextInFile("<title lang=\"en\">The garden full of beans</title>", original));
    }

    public void testExternalChange() throws IOException {
        initDataObject();
        String golden = "ChangedChapterTitle.pass";
        FileObject fo = bookDO.getPrimaryFile();
        InputStream is = new FileInputStream(getGoldenFile(golden));
        try {
            org.openide.filesystems.FileLock lock = fo.lock();
            OutputStream os = fo.getOutputStream(lock);
            try {

                int b;
                while ((b = is.read()) != -1) {
                    char ch = (char) b;
                    if (ch == '2') {
                        os.write(b);
                    }
                    os.write(b);
                }
            }
            finally {
                os.close();
                is.close();
                lock.releaseLock();
            }
        } catch (org.openide.filesystems.FileAlreadyLockedException ex) {
            throw new AssertionFailedErrorException("Lock problem : ", ex);
        }

        Helper.waitForDispatchThread();

        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport) bookDO.getCookie(EditorCookie.class);
        Document doc = editor.getDocument();
        try {
            assertTrue("XML document doesn't contain the external changes: ",
                    doc.getText(0, doc.getLength()).indexOf("<chapter length=\"122\">") > 0);
        } catch (BadLocationException ex) {
            throw new AssertionFailedErrorException(ex);
        }
    }

    private void doSetPreferredLoader (FileObject fo, DataLoader loader) throws IOException {
        DataLoaderPool.setPreferredLoader (fo, loader);
    }

    private void initDataObject() throws IOException {
        if (bookDO == null) {
            File f = Helper.getBookFile(getDataDir());
            FileObject fo = FileUtil.toFileObject(f);
            assertNotNull(fo);

            doSetPreferredLoader(fo, loader);
            DataObject dObj = DataObject.find(fo);
            assertNotNull("Book DataObject not found", dObj);
            assertEquals(BookDataObject.class, dObj.getClass());

            bookDO = (BookDataObject) dObj;
            ((EditCookie) bookDO.getCookie(EditCookie.class)).edit();

            // wait to open the document
            Helper.waitForDispatchThread();

            XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport) bookDO.getCookie(EditorCookie.class);
            Document doc = Helper.getDocument(editor);
            assertTrue("The document is empty :", doc.getLength() > 0);
        }
    }

    /**
     * Used for running test from inside the IDE by internal execution.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
