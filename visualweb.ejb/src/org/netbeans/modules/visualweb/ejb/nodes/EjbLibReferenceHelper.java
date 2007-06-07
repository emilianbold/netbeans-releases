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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
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
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * 
 * @author cao
 */
public class EjbLibReferenceHelper {

    private static final String EJB_REFS_XML = "ejb-refs.xml";

    /**
     * @return The library definition containing the EJB support jar files
     */
    public static Library getEjbSupportLibDef(boolean isJavaEE5) {
        /*
         * The name "EJB_SUPPORT_LIB" is defined in the
         * org.netbeans.modules.visualweb.ejb.libraries.ejbsupport-designtime.xml
         */
        String libraryName = (isJavaEE5) ? "XXX" : "EJB_SUPPORT_LIB";
        // TODO Add JavaEE 5 support!
        libraryName = "EJB_SUPPORT_LIB";
        Library libDef = LibraryManager.getDefault().getLibrary(libraryName);
        if (libDef == null)
            Util.getLogger().log(Level.SEVERE,
                    "Can not find pre-defined EJB support library" + libraryName);
        return libDef;
    }

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
     * Gets or creates the library definition containing the client wrapper classes
     */
    public static Library getWrapperClientLibDef(EjbGroup ejbGroup) {

        try {
            File wrapperClientJarFile = new File(ejbGroup.getClientWrapperBeanJar());
            String wrapperClientJarName = wrapperClientJarFile.getName();

            String libraryName = new String(wrapperClientJarName.substring(0, wrapperClientJarName
                    .lastIndexOf('.')));
            libraryName = libraryName.replaceAll("Wrapper", " ").trim();

            String libraryDesc = "Library for EJBs";
            String localizingBundle = "org.netbeans.modules.visualweb.ejb.Bundle";

            // Create the Library Definition if not create yet
            Library libDef = LibraryManager.getDefault().getLibrary(libraryName);
            if (libDef == null) {

                // The jar files in this library
                ArrayList classpathURLs = new ArrayList();

                // - wrapper jar
                classpathURLs.add(wrapperClientJarFile.toURI().toURL());

                // - all the client jars
                ArrayList clientJars = ejbGroup.getClientJarFiles();
                for (int i = 0; i < clientJars.size(); i++) {
                    String jarPath = (String) clientJars.get(i);
                    classpathURLs.add(new File(jarPath).toURI().toURL());
                }

                // <MIGRATION Fix Me - Use latest API from project >

                // libDef = JsfProjectUtils.createJ2SELibrary( libraryName,
                // libraryDesc,
                // localizingBundle,
                // LibraryDefinition.LIBRARY_DOMAIN_USER,
                // classpathURLs,
                // null, // No Souce
                // null ); // no javadocs
                // System.out.println( "########### SUCCESSFULLY created " +
                // libDef.getName() );

                // </MIGRATION>
                return libDef;
            }

            // TODO Should I call update if the library is already
            // existed??????????

            return libDef;

        } catch (java.io.IOException ie) {
            ErrorManager.getDefault().getInstance(
                    "org.netbeans.modules.visualweb.ejb.nodes.EjbLibReferenceHelper").log(
                    ErrorManager.WARNING, "create library definition failed");
            ie.printStackTrace();
            return null;
        }
    }

    /**
     * Gets or creates the library definition containing the DesignInfo classes
     */
    public static Library getDesignInfoLibDef(EjbGroup ejbGroup) {

        try {
            File designTimeJarFile = new File(ejbGroup.getDesignInfoJar());
            String designTimeJarName = designTimeJarFile.getName();

            String libraryName = new String(designTimeJarName.substring(0, designTimeJarName
                    .lastIndexOf('.')));

            String libraryDesc = "Library for EJBs";
            String localizingBundle = "org.netbeans.modules.visualweb.ejb.Bundle";

            // Create the Library Definition if not create yet
            Library libDef = LibraryManager.getDefault().getLibrary(libraryName);
            if (libDef == null) {

                // The jar files in this library
                ArrayList classpathURLs = new ArrayList();

                // - DesignInfo jar
                classpathURLs.add(designTimeJarFile.toURI().toURL());

                // <MIGRATION Fix Me - Use latest API from project >

                // libDef = JsfProjectUtils.createJ2SELibrary( libraryName,
                // libraryDesc,
                // localizingBundle,
                // LibraryDefinition.LIBRARY_DOMAIN_USER,
                // classpathURLs,
                // null, // No Souce
                // null ); // no javadocs
                // System.out.println( "########### SUCCESSFULLY created " +
                // libDef.getName() );

                // </MIGRATION>

                return libDef;
            }

            // TODO Should I call update if the library is already
            // existed??????????

            return libDef;

        } catch (java.io.IOException ie) {
            ErrorManager.getDefault().getInstance(
                    "org.netbeans.modules.visualweb.ejb.nodes.EjbLibReferenceHelper").log(
                    ErrorManager.WARNING, "create library definition failed");
            ie.printStackTrace();
            return null;
        }
    }

