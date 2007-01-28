/*
 * EjbLibReferenceHelper.java
 *
 * Created on March 6, 2005, 9:35 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectClassPathExtender;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.project.jsf.api.LibraryDefinition;
import org.netbeans.modules.visualweb.ejb.EjbDataSourceManager;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.visualweb.ejb.EjbRefMaintainer;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.util.Util;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author  cao
 */
public class EjbLibReferenceHelper {
    /**
     * Gets or creates the library definition containing the ejb20.jar
     */
    public static Library getEjb20LibDef() {
        // The name "EJB_SUPPORT_LIB" is defined in the org.netbeans.modules.visualweb.ejb.libraries.ejbsupport-designtime.xml
        Library libDef = LibraryManager.getDefault().getLibrary( "EJB_SUPPORT_LIB" ); // NO I18N
        if( libDef == null )
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.libraries.EjbLibReferenceHelper" ).log( ErrorManager.ERROR, "Can not find pre-defined EJB support library" );
        return libDef;
    }
  
    /**
     * Gets or creates the library definition containing the client wrapper classes
     */
    public static Library getWrapperClientLibDef( EjbGroup ejbGroup ) {
        
        try {
            File wrapperClientJarFile = new File( ejbGroup.getClientWrapperBeanJar() );
            String wrapperClientJarName = wrapperClientJarFile.getName();
            
            String libraryName = new String( wrapperClientJarName.substring( 0, wrapperClientJarName.lastIndexOf( '.' ) ) );
            libraryName = libraryName.replaceAll( "Wrapper", " " ).trim();
            
            String libraryDesc = "Library for EJBs";
            String localizingBundle = "org.netbeans.modules.visualweb.ejb.Bundle";
            
            // Create the Library Definition if not create yet
            Library libDef = LibraryManager.getDefault().getLibrary( libraryName );
            if( libDef == null ) {
                
                // The jar files in this library
                ArrayList classpathURLs = new ArrayList();

                // - wrapper jar
                classpathURLs.add( wrapperClientJarFile.toURI().toURL() );  

                // - all the client jars
                ArrayList clientJars = ejbGroup.getClientJarFiles();
                for( int i = 0; i < clientJars.size(); i ++ ) {
                    String jarPath = (String)clientJars.get( i );
                    classpathURLs.add( new File( jarPath ).toURI().toURL() );
                }
                
                libDef = JsfProjectUtils.createJ2SELibrary( libraryName, 
                             libraryDesc,
                             localizingBundle,
                             LibraryDefinition.LIBRARY_DOMAIN_USER,
                             classpathURLs,
                             null,  // No Souce
                             null ); // no javadocs
                //System.out.println( "########### SUCCESSFULLY created " + libDef.getName() );
                
                return libDef;
            }
            
            // TODO Should I call update if the library is already existed??????????
            
            return libDef;
            
        } catch( java.io.IOException ie ) {
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.EjbLibReferenceHelper" ).log( ErrorManager.WARNING, "create library definition failed" );
            ie.printStackTrace();
            return null;
        }
    }
    
    /**
     * Gets or creates the library definition containing the DesignInfo classes
     */
    public static Library getDesignInfoLibDef( EjbGroup ejbGroup ) {
        
        try {
            File designTimeJarFile = new File( ejbGroup.getDesignInfoJar() );
            String designTimeJarName = designTimeJarFile.getName();
            
            String libraryName = new String( designTimeJarName.substring( 0, designTimeJarName.lastIndexOf( '.' ) ) );
            
            String libraryDesc = "Library for EJBs";
            String localizingBundle = "org.netbeans.modules.visualweb.ejb.Bundle";
            
            // Create the Library Definition if not create yet
            Library libDef = LibraryManager.getDefault().getLibrary( libraryName );
            if( libDef == null ) {
                
                // The jar files in this library
                ArrayList classpathURLs = new ArrayList();

                // - DesignInfo jar
                classpathURLs.add( designTimeJarFile.toURI().toURL() ); 
                
                libDef = JsfProjectUtils.createJ2SELibrary( libraryName, 
                             libraryDesc,
                             localizingBundle,
                             LibraryDefinition.LIBRARY_DOMAIN_USER,
                             classpathURLs,
                             null,  // No Souce
                             null ); // no javadocs
                //System.out.println( "########### SUCCESSFULLY created " + libDef.getName() );
                
                return libDef;
            }
            
            // TODO Should I call update if the library is already existed??????????
            
            return libDef;
            
        } catch( java.io.IOException ie ) {
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.EjbLibReferenceHelper" ).log( ErrorManager.WARNING, "create library definition failed" );
            ie.printStackTrace();
            return null;
        }
    }
    
