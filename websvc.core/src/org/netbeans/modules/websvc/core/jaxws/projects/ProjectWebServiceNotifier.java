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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.core.jaxws.projects;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.WebServiceNotifier;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.jaxws.JaxWs;
import org.netbeans.modules.websvc.wsstack.jaxws.JaxWsStackProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 *
 * @author mkuchtiak
 */
public class ProjectWebServiceNotifier implements WebServiceNotifier {
    private static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; //NOI18N
    
    private Project proj;
    public ProjectWebServiceNotifier(Project proj) {
        this.proj=proj;
    }

    /** Notifies that web service was added */
    public void serviceAdded(String serviceName, String implementationClass) {
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(proj.getProjectDirectory());
        if (jaxWsSupport!=null) jaxWsSupport.addService(serviceName, implementationClass, isJsr109Supported());
    }

    /** Notifies that web service was removed */
    public void serviceRemoved(String serviceName) {
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(proj.getProjectDirectory());
        if (jaxWsSupport!=null) jaxWsSupport.serviceFromJavaRemoved(serviceName);
    }

    private boolean isJsr109Supported() {
        boolean jsr109Supported = false;
        EditableProperties projectProperties = null;
        try {
            projectProperties = WSUtils.getEditableProperties(proj, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        } catch (IOException ex) {
            
        }
        if (projectProperties!=null) {
            String serverInstance = projectProperties.getProperty(J2EE_SERVER_INSTANCE);
            if (serverInstance != null) {
                try {
                    J2eePlatform j2eePlatform = Deployment.getDefault().getServerInstance(serverInstance).getJ2eePlatform();
                    WSStack<JaxWs> wsStack = JaxWsStackProvider.getJaxWsStack(j2eePlatform);
                    if (wsStack != null) {
                        return wsStack.isFeatureSupported(JaxWs.Feature.JSR109);
                    }
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Failed to find J2eePlatform", ex);
                }
            }
        }
        return jsr109Supported;
    }

}
