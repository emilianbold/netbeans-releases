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
import javax.swing.event.ChangeListener;
import org.glassfish.tools.ide.data.cloud.GlassFishCloud;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.api.server.properties.InstancePropertiesManager;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.util.ChangeSupport;

/**
 * GlassFish User Account Instances Provider.
 * <p>
 * Handles all registered glassFish user acocunt instances. Implemented
 * as singleton because NetBeans GUI components require singleton implementing
 * <code>ServerInstanceProvider</code> interface.
 * <p/>
 * Usage inside module is done trough static methods to avoid
 * <code>getInstance</code> method calls every time this provider is being
 * accessed.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishAccountInstanceProvider
    implements ServerInstanceProvider {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish user account instance properties name space. */
    static final String PROPERTIES_NAME_SPACE="GlassFish.cloud.userAccount";

    /** GlassFish user account instance key ring name space. */
    static final String KEYRING_NAME_SPACE="GlassFish.cloud.userAccount";

    /**
     * GlassFish user account instance key ring field separator.
     * <p/>
     * Key ring name is constructed in following form:
     * <field>{'.'<field>}':'<identifier>
     * e.g. "GlassFish.cloud.userAccount.userPassword:someUser".
     */
    static final String KEYRING_NAME_SEPARATOR=".";

    /**
     * GlassFish user account instance key ring identifier separator.
     * <p/>
     * Key ring name is constructed in following form:
     * <field>{'.'<field>}':'<identifier>
     * e.g. "GlassFish.cloud.userAccount.userPassword:someUser".
     */
    static final String KEYRING_IDENT_SEPARATOR=":";

    /** Singleton object instance. */
    private static volatile GlassFishAccountInstanceProvider instance;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Return existing singleton instance of this class or create a new one
     * when no instance exists.
     * <p>
     * @return <code>AdminFactoryHttp</code> singleton instance.
     */
    public static GlassFishAccountInstanceProvider getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (GlassFishAccountInstanceProvider.class) {
            if (instance == null) {
                instance = new GlassFishAccountInstanceProvider();
            }
        }
        return instance;
    }

    /**
     * Add new GlassFish user account instance into this provider.
     * <p/>
     * @param instance GlassFish user account instance to be added.
     */
    public static void addAccountInstance(GlassFishAccountInstance instance) {
        getInstance().addInstance(instance);
    }

    /**
     * Remove GlassFish user account instance from this provider.
     * <p/>
     * @param instance GlassFish user account instance to be removed.
     */
    public static void removeAccountInstance(
            GlassFishAccountInstance instance) {
        getInstance().removeInstance(instance);
    }

    /**
     * Get GlassFish user account instance from this provider.
     * <p/>
     * @param name GlassFish user account instance display name used as key.
     */
    public static GlassFishAccountInstance getAccountInstance(String name) {
        return getInstance().getAccountInstances().get(name);
    }

    /**
     * Update properties to persist using given GlassFish user account Instance.
     * <p/>
     * @param instance GlassFish user account Instance used to persist.
     */
    public static void persist(GlassFishAccountInstance instance) {
        getInstance().update(instance);
    }

    /**
     * Check if any registered GlassFish user account instance contains
     * reference to given GlassFish cloud instance.
     * <p/>
     * @param instance GlassFish cloud instance to search for.
     * @return <code>true if GlassFish cloud instance was found
     *         or <code>false</code> otherwise.
     */
    public static boolean containsCloudInstance(
            GlassFishCloudInstance cloudInstance) {
        return getInstance().containsCloud(cloudInstance);
        
    }
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////


    /** Stored NEtBeans server instances. */
    private List<ServerInstance> serverInstances;

    /** Stored GlassFish user account instances. */
    private Map<String, GlassFishAccountInstance> accountInstances;

    /** Change listeners. */
    private ChangeSupport changeListeners;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    private GlassFishAccountInstanceProvider() {
        changeListeners = new ChangeSupport(this);
        serverInstances = new LinkedList<ServerInstance>();
        accountInstances = new HashMap<String, GlassFishAccountInstance>();
        load();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Interface Methods                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns list of known user account instances.
     * <p/>
     * Will return copy of internal <code>ServerInstance<code>
     * <code>List</code>. Any changes made to returned <code>List</code>
     * will not affect content of this provider.
     * <p/>
     * @return <code>List</code> of known user account instances.
     */
    @Override
    public synchronized List<ServerInstance> getInstances() {
        return new ArrayList<ServerInstance>(serverInstances);
    }

    /**
     * Adds a change listener to this provider.
     * <p/>
     * The listener must be notified any time instance is added or removed.
     * <p/>
     * @param listener Change listener to add, <code>null</code> is allowed 
     *                 (but it si no op then).
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        changeListeners.addChangeListener(listener);
    }

    /**
     * Removes the previously added listener.
     * <p/>
     * No more events will be fired on the removed listener.
     * <p/>
     * @param listener Listener to remove, <code>null</code> is allowed
     *                 (but it si no op then).
     */
    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeListeners.removeChangeListener(listener);
    }
   
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get stored GlassFish user account instances.
     * <p/>
     * Changing content of this <code>Map</code> without doing equivalent
     * changes in <code>List</code> of <code>ServerInstance</code>s returned
     * by <code>getInstances</code> method will corrupt this provider.
     * <p/>
     * @return Stored GlassFish user account instances
     */
    private Map<String, GlassFishAccountInstance> getAccountInstances() {
        return accountInstances;
    } 

    /**
     * Add new GlassFish user account instance into this provider.
     * <p/>
     * Instance is registered in this provider without being stored into
     * persistence properties.
     * <p/>
     * @param instance GlassFish user account instance to be added.
     */
    private void addInstanceWithoutStoring(GlassFishAccountInstance instance) {
        synchronized (this) {
            serverInstances.add(instance.getServerInstance());
            accountInstances.put(instance.getDisplayName(), instance);
        }
        changeListeners.fireChange();
    }

    /**
     * Add new GlassFish user account instance into this provider.
     * <p/>
     * Instance is registered in this provider and stored into persistence
     * properties.
     * <p/>
     * @param instance GlassFish user account instance to be added.
     */
    private void addInstance(GlassFishAccountInstance instance) {
        store(instance);
        addInstanceWithoutStoring(instance);
    }

    /**
     * Remove GlassFish user account instance from this provider.
     * <p/>
     * @param instance GlassFish user account instance to be removed.
     */
    private void removeInstance(GlassFishAccountInstance instance) {
        remove(instance);
        synchronized (this) {
            serverInstances.remove(instance.getServerInstance());
            accountInstances.remove(instance.getDisplayName());
        }
        changeListeners.fireChange();
    }

    /**
     * Check if any registered GlassFish user account instance contains
     * reference to given GlassFish cloud instance.
     * <p/>
     * @param instance GlassFish cloud instance to search for.
     * @return <code>true if GlassFish cloud instance was found
     *         or <code>false</code> otherwise.
     */
    private boolean containsCloud(
            GlassFishCloudInstance cloudInstance) {
        boolean found = false;
        Collection<GlassFishAccountInstance> instances
                = accountInstances.values();
        for (GlassFishAccountInstance accountInstance : instances) {
            GlassFishCloud cloudEntity = accountInstance.getCloudEntity();
            if (cloudEntity != null && cloudEntity.equals(cloudInstance)) {
                found = true;
            }
        }
        return found;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Persistency methods                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Store given GlassFish user account instance into properties to persist.
     * <p/>
     * @param instance GlassFish Cloud Instance to be stored.
     */
    private void store(GlassFishAccountInstance instance) {
        InstanceProperties props = InstancePropertiesManager.getInstance()
                .createProperties(
                PROPERTIES_NAME_SPACE);
        instance.store(props);
    }

    /**
     * Load all stored GlassFish user account instances into this provider from
     * persistent properties.
     */
    private void load() {
        for(InstanceProperties props
                : InstancePropertiesManager.getInstance()
                .getProperties(
                PROPERTIES_NAME_SPACE)) {
            GlassFishAccountInstance cloudInstance
                    = GlassFishAccountInstance.load(props);
            if (cloudInstance != null) {
                addInstanceWithoutStoring(cloudInstance);
            }
        }
    }
    
    /**
     * Remove given GlassFish user account instance from properties to persist.
     * <p/>
     * @param instance GlassFish Cloud Instance to be removed.
     */
    private void remove(GlassFishAccountInstance instance) {
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
     * Update properties to persist using given GlassFish user account instance.
     * <p/>
     * @param instance GlassFish user account instance used to update
     *                 properties.
     */
    private void update(GlassFishAccountInstance instance) {
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
