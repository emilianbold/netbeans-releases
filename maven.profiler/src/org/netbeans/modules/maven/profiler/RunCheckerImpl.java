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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

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

    private static final Logger LOG = Logger.getLogger(RunCheckerImpl.class.getName());
    
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
                    value = value.replace(PROFILER_ARGS, profilerArgs(sessionProperties, false));
                    config.setProperty(key, value.trim());
                }
                if (value.contains(PROFILER_ARGS_PREFIXED)) {
                    value = value.replace(PROFILER_ARGS_PREFIXED, profilerArgs(sessionProperties, true));
                    config.setProperty(key, value.trim());
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
                            String jdkhome = binJava.getParentFile().getParent();
                            opt = Utilities.escapeParameters(new String[] {"--jdkhome", jdkhome});
                            LOG.log(Level.FINE, "from {0} escaped {1}", new Object[] {jdkhome, opt});
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

    private String profilerArgs(Properties sessionProperties, boolean prefixed) {
        List<String> args = new ArrayList<String>();
        String jvmargs = sessionProperties.getProperty("profiler.info.jvmargs");
        for (String arg : Utilities.parseParameters(jvmargs)) {
            args.add(prefixed ? "-J" + arg : arg);
        }
        String agentarg = sessionProperties.getProperty("profiler.info.jvmargs.agent");
        if (Utilities.isWindows()) {
            agentarg = agentarg.replace('\\', '/'); // XXX is this still necessary given quoting?
        }
        args.add(prefixed ? "-J" + agentarg : agentarg);
        String escaped = Utilities.escapeParameters(args.toArray(new String[args.size()]));
        LOG.log(Level.FINE, "from {0} and {1} produced {2}", new Object[] {jvmargs, agentarg, escaped});
        return escaped;
    }

}
