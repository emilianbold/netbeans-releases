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

package org.netbeans.modules.web.jsf;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.project.libraries.LibraryFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl, Radko Najman
 */
public class JSFUtils {
    private static final String LIB_JSF_NAME = "JSF";       //NOI18N
    private static final String LIB_FOLDER = "lib";         //NOI18N
    
    // the names of bundled jsf libraries
    public static String DEFAULT_JSF_1_1_NAME = "jsf1102";  //NOI18N
    public static String DEFAULT_JSF_1_2_NAME = "jsf12";    //NOI18N
    // the name of jstl libraryr
    public static String DEFAULT_JSTL_1_1_NAME = "jstl11";  //NOI18N
    // prefix for all jsf libraries, created by the framework. 
    public static String CREATED_JSF_NAME_PREFIX = "JSF-";  //NOI18N
    
    /** This method finds out, whether the input file is a folder that contains
     * a jsf implementation. It looks for lib folder and in this lib looks 
     * through jars, whether their contain javax.faces.FacesException class.
     * 
     * @return null if the folder contains a jsf implemention or a error message
     */
    public static String isJSFInstallFolder(File folder) {
        String result = null;
        if (folder.exists() && folder.isDirectory()) {
            File libFolder = new File(folder, LIB_FOLDER);
            if (libFolder.exists()) {
                File[] files = libFolder.listFiles(new FileFilter() {

                    public boolean accept(File pathname) {
                        boolean accepted = false;
                        if (pathname.getName().endsWith(".jar")) {  //NOI18N
                            accepted = true;
                        }
                        return accepted;
                    }
                    
                });
                if (files != null && files.length > 0) {
                    URL [] urls = new URL[files.length];
                    try {
                        for (int i = 0; i < files.length; i++) {
                            urls[i] = files[i].toURL();
                        }
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    URLClassLoader classLoader = new URLClassLoader(urls);
                    URL facesException = classLoader.getResource("javax/faces/FacesException.class"); //NOI18N
                    if (facesException == null) {
                        result = NbBundle.getMessage(JSFUtils.class, "ERROR_IS_NOT_JSF_API", libFolder.getPath());
                    }
                }                
            }
            else {
                result = NbBundle.getMessage(JSFUtils.class, "ERROR_THERE_IS_NOT_LIB_FOLDER", folder.getPath());
            }
        }
        else {
            result = NbBundle.getMessage(JSFUtils.class, "ERROR_IS_NOT_VALID_PATH", folder.getPath());
        }
        return result;
    }
    
    public static boolean createJSFUserLibrary2(File folder, final String libraryName) throws IOException {
        boolean result = false;
        
        // find all jars in the folder/lib
        File libFolder = new File(folder, LIB_FOLDER);
        if (libFolder.exists() && libFolder.isDirectory()) {
            File[] jars = libFolder.listFiles( new FileFilter () {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".jar"); //NOI18N
                }
            });

            // obtain URLs of the jar file
            List <URL> urls = new ArrayList <URL> ();
            for (int i = 0; i < jars.length; i++) {
                urls.add(jars[i].toURL());
            }

            // create new library and regist in the Library Manager. 
            LibraryManager libraryManager = LibraryManager.getDefault();
            LibraryImplementation libImpl = LibrariesSupport.getLibraryTypeProvider("j2se").createLibrary(); //NOI18N
            libImpl.setName("JSF-" + convertLibraryName(libraryName));  //NOI18N
            libImpl.setDescription(libraryName);
            libImpl.setContent("classpath", urls);  //NOI18N
            libraryManager.addLibrary(LibraryFactory.createLibrary(libImpl));
            
            result = true;
        }
        
        return result;
    }
    
    public static Library getJSFLibrary(final String version){
        String convertedVersion = convertLibraryName(version);
        return LibraryManager.getDefault().getLibrary( LIB_JSF_NAME + "-" + convertedVersion); //NOI18N
    }
    
    /* Converts a string to the text, which is fit for the LibraryManager as a library name
     */
    public static String convertLibraryName(String version){
        String converted = version;
        converted = converted.replace('.', '-');
        converted = converted.replace(' ', '_');
        return converted;
    }
}
