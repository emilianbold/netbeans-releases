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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.ui.wizards;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.autoupdate.UpdateElement;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jiri Rechtacek
 */
public class LicenseApprovalPanel extends javax.swing.JPanel {
    public static final String LICENSE_APPROVED = "license-approved";
    private Map<String, Set<String>> license4plugins;
    
    /** Creates new form LicenseApprovalPanel */
    public LicenseApprovalPanel (InstallUnitWizardModel model, boolean isApproved) {
        initComponents ();
        cbAccept.setSelected (isApproved);
        if (model != null) {
            writeLicenses(model);
        } else {
            cbAccept.setEnabled (false);
            taLicenses.setEnabled (false);
        }
    }
    
    Collection<String> getLicenses () {
        assert license4plugins != null : "Licenses must found.";
        if (license4plugins == null && license4plugins.isEmpty ()) {
            return Collections.emptyList ();
        }
        return license4plugins.keySet ();
    }
    
    private void goOverLicenses (InstallUnitWizardModel model) {
        for (UpdateElement el : model.getAllUpdateElements ()) {
            if (el.getLicence () != null) {
                if (license4plugins == null) {
                    license4plugins = new HashMap<String, Set<String>> ();
                }
                if (license4plugins.containsKey (el.getLicence ())) {
                    // add plugin
                    license4plugins.get (el.getLicence ()).add (el.getDisplayName ());
                } else {
                    // license
                    Set<String> plugins = new HashSet<String> ();
                    plugins.add (el.getDisplayName ());
                    license4plugins.put (el.getLicence (), plugins);
                }
                //licenses.put (el.getDisplayName (), el.getLicence ());
            }
        }
    }
    
    public boolean isApproved () {
        return cbAccept.isSelected ();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        taTitle = new javax.swing.JTextArea();
        spLicenses = new javax.swing.JScrollPane();
        taLicenses = new javax.swing.JTextArea();
        cbAccept = new javax.swing.JCheckBox();

        taTitle.setEditable(false);
        taTitle.setLineWrap(true);
        taTitle.setText(org.openide.util.NbBundle.getMessage(LicenseApprovalPanel.class, "LicenseApprovalPanel_taTitle_Text")); // NOI18N
        taTitle.setWrapStyleWord(true);
        taTitle.setMargin(new java.awt.Insets(0, 4, 0, 0));
        taTitle.setOpaque(false);

        taLicenses.setColumns(20);
        taLicenses.setEditable(false);
        taLicenses.setLineWrap(true);
        taLicenses.setRows(5);
        taLicenses.setWrapStyleWord(true);
        taLicenses.setMargin(new java.awt.Insets(0, 4, 0, 4));
        spLicenses.setViewportView(taLicenses);
        taLicenses.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LicenseApprovalPanel.class, "LicenseApprovalPanel_taLicenses_ACN")); // NOI18N
        taLicenses.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LicenseApprovalPanel.class, "LicenseApprovalPanel_taLicenses_ACD")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbAccept, org.openide.util.NbBundle.getMessage(LicenseApprovalPanel.class, "LicenseApprovalPanel.cbAccept.text")); // NOI18N
        cbAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAcceptActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbAccept)
                    .addComponent(taTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
                    .addComponent(spLicenses, javax.swing.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(taTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spLicenses)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbAccept)
                .addContainerGap())
        );

        cbAccept.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LicenseApprovalPanel.class, "LicenseApprovalPanel_cbAccept_ACN")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAcceptActionPerformed
        firePropertyChange (LICENSE_APPROVED, null, cbAccept.isSelected ());
    }//GEN-LAST:event_cbAcceptActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbAccept;
    private javax.swing.JScrollPane spLicenses;
    private javax.swing.JTextArea taLicenses;
    private javax.swing.JTextArea taTitle;
    // End of variables declaration//GEN-END:variables
    
    private void writeLicenses (InstallUnitWizardModel model) {
        goOverLicenses (model);
        StringBuffer content = new StringBuffer ();
        for (String lic : license4plugins.keySet ()) {
            StringBuffer title = new StringBuffer ();
            for (String plugin : license4plugins.get (lic)) {
                title.append ((title.length () == 0 ? "" :
                    NbBundle.getMessage (LicenseApprovalPanel.class, "LicenseApprovalPanel_tpLicense_Delimeter")) + plugin); // NOI18N
            }
            content.append (NbBundle.getMessage (LicenseApprovalPanel.class, "LicenseApprovalPanel_tpLicense_Head", title)); // NOI18N
            content.append ("\n"); // NOI18N
            content.append (lic);
            content.append (NbBundle.getMessage (LicenseApprovalPanel.class, "LicenseApprovalPanel_tpLicense_Separator")); // NOI18N
        }
        taLicenses.setText (content.toString ());
        taLicenses.setCaretPosition (0);
    }
}
