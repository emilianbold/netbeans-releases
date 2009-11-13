/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.ui;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

class RemoteSyncActions {

    private static UploadAction uploadAction;
    private static DownloadAction downloadAction;

    /* A common active nodes cache to be used by all actions */
    private static final AtomicReference<Node[]> activatedNodesCache = new AtomicReference<Node[]>();

    /** A task that activatedNodesCache */
    private static final RequestProcessor.Task clearCacheTask = RequestProcessor.getDefault().create(new Runnable() {
                public void run() {
                    activatedNodesCache.set(null);
                }
    });
    
    /** prevents instance creation */
    private RemoteSyncActions() {
    }

    public static Action createUploadAction() {
        if (uploadAction == null) {
            uploadAction = new UploadAction();
        }
        return uploadAction;
    }

    public static Action createDownloadAction() {
        if (downloadAction == null) {
            downloadAction = new DownloadAction();
        }
        return downloadAction;
    }

    private static void cacheActiveNodes(Node[] activatedNodes)  {
        activatedNodesCache.set(activatedNodes);
        clearCacheTask.schedule(5000);
    }

    /** contains upload / download similarites */
    private static abstract class UpDownLoader implements Cancellable {

        protected final ExecutionEnvironment execEnv;
        protected final String envName;
        protected final InputOutput tab;

        private boolean cancelled = false;
        private final Node[] nodes;
        private volatile Thread workingThread;

        public UpDownLoader(ExecutionEnvironment execEnv, Node[] nodes, InputOutput tab) {
            this.execEnv = execEnv;
            this.nodes = nodes;
            this.tab = tab;
            envName = ServerList.get(execEnv).getDisplayName();
        }

        public void work() {
            workingThread = Thread.currentThread();
            String title = getProgressTitle();
            tab.getOut().println(title);
            long time = System.currentTimeMillis();
            int errCnt = 0;
            int okCnt = 0;
            ProgressHandle progressHandle = ProgressHandleFactory.createHandle(title, this);
            progressHandle.start();
            try {
                if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                    ConnectionManager.getInstance().connectTo(execEnv);
                }
                PathMap pathMap = HostInfoProvider.getMapper(execEnv);
                Collection<File> files = new ArrayList<File>();
                gatherFiles(files, nodes);
                int cnt = 0;
                progressHandle.switchToDeterminate(files.size());
                for (File file : files) {
                    if (cancelled) {
                        break;
                    }
                    String progressMessage = getFileProgressMessage(file);
                    String remotePath = pathMap.getRemotePath(file.getAbsolutePath(), false);
                    if (remotePath == null) {
                        tab.getErr().println(NbBundle.getMessage(RemoteSyncActions.class, "ERR_MAPPING", file.getAbsolutePath()));
                    } else {
                        tab.getOut().println(progressMessage);
                        try {
                            processFile(file, remotePath);
                            okCnt++;
                        } catch (InterruptedException ex) {
                            break;
                        } catch (ExecutionException ex) {
                            tab.getErr().println(NbBundle.getMessage(RemoteSyncActions.class, "ERR_FILE", file.getAbsolutePath(), ex.getMessage()));
                            errCnt++;
                        } catch (IOException ex) {
                            tab.getErr().println(NbBundle.getMessage(RemoteSyncActions.class, "ERR_FILE", file.getAbsolutePath(), ex.getMessage()));
                            errCnt++;
                        }
                    }
                    progressHandle.progress(progressMessage, cnt++);
                }
            } catch (CancellationException ex) {
                cancelled = true;
            } catch (InterruptedIOException ex) {
                cancelled = true;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                progressHandle.finish();
            }
            workingThread = null;
            time = System.currentTimeMillis() - time;
            if (errCnt == 0) {
                tab.getOut().println(NbBundle.getMessage(RemoteSyncActions.class, "SUMMARY_SUCCESS", okCnt));
            } else if (cancelled) {
                tab.getOut().println(NbBundle.getMessage(RemoteSyncActions.class, "SUMMARY_CANCELLED", okCnt));
            } else {
                tab.getErr().println(NbBundle.getMessage(RemoteSyncActions.class, "SUMMARY_ERROR", okCnt));
            }
        }

        public boolean cancel() {
            cancelled = true;
            Thread thread = workingThread;
            if (thread != null) {
                thread.interrupt();
            }
            return true;
        }

