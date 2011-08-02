/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven;

import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import static org.netbeans.modules.maven.Bundle.*;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.ModelRunConfig;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.io.xpp3.NetbeansBuildActionXpp3Reader;
import org.netbeans.modules.maven.execute.ui.RunGoalsPanel;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.operations.Operations;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.problems.ProblemReporterImpl;
import org.netbeans.modules.maven.problems.ProblemsPanel;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.ActionConvertor;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.ReplaceTokenProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.execution.ExecutorTask;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author  Milos Kleint
 */
public class ActionProviderImpl implements ActionProvider {

    public static final String BUILD_WITH_DEPENDENCIES = "build-with-dependencies"; // NOI18N

    private final NbMavenProjectImpl project;
    private static String[] supported = new String[]{
        COMMAND_BUILD,
        BUILD_WITH_DEPENDENCIES,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        "javadoc", //NOI18N
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        "debug.fix", //NOI18N

        //operations
        COMMAND_DELETE,
        COMMAND_RENAME,
        COMMAND_MOVE,
        COMMAND_COPY
    };
    
    Lookup.Result<? extends MavenActionsProvider> result;

    /** Creates a new instance of ActionProviderImpl */
    public ActionProviderImpl(NbMavenProjectImpl proj) {
        project = proj;
        result = Lookup.getDefault().lookupResult(MavenActionsProvider.class);
    }

    @Override
    public String[] getSupportedActions() {
        Set<String> supp = new HashSet<String>();
        supp.addAll( Arrays.asList( supported));
        for (MavenActionsProvider add : result.allInstances()) {
            Set<String> added = add.getSupportedDefaultActions();
            if (added != null) {
                supp.addAll( added);
            }
        }
        if (RunUtils.hasTestCompileOnSaveEnabled(project) || (usingSurefire28() && usingJUnit4())) {
            supp.add(SingleMethod.COMMAND_RUN_SINGLE_METHOD);
            supp.add(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD);
        }
        return supp.toArray(new String[0]);
    }

    private boolean usingSurefire28() {
        String v = PluginPropertyUtils.getPluginVersion(project.getOriginalMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE);
        return v != null && new ComparableVersion(v).compareTo(new ComparableVersion("2.8")) >= 0;
    }

    private boolean usingJUnit4() { // SUREFIRE-724
        for (Artifact a : project.getOriginalMavenProject().getArtifacts()) {
            if ("junit".equals(a.getGroupId()) && "junit".equals(a.getArtifactId())) {
                String version = a.getVersion();
                if (version != null && new ComparableVersion(version).compareTo(new ComparableVersion("4.8")) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void invokeAction(final String action, final Lookup lookup) {
        if (COMMAND_DELETE.equals(action)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return;
        }
        if (COMMAND_COPY.equals(action)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return;
        }
        if (COMMAND_MOVE.equals(action)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return;
        }

        if (COMMAND_RENAME.equals(action)) {
            Operations.renameProject(project);
            return;
        }

        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    invokeAction(action, lookup);
                }

            });
            return;
        }
        //TODO if order is important, use the lookupmerger
        Collection<? extends ActionConvertor> convertors = project.getLookup().lookupAll(ActionConvertor.class);
        String convertedAction = null;
        for (ActionConvertor convertor : convertors) {
            convertedAction = convertor.convert(action, lookup);
            if (convertedAction != null) {
                break;
            }
        }
        if (convertedAction == null) {
            convertedAction = action;
        }

        Lookup enhanced = new ProxyLookup(lookup, Lookups.fixed(replacements(convertedAction, lookup)));
        
        RunConfig rc = ActionToGoalUtils.createRunConfig(convertedAction, project, enhanced);
        if (rc == null) {
            Logger.getLogger(ActionProviderImpl.class.getName()).log(Level.INFO, "No handling for action: {0}. Ignoring.", action); //NOI18N

        } else {
            setupTaskName(action, rc, lookup);
            runGoal(rc, true);
        }
    }

