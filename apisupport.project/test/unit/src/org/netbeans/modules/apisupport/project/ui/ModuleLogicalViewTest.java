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
import org.openide.util.RequestProcessor;

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
        LogicalViewProvider lvp = (LogicalViewProvider) freeform.getLookup().lookup(LogicalViewProvider.class);
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
        LogicalViewProvider lvp = (LogicalViewProvider) p.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("have a LogicalViewProvider", lvp);
        Node root = lvp.createLogicalView();
        Node iFiles = root.getChildren().findChild(ModuleLogicalView.IMPORTANT_FILES_NAME);
        assertNotNull("have the Important Files node", iFiles);
        iFiles.getChildren().getNodes(true); // ping
        flushRequestProcessor();
        assertEquals("four important files", 4, iFiles.getChildren().getNodes(true).length);
        FileUtil.createData(p.getProjectDirectory(), "nbproject/project.properties");
        iFiles.getChildren().getNodes(true); // ping
        flushRequestProcessor();
        assertEquals("nbproject/project.properties noticed", 5, iFiles.getChildren().getNodes(true).length);
    }
    
    private Node find(LogicalViewProvider lvp, String path) throws Exception {
        FileObject f = FileUtil.toFileObject(file(path));
        assertNotNull("found " + path, f);
        Node root = new FilterNode(lvp.createLogicalView());
        
        lvp.findPath(root, f); // ping
        flushRequestProcessor();
        
        Node n = lvp.findPath(root, f);
        DataObject d = DataObject.find(f);
        assertEquals("same result for DataObject as for FileObject", n, lvp.findPath(root, d));
        if (n != null) {
            assertEquals("right DataObject", d, n.getLookup().lookup(DataObject.class));
        }
        return n;
    }
    
    private void flushRequestProcessor() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                // flush
            }
        }).waitFinished();
    }
    
    public void testNewlyCreatedSourceRootsDisplayed() throws Exception { // #72476
        Project p = generateStandaloneModule("module");
        LogicalViewProvider lvp = (LogicalViewProvider) p.getLookup().lookup(LogicalViewProvider.class);
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
    
    private static List/*<String>*/ findKids(Children ch) {
        List l = new ArrayList();
        Node[] kids = ch.getNodes(true);
        for (int i = 0; i < kids.length; i++) {
            l.add(kids[i].getName());
        }
        return l;
    }
    
}
