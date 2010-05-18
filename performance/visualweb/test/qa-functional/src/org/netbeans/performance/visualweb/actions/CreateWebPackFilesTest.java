/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.performance.visualweb.actions;

import org.netbeans.performance.visualweb.VWPUtilities;
import org.netbeans.performance.visualweb.setup.VisualWebSetup;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;

import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test create Web Pack projects
 *
 * @author  mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class CreateWebPackFilesTest extends PerformanceTestCase {
   
    private String doccategory, doctype, docname, docfolder, suffix, projectfolder, buildedname;
    private NewFileNameLocationStepOperator location;
    
    private static final String project_name = "UltraLargeWA";
    private ProjectsTabOperator pto;
    private Node projectRoot;

    /**
     * Creates a new instance of CreateWebPackFiles
     * @param testName the name of the test
     */
    public CreateWebPackFilesTest(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of CreateWebPackFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateWebPackFilesTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(VisualWebSetup.class)
             .addTest(CreateWebPackFilesTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

  
    public void testCreateVWJSPPage(){
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
        docname = "JSFPage"; //NOI18N
        doccategory = "JavaServer Faces"; //NOI18N
        doctype ="Visual Web JSF Page"; //NOI18N
        docfolder = "web";
        suffix = ".jsp";
        projectfolder = VWPUtilities.WEB_PAGES;
        doMeasurement();
    }

    public void testCreateVWJSPFragment(){
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
        docname = "JSFFragment"; //NOI18N
        doccategory = "JavaServer Faces"; //NOI18N
        doctype = "Visual Web JSF Page Fragment"; //NOI18N
        docfolder = "web";
        suffix = ".jspf";
        projectfolder = VWPUtilities.WEB_PAGES;
        doMeasurement();
    }

    public void testCreateVWCSSTable(){
        expectedTime = 1000;
        WAIT_AFTER_OPEN=2000;
        docname = "CSSTable"; //NOI18N
        doccategory = "Web"; //NOI18N
        doctype = "Cascading Style Sheet"; //NOI18N
        docfolder = "web" + java.io.File.separatorChar + "resources"; // NOI18N
        suffix = ".css";
        projectfolder = VWPUtilities.WEB_PAGES+"|"+"resources"; // NOI18N
        doMeasurement();
    }

    public ComponentOperator open(){
        location.finish();
        return null; 
    }
    
    @Override
    public void initialize(){
        pto = VWPUtilities.invokePTO();
                
        projectRoot = null;
        try {
            projectRoot = pto.getProjectRootNode(project_name);
            projectRoot.select();

        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }
     }

    public void prepare(){

        // workaround for 143497
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        
        // create exactly (full match) and case sensitively comparing comparator
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
        wizard.lstFileTypes().setComparator(comparator);
        wizard.selectProject(project_name);
        wizard.selectCategory(doccategory);
        wizard.selectFileType(doctype);
        wizard.next();

        location = new NewFileNameLocationStepOperator();
        buildedname = docname+"_"+System.currentTimeMillis();
        location.txtObjectName().setText(buildedname);

        JTextFieldOperator pathField = new JTextFieldOperator(wizard,2);
        pathField.setText(docfolder);
    }

    @Override
    public void close(){
    }
   
    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
        EditorOperator.closeDiscardAll();
    }

}
