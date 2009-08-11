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

package org.netbeans.modules.maven.j2ee.ejb;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.ejb.EjbJarMetadataModelFactory;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.RootedEntry;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation2;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * implementation of ejb netbeans functionality
 * @author Milos Kleint 
 */
public class EjbJarImpl implements EjbJarImplementation2, J2eeModuleImplementation2, ModuleChangeReporter {
    
    private Project project;
    private List versionListeners;
    
    private EjbModuleProviderImpl provider;

    private MetadataModel<EjbJarMetadata> ejbJarMetadataModel;
    private MetadataModel<WebservicesMetadata> webservicesMetadataModel;
    private NbMavenProject mavenproject;
    
    
    /** Creates a new instance of EjbJarImpl */
    EjbJarImpl(Project proj, EjbModuleProviderImpl prov) {
        project = proj;
        versionListeners = new ArrayList();
        provider = prov;
        mavenproject = project.getLookup().lookup(NbMavenProject.class);
    }
    
    boolean isValid() {
        //TODO any checks necessary??.
        return true;
    }

    public Profile getJ2eeProfile() {
        //try to apply the hint if it exists.
        String version = project.getLookup().lookup(AuxiliaryProperties.class).get(Constants.HINT_J2EE_VERSION, true);
        if (version != null) {
            return Profile.fromPropertiesString(version);
        }
        String ver = getModuleVersion();
        if (EjbJar.VERSION_2_1.equals(ver)) {
            return Profile.J2EE_14;
        }
        if (EjbJar.VERSION_3_0.equals(ver)) {
            return Profile.JAVA_EE_5;
        }
        if (EjbJar.VERSION_3_1.equals(ver)) {
            return Profile.JAVA_EE_6_FULL;
        }
        return Profile.J2EE_14;
    }
    
    /** META-INF folder for the web module.
     */
    
    public FileObject getMetaInf() {
        Sources srcs = ProjectUtils.getSources(project);
        if (srcs != null) {
            SourceGroup[] grp = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
            for (int i = 0; i < grp.length; i++) {
                FileObject fo = grp[i].getRootFolder().getFileObject("META-INF"); //NOI18N
                if (fo != null) {
                    return fo;
                }
            }
        }
        return null;
    }
    
    File getDDFile(String path) {
        URI[] dir = mavenproject.getResources(false);
        File fil = new File(new File(dir[0]), path);
        fil = FileUtil.normalizeFile(fil);
//        System.out.println("EjbM:getDDFile=" + fil.getAbsolutePath());
        
        return fil;
    }
    
    /** Deployment descriptor (ejb-jar.xml file) of the ejb module.
     */
    public FileObject getDeploymentDescriptor() {
        FileObject metaInf = getMetaInf();
        if (metaInf != null) {
            return metaInf.getFileObject("ejb-jar.xml"); //NOI18N
        }
        return null;
    }
    
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
    
