package org.netbeans.modules.cnd.remote.sync;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.remote.sync.download.HostUpdates;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

class RfsLocalController implements Runnable {

    private final BufferedReader requestReader;
    private final PrintWriter responseStream;
    private final File[] files;
    private final ExecutionEnvironment execEnv;
    private final PrintWriter err;
    private final FileData fileData;
    private final RemotePathMap mapper;
    private final Set<File> remoteUpdates;
    private final File privProjectStorageDir;
    /**
     * Maps remote canonical remote path remote controller operates with
     * to the absolute remote path local controller uses
     */
    private final Map<String, String> canonicalToAbsolute = new HashMap<String, String>();

    private static enum RequestKind {
        REQUEST,
        WRITTEN
    }

    public RfsLocalController(ExecutionEnvironment executionEnvironment, File[] files,
            BufferedReader requestStreamReader, PrintWriter responseStreamWriter, PrintWriter err,
            File privProjectStorageDir) {
        super();
        this.execEnv = executionEnvironment;
        this.files = files;
        this.requestReader = requestStreamReader;
        this.responseStream = responseStreamWriter;
        this.err = err;
        this.mapper = RemotePathMap.getPathMap(execEnv);
        this.remoteUpdates = new HashSet<File>();
        this.privProjectStorageDir = privProjectStorageDir;
        this.fileData = new FileData(privProjectStorageDir, executionEnvironment);
    }

    private void respond_ok() {
        responseStream.printf("1\n"); // NOI18N
        responseStream.flush();
    }

    private void respond_err(String tail) {
        responseStream.printf("0 %s\n", tail); // NOI18N
        responseStream.flush();
    }

//    private String toRemoteFilePathName(String localAbsFilePath) {
//        String out = localAbsFilePath;
//        if (Utilities.isWindows()) {
//            out = WindowsSupport.getInstance().convertToMSysPath(localAbsFilePath);
//        }
//        if (out.charAt(0) == '/') {
//            out = out.substring(1);
//        } else {
//            RemoteUtil.LOGGER.warning("Path must start with /: " + out + "\n");
//        }
//        return out;
//    }

    private RequestKind getRequestKind(String request) {
        if (request.charAt(1) != ' ') {
            throw new IllegalArgumentException("Protocol error: " + request); // NOI18N
        }
        switch (request.charAt(0)) {
            case 'r':   return RequestKind.REQUEST;
            case 'w':   return RequestKind.WRITTEN;
            default:
                throw new IllegalArgumentException("Protocol error: " + request); // NOI18N
        }
    }

