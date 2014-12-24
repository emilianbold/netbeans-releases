/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import org.netbeans.modules.remote.api.ServerList;
import org.netbeans.modules.remote.api.ServerRecord;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remotefs.versioning.api.RemoteVcsSupport;
import org.netbeans.modules.subversion.remote.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander Simon
 */
public final class VCSFileProxySupport {
    private VCSFileProxySupport(){
    }

    public static final class VCSFileProxyComparator implements Comparator<VCSFileProxy> {

        @Override
        public int compare(VCSFileProxy o1, VCSFileProxy o2) {
            return o1.getPath().compareTo(o2.getPath());
        }
    }
    
    public static void delete(VCSFileProxy file) {
        File javaFile = file.toFile();
        if (javaFile != null) {
            javaFile.delete();
        } else {
            // TODO: rewrite it with using sftp
            ExitStatus status = ProcessUtils.executeInDir(file.getParentFile().getPath(), null, false, new ProcessUtils.Canceler(), VersioningSupport.createProcessBuilder(file), "rm", "-rf", file.getPath());
            if (!status.isOK()) {
                ProcessUtils.LOG.log(Level.INFO, status.toString());
            }
        }
    }
    
    public static void deleteOnExit(VCSFileProxy file) {
        //TODO: implemetn it!
    }

    public static void setLastModified(VCSFileProxy file, long time) {
        throw new UnsupportedOperationException();
    }
    
    public static boolean mkdir(VCSFileProxy file) {
        File javaFile = file.toFile();
        if (javaFile != null) {
            return javaFile.mkdir();
        } else {
            // TODO: rewrite it with using sftp
            ExitStatus status = ProcessUtils.executeInDir(file.getParentFile().getPath(), null, false, new ProcessUtils.Canceler(), VersioningSupport.createProcessBuilder(file), "mkdir", file.getPath());
            if (!status.isOK()) {
                ProcessUtils.LOG.log(Level.INFO, status.toString());
                return false;
            } else {
                // TODO: make sure that file.exists() returns true
                return true;
            }
        }
    }
    
    public static boolean mkdirs(VCSFileProxy file) {
        File javaFile = file.toFile();
        if (javaFile != null) {
            return javaFile.mkdirs();
        } else {
            // TODO: rewrite it with using sftp
            ExitStatus status = ProcessUtils.executeInDir(file.getParentFile().getPath(), null, false, new ProcessUtils.Canceler(), VersioningSupport.createProcessBuilder(file), "mkdir", "-p", file.getPath());
            if (!status.isOK()) {
                ProcessUtils.LOG.log(Level.INFO, status.toString());
                return false;
            } else {
                // TODO: make sure that file.exists() returns true
                return true;
            }
        }
    }
    
    public static VCSFileProxy fromURI(URI uri) {
        if ("file".equals(uri.getScheme())) { // NOI18N
            return VCSFileProxy.createFileProxy(new File(uri));
        } else {
            try {
                List<String> segments = new ArrayList<>();
                FileObject fo = findExistingParent(uri, segments);
                VCSFileProxy res = VCSFileProxy.createFileProxy(fo);
                for (int i = segments.size() - 1; i >= 0; i--) {
                    res = VCSFileProxy.createFileProxy(res, segments.get(i));
                }
                return res;
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }

    private static FileObject findExistingParent(URI uri, List<String> segments) throws MalformedURLException, URISyntaxException {
        while (true) {
            URL url =  uri.toURL();
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                return fo;
            }
            String path = uri.getPath();
            int i = path.indexOf('/');
            if (i < 0) {
                i = path.indexOf('\\');
            }
            if (i <= 0) {
                throw new MalformedURLException();
            }
            segments.add(path.substring(i+1));
            path = path.substring(0, i);
            uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(), uri.getFragment());
        }
    }
    
