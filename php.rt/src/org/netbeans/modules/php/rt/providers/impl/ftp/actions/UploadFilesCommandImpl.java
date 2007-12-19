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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.rt.providers.impl.ftp.actions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.rt.providers.impl.actions.UploadFilesCommand;
import org.netbeans.modules.php.rt.providers.impl.ftp.FtpHostImpl;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.FtpClientLoginException;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.FtpException;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.FtpFileInfo;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.impl.FtpConnection;
import org.netbeans.modules.php.rt.providers.impl.ftp.ftpclient.ui.FtpDialogs;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class UploadFilesCommandImpl extends UploadFilesCommand {

    static final String LBL_UPLOAD = "LBL_PutFilesToServer"; // NOI18N
    static final String LBL_DESTINATION_PATH_INIT_ERROR = "LBL_DestPathInitError"; // NOI18N
    static final String LBL_DESTINATION_PATH_NOT_WRITABLE = "LBL_DestPathNotWritable"; // NOI18N
    static final String LBL_DESTINATION_PATH_CREATE_ERROR = "LBL_DestPathCreateError"; // NOI18N
    static final String LBL_ABSENT_FTP_PATH_WAS_CREATED = "LBL_AbsentFtpPathWasCreated"; // NOI18N

    private static Logger LOGGER = Logger.getLogger(UploadFilesCommandImpl.class.getName());

    public UploadFilesCommandImpl(Project project, WebServerProvider provider) {
        super(project, provider);
    }

    public UploadFilesCommandImpl(Project project, boolean notify, WebServerProvider provider) {
        super(project, notify, provider);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Command#getLabel()
     */
    @Override
    public String getLabel() {
        return NbBundle.getMessage(UploadFilesCommandImpl.class, LBL_UPLOAD);
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

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        refresh();
        Host host = getHost();
        if (!checkHost(host)) {
            setInterrupted();
            return;
        }

        ProgressHandle progress = ProgressHandleFactory.createHandle(getLabel()); // NOI18N
        progress.start();
        notifyTransferStarted();

        boolean success = false;
        try {
            FileObject[] fromFileObjects = getFileObjects();
            success = upload(fromFileObjects);
            if (!success){
                setUnsuccess();    
            }
        } finally {
            progress.finish();
            notifyTransferFinished(success);
        }
    }

    @Override
    protected void refresh() {
        super.refresh();
        myHost = null;
    }
    
    @Override
    protected boolean checkHost(Host host) {
        if (!super.checkHost(host)){
            return false;
        }
        return FtpActionUtils.checkHostFtpPart(host, getProject(), getLabel());
    }


    private boolean upload(FileObject[] fromFileObjects){
            FtpHostImpl host = getHost();
            if (!checkHost(host)) {
                return false;
            }

            String context = getContext();
            checkSources();

            boolean[] success = new boolean[]{true};
            try {
                ftpConn = initConnection(host);

                String path = initRootPath(host, context);
                if (path == null) {
                    return false;
                }
                if (!createAndCdFolder(path, true)) {
                    return false;
                }

                if (fromFileObjects == null) {
                    success[0]=false;
                } else {
                    copyFiles(fromFileObjects, success);
                }

            } catch (FtpException ex) {
                success[0]=false;
                String ftpUrl = FtpHostImpl.Helper.getFtpUrl(getHost());
                LOGGER.log(Level.WARNING, "Exception while uploading to " + ftpUrl);
            } finally {
                if (ftpConn != null) {
                    ftpConn.close();
                    ftpConn = null;
                }
            }
            return success[0];
    }
    
    private void copyFilesRecursively(FileObject[] from, boolean[] errors) {
        for (FileObject fileObject : from) {
            copyFilesRecursively(fileObject, errors);
        }
    }

    private void copyFilesRecursively(FileObject from, boolean[] errors) {
        if (!from.isFolder()) {
            return;
        }

        Collection<String> ftpChildren = loadChildrenList();

        if (ftpChildren == null) {
            errors[0] = false;
            return;
        }

        FileObject[] children = from.getChildren();
        for (FileObject child : children) {
            String dst = child.getNameExt();
            if (skipFile(child)) {
                rememberSkippedFile(getRelativeSrcPath(child));
                continue;
            }
            if (child.isFolder()) {
                //boolean isDirExists = ftpChildren.contains(dst);
                copyFolder(errors, child, dst);
            } else {
                try {
                    if (ftpChildren.contains(dst)) {
                        overwriteFile(child, dst, ftpChildren);
                    } else {
                        copyFile(child, dst);
                    }
                } catch (IOException e) {
                    errors[0] = false;
                }
            }
        }
    }

    // tmp method. ftpConn.listNames throws exception for empty ftp directory..
    private Collection<String> loadChildrenList() {
        Collection<String> resultList = new ArrayList<String>();
        try {
            Collection<FtpFileInfo> list  = ftpConn.list();
            for (FtpFileInfo item : list){
                resultList.add(item.getName());
            }
        } catch (FtpException ex) {
        }
        return resultList;
    }

    /**
     * Changes directory to dst and copyes all content from FileObject child to it.
     * If can't go into directory, tries to create it.
     */
    private void copyFolder(boolean[] errors, FileObject child, String dst) {
        String startDir = ftpConn.getCashedCurrDir();
        try {
            if (!createAndCdFolder(dst)) {
                errors[0] = false;
                return;
            }
            copyFilesRecursively(child, errors);
        } finally {
            if (!startDir.equals(ftpConn.getCashedCurrDir())) {
                try {
                    ftpConn.cd(startDir);
                } catch (FtpException ex) {
                    errors[0] = false;
                }
            }
        }
    }

    /**
     * tries to create folder and cd to it.
     * @return true if folder already existed or was successfully created.
     * false if folder is not created.
     */
    private boolean createAndCdFolder(String dst) {
        return createAndCdFolder(dst, false);
    }
    
    private boolean createAndCdFolder(String dst, boolean notify) {
        // try to cd to dir
        try {
            ftpConn.cd(dst);
            return true;
        } catch (FtpException cdException) {
            // just go forward
        }
        // dir doesn't exist.
        // try to create and cd
        try {
            ftpConn.mkdir(dst);
            ftpConn.cd(dst);
            
            if (notify){
                // log with ftp error prefixes, added by this.notifyMsg()
                // fix logging methods names
                super.notifyMsg(LBL_ABSENT_FTP_PATH_WAS_CREATED, dst);
            }
            
            return true;
        } catch (FtpException mkdirException) {
            // just go forward
        }

        notifyMsg(LBL_DESTINATION_PATH_CREATE_ERROR, dst);
        return false;
    }

    private void copyFiles(FileObject[] fromFileObjects, boolean[] success) {
        for (FileObject fileObject : fromFileObjects) {
            String path = getRelativeSrcPath(fileObject);
            if (path == null) {
                // TODO
            } else if (path.equals("")) {
                FileObject[] sources = getSourceObjects(getProject());
                copyFilesRecursively(sources, success);
            } else {

                if (fileObject.isFolder()) {
                    copyFolder(success, fileObject, path);
                } else {
                    FileObject parentObject = fileObject.getParent();
                    String parentPath = getRelativeSrcPath(parentObject);

                    if (parentPath == null) {
                        parentPath = getRootPath();
                    }

                    if (!createAndCdFolder(parentPath)) {
                        success[0] = false;
                        continue;
                    }

                    Collection<String> ftpChildren = loadChildrenList();

                    try {
                        String dst = fileObject.getNameExt();
                        if (ftpChildren.contains(dst)) {
                            overwriteFile(fileObject, dst, ftpChildren);
                        } else {
                            copyFile(fileObject, dst);
                        }
                    } catch (IOException e) {
                        success[0] = false;
                        continue;
                    }
                }
            }
        }
    }

    private FtpConnection initConnection(FtpHostImpl ftpHost) throws FtpException {
        FtpConnection conn = null;
        String ftpServer = (String) ftpHost.getProperty(FtpHostImpl.FTP_SERVER);
        FtpConnection.FtpLogger ftpLogger 
                = new FtpConnection.OutputTabFtpLogger(getOutputTabTitle());

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
            throw new FtpClientLoginException(problem.getMessage());
        }
        return conn;
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

    private String initRootPath(FtpHostImpl host, String context) {
        myRootPath = FtpHostImpl.Helper.getFtpInitialDirWithSubdir(host, context);

        if (myRootPath == null) {
            notifyMsg(LBL_DESTINATION_PATH_INIT_ERROR);
        }
        return getRootPath();
    }

    private String getRootPath() {
        return myRootPath;
    }

    private void overwriteFile(FileObject from, String to, Collection<String> parentListing) throws IOException {
        boolean overwrite = false;
        if (getOverwriteFiles() == null) {
            overwrite = confirmOverwrite(to);
            if (!ftpConn.isConnectionOpen()) {
                // we can use getHost() result without check
                // because we have checked it already
                ftpConn = initConnection(getHost());
            }
        } else {
            overwrite = getOverwriteFiles().booleanValue();
        }
        if (overwrite) {
            //ftpConn.logMessage(">>> overwrite child: " + to);
            String tmpName = System.currentTimeMillis() + to;
            while (parentListing.contains(tmpName)) {
                tmpName = System.currentTimeMillis() + to;
            }
            copyFile(from, tmpName);
            ftpConn.delete(to);
            ftpConn.rename(tmpName, to);
        } else {
            rememberNotOverwrittenFile(getRelativeSrcPath(from));
        }
    }

    private void copyFile(FileObject from, String to) throws IOException {

        InputStream srcStream = null;
        OutputStream dstStream = null;
        FileLock lock = null;

        saveFile(from);
        File src = FileUtil.toFile(from);

        lock = from.lock();
        try {
            ftpConn.putFile(src, to);
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
        rememberCopiedFile(from.getPath());
    }

    private boolean skipFile(FileObject file) {
        return isTempFile(file)
                || isNbProject(file);
    }


    private String myRootPath = null;
    private FtpConnection ftpConn = null;
    private FtpHostImpl myHost;
    
}