    /**
     * Updates the archive refs in all the current open objects
     */
    public static void updateArchiveRefs(Project[] projects, EjbGroup ejbGroup) {
        // Get the jar files from the ejb group
        Map jars = new HashMap();

        // <MIGRATION Fix Me - Use latest API from project >
        // jars.put( ejbGroup.getClientWrapperBeanJar(), new
        // JsfProjectClassPathExtender.LibraryRole[]
        // {JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN,
        // JsfProjectClassPathExtender.LIBRARY_ROLE_DEPLOY} );
        // jars.put( ejbGroup.getDesignInfoJar(), new
        // JsfProjectClassPathExtender.LibraryRole[] {
        // JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN } );
        // for( Iterator iter = ejbGroup.getClientJarFiles().iterator();
        // iter.hasNext(); ) {
        // jars.put( iter.next(), new JsfProjectClassPathExtender.LibraryRole[]
        // {JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN,
        // JsfProjectClassPathExtender.LIBRARY_ROLE_DEPLOY} );
        // }
        // </MIGRATION>

        // Update the jars in each open project
        for (int i = 0; i < projects.length; i++) {
            updateArchiveRefsToProject(projects[i], jars);
        }
    }

    public static void updateArchiveRefsToProject(Project project, Map jars) {
        // <MIGRATION Fix Me - Use latest API from project >

        // try {
        // // Obtain the path to the project's library directory
        // FileObject projectLibDir =
        // JsfProjectUtils.getProjectLibraryDirectory( project );
        // FileObject ejbSubDir = projectLibDir.getFileObject(
        // EjbDataSourceManager.EJB_DATA_SUB_DIR );
        // if( ejbSubDir == null )
        // // No ejbs in this project. Done
        // return;
        //            
        // Map role2JarsMap = new HashMap();
        //            
        // // Copy over the jar files into the project library directory
        // for( Iterator iter = jars.keySet().iterator(); iter.hasNext(); ) {
        // String jarFilePath = (String)iter.next();
        // String jarFileName = Util.getFileName( jarFilePath );
        //                
        // JsfProjectClassPathExtender.LibraryRole[] roles =
        // (JsfProjectClassPathExtender.LibraryRole[])jars.get( jarFilePath );
        //                
        // FileObject destJar = ejbSubDir.getFileObject( jarFileName );
        // if( destJar == null )
        // // This jar is not in the project. Done
        // return;
        // else
        // {
        // // Copy over (overwrite) the jar to the project data directory
        // OutputStream outStream = destJar.getOutputStream( destJar.lock() );
        // DataOutputStream out = new DataOutputStream(outStream);
        // DataInputStream in = new DataInputStream(new FileInputStream(new
        // File(jarFilePath)));
        //
        // byte[] bytes = new byte[1024];
        // int byteCount = in.read(bytes);
        //
        // while (byteCount > -1) {
        // out.write(bytes, 0, byteCount);
        // byteCount = in.read(bytes);
        // }
        // out.flush();
        // out.close();
        // outStream.close();
        // in.close();
        //                    
        // // Add the FileOject jar to the role-jars map
        // // Note: do not check whether the reference is in the project or not
        // // because it is an update.
        // for( int i = 0; i < roles.length; i ++ ) {
        // List jarsForTheRole = (ArrayList)role2JarsMap.get( roles[i] );
        // if( jarsForTheRole == null )
        // jarsForTheRole = new ArrayList();
        // jarsForTheRole.add( destJar );
        // role2JarsMap.put( roles[i], jarsForTheRole );
        // }
        //                        
        // }
        // }
        //            
        // // Finally, add the reference to the project
        // for( Iterator iter = role2JarsMap.keySet().iterator();
        // iter.hasNext(); ) {
        // JsfProjectClassPathExtender.LibraryRole role =
        // (JsfProjectClassPathExtender.LibraryRole)iter.next();
        // FileObject[] jarFileObjects =
        // (FileObject[])((ArrayList)role2JarsMap.get( role )).toArray( new
        // FileObject[0] );
        // JsfProjectUtils.removeArchiveReferences( project, jarFileObjects,
        // role );
        // JsfProjectUtils.addArchiveReferences( project, jarFileObjects, role
        // );
        // }
        //            
        // } catch( java.io.IOException ie ) {
        // ErrorManager.getDefault().notify(ie);
        // ie.printStackTrace();
        // return;
        // }
        // </MIGRATION>
    }

