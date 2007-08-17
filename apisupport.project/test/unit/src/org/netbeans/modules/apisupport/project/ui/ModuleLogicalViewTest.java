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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 * Test functionality of {@link ModuleLogicalView}.
 * @author Jesse Glick
 */
public class ModuleLogicalViewTest extends TestBase {
    
    public ModuleLogicalViewTest(String name) {
        super(name);
    }
    
    public void testFindPath() throws Exception {
        Project freeform = ProjectManager.getDefault().findProject(FileUtil.toFileObject(file("ant/freeform")));
        assertNotNull("have project in ant/freeform", freeform);
        LogicalViewProvider lvp = freeform.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        assertNotNull("found arch.xml", find(lvp, "ant/freeform/arch.xml"));
        assertNotNull("found FreeformProject.java", find(lvp, "ant/freeform/src/org/netbeans/modules/ant/freeform/FreeformProject.java"));
        assertNotNull("found freeform-project-general.xsd", find(lvp, "ant/freeform/src/org/netbeans/modules/ant/freeform/resources/freeform-project-general.xsd"));
        assertNotNull("found FreeformProjectTest.java", find(lvp, "ant/freeform/test/unit/src/org/netbeans/modules/ant/freeform/FreeformProjectTest.java"));
        assertNull("did not find test/cfg-unit.xml", find(lvp, "ant/freeform/test/cfg-unit.xml"));
        // XXX test that layer.xml is found under Original Files, not Sources
    }
    
    public void testImportantFilesListening() throws Exception {
        Project p = generateStandaloneModule("module");
        LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        Node iFiles = root.getChildren().findChild(ImportantFilesNodeFactory.IMPORTANT_FILES_NAME);
        assertNotNull("have the Important Files node", iFiles);
        iFiles.getChildren().getNodes(true); // ping
        waitForChildrenUpdate();
        assertEquals("five important files", 5, iFiles.getChildren().getNodes(true).length);
        FileUtil.createData(p.getProjectDirectory(), "nbproject/project.properties");
        iFiles.getChildren().getNodes(true); // ping
        waitForChildrenUpdate();
        assertEquals("nbproject/project.properties noticed", 6, iFiles.getChildren().getNodes(true).length);
    }
    
    private Node find(LogicalViewProvider lvp, String path) throws Exception {
        FileObject f = FileUtil.toFileObject(file(path));
        assertNotNull("found " + path, f);
        Node root = new FilterNode(lvp.createLogicalView());
        
        lvp.findPath(root, f); // ping
        waitForChildrenUpdate();
        
        Node n = lvp.findPath(root, f);
        DataObject d = DataObject.find(f);
        assertEquals("same result for DataObject as for FileObject", n, lvp.findPath(root, d));
        if (n != null) {
            assertEquals("right DataObject", d, n.getLookup().lookup(DataObject.class));
        }
        return n;
    }
    
    private void waitForChildrenUpdate() {
        ImportantFilesNodeFactory.RP.post(new Runnable() {
            public void run() {
                // flush ModuleLogicalView.RP under which is the Children's update run
            }
        }).waitFinished();
    }
    
    public void testNewlyCreatedSourceRootsDisplayed() throws Exception { // #72476
        Project p = generateStandaloneModule("module");
        LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
        Node root = lvp.createLogicalView();
        p.getProjectDirectory().getFileObject("test").delete();
        Children ch = root.getChildren();
        assertEquals(Arrays.asList(new String[] {"${src.dir}", "important.files", "libraries"}), findKids(ch));
        /* XXX does not work reliably; ChildrenArray.finalize removes listener!
        final boolean[] added = new boolean[1];
        root.addNodeListener(new NodeAdapter() {
            public void childrenAdded(NodeMemberEvent ev) {
                added[0] = true;
            }
        });
         */
        p.getProjectDirectory().createFolder("javahelp");
        //assertTrue("got node added event", added[0]);
        assertEquals(Arrays.asList(new String[] {"${src.dir}", "javahelp", "important.files", "libraries"}), findKids(ch));
    }
    
    private static List<String> findKids(Children ch) {
        List<String> l = new ArrayList<String>();
        Node[] kids = ch.getNodes(true);
        for (int i = 0; i < kids.length; i++) {
            l.add(kids[i].getName());
        }
        return l;
    }
    
}
