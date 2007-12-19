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

import org.netbeans.modules.php.rt.providers.impl.actions.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.rt.providers.impl.AbstractProvider;
import org.netbeans.modules.php.rt.utils.ActionsDialogs;
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
class DownloadFilesCommandImpl extends DownloadFilesCommand {

    static final String LBL_DESTINATION_PATH_INIT_ERROR = "LBL_DestPathInitError"; // NOI18N

    static final String LBL_PATH_IS_SRC_ROOT = "LBL_DocumentPathIsSrcRoot"; // NOI18N

    
    private static Logger LOGGER = Logger.getLogger(AbstractProvider.class.getName());

    public DownloadFilesCommandImpl(Project project, WebServerProvider provider) {
        super(project, provider);
    }

    public DownloadFilesCommandImpl(Project project, boolean notify, Lookup lookup, WebServerProvider provider) {
        super(project, notify, provider);
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        refresh();
        
        Host host = getHost();
        if (!checkHost(host)) {
            return;
        }


        ProgressHandle progress = ProgressHandleFactory.createHandle(getLabel()); // NOI18N
        progress.start();
        
        boolean[] success = new boolean[]{true};
        notifyTransferStarted();
        try {
            String context = getContext();
            FileObject sourceRoot = getSourceRootObject();

            if (!checkDestinationFile(sourceRoot)) {
                success[0] = false;
                return;
            }

            String path = getPath(host, context);

            File file = checkPath(path);
            if (file == null) {
                success[0] = false;
                return;
            } else {
                if (isSrcRoot(file)){
                    return;
                }   
            }

            FileObject[] dstFileObjects = getFileObjects();
            if (dstFileObjects == null) {
                success[0] = false;
            } else {
                copyFiles(file, dstFileObjects, success);
            }
        } finally {
            progress.finish();
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

    private File checkPath(String path) {
        if (path == null) {
            notifyMsg(LBL_DESTINATION_PATH_INIT_ERROR);
            return null;
        }
        File file = new File(path).getAbsoluteFile();
        if (file.exists()) {
            return file;
        }
        return null;
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

    
    private File getFromFile(File fromDir, FileObject dstFileObject){
            String from = getRelativeSrcPath(dstFileObject);
            
            File fromFile = new File(fromDir, from);
            if (!fromFile.exists()) {
                return null;
            }
            return fromFile;
    }
    
    private File getToFile(FileObject fileObject){
        FileObject dstFileObject = fileObject;
        if ( fileObject.equals(getProject().getProjectDirectory() )) {
            dstFileObject = getSourceRootObject();
        }
        return FileUtil.toFile(dstFileObject);
    }
    
    private void copyFiles(File fromDir, FileObject[] dstFileObjects, boolean[] success) {
        //FileObject sourceRoot = getSourceRootObject();

        for (FileObject dstFileObject : dstFileObjects) {

            File fromFile = getFromFile(fromDir, dstFileObject);
            if (fromFile == null){
                continue;
            }
            
            File toFile = getToFile(dstFileObject);
            if (toFile == null){
                continue;
            }
            
            if (fromFile.isDirectory()) {
                copyFolder(success, fromFile, toFile);
            } else {
                try {
                    copyFile(fromFile, toFile);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, null, e);
                    success[0] = false;
                }
            }
        }
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
    
    private void copyFilesRecursively(File[] from, File to, boolean[] errors) {
        for (File file : from) {
            copyFilesRecursively(file, to, errors);
        }
    }

    private void copyFilesRecursively(File from, File to, boolean[] errors) {
        if (!from.isDirectory()) {
            return;
        }

        File[] children = from.listFiles();
        for (File child : children) {
            File dst = new File(to, child.getName());
            if (skipRemoteFile(child) || skipLocalFile(dst)) {
                rememberSkippedFile(child.getPath());
                continue;
            }
            if (child.isDirectory()) {
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

    private boolean skipLocalFile(File file) {
        return isNbProject(file);
    }
    
    private boolean skipRemoteFile(File File) {
        return false;
    }
    
    private void copyFolder(boolean[] errors, File src, File dst) {
        boolean success = dst.mkdirs();
        if (!success && !dst.exists()) {
            errors[0] = false;
        } else {
            copyFilesRecursively(src, dst, errors);
        }
    }

    private void overwriteFile(File from, File to) throws IOException {
        boolean overwrite = false;
        String fileFullName = to.getPath();
        if (getOverwriteFiles() == null) {
            overwrite = confirmOverwrite(fileFullName);
        } else {
            overwrite = getOverwriteFiles().booleanValue();
        }

        if (overwrite) {
            copyFile(from, to);
        } else {
            rememberNotOverwrittenFile(from.getPath());
        }
    }

    FileObject copyFile(File from, File to) throws IOException {
        assert to != null;
        assert from != null;

        File tmpTo = null;
        try {
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
        return null;
    }

    FileObject doCopy(File from, File to) throws IOException {
        assert to != null;
        assert from != null;

        // do not use the fiollowing code because File 'to' doesn't have method to
        // get name without ext easily. FileUtil.copyFile needs name without ext.
        //FileObject fromFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(from));
        //FileObject destFolder = FileUtil.toFileObject(to.getParentFile());
        //if (fromFileObject != null && destFolder != null) {
        //    return FileUtil.copyFile(fromFileObject, destFolder, to.getName());
        //}

        FileObject dest = FileUtil.createData(to);

        FileLock lock = null;
        InputStream bufIn = null;
        OutputStream bufOut = null;

        try {
            lock = dest.lock();
            bufIn = new BufferedInputStream(new FileInputStream(from));

            bufOut = dest.getOutputStream(lock);

            FileUtil.copy(bufIn, bufOut);
        } finally {
            if (bufIn != null) {
                bufIn.close();
            }

            if (bufOut != null) {
                bufOut.close();
            }

            if (lock != null) {
                lock.releaseLock();
            }
        }
        return dest;
    }

    private Project myProject;
}