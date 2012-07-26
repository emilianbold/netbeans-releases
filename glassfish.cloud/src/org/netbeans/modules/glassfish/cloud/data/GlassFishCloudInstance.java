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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.glassfish.tools.ide.data.DataException;
import org.glassfish.tools.ide.data.GlassFishServer;
import org.glassfish.tools.ide.data.GlassFishServerEntity;
import org.glassfish.tools.ide.data.cloud.GlassFishCloudEntity;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.modules.glassfish.cloud.wizards.GlassFishCloudWizardCpasComponent;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;
import static org.openide.util.NbBundle.getMessage;

/**
 * GlassFish cloud instance extended to contain NetBeans related attributes.
 * <p/>
 * GlassFish cloud instance represents CPAS interface. Based on Tooling SDK
 * entity object.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishCloudInstance extends GlassFishCloudEntity
        implements ServerInstanceImplementation {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(
            GlassFishCloudInstance.class.getSimpleName());

    /** Name property name. */
    public static final String PROPERTY_NAME = "name";

    /** Host property name. */
    public static final String PROPERTY_HOST = "host";

    /** Port property name. */
    public static final String PROPERTY_PORT = "port";

    /** Local server directory property name. */
    public static final String PROPERTY_LOCAL_SERVER = "localServer";

    /** GlassFish cloud instance local URL_PREFIXserver URL prefix. */
    public static final String URL_PREFIX = "gfcl";

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** The display name of GlassFish cloud server type. */
    private final String serverDisplayName;

    /** GlassFish Cloud GUI Node. */
    private volatile Node basicNode;

    /** Stored server instance. */
    private ServerInstance serverInstance;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs GlassFish Cloud class instance with ALL values set.
     * <p/>
     * @param name        GlassFish cloud name to set.
     * @param host        GlassFish cloud host to set.
     * @param port        GlassFish server port to set.
     * @param localServer GlassFish cloud local server to set.
     * 
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public GlassFishCloudInstance(String name, String host, int port,
            GlassFishServer localServer) {
        super(name, host, port, localServer);
        this.serverDisplayName = getMessage(GlassFishCloudInstance.class,
                Bundle.GLASSFISH_CLOUD_SERVER_TYPE, new Object[]{});
        this.serverInstance = ServerInstanceFactory.createServerInstance(this);
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
    // Implemented ServerInstanceImplementation Interface Methods             //
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
                basicNode = new GlassFishCloudInstanceNode(this);
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
        return new GlassFishCloudWizardCpasComponent(this);
    }

    /**
     * Removes instance from provider(s).
     * <p/>
     * No {@link GlassFishCloudInstanceProvider} should return this instance
     * once it is removed.
     */
    @Override
    public void remove() {
        GlassFishCloudInstanceProvider.removeCloudInstance(this);
    }

    /**
     * An information that instance can be removed by {@link #remove()} method.
     * <p/>
     * @return <code>true</code> when there are no user account instances
     *         referencing this cloud instance.
     */
    @Override
    public boolean isRemovable() {
        return !GlassFishAccountInstanceProvider
                .containsCloudInstance(this);
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
        props.putString(PROPERTY_HOST, host);
        props.putInt(PROPERTY_PORT, port);
        props.putString(PROPERTY_LOCAL_SERVER,
                getLocalServer() != null
                ? getLocalServer().getServerHome() : null);
        LOG.log(Level.FINER,
                "Stored GlassFishCloudInstance({0}, {1}, {2})",
                new Object[]{name, host, port});

    }

    /**
     * Load content of GlassFish Cloud instance object from given properties.
     * <p/>
     * @param props Set of properties to convert into GlassFish Cloud instance.
     * @return Newly created instance of <code>GlassFishCloudInstance</code>
     *         reconstructed from properties.
     */
    static GlassFishCloudInstance load(InstanceProperties props) {
        String name = props.getString(PROPERTY_NAME, null);
        if (name != null) {
            String host = props.getString(PROPERTY_HOST, null);
            int port = props.getInt(PROPERTY_PORT, -1);
            String localServerHome = props.getString(
                    PROPERTY_LOCAL_SERVER, null);

            LOG.log(Level.FINER,
                    "Loaded GlassFishCloudInstance({0}, {1}, {2})",
                    new Object[]{name, host, port});
            GlassFishServerEntity localServer;
            try {
                localServer = new GlassFishServerEntity(localServerHome,
                        GlassFishCloudUrl.url(GlassFishCloudInstance.URL_PREFIX,
                        name));
            } catch (DataException de) {
                localServer = null;
            }
            return new GlassFishCloudInstance(name, host, port, localServer);
        } else {
            LOG.log(Level.WARNING,
                    "Stored GlassFishCloudInstance name is null, skipping");
            return null;
        }
    }

    /**
     * Compare if given properties represents this cloud instance.
     * <p/>
     * @param props Set of properties to compare with this
     *        GlassFish cloud instance.
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
