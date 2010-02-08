/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
    
/**
 *
 * @author Ludo
 */
public class Hk2JavaEEPlatformImpl extends J2eePlatformImpl {
    
    private Hk2DeploymentManager dm;
    private final LibraryImplementation lib = new J2eeLibraryTypeProvider().createLibrary();
    private final LibraryImplementation[] libraries = { lib };
    private Hk2JavaEEPlatformFactory pf;

    /**
     * 
     * @param dm 
     */
    public Hk2JavaEEPlatformImpl(Hk2DeploymentManager dm, Hk2JavaEEPlatformFactory pf) {
        this.dm = dm;
        this.pf = pf;
        String path = dm.getCommonServerSupport().getInstanceProperties().get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
        File f = new File(path, "modules"); // NOI18N
        FileUtil.toFileObject(FileUtil.normalizeFile(f)).addFileChangeListener(new FileChangeListener() {

            public void fileFolderCreated(FileEvent fe) {
                notifyLibrariesChanged();
            }

            public void fileDataCreated(FileEvent fe) {
                notifyLibrariesChanged();
            }

            public void fileChanged(FileEvent fe) {
                notifyLibrariesChanged();
            }

            public void fileDeleted(FileEvent fe) {
                notifyLibrariesChanged();
            }

            public void fileRenamed(FileRenameEvent fe) {
                notifyLibrariesChanged();
            }

            public void fileAttributeChanged(FileAttributeEvent fe) {
                // we can ignore this type of change
            }
        });
        initLibraries();
    }

    // Persistence provider strings
    private static final String PERSISTENCE_PROV_ECLIPSELINK = "org.eclipse.persistence.jpa.PersistenceProvider"; //NOI18N

    // WEB SERVICES PROPERTIES 
    // TODO - shall be removed and usages replaced by values from j2eeserver or websvc apis after api redesign
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
    private static final String KEYSTORE_LOCATION = "config/keystore.jks";
    private static final String TRUSTSTORE_LOCATION = "config/cacerts.jks";

    private static final String EMBEDDED_EJB_CONTAINER_PATH = "lib/embedded/glassfish-embedded-static-shell.jar";

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
        String gfRootStr = dm.getProperties().getGlassfishRoot();
        if (J2eePlatform.TOOL_EMBEDDABLE_EJB.equals(toolName)) {
            File jar = new File(gfRootStr, EMBEDDED_EJB_CONTAINER_PATH);
            return jar.exists() && jar.isFile() && jar.canRead();
        }

        File wsLib = null;
        
        if (gfRootStr != null) {
            wsLib = ServerUtilities.getJarName(gfRootStr, "webservices(|-osgi).jar");
        }

