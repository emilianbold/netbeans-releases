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

package org.netbeans.modules.tomcat5;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport;
import org.netbeans.modules.tomcat5.util.TomcatProperties;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Tomcat's implementation of the J2eePlatformImpl.
 *
 * @author Stepan Herold
 */
public class TomcatPlatformImpl extends J2eePlatformImpl {
    
    private static final String WSCOMPILE_LIBS[] = new String[] {
        "jaxrpc/lib/jaxrpc-api.jar",        // NOI18N
        "jaxrpc/lib/jaxrpc-impl.jar",       // NOI18N
        "jaxrpc/lib/jaxrpc-spi.jar",        // NOI18N
        "saaj/lib/saaj-api.jar",            // NOI18N
        "saaj/lib/saaj-impl.jar",           // NOI18N
        "jwsdp-shared/lib/mail.jar",        // NOI18N
        "jwsdp-shared/lib/activation.jar"   // NOI18N
    };

    private static final String JWSDP_LIBS[] = new String[] {
        "fastinfoset/lib/FastInfoset.jar",              // NOI18N
        "jaxb/lib/jaxb1-impl.jar",                      // NOI18N
        "jaxb/lib/jaxb-impl.jar",                       // NOI18N
        "jaxb/lib/jaxb-api.jar",                        // NOI18N
        "jaxb/lib/jaxb-xjc.jar",                        // NOI18N
        "jaxws/lib/jaxws-api.jar",                      // NOI18N
        "jaxws/lib/jaxws-rt.jar",                       // NOI18N
        "jaxws/lib/jaxws-tools.jar",                    // NOI18N
        "jaxws/lib/jsr181-api.jar",                     // NOI18N
        "jaxws/lib/jsr250-api.jar",                     // NOI18N
        "saaj/lib/saaj-api.jar",                        // NOI18N
        "saaj/lib/saaj-impl.jar",                       // NOI18N
        "sjsxp/lib/sjsxp.jar",                          // NOI18N
        "sjsxp/lib/jsr173_api.jar",                     // NOI18N
        "jwsdp-shared/lib/activation.jar",              // NOI18N
        "jwsdp-shared/lib/jaas.jar",                    // NOI18N
        "jwsdp-shared/lib/jta-spec1_0_1.jar",           // NOI18N
        "jwsdp-shared/lib/mail.jar",                    // NOI18N
        //"jwsdp-shared/lib/PackageFormat.jar",           // NOI18N
        "jwsdp-shared/lib/relaxngDatatype.jar",         // NOI18N
        "jwsdp-shared/lib/resolver.jar",                // NOI18N
        "jwsdp-shared/lib/xmlsec.jar",                  // NOI18N
        "jwsdp-shared/lib/xsdlib.jar"                  // NOI18N
    };

    private static final String WSIT_LIBS[] = new String[] {
        "shared/lib/webservices-rt.jar",              // NOI18N
        "shared/lib/webservices-tools.jar"     // NOI18N
    };
    
    private static final String JWSDP_WSGEN_LIBS[] = new String[] {
        "jaxws/lib/jaxws-tools.jar",                // NOI18N
        "jaxws/lib/jaxws-rt.jar",                   // NOI18N
        "sjsxp/lib/sjsxp.jar",                      // NOI18N
        "jaxb/lib/jaxb-xjc.jar",                    // NOI18N
        "saaj/lib/saaj-impl.jar",                    // NOI18N
        "saaj/lib/saaj-api.jar",                    // NOI18N
        "jwsdp-shared/lib/relaxngDatatype.jar",     // NOI18N
        "jwsdp-shared/lib/resolver.jar"             // NOI18N
    };

    private static final String JWSDP_WSIMPORT_LIBS[] = new String[] {
        "jaxws/lib/jaxws-tools.jar",                // NOI18N
        "jaxws/lib/jaxws-rt.jar",                   // NOI18N
        "sjsxp/lib/sjsxp.jar",                      // NOI18N
        "jaxb/lib/jaxb-xjc.jar",                    // NOI18N
        "jwsdp-shared/lib/relaxngDatatype.jar",     // NOI18N
        "jwsdp-shared/lib/resolver.jar"             // NOI18N
    };

