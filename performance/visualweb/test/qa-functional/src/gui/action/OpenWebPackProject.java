
/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package gui.action;

import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;

import org.netbeans.junit.ide.ProjectSupport;

/**
 * Test create Web Pack projects
 *
 * @author  mkhramov@netbeans.org
 */
public class OpenWebPackProject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    JButtonOperator openButton;
    private static String projectName = "VisualWebProject";
    
    /**
     * Creates a new instance of OpenWebPackProject
     * @param testName the name of the test
     */
    public OpenWebPackProject(String testName) {
        super(testName);
        expectedTime = 18000;
        WAIT_AFTER_OPEN=20000;
    }
    
    /**
     * Creates a new instance of OpenWebPackProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenWebPackProject(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 18000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public void initialize(){
        log("::initialize::");
    }
    
    public void prepare(){
        log("::prepare");
//TODO do Open project through UI        gui.VWPUtilities.waitProjectOpenedScanFinished(System.getProperty("xtest.tmpdir")+ java.io.File.separator +projectName);
        log("::open Project passed");
    }
    
    public ComponentOperator open(){
        log("::open");
        openButton.pushNoBlock();
        return null;
    }
    
    public void close(){
        log("::close");
        ProjectSupport.closeProject(projectName);
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OpenWebPackProject("measureTime"));
    }
    
}