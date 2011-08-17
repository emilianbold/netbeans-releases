/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.j2ee.weblogic9.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLJpa2SwitchSupport;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * The second panel of the custom wizard used for registering an instance of
 * the server. Here user should choose among the the existing local instances,
 * or enter the host/port/username/password conbination for a remote one
 *
 * @author Petr Hejl
 */
public class ServerPropertiesVisual extends javax.swing.JPanel {

    private transient WLInstantiatingIterator instantiatingIterator;

    private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

    private final List<Instance> instances = new ArrayList<Instance>();
    
    private WLJpa2SwitchSupport support;
    
    /**
     * Creates a new instance of the ServerPropertiesVisual. It initializes all
     * the GUI components that appear on the panel.
     *
     * @param steps the names of the steps in the wizard
     * @param index index of this panel in the wizard
     * @param listener a listener that will propagate the chage event higher in
     *      the hierarchy
     * @param instantiatingIterator the parent instantiating iterator
     */
    public ServerPropertiesVisual(WLInstantiatingIterator instantiatingIterator) {
        // save the instantiating iterator
        this.instantiatingIterator = instantiatingIterator;

        // set the panel's name
        setName(NbBundle.getMessage(
                ServerPropertiesPanel.class, "SERVER_PROPERTIES_STEP") );  // NOI18N

        initComponents();
        
        localInstancesCombo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                fireChangeEvent();
            }
        });
        localInstancesCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fireChangeEvent();
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                fireChangeEvent();
            }
        });
        passwordField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                fireChangeEvent();
            }
        });
    }
    
    public boolean valid(WizardDescriptor wizardDescriptor) {
        // clear the error message
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);


        // perhaps we could use just strings in combo
        // not sure about the domain with same name - use directory ?
        Object item = localInstancesCombo.getEditor().getItem();
        Instance instance = null;
        if (item instanceof Instance) {
            instance = (Instance) item;
        } else {
            instance = getServerInstance(item.toString().trim());
        }
        if (instance == null) {
            String value = item.toString().trim();
            for (Instance inst : instances) {
                if (value.equals(inst.getDomainName())) {
                    instance = inst;
                    break;
                }
            }
        }

        // check the profile root directory for validity
        if (instance == null || !isValidDomainRoot(instance.getDomainPath())) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerPropertiesVisual.class, "ERR_INVALID_DOMAIN_ROOT"))); // NOI18N
            return false;
        }

        if (instance != null && InstanceProperties.getInstanceProperties(getUrl(instance)) != null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerPropertiesVisual.class, "ERR_ALREADY_REGISTERED"))); // NOI18N
            return false;
        }

        if (instance != null && instance.getDomainVersion() != null
                && instantiatingIterator.getServerVersion() != null
                && !instantiatingIterator.getServerVersion().equals(instance.getDomainVersion())) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(
                            ServerPropertiesVisual.class, "ERR_INVALID_DOMAIN_VERSION"))); // NOI18N
            return false;
        }
        
        if (instance != null && instance.isProductionModeEnabled()){
            wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(
                            ServerPropertiesVisual.class, "WARN_PRODUCTION_MODE"))); // NOI18N
        }

        // show a hint for sample domain
        if (instance != null && instance.getName().startsWith("examples")) { // NOI18N
            if (passwordField.getPassword().length <= 0) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                        WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerPropertiesVisual.class, "ERR_EMPTY_PASSWORD"))); // NOI18N
            }
        }

        if (instance != null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(this.getClass(), "MSG_RegisterExisting", instance.getDomainName()))); // NOI18N

            // save the data to the parent instantiating iterator
            instantiatingIterator.setUrl(getUrl(instance));
            instantiatingIterator.setDomainRoot(instance.getDomainPath());
            instantiatingIterator.setUsername(usernameField.getText());
            instantiatingIterator.setPassword(new String(passwordField.getPassword()));
            instantiatingIterator.setPort(instance.getPort());
            instantiatingIterator.setDomainName(instance.getDomainName());
            instantiatingIterator.setHost(instance.getHost());
        }
        return true;
    }

    private String getUrl(Instance instance) {
        return WLDeploymentFactory.URI_PREFIX + instance.getHost()
                + ":" + instance.getPort() + ":" + instantiatingIterator.getServerRoot() // NOI18N;
                + ":" + instance.getDomainPath(); // NOI18N;
    }

    /**
     * Checks whether the specified path is the valid domain root directory.
     *
     * @return true if the path is the valid domain root, false otherwise
     */
    private boolean isValidDomainRoot(String path) {
        // set the child directories/files that should be present and validate
        // the directory as the domain root

        String[] children = {
                    "servers", // NOI18N
                    "config", // NOI18N
                    "config/config.xml", // NOI18N
        };
        return hasChildren(path, children);
    }

    /**
     * Checks whether the supplied directory has the required children
     *
     * @return true if the directory contains all the children, false otherwise
     */
    private boolean hasChildren(String parent, String[] children) {
        // if parent is null, it cannot contain any children
        if (parent == null) {
            return false;
        }

        // if the children array is null, then the condition is fullfilled
        if (children == null) {
            return true;
        }

        // for each child check whether it is contained and if it is not,
        // return false
        for (int i = 0; i < children.length; i++) {
            if (!(new File(parent + File.separator + children[i]).exists())) {
                return false;
            }
        }

        // all is good
        return true;
    }

    /**
     * Gets the list of local server instances.
     *
     * @return a vector with the local instances
     */
    private List<Instance> getServerInstances() {
        // initialize the resulting vector
        List<Instance> result = new ArrayList<Instance>();

        // get the list of registered profiles
        String[] domains = WLPluginProperties
                .getRegisteredDomainPaths(instantiatingIterator.getServerRoot());

        // for each domain get the list of instances
        for (int i = 0; i < domains.length; i++) {
            Instance localInstance = getServerInstance(domains[i]);
            if (localInstance != null) {
                result.add(localInstance);
            }
        }

        // convert the vector to an array and return
        return result;
    }
    
    private Instance getServerInstance(String domainPath) {
        Properties properties = WLPluginProperties.getDomainProperties(domainPath);
        if (properties.isEmpty()) {
            return null;
        }

        String name = properties.getProperty(WLPluginProperties.ADMIN_SERVER_NAME);
        String port = properties.getProperty(WLPluginProperties.PORT_ATTR);
        String host = properties.getProperty(WLPluginProperties.HOST_ATTR);
        String domainName = properties.getProperty(WLPluginProperties.DOMAIN_NAME);
        String versionString = properties.getProperty(WLPluginProperties.DOMAIN_VERSION);
        Version domainVersion = versionString != null ? Version.fromJsr277OrDottedNotationWithFallback(versionString) : null;

        Boolean isProductionMode = (Boolean)properties.get(
                WLPluginProperties.PRODUCTION_MODE);
        if ((name != null) && (!name.equals(""))) { // NOI18N
            // address and port have minOccurs=0 and are missing in 90
            // examples server
            port = (port == null || port.equals("")) // NOI18N
            ? Integer.toString(WLDeploymentFactory.DEFAULT_PORT)
                    : port;
            host = (host == null || host.equals("")) ? "localhost" // NOI18N
                    : host;
            return new Instance(name, host, port, domainPath, domainName, domainVersion,
                    isProductionMode != null && isProductionMode);
        }
        return null;
    }


    /**
     * Updates the local instances combobox model with the fresh local
     * instances list
     */
    public void updateInstancesList() {
        instances.clear();
        instances.addAll(getServerInstances());
        localInstancesCombo.setModel(new DefaultComboBoxModel(instances.toArray()));
    }
    
    public void updateJpa2Button() {
        File root = new File(instantiatingIterator.getServerRoot());
        support = new WLJpa2SwitchSupport(root);
        boolean statusVisible = support.isSwitchSupported();
        boolean buttonVisible = statusVisible
                && !support.isEnabledViaSmartUpdate();

        jpa2SwitchLabel.setVisible(statusVisible);
        jpa2Status.setVisible(statusVisible);
        jpa2SwitchButton.setVisible(buttonVisible);
        updateJpa2Status();
    }
    
    private void updateJpa2Status() {
        if (support.isEnabled() || support.isEnabledViaSmartUpdate()) {
            jpa2Status.setText(NbBundle.getMessage(ServerPropertiesVisual.class, "ServerPropertiesVisual.jpa2Status.enabledText"));
            Mnemonics.setLocalizedText(jpa2SwitchButton, NbBundle.getMessage(ServerPropertiesVisual.class, "ServerPropertiesVisual.jpa2SwitchButton.disableText"));
        } else {
            jpa2Status.setText(NbBundle.getMessage(ServerPropertiesVisual.class, "ServerPropertiesVisual.jpa2Status.disabledText"));
            Mnemonics.setLocalizedText(jpa2SwitchButton, NbBundle.getMessage(ServerPropertiesVisual.class, "ServerPropertiesVisual.jpa2SwitchButton.enableText"));
        }         
    }    

    /**
     * Adds a listener
     *
     * @param listener the listener to be added
     */
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a registered listener
     *
     * @param listener the listener to be removed
     */
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        localInstancesLabel = new javax.swing.JLabel();
        localInstancesCombo = new javax.swing.JComboBox(new javax.swing.DefaultComboBoxModel(getServerInstances().toArray()));
        usernameLabel = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        browseButton = new javax.swing.JButton();
        explanationLabel = new javax.swing.JLabel();
        jpa2SwitchLabel = new javax.swing.JLabel();
        jpa2Status = new javax.swing.JLabel();
        jpa2SwitchButton = new javax.swing.JButton();

        localInstancesLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        localInstancesLabel.setLabelFor(localInstancesCombo);
        org.openide.awt.Mnemonics.setLocalizedText(localInstancesLabel, org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "LBL_LOCAL_INSTANCE")); // NOI18N

        localInstancesCombo.setEditable(true);
        localInstancesCombo.addItemListener(new LocalInstancesItemListener());

        usernameLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        usernameLabel.setLabelFor(usernameField);
        org.openide.awt.Mnemonics.setLocalizedText(usernameLabel, org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "LBL_USERNAME")); // NOI18N

        usernameField.setColumns(15);
        usernameField.setText("weblogic"); // NOI18N

        passwordLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "LBL_PASSWORD")); // NOI18N

        passwordField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ServerPropertiesVisual.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        explanationLabel.setText(org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ServerPropertiesVisual.explanationLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jpa2SwitchLabel, org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ServerPropertiesVisual.jpa2SwitchLabel.text")); // NOI18N

        jpa2Status.setText(org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ServerPropertiesVisual.jpa2Status.disabledText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jpa2SwitchButton, org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ServerPropertiesVisual.jpa2SwitchButton.enableText")); // NOI18N
        jpa2SwitchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jpa2SwitchButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(localInstancesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(localInstancesCombo, 0, 298, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton))
            .addComponent(explanationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jpa2SwitchLabel)
                    .addComponent(passwordLabel)
                    .addComponent(usernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(passwordField, 0, 0, Short.MAX_VALUE)
                        .addComponent(usernameField, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))
                    .addComponent(jpa2Status))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpa2SwitchButton)
                .addContainerGap(53, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(localInstancesLabel)
                    .addComponent(localInstancesCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(explanationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jpa2SwitchLabel)
                    .addComponent(jpa2Status)
                    .addComponent(jpa2SwitchButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        localInstancesCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ACSD_ServerPropertiesPanel_localInstancesCombo")); // NOI18N
        usernameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ACSD_ServerPropertiesPanel_usernameField")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ACSD_ServerPropertiesPanel_passwordField")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        Object item = localInstancesCombo.getEditor().getItem();
        if (item != null && item.toString().trim().length() > 0) {
            chooser.setSelectedFile(new File(item.toString()));
        } else {
            chooser.setSelectedFile(new File(instantiatingIterator.getServerRoot()));
        }
        if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            localInstancesCombo.getEditor().setItem(chooser.getSelectedFile().getAbsolutePath());
            fireChangeEvent();
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void jpa2SwitchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jpa2SwitchButtonActionPerformed
        if (!support.isEnabled()) {
            support.enable();
        } else {
            support.disable();
        }
        updateJpa2Status();
    }//GEN-LAST:event_jpa2SwitchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel explanationLabel;
    private javax.swing.JLabel jpa2Status;
    private javax.swing.JButton jpa2SwitchButton;
    private javax.swing.JLabel jpa2SwitchLabel;
    private javax.swing.JComboBox localInstancesCombo;
    private javax.swing.JLabel localInstancesLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
    

    /**
     * Simple listener for instance combo box changes.
     */
    private class LocalInstancesItemListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            fireChangeEvent();
        }

    }

    /**
     * A model for the server instance. It contains all the critical properties
     * for the plugin: name, host, port, profile path, domain name.
     *
     * @author Kirill Sorokin
     */
    private static class Instance {

        /**
         * Instance's name, it is used a the parameter to the startup/shutdown
         * scripts
         */
        private String name;

        /**
         * Instance's host
         */
        private String host;

        /**
         * Instance's port
         */
        private String port;

        /**
         * Instance's profile directory
         */
        private String domainPath;
        
        /**
         * Instance's domain name
         */
        private String domainName;
        
        /**
         * Production mode is enabled for domain.
         */
        private boolean isProductionModeEnabled;

        private Version domainVersion;

        /**
         * Creates a new instance of Instance
         *
         * @param name the instance's name
         * @param host the instance's host
         * @param port the instance's port
         * @param domainPath the instance's profile path
         */
        public Instance(String name, String host, String port, String domainPath,
                String domainName, Version domainVersion, boolean isProductionModeEnabled) {
            // save the properties
            this.name = name;
            this.host = host;
            this.port = port;
            this.domainPath = domainPath;
            this.domainName = domainName;
            this.domainVersion = domainVersion;
            this.isProductionModeEnabled = isProductionModeEnabled;
        }

        /**
         * Getter for the instance's name
         *
         * @return the instance's name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Setter for the instance's name
         *
         * @param the new instance's name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Getter for the domain name
         *
         * @return the domain name
         */
        public String getDomainName() {
            return this.domainName;
        }

        /**
         * Setter for the domain name
         *
         * @param the new domain name
         */
        public void setDomainName(String name) {
            domainName = name;
        }

        /**
         * Getter for the instance's host
         *
         * @return the instance's host
         */
        public String getHost() {
            return this.host;
        }

        /**
         * Setter for the instance's host
         *
         * @param the new instance's host
         */
        public void setHost(String host) {
            this.host = host;
        }

        /**
         * Getter for the instance's port
         *
         * @return the instance's port
         */
        public String getPort() {
            return this.port;
        }

        /**
         * Setter for the instance's port
         *
         * @param the new instance's port
         */
        public void setPort(String port) {
            this.port = port;
        }

        /**
         * Getter for the instance's profile path
         *
         * @return the instance's profile path
         */
        public String getDomainPath() {
            return this.domainPath;
        }

        /**
         * Setter for the instance's profile path
         *
         * @param the new instance's profile path
         */
        public void setDomainPath(String domainPath) {
            this.domainPath = domainPath;
        }
        
        /**
         * 
         * Getter for domain production mode property. 
         * 
         * @return true if production mode is enabled for domain
         */
        public boolean isProductionModeEnabled(){
            return isProductionModeEnabled;
        }
        
        /**
         * Setter for production mode property.
         * 
         * @param productionMode isProductionModeEnabled property value
         */
        public void setProductionModeEnabled( boolean productionMode ){
            isProductionModeEnabled = productionMode;
        }

        public Version getDomainVersion() {
            return domainVersion;
        }

        public void setDomainVersion(Version domainVersion) {
            this.domainVersion = domainVersion;
        }

        /**
         * An overriden version of the Object's toString() so that the
         * instance is displayed properly in the combobox
         */
        @Override
        public String toString() {
            return domainPath;//domainName;
        }
    }
}
