/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.sync;

import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.windows.InputOutput;

/**
 *
 * @author Vladimir Kvashin
 */
/* package-local */
class RemoteBuildProjectActionHandler implements ProjectActionHandler {

    private static final Logger logger = Logger.getLogger("cnd.remote.logger");

    private ProjectActionHandler delegate;
    private ProjectActionEvent pae;
    private ProjectActionEvent[] paes;
    private ExecutionEnvironment execEnv;
    
    /* package-local */
    RemoteBuildProjectActionHandler() {
    }
    
    @Override
    public void init(ProjectActionEvent pae, ProjectActionEvent[] paes) {
        this.pae = pae;
        this.paes = paes;
        this.delegate = RemoteBuildProjectActionHandlerFactory.createDelegateHandler(pae);
        this.delegate.init(pae, paes);
        this.execEnv = pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment();
        initRfsIfNeed();
    }

    private void initRfsIfNeed() {
        if (RfsSyncFactory.ENABLE_RFS) {            
            if (execEnv.isRemote()) {
                if (ServerList.get(execEnv).getSyncFactory().getID().equals(RfsSyncFactory.ID)) {
                    initRfs();
                }
            }
        }
    }

    private void initRfs() {
        // FIXUP until remote host setup is done
        final Env env = pae.getProfile().getEnvironment();
        String preload = System.getProperty("cnd.remote.fs.preload");
        if (preload != null) {
            env.putenv("LD_PRELOAD", preload);
        }
        String dir =  System.getProperty("cnd.remote.fs.dir");
        if (dir != null) {
            env.putenv("RFS_CONTROLLER_DIR", dir);
        }
        String preloadLog = System.getProperty("cnd.remote.fs.preload.log");
        if (preloadLog != null) {
            env.putenv("RFS_PRELOAD_LOG", preloadLog);
        }
        String controllerLog = System.getProperty("cnd.remote.fs.controller.log");
        if (controllerLog != null) {
            env.putenv("RFS_CONTROLLER_LOG", controllerLog);
        }
        delegate.addExecutionListener(new ExecutionListener() {
            public void executionStarted(int pid) {
                logger.fine("RemoteBuildProjectActionHandler: build started; PID=" + pid);
            }
            public void executionFinished(int rc) {
                logger.fine("RemoteBuildProjectActionHandler: build finished; RC=" + rc);
            }
        });
    }

    @Override
    public void addExecutionListener(ExecutionListener l) {
        delegate.addExecutionListener(l);
    }

    @Override
    public void removeExecutionListener(ExecutionListener l) {
        delegate.removeExecutionListener(l);
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
        delegate.execute(io);
    }
}
