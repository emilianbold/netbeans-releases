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

package org.netbeans.modules.php.project.customizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.netbeans.modules.php.rt.utils.WebServersExplorer;
import org.netbeans.modules.php.rt.WebServerRegistry;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.openide.util.NbBundle;


/**
 * @author  ads
 */
public abstract class CustomizerHostVisual extends JPanel{

    private static final long serialVersionUID = 6233510568797205953L;

    private static final String MSG_NO_HOSTS = "MSG_NoHostFound"; // NOI18N
    private static final String MSG_ABSENT_HOST = "MSG_HostIsAbsent"; // NOI18N

    private static Logger LOGGER = Logger.getLogger(CustomizerHostVisual.class.getName());

    protected CustomizerHostVisual() {

        initComponents();
        initHostsPanel();

    }
    
    protected abstract JPanel loadProviderPanel(Host host);
    protected abstract void loadProviderProperties();

    public void setMessage(String msg) {
        myMessagePanel.setText(msg);
        myMessagePanel.setForeground(Color.BLACK);
    }

    public void setErrorMessage(String msg) {
        myMessagePanel.setText(msg);
        myMessagePanel.setForeground(Color.RED);
    }

    protected void initHostsPanel() {

        configureHostsPanel();
    }

    protected void update() {

        configureProviderPanel();
        
        loadProviderProperties();
        
        validateContent();
    }

    protected void setSelectedHost(Host host){
        if (host == null){
            return;
        }
        int index = getHostIndex(host);
        if (index < 0 && host instanceof AbsentHost){
            index = addHostToList(host);
        }
        myHostName.setSelectedIndex(index);
    }
    
    protected Host getSelectedHost(){
        int index = myHostName.getSelectedIndex();
        return myHostsVector.get(index);
    }
    
    protected int getHostIndex(Host host) {
        if (host != null) {
            for (Host item : myHostsVector) {
                if (item.getId().equals(host.getId())) {
                    return myHostsVector.indexOf(item);
                }
            }
        }
        return myHostsVector.indexOf(host);
    }
    
    protected int addHostToList(Host host){
        configureHostsPanel(host);
        return getHostIndex(host);
    }
    
    protected void validateContent() {
        Host host = getSelectedHost();
        if (host == null || host instanceof NoHost) {
            if (!haveExistingHosts()){
                String msg = NbBundle.getMessage(CustomizerHostVisual.class,
                        MSG_NO_HOSTS);
                setErrorMessage(msg);
            }
            setHostSelectionVisible(false);
        } 
        else if (host instanceof AbsentHost) {
            String msg = NbBundle.getMessage(CustomizerHostVisual.class,
                    MSG_ABSENT_HOST, host.getDisplayName());
            setErrorMessage(msg);
            setHostSelectionVisible(false);
        } 
    }

    private void configureHostsPanel() {
        configureHostsPanel(null);
    }
    
    private boolean haveExistingHosts(){
        if (myHostsVector.size() > 2){
            return true;
        }
        for (Host host : myHostsVector){
            if (isExistingHost(host)){
                return true;
            }
        }
        return false;
    }
    
    private boolean isExistingHost(Host host){
        if (    host == null 
                || host instanceof NoHost
                || host instanceof AbsentHost) 
        {
            return false;
        } 
        return true;
    }
    
