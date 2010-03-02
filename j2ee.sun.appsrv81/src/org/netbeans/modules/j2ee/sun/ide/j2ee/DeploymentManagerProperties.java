/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.j2ee.sun.ide.j2ee;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.SunURIManager;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.CustomizerSupport;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author  ludo
 */
public class DeploymentManagerProperties {
    /**
     * Domain property,
     */
    public static final String DOMAIN_ATTR = "DOMAIN"; //NOI18N
    
    /**
     * HTTP Monitor on or off property, its value is jsut before starting the server with the correct setting.
     */
    public static final String HTTP_MONITOR_ATTR = "HttpMonitorOn"; //NOI18N
    
    /**
     * HTTP Proxy sync up with IDE setting: on or off property, its value is just before starting the server with the correct setting.
     */
    public static final String HTTP_PROXY_SYNCHED_ATTR = "HttpProxySynced"; //NOI18N
    
    /**
     * directory deployment possible on or off property, .
     */
    public static final String DIRDEPLOYMENT_POSSIBLE_ATTR = "DirectoryDeploymentPossible"; //NOI18N
    /**
     * Password property, its value is used by the deployment manager.
     */
    public static final String PASSWORD_ATTR = "password"; //NOI18N
    
    /**
     * Display name property, its value is used by IDE to represent server instance.
     */
    public static final String DISPLAY_NAME_ATTR = "displayName"; //NOI18N
    /**
     * Location of the app server instance property, its value is used by IDE to represent server instance.
     */
    public static final String LOCATION_ATTR = "LOCATION"; //NOI18N
    
    
    /**
     * HTTP port property, The port where the instance runs
     */
    public static final String HTTP_PORT_NUMBER_ATTR = "httpportnumber";
    
    /* AVK Instrumentation on or off
     *
     **/
    public static final String AVK_INSTRUMENTED_ATTR = "AVKTurnedOn";
    
    /* Driver Deployment on or off
     *
     **/
    public static final String DRIVER_DEPLOYMENT_ATTR = "DriverDeploymentEnabled";
    
    /**
     * Start of Java DB database on or off
     */
    public static final String DATABASE_START_ATTR = "DatabaseStartEnabled"; //NOI18N

    private static final String PROP_SOURCES       = "sources";         // NOI18N
    private static final String PROP_JAVADOCS      = "javadocs";        // NOI18N
    
    final private InstanceProperties instanceProperties;
    final private SunDeploymentManagerInterface sunDM;

    /** Creates a new instance of DeploymentManagerProperties */
    public DeploymentManagerProperties(DeploymentManager deploymentManager) {
        sunDM = (SunDeploymentManagerInterface)deploymentManager;
        InstanceProperties ip = SunURIManager.getInstanceProperties(sunDM.getPlatformRoot(),
                sunDM.getHost(),sunDM.getPort());
        String httpport =  sunDM.getNonAdminPortNumber();
        if (httpport==null){
            httpport ="8080";//hard code //NOI18N
        }
        if (ip==null){
            try {                
                ip = 
                        SunURIManager.createInstanceProperties(sunDM.getPlatformRoot(),
                            sunDM.getHost(),""+sunDM.getPort(), sunDM.getUserName(),
                            sunDM.getPassword() , sunDM.getHost()+":"+sunDM.getPort() );
                if (null != ip) {
                    ip.setProperty(HTTP_PORT_NUMBER_ATTR, httpport);
                }
            } catch (InstanceCreationException e){
            }
        }
        if (ip != null && ip.getProperty(HTTP_PORT_NUMBER_ATTR)==null){
            ip.setProperty(HTTP_PORT_NUMBER_ATTR, httpport);            
        }
        instanceProperties = ip;
    }
    
    /**
     * Getter for property domainName. Should never be null
     * @return Value of property domainName.
     */
    public java.lang.String getDomainName() {
        String retVal = ""; //NOI18N
        if (instanceProperties==null){
            return retVal;
        }
        retVal = instanceProperties.getProperty(DOMAIN_ATTR) ;
        if (null == retVal || (retVal.trim().length() == 0)) {
            retVal = ""; //NOI18N
        }
        return retVal;
    }
    
