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

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;
import java.util.Collection;

/**
 * Tests importing of single project (that is without workspace provided).
 *
 * <p>
 * This is first level check if importer is working correctly - i.e. it is able
 * to parse project without <code>ProjectImporterException<code> and similar to
 * be thrown.
 * </p>
 *
 * @author mkrauskopf
 */
public final class SingleProjectAnalysisTest extends ProjectImporterTestCase {
    
    public SingleProjectAnalysisTest(String name) {
        super(name);
    }
    
    public void testSimpleAloneProjectForLatestMilestone() throws Exception {
        File projectDir = extractToWorkDir("simpleAlone-3.1M6.zip");
        EclipseProject project = ProjectFactory.getInstance().load(projectDir);
        assertNotNull(project);
        doBasicProjectTest(project);
        Collection projects = project.getProjectsEntries();
        assertTrue("There are no required projects for the project.", projects.isEmpty());
        printCollection("projects", projects);
    }
    
    public void testEmptyWithoutConAndSrc58033() throws Exception {
        File projectDir = extractToWorkDir("emptyWithoutConAndSrc-3.0.2.zip");
        EclipseProject project = ProjectFactory.getInstance().load(projectDir);
        assertNotNull(project);
    }
    
    static void doBasicProjectTest(EclipseProject project) {
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
        assertTrue("There are no libraries for the project.", libs.isEmpty());
        
        Collection extLibs = project.getExternalLibraries();
        assertTrue("There are no external libraries for the project",
                extLibs.isEmpty());
        
        Collection variables = project.getVariables();
        assertTrue("There are no variables for the project.", variables.isEmpty());
        
        /* print data (if verbose is true) */
        printMessage("\n\n\nGathered info:");
        printMessage("  name: " + name);
        printMessage("  dir: " + directory);
        printMessage("  jdkDir: " + jdkDir);
        printCollection("sourceRoots", srcRoots);
        printCollection("externalSourceRoots", extSrcRoots);
        printCollection("libraries", libs);
        printCollection("external libraries", extLibs);
        printCollection("variables", variables);
    }
}
