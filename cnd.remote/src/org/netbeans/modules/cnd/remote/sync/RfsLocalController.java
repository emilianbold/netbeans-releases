package org.netbeans.modules.cnd.remote.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

class RfsLocalController implements Runnable {

    private final BufferedReader requestReader;
    private final PrintStream responseStream;
    private final String remoteDir;
    private final File[] localDirs;
    private final ExecutionEnvironment execEnv;
    private final PrintWriter err;
    private final FileData fileData;
    private final Set<String> processedFiles = new HashSet<String>();

    public RfsLocalController(ExecutionEnvironment executionEnvironment, File[] localDirs, String remoteDir,
            InputStream requestStream, OutputStream responseStream, PrintWriter err,
            FileData fileData) {
        super();
        this.execEnv = executionEnvironment;
        this.localDirs = localDirs;
        this.remoteDir = remoteDir;
        this.requestReader = new BufferedReader(new InputStreamReader(requestStream));
        this.responseStream = new PrintStream(responseStream);
        this.err = err;
        this.fileData = fileData;
    }

    private void respond_ok() {
        responseStream.printf("1\n"); // NOI18N
        // NOI18N
        responseStream.flush();
    }

    private void respond_err(String tail) {
        responseStream.printf("0 %s\n", tail); // NOI18N
        // NOI18N
        responseStream.flush();
    }

    private String toRemoteFilePathName(String localAbsFilePath) {
        String out = localAbsFilePath;
        if (Utilities.isWindows()) {
            out = WindowsSupport.getInstance().convertToMSysPath(localAbsFilePath);
        }
        if (out.charAt(0) == '/') {
            out = out.substring(1);
        } else {
            RemoteUtil.LOGGER.warning("Path must start with /: " + out + "\n");
        }
        return out;
    }

