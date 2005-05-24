/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.ide.j2ee.db.RegisterPointbase;

/**
 */
public class PlatformImpl extends J2eePlatformImpl implements PropertyChangeListener {
    
    private static final Set/*<Object>*/ MODULE_TYPES = new HashSet();
    private static final Set/*<String>*/ SPEC_VERSIONS = new HashSet();
    private static final Set/*<String>*/ SPEC_VERSIONS_WITH_5 = new HashSet();
    // Appserver version strings.  
    private static final String APPSERVER_VERSION_9 = "9.0"; // NOI18N
    private static final String APPSERVER_VERSION_8_1 = "8.1"; // NOI18N
    private static final String APPSERVER_VERSION_UNKNOWN = "unknown"; // NOI18N
    private static String version = APPSERVER_VERSION_UNKNOWN;	// NOI18N
    private static final String J2EE_14_JAR = "lib/j2ee.jar"; //NOI18N
    private static final String JSF_API_JAR = "lib/jsf-api.jar"; //NOI18N
    private static final String JSF_IMPL_JAR = "lib/jsf-impl.jar"; //NOI18N
    private static final String COMMON_LOGGING_JAR = "lib/commons-logging.jar"; //NOI18N
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
          
          
    private List/*<LibraryImpl>*/ libraries  = new ArrayList();
    
    static {
        MODULE_TYPES.add(J2eeModule.WAR);
        MODULE_TYPES.add(J2eeModule.EAR);
        MODULE_TYPES.add(J2eeModule.EJB);
        MODULE_TYPES.add(J2eeModule.CONN);
        MODULE_TYPES.add(J2eeModule.CLIENT);
        SPEC_VERSIONS.add(J2eeModule.J2EE_14);
        SPEC_VERSIONS.add(J2eeModule.J2EE_13);
        SPEC_VERSIONS_WITH_5.add(J2eeModule.J2EE_15);
        SPEC_VERSIONS_WITH_5.add(J2eeModule.J2EE_14);
        SPEC_VERSIONS_WITH_5.add(J2eeModule.J2EE_13);
    }
    
    private File root;
    private String displayName;
    
