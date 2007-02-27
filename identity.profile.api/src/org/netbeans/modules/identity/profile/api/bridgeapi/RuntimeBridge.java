/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.identity.profile.api.bridgeapi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.ErrorManager;

/**
 * Bridge class for the appserver and AM server runtime.
 *
 * Created on April 5, 2006, 10:50 AM
 *
 * @author Vidhya Narayanan
 */
public class RuntimeBridge {
    private static String amStatusCheckURL =
            "/amserver/isAlive.jsp"; //NOI18N
    
    private RuntimeBridge() {
    }
    
    public static boolean isAppServerSun(J2eeModuleProvider provider) {
        if (provider != null) {
            if (provider.getInstanceProperties() != null) {
                return provider.getInstanceProperties().getProperty(
                        InstanceProperties.URL_ATTR).contains("Sun:AppServer"); //NOI18N
            }
        }
        return false;
    }
    
    public static boolean isAMRunning(J2eeModuleProvider provider) {
        boolean status = false;
        if (provider != null) {
            String port = provider.getInstanceProperties().getProperty(
                    InstanceProperties.HTTP_PORT_NUMBER);
            String host = "localhost"; //Need to obtain this from InstanceProperties //NOI18N
            String protocol = "http"; // Need to obtain this from InstanceProperties //NOI18N
            if(port == null || port.equals("")) { //NOI18N
                port = "8080"; //NOI18N
            }
            URL amurl = null;
            try {
                amurl = new URL(protocol+"://"+host+":"+port+amStatusCheckURL); //NOI18N
                
                HttpURLConnection conn = (HttpURLConnection)amurl.openConnection();
                if (conn != null) {
                    int resCode = conn.getResponseCode();
                    if (resCode != -1 && resCode == 200)
                        status = true;
                    conn.disconnect();
                }
            } catch (MalformedURLException mexcp) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new Exception(mexcp.getMessage()));
            }  catch (IOException io) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new Exception(io.getMessage()));
            }
        }
        
        return status;
    }
    
}
