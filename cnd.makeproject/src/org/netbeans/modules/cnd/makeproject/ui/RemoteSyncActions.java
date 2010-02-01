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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport.PathMapperException;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport.Worker;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
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
        @Override
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
                Map<Project, Collection<File>> filesMap = gatherFiles(nodes);
                int cnt = 0;
                int total = 0;
                for (Collection<File> files : filesMap.values()) {
                    total += files.size();
                }
                progressHandle.switchToDeterminate(total);
                for (Map.Entry<Project, Collection<File>> entry : filesMap.entrySet()) {
                    RemoteSyncSupport.Worker worker = createWorker(entry.getKey(), execEnv);
                    try {
                        for (File file : entry.getValue()) {
                            if (cancelled) {
                                break;
                            }
                            String progressMessage = getFileProgressMessage(file);
                            tab.getOut().println(progressMessage);
                            try {
                                worker.process(file, tab.getErr());
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
                            progressHandle.progress(progressMessage, cnt++);
                        }
                    } finally {
                        worker.close();
                    }
                }
            } catch (PathMapperException ex) {
                tab.getErr().println(NbBundle.getMessage(RemoteSyncActions.class, "ERR_MAPPING", ex.getFile().getAbsolutePath()));
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

        @Override
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
        protected abstract RemoteSyncSupport.Worker createWorker(Project project, ExecutionEnvironment execEnv);
    }

    private static class Uploader extends UpDownLoader {

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
        protected Worker createWorker(Project project, ExecutionEnvironment execEnv) {
            return RemoteSyncSupport.createUploader(project, execEnv);
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
        protected Worker createWorker(final Project project, final ExecutionEnvironment execEnv) {
            return new Worker() {
                private final PathMap pathMap = HostInfoProvider.getMapper(execEnv);
                @Override
                public void process(File file, Writer err) throws PathMapperException, InterruptedException, ExecutionException, IOException {
                    String remotePath = pathMap.getRemotePath(file.getAbsolutePath(), false);
                    if (remotePath == null) {
                        throw new RemoteSyncSupport.PathMapperException(file);
                    }
                    Future<Integer> task = CommonTasksSupport.downloadFile(remotePath, execEnv, file.getAbsolutePath(), err);
                    int rc = task.get().intValue();
                    if (rc != 0) {
                        throw new IOException(NbBundle.getMessage(RemoteSyncActions.class, "ERR_RC", Integer.valueOf(rc)));
                    }
                }
                @Override
                public void close() {}
            };
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
        ExecutionEnvironment developmentHost = ServerList.getDefaultRecord().getExecutionEnvironment();
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

    private static Map<Project, Collection<File>> gatherFiles(Node[] nodes) {
        Map<Project, Collection<File>> result = new HashMap<Project, Collection<File>>();
        for (Node node : nodes) {
            Project project = getNodeProject(node);
            Collection<File> files = result.get(project);
            if (files == null) {
                files = new ArrayList<File>();
                result.put(project, files);
            }
            gatherFiles(files, node);
        }
        return result;
    }

    private static void gatherFiles(Collection<File> files, Node node) {
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
        Folder folder = node.getLookup().lookup(Folder.class);
        if (folder != null) {
            gatherFiles(files, folder);
        }
    }

    private static void gatherFiles(Collection<File> files, Folder folder) {
        for (Item item : folder.getItemsAsArray()) {
            File file = item.getFile();
            if (file != null && !file.isDirectory()) {
                files.add(file);
            }
        }
        for (Folder subfolder : folder.getFolders()) {
            gatherFiles(files, subfolder);
        }
    }
}