    public void run() {
        long totalCopyingTime = 0;
        RemotePathMap mapper = RemotePathMap.getPathMap(execEnv);
        while (true) {
            try {
                String request = requestReader.readLine();
                String remoteFile = request;
                RemoteUtil.LOGGER.finest("LC: REQ " + request);
                if (request == null) {
                    break;
                }
                if (processedFiles.contains(remoteFile)) {
                    RemoteUtil.LOGGER.info("RC asks for file " + remoteFile + " again?!");
                    respond_ok();
                    continue;
                } else {
                    processedFiles.add(remoteFile);
                }
                String localFilePath = mapper.getLocalPath(remoteFile);
                if (localFilePath != null) {
                    File localFile = new File(localFilePath);
                    if (localFile.exists() && !localFile.isDirectory()) {
                        FileState state = fileData.getState(localFile);
                        if (needsCopying(localFile)) {
                            RemoteUtil.LOGGER.finest("LC: uploading " + localFile + " to " + remoteFile + " started");
                            long fileTime = System.currentTimeMillis();
                            Future<Integer> task = CommonTasksSupport.uploadFile(localFile.getAbsolutePath(), execEnv, remoteFile, 511, err);
                            try {
                                int rc = task.get();
                                fileTime = System.currentTimeMillis() - fileTime;
                                totalCopyingTime += fileTime;
                                System.err.printf("LC: uploading %s to %s finished; rc=%d time =%d total time = %d ms \n", localFile, remoteFile, rc, fileTime, totalCopyingTime);
                                if (rc == 0) {
                                    fileData.setState(localFile, FileState.COPIED);
                                    respond_ok();
                                } else {
                                    respond_err("1"); // NOI18N
                                }
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                                break;
                            } catch (ExecutionException ex) {
                                Exceptions.printStackTrace(ex);
                                respond_err("2 execution exception\n"); // NOI18N
                            } finally {
                                responseStream.flush();
                            }
                        } else {
                            RemoteUtil.LOGGER.finest("LC: file " + localFile + " not accepted");
                            respond_ok();
                        }
                    } else {
                        respond_ok();
                    }
                } else {
                    respond_ok();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        fileData.store();
    }

    public boolean needsCopying(File file) {
        FileData.FileInfo info = fileData.getFileInfo(file);
        if (info == null) {
            return false;
        } else {
            switch (info.state) {
                case COPIED:
                    return file.lastModified() != info.timestamp;
                case TOUCHED:
                    return true;
                case INITIAL:
                    return true;
                case UNCONTROLLED:
                    return false;
                case ERROR:
                    return true; // TODO: ???
                default:
                    CndUtils.assertTrue(false, "Unexpecetd state: " + info.state); //NOI18N
                    return false;
            }
        }
    }

    void shutdown() {
        fileData.store();
    }

    private static class FileGatheringInfo {
        public final File file;
        public final String relPath;
        public FileGatheringInfo(File file, String relPath) {
            this.file = file;
            this.relPath = relPath;
        }
        @Override
        public String toString() {
            return relPath;
        }
    }

    /**
     * Feeds remote controller with the list of files and their lengths
     * @param rcOutputStream
     */
    void feedFiles(OutputStream rcOutputStream, SharabilityFilter filter) {
        PrintWriter writer = new PrintWriter(rcOutputStream);
        List<FileGatheringInfo> files = new ArrayList<FileGatheringInfo>(512);
        RemotePathMap mapper = RemotePathMap.getPathMap(execEnv);
        for (File localDir : localDirs) {
            localDir = CndFileUtils.normalizeFile(localDir);
            final String toRemoteFilePathName = mapper.getRemotePath(localDir.getAbsolutePath());
            File[] children = localDir.listFiles(filter);
            if (children != null) {
                for (File child : children) {
                    gatherFiles(child, toRemoteFilePathName, filter, files);
                }
            }
        }
        Collections.sort(files, new Comparator<FileGatheringInfo>() {
            public int compare(FileGatheringInfo f1, FileGatheringInfo f2) {
                if (f1.file.isDirectory() || f2.file.isDirectory()) {
                    if (f1.file.isDirectory() && f2.file.isDirectory()) {
                        return f1.relPath.compareTo(f2.relPath);
                    } else {
                        return f1.file.isDirectory() ? -1 : +1;
                    }
                } else {
                    long delta = f1.file.lastModified() - f2.file.lastModified();
                    return (delta == 0) ? 0 : ((delta < 0) ? -1 : +1); // signum(delta)
                }
            }
        });
        for (FileGatheringInfo info : files) {
            sendFileInitRequest(writer, info.file, info.relPath);
        }
        writer.printf("\n"); // NOI18N
        writer.flush();
        fileData.store();
    }

    private void sendFileInitRequest(PrintWriter writer, File file, String relPath) {
        if (file.isDirectory()) {
            writer.printf("D %s\n", relPath); //NOI18N
            writer.flush(); //TODO: remove?
        } else {
            FileData.FileInfo info = fileData.getFileInfo(file);
            FileState newState;
            switch(info  == null ? FileState.INITIAL : info.state) {
                case COPIED:
                case TOUCHED:
                    if (info.timestamp == file.lastModified()) {
                        newState = info.state;
                    } else {
                        newState = FileState.INITIAL;
                    }
                    break;
                case ERROR: // fall through
                case INITIAL:
                    newState = FileState.INITIAL;
                    break;
                case UNCONTROLLED:
                    return;
                default:
                    CndUtils.assertTrue(false, "Unexpected state: " + info.state); //NOI18N
                    return;
            }
            CndUtils.assertTrue(newState == FileState.INITIAL || newState == FileState.COPIED || newState == FileState.TOUCHED,
                    "State shouldn't be " + newState); //NOI18N
            writer.printf("%c %d %s\n", newState.id, file.length(), relPath); // NOI18N
            writer.flush(); //TODO: remove?
            fileData.setState(file, newState);
        }
    }

    private static void gatherFiles(File file, String base, FileFilter filter, List<FileGatheringInfo> files) {
        // it is assumed that the file itself was already filtered
        String fileName = isEmpty(base) ? file.getName() : base + '/' + file.getName();
        files.add(new FileGatheringInfo(file, fileName));
        if (file.isDirectory()) {
            File[] children = file.listFiles(filter);
            for (File child : children) {
                String newBase = isEmpty(base) ? file.getName() : (base + "/" + file.getName()); // NOI18N
                gatherFiles(child, newBase, filter, files);
            }
        }
    }

    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

}
