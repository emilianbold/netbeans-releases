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
// </editor-fold>

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport;
import org.netbeans.modules.j2ee.sun.api.Asenv;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

// TODO finish migration towards being an abstract class
import org.openide.util.lookup.Lookups;
/**
 */
public class PlatformImpl extends J2eePlatformImpl {
    
    private static final Set<J2eeModule.Type> MODULE_TYPES = new HashSet<J2eeModule.Type>();
    protected static final Set/*<String>*/ SPEC_VERSIONS = new HashSet();
    private static final Set/*<String>*/ SPEC_VERSIONS_WITH_5 = new HashSet();
    // Appserver version strings.
//    private static final String APPSERVER_VERSION_9_1 = "9.1"; // NOI18N
//    private static final String APPSERVER_VERSION_9 = "9.0"; // NOI18N
//    private static final String APPSERVER_VERSION_8_1 = "8.1"; // NOI18N
//    private static final String APPSERVER_VERSION_8_2 = "8.2"; // NOI18N
//    private static final String APPSERVER_VERSION_UNKNOWN = "unknown"; // NOI18N
//    private  String version = APPSERVER_VERSION_UNKNOWN;	// NOI18N
    private static final String J2EE_14_JAR = "lib/j2ee.jar"; //NOI18N
    private static final String JAVA_EE_JAR = "lib/javaee.jar"; //NOI18N
    private static final String JSF_API_JAR = "lib/jsf-api.jar"; //NOI18N
    private static final String JSF_IMPL_JAR = "lib/jsf-impl.jar"; //NOI18N
    private static final String JAX_QNAME_JAR = "lib/jax-qname.jar"; //NOI18N
    private static final String JAXRPC_API_JAR = "lib/jaxrpc-api.jar"; //NOI18N
    private static final String JAXRPC_IMPL_JAR = "lib/jaxrpc-impl.jar"; //NOI18N
    private static final String JAXR_API_JAR = "lib/jaxr-api.jar"; //NOI18N
    private static final String JAXR_IMPL_JAR = "lib/jaxr-impl.jar"; //NOI18N
    private static final String SAAJ_API_JAR = "lib/saaj-api.jar"; //NOI18N
    private static final String SAAJ_IMPL_JAR = "lib/saaj-impl.jar"; //NOI18N
    
    private static final String ACTIVATION_JAR = "lib/activation.jar"; //NOI18N
    private static final String TAGS_JAR = "lib/appserv-tags.jar"; //NOI18N
    private static final String MAIL_JAR =  "lib/mail.jar"; //NOI18N
    private static final String JSTL_JAR =  "lib/appserv-jstl.jar"; //NOI18N
    
    // appserver jars
    private static final String APPSERV_WS_JAR = "lib/appserv-ws.jar"; //NOI18N
    private static final String TOOLS_JAR = "lib/tools.jar"; //NOI18N
    
    // jwsdp jars
    private static final String JWSDP_JAR = "lib/appserv-ws-update.jar"; //NOI18N
    private static final String JAXWSA_API_JAR = "lib/jaxwsa-api.jar"; //NOI18N
    private static final String JAXWSA_RI_JAR = "lib/jaxwsa-ri.jar"; //NOI18N
    
    // wsit jars
    private static final String WEBSERVICES_API_JAR = "lib/endorsed/webservices-api.jar"; //NOI18N
    private static final String WEBSERVICES_RT_JAR = "lib/webservices-rt.jar"; //NOI18N
    private static final String WEBSERVICES_TOOLS_JAR = "lib/webservices-tools.jar"; //NOI18N
    
    private static final String[] SWDP_JARS = new String[] {
            "jersey.jar",
            "jsr311-api.jar",
            "wadl2java.jar"
    };
    
    private static final String[] TRUSTSTORE_LOCATION = new String[] {
        "config/cacerts.jks"  //NOI18N
    };
    
