// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
//</editor-fold>

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import org.netbeans.modules.glassfish.eecommon.api.Utils;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.ManagerNode;
import org.netbeans.modules.reglib.BrowserSupport;
import org.netbeans.modules.reglib.NbConnectionSupport;
import org.netbeans.modules.servicetag.RegistrationData;
import org.netbeans.modules.servicetag.ServiceTag;
import org.openide.awt.HtmlBrowser;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Action that can always be invoked and work procedurally.
 * This action will send registration data to the Registration Relay service.
 * The service resonds with a web page that associates a sun dev network ID with
 * the registration data.
 *
 * @author  vkraemer
 */
public class RegisterAction extends CookieAction {

    protected Class[] cookieClasses() {
        return new Class[]{};
    }

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    protected void performAction(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return;
        }
        ManagerNode managerNode = nodes[0].getLookup().lookup(ManagerNode.class);
        if (managerNode != null) {
            InputStream in = null;
            try {
                File root = managerNode.getDeploymentManager().getPlatformRoot();
                File serviceTagFile = new File(root,
                        "lib" + File.separator + "registration" + File.separator + "servicetag-registry.xml"); // NOI18N

                RepositoryReader rr = new RepositoryReader(serviceTagFile);
                List<RepositoryServiceTag> sts = rr.getRepositoryServiceTags();
                RegistrationData regData = new RegistrationData();
                for (RepositoryServiceTag st : sts) {
                    // create real ServiceTag data from the evolved data that GF
                    // has in it's ServiceTag.
                    ServiceTag stst = ServiceTag.newInstance(
                            st.getInstanceURN(), st.getProductName(), st.getProductVersion(),
                            st.getProductURN(), st.getProductParent(), st.getProductParentURN(),
                            st.getProductDefinedInstID(), st.getProductVendor(), st.getPlatformArch(),
                            st.getContainer(), st.getSource());

                    regData.addServiceTag(stst);
                }

                // using nb product ID
                register(regData, "nb", serviceTagFile); // NOI18N

            // TODO: handle these exceptions better.
            } catch (ParserConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (TransformerConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            } catch (FileNotFoundException fnfe) {
                Exceptions.printStackTrace(fnfe); 
            } catch (IOException ioe) {
                // if there was some other problem....
                Exceptions.printStackTrace(ioe); 
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }
    }

    public String getName() {
        return NbBundle.getMessage(RegisterAction.class, "LBL_RegisterAction"); // NOI18N
    }

    // TODO: find/get a better gif/png for this.
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/j2ee/sun/ide/resources/jms.gif";  // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean enable(Node[] nodes) {
        return (nodes != null) && (nodes.length == 1); // true;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.j2ee.sun.ide.runtime.actions.RegisterAction"); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor("GlassFish Registration RP");  // NOI18N

    /**
     * This method updates local GF registration status
     * at [gf.home]/lib/registration/servicetag-registry.xml from 'NOT REGISTERED'  to 'REGISTERED'.
     *
     * @param regFile the file to change.
     *
     */
    private static void updateGFRegistrationStatus(File regFile) {
        //It checks both existence and write access
        if (!regFile.canWrite()) {
            return;
        }
        LOG.log(Level.INFO, "Update file: " + regFile); 
        InputStream in = null;
        PrintWriter pw = null;
        try {
            in = new FileInputStream(regFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            File f = File.createTempFile("nbt", null, regFile.getParentFile());
            pw = new PrintWriter(f, "UTF-8");
            String line = null;
            while ((line = reader.readLine()) != null) {
                String output = line;
                if (line.contains("NOT_REGISTERED")) {
                    output = line.replace("NOT_REGISTERED", "REGISTERED");
                } else if (line.contains("ASK_FOR_REGISTRATION")) {
                    output = line.replace("ASK_FOR_REGISTRATION", "DONT_ASK_FOR_REGISTRATION");
                }
                pw.println(output);
            }
            pw.flush();
            regFile.delete();
            boolean ret = f.renameTo(regFile);
            LOG.log(Level.FINE, "Did rename succeed: " + ret);
        } catch (IOException exc) {
            LOG.log(Level.INFO, "Cannot update: " + regFile, exc);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
            if (null != pw) {
                pw.close();
            }
        }
    }

    /**
     * Registers all products in the given product registry.  If it fails
     * to post the service tag registry, open the browser with the offline
     * registration page.
     *
     * @param regData registration data to be posted to the Sun Connection
     *             for registration.
     * @param product the product identifier that the Registration Relay Services
     *             "knows"
     * @param regFile the file with the GF servicetag registry... usually named
     *             servicetag-registry.xml
     */
    static void register(final RegistrationData regData, final String product, final File regFile) {
        RP.post(new Runnable() {

            public void run() {
                // Gets the URL for SunConnection registration relay service
                LOG.log(Level.FINE, "Product registration");
                URL url = NbConnectionSupport.getRegistrationURL(regData.getRegistrationURN(), product);

                // Post the Product Registry to Sun Connection
                LOG.log(Level.FINE, "POST registration data to:" + url);
                boolean succeed = NbConnectionSupport.postRegistrationData(url, regData);
                if (succeed) {
                    // service tags posted successfully
                    // now prompt for registration
                    LOG.log(Level.FINE, "Open browser with:" + url);
                    try {
                        updateGFRegistrationStatus(regFile);
                        openBrowser(url);
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, "Error: Cannot open browser", ex);
                    }
                } else {
                    // TODO: what happens if the registration fails?
                    // open browser with the offline registration page
//                    try {
//                        openOfflineRegisterPage(product);
//                    } catch (IOException ex) {
//                        LOG.log(Level.INFO, "Error: Cannot open browser", ex);
//                    }
                }
            }
        });
    }

    /**
     * Opens a browser for product registration.
     * @param url Registration Webapp URL
     */
    private static void openBrowser(URL url) throws IOException {
        if (BrowserSupport.isSupported()) {
            try {
                BrowserSupport.browse(url.toURI());
            } catch (URISyntaxException ex) {
                InternalError x = new InternalError("Error in registering: " + ex.getMessage()); // NOI18N
                x.initCause(ex);
                throw x;
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.FINE, "Cannot open browser:", ex); // NOI18N
            } catch (UnsupportedOperationException ex) {
                // ignore if not supported
                LOG.log(Level.FINE, "Cannot open browser:", ex); // NOI18N
            }
        } else {
            //Fallback to openide API in JDK 5
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        }
    }

