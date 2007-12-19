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
package org.netbeans.modules.php.rt.providers.impl.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.rt.providers.impl.AbstractProvider;
import org.netbeans.modules.php.rt.providers.impl.actions.UploadFilesCommand;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;


/**
 * @author ads
 *
 */
class UploadFilesCommandImpl extends UploadFilesCommand {

    static final String LBL_DESTINATION_PATH_INIT_ERROR = "LBL_DestPathInitError"; // NOI18N
    static final String LBL_DESTINATION_PATH_NOT_WRITABLE = "LBL_DestPathNotWritable"; // NOI18N
    static final String LBL_DESTINATION_PATH_CREATE_ERROR = "LBL_DestPathCreateError"; // NOI18N

    static final String LBL_PATH_IS_SRC_ROOT = "LBL_DocumentPathIsSrcRoot"; // NOI18N

    private static Logger LOGGER = Logger.getLogger(AbstractProvider.class.getName());

    UploadFilesCommandImpl(Project project, WebServerProvider provider) {
        super(project, provider);
    }

    UploadFilesCommandImpl(Project project, boolean notify, Lookup lookup, WebServerProvider provider) {
        super(project, notify, provider);
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        LOGGER.info(">>> local UploadFilesCommandImpl run");
        refresh();
        

        Host host = getHost();
        if (!checkHost(host)) {
            setInterrupted();
            return;
        }

        
        ProgressHandle progress = ProgressHandleFactory.createHandle(getLabel()); // NOI18N
        progress.start();


        boolean[] success = new boolean[]{true};
        notifyTransferStarted();
        try {

            String context = getContext();

            String path = getPath(host, context);

            File file = checkPath(path);
            if (file == null) {
                return;
            }

            if (isSrcRoot(file)) {
                return;
            }
            checkSources();

            if (getFileObjects() == null) {
                success[0] = false;
                //copyFilesRecursively(sources, file, success);
            } else {
                copyFiles(file, success);
            }
        } finally {
            progress.finish();
            if (!success[0]){
                setUnsuccess();
            }
            notifyTransferFinished(success[0]);
        }
    }

    @Override
    protected boolean checkHost(Host host) {
        if (!super.checkHost(host)){
            return false;
        }
        return LocalActionUtils.checkHostFilePart(host, getProject(), getLabel());
    }

    private void copyFilesRecursively(FileObject[] from, File to, boolean[] errors) {
        for (FileObject fileObject : from) {
            copyFilesRecursively(fileObject, to, errors);
        }
    }

    private void copyFilesRecursively(FileObject from, File to, boolean[] errors) {
        if (!from.isFolder()) {
            return;
        }

        FileObject[] children = from.getChildren();
        for (FileObject child : children) {
            File dst = new File(to, child.getNameExt());
            if (skipFile(child)) {
                rememberSkippedFile(getRelativeSrcPath(child));
                continue;
            }
            if (child.isFolder()) {
                copyFolder(errors, child, dst);
            } else {
                try {
                    if (dst.exists()) {
                        overwriteFile(child, dst);
                    } else {
                        copyFile(child, dst);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, null, e);
                    errors[0] = false;
                }
            }
        }
    }

    private void copyFolder(boolean[] errors, FileObject child, File dst) {
        boolean success = dst.mkdirs();
        if (!success && !dst.exists()) {
            errors[0] = false;
        } else {
            copyFilesRecursively(child, dst, errors);
        }
    }

    private FileObject getFromFile(FileObject fileObject){
        FileObject srcFileObject = fileObject;
        if ( fileObject.equals(getProject().getProjectDirectory() )) {
            srcFileObject = getSourceRootObject();
                    
        }
        return srcFileObject;
    }
    
    private File getToFile(File toDir, FileObject fileObject){
            String path = getRelativeSrcPath(fileObject);
            if (path != null) {
                return new File(toDir, path);
            } else {
                // TODO
            }
            return null;

    }
    
