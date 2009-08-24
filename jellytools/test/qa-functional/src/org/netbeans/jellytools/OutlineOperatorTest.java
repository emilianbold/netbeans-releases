/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.jellytools;

import javax.swing.tree.TreePath;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.debugger.actions.DebugJavaFileAction;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.OutlineNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 *
 * @author Vojtech.Sigler@sun.com
 */
public class OutlineOperatorTest extends JellyTestCase {

    public OutlineOperatorTest(String isName)
    {
        super(isName);
    }

     /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }

    public static final String[] tests = new String[] {
                "testNodes" };

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        
        return createModuleTest(OutlineOperatorTest.class,
                tests);
    }

    @Override
    public void setUp() throws Exception
    {
        System.out.println("### " + getName() + " ###");
        
        openDataProjects("SampleProject");

        ProjectsTabOperator lrPTO = ProjectsTabOperator.invoke();
        JavaNode lrTestClass = new JavaNode(lrPTO.getProjectRootNode("SampleProject"), "Source Packages|sample1|TestOutline.java");

        lrTestClass.open();

        EditorOperator eo = new EditorOperator("TestOutline.java");
        eo.setCaretPosition(67, 1);

        new ToggleBreakpointAction().perform();
        
        new EventTool().waitNoEvent(500);
        
        String windowMenu = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window");
        String debugMenu = Bundle.getStringTrimmed("org.netbeans.modules.debugger.resources.Bundle", "Menu/Window/Debug");
        String watchesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchesAction");

        (new Action(windowMenu + "|" + debugMenu +"|" + watchesItem,null)).perform();

        String debug = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
        String newWatch = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_New_Watch");

        (new ActionNoBlock(debug + "|" + newWatch, null)).performMenu();

        NbDialogOperator dia = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchDialog_Title"));

        JEditorPaneOperator txtWatch = new JEditorPaneOperator (dia);

        txtWatch.typeText("test");

        dia.ok();

        (new ActionNoBlock(debug + "|" + newWatch, null)).performMenu();

        dia = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchDialog_Title"));

        txtWatch = new JEditorPaneOperator (dia);

        txtWatch.typeText("test");

        dia.ok();

        (new ActionNoBlock(debug + "|" + newWatch, null)).performMenu();

        dia = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchDialog_Title"));

        txtWatch = new JEditorPaneOperator (dia);

        txtWatch.typeText("test");

        dia.ok();

        new DebugJavaFileAction().perform(lrTestClass);


        Thread.sleep(6000);

    }

    public void tearDown()
    {

    }

    public void testNodes() throws Exception
    {
        TopComponentOperator tco = new TopComponentOperator(
                Bundle.getString("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Watches_view"));

        Thread.sleep(5000);

        OutlineOperator lrOVO = new OutlineOperator(tco);

        lrOVO.getRootNode("test").expand();

        lrOVO.getRootNode("test", 1).expand();

        OutlineNode node = lrOVO.getRootNode("test",2);

        node.expand();

        OutlineNode newNode = new OutlineNode(node, "test");

        newNode.callPopup();

                Thread.sleep(5000);

    }
}
