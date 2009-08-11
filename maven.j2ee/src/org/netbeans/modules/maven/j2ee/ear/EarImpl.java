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
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.ear.model.ApplicationMetadataModelImpl;
import hidden.org.codehaus.plexus.util.StringInputStream;
import hidden.org.codehaus.plexus.util.StringUtils;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
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
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.RootedEntry;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation2;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.NBPluginParameterExpressionEvaluator;
import org.netbeans.modules.maven.spi.debug.AdditionalDebuggedProjects;
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
class EarImpl implements EarImplementation, EarImplementation2,
        J2eeApplicationImplementation2,
        ModuleChangeReporter,
        AdditionalDebuggedProjects {

    private Project project;
    private EarModuleProviderImpl provider;
    private MetadataModel<ApplicationMetadata> metadataModel;
    private final NbMavenProject mavenproject;

    /** Creates a new instance of EarImpl */
    EarImpl(Project proj, EarModuleProviderImpl prov) {
        project = proj;
        mavenproject = project.getLookup().lookup(NbMavenProject.class);

        provider = prov;
    }

    public Profile getJ2eeProfile() {
        //try to apply the hint if it exists.
        String version = project.getLookup().lookup(AuxiliaryProperties.class).get(Constants.HINT_J2EE_VERSION, true);
        if (version != null) {
            return Profile.fromPropertiesString(version);
        }
        if (isApplicationXmlGenerated()) {
            version = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS,
                    Constants.PLUGIN_EAR, "version", "generate-application-xml"); //NOI18N
            // the default version in maven plugin is also 1.3
            //TODO what if the default changes?
            if (version != null) {
                // 5 is not valid value in netbeans, it's 1.5
                if ("5".equals(version)) {
                    return Profile.JAVA_EE_5;
                }
                // 6 is not valid value in netbeans, it's 1.6
                if ("6".equals(version)) {
                    return Profile.JAVA_EE_6_FULL;
                }
                return Profile.fromPropertiesString(version.trim());
            }
        } else {
            DDProvider prov = DDProvider.getDefault();
            FileObject dd = getDeploymentDescriptor();
            if (dd != null) {
                try {
                    Application app = prov.getDDRoot(dd);
                    String appVersion = app.getVersion().toString();
                    return Profile.fromPropertiesString(appVersion);
                } catch (IOException exc) {
                    ErrorManager.getDefault().notify(exc);
                }
            } else {
                //TODO try to check the pom model again and user 'version' element if existing..
                return Profile.JAVA_EE_6_FULL;
            }
        }
        // hardwire?
//        System.out.println("eariml: getj2eepaltform");
        return Profile.J2EE_14;
    }

    public String getJ2eePlatformVersion() {
        return getJ2eeProfile().toPropertiesString();
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

    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.EAR;
    }

    /**
     * Returns module specification version
     */
    public String getModuleVersion() {
        String j2ee = getJ2eePlatformVersion();
        if (EjbProjectConstants.J2EE_14_LEVEL.equals(j2ee)) {
            return J2eeModule.JAVA_EE_5;
        }
//        System.out.println("earimpl: get module version");
        //TODO??
        return J2eeModule.J2EE_14;
    }

    /**
     * Returns the location of the module within the application archive.
     */
    public String getUrl() {
        String toRet =  "/" + mavenproject.getMavenProject().getBuild().getFinalName(); //NOI18N
        return toRet;
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
        if ("application.xml".equals(location)) { //NOI18N
            location = J2eeModule.APP_XML;
        }
        if (J2eeModule.APP_XML.equals(location)) {
            try {

                FileObject content = getDeploymentDescriptor();
                if (content == null) {
//                    System.out.println("getDeploymentDescriptor.application dd is null");
                    StringInputStream str = new StringInputStream(
                            "<application xmlns=\"http://java.sun.com/xml/ns/j2ee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/application_1_4.xsd\" version=\"1.4\">" +//NOI18N
                            "<description>description</description>" +//NOI18N
                            "<display-name>" + mavenproject.getMavenProject().getArtifactId() + "</display-name></application>");//NOI18N
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
        MavenProject mp = mavenproject.getMavenProject();
        @SuppressWarnings("unchecked")
        Set<Artifact> artifactSet = mp.getArtifacts();
        @SuppressWarnings("unchecked")
        List<Dependency> deps = mp.getRuntimeDependencies();
        String fileNameMapping = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_EAR, "fileNameMapping", "ear"); //NOI18N
        if (fileNameMapping == null) {
            fileNameMapping = "standard"; //NOI18N
        }

        List<J2eeModule> toRet = new ArrayList<J2eeModule>();
        EarImpl.MavenModule[] mm = readPomModules();
        //#162173 order by dependency list, artifacts is unsorted set.
        for (Dependency d : deps) {
            //TODO do we really care about war and ejb only??
            if ("war".equals(d.getType()) || "ejb".equals(d.getType())) {//NOI18N
                for (Artifact a : artifactSet) {
                    if (a.getGroupId().equals(d.getGroupId()) &&
                            a.getArtifactId().equals(d.getArtifactId()) &&
                            StringUtils.equals(a.getClassifier(), d.getClassifier())) {
                        File fil = a.getFile();
                        FileObject fo = FileUtil.toFileObject(fil);
                        boolean found = false;
                        if (fo != null) {
                            Project owner = FileOwnerQuery.getOwner(fo);
                            if (owner != null) {
                                J2eeModuleProvider prov = owner.getLookup().lookup(J2eeModuleProvider.class);
                                if (prov != null) {
                                    J2eeModule mod = prov.getJ2eeModule();
                                    EarImpl.MavenModule m = findMavenModule(a, mm);
                                    J2eeModule module = J2eeModuleFactory.createJ2eeModule(new ProxyJ2eeModule(mod, m, fileNameMapping));
                                    //#162173 respect order in pom configuration.. shall we?
                                    if (m.pomIndex > -1 && toRet.size() > m.pomIndex) {
                                        toRet.add(m.pomIndex, module);
                                    } else {
                                        toRet.add(module);
                                    }
                                    found = true;
                                }
                            }
                        }
                        if (!found) {
                            J2eeModule mod = J2eeModuleFactory.createJ2eeModule(new NonProjectJ2eeModule(a, getJ2eePlatformVersion(), provider));
                            EarImpl.MavenModule m = findMavenModule(a, mm);
                            J2eeModule module = J2eeModuleFactory.createJ2eeModule(new ProxyJ2eeModule(mod, m, fileNameMapping));
                            //#162173 respect order in pom configuration.. shall we?
                            if (m.pomIndex > -1 && toRet.size() > m.pomIndex) {
                                toRet.add(m.pomIndex, module);
                            } else {
                                toRet.add(module);
                            }
                        }
                        break;
                    }
                }
            }
        }
        return toRet.toArray(new J2eeModule[toRet.size()]);
    }

    public List<Project> getProjects() {
        MavenProject mp = mavenproject.getMavenProject();
        @SuppressWarnings("unchecked")
        Set<Artifact> artifactSet = mp.getArtifacts();
        @SuppressWarnings("unchecked")
        List<Dependency> deps = mp.getRuntimeDependencies();
        List<Project> toRet = new ArrayList<Project>();
        EarImpl.MavenModule[] mm = readPomModules();
        //#162173 order by dependency list, artifacts is unsorted set.
        for (Dependency d : deps) {
            if ("war".equals(d.getType()) || "ejb".equals(d.getType())) {//NOI18N
                for (Artifact a : artifactSet) {
                    if (a.getGroupId().equals(d.getGroupId()) &&
                            a.getArtifactId().equals(d.getArtifactId()) &&
                            StringUtils.equals(a.getClassifier(), d.getClassifier())) {
                        File fil = a.getFile();
                        FileObject fo = FileUtil.toFileObject(fil);
                        if (fo != null) {
                            Project owner = FileOwnerQuery.getOwner(fo);
                            if (owner != null) {
                                EarImpl.MavenModule m = findMavenModule(a, mm);
                                //#162173 respect order in pom configuration.. shall we?
                                if (m.pomIndex > -1 && toRet.size() > m.pomIndex) {
                                    toRet.add(m.pomIndex, owner);
                                } else {
                                    toRet.add(owner);
                                }

                            }
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

    private EarImpl.MavenModule findMavenModule(Artifact art, EarImpl.MavenModule[] mm) {
        EarImpl.MavenModule toRet = null;
        for (EarImpl.MavenModule m : mm) {
            if (art.getGroupId().equals(m.groupId) && art.getArtifactId().equals(m.artifactId)) {
                m.artifact = art;
                toRet = m;
                break;
            }
        }
        if (toRet == null) {
            toRet = new EarImpl.MavenModule();
            toRet.artifact = art;
            toRet.groupId = art.getGroupId();
            toRet.artifactId = art.getArtifactId();
            toRet.classifier = art.getClassifier();
            //add type as well?
        }
        return toRet;
    }

    private EarImpl.MavenModule[] readPomModules() {
        MavenProject prj = mavenproject.getMavenProject();
        MavenModule[] toRet = new MavenModule[0];
        if (prj.getBuildPlugins() == null) {
            return toRet;
        }
        for (Object obj : prj.getBuildPlugins()) {
            Plugin plug = (Plugin)obj;
            if (Constants.PLUGIN_EAR.equals(plug.getArtifactId()) &&
                    Constants.GROUP_APACHE_PLUGINS.equals(plug.getGroupId())) {
                   toRet =  checkConfiguration(prj, plug.getConfiguration());
            }
        }
        if (toRet == null) {  //NOI18N
            if (prj.getPluginManagement() != null) {
                for (Object obj : prj.getPluginManagement().getPlugins()) {
                    Plugin plug = (Plugin)obj;
                    if (Constants.PLUGIN_EAR.equals(plug.getArtifactId()) &&
                            Constants.GROUP_APACHE_PLUGINS.equals(plug.getGroupId())) {
                        toRet = checkConfiguration(prj, plug.getConfiguration());
                        break;
                    }
                }
            }
        }
        return toRet;
    }

    private MavenModule[] checkConfiguration(MavenProject prj, Object conf) {
        List<MavenModule> toRet = new ArrayList<MavenModule>();
        if (conf != null && conf instanceof Xpp3Dom) {
            NBPluginParameterExpressionEvaluator eval = new NBPluginParameterExpressionEvaluator(prj, EmbedderFactory.getProjectEmbedder().getSettings(), new Properties());
            Xpp3Dom dom = (Xpp3Dom) conf;
            Xpp3Dom modules = dom.getChild("modules"); //NOI18N
            if (modules != null) {
                int index = 0;
                for (Xpp3Dom module : modules.getChildren()) {
                    MavenModule mm = new MavenModule();
                    mm.type = module.getName();
                    if (module.getChildren() != null) {
                        for (Xpp3Dom param : module.getChildren()) {
                            String value = param.getValue();
                            if (value == null) {
                                continue;
                            }
                            try {
                                Object evaluated = eval.evaluate(value.trim());
                                value = evaluated != null ? ("" + evaluated) : value.trim();  //NOI18N
                            } catch (ExpressionEvaluationException e) {
                                //log silently
                            }
                            if ("groupId".equals(param.getName())) { //NOI18N
                                mm.groupId = value;
                            } else if ("artifactId".equals(param.getName())) { //NOI18N
                                mm.artifactId = value;
                            } else if ("artifactId".equals(param.getName())) { //NOI18N
                                mm.artifactId = value;
                            } else if ("classifier".equals(param.getName())) { //NOI18N
                                mm.classifier = value;
                            } else if ("uri".equals(param.getName())) { //NOI18N
                                mm.uri = value;
                            } else if ("bundleDir".equals(param.getName())) { //NOI18N
                                mm.bundleDir = value;
                            } else if ("bundleFileName".equals(param.getName())) { //NOI18N
                                mm.bundleFileName = value;
                            } else if ("excluded".equals(param.getName())) { //NOI18N
                                mm.excluded = Boolean.valueOf(value);
                            }
                        }
                    }
                    mm.pomIndex = index;
                    index++;
                    toRet.add(mm);
                }
            }
        }
        return toRet.toArray(new MavenModule[0]);
    }

    private static class MavenModule {
        String uri;
        Artifact artifact;
        String groupId;
        String artifactId;
        String type;
        String classifier;
        String bundleDir;
        String bundleFileName;
        int pomIndex = -1;
        boolean excluded = false;


        String resolveUri(String fileNameMapping) {
            if (uri != null) {
                return uri;
            }
            String bDir = resolveBundleDir();
            return bDir + resolveBundleName(fileNameMapping);
        }

        String resolveBundleDir() {
            String toRet = bundleDir;
            if (toRet != null) {
                // Using slashes
                toRet = toRet.replace('\\', '/'); //NOI18N

                // Remove '/' prefix if any so that directory is a relative path
                if (toRet.startsWith("/")) { //NOI18N
                    toRet = toRet.substring(1, toRet.length());
                }

                if (toRet.length() > 0 && !toRet.endsWith("/")) { //NOI18N
                    // Adding '/' suffix to specify a directory structure if it is not empty
                    toRet = toRet + "/"; //NOI18N
                }
                return toRet;

            }
            return "";
        }

        String resolveBundleName(String fileNameMapping) {
            if (bundleFileName != null) {
                return bundleFileName;
            }
            if ("standard".equals(fileNameMapping)) { //NOI18N
                return artifact.getFile().getName();
            }
            if ("full".equals(fileNameMapping)) { //NOI18N
                final String dashedGroupId = groupId.replace( '.', '-'); //NOI18N
                return dashedGroupId + "-" + artifact.getFile().getName(); //NOI18N
            }
            //TODO it seems the fileNameMapping can also be a class (from ear-maven-plugin's classpath
            // of type FileNameMapping that resolves the name.. we ignore it for now.. not common usecase anyway..
            return artifact.getFile().getName();
        }

    }


    private static class ProxyJ2eeModule implements J2eeModuleImplementation2 {
        private final J2eeModule module;
        private final EarImpl.MavenModule mavenModule;
        private final String fileNameMapping;

        ProxyJ2eeModule(J2eeModule module, EarImpl.MavenModule mavModule, String fileNameMapping) {
            this.mavenModule = mavModule;
            this.module = module;
            this.fileNameMapping = fileNameMapping;
        }

        public String getModuleVersion() {
            return module.getModuleVersion();
        }

        public J2eeModule.Type getModuleType() {
            return module.getType();
        }

        public String getUrl() {
            return mavenModule.resolveUri(fileNameMapping);
        }

        public FileObject getArchive() throws IOException {
            return module.getArchive();
        }

        public Iterator getArchiveContents() throws IOException {
            return module.getArchiveContents();
        }

        public FileObject getContentDirectory() throws IOException {
            return module.getContentDirectory();
        }

        public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
            return module.getMetadataModel(type);
        }

        public File getResourceDirectory() {
            return module.getResourceDirectory();
        }

        public File getDeploymentConfigurationFile(String name) {
            return module.getDeploymentConfigurationFile(name);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            module.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            module.removePropertyChangeListener(listener);
        }

        @Override
        public boolean equals(Object obj) {
            return module.equals(obj);
        }

        @Override
        public int hashCode() {
            return module.hashCode();
        }

    }

}
