/*
 * WebProjectJAXWSVersionProvider.java
 *
 * Created on March 21, 2007, 3:21 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.clientproject.wsclient;

import java.io.File;
import java.util.Map;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.websvc.api.jaxws.project.JAXWSVersionProvider;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 *
 * @author rico
 */
public class AppClientProjectJAXWSVersionProvider implements JAXWSVersionProvider{
    
    private AntProjectHelper h;
    /** Creates a new instance of WebProjectJAXWSVersionProvider */
    public AppClientProjectJAXWSVersionProvider(AntProjectHelper h) {
        this.h = h;
    }
    
    public String getJAXWSVersion(){
        File appSvrRoot = null;
        Map properties = h.getStandardPropertyEvaluator().getProperties();
        if(properties != null){
            String serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
            if(serverInstance == null){
                String serverType = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(AppClientProjectProperties.J2EE_SERVER_TYPE);
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
