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

package org.netbeans.modules.apisupport.project.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.actions.FindAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Defines actions available on a suite.
 * @author Jesse Glick
 */
public final class SuiteActions implements ActionProvider {
    
    static Action[] getProjectActions(SuiteProject project) {
        List/*<Action>*/ actions = new ArrayList();
        actions.add(CommonProjectActions.newFileAction());
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_build"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_rebuild"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_clean"), null));
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_run"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_debug"), null));
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction("build-zip", NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_zip"), null));
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction("build-jnlp", NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_build_jnlp"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction("run-jnlp", NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_run_jnlp"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction("debug-jnlp", NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_debug_jnlp"), null));
        actions.add(null);
        actions.add(CommonProjectActions.setAsMainProjectAction());
        actions.add(CommonProjectActions.openSubprojectsAction());
        actions.add(CommonProjectActions.closeProjectAction());
        actions.add(null);
        actions.add(SystemAction.get(FindAction.class));
        /*
        actions.add(null);
        actions.add(CommonProjectActions.deleteProjectAction());
         */
        try {
            FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
            FileObject fo = sfs.findResource("Projects/Actions"); // NOI18N
            if (fo != null) {
                DataObject dobj = DataObject.find(fo);
                FolderLookup actionRegistry = new FolderLookup((DataFolder) dobj);
                Lookup.Template query = new Lookup.Template(Object.class);
                Lookup lookup = actionRegistry.getLookup();
                Iterator it = lookup.lookup(query).allInstances().iterator();
                if (it.hasNext()) {
                    actions.add(null);
                }
                while (it.hasNext()) {
                    Object next = it.next();
                    if (next instanceof Action) {
                        actions.add(next);
                    } else if (next instanceof JSeparator) {
                        actions.add(null);
                    }
                }
            }
        } catch (DataObjectNotFoundException ex) {
            assert false : ex;
        }
        actions.add(null);
        actions.add(SystemAction.get(ToolsAction.class));
        actions.add(null);
        actions.add(CommonProjectActions.customizeProjectAction());
        return (Action[]) actions.toArray(new Action[actions.size()]);
    }
    
    private final SuiteProject project;
    
    public SuiteActions(SuiteProject project) {
        this.project = project;
    }

    public String[] getSupportedActions() {
        return new String[] {
            ActionProvider.COMMAND_BUILD,
            ActionProvider.COMMAND_CLEAN,
            ActionProvider.COMMAND_REBUILD,
            ActionProvider.COMMAND_RUN,
            ActionProvider.COMMAND_DEBUG,
            "build-zip", // NOI18N
            "build-jnlp", // NOI18N
            "run-jnlp", // NOI18N
            "debug-jnlp", // NOI18N
        };
    }
    
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (Arrays.asList(getSupportedActions()).contains(command)) {
            return findBuildXml(project) != null;
        } else {
            throw new IllegalArgumentException(command);
        }
    }

    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        String[] targetNames;
        if (command.equals(ActionProvider.COMMAND_BUILD)) {
            targetNames = new String[] {"build"}; // NOI18N
        } else if (command.equals(ActionProvider.COMMAND_CLEAN)) {
            targetNames = new String[] {"clean"}; // NOI18N
        } else if (command.equals(ActionProvider.COMMAND_REBUILD)) {
            targetNames = new String[] {"clean", "build"}; // NOI18N
        } else if (command.equals(ActionProvider.COMMAND_RUN)) {
            targetNames = new String[] {"run"}; // NOI18N
        } else if (command.equals(ActionProvider.COMMAND_DEBUG)) {
            targetNames = new String[] {"debug"}; // NOI18N
        } else if (command.equals("build-zip")) { // NOI18N
            targetNames = new String[] {"build-zip"}; // NOI18N
        } else if (command.equals("build-jnlp")) { // NOI18N
            targetNames = new String[] {"build-jnlp"}; // NOI18N
        } else if (command.equals("run-jnlp")) { // NOI18N
            targetNames = new String[] {"run-jnlp"}; // NOI18N
        } else if (command.equals("debug-jnlp")) { // NOI18N
            targetNames = new String[] {"debug-jnlp"}; // NOI18N
        } else {
            throw new IllegalArgumentException(command);
        }
        try {
            ActionUtils.runTarget(findBuildXml(project), targetNames, null);
        } catch (IOException e) {
            Util.err.notify(e);
        }
    }
    
    private static FileObject findBuildXml(SuiteProject project) {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
}
