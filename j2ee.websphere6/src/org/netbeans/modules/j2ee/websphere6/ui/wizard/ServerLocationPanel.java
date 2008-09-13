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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.j2ee.websphere6.WSVersion;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * The first panel of the custom wizard used to register new server instance.
 * User is required to enter the local server's installation directory at this
 * phase.
 *
 * @author Kirill Sorokin
 * @author Arathi
 */
public class ServerLocationPanel extends JPanel implements WizardDescriptor.Panel {
    
    private static final Logger LOGGER = Logger.getLogger(ServerLocationPanel.class.getName());
    
    /**
     * The parent wizard descriptor handle
     */
    private transient WizardDescriptor wizardDescriptor;
    
    /**
     * The parent instantiaing iterator handle
     */
    private transient WSInstantiatingIterator instantiatingIterator;
    
    /**
     * Creates a new instance of the ServerLocationPanel. It initializes all the
     * GUI components that appear on the panel.
     *
     * @param steps the names of the steps in the wizard
     * @param index index of this panel in the wizard
     * @param listener a listener that will propagate the chage event higher in
     *      the hierarchy
     * @param instantiatingIterator the parent instantiating iterator
     */
    public ServerLocationPanel(String[] steps, int index,
            ChangeListener listener,
            WSInstantiatingIterator instantiatingIterator) {
        // save the instantiating iterator
        this.instantiatingIterator = instantiatingIterator;
        
        // set the required properties, so that the panel appear correct in
        // the steps
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);           // NOI18N
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,          // NOI18N
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
        
