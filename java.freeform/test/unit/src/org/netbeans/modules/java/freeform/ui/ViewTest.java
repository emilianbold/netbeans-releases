/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.freeform.ui;

import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Test just style="packages".
 * @author Jesse Glick
 */
public class ViewTest extends TestBase {
    
    public ViewTest(String name) {
        super(name);
    }
    
    private LogicalViewProvider lpp;
    
    protected void setUp() throws Exception {
        super.setUp();
        lpp = (LogicalViewProvider) extsrcroot.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("found a LogicalViewProvider", lpp);
    }
    
    public void testViewItemBasic() throws Exception {
        Node root = lpp.createLogicalView();
        Children ch = root.getChildren();
        Node[] kids = ch.getNodes(true);
        assertEquals("two child nodes", 2, kids.length);
        assertEquals("correct code name #1", "../src", kids[0].getName());
        assertEquals("correct display name #1", "External Sources", kids[0].getDisplayName());
        assertEquals("correct cookie #1",
            DataObject.find(egdirFO.getFileObject("extsrcroot/src")),
            kids[0].getLookup().lookup(DataObject.class));
        Node[] kids2 = kids[0].getChildren().getNodes(true);
        assertEquals("one child of ../src", 1, kids2.length);
        assertEquals("correct name of #1's kid", "org.foo", kids2[0].getName());
        // Do not test node #2; supplied by ant/freeform.
    }
    
    public void testFindPath() throws Exception {
        LogicalViewProvider lpp2 = (LogicalViewProvider) simple.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull(lpp2);
        Node root = lpp2.createLogicalView();
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathPositive(lpp2, root, simple, "src/org/foo/myapp/MyApp.java");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathPositive(lpp2, root, simple, "src/org/foo/myapp");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathNegative(lpp2, root, simple, "src/org/foo");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathNegative(lpp2, root, simple, "src/org");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathPositive(lpp2, root, simple, "src");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathPositive(lpp2, root, simple, "antsrc/org/foo/ant/SpecialTask.java");
    }
    
}
