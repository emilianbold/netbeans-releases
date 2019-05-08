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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.buildsupport;

import org.netbeans.modules.cnd.discovery.api.BuildTraceSupport;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProviderFactory;
import org.netbeans.modules.cnd.discovery.services.DiscoveryManagerImpl;
import org.netbeans.modules.cnd.discovery.wizard.BuildActionsProviderImpl;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryExtension;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.OutputStreamHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.windows.InputOutput;

/**
 *
 */
public class BuildProjectActionHandler implements ProjectActionHandler {

    private ProjectActionHandler delegate;
    private ProjectActionEvent pae;
    private ExecutionEnvironment execEnv;
    private final List<ExecutionListener> listeners = new CopyOnWriteArrayList<>();
    private Collection<OutputStreamHandler> outputHandlers;
    private BuildTraceSupport.BuildTrace support;
    static final Logger logger = Logger.getLogger(BuildProjectActionHandler.class.getName());

    /* package-local */
    BuildProjectActionHandler() {
    }
    
    @Override
    public void init(ProjectActionEvent pae, ProjectActionEvent[] paes, Collection<OutputStreamHandler> outputHandlers) {
        this.pae = pae;
        this.delegate = BuildProjectActionHandlerFactory.createDelegateHandler(pae);
        this.delegate.init(pae, paes, outputHandlers);
        this.execEnv = pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment();
        this.outputHandlers = outputHandlers;
        this.support = BuildTraceSupport.supportedPlatforms(execEnv, pae.getConfiguration(), pae.getProject());
    }

    @Override
    public void addExecutionListener(ExecutionListener l) {
        delegate.addExecutionListener(l);
        if (pae.getType() == ProjectActionEvent.PredefinedType.BUILD) {
            listeners.add(l);
        }
    }

    @Override
    public void removeExecutionListener(ExecutionListener l) {
        delegate.removeExecutionListener(l);
        if (pae.getType() == ProjectActionEvent.PredefinedType.BUILD) {
            listeners.remove(l);
        }
    }

    @Override
    public boolean canCancel() {
        return delegate.canCancel();
    }

    @Override
    public void cancel() {
        delegate.cancel();
    }
    
    @Override
    public void execute(InputOutput io) {
        if (pae.getType() == ProjectActionEvent.PredefinedType.PRE_BUILD ||
            pae.getType() == ProjectActionEvent.PredefinedType.CLEAN ||
            pae.getType() == ProjectActionEvent.PredefinedType.BUILD) {
            support.modifyEnv(pae.getProfile().getEnvironment());
        }
        if (pae.getType() == ProjectActionEvent.PredefinedType.BUILD) {
            executeBuild(io);
        } else {
            delegate.execute(io);
        }
    }

    private void executeBuild(InputOutput io) {
        File execLog;
        String remoteExecLog = null;
        try {
            execLog = File.createTempFile("exec", ".log"); // NOI18N
            execLog.deleteOnExit();
            if (execEnv.isRemote()) {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
                remoteExecLog = hostInfo.getTempDir()+"/"+execLog.getName(); // NOI18N
            }
        } catch (CancellationException ex) {
            execLog = null;
        } catch (IOException ex) {
            execLog = null;
        }
        if (execLog != null) {
            Env env = pae.getProfile().getEnvironment();
            env.putenv(BuildTraceSupport.CND_TOOLS,BuildTraceSupport.getTools(pae.getConfiguration(), execEnv));
            if (execEnv.isRemote()) {
                env.putenv(BuildTraceSupport.CND_BUILD_LOG,remoteExecLog);
            } else {
                env.putenv(BuildTraceSupport.CND_BUILD_LOG,execLog.getAbsolutePath());
            }
            if (support.getKind() == BuildTraceSupport.BuildTraceKind.Preload) {
                try {
                    support.modifyPreloadEnv(env);
                } catch (CancellationException ex) {
                    // don't report CancellationException
                } catch (IOException ex) {
                    io.getErr().println(ex.getLocalizedMessage());
                }
            }
        }
        final ExecLogWrapper wrapper = new ExecLogWrapper(execLog, execEnv);
        if (outputHandlers != null) {
            for(OutputStreamHandler handler : outputHandlers) {
                if (handler instanceof BuildActionsProviderImpl.ConfigureAction) {
                    BuildActionsProviderImpl.ConfigureAction myHandler = (BuildActionsProviderImpl.ConfigureAction) handler;
                    myHandler.setExecLog(wrapper);
                }
            }
        }
        final ExecutionListener listener = new ExecutionListener() {
            @Override
            public void executionStarted(int pid) {
            }
            @Override
            public void executionFinished(int rc) {
                delegate.removeExecutionListener(this);
                reconfigureCodeAssistance(rc, wrapper);
                
            }
        };
        delegate.addExecutionListener(listener);
        delegate.execute(io);
    }

