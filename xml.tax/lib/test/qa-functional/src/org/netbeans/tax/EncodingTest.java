/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import junit.textui.TestRunner;
import org.netbeans.modules.xml.core.XMLDataObject;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.tests.xml.XTest;
import org.openide.cookies.CloseCookie;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module API Test: Encoding Test
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 * Tests check whether the documents saved with different encodings are identical.<BR>
 *
 * <BR><B>How it works:</B><BR>
 * Test doing for each encoding:<BR>
 * - save document with selected encoding<BR>
 * - close the document<BR>
 * - reload the document from disk<BR>
 * - check if the reloaded document and the original are identical<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * None
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * DTD for the XML document.<BR>
 * <BR><B>Possible reasons of failure:</B>
 * <UL>
 * <LI type="circle">
 * <I>None<BR></I>
 * </LI>
 * </UL>
 * <P>
 */

public class EncodingTest extends XTest {
    /** Creates new CoreSettingsTest */
    public EncodingTest(String testName) {
        super(testName);
    }
    
    public void testEncoding() throws Exception {
        final String DATA_OBJECT = "encoding.xml";
        final String NDATA_OBJECT = "newEncoding.xml";
        TreeEditorCookie cake;
        
        // prepare data
        XMLDataObject original = (XMLDataObject) TestUtil.THIS.findData(DATA_OBJECT);
        if (original == null) {
            fail("\"" + DATA_OBJECT + "\" data object not found!");
        }
        cake = (TreeEditorCookie) original.getCookie(TreeEditorCookie.class);        
        TreeElement docRoot = ((TreeDocument)cake.openDocumentRoot()).getDocumentElement();
        String defEncoding =  cake.getDocumentRoot().getEncoding();
        String gString = TestUtil.THIS.nodeToString(docRoot);
        Iterator encodings = TreeUtilities.getSupportedEncodings().iterator();
        
        // prepare workdir
        File workDir = getWorkDir();
        try {
            clearWorkDir();
        } catch (IOException ex) {
            log("clearWorkDir() throws: " + ex);
        }
        
        FileSystem fs = TestUtil.THIS.mountDirectory(workDir);
        DataFolder dataFolder = DataFolder.findFolder(fs.getRoot());
        
        while (encodings.hasNext()) {
            String encoding = (String) encodings.next();
            String fileName = encoding + ".xml";
            try {
                dbg.println("Testing encoding: " + encoding + " ... ");
                if (encoding.equals(defEncoding)) break;  // Nothing to test.
                
                // create new document, set encoding, save and close it
                XMLDataObject xdao = (XMLDataObject) original.createFromTemplate(dataFolder, encoding);
                cake = (TreeEditorCookie) xdao.getCookie(TreeEditorCookie.class);        
                TreeDocument newDoc = (TreeDocument) cake.openDocumentRoot();
                newDoc.setEncoding(encoding);
                TestUtil.THIS.saveDataObject(xdao);
                CloseCookie cc = (CloseCookie) xdao.getCookie(CloseCookie.class);
                cc.close();
                
                // read the document and check his content
                cake = (TreeEditorCookie) xdao.getCookie(TreeEditorCookie.class);        
                TreeElement newRoot = ((TreeDocument) cake.getDocumentRoot()).getDocumentElement();
                String nString = TestUtil.THIS.nodeToString(newRoot);
                assertEquals("Encoding: " + encoding + ", documents are differ", gString, nString);
            } catch (Exception ex) {
                ex.printStackTrace(dbg);
                fail("Encoding: " + encoding + ", test faill due:\n" + ex);
            }
        }
    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception {
        DEBUG = true;
        TestRunner.run(EncodingTest.class);
    }
}
