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

package gui.actions;

import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.WizardOperator;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.ide.ProjectSupport;


/**
 * Test Open BPEL project
 *
 * @author  rashid@netbeans.org
 */
public class OpenBPELproject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static String projectName = "BPELProject_Open";
    private JButtonOperator openButton;
    
    /**
     * Creates a new instance of OpenBPELproject
     * @param testName the name of the test
     */
    public OpenBPELproject(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of CreateProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenBPELproject(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    public void initialize(){
    }
    
    public void prepare(){
        new ActionNoBlock("File|Open Project...",null).perform(); //NOI18N
        WizardOperator opd = new WizardOperator("Open Project"); //NOI18N
        JTextComponentOperator path = new JTextComponentOperator(opd,1);
        openButton = new JButtonOperator(opd,"Open Project"); //NOI18N
        String paths= (System.getProperty("xtest.tmpdir") + java.io.File.separator + projectName + java.io.File.separator + projectName);
        path.setText(paths);
    }
    
    public ComponentOperator open(){
        openButton.pushNoBlock();
        return null;
    }
    
    public void close(){
        ProjectSupport.closeProject(projectName);
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OpenBPELproject("measureTime"));
    }
}