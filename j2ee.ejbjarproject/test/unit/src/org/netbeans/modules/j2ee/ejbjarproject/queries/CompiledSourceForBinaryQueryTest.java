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

package org.netbeans.modules.j2ee.ejbjarproject.queries;

import java.io.File;
import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;
/**
 *
 * @author Andrei Badea
 */
public class CompiledSourceForBinaryQueryTest extends NbTestCase {
    
    private Project project;
    private AntProjectHelper helper;
    
    public CompiledSourceForBinaryQueryTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
        File f = new File(getDataDir().getAbsolutePath(), "projects/EJBModule1");
        project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
        // XXX should not cast a Project
        helper = ((EjbJarProject)project).getAntProjectHelper();
    }
    
    public void testSourceRootsFoundForNonExistingBinaryRootIssue65733() throws Exception {
        File buildClassesDir  = helper.resolveFile(helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.BUILD_CLASSES_DIR));
        // the file must not exist
        assertFalse("Cannot test, the project should be cleaned first!", buildClassesDir .exists());
        URL buildClassesDirURL = new URL(buildClassesDir.toURL().toExternalForm() + "/");
        SourceForBinaryQueryImplementation s4bqi = (SourceForBinaryQueryImplementation)project.getLookup().lookup(SourceForBinaryQueryImplementation.class);
        assertNotNull(s4bqi.findSourceRoots(buildClassesDirURL));
    }
}
