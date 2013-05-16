/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.cnd.makeproject.ui.BrokenLinks;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.modules.cnd.makeproject.ui.ResolveReferencePanel;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Alexander Simon
 */
public final class BrokenReferencesSupport {

    private BrokenReferencesSupport() {
    }

    @NonNull
    public static ProjectProblemsProvider createPlatformVersionProblemProvider(
            @NonNull final MakeProject project,
            @NonNull final MakeProjectHelper helper,
            @NonNull final ConfigurationDescriptorProvider projectDescriptorProvider,
            @NonNull final MakeProjectConfigurationProvider makeProjectConfigurationProvider) {
        ProjectProblemsProviderImpl pp = new ProjectProblemsProviderImpl(project, helper, projectDescriptorProvider, makeProjectConfigurationProvider);
        pp.attachListeners();
        return pp;
    }

    public static boolean isIncorrectPlatform(ConfigurationDescriptorProvider projectDescriptorProvider) {
        if (projectDescriptorProvider.gotDescriptor()) {
            Configuration[] confs = projectDescriptorProvider.getConfigurationDescriptor().getConfs().toArray();
            for (int i = 0; i < confs.length; i++) {
                MakeConfiguration conf = (MakeConfiguration) confs[i];
                if (conf.getDevelopmentHost().isLocalhost()
                        && CompilerSetManager.get(conf.getDevelopmentHost().getExecutionEnvironment()).getPlatform() != conf.getDevelopmentHost().getBuildPlatformConfiguration().getValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isIncorectVersion(@NonNull final MakeProject project) {
        MakeLogicalViewProvider view = project.getLookup().lookup(MakeLogicalViewProvider.class);
        return view.isIncorectVersion();
    }

    @NonNull
    private static Set<? extends ProjectProblemsProvider.ProjectProblem> getReferenceProblems(@NonNull final MakeProject project) {
        Set<ProjectProblemsProvider.ProjectProblem> set = new LinkedHashSet<ProjectProblemsProvider.ProjectProblem>();
        final List<BrokenLinks.BrokenLink> brokenLinks = BrokenLinks.getBrokenLinks(project);
        if (!brokenLinks.isEmpty()) {
            ProjectProblemsProvider.ProjectProblem error =
                    ProjectProblemsProvider.ProjectProblem.createError(
                    NbBundle.getMessage(ResolveReferencePanel.class, "Link_Resolve_Name"), //NOI18N
                    NbBundle.getMessage(ResolveReferencePanel.class, "Link_Resolve_Description"), //NOI18N
                    new ToolCollectionResolverImpl(project, brokenLinks));
            set.add(error);
        }
        return set;
    }

    @NonNull
    private static Set<? extends ProjectProblemsProvider.ProjectProblem> getPlatformProblems(@NonNull final MakeProject project) {
        Set<ProjectProblemsProvider.ProjectProblem> set = new LinkedHashSet<ProjectProblemsProvider.ProjectProblem>();
        if (BrokenReferencesSupport.isIncorrectPlatform(project.getLookup().lookup(ConfigurationDescriptorProvider.class))) {
            final ProjectProblemsProvider.ProjectProblem error =
                    ProjectProblemsProvider.ProjectProblem.createError(
                    NbBundle.getMessage(ResolveReferencePanel.class, "MSG_platform_resolve_name"), //NOI18N
                    NbBundle.getMessage(ResolveReferencePanel.class, "MSG_platform_resolve_description"), //NOI18N
                    new PlatformResolverImpl(project));
            set.add(error);
        }
        return set;
    }

    @NonNull
    private static Set<? extends ProjectProblemsProvider.ProjectProblem> getVersionProblems(@NonNull final MakeProject project) {
        Set<ProjectProblemsProvider.ProjectProblem> set = new LinkedHashSet<ProjectProblemsProvider.ProjectProblem>();
        if (BrokenReferencesSupport.isIncorectVersion(project)) {
            ProjectProblemsProvider.ProjectProblem error =
                    ProjectProblemsProvider.ProjectProblem.createError(
                    NbBundle.getMessage(ResolveReferencePanel.class, "MSG_version_resolve_name"), //NOI18N
                    NbBundle.getMessage(ResolveReferencePanel.class, "MSG_version_resolve_description"), //NOI18N
                    new VersionResolverImpl(project));
            set.add(error);
        }
        return set;
    }

    private static void reInitWithRemovedPrivate(final MakeProject project) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        final MakeConfigurationDescriptor mcd = cdp.getConfigurationDescriptor();
        Configuration[] confs = mcd.getConfs().toArray();
        boolean save = false;
        for (int i = 0; i < confs.length; i++) {
            MakeConfiguration conf = (MakeConfiguration) confs[i];
            if (conf.getDevelopmentHost().isLocalhost()) {
                final int platform1 = CompilerSetManager.get(conf.getDevelopmentHost().getExecutionEnvironment()).getPlatform();
                final int platform2 = conf.getDevelopmentHost().getBuildPlatformConfiguration().getValue();
                if (platform1 != platform2) {
                    conf.getDevelopmentHost().getBuildPlatformConfiguration().setValue(platform1);
                    mcd.setModified();
                    save = true;
                }
            }
        }
        if (save) {
            mcd.save();
        }
        MakeLogicalViewProvider view = project.getLookup().lookup(MakeLogicalViewProvider.class);
        view.reInit(mcd, false);
    }

    private static void reInitWithUnsupportedVersion(final MakeProject project) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        final MakeConfigurationDescriptor mcd = cdp.getConfigurationDescriptor();
        MakeLogicalViewProvider view = project.getLookup().lookup(MakeLogicalViewProvider.class);
        view.reInit(mcd, true);
    }

    private static class ProjectProblemsProviderImpl implements ProjectProblemsProvider, PropertyChangeListener {

        private final ProjectProblemsProviderSupport problemsProviderSupport = new ProjectProblemsProviderSupport(this);
        private final MakeProject project;
        private final MakeProjectHelper helper;
        private final ConfigurationDescriptorProvider projectDescriptorProvider;
        private final MakeProjectConfigurationProvider makeProjectConfigurationProvider;

        public ProjectProblemsProviderImpl(
                @NonNull final MakeProject project,
                @NonNull final MakeProjectHelper helper,
                @NonNull final ConfigurationDescriptorProvider projectDescriptorProvider,
                @NonNull final MakeProjectConfigurationProvider makeProjectConfigurationProvider) {
            this.project = project;
            this.helper = helper;
            this.projectDescriptorProvider = projectDescriptorProvider;
            this.makeProjectConfigurationProvider = makeProjectConfigurationProvider;
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            problemsProviderSupport.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            problemsProviderSupport.removePropertyChangeListener(listener);
        }

        @Override
        public Collection<? extends ProjectProblemsProvider.ProjectProblem> getProblems() {
            return problemsProviderSupport.getProblems(new ProjectProblemsProviderSupport.ProblemsCollector() {
                @Override
                public Collection<? extends ProjectProblemsProvider.ProjectProblem> collectProblems() {
                    Collection<? extends ProjectProblemsProvider.ProjectProblem> currentProblems = ProjectManager.mutex().readAccess(
                            new Mutex.Action<Collection<? extends ProjectProblemsProvider.ProjectProblem>>() {
                        @Override
                        public Collection<? extends ProjectProblemsProvider.ProjectProblem> run() {
                            final Set<ProjectProblemsProvider.ProjectProblem> newProblems = new LinkedHashSet<ProjectProblemsProvider.ProjectProblem>();
                            Set<? extends ProjectProblem> versionProblems = getVersionProblems(project);
                            newProblems.addAll(versionProblems);
                            if (versionProblems.isEmpty()) {
                                newProblems.addAll(getReferenceProblems(project));
                                newProblems.addAll(getPlatformProblems(project));
                            }
                            return Collections.unmodifiableSet(newProblems);
                        }
                    });
                    return currentProblems;
                }
            });
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            problemsProviderSupport.fireProblemsChange();
        }

        void attachListeners() {
            makeProjectConfigurationProvider.registerPropertyChangeListener(this);
        }
    }

    private static final class Done implements Future<ProjectProblemsProvider.Result> {

        private final ProjectProblemsProvider.Result result;

        Done(@NonNull final ProjectProblemsProvider.Result result) {
            Parameters.notNull("result", result);   //NOI18N
            this.result = result;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public ProjectProblemsProvider.Result get() throws InterruptedException, ExecutionException {
            return result;
        }

        @Override
        public ProjectProblemsProvider.Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }

    private static class PlatformResolverImpl implements ProjectProblemResolver {

        private final MakeProject project;

        public PlatformResolverImpl(MakeProject project) {
            this.project = project;
        }

        @Override
        public Future<ProjectProblemsProvider.Result> resolve() {
            String title = NbBundle.getMessage(ResolveReferencePanel.class, "MSG_platform_fix_title"); //NOI18N
            String message = NbBundle.getMessage(ResolveReferencePanel.class, "MSG_platform_fix"); //NOI18N
            NotifyDescriptor nd = new NotifyDescriptor(message,
                    title, NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    null, NotifyDescriptor.YES_OPTION);
            Object ret = DialogDisplayer.getDefault().notify(nd);
            if (ret == NotifyDescriptor.YES_OPTION) {
                BrokenReferencesSupport.reInitWithRemovedPrivate(project);
                return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
            } else {
                return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
            }
        }

        @Override
        public int hashCode() {
            return this.project.hashCode() + PlatformResolverImpl.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PlatformResolverImpl other = (PlatformResolverImpl) obj;
            return this.project.equals(other.project);
        }
    }

    private static class VersionResolverImpl implements ProjectProblemResolver {

        private final MakeProject project;

        public VersionResolverImpl(MakeProject project) {
            this.project = project;
        }

        @Override
        public Future<ProjectProblemsProvider.Result> resolve() {
            String title = NbBundle.getMessage(ResolveReferencePanel.class, "MSG_version_ignore_title"); //NOI18N
            String message = NbBundle.getMessage(ResolveReferencePanel.class, "MSG_version_ignore"); //NOI18N
            NotifyDescriptor nd = new NotifyDescriptor(message,
                    title, NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    null, NotifyDescriptor.YES_OPTION);
            Object ret = DialogDisplayer.getDefault().notify(nd);
            if (ret == NotifyDescriptor.YES_OPTION) {
                BrokenReferencesSupport.reInitWithUnsupportedVersion(project);
                return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
            } else {
                //if (negativeAction != null) {
                //    negativeAction.run();
                //}
                return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
            }
        }

        @Override
        public int hashCode() {
            return this.project.hashCode() + VersionResolverImpl.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final VersionResolverImpl other = (VersionResolverImpl) obj;
            return this.project.equals(other.project);
        }
    }

    private static class ToolCollectionResolverImpl implements ProjectProblemResolver {

        private final List<BrokenLinks.BrokenLink> brokenLinks;
        private final MakeProject project;

        public ToolCollectionResolverImpl(MakeProject project, List<BrokenLinks.BrokenLink> brokenLinks) {
            this.project = project;
            this.brokenLinks = brokenLinks;
        }

        @Override
        public Future<ProjectProblemsProvider.Result> resolve() {
            ResolveReferencePanel panel = new ResolveReferencePanel(brokenLinks);
            DialogDescriptor dd = new DialogDescriptor(panel,
                    NbBundle.getMessage(ResolveReferencePanel.class, "Link_Dialog_Title"), true,
                    new Object[]{DialogDescriptor.CLOSED_OPTION}, DialogDescriptor.CLOSED_OPTION,
                    DialogDescriptor.DEFAULT_ALIGN, null, null);
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.setVisible(true);
            dialog.dispose();
            return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
        }
        
        @Override
        public int hashCode() {
            return this.project.hashCode() + ToolCollectionResolverImpl.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ToolCollectionResolverImpl other = (ToolCollectionResolverImpl) obj;
            return this.project.equals(other.project);
        }
    }
}
