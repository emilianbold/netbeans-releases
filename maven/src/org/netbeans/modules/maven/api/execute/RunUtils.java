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

package org.netbeans.modules.maven.api.execute;

import java.io.File;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.customizer.WarnPanel;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.MavenCommandLineExecutor;
import org.netbeans.modules.maven.execute.MavenExecutor;
import org.netbeans.modules.maven.execute.MavenJavaExecutor;
import org.netbeans.modules.maven.options.DontShowAgainSettings;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.util.NbBundle;

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
            //TODO somehow use the config.getMavenProject() call rather than looking up the
            // AuxiliaryProperties from lookup. The loaded project can be different from the executed one.
            AuxiliaryProperties props = config.getProject().getLookup().lookup(AuxiliaryProperties.class);
            String val = props.get(Constants.HINT_USE_EXTERNAL, true);
            if ("false".equalsIgnoreCase(val)) { //NOI18N
                useEmbedded = true;
            }
        }
        
        if (!useEmbedded && MavenSettings.canFindExternalMaven()) {
            exec = new MavenCommandLineExecutor(config);
        } else {
            if (!warningShown && DontShowAgainSettings.getDefault().showWarningAboutEmbeddedBuild()) {
                WarnPanel panel = new WarnPanel(NbBundle.getMessage(RunUtils.class, "HINT_EmbeddedBuild"));
                NotifyDescriptor dd = new NotifyDescriptor.Message(panel, NotifyDescriptor.PLAIN_MESSAGE);
                DialogDisplayer.getDefault().notify(dd);
                if (panel.disabledWarning()) {
                    DontShowAgainSettings.getDefault().dontshowWarningAboutEmbeddedBuildAnymore();
                }
                
                warningShown = true;
            }
            exec = new MavenJavaExecutor(config);
        }
        return executeMavenImpl(config.getTaskDisplayName(), exec);
    }

    private static boolean warningShown = false;

    public static RunConfig createRunConfig(File execDir, Project prj, String displayName, List<String> goals)
    {
        BeanRunConfig brc = new BeanRunConfig();
        brc.setExecutionName(displayName);
        brc.setExecutionDirectory(execDir);
        brc.setProject(prj);
        brc.setTaskDisplayName(displayName);
        brc.setGoals(goals);
        return brc;
    }
    
    private static ExecutorTask executeMavenImpl(String runtimeName, MavenExecutor exec) {
        ExecutorTask task =  ExecutionEngine.getDefault().execute(runtimeName, exec, exec.getInputOutput());
        exec.setTask(task);
        return task;
    }

    /**
     *
     * @param project
     * @return true if compile on save is allowed for running the application.
     */
    public static boolean hasApplicationCompileOnSaveEnabled(Project prj) {
        String cos = prj.getLookup().lookup(AuxiliaryProperties.class).get(Constants.HINT_COMPILE_ON_SAVE, true);
        return cos != null && ("all".equalsIgnoreCase(cos) || "app".equalsIgnoreCase(cos));
    }

    /**
     *
     * @param config
     * @return true if compile on save is allowed for running the application.
     */
    public static boolean hasApplicationCompileOnSaveEnabled(RunConfig config) {
        Project prj = config.getProject();
        if (prj != null) {
            return hasApplicationCompileOnSaveEnabled(prj);
        }
        return false;
    }

    /**
     *
     * @param project
     * @return true if compile on save is allowed for running tests.
     */
    public static boolean hasTestCompileOnSaveEnabled(Project prj) {
        String cos = prj.getLookup().lookup(AuxiliaryProperties.class).get(Constants.HINT_COMPILE_ON_SAVE, true);
        //COS for tests is the default value.
        return cos == null || ("all".equalsIgnoreCase(cos) || "test".equalsIgnoreCase(cos));
    }
    /**
     *
     * @param config
     * @return true if compile on save is allowed for running tests.
     */
    public static boolean hasTestCompileOnSaveEnabled(RunConfig config) {
        Project prj = config.getProject();
        if (prj != null) {
            return hasTestCompileOnSaveEnabled(prj);
        }
        return false;
    }

}
