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

package org.netbeans.modules.registration;

import com.sun.servicetag.RegistrationData;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.SwingUtilities;
import org.openide.awt.HtmlBrowser;
import org.openide.util.SharedClassObject;
import org.openide.windows.WindowManager;

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
class NbConnection {
    
    private static String NB_REGISTRATION_URL =
       "https://inventory.sun.com/RegistrationWeb/register";
    private static String SANDBOX_TESTING_URL =
       "https://connection-tst.sun.com/RegistrationWeb/register";
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.registration.NbConnection"); // NOI18N
    
    private static final String STATUS_FILE = "status.xml";
    
    private static StatusData status = new StatusData(StatusData.STATUS_UNKNOWN);
    
    private NbConnection() {
    }
    
    static void init () {
        //As we need this code for NB 5.5 we cannot use new Winsys API method
        //WindowManager.invokeWhenUIReady(Runnable). Here we use old way how to
        //perform something after opening of main window
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final Frame mainWindow = WindowManager.getDefault().getMainWindow();
                mainWindow.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentShown(ComponentEvent evt) {
                        mainWindow.removeComponentListener(this);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                checkStatus();
                            }
                        });
                    }
                });
            }
        });
    }
    
    private static void checkStatus () {
        LOG.log(Level.FINE,"checkStatus");
        File dir = NbInstaller.getServiceTagDirHome();
        File statusFile = new File(dir,STATUS_FILE);
        if (statusFile.exists()) {
            //Status file exists, check its content
            BufferedInputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(statusFile));
                status = StatusData.loadFromXML(in);
            } catch (IOException ex) {
                LOG.log(Level.INFO,"Error: Bad registration data \"" +
                statusFile + "\"",ex);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        LOG.log(Level.INFO,"Error: Cannot close reader",ex);
                    }
                }
            }
            if (status.getStatus().equals(StatusData.STATUS_REGISTERED)) {
                LOG.log(Level.FINE,"Status is STATUS_REGISTERED");
            } else if (status.getStatus().equals(StatusData.STATUS_NEVER)) {
                LOG.log(Level.FINE,"Status is STATUS_NEVER");
            } else if (status.getStatus().equals(StatusData.STATUS_LATER)) {
                LOG.log(Level.FINE,"Status is STATUS_LATER");
                //Check current date
                if (status.getTimestamp().getTime() + (15L * 24L * 60L * 60L * 1000L) >= System.currentTimeMillis()) {
                    //Time is over, ask again
                    RegisterAction a = SharedClassObject.findObject(RegisterAction.class, true);
                    a.showDialog();
                }
            } else {
                LOG.log(Level.FINE,"Status is unknown");
                //Status is unknown, ask user
                RegisterAction a = SharedClassObject.findObject(RegisterAction.class, true);
                a.showDialog();
            }
        } else {
            //Status file does not exist so directly show dialog
            RegisterAction a = SharedClassObject.findObject(RegisterAction.class, true);
            a.showDialog();
        }
    }
    
    /** This method updates registration status. It saves user selection.
     * If user selects Register registration is started
     * @param value User choice in reminder dialog
     */
    static void updateStatus (String value) {
        LOG.log(Level.FINE,"updateStatus status:" + value);
        //Ignore null value ie. do not change status if null is passed
        if (value != null) {
            status.setStatus(value);
        }
        File dir = NbInstaller.getServiceTagDirHome();
        File statusFile = new File(dir,STATUS_FILE);
        
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(statusFile));
            status.storeToXML(out);
        } catch (IOException ex) {
            LOG.log(Level.INFO,
            "Error: Cannot save status data to \"" + statusFile, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    LOG.log(Level.INFO,
                    "Error: Cannot close writer", ex);
                }
            }
        }
        if (StatusData.STATUS_REGISTERED.equals(status.getStatus())) {
            try {
                NbConnection.register(NbInstaller.getRegistrationData());
            } catch (IOException ex) {
                LOG.log(Level.INFO,
                "Error: Cannot register product", ex);
            }
        }
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
    static URL getRegistrationURL(String registrationURN) {
        String url;
        url = SANDBOX_TESTING_URL;
        //url = NB_REGISTRATION_URL;

        // trim whitespaces 
        url = url.trim(); 
        if (url.length() == 0) {
            throw new InternalError("Empty registration url set");
        }

        // Add the registry_urn in the URL's query
        String registerURL = rewriteURL(url, registrationURN);
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
    
    private static String rewriteURL(String url, String registryURN) {
        StringBuilder sb = new StringBuilder(url.trim());
        int len = sb.length();
        if (sb.charAt(len-1) != '/') {
            sb.append('/');
        }
        sb.append(registryURN);
        sb.append("?");
        sb.append("product=jdk");
        sb.append("&");
        sb.append("locale=").append(Locale.getDefault().getLanguage());
        return sb.toString();
    }

    /**
     * Registers all products in the given product registry.  If it fails
     * to post the service tag registry, open the browser with the offline 
     * registration page.
     * 
     * @param regData registration data to be posted to the Sun Connection
     *             for registration.
     *
     * @throws IOException if I/O error occurs in this operation
     */
    static void register(RegistrationData regData) throws IOException {
        // Gets the URL for SunConnection registration relay service
        LOG.log(Level.FINE,"Product registration");
        URL url = getRegistrationURL(regData.getRegistrationURN());

        // Post the Product Registry to Sun Connection
        boolean succeed = postRegistrationData(url, regData);
        if (succeed) {
            // service tags posted successfully
            // now prompt for registration
            openBrowser(url);
        } else {
            // open browser with the offline registration page
            openOfflineRegisterPage();
        }
    }

    /**
     * Opens a browser for JDK product registration.
     * @param url Registration Webapp URL
     */
    private static void openBrowser(URL url) throws IOException {
        if (BrowserSupport.isSupported()) {
            try {
                BrowserSupport.browse(url.toURI());
            } catch (URISyntaxException ex) {
                InternalError x = new InternalError("Error in registering: " + ex.getMessage());
                x.initCause(ex);
                throw x;
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.FINE,"Cannot open browser:",ex);
            } catch (UnsupportedOperationException ex) {
                // ignore if not supported
                LOG.log(Level.FINE,"Cannot open browser:",ex);
            }
        } else {
            //Fallback to openide API in JDK 5
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        }
    }

    /**
     * POST service tag registry to Sun Connection
     * @param loc the URL of the webapp to handle the POST request
     * @param streg the Service Tag registry
     * @return true if posting succeeds; otherwise, false.
     */
    private static boolean postRegistrationData(URL url, 
                                                RegistrationData registration) {
        try {
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setAllowUserInteraction(false);
            con.setRequestMethod("POST");

            con.setRequestProperty("Content-Type", "text/xml;charset=\"utf-8\"");
            con.connect();
            
            LOG.log(Level.FINE,"POSTing registration data at " + url);
            OutputStream out = con.getOutputStream();
            registration.storeToXML(out);
            LOG.log(Level.FINE,"Registration data: " + registration.toString());
            out.flush();
            out.close();

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

    /**
     * Opens the offline registratioin page in the browser.
     * 
     */
    private static void openOfflineRegisterPage()
            throws IOException {
        File registerPage = NbInstaller.getRegistrationHtmlPage();
        if (BrowserSupport.isSupported()) {
            try {
                BrowserSupport.browse(registerPage.toURI());
            } catch (FileNotFoundException ex) {
                // should never reach here
                InternalError x = 
                    new InternalError("Error in launching " + registerPage + ": " + ex.getMessage());
                x.initCause(ex);
                throw x;
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.FINE,"Cannot open browser:",ex);
            } catch (UnsupportedOperationException ex) {
                // ignore if not supported
                LOG.log(Level.FINE,"Cannot open browser:",ex);
            }
        } else {
            //Fallback to openide API in JDK 5
            HtmlBrowser.URLDisplayer.getDefault().showURL(registerPage.toURI().toURL());
        }
    }

    private static void printReturnData(HttpURLConnection con, int returnCode)
            throws IOException {
        BufferedReader reader = null;
        try {
            if (returnCode < 400) {
                reader = new BufferedReader(
                             new InputStreamReader(con.getInputStream()));
            } else {
                reader = new BufferedReader(
                             new InputStreamReader(con.getErrorStream()));
            }
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
