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

package org.netbeans.modules.java.freeform.ui;

import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.modules.java.freeform.JavaProjectNature;
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

    private LogicalViewProvider lvp;

    protected void setUp() throws Exception {
        super.setUp();
        lvp = extsrcroot.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("found a LogicalViewProvider", lvp);
    }
    
    public void testViewItemBasic() throws Exception {
        Node root = lvp.createLogicalView();
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
        LogicalViewProvider lvp2 = simple.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull(lvp2);
        Node root = lvp2.createLogicalView();
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathPositive(lvp2, root, simple, "src/org/foo/myapp/MyApp.java");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathPositive(lvp2, root, simple, "src/org/foo/myapp");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathNegative(lvp2, root, simple, "src/org/foo");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathNegative(lvp2, root, simple, "src/org");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathPositive(lvp2, root, simple, "src");
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestFindPathPositive(lvp2, root, simple, "antsrc/org/foo/ant/SpecialTask.java");
    }
    
    public void testIncludesExcludes() throws Exception {
        org.netbeans.modules.ant.freeform.ui.ViewTest.doTestIncludesExcludes(this, JavaProjectNature.STYLE_PACKAGES,
                "prj{s{ignored{file} relevant.excluded{file} relevant.included{file}}}",
                "prj{s{relevant.included{file}}}",
                "prj{s{ignored{file} relevant.included{file}}}",
                "prj{s{relevant.included{file}}}");
    }

}
