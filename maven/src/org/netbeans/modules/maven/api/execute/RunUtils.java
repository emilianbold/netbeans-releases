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

package org.netbeans.modules.maven.api.execute;

import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.execute.MavenCommandLineExecutor;
import org.netbeans.modules.maven.execute.MavenExecutor;
import org.netbeans.modules.maven.execute.MavenJavaExecutor;
import org.netbeans.modules.maven.options.MavenExecutionSettings;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;

/**
 * Utility method for executing a maven build, using the RunConfig.
 * @author mkleint
 */
public final class RunUtils {
    
    
    /** Creates a new instance of RunUtils */
    private RunUtils() {
    }
    
    /**
     *  execute maven build in netbeans execution engine.
     */
    public static ExecutorTask executeMaven(RunConfig config) {
        MavenExecutor exec;
        boolean useEmbedded = false;
        if (config.getProject() != null) {
            AuxiliaryProperties props = config.getProject().getLookup().lookup(AuxiliaryProperties.class);
            String val = props.get(Constants.HINT_USE_EXTERNAL, true);
            if ("false".equalsIgnoreCase(val)) { //NOI18N
                useEmbedded = true;
            }
        }
        
        if (!useEmbedded && MavenExecutionSettings.canFindExternalMaven()) {
            exec = new MavenCommandLineExecutor(config);
        } else {
            exec = new MavenJavaExecutor(config);
        }
        return executeMavenImpl(config.getTaskDisplayName(), exec);
    }

    
    
    private static ExecutorTask executeMavenImpl(String runtimeName, MavenExecutor exec) {
        ExecutorTask task =  ExecutionEngine.getDefault().execute(runtimeName, exec, exec.getInputOutput());
        exec.setTask(task);
        return task;
    }

    /**
     *
     * @param config
     * @return true if compile on save is allowed for running the application.
     */
    public static boolean hasApplicationCompileOnSaveEnabled(RunConfig config) {
        Project prj = config.getProject();
        if (prj != null) {
              String cos = prj.getLookup().lookup(AuxiliaryProperties.class).get(Constants.HINT_COMPILE_ON_SAVE, true);
              return cos != null && ("all".equalsIgnoreCase(cos) || "app".equalsIgnoreCase(cos));
        }
        return false;
    }

    /**
     *
     * @param config
     * @return true if compile on save is allowed for running tests.
     */
    public static boolean hasTestCompileOnSaveEnabled(RunConfig config) {
        Project prj = config.getProject();
        if (prj != null) {
              String cos = prj.getLookup().lookup(AuxiliaryProperties.class).get(Constants.HINT_COMPILE_ON_SAVE, true);
              return cos != null && ("all".equalsIgnoreCase(cos) || "test".equalsIgnoreCase(cos));
        }
        return false;
    }

}
