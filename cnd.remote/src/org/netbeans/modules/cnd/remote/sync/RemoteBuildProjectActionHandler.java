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

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.remote.support.RemoteException;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 *
 * @author Vladimir Kvashin
 */
/* package-local */
class RemoteBuildProjectActionHandler implements ProjectActionHandler {

    private ProjectActionHandler delegate;
    private ProjectActionEvent pae;
    private ProjectActionEvent[] paes;
    private ExecutionEnvironment execEnv;

    private PrintWriter out;
    private PrintWriter err;
    private String remoteDir;

    private RfsSupport rfsSupport;
    
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
        this.rfsSupport = new RfsSupport(pae);
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
        try {
            rfsSupport.setIO(io);
            if (checkRemotePath()) {
                rfsSupport.initRfsIfNeed();
                delegate.addExecutionListener(rfsSupport);
                delegate.execute(io);
                delegate.removeExecutionListener(rfsSupport);
            }
        } catch (InterruptedException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (InterruptedIOException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (RemoteException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            printErr(ex, io);
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            printErr(ex, io);
        } catch (ExecutionException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            printErr(ex, io);
        }
    }

    private void printErr(Exception ex, InputOutput io) throws MissingResourceException {
        if (err != null) {
            String message = NbBundle.getMessage(getClass(), "MSG_Error_Copying", remoteDir, ServerList.get(execEnv).toString(), ex.getLocalizedMessage());
            io.getErr().printf("%s\n", message); // NOI18N
            io.getErr().printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Build_Failed"));
            err.printf("%s\n", message); // NOI18N
        }
    }

    /*package-local*/
    boolean checkRemotePath() {
        boolean success = true;
        MakeConfiguration conf = pae.getConfiguration();
        ExecutionEnvironment env = conf.getDevelopmentHost().getExecutionEnvironment();
        if (env.isRemote()) {
            // the project itself
            File baseDir = new File(pae.getProfile().getBaseDir()).getAbsoluteFile(); // or canonical?
            List<File> extraSourceRoots = new ArrayList<File>();
            MakeConfigurationDescriptor mcs = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(pae.getProject());
            for(String soorceRoot : mcs.getSourceRoots()) {
                String path = IpeUtils.toAbsolutePath(baseDir.getAbsolutePath(), soorceRoot);
                File file = new File(path); // or canonical?
                extraSourceRoots.add(file);
            }
            // Make sure 1st level subprojects are visible remotely
            // First, remembr all subproject locations
            for (String subprojectDir : conf.getSubProjectLocations()) {
                subprojectDir = IpeUtils.toAbsolutePath(baseDir.getAbsolutePath(), subprojectDir);
                extraSourceRoots.add(new File(subprojectDir));
            }
            // Then go trough open subprojects and add their external source roots
            for (Project subProject : conf.getSubProjects()) {
                File subProjectDir = FileUtil.toFile(subProject.getProjectDirectory());
                MakeConfigurationDescriptor subMcs =
                        MakeConfigurationDescriptor.getMakeConfigurationDescriptor(subProject);
                for(String soorceRoot : mcs.getSourceRoots()) {
                    File file = new File(soorceRoot).getAbsoluteFile(); // or canonical?
                    extraSourceRoots.add(file);
                }
            }
            success = sync(env, baseDir, extraSourceRoots, err, out);
        }
        return success;
    }

    private boolean sync(ExecutionEnvironment env, File projectDir, List<File> externSourceRoots,
            PrintWriter err, PrintWriter out) {
        File privProjectStorage = new File(new File(projectDir, "nbproject"), "private"); //NOI18N
        List<File> allFiles = new ArrayList<File>(externSourceRoots.size() + 1);
        allFiles.add(projectDir);
        allFiles.addAll(externSourceRoots);
        RemoteSyncWorker syncWorker = ServerList.get(env).getSyncFactory().createNew(
                env, out, err, privProjectStorage, allFiles.toArray(new File[allFiles.size()]));
        return syncWorker.synchronize();
    }


}
