/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.toolchain.ui.options;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.spi.toolchain.CompilerSetFactory;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetImpl;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerFlavorImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolUtils;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.DialogDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  thp
 */
/*package-local*/ final class AddCompilerSetPanel extends javax.swing.JPanel implements DocumentListener {

    private DialogDescriptor dialogDescriptor = null;
    private final CompilerSetManagerImpl csm;
    private final boolean local;
    private final Object lock = new Object();
    private final Object remoteCompilerCheckExecutorLock = new Object();
    private ExecutorService remoteCompilerCheckExecutor;

    /** Creates new form AddCompilerSetPanel */
    public AddCompilerSetPanel(CompilerSetManager csm) {
        initComponents();
        this.csm = (CompilerSetManagerImpl) csm;
        this.local = ((CompilerSetManagerImpl)csm).getExecutionEnvironment().isLocal();

        if (!local) {
            // we can't have Browse button for remote, so we use it to validate path on remote host
            btBaseDirectory.setText(getString("AddCompilerSetPanel.btBaseDirectoryRemoteMode.text"));
            btBaseDirectory.setMnemonic(0);
        }

        List<CompilerFlavor> list = CompilerFlavorImpl.getFlavors(csm.getPlatform());
        for (CompilerFlavor cf : list) {
            cbFamily.addItem(cf);
        }
        // add unknown as well
        cbFamily.addItem(CompilerFlavor.getUnknown(csm.getPlatform()));
        tfName.setText(""); // NOI18N
        validateData();

        setPreferredSize(new Dimension(800, 300));

        tfBaseDirectory.getDocument().addDocumentListener(AddCompilerSetPanel.this);
        tfName.getDocument().addDocumentListener(AddCompilerSetPanel.this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        synchronized (remoteCompilerCheckExecutorLock) {
            if (remoteCompilerCheckExecutor != null) {
                AccessController.doPrivileged(new PrivilegedAction<Object>() {

                    @Override
                    public Object run() {
                        return remoteCompilerCheckExecutor.shutdownNow();
                    }
                });
            }
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(AddCompilerSetPanel.class, key);
    }

    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
        dialogDescriptor.setValid(false);
    }
    private CompilerSet lastFoundRemoteCompilerSet;

    private void updateDataBaseDir() {
        if (local) {
            //This will be invoked in UI thread
            File dirFile = new File(tfBaseDirectory.getText());
            List<CompilerFlavor> flavors = CompilerSetFactory.getCompilerSetFlavor(dirFile.getAbsolutePath(), csm.getPlatform());
            if (flavors.size() > 0) {
                cbFamily.setSelectedItem(flavors.get(0));
            } else {
                cbFamily.setSelectedItem(CompilerFlavor.getUnknown(csm.getPlatform()));
            }
            updateDataFamily();
            if (!dialogDescriptor.isValid()) {
                tfName.setText("");
            }
        } else {
            synchronized (lock) {
                lastFoundRemoteCompilerSet = null;
            }
            final String path = tfBaseDirectory.getText().trim();
            if (path.length() > 0) {
                tfBaseDirectory.setEnabled(false);
                btBaseDirectory.setEnabled(false);
                final Runnable enabler = new Runnable() {
                    @Override
                    public void run() {
                        tfBaseDirectory.setEnabled(true);
                        btBaseDirectory.setEnabled(true);
                    }
                };
                //go to non UI thread
                synchronized (remoteCompilerCheckExecutorLock) {
                    if (remoteCompilerCheckExecutor != null) {
                        AccessController.doPrivileged(new PrivilegedAction<Object>() {

                            @Override
                            public Object run() {
                                return remoteCompilerCheckExecutor.shutdownNow();
                            }
                        });
                        remoteCompilerCheckExecutor = null;
                    }
                    remoteCompilerCheckExecutor = Executors.newSingleThreadExecutor();
                }
                remoteCompilerCheckExecutor.submit(new Runnable() {

                    @Override
                    public void run() {
                        if (!checkConnection()) {
                            SwingUtilities.invokeLater(enabler);
                            return;
                        }
                        final List<CompilerSet> css = csm.findRemoteCompilerSets(path);
                        //check if we are not shutdowned already
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                        }
                        if (Thread.interrupted()) {
                            return;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                enabler.run();
                                if (css.size() > 0) {
                                    cbFamily.setSelectedItem(css.get(0).getCompilerFlavor());
                                    synchronized (AddCompilerSetPanel.this.lock) {
                                        lastFoundRemoteCompilerSet = css.get(0);
                                    }
                                    updateDataFamily();
                                    if (!dialogDescriptor.isValid()) {
                                        tfName.setText("");
                                    }
                                }
                            }
                        });
                    }
                });

            }
        }

    }

    private boolean checkConnection() {
        if (ConnectionManager.getInstance().isConnectedTo(csm.getExecutionEnvironment())) {
            return true;
        } else {
            try {
                ConnectionManager.getInstance().connectTo(csm.getExecutionEnvironment());
                return true;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            } catch (CancellationException ex) {
                return false;
            }
        }
    }

    private void updateDataFamily() {
        CompilerFlavor flavor = (CompilerFlavor) cbFamily.getSelectedItem();
        int n = 0;
        String suggestedName = null;
        while (true) {
            suggestedName = flavor.toString() + (n > 0 ? ("_" + n) : ""); // NOI18N
            if (csm.getCompilerSet(suggestedName) != null) {
                n++;
            } else {
                break;
            }
        }
        tfName.setText(suggestedName);

        validateData();
    }

    private void validateData() {
        boolean valid = true;
        lbError.setText(""); // NOI18N

        if (local) {
            File dirFile = new File(tfBaseDirectory.getText());
            if (valid && !dirFile.exists() || !dirFile.isDirectory() || !ToolUtils.isPathAbsolute(dirFile.getPath())) {
                valid = false;
                lbError.setText(getString("BASE_INVALID"));
            }
        } else {
            synchronized (lock) {
                if (lastFoundRemoteCompilerSet == null) {
                    valid = false;
                    lbError.setText(getString("REMOTEBASE_INVALID"));
                }
            }
        }

        cbFamily.setEnabled(valid);
        tfName.setEnabled(valid);

        String compilerSetName = ToolUtils.replaceOddCharacters(tfName.getText().trim(), '_');
        if (valid && compilerSetName.length() == 0 || compilerSetName.contains("|")) { // NOI18N
            valid = false;
            lbError.setText(getString("NAME_INVALID"));
        }

        if (valid && csm.getCompilerSet(compilerSetName.trim()) != null) {
            valid = false;
            lbError.setText(getString("TOOLNAME_ALREADY_EXISTS"));
        }

        if (dialogDescriptor != null) {
            dialogDescriptor.setValid(valid);
        }
    }

    private void handleUpdate(DocumentEvent e) {
        if (e.getDocument() == tfBaseDirectory.getDocument()) {
            if (local) { // we can't support real-time validation for remote base dir
                updateDataBaseDir();
            }
        } else {
            validateData();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        handleUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        handleUpdate(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        //validateData();
    }

    public String getBaseDirectory() {
        return tfBaseDirectory.getText();
    }

    private CompilerFlavor getFamily() {
        return (CompilerFlavor) cbFamily.getSelectedItem();
    }

    private String getCompilerSetName() {
        return ToolUtils.replaceOddCharacters(tfName.getText().trim(), '_');
    }

    public CompilerSet getCompilerSet() {
        String compilerSetName = getCompilerSetName().trim();
        if (local) {
            String baseDirectory = getBaseDirectory();
            CompilerFlavor flavor = getFamily();
            CompilerSet cs = CompilerSetFactory.getCustomCompilerSet(new File(baseDirectory).getAbsolutePath(), flavor, compilerSetName);
            ((CompilerSetManagerImpl)CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal())).initCompilerSet(cs);
            return cs;
        } else {
            synchronized (lock) {
                if (lastFoundRemoteCompilerSet != null){
                    ((CompilerSetImpl)lastFoundRemoteCompilerSet).setName(compilerSetName);
                    return lastFoundRemoteCompilerSet;
                }else{
                    return lastFoundRemoteCompilerSet;
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        infoLabel = new javax.swing.JLabel();
        lbBaseDirectory = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        btBaseDirectory = new javax.swing.JButton();
        lbFamily = new javax.swing.JLabel();
        cbFamily = new javax.swing.JComboBox();
        lbName = new javax.swing.JLabel();
        tfBaseDirectory = new javax.swing.JTextField();
        lbError = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        infoLabel.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.taInfo.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 16, 0, 16);
        add(infoLabel, gridBagConstraints);

        lbBaseDirectory.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle").getString("lbBaseDirectory_MN").charAt(0));
        lbBaseDirectory.setLabelFor(tfBaseDirectory);
        lbBaseDirectory.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.lbBaseDirectory.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 16, 0, 0);
        add(lbBaseDirectory, gridBagConstraints);

        tfName.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 16, 0);
        add(tfName, gridBagConstraints);
        tfName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.tfName.AccessibleContext.accessibleDescription")); // NOI18N

        btBaseDirectory.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle").getString("btBrowse").charAt(0));
        btBaseDirectory.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.btBaseDirectory.text")); // NOI18N
        btBaseDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBaseDirectoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 4, 0, 16);
        add(btBaseDirectory, gridBagConstraints);
        btBaseDirectory.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.btBaseDirectory.AccessibleContext.accessibleDescription")); // NOI18N

        lbFamily.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle").getString("lbFamily_MN").charAt(0));
        lbFamily.setLabelFor(cbFamily);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle"); // NOI18N
        lbFamily.setText(bundle.getString("AddCompilerSetPanel.lbFamily.text")); // NOI18N
        lbFamily.setToolTipText(bundle.getString("AddCompilerSetPanel.lbFamily.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 0);
        add(lbFamily, gridBagConstraints);

        cbFamily.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbFamilyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 0);
        add(cbFamily, gridBagConstraints);

        lbName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle").getString("lbToolSetName_MN").charAt(0));
        lbName.setLabelFor(tfName);
        lbName.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.lbName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(9, 16, 16, 0);
        add(lbName, gridBagConstraints);

        tfBaseDirectory.setColumns(40);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(16, 4, 0, 0);
        add(tfBaseDirectory, gridBagConstraints);
        tfBaseDirectory.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.tfBaseDirectory.AccessibleContext.accessibleDescription")); // NOI18N

        lbError.setForeground(new java.awt.Color(255, 51, 51));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 16, 16);
        add(lbError, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void btBaseDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBaseDirectoryActionPerformed
    if (local) {
        String seed = null;
        if (tfBaseDirectory.getText().length() > 0) {
            seed = tfBaseDirectory.getText();
        } else if (FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        FileChooser fileChooser = new FileChooser(getString("SELECT_BASE_DIRECTORY_TITLE"), null, JFileChooser.DIRECTORIES_ONLY, null, seed, true);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String dirPath = fileChooser.getSelectedFile().getPath();
        tfBaseDirectory.setText(dirPath);

    }
    updateDataBaseDir();
}//GEN-LAST:event_btBaseDirectoryActionPerformed

private void cbFamilyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbFamilyActionPerformed
    updateDataFamily();
}//GEN-LAST:event_cbFamilyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btBaseDirectory;
    private javax.swing.JComboBox cbFamily;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel lbBaseDirectory;
    private javax.swing.JLabel lbError;
    private javax.swing.JLabel lbFamily;
    private javax.swing.JLabel lbName;
    private javax.swing.JTextField tfBaseDirectory;
    private javax.swing.JTextField tfName;
    // End of variables declaration//GEN-END:variables
}
