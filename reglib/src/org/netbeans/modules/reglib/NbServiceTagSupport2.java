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
import org.netbeans.modules.servicetag.ServiceTag;
import org.netbeans.modules.servicetag.Registry;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Used to handle registration data for standalone application like JavaFX SDK standalone
 * installer.
 *
 * @author Marek Slama
 * 
 */
public class NbServiceTagSupport2 {
    
    private static final String ST_DIR = "servicetag";
    
    private static final String ST_FILE = "servicetag";
    
    private static final String REG_FILE = "registration.xml";
    
    /** Registration data for NB/installer use */
    private static RegistrationData registration;
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.reglib.NbServiceTagSupport2"); // NOI18N
    
    private final static String REGISTRATION_HTML_NAME = "register";
    
    /** Returns NetBeans IDE product name. */
    public static String getProductName () {
        return NbBundle.getMessage(NbServiceTagSupport2.class,"servicetag.javafxsdk.name");
    }

    /**
     * First look in registration data if JavaFX SDK Standalone service tag exists.
     * If not then create new service tag.
     *
     * @param source client who creates service tag eg.: "NetBeans IDE 6.0.1 Installer"
     * or "NetBeans IDE 6.0.1"
     * @param javaVersion IDE will provides java version on which IDE is running ie. value of system
     * property java.version. Installer will provide java version selected to run IDE
     * @param rootDir location of registration.xml it will be $rootDir/servicetag
     * @return service tag instance for JavaFX SDK
     * @throws java.io.IOException
     */
    public static ServiceTag createJavaFXSdkServiceTag (String source, String javaVersion, String rootDir) throws IOException {
        LOG.log(Level.FINE,"Creating JavaFX SDK Standalone service tag");

        ServiceTag st = getJavaFXSdkServiceTag(rootDir);
        // New service tag entry if not created
        if (st == null) {
            LOG.log(Level.FINE,"Creating new service tag");
            st = newJavaFXSdkServiceTag(source, javaVersion, rootDir);
            // Add the service tag to the registration data in NB
            getRegistrationData(rootDir).addServiceTag(st);
            writeRegistrationXml(rootDir);
        }

        // Install a system service tag if supported
        if (Registry.isSupported()) {
            LOG.log(Level.FINE,"Add service tag to system registry");
            installSystemServiceTag(st,rootDir);
        } else {
            LOG.log(Level.FINE,"Cannot add service tag to system registry as ST infrastructure is not found");
        }
        return st;
    }
    
    /**
     * Write the registration data to $rootDir/servicetag/registration.xml file
     *
     * @param rootDir location where servicetag/registration.xml is stored
     *
     * @throws java.io.IOException
     */
    private static void writeRegistrationXml (String rootDir) throws IOException {
        File targetFile = new File(rootDir + File.separator + ST_DIR + File.separator + REG_FILE);
        File targetDir = new File(rootDir + File.separator + ST_DIR);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        
        try {
            OutputStream os = new FileOutputStream(targetFile);
            try {
                BufferedOutputStream out = new BufferedOutputStream(os);
                getRegistrationData(rootDir).storeToXML(out);
                out.close();
            } finally {
                os.close();
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO,
            "Error: Cannot save registration data to \"" + targetFile + "\":" + ex.getMessage());
            throw ex;
        }
    }
    
    /**
     * Returns registration data located in $rootDir/servicetag/registration.xml
     *
     * @param rootDir root directory where registration data are located
     * 
     * @throws IllegalArgumentException if the registration data
     *         is of invalid format.
     */
    public static RegistrationData getRegistrationData (String rootDir) throws IOException {
        if (registration != null) {
            return registration;
        }

        File srcFile = new File(rootDir + File.separator + ST_DIR + File.separator + REG_FILE);
        if (srcFile.exists()) {
            LOG.log(Level.FINE,"Service tag will be loaded from: " + srcFile);
        } else {
            registration = new RegistrationData();
            LOG.log(Level.FINE,"Service tag file not found");
            return registration;
        }

        try {
            InputStream is = new FileInputStream(srcFile);
            try {
                BufferedInputStream in = new BufferedInputStream(is);
                registration = RegistrationData.loadFromXML(in);
                in.close();
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO,"Error: Bad registration data \"" +
            srcFile + "\":" + ex.getMessage());
            throw ex;
        }
        return registration;
    }


