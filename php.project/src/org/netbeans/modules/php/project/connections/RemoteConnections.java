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
import java.util.ArrayList;
import java.util.Arrays;
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
import org.netbeans.modules.php.project.PhpPreferences;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.netbeans.modules.php.project.connections.spi.RemoteConnectionProvider;
import org.netbeans.modules.php.project.connections.ftp.FtpConnectionProvider;
import org.netbeans.modules.php.project.connections.sftp.SftpConnectionProvider;
import org.netbeans.modules.php.project.connections.spi.RemoteConfigurationPanel;
import org.netbeans.modules.php.project.connections.ui.RemoteConnectionsPanel;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class RemoteConnections {

    static final Logger LOGGER = Logger.getLogger(RemoteConnections.class.getName());
    // Do not change arbitrary - consult with layer's folder OptionsExport
    private static final String PREFERENCES_PATH = "RemoteConnections"; // NOI18N
    private static final RemoteConfiguration UNKNOWN_REMOTE_CONFIGURATION =
            new RemoteConfiguration.Empty("unknown-config", NbBundle.getMessage(RemoteConnections.class, "LBL_UnknownRemoteConfiguration")); // NOI18N

    private final ConfigManager configManager;
    private final ConfigManager.ConfigProvider configProvider = new DefaultConfigProvider();
    RemoteConnectionsPanel panel = null;

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
        panel = new RemoteConnectionsPanel(this, configManager);
        panel.setConfigurations(getConfigurations());
    }

    private static Preferences getPreferences() {
        return PhpPreferences.getPreferences(true).node(PREFERENCES_PATH);
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
    public boolean openManager(RemoteConfiguration remoteConfiguration) {
        initPanel();
        assert panel != null;
        boolean changed = panel.open(remoteConfiguration);
        if (changed) {
            saveRemoteConnections();
        }
        return changed;
    }

    List<RemoteConnectionProvider> getConnectionProviders() {
        return Arrays.<RemoteConnectionProvider>asList(FtpConnectionProvider.get(), SftpConnectionProvider.get());
    }

    public List<String> getRemoteConnectionTypes() {
        List<String> names = new ArrayList<String>();
        for (RemoteConnectionProvider provider : getConnectionProviders()) {
            names.add(provider.getDisplayName());
        }
        return Collections.unmodifiableList(names);
    }

    /** Can be null. */
    public RemoteConfiguration getRemoteConfiguration(ConfigManager.Configuration cfg) {
        for (RemoteConnectionProvider provider : getConnectionProviders()) {
            RemoteConfiguration configuration = provider.getRemoteConfiguration(cfg);
            if (configuration != null) {
                return configuration;
            }
        }
        return null;
    }

    /** Can be null. */
    public RemoteConfigurationPanel getConfigurationPanel(ConfigManager.Configuration cfg) {
        for (RemoteConnectionProvider provider : getConnectionProviders()) {
            RemoteConfigurationPanel configurationPanel = provider.getRemoteConfigurationPanel(cfg);
            if (configurationPanel != null) {
                return configurationPanel;
            }
        }
        return null;
    }

    /** Can be null. */
    public String getConfigurationType(ConfigManager.Configuration cfg) {
        for (RemoteConnectionProvider provider : getConnectionProviders()) {
            RemoteConfigurationPanel remoteConfigurationPanel = provider.getRemoteConfigurationPanel(cfg);
            if (remoteConfigurationPanel != null) {
                return provider.getDisplayName();
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

    /** Can be null. */
    public RemoteConfiguration createRemoteConfiguration(String type, ConfigManager.Configuration configuration) {
        assert type != null;
        for (RemoteConnectionProvider provider : getConnectionProviders()) {
            if (type.equals(provider.getDisplayName())) {
                RemoteConfiguration remoteConfiguration = provider.createRemoteConfiguration(configuration);
                assert remoteConfiguration != null : "Remote configuration must be provided for " + type;
                return remoteConfiguration;
            }
        }
        return null;
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

    private void saveRemoteConnections() {
        Preferences remoteConnections = getPreferences();
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
            Preferences remoteConnections = getPreferences();
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
}
