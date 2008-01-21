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

package org.netbeans.api.server.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Petr Hejl
 */
public final class InstancePropertiesManager {

    private static final Logger LOGGER = Logger.getLogger(InstancePropertiesManager.class.getName());

    private static InstancePropertiesManager manager;

    private Random random = new Random();

    private InstancePropertiesManager() {
        super();
    }

    /**
     * Returns the instance of the default manager.
     *
     * @return the instance of the default manager
     */
    public static synchronized InstancePropertiesManager getInstance() {
        if (manager == null) {
            manager = new InstancePropertiesManager();
        }

        return manager;
    }

    /**
     * Creates and returns properties for the given server plugin. It is
     * perfectly legal to call this method multiple times with the same plugin - it
     * will always create new instance of Properties. Returned properties
     * should serve for persistence of the single server instance.
     *
     * @param plugin string identifying plugin
     * @return new Properties for the given plugin
     */
    public InstanceProperties createProperties(String plugin) {
        Preferences prefs = NbPreferences.forModule(InstancePropertiesManager.class);

        try {
            prefs = prefs.node(plugin);

            boolean next = true;
            String id = null;
            synchronized (this) {
                while (next) {
                    id = Integer.toString(random.nextInt(Integer.MAX_VALUE));
                    next = prefs.nodeExists(id);
                }
                prefs = prefs.node(id);
                prefs.flush();
            }
            return new DefaultInstanceProperties(id, prefs);
        } catch (BackingStoreException ex) {
            LOGGER.log(Level.INFO, null, ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Returns all existing properties created by the given plugin.
     *
     * @param plugin string identifying plugin
     * @return list of all existing properties created by the given plugin
     */
    public List<InstanceProperties> getProperties(String plugin) {
        Preferences prefs = NbPreferences.forModule(InstancePropertiesManager.class);

        try {
            prefs = prefs.node(plugin);
            prefs.flush();

            List<InstanceProperties> props = new ArrayList<InstanceProperties>();
            synchronized (this) {
                for (String id : prefs.childrenNames()) {
                    props.add(new DefaultInstanceProperties(id, prefs.node(id)));
                }
            }
            return props;
        } catch (BackingStoreException ex) {
            LOGGER.log(Level.INFO, null, ex);
            throw new IllegalStateException(ex);
        }
    }

    private static class DefaultInstanceProperties extends InstanceProperties {

        /** <i>GuardedBy("this")</i> */
        private Preferences prefs;

        public DefaultInstanceProperties(String id, Preferences prefs) {
            super(id);
            this.prefs = prefs;
        }

        @Override
        public boolean getBoolean(String key, boolean def) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                return prefs.getBoolean(key, def);
            }
        }

        @Override
        public double getDouble(String key, double def) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                return prefs.getDouble(key, def);
            }
        }

        @Override
        public float getFloat(String key, float def) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                return prefs.getFloat(key, def);
            }
        }

        @Override
        public int getInt(String key, int def) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                return prefs.getInt(key, def);
            }
        }

        @Override
        public long getLong(String key, long def) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                return prefs.getLong(key, def);
            }
        }

        @Override
        public String getString(String key, String def) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                return prefs.get(key, def);
            }
        }

        @Override
        public void putBoolean(String key, boolean value) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                prefs.putBoolean(key, value);
            }
        }

        @Override
        public void putDouble(String key, double value) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                prefs.putDouble(key, value);
            }
        }

        @Override
        public void putFloat(String key, float value) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                prefs.putFloat(key, value);
            }
        }

        @Override
        public void putInt(String key, int value) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                prefs.putInt(key, value);
            }
        }

        @Override
        public void putLong(String key, long value) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                prefs.putLong(key, value);
            }
        }

        @Override
        public void putString(String key, String value) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                prefs.put(key, value);
            }
        }

        @Override
        public void removeKey(String key) {
            if (prefs == null) {
                throw new IllegalStateException("Poperties are not valid anymore");
            }
            synchronized (this) {
                prefs.remove(key);
            }
        }

        @Override
        public void remove() {
            try {
                synchronized (this) {
                    if (prefs != null) {
                        prefs.removeNode();
                        prefs = null;
                    }
                }
            } catch (BackingStoreException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }

}
