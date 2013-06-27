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

package org.netbeans.modules.maven.execute;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.netbeans.api.extexecution.ExternalProcessSupport;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.LateBoundPrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.cos.CosChecker;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.execute.cmd.Constructor;
import org.netbeans.modules.maven.execute.cmd.ShellConstructor;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * support for executing maven, externally on the command line.
 * @author  Milos Kleint (mkleint@codehaus.org)
 */
public class MavenCommandLineExecutor extends AbstractMavenExecutor {
    static final String ENV_PREFIX = "Env."; //NOI18N
    static final String ENV_JAVAHOME = "Env.JAVA_HOME"; //NOI18N

    private static final String KEY_UUID = "NB_EXEC_MAVEN_PROCESS_UUID"; //NOI18N
    
    private Process process;
    private String processUUID;
    private Process preProcess;
    private String preProcessUUID;
    
    private static final Logger LOGGER = Logger.getLogger(MavenCommandLineExecutor.class.getName());
    
    private static final RequestProcessor RP = new RequestProcessor(MavenCommandLineExecutor.class.getName(),1);
    
    @SuppressWarnings("LeakingThisInConstructor")
    public MavenCommandLineExecutor(RunConfig conf) {
        super(conf);       
    }
    
    /**
     * not to be called directly.. use execute();
     */
    @Override
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
        final InputOutput ioput = getInputOutput();
        
