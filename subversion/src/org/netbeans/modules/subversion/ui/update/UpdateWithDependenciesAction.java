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

package org.netbeans.modules.subversion.ui.update;

import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

import java.util.*;

/**
 * Updates selected projects and all projects they depend on.
 *
 * @author Maros Sandor
 */
public class UpdateWithDependenciesAction extends ContextAction {
    
    private boolean running;

    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_UpdateWithDependencies";    // NOI18N
    }

    protected boolean enable(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            if (SvnUtils.isVersionedProject(node) == false) {
                return false;
            }
        }
        return !running && nodes.length > 0;
    }
    
    protected void performContextAction(final Node[] nodes) {
        running = true;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    updateWithDependencies(nodes);
                } finally {
                    running = false;
                }
            }
        });
    }

    private void updateWithDependencies(Node[] nodes) {
        Set<Project> projectsToUpdate = new HashSet<Project>(nodes.length * 2);
        for (Node node : nodes) {
            Project project =  (Project) node.getLookup().lookup(Project.class);
            projectsToUpdate.add(project);
            SubprojectProvider deps = (SubprojectProvider) project.getLookup().lookup(SubprojectProvider.class);
            Set<? extends Project> children = deps.getSubprojects();
            for (Project child : children) {
                if (SvnUtils.isVersionedProject(child)) {
                    projectsToUpdate.add(child);
                }
            }
        }
        Context context = SvnUtils.getProjectsContext(projectsToUpdate.toArray(new Project[projectsToUpdate.size()]));
        UpdateAction.performUpdate(context);
    }
}
