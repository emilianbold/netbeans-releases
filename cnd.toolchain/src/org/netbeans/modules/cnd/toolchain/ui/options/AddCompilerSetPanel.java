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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.netbeans.modules.remote.api.ui.FileChooserBuilder;
import org.openide.DialogDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  thp
 */
/*package-local*/ final class AddCompilerSetPanel extends javax.swing.JPanel implements DocumentListener, Runnable {

    private DialogDescriptor dialogDescriptor = null;
    private final CompilerSetManagerImpl csm;
    private final boolean local;

    private final Object lastFoundLock = new Object();
    private CompilerSet lastFoundRemoteCompilerSet;

    private final Object compilerCheckExecutorLock = new Object();
    private final ScheduledExecutorService compilerCheckExecutor;
    private ScheduledFuture<?> compilerCheckTask;

    private final Color defaultLbErrColor;

    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N

    /** Creates new form AddCompilerSetPanel */
    public AddCompilerSetPanel(CompilerSetManager csm) {
        initComponents();
        defaultLbErrColor = lbError.getForeground();
        this.csm = (CompilerSetManagerImpl) csm;
        this.local = ((CompilerSetManagerImpl)csm).getExecutionEnvironment().isLocal();

        List<CompilerFlavor> list = CompilerFlavorImpl.getFlavors(csm.getPlatform());
        for (CompilerFlavor cf : list) {
            cbFamily.addItem(cf);
        }
        // add unknown as well
        cbFamily.addItem(CompilerFlavor.getUnknown(csm.getPlatform()));
        tfName.setText(""); // NOI18N
        validateData();

        tfBaseDirectory.getDocument().addDocumentListener(AddCompilerSetPanel.this);
        tfName.getDocument().addDocumentListener(AddCompilerSetPanel.this);

        compilerCheckExecutor = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                return compilerCheckExecutor.shutdownNow();
            }
        });
    }

    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
        dialogDescriptor.setValid(false);
    }

    private void handleBaseDirUpdate() {
        if (this.dialogDescriptor != null) {
            this.dialogDescriptor.setValid(false);
        }
        lastFoundRemoteCompilerSet = null;
        final String path = tfBaseDirectory.getText().trim();
        if (path.length() > 0) {
            //go to non UI thread
            synchronized (compilerCheckExecutorLock) {
                if (compilerCheckTask != null) {
                    log.log(Level.FINEST, "Cancelling check for {0}", path);
                    compilerCheckTask.cancel(true);
                }
                log.log(Level.FINEST, "Submitting check for {0}", path);
                compilerCheckTask = compilerCheckExecutor.schedule(this,
                        local ? 500 : 1000, TimeUnit.MILLISECONDS);
            }
        } else {
            showError(NbBundle.getMessage(getClass(), "BASE_EMPTY"));
        }
    }

    /** check data dir */
    @Override
    public void run() {
        final String path = tfBaseDirectory.getText().trim();
        log.log(Level.FINEST, "Running check for {0}", path);
        showStatus(NbBundle.getMessage(getClass(), "CHECK_IN_PROGRESS", path));
        long time = System.currentTimeMillis();
        if (path.length() == 0) {
            log.log(Level.FINEST, "Done check for {0} - the path is empty", path);
            showError(NbBundle.getMessage(getClass(), "BASE_EMPTY"));
            return;
        }
        if (local) {
            final List<CompilerFlavor> flavors = CompilerSetFactory.getCompilerSetFlavor(new File(path).getAbsolutePath(), csm.getPlatform());
            if (flavors.size() > 0) {
                String baseDirectory = getBaseDirectory();
                String compilerSetName = getCompilerSetName().trim();
                CompilerSet cs = CompilerSetFactory.getCustomCompilerSet(new File(baseDirectory).getAbsolutePath(), flavors.get(0), compilerSetName);
                ((CompilerSetManagerImpl)CompilerSetManager.get(ExecutionEnvironmentFactory.getLocal())).initCompilerSet(cs);
                synchronized (lastFoundLock) {
                    lastFoundRemoteCompilerSet = cs;
                }
            }
        } else {
            if (!checkConnection()) {
                log.log(Level.FINEST, "Done check for {0} - no connection to host", path);
                showError(NbBundle.getMessage(getClass(), "CANNOT_CONNECT"));
                return;
            }
            final List<CompilerSet> css = csm.findRemoteCompilerSets(path);
            //check if we are not shutdowned already
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                log.log(Level.FINEST, "Interrupted (1) check for {0}", path);
            }
            if (Thread.interrupted()) {
                log.log(Level.FINEST, "Interrupted (2) check for {0}", path);
                showError(NbBundle.getMessage(getClass(), "CANCELLED"));
                return;
            }
            if (css.size() > 0) {
                synchronized (lastFoundLock) {
                    lastFoundRemoteCompilerSet = css.get(0);
                }
            }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showStatus(""); //NOI18N
                if (lastFoundRemoteCompilerSet != null) {
                    cbFamily.setSelectedItem(lastFoundRemoteCompilerSet.getCompilerFlavor());
                } else {
                    cbFamily.setSelectedItem(CompilerFlavor.getUnknown(csm.getPlatform()));
                }
                updateDataFamily();
                if (!dialogDescriptor.isValid()) {
                    tfName.setText("");
                }
            }
        });
        if (log.isLoggable(Level.FINEST)) {
            time = System.currentTimeMillis() - time;
            log.log(Level.FINEST, "Done check for {0}; check took {1} ms", new Object[] { path, time });
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
        final String path = tfBaseDirectory.getText().trim();
        showStatus(""); // NOI18N
        synchronized (lastFoundLock) {
            if (lastFoundRemoteCompilerSet == null) {
                valid = false;
                showError(NbBundle.getMessage(getClass(), path.length() == 0 ? "BASE_EMPTY" : "REMOTEBASE_INVALID", path));
            }
        }
        cbFamily.setEnabled(valid);
        tfName.setEnabled(valid);

        String compilerSetName = ToolUtils.replaceOddCharacters(tfName.getText().trim(), '_');
        if (valid) {
            if (compilerSetName.length() == 0) { // NOI18N
                valid = false;
                showError(NbBundle.getMessage(getClass(),"NAME_EMPTY"));
            }
            else if (compilerSetName.contains("|")) { // NOI18N
                valid = false;
                showError(NbBundle.getMessage(getClass(),"NAME_INVALID", compilerSetName));
            }
        }

        if (valid && csm.getCompilerSet(compilerSetName.trim()) != null) {
            valid = false;
            showError(NbBundle.getMessage(getClass(),"TOOLNAME_ALREADY_EXISTS", compilerSetName));
        }

        if (dialogDescriptor != null) {
            dialogDescriptor.setValid(valid);
        }
    }

    private void showError(String message) {
        lbError.setForeground(Color.RED);
        lbError.setText(message);
    }

    private void showStatus(String message) {
        lbError.setForeground(defaultLbErrColor);
        lbError.setText(message);
    }

    private void handleUpdate(DocumentEvent e) {
        if (e.getDocument() == tfBaseDirectory.getDocument()) {
            handleBaseDirUpdate();
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
        synchronized (lastFoundLock) {
            if (lastFoundRemoteCompilerSet != null){
                ((CompilerSetImpl)lastFoundRemoteCompilerSet).setName(compilerSetName);
                return lastFoundRemoteCompilerSet;
            }
            return null;
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

        infoLabel = new javax.swing.JLabel();
        lbBaseDirectory = new javax.swing.JLabel();
        tfName = new javax.swing.JTextField();
        btBaseDirectory = new javax.swing.JButton();
        lbFamily = new javax.swing.JLabel();
        cbFamily = new javax.swing.JComboBox();
        lbName = new javax.swing.JLabel();
        tfBaseDirectory = new javax.swing.JTextField();
        lbError = new javax.swing.JLabel();

        infoLabel.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.taInfo.text")); // NOI18N

        lbBaseDirectory.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle").getString("lbBaseDirectory_MN").charAt(0));
        lbBaseDirectory.setLabelFor(tfBaseDirectory);
        lbBaseDirectory.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.lbBaseDirectory.text")); // NOI18N

        tfName.setColumns(20);

        btBaseDirectory.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle").getString("btBrowse").charAt(0));
        btBaseDirectory.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.btBaseDirectory.text")); // NOI18N
        btBaseDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btBaseDirectoryActionPerformed(evt);
            }
        });

        lbFamily.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle").getString("lbFamily_MN").charAt(0));
        lbFamily.setLabelFor(cbFamily);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle"); // NOI18N
        lbFamily.setText(bundle.getString("AddCompilerSetPanel.lbFamily.text")); // NOI18N
        lbFamily.setToolTipText(bundle.getString("AddCompilerSetPanel.lbFamily.toolTipText")); // NOI18N

        cbFamily.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbFamilyActionPerformed(evt);
            }
        });

        lbName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/toolchain/ui/options/Bundle").getString("lbToolSetName_MN").charAt(0));
        lbName.setLabelFor(tfName);
        lbName.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.lbName.text")); // NOI18N

        tfBaseDirectory.setColumns(40);

        lbError.setText(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.lbError.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(lbBaseDirectory))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lbFamily))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lbName)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tfBaseDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btBaseDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cbFamily, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(infoLabel)
                .addGap(120, 120, 120))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbError, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoLabel)
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbBaseDirectory)
                    .addComponent(btBaseDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfBaseDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbFamily)
                    .addComponent(cbFamily, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(lbError)
                .addContainerGap())
        );

        tfName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.tfName.AccessibleContext.accessibleDescription")); // NOI18N
        btBaseDirectory.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.btBaseDirectory.AccessibleContext.accessibleDescription")); // NOI18N
        tfBaseDirectory.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.tfBaseDirectory.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddCompilerSetPanel.class, "AddCompilerSetPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void btBaseDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btBaseDirectoryActionPerformed
    String seed = null;
    if (tfBaseDirectory.getText().length() > 0) {
        seed = tfBaseDirectory.getText();
    } else if (FileChooser.getCurrectChooserFile() != null) {
        seed = FileChooser.getCurrectChooserFile().getPath();
    } else {
        seed = System.getProperty("user.home"); // NOI18N
    }
    JFileChooser fileChooser = new FileChooserBuilder(csm.getExecutionEnvironment()).createFileChooser(seed);
    fileChooser.setDialogTitle(NbBundle.getMessage(getClass(), "SELECT_BASE_DIRECTORY_TITLE"));
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int ret = fileChooser.showOpenDialog(this);
    if (ret == JFileChooser.CANCEL_OPTION) {
        return;
    }
    String dirPath = fileChooser.getSelectedFile().getPath();
    tfBaseDirectory.setText(dirPath);
    //updateDataBaseDir();
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