    /**
     * Setter for property domainName.
     * @param domainName New value of property domainName.
     */
    public void setDomainName(java.lang.String domainName) {
        instanceProperties.setProperty(DOMAIN_ATTR, domainName);
    }
    
    /**
     * Getter for property location. can be null if the dm is remote
     * @return Value of property location.
     */
    public java.lang.String getLocation() {
        java.io.File irf = sunDM.getPlatformRoot();
        String installRoot = null;
        if (null != irf && irf.exists()){
            installRoot = irf.getAbsolutePath();
        }
        if (instanceProperties==null){
            return installRoot;
        }
        String ret= instanceProperties.getProperty(LOCATION_ATTR) ;
        if (ret==null){
            return installRoot;
        }
        return ret;
    }
    
    /**
     * Setter for property location.
     * @param location New value of property location.
     */
    public void setLocation(java.lang.String location) {
        if (instanceProperties==null){
            return;
        }
        instanceProperties.setProperty(LOCATION_ATTR, location);
    }
    
    /**
     * Getter for property password. can be null if the DM is a disconnected DM
     * @return Value of property password.
     */
    public java.lang.String getPassword() {
        if (instanceProperties==null){
            return null;
        }
        return getPassword(instanceProperties);
    }

    public static String getPassword(InstanceProperties instanceProperties) {
        String retVal = instanceProperties.getProperty(InstanceProperties.PASSWORD_ATTR);
        String key = instanceProperties.getProperty(InstanceProperties.URL_ATTR);
        char[] retChars = Keyring.read(key);
        if (null == retChars || retChars.length < 1 || !GlassfishModule.PASSWORD_CONVERTED_FLAG.equals(retVal)) {
            retChars = retVal.toCharArray();
            Keyring.save(key, retChars, "a Glassfish/SJSAS passord");
            instanceProperties.setProperty(InstanceProperties.PASSWORD_ATTR, GlassfishModule.PASSWORD_CONVERTED_FLAG) ;
        } else {
            retVal = String.copyValueOf(retChars);
        }
        return retVal;
    }
    
    /**
     * Setter for property password.
     * @param password New value of property password.
     */
    public void setPassword(java.lang.String password) {
        String key = instanceProperties.getProperty(InstanceProperties.URL_ATTR);
        Keyring.save(key, password.toCharArray(), "a Glassfish/SJSAS passord");
    }
        
    /**
     * Getter for property UserName. can be null for a disconnected DM.
     * @return Value of property UserName.
     */
    public java.lang.String getUserName() {
        if (instanceProperties==null){
            return null;
        }
        return instanceProperties.getProperty(InstanceProperties.USERNAME_ATTR) ;
    }
    
    /**
     * Setter for property UserName.
     * @param UserName New value of property UserName.
     */
    public void setUserName(java.lang.String UserName) {
        instanceProperties.setProperty(InstanceProperties.USERNAME_ATTR, UserName);
        
    }
    
    public String getHttpMonitorOn() {
        if (instanceProperties==null){
            return "false";
        }
        String s = instanceProperties.getProperty(HTTP_MONITOR_ATTR);
        if (s==null){
            return "false";
        }
        
        return s;
    }
    
    public void setHttpMonitorOn(String HttpMonitorOn) {
        instanceProperties.setProperty(HTTP_MONITOR_ATTR, HttpMonitorOn);
    }
    public String getHttpPortNumber() {
        if (instanceProperties==null){
            return "8080";
        }
        return instanceProperties.getProperty(HTTP_PORT_NUMBER_ATTR) ;
    }
    
    public void setHttpPortNumber(String port) {
        instanceProperties.setProperty(HTTP_PORT_NUMBER_ATTR, port);
    }
    
    public String getDisplayName() {
        if (instanceProperties == null) {
            return null;
        }
        return instanceProperties.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
    }
    
