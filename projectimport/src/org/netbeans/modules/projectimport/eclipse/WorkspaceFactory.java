/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;
import org.netbeans.modules.projectimport.ProjectImporterException;

/**
 * Able to load and fill up an <code>EclipseWorkspace</code> from an Eclipse
 * workspace directory using a .workspace and .classpath file and eventually
 * passed workspace. It is also able to load a basic information from workspace.
 *
 * @author mkrauskopf
 */
public final class WorkspaceFactory {
    
    /** singleton */
    private static WorkspaceFactory instance = new WorkspaceFactory();
    
    private WorkspaceFactory() {/*empty constructor*/}
    
    public static WorkspaceFactory getInstance() {
        return instance;
    }
    
    /**
     * Loads a workspace contained in the given <code>workspaceDir</code>.
     *
     * @throws InvalidWorkspaceException if workspace in the given
     *     <code>workspaceDir</code> is not a valid Eclipse workspace.
     */
    public Workspace load(String workspaceDir) throws ProjectImporterException {
        if (workspaceDir != null) {
            return load(new File(workspaceDir));
        }
        return null;
    }
    
    /**
     * Loads a workspace contained in the given <code>workspaceDir</code>.
     *
     * @throws InvalidWorkspaceException if workspace in the given
     *     <code>workspaceDir</code> is not a valid Eclipse workspace.
     */
    public Workspace load(File workspaceDir) throws ProjectImporterException {
        Workspace workspace = Workspace.createWorkspace(workspaceDir);
        if (workspace != null) {
            WorkspaceParser parser = new WorkspaceParser(workspace);
            parser.parse();
        }
        return workspace;
    }
}
