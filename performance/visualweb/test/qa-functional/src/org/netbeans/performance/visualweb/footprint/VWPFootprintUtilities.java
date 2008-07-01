/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.performance.visualweb.footprint;

import java.io.File;
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
public class VWPFootprintUtilities extends org.netbeans.performance.visualweb.VWPUtilities {
    
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
        
        String projectLoc = System.getProperty("nbjunit.workdir") + File.pathSeparator + "createdProjects";
        wizard_location.txtProjectLocation().typeText(projectLoc);
        
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
