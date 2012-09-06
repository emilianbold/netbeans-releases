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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.clientproject.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;


/**
 * @author ads
 *
 */
public class WebClientLibraryManager {
    
    /**
     * Library TYPE.
     */
    public static String TYPE = "javascript"; // NOI18N
    
    /**
     * Volume for regular JS files.
     */
    public static String VOL_REGULAR = "regular"; // NOI18N
    
    /**
     * Volume for minified JS files.
     */
    public static String VOL_MINIFIED = "minified"; // NOI18N
    
    /**
     * Volume for documented JS files.
     */
    public static String VOL_DOCUMENTED = "documented"; // NOI18N
    
    /**
     * Real name of the library, that is without CND source prefix .
     */
    public static final String PROPERTY_REAL_NAME = "name"; // NOI18N
    
    /**
     * Default relative path for libraries folder
     */
    public static final String LIBS = "public_html/js/libs";       // NOI18N
    
    /**
     * Library version.
     */
    public static final String PROPERTY_VERSION = "version"; // NOI18N
    
    /**
     * Finds library with the specified <code>name</code> and <code>version</code>.
     * <code>version</code> could be null. In the latter case most recent version
     * will be returned.
     * @param name library name 
     * @param version library version
     * @return library 
     */
    public static Library findLibrary( String name , String version ){
        Library[] libraries = LibraryManager.getDefault().getLibraries();
        SpecificationVersion lastVersion=null;
        Library lib = null;
        for (Library library : libraries) {
            if ( library.getType().equals(TYPE)){
                String libName = library.getProperties().get(PROPERTY_REAL_NAME);
                String libVersion = library.getProperties().get(PROPERTY_VERSION);
                if ( name.equals(libName)){
                    if ( version!= null && version.equals( libVersion)){
                        return library;
                    }
                    else {
                        int index = libVersion.indexOf(' ');
                        if ( index !=-1) {
                            libVersion = libVersion.substring( 0, index);
                        }
                        try {
                            SpecificationVersion specVersion = 
                                new SpecificationVersion(libVersion);
                            if ( lastVersion == null || specVersion.compareTo(lastVersion)>0){
                                lastVersion = specVersion;
                                lib = library;
                            }
                        }
                        catch( NumberFormatException e ){
                            continue;
                        }
                    }
                }
            }
        }
        return lib;
    }
    
    /**
     * Get all versions of library with name <code>libraryName</code>
     * @param libraryName library name
     * @return all version of library
     */
    public static String[] getVersions( String libraryName ){
        Library[] libraries = LibraryManager.getDefault().getLibraries();
        List<String> result = new LinkedList<String>();
        for (Library library : libraries) {
            if ( library.getType().equals(TYPE)){
                String libName = library.getProperties().get(PROPERTY_REAL_NAME);
                if ( libName.equals(libraryName)){
                    String libVersion = library.getProperties().get(PROPERTY_VERSION);
                    int index = libVersion.indexOf(' ');
                    if ( index !=-1) {
                        libVersion = libVersion.substring( 0, index);
                    }
                    result.add(libVersion);
                }
            }
        }
        return result.toArray( new String[ result.size()]);
    }
    
    /**
     * Adds libraries to the project into the <code>folder</code>.
     * <code>volume</code> could be null. In the latter case some available 
     * volume will be used.
     * @param libraries libraries to add
     * @param folder directory in the project where libraries should be added 
     * @param volume library volume
     * @return true if all libraries are successfully  added
     */
    public static List<FileObject> addLibraries(Library[] libraries, FileObject folder , 
            String volume ) throws IOException, MissingLibResourceException
    {
        boolean missingFiles = false;
        List<FileObject> result = new LinkedList<FileObject>();
        for (Library library : libraries) {
            FileObject libRoot = folder.createFolder(
                    library.getProperties().get(
                            PROPERTY_REAL_NAME).replace(' ', '-')+"-"+ // NOI18N
                            library.getProperties().get(PROPERTY_VERSION));
            List<URL> urls =null;
            String vol = volume;
            if ( volume != null ){
                urls = library.getContent(volume);
            }
            else {
                urls = library.getContent(VOL_MINIFIED);
                if ( urls.isEmpty() ){
                    urls = library.getContent(VOL_REGULAR);
                }
                if ( urls.isEmpty() ){
                    urls = library.getContent(VOL_DOCUMENTED);
                }
            }
            assert !urls.isEmpty() : library + " "+vol; // NOI18N
            for (URL url : urls) {
                String name = url.getPath();
                name = name.substring(name.lastIndexOf("/")+1); // NOI18N
                FileObject fileObject = copySingleFile(url, name, libRoot);
                if ( fileObject == null ){
                    missingFiles = true;
                }
                else {
                    result.add(fileObject);
                }
            }
        }
        if ( missingFiles ){
            throw new MissingLibResourceException(result);
        }
        return result;
    }
    
    private static FileObject copySingleFile(URL url, String name, FileObject 
            libRoot) throws IOException 
    {
        FileObject fo = libRoot.createData(name);
        InputStream is;
        try {
            is = url.openStream();
        } 
        catch (FileNotFoundException ex) {
            Logger.getLogger(WebClientLibraryManager.class.getName()).log(Level.INFO, 
                    "could not open stream for "+url, ex); // NOI18N
            return null;
        } 
        catch (IOException ex) {
            Logger.getLogger(WebClientLibraryManager.class.getName()).log(Level.INFO, 
                    "could not open stream for "+url, ex); // NOI18N
            return null;
        }
        OutputStream os = null;
        try {
            os = fo.getOutputStream();
            FileUtil.copy(is, os);
        } finally {
            is.close();
            if (os != null) {
                os.close();
            }
        }
        return fo;
    }

}
