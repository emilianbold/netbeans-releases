/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.profiler.nbimpl.actions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.common.SessionSettings;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.global.Platform;
import org.netbeans.modules.profiler.HeapDumpWatch;
import org.netbeans.modules.profiler.actions.ProfilingSupport;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.api.project.ProjectProfilingSupport;
import org.netbeans.modules.profiler.api.project.ProjectStorage;
import org.netbeans.modules.profiler.nbimpl.NetBeansProfiler;
import org.netbeans.modules.profiler.utils.IDEUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ProfilerLauncher {
    final private static Logger LOG = Logger.getLogger(ProfilerLauncher.class.getName());
    
    final private static String AGENT_ARGS = "agent.jvmargs"; // NOI18N
    final private static String LINUX_THREAD_TIMER_KEY = "-XX:+UseLinuxPosixThreadCPUClocks"; // NOI18N

    final public static class Session  {
        private SessionSettings ss;
        private ProfilingSettings ps;
        private Map<String, String> props;
        private Launcher launcher;
        private Project project;
        private FileObject fo;
        private JavaPlatform platform;
        private String command;
        private Lookup context;
        final private Map<String, Object> customProps = new HashMap<String, Object>();
        
        private boolean configured = false;
        private boolean rerun;

        public static Session createSession(String command, Lookup context) {
            Session s = new Session(command, context);
            config(s);
            lastSession = s;
            
            return s;
        }
        
        public static Session createSession(Project p) {
            Session s = new Session(p);
            config(s);
            return s;
        }
        
        private Session(Project p) {
            this.props = new HashMap<String, String>();
            this.launcher = null;
            this.project = p;
        }
        
        private Session(String command, Lookup context) {
            assert command != null;
            assert context != null;
            
            this.project = context.lookup(Project.class);
            this.fo = context.lookup(FileObject.class);
            this.command = command;
            this.props = new HashMap<String, String>();
            this.context = context;
            
            initLauncher();
        }

        public ProfilingSettings getProfilingSettings() {
            return ps;
        }
        
        public void setProfilingSettings(ProfilingSettings ps) {
            this.ps = ps;
        }
        
        public SessionSettings getSessionSettings() {
            return ss;
        }
        
        public void setSessionSettings(SessionSettings ss) {
            this.ss = ss;
            this.ss.store(props);
        }
        
        public Map<String, String> getProperties() {
            if (!configured) {
                if (configure()) {
                    return props;
                } else {
                    return null;
                }
            }
            return props;
        }
        
        public Project getProject() {
            return project;
        }
        
        public JavaPlatform getPlatform() {
            return platform;
        }
        
        public Lookup getContext() {
            return context;
        }
        
        public FileObject getFile() {
            return fo;
        }
        
        public String getCommand() {
            return command;
        }
        
        public Object getAttribute(String name) {
            return customProps.get(name);
        }
        
        public void setAttribute(String name, Object value) {
            customProps.put(name, value);
        }
        
        public boolean hasAttribute(String name) {
            return customProps.containsKey(name);
        }
        
        public boolean isConfigured() {
            return configured;
        }
        
        public boolean configure() {
            if (ss == null || ss.getJavaExecutable() == null) return false; // No platform defined; fail
            
            final ProjectProfilingSupport pSupport = ProjectProfilingSupport.get(project);

            NetBeansProfiler.getDefaultNB().setProfiledProject(project, fo);

            // ** display select task panel
            ProfilingSettings ps = ProfilingSupport.getDefault().selectTaskForProfiling(project, ss, fo, true);;
            if (ps != null) {
                this.ps = ps;
                this.ps.store(props);
                
                setupAgentEnv(platform, ss, ProfilerIDESettings.getInstance(), ps, project, props);
                pSupport.configurePropertiesForProfiling(props, fo);

                rerun = false;
                configured = true;
            }
            return configured;
        }
        
        public void run() {
            if (launcher != null) {
                launcher.launch(rerun);
                rerun = true;
            } else {
                // LOG
            }
        }
        
        private void initLauncher() {
            Project p = null;
            if (project != null) {
                p = project;
                
            } else if (fo != null) {
                p = FileOwnerQuery.getOwner(fo);
            }
            
            if (p != null) {
                LauncherFactory f = p.getLookup().lookup(LauncherFactory.class);
                if (f != null) {
                    launcher = f.createLauncher(this);
                }
            }
        }
    }
    
    public interface Launcher {
        void launch(boolean rerun);
    }
    
    public interface LauncherFactory {
        Launcher createLauncher(Session session);
    }
    
    @ProjectServiceProvider(service=LauncherFactory.class, projectTypes={
        @ProjectType(id="org-netbeans-modules-java-j2seproject"), 
        @ProjectType(id="org-netbeans-modules-ant-freeform"),
        @ProjectType(id="org-netbeans-modules-apisupport-project"),
        @ProjectType(id="org-netbeans-modules-apisupport-project-suite"),
        @ProjectType(id="org-netbeans-modules-j2ee-earproject"),
        @ProjectType(id="org-netbeans-modules-j2ee-ejbjarproject"),
        @ProjectType(id="org-netbeans-modules-web-project"),
        @ProjectType(id="org-netbeans-modules-autoproject")
    })
    final public static class AntLauncherFactory implements LauncherFactory {
        final private Project prj;
        public AntLauncherFactory(Project prj) {
            this.prj = prj;
        }

        @Override
        public Launcher createLauncher(Session session) {
            ActionProvider ap = prj.getLookup().lookup(ActionProvider.class);
            if (ap != null) {
                return new AntLauncher(ap, session.command, session.context);
            }
            
            return null;
        }
    }
    
    final private static class AntLauncher implements Launcher {
        private ActionProvider ap;
        private String command;
        private Lookup context;

        public AntLauncher(ActionProvider ap, String command, Lookup context) {
            this.ap = ap;
            this.command = command;
            this.context = context;
        }

        @Override
        public void launch(boolean rerun) {
            ap.invokeAction(
                command, 
                context
            );
        }
    }
    
    private static Session lastSession;
    
    public static Session newSession(@NonNull final String command, @NonNull final Lookup context) {
        return Session.createSession(command, context);                
    }
    
    public static Session getLastSession() {
        return lastSession;
    }
    
    public static void clearLastSession() {
        lastSession = null;
    }
    
    public static boolean canRelaunch() {
        return lastSession != null;
    }
    
    @NbBundle.Messages({
        "InvalidPlatformProjectMsg=The Java platform defined for the project is invalid. Right-click the project\nand choose a different platform using Properties | Libraries | Java Platform.\n\nInvalid platform: {0}",
        "InvalidPlatformProfilerMsg=The Java platform defined for profiling is invalid. Choose a different platform\nin Tools | Options | Miscellaneous | Profiler | Profiler Java Platform.\n\nInvalid platform: {0}",
        "FailedDetermineJavaPlatformMsg=Failed to determine version of Java platform: {0}",
        "LBL_Unknown=Unknown"
    })
    private static void config(Session session) {
        Project project = null;
        if (session.project == null) {
            if (session.fo != null) {
                project = FileOwnerQuery.getOwner(session.fo);
            }
        } else {
            project = session.project;
        }
        if (project == null) return; // sanity check; we need project here
        
        if (ProfilingSupport.getDefault().checkProfilingInProgress()) {
            return;
        }
        
        final ProjectProfilingSupport pSupport = ProjectProfilingSupport.get(project);
        if (pSupport == null) {
            return;
        }

        // *** java platform recheck
        JavaPlatform platform = pSupport.getProjectJavaPlatform();
        final String javaFile = platform != null ? platform.getPlatformJavaFile() : null;
        
        if (javaFile == null) {
            if (ProfilerIDESettings.getInstance().getJavaPlatformForProfiling() == null) {
                // used platform defined for project
                ProfilerDialogs.displayError(Bundle.InvalidPlatformProjectMsg(platform != null ? platform.getDisplayName() : Bundle.LBL_Unknown()));
            } else {
                // used platform defined in Options / Profiler
                ProfilerDialogs.displayError(Bundle.InvalidPlatformProfilerMsg(platform != null ? platform.getDisplayName() : Bundle.LBL_Unknown()));
            }
            return;
        }
        
        final SessionSettings ss = new SessionSettings();
        // *** session settings setup
        pSupport.setupProjectSessionSettings(ss);
        ProfilerIDESettings gps = ProfilerIDESettings.getInstance();

        ss.setPortNo(gps.getPortNo());
        // ***

        final String javaVersion = platform.getPlatformJDKVersion();

        if (javaVersion == null) {
            ProfilerDialogs.displayError(Bundle.FailedDetermineJavaPlatformMsg(platform.getDisplayName()));

            return;
        }
        
        session.ss = ss;
        session.platform = platform;
    }
    
    private static void setupAgentEnv(JavaPlatform platform, SessionSettings ss, ProfilerIDESettings gps, ProfilingSettings pSettings, Project project, Map<String, String> props) {
        String javaVersion = platform.getPlatformJDKVersion();
        if (javaVersion.equals(CommonConstants.JDK_15_STRING)) {
            // JDK 1.5 used
            props.put(
                AGENT_ARGS, 
                IDEUtils.getAntProfilerStartArgument15(ss.getPortNo(), ss.getSystemArchitecture())
            );

            if (platform.getPlatformJDKMinor() >= 7) {
                activateOOMProtection(gps, props, project);
            } else {
                ProfilerLogger.log("Profiler.OutOfMemoryDetection: Disabled. Not supported JVM. Use at least 1.4.2_12 or 1.5.0_07"); // NOI18N
            }
        } else if (javaVersion.equals(CommonConstants.JDK_16_STRING)) {
            // JDK 1.6 used
            props.put(
                AGENT_ARGS,
                IDEUtils.getAntProfilerStartArgument16(ss.getPortNo(), ss.getSystemArchitecture())
            );
            activateOOMProtection(gps, props, project);
        } else if (javaVersion.equals(CommonConstants.JDK_17_STRING)) {
            props.put(
                AGENT_ARGS,
                IDEUtils.getAntProfilerStartArgument17(ss.getPortNo(), ss.getSystemArchitecture())
            );
            activateOOMProtection(gps, props, project);
        } else if (javaVersion.equals(CommonConstants.JDK_18_STRING)) {
            props.put(
                AGENT_ARGS,
                IDEUtils.getAntProfilerStartArgument18(ss.getPortNo(), ss.getSystemArchitecture())
            );
            activateOOMProtection(gps, props, project);
        } else {
            throw new IllegalArgumentException("Unsupported JDK " + javaVersion); // NOI18N
        }

        if (Platform.isLinux() && javaVersion.equals(CommonConstants.JDK_16_STRING)) {
            activateLinuxPosixThreadTime(pSettings, props);
        }
        
        props.put("profiler.info.project.dir", project.getProjectDirectory().getPath());
    }
    
    private static void activateLinuxPosixThreadTime(ProfilingSettings ps, Map<String, String> props) {
        if (ps.getThreadCPUTimerOn()) {
            props.put("profiler.info.jvmargs", LINUX_THREAD_TIMER_KEY + " " + props.get("profiler.info.jvmargs")); // NOI18N
            ProfilerLogger.log("Profiler.UseLinuxPosixThreadCPUClocks: Enabled"); // NOI18N
        }
    }

    private static void activateOOMProtection(ProfilerIDESettings gps, Map<String, String> props, Project project) {
        if (gps.isOOMDetectionEnabled()) {
            String oldArgs = props.get("profiler.info.jvmargs");
            oldArgs = (oldArgs != null) ? oldArgs : "";

            StringBuilder oomArgsBuffer = new StringBuilder(oldArgs);
            String heapDumpPath = HeapDumpWatch.getHeapDumpPath(project);

            if ((heapDumpPath != null) && (heapDumpPath.length() > 0)) {
                // used as an argument for starting java process
                if (heapDumpPath.contains(" ")) {
                    heapDumpPath = "\"" + heapDumpPath + "\"";
                }

                oomArgsBuffer.append(" -XX:+HeapDumpOnOutOfMemoryError"); // NOI18N
                oomArgsBuffer.append(" -XX:HeapDumpPath=").append(heapDumpPath).append(" "); // NOI18N

                ProfilerLogger.log("Profiler.OutOfMemoryDetection: Enabled"); // NOI18N
            }

            props.put("profiler.info.jvmargs", oomArgsBuffer.toString()); // NOI18N
        }
    }
}
