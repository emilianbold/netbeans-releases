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

package org.netbeans.modules.j2ee.sun.ide.dm;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunURIManager;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PlatformValidator;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties;

import org.openide.util.NbPreferences;

/** This deploymenmt factory can creates an alternate deployment manager for
 * S1AS.
 * @author vkraemer,ludo
 */
public class SunDeploymentFactory implements Constants, DeploymentFactory, InstanceListener {
    
    /* default server as defined in the nbdep.xml file by out plugin
     * if we get this URL, we need to calculate an existing default domain from
     * the registry, preferably a AS 9.0 one, if we have the choice between AS 8.x and 9.0
     *
     **/
    private static final String DEFAULTSERVERDEF="[  ]deployer:Sun:AppServer::localhost:4849";// NOI18N
    
    //
    // this whole class should probably be a subclass of the
    // com.sun.enterprise.deployapi.SunDeploymentFactory...
    private DeploymentFactory innerDF = null;
    
    
    /** resource bundle
     */
    protected static final ResourceBundle bundle = ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.ide.dm.Bundle");    // NOI18N
    
    private boolean instanceListenerAdded;
    private String displayName=null;
    
    public SunDeploymentFactory() {
        instanceListenerAdded = false;
        displayName= bundle.getString("FACTORY_DISPLAYNAME");//NOI18N
    }
    
    public SunDeploymentFactory(String displayName) {
        instanceListenerAdded = false;
        this.displayName= displayName;
    }
    static final private Map<String,DeploymentManager> dms = new HashMap<String,DeploymentManager>();
    
    /** This method returns a connected deployment manager.
     *
     * @param uri
     * @param userName
     * @param password
     * @throws DeploymentManagerCreationException
     * @return a deployment manager for a particular server instance
     */
    public DeploymentManager getDeploymentManager(String uri, String userName,String password) throws DeploymentManagerCreationException {
        registerInstanceListener();
        
        try {
            if (DEFAULTSERVERDEF.equals(uri)){
                String defaultURI = null;//don;t know yet which one
                String ServerUrls[] = InstanceProperties.getInstanceList();
                for (String elem : ServerUrls) {
                    if(handlesURI(elem)){
                        if (defaultURI == null){
                            defaultURI = elem; //get the first one available. Could be 8 or 9.0 server
                        }
                        File serverLocation = getServerLocationFromURI(elem);
                        if (ServerLocationManager.isGlassFish(serverLocation)){
                            defaultURI = elem;// make sure we pick one AS 9 is one is available
                            break;
                        }
                    }
                }
                if (defaultURI != null){
                    uri = defaultURI;
                }
            }
            innerDF = ServerLocationManager.getDeploymentFactory(getServerLocationFromURI(uri));
            if (innerDF==null){
                throw new DeploymentManagerCreationException(getRealURI(uri)+getServerLocationFromURI( uri)+bundle.getString("MSG_WrongInstallDir"));
            }
            synchronized (dms) {
                DeploymentManager retVal = dms.get(uri);
                if (null == retVal) {
                    retVal = new SunDeploymentManager(innerDF, getRealURI(uri), userName, password,getServerLocationFromURI( uri));
                    dms.put(uri,retVal);
                }
                return retVal;
            }
        } catch (Exception e) {
            throw new DeploymentManagerCreationException(getRealURI(uri)+getServerLocationFromURI( uri)+bundle.getString("MSG_WrongInstallDir"));
        }
    }
    
    /** This method returns a disconnected deployment manager.
     *
     * Should a disconnected deployment manager be able to become connected?
     *
     * @param uri
     * @throws DeploymentManagerCreationException
     * @return a deployment manager for doing configuration.
     */
    public DeploymentManager getDisconnectedDeploymentManager(String uri) throws DeploymentManagerCreationException {
        try {
            
            return getDeploymentManager(uri,null,null);
        } catch (Exception e) {
            throw new DeploymentManagerCreationException(getRealURI(uri)+getServerLocationFromURI( uri)+bundle.getString("MSG_WrongInstallDir"));
        }
    }
    
    /** Determines whether this URI is handled by the Deployment factory
     * Iniitally we need to test the prefix. If the factory will support
     * multiple managers, we need to extend this test to catch those cases.
     *
     *The tests should also be extended to deteremine if the URI is "complete"
     * for this factory.  It has to have a machine name (that can be resolved
     * to an IP address) and a port. Whether the server is "up" may be an open
     * question.
     *
     * @param uri
     * @return boolean value
     */
    // also check the urlPattern in layer.xml when changing this
    public boolean handlesURI(String uri) {
        if (uri==null){
            return false;
        }
        if(uri.startsWith("[")){//NOI18N
            boolean validServer = checkServer(uri);
            if (uri.indexOf(SunURIManager.SUNSERVERSURI)!=-1 && validServer){
                return true;
            }
        }
        
        
        return false;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getProductVersion() {
        if (null != innerDF){
            return innerDF.getProductVersion();
        }
        return "1.0";//NOI18N
    }
    
    private static File getServerLocationFromURI(String uri) throws DeploymentManagerCreationException{
        
        if(uri.startsWith("[")){//NOI18N
            String loc = uri.substring(1,uri.indexOf("]"));
            return  new File(loc);
        }
        throw new DeploymentManagerCreationException(uri+bundle.getString("MSG_WrongInstallDir"));
    }
    
    private static String getRealURI(String uri) throws DeploymentManagerCreationException{
        if(uri.startsWith("[")){//NOI18N
            return uri.substring(uri.indexOf("]")+1,uri.length());
        }
        return uri;// the old one.
    }
    
    private void registerInstanceListener() {
        synchronized(dms) {
            if(!instanceListenerAdded) {
                Deployment.getDefault().addInstanceListener(this);
                instanceListenerAdded = true;
            }
        }
    }
    
    // Listen for server instance removed event so we can clear it's DM from the cache, if necessary
    public void instanceRemoved(String serverInstanceID) {
        synchronized (dms) {
            // serverInstanceID is really the URI of this installed server :)
            dms.remove(serverInstanceID);
            NbPreferences.forModule(PluginProperties.class).putBoolean(PluginProperties.REMOVED_DEFAULT_GFV2, true);
        }
    }
    
    public void instanceAdded(String serverInstanceID) {
        // n/a
    }
    
    public void changeDefaultInstance(String oldServerInstanceID, String newServerInstanceID) {
        // n/a
    }
    
    public boolean checkServer(String uri){
        boolean corrServer = false;
        if(uri.startsWith("[")){//NOI18N
            String loc = uri.substring(1,uri.indexOf("]"));
            File location = new File(loc);
            PlatformValidator pv = new PlatformValidator();
            String serverVersion = pv.getServerVersionByName(this.displayName);
            if(serverVersion != null){
                corrServer = pv.isDescriminatorPresent(location, serverVersion);
            }   
        }
        return corrServer;
    }
    
}
