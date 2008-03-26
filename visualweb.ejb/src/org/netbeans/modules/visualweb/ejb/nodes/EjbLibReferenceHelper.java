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
/*
 * EjbLibReferenceHelper.java
 *
 * Created on March 6, 2005, 9:35 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

import java.util.zip.ZipEntry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedEjbResource;
import org.netbeans.modules.visualweb.ejb.EjbDataSourceManager;
import org.netbeans.modules.visualweb.ejb.EjbRefMaintainer;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.util.Util;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * 
 * @author cao
 */
public class EjbLibReferenceHelper {

    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF"; // NOI18N
    private static final String TIMESTAMP_ATTR = "Generated-time-in-millies"; // NOI18N
    
    /**
     * Class to encapsulate a hack to implement a feature that is not yet implemented by the Sun
     * AppServer plugin. Remove this hack once it is implemented. Also, remove dependency on the
     * module. 2007-06-20
     * 
     * @author Edwin Goei
     */
    private static class SunAppServerHack {

        private FileObject sunWebXml;

        private SunWebApp sunWebApp;

        private SunAppServerHack(FileObject sunWebXml) throws IOException {
            this.sunWebXml = sunWebXml;

            RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(sunWebXml);
            if (sunDDRoot instanceof SunWebApp) {
                sunWebApp = (SunWebApp) sunDDRoot;
            } else {
                throw new IllegalStateException("Cannot process sun-web.xml");
            }
        }

        /**
         * Factory method to create a DD handler for a project.
         * 
         * @param project
         * @return
         * @throws IOException
         */
        public static SunAppServerHack getInstance(Project project) throws IOException {
            FileObject sunWebXml = getSunWebXml(project);
            if (sunWebXml == null) {
                return null;
            } else {
                return new SunAppServerHack(sunWebXml);
            }
        }

        private static FileObject getSunWebXml(Project project) {
            Lookup lookup = project.getLookup();
            J2eeModuleProvider provider = (J2eeModuleProvider) lookup
                    .lookup(J2eeModuleProvider.class);
            FileObject[] configFiles = provider.getConfigurationFiles();
            for (FileObject fo : configFiles) {
                if (fo.getNameExt().equals("sun-web.xml")) {
                    return fo;
                }
            }
            return null;
        }

        public SunWebApp getSunWebApp() {
            return sunWebApp;
        }

        public void finish() throws IOException {
            sunWebApp.write(sunWebXml);
        }
    }

    private static final String EJB_REFS_XML = "ejb-refs.xml";

    /**
     * Return the Project that is currently active according to the Designer.
     * 
     * @return currently active project or null, if none.
     */
    public static Project getActiveProject() {
        FileObject fileObject = DesignerServiceHack.getDefault().getCurrentFile();
        if (fileObject == null) {
            return null;
        }
        return FileOwnerQuery.getOwner(fileObject);
    }

    /**
     * Refreshed the EjbGroup jars in all the currently open objects
     * 
     * @throws IOException
     */
    public static void updateEjbGroupForProjects(Project[] projects, EjbGroup ejbGroup)
            throws IOException, ConfigurationException {
        // Update the jars in each open visual web project
        for (int i = 0; i < projects.length; i++) {
            Project project = projects[i];
            if (JsfProjectUtils.isJsfProject(project)) {
                updateEjbGroupForProject(ejbGroup, project);
            }
        }
    }

    /**
     * Sync the the EJB related archive refs in the given projects
     */
    public static void syncArchiveRefs(Project[] projects) {

        for (int i = 0; i < projects.length; i++) {
            if (!JsfProjectUtils.isJsfProject(projects[i])) {
                // Skip this project unless this is a JSF Project
                // 107000 unwanted "lib" folder created for every opened/created project
                continue;
            }

            try {
                FileObject ejbSubDir = projects[i].getProjectDirectory().getFileObject(
                        JsfProjectConstants.PATH_LIBRARIES + '/'
                                + EjbDataSourceManager.EJB_DATA_SUB_DIR);
                if (ejbSubDir == null)
                    // NO ejb in this project. Move on
                    continue;

                // Get the ejb groups dropped in this project
                String ejbSubDirPath = FileUtil.toFile(ejbSubDir).getAbsolutePath();
                EjbRefMaintainer refMaintainer = new EjbRefMaintainer(ejbSubDirPath
                        + File.separator + EJB_REFS_XML);
                Collection ejbGroups = refMaintainer.getRefferedEjbGroups();

                if (ejbGroups == null) return;
                
                // Now lets see whehther the ejb groups are still in the data
                // model
                for (Iterator iter = ejbGroups.iterator(); iter != null && iter.hasNext();) {
                    EjbGroup ejbGrp = (EjbGroup) iter.next();
                    EjbGroup ejbGrpInModel = EjbDataModel.getInstance()
                            .findEjbGroupForClientWrapperJar(
                                    Util.getFileName(ejbGrp.getClientWrapperBeanJar()));

                    if (ejbGrpInModel == null)
                        // This group is gone from the data model
                        continue;
                    else
                        updateEjbGroupForProjects(new Project[] { projects[i] }, ejbGrpInModel);
                }
            } catch (java.io.IOException ie) {
                ErrorManager.getDefault().notify(ie);
                ie.printStackTrace();
                continue;
            } catch (ConfigurationException ce) {
                ErrorManager.getDefault().notify(ce);
                ce.printStackTrace();
                continue;                
            }
        }
    }

