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
package org.netbeans.modules.j2ee.weblogic9.ui.wizard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 *
 * @author Kirill Sorokin
 */
public class ServerPropertiesPanel extends JPanel implements WizardDescriptor.Panel {
    private final static String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N
    
    private transient WizardDescriptor wizardDescriptor;
    private transient WLInstantiatingIterator instantiatingIterator;
    
    
    public ServerPropertiesPanel(String[] steps, int index, ChangeListener listener, WLInstantiatingIterator instantiatingIterator) {
        this.instantiatingIterator = instantiatingIterator;
        
        putClientProperty("WizardPanel_contentData", steps); // NOI18N
        putClientProperty("WizardPanel_contentSelectedIndex", new Integer(index)); // NOI18N
        addChangeListener(listener);
        
        setName(steps[index]);
        
        init();
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_server_weblogic"); //NOI18N
    }
    
    public Component getComponent() {
        return this;
    }
    
    public boolean isValid() {
        wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, ""); // NOI18N
        
        if (serverTypeCombo.getSelectedItem().equals(NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_TYPE_LOCAL"))) { // NOI18N
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
        instantiatingIterator.setIsLocal(serverTypeCombo.getSelectedItem().equals(NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_TYPE_LOCAL")) ? "true" : "false"); // NOI18N
        
        return true;
    }
    
    private boolean isValidDomainRoot(String path) {
        String[] children = {
                    "servers", // NOI18N
                    "config", // NOI18N
                    "config/config.xml", // NOI18N
                    "domain-info.xml", // NOI18N
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
        serverTypeCombo = new JComboBox(new Object[] {NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_TYPE_LOCAL"), NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_TYPE_REMOTE")}); // NOI18N
        localInstanceLabel = new JLabel();
        localInstancesCombo = new JComboBox(new InstancesModel(getServerInstances()));
        serverTypeLabel = new JLabel();
        
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
        
        localInstanceLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_LOCAL_INSTANCE")); // NOI18N
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
        
        domainPathLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_DOMAIN_LOCATION")); // NOI18N
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
        
        hostLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_HOST")); // NOI18N
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
        
        portLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_PORT")); // NOI18N
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
        
        userNameLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_USERNAME")); // NOI18N
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
        
        passwordLabel.setText(NbBundle.getMessage(ServerPropertiesPanel.class, "LBL_PASSWORD")); // NOI18N
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
        
        String domainListFile = "/common/nodemanager/nodemanager.domains";  // NOI18N
        
        File file = new File(serverRoot + domainListFile);
        LineNumberReader lnr = null;
        
        try {
            lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
            
            String line;
            while ((line = lnr.readLine()) != null){
                if (line.startsWith("#")) {
                    continue;
                }
                
                String path = line.split("=")[1].replaceAll("\\\\\\\\", "/").replaceAll("\\\\:", ":"); // NOI18N
                
                result.add(path);
            }
        } catch (FileNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            try {
                if (lnr != null) {
                    lnr.close();
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        return (String[]) result.toArray(new String[result.size()]);
    }
    
    private Vector getServerInstances(){
        Vector result = new Vector();
        
        String[] domains = getRegisteredDomains(instantiatingIterator.getServerRoot());
        for (int i = 0; i < domains.length; i++) {
            String configPath = domains[i] + "/config/config.xml"; // NOI18N

            InputStream inputStream = null;
            Document document = null;

            try {
                inputStream = new FileInputStream(new File(configPath));

                document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);

                Element root = document.getDocumentElement();

                NodeList children = root.getChildNodes();

                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);
                    if (child.getNodeName().matches("(?:[a-z]+\\:)?server")) {  // NOI18N
                        NodeList nl = child.getChildNodes();

                        String name = "";
                        String port = "";
                        String host = "";
                        for (int k = 0; k < nl.getLength(); k++){
                            Node ch = nl.item(k);

                            if (ch.getNodeName().matches("(?:[a-z]+\\:)?name")) {  // NOI18N
                                name = ch.getFirstChild().getNodeValue();
                            }

                            if (ch.getNodeName().matches("(?:[a-z]+\\:)?listen-port")) {  // NOI18N
                                port = ch.getFirstChild().getNodeValue();
                            }

                            if (ch.getNodeName().matches("(?:[a-z]+\\:)?listen-address")) {  // NOI18N
                                if (ch.hasChildNodes()){
                                    host = ch.getFirstChild().getNodeValue();
                                }
                                host = host.equals("") ? "localhost" : host; // NOI18N
                            }
                        }
                        if ((name != null) && (port != null) && (!name.equals("")) && (!port.equals(""))) { // NOI18N
                            result.add(new Instance(name, host, port, domains[i]));
                        }
                    }
                }
            } catch(FileNotFoundException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (ParserConfigurationException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (SAXException e) {
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
            if (serverTypeCombo.getSelectedItem().equals(NbBundle.getMessage(ServerPropertiesPanel.class, "SERVER_TYPE_LOCAL"))) { // NOI18N
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
}