    /**
     * Updates the archive refs in all the current open objects
     */
    public static void updateArchiveRefs( Project[] projects, EjbGroup ejbGroup )
    {
        // Get the jar files from the ejb group
        Map jars = new HashMap();
        jars.put( ejbGroup.getClientWrapperBeanJar(), new JsfProjectClassPathExtender.LibraryRole[] {JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN, JsfProjectClassPathExtender.LIBRARY_ROLE_DEPLOY} );
        jars.put( ejbGroup.getDesignInfoJar(), new JsfProjectClassPathExtender.LibraryRole[] { JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN } );
        for( Iterator iter = ejbGroup.getClientJarFiles().iterator(); iter.hasNext(); ) {
            jars.put( iter.next(), new JsfProjectClassPathExtender.LibraryRole[] {JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN, JsfProjectClassPathExtender.LIBRARY_ROLE_DEPLOY} );
        }
        
        // Update the jars in each open project
        for( int i = 0; i < projects.length; i ++ ) 
        {
            updateArchiveRefsToProject( projects[i], jars );
        }
    }
    
    public static void updateArchiveRefsToProject( Project project, Map jars )
    {
        try {
            // Obtain the path to the project's library directory
            FileObject projectLibDir = JsfProjectUtils.getProjectLibraryDirectory( project );
            FileObject ejbSubDir = projectLibDir.getFileObject( EjbDataSourceManager.EJB_DATA_SUB_DIR );
            if( ejbSubDir == null )
                // No ejbs in this project. Done
                return;  
            
            Map role2JarsMap = new HashMap();
            
            // Copy over the jar files into the project library directory
            for( Iterator iter = jars.keySet().iterator(); iter.hasNext(); ) {
                String jarFilePath = (String)iter.next();
                String jarFileName = Util.getFileName( jarFilePath );
                
                JsfProjectClassPathExtender.LibraryRole[] roles = (JsfProjectClassPathExtender.LibraryRole[])jars.get( jarFilePath ); 
                
                FileObject destJar = ejbSubDir.getFileObject( jarFileName );
                if( destJar == null )
                    // This jar is not in the project. Done
                    return;
                else
                {
                    // Copy over (overwrite) the jar to the project data directory
                    OutputStream outStream = destJar.getOutputStream( destJar.lock() );
                    DataOutputStream out = new DataOutputStream(outStream);
                    DataInputStream in = new DataInputStream(new FileInputStream(new File(jarFilePath)));

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
                    
                    // Add the FileOject jar to the role-jars map
                    // Note: do not check whether the reference is in the project or not 
                    // because it is an update. 
                    for( int i = 0; i < roles.length; i ++ ) {
                        ArrayList jarsForTheRole = (ArrayList)role2JarsMap.get( roles[i] );
                        if( jarsForTheRole == null )
                            jarsForTheRole = new ArrayList();
                        jarsForTheRole.add( destJar );
                        role2JarsMap.put( roles[i], jarsForTheRole );
                    }
                        
                }
            }
            
            // Finally, add the reference to the project
            for( Iterator iter = role2JarsMap.keySet().iterator(); iter.hasNext(); ) {
                JsfProjectClassPathExtender.LibraryRole role = (JsfProjectClassPathExtender.LibraryRole)iter.next();
                FileObject[] jarFileObjects = (FileObject[])((ArrayList)role2JarsMap.get( role )).toArray( new FileObject[0] );
                JsfProjectUtils.removeArchiveReferences( project, jarFileObjects, role );
                JsfProjectUtils.addArchiveReferences( project, jarFileObjects, role );
            }
            
        } catch( java.io.IOException ie ) {
            ErrorManager.getDefault().notify(ie);
            ie.printStackTrace();
            return;
        }
    }
    
