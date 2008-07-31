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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.jellytools;

import java.io.IOException;
import java.util.Properties;
import junit.textui.TestRunner;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Exceptions;

/** Tests org.netbeans.jellytools.OptionsOperator. */
public class OptionsOperatorTest extends JellyTestCase {

    // "IDE Configuration"
    //private static String ideConfLabel;
    // "IDE Configuration|System|File Types|HTML and XHTML files"
    //private static String path1;
    // "IDE Configuration|System|Print Settings"
    //private static String path2;
    // "IDE Configuration|Look and Feel|Toolbars"
    //private static String path3;
    private static Properties props;

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
            TestRunner.run(suite());
    }
    
    public static String[] tests = new String[] {
// test classic view
        /*
        "testTreeTable",
        "testLevelsShowing",
        "testLevelChanging",
        "testPopup",
        "testGetPropertySheet",
        "testSelectOption",
         */
        // test modern view
        "testSwitchToModernView",
        "testSelectEditor",
        "testSelectFontAndColors",
        "testSelectKeymap",
        "testSelectMiscellaneous",
        "testSelectGeneral",
        "testClose"};
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static NbTestSuite suite() {
        /*
        NbTestSuite suite = new NbTestSuite();
        // test classic view
        suite.addTest(new OptionsOperatorTest("testTreeTable"));
        suite.addTest(new OptionsOperatorTest("testLevelsShowing"));
        suite.addTest(new OptionsOperatorTest("testLevelChanging"));
        suite.addTest(new OptionsOperatorTest("testPopup"));
        suite.addTest(new OptionsOperatorTest("testGetPropertySheet"));
        suite.addTest(new OptionsOperatorTest("testSelectOption"));
        // test modern view
        suite.addTest(new OptionsOperatorTest("testSwitchToModernView"));
        suite.addTest(new OptionsOperatorTest("testSelectEditor"));
        suite.addTest(new OptionsOperatorTest("testSelectFontAndColors"));
        suite.addTest(new OptionsOperatorTest("testSelectKeymap"));
        suite.addTest(new OptionsOperatorTest("testSelectMiscellaneous"));
        suite.addTest(new OptionsOperatorTest("testSelectGeneral"));
        suite.addTest(new OptionsOperatorTest("testClose"));
        return suite;
         */
        return (NbTestSuite) createModuleTest(OptionsOperatorTest.class, 
        tests);
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public OptionsOperatorTest(String testName) {
        super(testName);
    }
    
    private static OptionsOperator optionsOperator = null;
    
    /** Setup */
    public void setUp() throws IOException {
            props=new Properties();
            props.load(OptionsOperatorTest.class.getClassLoader().getResourceAsStream("org/netbeans/jellytools/Bundle.properties"));
        System.out.println("### "+getName()+" ###");
    //ideConfLabel =
    //    Bundle.getString("org.netbeans.core.ui.resources.Bundle", "UI/Services/IDEConfiguration");
    //path1 = ideConfLabel+"|"+
        //Bundle.getString("org.netbeans.core.ui.resources.Bundle", "UI/Services/IDEConfiguration/System")+"|"+
    //    props.getProperty("UI/Services/IDEConfiguration/System")+"|"+
    //    Bundle.getString("org.netbeans.core.Bundle", "Services/MIMEResolver")+"|"+
    //    Bundle.getString("org.netbeans.modules.html.Bundle", "Services/MIMEResolver/html.xml");
    //path2 = ideConfLabel+"|"+
    //    Bundle.getStrin   g("org.netbeans.core.ui.resources.Bundle", "UI/Services/IDEConfiguration/System")+"|"+
    //    Bundle.getString("org.netbeans.core.Bundle", "Services/org-openide-text-PrintSettings.settings");
    //path3 = ideConfLabel+"|"+
        //Bundle.getString("org.netbeans.core.ui.resources.Bundle", "UI/Services/IDEConfiguration/LookAndFeel")+"|"+
    //    props.getProperty("org.netbeans.core.ui.resources.Bundle", "UI/Services/IDEConfiguration/LookAndFeel")+"|"+
    //    Bundle.getString("org.netbeans.core.ui.resources.Bundle", "Toolbars");
        // opens Options window
        if(optionsOperator == null) {
            optionsOperator = OptionsOperator.invoke();
        }
    }
    
    /** Tear down. */
    public void tearDown() {
    }
    /*
    public void testTreeTable() {
        optionsOperator.switchToClassicView();
        optionsOperator.selectOption(path1);
        //optionsOperator.selectOption(path2);
        optionsOperator.selectOption(path1);
        //optionsOperator.selectOption(path2);
    }
*/
    public void testLevelsShowing() {
        optionsOperator.hideLevels();
        optionsOperator.showLevels();
        optionsOperator.hideLevels();
        optionsOperator.showLevels();
    }
  /*
    public void testLevelChanging() {
        optionsOperator.setUserLevel(path1);
        //optionsOperator.setUserLevel(path2);
        optionsOperator.setDefaultLevel(path1);
        optionsOperator.setUserLevel(path1);
        //optionsOperator.setDefaultLevel(path2);
        //optionsOperator.setUserLevel(path2);
    }
    */
    public void testPopup() {
        // "Refresh Folder"
        //String refreshFolderLabel = Bundle.getString("org.openide.loaders.Bundle", "LAB_Refresh");
        //new Node(optionsOperator.treeTable().tree(), path3).performPopupAction(refreshFolderLabel);
    }
    
    /** Test getPropertySheet() method. */
    /*
    public void testGetPropertySheet() {
        PropertySheetOperator pso = optionsOperator.getPropertySheet(path1);
    }
    */
    /** Test selectOption() method. */
    /*
    public void testSelectOption() {
        optionsOperator.selectOption(path1);
        String nodeName = optionsOperator.treeTable().tree().getSelectionPath().getLastPathComponent().toString();
        PropertySheetOperator pso = new PropertySheetOperator(optionsOperator);
        assertEquals("Wrong node was selected.", nodeName, pso.getDescriptionHeader());  // NOI18N
    }
    */
    /** Test of switchToModernView method. */
    public void testSwitchToModernView() {
        optionsOperator.switchToModernView();
    }

    /** Test of selectEditor method. */
    public void testSelectEditor() {
        optionsOperator.selectEditor();
    }

    /** Test of selectFontAndColors method. */
    public void testSelectFontAndColors() {
        optionsOperator.selectFontAndColors();
    }

    /** Test of selectKeymap method. */
    public void testSelectKeymap() {
        optionsOperator.selectKeymap();
    }

    /** Test of selectMiscellaneous method. */
    public void testSelectMiscellaneous() {
        optionsOperator.selectMiscellaneous();
    }
    
    /** Test of selectGeneral method.  */
    public void testSelectGeneral() {
        optionsOperator.selectGeneral();
    }
    
    /** Test of close method.  */
    public void testClose() {
        optionsOperator.close();
    }

}
