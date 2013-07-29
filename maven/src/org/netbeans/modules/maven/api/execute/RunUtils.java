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

import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.MavenCommandLineExecutor;
import org.netbeans.modules.maven.execute.MavenExecutor;
import org.netbeans.modules.maven.execute.ProxyNonSelectableInputOutput;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.LifecycleManager;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.WindowManager;

/**
 * Utility method for executing a maven build, using the RunConfig.
 * @author mkleint
 */
public final class RunUtils {
    
    /** Creates a new instance of RunUtils */
    private RunUtils() {
    }

    /**
     * Runs Maven after checking prerequisites.
     * @param config a run configuration (try {@link #createRunConfig})
     * @return a task to track progress, or null if prerequisites were not satisfied
     * @see #executeMaven
     * @see PrerequisitesChecker
     * @since 2.18
     */
    public static @CheckForNull ExecutorTask run(RunConfig config) {
        SwingUtilities.invokeLater(new Runnable() { //#233275
            @Override
            public void run() {
                JFrame frm = (JFrame) WindowManager.getDefault().getMainWindow();
                frm.getGlassPane().setVisible(true);
                frm.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                frm.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        });
        try {
        for (PrerequisitesChecker elem : config.getProject().getLookup().lookupAll(PrerequisitesChecker.class)) {
            if (!elem.checkRunConfig(config)) {
                return null;
            }
            if (config.getPreExecution() != null) {
                if (!elem.checkRunConfig(config.getPreExecution())) {
                    return null;
                }
            }
        }
        return executeMaven(config);
        } finally {
            SwingUtilities.invokeLater(new Runnable() { //#233275
            @Override
            public void run() {
                JFrame frm = (JFrame) WindowManager.getDefault().getMainWindow();
                frm.getGlassPane().setVisible(false);
                frm.getGlassPane().setCursor(null);
                frm.setCursor(null);
            }
        });
        }
    }
    
    /**
     * Execute maven build in NetBeans execution engine.
     * Most callers should rather use {@link #run} as this variant does no (non-late-bound) prerequisite checks.
     * It is mostly suitable for cases where you need full control by the caller over the config, or want to rerun a previous execution.
     */
    public static ExecutorTask executeMaven(final RunConfig config) {
        // save all edited files.. maybe finetune for project's files only, however that would fail for multiprojects..
        LifecycleManager.getDefault().saveAll();

        MavenExecutor exec = new MavenCommandLineExecutor(config);
        ExecutorTask task = executeMavenImpl(config.getTaskDisplayName(), exec);
        // fire project change on when finishing maven execution, to update the classpath etc. -MEVENIDE-83
        task.addTaskListener(new TaskListener() {
            @Override public void taskFinished(Task t) {
                // fireMavenProjectReload is done in executors
                MavenProject mp = config.getMavenProject();
                if (mp == null) {
                    return;
                }
                List<Artifact> arts = new ArrayList<Artifact>();
                Artifact main = mp.getArtifact();
                if (main != null) { // #157572?
                    arts.add(main);
                }
                arts.addAll(mp.getArtifacts());
                RepositoryIndexer.updateIndexWithArtifacts(RepositoryPreferences.getInstance().getLocalRepository(), arts);
            }
        });
        return task;
    }

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
    
    private static ExecutorTask executeMavenImpl(String runtimeName, final MavenExecutor exec) {
        ExecutorTask task =  ExecutionEngine.getDefault().execute(runtimeName, exec, new ProxyNonSelectableInputOutput(exec.getInputOutput()));
        exec.setTask(task);
        return task;
    }
    
    /**
     * return a new instance of runconfig by the template passed as parameter
     * @param original
     * @return 
     * @since 2.40
     */
    public static RunConfig cloneRunConfig(RunConfig original) {
        return new BeanRunConfig(original);
    }

    
    public static boolean isCompileOnSaveEnabled(Project prj) {
        AuxiliaryProperties auxprops = prj.getLookup().lookup(AuxiliaryProperties.class);
        if (auxprops == null) {
            // Cannot use ProjectUtils.getPreferences due to compatibility.
            return false;
        }
        String cos = auxprops.get(Constants.HINT_COMPILE_ON_SAVE, true);
        if (cos == null) {
            cos = "all";
        }
        return !"none".equalsIgnoreCase(cos);    
    }
    
    public static boolean isCompileOnSaveEnabled(RunConfig config) {
        Project prj = config.getProject();
        if (prj != null) {
            return isCompileOnSaveEnabled(prj);
        }
        return false;
    }
    
    /**
     *
     * @param project
     * @return true if compile on save is allowed for running the application.
     */
    @Deprecated
    public static boolean hasApplicationCompileOnSaveEnabled(Project prj) {
        AuxiliaryProperties auxprops = prj.getLookup().lookup(AuxiliaryProperties.class);
        if (auxprops == null) {
            // Cannot use ProjectUtils.getPreferences due to compatibility.
            return false;
        }
        String cos = auxprops.get(Constants.HINT_COMPILE_ON_SAVE, true);
        if (cos == null) {
            cos = "all";
//            String packaging = prj.getLookup().lookup(NbMavenProject.class).getPackagingType();
//            if ("war".equals(packaging) || "ejb".equals(packaging) || "ear".equals(packaging)) {
//                cos = "app";
//            } else {
//                cos = "none";
//            }
        }
        return "all".equalsIgnoreCase(cos) || "app".equalsIgnoreCase(cos);
    }

    /**
     *
     * @param config
     * @return true if compile on save is allowed for running the application.
     */
    @Deprecated 
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
    @Deprecated
    public static boolean hasTestCompileOnSaveEnabled(Project prj) {
        AuxiliaryProperties auxprops = prj.getLookup().lookup(AuxiliaryProperties.class);
        if (auxprops == null) {
            // Cannot use ProjectUtils.getPreferences due to compatibility.
            return true;
        }
        String cos = auxprops.get(Constants.HINT_COMPILE_ON_SAVE, true);
        if (cos == null) {
            cos = "all";

//            String packaging = prj.getLookup().lookup(NbMavenProject.class).getPackagingType();
//            if ("war".equals(packaging) || "ejb".equals(packaging) || "ear".equals(packaging)) {
//                cos = "app";
//            } else {
//                cos = "none";
//            }
        }
        return "all".equalsIgnoreCase(cos) || "test".equalsIgnoreCase(cos);
    }
    /**
     *
     * @param config
     * @return true if compile on save is allowed for running tests.
     */
    @Deprecated
    public static boolean hasTestCompileOnSaveEnabled(RunConfig config) {
        Project prj = config.getProject();
        if (prj != null) {
            return hasTestCompileOnSaveEnabled(prj);
        }
        return false;
    }

}
