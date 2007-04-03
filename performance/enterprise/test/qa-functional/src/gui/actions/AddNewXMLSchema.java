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

package gui.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.junit.ide.ProjectSupport;
import java.io.File;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

/**
 * Test Add New XML Schema
 *
 * @author  rashid@netbeans.org
 */
public class AddNewXMLSchema extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewFileNameLocationStepOperator location;
    
    private static String testProjectName = "BPELTestProject";    
    private int index;
    private int indexI;
    /**
     * Creates a new instance of AddNewXMLSchema
     * @param testName the name of the test
     */
    public AddNewXMLSchema(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of AddNewXMLSchema
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public AddNewXMLSchema(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
   
    
    public void initialize(){
        indexI=1;
        
        ProjectSupport.openProject(System.getProperty("xtest.tmpdir")+File.separator+testProjectName);
        new CloseAllDocumentsAction().performAPI();
    }
    
    public void prepare(){
        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        Node doc = new Node(pNode,"Process Files");
        doc.select();

        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        wizard.selectCategory("XML"); //NOI18N
        wizard.selectFileType("XML Schema"); //NOI18N
        wizard.next();
        
        new EventTool().waitNoEvent(1000);
        location = new NewFileNameLocationStepOperator();
        location.txtObjectName().setText("XMLSchema_"+(indexI++));

    }
    
    public ComponentOperator open(){
       location.finish();
       return null;
    }
    
    public void close(){
       new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }

    protected void shutdown() {
        ProjectSupport.closeProject(testProjectName);

    }   
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new AddNewXMLSchema("measureTime"));
    }
}
