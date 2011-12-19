/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.weblogic9.j2ee;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.ui.BrokenServerLibrarySupport;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.j2ee.deployment.plugins.spi.ServerLibraryFactory;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibraryManager;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibrarySupport;
import org.netbeans.modules.j2ee.weblogic9.config.WLServerLibrarySupport.WLServerLibrary;
import org.netbeans.modules.javaee.specs.support.spi.JaxRsStackSupportImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Denis Anisimov
 */
class JaxRsStackSupportImpl implements JaxRsStackSupportImplementation {
    private static final String API = "api"; // NOI18N
    private static final String JAX_RS = "jax-rs"; // NOI18N
    private static final String JERSEY = "jersey"; //NOI18N
    private static final String JSON = "json"; //NOI18N
    private static final String JETTISON = "jettison"; //NOI18N
    private static final String ROME = "rome"; //NOI18N

    private final WLJ2eePlatformFactory.J2eePlatformImplImpl platformImpl;

    JaxRsStackSupportImpl(WLJ2eePlatformFactory.J2eePlatformImplImpl platformImpl) {
        this.platformImpl = platformImpl;
    }

    @Override
    public boolean addJsr311Api(Project project) {
        if ( hasJee6Profile() ){
            FileObject core = getJarFile("com.sun.jersey.core_");   // NOI18N
            if ( core!= null ){
                try {
                    return addJars(project, Collections.singleton( core.getURL() ));
                }
                catch( FileStateInvalidException e ){
                    Logger.getLogger(JaxRsStackSupportImpl.class.getName()).
                        log(Level.WARNING, 
                                "Exception during extending a project classpath", e); //NOI18N
                    return false;
                }
            }
        }
        return addJsr311ServerLibraryApi(project);
    }

