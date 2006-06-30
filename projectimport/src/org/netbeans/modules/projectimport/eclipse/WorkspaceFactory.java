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
import org.netbeans.modules.projectimport.ProjectImporterException;
import org.openide.filesystems.FileUtil;

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
            return load(FileUtil.normalizeFile(new File(workspaceDir)));
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