        // WEB SERVICES SUPPORT
        if ((wsLib != null) && (wsLib.exists())) {      // existence of webservice libraries
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
            if (TOOL_JSR109.equals(toolName)) {        //NOI18N
                return true;
            }
            if (TOOL_KEYSTORE.equals(toolName)) {      //NOI18N
                return true;
            }
            if (TOOL_KEYSTORECLIENT.equals(toolName)) {//NOI18N
                return true;
            }
            if (TOOL_TRUSTSTORE.equals(toolName)) {    //NOI18N
                return true;
            }
            if (TOOL_TRUSTSTORECLIENT.equals(toolName)) {  //NOI18N
                return true;
            }
            if (TOOL_WSCOMPILE.equals(toolName)) {     //NOI18N
                if (ServerUtilities.getJarName(gfRootStr, "webservices.jar") != null)
                return true;   // TODO - the support is there - need to find the right classpath then change to true
            }
            if (TOOL_APPCLIENTRUNTIME.equals(toolName)) { //NOI18N
                return true;
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
        String gfRootStr = dm.getProperties().getGlassfishRoot();
        if (null != gfRootStr) {
            if (J2eePlatform.TOOL_EMBEDDABLE_EJB.equals(toolName)) {
                return new File[]{new File(gfRootStr, EMBEDDED_EJB_CONTAINER_PATH)};
            }
            if (TOOL_WSGEN.equals(toolName) || TOOL_WSIMPORT.equals(toolName)) {
                String[] entries = new String[]{"webservices(|-osgi).jar", //NOI18N
                    "webservices-api(|-osgi).jar", //NOI18N
                    "jaxb(|-osgi).jar", //NOI18N
                    "jaxb-api(|-osgi).jar", //NOI18N
                    "javax.activation.jar"}; //NOI18N
                List<File> cPath = new ArrayList<File>();

                for (String entry : entries) {
                    File f = ServerUtilities.getWsJarName(gfRootStr, entry);
                    if ((f != null) && (f.exists())) {
                        cPath.add(f);
                    }
                }
                return cPath.toArray(new File[cPath.size()]);
            }

            if (TOOL_WSCOMPILE.equals(toolName)) {
                String[] entries = new String[]{"webservices", //NOI18N
                    "javax.activation"}; //NOI18N
                List<File> cPath = new ArrayList<File>();

                for (String entry : entries) {
                    File f = ServerUtilities.getWsJarName(gfRootStr, entry);
                    if ((f != null) && (f.exists())) {
                        cPath.add(f);
                    }
                }
                return cPath.toArray(new File[cPath.size()]);
            }

            File domainDir = null;
            File gfRoot = new File(gfRootStr);
            if ((gfRoot != null) && (gfRoot.exists())) {
                String domainDirName = dm.getProperties().getDomainDir();
                if (domainDirName != null) {
                    domainDir = new File(domainDirName);

                    if (TOOL_KEYSTORE.equals(toolName) || TOOL_KEYSTORECLIENT.equals(toolName)) {
                        return new File[]{
                                    new File(domainDir, KEYSTORE_LOCATION) //NOI18N
                                };
                    }

                    if (TOOL_TRUSTSTORE.equals(toolName) || TOOL_TRUSTSTORECLIENT.equals(toolName)) {
                        return new File[]{
                                    new File(domainDir, TRUSTSTORE_LOCATION) //NOI18N
                                };
                    }
                }
            }
        } else {
            Logger.getLogger("glassfish-javaee").log(Level.INFO, "dm has no root???", new Exception());
        }
        
        return new File[0];
    }

    @Override
    public Set<Profile> getSupportedProfiles() {
        return getCorrectedProfileSet();
    }
    
    @Override
    public Set<Profile> getSupportedProfiles(J2eeModule.Type type) {
        return getCorrectedProfileSet();
    }

    private Set<Profile> getCorrectedProfileSet() {
        Set<Profile> retVal = pf.getSupportedProfiles();
        String gfRootStr = dm.getProperties().getGlassfishRoot();
        File descriminator = new File(gfRootStr,"modules/appclient-server-core.jar");
        if (!descriminator.exists()) {
            retVal.remove(Profile.JAVA_EE_6_FULL);
        }
        return retVal;
    }

    @Override
    public Set<Type> getSupportedTypes() {
        Set<Type> retVal = pf.getSupportedTypes();
        Set<Profile> ps = getCorrectedProfileSet();
        if (ps.contains(Profile.JAVA_EE_6_WEB) && !ps.contains(Profile.JAVA_EE_6_FULL)) {
            retVal.remove(Type.CAR);
            retVal.remove(Type.EAR);
            retVal.remove(Type.EJB);
            retVal.remove(Type.RAR);
        }
        return retVal;
    }

    
    /**
     * 
     * @return 
     */
    public java.io.File[] getPlatformRoots() {
        String gfRootStr = dm.getProperties().getGlassfishRoot();
        File returnedElement;
        File[] retVal = new File[0];
        if (gfRootStr != null) {
            returnedElement = new File(gfRootStr);
            if (returnedElement.exists()) {
                retVal = new File[] { returnedElement };
            }
        }
        return retVal;
    }
    
    /**
     * 
     * @return 
     */
    public LibraryImplementation[] getLibraries() {
        return libraries.clone();
    }
    
    /**
     * 
     * @return 
     */
    public java.awt.Image getIcon() {
        return ImageUtilities.loadImage("org/netbeans/modules/j2ee/hk2/resources/server.gif"); // NOI18N
    }
    
    /**
     * 
     * @return 
     */
    public String getDisplayName() {
        return pf.getDisplayName();
    }
    
    /**
     * 
     * @return 
     */
    public Set getSupportedJavaPlatformVersions() {
        return pf.getSupportedJavaPlatforms();
    }
    
    /**
     * 
     * @return 
     */
    public JavaPlatform getJavaPlatform() {
        return pf.getJavaPlatform();
    }
    
    /**
     * 
     */
    public void notifyLibrariesChanged() {
        initLibraries();
    }

    private static RequestProcessor libInitThread =
            new RequestProcessor("init libs -- Hk2JavaEEPlatformImpl");
    
    private void initLibraries() {
        libInitThread.post(new Runnable() {

            @Override
            public void run() {
                lib.setName(pf.getLibraryName());
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, dm.getProperties().getClasses());
                lib.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_JAVADOC, dm.getProperties().getJavadocs());
                firePropertyChange(PROP_LIBRARIES, null, libraries.clone());
            }
        });
    }
    
    @Override
    public Lookup getLookup() {
        String gfRootStr = dm.getProperties().getGlassfishRoot();
        Lookup baseLookup = Lookups.fixed(gfRootStr);
        return LookupProviderSupport.createCompositeLookup(baseLookup, pf.getLookupKey()); 
//
//        WSStackSPI metroStack = new GlassfishJaxWsStack(gfRootStr);
//        return Lookups.fixed(WSStackFactory.createWSStack(metroStack));
    }
    /* return the string within quotes
     **/
    private String quotedString(String s){
        return "\""+s+"\"";
    }
    @Override
    public String getToolProperty(String toolName, String propertyName) {
        if (J2eePlatform.TOOL_APP_CLIENT_RUNTIME.equals(toolName)) {
            File root = new File(dm.getProperties().getGlassfishRoot());
            String domainPath = dm.getProperties().getDomainDir();
            if (J2eePlatform.TOOL_PROP_MAIN_CLASS.equals(propertyName)) {
                return "org.glassfish.appclient.client.AppClientFacade"; // NOI18N
            }
            if (J2eePlatform.TOOL_PROP_MAIN_CLASS_ARGS.equals(propertyName)) {
                return "${j2ee.appclient.tool.args}"; // NOI18N
            }
            if (J2eePlatform.TOOL_PROP_JVM_OPTS.equals(propertyName)) {
                if(domainPath != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("-Djava.endorsed.dirs=");
                     sb.append(quotedString(new File(root,"lib/endorsed").getAbsolutePath()));
                    sb.append(File.pathSeparator);
                     sb.append(quotedString(new File(root,"modules/endorsed").getAbsolutePath()));
                     sb.append(" -javaagent:");
                     sb.append(quotedString(new File(root,"modules/gf-client.jar").getAbsolutePath()));
                      sb.append("=mode=acscript,arg=-configxml,arg=");
                      sb.append(quotedString(new File(domainPath, "config/sun-acc.xml").getAbsolutePath()));
                      sb.append(",client=jar=");
//                  sb.append(""); // path to /tmp/test/ApplicationClient1Client.jar
//                   sb.append(" -jar ");
//                    sb.append(""); // path to /tmp/test/ApplicationClient1Client.jar
                    return sb.toString();
                } else {
                    return null;
                }
            }
            if (J2eePlatform.TOOL_PROP_CLIENT_JAR_LOCATION.equals(propertyName)) {
                if(domainPath != null) {
                    FileObject location = FileUtil.toFileObject(FileUtil.normalizeFile(new File(domainPath)));
                    if (location == null) {
                        return null;
                    }
                    return (FileUtil.toFile(location).getAbsolutePath())+File.separator+"generated"+File.separator+"xml"; // NOI18N
                } else {
                    return null;
                }
            }
            if ("j2ee.appclient.args".equals(propertyName)) { // NOI18N
                return null; // "-configxml " + quotedString(new File(dmProps.getLocation(), dmProps.getDomainName() +
                // "/config/sun-acc.xml").getAbsolutePath()); // NOI18N
            }
        }
        return null;
    }
}
