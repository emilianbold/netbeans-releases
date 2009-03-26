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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.Pair;
import org.netbeans.modules.php.project.ui.LastUsedFolders;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.DebugUrl;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
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

    private final PhpProject project;
    private final PathMappingTableModel pathMappingTableModel;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport notificationLineSupport;

    RunAsWebAdvanced(PhpProject project, String debugUrl, String urlPreview, String remotePaths, String localPaths) {
        assert project != null;
        assert urlPreview != null;

        this.project = project;

        initComponents();
        setDebugUrl(debugUrl, urlPreview);

        String[] columnNames = {
            NbBundle.getMessage(RunAsWebAdvanced.class, "LBL_ServerPath"),
            NbBundle.getMessage(RunAsWebAdvanced.class, "LBL_LocalPath"),
        };
        // tmysik init data
        pathMappingTableModel = new PathMappingTableModel(columnNames, getPathMappings(remotePaths, localPaths));
        pathMappingTable.setModel(pathMappingTableModel);
        pathMappingTable.setDefaultRenderer(LocalPathCell.class, new LocalPathCellRenderer());
        pathMappingTable.addMouseListener(new LocalPathCellMouseListener(pathMappingTable));

        ActionListener debugUrlListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                validateFields();
            }
        };
        defaultUrlRadioButton.addActionListener(debugUrlListener);
        askUrlRadioButton.addActionListener(debugUrlListener);
        doNotOpenBrowserRadioButton.addActionListener(debugUrlListener);
        pathMappingTableModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                handleButtonStates();
                validateFields();
            }
        });
        pathMappingTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                handleButtonStates();
            }
        });
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
            if (PhpProjectUtils.hasText(remotePath)) {
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
                PhpProjectUtils.implode(remotes, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR),
                PhpProjectUtils.implode(locals, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR));
    }

    void validateFields() {
        assert notificationLineSupport != null;

        for (int i = 0; i < pathMappingTableModel.getRowCount(); ++i) {
            String remotePath = (String) pathMappingTableModel.getValueAt(i, COLUMN_REMOTE_PATH);
            String localPath = ((LocalPathCell) pathMappingTableModel.getValueAt(i, COLUMN_LOCAL_PATH)).getPath();
            if (!PhpProjectUtils.hasText(remotePath)
                    && !PhpProjectUtils.hasText(localPath)) {
                // empty line
                continue;
            } else if (!PhpProjectUtils.hasText(remotePath)
                    && PhpProjectUtils.hasText(localPath)) {
                notificationLineSupport.setErrorMessage(NbBundle.getMessage(RunAsWebAdvanced.class, "MSG_RemotePathEmpty"));
                descriptor.setValid(false);
                return;
            } else if (PhpProjectUtils.hasText(remotePath)
                    && !PhpProjectUtils.hasText(localPath)) {
                notificationLineSupport.setErrorMessage(NbBundle.getMessage(RunAsWebAdvanced.class, "MSG_LocalPathEmpty"));
                descriptor.setValid(false);
                return;
            } else if (!isLocalPathValid(localPath)) {
                notificationLineSupport.setErrorMessage(NbBundle.getMessage(RunAsWebAdvanced.class, "MSG_LocalPathNotValid", localPath));
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
            if (PhpProjectUtils.hasText(remotePath)) {
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
        List<String> remotes = PhpProjectUtils.explode(remotePaths, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR);
        List<String> locals = PhpProjectUtils.explode(localPaths, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR);
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
        if (PhpProjectUtils.hasText(remotePath)) {
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

    private void setDebugUrl(String debugUrl, String urlPreview) {
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
        defaultUrlPreviewLabel.setText(urlPreview);
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
        infoLabel = new JLabel();

        debugUrlLabel.setLabelFor(defaultUrlRadioButton);

        Mnemonics.setLocalizedText(debugUrlLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.debugUrlLabel.text")); // NOI18N
        debugUrlButtonGroup.add(defaultUrlRadioButton);
        defaultUrlRadioButton.setSelected(true);

        Mnemonics.setLocalizedText(defaultUrlRadioButton, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.defaultUrlRadioButton.text")); // NOI18N
        defaultUrlPreviewLabel.setLabelFor(defaultUrlRadioButton);
        Mnemonics.setLocalizedText(defaultUrlPreviewLabel, "dummy"); // NOI18N
        defaultUrlPreviewLabel.setEnabled(false);

        debugUrlButtonGroup.add(askUrlRadioButton);

        Mnemonics.setLocalizedText(askUrlRadioButton, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.askUrlRadioButton.text")); // NOI18N
        debugUrlButtonGroup.add(doNotOpenBrowserRadioButton);


        Mnemonics.setLocalizedText(doNotOpenBrowserRadioButton, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.doNotOpenBrowserRadioButton.text"));
        Mnemonics.setLocalizedText(pathMappingLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.pathMappingLabel.text"));
        pathMappingTable.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        pathMappingScrollPane.setViewportView(pathMappingTable);

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
        Mnemonics.setLocalizedText(infoLabel, NbBundle.getMessage(RunAsWebAdvanced.class, "RunAsWebAdvanced.infoLabel.text"));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(pathMappingScrollPane, GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(removePathMappingButton)
                            .add(newPathMappingButton)))
                    .add(debugUrlLabel)
                    .add(layout.createSequentialGroup()
                        .add(defaultUrlRadioButton)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(defaultUrlPreviewLabel))
                    .add(askUrlRadioButton)
                    .add(doNotOpenBrowserRadioButton)
                    .add(pathMappingLabel)
                    .add(infoLabel))
                .addContainerGap())
        );

        layout.linkSize(new Component[] {newPathMappingButton, removePathMappingButton}, GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(debugUrlLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(defaultUrlRadioButton)
                    .add(defaultUrlPreviewLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(askUrlRadioButton)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(doNotOpenBrowserRadioButton)
                .addPreferredGap(LayoutStyle.UNRELATED)
                .add(pathMappingLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(newPathMappingButton)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(removePathMappingButton))
                    .add(pathMappingScrollPane, GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(infoLabel)
                .add(0, 0, 0))
        );
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
        assert PhpProjectUtils.hasText(localPath);
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
    private JLabel infoLabel;
    private JButton newPathMappingButton;
    private JLabel pathMappingLabel;
    private JScrollPane pathMappingScrollPane;
    private JTable pathMappingTable;
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
            return PhpProjectUtils.hasText((String) getValueAt(rowCount - 1, COLUMN_REMOTE_PATH));
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

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            LocalPathCell localPathCell = (LocalPathCell) value;
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
                    File newLocation = Utils.browseLocationAction(table, LastUsedFolders.getPathMapping(), NbBundle.getMessage(RunAsWebAdvanced.class, "LBL_SelectProjectFolder"));
                    if (newLocation != null) {
                        localPathCell.setPath(newLocation.getAbsolutePath());
                        LastUsedFolders.setPathMapping(newLocation);
                    }
                    validateFields();
                }
            }
        }
    }
}
