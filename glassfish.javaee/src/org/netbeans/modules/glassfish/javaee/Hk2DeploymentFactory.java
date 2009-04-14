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

package org.netbeans.modules.glassfish.javaee;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.openide.util.NbBundle;


/**
 *
 * @author Ludo
 * @author vince
 */
public class Hk2DeploymentFactory implements DeploymentFactory {

    private static Hk2DeploymentFactory preludeInstance;
    private static Hk2DeploymentFactory ee6Instance;
    private String[] uriFragments;
    private String version;
    private String displayName;
    private ServerUtilities su;

    private Hk2DeploymentFactory(String[] uriFragments, String version, String displayName) {
        this.uriFragments = uriFragments;
        this.version = version;
        this.displayName = displayName;
    }

    private void setServerUtilities(ServerUtilities su) {
        this.su = su;
    }


    /**
     * 
     * @return 
     */
    public static synchronized DeploymentFactory createPrelude() {
        if (preludeInstance == null) {
            // TODO - find way to get uri fragment from GlassfishInstanceProvider
            //ServerUtilities t = ServerUtilities.getEe6Utilities();
            String[] allowed;
            // FIXME -- these strings should come from some constant place
            String v3Root = System.getProperty("org.glassfish.v3ee6.installRoot");
            if ("true".equals(System.getProperty("org.glassfish.v3.enableExperimentalFeatures")) ||
                (null != v3Root && v3Root.trim().length() > 0) ) {
                // pick up v3 Prelude and v3 instances and treat themn like Prelude
                allowed = new String[] { "deployer:gfv3:" };
            } else {
                allowed = new String[] { "deployer:gfv3:", "deployer:gfv3ee6:" };
            }
            preludeInstance = new Hk2DeploymentFactory(allowed, "0.1",
                    NbBundle.getMessage(Hk2DeploymentFactory.class, "TXT_PreludeDisplayName"));
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(preludeInstance);
        }
        return preludeInstance;
    }

    /**
     *
     * @return
     */
    public static synchronized DeploymentFactory createEe6() {
        // FIXME -- these strings should come from some constant place
        String v3Root = System.getProperty("org.glassfish.v3ee6.installRoot");
        if ("true".equals(System.getProperty("org.glassfish.v3.enableExperimentalFeatures")) ||
            (null != v3Root && v3Root.trim().length() > 0) ) {
            if (ee6Instance == null) {
                ee6Instance = new Hk2DeploymentFactory(new String[]
                    { "deployer:gfv3ee6:" }, "0.2",
                        NbBundle.getMessage(Hk2DeploymentFactory.class, "TXT_DisplayName"));
                DeploymentFactoryManager.getInstance().registerDeploymentFactory(ee6Instance);
            }
        }
        return ee6Instance;
    }

    /**
     * 
     * @param uri 
     * @return 
     */
    public boolean handlesURI(String uri) {
        if (uri == null) {
            return false;
        }
        
        if(uri.startsWith("[")) {//NOI18N
            for (String uriFragment : uriFragments) {
                if (uri.indexOf(uriFragment)!=-1) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 
     * @param uri 
     * @param uname 
     * @param passwd 
     * @return 
     * @throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException 
     */
    public DeploymentManager getDeploymentManager(String uri, String uname, String passwd) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI:" + uri); // NOI18N
        }
        finishInit();
        return new Hk2DeploymentManager(uri, uname, passwd, su);
    }

    /**
     * 
     * @param uri 
     * @return 
     * @throws javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException 
     */
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        if (!handlesURI(uri)) {
            throw new DeploymentManagerCreationException("Invalid URI:" + uri); // NOI18N
        }
        finishInit();
        return new Hk2DeploymentManager(uri, null, null, su);
    }

    /**
     * 
     * @return 
     */
    public String getProductVersion() {
        return version;
    }

    /**
     * 
     * @return 
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Creating the server utility instance in the constructor triggered an
     * exception, since some infrastucture wasn't initialized completely.
     */
    private void finishInit() {
        if (null != preludeInstance)
            preludeInstance.setServerUtilities(ServerUtilities.getPreludeUtilities());
        if (null != ee6Instance)
            ee6Instance.setServerUtilities(ServerUtilities.getEe6Utilities());
    }
}
