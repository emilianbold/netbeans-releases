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

package org.netbeans.modules.compapp.projects.jbi.ui.customizer;

import javax.swing.*;
import org.netbeans.modules.compapp.jbiserver.JbiManager;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;

// import org.netbeans.modules.compapp.projects.jbi.spi.DeploymentService;

public class CustomizerRun extends JPanel implements JbiJarCustomizer.Panel, HelpCtx.Provider {

    // Helper for storing properties
    private VisualPropertySupport vps;

//    String[] serverInstanceIDs;
//    String[] serverNames;
//    String[] serverURLs;
    boolean initialized = false;
    
    private JbiProjectProperties webProperties;
    
    /** Creates new form CustomizerCompile */
    public CustomizerRun(JbiProjectProperties webProperties) {
        this.webProperties = webProperties;
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_A11YDesc")); // NOI18N
        vps = new VisualPropertySupport(webProperties);
    }
    
    public void initValues() {
        initialized = false;
        Deployment deployment = Deployment.getDefault();
        
        String[] serverInstanceIDs = JbiManager.getAppServers();
        
        String[] serverNames = new String[serverInstanceIDs.length];
        
        String instance = (String) webProperties.get(
                JbiProjectProperties.J2EE_SERVER_INSTANCE
                );
        int selected = -1;
        
        for (int i = 0; i < serverInstanceIDs.length; i++) {
            String serverID = serverInstanceIDs [i];
            
            String serverInstanceDisplayName =
                    deployment.getServerInstanceDisplayName(serverID);
            // if displayName not set use instanceID instead
            if (serverInstanceDisplayName == null) {
                serverInstanceDisplayName = serverID;
            }
            
            serverNames[i] = deployment.getServerDisplayName(deployment.getServerID(serverID))
            + " (" + serverInstanceDisplayName + ")"; // NOI18N
            
            if ((instance != null) && (selected < 0)) {
                if (instance.equalsIgnoreCase(serverID)) {
                    selected = i;
                }
            }
        }
        
        vps.register(jComboBoxServer, serverNames, serverInstanceIDs, 
                JbiProjectProperties.J2EE_SERVER_INSTANCE);
        
        if (selected > -1) {
            jComboBoxServer.setSelectedIndex(selected);
        }
        initialized = true;
        
//        if (instance == null) {
//            int index = jComboBoxServer.getSelectedIndex();
//            if (index == -1 && jComboBoxServer.getModel().getSize() == 1) {
//                instance = serverInstanceIDs[0];
//                webProperties.put(JbiProjectProperties.J2EE_SERVER_INSTANCE, instance);
//                webProperties.store();
//                jComboBoxServer.setSelectedIndex(0);
//            }
//        }
        
        /*
        //todo: 01/06/06 test code for plug-in services
        Lookup.Template tpl  = new Lookup.Template (DeploymentService.class);
        Lookup.Result result = Lookup.getDefault().lookup (tpl);
        Collection c         = result.allInstances();
        for( Iterator it = c.iterator(); it.hasNext(); ) {
            DeploymentService ds = (DeploymentService)it.next();
            String j2eeType = ds.getJ2EEServerType();
            String jbiType = ds.getJBIServerType();
            int i = 5;
        }
         */
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelServer = new javax.swing.JLabel();
        jComboBoxServer = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelServer.setLabelFor(jComboBoxServer);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelServer, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Server_JLabel")); // NOI18N

        jComboBoxServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxServerActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabelServer)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxServer, 0, 346, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabelServer)
                    .add(jComboBoxServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(58, 58, 58))
        );

        jLabelServer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACS_SERVER")); // NOI18N
        jComboBoxServer.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_Server_A11YName")); // NOI18N
        jComboBoxServer.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_Server_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void jComboBoxServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxServerActionPerformed
        if (jComboBoxServer.getSelectedIndex() == -1 || !initialized)
            return;
    }//GEN-LAST:event_jComboBoxServerActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxServer;
    private javax.swing.JLabel jLabelServer;
    // End of variables declaration//GEN-END:variables

  /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerRun.class);
    }   
}
