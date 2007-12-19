/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.rt.providers.impl.ftp.actions;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.rt.providers.impl.actions.DownloadFilesCommand;
import org.netbeans.modules.php.rt.providers.impl.ftp.FtpHostImpl;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.FtpClientLoginException;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.impl.FtpConnection;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.ui.FtpDialogs;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.FtpException;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.FtpFileInfo;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public class DownloadFilesCommandImpl extends DownloadFilesCommand {

    static final String LBL_FTP_PATH_INIT_ERROR = "LBL_FtpPathInitError"; // NOI18N
    static final String LBL_FTP_PATH_NOT_READABLE = "LBL_FtpPathNotReadable"; // NOI18N
    //static final String LBL_NOTHING_TO_DOWNLOAD = "LBL_FtpNothingToDownload"; // NOI18N
    static final String DOWNLOAD_LABEL = "LBL_DownloadFilesFromServer";
    static final String LBL_DOWNLOADING_EXCEPTION = "LBL_ExceptionDuringDownloadFrom";

    public DownloadFilesCommandImpl(Project project,  WebServerProvider provider) {
        super(project, provider);
    }

    public DownloadFilesCommandImpl(Project project, boolean notify, WebServerProvider provider) {
        super(project, notify, provider);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Command#getLabel()
     */
    @Override
    public String getLabel() {
        return NbBundle.getMessage(UploadFilesCommandImpl.class, DOWNLOAD_LABEL);
    }

    public void run() {
        refresh();
        Host host = getHost();
        if (!checkHost(host)) {
            return;
        }
        
        
        ProgressHandle progress = ProgressHandleFactory.createHandle(getLabel()); // NOI18N
        progress.start();
        notifyTransferStarted();

        boolean success = false;
        try {
            FileObject[] toFileObjects = getFileObjects();
            success = download(toFileObjects);

        } finally {
            progress.finish();
            notifyTransferFinished(success);
        }
    }

    @Override
    protected boolean checkHost(Host host) {
        if (!super.checkHost(host)){
            return false;
        }
        return FtpActionUtils.checkHostFtpPart(host, getProject(), getLabel());
    }

    @Override
    protected void notifyMsg(String bundleKey, Object... args) {
        notifyMsg(bundleKey, getClass(), args);
    }

    @Override
    protected void notifyMsg(String bundleKey, Class clazz, Object... args) {
        if (ftpConn != null) {
            String msg = loadFormattedMsg(bundleKey, clazz, args);
            ftpConn.getLogger().logError(msg);
        } else {
            super.notifyMsg(bundleKey, clazz, args);
        }
    }

    @Override
    protected FtpHostImpl getHost() {
        if (myHost == null) {

            Host host = super.getHost();
            if (host instanceof FtpHostImpl) {
                myHost = (FtpHostImpl) host;
            }
        }

        return myHost;
    }

    private boolean download(FileObject[] toFileObjects) {
        if (!initDownloadData()) {
            return false;
        }
        
        boolean[] success = new boolean[]{true};
        try {
            ftpConn = initConnection(getHost());

            if (toFileObjects == null) {
                success[0] = false;
            } else {
                copyFilesTo(toFileObjects, success);
            }

        } catch (FtpException ex) {
            success[0]=false;
            String ftpUrl = FtpHostImpl.Helper.getFtpUrl(getHost());
            notifyMsg(LBL_DOWNLOADING_EXCEPTION, ftpUrl, ex.getMessage());
        } finally {
            if (ftpConn != null) {
                ftpConn.close();
                ftpConn = null;
            }
        }
        return success[0];
    }

    @Override
    protected void refresh() {
        super.refresh();
        myHost = null;
    }

    private boolean initDownloadData() {
        FtpHostImpl host = getHost();
        if (!checkHost(host)) {
            return false;
        }

        String path = initRootPath(getHost(), getContext());
        if (path == null) {
            return false;
        }
        return true;
    }

    private void copyFilesTo(FileObject[] toFileObjects, boolean[] success) {
        for (FileObject fileObject : toFileObjects) {
            String path = getRelativeSrcPath(fileObject);

            if (path == null){
                rememberSkippedFile(fileObject.getPath());

                continue;
            } else if (path.equals("")){
                copyAllFiles(success);
            } else {

                if (!checkDestinationFile(fileObject)) {
                    continue;
                }

                String parentPath = getParentDirPath(fileObject);
                if (!cdFolder(parentPath)) {
                    success[0] = false;
                    continue;
                }

                //FtpFileInfo from = loadFtpFileInfo(parentPath, fileObject.getNameExt());
                //if (from == null) {
                //    continue;
                //}
                FtpFileInfo from = new FtpFileInfo(path);

                File dst = FileUtil.toFile(fileObject);
                if (dst.isDirectory()) {
                    copyFolder(success, from, dst);
                } else {
                    try {
                        copyFile(from, dst);
                    } catch (IOException e) {
                        success[0] = false;
                    }
                }
            }
        }
    }

    private void copyAllFiles(boolean[] errors) {
        if (!cdFolder(getRootPath())) {
            errors[0] = false;
            return;
        }
        File sourceRoot = FileUtil.toFile(getSourceRootObject());

        copyFilesRecursively(null, sourceRoot, errors);
    }

    private void copyFilesRecursively(FtpFileInfo from, File to, boolean[] errors) {
        // from == null means root ftp directpry+context
        // TODO: from.isDirectory() is commented becauyse manually created from object
        //       doesn't have dir flag.
        //if (from != null && !from.isDirectory()) {
        //    return;
        //}

        Collection<FtpFileInfo> ftpChildren = loadChildrenList();

        if (ftpChildren == null) {
            errors[0] = false;
            return;
        }
        
        for (FtpFileInfo src : ftpChildren) {
            File dst = new File(to, src.getName());
            if (skipRemoteFile(src) || skipLocalFile(dst)) {
                rememberSkippedFile(src.getFullName());
                continue;
            }
            
            if (src.isDirectory()) {
                copyFolder(errors, src, dst);
            } else {
                try {
                    if (dst.exists()) {
                        overwriteFile(src, dst);
                    } else {
                        copyFile(src, dst);
                    }
                } catch (IOException e) {
                    errors[0] = false;
                }
            }
        }
    }

    private String getParentDirPath(FileObject fileObject) {
        FileObject parentObject = fileObject.getParent();
        String parentPath = getRelativeSrcPath(parentObject);

        if (parentPath == null) {
            return getRootPath();
        } else {
            return getRootPath() + "/" + parentPath;
        }
    }
    
    /*
    private FtpFileInfo loadFtpFileInfo(String parentDir, String fileName) {
        if (cachedFtpListing != null && cachedFtpListing.containsKey(parentDir)) {
            Collection<FtpFileInfo> list = cachedFtpListing.get(parentDir);
            for (FtpFileInfo file : list) {
                if (file.getName().equals(fileName)) {
                    return file;
                }
            }
        }

        try {
            if (cachedFtpListing == null) {
                cachedFtpListing = new HashMap<String, Collection<FtpFileInfo>>();
            }
            Collection<FtpFileInfo> list = ftpConn.list();
            cachedFtpListing.put(parentDir, list);
            for (FtpFileInfo file : list) {
                if (file.getName().equals(fileName)) {
                    return file;
                }
            }
            notifyMsg(LBL_NOTHING_TO_DOWNLOAD, 
                    parentDir+"/"+fileName, getHost().getName());
        } catch (FtpException listEx) {
            return null;
        }
        return null;
    }
    */
    
    private boolean skipLocalFile(File file) {
        return isNbProject(file);
    }
    
    private boolean skipRemoteFile(FtpFileInfo ftpFile) {
        if (ftpFile.isLink()) {
            return true;
        }
        if (ftpFile.getName().equals(FtpConnection.getCurrentDirPattern())) {
            return true;
        }
        if (ftpFile.getName().equals(FtpConnection.getParentDirPattern())) {
            return true;
        }
        return false;
    }

    private void copyFolder(boolean[] errors, FtpFileInfo src, File dst) {
        boolean needCdUp = false;

        try {
            boolean success = dst.mkdirs();
            if (!success && !dst.exists()) {
                errors[0] = false;
            } else {
                ftpConn.cd(src.getName());
                needCdUp = true;

                copyFilesRecursively(src, dst, errors);
            }
        } catch (FtpException ex) {
            errors[0] = false;
        } finally {
            if (needCdUp) {
                try {
                    ftpConn.cdUp();
                } catch (FtpException ex) {
                    errors[0] = false;
                }
            }
        }
    }

    private void copyFile(FtpFileInfo from, File to) throws IOException {
        File tmpTo = null;
        try {
            // create temporary not existing name
            tmpTo = getNotExistingTmpFile(to);

            // copy as file with tmp name and then rename to real name
            doCopy(from, tmpTo);
            to.delete();
            tmpTo.renameTo(to);
            rememberCopiedFile(to.getPath());
            refreshParent(to);
        } finally {
            if (tmpTo != null && tmpTo.exists()) {
                tmpTo.delete();
            }
        }
    }
    
    private void doCopy(FtpFileInfo from, File to) throws IOException {

        FileLock lock = null;

        //to.createNewFile();
        //lock = FileUtil.toFileObject(to).lock();
        lock = FileUtil.createData(to).lock();
        try {
            ftpConn.getFile(from.getName(), to);
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }

    private void overwriteFile(FtpFileInfo from, File to) throws IOException {
        boolean overwrite = false;
        String fileFullName = to.getPath();
        
        if (getOverwriteFiles() == null) {
            overwrite = confirmOverwrite(fileFullName);
            if (!ftpConn.isConnectionOpen()) {
                ftpConn = initConnection(getHost());
            }
        } else {
            overwrite = getOverwriteFiles().booleanValue();
        }
        
        if (overwrite) {
            copyFile(from, to);
        } else {
            rememberNotOverwrittenFile(from.getFullName());
        }
    }

    private Collection<FtpFileInfo> loadChildrenList() {
        try {
            //String dirToLoad = dirInfo != null ? dirInfo.getFullName() : ftpConn.pwd();
            return ftpConn.list();
        } catch (FtpException ex) {
        }
        return null;
    }

    private String initRootPath(FtpHostImpl host, String context) {
        myRootPath = FtpHostImpl.Helper.getFtpInitialDirWithSubdir(host, context);
        
        if (myRootPath == null) {
            notifyMsg(LBL_FTP_PATH_INIT_ERROR);
        }
        return getRootPath();
    }

    private boolean cdFolder(String path) {
        try {
            ftpConn.cd(path);
        } catch (FtpException ex) {
            notifyMsg(LBL_FTP_PATH_NOT_READABLE, path);
            return false;
        }
        return true;
    }

    private String getRootPath() {
        return myRootPath;
    }

    private FtpConnection initConnection(FtpHostImpl ftpHost) throws FtpException {
        FtpConnection conn = null;
        String ftpServer = (String) ftpHost.getProperty(FtpHostImpl.FTP_SERVER);
        FtpConnection.FtpLogger ftpLogger 
                = new FtpConnection.OutputTabFtpLogger(getOutputTabTitle());
                //= new FtpConnection.OutputTabFtpLogger(getOutputTabTitle(), true);
        
        boolean retry = true;
        Throwable problem = null;
        while (retry) {
            String ftpUserName = (String) ftpHost.getProperty(FtpHostImpl.FTP_USER_NAME);
            char[] ftpPassword = (char[]) ftpHost.getProperty(FtpHostImpl.FTP_PASSWORD);
            try {
                if (conn != null) {
                    conn.closeServer();
                }
                conn = FtpConnection.createConnection(ftpServer, ftpLogger);
                conn.login(ftpUserName, String.copyValueOf(ftpPassword));
                retry = false;
                problem = null;
            } catch (FtpClientLoginException lex) {
                problem = lex;
                retry = FtpDialogs.retryLoginDialog(ftpHost);
            }
        }
        if (problem != null) {
            throw new FtpException(problem.getMessage());
        }
        return conn;
    }

    private String myRootPath = null;
    
    private FtpConnection ftpConn = null;
    
    private FtpHostImpl myHost;
    
    /**
     * map to cache dirs listing.
     * directory name - List with FtpFileInfo
     */
    //private Map<String, Collection<FtpFileInfo>> cachedFtpListing = null;
    
}