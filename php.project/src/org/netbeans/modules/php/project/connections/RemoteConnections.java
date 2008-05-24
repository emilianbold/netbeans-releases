/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author Tomas Mysik
 */
public final class RemoteConnections {

    // XXX temporary
    public static final String DEBUG_PROPERTY = "remote.connections"; // NOI18N

    public static enum ConnectionType {
        FTP ("LBL_Ftp"); // NOI18N

        private final String label;

        private ConnectionType(String labelKey) {
            label = NbBundle.getMessage(RemoteConnections.class, labelKey);
        }

        public String getLabel() {
            return label;
        }
    }

    static final Logger LOGGER = Logger.getLogger(RemoteConnections.class.getName());

    private static final String PREFERENCES_PATH = "RemoteConnections"; // NOI18N

    private static final ConnectionType DEFAULT_TYPE = ConnectionType.FTP;
    private static final int DEFAULT_PORT = 21;
    private static final int DEFAULT_TIMEOUT = 30;

    static final String TYPE = "type"; // NOI18N
    static final String HOST = "host"; // NOI18N
    static final String PORT = "port"; // NOI18N
    static final String USER = "user"; // NOI18N
    static final String PASSWORD = "password"; // NOI18N
    static final String ANONYMOUS_LOGIN = "anonymousLogin"; // NOI18N
    static final String INITIAL_DIRECTORY = "initialDirectory"; // NOI18N
    static final String TIMEOUT = "timeout"; // NOI18N

    static final String[] PROPERTIES = new String[] {
        TYPE,
        HOST,
        PORT,
        USER,
        PASSWORD,
        ANONYMOUS_LOGIN,
        INITIAL_DIRECTORY,
        TIMEOUT,
    };

    private final ConfigManager configManager;
    private final ConfigManager.ConfigProvider configProvider = new DefaultConfigProvider();
    private final ChangeListener defaultChangeListener = new DefaultChangeListener();
    RemoteConnectionsPanel panel = null;
    private DialogDescriptor descriptor = null;

    public static RemoteConnections get() {
        return new RemoteConnections();
    }

    private RemoteConnections() {
        configManager = new ConfigManager(configProvider);
    }

