/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectTest;
import org.netbeans.modules.apisupport.project.ui.SuiteLogicalView.ModulesNode.ModuleChildren;
import org.netbeans.modules.apisupport.project.ui.SuiteLogicalView.SuiteRootNode;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.util.Mutex;

/**
 * Test functionality of {@link SuiteLogicalView}.
 *
 * @author Martin Krauskopf
 */
public class SuiteLogicalViewTest extends TestBase {
    
    public SuiteLogicalViewTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        TestBase.initializeBuildProperties(getWorkDir());
    }
    
    public void testModulesNode() throws Exception {
        SuiteProject suite1 = generateSuite("suite1");
        TestBase.generateSuiteComponent(suite1, "module1a");
        Node modulesNode = new SuiteLogicalView.ModulesNode(suite1);
        assertEquals("one children", 1, modulesNode.getChildren().getNodes(true).length);
        
        final ModuleChildren children = (ModuleChildren) modulesNode.getChildren();
        
        TestBase.generateSuiteComponent(suite1, "module1b");
        children.propertiesChanged(null); // #70914
        assertEquals("two children", 2, children.getNodes(true).length);
        
        TestBase.generateSuiteComponent(suite1, "module1c");
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                children.propertiesChanged(null); // #70914
                return null; // #70914
            }
        });
        EventQueue.invokeAndWait(new Runnable() { public void run() {} });
        assertEquals("three children", 3, children.getNodes(true).length);
    }
    
    public void testNameAndDisplayName() throws Exception {
        SuiteProject p = generateSuite("Sweet Stuff");
        Node n = ((LogicalViewProvider) p.getLookup().lookup(LogicalViewProvider.class)).createLogicalView();
        assertEquals("Sweet_Stuff", n.getName());
        assertEquals("Sweet Stuff", n.getDisplayName());
        NL nl = new NL();
        n.addNodeListener(nl);
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("app.name", "sweetness");
        ep.setProperty("app.title", "Sweetness is Now!");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        assertEquals(new HashSet(Arrays.asList(new String[] {Node.PROP_NAME, Node.PROP_DISPLAY_NAME})), nl.changed);
        assertEquals("Sweet_Stuff", n.getName());
        assertEquals("Sweetness is Now!", n.getDisplayName());
    }
    
    public void testProjectFiles() throws Exception {
        SuiteProject suite = generateSuite("suite");
        TestBase.generateSuiteComponent(suite, "module");
        SuiteProjectTest.openSuite(suite);
        SuiteLogicalView.SuiteRootNode rootNode = (SuiteRootNode) ((LogicalViewProvider)
            suite.getLookup().lookup(LogicalViewProvider.class)).createLogicalView();
        Set expected = new HashSet(Arrays.asList(
            new FileObject[] {
                suite.getProjectDirectory().getFileObject("nbproject"),
                suite.getProjectDirectory().getFileObject("build.xml")
            }
        ));
        assertTrue(expected.equals(rootNode.getProjectFiles()));
    }
    
    private static final class NL extends NodeAdapter {
        public final Set/*<String>*/ changed = new HashSet();
        public void propertyChange(PropertyChangeEvent evt) {
            changed.add(evt.getPropertyName());
        }
    }
    
}
