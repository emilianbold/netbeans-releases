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

package org.netbeans.modules.reglib;

import org.netbeans.modules.servicetag.RegistrationData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;

/**
 * NetBeans Connection Class for Product Registration.
 *
 * Registration Web Application Interface
 * 1) POST the product registry to the output stream of the registration
 *    relay service.
 * 2) Open the webapp URL from a browser with the following parameters:
 *    registry-urn
 *    product=jdk
 *    locale=<locale-lang>
 *
 * @see https://sn-tools.central.sun.com/twiki/pub/ServiceTags/RegistrationRelayService/
 * 
 */
public class NbConnectionSupport {
    
    private static String NB_REGISTRATION_HOST = "https://inventory.sun.com";
    private static String SANDBOX_TESTING_HOST = "https://connection-tst.sun.com";
    //private static String SANDBOX_TESTING_HOST = "http://devtest2-fe.central.sun.com";
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.reglib.NbConnectionSupport"); // NOI18N
    
    private NbConnectionSupport() {
    }
        
    /**
     * Returns a URL for JDK registration interfacing with the Sun Connection
     * registration relay service in this form:
     *   <registration-url>/<registry_urn>?product=jdk&locale=<locale-lang>
     *
     * The <registration-url> can be overridden by an environment 
     * variable or a system property.
     *
     * 1) "servicetag.register.testing" system property to switch to the
     *    Sun Connection registration sandbox testing.
     * 2) "servicetag.registration.url" system property to override 
     *    the URL
     * 3) Default production URL
     *
     */
    public static URL getRegistrationURL(String registrationURN, String product) {
        String url = System.getProperty("nb.registration.host");
        if (url == null) {
            url = NB_REGISTRATION_HOST + "/RegistrationWeb/register";
        } else {
            url += "/RegistrationWeb/register";
        }

        // trim whitespaces 
        url = url.trim(); 
        if (url.length() == 0) {
            throw new InternalError("Empty registration url set");
        }

        // Add the registry_urn in the URL's query
        String registerURL = rewriteURL(url, registrationURN, product);
        try {
            return new URL(registerURL);
        } catch (MalformedURLException ex) {
            // should never reach here
            InternalError x = 
                new InternalError(ex.getMessage());
            x.initCause(ex);
            throw x;               
        }
    }
    
    public static String getRegistrationQueryHost () {
        String url = System.getProperty("nb.registration.host");
        if (url == null) {
            url = NB_REGISTRATION_HOST;
        }

        // trim whitespaces 
        url = url.trim(); 
        if (url.length() == 0) {
            throw new InternalError("Empty registration url set");
        }
        return url;
    }
    
    private static String rewriteURL(String url, String registryURN, String product) {
        StringBuilder sb = new StringBuilder(url.trim());
        int len = sb.length();
        if (sb.charAt(len-1) != '/') {
            sb.append('/');
        }
        sb.append(registryURN);
        sb.append("?");
        sb.append("product=" + product);
        sb.append("&");
        sb.append("locale=").append(Locale.getDefault().getLanguage());
        return sb.toString();
    }
    
    /**
     * POST service tag registry to Sun Connection
     * @param loc the URL of the webapp to handle the POST request
     * @param streg the Service Tag registry
     * @return true if posting succeeds; otherwise, false.
     */
    public static boolean postRegistrationData(URL url, 
                                                RegistrationData registration) {
        try {
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            //HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setAllowUserInteraction(false);
            con.setRequestMethod("POST");

            con.setRequestProperty("Content-Type", "text/xml;charset=\"utf-8\"");
            con.connect();
            
            LOG.log(Level.FINE,"POSTing registration data at " + url);
            OutputStream out = con.getOutputStream();
            try {
                registration.storeToXML(out);
                LOG.log(Level.FINE,"Registration data: " + registration.toString());
                out.flush();
            } finally {
                out.close();
            }

            int returnCode = con.getResponseCode();
            LOG.log(Level.FINE,"POST return status = " + returnCode);
            printReturnData(con, returnCode);
            return (returnCode == HttpURLConnection.HTTP_OK);
        } catch (MalformedURLException me) {
            // should never reach here
            InternalError x = new InternalError("Error in registering: " + me.getMessage());
            x.initCause(me);
            throw x;
        } catch (Exception ioe) {
            // IOException and UnknownHostException
            LOG.log(Level.FINE,"Post registration data failed:",ioe);
            return false;
        }
    }
    