    /**
     * Sync the the EJB related archive refs in the given projects
     */
    public static void syncArchiveRefs( Project[] projects ) 
    {
        
        for( int i = 0; i < projects.length; i ++ ) 
        {
            try {     
                FileObject projectLibDir = JsfProjectUtils.getProjectLibraryDirectory( projects[i] );
                FileObject ejbSubDir = projectLibDir.getFileObject( EjbDataSourceManager.EJB_DATA_SUB_DIR );
                if( ejbSubDir == null )
                    // NO ejb in this project. Move on
                    continue; 
                
                // Get the ejb groups dropped in this project
                String ejbSubDirPath = FileUtil.toFile( ejbSubDir ).getAbsolutePath();
                EjbRefMaintainer refMaintainer = new EjbRefMaintainer( ejbSubDirPath + File.separator + "ejb-refs.xml" );
                Collection ejbGroups = refMaintainer.getRefferedEjbGroups();
                
                // Now lets see whehther the ejb groups are still in the data model
                for( Iterator iter = ejbGroups.iterator(); iter.hasNext(); )
                {
                    EjbGroup ejbGrp = (EjbGroup)iter.next();
                    EjbGroup ejbGrpInModel = EjbDataModel.getInstance().findEjbGroupForClientWrapperJar( Util.getFileName(ejbGrp.getClientWrapperBeanJar()));
                    
                    if( ejbGrpInModel == null )
                        // This group is gone from the data model
                        continue;
                    else
                        updateArchiveRefs( new Project[] {projects[i]}, ejbGrpInModel );
                }
            } catch( java.io.IOException ie ) {
                ErrorManager.getDefault().notify(ie);
                ie.printStackTrace();
                continue;
            }
        }
    }
     
    /**
     * Adds the given jars to the project as archive references
     *
     * @param project the project to be added to
     * @param jars jar files to be added to the project. It is a map of (jar file full path, JsfProjectClassPathExtender.LibraryRole[])
     */
    public static void addArchiveRefsToProject( Project project, Map jars )
    {
        try {
            // Obtain the path to the project's library directory
            FileObject projectLibDir = JsfProjectUtils.getProjectLibraryDirectory( project );
            FileObject ejbSubDir = projectLibDir.getFileObject( EjbDataSourceManager.EJB_DATA_SUB_DIR );
            if( ejbSubDir == null )
                ejbSubDir = projectLibDir.createFolder( EjbDataSourceManager.EJB_DATA_SUB_DIR );   
            
            Map role2JarsMap = new HashMap();
            
            // Copy over the jar files into the project library directory
            for( Iterator iter = jars.keySet().iterator(); iter.hasNext(); ) {
                String jarFilePath = (String)iter.next();
                String jarFileName = Util.getFileName( jarFilePath );
                
                JsfProjectClassPathExtender.LibraryRole[] roles = (JsfProjectClassPathExtender.LibraryRole[])jars.get( jarFilePath ); 
                
                FileObject destJar = ejbSubDir.getFileObject( jarFileName );
                if( destJar == null ) 
                {
                    // Copy the jar to the project data directory
                    destJar = ejbSubDir.createData( jarFileName );
                    
                    OutputStream outStream = destJar.getOutputStream( destJar.lock() );
                    DataOutputStream out = new DataOutputStream(outStream);
                    DataInputStream in = new DataInputStream(new FileInputStream(new File(jarFilePath)));

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
                }
                
                // Add the FileOject jar to the role-jars map if the ref is not in the project yet
                for( int i = 0; i < roles.length; i ++ ) {
                    ArrayList jarsForTheRole = (ArrayList)role2JarsMap.get( roles[i] );
                    if( jarsForTheRole == null )
                        jarsForTheRole = new ArrayList();

                    if( !JsfProjectUtils.hasArchiveReference(project,destJar, roles[i]) ) {
                        jarsForTheRole.add( destJar );
                        role2JarsMap.put( roles[i], jarsForTheRole );
                    }
                }
            }
            
            // Finally, add the reference to the project
            for( Iterator iter = role2JarsMap.keySet().iterator(); iter.hasNext(); ) {
                JsfProjectClassPathExtender.LibraryRole role = (JsfProjectClassPathExtender.LibraryRole)iter.next();
                FileObject[] jarFileObjects = (FileObject[])((ArrayList)role2JarsMap.get( role )).toArray( new FileObject[0] );
                JsfProjectUtils.addArchiveReferences( project, jarFileObjects, role );
            }
            
        } catch( java.io.IOException ie ) {
            ErrorManager.getDefault().notify(ie);
            ie.printStackTrace();
            return;
        }
    }
    
