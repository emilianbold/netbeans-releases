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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.team.server;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.team.server.api.TeamServerManager;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.server.ui.spi.TeamServerProvider;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Becicka
 */
public class TeamServerInstanceCustomizer extends javax.swing.JPanel implements java.beans.Customizer, DocumentListener, ActionListener {

    private Object bean;
    private NotificationLineSupport ns;
    private DialogDescriptor dd;
    private JButton okButton;
    private TeamServerProvider provider;
    private String originalName;
    private String originalUrl;

    public TeamServerInstanceCustomizer(JButton okButton, TeamServerProvider provider) {
        this(okButton, (Collection<TeamServerProvider>) null);
        this.provider = provider;
    }
    
    /** Creates new customizer TeamInstanceCustomizer */
    public TeamServerInstanceCustomizer(JButton okButton, Collection<TeamServerProvider> providers) {
        this.okButton = okButton;
        initComponents();
        progress.setVisible(false);
        if(providers == null) {
            cmbProvider.setVisible(false);
            lblProvider.setVisible(false);
        } else {
            cmbProvider.addActionListener(this);
            cmbProvider.setModel(new DefaultComboBoxModel(providers.toArray(new TeamServerProvider[providers.size()])));
            updateSelection();
            cmbProvider.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    String tooltip = null;
                    if (value instanceof TeamServerProvider) {
                        TeamServerProvider prov = (TeamServerProvider) value;
                        value = prov.getDisplayName();
                        tooltip = prov.getDescription();
                    }
                    Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (comp instanceof JComponent) {
                        ((JComponent) comp).setToolTipText(tooltip);
                    }
                    return comp;
                }
            });
            cmbProvider.setEnabled(providers.size() > 1);
        }
        txtDisplayName.getDocument().addDocumentListener(this);
        txtUrl.getDocument().addDocumentListener(this);
    }

    public void setObject(Object bean) {
        this.bean = bean;
    }

    public String getDisplayName() {
        return txtDisplayName.getText();
    }

    public String getUrl() {
        return txtUrl.getText();
    }
    
    public void setDisplayName(String name) {
        txtDisplayName.setText(name);
        originalName = name;
    }

    public void setUrl(String url) {
        txtUrl.setText(url);
        originalUrl = url;
    }

    public TeamServerProvider getProvider () {
        return cmbProvider.isVisible() ? (TeamServerProvider) cmbProvider.getSelectedItem() : provider;
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == cmbProvider) {
            updateSelection();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblName = new javax.swing.JLabel();
        lblUrl = new javax.swing.JLabel();
        txtDisplayName = new javax.swing.JTextField();
        txtUrl = new javax.swing.JTextField();
        progress = new javax.swing.JProgressBar();
        proxy = new javax.swing.JButton();
        lblProvider = new javax.swing.JLabel();
        cmbProvider = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        lblName.setLabelFor(txtDisplayName);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "TeamServerInstanceCustomizer.lblName.text")); // NOI18N

        lblUrl.setLabelFor(txtUrl);
        org.openide.awt.Mnemonics.setLocalizedText(lblUrl, org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "TeamServerInstanceCustomizer.lblUrl.text")); // NOI18N

        txtUrl.setText(org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "TeamServerInstanceCustomizer.txtUrl.text")); // NOI18N

        progress.setIndeterminate(true);
        progress.setString(org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "TeamServerInstanceCustomizer.progress.string")); // NOI18N
        progress.setStringPainted(true);

        org.openide.awt.Mnemonics.setLocalizedText(proxy, org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "TeamServerInstanceCustomizer.proxy.text")); // NOI18N
        proxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proxyActionPerformed(evt);
            }
        });

        lblProvider.setLabelFor(cmbProvider);
        org.openide.awt.Mnemonics.setLocalizedText(lblProvider, org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "TeamServerInstanceCustomizer.lblProvider.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblName)
                            .addComponent(lblUrl)
                            .addComponent(lblProvider))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cmbProvider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(txtUrl, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                            .addComponent(txtDisplayName)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(proxy)
                        .addGap(18, 18, 18)
                        .addComponent(progress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbProvider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProvider))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(txtDisplayName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUrl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(proxy)
                    .addComponent(progress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2))
        );

        txtDisplayName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "TeamServerInstanceCustomizer.txtDisplayName.AccessibleContext.accessibleName")); // NOI18N
        txtDisplayName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "TeamServerInstanceCustomizer.txtDisplayName.AccessibleContext.accessibleDescription")); // NOI18N
        txtUrl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "TeamServerInstanceCustomizer.txtUrl.AccessibleContext.accessibleName")); // NOI18N
        txtUrl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "TeamServerInstanceCustomizer.txtUrl.AccessibleContext.accessibleDescription")); // NOI18N
        proxy.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "TeamServerInstanceCustomizer.proxy.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void proxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proxyActionPerformed
        OptionsDisplayer.getDefault().open("General"); // NOI18N
    }//GEN-LAST:event_proxyActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbProvider;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblProvider;
    private javax.swing.JLabel lblUrl;
    private javax.swing.JProgressBar progress;
    private javax.swing.JButton proxy;
    private javax.swing.JTextField txtDisplayName;
    private javax.swing.JTextField txtUrl;
    // End of variables declaration//GEN-END:variables

    private void validateInput() {
        clearError();
        
        String e = nameValid(getDisplayName());
        if (e!=null) {
            showError(e);
            return;
        }
        
        e = urlValid(getUrl());
        if (e!=null) {
            showError(e);
            return;
        }
    }

    public void showError(String text) {
        stopProgress();
        ns.setInformationMessage(text);
        dd.setValid(false);
        okButton.setEnabled(false);
    }

    void clearError() {
        ns.clearMessages();
        dd.setValid(true);
        okButton.setEnabled(true);
    }

    public void startProgress() {
        progress.setVisible(true);
    }

    public void stopProgress() {
        progress.setVisible(false);
    }

    private String urlValid (String url) {
        String msg = getProvider().validate(url);
        if (msg != null) {
            return msg;
        }
        for (TeamServer instance : TeamServerManager.getDefault().getTeamServers()) {
            if ( !url.equals(originalUrl) && 
                 instance.getUrl().toString().equals(url.endsWith("/") ? url.substring(0, url.length() - 1) : url)) // NOI18N 
            { 
                return NbBundle.getMessage(TeamServerInstanceCustomizer.class, "ERR_AlreadyUsed", url); // NOI18N
            }
        }

        try {
            new URL(url);
            return null;
        } catch (MalformedURLException ex) {
            return ex.getMessage();
        }
    }
    
    private String nameValid (String name) {
        if (name.trim().length()==0) {
            return org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "ERR_NoName"); // NOI18N
        }
        if (name.contains(",")) {//NOI18N
            return org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "ERR_IllegalCharacter","','"); // NOI18N
        }
        if (name.contains(";")) {//NOI18N
            return org.openide.util.NbBundle.getMessage(TeamServerInstanceCustomizer.class, "ERR_IllegalCharacter","';'"); // NOI18N
        }
        
        for (TeamServer instance : TeamServerManager.getDefault().getTeamServers()) {
            if ( !name.equals(originalName) && instance.getDisplayName().equals(name) ) { 
                return NbBundle.getMessage(TeamServerInstanceCustomizer.class, "ERR_AlreadyUsed", name); // NOI18N
            }
        }
        return null;
    }

    public void insertUpdate(DocumentEvent e) {
        validateInput();
    }

    public void removeUpdate(DocumentEvent e) {
        validateInput();
    }

    public void changedUpdate(DocumentEvent e) {
        validateInput();
    }

    public void setNotificationsSupport(NotificationLineSupport support) {
        this.ns = support;
    }

    public void setDialogDescriptor(DialogDescriptor dd) {
        this.dd = dd;
    }

    private void updateSelection () {
        Object sel = cmbProvider.getSelectedItem();
        if (sel instanceof TeamServerProvider) {
            cmbProvider.setToolTipText(((TeamServerProvider) sel).getDescription());
        } else {
            cmbProvider.setToolTipText(null);
        }
    }

}
