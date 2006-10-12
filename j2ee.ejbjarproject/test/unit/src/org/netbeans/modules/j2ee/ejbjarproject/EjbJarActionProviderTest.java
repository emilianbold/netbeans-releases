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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.ejbjarproject.test.TestBase;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

// XXX much more to test

/**
 * Test {@link EjbJarActionProvider}.
 *
 * @author Martin Krauskopf, Andrei Badea
 */
public class EjbJarActionProviderTest extends TestBase {
    
    private Project project;
    private ActionProvider ap;
    
    public EjbJarActionProviderTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        ap = (ActionProvider) project.getLookup().lookup(ActionProvider.class);
        assertNotNull("have ActionProvider", ap);
    }
    
    public void testDebugSingle() throws Exception { // #72733
        FileObject test = project.getProjectDirectory().getFileObject("test/pkg/NewClassTest.java");
        assertNotNull("have test/pkg/NewClassTest.java", test);
        assertTrue("Debug File is enabled on test", ap.isActionEnabled(
                ActionProvider.COMMAND_DEBUG_SINGLE,
                Lookups.singleton(DataObject.find(test))));
        
        // Test removed from suite since it accesses MDR repository and as such
        // it must be executed by ide executor, see issue #82795
        // FileObject source = project.getProjectDirectory().getFileObject("src/java/pkg/NewClass.java");
        // assertNotNull("have src/java/pkg/NewClass.java", source);
        // assertFalse("Debug File is disabled on source file", ap.isActionEnabled(
        //         ActionProvider.COMMAND_DEBUG_SINGLE,
        //         Lookups.singleton(DataObject.find(source))));
    }
    
    public void testCompileSingle() throws Exception { // #79581
        assertFalse("Compile Single is disabled on empty context", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookup.EMPTY));
        assertFalse("Compile Single is disabled on project directory", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(project.getProjectDirectory()))));
        
        FileObject testPackage = project.getProjectDirectory().getFileObject("test/pkg");
        assertNotNull("have test/pkg", testPackage);
        assertTrue("Compile Single is enabled on test package", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(testPackage))));
        FileObject test = project.getProjectDirectory().getFileObject("test/pkg/NewClassTest.java");
        assertNotNull("have test/pkg/NewClassTest.java", test);
        assertTrue("Compile Single is enabled on test", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(test))));

        FileObject srcPackage = project.getProjectDirectory().getFileObject("src/java/pkg");
        assertNotNull("have src/java/pkg", srcPackage);
        assertTrue("Compile Single is enabled on source package", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(srcPackage))));
        FileObject src = project.getProjectDirectory().getFileObject("src/java/pkg/NewClass.java");
        assertNotNull("have src/java/pkg/NewClass.java", src);
        assertTrue("Compile Single is enabled on source", ap.isActionEnabled(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                Lookups.singleton(DataObject.find(src))));
    }
}
