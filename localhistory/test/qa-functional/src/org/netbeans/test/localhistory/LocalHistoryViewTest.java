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
/*
 * LocalHistoryViewTest.java
 *
 * Created on February 2, 2007, 1:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author peter
 */
package org.netbeans.test.localhistory;

import java.io.File;
import java.io.PrintStream;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.localhistory.operators.ShowLocalHistoryOperator;
import org.netbeans.test.localhistory.utils.TestKit;



/**
 *
 * @author pvcs
 */
public class LocalHistoryViewTest extends JellyTestCase {
    
    public static final String TMP_PATH = "/tmp";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator; 
    
    /** Creates a new instance of LocalHistoryViewTest */
    public LocalHistoryViewTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {        
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### "+getName()+" ###");
        
    }
    
    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new LocalHistoryViewTest("testLocalHistoryInvoke"));
        return suite;
    }
    
    public void testLocalHistoryInvoke() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);    
        TestKit.closeProject(PROJECT_NAME);
        
        new File(TMP_PATH).mkdirs();
        projectPath = TestKit.prepareProject("General", "Java Application", PROJECT_NAME);
        ProjectSupport.waitScanFinished();
        Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");    
        
        node.performPopupAction("Open");
        EditorOperator eo = new EditorOperator("Main.java");
        eo.deleteLine(2);
        eo.saveDocument();

        ShowLocalHistoryOperator slho = ShowLocalHistoryOperator.invoke(node);
        slho.verify();
        
        slho.performPopupAction(1, "Revert from History");
        int versions=slho.getVersionCount();
        assertEquals("1. Wrong number of versions!", 2, versions);
        
        slho.performPopupAction(2, "Delete from History");        
        Thread.sleep(100);
        versions=slho.getVersionCount();
        assertEquals("2. Wrong number of versions!", 1, versions);
        
        eo.insert("// modification //", 11, 1);
        eo.save();
        
        Thread.sleep(100);
        versions=slho.getVersionCount();
        assertEquals("2. Wrong number of versions!", 2, versions);
        slho.close();
        
        TestKit.createNewPackage(PROJECT_NAME, "NewPackage");
        TestKit.createNewElement(PROJECT_NAME, "NewPackage", "NewClass");
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "NewPackage|NewClass.java");
        node.performPopupAction("Open");
        eo = new EditorOperator("NewClass.java");
        eo.deleteLine(5);
        eo.insert(os_name, 12, 1);
        eo.saveDocument();
        String fileContent=eo.getText();
        
        slho = ShowLocalHistoryOperator.invoke(node);
        Thread.sleep(100);
        versions = slho.getVersionCount();
        assertEquals("3. Wrong number of versions!", 1, versions);
        slho.close();
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "NewPackage");
        node.performPopupActionNoBlock("Delete");
        NbDialogOperator dialog = new NbDialogOperator("Confirm Object Deletion");
        dialog.yes();
        node = new Node(new SourcePackagesNode(PROJECT_NAME), "");
        node.performPopupAction("Local History|Revert Deleted");

        node = new Node(new SourcePackagesNode(PROJECT_NAME), "NewPackage|NewClass.java");
        slho = ShowLocalHistoryOperator.invoke(node);        
        Thread.sleep(100);        
        versions = slho.getVersionCount();
        assertEquals("3. Wrong number of versions!", 2, versions);
        node.performPopupAction("Open");
        eo = new EditorOperator("NewClass.java");
        assertEquals("Content of file differs after revert!", fileContent, eo.getText());
        eo.deleteLine(5);
        eo.insert(os_name, 12, 1);
        eo.save();        
        Thread.sleep(100);
        versions=slho.getVersionCount();
        assertEquals("4. Wrong number of versions!", 3, versions);
        
    }
}
