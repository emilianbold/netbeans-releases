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
package org.netbeans.modules.j2ee.weblogic9.ui.wizard;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.openide.*;
import org.openide.util.*;

/**
 * The second panel of the custom wizard used for registering an instance of
 * the server. Here user should choose among the the existing local instances,
 * or enter the host/port/username/password conbination for a remote one
 *
 * @author Kirill Sorokin
 */
public class ServerPropertiesPanel extends JPanel implements WizardDescriptor.Panel {
    /**
     * Since the WizardDescriptor does not expose the property name for the 
     * error message label, we have to keep it here also
     */
    private final static String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N
    
    /**
     * The parent wizard descriptor handle
     */
    private transient WizardDescriptor wizardDescriptor;
    
    /**
     * The parent instantiaing iterator handle
     */
    private transient WLInstantiatingIterator instantiatingIterator;
    
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
    public ServerPropertiesPanel(String[] steps, int index, ChangeListener listener, WLInstantiatingIterator instantiatingIterator) {
        // save the instantiating iterator
        this.instantiatingIterator = instantiatingIterator;
        
        // set the required properties, so that the panel appear correct in 
        // the steps
        putClientProperty("WizardPanel_contentData", steps); // NOI18N
        putClientProperty("WizardPanel_contentSelectedIndex", new Integer(index)); // NOI18N
        
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
        return new HelpCtx("j2eeplugins_registering_app_server_weblogic_properties"); //NOI18N
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
        wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, ""); // NOI18N
        
        // if the server instance is local, then check the profile root 
        // directory for validity
        if (serverTypeCombo.getSelectedItem().equals(NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_TYPE_LOCAL"))) { // NOI18N
            if (!isValidDomainRoot(domainPathField.getText())) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ServerPropertiesPanel.class, "ERR_INVALID_DOMAIN_ROOT")); // NOI18N
                return false;
            }
        }
        
        // check the host field (not empty)
        if (hostField.getText().trim().equals("")) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ServerPropertiesPanel.class, "ERR_INVALID_HOST")); // NOI18N
        }
        
        // check the port field (not empty and a positive integer)
        if (!portField.getText().trim().matches("[0-9]+")) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ServerPropertiesPanel.class, "ERR_INVALID_PORT")); // NOI18N
        }
        
        // no checks for username & password as they may be intentionally blank
        
        // save the data to the parent instantiating iterator
        instantiatingIterator.setDomainRoot(domainPathField.getText());
        instantiatingIterator.setHost(hostField.getText());
        instantiatingIterator.setPort(portField.getText());
        instantiatingIterator.setUsername(usernameField.getText());
        instantiatingIterator.setPassword(new String(passwordField.getPassword()));
        instantiatingIterator.setIsLocal(serverTypeCombo.getSelectedItem().equals(NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_TYPE_LOCAL")) ? "true" : "false"); // NOI18N
        
        // everything seems ok
        return true;
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
        serverTypeCombo = new JComboBox(new Object[] {NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_TYPE_LOCAL")/*, NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_TYPE_REMOTE")*/}); // NOI18N
        localInstanceLabel = new JLabel();
        localInstancesCombo = new JComboBox(new InstancesModel(getServerInstances()));
        serverTypeLabel = new JLabel();
        
        // set the desired layout
        setLayout(new GridBagLayout());
        
        //temporarily commented for issue #64480 (remote instances not supported in 5.0)
        
        // add server type field label
//        serverTypeLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_serverType")); // NOI18N
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.anchor = GridBagConstraints.EAST;
//        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
//        add(serverTypeLabel, gridBagConstraints);
//        
//        // add server type combobox
//        serverTypeCombo.addActionListener(new ServerTypeActionListener());
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.anchor = GridBagConstraints.WEST;
//        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
//        add(serverTypeCombo, gridBagConstraints);
        
        // add local instances field label
        localInstanceLabel.setLabelFor(localInstancesCombo);
        org.openide.awt.Mnemonics.setLocalizedText(localInstanceLabel, NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_LOCAL_INSTANCE")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(localInstanceLabel, gridBagConstraints);
        
        // add local instances combobox
        localInstancesCombo.addActionListener(new InstanceSelectionListener());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(localInstancesCombo, gridBagConstraints);
        localInstancesCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ServerPropertiesPanel.class, "ACSD_ServerPropertiesPanel_localInstancesCombo")); // NOI18N
        
        // add domain path field label
        domainPathLabel.setLabelFor(domainPathField);
        org.openide.awt.Mnemonics.setLocalizedText(domainPathLabel, NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_DOMAIN_LOCATION")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(domainPathLabel, gridBagConstraints);
        
        // add domain path field
        domainPathField.setColumns(20);
        domainPathField.setText(""); // NOI18N
        domainPathField.setEditable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(domainPathField, gridBagConstraints);
        domainPathField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ServerPropertiesPanel.class, "ACSD_ServerPropertiesPanel_domainPathField")); // NOI18N
        
        // add host field label
        hostLabel.setLabelFor(hostField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_HOST")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(hostLabel, gridBagConstraints);
        
        DocumentListener updateListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                fireChangeEvent();
            }

            public void removeUpdate(DocumentEvent e) {
                fireChangeEvent();
            }

            public void insertUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
        };
        
        // add host field
        hostField.setColumns(20);
        hostField.setText(""); // NOI18N
        hostField.getDocument().addDocumentListener(updateListener);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(hostField, gridBagConstraints);
        hostField.setEditable(false);
        hostField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ServerPropertiesPanel.class, "ACSD_ServerPropertiesPanel_hostField")); // NOI18N
        
        // add port field label
        portLabel.setLabelFor(portField);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_PORT")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(portLabel, gridBagConstraints);
        
        // add port field
        portField.setColumns(20);
        portField.setText(""); // NOI18N
        portField.getDocument().addDocumentListener(updateListener);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(portField, gridBagConstraints);
        portField.setEditable(false);
        portField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ServerPropertiesPanel.class, "ACSD_ServerPropertiesPanel_portField")); // NOI18N
        
        // add username field label
        userNameLabel.setLabelFor(usernameField);
