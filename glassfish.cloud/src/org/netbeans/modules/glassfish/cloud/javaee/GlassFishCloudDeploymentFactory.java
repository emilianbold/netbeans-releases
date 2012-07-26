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
package org.netbeans.modules.glassfish.cloud.javaee;

import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.glassfish.tools.ide.utils.StringPrefixTree;
import org.netbeans.modules.glassfish.cloud.data.GlassFishAccountInstance;
import org.netbeans.modules.glassfish.cloud.data.GlassFishCloudInstance;
import org.netbeans.modules.glassfish.cloud.data.GlassFishUrl;
import static org.openide.util.NbBundle.getMessage;

/**
 * Deployment driver for GlassFish cloud.
 * <p/>
 * It returns a <code>DeploymentManager<code> object which represents
 * a connection to GlassFish cloud registered in NetBeans and identified by URI.
 * <p/>
 * Based on API that will be made optional in JavaEE 7 platform.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishCloudDeploymentFactory implements DeploymentFactory {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(
            GlassFishCloudDeploymentFactory.class.getSimpleName());

    /** Code marking local GlassFish registered with cloud (CPAS). */
    private static final int URL_LOCAL = 1;

    /** Code marking remote cloud registered as user account. */
    private static final int URL_CLOUD = 2;

    /** Matcher used to find registered URL prefixes. */
    private static final StringPrefixTree<Integer> uriMap
        = new StringPrefixTree<Integer>(false);
    static {
        uriMap.add(GlassFishCloudInstance.URL_PREFIX
                + GlassFishUrl.URL_SEPARATOR, URL_LOCAL);
        uriMap.add(GlassFishAccountInstance.URL_PREFIX
                + GlassFishUrl.URL_SEPARATOR, URL_CLOUD);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of deployment driver for GlassFish cloud.
     */
    public GlassFishCloudDeploymentFactory() {
        // Nothing at this moment    
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Interface Methods                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Tests whether this factory can create a DeploymentManager object based
     * on the specified URI.
     * <p/>
     * @param uri The URI to check.
     * @return <code>true</code> if the factory can handle the URI
     *         or <code>false<code> otherwise.
     */
    @Override
    public boolean handlesURI(String uri) {
        return uriMap.prefixMatch(uri) != null;
    }

    /**
     * Return connected DeploymentManager instance.
     * <p/>
     * @param uri The URI that specifies the connection parameters.
     * @param userName User name required to connect to registered GlassFish
     *                 cloud. This attribute is ignored for GlassFish because
     *                 it's stored in GlassFish entity object.
     * @param password User password required to connect to registered GlassFish
     *                 cloud. This attribute is ignored for GlassFish because
     *                 it's stored in GlassFish entity object.
     * @return Connected and ready <code>DeploymentManager</code> instance.
     * @throws DeploymentManagerCreationException Occurs when
     *         <code>DeploymentManager</code> could not be returned (server
     *         down, unable to authenticate, etc).
     */
    @Override
    public DeploymentManager getDeploymentManager(String uri, String userName,
            String password) throws DeploymentManagerCreationException {
        GlassFishUrl url;
        try {
            url = new GlassFishUrl(uri);
        } catch (IllegalArgumentException iae) {
            throw new DeploymentManagerCreationException(iae.getMessage());
        }
        switch(url.getType()) {
            case CLOUD: return new GlassFishCloudDeploymentManager();
            case LOCAL: return new GlassFishAccountDeploymentManager();
            // This is unrecheable. Being here means this class does not handle
            // all possible values correctly.
            default: throw new DeploymentManagerCreationException(
                    "URL constructor set unknown URL type");
        }
    }

    /**
     * Return disconnected DeploymentManager instance.
     * <p/>
     * @param uri The URI that specifies the connection parameters.
     * @return Disconnected <code>DeploymentManager</code> instance.
     * @throws DeploymentManagerCreationException Occurs when
     *         <code>DeploymentManager</code> could not be created.
     */
    @Override
    public DeploymentManager getDisconnectedDeploymentManager(String uri)
            throws DeploymentManagerCreationException {
        // User and password arguments are ignored anyway.
        return getDeploymentManager(uri, null, null);
    }

    /**
     * Provide string with name of GlassFish cloud deployment manager.
     * <p/>
     * @return Name of GlassFish cloud deployment manager.
     */
    @Override
    public String getDisplayName() {
        return getMessage(GlassFishCloudDeploymentFactory.class,
                Bundle.GLASSFISH_CLOUD_DEPL_FACTORY_DISPLAY_NAME);
    }

    /**
     * Provide string identifying version of GlassFish cloud deployment manager.
     * <p/>
     * @return Version of GlassFish cloud deployment manager.
     */
    @Override
    public String getProductVersion() {
        return getMessage(GlassFishCloudDeploymentFactory.class,
                Bundle.GLASSFISH_CLOUD_DEPL_FACTORY_VERSION);
    }
    
}
