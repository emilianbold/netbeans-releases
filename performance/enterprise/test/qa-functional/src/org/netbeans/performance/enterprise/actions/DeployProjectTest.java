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

package org.netbeans.performance.enterprise.actions;

import junit.framework.Test;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.EPUtilities;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;
/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org, mmirilovic@netbeans.org
 *
 */
public class DeployProjectTest extends PerformanceTestCase {
    
    private static String  project_name = "ReservationPartnerServices";
    private static String TIMEOUT_NAME = "ComponentOperator.WaitStateTimeout";
    private static long timeout;
    
    /** Creates a new instance of DeployProject */
    public DeployProjectTest(String testName) {
        super(testName);
        //TODO: Adjust expectedTime value
        expectedTime = 60000;
        WAIT_AFTER_OPEN=60000;
    }
    
    public DeployProjectTest(String testName, String  performanceDataName) {
        super(testName,performanceDataName);
        //TODO: Adjust expectedTime value
        expectedTime = 60000;
        WAIT_AFTER_OPEN=60000;
    }

    public void testDeployProject() {
        doMeasurement();
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(DeployProjectTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    public void initialize(){
        log(":: initialize");
        Node asNode = new EPUtilities().startApplicationServer();
        
        OutputOperator oot = new OutputOperator();
        timeout = JemmyProperties.getCurrentTimeout(TIMEOUT_NAME);
        JemmyProperties.setCurrentTimeout(TIMEOUT_NAME,300000);
        
        OutputTabOperator asot = oot.getOutputTab(asNode.getText()); // NOI18N
        asot.waitText("Application server startup complete"); // NOI18N
        
//        new CloseAllDocumentsAction().performAPI();
    }
    
    public void prepare() {
        log(":: prepare");
    }
    
    public ComponentOperator open() {
        log("::open");
        
        Node projectNode = new ProjectsTabOperator().getProjectRootNode(project_name);
        projectNode.performPopupActionNoBlock("Deploy"); // NOI18N
        new EventTool().waitNoEvent(60000);
        MainWindowOperator.getDefault().waitStatusText("Finished building "+project_name); // NOI18N
        
        return null;
    }
    
    @Override
    protected void shutdown() {
        log("::shutdown");
        JemmyProperties.setCurrentTimeout(TIMEOUT_NAME,timeout);
    }
    
    @Override
    public void close(){
        log("::close");
        new CloseAllDocumentsAction().performAPI();
    }
    
  
}