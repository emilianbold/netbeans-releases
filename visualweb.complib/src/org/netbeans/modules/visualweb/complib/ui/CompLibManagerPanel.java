/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
/*
 * CompLibManagerPanel.java
 *
 * Created on April 21, 2005, 8:35 AM
 */

package org.netbeans.modules.visualweb.complib.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.MissingResourceException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.visualweb.complib.BuiltInComplib;
import org.netbeans.modules.visualweb.complib.Complib;
import org.netbeans.modules.visualweb.complib.ComplibServiceProvider;
import org.netbeans.modules.visualweb.complib.ExtensionComplib;
import org.netbeans.modules.visualweb.complib.IdeUtil;
import org.openide.awt.Mnemonics;

/**
 *
 * @author jhoff
 * @author Edwin Goei
 */
public class CompLibManagerPanel extends javax.swing.JPanel {

    private static final ChildUserObject DESIGN_TIME_UO = new ChildUserObject(
            "manager.designTimeNode");

    private static final ChildUserObject RUNTIME_UO = new ChildUserObject(
            "manager.runtimeNode");

    private static final ChildUserObject JAVADOC_UO = new ChildUserObject(
            "manager.javadocNode");

    private static final ChildUserObject SOURCE_UO = new ChildUserObject(
            "manager.sourceNode");

    private static final ChildUserObject WEB_RESOURCES_UO = new ChildUserObject(
            "manager.webResourcesNode");

    private static final ChildUserObject HELP_UO = new ChildUserObject(
            "manager.helpNode");

    private static final ComplibServiceProvider csp = ComplibServiceProvider
            .getInstance();

    /**
     * User object for main complib node that returns a localized string
     *
     * @author Edwin Goei
     */
    private static class CompLibUserObject {
        private Complib compLib;

        private CompLibUserObject(Complib compLib) {
            this.compLib = compLib;
        }

        public Complib getComponentLibrary() {
            return compLib;
        }

        public String toString() {
            return compLib.getVersionedTitle();
        }
    }

    /**
     * User object for child nodes that returns a localized string
     *
     * @author Edwin Goei
     */
    private static class ChildUserObject {
        private String key;

        private ChildUserObject(String key) {
            this.key = key;
        }

        public String toString() {
            try {
                return NbBundle.getMessage(CompLibManagerPanel.class, key);
            } catch (MissingResourceException e) {
                // Fail gracefully
                IdeUtil.logWarning(e);
                return key;
            }
        }
    }

    public static class TreeCellRenderer extends DefaultTreeCellRenderer {

        private static ImageIcon LIBRARY_ICON = new ImageIcon(
                TreeCellRenderer.class.getResource("images/libraries.png"));

        private static ImageIcon HELP_ICON = new ImageIcon(
                TreeCellRenderer.class.getResource("images/help.gif"));

        private static ImageIcon JAVADOC_ICON = new ImageIcon(
                TreeCellRenderer.class.getResource("images/JavaDoc.gif"));

        private static ImageIcon DESIGN_TIME_ICON = new ImageIcon(
                TreeCellRenderer.class.getResource("images/library.png"));

        private static ImageIcon RUNTIME_ICON = new ImageIcon(
                TreeCellRenderer.class.getResource("images/library.png"));

        private static ImageIcon JAR_ICON = new ImageIcon(
                TreeCellRenderer.class.getResource("images/jar.gif"));

        private static ImageIcon WEB_RESOURCES_ICON = new ImageIcon(
                TreeCellRenderer.class.getResource("images/library.gif"));

        public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
            // Render selection
            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf, row, hasFocus);

            // By default assume that a top level complib node is selected
            setIcon(LIBRARY_ICON);

            Object userObject = ((DefaultMutableTreeNode) value)
                    .getUserObject();
            if (userObject instanceof ChildUserObject) {
                // Second level node is selected
                if (userObject == DESIGN_TIME_UO) {
                    setIcon(DESIGN_TIME_ICON);
                } else if (userObject == RUNTIME_UO) {
                    setIcon(RUNTIME_ICON);
                } else if (userObject == JAVADOC_UO) {
                    setIcon(JAVADOC_ICON);
                } else if (userObject == SOURCE_UO) {
                    setIcon(JAR_ICON);
                } else if (userObject == WEB_RESOURCES_UO) {
                    setIcon(WEB_RESOURCES_ICON);
                } else if (userObject == HELP_UO) {
                    setIcon(HELP_ICON);
                }
            }

