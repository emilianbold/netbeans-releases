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

package gui.action;

import gui.VWPUtilities;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 */
public class WebProjectDeployment extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    private String targetProject;
    private Node proj;
    private JPopupMenuOperator projectMenu;
    
    /**
     * Creates a new instance of WebProjectDeployment
     */
    public WebProjectDeployment(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=60000;
    }
    
    public WebProjectDeployment(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=60000;
    }
    
    public void testDeploySmallProject() {
        targetProject = "VisualWebProject";
        doMeasurement();
    }
    
    public void testDeployLargeProject() {
        targetProject = "HugeApp";
        doMeasurement();
    }
    
    public void initialize() {
        log("::initialize");
        VWPUtilities.initLog(this);
        String asName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.j2ee.sun.ide.dm.Bundle", "FACTORY_DISPLAYNAME");
        log("Applications server node name from bundle is: "+asName+" ??"); 
        long start = System.currentTimeMillis();
        
        VWPUtilities.startApplicationServer();
        long stop = System.currentTimeMillis();
        
        log("App server started in "+(stop-start)+" ms");
        
        ProjectsTabOperator.invoke();
        VWPUtilities.verifyAndResolveMissingWebServer(targetProject, asName);
        VWPUtilities.waitForPendingBackgroundTasks();
        
        VWPUtilities.buildproject(targetProject);        
        VWPUtilities.waitForPendingBackgroundTasks();
        
    }
    
    public void prepare() {
        log(":: prepare");
        proj = null;
        try {
            proj = new ProjectsTabOperator().getProjectRootNode(targetProject);
            proj.select();
            
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }
        projectMenu = proj.callPopup();
    }
    
    public ComponentOperator open() {
        log(":: open");
        projectMenu.pushMenuNoBlock(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "LBL_RedeployAction_Name"));
        
        VWPUtilities.waitForPendingBackgroundTasks();
        String statusText = "Finished building " + targetProject + " (run-deploy)."; // NOI18N
        log("Waiting for: '"+statusText+"' text in status bar");  
        MainWindowOperator.getDefault().waitStatusText(statusText); 
        
        return null;
    }
    
    public void close() {
        //TODO Undeploy?
    }
    
    public void shutdown() {
        log(":: shutdown ");
        VWPUtilities.stopApplicationServer();
        VWPUtilities.closeLog();
    }
}
