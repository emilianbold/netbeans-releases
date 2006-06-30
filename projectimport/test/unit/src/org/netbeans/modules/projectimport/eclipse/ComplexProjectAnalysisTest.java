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

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;
import java.util.Collection;

/**
 * Tests importing of complex project (still without workspace provided). This
 * test should check all features if project analyzer.
 *
 * @author mkrauskopf
 */
public class ComplexProjectAnalysisTest extends ProjectImporterTestCase {

    /**
     * Creates a new instance of ComplexProjectAnalysisTest
     */
    public ComplexProjectAnalysisTest(String name) {
        super(name);
    }

    public void testComplexAloneProjectForLatestMilestone() throws Exception {
        File projectDir = extractToWorkDir("complexAlone-3.1M6.zip");
        EclipseProject project = ProjectFactory.getInstance().load(projectDir);
        assertNotNull(project);
        doProjectTest(project);
    }
    
    private void doProjectTest(EclipseProject project) {
        /* usage (see printOtherProjects to see how to use them) */
        String name = project.getName();
        assertTrue("Name cannot be null or empty", (name != null && !name.equals("")));
        
        File directory = project.getDirectory();
        assertNotNull(directory);
        
        String jdkDir = project.getJDKDirectory();
        //        assertNotNull("Cannot resolve JDK directory \"" + jdkDir + "\"", jdkDir);
        
        Collection srcRoots = project.getSourceRoots();
        assertFalse("Tere should be at least on source root",
                srcRoots.isEmpty());
        
        Collection extSrcRoots = project.getExternalSourceRoots();
        assertTrue("There shouldn't be any external source roots for the project",
                extSrcRoots.isEmpty());
        
        Collection libs = project.getLibraries();
        assertFalse("There are some libraries for the project.", libs.isEmpty());
        
        Collection extLibs = project.getExternalLibraries();
        assertFalse("There are some external libraries for the project",
                extLibs.isEmpty());
        
        Collection variables = project.getVariables();
        assertTrue("There are no variables for the project.", variables.isEmpty());
        
        Collection projects = project.getProjectsEntries();
        assertTrue("There are no required projects for the project.", projects.isEmpty());
        
        /* print data (if verbose is true) */
        printMessage("\n\n\nGathered info:");
        printMessage("  name: " + name);
        printMessage("  dir: " + directory);
        printMessage("  jdkDir: " + jdkDir);
        printCollection("sourceRoots", srcRoots);
        printCollection("externalSourceRoots", extSrcRoots);
        printCollection("libraries", libs);
        printCollection("external libraries", extLibs);
        printCollection("projects", projects);
        printCollection("variables", variables);
    }
}
