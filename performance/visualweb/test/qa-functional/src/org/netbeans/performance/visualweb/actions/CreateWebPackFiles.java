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

package org.netbeans.performance.visualweb.actions;

import org.netbeans.performance.visualweb.VWPUtilities;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestSuite;

/**
 * Test create Web Pack projects
 *
 * @author  mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class CreateWebPackFiles extends org.netbeans.modules.performance.utilities.PerformanceTestCase {
   
    private String doccategory, doctype, docname, docfolder, suffix, projectfolder, buildedname;
    private NewFileNameLocationStepOperator location;
    
    private static final String project_name = "VisualWebProject";
    private ProjectsTabOperator pto;
    private Node projectRoot;
    public static final String suiteName="UI Responsiveness VisualWeb Actions suite";
    /**
     * Creates a new instance of CreateWebPackFiles
     * @param testName the name of the test
     */
    public CreateWebPackFiles(String testName) {
        super(testName);
    }
    
    /**
     * Creates a new instance of CreateWebPackFiles
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateWebPackFiles(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateWebPackFiles("testCreateJSPPage","Create JSF Page"));
        suite.addTest(new CreateWebPackFiles("testCreateJSPFragment","Create JSF fragment"));
        suite.addTest(new CreateWebPackFiles("testCreateCSSTable","Create CSS Table"));
        return suite;
    }  
    
    public void testCreateJSPPage(){
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=15000;
        docname = "JSFPage"; //NOI18N
        doccategory = "JavaServer Faces"; //NOI18N
        doctype ="Visual Web JSF Page"; //NOI18N
	docfolder = "web";
	suffix = ".jsp";
        projectfolder = VWPUtilities.WEB_PAGES;
	doMeasurement();
    }

    public void testCreateJSPFragment(){
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=15000;
        docname = "JSFFragment"; //NOI18N
        doccategory = "JavaServer Faces"; //NOI18N
        doctype = "Visual Web JSF Page Fragment"; //NOI18N
	docfolder = "web";
	suffix = ".jspf";
        projectfolder = VWPUtilities.WEB_PAGES;
	doMeasurement();
    }
    
    public void testCreateCSSTable(){
        expectedTime = 1000;
        WAIT_AFTER_OPEN=5000;
	docname = "CSSTable"; //NOI18N
        doccategory = "Web"; //NOI18N
        doctype = "Cascading Style Sheet"; //NOI18N
	docfolder = "web" + java.io.File.separatorChar + "resources"; // NOI18N
	suffix = ".css";
        projectfolder = VWPUtilities.WEB_PAGES+"|"+"resources"; // NOI18N
	doMeasurement();
    }

    public ComponentOperator open(){
        log("::open::");
        location.finish();
	
        return null; //new EditorOperator(docname+"_"+(index)+suffix);
    }
    
    @Override
    public void initialize(){
	log("::initialize::");
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
        log("::prepare::");

        try {
            projectRoot = pto.getProjectRootNode(project_name);
            projectRoot.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }

        
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
            
        
        // create exactly (full match) and case sensitively comparing comparator
        Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
        wizard.lstFileTypes().setComparator(comparator);
        log("Selected Project: "+wizard.getSelectedProject());
        wizard.selectProject(project_name);
        log("Selected Project: "+wizard.getSelectedProject());
        wizard.selectCategory(doccategory);
        wizard.selectFileType(doctype);
	
        wizard.next();

        waitNoEvent(1000);
        location = new NewFileNameLocationStepOperator();
        buildedname = docname+"_"+System.currentTimeMillis();
        location.txtObjectName().setText(buildedname);

	JTextFieldOperator pathField = new JTextFieldOperator(wizard,2);
	pathField.setText(docfolder);
        waitNoEvent(1000);
    }

    @Override
    public void close(){
        log("::close");
        cleanupTest();        
    }
    
    private void cleanupTest() {
        log(":: do cleanup.....");
        long nodeTimeout = pto.getTimeouts().getTimeout("ComponentOperator.WaitStateTimeout");
        long dialogTimeout = JemmyProperties.getCurrentTimeouts().getTimeout("DialogWaiter.WaitDialogTimeout");
        
        pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        JemmyProperties.getCurrentTimeouts().setTimeout("DialogWaiter.WaitDialogTimeout", 60000);
        
        waitNoEvent(2000);
        
        try {
            Node projectRootNode = pto.getProjectRootNode(project_name);
            projectRootNode.select();
            //waitNoEvent(2000);
            Node objNode;
            objNode = new Node(projectRootNode,projectfolder);
            objNode.select();
            objNode = new Node(projectRootNode,projectfolder+"|"+ buildedname+suffix);
            objNode.select();             
            new DeleteAction().performPopup(objNode);
            String dialogCaption = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.visualweb.navigation.Bundle", "MSG_ConfirmDeleteObjectTitle");
            new NbDialogOperator(dialogCaption).yes();
            
        } catch (TimeoutExpiredException timeoutExpiredException) {
            
            log("Cleanup failed because of: "+timeoutExpiredException.getMessage());
            pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",nodeTimeout);
            JemmyProperties.getCurrentTimeouts().setTimeout("DialogWaiter.WaitDialogTimeout",dialogTimeout);
            return;
        }
        pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",nodeTimeout); 
        JemmyProperties.getCurrentTimeouts().setTimeout("DialogWaiter.WaitDialogTimeout",dialogTimeout);
        log(":: cleanup passed");
    }
    
    @Override
    public void shutdown() {
        log("::shutdown");
        super.shutdown();
    }
    
    public static void main(String[] args) {
       junit.textui.TestRunner.run(suite()); 
    }

}
