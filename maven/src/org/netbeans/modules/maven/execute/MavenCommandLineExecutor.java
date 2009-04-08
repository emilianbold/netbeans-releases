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

package org.netbeans.modules.maven.execute;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.maven.api.execute.RunConfig;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.options.MavenSettings;
import hidden.org.codehaus.plexus.util.StringUtils;
import hidden.org.codehaus.plexus.util.cli.CommandLineUtils;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 * support for executing maven, externally on the command line.
 * @author  Milos Kleint (mkleint@codehaus.org)
 */
public class MavenCommandLineExecutor extends AbstractMavenExecutor {
    static final String ENV_PREFIX = "Env."; //NOI18N
    static final String ENV_JAVAHOME = "Env.JAVA_HOME"; //NOI18N
    
    private ProgressHandle handle;
    private Process process;
    private Process preProcess;
    
    private Logger LOGGER = Logger.getLogger(MavenCommandLineExecutor.class.getName());
    
    
    public MavenCommandLineExecutor(RunConfig conf) {
        super(conf);
        handle = ProgressHandleFactory.createHandle(conf.getTaskDisplayName(), this);
    }
    
    /**
     * not to be called directrly.. use execute();
     */
    public void run() {
        synchronized (SEMAPHORE) {
            if (task == null) {
                try {
                    SEMAPHORE.wait();
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.FINE, "interrupted", ex);
                }
            }
        }

        final BeanRunConfig clonedConfig = new BeanRunConfig(this.config);
        if (clonedConfig.getPreExecution() != null) {
            clonedConfig.setPreExecution(new BeanRunConfig(clonedConfig.getPreExecution()));
        }
        int executionresult = -10;
        InputOutput ioput = getInputOutput();
        ExecutionContext exCon = ActionToGoalUtils.ACCESSOR.createContext(ioput, handle);
        // check the prerequisites
        if (clonedConfig.getProject() != null) {
            Lookup.Result<LateBoundPrerequisitesChecker> result = clonedConfig.getProject().getLookup().lookup(new Lookup.Template<LateBoundPrerequisitesChecker>(LateBoundPrerequisitesChecker.class));
            for (LateBoundPrerequisitesChecker elem : result.allInstances()) {
                if (!elem.checkRunConfig(clonedConfig, exCon)) {
                    return;
                }
                if (clonedConfig.getPreExecution() != null) {
                    if (!elem.checkRunConfig(clonedConfig.getPreExecution(), exCon)) {
                        return;
                    }
                }
            }
        }
        
//        final Properties originalProperties = clonedConfig.getProperties();

