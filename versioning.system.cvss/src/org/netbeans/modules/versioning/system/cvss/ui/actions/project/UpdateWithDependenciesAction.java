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

package org.netbeans.modules.versioning.system.cvss.ui.actions.project;

import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SubprojectProvider;

import java.io.File;
import java.util.*;

/**
 * Updates given project and all sources of
 * dependee projects that are under CVS version
 * control.
 *
 * @author Petr Kuzel
 */
public final class UpdateWithDependenciesAction extends NodeAction {

    public UpdateWithDependenciesAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    protected void performAction(Node[] nodes) {

        Set projects = new HashSet();
        Set contexts = new LinkedHashSet();

        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            Project project =  (Project) node.getLookup().lookup(Project.class);
            addUpdateContexts(contexts, project, projects);
        }

        ExecutorGroup group = new ExecutorGroup("Updating with dependencies");
        if (contexts.size() > 0) {
            Iterator it = contexts.iterator();
            while (it.hasNext()) {
                Context ctx = (Context) it.next();

                UpdateCommand updateCommand = new UpdateCommand();
                updateCommand.setBuildDirectories(true);
                updateCommand.setPruneDirectories(true);
                updateCommand.setFiles(ctx.getFiles());

                GlobalOptions gtx = new GlobalOptions();
                gtx.setExclusions((File[]) ctx.getExclusions().toArray(new File[0]));
                ExecutorSupport[] execs = UpdateExecutor.createExecutors(updateCommand, CvsVersioningSystem.getInstance(), gtx);
                if (execs != null) {
                    for (int i = 0; i < execs.length; i++) {
                        ExecutorSupport exec = execs[i];
                        group.addExecutor(exec);
                    }
                }
            }
        }

        group.execute();
    }

    protected boolean enable(Node[] nodes) {
        if (nodes.length > 0) {
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (Utils.isVersionedProject(node) == false) {
                    return false;
                }
            }
            return true;
        }
        return false;
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
        List files = new LinkedList();
        List roots = new LinkedList();
        List excludes = new LinkedList();

        // remove nonversioned files
        Utils.addProjectFiles(files, roots, excludes, project);
        Iterator it = files.iterator();
        while (it.hasNext()) {
            File file = (File) it.next();
            File probe = null;
            if (file.isDirectory()) {
                probe = new File(file, "CVS/Repository");  // NOi18N
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
