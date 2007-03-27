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

import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.StringComparator;


/**
 * Utilities for Memory footprint tests
 *
 * @author  anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class VWPFootprintUtilities extends gui.VWPUtilities{
    
    static String createproject(String category, String project, int framework, boolean wait) {
        // select Projects tab
        ProjectsTabOperator.invoke();
        
        // create a project
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        
       // Added more strict comparation behaviour for strings 
        StringComparator ptree = wizard.treeCategories().getComparator();
        StringComparator plist = wizard.lstProjects().getComparator(); 
        
        Operator.DefaultStringComparator ncs;
	ncs = new Operator.DefaultStringComparator(true,true);
        wizard.lstProjects().setComparator(ncs);
        wizard.treeCategories().setComparator(ncs);
        
        wizard.selectCategory(category);
        wizard.selectProject(project);

	wizard.lstProjects().setComparator(plist);
        wizard.treeCategories().setComparator(ptree);
        
        wizard.next();

        NewProjectNameLocationStepOperator wizard_location = new NewProjectNameLocationStepOperator();
        wizard_location.txtProjectLocation().clearText();
        wizard_location.txtProjectLocation().typeText(System.getProperty("xtest.tmpdir"));
        String pname = wizard_location.txtProjectName().getText();

        // if the project exists, try to generate new name
        for (int i = 0; i < 5 && !wizard.btFinish().isEnabled(); i++) {
            pname = pname+"1";
            wizard_location.txtProjectName().clearText();
            wizard_location.txtProjectName().typeText(pname);
        }
        
        wizard.next();
        
        JTableOperator frameworkselector = new JTableOperator(wizard);
        frameworkselector.selectCell(0,framework);

        wizard.finish();
        
        // wait for 10 seconds
        waitForProjectCreation(10000, wait);
        
        return pname;        
    }

}
