
/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * WebProjectJAXWSVersionProvider.java
 *
 * Created on March 21, 2007, 3:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.project.jaxws;

import java.io.File;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.modules.websvc.api.jaxws.project.JAXWSVersionProvider;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 *
 * @author rico
 */
public class WebProjectJAXWSVersionProvider implements JAXWSVersionProvider{
    
    private AntProjectHelper h;
    private WebProject webProject;
    /** Creates a new instance of WebProjectJAXWSVersionProvider */
    public WebProjectJAXWSVersionProvider(AntProjectHelper h, WebProject webProject) {
        this.h = h;
        this.webProject = webProject;
    }
    
    public String getJAXWSVersion(){
        File appSvrRoot = null;
        Map properties = h.getStandardPropertyEvaluator().getProperties();
        String serverInstance = null;
        if(properties != null){
            serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
            if (serverInstance == null){
                WebProjectProperties wpp = webProject.getWebProjectProperties();
                String serverType = (String) wpp.get(WebProjectProperties.J2EE_SERVER_TYPE);
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
