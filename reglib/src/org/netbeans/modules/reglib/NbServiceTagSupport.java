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

import com.sun.servicetag.RegistrationData;
import com.sun.servicetag.ServiceTag;
import com.sun.servicetag.Registry;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marek Slama
 * 
 */
public class NbServiceTagSupport {
    
    private static String NB_CLUSTER;
    
    private static String NB_VERSION;
    
    private static String GF_VERSION;
    
    private static final String USER_HOME = System.getProperty("user.home");
    
    private static final String USER_DIR = System.getProperty("netbeans.user");
    
    private static final String ST_DIR = "servicetag";
    
    private static final String ST_FILE = "servicetag";
    
    private static final String REG_FILE = "registration.xml";
    
    /** Dir in home dir */
    private static File svcTagDirHome;
    
    /** Dir in install dir */
    private static File svcTagDirNb;
    
    private static File serviceTagFileHome;
    
    private static File serviceTagFileNb;
    
    /** Cluster dir */
    private static File nbClusterDir;
    
    /** Install dir */
    private static File nbInstallDir;
    
    /** File in home dir */
    private static File regXmlFileHome;
    
    /** File in install dir */
    private static File regXmlFileNb;
    
    private static RegistrationData registration;
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.reglib.NbServiceTagSupport"); // NOI18N
    
    private static File registerHtmlParent;
    
    private static Set<Locale> supportedLocales = new HashSet<Locale>();
    
    private final static String REGISTRATION_HTML_NAME = "register";

    private final static Locale[] knownSupportedLocales = 
        new Locale[] { Locale.ENGLISH,
                       Locale.JAPANESE,
                       Locale.SIMPLIFIED_CHINESE};
    
    private static boolean inited = false;
    
    private static void init () {
        LOG.log(Level.FINE,"Initializing");
        Util.loadProperties();
        NB_CLUSTER = Util.getProperty("nb.cluster");    
        NB_VERSION = Util.getProperty("servicetag.nb.version");    
        GF_VERSION = Util.getProperty("servicetag.gf.version");
        
        //This return platfomX dir but we need install dir
        File f = new File(System.getProperty("netbeans.home"));
        
        nbInstallDir = f.getParentFile();
        LOG.log(Level.FINE,"NetBeans install dir is:" + nbInstallDir);
        
        nbClusterDir = new File(nbInstallDir,NB_CLUSTER);
        LOG.log(Level.FINE,"nb cluster dir is:" + nbClusterDir);
        
        svcTagDirNb = new File(nbClusterDir.getPath() + File.separator + ST_DIR);
        svcTagDirHome = new File(USER_HOME + File.separator + ".netbeans-registration"
        + File.separator + NB_VERSION);
        if (nbClusterDir.canWrite() && (!svcTagDirNb.exists())) {
            svcTagDirNb.mkdirs();
        }
        if (!svcTagDirHome.exists()) {
            svcTagDirHome.mkdirs();
        }
        
        regXmlFileNb = new File(svcTagDirNb,REG_FILE);
        regXmlFileHome = new File(svcTagDirHome,REG_FILE);
        
        serviceTagFileNb = new File(svcTagDirNb,ST_FILE);
        serviceTagFileHome = new File(svcTagDirHome,ST_FILE);
        
        inited = true;
    }

    /** 
     * First look in registration data if NetBeans service tag exists.
     * If not then create new service tag.
     * @return service tag instance for NetBeans
     * @throws java.io.IOException
     */
    public static ServiceTag createNbServiceTag (String source) throws IOException {
        if (!inited) {
            init();
        }
        LOG.log(Level.FINE,"Creating NetBeans service tag");
        
        ServiceTag st = getNbServiceTag();
        if (st != null) {
            if ((serviceTagFileNb.exists() || serviceTagFileHome.exists())) {
                LOG.log(Level.FINE,
                "NetBeans service tag is already created and saved in registration.xml");
                return st;
            } else {
                LOG.log(Level.FINE,"NetBeans service tag is already created");
            }
        }
        
        // New service tag entry if not created
        if (st == null) {
            LOG.log(Level.FINE,"Creating new service tag");
            st = newNbServiceTag(source);
            // Add the service tag to the registration data in NB
            getRegistrationData().addServiceTag(st);
            writeRegistrationXml();
        }
        
        // Install a system service tag if supported
        if (Registry.isSupported()) {
            LOG.log(Level.FINE,"Add service tag to system registry");
            installSystemServiceTag(st);
        } else {
            LOG.log(Level.FINE,"Cannot add service tag to system registry as ST infrastructure is not found");
        }
        return st;
    }
    
