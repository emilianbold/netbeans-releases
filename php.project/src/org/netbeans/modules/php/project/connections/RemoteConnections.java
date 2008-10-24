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

import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.netbeans.modules.php.project.connections.spi.RemoteConnectionProvider;
import org.netbeans.modules.php.project.connections.ftp.FtpConnectionProvider;
import org.netbeans.modules.php.project.connections.spi.RemoteConfigurationPanel;
import org.netbeans.modules.php.project.connections.ui.RemoteConnectionsPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * @author Tomas Mysik
 */
public final class RemoteConnections {

    static final Logger LOGGER = Logger.getLogger(RemoteConnections.class.getName());

    private static final String PREFERENCES_PATH = "RemoteConnections"; // NOI18N
    private static final RequestProcessor TEST_CONNECTION_RP = new RequestProcessor("Test Remote Connection", 1); // NOI18N
    private static final RemoteConfiguration UNKNOWN_REMOTE_CONFIGURATION =
            new RemoteConfiguration.Empty("unknown-config", NbBundle.getMessage(RemoteConnections.class, "LBL_UnknownRemoteConfiguration")); // NOI18N

    private final ConfigManager configManager;
    private final ConfigManager.ConfigProvider configProvider = new DefaultConfigProvider();
    private final ChangeListener defaultChangeListener = new DefaultChangeListener();
    RemoteConnectionsPanel panel = null;
    private DialogDescriptor descriptor = null;
    private JButton testConnectionButton = null;
    private RequestProcessor.Task testConnectionTask = null;

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
        panel = new RemoteConnectionsPanel(this);
        // data
        panel.setConfigurations(getConfigurations());

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
                enableTestConnection();
            }
        });
    }

    /**
     * Open the UI manager for {@link RemoteConfiguration remote configurations} (optionally,
     * the first configuration is preselected). One can easily add, remove and edit remote configuration.
     * @return <code>true</code> if there are changes in remote configurations.
     */
    public boolean openManager() {
        return openManager(null);
    }

    /**
     * Open the UI manager for {@link RemoteConfiguration remote configurations} with the preselected
     * configuration (if possible). One can easily add, remove and edit remote configuration.
     * @param configName configuration name to be preselected, can be <code>null</code>.
     * @return <code>true</code> if there are changes in remote configurations.
     */
    public boolean openManager(final RemoteConfiguration remoteConfiguration) {
        initPanel();
        testConnectionButton = new JButton(NbBundle.getMessage(RemoteConnections.class, "LBL_TestConnection"));
        testConnectionTask = TEST_CONNECTION_RP.create(new Runnable() {
            public void run() {
                testConnection();
            }
        }, true);
        descriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(RemoteConnections.class, "LBL_ManageRemoteConnections"),
                true,
                new Object[] {testConnectionButton, NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION},
                NotifyDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        descriptor.setClosingOptions(new Object[] {NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION});
        testConnectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testConnectionTask.schedule(0);
            }
        });
        testConnectionTask.addTaskListener(new TaskListener() {
            public void taskFinished(Task task) {
                enableTestConnection();
            }
        });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        try {
            // XXX probably not the best solution
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (panel.getConfigurations().isEmpty()) {
                        // no config available => show add config dialog
                        addConfig();
                    } else {
                        // this would need to implement hashCode() and equals() for RemoteConfiguration.... hmm, probably not needed
                        //assert getConfigurations().contains(remoteConfiguration) : "Unknow remote configration: " + remoteConfiguration;
                        if (remoteConfiguration != null) {
                            // select config
                            panel.selectConfiguration(remoteConfiguration.getName());
                        } else {
                            // select the first one
                            panel.selectConfiguration(0);
                        }
                    }
                }
            });
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
        boolean changed = descriptor.getValue() == NotifyDescriptor.OK_OPTION;
        if (changed) {
            saveRemoteConnections();
        }
        return changed;
    }

    List<RemoteConnectionProvider> getConnectionProviders() {
        // XXX
        return Collections.<RemoteConnectionProvider>singletonList(FtpConnectionProvider.get());
    }

    public List<String> getRemoteConnections() {
        List<String> names = new ArrayList<String>();
        for (RemoteConnectionProvider provider : getConnectionProviders()) {
            names.add(provider.getDisplayName());
        }
        return Collections.unmodifiableList(names);
    }

    private RemoteConfiguration getRemoteConfiguration(ConfigManager.Configuration cfg) {
        for (RemoteConnectionProvider provider : getConnectionProviders()) {
            RemoteConfiguration configuration = provider.getRemoteConfiguration(cfg);
            if (configuration != null) {
                return configuration;
            }
        }
        return null;
    }

    /** can be null */
    public RemoteConfigurationPanel getConfigurationPanel(ConfigManager.Configuration cfg) {
        RemoteConfiguration remoteConfiguration = getRemoteConfiguration(cfg);
        if (remoteConfiguration == null) {
            return null;
        }
        for (RemoteConnectionProvider provider : getConnectionProviders()) {
            RemoteConfigurationPanel configurationPanel = provider.getRemoteConfigurationPanel(remoteConfiguration);
            if (configurationPanel != null) {
                return configurationPanel;
            }
        }
        return null;
    }

    /**
     * Get the ordered list of existing (already defined) {@link RemoteConfiguration remote configurations}.
     * The list is ordered according to configuration's display name (locale-sensitive string comparison).
     * @return the ordered list of all the existing remote configurations.
     * @see RemoteConfiguration
     */
    public List<RemoteConfiguration> getRemoteConfigurations() {
        // get all the configs
        List<Configuration> configs = getConfigurations();

        // convert them to remote connections
        List<RemoteConfiguration> remoteConfigs = new ArrayList<RemoteConfiguration>(configs.size());
        for (Configuration cfg : configs) {
            RemoteConfiguration configuration = getRemoteConfiguration(cfg);
            if (configuration == null) {
                // unknown configuration type => create config of unknown type
                configuration = UNKNOWN_REMOTE_CONFIGURATION;
            }
            remoteConfigs.add(configuration);
        }
        return Collections.unmodifiableList(remoteConfigs);
    }

    /**
     * Get the {@link RemoteConfiguration remote configuration} for the given name (<b>NOT</b> the display name).
     * @param name the name of the configuration.
     * @return the {@link RemoteConfiguration remote configuration} for the given name or <code>null</code> if not found.
     */
    public RemoteConfiguration remoteConfigurationForName(String name) {
        assert name != null;
        for (RemoteConfiguration remoteConfig : getRemoteConfigurations()) {
            if (remoteConfig.getName().equals(name)) {
                return remoteConfig;
            }
        }
        return null;
    }

    void testConnection() {
        testConnectionButton.setEnabled(false);

        Configuration selectedConfiguration = panel.getSelectedConfiguration();
        assert selectedConfiguration != null;
        RemoteConfiguration remoteConfiguration = getRemoteConfiguration(selectedConfiguration);
        assert remoteConfiguration != null : "Cannot find remote configuration for config manager configuration " + selectedConfiguration.getName();

        String configName = selectedConfiguration.getDisplayName();
        String progressTitle = NbBundle.getMessage(RemoteConnections.class, "MSG_TestingConnection", configName);
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(progressTitle);
        RemoteClient client = new RemoteClient(remoteConfiguration);
        RemoteException exception = null;
        try {
            progressHandle.start();
            client.connect();
        } catch (RemoteException ex) {
            exception = ex;
        } finally {
            try {
                client.disconnect();
            } catch (RemoteException ex) {
                // ignored
            }
            progressHandle.finish();
        }

        // notify user
        String msg = null;
        int msgType = 0;
        if (exception != null) {
            if (exception.getRemoteServerAnswer() == null) {
                msg = exception.getMessage();
            } else {
                msg = NbBundle.getMessage(RemoteConnections.class, "MSG_TestConnectionFailed", exception.getMessage(), exception.getRemoteServerAnswer());
            }
            msgType = NotifyDescriptor.ERROR_MESSAGE;
        } else {
            msg = NbBundle.getMessage(RemoteConnections.class, "MSG_TestConnectionSucceeded");
            msgType = NotifyDescriptor.INFORMATION_MESSAGE;
        }
        DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    msg,
                    configName,
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    msgType,
                    new Object[] {NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION));
    }

    void enableTestConnection() {
        assert testConnectionButton != null;
        assert testConnectionTask != null;
        assert panel != null;
        testConnectionButton.setEnabled(testConnectionTask.isFinished() && panel.getSelectedConfiguration() != null && panel.getSelectedConfiguration().isValid());
    }

    private List<Configuration> getConfigurations() {
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
        return configs;
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
            // XXX
//            cfg.putValue(PORT, String.valueOf(DEFAULT_PORT));
//            cfg.putValue(INITIAL_DIRECTORY, DEFAULT_INITIAL_DIRECTORY);
//            cfg.putValue(TIMEOUT, String.valueOf(DEFAULT_TIMEOUT));
            panel.addConfiguration(cfg);
            configManager.markAsCurrentConfiguration(config);
        }
    }

    void removeConfig() {
        assert panel != null;
        Configuration cfg = panel.getSelectedConfiguration();
        assert cfg != null;
        configManager.configurationFor(cfg.getName()).delete();
        panel.removeConfiguration(cfg); // this will change the current selection in the list => selectCurrentConfig() is called
    }

    void selectCurrentConfig() {
        assert panel != null;
        Configuration cfg = panel.getSelectedConfiguration();

        // unregister default listener (validate() would be called soooo many times)
        panel.removeChangeListener(defaultChangeListener);

        // change the state of the fields
        panel.setEnabledFields(cfg != null);

        if (cfg != null) {
            configManager.markAsCurrentConfiguration(cfg.getName());
            panel.setActiveConfig(cfg);
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
        assert panel != null;

        panel.isValidConfiguration();
        setError(panel.getError());

        // check whether all the configs are errorless
        checkAllTheConfigs();
    }

    private void checkAllTheConfigs() {
        for (Configuration cfg : panel.getConfigurations()) {
            assert cfg != null;
            if (!cfg.isValid()) {
                panel.setError(NbBundle.getMessage(RemoteConnections.class, "MSG_InvalidConfiguration", cfg.getDisplayName()));
                assert descriptor != null;
                descriptor.setValid(false);
                return;
            }
        }
    }

    private void setError(String error) {
        assert panel != null;
        Configuration cfg = panel.getSelectedConfiguration();
        cfg.setErrorMessage(error);
        panel.setError(error);
        assert descriptor != null;
        descriptor.setValid(error == null);
        enableTestConnection();
    }

    private void updateActiveConfig() {
        assert panel != null;
        Configuration cfg = panel.getSelectedConfiguration();
        if (cfg == null) {
            // no config selected
            return;
        }
        panel.updateActiveConfig(cfg);
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

    private class DefaultConfigProvider implements ConfigManager.ConfigProvider {
        final Map<String, Map<String, String>> configs;

        public DefaultConfigProvider() {
            configs = ConfigManager.createEmptyConfigs();
            readConfigs();
        }

        public String[] getConfigProperties() {
            Set<String> properties = new HashSet<String>();
            for (RemoteConnectionProvider provider : getConnectionProviders()) {
                properties.addAll(provider.getPropertyNames());
            }
            return properties.toArray(new String[properties.size()]);
        }

        public Map<String, Map<String, String>> getConfigs() {
            return configs;
        }

        public String getActiveConfig() {
            return null;
        }

        public void setActiveConfig(String configName) {
        }

        private void readConfigs() {
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
