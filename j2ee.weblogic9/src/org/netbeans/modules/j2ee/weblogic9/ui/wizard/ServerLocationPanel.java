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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 *
 * @author Kirill Sorokin
 */
public class ServerLocationPanel extends JPanel implements WizardDescriptor.Panel {
    private final static String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N
    
    private transient WizardDescriptor wizardDescriptor;
    private transient WLInstantiatingIterator instantiatingIterator;
    
    public ServerLocationPanel(String[] steps, int index, ChangeListener listener, WLInstantiatingIterator instantiatingIterator) {
        this.instantiatingIterator = instantiatingIterator;
        
        putClientProperty("WizardPanel_contentData", steps); // NOI18N
        putClientProperty("WizardPanel_contentSelectedIndex", new Integer(index)); // NOI18N
        addChangeListener(listener);
        
        setName(steps[index]);
        
        init();
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_server_weblogic"); // NOI18N
    }
    
    public Component getComponent() {
        return this;
    }
    
    public boolean isValid() {
        wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, "");
        
        if (!isValidServerRoot(locationField.getText())) {
            wizardDescriptor.putProperty(PROP_ERROR_MESSAGE, NbBundle.getMessage(ServerLocationPanel.class, "ERR_INVALID_SERVER_ROOT")); // NOI18N
            return false;
        }
        
        instantiatingIterator.setServerRoot(locationField.getText());
        
        return true;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // JPanel section
    ////////////////////////////////////////////////////////////////////////////
    private JButton locationBrowseButton;
    private JLabel locationLabel;
    private JTextField locationField;
    private JPanel formattingPanel;
    
    private void init() {
        GridBagConstraints gridBagConstraints;
        
        locationLabel = new JLabel();
        locationField = new JTextField();
        locationBrowseButton = new JButton();
        formattingPanel = new JPanel();
        
        setLayout(new GridBagLayout());
        
        locationLabel.setText(NbBundle.getMessage(ServerLocationPanel.class, "LBL_SERVER_LOCATION")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        add(locationLabel, gridBagConstraints);
        
        locationField.addKeyListener(new LocationKeyListener());
        if (System.getProperty("weblogic.home") != null) { // NOI18N
            locationField.setText(System.getProperty("weblogic.home")); // NOI18N
        }
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 10, 0, 10);
        add(locationField, gridBagConstraints);
        
        locationBrowseButton.setText(NbBundle.getMessage(ServerLocationPanel.class, "LBL_BROWSE_BUTTON")); // NOI18N
        locationBrowseButton.setMnemonic(KeyEvent.VK_O);
        locationBrowseButton.setDisplayedMnemonicIndex(2);
        locationBrowseButton.addActionListener(new BrowseActionListener());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(locationBrowseButton, gridBagConstraints);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        add(formattingPanel, gridBagConstraints);
    }
    
    private JFileChooser fileChooser = new JFileChooser();
    
    private void showFileChooser() {
        fileChooser.setFileFilter(new DirectoryFileFilter());
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        File currentLocation = new File(locationField.getText());
        if (currentLocation.exists() && currentLocation.isDirectory()) {
            fileChooser.setCurrentDirectory(currentLocation.getParentFile());
            fileChooser.setSelectedFile(currentLocation);
        }
        
        if (fileChooser.showOpenDialog(this) == fileChooser.APPROVE_OPTION) {
            locationField.setText(fileChooser.getSelectedFile().getPath());
            fireChangeEvent();
        }
    }
    
    private boolean isValidServerRoot(String path) {
        String[] children = {
                    "common", // NOI18N
                    "javelin", // NOI18N
                    "uninstall", // NOI18N
                    "common/bin", // NOI18N
                    "server/lib/weblogic.jar" // NOI18N
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
    // Inner Classes
    ////////////////////////////////////////////////////////////////////////////
    private class LocationKeyListener extends KeyAdapter {
        public void keyTyped(KeyEvent event) {
            fireChangeEvent();
        }
        
        public void keyReleased(KeyEvent event) {
            fireChangeEvent();
        }
    }
    
    private class BrowseActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            showFileChooser();
        }
    }
    
    private static class DirectoryFileFilter extends FileFilter {
        public boolean accept(File file) {
            if (file.exists() && file.isDirectory()) {
                return true;
            }
            return false;
        }
        
        public String getDescription() {
            return NbBundle.getMessage(ServerLocationPanel.class, "DIRECTORIES_FILTER_NAME"); // NOI18N
        }
    }
}
