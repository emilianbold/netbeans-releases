/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.project.connections.common.RemoteValidator;
import org.netbeans.modules.php.project.ui.LastUsedFolders;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.DebugUrl;
import org.openide.NotificationLineSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Tomas Mysik
 */
public class RunAsWebAdvanced extends JPanel {
    private static final long serialVersionUID = 7842376554376847L;
    static final String DEFAULT_LOCAL_PATH = ""; // NOI18N
    static final int COLUMN_REMOTE_PATH = 0;
    static final int COLUMN_LOCAL_PATH = 1;

    final PhpProject project;

    private final PathMappingTableModel pathMappingTableModel;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport notificationLineSupport;

    RunAsWebAdvanced(PhpProject project, Properties properties) {
        assert project != null;
        assert properties != null;

        this.project = project;

        initComponents();
        setDebugUrl(properties);
        setDebugProxy(properties);

        String[] columnNames = {
            NbBundle.getMessage(RunAsWebAdvanced.class, "LBL_ServerPath"),
            NbBundle.getMessage(RunAsWebAdvanced.class, "LBL_LocalPath"),
        };
        pathMappingTableModel = new PathMappingTableModel(columnNames, getPathMappings(properties.remotePaths, properties.localPaths));
        pathMappingTable.setModel(pathMappingTableModel);
        pathMappingTable.setDefaultRenderer(LocalPathCell.class, new LocalPathCellRenderer());
        pathMappingTable.addMouseListener(new LocalPathCellMouseListener(pathMappingTable));

        ActionListener debugUrlListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateFields();
            }
        };
        defaultUrlRadioButton.addActionListener(debugUrlListener);
        askUrlRadioButton.addActionListener(debugUrlListener);
        doNotOpenBrowserRadioButton.addActionListener(debugUrlListener);
        pathMappingTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                handleButtonStates();
                validateFields();
            }
        });
        pathMappingTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                handleButtonStates();
            }
        });
        DocumentListener defaultDocumentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }
            private void processUpdate() {
                validateFields();
            }
        };
        proxyHostTextField.getDocument().addDocumentListener(defaultDocumentListener);
        proxyPortTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    public boolean open() {
        descriptor = new DialogDescriptor(
                this,
                NbBundle.getMessage(RunAsWebAdvanced.class, "LBL_AdvancedWebConfiguration"),
                true,
                null);
        notificationLineSupport = descriptor.createNotificationLineSupport();
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            validateFields();
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        return descriptor.getValue() == NotifyDescriptor.OK_OPTION;
    }

    public DebugUrl getDebugUrl() {
        DebugUrl debugUrl = null;
        if (defaultUrlRadioButton.isSelected()) {
            debugUrl = DebugUrl.DEFAULT_URL;
        } else if (askUrlRadioButton.isSelected()) {
            debugUrl = DebugUrl.ASK_FOR_URL;
        } else if (doNotOpenBrowserRadioButton.isSelected()) {
            debugUrl = DebugUrl.DO_NOT_OPEN_BROWSER;
        }
        assert debugUrl != null;
        return debugUrl;
    }

    public Pair<String, String> getPathMapping() {
        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(project);
        int rowCount = pathMappingTableModel.getRowCount();
        List<String> remotes = new ArrayList<String>(rowCount);
        List<String> locals = new ArrayList<String>(rowCount);
        for (int i = 0; i < rowCount; ++i) {
            String remotePath = (String) pathMappingTableModel.getValueAt(i, COLUMN_REMOTE_PATH);
            if (StringUtils.hasText(remotePath)) {
                String localPath = null;
                localPath = ((LocalPathCell) pathMappingTableModel.getValueAt(i, COLUMN_LOCAL_PATH)).getPath();
                File local = new File(localPath);
                assert local.isDirectory() : localPath + " must be a directory!";
                FileObject localFileObject = FileUtil.toFileObject(local);
                String relativePath = FileUtil.getRelativePath(sources, localFileObject);
                if (relativePath != null) {
                    localPath = relativePath;
                }

                remotes.add(remotePath);
                locals.add(localPath);
            }
        }
        return Pair.of(
                StringUtils.implode(remotes, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR),
                StringUtils.implode(locals, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR));
    }

    public Pair<String, String> getDebugProxy() {
        String proxyHost = proxyHostTextField.getText();
        String proxyPort = null;
        if (StringUtils.hasText(proxyHost)) {
            proxyPort = proxyPortTextField.getText();
        }
        return Pair.of(proxyHost, proxyPort);
    }

    void validateFields() {
        assert notificationLineSupport != null;

        for (int i = 0; i < pathMappingTableModel.getRowCount(); ++i) {
            String remotePath = (String) pathMappingTableModel.getValueAt(i, COLUMN_REMOTE_PATH);
            String localPath = ((LocalPathCell) pathMappingTableModel.getValueAt(i, COLUMN_LOCAL_PATH)).getPath();
            if (!StringUtils.hasText(remotePath)
                    && !StringUtils.hasText(localPath)) {
                // empty line
                continue;
            } else if (!StringUtils.hasText(remotePath)
                    && StringUtils.hasText(localPath)) {
                notificationLineSupport.setErrorMessage(NbBundle.getMessage(RunAsWebAdvanced.class, "MSG_RemotePathEmpty"));
                descriptor.setValid(false);
                return;
            } else if (StringUtils.hasText(remotePath)
                    && !StringUtils.hasText(localPath)) {
                notificationLineSupport.setErrorMessage(NbBundle.getMessage(RunAsWebAdvanced.class, "MSG_LocalPathEmpty"));
                descriptor.setValid(false);
                return;
            } else if (!isLocalPathValid(localPath)) {
                notificationLineSupport.setErrorMessage(NbBundle.getMessage(RunAsWebAdvanced.class, "MSG_LocalPathNotValid", localPath));
                descriptor.setValid(false);
                return;
            }
        }

        String proxyHost = proxyHostTextField.getText();
        if (StringUtils.hasText(proxyHost)) {
            String err = RemoteValidator.validatePort(proxyPortTextField.getText());
            if (err != null) {
                notificationLineSupport.setErrorMessage(err);
                descriptor.setValid(false);
                return;
            }
        }

        String warning = null;
        if ((doNotOpenBrowserRadioButton.isSelected() || askUrlRadioButton.isSelected())
                && !isAnyRemotePathDefined()) {
            warning = NbBundle.getMessage(RunAsWebAdvanced.class, "MSG_WarningNoPathMapping");
        }

        if (warning != null) {
            notificationLineSupport.setWarningMessage(warning);
        } else {
            notificationLineSupport.clearMessages();
        }
        descriptor.setValid(true);
    }

    private boolean isAnyRemotePathDefined() {
        for (int i = 0; i < pathMappingTableModel.getRowCount(); ++i) {
            String remotePath = (String) pathMappingTableModel.getValueAt(i, COLUMN_REMOTE_PATH);
            if (StringUtils.hasText(remotePath)) {
                return true;
            }
        }
        return false;
    }

    void handleButtonStates() {
        removePathMappingButton.setEnabled(isTableRowSelected());
        newPathMappingButton.setEnabled(pathMappingTableModel.isLastServerPathFilled());
    }

    private Object[][] getPathMappings(String remotePaths, String localPaths) {
        List<String> remotes = StringUtils.explode(remotePaths, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR);
        List<String> locals = StringUtils.explode(localPaths, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR);
        int remotesSize = remotes.size();
        int localsSize = locals.size();
        Object[][] paths = new Object[remotesSize + 1][2];
        for (int i = 0; i < remotesSize; ++i) {
            // if user has only 1 path and local == sources => property is not stored at all!
            String local = DEFAULT_LOCAL_PATH;
            if (i < localsSize) {
                local = locals.get(i);
            }
            Pair<String, String> pathMapping = getPathMapping(remotes.get(i), local);
            paths[i][COLUMN_REMOTE_PATH] = pathMapping.first;
            paths[i][COLUMN_LOCAL_PATH] = new LocalPathCell(pathMapping.second);
        }
        paths[remotesSize][COLUMN_REMOTE_PATH] = null;
        paths[remotesSize][COLUMN_LOCAL_PATH] = new LocalPathCell(DEFAULT_LOCAL_PATH);
        return paths;
    }

    private Pair<String, String> getPathMapping(String remotePath, String localPath) {
        if (StringUtils.hasText(remotePath)) {
            FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(project);
            if (isSources(localPath)) {
                localPath = FileUtil.toFile(sources).getAbsolutePath();
            } else {
                FileObject resolved = sources.getFileObject(localPath);
                if (resolved != null) {
                    localPath = FileUtil.toFile(resolved).getAbsolutePath();
                }
            }
        } else {
            localPath = DEFAULT_LOCAL_PATH;
        }
        return Pair.of(remotePath, localPath);
    }

    private int getTableSelectedRow() {
        return pathMappingTable.getSelectedRow();
    }

    private boolean isTableRowSelected() {
        return getTableSelectedRow() != -1;
    }

    private void setDebugUrl(Properties properties) {
        String debugUrl = properties.debugUrl;
        if (debugUrl == null) {
            debugUrl = DebugUrl.DEFAULT_URL.name();
        }
        switch (DebugUrl.valueOf(debugUrl)) {
            case DEFAULT_URL:
                defaultUrlRadioButton.setSelected(true);
                break;
            case ASK_FOR_URL:
                askUrlRadioButton.setSelected(true);
                break;
            case DO_NOT_OPEN_BROWSER:
                doNotOpenBrowserRadioButton.setSelected(true);
                break;
            default:
                throw new IllegalArgumentException("Unknown debug url type: " + debugUrl);
        }
        defaultUrlPreviewLabel.setText(properties.urlPreview);
    }

    private void setDebugProxy(Properties properties) {
        proxyHostTextField.setText(properties.proxyHost);
        String port = properties.proxyPort;
        if (RemoteValidator.validatePort(port) != null) {
            port = String.valueOf(PhpProjectProperties.DEFAULT_DEBUG_PROXY_PORT);
        }
        proxyPortTextField.setText(port);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        debugUrlButtonGroup = new ButtonGroup();
        debugUrlLabel = new JLabel();
        defaultUrlRadioButton = new JRadioButton();
        defaultUrlPreviewLabel = new JLabel();
        askUrlRadioButton = new JRadioButton();
        doNotOpenBrowserRadioButton = new JRadioButton();
        pathMappingLabel = new JLabel();
        pathMappingScrollPane = new JScrollPane();
        pathMappingTable = new JTable();
        newPathMappingButton = new JButton();
        removePathMappingButton = new JButton();
        pathMappingInfoLabel = new JLabel();
        proxyLabel = new JLabel();
        proxyHostLabel = new JLabel();
        proxyHostTextField = new JTextField();
        proxyPortLabel = new JLabel();
        proxyPortTextField = new JTextField();

        setFocusTraversalPolicy(null);

        debugUrlLabel.setLabelFor(defaultUrlRadioButton);
        Mnemonics.setLocalizedText(debugUrlLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.debugUrlLabel.text")); // NOI18N

        debugUrlButtonGroup.add(defaultUrlRadioButton);
        defaultUrlRadioButton.setSelected(true);
        Mnemonics.setLocalizedText(defaultUrlRadioButton, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.defaultUrlRadioButton.text")); // NOI18N

        defaultUrlPreviewLabel.setLabelFor(defaultUrlRadioButton);
        Mnemonics.setLocalizedText(defaultUrlPreviewLabel, "dummy"); // NOI18N

        debugUrlButtonGroup.add(askUrlRadioButton);
        Mnemonics.setLocalizedText(askUrlRadioButton, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.askUrlRadioButton.text")); // NOI18N

        debugUrlButtonGroup.add(doNotOpenBrowserRadioButton);
        Mnemonics.setLocalizedText(doNotOpenBrowserRadioButton, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.doNotOpenBrowserRadioButton.text")); // NOI18N

        pathMappingLabel.setLabelFor(pathMappingTable);

        Mnemonics.setLocalizedText(pathMappingLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.pathMappingLabel.text"));
        pathMappingTable.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        pathMappingScrollPane.setViewportView(pathMappingTable);

        pathMappingTable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.pathMappingTable.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(newPathMappingButton, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.newPathMappingButton.text")); // NOI18N
        newPathMappingButton.setEnabled(false);
        newPathMappingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newPathMappingButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(removePathMappingButton, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.removePathMappingButton.text")); // NOI18N
        removePathMappingButton.setEnabled(false);

        removePathMappingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removePathMappingButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(pathMappingInfoLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.pathMappingInfoLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(proxyLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.proxyLabel.text"));

        proxyHostLabel.setLabelFor(proxyHostTextField);
        Mnemonics.setLocalizedText(proxyHostLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.proxyHostLabel.text")); // NOI18N

        proxyPortLabel.setLabelFor(proxyPortTextField);

        Mnemonics.setLocalizedText(proxyPortLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.proxyPortLabel.text"));
        proxyPortTextField.setPreferredSize(new Dimension(46, 19));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(pathMappingScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(removePathMappingButton)
                            .addComponent(newPathMappingButton)))
                    .addComponent(debugUrlLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(defaultUrlRadioButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(defaultUrlPreviewLabel))
                    .addComponent(askUrlRadioButton)
                    .addComponent(doNotOpenBrowserRadioButton)
                    .addComponent(pathMappingLabel)
                    .addComponent(pathMappingInfoLabel)
                    .addComponent(proxyLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(proxyHostLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(proxyHostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(proxyPortLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(proxyPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {newPathMappingButton, removePathMappingButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(debugUrlLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(defaultUrlRadioButton)
                    .addComponent(defaultUrlPreviewLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(askUrlRadioButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(doNotOpenBrowserRadioButton)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(pathMappingLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newPathMappingButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(removePathMappingButton))
                    .addComponent(pathMappingScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(pathMappingInfoLabel)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(proxyLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(proxyHostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(proxyHostLabel)
                    .addComponent(proxyPortLabel)
                    .addComponent(proxyPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        debugUrlLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.debugUrlLabel.AccessibleContext.accessibleName")); // NOI18N
        debugUrlLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.debugUrlLabel.AccessibleContext.accessibleDescription")); // NOI18N
        defaultUrlRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.defaultUrlRadioButton.AccessibleContext.accessibleName")); // NOI18N
        defaultUrlRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.defaultUrlRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        defaultUrlPreviewLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.defaultUrlPreviewLabel.AccessibleContext.accessibleName")); // NOI18N
        defaultUrlPreviewLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.defaultUrlPreviewLabel.AccessibleContext.accessibleDescription")); // NOI18N
        askUrlRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.askUrlRadioButton.AccessibleContext.accessibleName")); // NOI18N
        askUrlRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.askUrlRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        doNotOpenBrowserRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.doNotOpenBrowserRadioButton.AccessibleContext.accessibleName")); // NOI18N
        doNotOpenBrowserRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.doNotOpenBrowserRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        pathMappingLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.pathMappingLabel.AccessibleContext.accessibleName")); // NOI18N
        pathMappingLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.pathMappingLabel.AccessibleContext.accessibleDescription")); // NOI18N
        pathMappingScrollPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.pathMappingScrollPane.AccessibleContext.accessibleName")); // NOI18N
        pathMappingScrollPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.pathMappingScrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        newPathMappingButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.newPathMappingButton.AccessibleContext.accessibleName")); // NOI18N
        newPathMappingButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.newPathMappingButton.AccessibleContext.accessibleDescription")); // NOI18N
        removePathMappingButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.removePathMappingButton.AccessibleContext.accessibleName")); // NOI18N
        removePathMappingButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.removePathMappingButton.AccessibleContext.accessibleDescription")); // NOI18N
        pathMappingInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.pathMappingInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        pathMappingInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.pathMappingInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        proxyLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.proxyLabel.AccessibleContext.accessibleName")); // NOI18N
        proxyLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.proxyLabel.AccessibleContext.accessibleDescription")); // NOI18N
        proxyHostLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.proxyHostLabel.AccessibleContext.accessibleName")); // NOI18N
        proxyHostLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.proxyHostLabel.AccessibleContext.accessibleDescription")); // NOI18N
        proxyHostTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.proxyHostTextField.AccessibleContext.accessibleName")); // NOI18N
        proxyHostTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.proxyHostTextField.AccessibleContext.accessibleDescription")); // NOI18N
        proxyPortLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.proxyPortLabel.AccessibleContext.accessibleName")); // NOI18N
        proxyPortLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.proxyPortLabel.AccessibleContext.accessibleDescription")); // NOI18N
        proxyPortTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.proxyPortTextField.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void newPathMappingButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_newPathMappingButtonActionPerformed
        pathMappingTableModel.addRow(new Object[] {null, new LocalPathCell(DEFAULT_LOCAL_PATH)});
    }//GEN-LAST:event_newPathMappingButtonActionPerformed

    private void removePathMappingButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_removePathMappingButtonActionPerformed
        assert getTableSelectedRow() != -1 : "A table row must be selected";
        while (getTableSelectedRow() != -1) {
            pathMappingTableModel.removeRow(getTableSelectedRow());
        }
        if (pathMappingTableModel.getRowCount() == 0) {
            newPathMappingButtonActionPerformed(null);
        }
    }//GEN-LAST:event_removePathMappingButtonActionPerformed

    static boolean isSources(String path) {
        return path == null || DEFAULT_LOCAL_PATH.equals(path);
    }

    private boolean isLocalPathValid(String localPath) {
        assert StringUtils.hasText(localPath);
        File directory = new File(localPath);
        return directory.isDirectory() && directory.isAbsolute();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JRadioButton askUrlRadioButton;
    private ButtonGroup debugUrlButtonGroup;
    private JLabel debugUrlLabel;
    private JLabel defaultUrlPreviewLabel;
    private JRadioButton defaultUrlRadioButton;
    private JRadioButton doNotOpenBrowserRadioButton;
    private JButton newPathMappingButton;
    private JLabel pathMappingInfoLabel;
    private JLabel pathMappingLabel;
    private JScrollPane pathMappingScrollPane;
    private JTable pathMappingTable;
    private JLabel proxyHostLabel;
    private JTextField proxyHostTextField;
    private JLabel proxyLabel;
    private JLabel proxyPortLabel;
    private JTextField proxyPortTextField;
    private JButton removePathMappingButton;
    // End of variables declaration//GEN-END:variables

    private final class PathMappingTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 169356031075115831L;

        public PathMappingTableModel(String[] columnNames, Object[][] data) {
            super(data, columnNames);
        }

        public boolean isLastServerPathFilled() {
            int rowCount = getRowCount();
            if (rowCount == 0) {
                return true;
            }
            return StringUtils.hasText((String) getValueAt(rowCount - 1, COLUMN_REMOTE_PATH));
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == COLUMN_LOCAL_PATH) {
                return LocalPathCell.class;
            } else if (columnIndex == COLUMN_REMOTE_PATH) {
                return String.class;
            }
            throw new IllegalStateException("Unhandled column index: " + columnIndex);
        }
    }

    private final class LocalPathCellRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            LocalPathCell localPathCell = (LocalPathCell) value;
            // #164688 - sorry, no idea how this can happen
            if (localPathCell == null) {
                localPathCell = new LocalPathCell(DEFAULT_LOCAL_PATH);
            }
            if (isSelected) {
                localPathCell.setBgColor(table.getSelectionBackground());
                localPathCell.setFgColor(table.getSelectionForeground());
            } else {
                localPathCell.setBgColor(table.getBackground());
                localPathCell.setFgColor(table.getForeground());
            }

            return localPathCell;
        }
    }

    private final class LocalPathCellMouseListener extends MouseAdapter {

        private final JTable table;

        public LocalPathCellMouseListener(JTable table) {
            this.table = table;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Point p = new Point(e.getX(), e.getY());
            int row = table.rowAtPoint(p);
            int col = table.columnAtPoint(p);
            Object value = table.getValueAt(row, col);
            if (value instanceof LocalPathCell) {
                Rectangle cellRect = table.getCellRect(row, col, false);
                LocalPathCell localPathCell = (LocalPathCell) value;
                JButton button = localPathCell.getButton();
                if (e.getX() > (cellRect.x + cellRect.width - button.getWidth())) {
                    //inside changeButton
                    File newLocation = Utils.browseLocationAction(table, getLastFolder(), NbBundle.getMessage(RunAsWebAdvanced.class, "LBL_SelectProjectFolder"));
                    if (newLocation != null) {
                        localPathCell.setPath(newLocation.getAbsolutePath());
                        LastUsedFolders.setPathMapping(newLocation);
                    }
                    validateFields();
                }
            }
        }

        private File getLastFolder() {
            File lastFolder = LastUsedFolders.getPathMapping();
            if (lastFolder == null) {
                return null;
            }
            FileObject fo = FileUtil.toFileObject(lastFolder);
            if (!CommandUtils.isUnderAnySourceGroup(project, fo, false)) {
                return FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(project));
            }
            return lastFolder;
        }
    }

    public static final class Properties {
        public final String debugUrl;
        public final String urlPreview;
        public final String remotePaths;
        public final String localPaths;
        public final String proxyHost;
        public final String proxyPort;

        public Properties(String debugUrl, String urlPreview, String remotePaths, String localPaths, String proxyHost, String proxyPort) {
            this.debugUrl = debugUrl;
            this.urlPreview = urlPreview;
            this.remotePaths = remotePaths;
            this.localPaths = localPaths;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
        }
    }
}
