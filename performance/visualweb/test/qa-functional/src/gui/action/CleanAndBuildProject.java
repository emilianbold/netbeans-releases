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

import gui.window.WebFormDesignerOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.progress.module.Controller;
import org.netbeans.progress.spi.InternalHandle;
import org.netbeans.progress.spi.TaskModel;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class CleanAndBuildProject extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    private String targetProject;
    private Node proj;
    private JPopupMenuOperator projectMenu;
    private ProjectsTabOperator pto;
    private String[] pagesToOpen;
    
    public CleanAndBuildProject(String testName) {
        super(testName);
        this.expectedTime = 10000;
    }
    public CleanAndBuildProject(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        this.expectedTime = 10000;        
    }
    
    public void testCleanAndBuildSingleOpenedPageProject() {
        targetProject = "UltraLargeWA";
        pagesToOpen = new String[] {"Page1"};
        doMeasurement();
    }
    
    public void testCleanAndBuildMultipleOpenedPagesProject() {
        targetProject = "UltraLargeWA";
        pagesToOpen = new String[] {"Page1", "Page1_1"};
        doMeasurement();
       
    }
    @Override
    public void initialize() {
        log("::initialize");  
        EditorOperator.closeDiscardAll();
        pto = ProjectsTabOperator.invoke();
        long oldTimeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitStateTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 120000);
        for(String namme: pagesToOpen) {
            System.out.println("Opening page "+namme);
            Node docNode = new Node(pto.getProjectRootNode(targetProject),gui.VWPUtilities.WEB_PAGES + "|"+namme+".jsp");
            new OpenAction().performAPI(docNode);                                        
            WebFormDesignerOperator.findWebFormDesignerOperator(namme);
        }
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", oldTimeout);
    }
    
    public void prepare() {
        log(":: prepare");
        invokeProjectCleanAndBuild();
    }

    
    public ComponentOperator open() {
        waitForTask(targetProject+" (clean,dist)");
        return null;
    }

    @Override
    public void shutdown() {
        EditorOperator.closeDiscardAll();        
    }
    private static void waitForTask(String taskName) {
        Controller controller = Controller.getDefault();
        TaskModel model = controller.getModel();
        
        InternalHandle task = waitTaskHandle(model,taskName);
        
        int i=0;
        while(i<12000) { // max 12000*50=600000=10 min
            i++;
            int state = task.getState();
            if(state == InternalHandle.STATE_FINISHED) { return; }
            try {
                Thread.sleep(50);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
                return;
            }
        }
    }
    
    private static InternalHandle waitTaskHandle(TaskModel model, String taskName) {
        for(int i=0; i<12000; i++) { // max 12000*50=600000=10 min
            InternalHandle[] handles =  model.getHandles();
            InternalHandle  serverTask = getTaskHandle(handles,taskName);
            if(serverTask != null) {
                return serverTask;
            }
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException exc) {
                exc.printStackTrace(System.err);
            }
        }
        
        return null;
    }    
    /*
     *  This is copied from Utilities.java and just renamed to getTaskHandle()
     */
    
    private static InternalHandle getTaskHandle(InternalHandle[] handles, String taskName) {
        if(handles.length == 0)  {
            return null;
        }
        
        for (InternalHandle internalHandle : handles) {
            if(internalHandle.getDisplayName().equals(taskName)) {
                return internalHandle;
            }
        }
        return null;
    }

    private void invokeProjectCleanAndBuild() {
        proj = null;
        try {
            proj = new ProjectsTabOperator().getProjectRootNode(targetProject);
            proj.select();
        } catch (org.netbeans.jemmy.TimeoutExpiredException ex) {
            fail("Cannot find and select project root node");
        }
        projectMenu = proj.callPopup();
        projectMenu.pushMenuNoBlock(org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "LBL_RebuildAction_Name"));
        
    }
    
    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() { 
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CleanAndBuildProject("testCleanAndBuildSingleOpenedPageProject"));
        return suite;        
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
