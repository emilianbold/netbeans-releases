/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.api.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.impl.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author ak119685
 */
/*package*/ final class RemoteFileSystemView extends FileSystemView {

    public static final String LOADING_STATUS = "ls"; //  NOI18N
    private final FileSystem fs;
    private final PropertyChangeSupport changeSupport;

    public RemoteFileSystemView(final String root, final ExecutionEnvironment execEnv) {
        fs = FileSystemProvider.getFileSystem(execEnv, root);
        assert (fs != null);
        changeSupport = new PropertyChangeSupport(this);
    }

    public FileObject getFSRoot() {
        return fs.getRoot();
    }

    @Override
    public File createFileObject(String path) {
        FileObject fo = fs.findResource(path);
        if (fo == null) {
            return new FileObjectBasedFile(path);
        } else {
            return new FileObjectBasedFile(fo);
        }
    }

    @Override
    public File createFileObject(File dir, String filename) {
        String parent = dir == null ? fs.getRoot().getPath() : dir.getPath();
        return createFileObject(parent + "/" + filename); // NOI18N
    }

    @Override
    public File[] getRoots() {
        return new File[]{new FileObjectBasedFile(fs.getRoot())};
    }

    @Override
    public String getSystemDisplayName(File f) {
        return "".equals(f.getName()) ? "/" : f.getName(); // NOI18N
    }

    @Override
    public File getDefaultDirectory() {
        return new FileObjectBasedFile(fs.getRoot());
    }

    @Override
    public File getHomeDirectory() {
        return new FileObjectBasedFile(fs.getRoot());
    }

    @Override
    public boolean isFileSystem(File f) {
        return true;
    }

    @Override
    public File getParentDirectory(File dir) {
        File parentFile = dir.getParentFile();
        return parentFile == null ? null : createFileObject(parentFile.getPath());
    }

    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        if (!(dir instanceof FileObjectBasedFile)) {
            dir = new FileObjectBasedFile(dir.getAbsolutePath());
        }
        FileObjectBasedFile rdir = (FileObjectBasedFile) dir;
        File[] result = null;

        changeSupport.firePropertyChange(LOADING_STATUS, null, rdir);

        try {
            result = rdir.listFiles();
        } finally {
            changeSupport.firePropertyChange(LOADING_STATUS, rdir, null);
        }

        return result;
    }

    @Override
    public File createNewFolder(File containingDir) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    protected File createFileSystemRoot(File f) {
        return new FileObjectBasedFile(fs.getRoot());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public Icon getSystemIcon(File f) {
        return UIManager.getIcon(f == null || f.isDirectory() ? "FileView.directoryIcon" : "FileView.fileIcon");
    }


}
