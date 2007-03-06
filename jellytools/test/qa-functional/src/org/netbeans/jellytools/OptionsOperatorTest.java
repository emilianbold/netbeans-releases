/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import junit.textui.TestRunner;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.junit.NbTestSuite;

/** Tests org.netbeans.jellytools.OptionsOperator. */
public class OptionsOperatorTest extends JellyTestCase {

    // "IDE Configuration"
    private static final String ideConfLabel =
        Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration");
    // "IDE Configuration|System|File Types|HTML and XHTML files"
    private static final String path1 = ideConfLabel+"|"+
        Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration/System")+"|"+
        Bundle.getString("org.netbeans.core.Bundle", "Services/MIMEResolver")+"|"+
        Bundle.getString("org.netbeans.modules.html.Bundle", "Services/MIMEResolver/html.xml");
    // "IDE Configuration|System|Print Settings"
    private static final String path2 = ideConfLabel+"|"+
        Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration/System")+"|"+
        Bundle.getString("org.netbeans.core.Bundle", "Services/org-openide-text-PrintSettings.settings");
    // "IDE Configuration|Look and Feel|Toolbars"
    private static final String path3 = ideConfLabel+"|"+
        Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration/LookAndFeel")+"|"+
        Bundle.getString("org.netbeans.core.Bundle", "Toolbars");

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static NbTestSuite suite() {
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
        suite.addTest(new OptionsOperatorTest("testSelectAdvanced"));
        suite.addTest(new OptionsOperatorTest("testSelectGeneral"));
        suite.addTest(new OptionsOperatorTest("testClose"));
        return suite;
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public OptionsOperatorTest(String testName) {
        super(testName);
    }
    
    private static OptionsOperator optionsOperator = null;
    
    /** Setup */
    public void setUp() {
        System.out.println("### "+getName()+" ###");
        // opens Options window
        if(optionsOperator == null) {
            optionsOperator = OptionsOperator.invoke();
        }
    }
    
    /** Tear down. */
    public void tearDown() {
    }
    
    public void testTreeTable() {
        optionsOperator.switchToClassicView();
        optionsOperator.selectOption(path1);
        optionsOperator.selectOption(path2);
        optionsOperator.selectOption(path1);
        optionsOperator.selectOption(path2);
    }

    public void testLevelsShowing() {
        optionsOperator.hideLevels();
        optionsOperator.showLevels();
        optionsOperator.hideLevels();
        optionsOperator.showLevels();
    }
    
    public void testLevelChanging() {
        optionsOperator.setUserLevel(path1);
        optionsOperator.setUserLevel(path2);
        optionsOperator.setDefaultLevel(path1);
        optionsOperator.setUserLevel(path1);
        optionsOperator.setDefaultLevel(path2);
        optionsOperator.setUserLevel(path2);
    }
    
    public void testPopup() {
        // "Refresh Folder"
        String refreshFolderLabel = Bundle.getString("org.openide.loaders.Bundle", "LAB_Refresh");
        new Node(optionsOperator.treeTable().tree(), path3).performPopupAction(refreshFolderLabel);
    }
    
    /** Test getPropertySheet() method. */
    public void testGetPropertySheet() {
        PropertySheetOperator pso = optionsOperator.getPropertySheet(path1);
    }
    
    /** Test selectOption() method. */
    public void testSelectOption() {
        optionsOperator.selectOption(path1);
        String nodeName = optionsOperator.treeTable().tree().getSelectionPath().getLastPathComponent().toString();
        PropertySheetOperator pso = new PropertySheetOperator(optionsOperator);
        assertEquals("Wrong node was selected.", nodeName, pso.getDescriptionHeader());  // NOI18N
    }
    
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

    /** Test of selectAdvanced method. */
    public void testSelectAdvanced() {
        optionsOperator.selectAdvanced();
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
