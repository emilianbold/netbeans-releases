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

package org.netbeans.modules.maven.profiler;

import java.util.*;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher.Launcher;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher.Session;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 *
 * @author mkleint
 * @author Jiri Sedlacek
 */
@ProjectServiceProvider(service=LateBoundPrerequisitesChecker.class, projectType="org-netbeans-modules-maven")
public class RunCheckerImpl implements LateBoundPrerequisitesChecker {

    private static final Logger LOG = Logger.getLogger(RunCheckerImpl.class.getName());
    
//    private static final String EXEC_ARGS = "exec.args"; // NOI18N
//    private static final String PROFILER_ARGS = "${profiler.args}"; // NOI18N
//    private static final String PROFILER_ARGS_PREFIXED = "${profiler.args.prefixed}"; // NOI18N
//    private static final String EXEC_EXECUTABLE = "exec.executable"; // NOI18N
//    private static final String PROFILER_JAVA = "${profiler.java}"; // NOI18N
//    private static final String PROFILER_JDKHOME_OPT = "${profiler.jdkhome.opt}"; // NOI18N
    
    private final Project project;

    @ProjectServiceProvider(service=ProfilerLauncher.LauncherFactory.class, projectType="org-netbeans-modules-maven")
    final public static class MavenLauncherFactory implements ProfilerLauncher.LauncherFactory {
        @Override
        public Launcher createLauncher(final Session session) {
            return new Launcher() {

                @Override
                public void launch(boolean rerun) {
                    if (rerun) {
                        RunConfig config = (RunConfig)session.getAttribute("mvn-run-checker.config");
                        if (config != null) {
                            RunUtils.executeMaven(config);
                        }
                    } else {
                        Project p = session.getProject();
                        if (p == null) {
                            FileObject f = session.getFile();
                            if (f != null) {
                                p = FileOwnerQuery.getOwner(f);
                            }
                        }
                        if (p != null) {
                            ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
                            if (ap != null) {
                                ap.invokeAction(session.getCommand(), session.getContext());
                            }
                        }
                    }
                }
            };
        }
        
    }
    
    public RunCheckerImpl(Project prj) {
        project = prj;
    }
    
    @Override
    public boolean checkRunConfig(final RunConfig config, ExecutionContext context) {
        Map<? extends String,? extends String> configProperties = config.getProperties();
        
        if (ActionProvider.COMMAND_PROFILE.equals(config.getActionName()) ||
               ActionProvider.COMMAND_PROFILE_TEST_SINGLE.equals(config.getActionName()) ||
              (config.getActionName() != null && config.getActionName().startsWith(ActionProvider.COMMAND_PROFILE_SINGLE))) {
            
            ProfilerLauncher.Session session = ProfilerLauncher.getLastSession();
            
            if (session == null) {
                closeInputOuptut(context);
                return false;
            }
                       
            Map<String, String> sProps = session.getProperties();
            if (sProps == null) {
                closeInputOuptut(context);
                return false;
            }
            
            session.setAttribute("mvn-run-checker.config", config);
                        
            final ProfilerLauncher.Session s = session;
            // Attach profiler engine (in separate thread) to profiled process
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    Profiler.getDefault().connectToStartedApp(s.getProfilingSettings(), s.getSessionSettings());
                }
            });
        }
        
        return true;
    }

    private void closeInputOuptut(ExecutionContext context) {
        InputOutput ioput = context.getInputOutput();
        if (ioput != null) {
            ioput.closeInputOutput();
        }
    }
}