    /**
     * Create new service tag instance for JavaFX SDK Standalone
     * @param svcTagSource
     * @return
     * @throws java.io.IOException
     */
    private static ServiceTag newJavaFXSdkServiceTag
    (String svcTagSource, String javaVersion, String rootDir) throws IOException {
        // Determine the product URN and name
        String productURN, productName, productVersion, parentURN, parentName;

        productURN = NbBundle.getMessage(NbServiceTagSupport2.class,"servicetag.javafxsdk.urn");
        productName = NbBundle.getMessage(NbServiceTagSupport2.class,"servicetag.javafxsdk.name");

        productVersion = NbBundle.getMessage(NbServiceTagSupport2.class,"servicetag.javafxsdk.version");

        parentURN = NbBundle.getMessage(NbServiceTagSupport2.class,"servicetag.javafxsdk.parent.urn");
        parentName = NbBundle.getMessage(NbServiceTagSupport2.class,"servicetag.javafxsdk.parent.name");

        return ServiceTag.newInstance(ServiceTag.generateInstanceURN(),
                                      productName,
                                      productVersion,
                                      productURN,
                                      parentName,
                                      parentURN,
                                      getNbProductDefinedId(javaVersion, productVersion, rootDir),
                                      "Sun Microsystems",
                                      System.getProperty("os.arch"),
                                      getZoneName(),
                                      svcTagSource);
    }

    /**
     * Return the JavaFX SDK Standalone service tag from local registration data.
     * Return null if service tag is not found.
     *
     * @return a service tag for
     */
    private static ServiceTag getJavaFXSdkServiceTag (String rootDir) throws IOException {
        String productURN = NbBundle.getMessage(NbServiceTagSupport2.class,"servicetag.javafxsdk.urn");
        RegistrationData regData = getRegistrationData(rootDir);
        Collection<ServiceTag> svcTags = regData.getServiceTags();
        for (ServiceTag st : svcTags) {
            if (productURN.equals(st.getProductURN())) {
                return st;
            }
        }
        return null;
    }
    
    /**
     * Returns the product defined instance ID for NetBeans IDE.
     * It is a list of comma-separated name/value pairs:
     *    "id=<full-version>"
     *    "dir=<NetBeans install dir>"
     *
     * where <full-version> is the full version string of the NetBeans IDE,
     *
     * Example: "id=6.0,dir=/home/mslama/netbeans-6.0"
     * 
     * The "dir" property is included in the service tag to enable
     * the Service Tag software to determine if a service tag for 
     * NetBeans IDE is invalid and perform appropriate service tag
     * cleanup if necessary.  See RFE# 6574781 Service Tags Enhancement. 
     *
     */
    private static String getNbProductDefinedId (String javaVersion, String productVersion, String rooDir) {
        StringBuilder definedId = new StringBuilder();
        definedId.append("id=");
        definedId.append(productVersion);

        definedId.append(",java.version=");
        definedId.append(javaVersion);

        String location = ",dir=" + rooDir;
        if ((definedId.length() + location.length()) < 256) {
            definedId.append(location);
        } else {
            // if it exceeds the limit, we will not include the location
            LOG.log(Level.INFO, "Warning: Product defined instance ID exceeds the field limit:");
        }
        
        return definedId.toString();
    }
    
    /**
     * Return the zonename if zone is supported; otherwise, return
     * "global".
     */
    private static String getZoneName() throws IOException {
        String zonename = "global";

        String command = "/usr/bin/zonename";
        File f = new File(command);
        // com.sun.servicetag package has to be compiled with JDK 5 as well
        // JDK 5 doesn't support the File.canExecute() method.
        // Risk not checking isExecute() for the zonename command is very low.
        if (f.exists()) {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();
            String output = Util.commandOutput(p);
            if (p.exitValue() == 0) {
                zonename = output.trim();
            }

        }
        return zonename;
    }
    
