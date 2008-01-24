/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

package org.netbeans.test.j2ee;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author lm97939
 */
public class FreeFormProjects extends JellyTestCase {
    
    /** Creates a new instance of AddMethodTest */
    public FreeFormProjects(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
//        suite.addTest(new FreeFormProjects("testFreeFormEjb"));
        suite.addTest(new FreeFormProjects("testEjbWithSources"));
        suite.addTest(new FreeFormProjects("testEarWithSources"));
        return suite;    
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
    }
    
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
//    public void testFreeFormEjb() {
//        String cmp2ProjectPath = new File(getDataDir(), "freeform_projects/cmp2").getAbsolutePath();
//        createNewFreeFormEjbProject(cmp2ProjectPath, null, 
//                new String[] {"Gangster Entity Bean", "Job Entity Bean", "Organization Entity Bean"},
//                new String[] {"GangsterBean", "JobBean", "OrganizationBean"});
//    }
    
    public void testEjbWithSources() {
        String travelProjectPath = new File(getDataDir(), "freeform_projects/travel").getAbsolutePath();
        createNewEjbProjectFromExistingSources(travelProjectPath, "Travel", travelProjectPath+"-projects",
                new String[] {"TravelAgentEJB", "CabinEJB"},
                new String[] {"TravelAgentBean", "CabinBean"});
    }
    
    public void testEarWithSources() {
        String secureProjectPath = new File(getDataDir(), "freeform_projects/Secure").getAbsolutePath();
        createNewEarProjectFromExistingSources(secureProjectPath, "Secure", secureProjectPath+"-projects");
    }
    
//    private void createNewFreeFormEjbProject(String location, String name, String beans[], String files[]) {
//        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
//        npwo.selectCategory("Enterprise");
//        npwo.selectProject("EJB Module with Existing Ant Script");
//        npwo.next();
//        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
//        new JTextFieldOperator(npnlso,3).setText(location); // NOI18N
//        if (name != null)
//            new JTextFieldOperator(npnlso,1).setText(name);
//        else
//            name = new JTextFieldOperator(npnlso,1).getText();
//        npnlso.next();
//        new NewProjectWizardOperator().next();
//        new NewProjectWizardOperator().next();
//        new NewProjectWizardOperator().next();
//        new NewProjectWizardOperator().finish();
//        //wait project appear in projects view
//        Node rootNode = new ProjectsTabOperator().getProjectRootNode(name);
//        // wait classpath scanning finished
//        ProjectSupport.waitScanFinished();
//        
//        Node beansNode =  new Node(rootNode, Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node"));
//        if (beans != null) {
//            for (int i=0; i<beans.length; i++) {
//                Node node = new Node(beansNode, beans[i]);
//                node.expand();
//                String children[] = node.getChildren();
//                if (children == null || children.length <= 0) {
//                    fail ("Bean node "+beans[i]+" has no children");
//                }
//                if (beans != null) {
//                    new OpenAction().perform(node);
//                    new EditorOperator(files[i]).close();
//                }
//            }
//        }
//        new Node(new ProjectsTabOperator().getProjectRootNode(name),
//                      "Configuration Files|ejb-jar.xml");        
//        // Build project
//        rootNode.performPopupAction(Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Actions/Build"));
//        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 300000);
//        MainWindowOperator.getDefault().waitStatusText(Bundle.getString("org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status", new String[] {name+" (build)"}));
//
//    }
    
    private void createNewEjbProjectFromExistingSources(String location, String name, String folder, String beans[], String files[]) {
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Enterprise");
        npwo.selectProject("EJB Module with Existing Sources");
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        new JTextFieldOperator(npnlso,0).setText(location); // NOI18N
        new JTextFieldOperator(npnlso,1).setText(name); // NOI18N
        new JTextFieldOperator(npnlso,2).setText(folder); // NOI18N
        new NewProjectWizardOperator().next();
        new NewProjectWizardOperator().finish();
        //wait project appear in projects view
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(name);
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
        Node beansNode =  new Node(rootNode, Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node"));
        if (beans != null) {
            for (int i=0; i<beans.length; i++) {
                Node node = new Node(beansNode, beans[i]);
                node.expand();
                String children[] = node.getChildren();
                if (children == null || children.length <= 0) {
                    fail ("Bean node "+beans[i]+" has no children");
                }
                if (beans != null) {
                    new OpenAction().perform(node);
                    new EditorOperator(files[i]).close();
                }
            }
        }
        new Node(new ProjectsTabOperator().getProjectRootNode(name), "Configuration Files|ejb-jar.xml");        
        // Build project
        rootNode.performPopupAction(Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Actions/Build"));
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 300000);
        MainWindowOperator.getDefault().waitStatusText(Bundle.getString("org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status", new String[] {name+" (dist)"}));
    }
    
    private void createNewEarProjectFromExistingSources(String location, String name, String folder) {
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Enterprise");
        npwo.selectProject("Enterprise Application with Existing Sources");
        npwo.next();
        NewProjectNameLocationStepOperator npnlso = new NewProjectNameLocationStepOperator();
        new JTextFieldOperator(npnlso,1).setText(location); // NOI18N
        new JTextFieldOperator(npnlso,0).setText(name); // NOI18N
        new JTextFieldOperator(npnlso,2).setText(folder); // NOI18N
        new NewProjectWizardOperator().btFinish().pushNoBlock();
        new NbDialogOperator("Warning").ok();
        //wait project appear in projects view
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(name);
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
       
        // Build project
        //rootNode.performPopupAction(Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Actions/Build"));
        //MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 300000);
        //MainWindowOperator.getDefault().waitStatusText(Bundle.getString("org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status", new String[] {name+" (build)"}));
    }
    
}