    /**
     * First look in registration data if GlassFish tag exists.
     * If not then create new service tag.
     * @return service tag instance for GlassFish
     * @throws java.io.IOException
     */
    public static ServiceTag createGfServiceTag (String source) throws IOException {
        if (!inited) {
            init();
        }
        LOG.log(Level.FINE,"Creating GlassFish service tag");
        
        ServiceTag st = getGfServiceTag();
        if (st != null) {
            if ((serviceTagFileNb.exists() || serviceTagFileHome.exists())) {
                LOG.log(Level.FINE,
                "GlassFish service tag is already created and saved in registration.xml");
                return st;
            } else {
                LOG.log(Level.FINE,"GlassFish service tag is already created");
            }
        }
        
        // New service tag entry if not created
        if (st == null) {
            LOG.log(Level.FINE,"Creating new GlassFish service tag");
            st = newGfServiceTag(source);
            // Add the service tag to the registration data in NB
            getRegistrationData().addServiceTag(st);
            writeRegistrationXml();
        }
        
        return st;
    }
    
    /**
     * Used to transfer service tag from GF to NB.
     * First look in registration data if GlassFish tag exists and is
     * different then replace it by passed parameter.
     * @return service tag instance for GlassFish
     * @throws java.io.IOException
     */
    public static ServiceTag createGfServiceTag (ServiceTag serviceTag) throws IOException {
        if (!inited) {
            init();
        }
        LOG.log(Level.FINE,"Creating GlassFish service tag");
        
        ServiceTag st = getGfServiceTag();
        if (st != null) {
            //If GF service tag already exists replace it with passed instance
            if (!st.equals(serviceTag)) {
                //First remove existing
                getRegistrationData().removeServiceTag(st.getInstanceURN());
                getRegistrationData().addServiceTag(serviceTag);
                writeRegistrationXml();
            }
        } else {
            getRegistrationData().addServiceTag(serviceTag);
            writeRegistrationXml();
        }
        return serviceTag;
    }
    
    /**
     * First look in registration data if JDK tag exists.
     * If not then create new service tag. This method must be called from correct JDK.
     * @return service tag instance for JDK
     * @throws java.io.IOException
     */
    public static ServiceTag createJdkServiceTag (String source) throws IOException {
        if (!inited) {
            init();
        }
        LOG.log(Level.FINE,"Creating JDK service tag");
        
        ServiceTag st = getJdkServiceTag();
        if (st != null) {
            if ((serviceTagFileNb.exists() || serviceTagFileHome.exists())) {
                LOG.log(Level.FINE,
                "JDK service tag is already created and saved in registration.xml");
                return st;
            } else {
                LOG.log(Level.FINE,"JDK service tag is already created");
            }
        }
        
        // New service tag entry if not created
        if (st == null) {
            LOG.log(Level.FINE,"Creating new JDK service tag");
            st = ServiceTag.getJavaServiceTag(source);
            // Add the service tag to the registration data in NB
            getRegistrationData().addServiceTag(st);
            writeRegistrationXml();
        }
        
        //Do not save JDK service tag to system registry as call ServiceTag.getJavaServiceTag(source)
        //handles it.
        return st;
    }
    
