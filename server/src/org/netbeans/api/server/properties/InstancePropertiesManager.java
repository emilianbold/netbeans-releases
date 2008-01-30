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
 * This class acts as a manager of the properties. It manages the set
 * of persisted properties grouped by the property set identifier.
 * <p>
 * Typical use case:<p>
 * <pre>
 *     // we have some instance to persist
 *     InstancePropertiesManager manager = InstancePropertiesManager.getInstance();
 *     InstanceProperties props1 = manager.createProperties("myspace");
 *     props1.put("property", "value");
 *
 *     // we want to persist yet another instance
 *     InstanceProperties props2 = manager.createProperties("myspace");
 *     props2.put("property", "value");
 *
 *     // we want to retrieve all InstanceProperties from "myspace"
 *     // the list will have two elements
 *     List&lt;InstanceProperties&gt; props = manager.getInstanceProperties("myspace");
 * </pre>
 * <p>
 * This class is <i>ThreadSafe</i>.
 *
 * @author Petr Hejl
 */
public final class InstancePropertiesManager {

    private static final Logger LOGGER = Logger.getLogger(InstancePropertiesManager.class.getName());

    private static InstancePropertiesManager manager;

    private final Random random = new Random();

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
     * Creates and returns properties in the given property set. It is
     * perfectly legal to call this method multiple times with the same property
     * set as parameter - it will always create new instance of Properties.
     * Returned properties should serve for persistence of the single server
     * instance.
     *
     * @param propertySetId string identifying the set of properties
     * @return new Properties logically placed in the given set
     */
    public InstanceProperties createProperties(String propertySetId) {
        Preferences prefs = NbPreferences.forModule(InstancePropertiesManager.class);

        try {
            prefs = prefs.node(propertySetId);

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
     * Returns all existing properties created in the given property set.
     *
     * @param propertySetId string identifying the set of properties
     * @return list of all existing properties created in the given set
     */
    public List<InstanceProperties> getProperties(String propertySetId) {
        Preferences prefs = NbPreferences.forModule(InstancePropertiesManager.class);

        try {
            prefs = prefs.node(propertySetId);
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
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                return prefs.getBoolean(key, def);
            }
        }

        @Override
        public double getDouble(String key, double def) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                return prefs.getDouble(key, def);
            }
        }

        @Override
        public float getFloat(String key, float def) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                return prefs.getFloat(key, def);
            }
        }

        @Override
        public int getInt(String key, int def) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                return prefs.getInt(key, def);
            }
        }

        @Override
        public long getLong(String key, long def) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                return prefs.getLong(key, def);
            }
        }

        @Override
        public String getString(String key, String def) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                return prefs.get(key, def);
            }
        }

        @Override
        public void putBoolean(String key, boolean value) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.putBoolean(key, value);
            }
        }

        @Override
        public void putDouble(String key, double value) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.putDouble(key, value);
            }
        }

        @Override
        public void putFloat(String key, float value) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.putFloat(key, value);
            }
        }

        @Override
        public void putInt(String key, int value) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.putInt(key, value);
            }
        }

        @Override
        public void putLong(String key, long value) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.putLong(key, value);
            }
        }

        @Override
        public void putString(String key, String value) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
                prefs.put(key, value);
            }
        }

        @Override
        public void removeKey(String key) {
            synchronized (this) {
                if (prefs == null) {
                    throw new IllegalStateException("Properties are not valid anymore");
                }
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