    // TODO: do we need this (or something similar?)
    /**
     * Opens the offline registratioin page in the browser.
     *
     */
//    private static void openOfflineRegisterPage (String product)
//            throws IOException {
//        File registerPage =
//        NbServiceTagSupport.getRegistrationHtmlPage
//        (product, new String [] {NbServiceTagSupport.getProductName()});
//        if (BrowserSupport.isSupported()) {
//            try {
//                BrowserSupport.browse(registerPage.toURI());
//            } catch (FileNotFoundException ex) {
//                // should never reach here
//                InternalError x =
//                    new InternalError("Error in launching " + registerPage + ": " + ex.getMessage());
//                x.initCause(ex);
//                throw x;
//            } catch (IllegalArgumentException ex) {
//                LOG.log(Level.FINE,"Cannot open browser:",ex);
//            } catch (UnsupportedOperationException ex) {
//                // ignore if not supported
//                LOG.log(Level.FINE,"Cannot open browser:",ex);
//            }
//        } else {
//            //Fallback to openide API in JDK 5
//            HtmlBrowser.URLDisplayer.getDefault().showURL(registerPage.toURI().toURL());
//        }
//    }

    // this code is adapted from
    // http://fisheye4.atlassian.com/browse/glassfish-svn/trunk/v3/registration/registration-impl/src/main/java/com/sun/enterprise/registration/impl/RepositoryManager.java
    //
    static class RepositoryReader {