    public String getModuleVersion() {
        DDProvider prov = DDProvider.getDefault();
        FileObject dd = getDeploymentDescriptor();
        if (dd != null) {
            try {
                EjbJar ejb = prov.getDDRoot(dd);
                String ejbVersion = ejb.getVersion().toString();
                return ejbVersion;
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            }
        }
        //look in pom's config.
        String version = PluginPropertyUtils.getPluginProperty(project,
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_EJB,
                "ejbVersion", "ejb"); //NOI18N
        if (version != null) {
            return version.trim();
        }
        // in case there is no descriptor, we probably have 3.x spec stuff?
        //TODO we cannot differenciate ee5 and ee6 at this point, most cases shall
        // be coved by the previous cases
       return EjbJar.VERSION_3_0;
    }
    
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.EJB;
    }
    
    /**
     * Returns the location of the module within the application archive.
     */
    public String getUrl() {
        return "/" + mavenproject.getMavenProject().getBuild().getFinalName(); //NOI18N
    }
    
    
    /**
     * Returns the archive file for the module of null if the archive file
     * does not exist (for example, has not been compiled yet).
     */
    public FileObject getArchive() throws IOException {
        //TODO get the correct values for the plugin properties..
        String jarfile = PluginPropertyUtils.getPluginProperty(project,
                    Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_EJB, 
                    "jarName", "ejb"); //NOI18N
        MavenProject proj = mavenproject.getMavenProject();
        if (jarfile == null) {
            jarfile = proj.getBuild().getFinalName();
        }
        String loc = proj.getBuild().getDirectory();
        File fil = FileUtil.normalizeFile(new File(loc, jarfile + ".jar")); //NOI18N
//        System.out.println("get ejb archive=" + fil);
        return FileUtil.toFileObject(fil);
        
    }
    
    /**
     * Returns the contents of the archive, in copyable form.
     *  Used for incremental deployment.
     *  Currently uses its own {@link RootedEntry} interface.
     *  If the J2eeModule instance describes a
     *  j2ee application, the result should not contain module archives.
     *
     * @return Iterator through {@link RootedEntry}s
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
    
    /**
     * This call is used in in-place deployment.
     *  Returns the directory staging the contents of the archive
     *  This directory is the one from which the content entries returned
     *  by {@link #getArchiveContents} came from.
     *
     * @return FileObject for the content directory
     */
    public FileObject getContentDirectory() throws IOException {
        String loc = mavenproject.getMavenProject().getBuild().getOutputDirectory();
        File fil = FileUtil.normalizeFile(new File(loc));
//        System.out.println("ejb jar..get content=" + fil);
        FileObject fo = FileUtil.toFileObject(fil.getParentFile());
        if (fo != null) {
            fo.refresh();
        }
        return FileUtil.toFileObject(fil);
    }
    
    
    /**
     * Returns a live bean representing the final deployment descriptor
     * that will be used for deploment of the module. This can be
     * taken from sources, constructed on fly or a combination of these
     * but it needs to be available even if the module has not been built yet.
     *
     * @param location Parameterized by location because of possibility of multiple
     * deployment descriptors for a single module (jsp.xml, webservices.xml, etc).
     * Location must be prefixed by /META-INF or /WEB-INF as appropriate.
     * @return a live bean representing the final DD
     */
    public RootInterface getDeploymentDescriptor(String location) {
        if ("ejb-jar.xml".equals(location)) { //NOI18N
            location = J2eeModule.EJBJAR_XML;
        }
        if (J2eeModule.EJBJAR_XML.equals(location)) {
            try {
                FileObject content = getContentDirectory();
                if (content == null) {
                    URI[] uris = mavenproject.getResources(false);
                    if (uris.length > 0) {
                        content = URLMapper.findFileObject(uris[0].toURL());
                    }
                }
                if (content != null) {
                    FileObject deploymentDescriptor = content.getFileObject(J2eeModule.EJBJAR_XML);
                    if(deploymentDescriptor != null) {
                        return DDProvider.getDefault().getDDRoot(deploymentDescriptor);
                    }
                }
             } catch (IOException e) {
                ErrorManager.getDefault().log(e.getLocalizedMessage());
             }
        }
//        System.out.println("no dd for=" + location);
        return null;
        
    }
    
    
    
    public EjbChangeDescriptor getEjbChanges(long timestamp) {
        return new EjbChange();
    }
    
    public boolean isManifestChanged(long timestamp) {
        //TODO
        return false;
    }

//TODO
    private class EjbChange implements EjbChangeDescriptor {
        public boolean ejbsChanged() {
            return false;
        }
        
        public String[] getChangedEjbs() {
            return new String[0];
        }
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
        File toRet = new File(FileUtil.toFile(project.getProjectDirectory()), "src" + File.separator + "main" + File.separator + "setup");//NOI18N
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
        String path = provider.getConfigSupport().getContentRelativePath(name);
        if (path == null) {
            path = name;
        }
        return getDDFile(path);
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

    public synchronized MetadataModel<EjbJarMetadata> getMetadataModel() {
        if (ejbJarMetadataModel == null) {
            FileObject ddFO = getDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            ProjectSourcesClassPathProvider cpProvider = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            ejbJarMetadataModel = EjbJarMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return ejbJarMetadataModel;
    }

    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == EjbJarMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getMetadataModel();
            return model;
        } else if (type == WebservicesMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getWebservicesMetadataModel();
            return model;
        }
        return null;
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

    /** Deployment descriptor (META-INF/webservices.xml file) of the ejb module.
     */
    private FileObject getWebServicesDeploymentDescriptor() {
        FileObject metaInf = getMetaInf();
        if (metaInf != null) {
            return metaInf.getFileObject("webservices.xml"); //NOI18N
        }
        return null;
    }

    
}
