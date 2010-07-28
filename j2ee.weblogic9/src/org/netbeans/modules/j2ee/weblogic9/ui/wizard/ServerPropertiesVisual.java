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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The second panel of the custom wizard used for registering an instance of
 * the server. Here user should choose among the the existing local instances,
 * or enter the host/port/username/password conbination for a remote one
 *
 * @author Kirill Sorokin
 * @author Petr Hejl
 */
public class ServerPropertiesVisual extends javax.swing.JPanel {

    private static final String DEFAULT_USERNAME = "weblogic"; // NOI18N

    private transient WLInstantiatingIterator instantiatingIterator;
    
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
    }
    
    public boolean valid(WizardDescriptor wizardDescriptor) {
        // clear the error message
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);

        // check the profile root directory for validity
        if (!isValidDomainRoot(domainPathField.getText())) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerPropertiesVisual.class, "ERR_INVALID_DOMAIN_ROOT"))); // NOI18N
            return false;
        }

        if (InstanceProperties.getInstanceProperties(getUrl()) != null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerPropertiesVisual.class, "ERR_ALREADY_REGISTERED"))); // NOI18N
            return false;
        }

        // check the host field (not empty)
        if (hostField.getText().trim().length() < 1) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerPropertiesVisual.class, "ERR_INVALID_HOST"))); // NOI18N
        }

        // check the port field (not empty and a positive integer)
        if (portField.getText().trim().length() < 1) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerPropertiesVisual.class, "ERR_EMPTY_PORT"))); // NOI18N
        }
        if (!portField.getText().trim().matches("[0-9]+")) {  // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerPropertiesVisual.class, "ERR_INVALID_PORT"))); // NOI18N
        }

        Instance item = (Instance) localInstancesCombo.getSelectedItem();
        // show a hint for sample domain
        if (item != null && item.getName().startsWith("examples")) { // NOI18N
            if (passwordField.getPassword().length <= 0) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                        WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerPropertiesVisual.class, "ERR_EMPTY_PASSWORD"))); // NOI18N
            }
        }

        // save the data to the parent instantiating iterator
        instantiatingIterator.setUrl(getUrl());
        instantiatingIterator.setDomainRoot(domainPathField.getText());
        instantiatingIterator.setUsername(usernameField.getText());
        instantiatingIterator.setPassword(new String(passwordField.getPassword()));
        instantiatingIterator.setPort(portField.getText().trim());
        instantiatingIterator.setDomainName( item.getDomainName() );

        // everything seems ok
        return true;
    }

    private String getUrl() {
        return WLDeploymentFactory.URI_PREFIX + hostField.getText()
                + ":" + portField.getText() + ":" + instantiatingIterator.getServerRoot() // NOI18N;
                + ":" + domainPathField.getText(); // NOI18N;
    }

    /**
     * Checks whether the specified path is the valid domain root directory.
     *
     * @return true if the path is the valid domain root, false otherwise
     */
    private boolean isValidDomainRoot(String path) {
        // set the child directories/files that should be present and validate
        // the directory as the domain root

        // the layout is different for 90b and 90, temporarilly leaving both
        // versions in for testing TODO: remove
        String[] children = {
                    "servers", // NOI18N
                    "config", // NOI18N
                    "config/config.xml", // NOI18N
                    "init-info/domain-info.xml", // NOI18N
        };
        boolean is90 = hasChildren(path, children);
        String[] children90b = {
                    "servers", // NOI18N
                    "config", // NOI18N
                    "config/config.xml", // NOI18N
                    "domain-info.xml", // NOI18N
        };
        boolean is90b = hasChildren(path, children90b);
        return is90 || is90b;
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
    private Vector getServerInstances() {
        // initialize the resulting vector
        Vector result = new Vector();

        // get the list of registered profiles
        String[] domains = WLPluginProperties
                .getRegisteredDomainPaths(instantiatingIterator.getServerRoot());

        // for each domain get the list of instances
        for (int i = 0; i < domains.length; i++) {

            Properties properties = WLPluginProperties.getDomainProperties(domains[i]);
            String name = properties.getProperty( WLPluginProperties.ADMIN_SERVER_NAME);
            String port = properties.getProperty( WLPluginProperties.PORT_ATTR);
            String host = properties.getProperty( WLPluginProperties.HOST_ATTR );
            String domainName = properties.getProperty( WLPluginProperties.DOMAIN_NAME );
            if ((name != null) && (!name.equals(""))) { // NOI18N
                // address and port have minOccurs=0 and are missing in 90
                // examples server
                port = (port == null || port.equals("")) // NOI18N
                ? Integer.toString(WLDeploymentFactory.DEFAULT_PORT)
                        : port;
                host = (host == null || host.equals("")) ? "localhost" // NOI18N
                        : host;
                result.add(new Instance(name, host, port, domains[i], domainName));
            }
        }

        // convert the vector to an array and return
        return result;
    }

    /**
     * Updates the local instances combobox model with the fresh local
     * instances list
     */
    public void updateInstancesList() {
        localInstancesCombo.setModel(new InstancesModel(getServerInstances()));
        updateInstanceInfo();
    }

    /**
     * Updates the selected local instance information, i.e. profile path,
     * host, port.
     */
    private void updateInstanceInfo() {
        // get the selected local instance
        Instance instance = (Instance) localInstancesCombo.getSelectedItem();

        if (instance != null) {
            // set the fields' values
            domainPathField.setText(instance.getDomainPath());
            hostField.setText(instance.getHost());
            portField.setText(instance.getPort());
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Listeners section
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The registrered listeners vector
     */
    private final Vector listeners = new Vector();

    /**
     * Removes a registered listener
     *
     * @param listener the listener to be removed
     */
    public void removeChangeListener(ChangeListener listener) {
        if (listeners != null) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
    }

    /**
     * Adds a listener
     *
     * @param listener the listener to be added
     */
    public void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Fires a change event originating from this panel
     */
    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        fireChangeEvent(event);
    }

    /**
     * Fires a custom change event
     *
     * @param event the event
     */
    private void fireChangeEvent(ChangeEvent event) {
        Vector targetListeners;
        synchronized (listeners) {
            targetListeners = (Vector) listeners.clone();
        }

        for (int i = 0; i < targetListeners.size(); i++) {
            ChangeListener listener = (ChangeListener) targetListeners.elementAt(i);
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
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        UpdateListener updateListener = new UpdateListener();
        localInstancesLabel = new javax.swing.JLabel();
        localInstancesCombo = new javax.swing.JComboBox(new InstancesModel(getServerInstances()));
        domainPathLabel = new javax.swing.JLabel();
        domainPathField = new javax.swing.JTextField();
        hostLabel = new javax.swing.JLabel();
        hostField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        usernameLabel = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        localInstancesLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        localInstancesLabel.setLabelFor(localInstancesCombo);
        org.openide.awt.Mnemonics.setLocalizedText(localInstancesLabel, org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "LBL_LOCAL_INSTANCE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(localInstancesLabel, gridBagConstraints);

        localInstancesCombo.addItemListener(new LocalInstancesItemListener());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
        jPanel1.add(localInstancesCombo, gridBagConstraints);
        localInstancesCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ACSD_ServerPropertiesPanel_localInstancesCombo")); // NOI18N

        domainPathLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        domainPathLabel.setLabelFor(domainPathField);
        org.openide.awt.Mnemonics.setLocalizedText(domainPathLabel, org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "LBL_DOMAIN_LOCATION")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(domainPathLabel, gridBagConstraints);

        domainPathField.setColumns(20);
        domainPathField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
        jPanel1.add(domainPathField, gridBagConstraints);
        domainPathField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ACSD_ServerPropertiesPanel_domainPathField")); // NOI18N

        hostLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        hostLabel.setLabelFor(hostField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "LBL_HOST")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(hostLabel, gridBagConstraints);

        hostField.setEditable(false);
        hostField.getDocument().addDocumentListener(updateListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
        jPanel1.add(hostField, gridBagConstraints);
        hostField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ACSD_ServerPropertiesPanel_hostField")); // NOI18N

        portLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        portLabel.setLabelFor(portField);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "LBL_PORT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(portLabel, gridBagConstraints);

        portField.setColumns(15);
        portField.setEditable(false);
        portField.getDocument().addDocumentListener(updateListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
        jPanel1.add(portField, gridBagConstraints);
        portField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ACSD_ServerPropertiesPanel_portField")); // NOI18N

        usernameLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        usernameLabel.setLabelFor(usernameField);
        org.openide.awt.Mnemonics.setLocalizedText(usernameLabel, org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "LBL_USERNAME")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(usernameLabel, gridBagConstraints);

        usernameField.setColumns(15);
        usernameField.setText(DEFAULT_USERNAME);
        usernameField.getDocument().addDocumentListener(updateListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 0);
        jPanel1.add(usernameField, gridBagConstraints);
        usernameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ACSD_ServerPropertiesPanel_usernameField")); // NOI18N

        passwordLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "LBL_PASSWORD")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(passwordLabel, gridBagConstraints);

        passwordField.setColumns(15);
        passwordField.getDocument().addDocumentListener(updateListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(passwordField, gridBagConstraints);
        passwordField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerPropertiesVisual.class, "ACSD_ServerPropertiesPanel_passwordField")); // NOI18N

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField domainPathField;
    private javax.swing.JLabel domainPathLabel;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox localInstancesCombo;
    private javax.swing.JLabel localInstancesLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
    

    private class UpdateListener implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
            fireChangeEvent();
        }

        public void removeUpdate(DocumentEvent e) {
            fireChangeEvent();
        }

        public void insertUpdate(DocumentEvent e) {
            fireChangeEvent();
        }
    }

    /**
     * Simple listener for instance combo box changes.
     */
    private class LocalInstancesItemListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            updateInstanceInfo();
            isValid();
        }

    }


    /**
     * A combobox model that represents the list of local instances. It
     * contains a vector of objects of Instance class that contain all data
     * for the instance
     *
     * @author Kirill Sorokin
     */
    private static class InstancesModel extends AbstractListModel implements ComboBoxModel {
        /**
         * A vector with the instances
         */
        private Vector instances;

        /**
         * The index of the selected instance, or -1 if there is no selection
         */
        private int selectedIndex = 0;

        /**
         * Creates a new instance of InstancesModel
         *
         * @param instances a vector with the locally found instances
         */
        public InstancesModel(Vector instances) {
            // save the instances
            this.instances = instances;

            // set the selected index to zero
            this.selectedIndex = 0;
        }

        /**
         * Sets the selected index to the index of the supplied item
         *
         * @param item the instance which should be selected
         */
        public void setSelectedItem(Object item) {
            // set the index to the given item's index or to -1
            // if the item does not exists
            selectedIndex = instances.indexOf(item);
        }

        /**
         * Get the instance with the specified instance
         *
         * @param index the index of the desired instance
         *
         * @return the instance at the given index
         */
        public Object getElementAt(int index) {
            return instances.elementAt(index);
        }

        /**
         * Returns the total number of instances
         *
         * @return the number of instances
         */
        public int getSize() {
            return instances.size();
        }

        /**
         * Returns the instance at the selected index
         *
         * @return the instance at the selected index
         */
        public Object getSelectedItem() {
            // if there are no instances return null
            if (instances.size() == 0) {
                return null;
            }
            // #168297
            if (selectedIndex == -1) {
                return null;
            }

            // return the element at the index
            return instances.elementAt(selectedIndex);
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
         * Creates a new instance of Instance
         *
         * @param name the instance's name
         * @param host the instance's host
         * @param port the instance's port
         * @param domainPath the instance's profile path
         */
        public Instance(String name, String host, String port, String domainPath,
                String domainName) 
        {
            // save the properties
            this.name = name;
            this.host = host;
            this.port = port;
            this.domainPath = domainPath;
            this.domainName = domainName;
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
         * An overriden version of the Object's toString() so that the
         * instance is displayed properly in the combobox
         */
        @Override
        public String toString() {
            return name + " [" + host + ":" + port + "]"; // NOI18N
        }
    }
}
