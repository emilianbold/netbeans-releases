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
package org.netbeans.modules.php.rt.providers.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;
import javax.swing.JPanel;
import org.netbeans.modules.php.rt.WebServerRegistry;
import org.netbeans.modules.php.rt.actions.AddHostAction;
import org.netbeans.modules.php.rt.providers.impl.AbstractProvider;
import org.netbeans.modules.php.rt.providers.impl.HostImpl;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.modules.php.rt.ui.AddHostWizard;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.SharedClassObject;

/**
 *
 * @author  avk
 */
public class DefaultServerCustomizer extends javax.swing.JPanel {

    /** Creates new form ServerCustomizer */
    public DefaultServerCustomizer(HostImpl host) {
        initComponents();
        myHost = host;
        myProperties = new Properties();

    }

    public Window createCustomizerDialog(ServerCustomizerComponent innerPanel, 
            String title) 
    {
        init(innerPanel);
        OptionListener listener = new OptionListener();

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
                this, 
                title, 
                true, 
                DialogDescriptor.OK_CANCEL_OPTION, 
                DialogDescriptor.CANCEL_OPTION, 
                listener);
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dialogDescriptor);

        myDialog = dlg;
        myDialogDescriptor = dialogDescriptor;

        return dlg;
    }


    private void init(ServerCustomizerComponent inner) {

        if (myCustomizerPanel != null) {
            myCustomizerContainer.remove(myCustomizerPanel);
        }

        JPanel innerJPanel = inner.getPanel();
        if (innerJPanel !=null){
            myCustomizerContainer.add(BorderLayout.NORTH, (JPanel) innerJPanel);
            myCustomizerPanel = (JPanel) innerJPanel;
        }
        
        myCustomizerContainer.validate();
        validate();

        read(getHost());
    }

    void read(HostImpl impl) {
        if (myCustomizerPanel != null) {
            if (myCustomizerPanel instanceof ServerCustomizerComponent) {
                ServerCustomizerComponent panel 
                        = (ServerCustomizerComponent) myCustomizerPanel;
                
                getProperties().put(ServerCustomizerComponent.HOST, impl);
                panel.readValues(getProperties());
            }
        }
    }

    HostImpl store() {
        if (myCustomizerPanel != null) {
            if (myCustomizerPanel instanceof ServerCustomizerComponent) {
                ServerCustomizerComponent panel 
                        = (ServerCustomizerComponent) myCustomizerPanel;
                panel.storeValues(getProperties());
                
                HostImpl host = (HostImpl)getProperties()
                        .get( ServerCustomizerComponent.HOST);
                WebServerProvider provider = (WebServerProvider) getProperties()
                        .get(ServerCustomizerComponent.PROVIDER);
                
                if (provider != null && !provider.equals(host.getProvider())){
                    
                    if (provider instanceof AbstractProvider){
                        AbstractUiConfigProvider uiProvider 
                                = (AbstractUiConfigProvider)
                                ((AbstractProvider)provider).getConfigProvider();
                        return uiProvider.copyHost(host.getName(), host);
                    }
                }
                
                return host;
            }
        }
        return null;
    }

    public void stateChanged() {
        if (myCustomizerPanel != null) {
            if (myCustomizerPanel instanceof ServerCustomizerComponent) {
                
                ServerCustomizerComponent panel 
                        = (ServerCustomizerComponent) myCustomizerPanel;
                
                setErrorMessage("");
                boolean isContentValid = panel.doContentValidation();
                if (myDialogDescriptor != null) {
                    if (isContentValid) {
                        myDialogDescriptor.setValid(true);
                    } else {
                        myDialogDescriptor.setValid(false);
                    }
                }
            }
        }
    }

    public void setMessage(String msg) {
        myMessagePanel.setText(msg);
        myMessagePanel.setForeground(Color.BLACK);
    }

    public void setErrorMessage(String msg) {
        myMessagePanel.setText(msg);
        myMessagePanel.setForeground(Color.RED);
    }

    /** 
     * Listens to the actions on the Customizer's option buttons 
     */
    private class OptionListener extends WindowAdapter implements ActionListener {

        OptionListener() {
        }

        // Listening to OK button ----------------------------------------------
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == DialogDescriptor.OK_OPTION) {
                // Store updated host
                HostImpl host = store();
                // TODO check that provider was changed. null is TMP
                if (!host.getProvider().equals(myHost.getProvider())){
                    AddHostAction action  = AddHostAction.findInstance();
                    action.showCustomizer(host, AddHostWizard.Mode.UPDATE_EXISTING);
                } else {
                    instantiate(myHost, host);
                }
            }
            // Close & dispose the the dialog
            if (myDialog != null) {
                myDialog.setVisible(false);
                myDialog.dispose();
            }
        }


        private void instantiate(HostImpl oldHost, HostImpl newHost) {
            AbstractProvider provider = (AbstractProvider) oldHost.getProvider();

            provider.updateHost(oldHost, newHost);
            // send notification 
            WebServerRegistry.getInstance().upadateHost(newHost);
        }


        // Listening to window events ------------------------------------------
        @Override
        public void windowClosed(WindowEvent e) {
            myDialog = null;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            if (myDialog != null) {
                myDialog.setVisible(false);
                myDialog.dispose();
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        myCustomizerContainer = new javax.swing.JPanel();
        myCustomizerPanel = new javax.swing.JPanel();
        myMessageContainer = new javax.swing.JPanel();
        myMessagePanel = new javax.swing.JTextPane();

        setLayout(new java.awt.GridBagLayout());

        myCustomizerContainer.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout myCustomizerPanelLayout = new org.jdesktop.layout.GroupLayout(myCustomizerPanel);
        myCustomizerPanel.setLayout(myCustomizerPanelLayout);
        myCustomizerPanelLayout.setHorizontalGroup(
            myCustomizerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );
        myCustomizerPanelLayout.setVerticalGroup(
            myCustomizerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        myCustomizerContainer.add(myCustomizerPanel, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(myCustomizerContainer, gridBagConstraints);

        myMessagePanel.setEditable(false);
        myMessagePanel.setFocusable(false);
        myMessagePanel.setMinimumSize(new java.awt.Dimension(6, 10));
        myMessagePanel.setOpaque(false);
        myMessagePanel.setPreferredSize(new java.awt.Dimension(0, 0));

        org.jdesktop.layout.GroupLayout myMessageContainerLayout = new org.jdesktop.layout.GroupLayout(myMessageContainer);
        myMessageContainer.setLayout(myMessageContainerLayout);
        myMessageContainerLayout.setHorizontalGroup(
            myMessageContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myMessageContainerLayout.createSequentialGroup()
                .add(myMessagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                .addContainerGap())
        );
        myMessageContainerLayout.setVerticalGroup(
            myMessageContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myMessageContainerLayout.createSequentialGroup()
                .add(myMessagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                .addContainerGap())
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(myMessageContainer, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel myCustomizerContainer;
    private javax.swing.JPanel myCustomizerPanel;
    private javax.swing.JPanel myMessageContainer;
    private javax.swing.JTextPane myMessagePanel;
    // End of variables declaration//GEN-END:variables
    // End of variables declaration
    // End of variables declaration
    // End of variables declaration
    protected HostImpl getHost() {
        return myHost;
    }

    protected Properties getProperties() {
        return myProperties;
    }

    private HostImpl myHost;
    private Dialog myDialog;
    private DialogDescriptor myDialogDescriptor;
    private Properties myProperties;
}
