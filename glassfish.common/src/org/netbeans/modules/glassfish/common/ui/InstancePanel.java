/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.ui;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import org.glassfish.tools.ide.utils.NetUtils;
import org.glassfish.tools.ide.utils.ServerUtils;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import org.netbeans.modules.glassfish.common.GlassFishSettings;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.PortCollection;
import org.netbeans.modules.glassfish.common.utils.Util;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.openide.util.NbBundle;

/**
 * Local instance properties editor.
 * <p/>
 * @author Tomas Kraus
 */
public class InstancePanel extends javax.swing.JPanel {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Properties for check box fields.
     */
    private static class CheckBoxProperties {

        /** Comet support property. */
        final String cometSupportProperty;

        /** HTTP monitor property. */
        final String httpMonitorProperty;

        /** JDBC driver deployment property. */
        final String jdbcDriverDeploymentProperty;

        /** Preserve sessions property. */
        final String preserveSessionsProperty;

        /** Start Derby property. */
        final String startDerbyProperty;

        /**
         * Creates an instance of check box fields properties by retrieving them
         * from GlassFish instance object.
         * <p/>
         * @param instance GlassFish instance object containing check box
         *        fields properties.
         */
        private CheckBoxProperties(final GlassfishInstance instance) {
            String cometSupportPropertyTmp
                    = instance.getProperty(GlassfishModule.COMET_FLAG);
            cometSupportProperty = cometSupportPropertyTmp != null
                    ? cometSupportPropertyTmp
                    : System.getProperty(GlassfishModule.COMET_FLAG);
            httpMonitorProperty
                    = instance.getProperty(GlassfishModule.HTTP_MONITOR_FLAG);
            jdbcDriverDeploymentProperty
                    = instance.getProperty(GlassfishModule.DRIVER_DEPLOY_FLAG);
            preserveSessionsProperty
                    = instance.getProperty(
                    GlassfishModule.SESSION_PRESERVATION_FLAG);
            startDerbyProperty
                    = instance.getProperty(GlassfishModule.START_DERBY_FLAG);
            
        }

        /**
         * Store given <code>boolean</code> property into GlassFish instance
         * object properties.
         * <p/>
         * @param key      GlassFish instance object property key.
         * @param value    GlassFish instance object property value.
         * @param instance GlassFish instance object to store properties.
         */
        private void storeBooleanProperty(final String key, final boolean value,
                final GlassfishInstance instance) {
            // Store true value as String property.
            if (value) {
                instance.putProperty(key, Boolean.toString(value));
            // Store false valye by removal of property.
            } else {
                instance.removeProperty(key);
            }
        }

        /**
         * Store check box fields flags by setting them as GlassFish instance
         * object properties.
         * <p/>
         * @param cometSupportFlag         Comet support flag.
         * @param httpMonitorFlag          HTTP monitor flag.
         * @param jdbcDriverDeploymentFlag JDBC driver deployment flag.
         * @param preserveSessionsFlag     Preserve sessions flag.
         * @param startDerbyFlag           Start Derby flag.
         * @param instance                 GlassFish instance object to store
         *                                 check box fields properties.
         */
        private void store(final boolean cometSupportFlag,
                final boolean httpMonitorFlag,
                final boolean jdbcDriverDeploymentFlag,
                final boolean preserveSessionsFlag,
                final boolean startDerbyFlag,
                final GlassfishInstance instance) {
            // Update properties only when stored value differs.
            if (cometSupportFlag != getCommetSupportProperty()) {
                // Comet support is always stored into instance when differs.
                instance.putProperty(GlassfishModule.COMET_FLAG,
                        Boolean.toString(cometSupportFlag));
            }
            if (httpMonitorFlag != getHttpMonitorProperty()) {
                storeBooleanProperty(GlassfishModule.HTTP_MONITOR_FLAG,
                        httpMonitorFlag, instance);
            }
            if (jdbcDriverDeploymentFlag != getJdbcDriverDeploymentProperty()) {
                storeBooleanProperty(GlassfishModule.DRIVER_DEPLOY_FLAG,
                        jdbcDriverDeploymentFlag, instance);
            }
            if (preserveSessionsFlag != getPreserveSessionsProperty()) {
                storeBooleanProperty(GlassfishModule.SESSION_PRESERVATION_FLAG,
                        preserveSessionsFlag, instance);
            }
            if (startDerbyFlag != getStartDerbyProperty()) {
                storeBooleanProperty(GlassfishModule.START_DERBY_FLAG,
                        startDerbyFlag, instance);
            }
        }

