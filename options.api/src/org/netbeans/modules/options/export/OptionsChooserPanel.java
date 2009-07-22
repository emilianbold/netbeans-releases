/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.options.export;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import org.netbeans.swing.outline.CheckRenderDataProvider;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RowModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Export/import options panel.
 * @author Jiri Skrivanek
 */
public final class OptionsChooserPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(OptionsChooserPanel.class.getName());
    private static final Icon NODE_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/options/export/defaultNode.gif", true);  //NOI18N
    private static final Color DISABLED_COLOR = UIManager.getColor("Label.disabledForeground");  //NOI18N
    private DialogDescriptor dialogDescriptor;
    private PanelType panelType;
    private OptionsExportModel optionsExportModel;
    private TreeModel treeModel;

    /** To distinguish between import and export panels. */
    private enum PanelType {

        EXPORT, IMPORT
    };

    private OptionsChooserPanel() {
        initComponents();
        Mnemonics.setLocalizedText(btnBrowse, NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.btnBrowse"));
        Mnemonics.setLocalizedText(lblFile, NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.lblFile.text"));
        Mnemonics.setLocalizedText(lblHint, NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.lblHint.text"));
    }

    private void setOptionsExportModel(OptionsExportModel optionsExportModel) {
        this.optionsExportModel = optionsExportModel;
    }

    private OptionsExportModel getOptionsExportModel() {
        return optionsExportModel;
    }

    /** Shows panel for export of options. */
    public static void showExportDialog() {
        LOGGER.fine("showExportDialog");  //NOI18N
        File sourceUserdir = new File(System.getProperty("netbeans.user")); // NOI18N
        final OptionsChooserPanel optionsChooserPanel = new OptionsChooserPanel();
        optionsChooserPanel.panelType = PanelType.EXPORT;
        optionsChooserPanel.setOptionsExportModel(new OptionsExportModel(sourceUserdir));
        optionsChooserPanel.loadOptions();
        optionsChooserPanel.txtFile.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                optionsChooserPanel.dialogDescriptor.setValid(optionsChooserPanel.isPanelValid());
            }

            public void removeUpdate(DocumentEvent e) {
                optionsChooserPanel.dialogDescriptor.setValid(optionsChooserPanel.isPanelValid());
            }

            public void changedUpdate(DocumentEvent e) {
                optionsChooserPanel.dialogDescriptor.setValid(optionsChooserPanel.isPanelValid());
            }
        });

        DialogDescriptor dd = new DialogDescriptor(
                optionsChooserPanel,
                NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.export.title"),
                true,
                new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        // add bottom user notification area
        dd.createNotificationLineSupport();
        dd.setValid(false);
        optionsChooserPanel.setDialogDescriptor(dd);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);

        if (DialogDescriptor.OK_OPTION.equals(dd.getValue())) {
            String targetPath = optionsChooserPanel.getSelectedFilePath();
            optionsChooserPanel.getOptionsExportModel().doExport(new File(targetPath));
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.export.status.text"));
            LOGGER.fine("Export finished.");  //NOI18N
        }
    }

    /** Shows panel for import of options. */
    public static void showImportDialog() {
        LOGGER.fine("showImportDialog");  //NOI18N
        OptionsChooserPanel optionsChooserPanel = new OptionsChooserPanel();
        optionsChooserPanel.txtFile.setEditable(false);
        Mnemonics.setLocalizedText(optionsChooserPanel.lblFile, NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.lblFile.text"));
        Mnemonics.setLocalizedText(optionsChooserPanel.lblHint, NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.lblHint.text"));
        optionsChooserPanel.panelType = PanelType.IMPORT;

        DialogDescriptor dd = new DialogDescriptor(
                optionsChooserPanel,
                NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.title"),
                true,
                new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        dd.createNotificationLineSupport();
        dd.setValid(false);
        final ImportConfirmationPanel confirmationPanel = new ImportConfirmationPanel();
        dd.setButtonListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == DialogDescriptor.OK_OPTION) {
                    // show confirmation dialog when user click OK
                    confirmationPanel.showConfirmation();
                }
            }
        });
        optionsChooserPanel.setDialogDescriptor(dd);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);

        if (DialogDescriptor.OK_OPTION.equals(dd.getValue())) {
            if (!confirmationPanel.confirmed()) {
                LOGGER.fine("Import canceled.");  //NOI18N
                return;
            }
            // do import
            File targetUserdir = new File(System.getProperty("netbeans.user")); // NOI18N
            try {
                optionsChooserPanel.getOptionsExportModel().doImport(targetUserdir);
            } catch (IOException ioe) {
                // report exception and return if import failed
                Exceptions.attachLocalizedMessage(ioe,
                        NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.error"));
                LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
                return;
            }
            LOGGER.fine("Import finished.");  //NOI18N
            // restart IDE
            if (confirmationPanel.restartNow()) {
                LifecycleManager.getDefault().markForRestart();
                LifecycleManager.getDefault().exit();
            }
        }
    }

    /** Loading of available options for export/import moved from AWT thread
     * and a message is shown in the meantime (see #163142). */
    private void loadOptions() {
        assert SwingUtilities.isEventDispatchThread() : "Should be called from AWT thread only.";  //NOI18N
        JLabel loadingLabel = new JLabel(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.loading"));
        loadingLabel.setHorizontalAlignment(JLabel.CENTER);
        scrollPaneOptions.setViewportView(loadingLabel);
        Thread loadingThread = new Thread("Export/import options loading") {  //NOI18N

            @Override
            public void run() {
                OptionsExportModel model = getOptionsExportModel();
                LOGGER.fine("Loading started: " + getOptionsExportModel());  //NOI18N
                final TreeModel treeModel = createOptionsTreeModel();
                LOGGER.fine("Loading finished: " + getOptionsExportModel());  //NOI18N
                // change UI only if model not changed in between
                if (model == getOptionsExportModel()) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            LOGGER.fine("Changing options.");
                            scrollPaneOptions.setViewportView(getOutline(treeModel));
                            dialogDescriptor.setValid(isPanelValid());
                        }
                    });
                }
            }
        };
        loadingThread.start();
    }

    /** Returns outline view for displaying options for export/import. */
    private Outline getOutline(TreeModel treeModel) {
        Outline outline = new Outline();
        outline.setModel(DefaultOutlineModel.createOutlineModel(
                treeModel,
                new OptionsRowModel(),
                true,
                NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.outline.header.tree")));
        outline.setRenderDataProvider(new OptionsTreeDataProvider());
        //outline.setRootVisible(false);
        outline.getTableHeader().setReorderingAllowed(false);
        outline.setColumnHidingAllowed(false);
        // a11y
        outline.getAccessibleContext().setAccessibleName(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.outline.AN"));
        outline.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.outline.AD"));
        lblHint.setLabelFor(outline);
        return outline;
    }

    /** Returns tree model based on current state of OptionsExportModel. Sets treeModel field. */
    private TreeModel createOptionsTreeModel() {
        LOGGER.fine("getOptionsTreeModel - " + getOptionsExportModel());  //NOI18N
        String allLabel = NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.outline.all");
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(allLabel);
        for (OptionsExportModel.Category category : getOptionsExportModel().getCategories()) {
            LOGGER.fine("category=" + category);  //NOI18N
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
            List<OptionsExportModel.Item> items = category.getItems();
            for (OptionsExportModel.Item item : items) {
                LOGGER.fine("    item=" + item);  //NOI18N
                if (panelType == PanelType.EXPORT || item.isApplicable()) {
                    // do not show not applicable items for import
                    categoryNode.add(new DefaultMutableTreeNode(item));
                }
            }
            if (categoryNode.getChildCount() != 0) {
                // do not show category node if it has no children
                rootNode.add(categoryNode);
                updateCategoryNode(categoryNode);
            }
        }
        if (rootNode.getChildCount() == 0) {
            rootNode = null;
        }
        treeModel = new DefaultTreeModel(rootNode);
        return treeModel;
    }

    private String getSelectedFilePath() {
        return txtFile.getText();
    }

    private void setDialogDescriptor(DialogDescriptor dd) {
        this.dialogDescriptor = dd;
    }

    /** Returns true if all user inputs in this panel are valid. */
    private boolean isPanelValid() {
        if (panelType == PanelType.IMPORT) {
            if (txtFile.getText().length() == 0) {
                dialogDescriptor.getNotificationLineSupport().setWarningMessage(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.file.warning"));
            } else if (getOptionsExportModel().getState() == OptionsExportModel.State.DISABLED) {
                dialogDescriptor.getNotificationLineSupport().setWarningMessage(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.nooption.warning"));
            } else {
                dialogDescriptor.getNotificationLineSupport().clearMessages();
                return true;
            }
        } else {
            if (txtFile.getText().length() == 0 || !txtFile.getText().endsWith(".zip")) {  //NOI18N
                dialogDescriptor.getNotificationLineSupport().setWarningMessage(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.file.warning"));
            } else if (getOptionsExportModel().getState() == OptionsExportModel.State.DISABLED) {
                dialogDescriptor.getNotificationLineSupport().setWarningMessage(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.nooption.warning"));
            } else {
                dialogDescriptor.getNotificationLineSupport().clearMessages();
                return true;
            }
        }
        return false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblHint = new javax.swing.JLabel();
        scrollPaneOptions = new javax.swing.JScrollPane();
        lblFile = new javax.swing.JLabel();
        txtFile = new javax.swing.JTextField();
        btnBrowse = new javax.swing.JButton();

        lblHint.setText(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.lblHint.text")); // NOI18N

        lblFile.setLabelFor(txtFile);
        lblFile.setText(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.lblFile.text")); // NOI18N

        btnBrowse.setText(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.btnBrowse")); // NOI18N
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scrollPaneOptions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lblFile)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(txtFile, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnBrowse))
                    .add(lblHint, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblFile)
                    .add(txtFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnBrowse))
                .add(18, 18, 18)
                .add(lblHint)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrollPaneOptions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                .addContainerGap())
        );

        txtFile.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.txtFile.AD")); // NOI18N
        btnBrowse.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.btnBrowse.AN")); // NOI18N
        btnBrowse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.btnBrowse.AD")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.AD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseActionPerformed
        FileChooserBuilder fileChooserBuilder = new FileChooserBuilder(OptionsChooserPanel.class);
        fileChooserBuilder.setDefaultWorkingDirectory(new File(System.getProperty("user.home")));  //NOI18N
        fileChooserBuilder.setFileFilter(new FileNameExtensionFilter("*.zip", "zip"));  //NOI18N
        String approveText = NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.file.chooser.approve");
        fileChooserBuilder.setApproveText(approveText);
        if (panelType == PanelType.IMPORT) {
            fileChooserBuilder.setTitle(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.file.chooser.title"));
            File selectedFile = fileChooserBuilder.showOpenDialog();
            if (selectedFile != null) {
                if (selectedFile.isDirectory() && !new File(selectedFile, "config").exists()) {  //NOI18N
                    // #163142 - ask for confirmation when selected folder doesn't seem to be a valid userdir
                    String message = NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.invalid.userdir", selectedFile);
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                            message,
                            NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.import.invalid.userdir.title"),
                            NotifyDescriptor.YES_NO_OPTION);
                    DialogDisplayer.getDefault().notify(nd);
                    if (!NotifyDescriptor.YES_OPTION.equals(nd.getValue())) {
                        return;
                    }
                }
                txtFile.setText(selectedFile.getAbsolutePath());
                setOptionsExportModel(new OptionsExportModel(selectedFile));
                loadOptions();
            }
        } else {
            fileChooserBuilder.setTitle(NbBundle.getMessage(OptionsChooserPanel.class, "OptionsChooserPanel.file.chooser.title"));
            JFileChooser fileChooser = fileChooserBuilder.createFileChooser();
            if (JFileChooser.APPROVE_OPTION == fileChooser.showDialog(this, approveText)) {
                String selectedFileName = fileChooser.getSelectedFile().getAbsolutePath();
                if (!selectedFileName.endsWith(".zip")) {  //NOI18N
                    selectedFileName += ".zip";  //NOI18N
                }
                txtFile.setText(selectedFileName);
                dialogDescriptor.setValid(isPanelValid());
            }
        }
    }//GEN-LAST:event_btnBrowseActionPerformed


    /** Defines presentation of table. */
    private class OptionsRowModel implements RowModel {

        public Class getColumnClass(int column) {
            return null;
        }

        public int getColumnCount() {
            return 0;
        }

        public String getColumnName(int column) {
            return null;
        }

        public Object getValueFor(Object node, int column) {
            return null;
        }

        public boolean isCellEditable(Object node, int column) {
            return false;
        }

        public void setValueFor(Object node, int column, Object value) {
        }
    }

    /** Update state of category node according to state of sub items. */
    private static void updateCategoryNode(DefaultMutableTreeNode categoryNode) {
        int enabledCount = 0;
        int applicableCount = 0;
        for (int i = 0; i < categoryNode.getChildCount(); i++) {
            Object userObject = ((DefaultMutableTreeNode) categoryNode.getChildAt(i)).getUserObject();
            OptionsExportModel.Item item = (OptionsExportModel.Item) userObject;
            if (item.isApplicable()) {
                applicableCount++;
                if (item.isEnabled()) {
                    enabledCount++;
                }
            }
        }
        Object userObject = categoryNode.getUserObject();
        OptionsExportModel.Category category = ((OptionsExportModel.Category) userObject);
        if (enabledCount == 0) {
            category.setState(OptionsExportModel.State.DISABLED);
        } else if (enabledCount == applicableCount) {
            category.setState(OptionsExportModel.State.ENABLED);
        } else {
            category.setState(OptionsExportModel.State.PARTIAL);
        }
    }

    /** Defines visual appearance of tree. */
    private class OptionsTreeDataProvider implements CheckRenderDataProvider {

        public Color getBackground(Object node) {
            return null;
        }

        public String getDisplayName(Object node) {
            if (node == null) {
                return null;
            }
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            if (userObject instanceof OptionsExportModel.Category) {
                return ((OptionsExportModel.Category) userObject).getDisplayName();
            }
            if (userObject instanceof OptionsExportModel.Item) {
                return ((OptionsExportModel.Item) userObject).getDisplayName();
            }
            // root node
            return node.toString();
        }

        /** Return like disabled color for not applicable items. */
        public Color getForeground(Object node) {
            if (node == null) {
                return null;
            }
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            if (userObject instanceof OptionsExportModel.Category) {
                if (!((OptionsExportModel.Category) userObject).isApplicable()) {
                    return DISABLED_COLOR;
                }
            } else if (userObject instanceof OptionsExportModel.Item) {
                if (!((OptionsExportModel.Item) userObject).isApplicable()) {
                    return DISABLED_COLOR;
                }
            }
            return null;
        }

        public Icon getIcon(Object o) {
            return NODE_ICON;
        }

        public String getTooltipText(Object o) {
            return null;
        }

        public boolean isHtmlDisplayName(Object o) {
            return false;
        }

        public boolean isCheckable(Object node) {
            return true;
        }

        /** Disabled for not applicable items. */
        public boolean isCheckEnabled(Object node) {
            if (node == null) {
                return true;
            }
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            if (userObject instanceof OptionsExportModel.Category) {
                if (!((OptionsExportModel.Category) userObject).isApplicable()) {
                    return false;
                }
            } else if (userObject instanceof OptionsExportModel.Item) {
                if (!((OptionsExportModel.Item) userObject).isApplicable()) {
                    return false;
                }
            }
            return true;
        }

        public Boolean isSelected(Object node) {
            if (node == null) {
                return false;
            }
            if (((DefaultMutableTreeNode) node).isRoot()) {
                return getOptionsExportModel().getState().toBoolean();
            }
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            if (userObject instanceof OptionsExportModel.Category) {
                return ((OptionsExportModel.Category) userObject).getState().toBoolean();
            } else if (userObject instanceof OptionsExportModel.Item) {
                return ((OptionsExportModel.Item) userObject).isEnabled();
            }
            // should not happen
            assert false : "Node not recognized " + node;  //NOI18N
            return false;
        }

        public void setSelected(Object node, Boolean selected) {
            Object userObject = ((DefaultMutableTreeNode) node).getUserObject();
            if (((DefaultMutableTreeNode) node).isRoot()) {
                getOptionsExportModel().setState(OptionsExportModel.State.valueOf(selected));
            } else if (userObject instanceof OptionsExportModel.Category) {
                ((OptionsExportModel.Category) userObject).setState(OptionsExportModel.State.valueOf(selected));
            } else if (userObject instanceof OptionsExportModel.Item) {
                ((OptionsExportModel.Item) userObject).setEnabled(selected);
                // update parent category
                Object parent = ((TreeNode) node).getParent();
                updateCategoryNode((DefaultMutableTreeNode) parent);
            }
            // fire an event to refresh parent or child nodes
            ((DefaultTreeModel) treeModel).nodeChanged((TreeNode) node);
            dialogDescriptor.setValid(isPanelValid());
        }
    }

    /** FileFile for single extension. Remove it when JDK5 is obsolete and
     * use FileNameExtensionFilter from JDK6. */
    private static class FileNameExtensionFilter extends FileFilter {

        private final String description;
        private final String lowerCaseExtension;

        public FileNameExtensionFilter(String description, String extension) {
            assert extension != null;
            this.description = description;
            this.lowerCaseExtension = extension.toLowerCase();
        }

        @Override
        public boolean accept(File f) {
            if (f != null) {
                if (f.isDirectory()) {
                    return true;
                }
                String fileName = f.getName();
                int i = fileName.lastIndexOf('.');
                if (i > 0 && i < fileName.length() - 1) {
                    String desiredExtension = fileName.substring(i + 1).toLowerCase();
                    if (desiredExtension.equals(lowerCaseExtension)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.JLabel lblFile;
    private javax.swing.JLabel lblHint;
    private javax.swing.JScrollPane scrollPaneOptions;
    private javax.swing.JTextField txtFile;
    // End of variables declaration//GEN-END:variables
}