    private static final String[] KEYSTORE_CLIENT_LOCATION = new String[] {
        "config/keystore.jks"  //NOI18N
    };
    
    private static final String[] TRUSTSTORE_CLIENT_LOCATION = new String[] {
        "config/cacerts.jks"  //NOI18N
    };    
    
    private static final String PERSISTENCE_PROV_TOPLINK = "oracle.toplink.essentials.PersistenceProvider"; //NOI18N
    //private static final String PERSISTENCE_PROV_TOPLINK_OLD = "oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider"; //NOI18N
    private static final String PERSISTENCE_PROV_TOPLINK_DEFAULT = "toplinkPersistenceProviderIsDefault"; //NOI18N
            
    private static String CONST_LOCATION = "LOCATION"; // NOI18N
    private static String CONST_DOMAIN = "DOMAIN"; // NOI18N
    
    static {
        MODULE_TYPES.add(J2eeModule.Type.WAR);
        MODULE_TYPES.add(J2eeModule.Type.EAR);
        MODULE_TYPES.add(J2eeModule.Type.EJB);
        MODULE_TYPES.add(J2eeModule.Type.RAR);
        MODULE_TYPES.add(J2eeModule.Type.CAR);
        SPEC_VERSIONS.add(J2eeModule.J2EE_14);
        SPEC_VERSIONS.add(J2eeModule.J2EE_13);
        SPEC_VERSIONS_WITH_5.add(J2eeModule.JAVA_EE_5);
        SPEC_VERSIONS_WITH_5.add(J2eeModule.J2EE_14);
        SPEC_VERSIONS_WITH_5.add(J2eeModule.J2EE_13);
    }
    
    private final File root;
    private final DeploymentManagerProperties dmProps;
    private LibraryImplementation[] libraries;
    
    /** Creates a new instance of PlatformImpl 
     * @param root 
     * @param dmProps 
     */
    public PlatformImpl(File root, DeploymentManagerProperties dmProps) {
        this.dmProps = dmProps;
        this.root = root;
    }
    
