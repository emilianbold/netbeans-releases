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

package footprint;

import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;


/**
 * Utilities for Memory footprint tests
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class EPFootprintUtilities extends gui.EPUtilities{
    
    static String creatJ2EEeproject(String category, String project, boolean wait) {
        return createProjectGeneral(category, project, wait, true);
    }
    
    private static String createProjectGeneral(String category, String project, boolean wait, boolean j2eeProject) {
        // select Projects tab
        ProjectsTabOperator.invoke();
        
        // create a project
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();

        NewProjectNameLocationStepOperator wizard_location = new NewProjectNameLocationStepOperator();
        wizard_location.txtProjectLocation().clearText();
        wizard_location.txtProjectLocation().typeText(System.getProperty("xtest.tmpdir"));
        String pname = wizard_location.txtProjectName().getText();

        if(j2eeProject) {
            new JComboBoxOperator(wizard_location,1).selectItem(1);
            new JCheckBoxOperator(wizard_location,"Create Application Client module:").setSelected(true);
        }
        
        // if the project exists, try to generate new name
        for (int i = 0; i < 5 && !wizard.btFinish().isEnabled(); i++) {
            pname = pname+"1";
            wizard_location.txtProjectName().clearText();
            wizard_location.txtProjectName().typeText(pname);
        }
        wizard.finish();

        // wait 10 seconds
        waitForProjectCreation(10000, wait);
        
        return pname;
    }
    
    static void killRunOnProject(String project) {
        killProcessOnProject(project, "run");
    }
    
    static void killDebugOnProject(String project) {
        killProcessOnProject(project, "debug");
    }
    
    private static void killProcessOnProject(String project, String process) {
        // prepare Runtime tab
        RuntimeTabOperator runtime = RuntimeTabOperator.invoke();
        
        // kill the execution
        Node node = new Node(runtime.getRootNode(), "Processes|"+project+ " (" + process + ")");
        node.select();
        node.performPopupAction("Terminate Process");
    }
}
