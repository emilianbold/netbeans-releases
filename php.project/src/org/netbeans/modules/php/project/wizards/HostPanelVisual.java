/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.php.project.wizards;

import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.netbeans.modules.php.rt.WebServerRegistry;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.ProjectConfigProvider;
import org.netbeans.modules.php.rt.utils.WebServersExplorer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * TODO: was quickly updated to show design. Needs refactoring.
 * should use the same UI panel as customizer dialog
 * @author  ads
 */
class HostPanelVisual extends JPanel {

    private static final long serialVersionUID = -6801184871041303133L;

    private static final String MSG_ABSENT_HOST = "MSG_NoHostFound"; // NOI18N
    private static final String PROP_HOST = "host"; // NOI18N

    HostPanelVisual(ProviderPanelVisual panel) {
        myPanel = panel;
        initComponents();
    }

    void store(WizardDescriptor descriptor) {
        HostHolder hostHolder = (HostHolder) myHostName.getSelectedItem();
        if (hostHolder != null) {
            if (hostHolder.getHost() instanceof ProviderPanelVisual.NoHost) {
                hostHolder = null;
            }
        }

        descriptor.putProperty(NewPhpProjectWizardIterator.HOST, hostHolder);
    }

    void read(WizardDescriptor descriptor) {
        myDescriptor = descriptor;
        initHostConfig(descriptor);
    }

    boolean dataIsValid() {
        return validateHost(getDescriptor());
    }

    protected Host getSelectedHost(){
        HostHolder hostHolder = (HostHolder) myHostName.getSelectedItem();
        if (hostHolder != null) {
            return hostHolder.getHost();
        }
        return null;
    }

    protected void setSelectedHost(Host host){
        HostHolder hh = null;
        if (host != null){
            hh = new HostHolder((host));
        }
        setSelectedHost(hh);
    }

    protected void setSelectedHost(HostHolder hostHolder){
        if (hostHolder == null){
            hostHolder = new HostHolder(getVisualPanel().getNoHost());
        }
        myHostName.setSelectedItem(hostHolder);
    }
    
    private boolean validateHost(WizardDescriptor wizardDescriptor) {
        if (myHostName.getItemCount() <= 1) {
            String message = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, MSG_ABSENT_HOST);
            wizardDescriptor.putProperty(
                    NewPhpProjectWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, message);
        }
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hostNameLabel = new javax.swing.JLabel();
        myHostName = new javax.swing.JComboBox();
        myManageHostBtn = new javax.swing.JButton();

        hostNameLabel.setLabelFor(myHostName);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/php/project/wizards/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(hostNameLabel, bundle.getString("LBL_HotsName")); // NOI18N

        myHostName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostSelected(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(myManageHostBtn, org.openide.util.NbBundle.getMessage(HostPanelVisual.class, "HostPanelVisual.myManageHostBtn.text")); // NOI18N
        myManageHostBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myManageHostBtnActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(hostNameLabel)
                .add(18, 18, 18)
                .add(myHostName, 0, 240, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(myManageHostBtn)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostNameLabel)
                    .add(myManageHostBtn)
                    .add(myHostName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        hostNameLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HostPanelVisual.class, "A11_HostNameLbl")); // NOI18N
        myHostName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HostPanelVisual.class, "A11_HostName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void hostSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostSelected
        hostSelectionChanged();
    }//GEN-LAST:event_hostSelected

    private void myManageHostBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myManageHostBtnActionPerformed
        WebServersExplorer explorer = new WebServersExplorer();
        explorer.setSelection(getSelectedHost());
        explorer.requestFocusInWindow();
        boolean confirmed = explorer.showDialog();
        //----- after dialog is closed
        if (confirmed) {
            initHostConfig(getDescriptor());
            
            if (    !explorer.isRootSelected() 
                    && !(explorer.getSelection() == null) ) 
            {
                setSelectedHost(explorer.getSelection());
            }
        }
        
    }//GEN-LAST:event_myManageHostBtnActionPerformed

    private void hostSelectionChanged() {
        setServerSpecificPanel();
        getPanel().fireChangeEvent();
        firePropertyChange(PROP_HOST, null, myHostName.getSelectedItem());
    }

    private void setServerSpecificPanel() {
        /*
         * Set web server specific UI config.
         */
        HostHolder hostHolder = (HostHolder) myHostName.getSelectedItem();
        if (hostHolder != null) {
            ProjectConfigProvider provider = null;
            Host host = hostHolder.getHost();
            if (!(host instanceof ProviderPanelVisual.NoHost)){
                provider = host.getProvider().getProjectConfigProvider();
            }
            getVisualPanel().storeServerConfig(getDescriptor());
            getVisualPanel().useServerConfig(provider, host);
            getVisualPanel().readServerConfig(getDescriptor());
        }
    }

    private void initHostConfig(WizardDescriptor settings) {
        Collection<Host> collection = WebServerRegistry.getInstance().getHosts();
        HostHolder[] hosts = new HostHolder[collection.size() + 1];
        hosts[0] = new HostHolder(getVisualPanel().getNoHost());

        if (collection.size() > 0) {
            int i = 1;
            for (Host host : collection) {
                hosts[i++] = new HostHolder(host);
            }
        }
        myHostName.setModel(new DefaultComboBoxModel(hosts));

        /*
         * Set selected item in list.
         */
        HostHolder hostHolder = (HostHolder) settings.getProperty(
                NewPhpProjectWizardIterator.HOST);
        setSelectedHost(hostHolder);

        setServerSpecificPanel();
    }

    private ProviderSpecificPanel getPanel() {
        return myPanel.getPanel();
    }

    private ProviderPanelVisual getVisualPanel() {
        return myPanel;
    }

    private WizardDescriptor getDescriptor() {
        return myDescriptor;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel hostNameLabel;
    private javax.swing.JComboBox myHostName;
    private javax.swing.JButton myManageHostBtn;
    // End of variables declaration//GEN-END:variables

    private ProviderPanelVisual myPanel;

    private WizardDescriptor myDescriptor;

}
