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

import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.glassfish.cloud.data.GlassFishInstance;
import org.netbeans.modules.glassfish.cloud.data.GlassFishUrl;
import org.netbeans.modules.j2ee.deployment.plugins.spi.DeploymentManager2;

/**
 * Abstract deployment manager for GlassFish cloud.
 * <p/>
 * Contains common functionality for both local server and remote cloud server.
 * <p/>
 * Provides the core set of functions a Java EE platform must provide for
 * Java EE application deployment. It provides server related information,
 * such as list of deployment targets and GlassFish cloud unique runtime
 * configuration information.
 * <p/>
 * Based on API that will be made optional in JavaEE 7 platform.
 * <p/>
 */
public abstract class GlassFishDeploymentManager implements DeploymentManager2 {
    
    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish cloud URL. */
    final GlassFishUrl url;

    /** GlassFish instance interface. */
    final GlassFishInstance instance;
    
    /** GlassFish cloud local server and remote cloud life cycle services
      * manager from the IDE.
      * Life cycle services manager instance is bound to its deployment manager
      * instance.
      */
    final GlassFishStartServer startServer;
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of abstract deployment manager for GlassFish cloud.
     * <p/>
     * This is non public constructor called only in child classes to initialize
     * common deployment manager attributes.
     * <p/>
     * @param url             GlassFish cloud URL.
     * @param instance        GlassFish instance interface implementation.
     * @param startServer     GlassFish cloud local server and remote cloud life
     *                        cycle services from the IDE.
     */
    GlassFishDeploymentManager(GlassFishUrl url, GlassFishInstance instance,
            GlassFishStartServer startServer) {
        this.url = url;
        this.instance = instance;
        this.startServer = startServer;
        if (instance == null) {
            throw new NullPointerException("There is no account instance named "
                    + url.getName());
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // getters and setters                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get life cycle services manager instance bound to this manager.
     * <p/>
     * @return Life cycle services manager.
     */
    public GlassFishStartServer getStartServer() {
        return startServer;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented interface methods                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve the list of deployment targets supported by this
     * DeploymentManager.
     * <p/>
     * @return List of deployment Target designators the user may select for
     *         application deployment or <code>null</code> if there are none. 
     * @throws IllegalStateException Is thrown when the method is called when
     *         running in disconnected mode.
     */
    @Override
    public Target[] getTargets() throws IllegalStateException {
        return new Target[] {instance};
    }

}