    /**
     * Write the registration data to the registration.xml file
     * @throws java.io.IOException
     */
    private static void writeRegistrationXml() throws IOException {
        File targetFile = null;
        if (nbClusterDir.canWrite()) {
            targetFile = regXmlFileNb;
        } else {
            targetFile = regXmlFileHome;
        }
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(targetFile));
            getRegistrationData().storeToXML(out);
        } catch (IOException ex) {
            LOG.log(Level.INFO,
            "Error: Cannot save registration data to \"" + targetFile + "\":" + ex.getMessage());
            throw ex;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    /**
     * Returns the NetBeans registration data located in
     * the NB_INST_DIR/nb6.0/servicetag/registration.xml by default. 
     *
     * @throws IllegalArgumentException if the registration data
     *         is of invalid format.
     */
    public static RegistrationData getRegistrationData () throws IOException {
        if (!inited) {
            init();
        }
        if (registration != null) {
            return registration;
        }
        
        File srcFile = null;
        if (regXmlFileNb.exists()) {
            srcFile = regXmlFileNb;
            LOG.log(Level.FINE,"Service tag will be loaded from NB install dir: " + srcFile);
        } else if (regXmlFileHome.exists()) {
            srcFile = regXmlFileHome;
            LOG.log(Level.FINE,"Service tag will be loaded from user home dir: " + srcFile);
        } else {
            registration = new RegistrationData();
            LOG.log(Level.FINE,"Service tag file not found");
            return registration;
        }
        
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(srcFile));
            registration = RegistrationData.loadFromXML(in);
        } catch (IOException ex) {
            LOG.log(Level.INFO,"Error: Bad registration data \"" +
            srcFile + "\":" + ex.getMessage());
            throw ex;
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return registration;
    }

    /**
     * Create new service tag instance for NetBeans
     * @param svcTagSource
     * @return
     * @throws java.io.IOException
     */
    private static ServiceTag newNbServiceTag (String svcTagSource) throws IOException {
        // Determine the product URN and name
        String productURN, productName, parentURN, parentName;

        productURN = Util.getProperty("servicetag.nb.urn");
        productName = Util.getProperty("servicetag.nb.name");
        
        parentURN = Util.getProperty("servicetag.nb.parent.urn");
        parentName = Util.getProperty("servicetag.nb.parent.name");

        return ServiceTag.newInstance(ServiceTag.generateInstanceURN(),
                                      productName,
                                      NB_VERSION,
                                      productURN,
                                      parentName,
                                      parentURN,
                                      getNbProductDefinedId(),
                                      "NetBeans.org",
                                      System.getProperty("os.arch"),
                                      getZoneName(),
                                      svcTagSource);
    }
    
    /**
     * Create new service tag instance for GlassFish
     * @param svcTagSource
     * @return
     * @throws java.io.IOException
     */
    private static ServiceTag newGfServiceTag (String svcTagSource) throws IOException {
        // Determine the product URN and name
        String productURN, productName, parentURN, parentName;

        productURN = Util.getProperty("servicetag.gf.urn");
        productName = Util.getProperty("servicetag.gf.name");
        
        parentURN = Util.getProperty("servicetag.gf.parent.urn");
        parentName = Util.getProperty("servicetag.gf.parent.name");

        return ServiceTag.newInstance(ServiceTag.generateInstanceURN(),
                                      productName,
                                      GF_VERSION,
                                      productURN,
                                      parentName,
                                      parentURN,
                                      getGfProductDefinedId(),
                                      "Sun Microsystems Inc",
                                      System.getProperty("os.arch"),
                                      getZoneName(),
                                      svcTagSource);
    }
    
    /**
     * Return the NetBeans service tag from local registration data.
     * Return null if srevice tag is not found.
     * 
     * @return a service tag for 
     */
    private static ServiceTag getNbServiceTag () throws IOException {
        String productURN = Util.getProperty("servicetag.nb.urn");
        RegistrationData regData = getRegistrationData();
        Collection<ServiceTag> svcTags = regData.getServiceTags();
        for (ServiceTag st : svcTags) {
            if (productURN.equals(st.getProductURN())) {
                return st;
            }
        }
        return null;
    }
    
    /**
     * Return the GlassFish service tag from local registration data.
     * Return null if service tag is not found.
     * 
     * @return a service tag for 
     */
    private static ServiceTag getGfServiceTag () throws IOException {
        String productURN = Util.getProperty("servicetag.gf.urn");
        RegistrationData regData = getRegistrationData();
        Collection<ServiceTag> svcTags = regData.getServiceTags();
        for (ServiceTag st : svcTags) {
            if (productURN.equals(st.getProductURN())) {
                return st;
            }
        }
        return null;
    }
    
    /**
     * Return the JDK service tag from local registration data.
     * Return null if service tag is not found.
     * 
     * @return a service tag for 
     */
    private static ServiceTag getJdkServiceTag () throws IOException {
        String productURN = Util.getProperty("servicetag.jdk.urn");
        RegistrationData regData = getRegistrationData();
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
    private static String getNbProductDefinedId() {
        StringBuilder definedId = new StringBuilder();
        definedId.append("id=");
        definedId.append(NB_VERSION);
        
        String location = ",dir=" + nbInstallDir.getPath();
        if ((definedId.length() + location.length()) < 256) {
            definedId.append(",dir=");
            definedId.append(nbInstallDir.getPath());
        } else {
            // if it exceeds the limit, we will not include the location
            LOG.log(Level.INFO, "Warning: Product defined instance ID exceeds the field limit:");
        }

        return definedId.toString();
    }
    
    /**
     * Returns the product defined instance ID for GlassFish.
     * It is a list of comma-separated name/value pairs.
     *
     * Example: "os.name=Linux;os.version=2.6.22-14-generic;java.version=1.5.0_14;glassfish.home=/home/mslama/glassfish;java.home=/usr/java/jdk1.5.0_14/jre"
     * 
     * Caller (installer) must make sure that system property glassfish.home is set.
     *
     */
    private static String getGfProductDefinedId() {
        StringBuilder definedId = new StringBuilder();
        
        definedId.append("os.name=");
        definedId.append(System.getProperty("os.name"));
        
        definedId.append(",os.version=");
        definedId.append(System.getProperty("os.version"));
        
        definedId.append(",java.version=");
        definedId.append(System.getProperty("java.version"));
        
        definedId.append(",glassfish.home=");
        definedId.append(System.getProperty("glassfish.home"));
        
        definedId.append(",java.home=");
        definedId.append(System.getProperty("java.home"));
        
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
     * or empty string if file not exists.
     */
    private static String getInstalledURN() throws IOException {
        if (serviceTagFileNb.exists() || serviceTagFileHome.exists()) {
            File srcFile = null;
            if (serviceTagFileNb.exists()) {
                srcFile = serviceTagFileNb;
            } else if (serviceTagFileHome.exists()) {
                srcFile = serviceTagFileHome;
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(srcFile));
                String urn = in.readLine().trim();
                return urn;
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
        return "";
    }
    
    private static void installSystemServiceTag(ServiceTag st) throws IOException {
        if (getInstalledURN().length() > 0) {
            // Already installed
            LOG.log(Level.INFO, "ST is already installed ie. we have file servicetag.");
            return;
        }

        if (Registry.isSupported()) {
            // Install in the system ST registry
            Registry.getSystemRegistry().addServiceTag(st);
        }
        File targetFile;
        if (nbClusterDir.canWrite()) {
            targetFile = regXmlFileNb;
        } else {
            targetFile = regXmlFileHome;
        }

        // Write the instance_run to the servicetag file
        String instanceURN = st.getInstanceURN();
        BufferedWriter out = null;
        try {
            LOG.log(Level.FINE,"Creating file: " + targetFile);
            out = new BufferedWriter(new FileWriter(targetFile));
            out.write(instanceURN);
            out.newLine();
        } finally {
            if (out != null) {
                out.close();
            }
        }
        //For NB 6.0 save file 'servicetag' to user dir to avoid creating new ST
        //by code in IDE launcher
        if ("6.0".equals(NB_VERSION)) {
            targetFile = new File(USER_DIR + File.separator + ST_FILE);
            try {
                LOG.log(Level.FINE,"Creating file: " + targetFile + " Specific for 6.0.");
                out = new BufferedWriter(new FileWriter(targetFile));
                out.write(instanceURN);
                out.newLine();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }
    
    private static File getRegisterHtmlParent() {
        if (registerHtmlParent == null) {
            // initialize the supported locales
            initSupportedLocales(nbInstallDir);

            // Determine the location of the offline registration page
            registerHtmlParent = svcTagDirHome;
        }
        return registerHtmlParent;
    }
    
    /** This should be called after method init is invoked. */
    public static File getServiceTagDirHome () {
        if (!inited) {
            init();
        }
        return svcTagDirHome;
    }
    
    /**
     * Returns the File object of the offline registration page localized
     * for the default locale in the $HOME/.netbeans-registration/$NB_VERSION.
     */
    public static File getRegistrationHtmlPage(String product) throws IOException {
        if (!inited) {
            init();
        }

        File parent = getRegisterHtmlParent(); 

        // check if the offline registration page is already generated
        File f = new File(parent, REGISTRATION_HTML_NAME + ".html");
        if (!f.exists()) {
            // Generate the localized version of the offline registration Page
            generateRegisterHtml(parent,product);
        }

        String name = REGISTRATION_HTML_NAME;
        Locale locale = Locale.getDefault();
        if (supportedLocales.contains(locale)) {
            if (!locale.equals(Locale.ENGLISH)) {
                name = REGISTRATION_HTML_NAME + "_" + locale.toString();
            }
        }
        File htmlFile = new File(parent, name + ".html");
        if (f.exists()) {
            return htmlFile;
        } else {
            return new File(parent,
                            REGISTRATION_HTML_NAME + ".html");
        }
    }
    
    // Remove the offline registration pages 
    private static void deleteRegistrationHtmlPage() {
        File parent = getRegisterHtmlParent(); 
        if (parent == null) {
            return;
        }
        
        for (Locale locale : supportedLocales) {
            String name = REGISTRATION_HTML_NAME;
            if (!locale.equals(Locale.ENGLISH)) {
                name += "_" + locale.toString();
            }
            File f = new File(parent, name + ".html");
            if (f.exists()) {
                f.delete();
            }
        }
    }
    
    private static void initSupportedLocales(File nbDir) {
        if (supportedLocales.isEmpty()) {
            // initialize with the known supported locales
            for (Locale l : knownSupportedLocales) {
                supportedLocales.add(l);
            }
        }

        // Determine unknown supported locales if any
        // by finding the localized version of README.html
        // This prepares if a new locale in JDK is supported in
        // e.g. in the OpenSource world
        FilenameFilter ff = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String fname = name.toLowerCase();
                if (fname.startsWith("readme") && fname.endsWith(".html")) { 
                    return true;
                }
                return false;
            }
        };

        String[] readmes = svcTagDirHome.list(ff);
        for (String name : readmes) {
            String basename = name.substring(0, name.length() - ".html".length()); 
            String[] ss = basename.split("_");
            switch (ss.length) {
                case 1:
                    // English version
                    break;
                case 2:
                    supportedLocales.add(new Locale(ss[1]));
                    break;
                case 3:
                    supportedLocales.add(new Locale(ss[1], ss[2]));
                    break;
                default:
                    // ignore 
                    break;
            }
        }
        LOG.log(Level.FINE,"Supported locales: ");
        for (Locale l : supportedLocales) {
            LOG.log(Level.FINE,l.toString());
        }
    }            
    
    private static final String NB_HEADER_PNG_KEY = "@@NB_HEADER_PNG@@";
    private static final String NB_VERSION_KEY = "@@NB_VERSION@@";
    private static final String REGISTRATION_URL_KEY = "@@REGISTRATION_URL@@";
    private static final String REGISTRATION_PAYLOAD_KEY = "@@REGISTRATION_PAYLOAD@@";

    @SuppressWarnings("unchecked")
    private static void generateRegisterHtml(File parent, String product) throws IOException {
        RegistrationData regData = getRegistrationData();
        String registerURL = NbConnectionSupport.getRegistrationURL(
            regData.getRegistrationURN(), product).toString();
        
        //Extract image from jar
        String resource = "/org/netbeans/modules/reglib/resources/nb_header.png";
        File img = new File(svcTagDirHome, "nb_header.png");
        String headerImageSrc = img.getCanonicalPath();       
        InputStream in = NbServiceTagSupport.class.getResourceAsStream(resource);
        if (in == null) {
            // if the resource file is missing
            LOG.log(Level.FINE,"Missing resource file: " + resource);
        } else {
            LOG.log(Level.FINE,"Generating " + img + " from " + resource);
            BufferedInputStream bis = new BufferedInputStream(in);
            FileOutputStream fos = new FileOutputStream(img);
            try {
                int c;
                while ((c = bis.read()) != -1) {
                    fos.write(c);
                }
            } finally {
                if (bis != null) {
                    bis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
        }
        // Format the registration data in one single line
        String xml = regData.toString();
        String lineSep = System.getProperty("line.separator");
        String payload = xml.replaceAll("\"", "%22").replaceAll(lineSep, " ");

        String resourceFilename = "/org/netbeans/modules/reglib/resources/register";
        for (Locale locale : supportedLocales) {
            String name = REGISTRATION_HTML_NAME;
            resource = resourceFilename;
            if (!locale.equals(Locale.ENGLISH)) {
                name += "_" + locale.toString();
                resource += "_" + locale.toString();
            }
            File f = new File(parent, name + ".html");
            in = NbServiceTagSupport.class.getResourceAsStream(resource + ".html");
            if (in == null) {
                // if the resource file is missing
                LOG.log(Level.FINE,"Missing resource file: " + resource);
                continue;
            }
            LOG.log(Level.FINE,"Generating " + f + " from " + resource);

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter pw = new PrintWriter(f);
            String line = null;
            while ((line = reader.readLine()) != null) {
                String output = line;
                if (line.contains(NB_VERSION_KEY)) {
                    output = line.replace(NB_VERSION_KEY, NB_VERSION);
                } else if (line.contains(NB_HEADER_PNG_KEY)) {
                    output = line.replace(NB_HEADER_PNG_KEY, headerImageSrc);
                } else if (line.contains(REGISTRATION_URL_KEY)) {
                    output = line.replace(REGISTRATION_URL_KEY, registerURL);
                } else if (line.contains(REGISTRATION_PAYLOAD_KEY)) {
                    output = line.replace(REGISTRATION_PAYLOAD_KEY, payload);
                }
                pw.println(output);
            }
            pw.flush();
            pw.close();
            in.close();
        }
    }
    
}
