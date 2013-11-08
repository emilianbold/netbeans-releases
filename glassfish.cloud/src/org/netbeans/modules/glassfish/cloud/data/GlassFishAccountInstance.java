/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012-2013 Oracle and/or its affiliates. All rights reserved.
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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.glassfish.tools.ide.data.GlassFishAdminInterface;
import org.glassfish.tools.ide.data.GlassFishServer;
import org.glassfish.tools.ide.data.GlassFishVersion;
import org.glassfish.tools.ide.data.cloud.GlassFishAccountEntity;
import org.glassfish.tools.ide.data.cloud.GlassFishCloud;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.modules.glassfish.cloud.wizards.GlassFishAccountWizardUserComponent;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import static org.openide.util.NbBundle.getMessage;

/**
 * GlassFish cloud user account instance extended to contain NetBeans related
 * attributes.
 * <p/>
 * GlassFish user account instance represents user on cloud account. Based
 * on Tooling SDK entity object.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishAccountInstance extends GlassFishAccountEntity
    implements GlassFishServer, GlassFishInstance {
    
    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(
            GlassFishCloudInstance.class.getSimpleName());

    /** Name property name. */
    public static final String PROPERTY_NAME = "name";

    /** User name property name. */
    public static final String PROPERTY_USER_NAME = "userName";

    /** User password host property name. */
    public static final String PROPERTY_USER_PASSWORD = "userPassword";

    /** Account property name. */
    public static final String PROPERTY_ACCOUNT = "account";

    /** Related GlassFish cloud entity name (selected from combo box). */
    public static final String PROPERTY_CLOUD_NAME="cloudName";

    /** GlassFish cloud user account URL prefix. */
    public static final String URL_PREFIX = GlassFishUrl.Id.CLOUD.toString();

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build key ring identifier for password related to given user name.
     * <p/>
     * @param serverName Name of server to add into password key.
     * @param userName User name of account user who's password will be stored.
     * @return Key ring identifier for password related to given user name
     */
    private static String passwordKey(String serverName, String userName) {
        StringBuilder pwKey = new StringBuilder(
                GlassFishAccountInstanceProvider
                .KEYRING_NAME_SPACE.length()
                + GlassFishAccountInstanceProvider
                .KEYRING_NAME_SEPARATOR.length()
                + PROPERTY_USER_PASSWORD.length()
                + GlassFishAccountInstanceProvider
                .KEYRING_IDENT_SEPARATOR.length()
                + (serverName != null ? serverName.length() : 0)
                + GlassFishAccountInstanceProvider
                .KEYRING_IDENT_SEPARATOR.length()
                + (userName != null ? userName.length() : 0));
        pwKey.append(GlassFishAccountInstanceProvider.KEYRING_NAME_SPACE);
        pwKey.append(GlassFishAccountInstanceProvider.KEYRING_NAME_SEPARATOR);
        pwKey.append(PROPERTY_USER_PASSWORD);
        pwKey.append(GlassFishAccountInstanceProvider.KEYRING_IDENT_SEPARATOR);
        pwKey.append(serverName != null ? serverName : "");
        pwKey.append(GlassFishAccountInstanceProvider.KEYRING_IDENT_SEPARATOR);
        pwKey.append(userName != null ? userName : "");
        return pwKey.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** The display name of GlassFish cloud server type. */
    private final String serverDisplayName;

    /** Description of GlassFish cloud user account. */
    private final String description;

    /** GlassFish Cloud GUI Node. */
    private volatile Node basicNode;

    /** Stored server instance. */
    private ServerInstance serverInstance;

    /** Support for load events listeners. */
    private final ChangeSupport loadListeners;

    /** Support for store events listeners. */
    private final ChangeSupport storeListeners;

    /** Support for remove events listeners. */
    private final ChangeSupport removeListeners;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs GlassFish user account instance with ALL values set.
     * <p/>
     * @param name GlassFish user account name to set.
     * @param account GlassFish cloud account name to set.
     * @param userName GlassFish cloud account user name to set.
     * @param userPassword GlassFish cloud account user password to set.
     * @param cloudEntity  GlassFish cloud entity reference to set.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public GlassFishAccountInstance(String name, String account, String userName,
            String userPassword, GlassFishCloud cloudEntity) {
        super(name, account, userName, userPassword,
                GlassFishUrl.url(GlassFishUrl.Id.CLOUD, name), cloudEntity);
        this.serverDisplayName = getMessage(GlassFishCloudInstance.class,
                Bundle.GLASSFISH_ACCOUNT_SERVER_TYPE, new Object[]{});
        this.description = getMessage(GlassFishCloudInstance.class,
                Bundle.GLASSFISH_ACCOUNT_DESCRIPTION, new Object[]{});
        this.serverInstance = ServerInstanceFactory.createServerInstance(this);
        this.loadListeners = new ChangeSupport(Event.LOAD);
        this.storeListeners = new ChangeSupport(Event.STORE);
        this.removeListeners = new ChangeSupport(Event.REMOVE);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters and Setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get <code>ServerInstance</code> object related to this cloud instance.
     * <p/>
     * @return <code>ServerInstance</code> object related to this
     *         cloud instance.
     */
    public ServerInstance getServerInstance() {
        return serverInstance;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented missing GlassFishServer interface methods                  //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish cloud host.
     * <p/>
     * @return Cloud host.
     */
    @Override
    public String getHost() {
        return cloudEntity != null ? cloudEntity.getHost() : null;
    }

    /**
     * Get GlassFish cloud port.
     * <p/>
     * @return Cloud port.
     */
    @Override
    public int getPort() {
        return -1;
    }

   /**
     * Get GlassFish cloud CPAS port.
     * <p/>
     * @return CPAS port.
     */
    @Override
    public int getAdminPort() {
        return cloudEntity != null ? cloudEntity.getPort() : -1;
    }

    /**
     * Get GlassFish cloud account user name.
     * <p/>
     * @return Cloud account user name.
     */
    @Override
    public String getAdminUser() {
        return userName;
    }

    /**
     * Get GlassFish cloud account user password.
     * <p/>
     * @return Cloud account user password.
     */
    @Override
    public String getAdminPassword() {
        return userPassword;
    }

    /**
     * Get GlassFish cloud domains folder.
     * <p/>
     * @return Always returns <code>null</code> because cloud server is remote.
     */
    @Override
    public String getDomainsFolder() {
        return null;
    }

    /**
     * Get GlassFish cloud domain name.
     * <p/>
     * @return Always returns <code>null</code> because cloud server is remote.
     */
    @Override
    public String getDomainName() {
        return null;
    }

    /**
     * Get GlassFish cloud installation root.
     * <p/>
     * @return Always returns <code>null</code> because cloud server is remote.
     */
    @Override
    public String getServerRoot() {
        return null;
    }

    /**
     * Get GlassFish cloud server home.
     * <p/>
     * @return Always returns <code>null</code> because cloud server is remote.
     */
    @Override
    public String getServerHome() {
        return null;
    }

   /** Get GlassFish cloud version.
     * <p/>
     * Attempts to retrieve cloud version using CPAS interface.
     * <p/>
     * @return Cloud version or <code>null</code> if version could not
     *         be retrieved.
     */
    @Override
    public GlassFishVersion getVersion() {
        // TODO: implement version check.
        return null;
    }

    /**
     * Get GlassFish cloud administration interface type.
     * <p/>
     * @return GlassFish cloud administration interface is REST.
     */
    @Override
    public GlassFishAdminInterface getAdminInterface() {
        return GlassFishAdminInterface.REST;
    }

    /**
     * Is this server local or remote?
     * <p/>
     * @return Always returns <code>true</code> because cloud server is remote..
     */
    @Override
    public boolean isRemote() {
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented ServerInstanceImplementation interface methods             //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get GlassFish cloud name (display name in IDE).
     * <p/>
     * @return GlassFish cloud name (display name in IDE).
     */
    @Override
    public String getDisplayName() {
        return this.name;
    }

    /**
     * Get description of GlassFish cloud user account.
     * <p/>
     * @return Description of GlassFish cloud user account.
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Get the display name of GlassFish cloud server type
     * <p/>
     * @return The display name of GlassFish cloud server type.
     */
    @Override
    public String getServerDisplayName() {
        return serverDisplayName;
    }

    /**
     * Returns node representing runtime instance.
     * <p/>
     * Node should display instance status and provide actions to manage
     * the server.
     * <p/>
     * @return Node representing instance, may return <code>null</code>.
     */
    @Override
    public Node getFullNode() {
        return getBasicNode();
    }

    /**
     * Returns node representing instance while configuring it.
     * <p/>
     * Node should not display any status, actions or children.
     * <p/>
     * @return Node representing instance, may return <code>null</code>.
     */
    @Override
    public Node getBasicNode() {
        if (basicNode != null) {
            return basicNode;
        }
        synchronized(this) {
            if (basicNode == null) {
                basicNode = new GlassFishAccountInstanceNode(this);
            }
        }
        return basicNode;
    }

    /**
     * Returns component allowing customization of instance.
     * <p/>
     * May return <code>null</code>. Always called from Event Dispatch Thread.
     * <p/>
     * @return Component allowing customization of instance, may return
     *         <code>null</code>
     */
    @Override
    public JComponent getCustomizer() {
        return new GlassFishAccountWizardUserComponent(this);
    }

    /**
     * Removes instance from provider(s).
     * <p/>
     * No {@link ServerInstanceProvider} should return this instance once
     * it is removed.
     */
    @Override
    public void remove() {
        GlassFishAccountInstanceProvider.removeAccountInstance(this);
    }

    /**
     * An information that instance can be removed by {@link #remove()} method.
     * <p/>
     * @return Always returns <code>true</code>.
     */
    @Override
    public boolean isRemovable() {
        return true;
    }
    
    /**
     * Get GlassFish local server registered with cloud.
     * <p/>
     * @return GlassFish cloud local server.
     */
    @Override
    public GlassFishServer getLocalServer() {
        GlassFishCloud cloud = getCloudEntity();
        return cloud != null
                ? cloud.getLocalServer()
                : null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Listeners handling methods                                             //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Check for <code>Event</code> occurrence in <code>Event</code>s array.
     * <p/>
     * @param events Events to be checked.
     * @return Array of <code>Event.length</code> size where values on indexes
     *         of <code>Event</code> ordinal values passed to this method are
     *         set to <code>true</code> and rest of the array values set
     *         to <code>false</code>.
     */
    private boolean[] selectEventsToModify(Event[] events) {
        boolean addEvent[] = new boolean[Event.length];
        for (int i = 0; i < Event.length; i++) {
            addEvent[i] = false;
        }
        if (events != null) {
            for (int i = 0; i < events.length; i++) {
                addEvent[events[i].ordinal()] = true;
            }
        }
        return addEvent;
    }

    /**
     * Add a listener to changes of the panel validity.
     * <p/>
     * @param listener Listener to add.
     * @param events   Which events to listen for.
     * @see #isValid
     */
    @Override
    public void addChangeListener(ChangeListener listener, Event[] events) {
        boolean addEvent[] = selectEventsToModify(events);
        if (addEvent[Event.LOAD.ordinal()]) {
            loadListeners.addChangeListener(listener);
        }
        if (addEvent[Event.STORE.ordinal()]) {
            storeListeners.addChangeListener(listener);
        }
        if (addEvent[Event.REMOVE.ordinal()]) {
            removeListeners.addChangeListener(listener);
        }
    }

    /**
     * Remove a listener to changes of the panel validity.
     * <p/>
     * @param listener Listener to remove
     * @param events   Events from which specified listener will be removed.
     */
    @Override
    public void removeChangeListener(ChangeListener listener, Event[] events) {
        boolean addEvent[] = selectEventsToModify(events);
        if (addEvent[Event.LOAD.ordinal()]) {
            loadListeners.removeChangeListener(listener);
        }
        if (addEvent[Event.STORE.ordinal()]) {
            storeListeners.removeChangeListener(listener);
        }
        if (addEvent[Event.REMOVE.ordinal()]) {
            removeListeners.removeChangeListener(listener);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Persistency methods                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Store content of GlassFish Cloud instance object into given properties.
     * <p/>
     * @param props Set of properties to persist.
     */
    void store(InstanceProperties props) {
        props.putString(PROPERTY_NAME, name);
        props.putString(PROPERTY_ACCOUNT, account);
        props.putString(PROPERTY_USER_NAME, userName);
        props.putString(PROPERTY_CLOUD_NAME,
                cloudEntity != null ? cloudEntity.getName() : null);
        Keyring.save(passwordKey(name, userName), userPassword.toCharArray(),
                "GlassFish cloud account user password");
        LOG.log(Level.FINER,
                "Stored GlassFishCloudInstance({0}, {1}, {2}, <password>, {4})",
                new Object[]{name, account, userName, cloudEntity != null
                    ? cloudEntity.getName() : "null"});
        storeListeners.fireChange();
    }

    /**
     * Load content of GlassFish Cloud instance object from given properties.
     * <p/>
     * @param props Set of properties to convert into GlassFish Cloud instance.
     * @return Newly created instance of <code>GlassFishCloudInstance</code>
     *         reconstructed from properties.
     */
    static GlassFishAccountInstance load(InstanceProperties props) {
        String name = props.getString(PROPERTY_NAME, null);
        if (name != null) {
            String account = props.getString(PROPERTY_ACCOUNT, null);
            String userName = props.getString(PROPERTY_USER_NAME, null);
            String cloudName = props.getString(PROPERTY_CLOUD_NAME, null);
            String userPassword;
            if (userName != null) {
                char[] password = Keyring.read(passwordKey(name, userName));
                userPassword = password != null ? new String(password) : null;
            } else {
                userPassword = null;
            }
            GlassFishCloud cloudEntity = cloudName != null
                    ? GlassFishCloudInstanceProvider.getCloudInstance(cloudName)
                    : null;
            LOG.log(Level.FINER,
                    "Loaded GlassFishCloudInstance({0}, {1}, {2}, <password>, {4})",
                    new Object[]{name, account, userName, cloudEntity != null
                    ? cloudEntity.getName() : "null"});
            GlassFishAccountInstance instance 
                    = new GlassFishAccountInstance(name, account, userName,
                    userPassword, cloudEntity);
            instance.loadListeners.fireChange();
            return instance;
        } else {
            LOG.log(Level.WARNING,
                    "Stored GlassFishCloudInstance name is null, skipping");
            return null;
        }
    }

    /**
     * Remove content of GlassFish Cloud instance object from given properties.
     * <P/>
     * @param props Set of properties to remove.
     */
    void remove(InstanceProperties props) {
        props.remove();
        removeListeners.fireChange();
    }

    /**
     * Compare if given properties represents this user account instance.
     * <p/>
     * @param props Set of properties to compare with this
     *        GlassFish user account instance.
     * @return Returns <code>true</code> when both name stored in properties
     *         and this instance are equal <code>String</codse> objects
     *         or both are <code>null</code>. Otherwise <code>false</code>
     *         is returned.
     */
    boolean equalProps(InstanceProperties props) {
        String propsName = props.getString(PROPERTY_NAME, null);
        return propsName != null
                ? propsName.equals(name)
                : name == null;
    }

}