    public String getUrl() {
        if (instanceProperties == null) {
            return null;
        }
        return instanceProperties.getProperty(InstanceProperties.URL_ATTR);
    }
    
    public InstanceProperties getInstanceProperties() {
        return instanceProperties;
    }
    
    
    public boolean getAVKOn() {
        if (instanceProperties == null){
            return false;
        }
        String s = instanceProperties.getProperty(AVK_INSTRUMENTED_ATTR);
        if (s == null){
            return false;
        }
        return Boolean.valueOf(s).booleanValue();
    }
    
    public void setAVKOn(boolean AVKOn) {
        instanceProperties.setProperty(AVK_INSTRUMENTED_ATTR, Boolean.toString(AVKOn));
    }
    
    public void setJavadocs(List<URL> path) {
        instanceProperties.setProperty(PROP_JAVADOCS, CustomizerSupport.buildPath(path));
        PlatformImpl platform = (PlatformImpl) new PlatformFactory().getJ2eePlatformImpl((DeploymentManager) sunDM);
        platform.notifyLibrariesChanged();
    }
    
    public void setSources(List<URL> path) {
        instanceProperties.setProperty(PROP_SOURCES, CustomizerSupport.buildPath(path));
        PlatformImpl platform = (PlatformImpl) new PlatformFactory().getJ2eePlatformImpl((DeploymentManager) sunDM);
        platform.notifyLibrariesChanged();
    }
    
