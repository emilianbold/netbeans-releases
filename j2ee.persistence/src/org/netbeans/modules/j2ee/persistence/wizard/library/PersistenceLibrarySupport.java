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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.wizard.library;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.xml.XMLUtil;

/**
 * Various stuff copied mostly from org.netbeans.modules.project.libraries,
 * because it is not possible to add library to Library manager throught some API.
 */
public class PersistenceLibrarySupport  {
    
    public static final String VOLUME_TYPE_CLASSPATH = "classpath";       //NOI18N
    public static final String VOLUME_TYPE_SRC = "src";       //NOI18N
    public static final String VOLUME_TYPE_JAVADOC = "javadoc";       //NOI18N
    public static final String LIBRARY_TYPE = "j2se";       //NOI18N
    public static final String[] VOLUME_TYPES = new String[] {
        VOLUME_TYPE_CLASSPATH,
        VOLUME_TYPE_SRC,
        VOLUME_TYPE_JAVADOC,
    };
    
    private static final String LIBRARIES_REPOSITORY = "org-netbeans-api-project-libraries/Libraries";  //NOI18N
    private static int MAX_DEPTH = 3;
    
    private FileObject storage = null;
    private static PersistenceLibrarySupport instance;
    
    private PersistenceLibrarySupport() {
    }
    
    public static PersistenceLibrarySupport getDefault() {
        if (instance == null) {
            instance = new PersistenceLibrarySupport();
        }
        return instance;
    }
    
    public void addLibrary(LibraryImplementation library) {
        this.initStorage();
        assert this.storage != null : "Storage is not initialized";
        try {
            writeLibrary(this.storage,library);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private static final FileObject createStorage() {
        FileSystem storageFS = Repository.getDefault().getDefaultFileSystem();
        try {
            return FileUtil.createFolder(storageFS.getRoot(), LIBRARIES_REPOSITORY);
        } catch (IOException e) {
            return null;
        }
    }
    
    private synchronized void initStorage() {
        if (this.storage == null) {
            this.storage = createStorage();
            if (storage == null) {
                return;
            }
        }
    }
    
    private void writeLibrary(final FileObject storage, final LibraryImplementation library) throws IOException {
        storage.getFileSystem().runAtomicAction(
                new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject fo = storage.createData(library.getName(),"xml");   //NOI18N
                writeLibraryDefinition(fo, library);
            }
        }
        );
    }
    
    private static void writeLibraryDefinition(final FileObject definitionFile, final LibraryImplementation library) throws IOException {
        FileLock lock = null;
        PrintWriter out = null;
        try {
            lock = definitionFile.lock();
            out = new PrintWriter(new OutputStreamWriter(definitionFile.getOutputStream(lock),"UTF-8"));
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");      //NOI18N
            out.println("<!DOCTYPE library PUBLIC \"-//NetBeans//DTD Library Declaration 1.0//EN\" \"http://www.netbeans.org/dtds/library-declaration-1_0.dtd\">"); //NOI18N
            out.println("<library version=\"1.0\">");       			//NOI18N
            out.println("\t<name>"+library.getName()+"</name>");        //NOI18N
            out.println("\t<type>"+library.getType()+"</type>");
            String description = library.getDescription();
            if (description != null && description.length() > 0) {
                out.println("\t<description>"+description+"</description>");   //NOI18N
            }
            String localizingBundle = library.getLocalizingBundle();
            if (localizingBundle != null && localizingBundle.length() > 0) {
                out.println("\t<localizing-bundle>"+XMLUtil.toElementContent(localizingBundle)+"</localizing-bundle>");   //NOI18N
            }
            String[] volumeTypes = VOLUME_TYPES;
            for (int i = 0; i < volumeTypes.length; i++) {
                out.println("\t<volume>");      //NOI18N
                out.println("\t\t<type>"+volumeTypes[i]+"</type>");   //NOI18N
                List volume = library.getContent(volumeTypes[i]);
                if (volume != null) {
                    //If null -> broken library, repair it.
                    for (Iterator eit = volume.iterator(); eit.hasNext();) {
                        URL url = (URL) eit.next();
                        out.println("\t\t<resource>"+XMLUtil.toElementContent(url.toExternalForm())+"</resource>"); //NOI18N
                    }
                }
                out.println("\t</volume>");     //NOI18N
            }
            out.println("</library>");  //NOI18N
        } finally {
            if (out !=  null)
                out.close();
            if (lock != null)
                lock.releaseLock();
        }
    }
    
    // from org.netbeans.modules.java.j2seproject.queries.JavadocForBinaryQueryImpl
    
    /**
     * Tests if the query accepts the root as valid JavadocRoot,
     * the query accepts the JavaDoc root, if it can find the index-files
     * or index-all.html in the root.
     * @param rootURL the javadoc root
     * @return true if the root is a valid Javadoc root
     */
    public static boolean isValidLibraryJavadocRoot(final URL rootURL) {
        assert rootURL != null && rootURL.toExternalForm().endsWith("/");
        final FileObject root = URLMapper.findFileObject(rootURL);
        if (root == null) {
            return false;
        }
        return findIndexFolder(root,1) != null;
    }
    
    private static FileObject findIndexFolder(FileObject fo, int depth) {
        if (depth > MAX_DEPTH) {
            return null;
        }
        if (fo.getFileObject("index-files",null)!=null || fo.getFileObject("index-all.html",null)!=null) {  //NOI18N
            return fo;
        }
        FileObject[] children = fo.getChildren();
        for (int i=0; i< children.length; i++) {
            if (children[i].isFolder()) {
                FileObject result = findIndexFolder(children[i], depth+1);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    /**
     *@return true if the given library contains a class with the given name.
     */ 
    public static boolean containsClass(Library library, String className) {
        String classRelativePath = className.replace('.', '/') + ".class"; //NOI18N
        return containsPath(library.getContent("classpath"), classRelativePath); //NOI18N
    }
    
    /**
     *@return true if the given library contains a service with the given name.
     */ 
    public static boolean containsService(Library library, String serviceName) {
        String serviceRelativePath = "META-INF/services/" + serviceName; //NOI18N
        return containsPath(library.getContent("classpath"), serviceRelativePath); //NOI18N
    }
    
    /**
     *@return true if the given library contains a class with the given name.
     */ 
    public static boolean containsClass(LibraryImplementation library, String className) {
        String classRelativePath = className.replace('.', '/') + ".class"; //NOI18N
        return containsPath(library.getContent("classpath"), classRelativePath); //NOI18N
    }
    
    /**
     *@return true if the given library contains a service with the given name.
     */ 
    public static boolean containsService(LibraryImplementation library, String serviceName) {
        String serviceRelativePath = "META-INF/services/" + serviceName; //NOI18N
        return containsPath(library.getContent("classpath"), serviceRelativePath); //NO18N
    }
    
    private static boolean containsPath(List<URL> roots, String relativePath) {
        for (URL each :roots){
            FileObject root = URLMapper.findFileObject(each);
            if (root != null && "jar".equals(each.getProtocol())) {  //NOI18N
                FileObject archiveRoot = FileUtil.getArchiveRoot(FileUtil.getArchiveFile(root));
                if (archiveRoot.getFileObject(relativePath) != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
}
