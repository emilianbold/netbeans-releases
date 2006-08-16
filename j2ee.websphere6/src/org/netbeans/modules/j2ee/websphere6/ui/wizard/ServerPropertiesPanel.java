/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.ui.wizard;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.openide.*;
import org.openide.awt.Mnemonics;
import org.openide.util.*;
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
     * Since the WizardDescriptor does not expose the property name for the
     * error message label, we have to keep it here also
     */
    private final static String PROP_ERROR_MESSAGE =
            "WizardPanel_errorMessage";                                // NOI18N
    
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
        putClientProperty("WizardPanel_contentData", steps);           // NOI18N
        putClientProperty("WizardPanel_contentSelectedIndex",
                new Integer(index));                                   // NOI18N
        
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
    public boolean isValid() {
        // clear the error message
        wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, "");          // NOI18N
        
        // if the server instance is local, then check the profile root
        // directory for validity
        if (serverTypeCombo.getSelectedItem().equals(NbBundle.getMessage(
                Customizer.class,
                "TXT_ServerTypeLocal"))) {                             // NOI18N
            if (!wizardServerProperties.isValidDomainRoot(domainPathField.getText())) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE,
                        NbBundle.getMessage(ServerPropertiesPanel.class,
                        "ERR_INVALID_DOMAIN_ROOT"));                           // NOI18N
                return false;
            }
        }
        
        // check the host field (not empty)
        if (hostField.getText().trim().equals("")) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ServerPropertiesPanel.class,
                    "ERR_INVALID_HOST"));                              // NOI18N
        }
        
        // check the port field (not empty and a positive integer)
        //if (!portField.getValue().toString().trim().matches("[0-9]+")) {
        if (!portField.getText().trim().matches("[0-9]+")) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ServerPropertiesPanel.class,
                    "ERR_INVALID_PORT"));                              // NOI18N
        }
        if (portField.getText().trim().matches("[0-9]+") &&
                new java.lang.Integer(portField.getText().trim()).intValue()>65535) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ServerPropertiesPanel.class,
                    "ERR_INVALID_PORT"));                              // NOI18N
        }
        
        // no checks for username & password as they may be intentionally blank
        
        // save the data to the parent instantiating iterator
        instantiatingIterator.setDomainRoot(domainPathField.getText());
        instantiatingIterator.setHost(hostField.getText());
        //instantiatingIterator.setPort(portField.getValue().toString());
        instantiatingIterator.setPort(portField.getText());
        instantiatingIterator.setUsername(usernameField.getText());
        instantiatingIterator.setPassword(new String(
                passwordField.getPassword()));
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
    private JPanel formattingPanel;
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
        formattingPanel = new JPanel();
        serverTypeLabel = new JLabel();
        serverTypeCombo = new JComboBox(new Object[] {NbBundle.getMessage(
                Customizer.class, "TXT_ServerTypeLocal")/*,
        //NbBundle.getMessage(Customizer.class,
                "TXT_ServerTypeRemote")*/});// NOI18N
        localInstanceLabel = new JLabel();
        localInstancesCombo = new JComboBox(new InstancesModel(
                wizardServerProperties.getServerInstances(
                instantiatingIterator.getServerRoot())));
        remoteWarningLabel = new JTextArea(NbBundle.getMessage(
                ServerPropertiesPanel.class, 
                "LBL_remoteIncompatibilityWarning"));                  // NOI18N
        
        // set the desired layout
        setLayout(new GridBagLayout());
        
        wizardServerProperties.setVariables(serverTypeCombo,localInstancesCombo,domainPathField,hostField,portField);
        
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
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
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
        add(passwordField, gridBagConstraints);
        
        // remote warning label
        remoteWarningLabel.setEditable(false);
        remoteWarningLabel.setWrapStyleWord(true);
        remoteWarningLabel.setLineWrap(true);
        remoteWarningLabel.setOpaque(false);
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
    private Vector listeners = new Vector();
    
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
            ChangeListener listener =
                    (ChangeListener) targetListeners.elementAt(i);
            listener.stateChanged(event);
        }
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
        public void keyTyped(KeyEvent event) {
            fireChangeEvent();
        }
        
        /**
         * This method is called when a user releases a key on the keyboard
         */
        public void keyReleased(KeyEvent event) {
            fireChangeEvent();
        }
    }
}