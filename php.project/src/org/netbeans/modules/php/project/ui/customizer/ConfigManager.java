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
package org.netbeans.modules.php.project.ui.customizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.PhpConfigurationProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
public class ConfigManager {
    private static final String PROP_DISPLAY_NAME = "$label";//NOI18N    
    private final Map<String/*|null*/, Map<String, String/*|null*/>/*|null*/> configs;
    
    private final PhpProjectProperties properties;
    private final String[] propertyNames;
    private final ChangeSupport changeSupport;

    public ConfigManager(PhpProjectProperties properties) {
        this.changeSupport = new ChangeSupport(this);
        this.properties = properties;
        this.configs = properties.getRunConfigs();
        ArrayList<String> tmp = new ArrayList<String>(Arrays.asList(PhpProjectProperties.CFG_PROPS));
        tmp.add(PROP_DISPLAY_NAME);
        this.propertyNames = tmp.toArray(new String[tmp.size()]);
    }
    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }    
    
    
    public synchronized boolean exists(String name) {
        return configs.keySet().contains(name) && configs.get(name) != null;
    }

    public synchronized Configuration createNew(String name, String displayName) {
        assert !exists(name);
        configs.put(name, new HashMap<String, String>());
        Configuration retval  = new Configuration(name);
        if (!name.equals(displayName)) {
            retval.putValue( PROP_DISPLAY_NAME,displayName);
        }
        markAsCurrentConfiguration(name);
        return retval;
    }

    public synchronized Collection<String> configurationNames() {
        return configs.keySet();
    }

    public synchronized Configuration currentConfiguration() {
        return new Configuration(properties.activeConfig);
    }

    public Configuration defaultConfiguration() {
        return new Configuration();
    }

    public synchronized Configuration configurationFor(String name) {
        return new Configuration(name);
    }

    public synchronized void markAsCurrentConfiguration(String currentConfig) {
        assert configs.keySet().contains(currentConfig);
        properties.activeConfig = currentConfig;
        changeSupport.fireChange();
    }

    private String[] getPropertyNames() {
        return propertyNames;
    }

    private Map<String, String/*|null*/> getProperties() {
        return getProperties(null);
    }

    private Map<String, String/*|null*/> getProperties(String config) {
        return configs.get(config);
    }

    public class Configuration {
        private String name;
        private boolean def;

        private Configuration() {
            this(null);
        }

        private Configuration(String name) {
            this(name, name == null);
        }

        private Configuration(String name, boolean def) {
            if (name != null && name.trim().length() == 0) {
                name = null;
            }
            assert configs.keySet().contains(name) : name;
            this.name = name;
            this.def = def;
        }

        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            String retval = getValue(PROP_DISPLAY_NAME);
            retval = retval != null ? retval : getName();
            return retval != null ? retval : NbBundle.getMessage(PhpConfigurationProvider.class, 
                    "PhpConfigurationProvider.default.label");//NOI18N
        }
        
        public boolean isDefault() {
            return def;
        }
        
        public void delete() {
            synchronized(ConfigManager.this) {            
                configs.put(getName(), null);                
                //configs.remove(getName());
                markAsCurrentConfiguration(null);
            }
        }

        private boolean isDeleted() {
            return configs.get(getName()) == null;
        }

        public String getValue(String propertyName) {
            assert Arrays.asList(getPropertyNames()).contains(propertyName) : propertyName;
            //assert !isDeleted();
            synchronized (ConfigManager.this) {
                return !isDeleted() ?  getProperties(getName()).get(propertyName) : null;
            }
        }

        public void putValue(String propertyName, String value) {
            assert Arrays.asList(getPropertyNames()).contains(propertyName);
            assert !isDeleted();
            synchronized (ConfigManager.this) {
                getProperties(getName()).put(propertyName, value);
            }
        }

        public String[] getPropertyNames() {
            synchronized (ConfigManager.this) {
                return ConfigManager.this.getPropertyNames();
            }
        }
    }
}