    /** Returns error message for an invalid platform or an empty string
     * for a valid platform.
     * @param platformRoot 
     * @return 
     */
    private  String isValidPlatformRoot(File platformRoot) {
        String result = "";
        if(platformRoot == null || "".equals(platformRoot.getPath())) {
            result = "Install directory cannot be empty.";
        } else if(!platformRoot.exists()) {
            result = "Directory '" + platformRoot.getAbsolutePath() + "' does not exist.";
        } else {
//            version = getAppServerVersion(platformRoot);
            File testF = new File(platformRoot, "bin"); // NOI18N
            if(!testF.exists()) {
                result = "'" + platformRoot.getAbsolutePath() + "' is not a SJSAS 8.1 installation directory.";
            } //else if(APPSERVER_VERSION_8_0.equals(PlatformImpl.getAppServerVersion(platformRoot))) {
            //     result = "<html>SJSAS 8.0 or 8.0 update 1 cannot be used. Please use SJSAS 8.1.</html>";
            //} //else {
            // passed all tests
            //}
            
            testF = new File(platformRoot, "lib"); // NOI18N
            if(!testF.exists()) {
                result = "'" + platformRoot.getAbsolutePath() + "' is not a SJSAS 8.1 installation directory.";
            }
        }
        
        return result;
    }
    
    
    private void initLibraries() {
        List<LibraryImplementation> libs = new ArrayList<LibraryImplementation>();
        if ("".equals(isValidPlatformRoot(root))) { // NOI18N
            try {
                List<URL> sources = dmProps.getSources();
                List<URL> javadoc = dmProps.getJavadocs();
                J2eeLibraryTypeProvider lp = new J2eeLibraryTypeProvider();
                LibraryImplementation lib = lp.createLibrary();

                // WSIT - API - when present, needs to override the content of j2ee.jar
                List l = new ArrayList();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "wsit-api")); // NOI18N
                l.add(fileToUrl(new File(root, WEBSERVICES_API_JAR)));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC, sources);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, javadoc);
                libs.add(lib);

                lib = lp.createLibrary();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "j2ee14")); // NOI18N
                l = new ArrayList();
                File ff = (new File(root, JAVA_EE_JAR));
                if (!ff.exists()){
                    l.add(fileToUrl(new File(root, J2EE_14_JAR)));
                } else{
                    l.add(fileToUrl(ff));//In case we would have a glassfish for now
                }
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC, sources);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, javadoc);
                libs.add(lib);
                
                lib = lp.createLibrary();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "jsf11")); // NOI18N
                
                l = new ArrayList();
                l.add(fileToUrl(new File(root, JSF_API_JAR)));
                l.add(fileToUrl(new File(root, JSF_IMPL_JAR)));
                //     l.add(fileToUrl(new File(root, COMMON_LOGGING_JAR)));
                l.add(fileToUrl(new File(root, ACTIVATION_JAR)));
                l.add(fileToUrl(new File(root, TAGS_JAR)));
                l.add(fileToUrl(new File(root, MAIL_JAR)));
                l.add(fileToUrl(new File(root, JSTL_JAR)));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC, sources);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, javadoc);
                libs.add(lib);
                
                // JWSDP
                lib = lp.createLibrary();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "jwsdp")); // NOI18N
                l = new ArrayList();
                l.add(fileToUrl(new File(root, JWSDP_JAR)));
                l.add(fileToUrl(new File(root, JAXWSA_API_JAR)));
                l.add(fileToUrl(new File(root, JAXWSA_RI_JAR)));
                
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                libs.add(lib);

                // WSIT - Implementation
                l = new ArrayList();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "wsit")); // NOI18N
                l.add(fileToUrl(new File(root, WEBSERVICES_TOOLS_JAR)));
                l.add(fileToUrl(new File(root, WEBSERVICES_RT_JAR)));

                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC, sources);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, javadoc);
                libs.add(lib);
                
                lib = lp.createLibrary();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "jaxqname")); // NOI18N
                
                l = new ArrayList();
                l.add(fileToUrl(new File(root, JAX_QNAME_JAR)));
                l.add(fileToUrl(new File(root, "lib/endorsed/jaxp-api.jar")));
                
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC, sources);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, javadoc);
                libs.add(lib);
                
                lib = lp.createLibrary();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "jaxrpc11")); // NOI18N
                
                l = new ArrayList();
                l.add(fileToUrl(new File(root, APPSERV_WS_JAR)));
                l.add(fileToUrl(new File(root, JAXRPC_API_JAR)));
                l.add(fileToUrl(new File(root, JAXRPC_IMPL_JAR)));
                //          l.add(fileToUrl(new File(root, COMMON_LOGGING_JAR)));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC, sources);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, javadoc);
                libs.add(lib);
                
                lib = lp.createLibrary();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "jaxr10")); // NOI18N
                
                l = new ArrayList();
                l.add(fileToUrl(new File(root, JAXR_API_JAR)));
                l.add(fileToUrl(new File(root, JAXR_IMPL_JAR)));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC, sources);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, javadoc);
                libs.add(lib);
                
                lib = lp.createLibrary();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "saaj12")); // NOI18N
                
                l = new ArrayList();
                l.add(fileToUrl(new File(root, SAAJ_API_JAR)));
                l.add(fileToUrl(new File(root, SAAJ_IMPL_JAR)));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC, sources);
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, javadoc);
                libs.add(lib);
                
                l = getSwdpJarURLs();
                if (l != null) {
                    lib = lp.createLibrary();
                    lib.setName(NbBundle.getMessage(PlatformImpl.class, "swdp")); // NOI18N
                    lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                    libs.add(lib);
                }
            } catch(IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        libraries = (LibraryImplementation[])libs.toArray(new LibraryImplementation[libs.size()]);
    }
    
    private List<URL> getSwdpJarURLs() throws MalformedURLException {
        List<URL> ret = getSwdpJarURLs(new File(new File(dmProps.getLocation(), dmProps.getDomainName()), "lib")); //NOI18N
        if (ret == null) {
            ret = getSwdpJarURLs(new File(root, "lib/addons")); //NOI18N
        }
        return ret;
    }
    
    private List<URL> getSwdpJarURLs(File libDir) throws MalformedURLException {
        ArrayList<URL> ret = new ArrayList<URL>();
        for (String jarName : SWDP_JARS) {
            File jarFile = new File(libDir, jarName);
            if (jarFile.isFile()) {
                ret.add(fileToUrl(jarFile));
            } else {
                return null;
            }
        }
        return ret;
    }

    /**
     * Return platform's libraries.
     *
     * @return platform's libraries.
     */
    public LibraryImplementation[] getLibraries() {
        if (libraries == null) {
            initLibraries();
        }
        return libraries.clone();
    }
    
    /**
     * 
     */
    public void notifyLibrariesChanged() {
        initLibraries();
        firePropertyChange(PROP_LIBRARIES, null, libraries);
    }
    
    /**
     * Return platform's display name.
     *
     * @return platform's display name.
     */
    public String getDisplayName() {
        return dmProps.getDisplayName();
    }
    
    /**
     * Return platform's icon.
     *
     * @return platform's icon.
     */
    public Image getIcon() {
        return ImageUtilities.loadImage("org/netbeans/modules/j2ee/sun/ide/resources/ServerInstanceIcon.png"); // NOI18N;
    }
    
    /**
     * Return platform's root directories. This will be mostly server's installation
     * directory.
     *
     * @return platform's root directories.
     */
    public File[] getPlatformRoots() {
        return new File [] {root};
    }
    
    /**
     * Return classpath for the specified tool.
     *
     * @param  toolName tool's name e.g. "wscompile".
     * @return classpath for the specified tool.
     */
    public File[] getToolClasspathEntries(String toolName) {
        if (J2eePlatform.TOOL_WSCOMPILE.equals(toolName)) {
            if (isValidPlatformRoot(root).equals("")) {
                return new File[] {
                    new File(root, WEBSERVICES_API_JAR),   // possibly for AS 9.1
                    new File(root, "lib/j2ee.jar"),             //NOI18N
                    new File(root, "lib/saaj-api.jar"),         //NOI18N
                    new File(root, "lib/saaj-impl.jar"),        //NOI18N
                    new File(root, "lib/jaxrpc-api.jar"),       //NOI18N
                    new File(root, "lib/jaxrpc-impl.jar"),      //NOI18N
                    new File(root, "lib/endorsed/jaxp-api.jar"),//NOI18N
                    new File(root, APPSERV_WS_JAR),        // possibly for AS 9
                    new File(root, WEBSERVICES_TOOLS_JAR), // possibly for AS 9.1
                    new File(root, WEBSERVICES_RT_JAR),    // possibly for AS 9.1
                };
            }
        }
        if (J2eePlatform.TOOL_APP_CLIENT_RUNTIME.equals(toolName)) {    //NOI18N
            if (isValidPlatformRoot(root).equals("")) {
                return new File[] {
                    
                    new File(root, WEBSERVICES_API_JAR),       //NOI18N

                    // 8.2 only -- not in GF based servers
                    new File(root, "lib/dom.jar"),            //NOI18N
                    new File(root, "lib/xalan.jar"),            //NOI18N
                    new File(root, "lib/xercesImpl.jar"),       //NOI18N
                    
                    // Shared by all
                    new File(root, "lib/appserv-rt.jar"),       //NOI18N
                    
                    // GF V1U1 and V2 -- not in 8.2
                    new File(root, "lib/javaee.jar"),             //NOI18N

                    // 8.2 -- api's included in javaee.jar
                    new File(root, "lib/j2ee.jar"),             //NOI18N
                    
                    // GF V2 -- not present in other environments
                    new File(root, "lib/jmac-api.jar"),       // NOI18N
                    
                    // Shared
                    new File(root, "lib/appserv-ext.jar"),      //NOI18N
                    new File(root, "lib/mail.jar"),       // NOI18N
                    new File(root, "lib/activation.jar"),       // NOI18N
                    
                    // GF V2 -- not present in other environments
                    new File(root, WEBSERVICES_RT_JAR),       //NOI18N
                    new File(root, WEBSERVICES_TOOLS_JAR),       //NOI18N
                    
                    // GF V1U1 and V2 -- not present in 8.2
                    new File(root, "lib/appserv-ws.jar"),       //NOI18N
                    
                    // 8.2 -- not present in GF V1U1 or GF v2
                    new File(root, "lib/jaxrpc-impl.jar"),       //NOI18N
                    new File(root, "lib/saaj-impl.jar"),       //NOI18N
                    new File(root, "lib/jaxr-impl.jar"),       //NOI18N
                    new File(root, "lib/relaxngDatatype.jar"),       //NOI18N
                    new File(root, "lib/xsdlib.jar"),       //NOI18N
                   
                    // Shared
                    new File(root, "lib/appserv-cmp.jar"),      //NOI18N
                    
                    // GF V1U1 and V2 -- not present in 8.2
                    new File(root, "javadb/lib/derbyclient.jar"),      //NOI18N
                    new File(root, "lib/toplink-essentials.jar"),      //NOI18N
                    new File(root, "lib/dbschema.jar"),         //NOI18N
                    
                    // Shared
                    new File(root, "lib/appserv-admin.jar"),    //NOI18N
                    new File(root, "lib/install/applications/jmsra/imqjmsra.jar"), //NOI18N, standalone JMS
                    new File(root, "lib/fscontext.jar"),        //NOI18N
                    
                    // GF V1U1 and GF V2
                    new File(root, "lib/dtds"),                 //NOI18N
                    new File(root, "lib/schemas"),               //NOI18N
                };
            }
        }
        if (J2eePlatform.TOOL_KEYSTORE.equals(toolName)) {
            if (isValidPlatformRoot(root).equals("")) {        //NOI18N
                File keyStoreLoc = new File(new File(dmProps.getInstanceProperties().getProperty(CONST_LOCATION)),      //NOI18N
                        dmProps.getInstanceProperties().getProperty(CONST_DOMAIN) + File.separator + KEYSTORE_CLIENT_LOCATION[0]);  //NOI18N
                return new File[] {
                    keyStoreLoc
                };
            }
        }
        if (J2eePlatform.TOOL_KEYSTORE_CLIENT.equals(toolName)) {
            if (isValidPlatformRoot(root).equals("")) {        //NOI18N
                File keyStoreClientLoc = new File(new File(dmProps.getInstanceProperties().getProperty(CONST_LOCATION)),            //NOI18N
                        dmProps.getInstanceProperties().getProperty(CONST_DOMAIN) + File.separator + KEYSTORE_CLIENT_LOCATION[0]);  //NOI18N
                return new File[] {
                    keyStoreClientLoc
                };
            }
        }
        if (J2eePlatform.TOOL_TRUSTSTORE.equals(toolName)) {
            if (isValidPlatformRoot(root).equals("")) {        //NOI18N
                File trustStoreLoc = new File(new File(dmProps.getInstanceProperties().getProperty(CONST_LOCATION)),            //NOI18N
                        dmProps.getInstanceProperties().getProperty(CONST_DOMAIN) + File.separator + TRUSTSTORE_LOCATION[0]);   //NOI18N
                return new File[] {
                    trustStoreLoc
                };
            }
        }
        if (J2eePlatform.TOOL_TRUSTSTORE_CLIENT.equals(toolName)) {
            if (isValidPlatformRoot(root).equals("")) {        //NOI18N
                File trustStoreClientLoc = new File(new File(dmProps.getInstanceProperties().getProperty(CONST_LOCATION)),              //NOI18N
                        dmProps.getInstanceProperties().getProperty(CONST_DOMAIN) + File.separator + TRUSTSTORE_CLIENT_LOCATION[0]);    //NOI18N
                return new File[] {
                    trustStoreClientLoc
                };
            }
        }
        if (J2eePlatform.TOOL_WSGEN.equals(toolName) || J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
            File jwsdpJar = new File(root, JWSDP_JAR);  //NOI18N
            File wsToolsJar = new File(root, WEBSERVICES_TOOLS_JAR);  //NOI18N

            File wsEndorsedApiJar = new File(root, WEBSERVICES_API_JAR);  //NOI18N

            if (wsToolsJar.exists()) {          // WSIT installed on top
                if (isValidPlatformRoot(root).equals("")) {
                    if (wsEndorsedApiJar.exists()) {
                        return new File[] {
                            new File(root, WEBSERVICES_API_JAR),     // NOI18N
                            new File(root, WEBSERVICES_TOOLS_JAR),     // NOI18N
                            new File(root, WEBSERVICES_RT_JAR),           // NOI18N
                            new File(root, TOOLS_JAR),      //NOI18N
                            new File(root, JSTL_JAR),       //NOI18N
                            new File(root, JAVA_EE_JAR),    //NOI18N
                            new File(root, APPSERV_WS_JAR), //NOI18N
                            new File(root, MAIL_JAR),       //NOI18N
                            new File(root, ACTIVATION_JAR)  //NOI18N
                        };
                    } else {
                        return new File[] {
                            new File(root, WEBSERVICES_TOOLS_JAR),     // NOI18N
                            new File(root, WEBSERVICES_RT_JAR),           // NOI18N
                            new File(root, TOOLS_JAR),      //NOI18N
                            new File(root, JSTL_JAR),       //NOI18N
                            new File(root, JAVA_EE_JAR),    //NOI18N
                            new File(root, APPSERV_WS_JAR), //NOI18N
                            new File(root, MAIL_JAR),       //NOI18N
                            new File(root, ACTIVATION_JAR)  //NOI18N
                        };
                    }
                }
            } else if (jwsdpJar.exists()) { // JWSDP installed on top
                if (isValidPlatformRoot (root).equals("")) {
                    return new File[] {
                        new File(root, JWSDP_JAR),      //NOI18N
                        new File(root, JAXWSA_API_JAR), //NOI18N
                        new File(root, JAXWSA_RI_JAR),  //NOI18N
                        new File(root, TOOLS_JAR),      //NOI18N
                        new File(root, JSTL_JAR),       //NOI18N
                        new File(root, JAVA_EE_JAR),    //NOI18N
                        new File(root, APPSERV_WS_JAR), //NOI18N
                        new File(root, MAIL_JAR),       //NOI18N
                        new File(root, ACTIVATION_JAR)  //NOI18N
                    };
                }
            } else {                                                // regular appserver
                if (isValidPlatformRoot (root).equals("")) {
                    return new File[] {
                        new File(root, TOOLS_JAR),        //NOI18N
                        new File(root, JSTL_JAR),         //NOI18N
                        new File(root, JAVA_EE_JAR),      //NOI18N
                        new File(root, APPSERV_WS_JAR),   //NOI18N
                        new File(root, MAIL_JAR),         //NOI18N
                        new File(root, ACTIVATION_JAR)    //NOI18N
                    };
                }
            }
        }
        return new File[0];
    }
    
    /**
     * Specifies whether a tool of the given name is supported by this platform.
     *
     * @param  toolName tool's name e.g. "wscompile".
     * @return <code>true</code> if platform supports tool of the given name,
     *         <code>false</code> otherwise.
     */
    public boolean isToolSupported(String toolName) {
        if (J2eePlatform.TOOL_WSCOMPILE.equals(toolName)
        || J2eePlatform.TOOL_APP_CLIENT_RUNTIME.equals(toolName)) {
            return true;
        }
//        if (!APPSERVER_VERSION_8_1.equals(version) &&
//                !APPSERVER_VERSION_8_2.equals(version)) { // we want this to work for 9.1 as well
            if (J2eePlatform.TOOL_WSGEN.equals(toolName)) {
                return true;
            }
            if (J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
                return true;
            }
            if (J2eePlatform.TOOL_JSR109.equals(toolName)) {
                return true;
            }
            if (J2eePlatform.TOOL_JWSDP.equals(toolName) && (new File(root, JWSDP_JAR).exists())) {
                return true;
            }
            if (J2eePlatform.TOOL_WSIT.equals(toolName) && (new File(root, WEBSERVICES_TOOLS_JAR).exists())) {
                return true;
            }
            if (J2eePlatform.TOOL_KEYSTORE.equals(toolName)) {
                return true;
            }
            if (J2eePlatform.TOOL_KEYSTORE_CLIENT.equals(toolName)) {
                return true;
            }
            if (J2eePlatform.TOOL_TRUSTSTORE.equals(toolName)) {
                return true;
            }
            if (J2eePlatform.TOOL_TRUSTSTORE_CLIENT.equals(toolName)) {
                return true;
            }
            // Test if server has the JAX-WS Tester capability
            if ("jaxws-tester".equals(toolName)) { //NOI18N
                return true;
            }

            //Persistence Provoiders
            if(PERSISTENCE_PROV_TOPLINK.equals(toolName)){
                return true;
            }
            if (PERSISTENCE_PROV_TOPLINK_DEFAULT.equals(toolName)) {
                return true;
            }
            
            if ("org.hibernate.ejb.HibernatePersistence".equals(toolName) || //NOI18N
                "kodo.persistence.PersistenceProviderImpl".equals(toolName) || // NOI18N
                "org.apache.openjpa.persistence.PersistenceProviderImpl".equals(toolName)) { //NOI18N
                return true;
            }
            if ("defaultPersistenceProviderJavaEE5".equals(toolName)) {
                return true;
            }
            
        //}
        return false;
    }
    
    /**
     * Return a list of supported J2EE specification versions. Use J2EE specification
     * versions defined in the {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * class.
     *
     * @return list of supported J2EE specification versions.
     */
    public Set/*<String>*/ getSupportedSpecVersions() {        
        return SPEC_VERSIONS_WITH_5;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Type> getSupportedTypes() {
        return MODULE_TYPES;
    }


    
    public Set/*<String>*/ getSupportedJavaPlatformVersions() {
        Set versions = new HashSet();
        versions.add("1.5"); // NOI18N
        versions.add("1.6"); // NOI18N
        return versions;
    }
    
    public JavaPlatform getJavaPlatform() {
        if (dmProps.getSunDeploymentManager().isLocal()) {
            Asenv envData = new Asenv(root);
            File jdkPath = new File(envData.get(Asenv.AS_JAVA));
            FileObject currHome = FileUtil.toFileObject(FileUtil.normalizeFile(jdkPath));
            JavaPlatformManager jpm = JavaPlatformManager.getDefault();

            if (currHome != null) {
                JavaPlatform[] installedPlatforms = jpm.getPlatforms(null, new Specification("J2SE", null)); // NOI18N
                for (int i = 0; i < installedPlatforms.length; i++) {
                    JavaPlatform platform = installedPlatforms[i];
                    Iterator itr = platform.getInstallFolders().iterator();
                    while (itr.hasNext()) {
                        FileObject propName = (FileObject) itr.next();
                        if (propName.equals(currHome)) {
                            return platform;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private URL fileToUrl(File file) throws MalformedURLException {
        URL url = file.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        }
        return url;
    }
    
    /* return the string within quotes
     **/
    private String quotedString(String s){
        return "\""+s+"\"";
    }
    public String getToolProperty(String toolName, String propertyName) {
        if (J2eePlatform.TOOL_APP_CLIENT_RUNTIME.equals(toolName)) {
            if (J2eePlatform.TOOL_PROP_MAIN_CLASS.equals(propertyName)) {
                return "com.sun.enterprise.appclient.Main"; // NOI18N
            }
            if (J2eePlatform.TOOL_PROP_MAIN_CLASS_ARGS.equals(propertyName)) {
                return "-client ${dist.jar} ${j2ee.appclient.tool.args}"; // NOI18N
            }
            if (J2eePlatform.TOOL_PROP_JVM_OPTS.equals(propertyName)) {
                StringBuilder sb = new StringBuilder();
                sb.append("-Dcom.sun.aas.configRoot=").append(quotedString(new File(root, "config").getAbsolutePath())); // NOI18N
                sb.append(" -Dcom.sun.aas.installRoot=").append(quotedString(root.getAbsolutePath())); // NOI18N
                sb.append(" -Dcom.sun.aas.imqLib=").append(quotedString(new File(root, "imq/lib").getAbsolutePath())); // NOI18N
                sb.append(" -Djava.security.policy=").append(quotedString(new File(root, "lib/appclient/client.policy").getAbsolutePath())); // NOI18N
                sb.append(" -Djava.security.auth.login.config=").append(quotedString(new File(root, "lib/appclient/appclientlogin.conf").getAbsolutePath())); // NOI18N
                sb.append(" -Djava.endorsed.dirs=").append(quotedString(new File(root, "lib/endorsed").getAbsolutePath())); // NOI18N
                sb.append(" -Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl"); // NOI18N
                sb.append(" -Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl"); // NOI18N
                sb.append(" -Djavax.xml.transform.TransformerFactory=com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl"); // NOI18N
                sb.append(" -Dorg.xml.sax.parser=org.xml.sax.helpers.XMLReaderAdapter"); // NOI18N
                sb.append(" -Dorg.xml.sax.driver=com.sun.org.apache.xerces.internal.parsers.SAXParser"); // NOI18N
                sb.append(" -Djava.util.logging.manager=com.sun.enterprise.server.logging.ACCLogManager"); // NOI18N
                return sb.toString();
            }
            if (J2eePlatform.TOOL_PROP_CLIENT_JAR_LOCATION.equals(propertyName)) {
                File exFile = new File(root, "lib/javaee.jar"); // NOI18N
                FileObject location = FileUtil.toFileObject(FileUtil.normalizeFile(new File(dmProps.getLocation())));
                if (location == null) {
                    return null;
                }
                FileObject domain = location.getFileObject(
                        dmProps.getInstanceProperties().getProperty(DeploymentManagerProperties.DOMAIN_ATTR));
                if (domain == null) {
                    return null;
                }

                if (exFile.exists()) {
                    FileObject copyLocation = domain.getFileObject("generated/xml/j2ee-modules"); // NOI18N
                    if (copyLocation != null) {
                        return FileUtil.toFile(copyLocation).getAbsolutePath();
                    }
                } else {
                    FileObject copyLocation = domain.getFileObject("applications/j2ee-modules"); // NOI18N
                    if (copyLocation != null) {
                        return FileUtil.toFile(copyLocation).getAbsolutePath();
                    }
                }
                return null;
            }
            if ("j2ee.appclient.args".equals(propertyName)) { // NOI18N
                return "-configxml " + quotedString(new File(dmProps.getLocation(), dmProps.getDomainName() + "/config/sun-acc.xml").getAbsolutePath()); // NOI18N
            }
        }
        return null;
    }
    
    public Lookup getLookup() {
        Lookup baseLookup = Lookups.fixed(root);
        return LookupProviderSupport.createCompositeLookup(baseLookup, "J2EE/DeploymentPlugins/J2EE/Lookup"); //NOI18N
//        WSStackSPI metroStack = new GlassfishJaxWsStack(root);
//        return Lookups.fixed(WSStackFactory.createWSStack(metroStack));
    }
    
}
