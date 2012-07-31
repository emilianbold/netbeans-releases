/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2;
import org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport;
import org.netbeans.modules.javaee.specs.support.api.JaxRpc;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
    
/**
 *
 * @author Ludo
 */
public class Hk2JavaEEPlatformImpl extends J2eePlatformImpl2 {
    
    private Hk2DeploymentManager dm;
    private final LibraryImplementation lib = new J2eeLibraryTypeProvider().createLibrary();
    private final LibraryImplementation[] libraries = { lib };
    private Hk2JavaEEPlatformFactory pf;
    private FileChangeListener fcl;
    /** Keep local Lookup instance to be returned by getLookup method. */
    private volatile Lookup lkp;

    /**
     * 
     * @param dm 
     */
    public Hk2JavaEEPlatformImpl(Hk2DeploymentManager dm, Hk2JavaEEPlatformFactory pf) {
        this.dm = dm;
        this.pf = pf;
        addFcl();
        initLibraries();
    }

    private void addFcl() {
        if (null == fcl) {
            String path = dm.getCommonServerSupport().getInstanceProperties().get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
            File f = new File(path, "modules"); // NOI18N
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
            if (null == fo) {
                Logger.getLogger("glassfish-javaee").log(Level.WARNING, "{0} did not exist but should", f.getAbsolutePath());
                return;
            }
            fcl = new FileChangeListener() {

                @Override
                public void fileFolderCreated(FileEvent fe) {
                    notifyLibrariesChanged();
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    notifyLibrariesChanged();
                }

                @Override
                public void fileChanged(FileEvent fe) {
                    notifyLibrariesChanged();
                }

                @Override
                public void fileDeleted(FileEvent fe) {
                    notifyLibrariesChanged();
                }

                @Override
                public void fileRenamed(FileRenameEvent fe) {
                    notifyLibrariesChanged();
                }

                @Override
                public void fileAttributeChanged(FileAttributeEvent fe) {
                    // we can ignore this type of change
                }
            };
            fo.addFileChangeListener(fcl);
        }
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
    @Override
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
            if (TOOL_WSCOMPILE.equals(toolName)) {  //NOI18N
                return true;
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
    @Override
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
                String[] entries = new String[] {"webservices(|-osgi).jar"}; //NOI18N
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
    @Override
    public java.io.File[] getPlatformRoots() {
        File server = getServerHome();
        if (server != null) {
            return new File[] {server};
        }
        return new File[]{};
    }

    @Override
    public File getServerHome() {
        return getExistingFolder(dm.getProperties().getGlassfishRoot());
    }

    @Override
    public File getDomainHome() {
        return getExistingFolder(dm.getProperties().getDomainDir());
    }

    @Override
    public File getMiddlewareHome() {
        return getExistingFolder(dm.getProperties().getInstallRoot());
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public LibraryImplementation[] getLibraries() {
        addFcl();
        return libraries.clone();
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public java.awt.Image getIcon() {
        return ImageUtilities.loadImage("org/netbeans/modules/j2ee/hk2/resources/server.gif"); // NOI18N
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public String getDisplayName() {
        return pf.getDisplayName();
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public Set getSupportedJavaPlatformVersions() {
        return pf.getSupportedJavaPlatforms();
    }
    
    /**
     * 
     * @return 
     */
    @Override
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

    /**
     * Return Java EE platform lookup instance or create a new one if no such instance exists.
     *
     * @return Platform lookup instance.
     */
    @Override
    public Lookup getLookup() {
        // Avoid locking when instance already exists.
        if (lkp != null)
            return lkp;
        // Create only one for the first time.
        else {
            synchronized (this) {
                if (lkp == null) {
                    String gfRootStr = dm.getProperties().getGlassfishRoot();
                    WSStack<JaxWs> wsStack = WSStackFactory.createWSStack(JaxWs.class,
                            new Hk2JaxWsStack(gfRootStr, this), WSStack.Source.SERVER);
                    WSStack<JaxRpc> rpcStack = WSStackFactory.createWSStack(JaxRpc.class,
                            new Hk2JaxRpcStack(gfRootStr), WSStack.Source.SERVER);
                    Lookup baseLookup = Lookups.fixed(gfRootStr, new JaxRsStackSupportImpl(),
                            wsStack, rpcStack);
                    lkp = LookupProviderSupport.createCompositeLookup(baseLookup, pf.getLookupKey());
                }
            }
        }
        return lkp;
    }

    private File getExistingFolder(String path) {
        if (path != null) {
            File returnedElement = new File(path);
            if (returnedElement.exists()) {
                return returnedElement;
            }
        }
        return null;
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
                     String url = dm.getCommonServerSupport().getInstanceProperties().get(GlassfishModule.URL_ATTR);
                     File f = new File(root,"lib/gf-client.jar"); // NOI18N
                     if (f.exists()) {
                        sb.append(quotedString(f.getAbsolutePath()));
                     } else {
                        sb.append(quotedString(new File(root,"modules/gf-client.jar").getAbsolutePath()));
                    }
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
    
    private class JaxRsStackSupportImpl implements JaxRsStackSupportImplementation {
        
        private static final String VERSION_30X = "v3";     // NOI18N
        private static final String VERSION_31X = "3.1";    // NOI18N

        /* (non-Javadoc)
         * @see org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation#addJsr311Api(org.netbeans.api.project.Project)
         */
        @Override
        public boolean addJsr311Api( Project project ) {
            String version = getGFVersion();
            try {
                if (version == null) {
                    return false;
                } 
                else if (version.startsWith(VERSION_30X)) {
                    File jsr311 = ServerUtilities.getJarName(dm.getProperties().
                            getGlassfishRoot(), "jsr311-api.jar");          // NOI18N
                    if ( jsr311== null || !jsr311.exists()){
                        return false;
                    }
                    return addJars(project, Collections.singletonList(
                            jsr311.toURI().toURL()));
                } 
                else if (version.startsWith(VERSION_31X)) {
                    File jerseyCore = ServerUtilities.getJarName(dm.getProperties().
                            getGlassfishRoot(), "jersey-core.jar");          // NOI18N
                    if ( jerseyCore== null || !jerseyCore.exists()){
                        return false;
                    }
                    return addJars(project, Collections.singletonList(
                            jerseyCore.toURI().toURL()));
                }
            } catch (MalformedURLException ex) {
                return false;
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation#extendsJerseyProjectClasspath(org.netbeans.api.project.Project)
         */
        @Override
        public boolean extendsJerseyProjectClasspath( Project project ) {
            List<URL> urls = getJerseyLibraryURLs();
            if ( urls.size() >0 ){
                return addJars( project , urls );
            }
            return false;
        }
        
        @Override
        public void removeJaxRsLibraries(Project project) {
            List<URL> urls = getJerseyLibraryURLs();
            if ( urls.size() >0 ){
                SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                    JavaProjectConstants.SOURCES_TYPE_JAVA);
                if (sourceGroups == null || sourceGroups.length < 1) {
                    return;
                }
                FileObject sourceRoot = sourceGroups[0].getRootFolder();
                String[] classPathTypes = new String[]{ ClassPath.COMPILE , ClassPath.EXECUTE };
                for (String type : classPathTypes) {
                    try {
                        ProjectClassPathModifier.removeRoots(urls.toArray( 
                            new URL[ urls.size()]), sourceRoot, type);
                    }    
                    catch(UnsupportedOperationException ex) {
                        Logger.getLogger( JaxRsStackSupportImpl.class.getName() ).
                                log (Level.INFO, null , ex );
                    }
                    catch( IOException e ){
                        Logger.getLogger( JaxRsStackSupportImpl.class.getName() ).
                                log(Level.INFO, null , e );
                    }
                }     
            }
        }
        
        @Override
        public void configureCustomJersey( Project project ){
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation#isBundled(java.lang.String)
         */
        @Override
        public boolean isBundled( String classFqn ) {
            // TODO Auto-generated method stub
            return false;
        }
        
        private boolean hasJee6Profile(){
            Set<Profile> profiles = getSupportedProfiles();
            return profiles.contains(Profile.JAVA_EE_6_FULL) || 
                profiles.contains(Profile.JAVA_EE_6_WEB) ;
        }
        
        private List<URL> getJerseyLibraryURLs() {
            String version = getGFVersion();
            String gfRoot = dm.getProperties().getGlassfishRoot();
            List<URL> urls = new LinkedList<URL>();
            if ( version == null ){
                return Collections.emptyList();
            }
            else if ( version.startsWith( VERSION_30X )){
                File jackson = ServerUtilities.getJarName( gfRoot, 
                        "jackson(|-core-asl).jar");          // NOI18N
                addURL( urls , jackson);
                File jerseyBundle = ServerUtilities.getJarName( gfRoot, 
                        "jersey-gf-bundle.jar");          // NOI18N
                addURL( urls , jerseyBundle);
                File jettison = ServerUtilities.getJarName( gfRoot, 
                        "jettison.jar");          // NOI18N
                addURL( urls , jettison);
                File jsr311 = ServerUtilities.getJarName(gfRoot, 
                        "jsr311-api.jar");          // NOI18N
                addURL( urls , jsr311);
                File miltipart = ServerUtilities.getJarName(gfRoot, 
                        "jersey-multipart.jar");          // NOI18N
                addURL( urls , miltipart);
                File mimepull = ServerUtilities.getJarName(gfRoot, 
                        "mimepull.jar");          // NOI18N
                addURL( urls , mimepull);
                File asm = ServerUtilities.getJarName(gfRoot, 
                        "asm-all-repackaged.jar");          // NOI18N
                addURL( urls , asm);
            }
            else if ( version.startsWith( VERSION_31X )){
                File jackson = ServerUtilities.getJarName( gfRoot, 
                        "jackson(-core-asl).jar");          // NOI18N
                addURL( urls , jackson);
                File jacksonJaxRs = ServerUtilities.getJarName( gfRoot, 
                        "jackson-jaxrs.jar");          // NOI18N
                addURL( urls , jacksonJaxRs);
                File jacksonMapper = ServerUtilities.getJarName( gfRoot, 
                        "jackson-mapper(-asl).jar");          // NOI18N
                addURL( urls , jacksonMapper);
                File jerseyServer = ServerUtilities.getJarName( gfRoot, 
                        "jersey-gf-server.jar");          // NOI18N
                addURL( urls , jerseyServer);
                File jettison = ServerUtilities.getJarName( gfRoot, 
                        "jettison.jar");          // NOI18N
                addURL( urls , jettison);
                File miltipart = ServerUtilities.getJarName(gfRoot, 
                        "jersey-multipart.jar");          // NOI18N
                addURL( urls , miltipart);
                File mimepull = ServerUtilities.getJarName(gfRoot, 
                        "mimepull.jar");          // NOI18N
                addURL( urls , mimepull);
                File jerseyClient = ServerUtilities.getJarName( gfRoot, 
                        "jersey-client");          // NOI18N
                addURL( urls , jerseyClient);
                File jerseyCore = ServerUtilities.getJarName( gfRoot, 
                        "jersey-core");          // NOI18N
                addURL( urls , jerseyCore);
                File jerseyJson = ServerUtilities.getJarName( gfRoot, 
                        "jersey-json");          // NOI18N
                addURL( urls , jerseyJson);
                File asm = ServerUtilities.getJarName(gfRoot, 
                    "asm-all-repackaged.jar");          // NOI18N
                addURL( urls , asm);
            }
            return urls;
        }
        
        private void addURL( Collection<URL> urls, File file ){
            if ( file == null || !file.exists()) {
                return;
            }
            try {
                urls.add( file.toURI().toURL());
            } catch (MalformedURLException ex) {
                // ignore the file
            }
        }
        
        private boolean addJars( Project project, Collection<URL> jars ){
            List<URL> urls = new ArrayList<URL>();
            for (URL url : jars) {
                if ( FileUtil.isArchiveFile( url)){
                    urls.add(FileUtil.getArchiveRoot(url));
                }
            }
            SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (sourceGroups == null || sourceGroups.length < 1) {
               return false;
            }
            FileObject sourceRoot = sourceGroups[0].getRootFolder();
            try {
                String classPathType;
                if ( hasJee6Profile() ){
                    classPathType = JavaClassPathConstants.COMPILE_ONLY;
                }
                else {
                    classPathType = ClassPath.COMPILE;
                }
                ProjectClassPathModifier.addRoots(urls.toArray( new URL[ urls.size()]), 
                        sourceRoot, classPathType );
            } 
            catch(UnsupportedOperationException ex) {
                return false;
            }
            catch ( IOException e ){
                return false;
            }
            return true;
        }
        
        private String getGFVersion() {
            String gfRootStr = dm.getProperties().getGlassfishRoot();
            File serviceTag = new File(gfRootStr,
                    "lib/registration/servicetag-registry.xml");
            if (!serviceTag.exists()) {
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(
                        Level.WARNING, "Couldn't recognize GF version",
                        new Exception());
                return null;
            }
            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
                SAXParser saxParser = factory.newSAXParser();
                RegistrationHandler handler = new RegistrationHandler();
                saxParser.parse(serviceTag, handler);
                return handler.getVersion();
            }
            catch (ParserConfigurationException e) {
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(
                        Level.INFO, null, e);
            }
            catch (SAXException e) {
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(
                        Level.INFO, null, e);
            }
            catch (IOException e){
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(
                        Level.INFO, null, e);
            }
            return "";
        }
    }
    
    private static class RegistrationHandler extends DefaultHandler {
        
        private static final String VERSION_TAG = "product_version";    // NOI18N

        @Override
        public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException 
        {
            super.startElement(uri, localName, qName, attributes);
            if (VERSION_TAG.equals( localName )|| VERSION_TAG.equals( qName )){
                versionTag = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) 
                throws SAXException 
        {
            super.endElement(uri, localName, qName);
            if (VERSION_TAG.equals( localName )|| VERSION_TAG.equals( qName )){
                versionTag = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if ( versionTag ){
                version.append(ch, start, length);
            }
        }
        
        String getVersion() {
            return version.toString();
        }
        
        private boolean versionTag;
        private StringBuilder version = new StringBuilder();
    }
}