//        userNameLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_USERNAME")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(userNameLabel, NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_USERNAME"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(userNameLabel, gridBagConstraints);
        
        // add username field
        usernameField.setColumns(20);
        usernameField.setText(""); // NOI18N
        usernameField.getDocument().addDocumentListener(updateListener);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(usernameField, gridBagConstraints);
        usernameField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ServerPropertiesPanel.class, "ACSD_ServerPropertiesPanel_usernameField")); // NOI18N
        
        // add password field label
        passwordLabel.setLabelFor(passwordField);
//        passwordLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_PASSWORD")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_PASSWORD"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(passwordLabel, gridBagConstraints);
        
        // add password field
        passwordField.setColumns(20);
        passwordField.getDocument().addDocumentListener(updateListener);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(passwordField, gridBagConstraints);
        passwordField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ServerPropertiesPanel.class, "ACSD_ServerPropertiesPanel_passwordField")); // NOI18N
        
        // add the empty panel, that will take up all the remaining space
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        add(formattingPanel, gridBagConstraints);
    }
    
    /**
     * Gets the list of registered domains according to the given server 
     * installation root
     * 
     * @param serverRoot the server's installation location
     * 
     * @return an array if strings with the domains' paths
     */
    private String[] getRegisteredDomains(String serverRoot){
        // init the resulting vector
        Vector result = new Vector();
        
        // is the server root was not defined, return an empty array of domains
        if (serverRoot == null) {
            return new String[0];
        }
        
        // the relative path to the domains list file
        String domainListFile = "/common/nodemanager/nodemanager.domains";  // NOI18N
        
        // init the input stream for the file and the w3c document object
        File file = new File(serverRoot + domainListFile);
        LineNumberReader lnr = null;
        
        // read the list file line by line fetching out the domain paths
        try {
            // create a new reader for the FileInputStream
            lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
            
            // read the lines
            String line;
            while ((line = lnr.readLine()) != null){
                // skip the comments
                if (line.startsWith("#")) {
                    continue;
                }
                
                // fetch the domain path
                String path = line.split("=")[1].replaceAll("\\\\\\\\", "/").replaceAll("\\\\:", ":"); // NOI18N
                
                // add the path to the resulting set
                result.add(path);
            }
        } catch (FileNotFoundException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } catch (IOException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        } finally {
            try {
                // close the stream
                if (lnr != null) {
                    lnr.close();
                }
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
        }
        
        // convert the vector to an array and return
        return (String[]) result.toArray(new String[result.size()]);
    }
    
    /**
     * Gets the list of local server instances.
     * 
     * @return a vector with the local instances
     */
    private Vector getServerInstances(){
        // initialize the resulting vector
        Vector result = new Vector();
        
        // get the list of registered profiles
        String[] domains = getRegisteredDomains(instantiatingIterator.getServerRoot());
        
        // for each domain get the list of instances
        for (int i = 0; i < domains.length; i++) {
            // get the instances configuration file's path
            String configPath = domains[i] + "/config/config.xml"; // NOI18N
            
            // init the input stream for the file and the w3c document object
            InputStream inputStream = null;
            Document document = null;

            try {
                // open the stream from the instances config file
                inputStream = new FileInputStream(new File(configPath));
                
                // parse the document
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
                
                // get the root element
                Element root = document.getDocumentElement();
                
                // get the child nodes
                NodeList children = root.getChildNodes();

                // for each child
                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);
                    // if the child's name equals 'server' get its children
                    // and iterate over them
                    if (child.getNodeName().matches("(?:[a-z]+\\:)?server")) {  // NOI18N
                        NodeList nl = child.getChildNodes();
                        
                        // desclare the server's name/host/port
                        String name = "";
                        String port = "";
                        String host = "";

                        // iterate over the children
                        for (int k = 0; k < nl.getLength(); k++) {
                            Node ch = nl.item(k);

                            // if the child's name equals 'name' fetch the
                            // instance's name
                            if (ch.getNodeName().matches("(?:[a-z]+\\:)?name")) {  // NOI18N
                                name = ch.getFirstChild().getNodeValue();
                            }

                            // if the child's name equals 'listen-port' fetch the
                            // instance's port
                            if (ch.getNodeName().matches("(?:[a-z]+\\:)?listen-port")) {  // NOI18N
                                port = ch.getFirstChild().getNodeValue();
                            }

                            // if the child's name equals 'listen-address' fetch the
                            // instance's host
                            if (ch.getNodeName().matches("(?:[a-z]+\\:)?listen-address")) {  // NOI18N
                                if (ch.hasChildNodes()){
                                    host = ch.getFirstChild().getNodeValue();
                                }
                            }
                        }
                        
                        // if all the parameters were fetched successfully add
                        // them to the result
                        if ((name != null) && (!name.equals(""))) { // NOI18N
                            //address and port have minOccurs=0 and are missing in 90 examples server
                            port = port == null || port.equals("") ? "7001" : port; //NOI18N
                            host = host == null || host.equals("") ? "localhost" : host; // NOI18N
                            result.add(new Instance(name, host, port, domains[i]));
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } catch (ParserConfigurationException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } catch (SAXException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
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
    public void storeSettings(Object object) {}
    
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
            ChangeListener listener = (ChangeListener) targetListeners.elementAt(i);
            listener.stateChanged(event);
        }
    }
    
    /**
     * A listener that reacts to the change of the server type combobox,
     * is the local server type is selected we should disable several fields
     * and enable some others instead.
     * 
     * @author Kirill Sorokin
     */
    private class ServerTypeActionListener implements ActionListener {
        /**
         * The main action handler. This method is called when the combobox
         * value changes
         */
        public void actionPerformed(ActionEvent e) {
            // if the selected type is local
            if (serverTypeCombo.getSelectedItem().equals(NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_TYPE_LOCAL"))) { // NOI18N
                Instance instance = (Instance) localInstancesCombo.getSelectedItem();
                
                // enable the local instances combo
                localInstancesCombo.setEnabled(true);
                
                // enable and set as read-only the domain path field
                domainPathField.setEnabled(true);
                domainPathField.setEditable(false);
                
                // enable and set as read-only the host field
                hostField.setEnabled(true);
                hostField.setEditable(false);
                hostField.setText(instance.getHost());
                
                // enable and set as read-only the port field
                portField.setEnabled(true);
                portField.setEditable(false);
                portField.setText(instance.getPort());
            } else {
                // disable the local instances combo
                localInstancesCombo.setEnabled(false);
                
                // disable the domain path field
                domainPathField.setEnabled(false);
                domainPathField.setEditable(false);
                
                // enable and set as read-write the host field
                hostField.setEnabled(true);
                hostField.setEditable(true);
                
                // enable and set as read-write the port field
                portField.setEnabled(true);
                portField.setEditable(true);
            }
            
            isValid();
        }
    }
    
    /**
     * A simple listeners that reacts to user's selectin a local instance. It
     * updates the selected instance info.
     * 
     * @author Kirill Sorokin
     */
    private class InstanceSelectionListener implements ActionListener {
        /**
         * The main action handler. This method is called when a new local
         * instance is selected
         */
        public void actionPerformed(ActionEvent e) {
            updateInstanceInfo();
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
         * The index of the selected instance
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
            
            // return the element at the index
            return instances.elementAt(selectedIndex);
        }
        
    }
    
    /**
     * A model for the server instance. It contains all the critical properties
     * for the plugin: name, host, port, profile path.
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
         * Creates a new instance of Instance
         * 
         * @param name the instance's name
         * @param host the instance's host
         * @param port the instance's port
         * @param domainPath the instance's profile path
         */
        public Instance(String name, String host, String port, String domainPath) {
            // save the properties
            this.name = name;
            this.host = host;
            this.port = port;
            this.domainPath = domainPath;
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
        public String toString() {
            return name + " [" + host + ":" + port + "]"; // NOI18N
        }
    }
}
