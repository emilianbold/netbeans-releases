
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

/*
 * WebProjectJAXWSVersionProvider.java
 *
 * Created on March 21, 2007, 3:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.ejbjarproject.jaxws;

import java.io.File;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.websvc.api.jaxws.project.JAXWSVersionProvider;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 *
 * @author rico
 */
public class EjbProjectJAXWSVersionProvider implements JAXWSVersionProvider{
    
    private AntProjectHelper h;
    /** Creates a new instance of WebProjectJAXWSVersionProvider */
    public EjbProjectJAXWSVersionProvider(AntProjectHelper h) {
        this.h = h;
    }
    
    public String getJAXWSVersion(){
        File appSvrRoot = null;
        Map properties = h.getStandardPropertyEvaluator().getProperties();
        if(properties != null){
            String serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
            if(serverInstance == null){
                String serverType = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(EjbJarProjectProperties.J2EE_SERVER_TYPE);
                if (serverType != null) {
                    String[] servInstIDs = Deployment.getDefault().getInstancesOfServer(serverType);
                    if (servInstIDs.length > 0) {
                        serverInstance = servInstIDs[0];
                    }
                }
            }
            if (serverInstance != null) {
                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
                if (j2eePlatform != null) {
                    File[] roots = j2eePlatform.getPlatformRoots();
                    if(roots != null && roots.length > 0){
                        appSvrRoot = roots[0];
                    }
                }
            }
        }
        return WSUtils.getJAXWSVersion(appSvrRoot);
    }
}
