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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.remote.sync.FileData.FileInfo;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
/*package-local*/ final class ZipSyncWorker extends BaseSyncWorker implements RemoteSyncWorker {

    private TimestampAndSharabilityFilter filter;

    private int totalCount;
    private int uploadCount;
    private long totalSize;
    private long uploadSize;

    private class TimestampAndSharabilityFilter implements FileFilter {

        private final FileData fileData;
        private final SharabilityFilter delegate;

        public TimestampAndSharabilityFilter(File privProjectStorageDir, ExecutionEnvironment executionEnvironment) {
            fileData = new FileData(privProjectStorageDir, executionEnvironment);
            delegate = new SharabilityFilter();
        }

        @Override
        public boolean accept(File file) {
            boolean accepted = delegate.accept(file);
            if (accepted && ! file.isDirectory()) {
                accepted = needsCopying(file);
                if (accepted) {
                    fileData.setState(file, FileState.COPIED);
                } else {
                    accepted = false;
                }
            }
            refreshStatistics(file, accepted);
            return accepted;
        }

        public void flush() {
            fileData.store();
        }

        private void clear() {
            fileData.clear();
        }

        private boolean needsCopying(File file) {
            FileInfo info = fileData.getFileInfo(file);
            FileState state = (info == null) ? FileState.INITIAL : info.state;
            switch (state) {
                case INITIAL:       return true;
                case TOUCHED:       return true;
                case COPIED:        return false;
                case ERROR:         return true;
                case UNCONTROLLED:  return false;
                default:
                    CndUtils.assertTrue(false, "Unexpected state: " + state); //NOI18N
                    return false;
            }
        }
    }

    public ZipSyncWorker(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, File privProjectStorageDir, File... files) {
        super(executionEnvironment, out, err, privProjectStorageDir, files);
    }

    private static File getTemp() {
        String tmpPath = System.getProperty("java.io.tmpdir");
        File tmpFile = new File(tmpPath);
        return tmpFile.exists() ? tmpFile : null;
    }

    /** for trace/debug purposes */
    private StringBuilder getLocalFilesString() {
        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(f.getAbsolutePath());
        }
        return sb;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("REC")
    private void synchronizeImpl(String remoteRoot) throws InterruptedException, ExecutionException, IOException {

        totalCount = uploadCount = 0;
        totalSize = uploadSize = 0;
        long time = 0;
        
        if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {
            System.out.printf("Uploading %s to %s ...\n", getLocalFilesString(), executionEnvironment); // NOI18N
            time = System.currentTimeMillis();
        }
        filter = new TimestampAndSharabilityFilter(privProjectStorageDir, executionEnvironment);

        StringBuilder script = new StringBuilder("sh -c \""); // NOI18N
        for (int i = 0; i < files.length; i++) {
            String remoteFile = RemotePathMap.getPathMap(executionEnvironment).getRemotePath(files[i].getAbsolutePath(), true);
            if (files[i].isDirectory()) {
                script.append(String.format("test -d %s  || echo %s; ", remoteFile, remoteFile)); // echo all inexistent directories // NOI18N
            }
        }
        script.append("\""); // NOI18N
        
        RemoteCommandSupport rcs = new RemoteCommandSupport(executionEnvironment, script.toString());
        if (rcs.run() != 0) {
            throw new IOException("Can not check remote directories"); //NOI18N
        }

        Collection<Future<Integer>> mkDirs = new ArrayList<Future<Integer>>();
        final String scriptOutput = rcs.getOutput().trim();
        if (scriptOutput.length() > 0) {
            String[] inexistentDirs = scriptOutput.split("\n"); // NOI18N
            filter.clear();
            // we optimize check (via script) since it is preformed each build,
            // but does not optimize createion since it's done once
            for (int i = 0; i < inexistentDirs.length; i++) {
                mkDirs.add(CommonTasksSupport.mkDir(executionEnvironment, inexistentDirs[i], err));
            }
        }

        // success flag is for tracing only. TODO: should we drop it?
        boolean success = false;
        File zipFile = null;
        upload: // the label allows us exiting this block on condition
        try  {

            String localFileName = files[0].getName();
            if (localFileName.length() < 3) {
                localFileName = localFileName + ((localFileName.length() == 1) ? "_" : "__"); //NOI18N
            }
            zipFile = File.createTempFile(localFileName, ".zip", getTemp()); // NOI18N
            Zipper zipper = new Zipper(zipFile);
            {
                if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {System.out.printf("\tZipping %s to %s...\n", getLocalFilesString(), zipFile); } // NOI18N
                long zipStart = System.currentTimeMillis();
                PathMap pm = RemotePathMap.getPathMap(executionEnvironment);
                for (File file : files) {
                    String remoteDir = pm.getRemotePath(file.getAbsolutePath(), false);
                    if (remoteDir == null) { // this never happens since mapper is fixed
                        throw new IOException("Can not find remote path for " + file.getAbsolutePath()); //NOI18N
                    }
                    String base;
                    if (remoteDir.startsWith(remoteRoot)) {
                        base = remoteDir.substring(remoteRoot.length() + 1);
                    } else {
                        // this is never the case! - but...
                        throw new IOException(remoteDir + " should start with " + remoteRoot); //NOI18N
                    }
                    zipper.add(file, filter, base);
                }
                zipper.close();
                float zipTime = ((float) (System.currentTimeMillis() - zipStart))/1000f;
                if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {System.out.printf("\t%d files zipped; file size is %d\n", zipper.getFileCount(), zipFile.length()); } // NOI18N
                if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {System.out.printf("\tZipping %s to %s took %f s\n", getLocalFilesString(), zipFile, zipTime); } // NOI18N
            }

            if (zipper.getFileCount() == 0) {
                success = true; // just no changed files
                break upload;
            }

            // wait/check whether the remote dir was created sucessfully
            for (Future<Integer> mkDir : mkDirs) {
                if (mkDir.get() != 0) {
                    throw new IOException("Can not create directory " + remoteRoot); //NOI18N
                }
            }

            String remoteFile = remoteRoot + '/' + zipFile.getName(); //NOI18N
            {
                long uploaStart = System.currentTimeMillis();
                if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) { System.out.printf("\tZSCP: uploading %s to %s:%s ...\n", zipFile, executionEnvironment, remoteFile); } // NOI18N
                Future<Integer> upload = CommonTasksSupport.uploadFile(zipFile.getAbsolutePath(), executionEnvironment, remoteFile, 0777, err);
                int rc = upload.get();
                float uploadTime = ((float) (System.currentTimeMillis() - uploaStart))/1000f;
                if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) { System.out.printf("\tZSCP: uploading %s to %s:%s finished in %f s with rc=%d\n", zipFile, executionEnvironment, remoteFile, uploadTime, rc); } // NOI18N
                if (rc != 0) {
                    throw new IOException("uploading " + zipFile + " to " + executionEnvironment + ':' + remoteFile + // NOI18N
                            " finished with error code " + rc); // NOI18N
                }
            }

            {
                if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) { System.out.printf("\tZSCP: unzipping %s:%s ...\n", executionEnvironment, remoteFile); } // NOI18N
                long unzipStart = System.currentTimeMillis();

                NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
                pb.setCommandLine("unzip -o " + remoteFile + " > /dev/null"); // NOI18N
                //pb.setExecutable("unzip");
                //pb.setArguments("-o", remoteFile);
                pb.setWorkingDirectory(remoteRoot);
                pb.redirectError();
                Process proc = pb.call();

                String line;
                BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                // we now redirect instead of reading stderr // in = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                try {
                    while ((line = in.readLine()) != null) {
                        if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) { System.err.printf("\t%s\n", line); } //NOI18N
                    }
                } finally {
                    in.close();
                }
                    
                int rc = proc.waitFor();
                //String cmd = "sh -c \"unzip -o -q " + remoteFile + " > /dev/null";
                //RemoteCommandSupport rcs = new RemoteCommandSupport(executionEnvironment, cmd);
                //int rc = rcs.run();
                float unzipTime = ((float) (System.currentTimeMillis() - unzipStart))/1000f;
                if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) { System.out.printf("\tZSCP: unzipping %s:%s finished in %f s; rc=%d\n", executionEnvironment , remoteFile, unzipTime, rc); } // NOI18N
                if (rc != 0) {
                    throw new IOException("unzipping " + remoteFile + " at " + executionEnvironment + " finished with error code " + rc); // NOI18N
                }
            }
            success = true;
            CommonTasksSupport.rmFile(executionEnvironment, remoteFile, err);
            // NB: we aren't waining for completion,
            // since the name of the file made my File.createTempFile is new each time
            filter.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (zipFile != null && zipFile.exists()) {
                if (!zipFile.delete()) {
                    RemoteUtil.LOGGER.log(Level.INFO, "Can not delete temporary file {0}", zipFile.getAbsolutePath()); //NOI18N
                }
            }
        }

        if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {
            time = System.currentTimeMillis() - time;
            long bps = uploadSize * 1000L / time;
            String speed = (bps < 1024*8) ? (bps + " b/s") : ((bps/1024) + " Kb/s"); // NOI18N

            String strTotalSize = (totalSize < 1024 ? (totalSize + " bytes") : ((totalSize/1024) + " K")); // NOI18N
            String strUploadSize = (uploadSize < 1024 ? (uploadSize + " bytes") : ((uploadSize/1024) + " K")); // NOI18N
            System.out.printf("Total: %s in %d files. Copied to %s:%s: %s in %d files. Time: %d ms. %s. Avg. speed: %s\n", // NOI18N
                    strTotalSize, totalCount, executionEnvironment, remoteRoot,
                    strUploadSize, uploadCount, time, success ? "OK" : "FAILURE", speed); // NOI18N
        }
    }


    @Override
    public boolean startup(Map<String, String> env2add) {
        // Later we'll allow user to specify where to copy project files to
        String remoteRoot = RemotePathMap.getRemoteSyncRoot(executionEnvironment);
        if (remoteRoot == null) {
            if (err != null) {
                err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Cant_find_sync_root", ServerList.get(executionEnvironment).toString()));
            }
            return false; // TODO: error processing
        }

        boolean success = false;
        try {
            if (out != null) {
                out.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Copying",
                        remoteRoot, ServerList.get(executionEnvironment).toString()));
            }
            RemotePathMap mapper = RemotePathMap.getPathMap(executionEnvironment);
            synchronizeImpl(remoteRoot);
            success = true;
        } catch (InterruptedException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (InterruptedIOException ex) {
            // reporting does not make sense, just return false
            RemoteUtil.LOGGER.finest(ex.getMessage());
        } catch (ExecutionException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            if (err != null) {
                err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteRoot, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage()));
            }
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.FINE, null, ex);
            if (err != null) {
                err.printf("%s\n", NbBundle.getMessage(getClass(), "MSG_Error_Copying",
                        remoteRoot, ServerList.get(executionEnvironment).toString(), ex.getLocalizedMessage()));
            }
        }
        return success;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean cancel() {
        return false;
    }

    private void refreshStatistics(File file, boolean accepted) {
        totalCount++;
        totalSize += file.length();
        if (accepted) {
            uploadCount++;
            uploadSize += file.length();
        }
    }
}
