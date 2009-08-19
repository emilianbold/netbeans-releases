/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.j2ee.J2eeMavenSourcesImpl;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.web.WebAppMetadataModelFactory;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;


/**
 * war/webapp related apis implementation..
 * @author  Milos Kleint 
 */
public class WebModuleImpl implements WebModuleImplementation2, J2eeModuleImplementation2 {
    private Project project;
    private WebModuleProviderImpl provider;
    private MetadataModel<WebAppMetadata> webAppMetadataModel;
    private MetadataModel<WebAppMetadata> webAppAnnMetadataModel;
    private MetadataModel<WebservicesMetadata> webservicesMetadataModel;
    
    private boolean inplace = false;
    private NbMavenProject mavenproject;
    
    public WebModuleImpl(Project proj, WebModuleProviderImpl prov) {
        project = proj;
        mavenproject = project.getLookup().lookup(NbMavenProject.class);
        provider = prov;
    }
    
    public FileObject getWebInf() {
        FileObject root = getDocumentBase();
        if (root != null) {
            return root.getFileObject("WEB-INF"); //NOI18N
        }
        return null;
    }
    
    /**
     * to be used to denote that a war:inplace goal is used to build the web app.
     */
    public void setWarInplace(boolean inplace) {
        this.inplace = inplace;
    }

    public Profile getJ2eeProfile() {
        //try to apply the hint if it exists.
        String version = project.getLookup().lookup(AuxiliaryProperties.class).get(Constants.HINT_J2EE_VERSION, true);
        if (version != null) {
            return Profile.fromPropertiesString(version);
        }
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
            //make 15 the default..
            //TODO how to differentiate with 1.6??
            return Profile.JAVA_EE_5;
        } else {
            return Profile.JAVA_EE_6_WEB;
        }
    }
    
    public FileObject getDocumentBase() {
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] grp = srcs.getSourceGroups(J2eeMavenSourcesImpl.TYPE_DOC_ROOT);
        if (grp.length > 0) {
            return grp[0].getRootFolder();
        }
//        System.out.println("NO DOCUMENT BASE!!! " + project.getProjectDirectory());
        return null;
    }
    
    File getDDFile(final String path) {
        String webxmlDefined = PluginPropertyUtils.getPluginProperty(project,
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_WAR, //NOI18N
                "webXml", "war"); //NOI18N
        if (webxmlDefined != null) {
            //TODO custom location.. relative or absolute? what the *&#! is the default resolved to?
        }
        URI dir = mavenproject.getWebAppDirectory();
        File fil = new File(new File(dir), path);
        fil = FileUtil.normalizeFile(fil);
        return fil;
    }
    
    public FileObject getDeploymentDescriptor() {
        File dd = getDDFile(J2eeModule.WEB_XML);
//        System.out.println("getDDFIle=" + dd);
        if (dd != null) {
            return FileUtil.toFileObject(dd);
        }
        return null;
    }
    
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
        String toRet =  "/" + mavenproject.getMavenProject().getBuild().getFinalName(); //NOI18N
