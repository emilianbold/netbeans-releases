/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import static org.netbeans.modules.maven.Bundle.*;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.execute.ActionToGoalUtils;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.ModelRunConfig;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.ui.RunGoalsPanel;
import org.netbeans.modules.maven.operations.Operations;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.ActionConvertor;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.ReplaceTokenProvider;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.StatusDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
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
@ProjectServiceProvider(service={ActionProvider.class, ActionProviderImpl.class}, projectType="org-netbeans-modules-maven")
public class ActionProviderImpl implements ActionProvider {

    public static final String BUILD_WITH_DEPENDENCIES = "build-with-dependencies"; // NOI18N

    private final Project proj;
    private static final String[] supported = new String[]{
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
        COMMAND_PROFILE,
        COMMAND_PROFILE_SINGLE,
        COMMAND_PROFILE_TEST_SINGLE,
        
        //operations
        COMMAND_DELETE,
        COMMAND_RENAME,
        COMMAND_MOVE,
        COMMAND_COPY
    };
    
    private static final RequestProcessor RP = new RequestProcessor(ActionProviderImpl.class.getName(), 3);

    public ActionProviderImpl(Project proj) {
        this.proj = proj;
    }

    @Override
    public String[] getSupportedActions() {
        Set<String> supp = new HashSet<String>();
        supp.addAll( Arrays.asList( supported));
        for (MavenActionsProvider add : ActionToGoalUtils.actionProviders(proj)) {
            Set<String> added = add.getSupportedDefaultActions();
            if (added != null) {
                supp.addAll( added);
            }
        }
        supp.add(SingleMethod.COMMAND_RUN_SINGLE_METHOD);
        supp.add(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD);
        return supp.toArray(new String[0]);
    }

    private boolean usingSurefire28() {
        String v = PluginPropertyUtils.getPluginVersion(proj.getLookup().lookup(NbMavenProject.class).getMavenProject(), Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE);
        return v != null && new ComparableVersion(v).compareTo(new ComparableVersion("2.8")) >= 0;
    }

    private boolean usingJUnit4() { // SUREFIRE-724
        for (Artifact a : proj.getLookup().lookup(NbMavenProject.class).getMavenProject().getArtifacts()) {
            if ("junit".equals(a.getGroupId()) && ("junit".equals(a.getArtifactId()) || "junit-dep".equals(a.getArtifactId()))) { //junit-dep  see #214238
                String version = a.getVersion();
                if (version != null && new ComparableVersion(version).compareTo(new ComparableVersion("4.8")) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean usingTestNG() {
        for (Artifact a : proj.getLookup().lookup(NbMavenProject.class).getMavenProject().getArtifacts()) {
            if ("org.testng".equals(a.getGroupId()) && "testng".equals(a.getArtifactId())) {
                return true;
            }
        }
        return false;
    }

    boolean runSingleMethodEnabled() {
        return usingSurefire28() && (usingJUnit4() || usingTestNG());
    }

    @Messages("run_single_method_disabled=Surefire 2.8+ with JUnit 4.8+ or TestNG needed to run a single test method.")
    @Override public void invokeAction(final String action, final Lookup lookup) {
        if (action.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD) || action.equals(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD)) {
            if (!runSingleMethodEnabled()) {
                //TODO show a popup dialog with X Show Next time?
                StatusDisplayer.getDefault().setStatusText(run_single_method_disabled());
                return;
            }
        }
        if (COMMAND_DELETE.equals(action)) {
            DefaultProjectOperations.performDefaultDeleteOperation(proj);
            return;
        }
        if (COMMAND_COPY.equals(action)) {
            DefaultProjectOperations.performDefaultCopyOperation(proj);
            return;
        }
        if (COMMAND_MOVE.equals(action)) {
            DefaultProjectOperations.performDefaultMoveOperation(proj);
            return;
        }

        if (COMMAND_RENAME.equals(action)) {
            Operations.renameProject(proj.getLookup().lookup(NbMavenProjectImpl.class));
            return;
        }

        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    invokeAction(action, lookup);
                }

            });
            return;
        }
        //TODO if order is important, use the lookupmerger
        Collection<? extends ActionConvertor> convertors = proj.getLookup().lookupAll(ActionConvertor.class);
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