    private Map<String,String> replacements(String action, Lookup lookup) {
        Map<String,String> replacements = new HashMap<String,String>();
        for (ReplaceTokenProvider prov : project.getLookup().lookupAll(ReplaceTokenProvider.class)) {
            replacements.putAll(prov.createReplacements(action, lookup));
        }
        return replacements;
    }

    @Messages("TIT_Run_Maven=Run Maven")
    private void runGoal(RunConfig config, boolean checkShowDialog) {
        // check the prerequisites
        for (PrerequisitesChecker elem : config.getProject().getLookup().lookupAll(PrerequisitesChecker.class)) {
            if (!elem.checkRunConfig(config)) {
                return;
            }
            if (config.getPreExecution() != null) {
                if (!elem.checkRunConfig(config.getPreExecution())) {
                    return;
                }
            }
        }



        if (checkShowDialog && MavenSettings.getDefault().isShowRunDialog()) {
            RunGoalsPanel pnl = new RunGoalsPanel();
            DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Run_Maven());
            pnl.readConfig(config);
            Object retValue = DialogDisplayer.getDefault().notify(dd);
            if (retValue == DialogDescriptor.OK_OPTION) {
                BeanRunConfig newConfig = new BeanRunConfig();
                newConfig.setExecutionDirectory(config.getExecutionDirectory());
                newConfig.setExecutionName(config.getExecutionName());
                newConfig.setTaskDisplayName(config.getTaskDisplayName());
                newConfig.setProject(config.getProject());
                pnl.applyValues(newConfig);
                config = newConfig;
            } else {
                return;
            }
        }
        // setup executor now..   
        ExecutorTask task = RunUtils.executeMaven(config);

