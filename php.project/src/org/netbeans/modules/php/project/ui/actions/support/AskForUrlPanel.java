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

package org.netbeans.modules.php.project.ui.actions.support;

import java.awt.Dialog;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectSettings;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public class AskForUrlPanel extends JPanel {
    private static final long serialVersionUID = 9261149997804215L;

    private final PhpProject project;
    private final JTextField urlComboBoxEditor;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport notificationLineSupport;

    AskForUrlPanel(PhpProject project, URL defaultUrl) {
        assert project != null;
        assert defaultUrl != null;

        this.project = project;

        initComponents();

        Set<String> urls = new LinkedHashSet<String>();
        urls.add(defaultUrl.toExternalForm());
        urls.addAll(ProjectSettings.getDebugUrls(project));
        for (String s : urls) {
            urlComboBox.addItem(s);
        }

        urlComboBoxEditor = (JTextField) urlComboBox.getEditor().getEditorComponent();
        urlComboBoxEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateFields();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                validateFields();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                validateFields();
            }
        });
    }

    public boolean open() {
        descriptor = new DialogDescriptor(
                this,
                NbBundle.getMessage(AskForUrlPanel.class, "LBL_SpecifyUrl"),
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
        boolean ok = descriptor.getValue() == NotifyDescriptor.OK_OPTION;
        if (ok) {
            Set<String> urls = new LinkedHashSet<String>();
            urls.add((String) urlComboBox.getSelectedItem());
            for (int i = 1; i < urlComboBox.getItemCount(); ++i) {
                urls.add((String) urlComboBox.getItemAt(i));
            }
            ProjectSettings.setDebugUrls(project, new ArrayList<String>(urls));
        }
        return ok;
    }

    public URL getUrl() {
        try {
            return new URL((String) urlComboBox.getSelectedItem());
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("The URL must already be valid!", ex);
        }
    }

    void validateFields() {
        assert descriptor != null;
        assert notificationLineSupport != null;

        String url = urlComboBoxEditor.getText();
        if (!Utils.isValidUrl(url)) {
            notificationLineSupport.setErrorMessage(NbBundle.getMessage(AskForUrlPanel.class, "MSG_InvalidUrl"));
            descriptor.setValid(false);
            return;
        }

        notificationLineSupport.clearMessages();
        descriptor.setValid(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        urlLabel = new JLabel();
        urlComboBox = new JComboBox();

        urlLabel.setLabelFor(urlComboBox);
        Mnemonics.setLocalizedText(urlLabel, NbBundle.getMessage(AskForUrlPanel.class, "AskForUrlPanel.urlLabel.text")); // NOI18N

        urlComboBox.setEditable(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(urlLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(urlComboBox, 0, 337, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(urlLabel)
                    .addComponent(urlComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox urlComboBox;
    private JLabel urlLabel;
    // End of variables declaration//GEN-END:variables

}
