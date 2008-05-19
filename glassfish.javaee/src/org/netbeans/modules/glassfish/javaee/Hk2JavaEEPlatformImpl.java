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


package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.glassfish.javaee.ide.Hk2PluginProperties;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 *
 * @author Ludo
 */
public class Hk2JavaEEPlatformImpl extends J2eePlatformImpl {
    
    private Hk2PluginProperties properties;
    private Hk2DeploymentManager dm;
    private LibraryImplementation[] libraries;
    
    /**
     * 
     * @param dm 
     */
    public Hk2JavaEEPlatformImpl(Hk2DeploymentManager dm) {
        this.dm = dm;
        this.properties = dm.getProperties();
        initLibraries();
    }
    
    // Persistence provider strings
    private static final String PERSISTENCE_PROV_ECLIPSELINK = "org.eclipse.persistence.jpa.PersistenceProvider"; //NOI18N

    // WEB SERVICES PROPERTIES 
    // TODO - shall be removed and usages replaced by values from j2eeserver or websvc apis after redesign
    private static final String TOOL_WSCOMPILE = "wscompile";
    private static final String TOOL_JSR109 = "jsr109";
    private static final String TOOL_WSIMPORT = "wsimport";
    private static final String TOOL_WSGEN = "wsgen";
    private static final String TOOL_KEYSTORE = "keystore";
    private static final String TOOL_KEYSTORECLIENT = "keystoreClient";
    private static final String TOOL_TRUSTSTORE = "truststore";
    private static final String TOOL_TRUSTSTORECLIENT = "truststoreClient";
    private static final String TOOL_WSIT = "wsit";
    private static final String TOOL_JAXWSTESTER = "jaxws-tester";
    private static final String TOOL_APPCLIENTRUNTIME = "appClientRuntime";
    
    /**
     * 
     * @param toolName 
     * @return 
     */
    public boolean isToolSupported(String toolName) {
        // Persistence Providers
        if(PERSISTENCE_PROV_ECLIPSELINK.equals(toolName)) {
            return true;
        }

        if("org.hibernate.ejb.HibernatePersistence".equals(toolName) || //NOI18N
                "oracle.toplink.essentials.PersistenceProvider".equals(toolName) || // NOI18N
                "kodo.persistence.PersistenceProviderImpl".equals(toolName) || // NOI18N
                "org.apache.openjpa.persistence.PersistenceProviderImpl".equals(toolName)) { //NOI18N
            return true;
        }
        
        if("defaultPersistenceProviderJavaEE5".equals(toolName)) {  //NOI18N
            return true;
        }
        if("eclipseLinkPersistenceProviderIsDefault".equals(toolName)) {
            return true;
        }        

        // WEB SERVICES SUPPORT
        if (true) { // - check for existence of webservices libraries
            if (TOOL_WSGEN.equals(toolName)) {         //NOI18N
                return true;
            }
            if (TOOL_WSIMPORT.equals(toolName)) {      //NOI18N
                return true;
            }
            if (TOOL_WSIT.equals(toolName)) {          //NOI18N
                return true;
            }
            if (TOOL_JAXWSTESTER.equals(toolName)) {  //NOI18N
                return true;
            }
            if (TOOL_WSCOMPILE.equals(toolName)) {     //NOI18N
                return false;   // TODO - the support is there - need to find the right classpath then change to true
            }
            if (TOOL_JSR109.equals(toolName)) {        //NOI18N
                return false;   //TODO - when the support becomes available, change to true
            }
            if (TOOL_KEYSTORE.equals(toolName)) {      //NOI18N
                return false;   // TODO - when the support becomes available, change to true
            }
            if (TOOL_KEYSTORECLIENT.equals(toolName)) {//NOI18N
                return false;   // TODO - when the support becomes available, change to true
            }
            if (TOOL_TRUSTSTORE.equals(toolName)) {    //NOI18N
                return false;    // TODO  - when the support becomes available, change to true
            }
            if (TOOL_TRUSTSTORECLIENT.equals(toolName)) {  //NOI18N
                return false;    // TODO  - when the support becomes available, change to true
            }
            if (TOOL_APPCLIENTRUNTIME.equals(toolName)) { //NOI18N
                return false;    //TODO - when the support becomes available, change to true
            }
        }
        
        return false;     
    }
    
    /**
     * 
     * @param toolName 
     * @return 
     */
    public File[] getToolClasspathEntries(String toolName) {

//        if (TOOL_WSGEN.equals(toolName) || TOOL_WSIMPORT.equals(toolName)) {
//            return new File[] {
//                new File(root, TOOLS_JAR),        //NOI18N
//                new File(root, JSTL_JAR),         //NOI18N
//                new File(root, JAVA_EE_JAR),      //NOI18N
//                new File(root, APPSERV_WS_JAR),   //NOI18N
//                new File(root, MAIL_JAR),         //NOI18N
//                new File(root, ACTIVATION_JAR)    //NOI18N
//            }
//        }
        
        return new File[0];
    }
    
    /**
     * 
     * @return 
     */
    public Set getSupportedSpecVersions() {
        Set<String> result = new HashSet<String>();
        result.add(J2eeModule.J2EE_14);
        result.add(J2eeModule.JAVA_EE_5);
        return result;
    }
    
    /**
     * 
     * @return 
     */
    public Set getSupportedModuleTypes() {
        Set<Object> result = new HashSet<Object>();
        result.add(J2eeModule.WAR);
        return result;
    }
    
    /**
     * 
     * @return 
     */
    public java.io.File[] getPlatformRoots() {
        return new File[0];
    }
    
    /**
     * 
     * @return 
     */
    public LibraryImplementation[] getLibraries() {
        return libraries;
    }
    
    /**
     * 
     * @return 
     */
    public java.awt.Image getIcon() {
        return Utilities.loadImage("org/netbeans/modules/j2ee/hk2/resources/server.gif"); // NOI18N
        
    }
    
    /**
     * 
     * @return 
     */
    public String getDisplayName() {
        return NbBundle.getMessage(Hk2JavaEEPlatformImpl.class, "MSG_MyServerPlatform");
    }
    
    /**
     * 
     * @return 
     */
    public Set getSupportedJavaPlatformVersions() {
        Set<String> versions = new HashSet<String>();
        versions.add("1.4"); // NOI18N
        versions.add("1.5"); // NOI18N
        return versions;
    }
    
    /**
     * 
     * @return 
     */
    public JavaPlatform getJavaPlatform() {
        return JavaPlatformManager.getDefault().getDefaultPlatform();
    }
    
    /**
     * 
     */
    public void notifyLibrariesChanged() {
        initLibraries();
        firePropertyChange(PROP_LIBRARIES, null, libraries.clone());
    }
    
    private void initLibraries() {

        LibraryImplementation lib = new J2eeLibraryTypeProvider().createLibrary();
        lib.setName(NbBundle.getMessage(Hk2JavaEEPlatformImpl.class, "LBL_LIBRARY"));
        lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, properties.getClasses());
        libraries = new LibraryImplementation[] {lib};
    }
}
