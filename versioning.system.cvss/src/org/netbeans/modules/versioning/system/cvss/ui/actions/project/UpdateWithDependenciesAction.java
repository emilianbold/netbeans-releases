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

package org.netbeans.modules.versioning.system.cvss.ui.actions.project;

import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SubprojectProvider;

import java.io.File;
import java.util.*;
import java.awt.event.ActionEvent;

/**
 * Updates given project and all sources of
 * dependee projects that are under CVS version
 * control.
 *
 * @author Petr Kuzel
 */
public final class UpdateWithDependenciesAction extends SystemAction {

    public UpdateWithDependenciesAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public void actionPerformed(ActionEvent ev) {
        setEnabled(false);
        final Node nodes[] = WindowManager.getDefault().getRegistry().getActivatedNodes();
        RequestProcessor.getDefault().post(new Runnable() {
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
                group.addExecutors(UpdateExecutor.splitCommand(updateCommand, CvsVersioningSystem.getInstance(), gtx, org.netbeans.modules.versioning.util.Utils.getContextDisplayName(VCSContext.forNodes(nodes))));

                group.execute();
            }
        } finally {
            setEnabled(true);
        }
    }

    public boolean isEnabled() {
        Node [] nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            if (Utils.isVersionedProject(node) == false) {
                return false;
            }
        }
        return nodes.length > 0;
    }

    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(UpdateWithDependenciesAction.class, "CTL_MenuItem_UpdateWithDependencies");
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
        Iterator it = deps.getSubprojects().iterator();
        while (it.hasNext()) {
            Project subProject = (Project) it.next();
            addUpdateContexts(contexts, subProject, updatedProjects);  // RESURSION
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
