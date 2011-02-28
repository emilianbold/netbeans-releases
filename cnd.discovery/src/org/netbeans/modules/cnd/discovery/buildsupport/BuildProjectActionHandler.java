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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.cnd.discovery.wizard.BuildActionsProviderImpl;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.OutputStreamHandler;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HelperUtility;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;

/**
 *
 * @author Alexander Simon
 */
/* package-local */ class BuildProjectActionHandler implements ProjectActionHandler {

    private ProjectActionHandler delegate;
    private ProjectActionEvent pae;
    private ExecutionEnvironment execEnv;
    private final List<ExecutionListener> listeners = new CopyOnWriteArrayList<ExecutionListener>();
    private Collection<OutputStreamHandler> outputHandlers;

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
    }

    @Override
    public void addExecutionListener(ExecutionListener l) {
        delegate.addExecutionListener(l);
        listeners.add(l);
    }

    @Override
    public void removeExecutionListener(ExecutionListener l) {
        delegate.removeExecutionListener(l);
        listeners.remove(l);
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
        final ExecutionListener listener = new ExecutionListener() {
            @Override
            public void executionStarted(int pid) {
            }
            @Override
            public void executionFinished(int rc) {
                delegate.removeExecutionListener(this);
            }
        };
        delegate.addExecutionListener(listener);
        File execLog = null;
        try {
            execLog = File.createTempFile("exec", ".log"); // NOI18N
            execLog.deleteOnExit();
            if (outputHandlers != null) {
                for(OutputStreamHandler handler : outputHandlers) {
                    if (handler instanceof BuildActionsProviderImpl.ConfigureAction) {
                        BuildActionsProviderImpl.ConfigureAction myHandler = (BuildActionsProviderImpl.ConfigureAction) handler;
                        myHandler.setExecLog(execLog);
                    }
                }
            }
        } catch (IOException ex) {
        }
        if (execLog != null) {
            Env env = pae.getProfile().getEnvironment();
            env.putenv(BuildTraceSupport.CND_TOOLS,BuildTraceSupport.CND_TOOLS_VALUE);
            env.putenv(BuildTraceSupport.CND_BUILD_LOG,execLog.getAbsolutePath());
            try {
                String dll = BuildTraceHelper.INSTANCE.getPath(execEnv);
                String path = MacroExpanderFactory.getExpander(execEnv).expandPredefinedMacros("$osname-${platform}"); // NOI18N
                File where = new File(dll).getParentFile().getParentFile();
                path = where.getAbsolutePath() + "/" + path; // NOI18N
                env.putenv("LD_PRELOAD", new File(dll).getName() + ":${LD_PRELOAD}"); // NOI18N
                env.putenv("LD_LIBRARY_PATH", path + ":" + path + "_64" + ":${LD_LIBRARY_PATH}"); // NOI18N
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        delegate.execute(io);
    }

    private static final class BuildTraceHelper extends HelperUtility {
        private static final BuildTraceHelper INSTANCE = new BuildTraceHelper();
        private BuildTraceHelper() {
            super("org.netbeans.modules.cnd.actions", "bin/$osname-${platform}$_isa/libBuildTrace.so"); // NOI18N
        }
    }
}
