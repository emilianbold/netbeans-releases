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

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;
import org.netbeans.modules.projectimport.LoggerFactory;
import org.netbeans.modules.projectimport.ProjectImporterException;

/**
 * Able to load and fill up an <code>EclipseProject</code> from Eclipse project
 * directory using a .project and .classpath file and eventually passed
 * workspace. It is also able to load the basic information from workspace.
 *
 * @author mkrauskopf
 */
public final class ProjectFactory {
    
    /** Logger for this class. */
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(ProjectFactory.class);
    
    /** singleton */
    private static ProjectFactory instance = new ProjectFactory();
    
    private ProjectFactory() {/*empty constructor*/}
    
    /** Returns ProjectFactory instance. */
    public static ProjectFactory getInstance() {
        return instance;
    }
    
    /**
     * Loads a project contained in the given <code>projectDir</code> and tries
     * if there is workspace in the parent directory (which works only for
     * eclipse internal projects)
     *
     * @throws ProjectImporterException if project in the given
     *     <code>projectDir</code> is not a valid Eclipse project.
     */
    public EclipseProject load(File projectDir) throws
            ProjectImporterException {
        Workspace workspace = Workspace.createWorkspace(projectDir.getParentFile());
        if (workspace != null) {
            WorkspaceParser parser = new WorkspaceParser(workspace);
            parser.parse();
        }
        return load(projectDir, workspace);
    }
    
    /**
     * Loads a project contained in the given <code>projectDir</code>.
     *
     * @throws ProjectImporterException if project in the given
     *     <code>projectDir</code> is not a valid Eclipse project.
     */
    EclipseProject load(File projectDir, Workspace workspace) throws
            ProjectImporterException {
        
        EclipseProject project = EclipseProject.createProject(projectDir);
        if (project != null) {
            project.setWorkspace(workspace);
            load(project);
        }
        return project;
    }
    
    /**
     * Fullfill given <code>project</code> with all needed information.
     *
     * @throws ProjectImporterException if project in the given
     *     <code>projectDir</code> is not a valid Eclipse project.
     */
    void load(EclipseProject project) throws ProjectImporterException {
        logger.finest("Loading project: " + project.getDirectory().getAbsolutePath()); // NOI18N
        ProjectParser.parse(project);
        File cpFile = project.getClassPathFile();
        // non-java project doesn't need to have a classpath file
        if (cpFile != null && cpFile.exists()) {
            project.setClassPath(ClassPathParser.parse(cpFile));
            for (Iterator it = project.getClassPath().getEntries().iterator(); it.hasNext(); ) {
                project.setAbsolutePathForEntry((ClassPathEntry) it.next());
            }
        } else {
            logger.finer("Project " + project.getName() + // NOI18N
                    " doesn't have java nature."); // NOI18N
        }
    }
}


