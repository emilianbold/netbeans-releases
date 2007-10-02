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

package org.netbeans.modules.web.project.classpath;

import java.io.File;
import java.net.URL;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.test.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test for {@link WebProjectClassPathModifier}.
 * @author tmysik
 */
public class WebProjectClassPathModifierTest extends NbTestCase {
    
    private FileObject scratch;
    
    public WebProjectClassPathModifierTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        scratch = TestUtil.makeScratchDir(this);
    }

    // #113390
    public void testRemoveRoots() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication1");
        FileObject projdir = FileUtil.toFileObject(f);
        WebProject webProject = (WebProject) ProjectManager.getDefault().findProject(projdir);
        
        Sources sources = ProjectUtils.getSources(webProject);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject srcJava = webProject.getSourceRoots().getRoots()[0];
        assertEquals("We should edit sources", "${src.dir}", groups[0].getName());
        String classPathProperty = webProject.getClassPathProvider().getPropertyName(groups[0], ClassPath.COMPILE);
        
        AntProjectHelper helper = webProject.getAntProjectHelper();
        
        // create src folder
        final String srcFolder = "srcFolder";
        File folder = new File(getDataDir().getAbsolutePath(), srcFolder);
        if (folder.exists()) {
            folder.delete();
        }
        folder.mkdir();
        URL[] cpRoots = new URL[]{folder.toURL()};
        
        // init
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String cpProperty = props.getProperty(classPathProperty);
        boolean alreadyOnCp = cpProperty.indexOf(srcFolder) != -1;
        //assertFalse("srcFolder should not be on cp", alreadyInCp);
        
        // add
        boolean addRoots = ProjectClassPathModifier.addRoots(cpRoots, srcJava, ClassPath.COMPILE);
        // we do not check this - it can be already on cp (tests are created only before the 1st test starts)
        if (!alreadyOnCp) {
            assertTrue(addRoots);
        }
        props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        cpProperty = props.getProperty(classPathProperty);
        assertTrue("srcFolder should be on cp", cpProperty.indexOf(srcFolder) != -1);
        
        // simulate #113390
        folder.delete();
        assertFalse("srcFolder should not exist.", folder.exists());
        
        // remove
        boolean removeRoots = ProjectClassPathModifier.removeRoots(cpRoots, srcJava, ClassPath.COMPILE);
        assertTrue(removeRoots);
        props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        cpProperty = props.getProperty(classPathProperty);
        assertTrue("srcFolder should not be on cp", cpProperty.indexOf(srcFolder) == -1);
    }
}