        /**
         * Get Comet support property
         * <p/>
         * @return Comet support property.
         */
        private boolean getCommetSupportProperty() {
            return Boolean.parseBoolean(cometSupportProperty);
        }

        /**
         * Get HTTP monitor property.
         * <p/>
         * @return HTTP monitor property.
         */
        private boolean getHttpMonitorProperty() {
            return Boolean.parseBoolean(httpMonitorProperty);
        }

        /**
         * Get JDBC driver deployment property.
         * <p/>
         * @return JDBC driver deployment property.
         */
        private boolean getJdbcDriverDeploymentProperty() {
            return Boolean.parseBoolean(jdbcDriverDeploymentProperty);
        }

        /**
         * Get preserve sessions property.
         * <p/>
         * @return Preserve sessions property.
         */
        private boolean getPreserveSessionsProperty() {
            return Boolean.parseBoolean(preserveSessionsProperty);
        }

        /**
         * Get start Derby property.
         * <p/>
         * @return Start Derby property.
         */
        private boolean getStartDerbyProperty() {
            return Boolean.parseBoolean(startDerbyProperty);
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(InstancePanel.class);

    /** Maximum port number value. */
    private static final int MAX_PORT_VALUE = 0x10000 - 0x01;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish server instance to be modified. */
    private final GlassfishInstance instance;
    
    /** IP addresses selection content. */
    private Set<? extends InetAddress> ips;

    /** Comet support flag. */
    private boolean cometSupportFlag;

    /** HTTP monitor flag. */
    private boolean httpMonitorFlag;

    /** JDBC driver deployment flag. */
    private boolean jdbcDriverDeploymentFlag;

    /** Show password text in this form flag. */
    private boolean showPasswordFlag;

    /** Preserve sessions flag. */
    private boolean preserverSessionsFlag;

    /** Start Derby flag. */
    private boolean startDerbyFlag;

    /** Configuration file <code>domain.xml</code> was parsed successfully. */
    private boolean configFileParsed;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates new form InstanceLocalPanel
     */
    public InstancePanel(final GlassfishInstance instance) {
        this.instance = instance;
        ips = NetUtils.getHostIP4s();
        initComponents();
        ((AbstractDocument)dasPortField.getDocument())
                .setDocumentFilter(new Filter.PortNumber());
        ((AbstractDocument)httpPortField.getDocument())
                .setDocumentFilter(new Filter.PortNumber());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get host field value to be stored into local GlassFish server instance
     * object properties.
     * <p/>
     * @return Host field value converted to {@link String}
     */
    private String getHost() {
        Object hostValue = hostField.getEditor().getItem();
        if (hostValue instanceof IpComboBox.InetAddr) {
            return ((IpComboBox.InetAddr)hostValue).toString();
        } else if (hostValue instanceof String) {
            return (String)hostValue;
        } else {
            return IpComboBox.IP_4_127_0_0_1_NAME;
        }
    }

    /**
     * Read server configuration from <code>domain.xml</code> if exists
     * and update corresponding fields.
     * <p/>
     * Port fields values can't be changed when values are coming from server's
     * <code>domain.xml</code> file.
     */
    private void initFromServerConfig() {
        PortCollection ports = new PortCollection();
        String domainPath = ServerUtils.getDomainPath(instance);
        if (configFileParsed
                = Util.readServerConfiguration(new File(domainPath), ports)) {
            dasPortField.setText(Integer.toString(ports.getAdminPort()));
            httpPortField.setText(Integer.toString(ports.getHttpPort()));
        } else {
            dasPortField.setText(Integer.toString(instance.getAdminPort()));
            httpPortField.setText(Integer.toString(instance.getPort()));
        }
    }

    /**
     * Installation and domain directories fields initialization.
     * <p/>
     * Initialize installation root and domains directory fields  with values
     * stored in GlassFish instance object.
     */
    private void initDirectoriesFields() {
        installationLocationField.setText(instance.getGlassfishRoot());
        domainsFolderField.setText(instance.getDomainsFolder());
    }

    /**
     * Host field initialization.
     * <p/>
     * Attempts to initialize host combo box to existing IP address. Host name
     * string is used as a fallback.
     */
    private void initHost() {
        String hostProperty = instance.getHost();
        InetAddress addr;
        try {
            addr = InetAddress.getByName(hostProperty);
            localIpCB.setSelected(addr.isLoopbackAddress());
        } catch (UnknownHostException uhe) {
            addr = null;
            localIpCB.setSelected(true);
            LOGGER.log(Level.INFO,
                    NbBundle.getMessage(InstancePanel.class,
                    "InstanceLocalPanel.initHost.unknownHost", hostProperty));
        }
        ((IpComboBox)hostField).updateModel(ips, localIpCB.isSelected());
        if (addr != null && ips.contains(addr)) {
            ((IpComboBox)hostField).setSelectedItem(addr);
        } else {
            ((IpComboBox)hostField).getEditor().setItem(hostProperty);
        }
    }

    /**
     * Domain name and target fields initialization.
     * <p/>
     * initialize domain name and target fields with values stored in GlassFish
     * instance object.
     */
    private void initDomainAndTarget() {
        String target = instance.getTarget();
        String domainName = instance.getDomainName();
        domainField.setText(domainName != null ? domainName : "");
        targetField.setText(target != null ? target : "");
    }

    /**
     * Credential fields initialization.
     * <p/>
     * Initialize user name and password fields with values stored in GlassFish
     * instance object.
     */
    private void initCredentials() {
        userNameField.setText(instance.getUserName());
        passwordField.setText(instance.getPassword());
    }

    /**
     * Initialize internal properties storage from GlassFish instance object
     * properties.
     * <p/>
     * @param properties GlassFish instance object properties for check boxes.
     */
    private void initFlagsFromProperties(final CheckBoxProperties properties) {
        cometSupportFlag = properties.getCommetSupportProperty();
        httpMonitorFlag = properties.getHttpMonitorProperty();
        jdbcDriverDeploymentFlag= properties.getJdbcDriverDeploymentProperty();
        preserverSessionsFlag = properties.getPreserveSessionsProperty();
        startDerbyFlag = properties.getStartDerbyProperty();        
    }

    /**
     * Check box fields initialization.
     * <p/>
     * Initialize check box fields to allow GlassFish instance flags
     * modification and allowing user to display password text in password
     * field.
     */
    private void initCheckBoxes() {
        // Retrieve properties from GlassFish instance object.
        initFlagsFromProperties(new CheckBoxProperties(instance));
        // Initialize internal properties storage.
        showPasswordFlag
                = GlassFishSettings.getGfShowPasswordInPropertiesForm();
        // Set form fields values.
        commetSupport.setSelected(cometSupportFlag);
        httpMonitor.setSelected(httpMonitorFlag);
        jdbcDriverDeployment.setSelected(jdbcDriverDeploymentFlag);
        showPassword.setSelected(showPasswordFlag);
        preserveSessions.setSelected(preserverSessionsFlag);
        startDerby.setSelected(startDerbyFlag);
    }

    /**
     * Host name field storage.
     * <p/>
     * Store host field content when form fields value differs from GlassFish
     * instance property.
     */
    private void storeHost() {
        String host = getHost();
        if (!host.equals(instance.getHost())) {
            instance.setHost(host);
        }
    }

    /**
     * Check box fields storage.
     * <p/>
     * Store check box fields after GlassFish instance flags modification
     * and store current status of allowing user to display password text
     * in password field.
     */
    private void storeCheckBoxes() {
        CheckBoxProperties properties = new CheckBoxProperties(instance);
        properties.store(cometSupportFlag, httpMonitorFlag,
                jdbcDriverDeploymentFlag, preserverSessionsFlag,
                startDerbyFlag, instance);
        GlassFishSettings.setGfShowPasswordInPropertiesForm(showPasswordFlag);
    }

    /**
     * DAS and HTTP ports fields storage.
     * <p/>
     * Validate and store DAS and HTTP ports fields when form fields values
     * differs from GlassFish instance properties.
     */
    private void storePorts() {
        final String dasPortStr = dasPortField.getText().trim();
        final String httpPortStr = httpPortField.getText().trim();
        try {
            int dasPort = Integer.parseInt(dasPortStr);
            if (0 <= dasPort && dasPort < MAX_PORT_VALUE) {
                // Update value only when values differs.
                if (instance.getAdminPort() != dasPort) {
                    instance.setAdminPort(dasPort);
                }
            } else {
                LOGGER.log(Level.INFO,
                        NbBundle.getMessage(InstancePanel.class,
                        "InstanceLocalPanel.storePorts.dasPortRange",
                        dasPortStr));
            }
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(InstancePanel.class,
                    "InstanceLocalPanel.storePorts.dasPortInvalid",
                    dasPortStr));
        }
        try {
            int httpPort = Integer.parseInt(httpPortStr);
            if (0 <= httpPort && httpPort < MAX_PORT_VALUE) {
                if (instance.getPort() != httpPort) {
                    instance.setHttpPort(httpPort);
                }
            } else {
                LOGGER.log(Level.INFO,
                        NbBundle.getMessage(InstancePanel.class,
                        "InstanceLocalPanel.storePorts.httpPortRange",
                        dasPortStr));
            }
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(InstancePanel.class,
                    "InstanceLocalPanel.storePorts.httpPortInvalid",
                    httpPortStr));
        }
    }