    private void initPanel() {
        if (panel != null) {
            return;
        }
        panel = new RemoteConnectionsPanel();
        // data
        panel.setConnections(getConnections());

        // listeners
        panel.addChangeListener(defaultChangeListener);
        panel.addAddButtonActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addConfig();
            }
        });
        panel.addRemoveButtonActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeConfig();
            }
        });
        panel.addConfigListListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                selectCurrentConfig();
            }
        });
    }

    /**
     * Open the UI manager for remote connections. One can easily add, remove and edit remote connections.
     */
    public void openManager() {
        initPanel();
        String title = NbBundle.getMessage(RemoteConnectionsPanel.class, "LBL_ManageRemoteConnections");
        descriptor = new DialogDescriptor(panel, title, true, null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            // XXX probably not the best solution
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (panel.getConnections().isEmpty()) {
                        // no config available => show add config dialog
                        addConfig();
                    } else {
                        // XXX allow caller to select custom connection?
                        panel.selectConnection(0);
                    }
                }
            });
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        if (descriptor.getValue() == NotifyDescriptor.OK_OPTION) {
            saveRemoteConnections();
        }
    }

    /**
     * Get the ordered list of connections. The list is order according to connection's display
     * name (locale-sensitive string comparison).
     * @return the ordered list of connections.
     */
    public List<Configuration> getConnections() {
        Collection<String> cfgNames = configManager.configurationNames();
        List<Configuration> configs = new ArrayList<Configuration>(cfgNames.size() - 1); // without default config

        for (String name : cfgNames) {
            if (name == null) {
                // default config
                continue;
            }
            Configuration cfg = configManager.configurationFor(name);
            if (cfg == null) {
                // deleted configuration
                continue;
            }
            configs.add(cfg);
        }
        Collections.sort(configs, ConfigManager.getConfigurationComparator());
        return Collections.unmodifiableList(configs);
    }

    void addConfig() {
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(NbBundle.getMessage(RemoteConnections.class, "LBL_ConnectionName"),
                NbBundle.getMessage(RemoteConnections.class, "LBL_CreateNewConnection"));

        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
            String name = d.getInputText();
            String config = name.replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N

            String err = null;
            if (name.trim().length() == 0) {
                err = NbBundle.getMessage(RemoteConnections.class, "MSG_EmptyConnectionExists");
            } else if (configManager.exists(config)) {
                err = NbBundle.getMessage(RemoteConnections.class, "MSG_ConnectionExists", config);
            }
            if (err != null) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(err, NotifyDescriptor.WARNING_MESSAGE));
                return;
            }
            Configuration cfg = configManager.createNew(config, name);
            cfg.putValue(PORT, String.valueOf(DEFAULT_PORT));
            cfg.putValue(TIMEOUT, String.valueOf(DEFAULT_TIMEOUT));
            panel.addConnection(cfg);
            configManager.markAsCurrentConfiguration(config);
        }
    }

    void removeConfig() {
        Configuration cfg = panel.getSelectedConnection();
        assert cfg != null;
        configManager.configurationFor(cfg.getName()).delete();
        panel.removeConnection(cfg); // this will change the current selection in the list => selectCurrentConfig() is called
    }

    void selectCurrentConfig() {
        Configuration cfg = panel.getSelectedConnection();

        // unregister default listener (validate() would be called soooo many times)
        panel.removeChangeListener(defaultChangeListener);

        // change the state of the fields
        panel.setEnabledFields(cfg != null);

        if (cfg != null) {
            configManager.markAsCurrentConfiguration(cfg.getName());

            panel.setConnectionName(cfg.getDisplayName());
            panel.setType(resolveType(cfg.getValue(TYPE)));
            panel.setHostName(cfg.getValue(HOST));
            panel.setPort(cfg.getValue(PORT));
            panel.setUserName(cfg.getValue(USER));
            panel.setPassword(cfg.getValue(PASSWORD));
            panel.setAnonymousLogin(resolveBoolean(cfg.getValue(ANONYMOUS_LOGIN)));
            panel.setInitialDirectory(cfg.getValue(INITIAL_DIRECTORY));
            panel.setTimeout(cfg.getValue(TIMEOUT));
        } else {
            panel.resetFields();
        }
        // register default listener
        panel.addChangeListener(defaultChangeListener);

        if (cfg != null) {
            // validate fields only if there's valid config
            validate();
        }
    }

    void validate() {
        // remember password is dangerous
        // just warning - do it every time
        if (validateRememberPassword()) {
            setWarning(null);
        }

        if (!validateHost()) {
            return;
        }

        if (!validatePort()) {
            return;
        }

        if (!validateUser()) {
            return;
        }

        if (!validateTimeout()) {
            return;
        }

        // everything ok
        setError(null);

        // check whether all the configs are errorless
        checkAllTheConfigs();
    }

    private boolean validateHost() {
        if (panel.getHostName().trim().length() == 0) {
            setError("MSG_NoHostName");
            return false;
        }
        return true;
    }

    private boolean validatePort() {
        String err = null;
        try {
            int port = Integer.parseInt(panel.getPort());
            if (port < 1) {
                err = "MSG_PortNotPositive"; // NOI18N
            }
        } catch (NumberFormatException nfe) {
            err = "MSG_PortNotNumeric"; // NOI18N
        }
        setError(err);
        return err == null;
    }

    private boolean validateUser() {
        if (panel.isAnonymousLogin()) {
            return true;
        }
        if (panel.getUserName().trim().length() == 0) {
            setError("MSG_NoUserName");
            return false;
        }
        return true;
    }

    private boolean validateTimeout() {
        String err = null;
        try {
            int timeout = Integer.parseInt(panel.getTimeout());
            if (timeout < 0) {
                err = "MSG_TimeoutNotPositive"; // NOI18N
            }
        } catch (NumberFormatException nfe) {
            err = "MSG_TimeoutNotNumeric"; // NOI18N
        }
        setError(err);
        return err == null;
    }

    private boolean validateRememberPassword() {
        if (panel.getPassword().length() > 0) {
            setWarning("MSG_PasswordRememberDangerous"); // NOI18N
            return false;
        }
        return true;
    }

    private void checkAllTheConfigs() {
        for (Configuration cfg : panel.getConnections()) {
            assert cfg != null;
            if (!cfg.isValid()) {
                panel.setError(NbBundle.getMessage(RemoteConnections.class, "MSG_InvalidConfiguration", cfg.getDisplayName()));
                assert descriptor != null;
                descriptor.setValid(false);
                return;
            }
        }
    }

    private void setError(String errorKey) {
        Configuration cfg = panel.getSelectedConnection();
        String err = errorKey != null ? NbBundle.getMessage(RemoteConnections.class, errorKey) : null;
        cfg.setErrorMessage(err);
        panel.setError(err);
        assert descriptor != null;
        descriptor.setValid(err == null);
    }

    private void setWarning(String errorKey) {
        panel.setWarning(errorKey != null ? NbBundle.getMessage(RemoteConnections.class, errorKey) : null);
    }

    private void updateActiveConfig() {
        Configuration cfg = panel.getSelectedConnection();
        if (cfg == null) {
            // no config selected
            return;
        }
        cfg.putValue(TYPE, panel.getType().name());
        cfg.putValue(HOST, panel.getHostName());
        cfg.putValue(PORT, panel.getPort());
        cfg.putValue(USER, panel.getUserName());
        cfg.putValue(PASSWORD, panel.getPassword());
        cfg.putValue(ANONYMOUS_LOGIN, String.valueOf(panel.isAnonymousLogin()));
        cfg.putValue(INITIAL_DIRECTORY, panel.getInitialDirectory());
        cfg.putValue(TIMEOUT, panel.getTimeout());
    }

    private void saveRemoteConnections() {
        Preferences remoteConnections = NbPreferences.forModule(RemoteConnections.class).node(PREFERENCES_PATH);
        for (Map.Entry<String, Map<String, String>> entry : configProvider.getConfigs().entrySet()) {
            String config = entry.getKey();
            if (config == null) {
                // no default config
                continue;
            }
            Map<String, String> cfg = entry.getValue();
            if (cfg == null) {
                // config was deleted
                try {
                    remoteConnections.node(config).removeNode();
                } catch (BackingStoreException bse) {
                    LOGGER.log(Level.INFO, "Error while removing unused remote connection: " + config, bse);
                }
            } else {
                // add/update
                Preferences node = remoteConnections.node(config);
                for (Map.Entry<String, String> cfgEntry : cfg.entrySet()) {
                    node.put(cfgEntry.getKey(), cfgEntry.getValue());
                }
            }
        }
    }

    private ConnectionType resolveType(String type) {
        if (type == null) {
            return DEFAULT_TYPE;
        }
        ConnectionType connectionType = null;
        try {
            connectionType = ConnectionType.valueOf(type);
        } catch (IllegalArgumentException iae) {
            connectionType = DEFAULT_TYPE;
        }
        return connectionType;
    }

    private boolean resolveBoolean(String value) {
        return Boolean.valueOf(value);
    }

    private class DefaultConfigProvider implements ConfigManager.ConfigProvider {
        final Map<String, Map<String, String>> configs;

        public DefaultConfigProvider() {
            configs = ConfigManager.createEmptyConfigs();
            readConfig();
        }

        public String[] getConfigProperties() {
            return PROPERTIES;
        }

        public Map<String, Map<String, String>> getConfigs() {
            return configs;
        }

        public String getActiveConfig() {
            return null;
        }

        public void setActiveConfig(String configName) {
        }

        private void readConfig() {
            Preferences remoteConnections = NbPreferences.forModule(RemoteConnections.class).node(PREFERENCES_PATH);
            try {
                for (String name : remoteConnections.childrenNames()) {
                    Preferences node = remoteConnections.node(name);
                    Map<String, String> value = new TreeMap<String, String>();
                    for (String key : node.keys()) {
                        value.put(key, node.get(key, null));
                    }
                    configs.put(name, value);
                }
            } catch (BackingStoreException bse) {
                LOGGER.log(Level.INFO, "Error while reading existing remote connections", bse);
            }
        }
    }

    private class DefaultChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            updateActiveConfig();
            validate();
        }
    }
}
