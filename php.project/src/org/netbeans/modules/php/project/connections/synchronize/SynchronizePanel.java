/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.connections.synchronize;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Panel for remote synchronization.
 */
public final class SynchronizePanel extends JPanel {

    private static final long serialVersionUID = 1674646546545121L;

    // XXX
    @StaticResource
    private static final String RESET_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/info_icon.png"; // NOI18N

    static final TableCellRenderer DEFAULT_TABLE_CELL_RENDERER = new DefaultTableCellRenderer();
    static final TableCellRenderer ERROR_TABLE_CELL_RENDERER = new DefaultTableCellRenderer();

    final List<FileItem> files;
    final FileTableModel tableModel;

    private final String projectName;
    private final String remoteConfigurationName;

    // @GuardedBy(AWT)
    private DialogDescriptor descriptor = null;
    // @GuardedBy(AWT)
    private NotificationLineSupport notificationLineSupport = null;


    SynchronizePanel(String projectName, String remoteConfigurationName, List<FileItem> files) {
        assert SwingUtilities.isEventDispatchThread();
        assert files != null;

        this.projectName = projectName;
        this.remoteConfigurationName = remoteConfigurationName;
        this.files = files;
        tableModel = new FileTableModel(files);

        initComponents();
        initTable();
        initOperationButtons();
    }

    @NbBundle.Messages({
        "# 0 - project name",
        "# 1 - remote configuration name",
        "SynchronizePanel.title=Remote Synchronization for {0}: {1}",
        "SynchronizePanel.firstRun=Running for the first time for this project and this run configuration, more user actions will be needed."
    })
    public boolean open(boolean firstRun) {
        assert SwingUtilities.isEventDispatchThread();
        descriptor = new DialogDescriptor(
                this,
                Bundle.SynchronizePanel_title(projectName, remoteConfigurationName),
                true,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.OK_OPTION,
                null);
        descriptor.setValid(false);
        notificationLineSupport = descriptor.createNotificationLineSupport();
        if (firstRun) {
            notificationLineSupport.setInformationMessage(Bundle.SynchronizePanel_firstRun());
        }
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        return descriptor.getValue() == NotifyDescriptor.OK_OPTION;
    }

