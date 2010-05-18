/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.net.Socket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.management.Attribute;
import javax.management.ObjectName;

import org.netbeans.modules.j2ee.sun.api.ServerInterface;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.JvmOptions;

/**
 * @author edwingo
 *
 * Manipulate the app server http proxy options.
 * Requires app server to be running before this class can be used.
 */
public class HttpProxyUpdater {
    public static final String HTTP_PROXY_HOST = "-Dhttp.proxyHost=";
    public static final String HTTP_PROXY_PORT = "-Dhttp.proxyPort=";
    public static final String HTTPS_PROXY_HOST = "-Dhttps.proxyHost=";
    public static final String HTTPS_PROXY_PORT = "-Dhttps.proxyPort=";
    public static final String HTTP_PROXY_NO_HOST = "-Dhttp.nonProxyHosts=";

    private String httpProxyPort;
    private String httpProxyHost;
    private String httpsProxyPort;
    private String httpsProxyHost;
    private String httpProxyNoHost;
    private static String JVM_OPTIONS = "jvm-options";
    private ObjectName jvmOptionsObjectName;

    private ServerInterface si;
    private String[] options;
    private boolean serverRunning;
    
    ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.j2ee.Bundle");	// NOI18N
    /**
     * Requires app server to be running before this class can be used.
     * 
     * @param si
     * @throws Exception if http proxy options cannot be accessed.
     */
    public HttpProxyUpdater(ServerInterface si, boolean serverRunning) {
        this.serverRunning = serverRunning;
        this.si = si;
    }

    /*
     * Get list of proxy settings form JVM Options if any
     *
     */
    private void getOptionsFromServer() throws Exception {
        jvmOptionsObjectName = new JvmOptions(si.getMBeanServerConnection()).getConfigObjectName();
        options =
            (String[]) si.getAttribute(jvmOptionsObjectName, JVM_OPTIONS);

        initializeProps();
    }

    private void setOptionsToServer() throws Exception {
        jvmOptionsObjectName = new JvmOptions(si.getMBeanServerConnection()).getConfigObjectName();
        Attribute newOptions = new Attribute(JVM_OPTIONS, options);
        si.setAttribute(jvmOptionsObjectName, newOptions);
    }
    
    private void getOptionsFromXml() {
        DomainEditor dEditor = new DomainEditor(si.getDeploymentManager());
        options = dEditor.getHttpProxyOptions();
        initializeProps();
    }
    
    private void setOptionsToXml() {
        DomainEditor dEditor = new DomainEditor(si.getDeploymentManager());
        dEditor.setHttpProxyOptions(options);
    }

    /**
     * @param prefix
     * @return true iff anything changed
     */
    private boolean removeProperty(String prefix) {
        ArrayList al = new ArrayList();
        for (int i = 0; i < options.length; i++) {
            String option = options[i].trim();
            if (!option.startsWith(prefix)) {
                al.add(options[i]);
            }
        }

        boolean changed = options.length != al.size();
        if (changed) {
            options = (String[]) al.toArray(new String[al.size()]);
        }
        return changed;
    }

    /**
     * @param prefix
     * @param value
     * @return true iff anything changed
     */
    private boolean setProperty(String prefix, String value) {
        if (value == null) {
            return removeProperty(prefix);
        }

        String newValue = prefix + value;
        
        // Replace any existing properties
        boolean found = false;
        for (int i = 0; i < options.length; i++) {
            String option = options[i].trim();
            if (option.startsWith(prefix)) {
                options[i] = newValue;
                found = true;
                break;
            }
        }
        if (found) {
            return true;
        }

        // Not found so add a new property to the list
        String[] newOptions = new String[options.length + 1];
        System.arraycopy(options, 0, newOptions, 0, options.length);
        newOptions[options.length] = newValue;
        options = newOptions;
        return true;
    }

    private boolean removeProxy() {
        boolean changed = removeProperty(HTTP_PROXY_HOST);
        changed |= removeProperty(HTTP_PROXY_PORT);
        changed |= removeProperty(HTTPS_PROXY_HOST);
        changed |= removeProperty(HTTPS_PROXY_PORT);
        changed |= removeProperty(HTTP_PROXY_NO_HOST);
        return changed;
    }