        // fire project change on when finishing maven execution, to update the classpath etc. -MEVENIDE-83
        task.addTaskListener(new TaskListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void taskFinished(Task task2) {
//reload is done in executors
//                NbMavenProject.fireMavenProjectReload(project);
                RepositoryInfo info = RepositoryPreferences.getInstance().getRepositoryInfoById(RepositoryPreferences.LOCAL_REPO_ID);
                if (info != null) {
                    List<Artifact> arts = new ArrayList<Artifact>();
                    Artifact prjArt = project.getOriginalMavenProject().getArtifact();
                    if (prjArt != null) {
                        arts.add(prjArt);
                    }
                    //#157572
                    Set depArts = project.getOriginalMavenProject().getDependencyArtifacts();
                    if (depArts != null) {
                        arts.addAll(depArts);
                    }
                    RepositoryIndexer.updateIndexWithArtifacts(info, arts);
                }
            }
        });
    }

    @Messages({
        "TXT_Run=Run {0}",
        "TXT_Debug=Debug {0}",
        "TXT_Test=Test {0}",
        "TXT_Build=Build {0}"
    })
    private void setupTaskName(String action, RunConfig config, Lookup lkp) {
        assert config instanceof BeanRunConfig;
        BeanRunConfig bc = (BeanRunConfig) config;
        String title;
        DataObject dobj = lkp.lookup(DataObject.class);
        NbMavenProject prj = bc.getProject().getLookup().lookup(NbMavenProject.class);
        //#118926 prevent NPE, how come the dobj is null?
        String dobjName = dobj != null ? dobj.getName() : ""; //NOI18N

        if (ActionProvider.COMMAND_RUN.equals(action)) {
            title = TXT_Run(prj.getMavenProject().getArtifactId());
        } else if (ActionProvider.COMMAND_DEBUG.equals(action)) {
            title = TXT_Debug(prj.getMavenProject().getArtifactId());
        } else if (ActionProvider.COMMAND_TEST.equals(action)) {
            title = TXT_Test(prj.getMavenProject().getArtifactId());
        } else if (action.startsWith(ActionProvider.COMMAND_RUN_SINGLE)) {
            title = TXT_Run(dobjName);
        } else if (action.startsWith(ActionProvider.COMMAND_DEBUG_SINGLE) || ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(action)) {
            title = TXT_Debug(dobjName);
        } else if (ActionProvider.COMMAND_TEST_SINGLE.equals(action)) {
            title = TXT_Test(dobjName);
        } else {
            title = TXT_Build(prj.getMavenProject().getArtifactId());
        }
        bc.setTaskDisplayName(title);
    }

    @Override
    public boolean isActionEnabled(String action, Lookup lookup) {
        if (COMMAND_DELETE.equals(action) ||
                COMMAND_RENAME.equals(action) ||
                COMMAND_COPY.equals(action) ||
                COMMAND_MOVE.equals(action)) {
            return true;
        }
        //TODO if order is important, use the lookupmerger
        Collection<? extends ActionConvertor> convertors = project.getLookup().lookupAll(ActionConvertor.class);
        String convertedAction = null;
        for (ActionConvertor convertor : convertors) {
            convertedAction = convertor.convert(action, lookup);
            if (convertedAction != null) {
                break;
            }
        }
        if (convertedAction == null) {
            convertedAction = action;
        }
        return ActionToGoalUtils.isActionEnable(convertedAction, project, lookup);
    }

    public Action createCustomMavenAction(String name, NetbeansActionMapping mapping, boolean showUI) {
        return new CustomAction(name, mapping, showUI);
    }

    private final class CustomAction extends AbstractAction {

        private NetbeansActionMapping mapping;
        private boolean showUI;

        private CustomAction(String name, NetbeansActionMapping mapp, boolean showUI) {
            mapping = mapp;
            putValue(Action.NAME, name);
            this.showUI = showUI;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            Map<String,String> replacements = replacements((String) getValue(Action.NAME), /* is there ever a context? */Lookup.EMPTY);
            Properties mappingProperties = mapping.getProperties();
            for (Map.Entry<Object,Object> entry : mappingProperties.entrySet()) {
                mappingProperties.put(entry.getKey(), AbstractMavenActionsProvider.dynamicSubstitutions(replacements, (String) entry.getValue()));
            }

            if (!showUI) {
                ModelRunConfig rc = new ModelRunConfig(project, mapping, mapping.getActionName(), null, Lookup.EMPTY);
                rc.setShowDebug(MavenSettings.getDefault().isShowDebug());
                rc.setTaskDisplayName(TXT_Build(project.getOriginalMavenProject().getArtifactId()));

                setupTaskName("custom", rc, Lookup.EMPTY); //NOI18N
                runGoal(rc, true); //NOI18N

                return;
            }
            RunGoalsPanel pnl = new RunGoalsPanel();
            DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Run_Maven());
            ActionToGoalMapping maps = ActionToGoalUtils.readMappingsFromFileAttributes(project.getProjectDirectory());
            pnl.readMapping(mapping, project, maps);
            pnl.setShowDebug(MavenSettings.getDefault().isShowDebug());
            pnl.setOffline(MavenSettings.getDefault().isOffline() != null ? MavenSettings.getDefault().isOffline() : false);
            pnl.setRecursive(true);
            Object retValue = DialogDisplayer.getDefault().notify(dd);
            if (retValue == DialogDescriptor.OK_OPTION) {
                pnl.applyValues(mapping);
                if (maps.getActions().size() > 10) {
                    maps.getActions().remove(0);
                }
                maps.getActions().add(mapping);
                ActionToGoalUtils.writeMappingsToFileAttributes(project.getProjectDirectory(), maps);
                if (pnl.isRememberedAs() != null) {
                    try {
                        M2ConfigProvider conf = project.getLookup().lookup(M2ConfigProvider.class);
                        ActionToGoalMapping mappings = new NetbeansBuildActionXpp3Reader().read(new StringReader(conf.getDefaultConfig().getRawMappingsAsString()));
                        String tit = "CUSTOM-" + pnl.isRememberedAs(); //NOI18N

                        mapping.setActionName(tit);
                        NetbeansActionMapping exist = null;
                        for (NetbeansActionMapping m : mappings.getActions()) {
                            if (tit.equals(m.getActionName())) {
                                exist = m;
                                break;
                            }
                        }
                        if (exist != null) {
                            mappings.getActions().set(mappings.getActions().indexOf(exist), mapping);
                        } else {
                            mappings.addAction(mapping);
                        }
                        mapping.setDisplayName(pnl.isRememberedAs());
                        //TODO shall we write to configuration based files or not?
                        CustomizerProviderImpl.writeNbActionsModel(project, mappings, M2Configuration.getFileNameExt(M2Configuration.DEFAULT));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                ModelRunConfig rc = new ModelRunConfig(project, mapping, mapping.getActionName(), null, Lookup.EMPTY);
                rc.setOffline(Boolean.valueOf(pnl.isOffline()));
                rc.setShowDebug(pnl.isShowDebug());
                rc.setRecursive(pnl.isRecursive());
                rc.setUpdateSnapshots(pnl.isUpdateSnapshots());
                rc.setTaskDisplayName(TXT_Build(project.getOriginalMavenProject().getArtifactId()));

                setupTaskName("custom", rc, Lookup.EMPTY); //NOI18N
                runGoal(rc, false); //NOI18N

            }
        }
    }

    // XXX should this be an API somewhere?
    private static abstract class ConditionallyShownAction extends AbstractAction implements ContextAwareAction {

        protected ConditionallyShownAction() {
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        public final @Override void actionPerformed(ActionEvent e) {
            assert false;
        }

        protected abstract Action forProject(Project p);

        public final @Override Action createContextAwareInstance(Lookup actionContext) {
            Collection<? extends Project> projects = actionContext.lookupAll(Project.class);
            if (projects.size() != 1) {
                return this;
            }
            Action a = forProject(projects.iterator().next());
            return a != null ? a : this;
        }

    }

    @ActionID(id = "org.netbeans.modules.maven.customPopup", category = "Project")
    @ActionRegistration(displayName = "#LBL_Custom_Run")
    @ActionReference(position = 1400, path = "Projects/org-netbeans-modules-maven/Actions")
    @Messages("LBL_Custom_Run=Custom")
    public static ContextAwareAction customPopupActions() {
        return new ConditionallyShownAction() {
            protected @Override Action forProject(Project p) {
                ActionProviderImpl ap = p.getLookup().lookup(ActionProviderImpl.class);
                return ap != null ? ap.new CustomPopupActions() : null;
            }
        };
    }
    private final class CustomPopupActions extends AbstractAction implements Presenter.Popup {

        private CustomPopupActions() {
            putValue(Action.NAME, LBL_Custom_Run());
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
        }

        
        @Messages({
            "LBL_Loading=Loading...",
            "LBL_Custom_run_goals=Goals..."
        })
        @Override public JMenuItem getPopupPresenter() {

            final JMenu menu = new JMenu(LBL_Custom_Run());
            final JMenuItem loading = new JMenuItem(LBL_Loading());

            menu.add(loading);
            /*using lazy construction strategy*/
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    NetbeansActionMapping[] maps = ActionToGoalUtils.getActiveCustomMappings(project);
                    for (int i = 0; i < maps.length; i++) {
                        NetbeansActionMapping mapp = maps[i];
                        Action act = createCustomMavenAction(mapp.getActionName(), mapp, false);
                        JMenuItem item = new JMenuItem(act);
                        item.setText(mapp.getDisplayName() == null ? mapp.getActionName() : mapp.getDisplayName());
                        menu.add(item);
                    }
                    menu.add(new JMenuItem(createCustomMavenAction(LBL_Custom_run_goals(), new NetbeansActionMapping(), true)));
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            boolean selected = menu.isSelected();
                            menu.remove(loading);
                            menu.getPopupMenu().pack();
                            menu.repaint();
                            menu.updateUI();
                            menu.setSelected(selected);
                        }
                    });
                }
            }, 100);
            return menu;
        }
    }

    @ActionID(id = "org.netbeans.modules.maven.closeSubprojects", category = "Project")
    @ActionRegistration(displayName = "#ACT_CloseRequired")
    @ActionReference(position = 2000, path = "Projects/org-netbeans-modules-maven/Actions")
    @Messages("ACT_CloseRequired=Close Required Projects")
    public static ContextAwareAction closeSubprojectsAction() {
        return new ConditionallyShownAction() {
            protected @Override Action forProject(Project p) {
                NbMavenProjectImpl project = p.getLookup().lookup(NbMavenProjectImpl.class);
                if (project != null && NbMavenProject.TYPE_POM.equalsIgnoreCase(project.getProjectWatcher().getPackagingType())) {
                    return new CloseSubprojectsAction(project);
                } else {
                    return null;
                }
            }
        };
    }
    private static class CloseSubprojectsAction extends AbstractAction {
        private final NbMavenProjectImpl project;
        public CloseSubprojectsAction(NbMavenProjectImpl project) {
            this.project = project;
            putValue(Action.NAME, ACT_CloseRequired());
        }
        public @Override void actionPerformed(ActionEvent e) {
            SubprojectProvider subs = project.getLookup().lookup(SubprojectProvider.class);
            Set<? extends Project> lst = subs.getSubprojects();
            Project[] arr = lst.toArray(new Project[lst.size()]);
            OpenProjects.getDefault().close(arr);
        }
    }

    @ActionID(id = "org.netbeans.modules.maven.showProblems", category = "Project")
    @ActionRegistration(displayName = "#ACT_ShowProblems")
    @ActionReference(position = 3100, path = "Projects/org-netbeans-modules-maven/Actions")
    @Messages("ACT_ShowProblems=Show and Resolve Problems...")
    public static ContextAwareAction showProblemsAction() {
        return new ConditionallyShownAction() {
            protected @Override Action forProject(Project p) {
                ProblemReporterImpl reporter = p.getLookup().lookup(ProblemReporterImpl.class);
                return reporter != null && reporter.isBroken() ? new ShowProblemsAction(reporter) : null;
            }
        };
    }
    private static class ShowProblemsAction extends AbstractAction {
        private final ProblemReporterImpl reporter;
        ShowProblemsAction(ProblemReporterImpl reporter) {
            this.reporter = reporter;
            putValue(Action.NAME, ACT_ShowProblems());
        }
        @Messages({
            "BTN_Close=Close",
            "TIT_Show_Problems=Show Problems"
        })
        @Override public void actionPerformed(ActionEvent arg0) {
            JButton butt = new JButton();
            ProblemsPanel panel = new ProblemsPanel(reporter);
            panel.setActionButton(butt);
            JButton close = new JButton();
            panel.setCloseButton(close);
            close.setText(BTN_Close());
            DialogDescriptor dd = new DialogDescriptor(panel, TIT_Show_Problems());
            dd.setOptions(new Object[] { butt,  close});
            dd.setClosingOptions(new Object[] { close });
            dd.setModal(false);
            DialogDisplayer.getDefault().notify(dd);
        }
    }

    @ActionID(id = "org.netbeans.modules.maven.buildWithDependencies", category = "Project")
    @ActionRegistration(displayName = "#ACT_Build_Deps")
    @ActionReference(position = 500, path = "Projects/org-netbeans-modules-maven/Actions")
    @Messages("ACT_Build_Deps=Build with Dependencies")
    public static ContextAwareAction buildWithDependenciesAction() {
        return (ContextAwareAction) ProjectSensitiveActions.projectCommandAction(BUILD_WITH_DEPENDENCIES, ACT_Build_Deps(), null);
    }

}