    /**
     * Returns the instance urn stored in the servicetag file
     * or empty string if file does not exists.
     */
    private static String getInstalledURN (String urn, String rootDir) throws IOException {
        File srcFile = new File(rootDir + File.separator + ST_DIR + File.separator + ST_FILE);
        if (srcFile.exists()) {
            Reader r = new FileReader(srcFile);
            try {
                BufferedReader in = new BufferedReader(r);
                String line = in.readLine();
                while (line != null) {
                    if (urn.equals(line.trim())) {
                        return urn;
                    }
                    line = in.readLine();
                }
                in.close();
                return "";
            } finally {
                r.close();
            }
        }
        return "";
    }
    
    private static void installSystemServiceTag (ServiceTag st, String rootDir) throws IOException {
        if (getInstalledURN(st.getInstanceURN(),rootDir).length() > 0) {
            // Already installed
            LOG.log(Level.INFO, "ST is already installed ie. we have file servicetag.");
            return;
        }

        File targetFile = new File(rootDir + File.separator + ST_DIR + File.separator + ST_FILE);
        
        if (Registry.isSupported()) {
            //Check if given service tag is already installed in system registry
            if ((Registry.getSystemRegistry().getServiceTag(st.getInstanceURN()) != null)) {
                LOG.log(Level.FINE,"Service tag: " + st.getInstanceURN() 
                + " is already installed in system registry.");
                return;
            }
            //Install in the system ST registry
            Registry.getSystemRegistry().addServiceTag(st);

            // Write (append if any presents) the instance_run to the servicetag file            
            Writer w = new FileWriter(targetFile, true);
            try {
                LOG.log(Level.FINE,"Creating file: " + targetFile);
                BufferedWriter out = new BufferedWriter(w);
                out.write(st.getInstanceURN());
                out.newLine();
                out.close();
            } finally {
                w.close();
            }
        }
    }
    
    /**
     * Returns the File object of the offline registration page localized
     * for the default locale in $rootDir/servicetag.
     */
    public static File getRegistrationHtmlPage(String product, String [] productNames, String rootDir) throws IOException {

        File parent = new File(rootDir + File.separator + ST_DIR);

        File f = new File(parent, REGISTRATION_HTML_NAME + ".html");
        // Generate the localized version of the offline registration Page
        generateRegisterHtml(parent,product,productNames,rootDir);

        return f;
    }
    
    // Remove the offline registration pages 
    private static void deleteRegistrationHtmlPage (String rootDir) {
        File parent = new File(rootDir + File.separator + ST_DIR);
        
        String name = REGISTRATION_HTML_NAME;
        File f = new File(parent, name + ".html");
        if (f.exists()) {
            f.delete();
        }
    }
    
    private static final String NB_HEADER_PNG_KEY = "@@NB_HEADER_PNG@@";
    private static final String PRODUCT_KEY = "@@PRODUCT@@";
    private static final String PRODUCT_TITLE_KEY = "@@PRODUCT_TITLE@@";
    private static final String REGISTRATION_URL_KEY = "@@REGISTRATION_URL@@";
    private static final String REGISTRATION_PAYLOAD_KEY = "@@REGISTRATION_PAYLOAD@@";

