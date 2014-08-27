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
package org.netbeans.modules.web.clientproject.node;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.clientproject.grunt.TargetLister;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Problem: Project requires npm install
 */
public final class NpmProblemProvider extends NodeProblemProvider {

    private NpmProblemProvider(Project project) {
        super(project);
    }

    public static NpmProblemProvider create(Project project) {
        NpmProblemProvider problemProvider = new NpmProblemProvider(project);
        problemProvider.addFileChangesListeners("package.json", "node_modules");
        return problemProvider;
    }
    
    private boolean isNpmInstallRequired() {
        final FileObject root = project.getProjectDirectory();
        root.refresh();
        FileObject package_json = root.getFileObject("package.json");//NOI18N
        FileObject node_modules = root.getFileObject("node_modules");//NOI18N
        try {
            if (package_json != null && node_modules == null && hasDeps(package_json)) {
                return true;
            }
        } catch (IOException | ParseException ex) {
            //ignore;
        }
        return false;
    }
    
    private static boolean hasDeps(FileObject package_json) throws IOException, ParseException {
        JSONObject r;
        if (package_json.getSize() > 0) {
            JSONParser parser = new JSONParser();
            try (InputStreamReader inputStreamReader = new InputStreamReader(package_json.getInputStream())) {
                r = (JSONObject) parser.parse(inputStreamReader);
                Object deps = r.get("dependencies");
                if (deps != null) {
                    return true;
                }
                deps = r.get("devDependencies");
                if (deps != null) {
                    return true;
                }
            }
        }
        return false;
    }
    

    @NbBundle.Messages({
        "ERR_NpmInstall=Missing Node.JS modules",
        "TXT_NpmInstallDescription=This project uses Node.JS modules, but they are not installed.",
        "ERR_NpmInstallResolved=Node.JS modules were successfully installed.",
        "ERR_NpmInstallUnresolved=Node.JS modules were not successfully installed."
    })
    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        return problemsProviderSupport.getProblems(new ProjectProblemsProviderSupport.ProblemsCollector() {
            @Override
            public Collection<ProjectProblemsProvider.ProjectProblem> collectProblems() {
                final Collection<ProjectProblemsProvider.ProjectProblem> currentProblems = new ArrayList<>();
                if (isNpmInstallRequired()) {
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
                ClientSideProjectUtilities.logUsage(NpmInstallAction.class, "USG_NPM_INSTALL", null);
                done = new AtomicBoolean(false);
                cancelled = new AtomicBoolean(false);
                execute = 
                new NodeExecutor(
                        org.netbeans.modules.web.clientproject.node.Bundle.TTL_npm_install(ProjectUtils.getInformation(project).getDisplayName()),
                        "npm",
                        project.getProjectDirectory(), new String[]{"install"}).execute(); //NOI18N
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

            if (isNpmInstallRequired()) {
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