        Lookup enhanced = new ProxyLookup(lookup, Lookups.fixed(replacements(proj, convertedAction, lookup)));
        
        RunConfig rc = ActionToGoalUtils.createRunConfig(convertedAction, proj.getLookup().lookup(NbMavenProjectImpl.class), enhanced);
        if (rc == null) {
            Logger.getLogger(ActionProviderImpl.class.getName()).log(Level.INFO, "No handling for action: {0}. Ignoring.", action); //NOI18N

        } else {
            setupTaskName(action, rc, lookup);
            final ActionProgress listener = ActionProgress.start(lookup);
            final ExecutorTask task = RunUtils.run(rc);
            if (task != null) {
                task.addTaskListener(new TaskListener() {
                    @Override public void taskFinished(Task t) {
                        listener.finished(task.result() == 0);
                    }
                });
            } else {
                listener.finished(false);
            }
        }
    }

    public static Map<String,String> replacements(Project proj, String action, Lookup lookup) {
        Map<String,String> replacements = new HashMap<String,String>();
        for (ReplaceTokenProvider prov : proj.getLookup().lookupAll(ReplaceTokenProvider.class)) {
            replacements.putAll(prov.createReplacements(action, lookup));
        }
        return replacements;
    }

    @Messages({
        "# {0} - artifactId", "TXT_Run=Run {0}",
        "# {0} - artifactId", "TXT_Debug=Debug {0}",
        "# {0} - artifactId", "TXT_Profile=Profile {0}",
        "# {0} - artifactId", "TXT_Test=Test {0}",
        "# {0} - artifactId", "TXT_Build=Build {0}"
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
        } else if (ActionProvider.COMMAND_PROFILE.equals(action)) {
            title = TXT_Profile(prj.getMavenProject().getArtifactId());
        } else if (ActionProvider.COMMAND_TEST.equals(action)) {
            title = TXT_Test(prj.getMavenProject().getArtifactId());
        } else if (action.startsWith(ActionProvider.COMMAND_RUN_SINGLE)) {
            title = TXT_Run(dobjName);
        } else if (action.startsWith(ActionProvider.COMMAND_DEBUG_SINGLE) || ActionProvider.COMMAND_DEBUG_TEST_SINGLE.equals(action)) {
            title = TXT_Debug(dobjName);
        } else if (action.startsWith(ActionProvider.COMMAND_PROFILE_SINGLE) || ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(action)) {
            title = TXT_Profile(dobjName);
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
        if (action.equals(SingleMethod.COMMAND_RUN_SINGLE_METHOD) || action.equals(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD)) {
            return true;
        }
        //TODO if order is important, use the lookupmerger
        Collection<? extends ActionConvertor> convertors = proj.getLookup().lookupAll(ActionConvertor.class);
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
        return ActionToGoalUtils.isActionEnable(convertedAction, proj.getLookup().lookup(NbMavenProjectImpl.class), lookup);
    }

    public Action createCustomMavenAction(String name, NetbeansActionMapping mapping, boolean showUI, Lookup context) {
        return new CustomAction(name, mapping, showUI, context);
    }

    private final class CustomAction extends AbstractAction {

        private final NetbeansActionMapping mapping;
        private final boolean showUI;
        private final Lookup context;

        private CustomAction(String name, NetbeansActionMapping mapp, boolean showUI, Lookup context) {
            mapping = mapp;
            putValue(Action.NAME, name);
            this.showUI = showUI;
            this.context = context;
        }

        @Messages("TIT_Run_Maven=Run Maven")
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            Map<String,String> replacements = replacements(proj, (String) getValue(Action.NAME), context);
            for (Map.Entry<String,String> entry : mapping.getProperties().entrySet()) {
                entry.setValue(AbstractMavenActionsProvider.dynamicSubstitutions(replacements, entry.getValue()));
            }

            if (!showUI) {
                M2ConfigProvider conf = proj.getLookup().lookup(M2ConfigProvider.class);
                ModelRunConfig rc = createCustomRunConfig(conf);
                RunUtils.run(rc);

                return;
            }
            RunGoalsPanel pnl = new RunGoalsPanel();
            DialogDescriptor dd = new DialogDescriptor(pnl, TIT_Run_Maven());
            ActionToGoalMapping maps = ActionToGoalUtils.readMappingsFromFileAttributes(proj.getProjectDirectory());
            pnl.readMapping(mapping, proj.getLookup().lookup(NbMavenProjectImpl.class), maps);
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
                M2ConfigProvider conf = proj.getLookup().lookup(M2ConfigProvider.class);
                ActionToGoalUtils.writeMappingsToFileAttributes(proj.getProjectDirectory(), maps);
                if (pnl.isRememberedAs() != null) {
                    try {

                        String tit = "CUSTOM-" + pnl.isRememberedAs(); //NOI18N
                        mapping.setActionName(tit);
                        mapping.setDisplayName(pnl.isRememberedAs());
                        //TODO shall we write to configuration based files or not?
                        ModelHandle2.putMapping(mapping, proj, conf.getDefaultConfig());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                ModelRunConfig rc = createCustomRunConfig(conf);
                rc.setOffline(Boolean.valueOf(pnl.isOffline()));
                rc.setShowDebug(pnl.isShowDebug());
                rc.setRecursive(pnl.isRecursive());
                rc.setUpdateSnapshots(pnl.isUpdateSnapshots());
                
                setupTaskName("custom", rc, Lookup.EMPTY); //NOI18N
                RunUtils.run(rc);

            }
        }

        private ModelRunConfig createCustomRunConfig(M2ConfigProvider conf) {
            ModelRunConfig rc = new ModelRunConfig(proj, mapping, mapping.getActionName(), null, Lookup.EMPTY);

            //#171086 also inject profiles from currently selected configuratiin
            List<String> acts = new ArrayList<String>();
            acts.addAll(rc.getActivatedProfiles());
            acts.addAll(conf.getActiveConfiguration().getActivatedProfiles());
            rc.setActivatedProfiles(acts);
            Map<String, String> props = new HashMap<String, String>(rc.getProperties());
            props.putAll(conf.getActiveConfiguration().getProperties());
            rc.addProperties(props);
            rc.setTaskDisplayName(TXT_Build(proj.getLookup().lookup(NbMavenProject.class).getMavenProject().getArtifactId()));
            return rc;
        }
    }

    // XXX should this be an API somewhere?
    private static abstract class ConditionallyShownAction extends AbstractAction implements ContextAwareAction {
        protected boolean triggeredOnFile = false;
        protected boolean triggeredOnPom = false;
        
        protected ConditionallyShownAction() {
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        }

        public final @Override void actionPerformed(ActionEvent e) {
            assert false;
        }

        protected abstract Action forProject(@NonNull Project p, @NullAllowed FileObject file);

        public final @Override Action createContextAwareInstance(Lookup actionContext) {
            triggeredOnFile = false;
            triggeredOnPom = false;
            Collection<? extends Project> projects = actionContext.lookupAll(Project.class);
            if (projects.size() != 1) {
                Collection<? extends FileObject> fobs = actionContext.lookupAll(FileObject.class);
                if (fobs.size() == 1) {
                    if ("pom.xml".equals(fobs.iterator().next().getNameExt())) {
                        Project p = FileOwnerQuery.getOwner(fobs.iterator().next());
                        if (p != null) {
                             triggeredOnFile = true;
                             triggeredOnPom = true;
                             Action a = forProject(p, null);
                             return a != null ? a : this;
                        }
                    } else {
                       //other non-pom files
                        FileObject fob = fobs.iterator().next();
                        Project p = FileOwnerQuery.getOwner(fob);
                        if (p != null) {
                             triggeredOnFile = true;
                             Action a = forProject(p, fob);
                             return a != null ? a : this;
                        }
                    }
                }
                return this;
            }
            Action a = forProject(projects.iterator().next(), null);
            return a != null ? a : this;
        }

    }

    @ActionID(id = "org.netbeans.modules.maven.customPopup", category = "Project")
    @ActionRegistration(displayName = "#LBL_Custom_Run", lazy=false)
    @ActionReferences({
        @ActionReference(position = 1400, path = "Projects/org-netbeans-modules-maven/Actions"),
        @ActionReference(position = 250, path = "Loaders/text/x-maven-pom+xml/Actions"),
        @ActionReference(position = 1296, path = "Loaders/text/x-java/Actions"),
        @ActionReference(position = 1821, path = "Editors/text/x-java/Popup")
    })
    @Messages({"LBL_Custom_Run=Custom", "LBL_Custom_Run_File=Run Maven"})
    public static ContextAwareAction customPopupActions() {
        return new ConditionallyShownAction() {
            
            protected @Override Action forProject(Project p, FileObject fo) {
                ActionProviderImpl ap = p.getLookup().lookup(ActionProviderImpl.class);
                return ap != null ? ap.new CustomPopupActions(triggeredOnFile, triggeredOnPom, fo) : null;
            }
        };
    }
    private final class CustomPopupActions extends AbstractAction implements Presenter.Popup {
        private final boolean onFile;
        private final boolean onPom;
        private final Lookup lookup;

        private CustomPopupActions(boolean onFile, boolean onPomFile, FileObject fo) {
            putValue(Action.NAME, onFile ? LBL_Custom_Run_File() : LBL_Custom_Run());
            this.onFile = onFile;
            this.onPom = onPomFile;
            this.lookup = fo != null ? Lookups.singleton(fo) : Lookup.EMPTY;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
        }

        
        @Messages({
            "LBL_Loading=Loading...",
            "LBL_Custom_run_goals=Goals..."
        })
        @Override public JMenuItem getPopupPresenter() {

            final JMenu menu = new JMenu(onFile ? LBL_Custom_Run_File() : LBL_Custom_Run());
            final JMenuItem loading = new JMenuItem(LBL_Loading());

            menu.add(loading);
            /*using lazy construction strategy*/
            RP.post(new Runnable() {

                @Override
                public void run() {
                    NetbeansActionMapping[] maps;
                    if (onFile && !onPom) {
                      maps = ActionToGoalUtils.getActiveCustomMappingsForFile(proj.getLookup().lookup(NbMavenProjectImpl.class));
                    } else {
                      maps = ActionToGoalUtils.getActiveCustomMappings(proj.getLookup().lookup(NbMavenProjectImpl.class));
                    }
                    for (int i = 0; i < maps.length; i++) {
                        NetbeansActionMapping mapp = maps[i];
                        Action act = createCustomMavenAction(mapp.getActionName(), mapp, false, lookup);
                        JMenuItem item = new JMenuItem(act);
                        item.setText(mapp.getDisplayName() == null ? mapp.getActionName() : mapp.getDisplayName());
                        menu.add(item);
                    }
                    menu.add(new JMenuItem(createCustomMavenAction(LBL_Custom_run_goals(), new NetbeansActionMapping(), true, lookup)));
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
    @ActionRegistration(displayName = "#ACT_CloseRequired", lazy=false)
    @ActionReference(position = 2000, path = "Projects/org-netbeans-modules-maven/Actions")
    @Messages("ACT_CloseRequired=Close Required Projects")
    public static ContextAwareAction closeSubprojectsAction() {
        return new ConditionallyShownAction() {
            protected @Override Action forProject(Project p, FileObject fo) {
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
            RP.post(new Runnable() {
                @Override
                public void run() {
                    //mkleint: usage of subprojectprovider is correct here
                    SubprojectProvider subs = project.getLookup().lookup(SubprojectProvider.class);
                    Set<? extends Project> lst = subs.getSubprojects();
                    Project[] arr = lst.toArray(new Project[lst.size()]);
                    OpenProjects.getDefault().close(arr);
                }
            });
            
        }
    }

    @ActionID(id = "org.netbeans.modules.maven.buildWithDependencies", category = "Project")
    @ActionRegistration(displayName = "#ACT_Build_Deps", lazy=false)
    @ActionReference(position = 500, path = "Projects/org-netbeans-modules-maven/Actions")
    @Messages("ACT_Build_Deps=Build with Dependencies")
    public static ContextAwareAction buildWithDependenciesAction() {
        return (ContextAwareAction) ProjectSensitiveActions.projectCommandAction(BUILD_WITH_DEPENDENCIES, ACT_Build_Deps(), null);
    }

}
