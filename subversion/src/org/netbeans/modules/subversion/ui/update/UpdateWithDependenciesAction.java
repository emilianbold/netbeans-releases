/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;

/**
 * Updates selected projects and all projects they depend on.
 *
 * @author Maros Sandor
 */
public class UpdateWithDependenciesAction extends ContextAction {
    
    private boolean running;

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
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
        return !running && super.enable(nodes);
    }
    
    protected void performContextAction(final Node[] nodes) {
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
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
            if(deps != null) {
                Set<? extends Project> children = deps.getSubprojects();
                for (Project child : children) {
                    if (SvnUtils.isVersionedProject(child)) {
                        projectsToUpdate.add(child);
                    }
                }
            }
        }
        Context context = SvnUtils.getProjectsContext(projectsToUpdate.toArray(new Project[projectsToUpdate.size()]));
        UpdateAction.performUpdate(context, getContextDisplayName(nodes));
    }
}