        /**
         * Creates a new manager which persists the registration data in the specified File.
         * @throws RegistrationException for any errors creating the XML parser or
         * transformer used for reading and writing the registration data or during
         * the initial load of the repository from the local file
         */
        public RepositoryReader(File registrationFile) throws
                ParserConfigurationException,
                TransformerConfigurationException,
                SAXException,
                IOException {
            this.registrationFile = registrationFile;
            try {
                LOG.fine("RepositoryReader created for file " + registrationFile.getCanonicalPath());
            } catch (IOException ioe) {
                // ignoring this because it should not be fatal
            }
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            loadOrCreateDocument();
        }

        /**
         * Returns a list of all service tag objects represented in the repository.
         * @return List<ServiceTag>
         * the in-memory cache (if it is not already there)
         */
        public List<RepositoryServiceTag> getRepositoryServiceTags() {
            List<RepositoryServiceTag> serviceTags = new ArrayList<RepositoryServiceTag>();
            if (document != null) {
                NodeList nodes = document.getElementsByTagName(RepositoryServiceTag.SERVICE_TAG);

                /* nodes is guaranteed to be non-null */
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element elem = (Element) nodes.item(i);
                    serviceTags.add(new RepositoryServiceTag(elem));
                }
            }
            return serviceTags;
        }