    @Override
    public void run() {
        long totalCopyingTime = 0;
        while (true) {
            try {
                String request = requestReader.readLine();
                RemoteUtil.LOGGER.log(Level.FINEST, "LC: REQ {0}", request);
                if (request == null) {
                    break;
                }
                String remoteFile = request.substring(2);
                RequestKind kind = getRequestKind(request);
                String initialPath = canonicalToAbsolute.get(remoteFile);
                if (initialPath != null) {
                    remoteFile = initialPath;
                }
                String localFilePath = mapper.getLocalPath(remoteFile);
                if (localFilePath != null) {
                    File localFile = new File(localFilePath);
                    if (kind == RequestKind.WRITTEN) {
                        fileData.setState(localFile, FileState.UNCONTROLLED);
                        remoteUpdates.add(localFile);
                        RemoteUtil.LOGGER.log(Level.FINEST, "LC: uncontrolled {0}", localFile);
                    } else {
                        CndUtils.assertTrue(kind == RequestKind.REQUEST, "kind should be RequestKind.REQUEST, but is " + kind);
                        if (localFile.exists() && !localFile.isDirectory()) {
                            //FileState state = fileData.getState(localFile);
                            RemoteUtil.LOGGER.log(Level.FINEST, "LC: uploading {0} to {1} started", new Object[]{localFile, remoteFile});
                            long fileTime = System.currentTimeMillis();
                            Future<Integer> task = CommonTasksSupport.uploadFile(localFile.getAbsolutePath(), execEnv, remoteFile, 0777, err);
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
                            respond_ok();
                        }
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

    void shutdown() {
        fileData.store();
        if (!remoteUpdates.isEmpty()) {
            HostUpdates.register(remoteUpdates, execEnv, privProjectStorageDir);
        }
    }

    private static class FileGatheringInfo {
        
        public final File file;
        public final String remotePath;
        private String link;

        public FileGatheringInfo(File file, String remotePath) {
            this.file = file;
            this.remotePath = remotePath;
            CndUtils.assertTrue(remotePath.startsWith("/"), "Non-absolute remote path: " + remotePath);
            this.link = null;
        }

        @Override
        public String toString() {
            return (isLink() ? "L " : file.isDirectory() ? "D " : "F ") + file.getPath() + " -> " + remotePath; // NOI18N
        }

        public boolean isLink() {
            return link != null;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

    }

    /**
     * Feeds remote controller with the list of files and their lengths
     */
    void feedFiles(SharabilityFilter filter) {
        long time = System.currentTimeMillis();
        List<FileGatheringInfo> filesToFeed = new ArrayList<FileGatheringInfo>(512);

        // the set of top-level dirs 
        Set<File> topDirs = new HashSet<File>();

        for (File file : files) {
            file = CndFileUtils.normalizeFile(file);
            if (file.isDirectory()) {
                String toRemoteFilePathName = mapper.getRemotePath(file.getAbsolutePath());
                addFileGatheringInfo(filesToFeed, file, toRemoteFilePathName);
                File[] children = file.listFiles(filter);
                if (children != null) {
                    for (File child : children) {
                        gatherFiles(child, toRemoteFilePathName, filter, filesToFeed);
                    }
                }
                topDirs.add(file);
            } else {
                final File parentFile = file.getAbsoluteFile().getParentFile();
                String toRemoteFilePathName = mapper.getRemotePath(parentFile.getAbsolutePath());
                if (!topDirs.contains(parentFile)) {
                    // add parent folder for external file
                    topDirs.add(parentFile);
                    addFileGatheringInfo(filesToFeed, parentFile, toRemoteFilePathName);
                }
                gatherFiles(file, toRemoteFilePathName, filter, filesToFeed);
            }
        }

        Collection<File> parents = gatherParents(topDirs);
        for (File file : parents) {
            file = CndFileUtils.normalizeFile(file);
            String toRemoteFilePathName = mapper.getRemotePath(file.getAbsolutePath());
            addFileGatheringInfo(filesToFeed, file, toRemoteFilePathName);
        }

        checkLinks(filesToFeed);
        
        Collections.sort(filesToFeed, new Comparator<FileGatheringInfo>() {
            @Override
            public int compare(FileGatheringInfo f1, FileGatheringInfo f2) {
                if (f1.file.isDirectory() || f2.file.isDirectory()) {
                    if (f1.file.isDirectory() && f2.file.isDirectory()) {
                        return f1.remotePath.compareTo(f2.remotePath);
                    } else {
                        return f1.file.isDirectory() ? -1 : +1;
                    }
                } else {
                    long delta = f1.file.lastModified() - f2.file.lastModified();
                    return (delta == 0) ? 0 : ((delta < 0) ? -1 : +1); // signum(delta)
                }
            }
        });
        RemoteUtil.LOGGER.log(Level.FINE, "RFS_LC: gathered {0} files", filesToFeed.size());
        RemoteUtil.LOGGER.log(Level.FINE, "RFS_LC: gathering files took {1} ms", (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
        for (FileGatheringInfo info : filesToFeed) {
            sendFileInitRequest(info);
        }
        responseStream.printf("\n"); // NOI18N
        responseStream.flush();
        RemoteUtil.LOGGER.log(Level.FINE, "RFS_LC: file list took {0} ms", (System.currentTimeMillis() - time));
        try {
            time = System.currentTimeMillis();
            readFileInitResponse();
            RemoteUtil.LOGGER.log(Level.FINE, "RFS_LC: reading initial response took {0} ms", (System.currentTimeMillis() - time));
        } catch (IOException ex) {
            err.printf("%s\n", ex.getMessage());
        }
        fileData.store();
    }

    private Collection<File> gatherParents(Collection<File> files) {
        Set<File> parents = new HashSet<File>();
        for (File file : files) {
            gatherParents(file, parents);
        }
        return parents;
    }

    private void gatherParents(File file, Set<File> parents) {
        //file = CndFileUtils.normalizeFile(file);
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null && parent.getParentFile() != null) { // don't add top-level parents
            parents.add(parent);
            gatherParents(parent, parents);
        }
    }

    private void checkLinks(final List<FileGatheringInfo> filesToFeed) {
        NativeProcessBuilder pb = NativeProcessBuilder.newLocalProcessBuilder();
        pb.setExecutable("sh"); //NOI18N
        pb.setArguments("-c", "xargs ls -ld | grep '^l'"); //NOI18N
        final NativeProcess process;
        try {
            process = pb.call();
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.INFO, "Error when checking links: {0}", ex.getMessage());
            return;
        }
        
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                BufferedWriter requestWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                try {
                    for (FileGatheringInfo info : filesToFeed) {
                        final String path = info.file.getAbsolutePath();
                        RemoteUtil.LOGGER.log(Level.FINEST, "\tcheckLinks: {0}", path);
                        requestWriter.append(path);
                        requestWriter.newLine();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        requestWriter.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        RequestProcessor.getDefault().post(new Runnable() {
            private final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            @Override
            public void run() {
                try {
                    for (String errLine = errorReader.readLine(); errLine != null; errLine = errorReader.readLine()) {
                        RemoteUtil.LOGGER.info(errLine);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        errorReader.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        Map<String, FileGatheringInfo> map = new HashMap<String, FileGatheringInfo>(filesToFeed.size());
        for (FileGatheringInfo info : filesToFeed) {
            map.put(info.file.getAbsolutePath(), info);
        }

        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            boolean errorReported = false;
            for (String line = outputReader.readLine(); line != null; line = outputReader.readLine()) {
                // line format is:
                // lrwxrwxrwx   1 root     root           5 Mar 24 13:33 /export/link-home -> home/
                String[] parts = line.split(" +"); // NOI18N
                if (parts.length <= 4) {
                    if (!errorReported) {
                        errorReported = true;
                        RemoteUtil.LOGGER.log(Level.WARNING, "Unexpected ls output: {0}", line);
                    }
                }
                String linkTarget = parts[parts.length - 1];
                if (linkTarget.endsWith("/")) { // NOI18N
                    linkTarget = linkTarget.substring(0, linkTarget.length() - 1);
                }
                String linkPath = parts[parts.length - 3];
                FileGatheringInfo info = map.get(linkPath);
                CndUtils.assertNotNull(info, "Null FileGatheringInfo for " + linkPath); //NOI18N
                if (info != null) {
                    info.setLink(linkTarget);
                    File linkParentFile = new File(linkPath).getParentFile();
                    File linkTargetFile = new File(linkParentFile, linkTarget);
                    linkTargetFile = CndFileUtils.normalizeFile(linkTargetFile);
                    String remotePath = mapper.getRemotePath(linkTargetFile.getAbsolutePath(), false);
                    addFileGatheringInfo(filesToFeed, linkTargetFile, remotePath);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

// do we need to wait here?
//        try {
//            int rc = process.waitFor();
//        } catch (InterruptedException ex) {
//            // don't report InterruptedException
//            return;
//        }
    }

    private void readFileInitResponse() throws IOException {
        String request;
        while ((request = requestReader.readLine()) != null) {
            if (request.length() == 0) {
                break;
            }
            if (request.length() < 3) {
                throw new IllegalArgumentException("Protocol error: " + request); // NOI18N
            }
            // temporaraily we support both old and new protocols here
            if (request.startsWith("*")) { // "*" denotes new protocol
                char charState = request.charAt(1);
                FileState state = FileState.fromId(charState);
                if (state == null) {
                    throw new IllegalArgumentException("Protocol error: unexpected state: '" + charState + "'"); // NOI18N
                }
                String remotePath = request.substring(2);
                String remoteCanonicalPath = requestReader.readLine();
                if (remoteCanonicalPath == null) {
                    throw new IllegalArgumentException("Protocol error: no canoical path for " + remotePath); //NOI18N
                }
                String localFilePath = mapper.getLocalPath(remotePath);
                if (localFilePath != null) {
                    canonicalToAbsolute.put(remoteCanonicalPath, remotePath);
                    File localFile = new File(localFilePath);
                    fileData.setState(localFile, state);
                } else {
                    RemoteUtil.LOGGER.log(Level.FINEST, "LC: ERROR no local file for {0}", remotePath);
                }
            } else {
                // OLD protocol (temporarily)
                //update info about file where we thought file is copied, but it doesn't
                // exist remotely (i.e. project directory was removed)
                if (request.length() < 3 || !request.startsWith("t ")) { // NOI18N
                    throw new IllegalArgumentException("Protocol error: " + request); // NOI18N
                }
                String remoteFile = request.substring(2);
                String localFilePath = mapper.getLocalPath(remoteFile);
                if (localFilePath != null) {
                    File localFile = new File(localFilePath);
                    fileData.setState(localFile, FileState.TOUCHED);
                } else {
                    RemoteUtil.LOGGER.log(Level.FINEST, "LC: ERROR no local file for {0}", remoteFile);
                }
            }
        }
    }
    
    private void sendFileInitRequest(FileGatheringInfo fgi) {
        if(fgi.isLink()) {
            responseStream.printf("L %s\n%s\n", fgi.remotePath, fgi.getLink()); //NOI18N
        } else if (fgi.file.isDirectory()) {
            responseStream.printf("D %s\n", fgi.remotePath); //NOI18N
            responseStream.flush(); //TODO: remove?
        } else {
            File file = fgi.file;
            String remotePath = fgi.remotePath;
            FileData.FileInfo info = fileData.getFileInfo(file);
            FileState newState;
            if (file.exists()) {
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
                        newState = info.state;
                        break;
                    default:
                        CndUtils.assertTrue(false, "Unexpected state: " + info.state); //NOI18N
                        return;
                }
            } else {
                newState = FileState.INEXISTENT;
            }
            CndUtils.assertTrue(newState == FileState.INITIAL || newState == FileState.COPIED 
                    || newState == FileState.TOUCHED || newState == FileState.UNCONTROLLED
                    || newState == FileState.INEXISTENT,
                    "State shouldn't be " + newState); //NOI18N
            responseStream.printf("%c %d %s\n", newState.id, file.length(), remotePath); // NOI18N
            responseStream.flush(); //TODO: remove?
            if (newState == FileState.INITIAL ) {
                newState = FileState.TOUCHED;
            }
            fileData.setState(file, newState);
        }
    }

    private static void gatherFiles(File file, String base, FileFilter filter, List<FileGatheringInfo> files) {
        // it is assumed that the file itself was already filtered
        String fileName = isEmpty(base) ? file.getName() : base + '/' + file.getName();
        addFileGatheringInfo(files, file, fileName);
        if (file.isDirectory()) {
            File[] children = file.listFiles(filter);
            for (File child : children) {
                String newBase = isEmpty(base) ? file.getName() : (base + "/" + file.getName()); // NOI18N
                gatherFiles(child, newBase, filter, files);
            }
        }
    }

    private static void addFileGatheringInfo(List<FileGatheringInfo> filesToFeed, final File file, String remoteFilePathName) {
        //if (file.exists()) {
            filesToFeed.add(new FileGatheringInfo(file, remoteFilePathName));
        //}
    }


    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

}
