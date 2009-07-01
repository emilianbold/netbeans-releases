/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.


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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author lm97939
 */
public class FreeFormProjects extends J2eeTestCase {

    /** Creates a new instance of AddMethodTest */
    public FreeFormProjects(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(FreeFormProjects.class);
        conf = addServerTests(conf,
                "testEarWithSources",
                "testEjbWithSources");
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
    }

    public void testEjbWithSources() {
        String travelProjectPath = new File(getDataDir(), "freeform_projects/travel").getAbsolutePath();
        createNewEjbProjectFromExistingSources(travelProjectPath, "Travel", travelProjectPath + "-projects",
                new String[]{"TravelAgentEJB", "CabinEJB"},
                new String[]{"TravelAgentBean", "CabinBean"});
    }

    public void testEarWithSources() {
        String secureProjectPath = new File(getDataDir(), "freeform_projects/Secure").getAbsolutePath();
        createNewEarProjectFromExistingSources(secureProjectPath, "Secure", secureProjectPath + "-projects");
    }

    private void createNewEjbProjectFromExistingSources(String location, String name, String folder, String beans[], String files[]) {
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Java EE"); // XXX use Bundle.getString instead
        npwo.selectProject("EJB Module with Existing Sources");
        npwo.next();
        NewWebProjectNameLocationStepOperator npnlso = new NewWebProjectNameLocationStepOperator();
        npnlso.txtLocation().setText(location);
        npnlso.next();
        //server settings panel - accept defaults
        npnlso.next();
        new JButtonOperator(npwo, "Add Folder...", 0).pushNoBlock();
        JFileChooserOperator j = new JFileChooserOperator();
        j.chooseFile("src" + File.separator + "java");
        j.approveSelection();
        npnlso.finish();
        //wait project appear in projects view
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(name);
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
        Node beansNode = new Node(rootNode, Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node"));
        if (beans != null) {
            for (int i = 0; i < beans.length; i++) {
                Node node = new Node(beansNode, beans[i]);
                node.expand();
                String children[] = node.getChildren();
                if (children == null || children.length <= 0) {
                    fail("Bean node " + beans[i] + " has no children");
                }
                if (beans != null) {
                    new OpenAction().perform(node);
                    new EditorOperator(files[i]).close();
                }
            }
        }
        new Node(new ProjectsTabOperator().getProjectRootNode(name), "Configuration Files|ejb-jar.xml");
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.getTimeouts().setTimeout("Waiter.WaitingTime", 300000);
        // Build project
        //rootNode.performPopupAction(Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Actions/Build"));
        rootNode.performPopupAction("Clean and Build");
        mwo.waitStatusText("Finished");
    }

    private void createNewEarProjectFromExistingSources(String location, String name, String folder) {
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Java EE"); // XXX use Bundle.getString instead
        npwo.selectProject("Enterprise Application with Existing Sources");
        npwo.next();
        NewWebProjectNameLocationStepOperator npnlso = new NewWebProjectNameLocationStepOperator();
        npnlso.txtLocation().setText(location);
        npnlso.next();
        new JComboBoxOperator(npnlso, 1).selectItem(1);
        npnlso.next();
        npnlso.btFinish().pushNoBlock();
        new NbDialogOperator("Warning").ok();
        npnlso.waitClosed();
        //wait project appear in projects view
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(name);

        Node n = new Node(rootNode, "Java EE Modules|Secure-war.war");
        n.performPopupAction("Open Project");
        n = new Node(rootNode, "Java EE Modules|Secure-ejb.jar");
        n.performPopupAction("Open Project");
        // wait classpath scanning finished
        ProjectSupport.waitScanFinished();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.getTimeouts().setTimeout("Waiter.WaitingTime", 300000);
        // Build project
        //rootNode.performPopupAction(Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Actions/Build"));
        rootNode.performPopupAction("Clean and Build");
        mwo.waitStatusText("Finished");
    }
}