    private static final String WSIT_WSIMPORT_LIBS[] = new String[] {
        "shared/lib/webservices-rt.jar",               // NOI18N
        "shared/lib/webservices-tools.jar"         // NOI18N
    };

    private static final String WSIT_WSGEN_LIBS[] = new String[] {
        "shared/lib/webservices-rt.jar",               // NOI18N
        "shared/lib/webservices-tools.jar"         // NOI18N
    };

    private static final String[] KEYSTORE_LOCATION = new String[] {
        "certs/server-keystore.jks"  //NOI18N
    };
    
    private static final String[] TRUSTSTORE_LOCATION = new String[] {
        "certs/server-truststore.jks"  //NOI18N
    };
    
    private static final String[] KEYSTORE_CLIENT_LOCATION = new String[] {
        "certs/client-keystore.jks"  //NOI18N
    };
    
    private static final String[] TRUSTSTORE_CLIENT_LOCATION = new String[] {
        "certs/client-truststore.jks"  //NOI18N
    };
    
    private static final String ICON = "org/netbeans/modules/tomcat5/resources/tomcat5instance.png"; // NOI18N
    
    private String displayName;
    private TomcatProperties tp;
    private TomcatManager manager;
    
    private List/*<LibraryImpl>*/ libraries  = new ArrayList();
    
    /** Creates a new instance of TomcatInstallation */
    public TomcatPlatformImpl(TomcatManager manager) {
        this.manager = manager;
        this.tp = manager.getTomcatProperties();
        displayName = tp.getDisplayName();
        
        J2eeLibraryTypeProvider libProvider = new J2eeLibraryTypeProvider();
        LibraryImplementation lib = libProvider.createLibrary();
        lib.setName(NbBundle.getMessage(TomcatPlatformImpl.class, "LBL_lib_name", displayName));
        loadLibraries(lib);
        libraries.add(lib);
    }
    
    public void notifyLibrariesChanged() {
        LibraryImplementation lib = (LibraryImplementation)libraries.get(0);
        loadLibraries(lib);
        firePropertyChange(PROP_LIBRARIES, null, libraries);
    }
    