    /**
     * Administrator user credentials storage.
     * <p/>
     * Store administrator user name and password when form fields values
     * differs from GlassFish instance properties.
     */
    private void storeCredentials() {
        final String userName = userNameField.getText().trim();
        final String password = new String(passwordField.getPassword());
        if (!userName.equals(instance.getAdminUser())) {
            instance.setAdminUser(userName);
        }
        if (!password.equals(instance.getAdminPassword())) {
            instance.setAdminPassword(password);
        }
    }

    /**
     * Enable form fields that can be modified by user.
     * <p/>
     * Set those form fields that can be modified by user as enabled. This
     * is usually done after form has been initialized when all form fields
     * are currently disabled.
     */
    private void enableFields() {
        if (!configFileParsed) {
            dasPortField.setEnabled(true);
            httpPortField.setEnabled(true);
        }
        hostField.setEnabled(true);
//      Not implemented yet
//        domainField.setEnabled(true);
        targetField.setEnabled(true);
        userNameField.setEnabled(true);
        passwordField.setEnabled(true);
        commetSupport.setEnabled(true);
        httpMonitor.setEnabled(true);
        jdbcDriverDeployment.setEnabled(true);
        showPassword.setEnabled(true);
        preserveSessions.setEnabled(true);
        startDerby.setEnabled(true);
    }