    private void reconfigureCodeAssistance(int rc, ExecLogWrapper execLog) {
        DiscoveryProvider provider = null;
        if (execLog.getExecLog() != null) {
            provider = DiscoveryProviderFactory.findProvider(DiscoveryExtension.EXEC_LOG_PROVIDER);
        }
        if (false) {
            // use incremental configure code assistance only for interceptor.
            if (provider == null) {
                provider = DiscoveryProviderFactory.findProvider(DiscoveryExtension.MAKE_LOG_PROVIDER);
            }
        }
        if (provider == null) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        if (DiscoveryExtension.EXEC_LOG_PROVIDER.equals(provider.getID())) {
            map.put(DiscoveryManagerImpl.BUILD_EXEC_KEY, execLog.getExecLog());
        } else {
            map.put(DiscoveryManagerImpl.BUILD_LOG_KEY, execLog.getBuildLog());
        }
        DiscoveryManagerImpl.projectBuilt(pae.getProject(), map, true);
    }
    
    public static final class ExecLogWrapper {
        private File execLog;
        private String buildLog;
        private final ExecutionEnvironment execEnv;
        private final AtomicBoolean downloadedExecLog = new AtomicBoolean(false);
        
        public ExecLogWrapper(File execLog, ExecutionEnvironment execEnv){
            this.execLog = execLog;
            this.execEnv = execEnv;
        }

        private void downloadExecLog() {
            if (execLog != null && !downloadedExecLog.get()) {
                if (execEnv.isRemote()) {
                    String remoteExecLog = "?"; //NOI18N
                    try {
                        HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
                        remoteExecLog = hostInfo.getTempDir()+"/"+execLog.getName(); // NOI18N
                        if (HostInfoUtils.fileExists(execEnv, remoteExecLog)){
                            Future<Integer> task = CommonTasksSupport.downloadFile(remoteExecLog, execEnv, execLog.getAbsolutePath(), null);
                            /*int rc =*/ task.get();
                            CommonTasksSupport.rmFile(execEnv, remoteExecLog, null);
                        } else {
                            execLog = null;
                        }
                    } catch (Throwable ex) {
                        logger.log(Level.INFO, "BuildProjectActionHandler cannot download file {0} from {1} to {2}. Exception {3}: {4}", 
                                new Object[]{remoteExecLog, execEnv, execLog.getAbsolutePath(), ex.getClass().getName(), ex.getMessage()}); // NOI18N
                        execLog = null;                        
                        logger.log(Level.FINE, ex.getLocalizedMessage(), ex);
                    }
                    downloadedExecLog.set(true);
                }
            }
        }
        
        public void setBuildLog(String buildLog) {
            this.buildLog = buildLog;
        }

        public String getBuildLog() {
            return buildLog;
        }
        
        public synchronized String getExecLog() {
            downloadExecLog();
            if (execLog != null) {
                return execLog.getAbsolutePath();
            }
            return null;
        }
    }
}