    private void initTable() {
        // model
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                validateFiles();
            }
        });
        fileTable.setModel(tableModel);
        // renderer
        fileTable.setDefaultRenderer(String.class, new StringRenderer());
        fileTable.setDefaultRenderer(FileItem.Operation.class, new OperationRenderer());
        // columns
        fileTable.getTableHeader().setReorderingAllowed(false);
        TableColumnModel columnModel = fileTable.getColumnModel();
        columnModel.getColumn(0).setMinWidth(10);
        columnModel.getColumn(0).setMaxWidth(10);
        columnModel.getColumn(0).setResizable(false);
        columnModel.getColumn(2).setMinWidth(100);
        columnModel.getColumn(2).setMaxWidth(100);
        columnModel.getColumn(2).setResizable(false);
        // selections
        fileTable.setColumnSelectionAllowed(false);
        fileTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                int selectedRowCount = fileTable.getSelectedRowCount();
                setEnabledOperationButtons(selectedRowCount > 0);
                setEnabledDiffButton(selectedRowCount);
            }
        });
    }

    @NbBundle.Messages("SynchronizePanel.resetButton.title=Reset to the original state")
    private void initOperationButtons() {
        // operations
        initOperationButton(noopButton, FileItem.Operation.NOOP);
        initOperationButton(downloadButton, FileItem.Operation.DOWNLOAD);
        initOperationButton(uploadButton, FileItem.Operation.UPLOAD);
        initOperationButton(deleteLocallyButton, FileItem.Operation.DELETE_LOCALLY);
        initOperationButton(deleteRemotelyButton, FileItem.Operation.DELETE_REMOTELY);
        // reset
        initResetButton();
    }

    private void initOperationButton(JButton button, FileItem.Operation operation) {
        // XXX
        //button.setText(null);
        button.setText(operation.name());
        //button.setIcon(operation.getIcon());
        button.setToolTipText(operation.getTitle());
        button.addActionListener(new OperationButtonListener(operation));
    }

    private void initResetButton() {
        // XXX
        //resetButton.setText(null);
        resetButton.setText("RESET"); // NOI18N
        //resetButton.setIcon(ImageUtilities.loadImageIcon(RESET_ICON_PATH, false));
        resetButton.setToolTipText(Bundle.SynchronizePanel_resetButton_title());
        resetButton.addActionListener(new OperationButtonListener(null));
    }

    void setEnabledOperationButtons(boolean enabled) {
        noopButton.setEnabled(enabled);
        downloadButton.setEnabled(enabled);
        uploadButton.setEnabled(enabled);
        deleteLocallyButton.setEnabled(enabled);
        deleteRemotelyButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
    }

    void setEnabledDiffButton(int selectedRowCount) {
        if (selectedRowCount != 1) {
            diffButton.setEnabled(false);
            return;
        }
        // XXX improve, ask FileItem
        diffButton.setEnabled(true);
    }

    // XXX
    @NbBundle.Messages({
        "SynchronizePanel.error.operations=Some errors.",
        "SynchronizePanel.warn.operations=Some warnings."
    })
    void validateFiles() {
        assert SwingUtilities.isEventDispatchThread();
        boolean warn = false;
        for (FileItem fileItem : files) {
            if (fileItem.hasError()) {
                notificationLineSupport.setErrorMessage(Bundle.SynchronizePanel_error_operations());
                descriptor.setValid(false);
                return;
            }
            if (fileItem.hasWarning()) {
                warn = true;
            }
        }
        if (warn) {
            notificationLineSupport.setWarningMessage(Bundle.SynchronizePanel_warn_operations());
        } else {
            notificationLineSupport.clearMessages();
        }
        descriptor.setValid(true);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileScrollPane = new JScrollPane();
        fileTable = new JTable();
        diffButton = new JButton();
        noopButton = new JButton();
        downloadButton = new JButton();
        uploadButton = new JButton();
        deleteLocallyButton = new JButton();
        deleteRemotelyButton = new JButton();
        resetButton = new JButton();

        fileTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileScrollPane.setViewportView(fileTable);

        Mnemonics.setLocalizedText(diffButton, NbBundle.getMessage(SynchronizePanel.class, "SynchronizePanel.diffButton.text")); // NOI18N
        diffButton.setEnabled(false);

        Mnemonics.setLocalizedText(noopButton, " "); // NOI18N
        noopButton.setEnabled(false);

        Mnemonics.setLocalizedText(downloadButton, " "); // NOI18N
        downloadButton.setEnabled(false);

        Mnemonics.setLocalizedText(uploadButton, " "); // NOI18N
        uploadButton.setEnabled(false);

        Mnemonics.setLocalizedText(deleteLocallyButton, " "); // NOI18N
        deleteLocallyButton.setEnabled(false);

        Mnemonics.setLocalizedText(deleteRemotelyButton, " "); // NOI18N
        deleteRemotelyButton.setEnabled(false);

        Mnemonics.setLocalizedText(resetButton, " "); // NOI18N
        resetButton.setEnabled(false);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(diffButton)
                        .addGap(18, 18, 18)
                        .addComponent(noopButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(downloadButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(uploadButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(deleteLocallyButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(deleteRemotelyButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(resetButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(fileScrollPane, GroupLayout.DEFAULT_SIZE, 585, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {deleteLocallyButton, deleteRemotelyButton, downloadButton, noopButton, resetButton, uploadButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fileScrollPane, GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(diffButton)
                    .addComponent(noopButton)
                    .addComponent(downloadButton)
                    .addComponent(uploadButton)
                    .addComponent(deleteLocallyButton)
                    .addComponent(deleteRemotelyButton)
                    .addComponent(resetButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton deleteLocallyButton;
    private JButton deleteRemotelyButton;
    private JButton diffButton;
    private JButton downloadButton;
    private JScrollPane fileScrollPane;
    private JTable fileTable;
    private JButton noopButton;
    private JButton resetButton;
    private JButton uploadButton;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class FileTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 16478634354314324L;

        @NbBundle.Messages({
            "SynchronizePanel.table.column.remote.title=Remote Path",
            "SynchronizePanel.table.column.local.title=Local Path",
            "SynchronizePanel.table.column.operation.title=Operation"
        })
        private static final String[] COLUMNS = {
            "", // NOI18N
            Bundle.SynchronizePanel_table_column_remote_title(),
            Bundle.SynchronizePanel_table_column_operation_title(),
            Bundle.SynchronizePanel_table_column_local_title(),
        };

        private final List<FileItem> files;


        public FileTableModel(List<FileItem> files) {
            this.files = files;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public int getRowCount() {
            return files.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @NbBundle.Messages({
            "SynchronizePanel.error.cellValue=!",
            "SynchronizePanel.warning.cellValue=?"
        })
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            FileItem fileItem = files.get(rowIndex);
            if (columnIndex == 0) {
                if (!fileItem.hasError()) {
                    return Bundle.SynchronizePanel_error_cellValue();
                }
                if (fileItem.hasWarning()) {
                    return Bundle.SynchronizePanel_warning_cellValue();
                }
                return null;
            } else if (columnIndex == 1) {
                return fileItem.getRemotePath();
            } else if (columnIndex == 2) {
                return fileItem.getOperation();
            } else if (columnIndex == 3) {
                return fileItem.getLocalPath();
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0
                    || columnIndex == 1
                    || columnIndex == 3) {
                return String.class;
            } else if (columnIndex == 2) {
                return FileItem.Operation.class;
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        public void fireFileItemChange(int row) {
            fireTableCellUpdated(row, 0);
            fireTableCellUpdated(row, 2);
        }

    }

    private final class StringRenderer implements TableCellRenderer {

        private static final long serialVersionUID = 567654543546954L;


        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel rendererComponent;
            String text = (String) value;
            if (column == 0) {
                // error
                rendererComponent = (JLabel) ERROR_TABLE_CELL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                rendererComponent.setHorizontalAlignment(SwingConstants.CENTER);
                rendererComponent.setFont(rendererComponent.getFont().deriveFont(Font.BOLD));
                FileItem fileItem = files.get(row);
                rendererComponent.setForeground(UIManager.getColor(fileItem.hasError() ? "nb.errorForeground" : "nb.warningForeground")); // NOI18N
                rendererComponent.setToolTipText(files.get(row).getMessage());
            } else {
                rendererComponent = (JLabel) DEFAULT_TABLE_CELL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // file path
                rendererComponent.setToolTipText(text);
            }
            rendererComponent.setText(text);
            return rendererComponent;
        }

    }

    private final class OperationRenderer implements TableCellRenderer {

        private static final long serialVersionUID = -6786654671313465458L;


        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel rendererComponent = (JLabel) DEFAULT_TABLE_CELL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            FileItem.Operation operation = (FileItem.Operation) value;
            // XXX replace with icon
            rendererComponent.setText(operation.toString());
            rendererComponent.setToolTipText(operation.getTitle());
            return rendererComponent;
        }

    }

    private final class OperationButtonListener implements ActionListener {

        private final FileItem.Operation operation;


        public OperationButtonListener(FileItem.Operation operation) {
            this.operation = operation;
        }

        // can be done in background thread if needed
        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selectedRows = fileTable.getSelectedRows();
            assert selectedRows.length > 0;
            for (Integer index : selectedRows) {
                FileItem fileItem = files.get(index);
                if (operation == null) {
                    fileItem.resetOperation();
                } else {
                    fileItem.setOperation(operation);
                }
                fileItem.validate();
                tableModel.fireFileItemChange(index);
            }
        }

    }

}
