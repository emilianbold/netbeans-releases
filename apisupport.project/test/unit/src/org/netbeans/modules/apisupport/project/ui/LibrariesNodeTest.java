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

import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 * @author Martin Krauskopf
 */
public class LibrariesNodeTest extends TestBase {
    
    public LibrariesNodeTest(String testName) {
        super(testName);
    }
    
    public void testLibrariesNodeListening() throws Exception {
        NbModuleProject p = generateStandaloneModule("module");
        LogicalViewProvider lvp = (LogicalViewProvider) p.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        Node libraries = root.getChildren().findChild(LibrariesNode.LIBRARIES_NAME);
        assertNotNull("have the Libraries node", libraries);
        libraries.getChildren().getNodes(); // ping
        
        flushProjectMutex();
        assertEquals("just jdk node is present", 1, libraries.getChildren().getNodesCount());
        
        Util.addDependency(p, "org.netbeans.modules.java.project");
        ProjectManager.getDefault().saveProject(p);
        
        flushProjectMutex();
        assertEquals("dependency noticed", 2, libraries.getChildren().getNodesCount());
    }
    
    public void testDependencyNodeActions() throws Exception {
        NbModuleProject p = generateStandaloneModule("module");
        LogicalViewProvider lvp = (LogicalViewProvider) p.getLookup().lookup(LogicalViewProvider.class);
        Node root = lvp.createLogicalView();
        Node libraries = root.getChildren().findChild(LibrariesNode.LIBRARIES_NAME);
        
        Util.addDependency(p, "org.netbeans.modules.java.project");
        ProjectManager.getDefault().saveProject(p);
        libraries.getChildren().getNodes(); // ping
        flushProjectMutex();
        assertEquals("dependency noticed", 2, libraries.getChildren().getNodesCount());
        assertEquals("dependency noticed", 4, libraries.getChildren().getNodes()[1].getActions(false).length);
    }
    
    private void flushProjectMutex() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProjectManager.mutex().writeAccess(new Runnable() {
                    public void run() {
                        // flush
                    }
                });
            }
        }).waitFinished();
    }
    
    // XXX Much more needed
    
}
