/*
 * EjbLibReferenceHelper.java
 *
 * Created on March 6, 2005, 9:35 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

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
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * 
 * @author cao
 */
public class EjbLibReferenceHelper {

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
            throws IOException {
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

                // Now lets see whehther the ejb groups are still in the data
                // model
                for (Iterator iter = ejbGroups.iterator(); iter.hasNext();) {
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
            }
        }
    }

    /**
     * Adds the given jars to the project as archive references. This method is idempotent and is
     * safe to call even if jars already have been copied to the project.
     * 
     * @param project
     *            the project to be added to
     * @param role
     *            One of three values: ClassPath.COMPILE = compile-time only, do not deploy;
     *            ClassPath.EXECUTE = deploy only; or null = means both.
     * @param jars
     *            jar files to be copied to the project (filename Strings)
     * @throws IOException
     */
    private static void addJarsAndRefsToProject(Project project, String role, String... jars)
            throws IOException {
        FileObject ejbSubDir = getProjectEjbDataDir(project);

        // Copy over the jar files into the project library directory
        ArrayList<URL> copiedArchiveJars = new ArrayList<URL>();
        for (String jarFilePath : jars) {
            try {
                String jarFileName = new File(jarFilePath).getName();
                FileObject destJar = ejbSubDir.getFileObject(jarFileName);

                if (destJar == null) {
                    destJar = ejbSubDir.createData(jarFileName);
                    copyJarFile(jarFilePath, destJar);
                }

                copiedArchiveJars.add(new URL(destJar.getURL().toExternalForm() + "/")); // NOI18N
            } catch (IOException ex) {
                Util.getLogger().log(Level.SEVERE, null, ex);
            }
        }

        // Add archive references to the project
        if (role != null) {
            JsfProjectUtils.addRootReferences(project, copiedArchiveJars.toArray(new URL[0]), role);
        } else {
            JsfProjectUtils.addRootReferences(project, copiedArchiveJars.toArray(new URL[0]));
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

    private static void updateJarsForProject(Project project, String role, String... jars)
            throws IOException {
        final FileObject ejbSubDir = getProjectEjbDataDir(project);

        for (final String jarFilePath : jars) {
            final String jarFileName = new File(jarFilePath).getName();

            ejbSubDir.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    FileObject jar = ejbSubDir.getFileObject(jarFileName);
                    if (jar != null) {
                        jar.delete();
                    }

                    jar = ejbSubDir.createData(jarFileName);
                    copyJarFile(jarFilePath, jar);
                }
            });
        }
    }

    private static void copyJarFile(String srcPath, FileObject destJar) throws IOException {
        FileLock fileLock = destJar.lock();
        try {
            OutputStream outStream = destJar.getOutputStream(fileLock);
            DataInputStream in = new DataInputStream(new FileInputStream(new File(srcPath)));
            DataOutputStream out = new DataOutputStream(outStream);

            byte[] bytes = new byte[1024];
            int byteCount = in.read(bytes);

            while (byteCount > -1) {
                out.write(bytes, 0, byteCount);
                byteCount = in.read(bytes);
            }
            out.flush();
            out.close();
            outStream.close();
            in.close();
        } finally {
            fileLock.releaseLock();
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
        addJarsAndRefsToProject(project, null, wrapperJar);

        // Add EJB design-time archive to the project
        String dtJar = ejbGroup.getDesignInfoJar();
        addJarsAndRefsToProject(project, ClassPath.COMPILE, dtJar);

        // Add the client stub jars to the project
        ArrayList<String> clientJarFiles = ejbGroup.getClientJarFiles();
        for (String clientJar : clientJarFiles) {
            addJarsAndRefsToProject(project, null, clientJar);
        }
    }

    private static void updateEjbGroupForProject(EjbGroup ejbGroup, Project project)
            throws IOException {
        // Update EJB client wrapper archive
        String wrapperJar = ejbGroup.getClientWrapperBeanJar();
        updateJarsForProject(project, null, wrapperJar);

        // Update EJB design-time archive
        String dtJar = ejbGroup.getDesignInfoJar();
        updateJarsForProject(project, ClassPath.COMPILE, dtJar);

        // Update the client stub jars
        ArrayList<String> clientJarFiles = ejbGroup.getClientJarFiles();
        for (String clientJar : clientJarFiles) {
            updateJarsForProject(project, null, clientJar);
        }
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
