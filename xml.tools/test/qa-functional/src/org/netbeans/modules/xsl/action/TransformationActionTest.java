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

import java.awt.event.KeyEvent;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.xml.XSLTransformationDialog;
import org.netbeans.jellytools.modules.xsl.actions.TransformAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.tests.xml.JXTest;
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
        
        final String OUT_FILE = "../out/document.html";
        //final String OUT_FILE = "output.html"; //!!!
        final String OUT_NODE = "out" + DELIM + "document";
        //final String OUT_NODE = "sources" + DELIM + "output"; //!!!
        
        // clear output and display Transformation Dialog
        DataObject dao = TestUtil.THIS.findData("out/document.html");
        if (dao != null) /* then */ dao.delete();
        XSLTransformationDialog dialog = transformXML("sources" + DELIM + "document");
        
        // fill in the TransformationDialog and execute transformation
        dialog.cboXSLTScript().clearText();
        dialog.cboXSLTScript().typeText("../styles/doc2html.xsl");
        dialog.cboXSLTScript().pressKey(KeyEvent.VK_TAB);
        
        dialog.cboOutput().clearText();
        dialog.cboOutput().typeText(OUT_FILE);
        dialog.cboJComboBox().selectItem(dialog.ITEM_DONOTHING);
        dialog.oK();
        
        // check the transformation's output
        char[] cbuf = new char[4000];
        Node htmlNode = findDataNode(OUT_NODE);
        new OpenAction().perform(htmlNode);
        // force editor to reload document
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = ewo.getEditor(htmlNode.getText());
        eo.setCaretPositionToLine(1);
        eo.insert("\n");
        eo.waitModified(true);
        eo.deleteLine(1);
        eo.save();
        
        String substring = "<h1>Testing Document</h1>";
        boolean result = eo.getText().indexOf(substring) != -1;
        assertTrue("Cannot find control substring:\n" + substring, (result));
        ewo.close();
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
        TransformAction transform =  new TransformAction();
        transform.perform(findDataNode(path));
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
