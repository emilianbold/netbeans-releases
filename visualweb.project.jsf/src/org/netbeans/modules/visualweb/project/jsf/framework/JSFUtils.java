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

package org.netbeans.modules.visualweb.project.jsf.framework;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

// <RAVE> copy from org.netbeans.modules.j2ee.common.Util
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.openide.util.Parameters;
// </RAVE>

/**
 *
 * @author Po-Ting Wu
 */
public class JSFUtils {
    private static final String LIB_JSF_NAME = "JSF";
    private static final String LIBS_FOLDER = "org-netbeans-api-project-libraries/Libraries"; //NOI18N
    
    private static final String JSF_API_JAR = "jsf-api.jar";
    private static final String JSF_IMPL_JAR = "jsf-impl.jar";
    private static final String DIGESTER_JAR = "commons-digester.jar";
    private static final String LOGGING_JAR = "commons-logging.jar";
    private static final String COLLECTIONS_JAR = "commons-collections.jar";
    private static final String BEANUTILS_JAR = "commons-beanutils.jar";
        
    public static boolean isJSFInstallFolder(File file){
        boolean result = false;
        if (file.exists() && file.isDirectory()){
            File jsf_impl = new File(file, JSF_IMPL_JAR);
            if (jsf_impl.exists())
                result = true;
        }
        return result;
    }
    
    //XXX: Replace this when API for managing libraries is available
    public static boolean createJSFUserLibrary(File folder, final String version) throws IOException {
        assert folder != null;
        
        final File jsfLibFolder = new File(folder.getPath()); // NOI18N
        if (!jsfLibFolder.exists() || !jsfLibFolder.isDirectory())
            return false;
        
        final File jsfApiJar = new File(jsfLibFolder, JSF_API_JAR);
        final File jsfImplJar = new File(jsfLibFolder, JSF_IMPL_JAR);
        final File digesterJar = new File(jsfLibFolder, DIGESTER_JAR);
        final File loggingJar = new File(jsfLibFolder, LOGGING_JAR);
        final File collectionsJar = new File(jsfLibFolder, COLLECTIONS_JAR);
        final File beanutilsJar = new File(jsfLibFolder, BEANUTILS_JAR);
        if (!jsfApiJar.exists() || !jsfImplJar.exists()
                || !digesterJar.exists() || !loggingJar.exists()
                || !collectionsJar.exists() || !beanutilsJar.exists())
            return false;
        
        final FileSystem sysFs = Repository.getDefault().getDefaultFileSystem();
        final FileObject libsFolder = sysFs.findResource(LIBS_FOLDER);
        final String convertedVersion = convertLibraryVersion(version);
        assert libsFolder != null && libsFolder.isFolder();
        
        sysFs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                String fileName = LIB_JSF_NAME + "-" + convertedVersion; //NOI18N
                FileObject jsf = libsFolder.getFileObject(fileName + ".xml");
                if (jsf == null) {
                    jsf = libsFolder.createData(fileName + ".xml");
                }
                FileLock lock = jsf.lock();
                try {
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(jsf.getOutputStream(lock)));                    
                    try {
                        String[] resources = new String[]{
                            FileUtil.getArchiveRoot(jsfApiJar.toURI().toURL()).toString(),
                            FileUtil.getArchiveRoot(jsfImplJar.toURI().toURL()).toString(),
                            FileUtil.getArchiveRoot(digesterJar.toURI().toURL()).toString(),
                            FileUtil.getArchiveRoot(loggingJar.toURI().toURL()).toString(),
                            FileUtil.getArchiveRoot(collectionsJar.toURI().toURL()).toString(),
                            FileUtil.getArchiveRoot(beanutilsJar.toURI().toURL()).toString()
                        };
                        createLibraryFile(out, fileName, resources);
                    } finally {
                        out.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        });
        return true;
    }
    
    public static Library getJSFLibrary(final String version){
        String convertedVersion = convertLibraryVersion(version);
        return LibraryManager.getDefault().getLibrary( LIB_JSF_NAME + "-" + convertedVersion);
    }
    
    /* Converts a string to the text, which is fit for the LibraryManager as a library name
     */
    public static String convertLibraryVersion(String version){
        String converted = version;
        converted = converted.replace('.', '-');
        converted = converted.replace(' ', '_');
        return converted;
    }
        
    private static void createLibraryFile(PrintWriter out, String name, String[] resources ){
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");  //NOI18N
        out.println("<!DOCTYPE library PUBLIC \"-//NetBeans//DTD Library Declaration 1.0//EN\" \"http://www.netbeans.org/dtds/library-declaration-1_0.dtd\">");  //NOI18N
        out.println("<library version=\"1.0\">");           //NOI18N
        out.println("\t<name>"+name+"</name>");             //NOI18N
        out.println("\t<type>j2se</type>");                 //NOI18N
        out.println("\t<volume>");                          //NOI18N
        out.println("\t\t<type>classpath</type>");          //NOI18N
        for (int i = 0; i < resources.length; i++) {
            out.println("\t\t<resource>"+resources[i]+"</resource>");    //NOI18N
        }
        out.println("\t</volume>");                         //NOI18N
        out.println("\t<volume>");                          //NOI18N
        out.println("\t\t<type>src</type>");                //NOI18N
        out.println("\t</volume>");                         //NOI18N
        out.println("\t<volume>");                          //NOI18N
        out.println("\t\t<type>javadoc</type>");            //NOI18N
        out.println("\t</volume>");                         //NOI18N
        out.println("</library>");                          //NOI18N
    }
    
    // <RAVE> copy from org.netbeans.modules.j2ee.common.Util
    /**
     * Returns true if the specified classpath contains a class of the given name,
     * false otherwise.
     * 
     * @param classpath consists of jar files and folders containing classes
     * @param className the name of the class
     * 
     * @return true if the specified classpath contains a class of the given name,
     *         false otherwise.
     * 
     * @throws IOException if an I/O error has occurred
     * 
     * @since 1.15
     */
    public static boolean containsClass(Collection<File> classpath, String className) throws IOException {
        Parameters.notNull("classpath", classpath); // NOI18N
        Parameters.notNull("driverClassName", className); // NOI18N
        String classFilePath = className.replace('.', '/') + ".class"; // NOI18N
        for (File file : classpath) {
            if (file.isFile()) {
                JarFile jf = new JarFile(file);
                try {
                    Enumeration entries = jf.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = (JarEntry) entries.nextElement();
                        if (classFilePath.equals(entry.getName())) {
                            return true;
                        }
                    }
                } finally {
                    jf.close();
                }
            } else {
                if (new File(file, classFilePath).exists()) {
                    return true;
                }
            }
        }
        return false;
    }
    // </RAVE>
}
