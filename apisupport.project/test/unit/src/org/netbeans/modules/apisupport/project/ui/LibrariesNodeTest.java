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

package org.netbeans.modules.apisupport.project.ui;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.nodes.Node;

/**
 * @author Martin Krauskopf
 */
public class LibrariesNodeTest extends TestBase {
    
    public LibrariesNodeTest(String testName) {
        super(testName);
    }
    
    public void testLibrariesNodeListening() throws Exception {
        NbModuleProject p = generateStandaloneModule("module");
        LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        Node libraries = root.getChildren().findChild(LibrariesNode.LIBRARIES_NAME);
        assertNotNull("have the Libraries node", libraries);
        libraries.getChildren().getNodes(); // ping
        
        waitForChildrenUpdate();
        assertEquals("just jdk node is present", 1, libraries.getChildren().getNodes(true).length);
        
        Util.addDependency(p, "org.netbeans.modules.java.project");
        ProjectManager.getDefault().saveProject(p);
        
        waitForChildrenUpdate();
        assertEquals("dependency noticed", 2, libraries.getChildren().getNodes(true).length);
    }
    
    public void testDependencyNodeActions() throws Exception {
        NbModuleProject p = generateStandaloneModule("module");
        LogicalViewProvider lvp = (LogicalViewProvider) p.getLookup().lookup(LogicalViewProvider.class);
        Node root = lvp.createLogicalView();
        Node libraries = root.getChildren().findChild(LibrariesNode.LIBRARIES_NAME);
        
        Util.addDependency(p, "org.netbeans.modules.java.project");
        ProjectManager.getDefault().saveProject(p);
        libraries.getChildren().getNodes(); // ping
        waitForChildrenUpdate();
        Node[] nodes = libraries.getChildren().getNodes(true);
        assertEquals("dependency noticed", 2, nodes.length);
        assertEquals("dependency noticed", 4, nodes[1].getActions(false).length);
    }
    
    private void waitForChildrenUpdate() {
        LibrariesNode.RP.post(new Runnable() {
            public void run() {
                // flush LibrariesNode.RP under which is the Children's update run
            }
        }).waitFinished();
    }
    
    // XXX Much more needed
    
}
