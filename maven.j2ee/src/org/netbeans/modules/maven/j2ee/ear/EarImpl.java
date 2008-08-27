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

package org.netbeans.modules.maven.j2ee.ear;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.ear.model.ApplicationMetadataModelImpl;
import org.netbeans.modules.maven.spi.debug.AdditionalDebuggedProjects;
import hidden.org.codehaus.plexus.util.StringInputStream;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * implementation of ear related netbeans functionality
 * @author Milos Kleint 
 */
class EarImpl implements EarImplementation, 
                         J2eeModuleImplementation, 
                         J2eeApplicationImplementation, 
                         ModuleChangeReporter, 
                         AdditionalDebuggedProjects {

    private Project project;
    private EarModuleProviderImpl provider;
    private MetadataModel<ApplicationMetadata> metadataModel;
    private NbMavenProject mavenproject;
    
    /** Creates a new instance of EarImpl */
    EarImpl(Project proj, EarModuleProviderImpl prov) {
        project = proj;
        mavenproject = project.getLookup().lookup(NbMavenProject.class);
        
        provider = prov;
    }

    /** J2EE platform version - one of the constants 
     * defined in {@link org.netbeans.modules.j2ee.api.common.EjbProjectConstants}.
     * @return J2EE platform version
     */
    public String getJ2eePlatformVersion() {
        //try to apply the hint if it exists.
        String version = project.getLookup().lookup(AuxiliaryProperties.class).get(Constants.HINT_J2EE_VERSION, true);
        if (version != null) {
            return version;
        }
        if (isApplicationXmlGenerated()) {
            version = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, 
                                              Constants.PLUGIN_EAR, "version", "generate-application-xml"); //NOI18N
            // the default version in maven plugin is also 1.3
            //TODO what if the default changes?
            if (version != null) {
                // 5 is not valid value in netbeans, it's 1.5
                if ("5".equals(version)) {
                    return EjbProjectConstants.JAVA_EE_5_LEVEL;
                }
                return version.trim();
            }
        } else {
            DDProvider prov = DDProvider.getDefault();
            FileObject dd = getDeploymentDescriptor();
            if (dd != null) {
                try {
                    Application app = prov.getDDRoot(dd);
                    String appVersion = app.getVersion().toString();
                    return appVersion;
                } catch (IOException exc) {
                    ErrorManager.getDefault().notify(exc);
                }
            }
        }
        // hardwire?
//        System.out.println("eariml: getj2eepaltform");
        return EjbProjectConstants.J2EE_14_LEVEL;
    }

    /** META-INF folder for the Ear.
     */
    public FileObject getMetaInf() {
        String appsrcloc =  PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, 
                                              Constants.PLUGIN_EAR, "earSourceDirectory", "ear");//NOI18N
        if (appsrcloc == null) {
            appsrcloc = "src/main/application";//NOI18N
        }
        URI dir = FileUtilities.getDirURI(project.getProjectDirectory(), appsrcloc);
        FileObject root = FileUtilities.convertURItoFileObject(dir);
        if (root == null) {
            File fil = new File(dir);
            fil.mkdirs();
            project.getProjectDirectory().refresh();
            root = FileUtil.toFileObject(fil);
        }
        if (root != null) {
            FileObject metainf = root.getFileObject("META-INF");//NOI18N
            if (metainf == null) {
                try {
                    metainf = root.createFolder("META-INF");
                } catch (IOException iOException) {
                    Exceptions.printStackTrace(iOException);
                }
            }
            return metainf;
        }
        return null;
    }

    /** Deployment descriptor (application.xml file) of the ejb module.
     */
    public FileObject getDeploymentDescriptor() {
        if (isApplicationXmlGenerated()) {
            String generatedLoc = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, 
                                              Constants.PLUGIN_EAR, "generatedDescriptorLocation", "generate-application-xml");//NOI18N
            if (generatedLoc == null) {
                generatedLoc = mavenproject.getMavenProject().getBuild().getDirectory();
            }
            FileObject fo = FileUtilities.convertURItoFileObject(FileUtilities.getDirURI(project.getProjectDirectory(), generatedLoc));
            if (fo != null) {
                return fo.getFileObject("application.xml");//NOI18N
            } else {
                //TODO maybe run the generate-resources phase to get a DD
//                System.out.println("we don't have the application.xmk generated yet at=" + generatedLoc);
            }
        }
        String customLoc =  PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, 
                                              Constants.PLUGIN_EAR, "applicationXml", "ear");//NOI18N
        if (customLoc != null) {
            FileObject fo = FileUtilities.convertURItoFileObject(FileUtilities.getDirURI(project.getProjectDirectory(), customLoc));
            if (fo != null) {
                return fo;
            }
        }

        return null;
    }

    /** Add j2ee webmodule into application.
     * @param module the module to be added
     */
    public void addWebModule(WebModule webModule) {
        //TODO this probably means adding the module as dependency to the pom.
        throw new IllegalStateException("Not implemented for maven based projects.");//NOI18N
    }

    /** Add j2ee ejbjar module into application.
     * @param module the module to be added
     */
    public void addEjbJarModule(EjbJar ejbJar) {
        //TODO this probably means adding the module as dependency to the pom.
        throw new IllegalStateException("Not implemented for maven based projects.");//NOI18N
    }
    
    private boolean isApplicationXmlGenerated() {
        String str = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, 
                                                                    Constants.PLUGIN_EAR, 
                                                                    "generateApplicationXml", //NOI18N
                                                                    "generate-application-xml");//NOI18N
            //either the default or explicitly set generation of application.xml file 
        return (str == null || Boolean.valueOf(str).booleanValue());
    }

    boolean isValid() {
        //TODO how to check and what to check for..
        return true;
    }

    public Object getModuleType() {
        return J2eeModule.EAR;
    }

    /**
     * Returns module specification version
     */
    public String getModuleVersion() {
//        System.out.println("earimpl: get module version");
        //TODO??
        return J2eeModule.J2EE_14;
    }

    /**
     * Returns the location of the module within the application archive.
     */
    public String getUrl() {
//        System.out.println("EarImpl: getURL");
        return "/";//NOI18N
    }

    /**
     * Sets the location of the modules within the application archive.
     * For example, a web module could be at "/wbmodule1.war" within the ear
     * file. For standalone module the URL cannot be set to a different value
     * then "/"
     */
    public void setUrl(String url) {
        throw new IllegalStateException("Cannot set url for maven ear projects");//NOI18N
    }

    /**
     * Returns the archive file for the module of null if the archive file 
     * does not exist (for example, has not been compiled yet).
     */
    public FileObject getArchive() throws IOException {
        //TODO get the correct values for the plugin properties..
        MavenProject proj = mavenproject.getMavenProject();
        String finalName = proj.getBuild().getFinalName();
        String loc = proj.getBuild().getDirectory();
        File fil = FileUtil.normalizeFile(new File(loc, finalName + ".ear"));//NOI18N
//        System.out.println("ear = get archive=" + fil);
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
     */
    public Iterator getArchiveContents() throws IOException {
  //      System.out.println("ear get archive content");
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
        MavenProject proj = mavenproject.getMavenProject();
        String finalName = proj.getBuild().getFinalName();
        String loc = proj.getBuild().getDirectory();
        File fil = FileUtil.normalizeFile(new File(loc, finalName));
//        System.out.println("earimpl. get content=" + fil);
        FileObject fo = FileUtil.toFileObject(fil);
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
        if (J2eeModule.APP_XML.equals(location)) {
            try {
                
                FileObject content = getDeploymentDescriptor();
                if (content == null) {
//                    System.out.println("getDeploymentDescriptor.application dd is null");
                    StringInputStream str = new StringInputStream(
  "<application xmlns=\"http://java.sun.com/xml/ns/j2ee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/application_1_4.xsd\" version=\"1.4\">" +//NOI18N
  "<description>description</description>" +//NOI18N
  "<display-name>application</display-name></application>");//NOI18N
                    try {
                        return DDProvider.getDefault().getDDRoot(new InputSource(str));
                    } catch (SAXException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    return DDProvider.getDefault().getDDRoot(content);
                }
            } catch (IOException e) {
                ErrorManager.getDefault().log(e.getLocalizedMessage());
            }
        }
//        System.out.println("no dd for=" + location);
        return null;
    }

    public J2eeModule[] getModules() {
        Iterator it = mavenproject.getMavenProject().getArtifacts().iterator();
        List toRet = new ArrayList();
        while (it.hasNext()) {
            Artifact elem = (Artifact) it.next();
            if ("war".equals(elem.getType()) || "ejb".equals(elem.getType())) {//NOI18N
//                System.out.println("adding " + elem.getId());
                //TODO probaby figure out the context root etc..
                File fil = elem.getFile();
                FileObject fo = FileUtil.toFileObject(fil);
                boolean found = false;
                if (fo != null) {
                    Project owner = FileOwnerQuery.getOwner(fo);
                    if (owner != null) {
                        J2eeModuleProvider prov = owner.getLookup().lookup(J2eeModuleProvider.class);
                        if (prov != null) {
                            toRet.add(prov.getJ2eeModule());
                            found = true;
                        }
                    }
                }
                if (!found) {
                    toRet.add(J2eeModuleFactory.createJ2eeModule(new NonProjectJ2eeModule(elem, getJ2eePlatformVersion(), provider)));
                }
            }
        }
        //TODO need to also consult the pom file for potencial additional modules.
        return (J2eeModule[])toRet.toArray(new J2eeModule[toRet.size()]);
    }
    
    public List<Project> getProjects() {
        Iterator it = mavenproject.getMavenProject().getArtifacts().iterator();
        List<Project> toRet = new ArrayList<Project>();
        while (it.hasNext()) {
            Artifact elem = (Artifact) it.next();
            if ("war".equals(elem.getType()) || "ejb".equals(elem.getType())) {//NOI18N
                File fil = elem.getFile();
                FileObject fo = FileUtil.toFileObject(fil);
                if (fo != null) {
                    Project owner = FileOwnerQuery.getOwner(fo);
                    if (owner != null) {
                        J2eeModuleProvider prov = owner.getLookup().lookup(J2eeModuleProvider.class);
                        if (prov != null) {
                            toRet.add(owner);
                        }
                    }
                }
            }
        }
        return toRet;
    }
    
    
    File getDDFile(String path) {
//        System.out.println("getDD file=" + path);
        //TODO what is the actual path.. sometimes don't have any sources for deployment descriptors..
        URI dir = mavenproject.getEarAppDirectory();
        File fil = new File(new File(dir), path);
        if (!fil.getParentFile().exists()) {
            fil.getParentFile().mkdirs();
        }
        fil = FileUtil.normalizeFile(fil);
        return fil;
    }
    

    public void addModuleListener(ModuleListener ml) {
    }

    public void removeModuleListener(ModuleListener ml) {
    }

    public EjbChangeDescriptor getEjbChanges(long timestamp) {
        return new EjbChange();
    }

    public boolean isManifestChanged(long timestamp) {
        return false;
    }

    public void addCarModule(Car arg0) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }

    
    
    // inspired by netbeans' webmodule codebase, not really sure what is the point
    // of the iterator..
    private static final class ContentIterator implements Iterator {
        private ArrayList ch;
        private FileObject root;
        
        private ContentIterator(FileObject f) {
            this.ch = new ArrayList();
            ch.add(f);
            this.root = f;
        }
        
        public boolean hasNext() {
            return ! ch.isEmpty();
        }
        
        public Object next() {
            FileObject f = (FileObject) ch.get(0);
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
    
    //TODO
    private class EjbChange implements EjbChangeDescriptor {
        public boolean ejbsChanged() {
            return false;
        }
        
        public String[] getChangedEjbs() {
            return new String[0];
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
    
    /**
     * Get metadata model of enterprise application.
     */
    public synchronized MetadataModel<ApplicationMetadata> getMetadataModel() {
        if (metadataModel == null) {
            metadataModel = MetadataModelFactory.createMetadataModel(new ApplicationMetadataModelImpl(project));
        }
        return metadataModel;
    }
    

    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == ApplicationMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getMetadataModel();
            return model;
        }
        return null;
    }

 
}
