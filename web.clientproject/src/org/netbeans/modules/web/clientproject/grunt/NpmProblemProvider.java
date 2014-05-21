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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.grunt;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Problem: Project requires npm install
 */
public final class NpmProblemProvider implements ProjectProblemsProvider {

    final ProjectProblemsProviderSupport problemsProviderSupport = new ProjectProblemsProviderSupport(this);
    final Project project;


    private NpmProblemProvider(Project project) {
        assert project != null;
        this.project = project;
    }

    public static NpmProblemProvider create(Project project) {
        NpmProblemProvider problemProvider = new NpmProblemProvider(project);
        return problemProvider;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        problemsProviderSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        problemsProviderSupport.removePropertyChangeListener(listener);
    }

    @NbBundle.Messages({
        "ERR_NpmInstall=Missing node modules",
        "TXT_NpmInstallDescription=This project uses node modules, but they are not installed.",
        "ERR_NpmInstallResolved=Node modules were succesfully installed.",
        "ERR_NpmInstallUnresolved=Node modules were not succesfully installed."
    })
    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        return problemsProviderSupport.getProblems(new ProjectProblemsProviderSupport.ProblemsCollector() {
            @Override
            public Collection<ProjectProblemsProvider.ProjectProblem> collectProblems() {
                final FileObject root = project.getProjectDirectory();
                root.refresh();
                final Collection<ProjectProblemsProvider.ProjectProblem> currentProblems = new ArrayList<>();
                FileObject package_json = root.getFileObject("package.json");//NOI18N
                FileObject node_modules = root.getFileObject("node_modules");//NOI18N
                if (package_json != null && node_modules == null) {
                    ProjectProblem npmWarning = ProjectProblemsProvider.ProjectProblem.createWarning(Bundle.ERR_NpmInstall(), Bundle.TXT_NpmInstallDescription(), new ProjectProblemResolver() {
                        @Override
                        public Future<Result> resolve() {
                            return new FutureResult();
                        }
                    });
                    currentProblems.add(npmWarning);
                }

                return currentProblems;
            }
        });
    }

    private class FutureResult implements Future<Result> {
        
        private ExecutorTask execute;
        private IOException ex;
        private AtomicBoolean done;
        private AtomicBoolean cancelled;

        public FutureResult() {
            try {
                done = new AtomicBoolean(false);
                cancelled = new AtomicBoolean(false);
                execute = new NpmExecutor(project.getProjectDirectory(), new String[]{"install"}).execute();//NOI18N
            } catch (IOException ex) {
                this.ex = ex;
            }
        }
        
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (execute==null) {
                return false;
            }
            execute.stop();
            cancelled.set(true);
            return execute.isFinished();
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        @Override
        public boolean isDone() {
            return done.get();
        }

        @Override
        public ProjectProblemsProvider.Result get() throws InterruptedException, ExecutionException {
            if (ex != null) {
                throw new ExecutionException(ex);
            }

            int result = execute.result();
            TargetLister.invalidateCache(project.getProjectDirectory().getFileObject("Gruntfile.js"));

            FileObject root = project.getProjectDirectory();
            root.refresh();
            FileObject package_json = root.getFileObject("package.json");
            FileObject node_modules = root.getFileObject("node_modules");
            if (package_json != null && node_modules == null) {
                done.set(true);
                return ProjectProblemsProvider.Result.create(Status.UNRESOLVED, Bundle.ERR_NpmInstallUnresolved());
            } else {
                problemsProviderSupport.fireProblemsChange();
                done.set(true);
                return ProjectProblemsProvider.Result.create(Status.RESOLVED, Bundle.ERR_NpmInstallResolved());
            }
        }

        @Override
        public ProjectProblemsProvider.Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }
}
