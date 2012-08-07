/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.data;

import java.util.*;
import org.glassfish.tools.ide.data.cloud.GlassFishCloudEntity;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.api.server.properties.InstancePropertiesManager;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;

/**
 * GlassFish Cloud Instances Provider.
 * <p>
 * Handles all registered glassFish cloud instances. Implemented as singleton
 * because NetBeans GUI components require singleton implementing
 * <code>ServerInstanceProvider</code> interface.
 * <p/>
 * Usage inside module is done trough static methods to avoid
 * <code>getInstance</code> method calls every time this provider is being
 * accessed.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishCloudInstanceProvider
    extends GlassFishInstanceProvider {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish cloud instance properties name space. */
    private static final String PROPERTIES_NAME_SPACE="GlassFish.cloud.cpas";

    /** GlassFish cloud instance key ring name space. */
    private static final String KEYRING_NAME_SPACE="GlassFish.cloud.cpas";

    /** Singleton object instance. */
    private static volatile GlassFishCloudInstanceProvider instance;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods used as provider interface                              //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Return existing singleton instance of this class or create a new one
     * when no instance exists.
     * <p>
     * @return <code>AdminFactoryHttp</code> singleton instance.
     */
    public static GlassFishCloudInstanceProvider getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (GlassFishCloudInstanceProvider.class) {
            if (instance == null) {
                instance = new GlassFishCloudInstanceProvider();
            }
        }
        return instance;
    }

    /**
     * Check if stored cloud instances contains instance with given display
     * name.
     * <p/>
     * Display name (also cloud entity name) is used as unique key in cloud
     * instances <code>Map</code>.
     * <p/>
     * @param name Display name to search for.
     */
    public static boolean containsCloudInstanceWithName(String name) {
        return getInstance().cloudInstances.containsKey(name);
    }

    /**
     * Add new GlassFish cloud instance into this provider.
     * <p/>
     * @param instance GlassFish cloud instance to be added.
     */
    public static void addCloudInstance(GlassFishCloudInstance instance) {
        getInstance().addInstance(instance);
    }

    /**
     * Remove GlassFish cloud instance from this provider.
     * <p/>
     * @param instance GlassFish cloud instance to be removed.
     */
    public static void removeCloudInstance(GlassFishCloudInstance instance) {
        getInstance().removeInstance(instance);
    }

    /**
     * Create copy of stored GlassFish cloud instances.
     * <p/>
     * Changing content of returned list will not change content
     * of this provider.
     * <p/>
     * @return Cloned <code>List</code> of cloned
     *         <code>GlassFishCloudEntity</code> objects.
     */
    @SuppressWarnings(value={"rawtypes", "unchecked"})
    public static List<GlassFishCloudEntity> cloneCloudInstances() {
        return getInstance().cloneInstances();
    }

    /**
     * Get GlassFish cloud instance from this provider.
     * <p/>
     * @param name GlassFish cloud instance display name used as key.
     */
    public static GlassFishCloudInstance getCloudInstance(String name) {
        return getInstance().cloudInstances.get(name);
    }

    /**
     * Update properties to persist using given GlassFish cloud instance.
     * <p/>
     * @param instance GlassFish cloud instance used to persist.
     */
    public static void persist(GlassFishCloudInstance instance) {
        getInstance().update(instance);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Stored GlassFish cloud instances. */
    private Map<String, GlassFishCloudInstance> cloudInstances;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    private GlassFishCloudInstanceProvider() {
        super();
        cloudInstances = new HashMap<String, GlassFishCloudInstance>();
        load();
    }
   
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create copy of stored GlassFish cloud instances.
     * <p/>
     * Changing content of returned list will not change content
     * of this provider.
     * <p/>
     * @return Cloned <code>List</code> of cloned
     *         <code>GlassFishCloudEntity</code> objects.
     */
    @SuppressWarnings(value={"rawtypes", "unchecked"})
    private List<GlassFishCloudEntity> cloneInstances() {
        List<GlassFishCloudEntity> clonedCloudInstances
                = new ArrayList(cloudInstances.size());
        for (Iterator<String> i =  cloudInstances.keySet().iterator();
                i.hasNext(); ) {
            String key = i.next();
            GlassFishCloudInstance value = cloudInstances.get(key);
            GlassFishCloudEntity copyOfValue
                    = new GlassFishCloudEntity(value.getName(), value.getHost(),
                    value.getPort(), value.getLocalServer());
            clonedCloudInstances.add(copyOfValue);
        }
        return clonedCloudInstances;
    }

    /**
     * Add new GlassFish cloud instance into this provider.
     * <p/>
     * Instance is registered in this provider without being stored into
     * persistence properties.
     * <p/>
     * @param instance GlassFish cloud instance to be added.
     */
    private void addInstanceWithoutStoring(GlassFishCloudInstance instance) {
        synchronized (this) {
            serverInstances.add(instance.getServerInstance());
            cloudInstances.put(instance.getDisplayName(), instance);
        }
        if (instance.getLocalServer() != null
                && instance.getLocalServer().getUrl() != null) {
            try {
                org.netbeans.modules.j2ee.deployment.plugins.api
                        .InstanceProperties
                        .createInstancePropertiesNonPersistent(
                        instance.getLocalServer().getUrl(), null, null,
                        instance.getDisplayName(), null);
            } catch (InstanceCreationException ice) {
            }
        }
        changeListeners.fireChange();
    }

    /**
     * Add new GlassFish cloud instance into this provider.
     * <p/>
     * Instance is registered in this provider and stored into persistence
     * properties.
     * <p/>
     * @param instance GlassFish cloud instance to be added.
     */
    private void addInstance(GlassFishCloudInstance instance) {
        store(instance);
        addInstanceWithoutStoring(instance);
    }

    /**
     * Remove GlassFish cloud instance from this provider.
     * <p/>
     * @param instance GlassFish cloud instance to be removed.
     */
    private void removeInstance(GlassFishCloudInstance instance) {
        remove(instance);
        synchronized (this) {
            serverInstances.remove(instance.getServerInstance());
            cloudInstances.remove(instance.getDisplayName());
        }
        if (instance.getLocalServer() != null
                && instance.getLocalServer().getUrl() != null) {
                org.netbeans.modules.j2ee.deployment.plugins.api
                        .InstanceProperties
                        .removeInstance(instance.getLocalServer().getUrl());
        }
        changeListeners.fireChange();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Persistency methods                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Store given GlassFish cloud instance into properties to persist.
     * <p/>
     * @param instance GlassFish cloud instance to be stored.
     */
    private void store(GlassFishCloudInstance instance) {
        InstanceProperties props;
        synchronized (this) {
            props = InstancePropertiesManager.getInstance().createProperties(
                    PROPERTIES_NAME_SPACE);
        }
        instance.store(props);
    }

    /**
     * Load all stored GlassFish cloud instances into this provider from
     * persistent properties.
     */
    private void load() {
        for (InstanceProperties props
                : InstancePropertiesManager.getInstance()
                .getProperties(PROPERTIES_NAME_SPACE)) {
            GlassFishCloudInstance cloudInstance
                    = GlassFishCloudInstance.load(props);
            if (cloudInstance != null) {
                addInstanceWithoutStoring(cloudInstance);
            }
        }
    }

    /**
     * Remove given GlassFish cloud instance from properties to persist.
     * <p/>
     * @param instance GlassFish cloud instance to be removed.
     */
    private void remove(GlassFishCloudInstance instance) {
        List<InstanceProperties> propsList = InstancePropertiesManager
                .getInstance().getProperties(PROPERTIES_NAME_SPACE);
        synchronized (this) {
            for (Iterator<InstanceProperties> i = propsList.iterator();
                    i.hasNext(); ) {
                InstanceProperties props = i.next();
                if (instance.equalProps(props)) {
                    props.remove();
                }
            }
        }
    }

    /**
     * Update properties to persist using given GlassFish cloud instance.
     * <p/>
     * @param instance GlassFish cloud instance used to update properties.
     */
    private void update(GlassFishCloudInstance instance) {
        List<InstanceProperties> propsList = InstancePropertiesManager
                .getInstance().getProperties(PROPERTIES_NAME_SPACE);
        synchronized (this) {
            for (Iterator<InstanceProperties> i = propsList.iterator();
                    i.hasNext(); ) {
                InstanceProperties props = i.next();
                if (instance.equalProps(props)) {
                    instance.store(props);
                }
            }
        }
    }
    
}
