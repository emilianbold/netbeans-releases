/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.properties.jelly2tests.suites.properties_editing;

import lib.PropertiesEditorTestCase;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.PropertiesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Janie
 */
public class AddingNewKeyAndValues extends PropertiesEditorTestCase {
    
    //Variables of test
    public String WORKING_PACKAGE = "working";
    public String BUNDLE_NAME = "bundle";
    public ProjectsTabOperator pto;
    public ProjectRootNode prn;
    public PropertiesNode pn;

    public AddingNewKeyAndValues(String name) {
        super(name);
    }
    
    public static NbTestSuite suite(){
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new AddingNewKeyAndValues("testCreateNewBundle"));
        suite.addTest(new AddingNewKeyAndValues("testOpenningSimpleEditor"));
        suite.addTest(new AddingNewKeyAndValues("testOpenningAdvanceEditor"));
        suite.addTest(new AddingNewKeyAndValues("testAddNewKeyAndValue"));
        return suite;
    }
    public static void main(String[] args) {
       junit.textui.TestRunner.run(suite());
       // suite.addTestSuite(new AddingNewKeyAndValues(""));
    }
    
    public void testCreateNewBundle(){
            
        //open default project
        openDefaultProject();
        
        //Create new properties file
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(DEFAULT_PROJECT_NAME);
        nfwo.selectCategory(WIZARD_CATEGORY_FILE);
        nfwo.selectFileType(WIZARD_FILE_TYPE);
        nfwo.next();
        
        NewFileNameLocationStepOperator nfnlso = new NewFileNameLocationStepOperator();
        nfnlso.setObjectName(BUNDLE_NAME);
        JTextFieldOperator jtfo = new JTextFieldOperator(nfnlso, 2);
        jtfo.setText("src/" + WORKING_PACKAGE);
        nfnlso.finish();
        
        //Check that bundle was created
        if (!existsFileInEditor(BUNDLE_NAME)) {
            fail("File " + BUNDLE_NAME + " not found in Editor window");
        }
        if (!existsFileInExplorer(WORKING_PACKAGE, BUNDLE_NAME + ".properties")) {
            fail("File " + BUNDLE_NAME + " not found in explorer");
        }
    }
    
    public void testOpenningSimpleEditor() {
        
        //select bundle node in Project Window and do edit action
        selectBundle().edit();
        
        //Check that Advance Editor was opened
       if (!existsFileInEditor(BUNDLE_NAME)) {
            fail("File " + BUNDLE_NAME + " not opened in Editor window");
        }
    }
    
    public void testOpenningAdvanceEditor(){
        
         //select bundle node in Project Window and do open action
        selectBundle().open();
        
       if (!existsFileInAdvanceEditor(BUNDLE_NAME+".properties")) {
           fail("File " + BUNDLE_NAME + " not opened in Advance Editor window");
       }
    }
    
    public void testAddNewKeyAndValue(){
        selectBundle().open();
        TopComponentOperator tco = new TopComponentOperator(BUNDLE_NAME+".properties");
        JButtonOperator jbo = new JButtonOperator(tco, "New Property...");
        jbo.push();
    }

    public PropertiesNode selectBundle(){
        pto = ProjectsTabOperator.invoke();
        prn = new ProjectRootNode(new JTreeOperator(pto), DEFAULT_PROJECT_NAME);
        pn = new PropertiesNode(prn, "Source Packages" + TREE_SEPARATOR + WORKING_PACKAGE + TREE_SEPARATOR +BUNDLE_NAME + ".properties");
        return pn;
    }
}
