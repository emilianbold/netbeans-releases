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

package org.netbeans.modules.versioning.system.cvss.ui.actions.project;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SubprojectProvider;

import java.io.File;
import java.util.*;
import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;

/**
 * Updates given project and all sources of
 * dependee projects that are under CVS version
 * control.
 *
 * @author Petr Kuzel
 */
public final class UpdateWithDependenciesAction extends AbstractSystemAction {

    public UpdateWithDependenciesAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected void performCvsAction(final Node[] nodes) {
        setEnabled(false);
        org.netbeans.modules.versioning.util.Utils.logVCSActionEvent("CVS");
        CvsVersioningSystem.getInstance().getParallelRequestProcessor().post(new Runnable() {
            public void run() {
                async(nodes);
            }
        });

    }

    private void async(Node[] nodes) {

        ExecutorGroup group = new ExecutorGroup(NbBundle.getMessage(UpdateWithDependenciesAction.class, "BK2001"));
        try {
            group.progress(NbBundle.getMessage(UpdateWithDependenciesAction.class, "BK2002"));

            Set projects = new HashSet();
            Set<Context> contexts = new LinkedHashSet<Context>();

            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                Project project =  (Project) node.getLookup().lookup(Project.class);
                addUpdateContexts(contexts, project, projects);
            }

            if (contexts.size() > 0) {
                Context context = new Context(new HashSet(), new HashSet(), new HashSet());
                for (Context cx : contexts) {
                    context = context.union(cx);
                }
                UpdateCommand updateCommand = new UpdateCommand();
                updateCommand.setBuildDirectories(true);
                updateCommand.setPruneDirectories(true);
                updateCommand.setFiles(context.getFiles());

                GlobalOptions gtx = CvsVersioningSystem.createGlobalOptions();
                gtx.setExclusions((File[]) context.getExclusions().toArray(new File[0]));
                group.addExecutors(UpdateExecutor.splitCommand(updateCommand, CvsVersioningSystem.getInstance(), gtx, getContextDisplayName(nodes)));

                group.execute();
            }
        } finally {
            setEnabled(true);
        }
    }

    protected boolean enable(Node[] nodes) {
        boolean enabled = nodes.length > 0;
        if (enabled) {
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (!Utils.isVersionedProject(node, false)) {
                    enabled = false;
                    break;
                }
            }
        }
        return enabled;
    }

    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(UpdateWithDependenciesAction.class, "CTL_MenuItem_UpdateWithDependencies");
    }

    protected String getBaseName(Node[] activatedNodes) {
        return null;    // getName() is overriden, this method is never called
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }


    private static void addUpdateContexts(Collection contexts, Project project, Set updatedProjects) {
        if (updatedProjects.contains(project)) {
            return;
        }
        updatedProjects.add(project);

        Context ctx = createProjectContext(project);
        if (ctx.getFiles().length > 0) {
            contexts.add(ctx);
        }

        SubprojectProvider deps = (SubprojectProvider) project.getLookup().lookup(SubprojectProvider.class);
        if(deps != null) {
            Iterator it = deps.getSubprojects().iterator();
            while (it.hasNext()) {
                Project subProject = (Project) it.next();
                addUpdateContexts(contexts, subProject, updatedProjects);  // RESURSION
            }
        }
    }

    private static Context createProjectContext(Project project) {
        Set files = new HashSet();
        Set roots = new HashSet();
        Set excludes = new HashSet();

        // remove nonversioned files
        Utils.addProjectFiles(files, roots, excludes, project);
        Iterator it = files.iterator();
        while (it.hasNext()) {
            File file = (File) it.next();
            File probe = null;
            if (file.isDirectory()) {
                probe = new File(file, "CVS/Repository");  // NOI18N
            }
            if (file.isFile()) {
                probe = new File(file.getParentFile(), "CVS/Repository");  // NOI18N
            }
            if (probe == null || probe.isFile() == false) {
                it.remove();
            }
        }
        return new Context(files, roots, excludes);
    }

}
