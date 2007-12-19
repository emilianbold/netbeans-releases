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

package org.netbeans.modules.php.rt.providers.impl.local.apache;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.rt.WebServerRegistry;
import org.netbeans.modules.php.rt.providers.impl.local.LocalHostImpl;
import org.netbeans.modules.php.rt.providers.impl.local.LocalServerProvider;
import org.netbeans.modules.php.rt.providers.impl.local.LocalUiConfigProvider;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.modules.php.rt.ui.AddHostWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author  ads
 */
public class ServerChooserVisual extends JPanel {

    private static final long serialVersionUID = -3170968073330222259L;
    private static final String SELECT_CONFIG_LOCATION = "LBL_Select_config_Location"; // NOI18N
    private static final String WIZARD_PANEL_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N
    private static final String BROWSE = "BROWSE"; // NOI18N
    private static final String MSG_EMPTY_CONFIG_FILE = "MSG_EmptyConfigFile"; // NOI18N
    // DOTO change to illegal path to config file
    private static final String MSG_ILLEGAL_CONFIG_FILE_LOCATION = "MSG_IllegalConfigFileLocation"; // NOI18N
    private static final String MSG_FAILED_AUTOCONFIG = "MSG_FailedAutoConfig"; // NOI18N
    private static final String MSG_AUTOCONF_NOT_PERFORMED = "MSG_AtoconfIsNotPerformed"; // NOI18N
    private static final String MSG_NO_PHP_MODULE = "MSG_NoPhpModuleFound"; // NOI18N
    private static final String MSG_TOO_MANY_PHP_MODULES = "MSG_TooManyPhpModulesFound"; // NOI18N
    private static final String MSG_NO_PHP_SO = "MSG_NoPhpSoLibrary"; // NOI18N
    private static final String MSG_EXTENSIONS = "MSG_Extensions"; // NOI18N
    private static final String MSG_NO_PHP_EXTENSIONS = "MSG_NoPhpExtensions"; // NOI18N
    private static final String MSG_HOST_ALREADY_EXISTS = "MSG_HostAlreadyConfigured"; // NOI18N
    /** Apache conf folder */
    private static final String CONF = "conf"; // NOI18N
    /** Apache httpd.conf */
    public static final String HTTPD_CONF = "httpd.conf"; // NOI18N
    public static final String PATH_TO_HTTPD_CONF = File.separator + CONF + File.separator + HTTPD_CONF;

    private static Logger LOGGER = Logger.getLogger(ServerChooserVisual.class.getName());
    
