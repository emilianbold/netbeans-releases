/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.web.WebAppMetadataModelFactory;
import org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.j2ee.BaseEEModuleImpl;
import org.netbeans.modules.maven.j2ee.J2eeMavenSourcesImpl;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;


/**
 * war/webapp related apis implementation..
 * @author  Milos Kleint 
 */
public class WebModuleImpl extends BaseEEModuleImpl implements WebModuleImplementation2, J2eeModuleImplementation2 {

    private static final String WEB_INF = "WEB-INF"; //NOI18N
    private MetadataModel<WebAppMetadata> webAppMetadataModel;
    private MetadataModel<WebAppMetadata> webAppAnnMetadataModel;
    private MetadataModel<WebservicesMetadata> webservicesMetadataModel;
    private boolean inplace = false;


    public WebModuleImpl(Project project, WebModuleProviderImpl provider) {
        super(project, provider, "web.xml", J2eeModule.WEB_XML); //NOI18N
    }
    
        
    @Override
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.WAR;
    }
    
    @Override
    public FileObject getArchive() throws IOException {
        return getArchive(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_WAR, "war", "war");
    }
    
    /*****************************
     *  WebModule related methods
     *****************************/
    @Override
    public FileObject getWebInf() {
        FileObject root = getDocumentBase();
        if (root != null) {
            return root.getFileObject(WEB_INF); //NOI18N
        }
        return null;
    }

    /**
     * Creates new WEB-INF folder in the web root.
     *
     * @return {@code FileObject} of the WEB-INF folder or {@code null} in cases of
     * missing document base directory
     * @throws IOException if the folder failed to be created
     */
    public FileObject createWebInf() throws IOException {
        FileObject root = getDocumentBase();
        if (root != null) {
            return root.createFolder(WEB_INF);
        }
        return null;
    }
    
    @Override
    public FileObject getDocumentBase() {
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] grp = srcs.getSourceGroups(J2eeMavenSourcesImpl.TYPE_DOC_ROOT);
        if (grp.length > 0) {
            return grp[0].getRootFolder();
        }
        return null;
    }
    
    /**
     * to be used to denote that a war:inplace goal is used to build the web app.
     */
    public void setWarInplace(boolean inplace) {
        this.inplace = inplace;
    }

    @Override
    public Profile getJ2eeProfile() {
        Profile propProfile = getPropertyJ2eeProfile();
        Profile descriptorProfile = getDescriptorJ2eeProfile();
        if (descriptorProfile != null) {
            if (descriptorProfile.equals(Profile.JAVA_EE_6_WEB) && propProfile != null && propProfile.equals(Profile.JAVA_EE_6_FULL)) {
                //override the default in the descriptor value..
                return propProfile;
            }
            return descriptorProfile;
        } else {
            if (propProfile != null) {
                return propProfile;
            }
            //has DD but we didn't figure the version??
            return Profile.JAVA_EE_5;
        }
    }

    public Profile getPropertyJ2eeProfile() {
        //try to apply the hint if it exists.
        AuxiliaryProperties prop = project.getLookup().lookup(AuxiliaryProperties.class);
        if (prop != null) {
            // you may wonder how this can be null.. the story goes like this:
            // if called from the J2eeLookupProvider constructor, thus from
            // project lookup construction loop, the reentrant call to the lookup
            // doesn't include the AuxProperties instances yet..
            // too bad.. Not sure how may people use this feature/workaround anyway..
            String version = prop.get(MavenJavaEEConstants.HINT_J2EE_VERSION, true);
            if (version != null) {
                return Profile.fromPropertiesString(version);
            }
        }
        return null;
    }

    public Profile getDescriptorJ2eeProfile() {
        DDProvider prov = DDProvider.getDefault();
        FileObject dd = getDeploymentDescriptor();
        if (dd != null) {
            try {
                WebApp wa = prov.getDDRoot(dd);
                String waVersion = wa.getVersion() ;

                if (WebApp.VERSION_2_4.equals(waVersion)) {
                    return Profile.J2EE_14;
                }
                if (WebApp.VERSION_2_5.equals(waVersion)) {
                    return Profile.JAVA_EE_5;
                }
                if (WebApp.VERSION_3_0.equals(waVersion)) {
                    return Profile.JAVA_EE_6_WEB;
                }
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            }
            return null;
        } else {
            return Profile.JAVA_EE_6_WEB;
        }
    }

    @Override
    public File getDDFile(final String path) {
        URI webappDir = mavenproject().getWebAppDirectory();
        File file = new File(new File(webappDir), path);
        
        return FileUtil.normalizeFile(file);
    }
    
    @Override
    public FileObject getDeploymentDescriptor() {
        File dd = getDDFile(J2eeModule.WEB_XML);
        if (dd != null) {
            return FileUtil.toFileObject(dd);
        }
        return null;
    }
    
    @Override
    public String getContextPath() {
        Profile prof = getJ2eeProfile();
        // #170528the javaee6 level might not have a descriptor,
        // but I still keep the check for older versions, as it was known to fail without one
        // in older versions it probably means the web.xml file is generated..
        if(getDeploymentDescriptor() != null || prof == Profile.JAVA_EE_6_FULL || prof == Profile.JAVA_EE_6_WEB) {
            try {
                String path = provider.getConfigSupport().getWebContextRoot();
                if (path != null) {
                    return path;
                }
            } catch (ConfigurationException e) {
                // TODO #95280: inform the user that the context root cannot be retrieved
            }        
        }
        return "/" + mavenproject().getMavenProject().getArtifactId(); //NOI18N;
    }
    
    public void setContextPath(String newPath) {
        //TODO store as pom profile configuration, probably for the deploy-plugin.
        Profile prof = getJ2eeProfile();
        // #170528 the javaee6 level might not have a descriptor,
        // but I still keep the check for older versions, as it was known to fail without one
        // in older versions it probably means the web.xml file is generated..
        if (getDeploymentDescriptor() != null|| prof == Profile.JAVA_EE_6_FULL || prof == Profile.JAVA_EE_6_WEB) {
            try {
                provider.getConfigSupport().setWebContextRoot(newPath);
            }
            catch (ConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    } 
    
    @Override
    public String getModuleVersion() {
        WebApp wapp = getWebApp ();
        String version = null;
        if (wapp != null) {
            version = wapp.getVersion();
        }
        if (version == null) {
            version = WebApp.VERSION_3_0;
        }
        return version;
    }
    
    private WebApp getWebApp () {
        try {
            FileObject deploymentDescriptor = getDeploymentDescriptor ();
            if(deploymentDescriptor != null) {
                return DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            }
        } catch (java.io.IOException e) {
            ErrorManager.getDefault ().log (e.getLocalizedMessage ());
        }
        return null;
    }    

    @Override
    public FileObject getContentDirectory() throws IOException {
        FileObject webappFO;
        if (inplace) {
            webappFO = getDocumentBase();
        } else {
            MavenProject mavenProject = mavenproject().getMavenProject();
            String webappLocation = PluginPropertyUtils.getPluginProperty(project,
                Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_WAR,
                "webappDirectory", "war", null); //NOI18N
            if (webappLocation == null) {
                webappLocation = mavenProject.getBuild().getDirectory() + File.separator + mavenProject.getBuild().getFinalName();
            }
            File webapp = FileUtilities.resolveFilePath(FileUtil.toFile(project.getProjectDirectory()), webappLocation);
            webappFO = FileUtil.toFileObject(webapp);
        }
        if (webappFO != null) {
            webappFO.refresh();
        }
        return webappFO;
    }
    
    @Override
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == WebAppMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getAnnotationMetadataModel();
            return model;
        } else if (type == WebservicesMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getWebservicesMetadataModel();
            return model;
        }
        return null;
    }
    
    @Override
    public synchronized MetadataModel<WebAppMetadata> getMetadataModel() {
        if (webAppMetadataModel == null) {
            FileObject ddFO = getDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            ProjectSourcesClassPathProvider cpProvider = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            webAppMetadataModel = WebAppMetadataModelFactory.createMetadataModel(metadataUnit, true);
        }
        return webAppMetadataModel;
    }
    
    private synchronized MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        if (webservicesMetadataModel == null) {
            FileObject ddFO = getWebServicesDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            ProjectSourcesClassPathProvider cpProvider = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            webservicesMetadataModel = WebservicesMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return webservicesMetadataModel;
    }

    private FileObject getWebServicesDeploymentDescriptor() {
        FileObject root = getDocumentBase();
        if (root != null) {
            return root.getFileObject(J2eeModule.WEBSERVICES_XML);
        }
        return null;
    }
    
    /**
     * The server plugin needs all models to be either merged on annotation-based. 
     * Currently only the web model does a bit of merging, other models don't. So
     * for web we actually need two models (one for the server plugins and another
     * for everyone else). Temporary solution until merging is implemented
     * in all models.
     */
    public synchronized MetadataModel<WebAppMetadata> getAnnotationMetadataModel() {
        if (webAppAnnMetadataModel == null) {
            FileObject ddFO = getDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            ProjectSourcesClassPathProvider cpProvider = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            webAppAnnMetadataModel = WebAppMetadataModelFactory.createMetadataModel(metadataUnit, false);
        }
        return webAppAnnMetadataModel;
    }
    
}
