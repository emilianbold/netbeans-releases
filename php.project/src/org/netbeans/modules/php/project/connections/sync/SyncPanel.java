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
package org.netbeans.modules.php.project.connections.sync;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.sync.diff.DiffPanel;
import org.netbeans.modules.php.project.ui.HintArea;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Panel for remote synchronization.
 */
public final class SyncPanel extends JPanel implements HelpCtx.Provider {

    private static final long serialVersionUID = 1674646546545121L;

    static final Logger LOGGER = Logger.getLogger(SyncPanel.class.getName());

    @StaticResource
    private static final String DIFF_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/diff.png"; // NOI18N
    @StaticResource
    private static final String RESET_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/reset.png"; // NOI18N
    @StaticResource
    private static final String ERROR_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/error.png"; // NOI18N
    @StaticResource
    private static final String WARNING_ICON_PATH = "org/netbeans/modules/php/project/ui/resources/warning.gif"; // NOI18N

    static final TableCellRenderer DEFAULT_TABLE_CELL_RENDERER = new DefaultTableCellRenderer();
    static final TableCellRenderer ERROR_TABLE_CELL_RENDERER = new DefaultTableCellRenderer();

    // @GuardedBy(AWT)
    private static final List<SyncItem.Operation> OPERATIONS = Arrays.asList(
                SyncItem.Operation.NOOP,
                SyncItem.Operation.DOWNLOAD,
                SyncItem.Operation.UPLOAD,
                SyncItem.Operation.DELETE);

    final RemoteClient remoteClient;
    // @GuardedBy(AWT)
    final List<SyncItem> allItems;
    // @GuardedBy(AWT)
    final List<SyncItem> displayedItems;
    // @GuardedBy(AWT)
    final FileTableModel tableModel;
    // @GuardedBy(AWT)
    final JPopupMenu popupMenu = new JPopupMenu();
    // @GuardedBy(AWT)
    Point popupMenuPoint = new Point(); // XXX is there a better way?


    private final PhpProject project;
    private final String remoteConfigurationName;
    private final String defaultInfoMessage;
    // @GuardedBy(AWT)
    private final List<ViewCheckBox> viewCheckBoxes;
    // @GuardedBy(AWT)
    private final ItemListener viewListener = new ViewListener();

    // @GuardedBy(AWT)
    private DialogDescriptor descriptor = null;
    // @GuardedBy(AWT)
    private Boolean rememberShowSummary = null;
    // @GuardedBy(AWT)
    private JButton okButton = null;


    SyncPanel(PhpProject project, String remoteConfigurationName, List<SyncItem> items, RemoteClient remoteClient, boolean forProject, boolean firstRun) {
        assert SwingUtilities.isEventDispatchThread();
        assert items != null;

        this.project = project;
        this.remoteConfigurationName = remoteConfigurationName;
        this.allItems = items;
        displayedItems = new ArrayList<SyncItem>(items);
        this.remoteClient = remoteClient;
        tableModel = new FileTableModel(displayedItems);
        defaultInfoMessage = getDefaultInfoMessage(forProject, firstRun);

        initComponents();
        viewCheckBoxes = getViewCheckBoxes();
        initViewCheckBoxes();
        initViewButtons();
        initTable();
        initOperationButtons();
        initDiffButton();
        initMessages();
        initShowSummaryCheckBox(forProject);
    }