    /**
     * @return true iff HTTP proxy settings were changed on app server
     */
    public void addHttpProxySettings() throws Exception {
        // Get the current IDE proxy settings
        String host = System.getProperty("http.proxyHost", "");
        if (host.trim().length() == 0) {
            host = null;
        }
        String port = System.getProperty("http.proxyPort", "");
        if (port.trim().length() == 0) {
            port = null;
        }
        
        String nonHosts = System.getProperty("http.nonProxyHosts", ""); //NOI18N
        if (nonHosts.trim().length() != 0) { 
            // remove any spaces -Dhttp.nonProxyHosts= localhost| localhost.czech.sun.com
            nonHosts = nonHosts.replaceAll(" ", ""); //NOI18N
        } else {
            nonHosts = null;
        }

        if(this.serverRunning){
            try{
                getOptionsFromServer();
                if (host == null || port == null || nonHosts == null) {
                    removeOptionsFromServer();
                    return;
                }
            }catch(Exception ex){
                throw new Exception(bundle.getString("Err_CannotUpdateProxy"));
            }
            checkProxyInfo(host, port);
            if (updateOptions(host, port, nonHosts)) {
                try{
                    setOptionsToServer();
                }catch(Exception ex){
                    String message = MessageFormat.format(bundle.getString("Err_InvalidProxyInfo"), new Object[]{host, port});
                    throw new Exception(message);
                }
            } // end of if (changed)
        }else{
            //Manipulate domain.xml since server is stopped
            getOptionsFromXml();
            if (host == null || port == null || nonHosts == null) {
                removeOptionsFromXml();
                return;
            }
            checkProxyInfo(host, port);
            if (updateOptions(host, port, nonHosts)) {
                setOptionsToXml();
            } // end of if (changed)
        }
    }
    
     /*
     * Remove Proxy setting from the appserver if present
     *
     */
    public void removeHttpProxySettings() throws Exception {
        if(this.serverRunning){
            getOptionsFromServer();
            removeOptionsFromServer();
        }else{
            getOptionsFromXml();
            removeOptionsFromXml();
        }
    }
    
    private void removeOptionsFromXml() {
        if (httpProxyHost != null || httpProxyPort != null || httpProxyNoHost != null ||
                httpsProxyHost != null || httpsProxyPort != null) {
            if (removeProxy()) {
                setOptionsToXml();
            }
        }
    }
    
    private void removeOptionsFromServer() throws Exception {
        try{
            if (httpProxyHost != null || httpProxyPort != null || httpProxyNoHost != null ||
                    httpsProxyHost != null || httpsProxyPort != null) {
                if (removeProxy()) {
                    try{
                        setOptionsToServer();
                    }catch(Exception ex){
                        //Should not come here
                    }
                }
            }
        }catch(Exception ex){
            //Should not come here
        }
    }

    private boolean updateOptions(String host, String port, String nonHosts) {
        boolean changed = false;
        
        if (!host.equals(httpProxyHost)) {
            setProperty(HTTP_PROXY_HOST, host);
            changed = true;
        } // end of if (!host.equals(httpProxyHost))
        
        if (!port.equals(httpProxyPort)) {
            setProperty(HTTP_PROXY_PORT, port);
            changed = true;
        } // end of if (!port.equals(httpProxyPort))
        
        if (!host.equals(httpsProxyHost)) {
            setProperty(HTTPS_PROXY_HOST, host);
            changed = true;
        } // end of if (!host.equals(httpsProxyHost))
        
        if (!port.equals(httpsProxyPort)) {
            setProperty(HTTPS_PROXY_PORT, port);
            changed = true;
        } // end of if (!port.equals(httpsProxyPort))
        
        SunDeploymentManagerInterface sdm = (SunDeploymentManagerInterface)si.getDeploymentManager();
        if(ServerLocationManager.isGlassFish(sdm.getPlatformRoot())){
            if (!nonHosts.equals(httpProxyNoHost)) {
                setProperty(HTTP_PROXY_NO_HOST, nonHosts);
                changed = true;
            } // end of if (!nonHosts.equals(httpProxyNoHost))
        }
        
        return changed;
    }
    
    private void initializeProps(){
        // Extract the HTTP proxy properties
        for (int i = 0; i < options.length; i++) {
            String option = options[i].trim();
            if (option.startsWith(HTTP_PROXY_HOST)) {
                httpProxyHost = option.substring(HTTP_PROXY_HOST.length());
            } else if (option.startsWith(HTTP_PROXY_PORT)) {
                httpProxyPort = option.substring(HTTP_PROXY_PORT.length());
            } else if (option.startsWith(HTTPS_PROXY_HOST)) {
                httpsProxyHost = option.substring(HTTPS_PROXY_HOST.length());
            } else if (option.startsWith(HTTPS_PROXY_PORT)) {
                httpsProxyPort = option.substring(HTTPS_PROXY_PORT.length());
            } else if (option.startsWith(HTTP_PROXY_NO_HOST)) {
                httpProxyNoHost = option.substring(HTTP_PROXY_NO_HOST.length());
            }    
        }
    }
    
    private void checkProxyInfo(String host, String port) throws Exception {
        try{
            int portNo = Integer.parseInt(port);
            Socket socket = new Socket(host, portNo);
            return;
        }catch(Exception ex){
            String message = MessageFormat.format(bundle.getString("Err_InvalidProxyInfo"), new Object[]{host, port});
            throw new Exception(message);
        }
    }
}