        public String getInstanceURN(String productURN) { //throws RegistrationException {
            NodeList nodes = document.getElementsByTagName(RepositoryServiceTag.SERVICE_TAG);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element elem = (Element) nodes.item(i);
                String productURN1 = getSubElementValue(elem, RepositoryServiceTag.PRODUCT_URN);
                if (productURN.equals(productURN1)) {
                    return getSubElementValue(elem, RepositoryServiceTag.INSTANCE_URN);
                }
            }
            return null;
        }

        private String getSubElementValue(Element rootElement, String subElementName) {
            NodeList nodes = rootElement.getElementsByTagName(subElementName);
            if (nodes.getLength() > 0) {
                return ((Element) nodes.item(0)).getTextContent();
            }
            return null;
        }

        /**
         * Initializes the cached Document for the repository.
         * <p>
         * If the specified file exists, reads the document from that file.  If the
         * file does not exist, creates a new document and populates it with the
         * top-level registry element and the default registrationstatus element.
         * @throws RegistrationException for errors reading the registration into
         * the in-memory cache
         */
        private synchronized void loadOrCreateDocument() throws SAXException, IOException {
            if (document == null) {
                if (registrationFile.exists()) {
                    document = documentBuilder.parse(registrationFile);
                    registryElement = findRegistryElement();
                }
            }
        }

        /**
         * Locates the top-level element, the registry element.
         * @return the top-level registry element
         */
        private Element findRegistryElement() {
            Element result = null;
            NodeList nodes = document.getElementsByTagName(REGISTRY_TAG);
            if (nodes.getLength() > 0) {
                result = (Element) nodes.item(0);
            }
            return result;
        }
        /** element name for the registrationstatus element */
        private static final String REGISTRATION_REMINDER_TAG = "registration_reminder";
        /** element name for the top-level registry element */
        private static final String REGISTRY_TAG = "registry";
        /*
         * Doc builder factory, doc builder, transformer factory, and transformer
         * are all reusable so get them once.
         */
        private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        private DocumentBuilder documentBuilder;
        private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        private Transformer transformer;
        /** the file to write the registration data to and read it from */
        private File registrationFile = null;
        /** the cached in-memory data */
        private Document document = null;
        /** the cached top-level registry element */
        private Element registryElement = null;
    }

    // this code is adapted from
    // http://fisheye4.atlassian.com/browse/glassfish-svn/trunk/v3/registration/registration-impl/src/main/java/com/sun/enterprise/registration/impl/ServiceTag.java
    //
    static class RepositoryServiceTag {

        private Element tagData;
        private String instanceURN;
        private String productName;
        private String productVersion;
        private String productURN;
        private String productParentURN;
        private String productDefinedInstID;
        private String productVendor;
        private String container;
        private String source;

        public RepositoryServiceTag(Element tagData) {
            instanceURN = getValue(tagData, INSTANCE_URN);
            this.tagData = tagData;
            if (instanceURN == null) {
                instanceURN = "urn:st:" + UUID.randomUUID().toString();   // NOI18N
            }
            productName = getValue(tagData, PRODUCT_NAME);
            productVersion = getValue(tagData, PRODUCT_VERSION);
            productURN = getValue(tagData, PRODUCT_URN);
            productParentURN = getValue(tagData, PRODUCT_PARENT_URN);
            productParent = getValue(tagData, PRODUCT_PARENT);
            productDefinedInstID = getValue(tagData, PRODUCT_DEFINED_INST_ID);
            platformArch = getValue(tagData, PLATFORM_ARCH);
            productVendor = getValue(tagData, PRODUCT_VENDOR);
            container = getValue(tagData, CONTAINER);
            source = getValue(tagData, SOURCE);
        }

        private String getValue(Element rootElement, String subElementName) {
            NodeList nodes = rootElement.getElementsByTagName(subElementName);
            if (nodes.getLength() > 0) {
                return ((Element) nodes.item(0)).getTextContent();
            }
            return null;
        }

        public String getInstanceURN() {
            return Utils.truncate(instanceURN, MAX_URN_LEN);
        }

        public String getProductName() {
            return Utils.truncate(productName, MAX_PRODUCT_NAME_LEN);
        }

        public String getProductURN() {
            return Utils.truncate(productURN, MAX_URN_LEN);
        }

        public String getProductVersion() {
            return Utils.truncate(productVersion, MAX_PRODUCT_VERSION_LEN);
        }

        public String getProductParentURN() {
            return Utils.truncate(productParentURN, MAX_PRODUCT_PARENT_LEN);
        }

        public String getProductDefinedInstID() {
            return Utils.truncate(productDefinedInstID, MAX_URN_LEN);
        }

        public String getContainer() {
            return Utils.truncate(container, MAX_CONTAINER_LEN);
         }

        public String getSource() {
            return Utils.truncate(source, MAX_SOURCE_LEN);
        }

        public String getProductVendor() {
            return Utils.truncate(productVendor, MAX_PRODUCT_VENDOR_LEN);
        }

        public String getPlatformArch() {
            if (platformArch == null || platformArch.length() == 0) {
                platformArch = System.getProperty("os.arch");
            }
            return Utils.truncate(platformArch, MAX_PLATFORM_ARCH_LEN);
        }

        public String getProductParent() {
            return Utils.truncate(productParent, MAX_URN_LEN);
        }


        // these are not in svcTag but are required by stclient.
        private String platformArch,  productParent;
        public static final String PRODUCT_NAME = "product_name";
        public static final String PRODUCT_VERSION = "product_version";
        public static final String PRODUCT_URN = "product_urn";
        public static final String PRODUCT_PARENT_URN = "product_parent_urn";
        public static final String PRODUCT_PARENT = "product_parent";
        public static final String PRODUCT_DEFINED_INST_ID = "product_defined_inst_id";
        public static final String PLATFORM_ARCH = "platform_arch";
        public static final String CONTAINER = "container";
        public static final String SOURCE = "source";
        public static final String INSTANCE_URN = "instance_urn";
        public static final String PRODUCT_VENDOR = "product_vendor";
        public static final String STATUS = "status";
        public static final String REGISTRATION_STATUS = "registration_status";
        public static final String SERVICE_TAG = "service_tag";
        
        // Service Tag Field Lengths (defined in sthelper.h)
        // Since the constants defined in sthelper.h includes the null-terminated
        // character, so minus 1 from the sthelper.h defined values.
        private final int MAX_URN_LEN = 256 - 1;
        private final int MAX_PRODUCT_NAME_LEN = 256 - 1;
        private final int MAX_PRODUCT_VERSION_LEN = 64 - 1;
        private final int MAX_PRODUCT_PARENT_LEN = 256 - 1;
        private final int MAX_PRODUCT_VENDOR_LEN = 64 - 1;
        private final int MAX_PLATFORM_ARCH_LEN = 64 - 1;
        private final int MAX_CONTAINER_LEN = 64 - 1;
        private final int MAX_SOURCE_LEN = 64 - 1;
    }
}