        actionStatesAtStart();
        handle.start();
        processInitialMessage();
        CommandLineOutputHandler out = new CommandLineOutputHandler(ioput, clonedConfig.getProject(), handle, clonedConfig);
        try {
            BuildExecutionSupport.registerRunningItem(item);

            if (clonedConfig.getPreExecution() != null) {
                ProcessBuilder builder = constructBuilder(clonedConfig.getPreExecution(), ioput);
                preProcess = builder.start();
                out.setStdOut(preProcess.getInputStream());
                out.setStdIn(preProcess.getOutputStream());
                executionresult = preProcess.waitFor();
                out.waitFor();
                if (executionresult != 0) {
                    return;
                }
            }

//debugging..            
//            Map<String, String> env = builder.environment();
//            for (String key : env.keySet()) {
//                ioput.getOut().println(key + ":" + env.get(key));
//            }
            ProcessBuilder builder = constructBuilder(clonedConfig, ioput);
            process = builder.start();
            out.setStdOut(process.getInputStream());
            out.setStdIn(process.getOutputStream());
            executionresult = process.waitFor();
            out.waitFor();
        } catch (IOException x) {
            //TODO
            LOGGER.log(Level.WARNING , x.getMessage(), x);
        } catch (InterruptedException x) {
            //TODO
            LOGGER.log(Level.WARNING , x.getMessage(), x);
        } catch (ThreadDeath death) {
            if (preProcess != null) {
                preProcess.destroy();
            }
            if (process != null) {
                process.destroy();
            }
            throw death;
        } finally {
            BuildExecutionSupport.registerFinishedItem(item);

            try { //defend against badly written extensions..
                out.buildFinished();
                if (clonedConfig.getProject() != null) {
                    Lookup.Result<ExecutionResultChecker> result = clonedConfig.getProject().getLookup().lookup(new Lookup.Template<ExecutionResultChecker>(ExecutionResultChecker.class));
                    for (ExecutionResultChecker elem : result.allInstances()) {
                        elem.executionResult(clonedConfig, exCon, executionresult);
                    }
                }
            }
            finally {
                //MEVENIDE-623 re add original Properties
//                clonedConfig.setProperties(originalProperties);

                handle.finish();
                ioput.getOut().close();
                ioput.getErr().close();
                actionStatesAtFinish();
                markFreeTab();
                RequestProcessor.getDefault().post(new Runnable() { //#103460
                    public void run() {
                        if (clonedConfig.getProject() != null) {
                            NbMavenProject.fireMavenProjectReload(clonedConfig.getProject());
                        }
                    }
                });
                
            }
            
        }
    }
    
    public boolean cancel() {
        if (preProcess != null) {
            preProcess.destroy();
            preProcess = null;
        }
        if (process != null) {
            process.destroy();
            process = null;
        }
        return true;
    }
        
    private static List<String> createMavenExecutionCommand(RunConfig config) {
        File mavenHome = MavenSettings.getDefault().getCommandLinePath();
        
        List<String> toRet = new ArrayList<String>();
        String ex = Utilities.isWindows() ? "mvn.bat" : "mvn"; //NOI18N
        if (mavenHome != null) {
            File bin = new File(mavenHome, "bin" + File.separator + ex);//NOI18N
            if (bin.exists()) {
                toRet.add(bin.getAbsolutePath());
            } else {
                toRet.add(ex);
            }
        } else {
            toRet.add(ex);
        }
        if (Utilities.isWindows()) { //#153101
            toRet.add(0, "/c"); //NOI18N
            toRet.add(0, "cmd"); //NOI18N
        }
        
        for (Object key : config.getProperties().keySet()) {
            String val = config.getProperties().getProperty((String)key);
            String keyStr = (String)key;
            if (!keyStr.startsWith(ENV_PREFIX)) {
                //skip envs, these get filled in later.
                toRet.add("-D" + key + "=" + val);//NOI18N
            }
        }
        toRet.add("-Dnetbeans.execution=true"); //NOI18N

        LinkedHashSet<String> globals = new LinkedHashSet<String>();

        if (config.isOffline() != null && config.isOffline().booleanValue()) {
            toRet.add("--offline");//NOI18N
        }
        if (!config.isInteractive()) {
            toRet.add("--batch-mode"); //NOI18N
        }
        
        if (!config.isRecursive()) {
            toRet.add("--non-recursive");//NOI18N
        }
        if (config.isShowDebug()) {
            toRet.add("--debug");//NOI18N
        }
        if (config.isShowError()) {
            toRet.add("--errors");//NOI18N
        }
        if (config.isUpdateSnapshots()) {
            toRet.add("--update-snapshots");//NOI18N
        }



        String opts = MavenSettings.getDefault().getDefaultOptions();
        if (opts != null) {
            try {
                String[] s = CommandLineUtils.translateCommandline(opts);
                for (String one : s) {
                    one = one.trim();
                    if (one.startsWith("-D")) {
                        //check against the config.getProperties
                    } else {
                        if (!config.isShowDebug() && (one.equals("-X") || one.equals("--debug"))) {
                            continue;
                        }
                        if (!config.isShowError() && (one.equals("-e") || one.equals("--errors"))) {
                            continue;
                        }
                        if (!config.isUpdateSnapshots() && (one.equals("--update-snapshots") || one.equals("-U"))) {
                            continue;
                        }
                        if (config.isInteractive() && (one.equals("--batch-mode") || one.equals("-B"))) {
                            continue;
                        }
                        if ((config.isOffline() != null && !config.isOffline().booleanValue()) && (one.equals("--offline") || one.equals("-o"))) {
                            continue;
                        }
                    }
                    toRet.add(one);
                }

            } catch (Exception ex1) {
                Logger.getLogger(MavenSettings.class.getName()).fine("Error parsing global options:" + opts);
            }

        }

        String profiles = "";//NOI18N
        
        for (Object profile : config.getActivatedProfiles()) {
            profiles = profiles + "," + profile;//NOI18N
        }
        if (profiles.length() > 0) {
            profiles = profiles.substring(1);
            toRet.add("-P" + profiles);//NOI18N
        }
        
        for (String goal : config.getGoals()) {
            toRet.add(goal);
        }

        return toRet;
    }

    private ProcessBuilder constructBuilder(final RunConfig clonedConfig, InputOutput ioput) {
        List<String> cmdLine = createMavenExecutionCommand(clonedConfig);
        ProcessBuilder builder = new ProcessBuilder(cmdLine);
        builder.redirectErrorStream(true);
        builder.directory(clonedConfig.getExecutionDirectory());
        ioput.getOut().println("NetBeans: Executing '" + StringUtils.join(builder.command().iterator(), " ") + "'"); //NOI18N - to be shown in log.
        boolean hasJavaSet = false;
        for (Object key : clonedConfig.getProperties().keySet()) {
            String keyStr = (String) key;
            if (keyStr.startsWith(ENV_PREFIX)) {
                String env = keyStr.substring(ENV_PREFIX.length());
                String val = clonedConfig.getProperties().getProperty(keyStr);
                builder.environment().put(env, val);
                ioput.getOut().println("NetBeans:      " + env + "=" + val);
                if (keyStr.equals(ENV_JAVAHOME)) {
                    hasJavaSet = true;
                }
            }
        }
        if (!hasJavaSet) {
            if (clonedConfig.getProject() != null) {
                //TODO somehow use the config.getMavenProject() call rather than looking up the
                // ActiveJ2SEPlatformProvider from lookup. The loaded project can be different from the executed one.
                ActiveJ2SEPlatformProvider javaprov = clonedConfig.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class);
                File path = null;
                FileObject java = javaprov.getJavaPlatform().findTool("java"); //NOI18N
                if (java != null) {
                    Collection<FileObject> objs = javaprov.getJavaPlatform().getInstallFolders();
                    for (FileObject fo : objs) {
                        if (FileUtil.isParentOf(fo, java)) {
                            path = FileUtil.toFile(fo);
                            break;
                        }
                    }
                }
                if (path != null) {
                    builder.environment().put(ENV_JAVAHOME.substring(ENV_PREFIX.length()), path.getAbsolutePath());
                    ioput.getOut().println("NetBeans:      JAVA_HOME =" + path.getAbsolutePath());
                    hasJavaSet = true;
                }
            }
            //#151559
            if (!hasJavaSet && System.getenv("JAVA_HOME") == null) {
                //NOI18N
                builder.environment().put("JAVA_HOME", System.getProperty("java.home")); //NOI18N
                ioput.getOut().println("NetBeans:      JAVA_HOME =" + System.getProperty("java.home"));
            }
        }
        return builder;
    }
    
}