    /**
     * Disable all form fields.
     * <p/>
     * Set all form fields as disabled. This is usually done when form is being
     * initialized or stored.
     */
    private void disableAllFields() {
        installationLocationField.setEnabled(false);
        domainsFolderField.setEnabled(false);
        hostField.setEnabled(false);
        dasPortField.setEnabled(false);
        httpPortField.setEnabled(false);
        domainField.setEnabled(false);
        targetField.setEnabled(false);
        userNameField.setEnabled(false);
        passwordField.setEnabled(false);
        commetSupport.setEnabled(false);
        httpMonitor.setEnabled(false);
        jdbcDriverDeployment.setEnabled(false);
        showPassword.setEnabled(false);
        preserveSessions.setEnabled(false);
        startDerby.setEnabled(false);
    }

    /**
     * Initialize form field values from GlassFish server entity object.
     * <p/>
     * This is top level initialization method used when entering form.
     */
    private void initFormFields() {
        initFromServerConfig();
        initDirectoriesFields();
        initHost();
        initDomainAndTarget();
        initCredentials();
        initCheckBoxes();
        updatePasswordVisibility();
    }

    /**
     * Store form field values into GlassFish server entity object.
     * <p/>
     * This is top level storage method used when leaving form.
     */
    private void storeFormFields() {
        storeHost();
        storePorts();
        storeCredentials();
        storeCheckBoxes();
    }