//        System.out.println("get context path=" + toRet);
        return toRet;
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
    
    
    public boolean isValid() {
        //TODO any checks necessary?
        return true;
    }
    
    //88888888888888888888888888888888888888888888888888888888888888888888888888
    // methods of j2eeModule
    //88888888888888888888888888888888888888888888888888888888888888888888888888
    
    public String getModuleVersion() {
        WebApp wapp = getWebApp ();
        String version = WebApp.VERSION_2_5;
        if (wapp != null)
            version = wapp.getVersion();
        return version;
    }
    
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.WAR;
    }
    
    /**
     * @inherit
     */
    public String getUrl() {
        String toRet =  "/" + mavenproject.getMavenProject().getBuild().getFinalName(); //NOI18N
        return toRet;
    }
    
    /**
     * @inherit
     */
    public FileObject getArchive() throws IOException {
        //TODO get the correct values for the plugin properties..
        MavenProject proj = mavenproject.getMavenProject();
        //MEVENIDE-591 - find the lcoation according to plugin config
        String loc = proj.getBuild().getDirectory();
        String finalName = PluginPropertyUtils.getPluginProperty(project,
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_WAR, //NOI18N
                "warName", "war"); //NOI18N        
        if (finalName == null) {
            finalName = proj.getBuild().getFinalName();
        }
        
        File fil = FileUtil.normalizeFile(new File(loc, finalName + ".war")); //NOI18N
//        System.out.println("get archive=" + fil);
        return FileUtil.toFileObject(fil);
    }

    /**
     * @inherit
     *
     * according to sharold@netbeans.org this should return the iterator over
     * non-warred file, meaning from the expanded webapp. weird.
     */
    public Iterator getArchiveContents() throws IOException {
        
//        System.out.println("get archive content");
        FileObject fo = getContentDirectory();
        if (fo != null) {
            return new ContentIterator(fo);
        }
        return null;
    }
    
    public FileObject getContentDirectory() throws IOException {
        FileObject fo;
        if (inplace) {
            fo = getDocumentBase();
        } else {
            MavenProject proj = mavenproject.getMavenProject();
            String loc = PluginPropertyUtils.getPluginProperty(project,
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_WAR, //NOI18N
                "webappDirectory", "war"); //NOI18N        
            if (loc == null) {
                String finalName = proj.getBuild().getFinalName();
                loc = proj.getBuild().getDirectory() + File.separator + finalName;
            }
            File fil = FileUtilities.resolveFilePath(FileUtil.toFile(project.getProjectDirectory()), loc);
    //        System.out.println("get content=" + fil);
            fo = FileUtil.toFileObject(fil);
        } 
        if (fo != null) {
            fo.refresh();
        }
        return fo;
    }
    
    
    private WebApp getWebApp () {
        try {
            FileObject deploymentDescriptor = getDeploymentDescriptor ();
            if(deploymentDescriptor != null) {
                return DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            }
        } catch (java.io.IOException e) {
            org.openide.ErrorManager.getDefault ().log (e.getLocalizedMessage ());
        }
        return null;
    }    
    
    //TODO this probably also adds test sources.. is that correct?
    /**
     * @inherit
     * @return
     */
    @SuppressWarnings("deprecation")
    public FileObject[] getJavaSources() {
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] gr = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        List<FileObject> toRet = new ArrayList<FileObject>();
        if (gr != null) {
            for (int i = 0; i < gr.length; i++) {
                toRet.add(gr[i].getRootFolder());
            }
        }
        return toRet.toArray(new FileObject[toRet.size()]);
    }
    
    
    // inspired by netbeans' webmodule codebase, not really sure what is the point
    // of the iterator..
    private static final class ContentIterator implements Iterator {
        private ArrayList<FileObject> ch;
        private FileObject root;
        
        private ContentIterator(FileObject f) {
            this.ch = new ArrayList<FileObject>();
            ch.add(f);
            this.root = f;
        }
        
        public boolean hasNext() {
            return ! ch.isEmpty();
        }
        
        public Object next() {
            FileObject f = ch.get(0);
            ch.remove(0);
            if (f.isFolder()) {
                f.refresh();
                FileObject[] chArr = f.getChildren();
                for (int i = 0; i < chArr.length; i++) {
                    ch.add(chArr [i]);
                }
            }
            return new FSRootRE(root, f);
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private static final class FSRootRE implements J2eeModule.RootedEntry {
        private FileObject f;
        private FileObject root;
        
        FSRootRE(FileObject rt, FileObject fo) {
            f = fo;
            root = rt;
        }
        
        public FileObject getFileObject() {
            return f;
        }
        
        public String getRelativePath() {
            return FileUtil.getRelativePath(root, f);
        }
    }

    /**
     * Returns the module resource directory, or null if the module has no resource
     * directory.
     * 
     * @return the module resource directory, or null if the module has no resource
     *         directory.
     */

    public File getResourceDirectory() {
        //TODO .. in ant projects equals to "setup" directory.. what's it's use?
        File toRet = new File(FileUtil.toFile(project.getProjectDirectory()), "src" + File.separator + "main" + File.separator + "setup"); //NOI18N
        return toRet;
    }

    /**
     * Returns source deployment configuration file path for the given deployment 
     * configuration file name.
     *
     * @param name file name of the deployment configuration file, WEB-INF/sun-web.xml
     *        for example.
     * 
     * @return absolute path to the deployment configuration file, or null if the
     *         specified file name is not known to this J2eeModule.
     */
    public File getDeploymentConfigurationFile(String name) {
       if (name == null) {
            return null;
        }
        if ("web.xml".equals(name)) { //NOI18N
            name = J2eeModule.WEB_XML;
        } else {
            String path = provider.getConfigSupport().getContentRelativePath(name);
            if (path != null) {
                name = path;
            }
        }
        return getDDFile(name);
    }

   /**
     * Add a PropertyChangeListener to the listener list.
     * 
     * @param listener PropertyChangeListener
     */
    public void addPropertyChangeListener(PropertyChangeListener arg0) {
        //TODO..
    }
    
    /**
     * Remove a PropertyChangeListener from the listener list.
     * 
     * @param listener PropertyChangeListener
     */
    public void removePropertyChangeListener(PropertyChangeListener arg0) {
        //TODO..
    }

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

    /** Deployment descriptor (WEB-INF/webservices.xml file) of the web module.
     */
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