    private static void printReturnData(HttpURLConnection con, int returnCode) throws IOException {
        InputStream is = null;
        if (returnCode < 400) {
            is = con.getInputStream();
        } else {
            is = con.getErrorStream();
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
        } finally {
            is.close();
        }
    }
    
    /** Query web service if given product by instance_urn is registered.
     * Returns false only when we have 'NOT REGISTERED' response from server.
     * Otherwise return true to avoid false not registered status.
     */
    public static boolean isRegistered (String host, String uuid) {
        try {
            URL url = new URL(
                host
                + "/ProductRegistrationService/status/"
                + uuid);
            LOG.log(Level.FINE,"Query URL: " + url);
            //HttpURLConnection con = (HttpURLConnection) (url.openConnection());
            HttpsURLConnection con = (HttpsURLConnection) (url.openConnection());
            
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setAllowUserInteraction(false);
            con.setUseCaches(false);

            con.connect();
            int responseCode = con.getResponseCode();

            LOG.log(Level.FINE,"Response code = " + responseCode);
            if (responseCode == 200) {
                InputStream is = con.getInputStream();
                StringBuffer sb = new StringBuffer();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                    while (true) {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        sb.append(line);
                    }

                    reader.close();
                } finally {
                    is.close();
                }
                String response = sb.toString();
                
                LOG.log(Level.FINE,"Response: " + response);
                
                // the response should be equal to 'REGISTERED' or 'NOT REGISTERED'
                if (response.equals("NOT REGISTERED")) {
                    return false;
                } else {
                    return true;
                }
            } else if (responseCode == 404) {
                // response code of 404 is not found, which means not registered
                return true;
            } else {
                // unknown response code
                return true;
            }
        } catch (Exception ex) {
            LOG.log(Level.FINE,"Error: " + ex.getMessage(), ex);
        }
        return true;
    }
    
    /** Query web service if given product by instance_urn is registered.
     * Returns true only when we have 'REGISTERED' response from server.
     * Otherwise return false.
     */
    public static boolean isRegistered2 (String host, String uuid) {
        try {
            URL url = new URL(
                host
                + "/ProductRegistrationService/status/"
                + uuid);
            LOG.log(Level.FINE,"Query URL: " + url);
            //HttpURLConnection con = (HttpURLConnection) (url.openConnection());
            HttpsURLConnection con = (HttpsURLConnection) (url.openConnection());
            
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setAllowUserInteraction(false);
            con.setUseCaches(false);

            con.connect();
            int responseCode = con.getResponseCode();

            LOG.log(Level.FINE,"Response code = " + responseCode);
            if (responseCode == 200) {
                InputStream is = con.getInputStream();
                StringBuffer sb = new StringBuffer();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                    while (true) {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        sb.append(line);
                    }

                    reader.close();
                } finally {
                    is.close();
                }
                String response = sb.toString();
                
                LOG.log(Level.FINE,"Response: " + response);
                
                // the response should be equal to 'REGISTERED' or 'NOT REGISTERED'
                if (response.equals("REGISTERED")) {
                    return true;
                } else {
                    return false;
                }
            } else if (responseCode == 404) {
                // response code of 404 is not found, which means not registered
                return false;
            } else {
                // unknown response code
                return false;
            }
        } catch (Exception ex) {
            LOG.log(Level.FINE,"Error: " + ex.getMessage(), ex);
        }
        return false;
    }
}
