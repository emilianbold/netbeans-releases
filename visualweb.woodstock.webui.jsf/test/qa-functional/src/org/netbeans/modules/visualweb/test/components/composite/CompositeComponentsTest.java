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

import java.awt.Point;
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
import org.netbeans.modules.visualweb.gravy.RaveWindowOperator;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.properties.SheetTableOperator;
import org.netbeans.modules.visualweb.gravy.toolbox.PaletteContainerOperator;
import org.netbeans.modules.visualweb.test.components.util.ComponentUtils;

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
        new SaveAllAction().perform(); // Prevents from Save Modified Files dialog.
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
            String[][] properties;
            // Add Accordion component
            properties = new String[][]{
                {"id", "testAcrd"}
            };
            // XXX add static methods for adding all components
            addComponent(COMPOSITE, ACCORDION, 50, 50, properties);
            // Add Accordion Tab component
            properties = new String[][]{
                {"id", "testAccrdTab"},
                {"contentHeight", "133px"}
            };
            addComponent(COMPOSITE, ACCORDION_TAB, 75, 100, properties);
            // Add a Static Text into the tab
            properties = new String[][]{
                {"id", "stext1"},
                {"text", "Accordion Tab Text"},
                {"toolTip", "Accordion Tab Text Tooltip"}
            };
            addComponent(BASIC, STATIC_TEXT, 75, 150, properties);
            new SaveAllAction().perform();
            // Verify JSP source
            switchToJSPSource();
            EditorOperator editor = getEditorOperator("Page1");
            String[] checkFor = new String[]{
                "<webuijsf:accordion ",
                "<webuijsf:accordionTab ",
                "<webuijsf:staticText ",
                " id=\"testAcrd\"",
                " id=\"testAccrdTab\"",
                " id=\"stext1\"",
                " contentHeight=\"133px\""
            };
            for (String str : checkFor) {
                assertTrue("JSP source does not contain: " + str + "\n\nSOURCE DUMP:\n" + editor.getText(),
                        editor.contains(str));
            }
            switchToDesignerPane();
            deployProject(projectName);
        } catch (Exception ex) {
            fail(ex);
        }
    }

    public void testBubbleHelp() {
        String[][] properties;
        // Add Bubble Help component
        properties = new String[][]{
            {"id", "testBubble"},
            {"title", "Bubble Help Test"}
        };
        addComponent(COMPOSITE, BUBBLE_HELP, 50, 50, properties);
        // Add a Static Text into the component
        properties = new String[][]{
            {"id", "stext1"},
            {"text", "Bubble Help Text"},
            {"toolTip", "Bubble Help Text Tooltip"}
        };
        addComponent(BASIC, STATIC_TEXT, 50+36, 50+59, properties);
        new SaveAllAction().perform();
        // Verify JSP source
        switchToJSPSource();
        EditorOperator editor = getEditorOperator("Page1");
        String[] checkFor = new String[]{
            "<webuijsf:bubble ",
            " id=\"testBubble\"",
            "<webuijsf:staticText ",
            " id=\"stext1\""
        };
        for (String str : checkFor) {
            assertTrue("JSP source does not contain: " + str + "\n\nSOURCE DUMP:\n" + editor.getText(),
                    editor.contains(str));
        }
        switchToDesignerPane();
        deployProject(projectName);
    }

    public void testMenu() {
        String[][] properties;
        // Add Menu component
// XXX
//        properties = new String[][]{
//            {"id", "testMenu"}
//        };
        properties = null;
        addComponent(COMPOSITE, MENU, 50, 50, properties);
        // TODO set visible on true
        //SheetTableOperator sheet = new SheetTableOperator();
        //sheet.setValue("visible", true);
        new SaveAllAction().perform();
        // Verify JSP source
        switchToJSPSource();
        EditorOperator editor = getEditorOperator("Page1");
        String[] checkFor = new String[]{
            "<webuijsf:menu ",
            " id=\"menu1\""
        };
        for (String str : checkFor) {
            assertTrue("JSP source does not contain: " + str + "\n\nSOURCE DUMP:\n" + editor.getText(),
                    editor.contains(str));
        }
        switchToDesignerPane();
        deployProject(projectName);
    }

    @Override
    public void log(String msg) {
        super.log(msg);
        System.out.println(msg);
    }

    private void addComponent(String palette, String component, int x, int y, String[][] properties) {
        log("** Adding component " + palette + " > " + component);
        DesignerPaneOperator designerOp = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        PaletteContainerOperator paleteOp = new PaletteContainerOperator(palette);
        paleteOp.addComponent(component, designerOp, new Point(x, y));
        if (properties != null) {
            SheetTableOperator sheet = new SheetTableOperator();
            for (String[] property : properties) {
                log("** Setting property: " + property[0] + "=" + property[1]);
                sheet.setTextValue(property[0], property[1]);
            }
        }
    }

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
        waitBuildSuccessful(projectName);
        projectDeployed = true;
    }

    private EditorOperator getEditorOperator(String name) {
        return new EditorOperator(Util.getMainWindow(), name);
    }
    private static final String RES_BUNDLE =
            "org.netbeans.modules.visualweb.woodstock.webui.jsf.designtime.resources.Bundle";
    private static final String ACCORDION = Bundle.getStringTrimmed(RES_BUNDLE,
            "NAME_com-sun-webui-jsf-component-Accordion");
    private static final String ACCORDION_TAB = Bundle.getStringTrimmed(RES_BUNDLE,
            "NAME_com-sun-webui-jsf-component-AccordionTab");
    private static final String BASIC = Bundle.getStringTrimmed(RES_BUNDLE,
            "CreatorDesignerPalette5/Basic");
    private static final String BUBBLE_HELP = Bundle.getStringTrimmed(RES_BUNDLE,
            "NAME_com-sun-webui-jsf-component-Bubble");
    private static final String COMPOSITE = Bundle.getStringTrimmed(RES_BUNDLE,
            "CreatorDesignerPalette5/Composite");
    private static final String MENU = Bundle.getStringTrimmed(RES_BUNDLE,
            "NAME_com-sun-webui-jsf-component-Menu");
    private static final String STATIC_TEXT = Bundle.getStringTrimmed(RES_BUNDLE,
            "NAME_com-sun-webui-jsf-component-StaticText");
    private static final String UNDEPLOY_AND_DEPLOY = Bundle.getStringTrimmed(
            "org.netbeans.modules.web.project.ui.Bundle",
            "LBL_RedeployAction_Name");
    private static final String CLOSE = Bundle.getStringTrimmed(
            "org.netbeans.modules.web.project.ui.customizer.Bundle",
            "CTL_WebSourceRootsUi_Close");
}
