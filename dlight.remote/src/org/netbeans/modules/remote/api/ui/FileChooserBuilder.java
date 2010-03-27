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

import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 * @author ak119685
 */
public final class FileChooserBuilder {

    private static final String openDialogTitleTextKey = "FileChooser.openDialogTitleText"; // NOI18N
    private static final String saveDialogTitleTextKey = "FileChooser.saveDialogTitleText"; // NOI18N
    private static final String readOnlyKey = "FileChooser.readOnly"; // NOI18N
    private final ExecutionEnvironment env;

    public FileChooserBuilder(ExecutionEnvironment env) {
        this.env = env;
    }

    public JFileChooser createFileChooser() {
        return createFileChooser(null);
    }

    public JFileChooser createFileChooser(String selectedPath) {
        if (env.isLocal()) {
            return new JFileChooser(selectedPath);
        } else {
            if (selectedPath == null || selectedPath.trim().length() == 0) {
                selectedPath = "/"; //NOI18N
            }
            String currentOpenTitle = UIManager.getString(openDialogTitleTextKey);
            String currentSaveTitle = UIManager.getString(saveDialogTitleTextKey);
            Boolean currentReadOnly = UIManager.getBoolean(readOnlyKey);

            UIManager.put(openDialogTitleTextKey, currentOpenTitle + " @ " + env.getDisplayName()); // NOI18N
            UIManager.put(saveDialogTitleTextKey, currentSaveTitle + " @ " + env.getDisplayName()); // NOI18N

            RemoteFileSystemView remoteFileSystemView = new RemoteFileSystemView("/", env); // NOI18N

            JFileChooserImpl chooser = new JFileChooserImpl(selectedPath, remoteFileSystemView);//NOI18N
            remoteFileSystemView.addPropertyChangeListener(chooser);

            UIManager.put(openDialogTitleTextKey, currentOpenTitle);
            UIManager.put(saveDialogTitleTextKey, currentSaveTitle);
            UIManager.put(readOnlyKey, currentReadOnly);

            return chooser;
        }
    }

    private static class JFileChooserImpl extends JFileChooser
            implements PropertyChangeListener {

        public JFileChooserImpl(String currentDirectory, FileSystemView fsv) {
            super(currentDirectory, fsv);
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

        public void propertyChange(final PropertyChangeEvent evt) {
            if (RemoteFileSystemView.LOADING_STATUS.equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        FileObjectBasedFile file = (FileObjectBasedFile) evt.getNewValue();
                        if (file == null) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        } else {
                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        }
                    }
                });
            }
        }
    }
}
