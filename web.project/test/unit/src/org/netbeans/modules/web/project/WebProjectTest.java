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

package org.netbeans.modules.web.project;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.project.WebProject.ProjectOpenedHookImpl;
import org.netbeans.modules.web.project.test.TestUtil;
import org.netbeans.modules.web.project.ui.WebLogicalViewProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 * @author Martin Krauskopf, Radko Najman
 */
public class WebProjectTest extends NbTestCase {
    
    private String serverID;
    
    public WebProjectTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
    }
    
    public void testWebProjectIsGCed() throws Exception { // #83128
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
        FileObject projdir = FileUtil.toFileObject(f);
        Project webProject = ProjectManager.getDefault().findProject(projdir);
        WebProjectTest.openProject((WebProject) webProject);
        Node rootNode = ((WebLogicalViewProvider) webProject.getLookup().lookup(WebLogicalViewProvider.class)).createLogicalView();
//.getNodes(true) causes IllegalArgumentException: Called DataObject.find on null
//commenting out till it is fixed
//        rootNode.getChildren().getNodes(true); // ping
        Reference<Project> wr = new WeakReference<Project>(webProject);
        OpenProjects.getDefault().close(new Project[] { webProject });
        WebProjectTest.closeProject((WebProject) webProject);
        rootNode = null;
        webProject = null;
        assertGC("project cannot be garbage collected", wr);
    }
    
    /**
     * Accessor method for those who wish to simulate open of a project and in
     * case of suite for example generate the build.xml.
     */
    public static void openProject(final WebProject p) {
        ProjectOpenedHookImpl hook = (ProjectOpenedHookImpl) p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        hook.projectOpened(); // protected but can use package-private access
    }
    
    public static void closeProject(final WebProject p) {
        ProjectOpenedHookImpl hook = (ProjectOpenedHookImpl) p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        hook.projectClosed(); // protected but can use package-private access
    }
    
}
