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

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 * @author Jiri Sedlacek
 */
@ProjectServiceProvider(service=LateBoundPrerequisitesChecker.class, projectType="org-netbeans-modules-maven")
public class RunCheckerImpl implements LateBoundPrerequisitesChecker {
    
    private static final String ACTION_PROFILE = "profile"; // NOI18N
    private static final String ACTION_PROFILE_SINGLE = "profile-single"; // NOI18N
        private static final String ACTION_PROFILE_TESTS = "profile-tests"; // NOI18N
    
//    private static final String EXEC_ARGS = "exec.args"; // NOI18N
    private static final String PROFILER_ARGS = "${profiler.args}"; // NOI18N
    private static final String PROFILER_ARGS_PREFIXED = "${profiler.args.prefixed}"; // NOI18N
//    private static final String EXEC_EXECUTABLE = "exec.executable"; // NOI18N
    private static final String PROFILER_JAVA = "${profiler.java}"; // NOI18N
    private static final String PROFILER_JDKHOME_OPT = "${profiler.jdkhome.opt}"; // NOI18N
    
    private final Project project;
    private static final Map<Project, Properties> properties = new WeakHashMap();
    private static final Map<Project, ProfilingSettings> profilingSettings = new WeakHashMap();
    private static final Map<Project, SessionSettings> sessionSettings = new WeakHashMap();

    
    public RunCheckerImpl(Project prj) {
        project = prj;
    }
    
    static void configureProject(Project project, Properties p, ProfilingSettings ps, SessionSettings ss) {
        properties.put(project, p);
        profilingSettings.put(project, ps);
        sessionSettings.put(project, ss);
    }
    
    public boolean checkRunConfig(RunConfig config, ExecutionContext context) {
        Map<? extends String,? extends String> configProperties = config.getProperties();

        if (   ACTION_PROFILE.equals(config.getActionName()) ||
               ACTION_PROFILE_TESTS.equals(config.getActionName()) ||
              (config.getActionName() != null && config.getActionName().startsWith(ACTION_PROFILE_SINGLE))) { // action "profile"
            // Resolve profiling configuration
            Properties sessionProperties = properties.get(project);
            if (sessionProperties == null) return false;
            // Resolve profiling session properties
            for (Object k : configProperties.keySet()) {
                String key = (String)k;
                
                String value = configProperties.get(key);
                if (value.contains(PROFILER_ARGS)) {
                    String agentArg = fixAgentArg(sessionProperties.getProperty("profiler.info.jvmargs.agent"));
                    value = value.replace(PROFILER_ARGS, sessionProperties.getProperty("profiler.info.jvmargs") // NOI18N
                            + " " + agentArg); // NOI18N
                    config.setProperty(key, value.trim());
                }
                if (value.contains(PROFILER_ARGS_PREFIXED)) {
                    String agentArg = fixAgentArg(sessionProperties.getProperty("profiler.info.jvmargs.agent"));
                    value = value.replace(PROFILER_ARGS_PREFIXED,
                            (sessionProperties.getProperty("profiler.info.jvmargs") + " " + agentArg).trim().replaceAll("^|(?<= +)(?! )", "-J"));
                    config.setProperty(key, value);
                }
                if (value.contains(PROFILER_JAVA)) {
                    String profilerJava = sessionProperties.getProperty("profiler.info.jvm"); // NOI18N
                    value = value.replace(PROFILER_JAVA,
                            (profilerJava != null && new File(profilerJava).isFile()) ? profilerJava : "java"); // NOI18N
                    config.setProperty(key, value.trim());
                }
                if (value.contains(PROFILER_JDKHOME_OPT)) {
                    String opt = "";
                    String profilerJava = sessionProperties.getProperty("profiler.info.jvm"); // NOI18N
                    if (profilerJava != null) {
                        File binJava = new File(profilerJava);
                        if (binJava.isFile() && binJava.getName().matches("java([.]exe)?") && binJava.getParentFile().getName().equals("bin")) {
                            opt = "--jdkhome " + binJava.getParentFile().getParent();
                        }
                    }
                    value = value.replace(PROFILER_JDKHOME_OPT, opt);
                    config.setProperty(key, value.trim());
                }
            }
            
            // Attach profiler engine (in separate thread) to profiled process
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    ProfilingSettings ps = profilingSettings.get(project);
                    SessionSettings ss = sessionSettings.get(project);
                    Profiler.getDefault().connectToStartedApp(ps, ss);
                }
            });
            
//        } else if (ACTION_PROFILE_SINGLE.equals(actionName)) { // action "profile-single"
//            // profile-single not supported yet, shouldn't get here
//        } else if (ACTION_PROFILE_TESTS.equals(actionName)) {
//            // profile-tests not supported yet, shouldn't get here // action "profile-tests"
        }
        
        return true;
    }

    private String fixAgentArg(String agentArg) {
        // !!!!!!!!!!!!!!!!!!!!!!!! Never remove this replacement !!!!!!!!!!!!!!!!!!!!!!!!!!
        // !! It is absolutely needed for correct profiling of maven projects on Windows  !!
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        agentArg = agentArg.replace("\\", "/"); // NOI18N

        if (agentArg.indexOf(' ') != -1) { //NOI18N
            return "\"" + agentArg + "\""; // NOI18N
        }
        return agentArg;
    }
}
