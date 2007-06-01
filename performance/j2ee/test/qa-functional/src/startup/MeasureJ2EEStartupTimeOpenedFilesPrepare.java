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

package startup;

import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;


/**
 * Prepare user directory for measurement of startup time of IDE with opened files.
 * Open 10 java files and shut down ide. 
 * Created user directory will be used to measure startup time of IDE with opened files. 
 *
 * @author Marian.Mirilovic@sun.com
 */
public class MeasureJ2EEStartupTimeOpenedFilesPrepare extends JellyTestCase {
    
    /** Define testcase
     * @param testName name of the testcase
     */    
    public MeasureJ2EEStartupTimeOpenedFilesPrepare(String testName) {
        super(testName);
    }

    /** Testsuite
     * @return testuite
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new IDESetupTest("testCloseMemoryToolbar"));
        suite.addTest(new IDESetupTest("closeAllDocuments"));
        suite.addTest(new IDESetupTest("testAddAppServer"));
        suite.addTest(new MeasureJ2EEStartupTimeOpenedFilesPrepare("testOpenProjects"));
        suite.addTest(new MeasureJ2EEStartupTimeOpenedFilesPrepare("openFiles"));
        return suite;
    }
    
    @Override
    public void setUp() {
        System.out.println("########  "+getName()+"  ########");
    }
    
    public void testOpenProjects() {
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/startup/TestStartupApp");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/startup/TestStartupApp/TestStartupApp-ejb");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/startup/TestStartupApp/TestStartupApp-war");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/startup/TestStartupEJB1");
        ProjectSupport.waitScanFinished();
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+"/startup/TestStartupEJB2");
        ProjectSupport.waitScanFinished();
        //waitForScan();
    }
    
    private void waitForScan() {
        // "Scanning Project Classpaths"
        String titleScanning = Bundle.getString("org.netbeans.modules.javacore.Bundle", "TXT_ApplyingPathsTitle");
        NbDialogOperator scanningDialogOper = new NbDialogOperator(titleScanning);
        // scanning can last for a long time => wait max. 5 minutes
        scanningDialogOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 300000);
        scanningDialogOper.waitClosed();
    }  
    
    /** 
     * Open 10 selected files from jEdit project. 
     */
    public void openFiles(){
        
        new org.netbeans.jemmy.EventTool().waitNoEvent(10000);
        
        String[][] files_path = { 
            {"TestStartupApp","Configuration Files|sun-application.xml"},
            {"TestStartupApp-EJB","Enterprise Beans|TestSessionSB"},
            {"TestStartupApp-EJB","Configuration Files|ejb-jar.xml"},
            {"TestStartupApp-EJB","Configuration Files|sun-ejb-jar.xml"},
            {"TestStartupApp-WAR","Web Pages|index.jsp"},
            {"TestStartupApp-WAR","Configuration Files|web.xml"}, 
            {"TestStartupApp-WAR","Configuration Files|sun-web.xml"},
            {"TestStartupApp-WAR","Source Packages|test|TestServlet.java"},
            {"TestStartupEJB1","Enterprise Beans|TestSession2SB"},
            {"TestStartupEJB1","Enterprise Beans|TestMessageMDB"},
            {"TestStartupEJB1","Enterprise Beans|TestEntityEB"},
// TODO - uncomment when Web Services node becomes fixed
//            {"TestStartupEJB2","Web Services|TestWebService1"},
//            {"TestStartupEJB2","Web Services|TestWebService2"}
        };
        
        Node[] openFileNodes = new Node[files_path.length];
        
        for(int i=0; i<files_path.length; i++) {
                Node root = new ProjectsTabOperator().getProjectRootNode(files_path[i][0]);
                root.setComparator(new Operator.DefaultStringComparator(true, true));
                openFileNodes[i] = new Node(root, files_path[i][1]);
                // open file one by one, opening all files at once causes never ending loop (java+mdr)
                //new OpenAction().performAPI(openFileNodes[i]);
        }
        
        // try to come back and open all files at-once, rises another problem with refactoring, if you do open file and next expand folder, 
        // it doesn't finish in the real-time -> hard to reproduced by hand
        new OpenAction().performAPI(openFileNodes);
        
        new org.netbeans.jemmy.EventTool().waitNoEvent(60000);
        
    }
   

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }    
}
