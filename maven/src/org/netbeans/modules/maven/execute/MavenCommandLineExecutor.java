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
import java.util.Properties;
import org.apache.maven.execution.MavenExecutionRequest;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.options.MavenExecutionSettings;
import hidden.org.codehaus.plexus.util.StringUtils;
import java.util.Collection;
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
    private CommandLineOutputHandler out;
    private Process process;
    
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

        final RunConfig clonedConfig = new BeanRunConfig(this.config);
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
            }
        }
        
        final Properties origanalProperties = clonedConfig.getProperties();
        actionStatesAtStart();
        handle.start();
        processInitialMessage();
        try {
            BuildExecutionSupport.registerRunningItem(item);

            out = new CommandLineOutputHandler(ioput, clonedConfig.getProject(), handle, clonedConfig);
            
            File workingDir = clonedConfig.getExecutionDirectory();
            List<String> cmdLine = createMavenExecutionCommand(clonedConfig);
            ProcessBuilder builder = new ProcessBuilder(cmdLine);
            builder.redirectErrorStream(true);
            builder.directory(workingDir);
//            builder.environment();
            
            ioput.getOut().println("NetBeans: Executing '" + StringUtils.join(builder.command().iterator(), " ") + "'");//NOI18N - to be shown in log.
            boolean hasJavaSet = false;
            for (Object key : clonedConfig.getProperties().keySet()) {
                String keyStr = (String)key;
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
                if (config.getProject() != null) {
                    //TODO somehow use the config.getMavenProject() call rather than looking up the
                    // ActiveJ2SEPlatformProvider from lookup. The loaded project can be different from the executed one.
                    ActiveJ2SEPlatformProvider javaprov = config.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class);
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
                    }
                }
            }
//debugging..            
//            Map<String, String> env = builder.environment();
//            for (String key : env.keySet()) {
//                ioput.getOut().println(key + ":" + env.get(key));
//            }
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
                clonedConfig.setProperties(origanalProperties);

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
        if (process != null) {
            process.destroy();
            process = null;
        }
        return true;
    }
        
    private static List<String> createMavenExecutionCommand(RunConfig config) {
        File mavenHome = MavenExecutionSettings.getDefault().getCommandLinePath();
        
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
        
        for (Object key : config.getProperties().keySet()) {
            String val = config.getProperties().getProperty((String)key);
            String keyStr = (String)key;
            if (!keyStr.startsWith(ENV_PREFIX)) {
                //skip envs, these get filled in later.
                toRet.add("-D" + key + "=" + val);//NOI18N
            }
        }

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
        if (!MavenExecutionSettings.getDefault().isUsePluginRegistry()) {
            toRet.add("--no-plugin-registry");//NOI18N
        }
        String checksum = MavenExecutionSettings.getDefault().getChecksumPolicy();
        if (checksum != null) {
            if (MavenExecutionRequest.CHECKSUM_POLICY_FAIL.equals(checksum)) {
                toRet.add("--strict-checksums");//NOI18N
            }
            if (MavenExecutionRequest.CHECKSUM_POLICY_WARN.equals(checksum)) {
                toRet.add("--lax-checksums");//NOI18N
            }
        }
        if (config.isUpdateSnapshots()) {
            toRet.add("--update-snapshots");//NOI18N
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
    
}