    /**
     * Adds the given library definitions to the project
     *
     * @param project The project to be added to
     * @param libDefs The libraries to be added to the project. It is a map of( Library, JsfProjectClassPathExtender.LibraryRole[])
     */
    public static void addLibRefsToProject( Project project, Map libDefs ) 
    {
        // A map of( role, lib defs for the role )
        Map role2LibsMap = new HashMap();
        for( Iterator iter = libDefs.keySet().iterator(); iter.hasNext(); ) 
        {
            Library libDef = (Library)iter.next();
            JsfProjectClassPathExtender.LibraryRole[] roles = (JsfProjectClassPathExtender.LibraryRole[])libDefs.get( libDef );
            for( int i = 0; i < roles.length; i ++ ) 
            {
                ArrayList libsForTheRole = (ArrayList)role2LibsMap.get( roles[i] );
                if( libsForTheRole == null )
                    libsForTheRole = new ArrayList();
                
                if( !JsfProjectUtils.hasLibraryReference( project, libDef, roles[i] ) ) {
                    libsForTheRole.add( libDef );
                    role2LibsMap.put( roles[i], libsForTheRole );
                }
            }
        }
        
        try  
        {
            for( Iterator iter = role2LibsMap.keySet().iterator(); iter.hasNext(); ) {
                JsfProjectClassPathExtender.LibraryRole role = (JsfProjectClassPathExtender.LibraryRole)iter.next();
                Library[] libs = (Library[]) ((ArrayList)role2LibsMap.get(role)).toArray(new Library[0]);
                
                // NOTE: Add an existing lib ref to the project again return false. Mark will fix it.
                // The if-else will be put back once Mark fixes the problem
                JsfProjectUtils.addLibraryReferences( project, libs, role );
                //if( !JsfProjectUtils.addLibraryReferences( project, libs, role ) )
                //    ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.SessionBeanNode" ).log( ErrorManager.WARNING, "Failed to add " + role.getName() + " library reference to project. Most likely it's because it is already existed in the project.");
                //else
                //    ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.SessionBeanNode" ).log( ErrorManager.INFORMATIONAL, "########### SUCCESSFULLY added " + role.getName() + " library reference to project." );
            }
        } catch( java.io.IOException ie ) { 
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.EjbLibReferenceHelper" ).log( ErrorManager.ERROR, "Failed to add library references to project. IOException" );
            ie.printStackTrace();
        } 
    }
     
    /**
     * Adds the reference information of the ejbs in the give ejb group to the project.
     *
     * @param project The project to be added to
     * @param ejbGroup
     */
    public static void addToEjbRefXmlToProject( Project project, EjbGroup ejbGroup )
    {
        try {
            FileObject projectLibDir = JsfProjectUtils.getProjectLibraryDirectory( project );
            FileObject ejbSubDir = projectLibDir.getFileObject( EjbDataSourceManager.EJB_DATA_SUB_DIR );
            if( ejbSubDir == null )
                ejbSubDir = projectLibDir.createFolder( EjbDataSourceManager.EJB_DATA_SUB_DIR ); 
            
            String ejbSubDirPath = FileUtil.toFile( ejbSubDir ).getAbsolutePath();
            
            // Before writing the the ejb group to the xml,
            // change the client jars and wrapper bean jar locations to the project/lib/ejb-sources
            EjbGroup ejbGrpCopy = (EjbGroup)ejbGroup.clone();
            
            ArrayList newClientPaths = new ArrayList();
            for( Iterator iter = ejbGroup.getClientJarFiles().iterator(); iter.hasNext(); ) {
                String jarPath = (String)iter.next();
                newClientPaths.add( ejbSubDirPath + "/" + (new File(jarPath)).getName() );
            }
            ejbGrpCopy.setClientJarFiles( newClientPaths );
            
            ejbGrpCopy.setClientWrapperBeanJar( ejbSubDirPath + "/" + (new File(ejbGroup.getClientWrapperBeanJar())).getName() ); 
            ejbGrpCopy.setDesignInfoJar( ejbSubDirPath + "/" + (new File(ejbGroup.getDesignInfoJar())).getName() );
            
            EjbRefMaintainer refMaintainer = new EjbRefMaintainer( ejbSubDirPath + File.separator + "ejb-refs.xml" );
            refMaintainer.addToEjbRefXml( ejbGrpCopy );
        } catch( java.io.IOException ie ) {
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.nodes.EjbLibReferenceHelper" ).log( ErrorManager.WARNING, "Failed to save ejb refs" );
            ie.printStackTrace();
        }
    }
}