    private JCheckBox createViewCheckBox() {
        ViewCheckBox viewCheckBox = new ViewCheckBox();
        viewCheckBox.setSelected(true);
        viewCheckBox.addItemListener(viewListener);
        return viewCheckBox;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.project.connections.sync.SyncPanel"); // NOI18N
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "# {1} - remote configuration name",
        "SyncPanel.title=Remote Synchronization for {0}: {1}",
        "SyncPanel.button.titleWithMnemonics=S&ynchronize"
    })
    public boolean open() {
        assert SwingUtilities.isEventDispatchThread();
        okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, Bundle.SyncPanel_button_titleWithMnemonics());
        descriptor = new DialogDescriptor(
                this,
                Bundle.SyncPanel_title(project.getName(), remoteConfigurationName),
                true,
                new Object[] {okButton, DialogDescriptor.CANCEL_OPTION},
                okButton,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        okButton.addActionListener(new OkActionListener(dialog));
        descriptor.setClosingOptions(new Object[] {NotifyDescriptor.CANCEL_OPTION});
        descriptor.setAdditionalOptions(new Object[] {showSummaryCheckBox});
        validateItems();
        updateSyncInfo();
        boolean okPressed;
        try {
            dialog.setVisible(true);
            okPressed = descriptor.getValue() == okButton;
        } finally {
            dialog.dispose();
        }
        return okPressed;
    }

    public List<SyncItem> getItems() {
        assert SwingUtilities.isEventDispatchThread();
        return allItems;
    }

    private List<ViewCheckBox> getViewCheckBoxes() {
        return Arrays.asList(
                (ViewCheckBox) viewNoopCheckBox,
                (ViewCheckBox) viewDownloadCheckBox,
                (ViewCheckBox) viewUploadCheckBox,
                (ViewCheckBox) viewDeleteCheckBox,
                (ViewCheckBox) viewSymlinkCheckBox,
                (ViewCheckBox) viewFileDirCollisionCheckBox,
                (ViewCheckBox) viewFileConflictCheckBox,
                (ViewCheckBox) viewWarningCheckBox,
                (ViewCheckBox) viewErrorCheckBox);
    }

    @NbBundle.Messages({
        "SyncPanel.view.warning=W&arning",
        "SyncPanel.view.error=E&rror"
    })
    private void initViewCheckBoxes() {
        // operations
        initViewCheckBox(viewNoopCheckBox, SyncItem.Operation.NOOP);
        initViewCheckBox(viewDownloadCheckBox, EnumSet.of(SyncItem.Operation.DOWNLOAD, SyncItem.Operation.DOWNLOAD_REVIEW));
        initViewCheckBox(viewUploadCheckBox, EnumSet.of(SyncItem.Operation.UPLOAD, SyncItem.Operation.UPLOAD_REVIEW));
        initViewCheckBox(viewDeleteCheckBox, SyncItem.Operation.DELETE);
        initViewCheckBox(viewSymlinkCheckBox, SyncItem.Operation.SYMLINK);
        initViewCheckBox(viewFileDirCollisionCheckBox, SyncItem.Operation.FILE_DIR_COLLISION);
        initViewCheckBox(viewFileConflictCheckBox, SyncItem.Operation.FILE_CONFLICT);
        // warnings & errors
        initViewCheckBox(viewWarningCheckBox, Bundle.SyncPanel_view_warning());
        initViewCheckBox(viewErrorCheckBox, Bundle.SyncPanel_view_error());
        ((ViewCheckBox) viewWarningCheckBox).setFilter(new SyncItemFilter() {
            @Override
            public boolean accept(SyncItem syncItem) {
                return syncItem.validate().hasWarning();
            }
        });
        ((ViewCheckBox) viewErrorCheckBox).setFilter(new SyncItemFilter() {
            @Override
            public boolean accept(SyncItem syncItem) {
                return syncItem.validate().hasError();
            }
        });
    }

    private void initViewCheckBox(JCheckBox checkBox, SyncItem.Operation operation) {
        initViewCheckBox(checkBox, EnumSet.of(operation));
    }

    private void initViewCheckBox(JCheckBox checkBox, final EnumSet<SyncItem.Operation> operations) {
        SyncItem.Operation operation = operations.iterator().next();
        initViewCheckBox(checkBox, operation.getTitleWithMnemonic());
        ((ViewCheckBox) checkBox).setFilter(new SyncItemFilter() {
            @Override
            public boolean accept(SyncItem syncItem) {
                return operations.contains(syncItem.getOperation());
            }
        });
    }

    private void initViewCheckBox(JCheckBox checkBox, String titleWithMnemonic) {
        Mnemonics.setLocalizedText(checkBox, titleWithMnemonic);
    }

    private void initViewButtons() {
        checkAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setViewCheckBoxesSelected(true);
            }
        });
        uncheckAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setViewCheckBoxesSelected(false);
            }
        });
    }

    private void initTable() {
        assert SwingUtilities.isEventDispatchThread();
        // model
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                validateItems();
                updateSyncInfo();
            }
        });
        itemTable.setModel(tableModel);
        // renderer
        itemTable.setDefaultRenderer(Icon.class, new IconRenderer());
        itemTable.setDefaultRenderer(String.class, new StringRenderer());
        itemTable.setDefaultRenderer(SyncItem.Operation.class, new OperationRenderer());
        // rows
        itemTable.setRowHeight(20);
        // columns
        itemTable.getTableHeader().setReorderingAllowed(false);
        TableColumnModel columnModel = itemTable.getColumnModel();
        columnModel.getColumn(0).setMinWidth(20);
        columnModel.getColumn(0).setMaxWidth(20);
        columnModel.getColumn(0).setResizable(false);
        columnModel.getColumn(1).setPreferredWidth(1000);
        columnModel.getColumn(2).setMinWidth(40);
        columnModel.getColumn(2).setPreferredWidth(40);
        columnModel.getColumn(3).setPreferredWidth(1000);
        // selections
        itemTable.setColumnSelectionAllowed(false);
        itemTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                updateSyncInfo();
                setEnabledOperationButtons(itemTable.getSelectedRows());
                setEnabledDiffButton();
            }
        });
        // popup menu
        initTablePopupMenu();
        // actions
        itemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    // cycle operations?
                    if (itemTable.getSelectedColumn() == 2) {
                        cycleOperations();
                    }
                } else if (e.getClickCount() == 2
                        && isDiffActionPossible(false)) {
                    openDiffPanel();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenuPoint.x = e.getX();
                    popupMenuPoint.y = e.getY();
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    @NbBundle.Messages({
        "SyncPanel.popupMenu.resetItem=Reset Operation",
        "SyncPanel.popupMenu.disable.download=Disable Downloads",
        "SyncPanel.popupMenu.disable.upload=Disable Uploads",
        "SyncPanel.popupMenu.disable.delete=Disable Deletions",
        "SyncPanel.popupMenu.diffItem=Diff..."
    })
    private void initTablePopupMenu() {
        // reset
        JMenuItem resetMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_resetItem(), ImageUtilities.loadImageIcon(RESET_ICON_PATH, false));
        resetMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<SyncItem> selectedItems = getSelectedItems(true);
                for (SyncItem item : selectedItems) {
                    item.resetOperation();
                }
                updateDisplayedItems();
                reselectItems(selectedItems);
            }
        });
        popupMenu.add(resetMenuItem);
        popupMenu.addSeparator();
        // set operations
        List<SyncItem.Operation> setOperations = Arrays.asList(
                SyncItem.Operation.NOOP,
                SyncItem.Operation.DOWNLOAD,
                SyncItem.Operation.UPLOAD,
                SyncItem.Operation.DELETE);
        for (SyncItem.Operation operation : setOperations) {
            JMenuItem operationMenuItem = new JMenuItem(operation.getToolTip(), operation.getIcon());
            operationMenuItem.addActionListener(new PopupMenuItemListener(operation));
            popupMenu.add(operationMenuItem);
        }
        popupMenu.addSeparator();
        // disable operations
        @SuppressWarnings("unchecked")
        List<Pair<SyncItem.Operation, String>> disableOperations = Arrays.asList(
                Pair.of(SyncItem.Operation.DOWNLOAD, Bundle.SyncPanel_popupMenu_disable_download()),
                Pair.of(SyncItem.Operation.UPLOAD, Bundle.SyncPanel_popupMenu_disable_upload()),
                Pair.of(SyncItem.Operation.DELETE, Bundle.SyncPanel_popupMenu_disable_delete()));
        for (Pair<SyncItem.Operation, String> pair : disableOperations) {
            JMenuItem operationMenuItem = new JMenuItem(pair.second);
            operationMenuItem.addActionListener(new PopupMenuItemListener(pair.first, SyncItem.Operation.NOOP));
            popupMenu.add(operationMenuItem);
        }
        popupMenu.addSeparator();
        // diff
        final JMenuItem diffMenuItem = new JMenuItem(Bundle.SyncPanel_popupMenu_diffItem(), ImageUtilities.loadImageIcon(DIFF_ICON_PATH, false));
        diffMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDiffPanel();
            }
        });
        popupMenu.add(diffMenuItem);
        // listener
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                diffMenuItem.setEnabled(isDiffActionPossible(true));
            }
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // noop
            }
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // noop
            }
        });
    }

    private void initOperationButtons() {
        // operations
        initOperationButton(noopButton, SyncItem.Operation.NOOP);
        initOperationButton(downloadButton, SyncItem.Operation.DOWNLOAD);
        initOperationButton(uploadButton, SyncItem.Operation.UPLOAD);
        initOperationButton(deleteButton, SyncItem.Operation.DELETE);
        // reset
        initResetButton();
    }

    private void initOperationButton(JButton button, SyncItem.Operation operation) {
        button.setText(null);
        button.setIcon(operation.getIcon());
        button.setToolTipText(operation.getToolTip());
        button.addActionListener(new OperationButtonListener(operation));
    }

    @NbBundle.Messages("SyncPanel.resetButton.toolTip=Reset to suggested operation (discards Diff changes)")
    private void initResetButton() {
        resetButton.setText(null);
        resetButton.setIcon(ImageUtilities.loadImageIcon(RESET_ICON_PATH, false));
        resetButton.setToolTipText(Bundle.SyncPanel_resetButton_toolTip());
        resetButton.addActionListener(new OperationButtonListener(null));
    }

    @NbBundle.Messages("SyncPanel.diffButton.toolTip=Review differences between remote and local file")
    private void initDiffButton() {
        diffButton.setText(null);
        diffButton.setIcon(ImageUtilities.loadImageIcon(DIFF_ICON_PATH, false));
        diffButton.setToolTipText(Bundle.SyncPanel_diffButton_toolTip());
        diffButton.addActionListener(new DiffActionListener());
    }

    private void initMessages() {
        // sync info
        syncInfoPanel.setBackground(Utils.getHintBackground());
        syncInfoPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, UIManager.getColor("Table.gridColor"))); // NOI18N
        // messages
        messagesTextPane.setText(defaultInfoMessage);
    }

    @NbBundle.Messages({
        "SyncPanel.info.firstRun=<strong>First time for this project and configuration - more user actions may be needed.</strong><br>",
        "SyncPanel.info.individualFiles=<strong>Run synchronization on Source Files for more accurate result.</strong><br>",
        "SyncPanel.info.experimental=Note that Remote Synchronization is experimental. Review all suggested operations before proceeding. Note that remote timestamps may not be correct."
    })
    private String getDefaultInfoMessage(boolean forProject, boolean firstRun) {
        String msg = Bundle.SyncPanel_info_experimental();
        if (forProject) {
            if (firstRun) {
                msg = Bundle.SyncPanel_info_firstRun() + msg;
            }
        } else {
            // individual files
            msg = Bundle.SyncPanel_info_individualFiles() + msg;
        }
        return msg;
    }

    private void initShowSummaryCheckBox(boolean showSummary) {
        if (!showSummary) {
            showSummaryCheckBox.setVisible(false);
            return;
        }
        showSummaryCheckBox.setSelected(PhpOptions.getInstance().getRemoteSyncShowSummary());
        showSummaryCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rememberShowSummary = e.getStateChange() == ItemEvent.SELECTED;
            }
        });
    }

    void setEnabledOperationButtons(int[] selectedRows) {
        boolean enabled = areOperationButtonsEnabled(selectedRows);
        noopButton.setEnabled(enabled);
        downloadButton.setEnabled(enabled);
        uploadButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
    }

    private boolean areOperationButtonsEnabled(int[] selectedRows) {
        assert SwingUtilities.isEventDispatchThread();
        if (selectedRows.length == 0) {
            return false;
        }
        for (int i : selectedRows) {
            if (!displayedItems.get(i).isOperationChangePossible()) {
                return false;
            }
        }
        return true;
    }

    boolean isDiffActionPossible(boolean includingMousePosition) {
        List<SyncItem> selectedItems = getSelectedItems(includingMousePosition);
        if (selectedItems.size() != 1) {
            return false;
        }
        return selectedItems.get(0).isDiffPossible();
    }

    void setEnabledDiffButton() {
        diffButton.setEnabled(isDiffActionPossible(false));
    }

    SyncItem getSelectedItem(boolean includingMousePosition) {
        assert SwingUtilities.isEventDispatchThread();
        List<SyncItem> selectedItems = getSelectedItems(includingMousePosition);
        if (selectedItems.size() == 1) {
            return selectedItems.get(0);
        }
        assert false : "Any row should be selected";
        return null;
    }

    void reselectItem(SyncItem syncItem) {
        int index = displayedItems.indexOf(syncItem); // XXX performance?
        if (index != -1) {
            itemTable.getSelectionModel().addSelectionInterval(index, index);
        }
    }

    List<SyncItem> getSelectedItems(boolean includingMousePosition) {
        assert SwingUtilities.isEventDispatchThread();
        int[] selectedRows = itemTable.getSelectedRows();
        if (selectedRows.length == 0) {
            if (includingMousePosition) {
                return Collections.singletonList(displayedItems.get(itemTable.rowAtPoint(popupMenuPoint)));
            }
            return Collections.emptyList();
        }
        List<SyncItem> selectedItems = new ArrayList<SyncItem>(selectedRows.length);
        for (int index : selectedRows) {
            SyncItem syncItem = displayedItems.get(index);
            selectedItems.add(syncItem);
        }
        return selectedItems;
    }

    void reselectItems(List<SyncItem> selectedItems) {
        assert SwingUtilities.isEventDispatchThread();
        for (SyncItem item : selectedItems) {
            reselectItem(item);
        }
    }

    @NbBundle.Messages({
        "SyncPanel.error.operations=Synchronization not possible. Resolve conflicts first.",
        "SyncPanel.warn.operations=Synchronization possible but warnings should be reviewed first."
    })
    void validateItems() {
        assert SwingUtilities.isEventDispatchThread();
        boolean warn = false;
        for (SyncItem syncItem : allItems) {
            SyncItem.ValidationResult validationResult = syncItem.validate();
            if (validationResult.hasError()) {
                setError(Bundle.SyncPanel_error_operations());
                return;
            }
            if (validationResult.hasWarning()) {
                warn = true;
            }
        }
        if (warn) {
            setWarning(Bundle.SyncPanel_warn_operations());
        } else {
            clearError();
        }
    }

    void setError(String error) {
        String msg = getImgTag(ERROR_ICON_PATH) + getColoredText(error, UIManager.getColor("nb.errorForeground")) + "<br>" + defaultInfoMessage; // NOI18N
        messagesTextPane.setText(msg);
        descriptor.setValid(false);
        okButton.setEnabled(false);
    }

    void setWarning(String warning) {
        String msg = getImgTag(WARNING_ICON_PATH) + getColoredText(warning, UIManager.getColor("nb.warningForeground")) + "<br>" + defaultInfoMessage; // NOI18N
        messagesTextPane.setText(msg);
        descriptor.setValid(true);
        okButton.setEnabled(true);
    }

    void clearError() {
        messagesTextPane.setText(defaultInfoMessage);
        descriptor.setValid(true);
        okButton.setEnabled(true);
    }

    private String getImgTag(String src) {
        return "<img src=\"" + SyncPanel.class.getClassLoader().getResource(src).toExternalForm() + "\">&nbsp;"; // NOI18N
    }

    private String getColoredText(String text, Color color) {
        String colorText = "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")"; // NOI18N
        return "<span style=\"color: " + colorText + ";\">" + text + "</span>"; // NOI18N
    }

    @NbBundle.Messages({
        "# {0} - synchronization info",
        "SyncPanel.info.prefix.all=All: {0}",
        "# {0} - synchronization info",
        "SyncPanel.info.prefix.selection=Selection: {0}",
        "# {0} - file name",
        "# {1} - message",
        "SyncPanel.info.prefix.error=Error ({0}): {1}",
        "# {0} - file name",
        "# {1} - message",
        "SyncPanel.info.prefix.warning=Warning ({0}): {1}",
        "# {0} - number of files to be downloaded",
        "# {1} - number of files to be uploaded",
        "# {2} - number of files to be deleted",
        "# {3} - number of files without any operation",
        "# {4} - number of files with errors",
        "# {5} - number of files with warnings",
        "SyncPanel.info.status={0} downloads, {1} uploads, {2} deletions, "
            + "{3} no-ops, {4} errors, {5} warnings."
    })
    void updateSyncInfo() {
        List<SyncItem> selectedItems = getSelectedItems(false);
        if (selectedItems.size() == 1) {
            SyncItem syncItem = selectedItems.get(0);
            SyncItem.ValidationResult result = syncItem.validate();
            if (result.hasError()) {
                syncInfoLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
                syncInfoLabel.setText(Bundle.SyncPanel_info_prefix_error(syncItem.getName(), result.getMessage()));
                return;
            }
            if (result.hasWarning()) {
                syncInfoLabel.setForeground(UIManager.getColor("nb.warningForeground")); // NOI18N
                syncInfoLabel.setText(Bundle.SyncPanel_info_prefix_warning(syncItem.getName(), result.getMessage()));
                return;
            }
            selectedItems.clear();
        }
        // all or selection
        boolean all = false;
        if (selectedItems.isEmpty()) {
            all = true;
            selectedItems = allItems;
        }
        SyncInfo syncInfo = getSyncInfo(selectedItems);
        String info = Bundle.SyncPanel_info_status(syncInfo.download, syncInfo.upload, syncInfo.delete, syncInfo.noop, syncInfo.errors, syncInfo.warnings);
        String msg;
        if (all) {
            msg = Bundle.SyncPanel_info_prefix_all(info);
        } else {
            msg = Bundle.SyncPanel_info_prefix_selection(info);
        }
        syncInfoLabel.setForeground(UIManager.getColor("Label.foreground")); // NOI18N
        syncInfoLabel.setText(msg);
    }

    public SyncInfo getSyncInfo(List<SyncItem> items) {
        assert SwingUtilities.isEventDispatchThread();
        SyncInfo syncInfo = new SyncInfo();
        for (SyncItem syncItem : items) {
            SyncItem.ValidationResult validationResult = syncItem.validate();
            if (validationResult.hasError()) {
                syncInfo.errors++;
            } else if (validationResult.hasWarning()) {
                syncInfo.warnings++;
            }
            switch (syncItem.getOperation()) {
                case SYMLINK:
                    // noop
                    break;
                case NOOP:
                    syncInfo.noop++;
                    break;
                case DOWNLOAD:
                case DOWNLOAD_REVIEW:
                    syncInfo.download++;
                    break;
                case UPLOAD:
                case UPLOAD_REVIEW:
                    syncInfo.upload++;
                    break;
                case DELETE:
                    syncInfo.delete++;
                    break;
                case FILE_CONFLICT:
                case FILE_DIR_COLLISION:
                    // noop, already counted
                    break;
                default:
                    assert false : "Unknown operation: " + syncItem.getOperation();
            }
        }
        return syncInfo;
    }

    void openDiffPanel() {
        assert SwingUtilities.isEventDispatchThread();

        SyncItem syncItem = getSelectedItem(true);
        if (syncItem == null) {
            // should not happen
            return;
        }
        DiffPanel diffPanel = new DiffPanel(remoteClient, syncItem, ProjectPropertiesSupport.getEncoding(project));
        try {
            if (diffPanel.open()) {
                syncItem.setOperation(SyncItem.Operation.UPLOAD);
                // need to redraw table
                updateDisplayedItems();
                // reselect the row?
                reselectItem(syncItem);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error while saving document", ex);
            setError(Bundle.SyncPanel_error_documentSave());
        }
    }

    void cycleOperations() {
        SyncItem syncItem = getSelectedItem(false);
        if (syncItem == null) {
            // should not happen
            return;
        }
        int index = OPERATIONS.indexOf(syncItem.getOperation());
        if (index != -1) {
            if (index == OPERATIONS.size() - 1) {
                index = 0;
            } else {
                index++;
            }
            syncItem.setOperation(OPERATIONS.get(index));
            // need to redraw table
            updateDisplayedItems();
            reselectItem(syncItem);
        }
    }

    void setViewCheckBoxesSelected(boolean selected) {
        for (ViewCheckBox checkBox : viewCheckBoxes) {
            checkBox.setSelected(selected);
        }
    }

    /**
     * To preserve correct order and to show items that belong to more
     * view groups (e.g. symlink - warning & symlink).
     */
    void updateDisplayedItems() {
        assert SwingUtilities.isEventDispatchThread();
        displayedItems.clear();
        List<ViewCheckBox> selectedViewCheckBoxes = getSelectedViewCheckBoxes();
        if (!selectedViewCheckBoxes.isEmpty()) {
            // some view button selected
            for (SyncItem syncItem : allItems) {
                for (ViewCheckBox checkBox : selectedViewCheckBoxes) {
                    if (checkBox.getFilter().accept(syncItem)) {
                        displayedItems.add(syncItem);
                        break;
                    }
                }
            }
        }
        tableModel.fireSyncItemsChange();
    }

    private List<ViewCheckBox> getSelectedViewCheckBoxes() {
        List<ViewCheckBox> selected = new ArrayList<ViewCheckBox>(viewCheckBoxes.size());
        for (ViewCheckBox button : viewCheckBoxes) {
            if (button.isSelected()) {
                selected.add(button);
            }
        }
        return selected;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showSummaryCheckBox = new JCheckBox();
        operationsPanel = new JPanel();
        viewDownloadCheckBox = createViewCheckBox();
        viewUploadCheckBox = createViewCheckBox();
        viewNoopCheckBox = createViewCheckBox();
        viewDeleteCheckBox = createViewCheckBox();
        problemsPanel = new JPanel();
        viewWarningCheckBox = createViewCheckBox();
        viewErrorCheckBox = createViewCheckBox();
        viewFileConflictCheckBox = createViewCheckBox();
        viewFileDirCollisionCheckBox = createViewCheckBox();
        viewSymlinkCheckBox = createViewCheckBox();
        spaceHolderPanel = new JPanel();
        uncheckAllButton = new JButton();
        checkAllButton = new JButton();
        itemScrollPane = new JScrollPane();
        itemTable = new JTable();
        syncInfoPanel = new JPanel();
        syncInfoLabel = new JLabel();
        operationButtonsPanel = new JPanel();
        diffButton = new JButton();
        noopButton = new JButton();
        downloadButton = new JButton();
        uploadButton = new JButton();
        deleteButton = new JButton();
        resetButton = new JButton();
        messagesScrollPane = new JScrollPane();
        messagesTextPane = new HintArea();
        Mnemonics.setLocalizedText(showSummaryCheckBox, NbBundle.getMessage(SyncPanel.class, "SyncPanel.showSummaryCheckBox.text")); // NOI18N

        operationsPanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(SyncPanel.class, "SyncPanel.operationsPanel.title"))); // NOI18N

        GroupLayout operationsPanelLayout = new GroupLayout(operationsPanel);
        operationsPanel.setLayout(operationsPanelLayout);
        operationsPanelLayout.setHorizontalGroup(
            operationsPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(operationsPanelLayout.createSequentialGroup()
                .addContainerGap()

                .addGroup(operationsPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(viewDownloadCheckBox).addComponent(viewUploadCheckBox)).addGap(18, 18, 18).addGroup(operationsPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(viewNoopCheckBox).addComponent(viewDeleteCheckBox)).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        operationsPanelLayout.setVerticalGroup(
            operationsPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(operationsPanelLayout.createSequentialGroup()
                .addContainerGap()

                .addGroup(operationsPanelLayout.createParallelGroup(Alignment.TRAILING).addComponent(viewNoopCheckBox).addComponent(viewDownloadCheckBox)).addPreferredGap(ComponentPlacement.UNRELATED).addGroup(operationsPanelLayout.createParallelGroup(Alignment.TRAILING).addComponent(viewUploadCheckBox).addComponent(viewDeleteCheckBox)).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        problemsPanel.setBorder(BorderFactory.createTitledBorder(NbBundle.getMessage(SyncPanel.class, "SyncPanel.problemsPanel.title"))); // NOI18N

        GroupLayout problemsPanelLayout = new GroupLayout(problemsPanel);
        problemsPanel.setLayout(problemsPanelLayout);
        problemsPanelLayout.setHorizontalGroup(
            problemsPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(problemsPanelLayout.createSequentialGroup()
                .addContainerGap()

                .addGroup(problemsPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(viewWarningCheckBox).addComponent(viewErrorCheckBox)).addGap(18, 18, 18).addGroup(problemsPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(viewFileConflictCheckBox).addComponent(viewFileDirCollisionCheckBox)).addGap(18, 18, 18).addComponent(viewSymlinkCheckBox).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        problemsPanelLayout.setVerticalGroup(
            problemsPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(problemsPanelLayout.createSequentialGroup()
                .addContainerGap()

                .addGroup(problemsPanelLayout.createParallelGroup(Alignment.TRAILING).addComponent(viewSymlinkCheckBox).addComponent(viewFileConflictCheckBox).addComponent(viewWarningCheckBox)).addPreferredGap(ComponentPlacement.RELATED).addGroup(problemsPanelLayout.createParallelGroup(Alignment.TRAILING).addComponent(viewErrorCheckBox).addComponent(viewFileDirCollisionCheckBox)).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        Mnemonics.setLocalizedText(uncheckAllButton, NbBundle.getMessage(SyncPanel.class, "SyncPanel.uncheckAllButton.text")); // NOI18N
        Mnemonics.setLocalizedText(checkAllButton, NbBundle.getMessage(SyncPanel.class, "SyncPanel.checkAllButton.text")); // NOI18N

        GroupLayout spaceHolderPanelLayout = new GroupLayout(spaceHolderPanel);
        spaceHolderPanel.setLayout(spaceHolderPanelLayout);
        spaceHolderPanelLayout.setHorizontalGroup(
            spaceHolderPanelLayout.createParallelGroup(Alignment.LEADING).addComponent(uncheckAllButton).addComponent(checkAllButton, Alignment.TRAILING)
        );

        spaceHolderPanelLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {checkAllButton, uncheckAllButton});

        spaceHolderPanelLayout.setVerticalGroup(
            spaceHolderPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(spaceHolderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkAllButton)

                .addPreferredGap(ComponentPlacement.RELATED).addComponent(uncheckAllButton).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        itemTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        itemScrollPane.setViewportView(itemTable);
        Mnemonics.setLocalizedText(syncInfoLabel, "SYNC INFO LABEL"); // NOI18N

        GroupLayout syncInfoPanelLayout = new GroupLayout(syncInfoPanel);
        syncInfoPanel.setLayout(syncInfoPanelLayout);
        syncInfoPanelLayout.setHorizontalGroup(
            syncInfoPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(syncInfoPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(syncInfoLabel)

                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        syncInfoPanelLayout.setVerticalGroup(
            syncInfoPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(syncInfoPanelLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(syncInfoLabel)
                .addGap(5, 5, 5))
        );

        diffButton.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/php/project/ui/resources/diff.png"))); // NOI18N
        diffButton.setEnabled(false);
        operationButtonsPanel.add(diffButton);

        Mnemonics.setLocalizedText(noopButton, " "); // NOI18N
        noopButton.setEnabled(false);
        operationButtonsPanel.add(noopButton);

        Mnemonics.setLocalizedText(downloadButton, " "); // NOI18N
        downloadButton.setEnabled(false);
        operationButtonsPanel.add(downloadButton);

        Mnemonics.setLocalizedText(uploadButton, " "); // NOI18N
        uploadButton.setEnabled(false);
        operationButtonsPanel.add(uploadButton);

        Mnemonics.setLocalizedText(deleteButton, " "); // NOI18N
        deleteButton.setEnabled(false);
        operationButtonsPanel.add(deleteButton);

        Mnemonics.setLocalizedText(resetButton, " "); // NOI18N
        resetButton.setEnabled(false);
        operationButtonsPanel.add(resetButton);

        messagesScrollPane.setBorder(null);
        messagesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        messagesScrollPane.setViewportView(messagesTextPane);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                .addContainerGap()

                .addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(itemScrollPane, GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE).addGroup(layout.createSequentialGroup()

                        .addComponent(operationsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(problemsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.RELATED, 0, GroupLayout.PREFERRED_SIZE).addComponent(spaceHolderPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(messagesScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(operationButtonsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(syncInfoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup()
                .addContainerGap()

                .addGroup(layout.createParallelGroup(Alignment.LEADING, false).addComponent(operationsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(problemsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(spaceHolderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(itemScrollPane, GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE).addGap(0, 0, 0).addComponent(syncInfoPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(operationButtonsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(messagesScrollPane, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton checkAllButton;
    private JButton deleteButton;
    private JButton diffButton;
    private JButton downloadButton;
    private JScrollPane itemScrollPane;
    private JTable itemTable;
    private JScrollPane messagesScrollPane;
    private JTextPane messagesTextPane;
    private JButton noopButton;
    private JPanel operationButtonsPanel;
    private JPanel operationsPanel;
    private JPanel problemsPanel;
    private JButton resetButton;
    private JCheckBox showSummaryCheckBox;
    private JPanel spaceHolderPanel;
    private JLabel syncInfoLabel;
    private JPanel syncInfoPanel;
    private JButton uncheckAllButton;
    private JButton uploadButton;
    private JCheckBox viewDeleteCheckBox;
    private JCheckBox viewDownloadCheckBox;
    private JCheckBox viewErrorCheckBox;
    private JCheckBox viewFileConflictCheckBox;
    private JCheckBox viewFileDirCollisionCheckBox;
    private JCheckBox viewNoopCheckBox;
    private JCheckBox viewSymlinkCheckBox;
    private JCheckBox viewUploadCheckBox;
    private JCheckBox viewWarningCheckBox;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class FileTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 16478634354314324L;

        @NbBundle.Messages({
            "SyncPanel.table.column.remote.title=Remote Path",
            "SyncPanel.table.column.local.title=Local Path"
        })
        private static final String[] COLUMNS = {
            "", // NOI18N
            Bundle.SyncPanel_table_column_remote_title(),
            "", // NOI18N
            Bundle.SyncPanel_table_column_local_title(),
        };

        private final List<SyncItem> items;


        public FileTableModel(List<SyncItem> items) {
            assert SwingUtilities.isEventDispatchThread();
            this.items = items;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            assert SwingUtilities.isEventDispatchThread();
            return false;
        }

        @Override
        public int getRowCount() {
            assert SwingUtilities.isEventDispatchThread();
            return items.size();
        }

        @Override
        public int getColumnCount() {
            assert SwingUtilities.isEventDispatchThread();
            return COLUMNS.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            assert SwingUtilities.isEventDispatchThread();
            SyncItem syncItem = items.get(rowIndex);
            if (columnIndex == 0) {
                SyncItem.ValidationResult validationResult = syncItem.validate();
                if (validationResult.hasError()) {
                    return ImageUtilities.loadImageIcon(ERROR_ICON_PATH, false);
                }
                if (validationResult.hasWarning()) {
                    return ImageUtilities.loadImageIcon(WARNING_ICON_PATH, false);
                }
                return null;
            } else if (columnIndex == 1) {
                return syncItem.getRemotePath();
            } else if (columnIndex == 2) {
                return syncItem.getOperation();
            } else if (columnIndex == 3) {
                return syncItem.getLocalPath();
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public String getColumnName(int column) {
            assert SwingUtilities.isEventDispatchThread();
            return COLUMNS[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            assert SwingUtilities.isEventDispatchThread();
            if (columnIndex == 0) {
                return Icon.class;
            } else if (columnIndex == 1
                    || columnIndex == 3) {
                return String.class;
            } else if (columnIndex == 2) {
                return SyncItem.Operation.class;
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        public void fireSyncItemsChange() {
            assert SwingUtilities.isEventDispatchThread();
            fireTableDataChanged();
        }

    }

    private final class IconRenderer implements TableCellRenderer {

        private static final long serialVersionUID = -46865321321L;


        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            assert SwingUtilities.isEventDispatchThread();
            Icon icon = (Icon) value;
            JLabel rendererComponent = (JLabel) DEFAULT_TABLE_CELL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            rendererComponent.setHorizontalAlignment(SwingConstants.CENTER);
            rendererComponent.setToolTipText(displayedItems.get(row).validate().getMessage());
            rendererComponent.setText(null);
            rendererComponent.setIcon(icon);
            return rendererComponent;
        }

    }

    private final class StringRenderer implements TableCellRenderer {

        private static final long serialVersionUID = 567654543546954L;


        @NbBundle.Messages({
            "# {0} - file name",
            "SyncPanel.localFile.modified.mark={0}*"
        })
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String text = (String) value;
            JLabel rendererComponent = (JLabel) DEFAULT_TABLE_CELL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (text != null) {
                rendererComponent.setHorizontalAlignment(SwingConstants.LEFT);
                rendererComponent.setToolTipText(text);
                if (column == 3) {
                    // local file
                    if (displayedItems.get(row).hasTmpLocalFile()) {
                        text = Bundle.SyncPanel_localFile_modified_mark(text);
                    }
                }
                // add left padding - space just behaves better (on focus, "frame" has the same size as the cell itself)
                //rendererComponent.setBorder(new CompoundBorder(new EmptyBorder(new Insets(0, 2, 0, 0)), rendererComponent.getBorder()));
                text = " " + text; // NOI18N
            }
            rendererComponent.setText(text);
            rendererComponent.setIcon(null);
            return rendererComponent;
        }

    }

    private final class OperationRenderer implements TableCellRenderer {

        private static final long serialVersionUID = -6786654671313465458L;


        @NbBundle.Messages({
            "# {0} - operation",
            "SyncPanel.operation.tooltip={0} (click to change)"
        })
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel rendererComponent = (JLabel) DEFAULT_TABLE_CELL_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            SyncItem.Operation operation = (SyncItem.Operation) value;
            rendererComponent.setIcon(operation.getIcon());
            if (OPERATIONS.contains(operation)) {
                rendererComponent.setToolTipText(Bundle.SyncPanel_operation_tooltip(operation.getTitle()));
            } else {
                rendererComponent.setToolTipText(operation.getTitle());
            }
            rendererComponent.setText(null);
            rendererComponent.setHorizontalAlignment(SwingConstants.CENTER);
            return rendererComponent;
        }

    }

    private final class OperationButtonListener implements ActionListener {

        private final SyncItem.Operation operation;


        public OperationButtonListener(SyncItem.Operation operation) {
            this.operation = operation;
        }

        // can be done in background thread if needed
        @Override
        public void actionPerformed(ActionEvent e) {
            assert SwingUtilities.isEventDispatchThread();
            List<SyncItem> selectedItems = getSelectedItems(false);
            for (SyncItem syncItem : selectedItems) {
                if (operation == null) {
                    syncItem.resetOperation();
                } else {
                    syncItem.setOperation(operation);
                }
            }
            // need to redraw table
            updateDisplayedItems();
            // reselect the rows?
            reselectItems(selectedItems);
        }

    }

    private final class DiffActionListener implements ActionListener {

        @NbBundle.Messages("SyncPanel.error.documentSave=Cannot save file content.")
        @Override
        public void actionPerformed(ActionEvent e) {
            openDiffPanel();
        }

    }

    private final class OkActionListener implements ActionListener {

        private final Dialog dialog;


        public OkActionListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (rememberShowSummary != null) {
                PhpOptions.getInstance().setRemoteSyncShowSummary(rememberShowSummary);
            }
            if (!showSummaryCheckBox.isVisible()
                    || !showSummaryCheckBox.isSelected()) {
                closeDialog();
                return;
            }
            SyncInfo syncInfo = getSyncInfo(allItems);
            SummaryPanel panel = new SummaryPanel(
                    syncInfo.upload,
                    syncInfo.download,
                    syncInfo.delete,
                    syncInfo.noop);
            if (panel.open()) {
                closeDialog();
            }
        }

        private void closeDialog() {
            dialog.setVisible(false);
        }

    }

    private static final class ViewCheckBox extends JCheckBox {

        private static final long serialVersionUID = 16576854546544L;

        private SyncItemFilter filter;


        public SyncItemFilter getFilter() {
            return filter;
        }

        public void setFilter(SyncItemFilter filter) {
            this.filter = filter;
        }

    }

    private interface SyncItemFilter {
        boolean accept(SyncItem syncItem);
    }

    private class ViewListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            updateDisplayedItems();
        }

    }

    private class PopupMenuItemListener implements ActionListener {

        private final SyncItem.Operation fromOperation;
        private final SyncItem.Operation toOperation;

        public PopupMenuItemListener(SyncItem.Operation toOperation) {
            this(null, toOperation);
        }

        public PopupMenuItemListener(SyncItem.Operation fromOperation, SyncItem.Operation toOperation) {
            this.fromOperation = fromOperation;
            this.toOperation = toOperation;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<SyncItem> selectedItems = getSelectedItems(true);
            for (SyncItem item : selectedItems) {
                if (fromOperation == null
                        || fromOperation == item.getOperation()) {
                    item.setOperation(toOperation);
                }
            }
            updateDisplayedItems();
            reselectItems(selectedItems);
        }

    }

    public static final class SyncInfo {

        public int download = 0;
        public int upload = 0;
        public int delete = 0;
        public int noop = 0;
        public int errors = 0;
        public int warnings = 0;

    }

}
