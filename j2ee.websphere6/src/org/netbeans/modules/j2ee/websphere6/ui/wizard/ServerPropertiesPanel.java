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
package org.netbeans.modules.j2ee.websphere6.ui.wizard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import org.openide.awt.Mnemonics;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.websphere6.ui.Instance;
import org.netbeans.modules.j2ee.websphere6.ui.Customizer;
import org.netbeans.modules.j2ee.websphere6.ui.InstancesModel;
import org.netbeans.modules.j2ee.websphere6.ui.ServerProperties;

/**
 * The second panel of the custom wizard used for registering an instance of
 * the server. Here user should choose among the the existing local instances,
 * or enter the host/port/username/password conbination for a remote one
 *
 * @author Kirill Sorokin
 */
public class ServerPropertiesPanel extends JPanel
        implements WizardDescriptor.Panel {
    
    /**
     * The parent wizard descriptor handle
     */
    private transient WizardDescriptor wizardDescriptor;
    
    /**
     * The parent instantiaing iterator handle
     */
    private transient WSInstantiatingIterator instantiatingIterator;
    
    
    public class WizardServerProperties extends ServerProperties{
        public WizardServerProperties(JComboBox serverCombobox,
                JComboBox localInstancesCombobox,
                JTextField domainPathField,
                JTextField hostField,
                JTextField portField) {
            super(serverCombobox,localInstancesCombobox,domainPathField,hostField,portField);
        }
        public WizardServerProperties() {
            super();
        }
        
        public class WizardServerTypeActionListener extends ServerTypeActionListener{
            @Override
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                isValid();
            }
        }
    }
    
    private WizardServerProperties wizardServerProperties=new WizardServerProperties();
    
    /**
     * Returns wizardServerProperties;
     */
    public WizardServerProperties getWizardServerProperties(){
        return wizardServerProperties;
    }
    
    
    /**
     * Creates a new instance of the ServerPropertiesPanel. It initializes all
     * the GUI components that appear on the panel.
     *
     * @param steps the names of the steps in the wizard
     * @param index index of this panel in the wizard
     * @param listener a listener that will propagate the chage event higher in
     *      the hierarchy
     * @param instantiatingIterator the parent instantiating iterator
     */
    
    public ServerPropertiesPanel(String[] steps, int index,
            ChangeListener listener,
            WSInstantiatingIterator instantiatingIterator) {
        // save the instantiating iterator
        this.instantiatingIterator = instantiatingIterator;
        
        // set the required properties, so that the panel appear correct in
        // the steps
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                Integer.valueOf(index));
        
        // register the supplied listener
        addChangeListener(listener);
        
        // set the panel's name
        setName(steps[index]);
        
        // init the GUI
        init();
    }
    
    /**
     * Returns the named help article associated with this panel
     *
     * @return the associated help article
     */
    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_" +            // NOI18N
                "server_websphere");                                   // NOI18N
    }
    
    /**
     * Gets the panel's AWT Component object, in our case it coincides with this
     * object
     *
     * @return this
     */
    public Component getComponent() {
        return this;
    }
    
    /**
     * Checks whether the data input is valid
     *
     * @return true if the entered installation directory is valid, false
     *      otherwise
     */
    @Override
    public boolean isValid() {
        // clear the error message
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        
        // if the server instance is local, then check the profile root
        // directory for validity
        if (serverTypeCombo.getSelectedItem().equals(NbBundle.getMessage(
                Customizer.class,
                "TXT_ServerTypeLocal"))) {                             // NOI18N
            if (!WizardServerProperties.isValidDomainRoot(domainPathField.getText())) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ServerPropertiesPanel.class,
                        "ERR_INVALID_DOMAIN_ROOT"));                           // NOI18N
                return false;
            }
        }
        
        // check the host field (not empty)
        if (hostField.getText().trim().length() < 1) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                    NbBundle.getMessage(ServerPropertiesPanel.class,
                    "ERR_INVALID_HOST"));                              // NOI18N
        }
        
        // check the port field (not empty and a positive integer)
        if (portField.getText().trim().length() < 1) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                    NbBundle.getMessage(ServerPropertiesPanel.class,
                    "ERR_EMPTY_PORT"));                              // NOI18N
            return false;
        }
        if (!portField.getText().trim().matches("[0-9]+")) {  // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ServerPropertiesPanel.class,
                    "ERR_INVALID_PORT"));                              // NOI18N
            return false;
        }
        if (portField.getText().trim().matches("[0-9]+") &&     // NOI18N
                new java.lang.Integer(portField.getText().trim()).intValue()>65535) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ServerPropertiesPanel.class,
                    "ERR_INVALID_PORT"));                              // NOI18N
            return false;
        }
        
        if (((Instance) localInstancesCombo.getSelectedItem()).isSecurityEnabled()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ServerPropertiesPanel.class,
                    "ERR_SECURITY_ENABLED"));                              // NOI18N
            return false;
        }
        // no checks for username & password as they may be intentionally blank

        // save the data to the parent instantiating iterator
        instantiatingIterator.setDomainRoot(domainPathField.getText());
        instantiatingIterator.setHost(hostField.getText());
        instantiatingIterator.setPort(portField.getText());
        instantiatingIterator.setUsername(usernameField.getText());
        instantiatingIterator.setPassword(new String(passwordField.getPassword()));
        instantiatingIterator.setIsLocal(serverTypeCombo.getSelectedItem().
                equals(NbBundle.getMessage(Customizer.class,
                "TXT_ServerTypeLocal")) ? "true" : "false");           // NOI18N
        instantiatingIterator.setServerName(((Instance) localInstancesCombo.
                getSelectedItem()).getName());
        instantiatingIterator.setConfigXmlPath(((Instance) localInstancesCombo.
                getSelectedItem()).getConfigXmlPath());
        instantiatingIterator.setAdminPort(((Instance) localInstancesCombo.
                getSelectedItem()).getAdminPort());
        instantiatingIterator.setDefaultHostPort(((Instance) localInstancesCombo.
                getSelectedItem()).getDefaultHostPort());
        
        // everything seems ok
        return true;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    // JPanel section
    ////////////////////////////////////////////////////////////////////////////
    private JLabel domainPathLabel;
    private JLabel hostLabel;
    private JLabel portLabel;
    private JLabel userNameLabel;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JTextField domainPathField;
    private JTextField hostField;
    private JTextField portField;
    private JTextField usernameField;
    private JComboBox serverTypeCombo;
    private JComboBox localInstancesCombo;
    private JLabel localInstanceLabel;
    private JLabel serverTypeLabel;
    private JTextArea remoteWarningLabel;
    
    
    /**
     * Inits the GUI components
     */
    private void init() {
        // we use the GridBagLayout so we need the GridBagConstraints to
        // properly place the components
        GridBagConstraints gridBagConstraints;
        getAccessibleContext().setAccessibleDescription(
                java.util.ResourceBundle.getBundle(
                "org/netbeans/modules/j2ee/websphere6/ui/Bundle").    // NOI18N
                getString("MSG_ServerPropertiesPanelDescription"));    // NOI18N
        // initialize the components
        domainPathLabel = new JLabel();
        domainPathField = new JTextField();
        hostLabel = new JLabel();
        hostField = new JTextField();
        portLabel = new JLabel();
        portField = new JTextField();
        userNameLabel = new JLabel();
        usernameField = new JTextField();
        passwordLabel = new JLabel();
        passwordField = new JPasswordField();
        serverTypeLabel = new JLabel();
        serverTypeCombo = new JComboBox(new Object[] {NbBundle.getMessage(
                Customizer.class, "TXT_ServerTypeLocal")});// NOI18N
        localInstanceLabel = new JLabel();
        localInstancesCombo = new JComboBox(new InstancesModel(
                WizardServerProperties.getServerInstances(
                instantiatingIterator.getServerRoot())));
        remoteWarningLabel = new JTextArea(NbBundle.getMessage(
                ServerPropertiesPanel.class,
                "LBL_remoteIncompatibilityWarning"));                  // NOI18N
        
        // set the desired layout
        setLayout(new GridBagLayout());
        
        
        
        // add server type field label
        serverTypeLabel.setText(NbBundle.getMessage(
                Customizer.class, "LBL_LocalRemote"));       // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(serverTypeLabel, gridBagConstraints);
        
        // add server type combobox
        serverTypeCombo.addActionListener(wizardServerProperties.getServerTypeActionListener());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        serverTypeCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("TTL_AccessMethod"));
        serverTypeCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("MSG_AccessMethodDescription"));
        add(serverTypeCombo, gridBagConstraints);
        
        // add local instances field label
        localInstanceLabel.setText(NbBundle.getMessage(
                Customizer.class, "LBL_LocalInstances"));   // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(localInstanceLabel, gridBagConstraints);
        
        // add local instances combobox
        localInstancesCombo.addActionListener(
                wizardServerProperties.getInstanceSelectionListener());
        localInstancesCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fireChangeEvent();
            }
        });
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        localInstancesCombo.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("TTL_LocalInstances"));
        localInstancesCombo.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("MSG_LocalInstances"));
        add(localInstancesCombo, gridBagConstraints);
        
        // add domain path field label
        domainPathLabel.setText(NbBundle.getMessage(
                Customizer.class, "LBL_ProfilePath"));       // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(domainPathLabel, gridBagConstraints);
        
        // add domain path field
        domainPathField.setText(""); // NOI18N
        domainPathField.setEditable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        domainPathField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("TTL_ProfilePath"));
        domainPathField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("MSG_ProfilePath"));
        add(domainPathField, gridBagConstraints);
        
        // add host field label
        hostLabel.setText(NbBundle.getMessage(Customizer.class,
                "LBL_Host"));                                          // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(hostLabel, gridBagConstraints);
        
        // add host field
        hostField.setText(""); // NOI18N
        hostField.addKeyListener(new KeyListener());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        hostField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("TTL_Host"));
        hostField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("MSG_Host"));
        add(hostField, gridBagConstraints);
        hostField.setEditable(false);
        
        // add port field label
        portLabel.setText(NbBundle.getMessage(Customizer.class,
                "LBL_Port"));                                          // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(portLabel, gridBagConstraints);
        
        // add port field
        
        //portField.setModel(new SpinnerNumberModel(0,0,65535,1));
        //portField.setValue(new Integer(8880)); // NOI18N
        portField.setText("8880");// NOI18N
        portField.addKeyListener(new KeyListener());
        portField.setPreferredSize(new Dimension(50, 20));
        portField.setFont(hostField.getFont());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        portField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("TTL_Port"));
        portField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("MSG_Port"));
        add(portField, gridBagConstraints);
        portField.setEditable(false);
        
        // add username field label
        userNameLabel.setText(NbBundle.getMessage(Customizer.class,
                "LBL_Username"));                                      // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(userNameLabel, gridBagConstraints);
        
        // add username field
        usernameField.setText(""); // NOI18N
        usernameField.addKeyListener(new KeyListener());
        usernameField.setPreferredSize(new Dimension(100, 20));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        usernameField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("TTL_Username"));
        usernameField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("MSG_Username"));
        add(usernameField, gridBagConstraints);
        
        // add password field label
        passwordLabel.setText(NbBundle.getMessage(Customizer.class,
                "LBL_Password"));                                      // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(passwordLabel, gridBagConstraints);
        
        // add password field
        passwordField.setPreferredSize(new Dimension(100, 20));
        passwordField.addKeyListener(new KeyListener());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        passwordField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("TTL_Password"));
        passwordField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("MSG_Password"));
        add(passwordField, gridBagConstraints);
        
        // remote warning label
        remoteWarningLabel.setEditable(false);
        remoteWarningLabel.setWrapStyleWord(true);
        remoteWarningLabel.setLineWrap(true);
        remoteWarningLabel.setOpaque(false);
        remoteWarningLabel.getAccessibleContext().setAccessibleName(
                java.util.ResourceBundle.getBundle(
                "org/netbeans/modules/j2ee/websphere6/ui/wizard/Bundle").    // NOI18N
                getString("TTL_RemoteWarningA11Name"));    // NOI18N
        remoteWarningLabel.getAccessibleContext().setAccessibleDescription(
                java.util.ResourceBundle.getBundle(
                "org/netbeans/modules/j2ee/websphere6/ui/wizard/Bundle").    // NOI18N
                getString("MSG_RemoteWarningA11Description"));    // NOI18N
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(10, 0, 5, 0);
        add(remoteWarningLabel, gridBagConstraints);
        
        setMnemonics(domainPathLabel);
        domainPathLabel.setLabelFor(domainPathField);
        setMnemonics(serverTypeLabel);
        serverTypeLabel.setLabelFor(serverTypeCombo);
        setMnemonics(localInstanceLabel);
        localInstanceLabel.setLabelFor(localInstancesCombo);
        setMnemonics(hostLabel);
        hostLabel.setLabelFor(hostField);
        setMnemonics(portLabel);
        portLabel.setLabelFor(portField);
        setMnemonics(userNameLabel);
        userNameLabel.setLabelFor(usernameField);
        setMnemonics(passwordLabel);
        passwordLabel.setLabelFor(passwordField);
        
        wizardServerProperties.setVariables(serverTypeCombo,localInstancesCombo,
                domainPathField,hostField,portField,instantiatingIterator);
    }
    
    
    private void setMnemonics(JLabel label) {
        String name = label.getText();
        int index = Mnemonics.findMnemonicAmpersand(name);
        if(index < 0) {
            Mnemonics.setLocalizedText(label,name);
            label.setDisplayedMnemonic(name.charAt(0));
        } else {
            Mnemonics.setLocalizedText(label,name.substring(0,index) +  name.substring(index+1));
            label.setDisplayedMnemonic(name.charAt(index+1));
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    // Settings section
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Reads the supplied setting. The only one that can arrive this way is the
     * WizardDescriptor, thus we only convert the incoming object and save
     *
     * @param object the incoming setting (WizardDescriptor)
     */
    public void readSettings(Object object) {
        this.wizardDescriptor = (WizardDescriptor) object;
    }
    
    /**
     * Stores the supplied setting. I don't know the purpose of this method
     * thus we do not implement it
     */
    public void storeSettings(Object object) {
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Listeners section
    ////////////////////////////////////////////////////////////////////////////
    /**
     * The registrered listeners vector
     */
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    /**
     * Removes a registered listener
     *
     * @param listener the listener to be removed
     */
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
    
    /**
     * Adds a listener
     *
     * @param listener the listener to be added
     */
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    /**
     * Fires a change event originating from this panel
     */
    private void fireChangeEvent() {
        changeSupport.fireChange();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Simple key listener that delegates the event to its parent's listeners
     *
     * @author Kirill Sorokin
     */
    public class KeyListener extends KeyAdapter {
        /**
         * This method is called when a user presses a key on the keyboard
         */
        @Override
        public void keyTyped(KeyEvent event) {
            fireChangeEvent();
        }
        
        /**
         * This method is called when a user releases a key on the keyboard
         */
        @Override
        public void keyReleased(KeyEvent event) {
            fireChangeEvent();
        }
    }
}
