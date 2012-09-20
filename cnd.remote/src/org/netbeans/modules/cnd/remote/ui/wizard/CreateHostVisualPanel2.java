/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.ui.wizard;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.spi.remote.setup.support.TextComponentWriter;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ValidateablePanel;
import org.netbeans.modules.nativeexecution.api.util.ValidatablePanelListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/*package*/ final class CreateHostVisualPanel2 extends JPanel {

    private final ChangeListener wizardListener;
    private final CreateHostData data;
    private final ConfigPanelListener cfgListener = new ConfigPanelListener();
    private final ValidateablePanel configurationPanel;
    private static final RequestProcessor RP = new RequestProcessor(CreateHostVisualPanel2.class.getName(), 1);

    public CreateHostVisualPanel2(CreateHostData data, ChangeListener listener) {
        this.data = data;
        wizardListener = listener;
        initComponents();

        textLoginName.setText(System.getProperty("user.name"));

        configurationPanel = ConnectionManager.getInstance().getConfigurationPanel(null);
        configurationPanel.addValidationListener(cfgListener);

        authPanel.add(configurationPanel, BorderLayout.CENTER);

        DocumentListener dl = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                fireChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireChange();
            }
        };

        textLoginName.getDocument().addDocumentListener(dl);
//        textPassword.getDocument().addDocumentListener(dl);
    }

    private void fireChange() {
        hostFound = null;
        wizardListener.stateChanged(null);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CreateHostVisualPanel2.Title");
    }

    void init() {
    }

    private String getLoginName() {
        return textLoginName.getText();
    }

//    char[] getPassword() {
//        return textPassword.getPassword();
//    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        authPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tpOutput = new javax.swing.JTextPane();
        pbarStatusPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        textLoginName = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(534, 409));
        setRequestFocusEnabled(false);

        authPanel.setLayout(new java.awt.BorderLayout());

        tpOutput.setEditable(false);
        tpOutput.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel2.class, "CreateHostVisualPanel2.tpOutput.text")); // NOI18N
        tpOutput.setOpaque(false);
        jScrollPane1.setViewportView(tpOutput);

        pbarStatusPanel.setMaximumSize(new java.awt.Dimension(2147483647, 10));
        pbarStatusPanel.setMinimumSize(new java.awt.Dimension(100, 10));
        pbarStatusPanel.setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel2.class, "CreateHostVisualPanel2.jPanel1.border.title"))); // NOI18N

        jLabel1.setLabelFor(textLoginName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel2.class, "CreateHostVisualPanel2.jLabel1.text")); // NOI18N

        textLoginName.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel2.class, "CreateHostVisualPanel2.textLoginName.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textLoginName, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textLoginName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pbarStatusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(authPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(authPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pbarStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents
    private ProgressHandle phandle;

    /* package-local */ ExecutionEnvironment getHost() {
        return hostFound;
    }

    /* package-local */ Runnable getRunOnFinish() {
        return runOnFinish;
    }
    private ExecutionEnvironment hostFound = null;
    private Runnable runOnFinish = null;

    public void enableControls(boolean enable) {
        configurationPanel.setEnabled(enable);
        textLoginName.setEnabled(enable);
    }

    public boolean canValidateHost() {
        List<ServerRecord> records = new ArrayList<ServerRecord>();

        if (data.getCacheManager().getServerUpdateCache() != null && data.getCacheManager().getServerUpdateCache().getHosts() != null) {
            records.addAll(data.getCacheManager().getServerUpdateCache().getHosts());
        } else {
            records = new ArrayList<ServerRecord>(ServerList.getRecords());
        }

        for (ServerRecord record : records) {
            if (record.isRemote()) {
                if (record.getServerName().equals(data.getHostName())
                        && record.getExecutionEnvironment().getSSHPort() == data.getPort()
                        && record.getUserName().equals(textLoginName.getText())) {
                    return false;
                }
            }
        }

        return true;
    }

    public Future<Boolean> validateHost() {
        FutureTask<Boolean> validationTask = new FutureTask<Boolean>(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
//                final char[] password = getPassword();
//                final boolean rememberPassword = cbSavePassword.isSelected();
                tpOutput.setText("");
                TextComponentWriter textComponentWriter = new TextComponentWriter(tpOutput);
                if (isEmpty(getLoginName())) {
                    textComponentWriter.println(NbBundle.getMessage(CreateHostVisualPanel2.class, "EmptyLoginMessage"));
                    return Boolean.FALSE;
                }
                
                final ExecutionEnvironment env = ExecutionEnvironmentFactory.createNew(getLoginName(), data.getHostName(), data.getPort());
                configurationPanel.applyChanges(env);

                tpOutput.setText("");

                phandle = ProgressHandleFactory.createHandle(""); ////NOI18N
                pbarStatusPanel.removeAll();
                pbarStatusPanel.add(ProgressHandleFactory.createProgressComponent(phandle), BorderLayout.CENTER);
                pbarStatusPanel.validate();
                phandle.start();

                try {
                    HostValidatorImpl hostValidator = new HostValidatorImpl(data.getCacheManager());
                    if (hostValidator.validate(env, /*password, rememberPassword, */ new TextComponentWriter(tpOutput))) {
                        hostFound = env;
                        runOnFinish = hostValidator.getRunOnFinish();
                        try { // let user see the log ;-)
                            Thread.sleep(1500);
                        } catch (InterruptedException ex) {
                            // nothing
                        }
                    }
                } finally {
                    phandle.finish();
                    wizardListener.stateChanged(null);
                    pbarStatusPanel.setVisible(false);
                }

                return true;
            }
        });

        RP.post(validationTask);

        return validationTask;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel authPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pbarStatusPanel;
    private javax.swing.JTextField textLoginName;
    private javax.swing.JTextPane tpOutput;
    // End of variables declaration//GEN-END:variables

    boolean hasConfigProblems() {
        return configurationPanel.hasProblem();
    }

    String getConfigProblem() {
        return configurationPanel.getProblem();
    }

    void storeConfiguration() {
        if (!isEmpty(getLoginName())) {
        ExecutionEnvironment env = ExecutionEnvironmentFactory.createNew(getLoginName(), data.getHostName(), data.getPort());
        configurationPanel.applyChanges(env);
    }
    }

    // End of variables declaration
    private class ConfigPanelListener implements ValidatablePanelListener {

        @Override
        public void stateChanged(ValidateablePanel src) {
            fireChange();
        }
    }
    
    private static boolean isEmpty(String text) {
        return text == null || text.length() == 0;
    }
}