    /**
     * Sync the the EJB related archive refs in the given projects
     */
    public static void syncArchiveRefs(Project[] projects) {

        for (int i = 0; i < projects.length; i++) {
            try {
                FileObject projectLibDir = JsfProjectUtils.getProjectLibraryDirectory(projects[i]);
                FileObject ejbSubDir = projectLibDir
                        .getFileObject(EjbDataSourceManager.EJB_DATA_SUB_DIR);
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
                        updateArchiveRefs(new Project[] { projects[i] }, ejbGrpInModel);
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
        // Obtain the path to the project's library directory
        FileObject projectLibDir = JsfProjectUtils.getProjectLibraryDirectory(project);
        FileObject ejbSubDir = projectLibDir.getFileObject(EjbDataSourceManager.EJB_DATA_SUB_DIR);
        if (ejbSubDir == null)
            ejbSubDir = projectLibDir.createFolder(EjbDataSourceManager.EJB_DATA_SUB_DIR);

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
     * Adds the given library definitions to the project
     * 
     * @param project
     *            The project to be added to
     * @param libDefs
     *            The libraries to be added to the project. It is a map of( Library,
     *            JsfProjectClassPathExtender.LibraryRole[])
     */
    public static void addLibRefsToProject(Project project, Map libDefs) {
        // <MIGRATION Fix Me - Use latest API from project >
        // // A map of( role, lib defs for the role )
        // Map role2LibsMap = new HashMap();
        // for( Iterator iter = libDefs.keySet().iterator(); iter.hasNext(); )
        // {
        // Library libDef = (Library)iter.next();
        // JsfProjectClassPathExtender.LibraryRole[] roles =
        // (JsfProjectClassPathExtender.LibraryRole[])libDefs.get( libDef );
        // for( int i = 0; i < roles.length; i ++ )
        // {
        // ArrayList libsForTheRole = (ArrayList)role2LibsMap.get( roles[i] );
        // if( libsForTheRole == null )
        // libsForTheRole = new ArrayList();
        //                
        // if( !JsfProjectUtils.hasLibraryReference( project, libDef, roles[i] )
        // ) {
        // libsForTheRole.add( libDef );
        // role2LibsMap.put( roles[i], libsForTheRole );
        // }
        // }
        // }
        //        
        // try
        // {
        // for( Iterator iter = role2LibsMap.keySet().iterator();
        // iter.hasNext(); ) {
        // JsfProjectClassPathExtender.LibraryRole role =
        // (JsfProjectClassPathExtender.LibraryRole)iter.next();
        // Library[] libs = (Library[])
        // ((ArrayList)role2LibsMap.get(role)).toArray(new Library[0]);
        //                
        // // NOTE: Add an existing lib ref to the project again return false.
        // Mark will fix it.
        // // The if-else will be put back once Mark fixes the problem
        // JsfProjectUtils.addLibraryReferences( project, libs, role );
        // //if( !JsfProjectUtils.addLibraryReferences( project, libs, role ) )
        // // ErrorManager.getDefault().getInstance(
        // "org.netbeans.modules.visualweb.ejb.nodes.SessionBeanNode" ).log(
        // ErrorManager.WARNING, "Failed to add " + role.getName() + " library
        // reference to project. Most likely it's because it is already existed
        // in the project.");
        // //else
        // // ErrorManager.getDefault().getInstance(
        // "org.netbeans.modules.visualweb.ejb.nodes.SessionBeanNode" ).log(
        // ErrorManager.INFORMATIONAL, "########### SUCCESSFULLY added " +
        // role.getName() + " library reference to project." );
        // }
        // } catch( java.io.IOException ie ) {
        // ErrorManager.getDefault().getInstance(
        // "org.netbeans.modules.visualweb.ejb.nodes.EjbLibReferenceHelper"
        // ).log( ErrorManager.ERROR, "Failed to add library references to
        // project. IOException" );
        // ie.printStackTrace();
        // }
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

    public static EjbRefMaintainer getEjbRefMaintainer(Project project) {
        // Try to get the referred EJB groups from the ejb-refs.xml if there is
        // an ejb-refs.xml exists
        FileObject ejbRefXml = null;
        try {
            ejbRefXml = JsfProjectUtils.getProjectLibraryDirectory(project).getFileObject(
                    EjbDataSourceManager.EJB_DATA_SUB_DIR + "/" + EJB_REFS_XML);
        } catch (java.io.IOException ie) {
            // Trouble getting to the ref file
            return null;
        }

        // If the user has never added an EJB before, then there is no
        // ejb-refs.xml
        if (ejbRefXml == null)
            return null;

        EjbRefMaintainer refMaintainer = new EjbRefMaintainer(FileUtil.toFile(ejbRefXml)
                .getAbsolutePath());
        return refMaintainer;
    }

    /**
     * Add all session beans in EjbGroup grp to project. This method is idempotent.
     * 
     * @param project
     * @param grp
     */
    private static void addToWebXml(Project project, EjbGroup grp) {
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
        }
    }

    /**
     * Adds the EjbGroup to the currently active project. This method is idempotent.
     * 
     * @throws IOException
     */
    public static void addEjbGroupToActiveProject(EjbGroup ejbGroup) throws IOException {
        Project project = getActiveProject();
        boolean isJavaEE5 = JsfProjectUtils.isJavaEE5Project(project);

        // Add the EJB support lib ref to the project
        Library ejbLibDef = getEjbSupportLibDef(isJavaEE5);
        JsfProjectUtils.addLibraryReferences(project, new Library[] { ejbLibDef });

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

        // Add/update this ejb group to the ejb ref xml in the project
        addToEjbRefXmlToProject(project, ejbGroup);

        // Add an ejb-ref to the standard webapp DD, web.xml
        addToWebXml(project, ejbGroup);

        // Add it to the container-specific DD
        addToVendorDD(project, ejbGroup);
    }

    private static void addToVendorDD(Project project, EjbGroup ejbGroup) throws IOException {
        Lookup lookup = project.getLookup();
        J2eeModuleProvider provider = (J2eeModuleProvider) lookup.lookup(J2eeModuleProvider.class);
        FileObject[] configFiles = provider.getConfigurationFiles();
        for (FileObject fo : configFiles) {
            if (fo.getNameExt().equals("sun-web.xml")) {
                addToSunWebXml(ejbGroup, fo);
                // TODO Add support to other servers here
            }
        }
    }

    private static void addToSunWebXml(EjbGroup ejbGroup, FileObject fo) throws IOException {
        RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(fo);
        if (sunDDRoot instanceof SunWebApp) {
            SunWebApp sunWebXml = (SunWebApp) sunDDRoot;

            List<EjbInfo> sessionBeans = ejbGroup.getSessionBeans();
            for (EjbInfo ejbInfo : sessionBeans) {
                String refName = ejbInfo.getWebEjbRef();
                String jndiName = ejbInfo.getJNDIName();

                EjbRef ref = findEjbRefByName(sunWebXml, refName);
                if (ref != null) {
                    ref.setJndiName(jndiName);
                } else {
                    ref = sunWebXml.newEjbRef();
                    ref.setEjbRefName(refName);
                    ref.setJndiName(jndiName);
                    sunWebXml.addEjbRef(ref);
                }
            }
            sunWebXml.write(fo);
        }
    }

    private static EjbRef findEjbRefByName(SunWebApp sunWebXml, String ejbRefName) {
        EjbRef[] ejbRefs = sunWebXml.getEjbRef();
        for (EjbRef ref : ejbRefs) {
            if (ref.getEjbRefName().equals(ejbRefName)) {
                return ref;
            }
        }
        return null;
    }
}