    /**
     * Called when entering this panel.
     * <p/>
     * Initialize all panel form fields.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        disableAllFields();
        initFormFields();
        enableFields();
    }

    /**
     * Called when leaving this panel.
     * <p/>
     * Store all form fields from panel.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        disableAllFields();
        storeFormFields();
    }

    /**
     * Show and hide password text depending on related check box.
     */
    private void updatePasswordVisibility() {
        showPasswordFlag = showPassword.isSelected();
        passwordField.setEchoChar(showPasswordFlag ? '\0' : '*');        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Generated GUI code                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hostLabel = new javax.swing.JLabel();
        localIpCB = new javax.swing.JCheckBox();
        hostField = new IpComboBox(ips, localIpCB.isSelected());
        dasPortLabel = new javax.swing.JLabel();
        dasPortField = new javax.swing.JTextField();
        httpPortLabel = new javax.swing.JLabel();
        httpPortField = new javax.swing.JTextField();
        domainLabel = new javax.swing.JLabel();
        domainField = new javax.swing.JTextField();
        targetLabel = new javax.swing.JLabel();
        targetField = new javax.swing.JTextField();
        installationLocationLabel = new javax.swing.JLabel();
        installationLocationField = new javax.swing.JTextField();
        domainsFolderLabel = new javax.swing.JLabel();
        domainsFolderField = new javax.swing.JTextField();
        userNameLabel = new javax.swing.JLabel();
        userNameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        commetSupport = new javax.swing.JCheckBox();
        preserveSessions = new javax.swing.JCheckBox();
        httpMonitor = new javax.swing.JCheckBox();
        startDerby = new javax.swing.JCheckBox();
        jdbcDriverDeployment = new javax.swing.JCheckBox();
        showPassword = new javax.swing.JCheckBox();
        passwordField = new javax.swing.JPasswordField();

        setName(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.displayName")); // NOI18N
        setPreferredSize(new java.awt.Dimension(602, 304));

        hostLabel.setLabelFor(hostField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.hostLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(localIpCB, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.localIpCB")); // NOI18N
        localIpCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localIpCBActionPerformed(evt);
            }
        });

        hostField.setEditable(true);

        dasPortLabel.setLabelFor(dasPortField);
        org.openide.awt.Mnemonics.setLocalizedText(dasPortLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.dasPortLabel")); // NOI18N

        dasPortField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.dasPortField.text")); // NOI18N

        httpPortLabel.setLabelFor(httpPortField);
        org.openide.awt.Mnemonics.setLocalizedText(httpPortLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.httpPortLabel")); // NOI18N

        httpPortField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.httpPortField.text")); // NOI18N

        domainLabel.setLabelFor(domainField);
        org.openide.awt.Mnemonics.setLocalizedText(domainLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.domainLabel")); // NOI18N

        domainField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.domainField.text")); // NOI18N

        targetLabel.setLabelFor(targetField);
        org.openide.awt.Mnemonics.setLocalizedText(targetLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.targetLabel")); // NOI18N

        targetField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.targetField.text")); // NOI18N

        installationLocationLabel.setLabelFor(installationLocationField);
        org.openide.awt.Mnemonics.setLocalizedText(installationLocationLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.InstallationlocationLabel")); // NOI18N

        installationLocationField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.installationLocationField.text")); // NOI18N

        domainsFolderLabel.setLabelFor(domainsFolderField);
        org.openide.awt.Mnemonics.setLocalizedText(domainsFolderLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.domainsFolderLabel")); // NOI18N

        domainsFolderField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.domainsFolderField.text")); // NOI18N

        userNameLabel.setLabelFor(userNameField);
        org.openide.awt.Mnemonics.setLocalizedText(userNameLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.userNameLabel")); // NOI18N

        userNameField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.userNameField.text")); // NOI18N

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.passwordLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(commetSupport, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.commetSupport")); // NOI18N
        commetSupport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commetSupportActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(preserveSessions, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.preserveSessions")); // NOI18N
        preserveSessions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preserveSessionsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(httpMonitor, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.httpMonitor")); // NOI18N
        httpMonitor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                httpMonitorActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(startDerby, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.startDerby")); // NOI18N
        startDerby.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startDerbyActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jdbcDriverDeployment, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.jdbcDriverDeployment")); // NOI18N
        jdbcDriverDeployment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jdbcDriverDeploymentActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(showPassword, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.showPassword")); // NOI18N
        showPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPasswordActionPerformed(evt);
            }
        });

        passwordField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.passwordField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(installationLocationLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(domainsFolderLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(domainsFolderField)
                            .add(installationLocationField)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(hostLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(domainLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(dasPortLabel)
                            .add(userNameLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(hostField, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(localIpCB))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, domainField)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, dasPortField)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, userNameField))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(httpPortLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(targetLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(passwordLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(httpPortField)
                                    .add(targetField)
                                    .add(passwordField)))))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jdbcDriverDeployment, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, httpMonitor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(commetSupport, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(preserveSessions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                            .add(startDerby, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(showPassword, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {dasPortLabel, domainLabel, hostLabel, httpPortLabel, passwordLabel, targetLabel, userNameLabel}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {domainsFolderLabel, installationLocationLabel}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(installationLocationLabel)
                    .add(installationLocationField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(domainsFolderLabel)
                    .add(domainsFolderField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostLabel)
                    .add(hostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(localIpCB))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dasPortLabel)
                    .add(dasPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(httpPortLabel)
                    .add(httpPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(domainLabel)
                    .add(domainField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(targetLabel)
                    .add(targetField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE, false)
                    .add(userNameField)
                    .add(passwordLabel)
                    .add(userNameLabel)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(commetSupport)
                    .add(showPassword))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(httpMonitor)
                    .add(preserveSessions))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jdbcDriverDeployment)
                    .add(startDerby))
                .addContainerGap(13, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void commetSupportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commetSupportActionPerformed
        cometSupportFlag = commetSupport.isSelected();
    }//GEN-LAST:event_commetSupportActionPerformed

    private void preserveSessionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preserveSessionsActionPerformed
        preserverSessionsFlag = preserveSessions.isSelected();
    }//GEN-LAST:event_preserveSessionsActionPerformed

    private void showPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPasswordActionPerformed
        updatePasswordVisibility();
    }//GEN-LAST:event_showPasswordActionPerformed

    private void httpMonitorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_httpMonitorActionPerformed
        httpMonitorFlag = httpMonitor.isSelected();
    }//GEN-LAST:event_httpMonitorActionPerformed

    private void jdbcDriverDeploymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jdbcDriverDeploymentActionPerformed
        jdbcDriverDeploymentFlag = jdbcDriverDeployment.isSelected();
    }//GEN-LAST:event_jdbcDriverDeploymentActionPerformed

    private void startDerbyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startDerbyActionPerformed
        startDerbyFlag = startDerby.isSelected();
    }//GEN-LAST:event_startDerbyActionPerformed

    private void localIpCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localIpCBActionPerformed
        Object hostValue = hostField.getEditor().getItem();
        hostField.setEnabled(false);
        ((IpComboBox)hostField).updateModel(ips, localIpCB.isSelected());
        if (hostValue instanceof IpComboBox.InetAddr) {
            ((IpComboBox)hostField).setSelectedIp(
                    ((IpComboBox.InetAddr)hostValue).getIp());
        } else if (hostValue instanceof String) {
            ((IpComboBox)hostField).getEditor().setItem((String)hostValue);
        } else {
            ((IpComboBox)hostField).setSelectedItem(null);
        }
        hostField.setEnabled(true);
    }//GEN-LAST:event_localIpCBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox commetSupport;
    private javax.swing.JTextField dasPortField;
    private javax.swing.JLabel dasPortLabel;
    private javax.swing.JTextField domainField;
    private javax.swing.JLabel domainLabel;
    private javax.swing.JTextField domainsFolderField;
    private javax.swing.JLabel domainsFolderLabel;
    private javax.swing.JComboBox hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JCheckBox httpMonitor;
    private javax.swing.JTextField httpPortField;
    private javax.swing.JLabel httpPortLabel;
    private javax.swing.JTextField installationLocationField;
    private javax.swing.JLabel installationLocationLabel;
    private javax.swing.JCheckBox jdbcDriverDeployment;
    private javax.swing.JCheckBox localIpCB;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JCheckBox preserveSessions;
    private javax.swing.JCheckBox showPassword;
    private javax.swing.JCheckBox startDerby;
    private javax.swing.JTextField targetField;
    private javax.swing.JLabel targetLabel;
    private javax.swing.JTextField userNameField;
    private javax.swing.JLabel userNameLabel;
    // End of variables declaration//GEN-END:variables
}