        final ProgressHandle handle = ProgressHandleFactory.createHandle(clonedConfig.getTaskDisplayName(), this, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ioput.select();
            }
        });
        ExecutionContext exCon = ActionToGoalUtils.ACCESSOR.createContext(ioput, handle);
        // check the prerequisites
        if (clonedConfig.getProject() != null) {
            Lookup.Result<LateBoundPrerequisitesChecker> result = clonedConfig.getProject().getLookup().lookupResult(LateBoundPrerequisitesChecker.class);
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
        boolean isMaven3 = !isMaven2();
        boolean singlethreaded = !isMultiThreaded(clonedConfig);
        if (isMaven3 && singlethreaded) {
            injectEventSpy( clonedConfig );
            if (clonedConfig.getPreExecution() != null) {
                injectEventSpy( (BeanRunConfig) clonedConfig.getPreExecution());
            }
        }

        
        CommandLineOutputHandler out = new CommandLineOutputHandler(ioput, clonedConfig.getProject(), handle, clonedConfig, isMaven3 && singlethreaded);
        try {
            BuildExecutionSupport.registerRunningItem(item);
            if (MavenSettings.getDefault().isAlwaysShowOutput()) {
                ioput.select();
            }
            if (clonedConfig.getPreExecution() != null) {
                ProcessBuilder builder = constructBuilder(clonedConfig.getPreExecution(), ioput);
                preProcessUUID = UUID.randomUUID().toString();
                builder.environment().put(KEY_UUID, preProcessUUID);
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
            printCoSWarning(clonedConfig, ioput);
            processUUID = UUID.randomUUID().toString();
            builder.environment().put(KEY_UUID, processUUID);
            process = builder.start();
            out.setStdOut(process.getInputStream());
            out.setStdIn(process.getOutputStream());
            executionresult = process.waitFor();
            out.waitFor();
        } catch (IOException x) {
            if (Utilities.isWindows()) { //#153101
                processIssue153101(x, ioput);
            } else {
                ioput.getErr().println(x.getMessage());
            }
        } catch (InterruptedException x) {
            cancel();
        } catch (ThreadDeath death) {
            cancel();
            throw death;
        } finally {
            BuildExecutionSupport.registerFinishedItem(item);

            try { //defend against badly written extensions..
                out.buildFinished();
                if (clonedConfig.getProject() != null) {
                    Lookup.Result<ExecutionResultChecker> result = clonedConfig.getProject().getLookup().lookupResult(ExecutionResultChecker.class);
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
                actionStatesAtFinish(out.createResumeFromFinder(), out.getExecutionTree());
                markFreeTab();
                RP.post(new Runnable() { //#103460
                    @Override
                    public void run() {
                        //TODO we eventually know the coordinates of all built projects via EventSpy.
                        if (clonedConfig.getProject() != null) {
                            NbMavenProject.fireMavenProjectReload(clonedConfig.getProject());
                        }
                    }
                });
                
            }
            
        }
    }

    private void kill(Process prcs, String uuid) {
        Map<String, String> env = new HashMap<String, String>();
        env.put(KEY_UUID, uuid);
        ExternalProcessSupport.destroy(prcs, env);
    }
    
    @Override
    public boolean cancel() {
        final Process pre = preProcess;
        preProcess = null;
        final Process pro = process;
        process = null;
        RP.post(new Runnable() {
            @Override
            public void run() {
                if (pre != null) {
                    kill(pre, preProcessUUID);
                }
                if (pro != null) {
                    kill(pro, processUUID);
                }
            }
        });
        return true;
    }
        
    private static List<String> createMavenExecutionCommand(RunConfig config, Constructor base) {
        List<String> toRet = new ArrayList<String>(base.construct());

        if (Utilities.isUnix()) { // #198997 - defend against symlinks
            File basedir = config.getExecutionDirectory();
            try {
                if (basedir != null && !basedir.equals(basedir.getCanonicalFile())) {
                    File pom = new File(basedir, "pom.xml");
                    if (pom.isFile()) { // #201400
                        toRet.add("-f");
                        toRet.add(pom.getAbsolutePath());
                    }
                }
            } catch (IOException x) {
                LOGGER.log(Level.FINE, "Could not canonicalize " + basedir, x);
            }
        }

        //#164234
        //if maven.bat file is in space containing path, we need to quote with simple quotes.
        String quote = "\"";
        // the command line parameters with space in them need to be quoted and escaped to arrive
        // correctly to the java runtime on windows
        String escaped = "\\" + quote;        
        for (Map.Entry<? extends String,? extends String> entry : config.getProperties().entrySet()) {
            if (!entry.getKey().startsWith(ENV_PREFIX)) {
                //skip envs, these get filled in later.
                //#228901 since u21 we need to use cmd /c to execute on windows, quotes get escaped and when there is space in value, the value gets wrapped in quotes.
                String value = (Utilities.isWindows() ? entry.getValue().replace(quote, escaped) : entry.getValue().replace(quote, "'"));
                if (Utilities.isWindows() && value.endsWith("\"")) {
                    //#201132 property cannot end with 2 double quotes, add a space to the end after our quote to prevent the state
                    value = value + " ";
                }
                String s = "-D" + entry.getKey() + "=" + (Utilities.isWindows() && value.contains(" ") ? quote + value + quote : value);            
                toRet.add(s);
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
        if (config.isUpdateSnapshots()) {
            toRet.add("--update-snapshots");//NOI18N
        }
        if (config.getReactorStyle() != RunConfig.ReactorStyle.NONE) {
            File basedir = config.getExecutionDirectory();
            MavenProject mp = config.getMavenProject();
            File projdir = NbMavenProject.isErrorPlaceholder(mp) ? basedir : mp.getBasedir();
            String rel = basedir != null && projdir != null ? FileUtilities.relativizeFile(basedir, projdir) : null;
            if (!".".equals(rel)) {
                toRet.add(config.getReactorStyle() == RunConfig.ReactorStyle.ALSO_MAKE ? "--also-make" : "--also-make-dependents");
                toRet.add("--projects");
                toRet.add(rel != null ? rel : mp.getGroupId() + ':' + mp.getArtifactId());
            }
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
                Logger.getLogger(MavenSettings.class.getName()).log(Level.FINE, "Error parsing global options:{0}", opts);
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
        File javaHome = null;
        Map<String, String> envMap = new LinkedHashMap<String, String>();
        for (Map.Entry<? extends String,? extends String> entry : clonedConfig.getProperties().entrySet()) {
            if (entry.getKey().startsWith(ENV_PREFIX)) {
                String env = entry.getKey().substring(ENV_PREFIX.length());
                envMap.put(env, entry.getValue());
                if (entry.getKey().equals(ENV_JAVAHOME)) {
                    javaHome = new File(entry.getValue());
                }
            }
        }
        if (javaHome == null) {
            if (clonedConfig.getProject() != null) {
                //TODO somehow use the config.getMavenProject() call rather than looking up the
                // ActiveJ2SEPlatformProvider from lookup. The loaded project can be different from the executed one.
                ActiveJ2SEPlatformProvider javaprov = clonedConfig.getProject().getLookup().lookup(ActiveJ2SEPlatformProvider.class);
                File path;
                FileObject java = javaprov.getJavaPlatform().findTool("java"); //NOI18N
                if (java != null) {
                    Collection<FileObject> objs = javaprov.getJavaPlatform().getInstallFolders();
                    for (FileObject fo : objs) {
                        if (FileUtil.isParentOf(fo, java)) {
                            path = FileUtil.toFile(fo);
                            if (path != null) {
                                javaHome = path;
                                envMap.put(ENV_JAVAHOME.substring(ENV_PREFIX.length()), path.getAbsolutePath());
                            }
                            break;
                        }
                    }
                }
            }
            //#151559
            if (javaHome == null) {
                if (System.getenv("JAVA_HOME") == null) {
                    //NOI18N
                    javaHome = new File(System.getProperty("java.home"));
                    envMap.put("JAVA_HOME", javaHome.getAbsolutePath()); //NOI18N
                } else {
                    javaHome = new File(System.getenv("JAVA_HOME"));
                    envMap.put("JAVA_HOME", javaHome.getAbsolutePath()); //NOI18N
                }
            }
        }

        File mavenHome = EmbedderFactory.getEffectiveMavenHome();
        Constructor constructeur = new ShellConstructor(mavenHome);

        List<String> cmdLine = createMavenExecutionCommand(clonedConfig, constructeur);
        
        //#228901 on windows, since u21 we must use cmd /c
        // the working format is ""C:\Users\mkleint\space in path\apache-maven-3.0.4\bin\mvn.bat"
                           //-Dexec.executable=java -Dexec.args="-jar
                           //C:\Users\mkleint\Documents\NetBeansProjects\JavaApplication13\dist\JavaApplication13.jar
                           //-Dxx=\"space path\" -Dfoo=bar" exec:exec""
        if (cmdLine.get(0).equals("cmd")) {
            //merge all items after cmd /c into one string and quote it.
            StringBuilder sb = new StringBuilder();
            Iterator<String> it = cmdLine.iterator();
            //sb.append("cmd.exe /c ");
            it.next(); //cmd
            it.next(); //c
            String m = it.next();
            //this sounds weird but is true. if the bat file has spaces it has to be eclosed in quotes
            // but then on start and end of the entire string we need exactly 2 quotes. so sometimes it's ""aaa.bat and sometimes ""aa bb.bat" 
            if (m.startsWith("\"")) {
                sb.append("\"");
            } else {
                sb.append("\"\"");
            }
            sb.append(m);
            while (it.hasNext()) {
                sb.append(" ").append(it.next());
            }
            //XXX here we somehow assume that the last entry in line is the goal and it doesn't need to be enclosed in quotes itself. 3 quotes in line would break things.
            sb.append("\"\"");
            cmdLine = Arrays.asList(new String[] {
                "cmd", "/c", sb.toString() //merge everything into one item here..
            });
        }

        ProcessBuilder builder = new ProcessBuilder(cmdLine);
        builder.redirectErrorStream(true);
        builder.directory(clonedConfig.getExecutionDirectory());
        StringBuilder display = new StringBuilder("cd ").append(clonedConfig.getExecutionDirectory()).append("; "); // NOI18N
        for (Map.Entry<String, String> entry : envMap.entrySet()) {
            String env = entry.getKey();
            String val = entry.getValue();
            if ("M2_HOME".equals(env.toUpperCase(Locale.ENGLISH))) {
                continue;// #191374: would prevent bin/mvn from using selected installation
            }
            // TODO: do we really put *all* the env vars there? maybe filter, M2_HOME and JDK_HOME?
            builder.environment().put(env, val);
            display.append(Utilities.escapeParameters(new String[] {env + "=" + val})).append(' '); // NOI18N
        }
       
        //#195039
        builder.environment().put("M2_HOME", mavenHome.getAbsolutePath());
        if (!mavenHome.equals(EmbedderFactory.getDefaultMavenHome())) {
            //only relevant display when using the non-default maven installation.
            display.append(Utilities.escapeParameters(new String[] {"M2_HOME=" + mavenHome.getAbsolutePath()})).append(' '); // NOI18N
        }

        //very hacky here.. have a way to remove
        List<String> command = new ArrayList<String>(builder.command());
        for (Iterator<String> it = command.iterator(); it.hasNext();) {
            String s = it.next();
            if (s.startsWith("-D" + CosChecker.MAVENEXTCLASSPATH + "=") || s.startsWith("-D" + CosChecker.NETBEANS_PROJECT_MAPPINGS + "=")) {
                it.remove();
            }
        }
        display.append(Utilities.escapeParameters(command.toArray(new String[command.size()])));
        printGray(ioput, display.toString());
        
        return builder;
    }

    private static void printGray(InputOutput io, String text) {
        if (IOColorLines.isSupported(io)) {
            try {
                IOColorLines.println(io, text, IOColors.getColor(io, IOColors.OutputType.LOG_DEBUG));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            io.getOut().println(text);
        }
    }

    private void processIssue153101(IOException x, InputOutput ioput) {
        //#153101
        if (x.getMessage() != null && x.getMessage().contains("CreateProcess error=5")) {
            System.setProperty("maven.run.cmd", "true");
            LOGGER.log(Level.INFO, "Cannot create Process, next time we will run the build with 'cmd /c'", x); //NOI18N
            ioput.getErr().println("Cannot execute the mvn.bat executable directly due to wrong access rights, switching to execution via 'cmd.exe /c mvn.bat'."); //NOI18N - in maven output
            try {
                ioput.getErr().println("  See issue http://www.netbeans.org/issues/show_bug.cgi?id=153101 for details.", new OutputListener() {                    //NOI18N - in maven output
                    @Override
                    public void outputLineSelected(OutputEvent ev) {}
                    @Override
                    public void outputLineCleared(OutputEvent ev) {}
                    @Override
                    public void outputLineAction(OutputEvent ev) {
                        try {
                            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL("http://www.netbeans.org/issues/show_bug.cgi?id=153101")); //NOI18N - in maven output
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            ioput.getErr().println("  This message will show on the next start of the IDE again, to skip it, add -J-Dmaven.run.cmd=true to your etc/netbeans.conf file in your NetBeans installation."); //NOI18N - in maven output
            ioput.getErr().println("The detailed exception output is printed to the IDE's log file."); //NOI18N - in maven output
            RP.post(new Runnable() {
                @Override
                public void run() {
                    RunConfig newConfig = new BeanRunConfig(config);
                    RunUtils.executeMaven(newConfig);
                }
            });
        } else {
            ioput.getErr().println(x.getMessage());
        }
    }

    private void printCoSWarning(BeanRunConfig clonedConfig, InputOutput ioput) {
        if (clonedConfig.getProperties().containsKey(CosChecker.NETBEANS_PROJECT_MAPPINGS)) {
            printGray(ioput, "Running NetBeans Compile On Save execution. Phase execution is skipped and output directories of dependency projects (with Compile on Save turned on) will be used instead of their jar artifacts.");
            if (isMaven2()) {
                printGray(ioput, "WARNING: Using Maven 2.x for execution, NetBeans cannot establish links between current project and output directories of dependency projects with Compile on Save turned on. Only works with Maven 3.0+.");
            }
            
        }
    }
    
    boolean isMaven2() {
        File mvnHome = EmbedderFactory.getEffectiveMavenHome();
        String version = MavenSettings.getCommandLineMavenVersion(mvnHome);
        return version != null && version.startsWith("2");
    }

    private void injectEventSpy(final BeanRunConfig clonedConfig) {
        //TEMP 
        String mavenPath = clonedConfig.getProperties().get(CosChecker.MAVENEXTCLASSPATH);
        if (mavenPath == null) {
            mavenPath = "";
        } else {
            mavenPath = mavenPath + (Utilities.isWindows() ? ";" : ":");
        }
        File jar = InstalledFileLocator.getDefault().locate("maven-nblib/netbeans-eventspy.jar", "org.netbeans.modules.maven", false);
        mavenPath = mavenPath + jar.getAbsolutePath();
        clonedConfig.setProperty(CosChecker.MAVENEXTCLASSPATH, mavenPath);
    }

    private boolean isMultiThreaded(BeanRunConfig clonedConfig) {
        String list = MavenSettings.getDefault().getDefaultOptions();
        for (String s : clonedConfig.getGoals()) {
            list = list + " " + s;
        }
        if (clonedConfig.getPreExecution() != null) {
            for (String s : clonedConfig.getPreExecution().getGoals()) {
                list = list + " " + s;
            }
        }
        return list.contains("-T ") || list.contains("--threads ");
    } 
}
