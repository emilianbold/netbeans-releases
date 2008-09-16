/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.apisupport.project.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteCustomizer;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.NotifyDescriptor;
import org.openide.actions.FindAction;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * Defines actions available on a suite.
 * @author Jesse Glick
 */
public final class SuiteActions implements ActionProvider {
    
    static Action[] getProjectActions(SuiteProject project) {
        List<Action> actions = new ArrayList<Action>();
        actions.add(CommonProjectActions.newFileAction());
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_build"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_rebuild"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_clean"), null));
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_run"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_debug"), null));
        actions.addAll(Utilities.actionsForPath("Projects/Profiler_Actions_temporary")); //NOI18N
        actions.add(null);
        NbPlatform platform = project.getPlatform(true); //true -> #96095
        if (platform != null && platform.getHarnessVersion() >= NbPlatform.HARNESS_VERSION_61) {
            actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_test"), null));
            actions.add(null);
        }
        actions.add(ProjectSensitiveActions.projectCommandAction("build-zip", NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_zip"), null));
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction("build-jnlp", NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_build_jnlp"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction("run-jnlp", NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_run_jnlp"), null));
        actions.add(ProjectSensitiveActions.projectCommandAction("debug-jnlp", NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_debug_jnlp"), null));
        actions.add(null);
        if (platform != null && platform.getHarnessVersion() >= NbPlatform.HARNESS_VERSION_55u1) {
            actions.add(ProjectSensitiveActions.projectCommandAction("build-mac", NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_mac"), null));
            actions.add(null);
        }
        if (platform != null && platform.getHarnessVersion() >= NbPlatform.HARNESS_VERSION_50u1) { // #71631
            actions.add(ProjectSensitiveActions.projectCommandAction("nbms", NbBundle.getMessage(SuiteActions.class, "SUITE_ACTION_nbms"), null)); // #64426
            actions.add(null);
        }
        actions.add(CommonProjectActions.setAsMainProjectAction());
        actions.add(CommonProjectActions.openSubprojectsAction());
        actions.add(CommonProjectActions.closeProjectAction());
        actions.add(null);
        actions.add(CommonProjectActions.renameProjectAction());
        actions.add(CommonProjectActions.moveProjectAction());
        actions.add(CommonProjectActions.deleteProjectAction());
        
        actions.add(null);
        actions.add(SystemAction.get(FindAction.class));
        actions.add(null);
        actions.addAll(Utilities.actionsForPath("Projects/Actions")); // NOI18N
        actions.add(null);
        actions.add(CommonProjectActions.customizeProjectAction());
        return actions.toArray(new Action[actions.size()]);
    }
    
    private final SuiteProject project;
    
    public SuiteActions(SuiteProject project) {
        this.project = project;
    }
    
    public String[] getSupportedActions() {
        List<String> actions = new ArrayList<String>(Arrays.asList(
            ActionProvider.COMMAND_BUILD,
            ActionProvider.COMMAND_CLEAN,
            ActionProvider.COMMAND_REBUILD,
            ActionProvider.COMMAND_RUN,
            ActionProvider.COMMAND_DEBUG,
            "build-zip", // NOI18N
            "build-jnlp", // NOI18N
            "run-jnlp", // NOI18N
            "debug-jnlp", // NOI18N
            "build-mac", // NOI18N
            "nbms", // NOI18N
            "profile", // NOI18N
            ActionProvider.COMMAND_RENAME,
            ActionProvider.COMMAND_MOVE,
            ActionProvider.COMMAND_DELETE
        ));
        NbPlatform platform = project.getPlatform(true); //true -> #96095
        if (platform != null && platform.getHarnessVersion() >= NbPlatform.HARNESS_VERSION_61) {
            actions.add(ActionProvider.COMMAND_TEST);
        }
        return actions.toArray(new String[actions.size()]);
    }
    
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if (ActionProvider.COMMAND_DELETE.equals(command) ||
                ActionProvider.COMMAND_RENAME.equals(command) ||
                ActionProvider.COMMAND_MOVE.equals(command)) {
            return true;
        } else if (Arrays.asList(getSupportedActions()).contains(command)) {
            return findBuildXml(project) != null;
        } else {
            throw new IllegalArgumentException(command);
        }
    }
    
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        if (ActionProvider.COMMAND_DELETE.equals(command)) {
            if (SuiteOperations.canRun(project)) {
                DefaultProjectOperations.performDefaultDeleteOperation(project);
            }
        } else if (ActionProvider.COMMAND_RENAME.equals(command)) {
            if (SuiteOperations.canRun(project)) {
                DefaultProjectOperations.performDefaultRenameOperation(project, null);
            }
        } else if (ActionProvider.COMMAND_MOVE.equals(command)) {
            if (SuiteOperations.canRun(project)) {
                DefaultProjectOperations.performDefaultMoveOperation(project);
            }
        } else {
            NbPlatform plaf = project.getPlatform(false);
            if (plaf != null) {
                int v = plaf.getHarnessVersion();
                if (v != NbPlatform.HARNESS_VERSION_UNKNOWN) {
                    for (Project p : project.getLookup().lookup(SubprojectProvider.class).getSubprojects()) {
                        if (v < ((NbModuleProject) p).getMinimumHarnessVersion()) {
                            ModuleActions.promptForNewerHarness();
                            return;
                        }
                    }
                }
            }
            try {
                invokeActionImpl(command, context);
            } catch (IOException e) {
                Util.err.notify(e);
            }
        }
    }
    
    /** Used from tests to start the build script and get task that allows to track its progress.
     * @return null or task that was started
     */
    public ExecutorTask invokeActionImpl(String command, Lookup context) throws IllegalArgumentException, IOException {
        String[] targetNames;
        Properties p = null;

        if (command.equals(ActionProvider.COMMAND_BUILD)) {
            targetNames = new String[] {"build"}; // NOI18N
        } else if (command.equals(ActionProvider.COMMAND_CLEAN)) {
            targetNames = new String[] {"clean"}; // NOI18N
        } else if (command.equals(ActionProvider.COMMAND_REBUILD)) {
            targetNames = new String[] {"clean", "build"}; // NOI18N
        } else if (command.equals(ActionProvider.COMMAND_RUN)) {
            if (project.getTestUserDirLockFile().isFile()) {
                // #141069: lock file exists, run with bogus option
                p = new Properties();
                p.setProperty(ModuleActions.TEST_USERDIR_LOCK_PROP_NAME, ModuleActions.TEST_USERDIR_LOCK_PROP_VALUE);
            }
            targetNames = new String[] {"run"}; // NOI18N
        } else if (command.equals(ActionProvider.COMMAND_DEBUG)) {
            if (project.getTestUserDirLockFile().isFile()) {
                // #141069: lock file exists, run with bogus option
                p = new Properties();
                p.setProperty(ModuleActions.TEST_USERDIR_LOCK_PROP_NAME, ModuleActions.TEST_USERDIR_LOCK_PROP_VALUE);
            }
            targetNames = new String[] {"debug"}; // NOI18N
        } else if (command.equals(ActionProvider.COMMAND_TEST)) {
            targetNames = new String[] {"test"}; // NOI18N
        } else if (command.equals("build-zip")) { // NOI18N
            if (promptForAppName(PROMPT_FOR_APP_NAME_MODE_ZIP)) { // #65006
                return null;
            }
            targetNames = new String[] {"build-zip"}; // NOI18N
        } else if (command.equals("build-jnlp")) { // NOI18N
            if (promptForAppName(PROMPT_FOR_APP_NAME_MODE_JNLP)) {
                return null;
            }
            targetNames = new String[] {"build-jnlp"}; // NOI18N
        } else if (command.equals("run-jnlp")) { // NOI18N
            if (promptForAppName(PROMPT_FOR_APP_NAME_MODE_JNLP)) {
                return null;
            }
            targetNames = new String[] {"run-jnlp"}; // NOI18N
        } else if (command.equals("debug-jnlp")) { // NOI18N
            if (promptForAppName(PROMPT_FOR_APP_NAME_MODE_JNLP)) {
                return null;
            }
            targetNames = new String[] {"debug-jnlp"}; // NOI18N
        } else if (command.equals("build-mac")) { // NOI18N
            targetNames = new String[] {"build-mac"}; // NOI18N
        } else if (command.equals("nbms")) { // NOI18N
            targetNames = new String[] {"nbms"}; // NOI18N
        } else if (command.equals("profile")) { // NOI18N
            targetNames = new String[] {"profile"}; // NOI18N
        } else {
            throw new IllegalArgumentException(command);
        }
        
        return ActionUtils.runTarget(findBuildXml(project), targetNames, p);
    }
    
    private static FileObject findBuildXml(SuiteProject project) {
        return project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
    }
    
    private static final int PROMPT_FOR_APP_NAME_MODE_JNLP = 0;
    private static final int PROMPT_FOR_APP_NAME_MODE_ZIP = 1;
    /** @return true if the dialog is shown */
    private boolean promptForAppName(int mode) {
        String name = project.getEvaluator().getProperty("app.name"); // NOI18N
        if (name != null) {
            return false;
        }
        
        // #61372: warn the user, rather than disabling the action.
        String msg;
        switch (mode) {
            case PROMPT_FOR_APP_NAME_MODE_JNLP:
                msg = NbBundle.getMessage(ModuleActions.class, "ERR_app_name_jnlp");
                break;
            case PROMPT_FOR_APP_NAME_MODE_ZIP:
                msg = NbBundle.getMessage(ModuleActions.class, "ERR_app_name_zip");
                break;
            default:
                throw new AssertionError(mode);
        }
        if (UIUtil.showAcceptCancelDialog(
                NbBundle.getMessage(ModuleActions.class, "TITLE_app_name"),
                msg,
                NbBundle.getMessage(ModuleActions.class, "LBL_configure_app_name"),
                NbBundle.getMessage(ModuleActions.class, "ACSD_configure_app_name"),
                null,
                NotifyDescriptor.WARNING_MESSAGE)) {
            SuiteCustomizer cpi = project.getLookup().lookup(SuiteCustomizer.class);
            cpi.showCustomizer(SuiteCustomizer.APPLICATION, SuiteCustomizer.APPLICATION_CREATE_STANDALONE_APPLICATION);
        }
        return true;
    }
    
}
