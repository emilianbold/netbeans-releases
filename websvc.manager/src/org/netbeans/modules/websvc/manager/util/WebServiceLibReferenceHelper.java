/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.manager.util;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.websvc.manager.util.ManagerUtil;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 *
 * @author  cao
 */
public class WebServiceLibReferenceHelper {
    
    private static final String WEBSERVICE_CLIENTS_SUB_DIR = "webservice_clients"; // NOI18N
    private static final String WEB_SERVICE_PREFIX = "Web Service - ";
    private static final String WEB_SERVICE_JAXRPC_SUFFIX = "(JAX-RPC)";
    private static final String WEB_SERVICE_JAXWS_SUFFIX = "(JAX-WS)";
    private static final AddLibrary DESIGN_RUNTIME_LIBRARY_ADD = new AddLibraryToProject();
    
    /**
     * @return The library definition containing the web service support jar files
     */
    public static Library getWebServiceSupportLibDef(boolean isJ2EE_15) {
        String libraryName = (isJ2EE_15) ? "jaxws21" : "jaxrpc16";
        Library libDef = LibraryManager.getDefault().getLibrary(libraryName);
        if( libDef == null )
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.websvc.manager.libraries.WebServiceLibReferenceHelper" ).log( ErrorManager.ERROR, "Can not find pre-defined web service support library: " + libraryName );
        return libDef;
    }
    
    /**
     * Adds the given jars to the project as archive references
     *
     * @param project the project to be added to
     * @param jars jar files to be added to the project (filename Strings)
     */
    public static void addArchiveRefsToProject( Project project, List<String> jars) {
        try {
            // Obtain the path to the project's library directory
            FileObject projectLibDir = ManagerUtil.getProjectLibraryDirectory( project );
            FileObject wsClientsSubDir = projectLibDir.getFileObject( WEBSERVICE_CLIENTS_SUB_DIR );
            if( wsClientsSubDir == null )
                wsClientsSubDir = projectLibDir.createFolder( WEBSERVICE_CLIENTS_SUB_DIR );
            
            // Copy over the jar files into the project library directory
            ArrayList<URL> copiedArchiveJars = new ArrayList<URL>();
            for (String jarFilePath : jars) {
                try {
                    String jarFileName = new File(jarFilePath).getName();
                    FileObject destJar = wsClientsSubDir.getFileObject(jarFileName);
                    
                    if (destJar == null) {
                        destJar = wsClientsSubDir.createData(jarFileName);
                        copyJarFile(jarFilePath, destJar);
                    }
                    
                    copiedArchiveJars.add(new URL(destJar.getURL().toExternalForm() + "/")); // NOI18N
                }catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            
            // add archive references to the project
            addRefsToProject(project, copiedArchiveJars, new AddArchiveReferences(), new URL[0]);
        }catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            ioe.printStackTrace();
            return;            
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
     * @param project The project to be added to
     * @param libDefs The libraries to be added to the project.
     * @param role The role type to add (ClassPath.COMPILE for most cases)
     */
    public static void addLibRefsToProject( Project project, List<Library> libraries, String role) {
        addRefsToProject(project, libraries, new AddLibraryFromRole(role), new Library[0]);
    }
    
    public static void addLibRefsToProject( Project project, List<Library> libraries) {
        addRefsToProject(project, libraries, DESIGN_RUNTIME_LIBRARY_ADD, new Library[0]);
    }
    
    private static <T> void addRefsToProject( Project project, List<T> libDefs, AddLibrary<T> libraryAdder, T[] arr ) {
        ArrayList<T> libsToAdd = new ArrayList<T>();
        
        /**
         * Go through and check to see which libraries have already been added.
         */
        for (T lib : libDefs) {
            if (!libraryAdder.hasLibraryReference(project, lib)) {
                libsToAdd.add(lib);
            }
        }
        
        try {
            libraryAdder.addLibraryReferences(project, libsToAdd.toArray(arr));
        } catch( IOException ie ) {
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.websvc.manager.node.WebServiceLibReferenceHelper" ).log( ErrorManager.ERROR, "Failed to add library references to project. IOException" );
            ie.printStackTrace();
        }
    }
    
    private static interface AddLibrary<T> {
        public boolean hasLibraryReference(Project project, T library);
        public void addLibraryReferences(Project project, T[] libraries) throws IOException;
    }
    
    private static class AddLibraryFromRole implements AddLibrary<Library> {
        private String role;
        
        public AddLibraryFromRole(String role) {
            this.role = role;
        }
        
        public boolean hasLibraryReference(Project project, Library library) {
            return ManagerUtil.hasLibraryReference(project, library, role);
        }
        
        public void addLibraryReferences(Project project, Library[] libraries) throws IOException {
            ManagerUtil.addLibraryReferences(project, libraries, role);
        }
    }
    
    
    private static class AddArchiveReferences implements AddLibrary<URL> {
        
        public AddArchiveReferences() {
        }

        public boolean hasLibraryReference(Project project, URL library) {
            return ManagerUtil.hasRootReference(project, library);
        }
        
        public void addLibraryReferences(Project project, URL[] libraries) throws IOException {
            ManagerUtil.addRootReferences(project, libraries);
        }
        
    }
    
    private static class AddLibraryToProject implements AddLibrary<Library> {
        public AddLibraryToProject() {
        }
        
        public boolean hasLibraryReference(Project project, Library library) {
            return ManagerUtil.hasLibraryReference(project, library, null);
        }
        public void addLibraryReferences(Project project, Library[] libraries) throws IOException {
            ManagerUtil.addLibraryReferences(project, libraries, null);
        }
    }
    
}
