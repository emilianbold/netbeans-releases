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

import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 * @author ak119685
 */
final class RemoteFileChooserBuilder {

    private static final String openDialogTitleTextKey = "FileChooser.openDialogTitleText"; // NOI18N
    private static final String saveDialogTitleTextKey = "FileChooser.saveDialogTitleText"; // NOI18N
    private static final String readOnlyKey = "FileChooser.readOnly"; // NOI18N
    private final ExecutionEnvironment env;
//    private final boolean addProgressBar = true;

    public RemoteFileChooserBuilder(ExecutionEnvironment env) {
        this.env = env;
    }

    public JFileChooser createFileChooser() {
        String currentOpenTitle = UIManager.getString(openDialogTitleTextKey);
        String currentSaveTitle = UIManager.getString(saveDialogTitleTextKey);
        Boolean currentReadOnly = UIManager.getBoolean(readOnlyKey);

        UIManager.put(openDialogTitleTextKey, currentOpenTitle + " @ " + env.getDisplayName()); // NOI18N
        UIManager.put(saveDialogTitleTextKey, currentSaveTitle + " @ " + env.getDisplayName()); // NOI18N

        RemoteFileSystemView remoteFileSystemView = new RemoteFileSystemView("/", env); // NOI18N

        JFileChooserImpl chooser = new JFileChooserImpl("/", remoteFileSystemView);
        remoteFileSystemView.addPropertyChangeListener(chooser);

        UIManager.put(openDialogTitleTextKey, currentOpenTitle);
        UIManager.put(saveDialogTitleTextKey, currentSaveTitle);
        UIManager.put(readOnlyKey, currentReadOnly);

        return chooser;
    }

    private class JFileChooserImpl extends JFileChooser
            implements PropertyChangeListener {

        private JPanel outerPanel;
        private final JPanel progressPanel;
        private final JLabel progressBar;
        private final int progressBarW;
        private final int progressBarH;
//        private final JLabel progressText;

        public JFileChooserImpl(String currentDirectory, FileSystemView fsv) {
            super(currentDirectory, fsv);
            super.setLayout(new BorderLayout());
            progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//            progressText = new JLabel();
//            progressText.setVisible(false);
            progressPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
//            Icon pbImage = ImageUtilities.loadImageIcon("com/sun/dlight/spi/impl/resources/progress.gif", false); // NOI18N
            Icon pbImage = new ImageIcon(getClass().getResource("/org/netbeans/modules/remote/api/ui/progress.gif")); // NOI18N
            progressBarW = pbImage.getIconWidth();
            progressBarH = pbImage.getIconHeight();
//            progressPanel.setPreferredSize(new Dimension(100, pbImage.getIconHeight() + 10));
//            progressPanel.setBounds(0, 0, 200, pbImage.getIconHeight() + 10);
            progressBar = new JLabel(pbImage);
            progressBar.setVisible(false);
            progressPanel.add(progressBar);
//            progressPanel.add(progressText);
            super.add(progressPanel, BorderLayout.CENTER);
            super.add(outerPanel, BorderLayout.CENTER);
            addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(ComponentEvent e) {
                    Dimension newSize = outerPanel.getSize();
                    progressPanel.setBounds(10,
                            newSize.height - progressBarH,
                            progressBarW + 10, progressBarH + 10);
                    Component comp = SwingUtilities.getDeepestComponentAt(outerPanel, 24 + progressBarW, newSize.height - progressBarH / 2);
                    // TODO: a silly attempt...
                    if (comp != null && !(comp instanceof JPanel)) {
                        progressPanel.setVisible(false);
                    } else {
                        progressPanel.setVisible(true);
                    }
                }
            });
        }

        @Override
        protected void setup(FileSystemView view) {
            outerPanel = new JPanel();
            super.setup(view);
        }

        @Override
        public Component add(Component comp) {
            try {
                return outerPanel.add(comp);
            } catch (AWTError e) {
                e.printStackTrace();
                throw e;
            }
        }

        @Override
        public void add(Component comp, Object constraints) {
            outerPanel.add(comp, constraints);
        }

        @Override
        public void setLayout(LayoutManager mgr) {
            outerPanel.setLayout(mgr);
        }

        @Override
        public LayoutManager getLayout() {
            return outerPanel.getLayout();
        }

        @Override
        public void approveSelection() {
            File selectedFile = getSelectedFile();
            if (selectedFile != null) {
                if (selectedFile.isDirectory()) {
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
//                            progressText.setText("");
                            progressBar.setVisible(false);
                        } else {
//                            progressText.setText("Loading " + file.getName() + " ... ");
                            progressBar.setVisible(true);
                        }
                    }
                });
            }
        }
    }
}
