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
import org.openide.nodes.Node;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
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

    protected void performAction(Node[] nodes) {
        UpdateCommand cmd = new UpdateCommand();
        cmd.setDisplayName("Updating");
        cmd.setBuildDirectories(true);
        cmd.setPruneDirectories(true);
        File[] files = getFilesToProcess(nodes);
        assert files.length > 0;
        cmd.setFiles(files);

        UpdateExecutor.executeCommand(cmd, CvsVersioningSystem.getInstance(), null);

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

    public String getName() {
        return "Update with Dependencies";
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    private File[] getFilesToProcess(Node[] nodes) {
        Collection files = getActivatedFiles(nodes);
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
        return (File[]) files.toArray(new File[files.size()]);
    }

    private static Collection getActivatedFiles(Node[] nodes) {
        Set files = new LinkedHashSet(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            Project project =  (Project) node.getLookup().lookup(Project.class);
            addProjectFiles(files, project);

        }
        return files;
    }

    private static void addProjectFiles(Collection files, Project project) {
        Utils.addProjectFiles(files, project, ~0);
        SubprojectProvider deps = (SubprojectProvider) project.getLookup().lookup(SubprojectProvider.class);
        Iterator it = deps.getSubprojects().iterator();
        while (it.hasNext()) {
            Project dep = (Project) it.next();
            addProjectFiles(files, dep);  // RESURSION
        }
    }

}
