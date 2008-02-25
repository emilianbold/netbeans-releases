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

import java.io.File;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.util.NbBundle;


/**
 *
 * @author Ludo
 */
public class Hk2DeploymentFactory implements DeploymentFactory {

    public static final String URI_PREFIX = "deployer:gfv3"; // NOI18N

    private static DeploymentFactory instance;


    /**
     * 
     * @return 
     */
    public static synchronized DeploymentFactory create() {
        if (instance == null) {
            instance = new Hk2DeploymentFactory();
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
        }
        return instance;
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
            if (uri.indexOf(URI_PREFIX)!=-1) {
                return true;
            }
        }

        return false;
    }

    private static File getServerLocationFromURI(String uri) throws DeploymentManagerCreationException{
        if(uri.startsWith("[")) {//NOI18N
            String loc = uri.substring(1,uri.indexOf("]"));
            return  new File(loc);
        }
        return null;
    }

    private static String getRealURI(String uri) throws DeploymentManagerCreationException{
        if(uri.startsWith("[")) {//NOI18N
            return uri.substring(uri.indexOf("]")+1,uri.length());
        }
        return uri;// the old one.
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
        return new Hk2DeploymentManager(uri, uname, passwd);
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
        return new Hk2DeploymentManager(uri, null, null);
    }

    /**
     * 
     * @return 
     */
    public String getProductVersion() {
        return "0.1"; // NOI18N
    }

    /**
     * 
     * @return 
     */
    public String getDisplayName() {
        return NbBundle.getMessage(Hk2DeploymentFactory.class, "TXT_DisplayName"); // NOI18N
    }
}