    public List<URL> getClasses() {
        List data = new ArrayList();
        PlatformImpl platform = (PlatformImpl) new PlatformFactory().getJ2eePlatformImpl((DeploymentManager) sunDM);
        for (LibraryImplementation libImpl : platform.getLibraries()) {
            data.addAll(libImpl.getContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH));
        }
        return data;
    }
    
    public List<URL> getSources() {
        String path = instanceProperties.getProperty(PROP_SOURCES);
        if (path == null) {
            return new ArrayList();
        }
        return CustomizerSupport.tokenizePath(path);
    }
    
    public List<URL> getJavadocs() {
        String path = instanceProperties.getProperty(PROP_JAVADOCS);
        if (path == null) {
            ArrayList<URL> list = new ArrayList<URL>();
            try {
                File j2eeDoc = InstalledFileLocator.getDefault().locate("docs/javaee6-doc-api.zip", null, false); // NOI18N
                if (j2eeDoc != null) {
                    list.add(fileToUrl(j2eeDoc));
                }
            } catch (MalformedURLException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            return list;
        }
        return CustomizerSupport.tokenizePath(path);
    }
    
    /** Return URL representation of the specified file. */
    private static URL fileToUrl(File file) throws MalformedURLException {
        URL url = file.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        }
        return url;
    }
    
    /**
     * return true is the IDE needs to sync up this instance with the same proxies as the IDE
     * true by default
     **/
    public boolean isSyncHttpProxyOn() {
        if (instanceProperties == null){
            return true;//true by default
        }
        String s = instanceProperties.getProperty(HTTP_PROXY_SYNCHED_ATTR);
        if (s == null){
            instanceProperties.setProperty(HTTP_PROXY_SYNCHED_ATTR,"true");     // NOI18N
            s = instanceProperties.getProperty(HTTP_PROXY_SYNCHED_ATTR);
        }
        return Boolean.valueOf(s).booleanValue();
    }
    
    public void setSyncHttpProxyOn(boolean syncHttpProxyOn) {
        instanceProperties.setProperty(HTTP_PROXY_SYNCHED_ATTR, Boolean.toString(syncHttpProxyOn));
    }
    
    /**
     * return true is this instance can utilized directory deployment for Web Apps (for now)
     * true by default
     ** @return 
     */
    public boolean isDirectoryDeploymentPossible() {
        if (instanceProperties == null){
            return true;//true by default
        }
        String s = instanceProperties.getProperty(DIRDEPLOYMENT_POSSIBLE_ATTR);
        if (s == null){
            instanceProperties.setProperty(DIRDEPLOYMENT_POSSIBLE_ATTR, sunDM.isLocal()+"");
            s = instanceProperties.getProperty(DIRDEPLOYMENT_POSSIBLE_ATTR);
        }
        return Boolean.valueOf(s).booleanValue();
    }
    
    /**
     * 
     * @param dirpossible 
     */
    public void setDirectoryDeploymentPossible(boolean dirpossible) {
        instanceProperties.setProperty(DIRDEPLOYMENT_POSSIBLE_ATTR, Boolean.toString(dirpossible));
    }
    
    /**
     * 
     * @return 
     */
    public int getDeploymentTimeout() {
        String s = instanceProperties.getProperty(InstanceProperties.DEPLOYMENT_TIMEOUT);
        if (s == null){
            instanceProperties.setProperty(InstanceProperties.DEPLOYMENT_TIMEOUT, Integer.toString(144));
            s = instanceProperties.getProperty(InstanceProperties.DEPLOYMENT_TIMEOUT);
        }
        return Integer.parseInt(s);
    }
    
    /**
     * 
     * @param newVal 
     */
    public void setDeploymentTimeout(int newVal) {
        instanceProperties.setProperty(InstanceProperties.DEPLOYMENT_TIMEOUT, Integer.toString(newVal));
    }
    
    /**
     * 
     * @return 
     */
    public int getStartupTimeout() {
        String s = instanceProperties.getProperty(InstanceProperties.STARTUP_TIMEOUT);
        if (s == null){
            instanceProperties.setProperty(InstanceProperties.STARTUP_TIMEOUT, Integer.toString(288));
            s = instanceProperties.getProperty(InstanceProperties.STARTUP_TIMEOUT);
        }
        return Integer.parseInt(s);
    }
    
    /**
     * 
     * @param newVal 
     */
    public void setStartupTimeout(int newVal) {
        instanceProperties.setProperty(InstanceProperties.STARTUP_TIMEOUT, Integer.toString(newVal));
    }
    
    /**
     * Getter for property location. can be null if the dm is remote
     * @return Value of property location.
     */
    public File getDriverLocation() {
        String driverLocation = getLocation();
        String domainName = getDomainName();
        String driverDir = new File(driverLocation).getAbsolutePath();
        driverLocation = driverDir + File.separator + domainName + File.separator + "lib";
        return new File(driverLocation);
    }
    
    /**
     * return true for automatic deployment for JDBC Drivers
     * true by default
     ** @return 
     */
    public boolean isDriverDeploymentEnabled() {
         if (instanceProperties == null){
            return true;//true by default
        }
        String s = instanceProperties.getProperty(DRIVER_DEPLOYMENT_ATTR);
        if (s == null){
            instanceProperties.setProperty(DRIVER_DEPLOYMENT_ATTR,"true");     // NOI18N
            s = instanceProperties.getProperty(DRIVER_DEPLOYMENT_ATTR);
        }
        return Boolean.valueOf(s).booleanValue();
    }
    
    /**
     * 
     * @param dirpossible 
     */
    public void setDriverDeploymentEnabled(boolean driverEnabled) {
        instanceProperties.setProperty(DRIVER_DEPLOYMENT_ATTR, Boolean.toString(driverEnabled));
    }
    
    /**
     * return true if Java DB database should be started on server start
     * true by default
     ** @return 
     */
    public boolean isDatabaseStartEnabled() {
        if (instanceProperties == null){
            return true;//true by default
        }
        String s = instanceProperties.getProperty(DATABASE_START_ATTR);
        if (s == null){
            instanceProperties.setProperty(DATABASE_START_ATTR, "true"); //NOI18N
            s = instanceProperties.getProperty(DATABASE_START_ATTR);
        }
        return Boolean.valueOf(s).booleanValue();
    }
    
    /**
     * 
     * @param startEnabled
     */
    public void setDatabaseStartEnabled(boolean startEnabled) {
        instanceProperties.setProperty(DATABASE_START_ATTR, Boolean.toString(startEnabled));
    }
    
    public SunDeploymentManagerInterface getSunDeploymentManager(){
        return sunDM;
    }
}
