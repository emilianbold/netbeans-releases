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
package org.netbeans.jellytools.modules.xml.actions;

import java.io.InputStreamReader;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ExplorerOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.xml.XSLTransformationDialog;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventDispatcher;
import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.tests.xml.JXTest;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/** Checks XSL Transformation action. */

public class ActionsTest extends JXTest {
    private static final int CLASS = 0;
    private static final int TPATH = 1;
    private static final int ATTRS = 2;
    
    /** Creates new XMLNodeTest */
    public ActionsTest(String testName) {
        super(testName);
    }
    
    // TESTS ///////////////////////////////////////////////////////////////////
    
    public void test() {
        actionTest(new CheckDTDAction(), "PA",  "states");
        actionTest(new CheckXMLAction(),    "PAS",  "XMLDocument");
        //actionTest(new EditScenariosAction(), "PASc",  "stylesheet"); //!!! #26559
        actionTest(new GenerateCSSAction(), "PAc",  "states");
        actionTest(new GenerateDOMTreeScannerAction(), "PAc",  "states");
        actionTest(new GenerateDTDAction(), "PAc",  "XMLDocument");
        actionTest(new GenerateDocumentationAction(), "PAc",  "states");
        actionTest(new NewAttributeAction(), "Pc",  "XMLDocument" + DELIM + "root");
        actionTest(new NewCDATAction(), "Pc",  "XMLDocument" + DELIM + "root");
        actionTest(new NewCharRefAction(), "Pc",  "XMLDocument" + DELIM + "root");
        actionTest(new NewCommentAction(),  "Pc",   "XMLDocument");
        actionTest(new NewDoctypeAction(),  "Pc",   "XMLDocument");
        actionTest(new NewElementAction(),  "Pc",   "XMLDocument");
        actionTest(new NewEntityReferenceAction(),  "Pc",   "XMLwithDTD" + DELIM + "root");
        actionTest(new NewPIAction(),       "Pc",   "XMLDocument");
        actionTest(new NewTextAction(),  "Pc",   "XMLDocument" + DELIM + "root");
        actionTest(new NormalizeElementAction(),  "P",   "XMLDocument" + DELIM + "root");
        actionTest(new ReloadDocumentAction(), "PA",  "XMLDocument");
        actionTest(new SAXDocumentHandlerWizardAction(), "PAc",  "states");
        actionTest(new TransformAction(),   "PASc", "XMLDocument");
        actionTest(new ValidateXMLAction(), "PA",  "XMLDocument");
    }
    
    // LIB /////////////////////////////////////////////////////////////////////
    
    /** Tests org.netbeans.jellytools.actions.Action's subclases.
     * @param action tested action
     * @param attrs can consist from
     * <li> P - test performPopup()
     * <li> A - test performAPI()
     * <li> S - test performShortcut()
     * <li> c - close dialog after each perform method
     * @param treePath relative path to 'data' folder delimited by DELIM
     * @return true if test passes esle false
     */
    private boolean actionTest(Action action, String attrs, String treePath) {
        boolean pass = true;
        log("<test class=\"" + action.getClass().getName() + "\">");
        try {
            Node node = findDataNode(treePath);
            boolean close = (attrs.indexOf('c') != -1);
            
            //            if (attrs.indexOf('S') != -1) {
            //                log("  <shortcut-test-start\\>");
            //                action.performShortcut(node);
            //                if (close) cancelDialog();
            //                log("  <shortcut-test-finished\\>");
            //            }
            if (attrs.indexOf('P') != -1) {
                log("  <popup-test-start\\>");
                action.performPopup(node);
                if (close) cancelDialog();
                log("  <popup-test-finished\\>");
            }
            if (attrs.indexOf('A') != -1) {
                log("  <API-test-start\\>");
                action.performAPI(node);
                if (close) cancelDialog();
                log("  <API-test-finished\\>");
            }
            if (!close) sleepTest(2000);
        } catch (Exception ex) {
            pass = false;
            log("Failed:", ex);
        } finally {
            log("<\\test>");
        }
        return pass;
    }
    
    /**
     * Finds Node in the 'data' forlder.
     * @param path relative to the 'data' folder delimited by 'DELIM'
     */
    private Node findDataNode(String path) throws Exception {
        String treePath = getFilesystemName() + DELIM + getDataPackageName(DELIM) + DELIM + path;
        JTreeOperator tree = ExplorerOperator.invoke().repositoryTab().tree();
        return new Node(tree, treePath);
    }
    
    /** Waits for 3 secs and close the first dialog. */
    private void cancelDialog() {
        try {
            EventDispatcher.waitQueueEmpty();
        } catch (Exception e) { /* do nothing */ }
        sleepTest(1000);
        
        new NbDialogOperator("").cancel();
    }
    
    // MAIN ////////////////////////////////////////////////////////////////////
    
    public static void main(String[] args) throws Exception {
        System.setProperty("xmltest.dbgTimeouts", "true");
        logIntoConsole(true);
        TestRunner.run(ActionsTest.class);
    }
}
