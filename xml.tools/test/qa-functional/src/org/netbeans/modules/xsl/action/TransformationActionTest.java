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
package org.netbeans.modules.xsl.action;

import java.io.InputStreamReader;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.modules.xml.XSLTransformationDialog;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.oo.gui.jelly.Explorer;
import org.netbeans.test.oo.gui.jelly.xml.nodes.XMLNode;
import org.netbeans.tests.xml.JXTest;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/** Checks XSL Transformation action. */

public class TransformationActionTest extends JXTest {
    
    /** Creates new XMLNodeTest */
    public TransformationActionTest(String testName) {
        super(testName);
    }
    
    // TESTS ///////////////////////////////////////////////////////////////////
    
    /** Performs 'XSL Transformation...' action and checks output. */
    public void testTransformation() throws Exception {
        // clear output and display Transformation Dialog
        DataObject dao = TestUtil.THIS.findData("out/document.html");
        if (dao != null) /* then */ dao.delete();
        XSLTransformationDialog dialog = transformXML("sources" + DELIM + "document");
        
        // fill in the TransformationDialog and execute transformation
        dialog.cboXSLTScript().clearText();
        dialog.cboXSLTScript().typeText("../styles/doc2html.xsl");
        dialog.cboOutput().clearText();
        dialog.cboOutput().typeText("../out/document.html");
        dialog.cboJComboBox().selectItem(dialog.ITEM_DONOTHING);
        dialog.oK();
        
        // check the transformation's output
        char[] cbuf = new char[4000];
        FileObject fo = TestUtil.THIS.findData("out/document.html").getPrimaryFile();
        InputStreamReader isr = new InputStreamReader(fo.getInputStream());
        isr.read(cbuf);
        boolean result = String.valueOf(cbuf).indexOf("<h1>Testing Document</h1>") != -1;
        assertTrue("Cannot find control substring.", (result));
    }
    
    /** Displays XSL Transformation Dialog and vrerifies it */
    public void testTransformationDialog() throws Exception {
        // display Transformation Dialog
        XSLTransformationDialog dialog = transformXML("sources" + DELIM + "document");
        dialog.verify();
        dialog.close();
    }
    
    // LIB ////////////////////////////////////////////////////////////////////
    
    /**
     * Performs 'XSL Transformation...' action on a XML.
     * @param path relative to the 'data' folder delimited by 'DELIM'
     */
    private XSLTransformationDialog transformXML(String path) throws Exception {
        Explorer exp = Explorer.find();
        exp.switchToFilesystemsTab();
        String treePath = getFilesystemName() + DELIM + getDataPackageName(DELIM) + DELIM + path;
        XMLNode xnode = new XMLNode(exp.getJTreeOperator(), treePath);
        xnode.transform();
        XSLTransformationDialog dialog =  new XSLTransformationDialog();
        dialog.activate();
        return dialog;
    }
    
    // MAIN ////////////////////////////////////////////////////////////////////
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new TransformationActionTest("testTransformationDialog"));
        suite.addTest(new TransformationActionTest("testTransformation"));
        return suite;
    }
    
    public static void main(String[] args) throws Exception {
        System.setProperty("xmltest.dbgTimeouts", "true");
        //TestRunner.run(TransformationActionTest.class);
        TestRunner.run(suite());
    }
}
