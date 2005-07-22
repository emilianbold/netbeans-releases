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

package org.netbeans.modules.apisupport.project.queries;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.apisupport.project.*;

/**
 * Test AntArtifactProviderImpl.
 * @author Jaroslav Tulach, Jesse Glick
 */
public class AntArtifactProviderImplTest extends TestBase {
    
    public AntArtifactProviderImplTest(String name) {
        super(name);
    }
    
    private NbModuleProject javaProjectProject;
    private NbModuleProject loadersProject;
    
    protected void setUp() throws Exception {
        super.setUp();
        FileObject dir = nbroot.getFileObject("java/project");
        assertNotNull("have java/project checked out", dir);
        javaProjectProject = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
        dir = nbroot.getFileObject("openide/loaders");
        assertNotNull("have openide/loaders checked out", dir);
        loadersProject = (NbModuleProject) ProjectManager.getDefault().findProject(dir);
    }
    
    public void testJARFileIsProduced() throws Exception {
        AntArtifact[] arts = AntArtifactQuery.findArtifactsByType(loadersProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        assertEquals("one artifact produced", 1, arts.length);
        assertEquals("correct project", loadersProject, arts[0].getProject());
        assertEquals("correct type", JavaProjectConstants.ARTIFACT_TYPE_JAR, arts[0].getType());
        assertEquals("correct ID", "module", arts[0].getID());
        assertEquals("correct location",
            Collections.singletonList(URI.create("../../nbbuild/netbeans/platform6/modules/org-openide-loaders.jar")),
            Arrays.asList(arts[0].getArtifactLocations()));
        assertEquals("correct script", nbroot.getFileObject("openide/loaders/build.xml"), arts[0].getScriptFile());
        assertEquals("correct build target", "netbeans", arts[0].getTargetName());
        assertEquals("correct clean target", "clean", arts[0].getCleanTargetName());
        assertEquals("no properties", new Properties(), arts[0].getProperties());
        arts = AntArtifactQuery.findArtifactsByType(javaProjectProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        assertEquals("one artifact produced", 1, arts.length);
        assertEquals("correct location",
            Collections.singletonList(URI.create("../../nbbuild/netbeans/ide6/modules/org-netbeans-modules-java-project.jar")),
            Arrays.asList(arts[0].getArtifactLocations()));
    }
    
}