    private void copyFiles(File toDir, boolean[] success) {
        for (FileObject fileObject : getFileObjects()) {
            FileObject fromFile = getFromFile(fileObject);
            if (fromFile == null) {
                continue;
            }

            File toFile = getToFile(toDir, fileObject);
            if (toFile == null) {
                continue;
            }

            if (fromFile.isFolder()) {
                copyFolder(success, fromFile, toFile);
            } else {
                try {
                    toFile.getParentFile().mkdirs();
                    if (toFile.exists()) {
                        overwriteFile(fromFile, toFile);
                    } else {
                        copyFile(fromFile, toFile);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, null, e);
                    success[0] = false;
                }
            }
        }
    }

    private boolean isSrcRoot(File file){
        assert file != null;
        FileObject[] sources = getSourceObjects(getProject());
        for (FileObject source : sources) {
            if (areEqual(source, file)){
                notifyMsg(LBL_PATH_IS_SRC_ROOT, file.getPath());
                return true;
            }
        }
        return false;
    }

    private boolean areEqual(FileObject fo, File f){
        try {
            assert fo != null && f != null;
            File foFile = FileUtil.toFile(fo);

            return foFile.getCanonicalFile().equals(f.getCanonicalFile());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return false;
        }
        
    }
    
    private File checkPath(String path) {
        if (path == null) {
            notifyMsg(LBL_DESTINATION_PATH_INIT_ERROR);
            return null;
        }
        File file = new File(path).getAbsoluteFile();
        if (file.exists()) {
            if (!file.canWrite()) {
                notifyMsg(LBL_DESTINATION_PATH_NOT_WRITABLE, file.getPath());
            } else {
                return file;
            }
        } else {
            if (!file.mkdirs()) {
                notifyMsg(LBL_DESTINATION_PATH_CREATE_ERROR, file.getPath());
            } else {
                return file;
            }
        }
        return null;
    }

    private String getPath(Host host, String context) {
        assert host instanceof LocalHostImpl;
        LocalHostImpl impl = (LocalHostImpl) host;
        String hostDocumentRoot = (String) impl.getProperty( LocalHostImpl.DOCUMENT_PATH );

        if (hostDocumentRoot != null) {
            if (hostDocumentRoot.endsWith(File.separator)) {
                hostDocumentRoot = hostDocumentRoot.substring(0, hostDocumentRoot.length() - 1);
            }
            String contextPath = context.replace('/', File.separatorChar);
            if (!contextPath.startsWith(File.separator)){
                contextPath = File.separator + contextPath;
            }
            return hostDocumentRoot + contextPath;
        }
        return null;
    }

    private void overwriteFile(FileObject from, File to) throws IOException {
        boolean overwrite = false;
        String fileFullName = to.getPath();
        if (getOverwriteFiles() == null) {
            overwrite = confirmOverwrite(fileFullName);
        } else {
            overwrite = getOverwriteFiles().booleanValue();
        }
        if (overwrite) {
            copyFile(from, to);
            /* Do not need to copy file with temp name and then rename as we do with ftp.
             *
            File tmpDst = null;
            do {
            File parent = to.getParentFile();
            if (parent != null) {
            tmpDst = new File(parent, System.currentTimeMillis() + to.getName());
            } else {
            tmpDst = new File(System.currentTimeMillis() + to.getName());
            }
            } while (tmpDst.exists());
            // copy as file with tmp name and then rename to real name
            copyFile(from, tmpDst);
            to.delete();
            // TODO test if we should create new File object after deletion to use the same name
            tmpDst.renameTo(to);
             */
        } else {
            rememberNotOverwrittenFile(getRelativeSrcPath(from));
        }
    }

    private void copyFile(FileObject from, File to) throws IOException {
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        FileLock lock = null;

        saveFile(from);
        lock = from.lock();
        try {
            srcChannel = new FileInputStream(FileUtil.toFile(from)).getChannel();
            dstChannel = new FileOutputStream(to).getChannel();
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
            // Close the channels
            if (srcChannel != null) {
                srcChannel.close();
            }
            if (dstChannel != null) {
                dstChannel.close();
            }
        }
        rememberCopiedFile(from.getPath());
    }

    private boolean skipFile(FileObject file) {
        return isTempFile(file)
                || isNbProject(file);
    }

}