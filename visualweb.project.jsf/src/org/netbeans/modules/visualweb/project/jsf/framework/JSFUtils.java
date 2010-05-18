/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
        
    public static final String FACES_EXCEPTION = "javax.faces.FacesException"; //NOI18N
    public static final String JSF_1_2__API_SPECIFIC_CLASS = "javax.faces.application.StateManagerWrapper"; //NOI18N
    public static final String MYFACES_SPAECIFIC_CLASS = "org.apache.myfaces.webapp.StartupServletContextListener"; //NOI18N

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
        
        final FileObject libsFolder = FileUtil.getConfigFile(LIBS_FOLDER);
        final String convertedVersion = convertLibraryVersion(version);
        assert libsFolder != null && libsFolder.isFolder();
        
        FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
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
    
}
