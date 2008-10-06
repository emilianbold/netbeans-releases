/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
import java.util.Properties;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.profiler.utils.ProjectUtilities;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 * @author Jiri Sedlacek
 */
public class RunCheckerImpl implements LateBoundPrerequisitesChecker {
    
    private static final String ACTION_PROFILE = "profile"; // NOI18N
//    private static final String ACTION_PROFILE_SINGLE = "profile-single"; // NOI18N
//    private static final String ACTION_PROFILE_TESTS = "profile-tests"; // NOI18N
    
//    private static final String EXEC_ARGS = "exec.args"; // NOI18N
    private static final String PROFILER_ARGS = "${profiler.args}"; // NOI18N
//    private static final String EXEC_EXECUTABLE = "exec.executable"; // NOI18N
    private static final String PROFILER_JAVA = "${profiler.java}"; // NOI18N
    
    private Project project;

    
    public RunCheckerImpl(Project prj) {
        project = prj;
    }
    
    public boolean checkRunConfig(RunConfig config, ExecutionContext context) {
        Properties configProperties = config.getProperties();

        if (ACTION_PROFILE.equals(config.getActionName())) { // action "profile"
            // Get the ProjectTypeProfiler for Maven project
            final MavenProjectTypeProfiler ptp = (MavenProjectTypeProfiler)ProjectUtilities.getProjectTypeProfiler(project);
            
            // Resolve profiling session properties
            Properties sessionProperties = ptp.getLastSessionProperties();
            for (Object k : configProperties.keySet()) {
                String key = (String)k;
                String value = configProperties.getProperty(key);
                if (value.contains(PROFILER_ARGS)) {
                    value = value.replace(PROFILER_ARGS, sessionProperties.getProperty("profiler.info.jvmargs") // NOI18N
                            + " " + sessionProperties.getProperty("profiler.info.jvmargs.agent").replace("\\", "/")); // NOI18N
                    configProperties.setProperty(key, value);
                }
                if (value.contains(PROFILER_JAVA)) {
                    String profilerJava = sessionProperties.getProperty("profiler.info.jvm"); // NOI18N
                    value = value.replace(PROFILER_JAVA,
                            (profilerJava != null && new File(profilerJava).isFile()) ? profilerJava : "java"); // NOI18N
                    configProperties.setProperty(key, value);
                }
            }
            // Set the properties back to config
            config.setProperties(configProperties);
            
            // Attach profiler engine (in separate thread) to profiled process
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    Profiler.getDefault().connectToStartedApp(ptp.getLastProfilingSettings(), ptp.getLastSessionSettings());
                }
            });
            
//        } else if (ACTION_PROFILE_SINGLE.equals(actionName)) { // action "profile-single"
//            // profile-single not supported yet, shouldn't get here
//        } else if (ACTION_PROFILE_TESTS.equals(actionName)) {
//            // profile-tests not supported yet, shouldn't get here // action "profile-tests"
        }
        
        return true;
    }

}
