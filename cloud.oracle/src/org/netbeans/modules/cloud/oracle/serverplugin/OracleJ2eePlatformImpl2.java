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
package org.netbeans.modules.cloud.oracle.serverplugin;

import java.awt.Image;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.ArrayList;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.cloud.oracle.OracleInstance;
import org.netbeans.modules.j2ee.deployment.common.api.J2eeLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2;
import org.netbeans.modules.j2ee.deployment.plugins.spi.support.LookupProviderSupport;
import org.netbeans.libs.oracle.cloud.api.WhiteListQuerySupport;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.javaee.specs.support.spi.JpaProviderFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class OracleJ2eePlatformImpl2 extends J2eePlatformImpl2 implements ChangeListener {

    private OracleDeploymentManager dm;
    private final Set<Type> moduleTypes = new HashSet<Type>();
    

    public OracleJ2eePlatformImpl2(DeploymentManager dm) {
        assert dm instanceof OracleDeploymentManager;
        this.dm = (OracleDeploymentManager)dm;
        moduleTypes.add(Type.WAR);
        // deployment of EJB standalone module is not supported but user
        // should be able to create EJB project for cloud and deploy it
        // as part of EAR
        moduleTypes.add(Type.EJB);
        moduleTypes.add(Type.EAR);
        this.dm.addOnPremiseServerInstanceIdListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        firePropertyChange(PROP_LIBRARIES, null, getLibraries());
    }
    
    @Override
    public File getServerHome() {
        return null;
    }

    @Override
    public File getDomainHome() {
        return null;
    }

    @Override
    public File getMiddlewareHome() {
        return null;
    }

    @Override
    public LibraryImplementation[] getLibraries() {
        LibraryImplementation libs[] = getOnPremiseServerClasspath();
        if (libs == null) {
            libs = getJavaEELibrary(Collections.<ServerLibraryDependency>emptySet());
        }
        return libs;
    }
    
    @Override
    public LibraryImplementation[] getLibraries(Set<ServerLibraryDependency> libraries) {
        if (dm.getOnPremiseServiceInstanceId() == null) {
            return getJavaEELibrary(libraries);
        }
        try {
            J2eePlatform platform = Deployment.getDefault().getServerInstance(dm.getOnPremiseServiceInstanceId()).getJ2eePlatform();
            if (platform != null) {
                return createLibraryForFiles(platform.getClasspathEntries(libraries));
            }
        } catch (InstanceRemovedException ex) {
            // ignore
        }
        
        return getJavaEELibrary(libraries);
    }
        
    private LibraryImplementation[] getOnPremiseServerClasspath() {
        if (dm.getOnPremiseServiceInstanceId() == null) {
            return null;
        }
        File files[] = null;
        try {
            files = Deployment.getDefault().getServerInstance(dm.getOnPremiseServiceInstanceId()).getJ2eePlatform().getClasspathEntries();
        } catch (InstanceRemovedException ex) {
            return null;
        }
        return createLibraryForFiles(files);
    }
    
    private LibraryImplementation[] createLibraryForFiles(File[] files) {
        List<URL> urls = new ArrayList<URL>();
        for (File f : files) {
            try {
                urls.add(f.toURI().toURL());
            } catch (MalformedURLException ex) {
                // ignore
            }
        }
        if (urls.isEmpty()) {
            return null;
        }
        LibraryImplementation library = new J2eeLibraryTypeProvider().createLibrary();
        library.setName("temporary");
        library.setContent(J2eeLibraryTypeProvider.VOLUME_TYPE_CLASSPATH, urls);
        return new LibraryImplementation[]{library};
    }
    
    private LibraryImplementation[] getJavaEELibrary(Set<ServerLibraryDependency> libraries) {
        Library l = LibraryManager.getDefault().getLibrary("javaee-api-5.0");
        
        LibraryImplementation library = new J2eeLibraryTypeProvider().createLibrary();

        // set its name
        library.setName("JavaEEAPI");

        List<URL> cp = new ArrayList<URL>();
        
        for (ServerLibraryDependency dep : libraries) {
            if (dep.getName().equals("jsf") && dep.getSpecificationVersion(). // NOI18N
                    isAboveOrEqual(Version.fromDottedNotationWithFallback("2.0"))) { // NOI18N
                Library jsf = LibraryManager.getDefault().getLibrary("jsf20"); // NOI18N
                if (jsf != null) {
                    cp.addAll(jsf.getContent("classpath")); // NOI18N
                }
            }
        }
        Library eclipselink = LibraryManager.getDefault().getLibrary("eclipselink"); // NOI18N
        if ( eclipselink != null) {
            cp.addAll(eclipselink.getContent("classpath")); // NOI18N
        }
        
        cp.addAll(l.getContent("classpath")); // NOI18N
        
        library.setContent(J2eeLibraryTypeProvider.
                VOLUME_TYPE_CLASSPATH, cp);
        
        return new LibraryImplementation[]{library};
    }
    
    @Override
    public Set<Type> getSupportedTypes() {
        return moduleTypes;
    }

    @Override
    public Set<Profile> getSupportedProfiles() {
        // enabling EE 6 profile would cause many problems (eg. lite EJBs in Web Project,
        // no web.xml created by default, Servlet 3.0 spec, etc.) so it is better to stick with EE 5 here
        return new HashSet<Profile>(Arrays.<Profile>asList(new Profile[]{/*Profile.JAVA_EE_6_FULL,*/ Profile.JAVA_EE_5}));
    }

    @Override
    public Set<Profile> getSupportedProfiles(Type moduleType) {
        return getSupportedProfiles();
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(OracleJ2eePlatformImpl2.class, "OracleJ2eePlatformImpl2.displayName");
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("org/netbeans/modules/cloud/oracle/resources/weblogic.png"); // NOI18N
    }

    @Override
    public File[] getPlatformRoots() {
        return new File[0];
    }

    @Override
    public File[] getToolClasspathEntries(String toolName) {
        return new File[0];
    }

    @Override
    @Deprecated
    public boolean isToolSupported(String toolName) {
        return false;
    }

    @Override
    public Set getSupportedJavaPlatformVersions() {
        return new HashSet<String>(Arrays.asList(new String[] {"1.6"}));
    }

    @Override
    public org.netbeans.api.java.platform.JavaPlatform getJavaPlatform() {
        return JavaPlatformManager.getDefault().getDefaultPlatform();
    }

    @Override
    public Lookup getLookup() {
        File f = OracleInstance.findWeblogicJar(dm.getOnPremiseServiceInstanceId());
        return LookupProviderSupport.createCompositeLookup(
                Lookups.fixed(
                    WhiteListQuerySupport.createCloud9WhiteListQueryImpl(),
                    JpaProviderFactory.createJpaProvider("", true, true, false),
                    new JpaSupportImpl()
                    ), "J2EE/DeploymentPlugins/Oracle Cloud/Lookup"); //NOI18N
    }
}