    private void configureHostsPanel(Host absentHost) {
        Collection<Host> collection = WebServerRegistry.getInstance().getHosts();
        

        myHostsVector = new Vector<Host>(collection.size()+1);
        LinkedList<String> names = new LinkedList<String>();

        myHostsVector.add(myNoHost);
        names.add(myNoHost.getDisplayName());
        
        if (absentHost != null){
            myHostsVector.add(absentHost);
            names.add(absentHost.getDisplayName());
        }
        
        for (Host host : collection) {
            myHostsVector.add(host);
            names.add(host.getDisplayName());
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel( 
                names.toArray(new String[names.size()]) );
        
        myHostName.setModel( model );
    }

    /**
     *
     * @parameter ProjectConfigProvider that will provide customizer panel.
     * If == null, empty JPanel() will be placed.
     *
     */
    private void configureProviderPanel() {
        Host host = getSelectedHost();
        if (myProviderPanel != null) {
            myProviderPanelContainer.remove(myProviderPanel);
        }

        myProviderPanel = loadProviderPanel(host);
        if (myProviderPanel != null){
        myProviderPanelContainer.add(BorderLayout.NORTH, myProviderPanel);
        }

        myProviderPanelContainer.validate();
        validate();

    }

    private void hostSelectionChanged() {
        Host hostSelection = getSelectedHost();
        if (hostSelection != null) {
            setMessage("");
            update();
        }
    }


    private void setHostSelectionVisible(boolean aFlag) {
        if (myProviderPanel != null) {
            myProviderPanel.setVisible(aFlag);
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        myHostsPanel = new javax.swing.JPanel();
        myHostNameLabel = new javax.swing.JLabel();
        myHostName = new javax.swing.JComboBox();
        myManageHostBtn = new javax.swing.JButton();
        myProviderPanelContainer = new javax.swing.JPanel();
        myMessageContainer = new javax.swing.JPanel();
        myMessagePanel = new javax.swing.JTextPane();
        spacerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        myHostNameLabel.setLabelFor(myHostName);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/php/project/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(myHostNameLabel, bundle.getString("LBL_CstmzHostName")); // NOI18N

        myHostName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myHostNameActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(myManageHostBtn, org.openide.util.NbBundle.getMessage(CustomizerHostVisual.class, "CustomizerHostVisual.myManageHostBtn.text")); // NOI18N
        myManageHostBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myManageHostBtnActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout myHostsPanelLayout = new org.jdesktop.layout.GroupLayout(myHostsPanel);
        myHostsPanel.setLayout(myHostsPanelLayout);
        myHostsPanelLayout.setHorizontalGroup(
            myHostsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myHostsPanelLayout.createSequentialGroup()
                .add(myHostNameLabel)
                .add(14, 14, 14)
                .add(myHostName, 0, 246, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myManageHostBtn)
                .addContainerGap())
        );
        myHostsPanelLayout.setVerticalGroup(
            myHostsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myHostsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(myHostsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myManageHostBtn)
                    .add(myHostName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myHostNameLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        myHostNameLabel.getAccessibleContext().setAccessibleName(bundle.getString("A11_CstmzHostName")); // NOI18N
        myHostName.getAccessibleContext().setAccessibleDescription(bundle.getString("A11_CstmzHosts")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(myHostsPanel, gridBagConstraints);

        myProviderPanelContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(myProviderPanelContainer, gridBagConstraints);

        myMessagePanel.setEditable(false);
        myMessagePanel.setText(org.openide.util.NbBundle.getMessage(CustomizerHostVisual.class, "CustomizerHostVisual.myMessagePanel.text")); // NOI18N
        myMessagePanel.setFocusable(false);
        myMessagePanel.setMinimumSize(new java.awt.Dimension(6, 10));
        myMessagePanel.setOpaque(false);
        myMessagePanel.setPreferredSize(new java.awt.Dimension(0, 0));

        org.jdesktop.layout.GroupLayout myMessageContainerLayout = new org.jdesktop.layout.GroupLayout(myMessageContainer);
        myMessageContainer.setLayout(myMessageContainerLayout);
        myMessageContainerLayout.setHorizontalGroup(
            myMessageContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myMessagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
        );
        myMessageContainerLayout.setVerticalGroup(
            myMessageContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myMessageContainerLayout.createSequentialGroup()
                .add(myMessagePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(myMessageContainer, gridBagConstraints);

        org.jdesktop.layout.GroupLayout spacerPanelLayout = new org.jdesktop.layout.GroupLayout(spacerPanel);
        spacerPanel.setLayout(spacerPanelLayout);
        spacerPanelLayout.setHorizontalGroup(
            spacerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 467, Short.MAX_VALUE)
        );
        spacerPanelLayout.setVerticalGroup(
            spacerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 227, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(spacerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void myHostNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myHostNameActionPerformed
        hostSelectionChanged();
    }//GEN-LAST:event_myHostNameActionPerformed

    private void myManageHostBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myManageHostBtnActionPerformed
        WebServersExplorer explorer = new WebServersExplorer();
        explorer.setSelection(getSelectedHost());
        explorer.requestFocusInWindow();
        boolean confirmed = explorer.showDialog();
        //----- after dialog is closed
        if (confirmed) {
            initHostsPanel();

            if (!explorer.isRootSelected()) {
                setSelectedHost(explorer.getSelection());
            }
        }

}//GEN-LAST:event_myManageHostBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox myHostName;
    private javax.swing.JLabel myHostNameLabel;
    private javax.swing.JPanel myHostsPanel;
    private javax.swing.JButton myManageHostBtn;
    private javax.swing.JPanel myMessageContainer;
    private javax.swing.JTextPane myMessagePanel;
    private javax.swing.JPanel myProviderPanelContainer;
    private javax.swing.JPanel spacerPanel;
    // End of variables declaration//GEN-END:variables

    protected class AbsentHost implements Host{

        public AbsentHost(String name) {
            myName = name;
        }
        
        public Object getProperty(String key) {
            return null;
        }

        public void setProperty(String key, Object value) {
        }

        public WebServerProvider getProvider() {
            return null;
        }

        public String getDisplayName() {
            return myName;
        }

        public String getServerName() {
            return getDisplayName();
        }

        @Override
        public String toString() {
            return getDisplayName();
        }

        public String getId() {
            return getDisplayName();
        }
        
        private String myName;
    }
    protected class NoHost extends AbsentHost{

        public NoHost() {
            super("No Server");
        }

        public String getId() {
            return "9846573n2cm#$%jsdlc32@$%^35947534mcfoei%^%^8709gfgjhkfghjl";
        }
        
    }
    
    protected NoHost getNoHost(){
        return myNoHost;
    }
    
    private Vector<Host> myHostsVector;
    private NoHost myNoHost = new NoHost();
    
    private javax.swing.JPanel myProviderPanel;
}
