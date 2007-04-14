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

import gui.EPUtilities;

import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test Add New Bpel Process
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class AddNewBpelProcess extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewFileNameLocationStepOperator location;
    
    private int index;
    
    /**
     * Creates a new instance of AddNewBpelProcess
     * @param testName the name of the test
     */
    public AddNewBpelProcess(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of AddNewBpelProcess
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public AddNewBpelProcess(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    public void initialize(){
        index=1;
        new CloseAllDocumentsAction().performAPI();
    }
    
    public void prepare(){
        EPUtilities.getProcessFilesNode("BPELTestProject").select();
        
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        wizard.selectCategory("Service Oriented Architecture"); //NOI18N
        wizard.selectFileType("BPEL Process"); //NOI18N
        wizard.next();
        
        new EventTool().waitNoEvent(1000);
        location = new NewFileNameLocationStepOperator();
        location.txtObjectName().setText("BPELProcess_"+(index++));
    }
    
    public ComponentOperator open(){
        location.finish();
        return null;
    }
    
    public void close(){
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new AddNewBpelProcess("measureTime"));
    }
}
