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

package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbTestSuite;


/** Test of org.netbeans.jellytools.actions.Action.
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class ActionTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new ActionTest("testPerformMenu"));
        suite.addTest(new ActionTest("testPerformMenuOnNode"));
        suite.addTest(new ActionTest("testPerformPopup"));
        suite.addTest(new ActionTest("testPerformPopupOnNodes"));
        suite.addTest(new ActionTest("testPerformPopupOnComponent"));
        suite.addTest(new ActionTest("testPerformAPI"));
        suite.addTest(new ActionTest("testPerformAPIOnNodes"));
        suite.addTest(new ActionTest("testPerformShortcut"));
        suite.addTest(new ActionTest("testTestNodesMenu"));
        suite.addTest(new ActionTest("testTestNodesPopup"));
        suite.addTest(new ActionTest("testTestNodesAPI"));
        suite.addTest(new ActionTest("testTestNodesShortcut"));
        return suite;
    }
    
    /** method called before each testcase
     */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");  // NOI18N
    }
    
    /** method called after each testcase
     */
    protected void tearDown() {
        setDefaultMode(Action.POPUP_MODE);
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** simple test case
     */
    public void testPerformMenu() {
        setDefaultMode(Action.API_MODE);
        // open Window|Properties
        new Action(new PropertiesAction().getMenuPath(), null).perform();
        new PropertySheetOperator().close();
    }
    
    /** Test to perform main menu action on node. */
    public void testPerformMenuOnNode() {
        Node n = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java");
        // "Tools"
        String toolsItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "CTL_Tools");
        // "Add to Favorites"
        String addToFavoritesItem = Bundle.getStringTrimmed("org.netbeans.modules.favorites.Bundle", "ACT_Add");
        // "Tools|Add To Favorites"
        try {
            new Action(toolsItem+"|"+addToFavoritesItem, null).perform(n);
        } catch (TimeoutExpiredException e) {
            // Try it once more because sometimes for an uknown reason is Tools menu not fully populated first time.
            // See http://www.netbeans.org/issues/show_bug.cgi?id=85853.
            // push Escape key to ensure there is no open menu
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
            new Action(toolsItem+"|"+addToFavoritesItem, null).perform(n);
        }
        new FavoritesOperator().close();
    }
    
    /** simple test case
     */
    public void testPerformPopup() {
        try {
            new Action(null, "anything").performPopup();
            fail("UnsupportedOperationException not thrown");
        } catch (UnsupportedOperationException e) {
            // it is ok that it throws exception
        }
    }
    
    /** simple test case
     */
    public void testPerformPopupOnNodes() {
        setDefaultMode(Action.MENU_MODE);
        SourcePackagesNode sourceNode = new SourcePackagesNode("SampleProject");
        Node nodes[] = {
            new Node(sourceNode, "sample1|SampleClass1.java"),  // NOI18N
            new Node(sourceNode, "sample1.sample2|SampleClass2.java")   // NOI18N
        };
        // "Open"
        String openItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");// NOI18N
        new Action(null, openItem).perform(nodes);
        new EditorOperator("SampleClass");// NOI18N
    }
    
    /** simple test case
     */
    public void testPerformPopupOnComponent() {
        setDefaultMode(Action.API_MODE);
        EditorOperator op = new EditorOperator("SampleClass");// NOI18N
        // "Code Folds"
        String codeFoldsItem = Bundle.getStringTrimmed("org.netbeans.modules.editor.Bundle", "Menu/View/CodeFolds");
        // "Expand Fold"
        String expandFoldItem = Bundle.getStringTrimmed("org.netbeans.editor.Bundle", "popup-expand-fold");
        // "Code Folds|Expand Folds"
        new Action(null, codeFoldsItem+"|"+expandFoldItem).perform(op);
        op.closeDiscardAll();
    }
    
    /** simple test case
     */
    public void testPerformAPI() {
        RuntimeTabOperator.invoke().close();
        new RuntimeViewAction().performAPI();
        new RuntimeTabOperator();
    }
    
    /** simple test case
     */
    public void testPerformAPIOnNodes() {
        setDefaultMode(Action.POPUP_MODE);
        SourcePackagesNode sourceNode = new SourcePackagesNode("SampleProject");
        Node nodes[] = {
            new Node(sourceNode, "sample1|SampleClass1.java"),  // NOI18N
            new Node(sourceNode, "sample1.sample2|SampleClass2.java")   // NOI18N
        };
        new Action(null, null, "org.openide.actions.PropertiesAction").perform(nodes);// NOI18N
        new PropertySheetOperator().close();
    }
    
    /** simple test case
     */
    public void testPerformShortcut() {
        setDefaultMode(Action.MENU_MODE);
        // open global properties CTRL+Shift+7
        new Action(null, null, null, new Shortcut(KeyEvent.VK_7, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK)).perform();
        new PropertySheetOperator().close();
    }
    
    private static Node[] nodes;
    
    private static Node[] getNodes() {
        if(nodes == null) {
            nodes = new Node[] {
                FilesTabOperator.invoke().getProjectNode("SampleProject"),
                ProjectsTabOperator.invoke().getProjectRootNode("SampleProject"),
                RuntimeTabOperator.invoke().getRootNode()
            };
        }
        return nodes;
    }
    
    /** simple test case */
    public void testTestNodesMenu() {
        Node[] nodes = getNodes();
        try {
            new Action("", "").performMenu(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
        nodes[1]=null;
        try {
            new Action("", "").performMenu(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
        nodes=null;
        try {
            new Action("", "").performMenu(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
        try {
            new Action("", "").performMenu(new Node[0]);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
    }
    
    /** simple test case
     */
    public void testTestNodesPopup() {
        Node[] nodes = getNodes();
        try {
            new Action("", "").performPopup(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
        nodes[1]=null;
        try {
            new Action("", "").performPopup(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
        nodes=null;
        try {
            new Action("", "").performPopup(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
        try {
            new Action("", "").performPopup(new Node[0]);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
    }
    
    /** simple test case
     */
    public void testTestNodesAPI() {
        Node[] nodes = getNodes();
        try {
            new Action("", "", "java.lang.Object").performAPI(nodes);  // NOI18N
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
        nodes[1]=null;
        try {
            new Action("", "", "java.lang.Object").performAPI(nodes);  // NOI18N
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
        nodes=null;
        try {
            new Action("", "", "java.lang.Object").performAPI(nodes);  // NOI18N
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
        try {
            new Action("", "", "java.lang.Object").performAPI(new Node[0]);  // NOI18N
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
    }
    
    /** simple test case
     */
    public void testTestNodesShortcut() {
        Node[] nodes = getNodes();
        try {
            new Action("", "", new Shortcut(0)).performShortcut(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
        nodes[1]=null;
        try {
            new Action("", "", new Shortcut(0)).performShortcut(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
        nodes=null;
        try {
            new Action("", "", new Shortcut(0)).performShortcut(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
        try {
            new Action("", "", new Shortcut(0)).performShortcut(new Node[0]);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {}
    }
    
    private void setDefaultMode(int mode) {
        JemmyProperties.setCurrentProperty("Action.DefaultMode", new Integer(mode));  // NOI18N
    }
    
}