    /** Creates a new instance of PlatformImpl */
    public PlatformImpl(File rootLocation, String aDisplayName, InstanceProperties instanceProperties) {
        displayName = aDisplayName;
        init (rootLocation);
        if (instanceProperties != null) {
            instanceProperties.addPropertyChangeListener (this);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                RegisterPointbase.getDefault().register();
            }
        });
    }

    /** Returns error message for an invalid platform or an empty string
     * for a valid platform.
     */
    public static String isValidPlatformRoot (File platformRoot) {
        String result = "";
        if(platformRoot == null || "".equals(platformRoot.getPath())) {
                result = "Install directory cannot be empty.";
        } else if(!platformRoot.exists()) {
                result = "Directory '" + platformRoot.getAbsolutePath() + "' does not exist.";
        } else {
                version = getAppServerVersion(platformRoot);
                File testF = new File(platformRoot, "appserv_uninstall.class"); // NOI18N
                if(!testF.exists()) {
                        result = "'" + platformRoot.getAbsolutePath() + "' is not a SJSAS 8.1 installation directory.";
                } //else if(APPSERVER_VERSION_8_0.equals(PlatformImpl.getAppServerVersion(platformRoot))) {
                   //     result = "<html>SJSAS 8.0 or 8.0 update 1 cannot be used. Please use SJSAS 8.1.</html>";
                //} //else {
                        // passed all tests
                //}
        }
        
        return result;
    }
    
    /** Attempt to discern the application server who's root directory was passed in.
     *
     * 9.0 uses sun-domain_1_0.dtd
     * 8.1 uses sun-domain_1_1.dtd (also includes the 1_0 version for backwards compatibility)
     *
     */
    public static String getAppServerVersion(File asInstallRoot) {
            version = APPSERVER_VERSION_UNKNOWN;	// NOI18N

            if(asInstallRoot != null && asInstallRoot.exists()) {
                    File sunDomain11Dtd = new File(asInstallRoot, "lib/dtds/sun-domain_1_1.dtd"); // NOI18N
                    //now test for AS 9 (J2EE 5.0) which should work for this plugin
                    File as9 = new File((asInstallRoot)+"/lib/dtds/sun-web-app_2_5-0.dtd");
                    if(as9.exists()){
                        version = APPSERVER_VERSION_9;
                        
                    } else    if(sunDomain11Dtd.exists()) {
                        version = APPSERVER_VERSION_8_1;
                    }
            }
            return version;
    }
        

    
    private void init (File rootLocation) {
        libraries.clear();
        root = null;
        if (isValidPlatformRoot (rootLocation).equals("")) {
            root = rootLocation;
            try {
                J2eeLibraryTypeProvider lp = new J2eeLibraryTypeProvider();
                lp.createLibrary().setName ("a");
                LibraryImplementation lib = lp.createLibrary();

                lib.setName(NbBundle.getMessage(PlatformFactory.class, "j2ee14")); // NOI18N

                List l = new ArrayList();
                l.add(fileToUrl(new File(root, J2EE_14_JAR)));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);

                File doc = InstalledFileLocator.getDefault().locate("docs/j2eeri-1_4-doc-api.zip", null, false); // NOI18N
                if (doc != null) {
                    l = new ArrayList();
                    l.add(fileToUrl(doc));
                    lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, l);
                }
                libraries.add(lib);

                lib = lp.createLibrary();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "jsf11")); // NOI18N

                l = new ArrayList();
                l.add(fileToUrl(new File(root, JSF_API_JAR)));
                l.add(fileToUrl(new File(root, JSF_IMPL_JAR)));
                l.add(fileToUrl(new File(root, COMMON_LOGGING_JAR)));
                l.add(fileToUrl(new File(root, ACTIVATION_JAR)));
                l.add(fileToUrl(new File(root, TAGS_JAR)));
                l.add(fileToUrl(new File(root, MAIL_JAR)));
                l.add(fileToUrl(new File(root, JSTL_JAR)));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                libraries.add(lib);

                lib = lp.createLibrary();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "jaxqname")); // NOI18N

                l = new ArrayList();
                l.add(fileToUrl(new File(root, JAX_QNAME_JAR)));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                libraries.add(lib);

                lib = lp.createLibrary();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "jaxrpc11")); // NOI18N

                l = new ArrayList();
                l.add(fileToUrl(new File(root, JAXRPC_API_JAR)));
                l.add(fileToUrl(new File(root, JAXRPC_IMPL_JAR)));
                l.add(fileToUrl(new File(root, COMMON_LOGGING_JAR)));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                libraries.add(lib);

                lib = lp.createLibrary();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "jaxr10")); // NOI18N

                l = new ArrayList();
                l.add(fileToUrl(new File(root, JAXR_API_JAR)));
                l.add(fileToUrl(new File(root, JAXR_IMPL_JAR)));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                libraries.add(lib);

                lib = lp.createLibrary();
                lib.setName(NbBundle.getMessage(PlatformImpl.class, "saaj12")); // NOI18N

                l = new ArrayList();
                l.add(fileToUrl(new File(root, SAAJ_API_JAR)));
                l.add(fileToUrl(new File(root, SAAJ_IMPL_JAR)));
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, l);
                libraries.add(lib);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Return platform's libraries.
     *
     * @return platform's libraries.
     */
    public LibraryImplementation[] getLibraries() {
        return (LibraryImplementation[])libraries.toArray(new LibraryImplementation[libraries.size()]);
    }
    
    /**
     * Return platform's display name.
     *
     * @return platform's display name.
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Return platform's icon.
     *
     * @return platform's icon.
     */
    public Image getIcon() {
        return Utilities.loadImage("org/netbeans/modules/j2ee/sun/ide/resources/ServerInstanceIcon.gif"); // NOI18N;
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
        if ("wscompile".equals(toolName)) { // TODO this will be removed - just for testing
            if (isValidPlatformRoot (root).equals("")) {
                return new File[] {
                    new File(root, "lib/j2ee.jar"),
                    new File(root, "lib/saaj-api.jar"),
                    new File(root, "lib/saaj-impl.jar"),
                    new File(root, "lib/jaxrpc-api.jar"),
                    new File(root, "lib/jaxrpc-impl.jar")
                };
            }
        }
        return null;
    }
    
    /**
     * Specifies whether a tool of the given name is supported by this platform.
     *
     * @param  toolName tool's name e.g. "wscompile".
     * @return <code>true</code> if platform supports tool of the given name, 
     *         <code>false</code> otherwise.
     */
    public boolean isToolSupported(String toolName) {
        if ("wscompile".equals(toolName)) { // TODO this will be removed - just for testing
            return true;
        }
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

        if(APPSERVER_VERSION_9.equals(version)){
            return SPEC_VERSIONS_WITH_5;
        }
        else
            return SPEC_VERSIONS;
    }
    
    /**
     * Return a list of supported J2EE module types. Use module types defined in the 
     * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * class.
     *
     * @return list of supported J2EE module types.
     */
    public Set/*<Object>*/ getSupportedModuleTypes() {
        return MODULE_TYPES;
    }
    
    private URL fileToUrl(File file) throws MalformedURLException {
        URL url = file.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        }
        return url;
    }
    
    public void propertyChange (PropertyChangeEvent e) {
     /*   if (e.getPropertyName().equals(DeploymentManagerProperties.LOCATION_ATTR)) {
            root = new File ((String) e.getNewValue());
            init(root);
            firePropertyChange (PROP_LIBRARIES, null, null);
            firePropertyChange (J2eePlatform.PROP_CLASSPATH, null, null);
            firePropertyChange (PROP_PLATFORM_ROOTS, null, null);
        }*/
    }
}