    private static FileObject getProjectEjbDataDir(Project project) throws IOException {
        // Obtain the path to the project's library directory
        FileObject projectLibDir = JsfProjectUtils.getProjectLibraryDirectory(project);

        FileObject ejbSubDir = projectLibDir.getFileObject(EjbDataSourceManager.EJB_DATA_SUB_DIR);
        if (ejbSubDir == null)
            ejbSubDir = projectLibDir.createFolder(EjbDataSourceManager.EJB_DATA_SUB_DIR);
        return ejbSubDir;
    }
    
    private static void addRefsToProject(Project project, String role, List<FileObject> projectJars, String... jars) throws IOException {
        ArrayList<URL> copiedArchiveJars = new ArrayList<URL>(jars.length);
        for (String jar : jars) {
            FileObject jarFO = findFileObject(projectJars, jar);
            if (jarFO != null) {
                copiedArchiveJars.add(new URL(jarFO.getURL().toExternalForm() + "/"));
            }
        }
        
        URL[] jarArray = copiedArchiveJars.toArray(new URL[copiedArchiveJars.size()]);
        
        // Add archive references to the project
        if (role != null) {
            JsfProjectUtils.addRootReferences(project, jarArray, role);
        } else {
            JsfProjectUtils.addRootReferences(project, jarArray);
        }
    }
    
    private static FileObject findFileObject(List<FileObject> files, String filePath) {
        String fileName = new File(filePath).getName();
        for (FileObject fo : files) {
            if (fo.getNameExt().equals(fileName)) {
                return fo;
            }
        }
        
        return null;
    }
    
