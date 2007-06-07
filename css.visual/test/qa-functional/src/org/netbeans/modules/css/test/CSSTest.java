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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.css.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;


/**
 *
 * @author Jindrich Sedek
 */
public class CSSTest extends JellyTestCase {
    protected static final String newFileName = "newFileName";
    protected static final int rootRuleLineNumber = 15;
    protected static final String projectName = "CSSTestProject";
    
    /** Creates new CSS Test */
    public CSSTest(String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() throws Exception{
        super.setUp();
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    }
    
    protected void openFile(String fileName){
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        new Node(rootNode,"Web Pages|css|"+fileName).performPopupAction("Open");
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite("TestCSS");
        suite.addTest(new TestBasic("testNewCSS"));
        suite.addTest(new TestBasic("testAddRule"));
        suite.addTest(new TestBasic("testNavigator"));
        suite.addTest(new TestFontSettings("testSetFontFamily"));
        suite.addTest(new TestFontSettings("testChangeFontFamily"));
        suite.addTest(new TestFontSettings("testChangeFontSize"));
        //        disabled because of Issue #105728
        //        suite.addTest(new TestFontSettings("testChangeFontWeight"));
        //        suite.addTest(new TestFontSettings("testChangeFontStyle"));
        //        suite.addTest(new TestFontSettings("testChangeFontVariant"));
        //        suite.addTest(new TestFontSettings("testDecoration"));
        //        suite.addTest(new TestFontSettings("testChangeFontColor"));
        
        return suite;
    }
    
    public static void main(String[] args) throws Exception {
        TestRunner.run(new TestFontSettings("testChangeFontSize"));
        //TestRunner.run(suite());
    }
    
}