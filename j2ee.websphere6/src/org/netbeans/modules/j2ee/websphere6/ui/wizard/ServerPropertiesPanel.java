/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.ui.wizard;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.*;
import org.openide.*;
import org.openide.util.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;


/**
 *
 * @author Kirill Sorokin
 */
public class ServerPropertiesPanel extends JPanel implements WizardDescriptor.Panel {
    private final static String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N
    
    private transient WizardDescriptor wizardDescriptor;
    private transient WSInstantiatingIterator instantiatingIterator;
    
    
    public ServerPropertiesPanel(String[] steps, int index, ChangeListener listener, WSInstantiatingIterator instantiatingIterator) {
        this.instantiatingIterator = instantiatingIterator;
        
        putClientProperty("WizardPanel_contentData", steps); // NOI18N
        putClientProperty("WizardPanel_contentSelectedIndex", new Integer(index)); // NOI18N
        addChangeListener(listener);
        
        setName(steps[index]);
        
        init();
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_server_websphere"); //NOI18N
    }
    
    public Component getComponent() {
        return this;
    }
    
    public boolean isValid() {
        wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, ""); // NOI18N
        
        if (serverTypeCombo.getSelectedItem().equals(NbBundle.getMessage(ServerPropertiesPanel.class, "TXT_serverTypeLocal"))) { // NOI18N
            if (!isValidDomainRoot(domainPathField.getText())) {
                wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ServerPropertiesPanel.class, "ERR_INVALID_DOMAIN_ROOT")); // NOI18N
                return false;
            }
        }
        
        if (hostField.getText().trim().equals("")) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ServerPropertiesPanel.class, "ERR_INVALID_HOST")); // NOI18N
        }
        
        if (!portField.getText().trim().matches("[0-9]+")) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ServerPropertiesPanel.class, "ERR_INVALID_PORT")); // NOI18N
        }
        
        // no checks for username & password as they may be intentionally blank
        
        // save the data
        instantiatingIterator.setDomainRoot(domainPathField.getText());
        instantiatingIterator.setHost(hostField.getText());
        instantiatingIterator.setPort(portField.getText());
        instantiatingIterator.setUsername(usernameField.getText());
        instantiatingIterator.setPassword(new String(passwordField.getPassword()));
        instantiatingIterator.setIsLocal(serverTypeCombo.getSelectedItem().equals(NbBundle.getMessage(ServerPropertiesPanel.class, "TXT_serverTypeLocal")) ? "true" : "false"); // NOI18N
        instantiatingIterator.setServerName(((Instance) localInstancesCombo.getSelectedItem()).getName());
        
        return true;
    }
    
    private boolean isValidDomainRoot(String path) {
        String[] children = {
                    "config/cells", // NOI18N
                    "etc/ws-security", // NOI18N
                    "properties/soap.client.props", // NOI18N
                    "properties/wsadmin.properties", // NOI18N
        };
        return hasChildren(path, children);
    }
    
    private boolean hasChildren(String parent, String[] children) {
        if (parent == null) {
            return false;
        }
        if (children == null) {
            return true;
        }
        
        for (int i = 0; i < children.length; i++) {
            if (!(new File(parent + File.separator + children[i]).exists())) {
                return false;
            }
        }
        
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
    
    private void init() {
        GridBagConstraints gridBagConstraints;
        
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
        serverTypeCombo = new JComboBox(new Object[] {NbBundle.getMessage(ServerPropertiesPanel.class, "TXT_serverTypeLocal"), NbBundle.getMessage(ServerPropertiesPanel.class, "TXT_serverTypeRemote")}); // NOI18N
        localInstanceLabel = new JLabel();
        localInstancesCombo = new JComboBox(new InstancesModel(getServerInstances()));
        
        setLayout(new GridBagLayout());
        
        serverTypeLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_serverType")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(serverTypeLabel, gridBagConstraints);
        
        serverTypeCombo.addActionListener(new ServerTypeActionListener());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(serverTypeCombo, gridBagConstraints);
        
        localInstanceLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_localInstances")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(localInstanceLabel, gridBagConstraints);
        
        localInstancesCombo.addActionListener(new InstanceSelectionListener());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(localInstancesCombo, gridBagConstraints);
        
        domainPathLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_domainPath")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(domainPathLabel, gridBagConstraints);
        
        domainPathField.setText(""); // NOI18N
        domainPathField.setEditable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(domainPathField, gridBagConstraints);
        
        hostLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_host")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(hostLabel, gridBagConstraints);
        
        hostField.setText(""); // NOI18N
        hostField.addKeyListener(new KeyListener());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(hostField, gridBagConstraints);
        hostField.setEditable(false);
        
        portLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_port")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(portLabel, gridBagConstraints);
        
        portField.setText(""); // NOI18N
        portField.addKeyListener(new KeyListener());
        portField.setPreferredSize(new Dimension(50, 20));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(portField, gridBagConstraints);
        portField.setEditable(false);
        
        userNameLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_username")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(userNameLabel, gridBagConstraints);
        
        usernameField.setText(""); // NOI18N
        usernameField.addKeyListener(new KeyListener());
        usernameField.setPreferredSize(new Dimension(100, 20));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(usernameField, gridBagConstraints);
        
        passwordLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_password")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(passwordLabel, gridBagConstraints);
        
        passwordField.setPreferredSize(new Dimension(100, 20));
        passwordField.addKeyListener(new KeyListener());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 10, 5, 0);
        add(passwordField, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        add(formattingPanel, gridBagConstraints);
    }
    
    private String[] getRegisteredDomains(String serverRoot){
        Vector result = new Vector();
        
        if (serverRoot == null) {
            return new String[0];
        }
        
        String domainListFile = "/properties/profileRegistry.xml";  // NOI18N
        
        InputStream inputStream = null;
        Document document = null;

        try {
            inputStream = new FileInputStream(new File(serverRoot + domainListFile));

            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);

            Element root = document.getDocumentElement();
            
            NodeList children = root.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals("profile")) {  // NOI18N
                    String path = child.getAttributes().getNamedItem("path").getNodeValue(); // NOI18N
                    result.add(path);
                }
            }
        } catch (FileNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } catch (ParserConfigurationException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } catch (SAXException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
            
        return (String[]) result.toArray(new String[result.size()]);
    }

    private String getCellPort(String cellPath) {
        String[] files = new File(cellPath + "/nodes").list();
        
        for (int i = 0; i < files.length; i++) {
            String path = cellPath + "/nodes/" + files[i] + "/serverindex.xml";
            if (new File(path).exists()) { // NOI18N
                cellPath = path; // NOI18N
                break;
            }
        }
        
        InputStream inputStream = null;
        Document document = null;
        
        try {
            inputStream = new FileInputStream(new File(cellPath));
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            
            // get the root element
            Element root = document.getDocumentElement();
            
            // get the child nodes
            NodeList children = root.getChildNodes();
            
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals("serverEntries")) {  // NOI18N
                    NodeList nl = child.getChildNodes();
                    for (int j = 0; j < nl.getLength(); j++){
                        Node ch = nl.item(j);
                        if (ch.getNodeName().equals("specialEndpoints") && ch.getAttributes().getNamedItem("endPointName").getNodeValue().equals("SOAP_CONNECTOR_ADDRESS")) {  // NOI18N
                            NodeList nl2 = ch.getChildNodes();
                            for (int k = 0; k < nl2.getLength(); k++) {
                                Node ch2 = nl2.item(k);
                                if (ch2.getNodeName().equals("endPoint")) {
                                    String port = ch2.getAttributes().getNamedItem("port").getNodeValue(); // NOI18N
                                    return port;
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } catch (ParserConfigurationException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } catch (SAXException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        return ""; // NOI18N
        
    }
    
    private String getServerName(String cellPath) {
        String[] files = new File(cellPath + "/nodes").list();
        
        for (int i = 0; i < files.length; i++) {
            String path = cellPath + "/nodes/" + files[i] + "/serverindex.xml";
            if (new File(path).exists()) { // NOI18N
                cellPath = path; // NOI18N
                break;
            }
        }
        
        InputStream inputStream = null;
        Document document = null;
        
        try {
            inputStream = new FileInputStream(new File(cellPath));
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            
            // get the root element
            Element root = document.getDocumentElement();
            
            // get the child nodes
            NodeList children = root.getChildNodes();
            
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals("serverEntries")) {  // NOI18N
                    return  child.getAttributes().getNamedItem("serverName").getNodeValue(); // NOI18N
                }
            }
        } catch (FileNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } catch (ParserConfigurationException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } catch (SAXException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        return ""; // NOI18N
        
    }
    
    private Vector getServerInstances(){
        Vector result = new Vector();
        
        String[] domains = getRegisteredDomains(instantiatingIterator.getServerRoot());
        for (int i = 0; i < domains.length; i++) {
            File file = new File(domains[i] + "/config/cells"); // NOI18N

            String[] files = file.list(new DirectoryFilter());
            
            for (int j = 0; j < files.length; j++){
                String nextCellPath = file.getAbsolutePath() + File.separator + files[j];
                String address = "localhost"; // NOI18N
                String port = getCellPort(nextCellPath);
                String serverName = getServerName(nextCellPath);
                result.add(new Instance(serverName, address, port, domains[i]));
            }
        }
        
        return result;
    }
    
    public void updateInstancesList() {
        localInstancesCombo.setModel(new InstancesModel(getServerInstances()));
        updateInstanceInfo();
    }
    
    private void updateInstanceInfo() {
        Instance instance = (Instance) localInstancesCombo.getSelectedItem();
        
        domainPathField.setText(instance.getDomainPath());
        hostField.setText(instance.getHost());
        portField.setText(instance.getPort());
        
    }
    ////////////////////////////////////////////////////////////////////////////
    // Settings section
    ////////////////////////////////////////////////////////////////////////////
    public void readSettings(Object object) {
        this.wizardDescriptor = (WizardDescriptor) object;
    }
    
    public void storeSettings(Object object) {}
    
    ////////////////////////////////////////////////////////////////////////////
    // Listeners section
    ////////////////////////////////////////////////////////////////////////////
    private Vector listeners = new Vector();
    
    public void removeChangeListener(ChangeListener listener) {
        if (listeners != null) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
    }
    
    public void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        fireChangeEvent(event);
    }
    
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
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////////////////
    private class KeyListener extends KeyAdapter {
        public void keyTyped(KeyEvent event) {
            fireChangeEvent();
        }
        
        public void keyReleased(KeyEvent event) {
            fireChangeEvent();
        }
    }
    
    private class ServerTypeActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (serverTypeCombo.getSelectedItem().equals(NbBundle.getMessage(ServerPropertiesPanel.class, "TXT_serverTypeLocal"))) { // NOI18N
                Instance instance = (Instance) localInstancesCombo.getSelectedItem();
                
                localInstancesCombo.setEnabled(true);
                
                domainPathField.setEnabled(true);
                domainPathField.setEditable(false);
                
                hostField.setEnabled(true);
                hostField.setEditable(false);
                hostField.setText(instance.getHost());
                
                portField.setEnabled(true);
                portField.setEditable(false);
                portField.setText(instance.getPort());
            } else {
                localInstancesCombo.setEnabled(false);
                
                domainPathField.setEnabled(false);
                domainPathField.setEditable(false);
                
                hostField.setEnabled(true);
                hostField.setEditable(true);
                
                portField.setEnabled(true);
                portField.setEditable(true);
            }
            
            isValid();
        }
    }
    
    private class InstanceSelectionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            updateInstanceInfo();
        }
    }
    
    private static class InstancesModel extends AbstractListModel implements ComboBoxModel {
        private Vector instances;
        private int selectedIndex = 0;
        
        public InstancesModel(Vector instances) {
            this.instances = instances;
            
            this.selectedIndex = 0;
        }
        
        public void setSelectedItem(Object item) {
            selectedIndex = instances.indexOf(item);
        }

        public Object getElementAt(int index) {
            return instances.elementAt(index);
        }

        public int getSize() {
            return instances.size();
        }

        public Object getSelectedItem() {
            if (instances.size() == 0) {
                return null;
            }
            
            return instances.elementAt(selectedIndex);
        }
        
    }
    
    private static class Instance {
        private String name;
        private String host;
        private String port;
        private String domainPath;
        
        public Instance(String name, String host, String port, String domainPath) {
            this.name = name;
            this.host = host;
            this.port = port;
            this.domainPath = domainPath;
        }
        
        public String getName() {
            return this.name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getHost() {
            return this.host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public String getPort() {
            return this.port;
        }
        
        public void setPort(String port) {
            this.port = port;
        }
        
        public String getDomainPath() {
            return this.domainPath;
        }
        
        public void setDomainPath(String domainPath) {
            this.domainPath = domainPath;
        }
        
        public String toString() {
            return name + " [" + host + ":" + port + "]"; // NOI18N
        }
    }
    
    private static class DirectoryFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            if ((new File(dir.getAbsolutePath()+File.separator+name)).isDirectory()) {
                return true;
            }
            return false;
        }
    }
}
