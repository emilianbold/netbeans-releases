/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.api.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import sun.nio.cs.FastCharsetProvider;

/**
 *
 * @author ak119685
 */
public final class FileChooserBuilder {

    // TODO: think of a better name
    public abstract static class JFileChooserEx extends JFileChooser {

        protected JFileChooserEx(String currentDirectoryPath) {
            super(currentDirectoryPath);
        }

        public JFileChooserEx(String currentDirectoryPath, FileSystemView fsv) {
            super(currentDirectoryPath, fsv);
        }


        public abstract FileObject getSelectedFileObject();
        public abstract FileObject[] getSelectedFileObjects();
    }

    private static final String openDialogTitleTextKey = "FileChooser.openDialogTitleText"; // NOI18N
    private static final String saveDialogTitleTextKey = "FileChooser.saveDialogTitleText"; // NOI18N
    private static final String readOnlyKey = "FileChooser.readOnly"; // NOI18N

    private final ExecutionEnvironment env;
    private Preferences forModule;

    public FileChooserBuilder(ExecutionEnvironment env) {
        this.env = env;
    }

    public JFileChooserEx createFileChooser() {
        return createFileChooser(null);
    }

    public JFileChooserEx createFileChooser(String selectedPath) {
        if (env.isLocal()) {
            return new LocalFileChooserImpl(selectedPath);
        } else {
            if (selectedPath == null || selectedPath.trim().length() == 0) {
                selectedPath = "/"; //NOI18N
            }
            String currentOpenTitle = UIManager.getString(openDialogTitleTextKey);
            String currentSaveTitle = UIManager.getString(saveDialogTitleTextKey);
            Boolean currentReadOnly = UIManager.getBoolean(readOnlyKey);

            UIManager.put(openDialogTitleTextKey, decorateTitle(currentOpenTitle, env));
            UIManager.put(saveDialogTitleTextKey, decorateTitle(currentSaveTitle, env));

            RemoteFileSystemView remoteFileSystemView = new RemoteFileSystemView("/", env); // NOI18N

            RemoteFileChooserImpl chooser = new RemoteFileChooserImpl(selectedPath, remoteFileSystemView, env, forModule);//NOI18N
            remoteFileSystemView.addPropertyChangeListener(chooser);
            chooser.setFileView(new CustomFileView(remoteFileSystemView));

            UIManager.put(openDialogTitleTextKey, currentOpenTitle);
            UIManager.put(saveDialogTitleTextKey, currentSaveTitle);
            UIManager.put(readOnlyKey, currentReadOnly);

            return chooser;
        }
    }

    public FileChooserBuilder setPreferences(Preferences forModule) {
        this.forModule = forModule;
        return this;
    }

    private static String decorateTitle(String title, ExecutionEnvironment env) {
        return NbBundle.getMessage(FileChooserBuilder.class, "REMOTE_CHOOSER_TITLE", title, env.getDisplayName()); // NOI18N
    }

    private static class LocalFileChooserImpl extends JFileChooserEx {

        public LocalFileChooserImpl(String selectedPath) {
            super(selectedPath);
        }

        @Override
        public FileObject getSelectedFileObject() {
            File file = getSelectedFile();
            return (file == null) ? null : FileUtil.toFileObject(file);
        }

        @Override
        public FileObject[] getSelectedFileObjects() {
            File[] files = getSelectedFiles();
            if (files == null) {
                return null;
            } else {
                FileObject[] result = new FileObject[files.length];
                for (int i = 0; i < files.length; i++) {
                    result[i] = FileUtil.toFileObject(files[i]);
                }
                return result;
            }
        }

    }

    private static class RemoteFileChooserImpl extends JFileChooserEx
            implements PropertyChangeListener {
        private final Preferences forModule;
       private final ExecutionEnvironment env;

        public RemoteFileChooserImpl(String currentDirectory, RemoteFileSystemView fsv, ExecutionEnvironment env, Preferences forModule) {
            super(currentDirectory, fsv);
            this.env = env;
            this.forModule = forModule;
        }

        @Override
        public FileObject getSelectedFileObject() {
            File file = getSelectedFile();
            return (file instanceof FileObjectBasedFile) ? ((FileObjectBasedFile) file).getFileObject() : null;
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            setCursor(Cursor.getDefaultCursor());
        }

        @Override
        public FileObject[] getSelectedFileObjects() {
            File[] files = getSelectedFiles();
            if (files == null) {
                return null;
            } else {
                List<FileObject> result = new ArrayList<FileObject>(files.length);
                for (int i = 0; i < files.length; i++) {
                    if (files[i] instanceof FileObjectBasedFile) {
                        FileObject fo = ((FileObjectBasedFile) files[i]).getFileObject();
                        if (fo != null) {
                            result.add(fo);
                        } else {
                            RemoteLogger.getInstance().log(Level.FINEST, "Null file object for {0}", files[i].getAbsolutePath());
                        }
                    }
                }
                return result.toArray(new FileObject[result.size()]);
            }
        }

        @Override
        protected void setup(FileSystemView view) {
            super.setup(view);
        }

        @Override
        public void approveSelection() {
            File selectedFile = getSelectedFile();
            if (selectedFile != null) {
                if (selectedFile.isDirectory() && getFileSelectionMode() == FILES_ONLY) {
                    setCurrentDirectory(getSelectedFile());
                } else {
                    super.approveSelection();
                }
            }
        }

        @Override
        public void setCurrentDirectory(File dir) {
            if (dir != null && !(dir instanceof FileObjectBasedFile) && env != null) {
                String path = dir.getPath().replace('\\', FileSystemProvider.getFileSeparatorChar(env)); //NOI18N
                FileObject fo = FileSystemProvider.getFileObject(env, path);
                if (fo != null) {
                    dir = new FileObjectBasedFile(env, fo);
                }
            }
            super.setCurrentDirectory(dir);
        }

        @Override
        protected void fireActionPerformed(String command) {
            super.fireActionPerformed(command);
        }
        
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (RemoteFileSystemView.LOADING_STATUS.equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        String file = (String) evt.getNewValue();
                        if (file == null) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        } else {
                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        }
                    }
                });
            }
        }

        @Override
        public RemoteFileSystemView getFileSystemView() {
            return (RemoteFileSystemView) super.getFileSystemView();
        }

        @Override
        public void setDialogTitle(String dialogTitle) {
            super.setDialogTitle(decorateTitle(dialogTitle, getFileSystemView().getExecutionEnvironment()));
        }

        @Override
        public int showOpenDialog(Component parent) throws HeadlessException {
            int ret = super.showOpenDialog(parent);
            if (ret != CANCEL_OPTION) {
                if (getSelectedFile() != null) {
                    String path = getSelectedFile().getAbsolutePath();
                    if (forModule != null) {
                        String envID = ExecutionEnvironmentFactory.toUniqueID(env);
                        forModule.put("FileChooserPath"+envID, path); // NOI18N
                    }
                }
            }
            return ret;
        }
    }
    
    private static class CustomFileView extends FileView {
        final FileSystemView view;

        public CustomFileView(FileSystemView view) {
            this.view = view;
        }

        @Override
        public Icon getIcon(File f) {
            return view.getSystemIcon(f);
        }

        @Override
        public String getName(File f) {
            if (view.isRoot(f)) {
                return "/"; //NOI18N
            }
            return super.getName(f);
        }

        @Override
        public Boolean isTraversable(File f) {
            return f.isDirectory();
        }
    }

}