    public ServerChooserVisual(ServerChooserPanel panel) {
        initComponents();

        myPanel = panel;
        setOsDependency();
        myMap.put(myManualButton, new Component[]{myLabel1, myLocation, myBrowse});
        myMap.put(myAutoButton, new Component[]{myLabel2, myLocationAuto, myAuto});
        
        RadioListener listener = new RadioListener();
        myManualButton.addActionListener(listener);
        myAutoButton.addActionListener(listener);

        myLocationAuto.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setHosts();
                getPanel().stateChanged();
            }
        });
        myHosts.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                getPanel().stateChanged();
            }
        });
        myLocation.getDocument().addDocumentListener(new TextFieldListener());
    }

    private void setOsDependency(){
        if (getPanel().isSolaris()){
            myAutoButton.setSelected(true);
            myAutoButton.doClick();
        } else {
            myManualButton.setSelected(true);
            myManualButton.doClick();
            myAutoButton.setEnabled(false);
            myLabel2.setEnabled(false);
            myLocationAuto.setEnabled(false);
            myAuto.setEnabled(false);
        }
    }
    
    public void read(AddHostWizard wizard) {
        myWizard = wizard;

        HttpdHost host = (HttpdHost) myWizard.getProperty( ServerChooserPanel.HOST );
        if (host != null) {
            myHosts.getModel().setSelectedItem(host);
        }
        String path = (String) myWizard.getProperty( 
                ServerChooserPanel.CONFIG_LOCATION );

        JRadioButton configWay = (JRadioButton) myWizard.getProperty( 
                ServerChooserPanel.CONFIG_WAY );

        if (myAutoButton.equals(configWay)) {
            if (path != null) {
                myLocationAuto.setSelectedItem(path);
            }
            myAutoButton.doClick();
            // focus can be gained only when component becomes visible.
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    myAutoButton.requestFocusInWindow();
                }
            });
        } else if(myManualButton.equals(configWay)){
            myManualButton.doClick();
        }
        if (path != null) {
            myLocation.setText(path);
        }
    }

    public void store(AddHostWizard wizard) {
        if (myAutoButton.isSelected()) {
            myWizard.putProperty(ServerChooserPanel.CONFIG_WAY, myAutoButton);
            if (myLocationAuto.getSelectedItem() != null){
                myWizard.putProperty(ServerChooserPanel.CONFIG_LOCATION, 
                    myLocationAuto.getSelectedItem().toString());
            }
        } else if (myManualButton.isSelected()) {
            myWizard.putProperty(ServerChooserPanel.CONFIG_WAY, myManualButton);
            myWizard.putProperty(ServerChooserPanel.CONFIG_LOCATION, 
                    myLocation.getText());
        }

        Object object = myHosts.getModel().getSelectedItem();
        assert object instanceof HttpdHost || object == null;
        HttpdHost host = (HttpdHost) object;
        myWizard.putProperty(ServerChooserPanel.HOST, host);

        // set data to fill LocalWebServerPanelVisual form
        if (host != null) {
            LocalHostImpl configuredHost = (LocalHostImpl) wizard
                    .getProperty(LocalUiConfigProvider.HOST);
            if (configuredHost == null) {

                WebServerProvider provider = wizard.getCurrentProvider();
                assert provider instanceof LocalServerProvider;

                configuredHost = new LocalHostImpl(
                        host.toString(),
                        host.getName(), 
                        host.getPort(),   
                        "",
                        (LocalServerProvider) provider);
                configuredHost.setProperty(LocalHostImpl.DOCUMENT_PATH, host.getPath());

                wizard.putProperty(LocalUiConfigProvider.HOST, configuredHost);
            }
        }
        wizard.putProperty(LocalUiConfigProvider.WEB_CONFIGS_CACHE, myKnownConfigs);
    }

    public boolean isContentValid() {
        boolean isValid = validatePlatform() && validateConfiguredPhp(getHttpdConfig());

        //myHosts.setEnabled(isValid);

        if (isValid) {
            isValid = validateChosenHost();
        }
        // if manual configuration is selected,
        // allow next step even if this is not completed
        //if (myManualButton.isSelected()) {
        //    return true;
        //}
        // allow net step any way
        return true;
        //return isValid;
    }

    private boolean validateChosenHost() {
        //Do not prohibit records with the same http server
        /*
        HttpdHost host = (HttpdHost) myHosts.getSelectedItem();
        Collection<org.netbeans.modules.php.rt.spi.providers.Host> hosts 
                = WebServerRegistry.getInstance().getHosts();
        if (hosts.contains(host)) {
            setErrorMessage(MSG_HOST_ALREADY_EXISTS, host);
            return false;
        } else {
            myWizard.putProperty(WIZARD_PANEL_ERROR_MESSAGE, "");
        }
         */
        myWizard.putProperty(WIZARD_PANEL_ERROR_MESSAGE, "");
        return true;
    }

    private boolean validateConfiguredPhp(HttpdConfig config) {
        String pathPhpFive = config.getPhpSOPath(HttpdConfig.VERSION_5);
        String pathPhpFour = config.getPhpSOPath(HttpdConfig.VERSION_4);
        if (pathPhpFive == null && pathPhpFour == null) {
            setErrorMessage(MSG_NO_PHP_MODULE);
            return false;
        }

        if (pathPhpFive != null && pathPhpFour != null) {
            setErrorMessage(MSG_TOO_MANY_PHP_MODULES);
            return false;
        }

        String path = pathPhpFive == null ? pathPhpFour : pathPhpFive;
        File file = new File(path);
        if (!file.exists()) {
            setErrorMessage(MSG_NO_PHP_SO, path);
            return false;
        }

        Collection<String> collection = config.getPhpExtensions();
        boolean phpExtensionFound = false;
        StringBuilder builder = new StringBuilder();
        for (String string : collection) {
            builder.append(string);
            builder.append(" ");
            if (string.equals("." + HttpdConfig.PHP)) {
                phpExtensionFound = true;
                break;
            }
        }
        if (!phpExtensionFound) {
            if (!collection.isEmpty()) {
                setErrorMessage(MSG_EXTENSIONS, builder.toString());
            } else {
                setErrorMessage(MSG_NO_PHP_EXTENSIONS);
                return false;
            }
        }
        return true;
    }

    private void configureProgressPanel(JComponent progressComponent) {
        if (myProgress != null) {
            myProgressContainer.remove(myProgress);
        }

        if (progressComponent != null) {
            myProgressContainer.add(progressComponent, BorderLayout.CENTER);
        }
        myProgress = progressComponent;
        myProgressContainer.validate();
        validate();

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myButtonGroup = new javax.swing.ButtonGroup();
        myManualButton = new javax.swing.JRadioButton();
        myAutoButton = new javax.swing.JRadioButton();
        myLocation = new javax.swing.JTextField();
        myLocationAuto = new javax.swing.JComboBox();
        myBrowse = new javax.swing.JButton();
        myAuto = new javax.swing.JButton();
        myLabel1 = new javax.swing.JLabel();
        myLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        myChoose = new javax.swing.JLabel();
        myHosts = new javax.swing.JComboBox();
        myProgressContainer = new javax.swing.JPanel();

        setName(org.openide.util.NbBundle.getBundle(ServerChooserVisual.class).getString("LBL_ChooseServer")); // NOI18N

        myButtonGroup.add(myManualButton);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/php/rt/providers/impl/local/apache/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(myManualButton, bundle.getString("LBL_Manual")); // NOI18N
        myManualButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        myManualButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        myButtonGroup.add(myAutoButton);
        org.openide.awt.Mnemonics.setLocalizedText(myAutoButton, bundle.getString("LBL_Auto")); // NOI18N
        myAutoButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        myAutoButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        myAutoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myAutoButtonActionPerformed(evt);
            }
        });

        myLocation.setToolTipText(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "LBL_Select_config_Location_TIP")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myBrowse, bundle.getString("LBL_Browse")); // NOI18N
        myBrowse.setActionCommand(bundle.getString("BROWSE")); // NOI18N
        myBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doBrowse(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(myAuto, bundle.getString("LBL_BTN_Perform_Auto")); // NOI18N
        myAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doAutoConfigure(evt);
            }
        });

        myLabel1.setLabelFor(myLocation);
        org.openide.awt.Mnemonics.setLocalizedText(myLabel1, bundle.getString("LBL_Location")); // NOI18N

        myLabel2.setLabelFor(myLocationAuto);
        org.openide.awt.Mnemonics.setLocalizedText(myLabel2, bundle.getString("LBL_LocationAuto")); // NOI18N

        myChoose.setLabelFor(myHosts);
        org.openide.awt.Mnemonics.setLocalizedText(myChoose, bundle.getString("LBL_Host")); // NOI18N

        myProgressContainer.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myProgressContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(myLabel2)
                                .add(61, 61, 61))
                            .add(layout.createSequentialGroup()
                                .add(myLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(myLocationAuto, 0, 215, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, myLocation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(myBrowse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(myAuto, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myManualButton)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, myAutoButton)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(myChoose, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(34, 34, 34)
                        .add(myHosts, 0, 329, Short.MAX_VALUE))
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(myManualButton)
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myLabel1)
                    .add(myBrowse)
                    .add(myLocation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(myAutoButton)
                .add(15, 15, 15)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myLabel2)
                    .add(myAuto)
                    .add(myLocationAuto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(21, 21, 21)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myChoose)
                    .add(myHosts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(myProgressContainer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        myManualButton.getAccessibleContext().setAccessibleName(bundle.getString("A11_Manual")); // NOI18N
        myAutoButton.getAccessibleContext().setAccessibleName(bundle.getString("A11_Auto")); // NOI18N
        myLocation.getAccessibleContext().setAccessibleDescription(bundle.getString("A11_Location_Txt")); // NOI18N
        myLocationAuto.getAccessibleContext().setAccessibleDescription(bundle.getString("A11_Location_Combo")); // NOI18N
        myBrowse.getAccessibleContext().setAccessibleName(bundle.getString("A11_Browse")); // NOI18N
        myAuto.getAccessibleContext().setAccessibleName(bundle.getString("A11_Perform_Auto")); // NOI18N
        myLabel2.getAccessibleContext().setAccessibleName(bundle.getString("A11_Platform")); // NOI18N
        myChoose.getAccessibleContext().setAccessibleName(bundle.getString("A11_Choose_Host")); // NOI18N
        myHosts.getAccessibleContext().setAccessibleDescription(bundle.getString("A11_Host_Combo")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void doAutoConfigure(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doAutoConfigure
        String title = NbBundle.getMessage(ServerChooserPanel.class, "LBL_BTN_Perform_Auto");
        ProgressHandle progress = ProgressHandleFactory.createHandle(title); // NOI18N
        JComponent progressComponent = ProgressHandleFactory.createProgressComponent(progress);
        configureProgressPanel(progressComponent);
        progress.start();

        SolarisPackageFinder finder = new SolarisPackageFinder();
        String[] locations = finder.getPlatformLocations();
        if (locations == null || locations.length == 0) {
            setErrorMessage(MSG_FAILED_AUTOCONFIG);
        } else {
            myLocationAuto.setModel(new DefaultComboBoxModel(locations));
            myWizard.putProperty(WIZARD_PANEL_ERROR_MESSAGE, "");
            setHosts();
        }
        getPanel().stateChanged();

        progress.finish();
        configureProgressPanel(null);
    }//GEN-LAST:event_doAutoConfigure

    private void doBrowse(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doBrowse
        String command = evt.getActionCommand();

        if (NbBundle.getMessage(ServerChooserPanel.class, BROWSE).equals(command)) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(NbBundle.getMessage(ServerChooserPanel.class, SELECT_CONFIG_LOCATION));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            String path = myLocation.getText();
            if (path.length() > 0) {
                File f = new File(path);
                if (f.exists()) {
                    chooser.setSelectedFile(f);
                }
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File configFile = chooser.getSelectedFile();
                myLocation.setText(configFile.getAbsolutePath());
                setHosts();
            }
            getPanel().stateChanged();
        }
    }//GEN-LAST:event_doBrowse

    private void myAutoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myAutoButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_myAutoButtonActionPerformed

    private boolean validatePlatform() {
        if (myManualButton.isSelected()) {
            return validateCustomPath();
        } else if (myAutoButton.isSelected()) {
            return validateAutoConfig();
        }
        return false;
    }

    private boolean validateAutoConfig() {
        String message = "";
        String str = (String) myWizard.getProperty( WIZARD_PANEL_ERROR_MESSAGE);

        if (str != null && str.length() > 0) {
            /*
             * In the case when autoconfig button was pressed and it already sets
             * error message. We use it now for showing.
             */
            message = str;
        } else if (myLocationAuto.getItemCount() == 0) {
            // there was no autoconfig action yet
            message = NbBundle.getMessage(ServerChooserVisual.class, MSG_AUTOCONF_NOT_PERFORMED);
        }
        myWizard.putProperty(WIZARD_PANEL_ERROR_MESSAGE, message);
        if (message.length() == 0) {
            String path = null;
            if (myLocationAuto.getSelectedItem() != null){
                path = myLocationAuto.getSelectedItem().toString();
            }
            return validateLocation(path);
        } else {
            return false;
        }
    }

    private void setHosts() {
        HttpdConfig config = getAndSetHttpdConfig();
        HttpdHost[] hosts = config.getHosts();

        updateConfigsCache(hosts);
        myHosts.setModel(new DefaultComboBoxModel(hosts));
    }

    private void updateConfigsCache(HttpdHost[] hosts){
        for (HttpdHost host : hosts){
            String key = host.getName()+":"+host.getPort();
            if (!myKnownConfigs.containsKey(key)){
                myKnownConfigs.put(key, host.getPlarformPath());
            }
        }
    }
    
    private HttpdConfig getHttpdConfig() {
        if (myConfig == null) {
            return getAndSetHttpdConfig();
        } else {
            return myConfig;
        }
    }

    private HttpdConfig getAndSetHttpdConfig() {
        //String platformPath = null;
        String configPath = null;
        if (myManualButton.isSelected()) {
            //platformPath = myLocation.getText();
            //configPath = platformPath+PATH_TO_HTTPD_CONF;
            // we now browse directly to config file.
            configPath = myLocation.getText();
        } else if (myAutoButton.isSelected()) {
            Object obj = myLocationAuto.getSelectedItem();
            if (obj != null) {
                //platformPath = myLocationAuto.getSelectedItem().toString();
                //configPath = platformPath+PATH_TO_HTTPD_CONF;
                configPath = myLocationAuto.getSelectedItem().toString();
            }
        }
        //myConfig = new HttpdConfig(platformPath, configPath);
        myConfig = new HttpdConfig(configPath);
        return myConfig;
    }

    private boolean validateCustomPath() {
        String location = myLocation.getText();

        return validateLocation(location);
    }

    private boolean validateLocation(String location) {
        if (location == null || location.trim().length() == 0) {
            setErrorMessage(MSG_EMPTY_CONFIG_FILE);
            return false;
        }
        File file = new File(location).getAbsoluteFile();
        //String suggestedConf = file.getAbsolutePath();
        //File confDir = new File(suggestedConf).getAbsoluteFile();
        File canonicalFile = getCanonicalFile(file);
        if (canonicalFile == null 
                || !canonicalFile.exists() || !canonicalFile.isFile() ) 
        {
            setErrorMessage(MSG_ILLEGAL_CONFIG_FILE_LOCATION);
            return false;
        }

        myWizard.putProperty(WIZARD_PANEL_ERROR_MESSAGE, "");
        return true;
    }

    private ServerChooserPanel getPanel() {
        return myPanel;
    }

    private void setErrorMessage(String key, Object... args) {
        String message = null;
        if (args.length > 0) {
            message = MessageFormat.format(
                    NbBundle.getMessage(ServerChooserVisual.class, key), args);
        } else {
            message = NbBundle.getMessage(ServerChooserVisual.class, key);
        }
        myWizard.putProperty(WIZARD_PANEL_ERROR_MESSAGE, message);
    }

    public static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return null;
        }
    }

    private class RadioListener implements ActionListener {

        /*
         * (non-Javadoc)
         *
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent event) {
            Object obj = event.getSource();
            for (Entry<JRadioButton, Component[]> entry : myMap.entrySet()) {
                JRadioButton button = entry.getKey();
                Component[] components = entry.getValue();
                for (Component component : components) {
                    component.setEnabled(button == obj);
                }
            }
            if (myWizard != null) {
                myWizard.putProperty(WIZARD_PANEL_ERROR_MESSAGE, "");
            }
            getPanel().stateChanged();
        }
    }

    private class TextFieldListener implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
            actionPerformed();
        }

        public void insertUpdate(DocumentEvent e) {
            actionPerformed();
        }

        public void removeUpdate(DocumentEvent e) {
            actionPerformed();
        }

        private void actionPerformed() {
            setHosts();
            getPanel().stateChanged();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton myAuto;
    private javax.swing.JRadioButton myAutoButton;
    private javax.swing.JButton myBrowse;
    private javax.swing.ButtonGroup myButtonGroup;
    private javax.swing.JLabel myChoose;
    private javax.swing.JComboBox myHosts;
    private javax.swing.JLabel myLabel1;
    private javax.swing.JLabel myLabel2;
    private javax.swing.JTextField myLocation;
    private javax.swing.JComboBox myLocationAuto;
    private javax.swing.JRadioButton myManualButton;
    private javax.swing.JPanel myProgressContainer;
    // End of variables declaration//GEN-END:variables
    private ServerChooserPanel myPanel;
    private Map<JRadioButton, Component[]> myMap = new HashMap<JRadioButton, Component[]>(2);
    private AddHostWizard myWizard;
    private HttpdConfig myConfig;
    private JComponent myProgress;
    private Map<String, String> myKnownConfigs = new HashMap<String, String>();
}