            return this;
        }
    }

    /**
     * Creates a new CompLibManagerPanel form
     */
    public CompLibManagerPanel() {
        initComponents();
        compLibDetailPanel.setLayout(new GridBagLayout());
        clGBConstraints = new java.awt.GridBagConstraints();
        clGBConstraints.fill = GridBagConstraints.BOTH;
        clGBConstraints.weightx = 1.0;
        clGBConstraints.weighty = 1.0;

        treeCompLib.setCellRenderer(new TreeCellRenderer());
        treeCompLib.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        initTreeModel(null);

        // Scroll all the way to the left
        JScrollBar hBar = complibListScroll.getHorizontalScrollBar();
        hBar.setValue(hBar.getMinimum());
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        compLibSplit.setDividerLocation(0.5);
    }

    /**
     * Initialize the tree model and also set a selected node if possible.
     * 
     * @param selectedComplib
     *            if not null, select the main complib node to select or if
     *            null, try the select the first node if one exists
     */
    private void initTreeModel(Complib selectedComplib) {
        // Create the TreeModel based on current complibs
        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode(
                "Component Libraries"); // NOI18N
        DefaultTreeModel compLibModel = new DefaultTreeModel(treeRoot);
        treeCompLib.setModel(compLibModel);

        DefaultMutableTreeNode selectedComplibNode = null;
        ArrayList<Complib> complibs = csp.getInstalledComplibs();
        int i = 0;
        for (Complib complib : complibs) {
            // If no selectedComplib specified then select the first one
            if (selectedComplib == null && i == 0) {
                selectedComplib = complib;
            }

            CompLibUserObject cluo = new CompLibUserObject(complib);
            DefaultMutableTreeNode complibNode = new DefaultMutableTreeNode(
                    cluo);

            // Children of the main complib node
            DefaultMutableTreeNode designTimeNode = new DefaultMutableTreeNode(
                    DESIGN_TIME_UO, false);
            DefaultMutableTreeNode runtimeNode = new DefaultMutableTreeNode(
                    RUNTIME_UO, false);
            DefaultMutableTreeNode javadocNode = new DefaultMutableTreeNode(
                    JAVADOC_UO, false);
            DefaultMutableTreeNode sourceNode = new DefaultMutableTreeNode(
                    SOURCE_UO, false);
            DefaultMutableTreeNode webResourceNode = new DefaultMutableTreeNode(
                    WEB_RESOURCES_UO, false);
            DefaultMutableTreeNode helpNode = new DefaultMutableTreeNode(
                    HELP_UO, false);
            // TODO Check w/ JeffH for ordering
            complibNode.add(designTimeNode);
            complibNode.add(runtimeNode);
            complibNode.add(javadocNode);
            complibNode.add(sourceNode);
            complibNode.add(webResourceNode);
            complibNode.add(helpNode);

            // Add this complib node to the tree model
            compLibModel.insertNodeInto(complibNode, treeRoot, i++);

            // Expand this node and make it viewable
            TreeNode[] treeNodePath = runtimeNode.getPath();
            treeCompLib.scrollPathToVisible(new TreePath(treeNodePath));

            // Remember the selected complib node
            if (complib.equals(selectedComplib)) {
                selectedComplibNode = complibNode;
            }
        }

        if (selectedComplibNode != null) {
            treeCompLib.setSelectionPath((new TreePath(selectedComplibNode
                    .getPath())));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        compLibSplit = new javax.swing.JSplitPane();
        compLibDetailPanel = new javax.swing.JPanel();
        compLibListPanel = new javax.swing.JPanel();
        compLibListLabel = new javax.swing.JLabel();
        complibListScroll = new javax.swing.JScrollPane();
        treeCompLib = new javax.swing.JTree();
        buttonPanel = new javax.swing.JPanel();
        btnImport = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(875, 525));
        compLibSplit.setDividerLocation(275);
        compLibDetailPanel.setLayout(new java.awt.GridBagLayout());

        compLibSplit.setRightComponent(compLibDetailPanel);

        compLibListPanel.setLayout(new java.awt.GridBagLayout());

        compLibListLabel.setLabelFor(treeCompLib);
        org.openide.awt.Mnemonics.setLocalizedText(compLibListLabel, org.openide.util.NbBundle.getMessage(CompLibManagerPanel.class, "manager.componentLibrariesLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        compLibListPanel.add(compLibListLabel, gridBagConstraints);
        compLibListLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompLibManagerPanel.class, "manager.componentLibrariesLabelA11yDescription"));

        treeCompLib.setRootVisible(false);
        treeCompLib.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeCompLibValueChanged(evt);
            }
        });

        complibListScroll.setViewportView(treeCompLib);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        compLibListPanel.add(complibListScroll, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        org.openide.awt.Mnemonics.setLocalizedText(btnImport, org.openide.util.NbBundle.getMessage(CompLibManagerPanel.class, "manager.ImportButton"));
        btnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportActionPerformed(evt);
            }
        });

        buttonPanel.add(btnImport);
        btnImport.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompLibManagerPanel.class, "manager.importButtonA11yDescription"));

        org.openide.awt.Mnemonics.setLocalizedText(btnRemove, org.openide.util.NbBundle.getMessage(CompLibManagerPanel.class, "manager.RemoveButton"));
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        buttonPanel.add(btnRemove);
        btnRemove.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompLibManagerPanel.class, "manager.RemoveButtonA11YDescription"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        compLibListPanel.add(buttonPanel, gridBagConstraints);

        compLibSplit.setLeftComponent(compLibListPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(compLibSplit, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnImportActionPerformed
        showImportCompLibDialog();
    }// GEN-LAST:event_btnImportActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnRemoveActionPerformed
        // Find the currently selected complib
        Complib compLib;
        TreePath selectedPath = treeCompLib.getSelectionPath();
        if (selectedPath == null) {
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath
                .getPathComponent(1);
        compLib = getComponentLibraryForNode(node);
        if (!(compLib instanceof ExtensionComplib)) {
            return;
        }
        ExtensionComplib extCompLib = (ExtensionComplib) compLib;

        String inUseProjectNames = csp.getInUseProjectNames(extCompLib);
        if (inUseProjectNames == null) {
            // Confirmation dialog
            String message = NbBundle.getMessage(CompLibManagerPanel.class,
                    "manager.RemoveComplibConfirm");
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message,
                    NotifyDescriptor.OK_CANCEL_OPTION);
            Object result = DialogDisplayer.getDefault().notify(nd);
            if (NotifyDescriptor.OK_OPTION == result) {
                // Delete the currently selected compLib
                csp.remove(extCompLib);

                // Select the previous node in the tree
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
                        .getParent();
                DefaultMutableTreeNode prevChild = (DefaultMutableTreeNode) parent
                        .getChildBefore(node);
                if (prevChild == null) {
                    // No previous node in tree
                    initTreeModel(null);
                } else {
                    initTreeModel(getComponentLibraryForNode(prevChild));
                }
            }
        } else {
            String msg = NbBundle.getMessage(CompLibManagerPanel.class,
                    "manager.RemoveComplibInUse"); // NOI18N
            msg = MessageFormat.format(msg, inUseProjectNames);
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg,
                    NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }// GEN-LAST:event_btnRemoveActionPerformed

    private void treeCompLibValueChanged(
        javax.swing.event.TreeSelectionEvent evt) {// GEN-FIRST:event_treeCompLibValueChanged

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeCompLib
                .getLastSelectedPathComponent();

        if (node == null) {
            // Nothing was selected, remove the current one
            setDetailPanel(null);
            return;
        }

        Object userObject = node.getUserObject();
        if (userObject instanceof CompLibUserObject) {
            // Top level complib node is selected
            CompLibUserObject cluo = (CompLibUserObject) userObject;
            updateRemoveButtonState(cluo.getComponentLibrary());
            setDetailPanel(new MainDetailPanel(cluo.getComponentLibrary()));
        } else {
            // Second level node is selected
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
                    .getParent();
            CompLibUserObject cluo = (CompLibUserObject) parent.getUserObject();
            Complib compLib = cluo.getComponentLibrary();
            updateRemoveButtonState(compLib);

            String labelKey;
            String descriptionKey;
            if (userObject == DESIGN_TIME_UO) {
                labelKey = "manager.DesignTimeLabel";
                descriptionKey = "manager.DesignTimeDescription";
                setDetailPanel(new PathDetailPanel(compLib.getDesignTimePath(),
                        labelKey, descriptionKey));
            } else if (userObject == RUNTIME_UO) {
                labelKey = "manager.RuntimeLabel";
                descriptionKey = "manager.RuntimeDescription";
                setDetailPanel(new PathDetailPanel(compLib.getRuntimePath(),
                        labelKey, descriptionKey));
            } else if (userObject == JAVADOC_UO) {
                labelKey = "manager.JavadocLabel";
                descriptionKey = "manager.JavadocDescription";
                setDetailPanel(new PathDetailPanel(compLib.getJavadocPath(),
                        labelKey, descriptionKey));
            } else if (userObject == SOURCE_UO) {
                labelKey = "manager.SourceLabel";
                descriptionKey = "manager.SourceDescription";
                setDetailPanel(new PathDetailPanel(compLib.getSourcePath(),
                        labelKey, descriptionKey));
            } else if (userObject == WEB_RESOURCES_UO) {
                labelKey = "manager.WebResourceLabel";
                descriptionKey = "manager.WebResourceDescription";
                setDetailPanel(new PathDetailPanel(
                        compLib.getWebResourcePath(), labelKey, descriptionKey));
            } else if (userObject == HELP_UO) {
                setDetailPanel(new HelpSourcesDetailPanel(compLib));
            }
        }
    }// GEN-LAST:event_treeCompLibValueChanged

    private Complib getComponentLibraryForNode(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof CompLibUserObject) {
            // Top level complib node is selected
            CompLibUserObject cluo = (CompLibUserObject) userObject;
            return cluo.getComponentLibrary();
        } else {
            assert userObject instanceof ChildUserObject;
            // Second level node is selected
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
                    .getParent();
            CompLibUserObject cluo = (CompLibUserObject) parent.getUserObject();
            return cluo.getComponentLibrary();
        }
    }

    private synchronized void showImportCompLibDialog() {
        new ImportComplibPanel().showDialog(this);
    }

    synchronized void notifyImportComplete(Complib newCompLib) {
        initTreeModel(newCompLib);
    }

    private void updateRemoveButtonState(Complib compLib) {
        btnRemove.setEnabled(!(compLib instanceof BuiltInComplib));
    }

    /**
     * @param panel
     *            null means don't use a panel and just remove the current one
     */
    private void setDetailPanel(Component panel) {
        // First remove all components
        compLibDetailPanel.removeAll();
        if (panel != null) {
            // Add new panel
            compLibDetailPanel.add(panel, clGBConstraints);
        }

        /*
         * I'm not sure why a call to repaint() does not work here or where this
         * code came from.
         */
        compLibDetailPanel.updateUI();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnRemove;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel compLibDetailPanel;
    private javax.swing.JLabel compLibListLabel;
    private javax.swing.JPanel compLibListPanel;
    private javax.swing.JSplitPane compLibSplit;
    private javax.swing.JScrollPane complibListScroll;
    private javax.swing.JTree treeCompLib;
    // End of variables declaration//GEN-END:variables
    private GridBagConstraints clGBConstraints;

    /**
     * This method is the main entry point for this dialog panel
     * 
     * @param currentCategoryName
     *            to be used in dialog
     */
    public void showDialog() {
        String title = NbBundle.getMessage(CompLibManagerPanel.class,
                "manager.dialogTitle"); // NOI18N
        HelpCtx helpCtx = new HelpCtx(
                "projrave_ui_elements_dialogs_component_library_manager"); // NOI18N
        JButton closeButton = new JButton();
        String msg = org.openide.util.NbBundle.getMessage(
                CompLibManagerPanel.class, "manager.closeButtonLabel");
        Mnemonics.setLocalizedText(closeButton, msg);
        closeButton.getAccessibleContext().setAccessibleDescription(msg);

        Object[] options = new Object[] { closeButton };
        DialogDescriptor descriptor = new DialogDescriptor(this, title, true,
                options, closeButton, DialogDescriptor.DEFAULT_ALIGN, helpCtx,
                null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(CompLibManagerPanel.class,
                        "manager.dialogA11yName"));
        dialog.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(CompLibManagerPanel.class,
                        "manager.dialogA11yDescription"));
        descriptor.setValid(true);

        dialog.setVisible(true);
    }
}