        protected abstract String getProgressTitle();
        protected abstract String getFileProgressMessage(File file);
        protected abstract void processFile(File file, String remotePath) throws InterruptedException, ExecutionException, IOException;
    }

    private static class Uploader extends UpDownLoader {

        private final Set<String> checkedDirs = new HashSet<String>();

        public Uploader(ExecutionEnvironment execEnv, Node[] nodes, InputOutput tab) {
            super(execEnv, nodes, tab);
        }

        @Override
        protected String getFileProgressMessage(File file) {
            return NbBundle.getMessage(RemoteSyncActions.class, "MSG_UPLOAD_FILE", file.getAbsolutePath());
        }

        @Override
        protected String getProgressTitle() {
            return NbBundle.getMessage(RemoteSyncActions.class, "PROGRESS_TITLE_UPLOAD", envName);
        }

        @Override
        protected void processFile(File file, String remotePath) throws InterruptedException, ExecutionException, IOException {
            checkDir(remotePath);
            Future<Integer> task = CommonTasksSupport.uploadFile(file.getAbsolutePath(), execEnv, remotePath, 0700, tab.getErr());
            int rc = task.get().intValue();
            if (rc != 0) {
                throw new IOException(NbBundle.getMessage(RemoteSyncActions.class, "ERR_RC", Integer.valueOf(rc)));
            }
        }

        private void checkDir(String remoteFilePath) throws InterruptedException, ExecutionException {
            int slashPos = remoteFilePath.lastIndexOf('/'); //NOI18N
            if (slashPos >= 0) {
                String remoteDir = remoteFilePath.substring(0, slashPos);
                if (!checkedDirs.contains(remoteDir)) {
                    checkedDirs.add(remoteDir);
                    Future<Integer> task = CommonTasksSupport.mkDir(execEnv, remoteDir, null);
                    task.get();
                }
            }
        }
    }

    private static class Downloader extends UpDownLoader {

        public Downloader(ExecutionEnvironment execEnv, Node[] nodes, InputOutput tab) {
            super(execEnv, nodes, tab);
        }

        @Override
        protected String getFileProgressMessage(File file) {
            return NbBundle.getMessage(RemoteSyncActions.class, "MSG_DOWNLOAD_FILE", file.getAbsolutePath());
        }

        @Override
        protected String getProgressTitle() {
            return NbBundle.getMessage(RemoteSyncActions.class, "PROGRESS_TITLE_DOWNLOAD", envName);
        }

        @Override
        protected void processFile(File file, String remotePath) throws InterruptedException, ExecutionException, IOException {
            Future<Integer> task = CommonTasksSupport.downloadFile(remotePath, execEnv, file.getAbsolutePath(), tab.getErr());
            int rc = task.get().intValue();
            if (rc != 0) {
                throw new IOException(NbBundle.getMessage(RemoteSyncActions.class, "ERR_RC", Integer.valueOf(rc)));
            }
        }
    }

    private static abstract class BaseAction extends NodeAction {

        private boolean enabled;

        protected abstract void performAction(ExecutionEnvironment execEnv, Node[] activatedNodes);
        protected abstract String getDummyItemText();
        protected abstract String getItemText(String hostName);

        @Override
        protected boolean enable(Node[] activatedNodes) {
            cacheActiveNodes(activatedNodes);
            ExecutionEnvironment execEnv = getEnv(activatedNodes);
            enabled = execEnv != null && execEnv.isRemote();
            return enabled;
        }

        protected boolean wasEnabled() {
            return enabled;
        }


        @Override
        protected void performAction(Node[] activatedNodes) {
            final ExecutionEnvironment execEnv = getEnv(activatedNodes);
            if (execEnv != null && execEnv.isRemote()) {
                performAction(execEnv, activatedNodes);
            }
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        @Override
        public String getName() {
            if (!wasEnabled()) {
                return getDummyItemText();
            }
            final Node[] activatedNodes = activatedNodesCache.get();
            if (activatedNodes == null || activatedNodes.length == 0) {
                return getDummyItemText();
            }

            final ExecutionEnvironment execEnv = getEnv(activatedNodes);
            if (execEnv == null || execEnv.isLocal()) {
                return getDummyItemText();
            }
            final String hostName = ServerList.get(execEnv).getDisplayName();
            return getItemText(hostName);
        }
    }

    private static class UploadAction extends BaseAction  {

        @Override
        protected String getDummyItemText() {
            return NbBundle.getMessage(RemoteSyncActions.class, "LBL_UploadAction_Name_0");
        }

        @Override
        protected String getItemText(String hostName) {
            return NbBundle.getMessage(RemoteSyncActions.class, "LBL_UploadAction_Name_1", hostName);
        }

        @Override
        protected void performAction(final ExecutionEnvironment execEnv, final Node[] activatedNodes) {
            RequestProcessor.getDefault().post(new NamedRunnable("Uploading to " + ServerList.get(execEnv).getDisplayName()) { // NOI18N
                @Override
                protected void runImpl() {
                    upload(execEnv, activatedNodes);
                }
            });
        }

    }

    private static class DownloadAction extends BaseAction{

        @Override
        protected String getDummyItemText() {
            return NbBundle.getMessage(RemoteSyncActions.class, "LBL_DownloadAction_Name_0");
        }

        @Override
        protected String getItemText(String hostName) {
            return NbBundle.getMessage(RemoteSyncActions.class, "LBL_DownloadAction_Name_1", hostName);
        }

        @Override
        protected void performAction(final ExecutionEnvironment execEnv, final Node[] activatedNodes) {
            RequestProcessor.getDefault().post(new NamedRunnable("Uploading to " + ServerList.get(execEnv).getDisplayName()) { // NOI18N
                @Override
                protected void runImpl() {
                    download(execEnv, activatedNodes);
                }
            });
        }
    }

    private static ExecutionEnvironment getEnv(Node[] activatedNodes) {
        ExecutionEnvironment result = null;
        for (Node node : activatedNodes) {
            Project project = getNodeProject(node);
            ExecutionEnvironment env = getEnv(project);
            if (env != null) {
                if (result == null) {
                    result = env;
                } else {
                    if (!result.equals(env)) {
                        return null;
                    }
                }
            }
        }
        return result;
    }

    private static ExecutionEnvironment getEnv(Project project) {
        ExecutionEnvironment developmentHost = CompilerSetManager.getDefaultExecutionEnvironment();
        if (project != null) {
            RemoteProject info = project.getLookup().lookup(RemoteProject.class);
            if (info != null) {
                ExecutionEnvironment dh = info.getDevelopmentHost();
                if (dh != null) {
                    developmentHost = dh;
                }
            }
        }
        return developmentHost;
    }

    private static Project getNodeProject(Node node) {
        if (node == null) {
            return null;
        }
        Project project = node.getLookup().lookup(Project.class);
        if (project != null) {
            return project;
        } else {
            return getNodeProject(node.getParentNode());
        }
    }

    private static InputOutput getTab(String name, boolean reuse) {
        InputOutput tab;
        if (reuse) {
            tab = IOProvider.getDefault().getIO(name, false); // This will (sometimes!) find an existing one.
            tab.closeInputOutput(); // Close it...
        }
        tab = IOProvider.getDefault().getIO(name, true); // Create a new ...
        try {
            tab.getOut().reset();
        } catch (IOException ex) {
        }
        tab.select();
        return tab;
    }

    private static void upload(ExecutionEnvironment execEnv, Node[] nodes) {
        InputOutput tab = getTab(NbBundle.getMessage(RemoteSyncActions.class, "LBL_UploadTab_Name", execEnv), true);
        Uploader worker = new Uploader(execEnv, nodes, tab);
        worker.work();
    }

    private static void download(ExecutionEnvironment execEnv, Node[] nodes) {
        InputOutput tab = getTab(NbBundle.getMessage(RemoteSyncActions.class, "LBL_DownloadTab_Name", execEnv), true);
        Downloader worker = new Downloader(execEnv, nodes, tab);
        worker.work();
    }

    private static void gatherFiles(Collection<File> files, Node[] nodes) {
        for (Node node : nodes) {
            DataObject dataObject = node.getCookie(DataObject.class);
            if (dataObject != null) {
                FileObject fo = dataObject.getPrimaryFile();
                if (fo != null) {
                    File file = FileUtil.toFile(fo);
                    if (!file.isDirectory()) {
                        files.add(file);
                    }
                }
            }
            gatherFiles(files, node.getChildren().getNodes());
        }
    }
}