    public static URI toURI(VCSFileProxy file) {
        File javaFile = file.toFile();
        if (javaFile != null) {
            return javaFile.toURI();
        }
        try {
            List<String> segments = new ArrayList<>();
            FileObject fo = findExistingParent(file, segments);
            URI res = fo.toURI();
            for (int i = segments.size() - 1; i >= 0; i--) {
                String path;
                if (res.getPath().endsWith("/")) {
                    path = res.getPath()+segments.get(i);
                } else {
                    path = res.getPath()+"/"+segments.get(i);
                }
                res = new URI(res.getScheme(), res.getUserInfo(), res.getHost(), res.getPort(), path, res.getQuery(), res.getFragment());
            }
            return res;
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private static FileObject findExistingParent(VCSFileProxy file, List<String> segments) throws FileNotFoundException {
        while (true) {
            FileObject fo = file.toFileObject();
            if (fo != null) {
                return fo;
            }
            segments.add(file.getName());
            file = file.getParentFile();
            if (file == null) {
                throw new FileNotFoundException();
            }
        }
    }
    
    public static boolean isSymlink(VCSFileProxy file, VCSFileProxy root) {
        return RemoteVcsSupport.isSymlink(file);
    }
    
    public static boolean canRead(VCSFileProxy file) {
        return RemoteVcsSupport.canRead(file);
    }
    
    public static boolean canRead(VCSFileProxy base, String subdir) {
        return RemoteVcsSupport.canRead(base, subdir);
    }
    
    public static OutputStream getOutputStream(VCSFileProxy file) throws IOException {
        File toFile = file.toFile();
        if (toFile != null) {
            return new FileOutputStream(toFile);
        } else {
            FileObject fo = file.toFileObject();
            if (fo != null) {
                return fo.getOutputStream();
            }
            VCSFileProxy parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                mkdirs(parentFile);
            }
            // TODO: rewrite it with using sftp
            ExitStatus status = ProcessUtils.executeInDir(parentFile.getPath(), null, false, new ProcessUtils.Canceler(), VersioningSupport.createProcessBuilder(file), "touch", file.getName());
            if (!status.isOK()) {
                ProcessUtils.LOG.log(Level.INFO, status.toString());
                throw new IOException(status.toString());
            } else {
                // TODO: make sure that file.exists() returns true
                parentFile.toFileObject().refresh();
                return file.toFileObject().getOutputStream();
            }
        }
    }
    
    public static long length(VCSFileProxy file) {
        return RemoteVcsSupport.getSize(file);
    }
    
    public static String getCanonicalPath(VCSFileProxy file) throws IOException {
        return RemoteVcsSupport.getCanonicalPath(file);
    }

    public static VCSFileProxy getCanonicalFile(VCSFileProxy file) throws IOException {
        return RemoteVcsSupport.getCanonicalFile(file);
    }
    
    public static VCSFileProxy generateTemporaryFile(VCSFileProxy file, String name) {
        throw new UnsupportedOperationException();
    }

    public static VCSFileProxy createTempFile(VCSFileProxy file, String prefix, String suffix, boolean deleteOnExit) throws IOException {
        File javaFile = file.toFile();
        if (javaFile != null) {
            File res = File.createTempFile(prefix, suffix);
            res.deleteOnExit();
            return VCSFileProxy.createFileProxy(res);
        } else {
            // TODO: review it!
            return VCSFileProxy.createFileProxy(file.toFileObject().createData(prefix+Long.toString(System.currentTimeMillis()), suffix));
        }
    }
    
    /**
     * Creates a temporary folder. The folder will have deleteOnExit flag set to <code>deleteOnExit</code>.
     * @return
     */
    public static VCSFileProxy getTempFolder(VCSFileProxy file, boolean deleteOnExit) throws FileStateInvalidException, IOException {
        FileObject tmpDir = VCSFileProxySupport.getFileSystem(file).getTempFolder();
        for (;;) {
            try {
                //TODO: support delete on exit
                FileObject dir = tmpDir.createFolder("vcs-" + Long.toString(System.currentTimeMillis())); // NOI18N
                return VCSFileProxy.createFileProxy(dir).normalizeFile();
            } catch (IOException ex) {
                continue;
            }
        }
    }
    
    public static boolean renameTo(VCSFileProxy from, VCSFileProxy to){
        throw new UnsupportedOperationException();
    }

    public static boolean copyFile(VCSFileProxy from, VCSFileProxy to) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Returns the first found file whose filename is the same (in a case insensitive way) as given <code>file</code>'s.
     * @param file
     * @return the first found file with the same name, but ignoring case, or <code>null</code> if no such file is found.
     */
    public static String getExistingFilenameInParent(VCSFileProxy file) {
        String filename = null;
        if (file == null) {
            return filename;
        }
        VCSFileProxy parent = file.getParentFile();
        if (parent == null) {
            return filename;
        }
        VCSFileProxy[] children = parent.listFiles();
        if (children == null) {
            return filename;
        }
        for (VCSFileProxy child : children) {
            if (file.getName().equalsIgnoreCase(child.getName())) {
                filename = child.getName();
                break;
            }
        }
        return filename;
    }
    
    /**
     * Copies the specified sourceFile to the specified targetFile.
     * It <b>closes</b> the input stream.
     */
    public static void copyStreamToFile(InputStream inputStream, VCSFileProxy targetFile) throws IOException {
        OutputStream outputStream = null;
        try {            
            outputStream = VCSFileProxySupport.getOutputStream(targetFile);
            try {
                byte[] buffer = new byte[32768];
                for (int readBytes = inputStream.read(buffer);
                     readBytes > 0;
                     readBytes = inputStream.read(buffer)) {
                    outputStream.write(buffer, 0, readBytes);
                }
            } catch (IOException ex) {
                VCSFileProxySupport.delete(targetFile);
                throw ex;
            }
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
    }
    
    public static boolean isRemoteFileSystem(VCSFileProxy file) {
        return file.toFile() == null;
    }
    
    public static VCSFileProxy getResource(VCSFileProxy file, String absPath) {
        VCSFileProxy parent = file;
        while (true) {
            parent = file.getParentFile();
            if (parent == null) {
                parent = file;
                break;
            }
            file = parent;
        }
        return VCSFileProxy.createFileProxy(parent, absPath.substring(1));
    }

    public static VCSFileProxy getResource(FileSystem fileSystem, String absPath) {
        VCSFileProxy root = VCSFileProxy.createFileProxy(fileSystem.getRoot());
        return VCSFileProxy.createFileProxy(root, absPath);
    }
    
    public static VCSFileProxy getHome(VCSFileProxy file){
        //TODO: implement it!
        // temporary use local home path for remote home
        return VCSFileProxySupport.getResource(file, System.getProperty("user.home"));
    }

    /**
     * Creates or gets the user-localhost folder to persist svn configurations.
     * @return
     */
    public static VCSFileProxy getRemotePeristenceFolder(FileSystem fileSystem) throws FileStateInvalidException, IOException {
        FileObject tmpDir = fileSystem.getTempFolder();
        String userName = "user"; //RemoteExecutionEnvironment.getUser();
        String folderName = "svn_" + userName; // NOI18N
        FileObject res = tmpDir.getFileObject(folderName);
        if (res == null || !res.isValid()) {
            res = tmpDir.createFolder(folderName);
        }
        // TODO: create subfolder with hash of localhost
        return VCSFileProxy.createFileProxy(res);
    }
    
    public static boolean isMac(VCSFileProxy file) {
        return RemoteVcsSupport.isMac(file);
    }
    
    public static boolean isUnix(VCSFileProxy file){
        return RemoteVcsSupport.isUnix(file);
    }
    
    public static String getFileSystemKey(FileSystem file) {
        return RemoteVcsSupport.getFileSystemKey(file);
    }
    
    public static String toString(VCSFileProxy file) {
        return RemoteVcsSupport.toString(file);
    }
    
    public static VCSFileProxy fromString(String file) {
        return RemoteVcsSupport.fromString(file);
    }

    /**
     * 
     * @param proxy defines FS and initial selection
     * @return 
     */
    public static JFileChooser createFileChooser(VCSFileProxy proxy) {
        return RemoteVcsSupport.createFileChooser(proxy);
    }

    public static VCSFileProxy getSelectedFile(JFileChooser chooser) {
        return RemoteVcsSupport.getSelectedFile(chooser);
    }
    
    public static FileSystem getDefaultFileSystem() {
        // TODO: remove dependencies!
        return RemoteVcsSupport.getDefaultFileSystem();
    }

    public static FileSystem[] getFileSystems() {
        // TODO: return list of remote file systems
        return RemoteVcsSupport.getFileSystems();
    }

    public static FileSystem getFileSystem(VCSFileProxy file) {
        return RemoteVcsSupport.getFileSystem(file);
    }
    
    public static FileSystem readFileSystem(DataInputStream is) throws IOException {
        String uri = is.readUTF();
        try {
            return FileSystemProvider.getFileSystem(new URI(uri));
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    public static void writeFileSystem(DataOutputStream os, FileSystem fs) throws IOException {
        //TODO: implement it!
        os.writeUTF(fs.getRoot().toURI().toString());
    }
}
