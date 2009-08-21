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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class NewRemoteConnectionPanel extends JPanel {
    private static final long serialVersionUID = 2806958431387531044L;

    private final ConfigManager configManager;
    private DialogDescriptor descriptor;
    private NotificationLineSupport notificationLineSupport;

    public NewRemoteConnectionPanel(ConfigManager configManager) {
        this.configManager = configManager;
        initComponents();

        connectionTypeComboBox.setModel(new DefaultComboBoxModel(new Vector<String>(RemoteConnections.get().getRemoteConnectionTypes())));

        registerListeners();
    }

    public boolean open() {
        descriptor = new DialogDescriptor(
                this,
                NbBundle.getMessage(NewRemoteConnectionPanel.class, "LBL_CreateNewConnection"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        descriptor.setValid(false);
        notificationLineSupport = descriptor.createNotificationLineSupport();
        notificationLineSupport.setInformationMessage(NbBundle.getMessage(NewRemoteConnectionPanel.class, "TXT_ProvideConnectionName"));

        return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION;
    }

    public String getConnectionName() {
        return connectionNameTextField.getText().trim();
    }

    public String getConfigName() {
        return getConnectionName().replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N
    }

    public String getConnectionType() {
        return (String) connectionTypeComboBox.getSelectedItem();
    }

    void validateFields() {
        String name = getConnectionName();
        String config = getConfigName();
        String type = getConnectionType();

        String err = null;
        if (name.length() == 0) {
            err = NbBundle.getMessage(NewRemoteConnectionPanel.class, "MSG_EmptyConnectionName");
        } else if (type.length() == 0) {
            err = NbBundle.getMessage(NewRemoteConnectionPanel.class, "MSG_EmptyConnectionType");
        } else if (configManager.exists(config)) {
            err = NbBundle.getMessage(NewRemoteConnectionPanel.class, "MSG_ConnectionExists", config);
        }
        setError(err);
    }

    private void registerListeners() {
        connectionNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }
            private void processUpdate() {
                validateFields();
            }
        });
        connectionTypeComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                validateFields();
            }
        });
    }

    private void setError(String msg) {
        assert descriptor != null;
        assert notificationLineSupport != null;

        if (StringUtils.hasText(msg)) {
            notificationLineSupport.setErrorMessage(msg);
            descriptor.setValid(false);
        } else {
            notificationLineSupport.clearMessages();
            descriptor.setValid(true);
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

        connectionNameLabel = new JLabel();
        connectionNameTextField = new JTextField();
        connectionTypeLabel = new JLabel();
        connectionTypeComboBox = new JComboBox();

        connectionNameLabel.setLabelFor(connectionNameTextField);


        Mnemonics.setLocalizedText(connectionNameLabel,NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionNameLabel.text")); // NOI18N
        connectionNameTextField.setText(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionNameTextField.text")); // NOI18N
        connectionTypeLabel.setLabelFor(connectionTypeComboBox);

        Mnemonics.setLocalizedText(connectionTypeLabel, NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionTypeLabel.text"));
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(connectionNameLabel)
                    .add(connectionTypeLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(connectionTypeComboBox, 0, 221, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, connectionNameTextField, GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(connectionNameLabel)
                    .add(connectionNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(connectionTypeLabel)
                    .add(connectionTypeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        connectionNameLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionNameLabel.AccessibleContext.accessibleName")); // NOI18N
        connectionNameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionNameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        connectionNameTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionNameTextField.AccessibleContext.accessibleName")); // NOI18N
        connectionNameTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionNameTextField.AccessibleContext.accessibleDescription")); // NOI18N
        connectionTypeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionTypeLabel.AccessibleContext.accessibleName")); // NOI18N
        connectionTypeLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionTypeLabel.AccessibleContext.accessibleDescription")); // NOI18N
        connectionTypeComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionTypeComboBox.AccessibleContext.accessibleName")); // NOI18N
        connectionTypeComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionTypeComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel connectionNameLabel;
    private JTextField connectionNameTextField;
    private JComboBox connectionTypeComboBox;
    private JLabel connectionTypeLabel;
    // End of variables declaration//GEN-END:variables

}