    @Override
    public boolean extendsJerseyProjectClasspath(Project project) {
        if ( hasJee6Profile() ){
            try {
                List<URL> urls = getJerseyJars();
                return addJars(project,  urls );
            }
            catch( FileStateInvalidException e ){
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).
                log(Level.WARNING, 
                        "Exception during extending a project classpath", e); //NOI18N
                return false;
            }
        }
        return extendsJerseyServerLibraries(project);
    }

    @Override
    public void removeJaxRsLibraries(Project project) {
        if ( hasJee6Profile() ){
            try {
                List<URL> urls = getJerseyJars();
                FileObject core = getJarFile("com.sun.jersey.core_");   // NOI18N
                if ( core!= null ){
                    urls.add( core.getURL() );
                }
                removeLibraries(project,  urls );
            }
            catch( FileStateInvalidException e ){
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).
                log(Level.WARNING, 
                        "Exception during extending a project classpath", e); //NOI18N
            }
        }
    }

    private boolean extendsJerseyServerLibraries( Project project ) {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        Collection<ServerLibrary> serverLibraries = getServerJerseyLibraries();
        if (provider != null && serverLibraries.size() > 0) {
            try {
                for (ServerLibrary serverLibrary : serverLibraries) {
                    provider.getConfigSupport().configureLibrary(ServerLibraryDependency.minimalVersion(serverLibrary.getName(), serverLibrary.getSpecificationVersion(), serverLibrary.getImplementationVersion()));
                }
                Preferences prefs = ProjectUtils.getPreferences(project, ProjectUtils.class, true);
                prefs.put(BrokenServerLibrarySupport.OFFER_LIBRARY_DEPLOYMENT, Boolean.TRUE.toString());
                return true;
            } catch (org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException ex) {
                Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(Level.INFO, 
                        "Exception during extending a project classpath", ex); //NOI18N
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean hasJee6Profile(){
        Set<Profile> profiles = platformImpl.getSupportedProfiles();
        return profiles.contains(Profile.JAVA_EE_6_FULL) || 
                profiles.contains(Profile.JAVA_EE_6_WEB) ;
    }
    
    private List<URL> getJerseyJars() throws FileStateInvalidException {
        FileObject client = getJarFile("com.sun.jersey.client_");   // NOI18N
        List<URL> urls = new LinkedList<URL>();
        if ( client != null){
            urls.add( client.getURL());
        }
        FileObject json = getJarFile("com.sun.jersey.json_");       // NOI18N
        if ( json != null){
            urls.add( json.getURL());
        }
        FileObject multipart = getJarFile("com.sun.jersey.multipart_");// NOI18N
        if ( multipart != null){
            urls.add( multipart.getURL());
        }
        FileObject server = getJarFile("com.sun.jersey.server_");       // NOI18N
        if ( server != null){
            urls.add( server.getURL());
        }
        FileObject asl = getJarFile("org.codehaus.jackson.core.asl_");  // NOI18N
        if ( asl != null){
            urls.add( asl.getURL());
        }
        FileObject jacksonJaxRs = getJarFile("org.codehaus.jackson.jaxrs_");// NOI18N
        if ( jacksonJaxRs != null){
            urls.add( jacksonJaxRs.getURL());
        }
        FileObject jacksonMapper = getJarFile("org.codehaus.jackson.mapper.asl_");// NOI18N
        if ( jacksonMapper != null){
            urls.add( jacksonMapper.getURL());
        }
        FileObject jacksonXc = getJarFile("org.codehaus.jackson.xc_");// NOI18N
        if ( jacksonXc != null){
            urls.add( jacksonXc.getURL());
        }
        FileObject jettison = getJarFile("org.codehaus.jettison_");// NOI18N
        if ( jettison != null){
            urls.add( jettison.getURL());
        }
        return urls;
    }
    
    private boolean addJsr311ServerLibraryApi( Project project ) {
        /*
         *  WL has a deployable JSR311 war. But it will appear in the project's
         *  classpath only after specific user action. This is unacceptable
         *  because generated source code requires classes independently
         *  of additional explicit user actions.
         *
         *  So the following code returns true only if there is already deployed
         *  JSR311 library on the server
         */
        WLServerLibrarySupport support = getLibrarySupport();
        Set<WLServerLibrary> libraries = support.getDeployedLibraries();
        for (WLServerLibrary library : libraries) {
            String title = library.getImplementationTitle();
            if (title != null && title.toLowerCase(Locale.ENGLISH).contains(JAX_RS) && title.toLowerCase(Locale.ENGLISH).contains(API)) {
                ServerLibrary apiLib = ServerLibraryFactory.createServerLibrary(library);
                J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
                try {
                    provider.getConfigSupport().configureLibrary(ServerLibraryDependency.minimalVersion(apiLib.getName(), apiLib.getSpecificationVersion(), apiLib.getImplementationVersion()));
                } catch (org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException ex) {
                    Logger.getLogger(JaxRsStackSupportImpl.class.getName()).log(Level.INFO, null, ex);
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private Collection<ServerLibrary> getServerJerseyLibraries() {
        WLServerLibraryManager manager = getLibraryManager();
        Collection<ServerLibrary> libraries = new LinkedList<ServerLibrary>();
        libraries.addAll(findJerseyLibraries(manager.getDeployableLibraries()));
        libraries.addAll(findJerseyLibraries(manager.getDeployedLibraries()));
        return libraries;
    }

    private Collection<ServerLibrary> findJerseyLibraries(Collection<ServerLibrary> collection) {
        Collection<ServerLibrary> result = new ArrayList<ServerLibrary>(collection.size());
        for (Iterator<ServerLibrary> iterator = collection.iterator(); iterator.hasNext();) {
            ServerLibrary library = iterator.next();
            String title = library.getImplementationTitle();
            if (title == null) {
                continue;
            }
            title = title.toLowerCase(Locale.ENGLISH);
            if (title.contains(JERSEY) || title.contains(JSON) || title.contains(ROME) || title.contains(JETTISON)) {
                result.add(library);
            }
        }
        return result;
    }

    private WLServerLibraryManager getLibraryManager() {
        return new WLServerLibraryManager(platformImpl.getDeploymentManager());
    }

    private WLServerLibrarySupport getLibrarySupport() {
        return new WLServerLibrarySupport(platformImpl.getDeploymentManager());
    }
    
    private FileObject getModulesFolder(){
        File middlewareHome = platformImpl.getMiddlewareHome();
        FileObject middlware = FileUtil.toFileObject( FileUtil.normalizeFile( middlewareHome));
        if ( middlware == null ){
            return null;
        }
        FileObject modules = middlware.getFileObject("modules");     // NOI18N
        return modules;
    }
    
    private FileObject getJarFile( String startName ){
        FileObject modulesFolder = getModulesFolder();
        if ( modulesFolder == null ){
            return null;
        }
        FileObject[] children = modulesFolder.getChildren();
        for (FileObject child : children) {
            if ( child.getName().startsWith( startName) && child.hasExt( "jar")){    //  NOI18N
                return child;
            }
        }
        return null;
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
            ProjectClassPathModifier.addRoots(urls.toArray( new URL[ urls.size()]), 
                    sourceRoot, ClassPath.COMPILE);
        } 
        catch(UnsupportedOperationException ex) {
            return false;
        }
        catch ( IOException e ){
            return false;
        }
        return true;
    }
    
    private void removeLibraries(Project project, Collection<URL> urls) {
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

}
