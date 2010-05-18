/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.vmd.componentssupport.ui.helpers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.libraries.Library;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

/**
 * Helper class for getting preview of modifictions planned for adding 
 * library descriptor.
 * 
 * @author avk
 */
public class JavaMELibsPreviewHelper extends JavaMELibsConfigurationHelper{
    
    //private static final String MSG_ERR_CANT_FIND_JAR = "MSG_ERR_CantFindLibJar";       // NOI18N
        
    /**
     * parses library content. For each archive or folder registered 
     * in library prepares target path where this archive will be copied 
     * after adding library rescriptor to the project.
     * For folder returns zip arhive with name = library name.
     * @param library
     * @param libName library name
     * @return target path relative to project directory.
     */
    public static List<String> extractLibraryJarsPaths(Library library, String libName)
            //throws LibraryParsingException
    {
        List<String> list = new ArrayList<String>();

        Iterator<URL> it = null;
        
        it = library.getContent(VOLUME_CLASS).iterator();
        list.addAll( getFinalJarsPaths(it, LIB_EXTRACT_PATH_LIBS, libName) );//NOI18N
        
        it = library.getContent(VOLUME_SRC).iterator();
        list.addAll( getFinalJarsPaths(it, LIB_EXTRACT_SOURCES, libName) );//NOI18N
        
        it = library.getContent(VOLUME_JAVADOC).iterator();
        list.addAll( getFinalJarsPaths(it, LIB_EXTRACT_DOCS, libName) );//NOI18N
        
        return list;
    }
    
    private static List<String> getFinalJarsPaths(Iterator<URL> it, 
            String pathPrefix, String libName) 
            //throws LibraryParsingException
    {
        List<String> archives = new ArrayList<String>();
        while (it.hasNext()) {
            URL originalURL = it.next();
            String archive = createFinalPath(originalURL, pathPrefix, libName);//NOI18N
            if (archive != null) {
                archives.add(archive);
            }
        }
        return archives;
    }
    
    /* 
     * returns archive name or temporarily null 
     * cause there is no zip support for file protocol  
     */
    private static String createFinalPath(URL originalURL, 
            String pathPrefix, String libName) 
            //throws LibraryParsingException
    {
        URL archivURL = FileUtil.getArchiveFile(originalURL);
        
        if (archivURL != null && FileUtil.isArchiveFile(archivURL)) {
            return createFinalPath4Archive(archivURL, pathPrefix);
        } else {
            return createFinalPath4Folder(originalURL, pathPrefix, libName);
        }
    }
    
    private static String createFinalPath4Archive(URL archivURL, String pathPrefix) 
            //throws LibraryParsingException
    {
        FileObject archiv = URLMapper.findFileObject(archivURL);
        
        // null processing was fix for #129617. 
        // returning null is fix for #138126
        if (archiv == null){
            return null;
            //String msg = getMessage(MSG_ERR_CANT_FIND_JAR, archivURL);
            //throw new JavaMELibsPreviewHelper.LibraryParsingException(msg);
        }
        
        String name = archiv.getNameExt();
        StringBuffer sb = new StringBuffer();
        sb.append(pathPrefix).append(name);
        return sb.toString();
    }

    private static String createFinalPath4Folder(URL originalURL, 
            String pathPrefix, String libName) 
    {
        if (FILE_PROTOCOL.equals(originalURL.getProtocol())) {//NOI18N

            FileObject folderToZip;
            folderToZip = URLMapper.findFileObject(originalURL);
            if (folderToZip != null) {
                String name = libName + ZIP_EXTENSION;//NOI18N

                return pathPrefix + name;
            }
        }
        return null;
    }
    
    private static String getMessage(String key, Object... args) {
        return NbBundle.getMessage(JavaMELibsPreviewHelper.class, key, args);
    }

    /*
    public static class LibraryParsingException extends Exception {

        // no need in other constructors. want only to set error message.
        public LibraryParsingException(String message) {
            super(message);
        }
    }
     */

}
