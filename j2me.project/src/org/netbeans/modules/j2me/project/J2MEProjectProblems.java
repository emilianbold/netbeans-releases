/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2me.project.api.PropertyEvaluatorProvider;
import org.netbeans.modules.j2me.project.ui.ChooseOtherPlatformPanel;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import static org.netbeans.modules.java.api.common.project.ProjectProperties.PLATFORM_ACTIVE;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Roman Svitanic
 */
@ProjectServiceProvider(
        service = ProjectProblemsProvider.class,
        projectType = "org-netbeans-modules-j2me-project")
public class J2MEProjectProblems implements ProjectProblemsProvider, PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(J2MEProjectProblems.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(J2MEProjectProblems.class);
    private final ProjectProblemsProviderSupport problemsProviderSupport = new ProjectProblemsProviderSupport(this);
    private final Project prj;
    private final PropertyEvaluator eval;

    public J2MEProjectProblems(final Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        this.prj = lkp.lookup(Project.class);
        Parameters.notNull("prj", prj); //NOI18N
        PropertyEvaluatorProvider evalProvider = lkp.lookup(PropertyEvaluatorProvider.class);
        Parameters.notNull("evalProvider", evalProvider);   //NOI18N
        this.eval = evalProvider.getPropertyEvaluator();
        Parameters.notNull("eval", eval);   //NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        problemsProviderSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        problemsProviderSupport.removePropertyChangeListener(listener);
    }

    @NbBundle.Messages({
        "LBL_InvalidPlatform=Java ME Platform is invalid",
        "LBL_InvalidPlatformDesc=Active Java ME Platform is broken or missing.",})
    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        final String activePlatformId = eval.getProperty(PLATFORM_ACTIVE);
        final JavaPlatform activePlatform = CommonProjectUtils.getActivePlatform(
                activePlatformId,
                J2MEProjectProperties.PLATFORM_TYPE_J2ME);
        if (activePlatform != null && activePlatform instanceof J2MEPlatform) {
            if (((J2MEPlatform) activePlatform).isValid()) {
                return Collections.<ProjectProblem>emptySet();
            }
        }
        return problemsProviderSupport.getProblems(new ProjectProblemsProviderSupport.ProblemsCollector() {
            @Override
            public Collection<? extends ProjectProblemsProvider.ProjectProblem> collectProblems() {
                Collection<? extends ProjectProblemsProvider.ProjectProblem> currentProblems = ProjectManager.mutex().readAccess(
                        new Mutex.Action<Collection<? extends ProjectProblem>>() {
                            @Override
                            public Collection<? extends ProjectProblem> run() {
                                return Collections.singleton(ProjectProblem.createError(
                                                Bundle.LBL_InvalidPlatform(),
                                                Bundle.LBL_InvalidPlatformDesc(),
                                                new J2MEPlatformResolver(
                                                        prj,
                                                        J2MEPlatform.SPECIFICATION_NAME,
                                                        problemsProviderSupport
                                                )));
                            }
                        });
                return currentProblems;
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String propName = evt.getPropertyName();
        if (propName == null || ProjectProperties.PLATFORM_ACTIVE.equals(propName)) {
            problemsProviderSupport.fireProblemsChange();
        }
    }

    private static class J2MEPlatformResolver implements ProjectProblemResolver {

        private final Project project;
        private final String type;
        private final ProjectProblemsProviderSupport support;

        J2MEPlatformResolver(
                @NonNull final Project project,
                @NonNull final String type,
                @NonNull final ProjectProblemsProviderSupport support) {
            Parameters.notNull("project", project);   //NOI18N
            Parameters.notNull("type", type);   //NOI18N
            Parameters.notNull("support", support);   //NOI18N
            this.project = project;
            this.type = type;
            this.support = support;
        }

        @NbBundle.Messages({"LBL_ResolveME_SDK=Choose Java ME Platform - \"{0}\" Project"})
        @Override
        public Future<Result> resolve() {
            final ChooseOtherPlatformPanel choosePlatform = new ChooseOtherPlatformPanel(type);
            final DialogDescriptor dd = new DialogDescriptor(choosePlatform, Bundle.LBL_ResolveME_SDK(ProjectUtils.getInformation(project).getDisplayName()));
            if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
                final JavaPlatform jp = choosePlatform.getSelectedPlatform();
                if (jp != null) {
                    final Callable<ProjectProblemsProvider.Result> resultFnc
                            = new Callable<Result>() {
                                @Override
                                public Result call() throws Exception {
                                    try {
                                        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                                            @Override
                                            public Void run() throws IOException {
                                                AntProjectHelper helper = ((J2MEProject) project).getHelper();
                                                EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                                ep.setProperty(PLATFORM_ACTIVE, jp.getProperties().get(J2MEProjectProperties.PLATFORM_ANT_NAME));
                                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                                                try {
                                                    ProjectManager.getDefault().saveProject(project);
                                                } catch (IOException e) {
                                                    Exceptions.printStackTrace(e);
                                                }
                                                return null;
                                            }
                                        });
                                    } catch (MutexException e) {
                                        throw (IOException) e.getCause();
                                    }
                                    LOGGER.log(Level.INFO, "Set " + PLATFORM_ACTIVE + " to platform {0}", jp);
                                    support.fireProblemsChange();
                                    return ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED);
                                }
                            };
                    return RP.submit(resultFnc);
                }
            }
            return new J2MEProjectProblems.Done(
                    Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof J2MEProjectProblems.J2MEPlatformResolver;
        }

        @Override
        public int hashCode() {
            return 29;
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
}