    private static ArrayList<FileObject> addUpdateJarsForProject(Project project, final ArrayList<String> jars) throws IOException {
        final FileObject ejbSubDir = getProjectEjbDataDir(project);
        final ArrayList<FileObject> copiedJars = new ArrayList<FileObject>(jars.size());
        
        ejbSubDir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            
           public void run() throws IOException {
               for (String jarFile : jars) {                  
                   FileObject jarFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(new File(jarFile)));
                   if (jarFileObject != null) {
                       String jarFileName = jarFileObject.getName();
                       String jarFileExt = jarFileObject.getExt();
                       FileObject projectJar = ejbSubDir.getFileObject(jarFileName, jarFileExt);
                       
                       if (projectJar == null) { 
                           FileObject copiedJar = FileUtil.copyFile(jarFileObject, ejbSubDir, jarFileName);
                           copiedJars.add(copiedJar);
                       }else if (!isJarUpToDate(projectJar, jarFileObject)) {
                           projectJar.delete();
                           
                           FileObject copiedJar = FileUtil.copyFile(jarFileObject, ejbSubDir, jarFileName);
                           copiedJars.add(copiedJar);
                       }else {
                           copiedJars.add(projectJar);
                       }
                   }
               }
           }
        });
        
        return copiedJars;
    }
    
    private static boolean isJarUpToDate(FileObject projectJarFO, FileObject repositoryJarFO) {
        if (projectJarFO.getSize() != repositoryJarFO.getSize()) {
            return false;
        }
        
        JarFile projectJar = null;
        JarFile repositoryJar = null;
        
        try {
            File projectFile = FileUtil.toFile(projectJarFO);
            File repositoryFile = FileUtil.toFile(repositoryJarFO);
            
            projectJar = new JarFile(projectFile, false);
            repositoryJar = new JarFile(repositoryFile, false);
            
            ZipEntry projectManifestEntry = projectJar.getEntry(MANIFEST_PATH);
            ZipEntry repositoryManifestEntry = repositoryJar.getEntry(MANIFEST_PATH);
            
            if (projectManifestEntry == null && repositoryManifestEntry == null) {
                return true;
            }else if (projectManifestEntry == null || repositoryManifestEntry == null) {
                return false;
            }
            
            Manifest projectManifest = new Manifest(projectJar.getInputStream(projectManifestEntry));
            Manifest repositoryManifest = new Manifest(repositoryJar.getInputStream(repositoryManifestEntry));
            
            String projectTS = (String)projectManifest.getMainAttributes().getValue(TIMESTAMP_ATTR);
            String repositoryTS = (String)repositoryManifest.getMainAttributes().getValue(TIMESTAMP_ATTR);
            
            if (projectTS == null && repositoryTS == null) {
                return true;
            }
            
            return projectTS != null && repositoryTS != null && projectTS.equals(repositoryTS);
        }catch (Exception ex) {
            // if there is no manifest, the file sizes and names are at least the same, so
            // assume that the file is unchanged
            return true;
        }finally {
            if (projectJar != null) {
                try {
                    projectJar.close();
                }catch (IOException ex) {
                    Util.getLogger().log(Level.WARNING, "Unable to close jar file: " + projectJar.getName(), ex);
                }
            }
            
            if (repositoryJar != null) {
                try {
                    repositoryJar.close();
                }catch (IOException ex) {
                    Util.getLogger().log(Level.WARNING, "Unable to close jar file: " + repositoryJar.getName(), ex);
                }
            }
        }
        
    }

    /**
     * Adds the reference information of the ejbs in the give ejb group to the project.
     * 
     * @param project
     *            The project to be added to
     * @param ejbGroup
     */
    public static void addToEjbRefXmlToProject(Project project, EjbGroup ejbGroup) {
        try {
            FileObject projectLibDir = JsfProjectUtils.getProjectLibraryDirectory(project);
            FileObject ejbSubDir = projectLibDir
                    .getFileObject(EjbDataSourceManager.EJB_DATA_SUB_DIR);
            if (ejbSubDir == null)
                ejbSubDir = projectLibDir.createFolder(EjbDataSourceManager.EJB_DATA_SUB_DIR);

            String ejbSubDirPath = FileUtil.toFile(ejbSubDir).getAbsolutePath();

            // Before writing the the ejb group to the xml,
            // change the client jars and wrapper bean jar locations to the
            // project/lib/ejb-sources
            EjbGroup ejbGrpCopy = (EjbGroup) ejbGroup.clone();

            ArrayList<String> newClientPaths = new ArrayList<String>();
            for (Iterator iter = ejbGroup.getClientJarFiles().iterator(); iter.hasNext();) {
                String jarPath = (String) iter.next();
                newClientPaths.add(ejbSubDirPath + "/" + (new File(jarPath)).getName());
            }
            ejbGrpCopy.setClientJarFiles(newClientPaths);

            ejbGrpCopy.setClientWrapperBeanJar(ejbSubDirPath + "/"
                    + (new File(ejbGroup.getClientWrapperBeanJar())).getName());
            ejbGrpCopy.setDesignInfoJar(ejbSubDirPath + "/"
                    + (new File(ejbGroup.getDesignInfoJar())).getName());

            EjbRefMaintainer refMaintainer = new EjbRefMaintainer(ejbSubDirPath + File.separator
                    + EJB_REFS_XML);
            refMaintainer.addToEjbRefXml(ejbGrpCopy);
        } catch (java.io.IOException ie) {
            Util.getLogger().log(Level.WARNING, "Failed to save ejb refs", ie);
        }
    }

    /**
     * Add all session beans in EjbGroup grp to project. This method is idempotent.
     * 
     * @param project
     * @param grp
     * @throws IOException
     * @throws ConfigurationException
     */
    private static void addToDeploymentDescriptors(Project project, EjbGroup grp)
            throws IOException, ConfigurationException {
        SunAppServerHack sunAppServerHack = SunAppServerHack.getInstance(project);

        // Add all the EJBs in this group as EJB resources to the project
        // Note: we purposely decided to add all the EJBs vs just the used
        // ones
        for (Iterator ejbIter = grp.getSessionBeans().iterator(); ejbIter.hasNext();) {
            EjbInfo ejbInfo = (EjbInfo) ejbIter.next();

            String refName = ejbInfo.getWebEjbRef();
            String refType = ejbInfo.getBeanTypeName();
            String home = ejbInfo.getHomeInterfaceName();

            // The global JNDI name for this EJB
            // - corbaname:iiop:<hostname>:<port>#<jndiname> for Sun
            // Application server, weblogic
            // -
            // corbaname:iiop:<hostname>:<port>/NameServiceServerRoot#<jndiname>
            // for websphere 5.1
            String jndiName = "corbaname:iiop:" + grp.getServerHost() + ":" + grp.getIIOPPort()
                    + "#" + ejbInfo.getJNDIName();
            if (grp.isWebsphereAppServer())
                jndiName = "corbaname:iiop:" + grp.getServerHost() + ":" + grp.getIIOPPort()
                        + "/NameServiceServerRoot#" + ejbInfo.getJNDIName();

            String remote = ejbInfo.getCompInterfaceName();

            RequestedEjbResource resource = new RequestedEjbResource(refName, jndiName, refType,
                    home, remote);
            JsfProjectUtils.setEjbReference(project, resource);

            if (sunAppServerHack != null) {
                /*
                 * TODO This hack should be removed once the app server plugin implements the
                 * bindEjbReference API.
                 */
                SunWebApp sunWebApp = sunAppServerHack.getSunWebApp();
                EjbRef ref = findEjbRefByName(sunWebApp, refName);
                if (ref != null) {
                    ref.setJndiName(jndiName);
                } else {
                    ref = sunWebApp.newEjbRef();
                    ref.setEjbRefName(refName);
                    ref.setJndiName(jndiName);
                    sunWebApp.addEjbRef(ref);
                }
            } else {
                // Bind the EJB Reference
                J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(
                        J2eeModuleProvider.class);
                j2eeModuleProvider.getConfigSupport().bindEjbReference(refName, jndiName);
            }
        }

        if (sunAppServerHack != null) {
            sunAppServerHack.finish();
        }
    }

    /**
     * Adds the EjbGroup to the currently active project. This method is idempotent.
     * 
     * @throws IOException
     * @throws ConfigurationException
     */
    public static void addEjbGroupToActiveProject(EjbGroup ejbGroup) throws IOException,
            ConfigurationException {
        Project project = getActiveProject();

        addEjbGroupJarsToProject(ejbGroup, project);

        // Add/update this ejb group to the ejb ref xml in the project
        addToEjbRefXmlToProject(project, ejbGroup);

        // Add an ejb-ref to the standard and vendor webapp DD
        addToDeploymentDescriptors(project, ejbGroup);
    }

    private static void addEjbGroupJarsToProject(EjbGroup ejbGroup, Project project)
            throws IOException {
        // Add EJB client wrapper archive to the project
        String wrapperJar = ejbGroup.getClientWrapperBeanJar();
        String dtJar = ejbGroup.getDesignInfoJar();
        ArrayList<String> clientJarFiles = ejbGroup.getClientJarFiles();
        
        ArrayList<String> allJars = new ArrayList<String>(clientJarFiles.size() + 2);
        allJars.add(wrapperJar);
        allJars.add(dtJar);
        allJars.addAll(clientJarFiles);
        
        // copy all jars to the project
        List<FileObject> projectJars = addUpdateJarsForProject(project, allJars);
        
        // add EJB client wrapper archive to the project
        addRefsToProject(project, null, projectJars, wrapperJar);
        
        // Add EJB design-time archive to the project
        addRefsToProject(project, ClassPath.COMPILE, projectJars, dtJar);

        // Add the client stub jars to the project
        if (clientJarFiles.size() > 0) {
            addRefsToProject(project, null, projectJars, clientJarFiles.toArray(new String[clientJarFiles.size()]));
        }
    }
    
    private static void updateEjbGroupForProject(EjbGroup ejbGroup, Project project)
            throws IOException, ConfigurationException {
        // Update EJB client wrapper archive
        String wrapperJar = ejbGroup.getClientWrapperBeanJar();
        String dtJar = ejbGroup.getDesignInfoJar();
        ArrayList<String> clientJarFiles = ejbGroup.getClientJarFiles();
        
        ArrayList<String> allJars = new ArrayList<String>(clientJarFiles.size() + 2);
        allJars.add(wrapperJar);
        allJars.add(dtJar);
        allJars.addAll(clientJarFiles);
        
        addUpdateJarsForProject(project, allJars);
        
        // Partial fix for 119881
        addToDeploymentDescriptors(project, ejbGroup);
    }

    private static EjbRef findEjbRefByName(SunWebApp sunWebApp, String ejbRefName) {
        EjbRef[] ejbRefs = sunWebApp.getEjbRef();
        for (EjbRef ref : ejbRefs) {
            if (ref.getEjbRefName().equals(ejbRefName)) {
                return ref;
            }
        }
        return null;
    }
}
