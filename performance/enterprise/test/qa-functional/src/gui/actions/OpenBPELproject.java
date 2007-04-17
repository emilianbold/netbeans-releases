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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui.actions;

import java.io.File;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.junit.ide.ProjectSupport;


/**
 * Test Open BPEL project
 *
 * @author  rashid@netbeans.org
 */
public class OpenBPELproject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
      private static String ProjectName = "BPELProject_Open";   
      private JButtonOperator open;
         
        
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
      new ActionNoBlock("File|Open Project...",null).perform();
      WizardOperator opd = new WizardOperator("Open Project");
      JTextComponentOperator path = new JTextComponentOperator(opd,1);
      open = new JButtonOperator(opd,"Open Project Folder");
      String paths= (System.getProperty("xtest.tmpdir")+File.separator+ProjectName+File.separator+ProjectName");
      path.setText(paths);
          
    }
    
    public ComponentOperator open(){
        open.pushNoBlock(); 
        return null;
    }
    
    public void close(){
        ProjectSupport.closeProject(ProjectName);
        new CloseAllDocumentsAction().performAPI(); //avoid issue 68671 - editors are not closed after closing project by ProjectSupport
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(new OpenBPELproject("measureTime"));
    }
}