    @SuppressWarnings("unchecked")
    private static void generateRegisterHtml
    (File parent, String product, String [] productNames, String rootDir) throws IOException {
        RegistrationData regData = getRegistrationData(rootDir);
        String registerURL = NbConnectionSupport.getRegistrationURL(
            regData.getRegistrationURN(), product).toString();
        
        //Extract image from jar
        String resource = "/org/netbeans/modules/reglib/resources/nb_header.png";
        File img = new File(rootDir + File.separator + ST_DIR + File.separator + "nb_header.png");
        String headerImageSrc = img.toURI().toURL().toString();       
        InputStream in = NbServiceTagSupport2.class.getResourceAsStream(resource);
        if (in == null) {
            // if the resource file is missing
            LOG.log(Level.FINE,"Missing resource file: " + resource);
        } else {
            try {
                LOG.log(Level.FINE,"Generating " + img + " from " + resource);
                BufferedInputStream bis = null;
                FileOutputStream fos = null;
                try {
                    bis = new BufferedInputStream(in);
                    fos = new FileOutputStream(img);
                    int c;
                    while ((c = bis.read()) != -1) {
                        fos.write(c);
                    }
                } finally {
                    IOException exc = null;
                    try {
                        if (bis != null) {
                            bis.close();
                        }
                    } catch (IOException ex) {
                        exc = ex;
                    }
                    if (fos != null) {
                        fos.close();
                    }
                    if (exc != null) {
                        throw exc;
                    }
                }
            } finally {
                in.close();
            }
        }
        // Format the registration data in one single line
        String xml = regData.toString();
        String lineSep = System.getProperty("line.separator");
        String payload = xml.replaceAll("\"", "%22").replaceAll(lineSep, " ");

        String name = REGISTRATION_HTML_NAME;
        File f = new File(parent, name + ".html");
        
        in = null;
        Locale l = Locale.getDefault();
        Locale [] locales = new Locale[] {
          new Locale(l.getLanguage(), l.getCountry(), l.getVariant()),
          new Locale(l.getLanguage(), l.getCountry()),
          new Locale(l.getLanguage()),
          new Locale("")
        };
        for (Locale locale : locales) {
           resource = "/org/netbeans/modules/reglib/resources/register" + (locale.toString().equals("") ? "" : ("_" + locale)) + ".html";
           LOG.log(Level.FINE,"Looking for html in: " + resource);
           in = NbServiceTagSupport2.class.getResourceAsStream(resource);
           if (in != null) {
               break;
           }
        }
        if (in != null) {
            try {
                LOG.log(Level.FINE,"Found html in: " + resource);
                LOG.log(Level.FINE,"Generating " + f);

                BufferedReader reader = null;
                PrintWriter pw = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
                    pw = new PrintWriter(f,"UTF-8");
                    String line = null;
                    String productName = "", productNameTitle = "";
                    for (int i = 0; i < productNames.length; i++) {
                        if (i > 0) {
                            productName +=
                            " " + NbBundle.getMessage(NbServiceTagSupport2.class,"MSG_junction") + " ";
                            productNameTitle +=
                            " " + NbBundle.getMessage(NbServiceTagSupport2.class,"MSG_junction") + " ";
                        }
                        productName += "<strong>" + productNames[i] + "</strong>";
                        productNameTitle += productNames[i];
                    }
                    while ((line = reader.readLine()) != null) {
                        String output = line;
                        if (line.contains(PRODUCT_KEY)) {
                            output = line.replace(PRODUCT_KEY, productName);
                        } else if (line.contains(PRODUCT_TITLE_KEY)) {
                            output = line.replace(PRODUCT_TITLE_KEY, productNameTitle);
                        } else if (line.contains(NB_HEADER_PNG_KEY)) {
                            output = line.replace(NB_HEADER_PNG_KEY, headerImageSrc);
                        } else if (line.contains(REGISTRATION_URL_KEY)) {
                            output = line.replace(REGISTRATION_URL_KEY, registerURL);
                        } else if (line.contains(REGISTRATION_PAYLOAD_KEY)) {
                            output = line.replace(REGISTRATION_PAYLOAD_KEY, payload);
                        }
                        pw.println(output);
                    }
                } finally {
                    //PrintWriter.close does not throw IOException so no need to catch it here
                    //to perform next close
                    if (pw != null) {
                        pw.close();
                    }
                    if (reader != null) {
                        reader.close();
                    }
                }
            } finally {
                in.close();
            }
        }
    }
    
}
