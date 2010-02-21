/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.j2ee.weblogic9;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLDeploymentManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.util.NbBundle;

/**
 * The main entry point to the plugin. Keeps the required static data for the
 * plugin and returns the DeploymentManagers required for deployment and
 * configuration. Does not directly perform any interaction with the server.
 *
 * @author Kirill Sorokin
 * @author Petr Hejl
 */
public class WLDeploymentFactory implements DeploymentFactory {

    public static final String URI_PREFIX = "deployer:WebLogic:http://"; // NOI18N

    public static final int DEFAULT_PORT = 7001;

    private static final Logger LOGGER = Logger.getLogger(WLDeploymentFactory.class.getName());

    private ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * The singleton instance of the factory
     * <p>
     * <i>GuardedBy(WLDeploymentFactory.class)</i>
     */
    private static WLDeploymentFactory instance;


    /**
     * The singleton factory method
     *
     * @return the singleton instance of the factory
     */
    public static synchronized WLDeploymentFactory getInstance() {
        if (instance == null) {
            instance = new WLDeploymentFactory();
            //DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
        }
        return instance;
    }

    public boolean handlesURI(String uri) {
        if (uri != null && uri.startsWith(URI_PREFIX)) {
            return true;
        }

        return false;
    }

    public DeploymentManager getDeploymentManager(String uri, String username,
            String password) throws DeploymentManagerCreationException {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "getDeploymentManager, uri:" // NOI18N
                    + uri + " username:" + username + " password:" + password); // NOI18N
        }

        String[] parts = uri.split(":"); // NOI18N
        String host = parts[3].substring(2);
        String port = parts[4] != null ? parts[4].trim() : parts[4];

        WLDeploymentManager dm = new WLDeploymentManager(this, uri, host, port, false);
        return dm;
    }

    public DeploymentManager getDisconnectedDeploymentManager(String uri)
            throws DeploymentManagerCreationException {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "getDisconnectedDeploymentManager, uri:" + uri); // NOI18N
        }

        String[] parts = uri.split(":"); // NOI18N
        String host = parts[3].substring(2);
        String port = parts[4] != null ? parts[4].trim() : parts[4];
        WLDeploymentManager dm = new WLDeploymentManager(this, uri, host, port, true);
        return dm;
    }

    public String getProductVersion() {
        return NbBundle.getMessage(WLDeploymentFactory.class, "TXT_productVersion");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WLDeploymentFactory.class, "TXT_displayName");
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

}