        // check for the validity of the entered installation directory
        // if it's invalid, return false
        String location = locationField.getText();
        if (location.trim().length() < 1) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                    NbBundle.getMessage(ServerLocationPanel.class,
                    "ERR_SPECIFY_SERVER_ROOT"));    // NOI18N
            return false;
        }
        
        if (!isValidServerRoot(locationField.getText())) {
            WSVersion version = instantiatingIterator.getVersion();
            String strVersion = ""; // NOI18N
            switch (version) {
                case VERSION_60:
                    strVersion = "V6.0"; // NOI18N
                    break;
                case VERSION_61:
                    strVersion = "V6.1"; // NOI18N
                    break;
            }
            
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ServerLocationPanel.class,
                    "ERR_INVALID_SERVER_ROOT", strVersion));    // NOI18N
            return false;
        }
        
        // set the server root in the parent instantiating iterator
        instantiatingIterator.setServerRoot(locationField.getText());
        
        // everything seems ok
        return true;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // JPanel section
    ////////////////////////////////////////////////////////////////////////////
    private JButton locationBrowseButton;
    private JLabel locationLabel;
    private JTextField locationField;
    private JPanel formattingPanel;
    
    /**
     * Inits the GUI components
     */
    private void init() {
        // we use the GridBagLayout so we need the GridBagConstraints to
        // properly place the components
        GridBagConstraints gridBagConstraints;
        
        // initialize the components
        locationLabel = new JLabel();
        locationField = new JTextField();
        locationBrowseButton = new JButton();
        formattingPanel = new JPanel();
        
        // set the desired layout
        setLayout(new GridBagLayout());
        
        // add server installation directory field label
        locationLabel.setText(NbBundle.getMessage(ServerLocationPanel.class,
                "LBL_SERVER_LOCATION"));                               // NOI18N
        locationLabel.setDisplayedMnemonic(NbBundle.getMessage(ServerLocationPanel.class,
                "MNE_SERVER_LOCATION").charAt(0));    // NOI18N
        locationLabel.setLabelFor(locationField);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        add(locationLabel, gridBagConstraints);
        
        // add server installation directory field
        locationField.addKeyListener(new LocationKeyListener());
        if(System.getProperty("websphere.home") == null ||      // NOI18N
                System.getProperty("websphere.home").equals("")) {      // NOI18N
            String home = System.getProperty("user.home");      // NOI18N
            if (home != null) {
                try{
                    File f = new File(home + File.separator + ".WASRegistry");    // NOI18N
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            new FileInputStream(f)));
                    try {
                        String string;
                        while ((string = reader.readLine()) != null) {
                            if (string.length() > 1 && new File(string).exists()) {
                                System.setProperty("websphere.home", string);    // NOI18N
                            }
                        }
                    } finally {
                        reader.close();
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, null, e);
                }
            }
        }
        
        if (System.getProperty("websphere.home") != null) {            // NOI18N
            locationField.setText(System.getProperty(
                    "websphere.home"));                                // NOI18N
        }
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 10, 0, 10);
        locationField.getAccessibleContext().setAccessibleName(
                java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/wizard/Bundle").getString("TTL_Location"));
        locationField.getAccessibleContext().setAccessibleDescription(
                java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/wizard/Bundle").getString("MSG_Location"));
        
        add(locationField, gridBagConstraints);
        
        // add server installation directory field browse button
        locationBrowseButton.setText(NbBundle.getMessage(
                ServerLocationPanel.class, "LBL_BROWSE_BUTTON"));      // NOI18N
        locationBrowseButton.setMnemonic(KeyEvent.VK_O);
        locationBrowseButton.setDisplayedMnemonicIndex(2);
        locationBrowseButton.addActionListener(new BrowseActionListener());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        locationBrowseButton.getAccessibleContext().setAccessibleName(
                java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/wizard/Bundle").getString("TTL_Browse"));
        locationBrowseButton.getAccessibleContext().setAccessibleDescription(
                java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/wizard/Bundle").getString("MSG_Browse"));
        
        add(locationBrowseButton, gridBagConstraints);
        
        // add the empty panel, that will take up all the remaining space
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.weighty = 1.0;
        add(formattingPanel, gridBagConstraints);
        getAccessibleContext().setAccessibleDescription(
                java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/wizard/Bundle").
                getString("MSG_ServerLocationPanelDescription"));
    }
    
    /**
     * An instance of the fileschooser that is used for locating the server
     * installation directory
     */
    private JFileChooser fileChooser = new JFileChooser();
    
    /**
     * Shows the filechooser set to currently selected directory or to the
     * default system root if the directory is invalid
     */
    private void showFileChooser() {
        // set the chooser's properties
        fileChooser.setFileFilter(new DirectoryFileFilter());
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        // set the current directory
        File currentLocation = new File(locationField.getText());
        if (currentLocation.exists() && currentLocation.isDirectory()) {
            fileChooser.setCurrentDirectory(currentLocation.getParentFile());
            fileChooser.setSelectedFile(currentLocation);
        }
        
        // wait for the user to choose the directory and if he clicked the OK
        // button store the selected directory in the server location field
        if (fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(this))
                == JFileChooser.APPROVE_OPTION) {
            locationField.setText(fileChooser.getSelectedFile().getPath());
            fireChangeEvent();
        }
    }
    
    /**
     * Checks whether the supplied directory is the valid server installation
     * directory.
     *
     * @return true if the supplied directory is valid, false otherwise
     */
    private static boolean isValidServerRoot(String path) {
        // set the child directories/files that should be present and validate
        // the directory as the server's installation one
        
	// This is for WAS 6.1
        String dbDir = "derby";    // NOI18N
        String jsr88Jar = "plugins/com.ibm.ws.runtime_6.1.0.jar";    // NOI18N
        
	// This is for WAS 6.0
        if ((new File(path + File.separator + "cloudscape").exists())) {   // NOI18N
            dbDir = "cloudscape";    // NOI18N
	    jsr88Jar = "lib/wjmxapp.jar";    // NOI18N
	}
            
        String[] children = {
            "bin",                                             // NOI18N
            dbDir,                                             // NOI18N
            "profiles",                                        // NOI18N
            "properties/wsadmin.properties",                   // NOI18N
            "lib/j2ee.jar",                                    // NOI18N
            jsr88Jar                                  // NOI18N
        };
        return hasChildren(path, children);
    }
    
    /**
     * Checks whether the supplied directory has the required children
     *
     * @return true if the directory contains all the children, false otherwise
     */
    private static boolean hasChildren(String parent, String[] children) {
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
            ChangeListener listener = (ChangeListener) targetListeners.
                    elementAt(i);
            listener.stateChanged(event);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Simple key listener that delegates the event to its parent's listeners
     *
     * @author Kirill Sorokin
     */
    private class LocationKeyListener extends KeyAdapter {
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
    
    /**
     * Simple listener that reacts on the user's clicking the Browse button
     *
     * @author Kirill Sorokin
     */
    private class BrowseActionListener implements ActionListener {
        /**
         * this methos is called when a user clicks Browse and show the file
         * chooser dialog in response
         */
        public void actionPerformed(ActionEvent event) {
            showFileChooser();
        }
    }
    
    /**
     * An extension of the FileFilter class that is setup to accept only
     * directories.
     *
     * @author Kirill Sorokin
     */
    private static class DirectoryFileFilter extends FileFilter {
        /**
         * This method is called when it is needed to decide whether a chosen
         * file meets the filter's requirements
         *
         * @return true if the file meets the requirements, false otherwise
         */
        public boolean accept(File file) {
            // if the file exists and it's a directory - accept it
            if (file.exists() && file.isDirectory()) {
                return true;
            }
            
            // in all other cases - refuse
            return false;
        }
        
        /**
         * Returns the description of file group described by this filter
         *
         * @return group name
         */
        public String getDescription() {
            return NbBundle.getMessage(ServerLocationPanel.class,
                    "DIRECTORIES_FILTER_NAME");                        // NOI18N
        }
    }
}
