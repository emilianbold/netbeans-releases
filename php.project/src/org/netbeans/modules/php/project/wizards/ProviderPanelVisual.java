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

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.ProjectConfigProvider;
import org.netbeans.modules.php.rt.spi.providers.ProjectWizardComponent;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.openide.WizardDescriptor;

/**
 *
* TODO: was quickly updated to show design. Needs refactoring.
 * should use the same UI panel as customizer dialog
  * @author  ads
 */
class ProviderPanelVisual extends JPanel {

    private static final long serialVersionUID = 6317907249928135438L;

    ProviderPanelVisual(ProviderSpecificPanel panel) {
        myPanel = panel;
        initComponents();
        // temporary. while there is no additional options
        mySeparator.setVisible(false);
    }

    boolean dataIsValid(WizardDescriptor descriptor) {
        descriptor.putProperty(
                NewPhpProjectWizardIterator.WIZARD_PANEL_ERROR_MESSAGE, "");
        boolean flag = true;
        if (myHostsPanel != null) {
            flag = myHostsPanel.dataIsValid();
        }
        if (getProviderComoponent() != null) {
            flag = flag && getProviderComoponent().isContentValid();
        }
        /*
         * There is no sense to validate Version panel.
         */
        return flag;
    }

    void read(WizardDescriptor descriptor) {
        configureProviderPanel(descriptor);
        validate();
    }

    void store(WizardDescriptor descriptor) {
        //REMOVED
        //myVersionPanel.store(descriptor);
        if (myHostsPanel != null) {
            myHostsPanel.store(descriptor);
        }

        storeServerConfig(descriptor);
    }

    ProviderSpecificPanel getPanel() {
        return myPanel;
    }

    void useServerConfig(ProjectConfigProvider provider, Host host) {
        if (getProviderPanel() != null) {
            myProviderPanel.remove(getProviderPanel());
        }
        if (provider != null){
            ProjectWizardComponent comp = provider.getWizardComponent(host);
            myProviderPanel.add(BorderLayout.NORTH, comp.getPanel());
            myProviderComponent = comp;
        }
        myProviderPanel.validate();
        validate();
    }

    void storeServerConfig(WizardDescriptor descriptor){
        if (getProviderComoponent() != null) {
            getProviderComoponent().store(descriptor);
        }
    }

    void readServerConfig(WizardDescriptor descriptor){
        if (getProviderComoponent() != null) {
            getProviderComoponent().read(descriptor);
        }
    }

    private void configureProviderPanel(WizardDescriptor descriptor) {
        if (myHostsPanel != null) {
            myChoosePanel.remove(myHostsPanel);
        }
        myHostsPanel = new HostPanelVisual(this);
        myChoosePanel.add(BorderLayout.NORTH, myHostsPanel);
        myHostsPanel.read(descriptor);
        myChoosePanel.validate();
        
        readServerConfig(descriptor);
    }

    private ProjectWizardComponent getProviderComoponent() {
        return myProviderComponent;
    }
    private JPanel getProviderPanel() {
        if (getProviderComoponent() != null){
            return getProviderComoponent().getPanel();
        }
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        myChoosePanel = new javax.swing.JPanel();
        myProviderPanel = new javax.swing.JPanel();
        mySeparator = new javax.swing.JSeparator();
        myAdditionalConfiguration = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        myChoosePanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(myChoosePanel, gridBagConstraints);

        myProviderPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(myProviderPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        add(mySeparator, gridBagConstraints);

        myAdditionalConfiguration.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(myAdditionalConfiguration, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
            // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel myAdditionalConfiguration;
    private javax.swing.JPanel myChoosePanel;
    private javax.swing.JPanel myProviderPanel;
    private javax.swing.JSeparator mySeparator;
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
    

    private ProviderSpecificPanel myPanel;

    private HostPanelVisual myHostsPanel;

    //REMOVED
    //private PhpVersionPanel myVersionPanel;
    private ProjectWizardComponent myProviderComponent;

    private NoHost myNoHost = new NoHost();
}
