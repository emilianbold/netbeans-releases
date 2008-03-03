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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.visualweb.test.components.composite;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.modules.visualweb.gravy.Action;
import org.netbeans.modules.visualweb.gravy.EditorOperator;
import org.netbeans.modules.visualweb.gravy.RaveTestCase;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.test.components.util.ComponentUtils;
import org.netbeans.modules.visualweb.test.components.util.PaletteHelper;

import static org.netbeans.modules.visualweb.test.components.util.ComponentUtils.selectForm1Component;
import static org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator.switchToDesignerPane;
import static org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator.switchToJSPSource;

/**
 * @author Martin Schovanek (Martin.Schovanek@sun.com)
 */
public class CompositeComponentsTest extends RaveTestCase {

    private String projectName;
    private boolean projectDeployed;

    public CompositeComponentsTest(String testName) {
        super(testName);
        dumpScreen = false;
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new CompositeComponentsTest("testAccordion"));
        suite.addTest(new CompositeComponentsTest("testBubbleHelp"));
        suite.addTest(new CompositeComponentsTest("testMenu"));
        suite.addTest(new CheckIDELogTest("testCheckIDELog"));
        return suite;
    }

    /** method called before each testcase
     */
    @Override
    protected void setUp() {
        System.out.println(">> Running Test: " + getName() + " >>>>>>>>>>>>>>>");
        // Component Project so create a new Web Project
        if (getName().startsWith("test")) {
            projectName = getName().substring(4) + "TestPrj";
        } else {
            fail("Illegal test name, it must start with 'test' prefix");
        }
        ComponentUtils.createProject(projectName);
        // wait for project creation
        TestUtils.wait(10000);
        ProjectSupport.waitScanFinished();
        projectDeployed = false;
        // Workaround for issue
        switchToJSPSource();
        TestUtils.wait(2000);
        switchToDesignerPane();
    }

    /** method called after each testcase
     */
    @Override
    protected void tearDown() {
        // Close and Undeploy the project
        new SaveAllAction().performAPI(); // Prevents from Save Modified Files dialog.
        Node prjNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        new Action(null, CLOSE).perform(prjNode);
        if (projectDeployed) {
            log("** Undeploing project: " + projectName);
            undeployWebApp(projectName);
            log("** Undeploy done");
        }
        log("<< Finished Test: " + getName() + " <<<<<<<<<<<<<<<<<");
    }

    public void testAccordion() {
        try {
            // Add Accordion component
            PaletteHelper.COMPOSITE.addComponent("Accordion", 50, 50, "id=testAcrd");
            // Add Accordion Tab component
            PaletteHelper.COMPOSITE.addComponent("AccordionTab", 75, 100,
                    "id=testAccrdTab\n" +
                    "contentHeight=133px");
            // Add a Static Text into the tab
            PaletteHelper.BASIC.addComponent("StaticText", 75, 150,
                    "id=stext1\n" +
                    "text=Accordion Tab Text\n" +
                    "toolTip=Accordion Tab Text Tooltip");
            new SaveAllAction().perform();
            // Verify JSP source
            switchToJSPSource();
            assertEditorContains(getEditorOperator("Page1"), new String[]{
                        "<webuijsf:accordion ",
                        "<webuijsf:accordionTab ",
                        "<webuijsf:staticText ",
                        " id=\"testAcrd\"",
                        " id=\"testAccrdTab\"",
                        " id=\"stext1\"",
                        " contentHeight=\"133px\""
                    });
            switchToDesignerPane();
            deployProject(projectName);
        } catch (Exception ex) {
            fail(ex);
        }
    }

    public void testBubbleHelp() {
        // Add Bubble Help component
        PaletteHelper.COMPOSITE.addComponent("Bubble", 50, 50,
                "id=testBubble\n" +
                "title=Bubble Help Test");
        // Add a Static Text into the component
        PaletteHelper.BASIC.addComponent("StaticText", 50 + 36, 50 + 59,
                "id=stext1\n" +
                "text=Bubble Help Text\n" +
                "toolTip=Bubble Help Text Tooltip");
        new SaveAllAction().perform();
        // Verify JSP source
        switchToJSPSource();
        assertEditorContains(getEditorOperator("Page1"), new String[]{
                    "<webuijsf:bubble ",
                    " id=\"testBubble\"",
                    "<webuijsf:staticText ",
                    " id=\"stext1\""
                });
        switchToDesignerPane();
        deployProject(projectName);
    }

    public void testMenu() {
        // Add Menu component
        PaletteHelper.COMPOSITE.addComponent("Menu", 50, 50, null);
        selectForm1Component("menu1");
        SheetTableOperator sheet = new SheetTableOperator();
        sheet.setTextValue("id", "testMenu");
        sheet.setCheckBoxValue("visible", "true");
        new SaveAllAction().perform();
        // Verify JSP source
        switchToJSPSource();
        assertEditorContains(getEditorOperator("Page1"), new String[]{
                    "<webuijsf:menu ",
                    " id=\"testMenu\"",
                    " visible=\"true\""
                });
        switchToDesignerPane();
        deployProject(projectName);
    }

    @Override
    public void log(String msg) {
        super.log(msg);
        System.out.println(msg);
    }

    // TODO: move all this private utility methods into parent or Util clases
    private void waitBuildSuccessful(String projectName) {
        OutputTabOperator console = new OutputTabOperator(projectName);
        console.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 180000);
        console.waitText("BUILD SUCCESSFUL");
    }

    private void undeployWebApp(String app) {
        // XXX strings should come from Bundles
        String path = "Applications|Web Applications|" + app;
        Node webAppNode = new Node(J2eeServerNode.invoke("GlassFish"), path);
        new Action(null, "Undeploy").perform(webAppNode);
        projectDeployed = false;
    }

    private void deployProject(String prj) {
        log("** Deploy from menu");
        // XXX Add UndeploAndDeploy action into Jellytools
        Node prjNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        new Action(null, UNDEPLOY_AND_DEPLOY).perform(prjNode);
        Util.wait(15000);
        waitBuildSuccessful(projectName);
        projectDeployed = true;
    }

    private EditorOperator getEditorOperator(String name) {
        return new EditorOperator(Util.getMainWindow(), name);
    }

    private void assertEditorContains(EditorOperator editor, String[] checkedItems) {
        for (String str : checkedItems) {
            assertTrue("Editor soource does not contain: " + str + "\n\nSOURCE DUMP:\n" + editor.getText(),
                    editor.contains(str));
        }
    }

    private static final String UNDEPLOY_AND_DEPLOY = Bundle.getStringTrimmed(
            "org.netbeans.modules.web.project.ui.Bundle",
            "LBL_RedeployAction_Name");
    private static final String CLOSE = Bundle.getStringTrimmed(
            "org.netbeans.modules.web.project.ui.customizer.Bundle",
            "CTL_WebSourceRootsUi_Close");
}
