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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.railsprojects.ui;

import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.RailsProjectTestBase;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

public class RailsLogicalViewProviderTest extends RailsProjectTestBase {

    public RailsLogicalViewProviderTest(String testName) {
        super(testName);
    }

    public void testFindPath() throws Exception {
        registerLayer();
        RailsProject project = createTestPlainProject();
        LogicalViewProvider lvp = project.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = new FilterNode(lvp.createLogicalView());
        assertNotNull("found config", find(lvp, root, project, "config"));
        assertNotNull("found README", find(lvp, root, project, "README"));
    }
    
    public void testDoubleCreation() throws Exception { // #116678
        registerLayer();
        RailsProject project = createTestPlainProject();
        LogicalViewProvider lvp = project.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        lvp.createLogicalView().getChildren().getNodes(true);
        lvp.createLogicalView().getChildren().getNodes(true);
    }

    private Node find(LogicalViewProvider lvp, Node root, Project project, String path) throws Exception {
        FileObject f = project.getProjectDirectory().getFileObject(path);
        assertNotNull("found " + path, f);
        Node n = lvp.findPath(root, f);
//        DataObject d = DataObject.find(f);
//        assertEquals("same result for DataObject as for FileObject", n, lvp.findPath(root, f));
//        if (n != null) {
//            assertEquals("right DataObject", d,
//                    n.getLookup().lookup(DataObject.class));
//        }
        return n;
    }

}