    public LibraryImplementation[] getLibraries() {
       return (LibraryImplementation[])libraries.toArray(new LibraryImplementation[libraries.size()]);
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Image getIcon() {
        return ImageUtilities.loadImage(ICON);
    }
    
    public File[] getPlatformRoots() {
        if (tp.getCatalinaBase() != null) {
            return new File[] {tp.getCatalinaHome(), tp.getCatalinaBase()};
        } else {
            return new File[] {tp.getCatalinaHome()};
        }
    }
    
    public File[] getToolClasspathEntries(String toolName) {
        // wscompile support
        if (J2eePlatform.TOOL_WSCOMPILE.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_WSCOMPILE)) {
                File[] retValue = new File[WSCOMPILE_LIBS.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < WSCOMPILE_LIBS.length; i++) {
                    retValue[i] = new File(homeDir, WSCOMPILE_LIBS[i]);
                }
                return retValue;
            }
        }
        // wsgen support
        if (J2eePlatform.TOOL_WSGEN.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_WSGEN)) {
                if (isToolSupported(J2eePlatform.TOOL_WSIT)) {
                    File[] retValue = new File[WSIT_WSGEN_LIBS.length];
                    File homeDir = tp.getCatalinaHome();
                    for (int i = 0; i < WSIT_WSGEN_LIBS.length; i++) {
                        retValue[i] = new File(homeDir, WSIT_WSGEN_LIBS[i]);
                    }
                    return retValue;
                } else {
                    File[] retValue = new File[JWSDP_WSGEN_LIBS.length];
                    File homeDir = tp.getCatalinaHome();
                    for (int i = 0; i < JWSDP_WSGEN_LIBS.length; i++) {
                        retValue[i] = new File(homeDir, JWSDP_WSGEN_LIBS[i]);
                    }
                    return retValue;
                }
            }
        }
        // wsimport support
        if (J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_WSIMPORT)) {
                if (isToolSupported(J2eePlatform.TOOL_WSIT)) {
                    File[] retValue = new File[WSIT_WSIMPORT_LIBS.length];
                    File homeDir = tp.getCatalinaHome();
                    for (int i = 0; i < WSIT_WSIMPORT_LIBS.length; i++) {
                        retValue[i] = new File(homeDir, WSIT_WSIMPORT_LIBS[i]);
                    }
                    return retValue;
                } else {
                    File[] retValue = new File[JWSDP_WSIMPORT_LIBS.length];
                    File homeDir = tp.getCatalinaHome();
                    for (int i = 0; i < JWSDP_WSIMPORT_LIBS.length; i++) {
                        retValue[i] = new File(homeDir, JWSDP_WSIMPORT_LIBS[i]);
                    }
                    return retValue;
                }
            }
        }
        // jwsdp support
        if (J2eePlatform.TOOL_JWSDP.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_WSIT)) {
                return getToolClasspathEntries(J2eePlatform.TOOL_WSIT);
            } else {
                if (isToolSupported(J2eePlatform.TOOL_JWSDP)) {
                    File[] retValue = new File[JWSDP_LIBS.length];
                    File homeDir = tp.getCatalinaHome();
                    for (int i = 0; i < JWSDP_LIBS.length; i++) {
                        retValue[i] = new File(homeDir, JWSDP_LIBS[i]);
                    }
                    return retValue;
                }
            }
        }
        // keystore support
        if (J2eePlatform.TOOL_KEYSTORE.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_KEYSTORE)) {
                File[] retValue = new File[KEYSTORE_LOCATION.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < KEYSTORE_LOCATION.length; i++) {
                    retValue[i] = new File(homeDir, KEYSTORE_LOCATION[i]);
                }
                return retValue;
            }
        }
        // truststore support
        if (J2eePlatform.TOOL_TRUSTSTORE.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_TRUSTSTORE)) {
                File[] retValue = new File[TRUSTSTORE_LOCATION.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < TRUSTSTORE_LOCATION.length; i++) {
                    retValue[i] = new File(homeDir, TRUSTSTORE_LOCATION[i]);
                }
                return retValue;
            }
        }
        if (J2eePlatform.TOOL_KEYSTORE_CLIENT.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_KEYSTORE_CLIENT)) {
                File[] retValue = new File[KEYSTORE_CLIENT_LOCATION.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < KEYSTORE_CLIENT_LOCATION.length; i++) {
                    retValue[i] = new File(homeDir, KEYSTORE_CLIENT_LOCATION[i]);
                }
                return retValue;
            }
        }
        // truststore support
        if (J2eePlatform.TOOL_TRUSTSTORE_CLIENT.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_TRUSTSTORE_CLIENT)) {
                File[] retValue = new File[TRUSTSTORE_CLIENT_LOCATION.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < TRUSTSTORE_CLIENT_LOCATION.length; i++) {
                    retValue[i] = new File(homeDir, TRUSTSTORE_CLIENT_LOCATION[i]);
                }
                return retValue;
            }
        }
        // wsit support
        if (J2eePlatform.TOOL_WSIT.equals(toolName)) {
            if (isToolSupported(J2eePlatform.TOOL_WSIT)) {
                File[] retValue = new File[WSIT_LIBS.length];
                File homeDir = tp.getCatalinaHome();
                for (int i = 0; i < WSIT_LIBS.length; i++) {
                    retValue[i] = new File(homeDir, WSIT_LIBS[i]);
                }
                return retValue;
            }
        }
        return null;
    }
    
    public boolean isToolSupported(String toolName) {
        // jwsdp support
        if (J2eePlatform.TOOL_WSCOMPILE.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < WSCOMPILE_LIBS.length; i++) {
                if (!new File(homeDir, WSCOMPILE_LIBS[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_WSGEN.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            boolean wsit = isToolSupported(J2eePlatform.TOOL_WSIT);
            if (wsit) {
                for (int i = 0; i < WSIT_WSGEN_LIBS.length; i++) {
                    if (!new File(homeDir, WSIT_WSGEN_LIBS[i]).exists()) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < JWSDP_WSGEN_LIBS.length; i++) {
                    if (!new File(homeDir, JWSDP_WSGEN_LIBS[i]).exists()) {
                        return false;
                    }
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_WSIMPORT.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            boolean wsit = isToolSupported(J2eePlatform.TOOL_WSIT);
            
            if (wsit) {
                for (int i = 0; i < WSIT_WSIMPORT_LIBS.length; i++) {
                    if (!new File(homeDir, WSIT_WSIMPORT_LIBS[i]).exists()) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < JWSDP_WSIMPORT_LIBS.length; i++) {
                    if (!new File(homeDir, JWSDP_WSIMPORT_LIBS[i]).exists()) {
                        return false;
                    }
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_JWSDP.equals(toolName)) {
            
            if (isToolSupported(J2eePlatform.TOOL_WSIT)) {
                return true;
            }
            
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < JWSDP_LIBS.length; i++) {
                if (!new File(homeDir, JWSDP_LIBS[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_KEYSTORE.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < KEYSTORE_LOCATION.length; i++) {
                if (!new File(homeDir, KEYSTORE_LOCATION[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_TRUSTSTORE.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < TRUSTSTORE_LOCATION.length; i++) {
                if (!new File(homeDir, TRUSTSTORE_LOCATION[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_KEYSTORE_CLIENT.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < KEYSTORE_CLIENT_LOCATION.length; i++) {
                if (!new File(homeDir, KEYSTORE_CLIENT_LOCATION[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_TRUSTSTORE_CLIENT.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < TRUSTSTORE_CLIENT_LOCATION.length; i++) {
                if (!new File(homeDir, TRUSTSTORE_CLIENT_LOCATION[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_WSIT.equals(toolName)) {
            File homeDir = tp.getCatalinaHome();
            for (int i = 0; i < WSIT_LIBS.length; i++) {
                if (!new File(homeDir, WSIT_LIBS[i]).exists()) {
                    return false;
                }
            }
            return true;
        }
        if (J2eePlatform.TOOL_JSR109.equals(toolName)) {
            return false;
        }
        
        // Test if server has the JAX-WS Tester capability
        if ("jaxws-tester".equals(toolName)) { //NOI18N
            return true;
        }
        
        return false;
    }
        
    public Set/*<Object>*/ getSupportedModuleTypes() {
        Set moduleTypes = new HashSet(1);
        moduleTypes.add(J2eeModule.WAR);
        return moduleTypes;
    }
    
    public Set/*<String>*/ getSupportedSpecVersions() {
        Set specVersions = new HashSet(3);
        specVersions.add(J2eeModule.J2EE_13);
        specVersions.add(J2eeModule.J2EE_14);
        if (manager.isTomcat60()) {
            specVersions.add(J2eeModule.JAVA_EE_5);
        }
        return specVersions;
    }
    
    public Set/*<String>*/ getSupportedJavaPlatformVersions() {
        Set versions = new HashSet();
        versions.add("1.4"); // NOI18N
        versions.add("1.5"); // NOI18N
        return versions;
    }
    
    public JavaPlatform getJavaPlatform() {
        return tp.getJavaPlatform();
    }
    
    public Lookup getLookup() {
        Lookup baseLookup = Lookups.fixed(tp.getCatalinaHome());
        return LookupProviderSupport.createCompositeLookup(baseLookup, "J2EE/DeploymentPlugins/Tomcat5/Lookup"); //NOI18N
//        WSStackSPI jaxWsStack = new TomcatJaxWsStack(tp.getCatalinaHome());
//        return Lookups.fixed(WSStackFactory.createWSStack(jaxWsStack));
    }
    
    // private helper methods -------------------------------------------------
    
    private void loadLibraries(LibraryImplementation lib) {
        lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, tp.getClasses());
        lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, tp.getJavadocs());
        lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_SRC, tp.getSources());        
    }
}
