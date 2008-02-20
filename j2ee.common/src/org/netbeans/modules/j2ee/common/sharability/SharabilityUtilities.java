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

package org.netbeans.modules.j2ee.common.sharability;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.sharability.impl.ServerLibraryTypeProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Hejl
 * @since 1.21
 */
public final class SharabilityUtilities {

    public static final String DEFAULT_LIBRARIES_FILENAME = "nblibraries.properties";

    private static final Logger LOGGER = Logger.getLogger(SharabilityUtilities.class.getName());

    private SharabilityUtilities() {
        super();
    }

    public static String getLibraryLocation(String librariesDir) {
        String librariesDefinition = librariesDir;
        if (librariesDefinition != null) {
            if (!librariesDefinition.endsWith(File.separator)) {
                librariesDefinition += File.separatorChar;
            }
            librariesDefinition += SharabilityUtilities.DEFAULT_LIBRARIES_FILENAME;
        }
        return librariesDefinition;
    }

    public static Library getLibrary(File location, String libraryName) throws IOException {
        Parameters.notNull("location", location); // NOI18N

        FileObject libraries = FileUtil.toFileObject(FileUtil.normalizeFile(location));
        if (libraries == null) {
            return null;
        }

        LibraryManager manager = LibraryManager.forLocation(URLMapper.findURL(
                libraries, URLMapper.EXTERNAL));
        return manager.getLibrary(libraryName);
    }

    public static Library[] getLibraries(File location) {
        Parameters.notNull("location", location); // NOI18N

        FileObject libraries = FileUtil.toFileObject(FileUtil.normalizeFile(location));
        if (libraries == null) {
            return null;
        }

        LibraryManager manager = LibraryManager.forLocation(URLMapper.findURL(
                libraries, URLMapper.EXTERNAL));
        List<Library> ret = new  ArrayList<Library>();
        for (Library lib : manager.getLibraries()) {
            if (lib.getType().equals(ServerLibraryTypeProvider.LIBRARY_TYPE)) {
                ret.add(lib);
            }
        }
        return ret.toArray(new Library[ret.size()]);
    }

    public static Library createLibrary(File location, String libraryName, String serverInstanceId) throws IOException {
        Parameters.notNull("location", location); // NOI18N

        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceId);
        if (platform == null) {
            throw new IOException("Server instance does not exist"); // NOI18N
        }
//        return createLibrary(location, libraryName,
//                platform.getClasspathEntries(),
//                platform.getToolClasspathEntries(J2eePlatform.TOOL_WSCOMPILE),
//                platform.getToolClasspathEntries(J2eePlatform.TOOL_WSGEN),
//                platform.getToolClasspathEntries(J2eePlatform.TOOL_WSIMPORT),
//                platform.getToolClasspathEntries(J2eePlatform.TOOL_WSIT),
//                platform.getToolClasspathEntries(J2eePlatform.TOOL_JWSDP),
//                platform.getToolClasspathEntries(J2eePlatform.TOOL_APP_CLIENT_RUNTIME));
        return createLibrary(location, libraryName, platform.getClasspathEntries());        
    }

    private static Library createLibrary(File location, String libraryName, File[] files) throws IOException {
        Parameters.notNull("location", location); // NOI18N

        FileObject libraries = FileUtil.toFileObject(FileUtil.normalizeFile(location));
        if (libraries == null) {
            throw new IOException("Library folder does not exist"); // NOI18N
        }

        URL url = URLMapper.findURL(libraries, URLMapper.EXTERNAL);

        LibraryManager manager = LibraryManager.forLocation(url);
        Map<String, List<URL>> content = new HashMap<String, List<URL>>();
        List<URL> classpath = new  ArrayList<URL>();
        content.put(ServerLibraryTypeProvider.VOLUME_CLASSPATH, classpath); // NOI18N

        FileObject baseFolder = libraries.getParent();
        String folderName = getFolderName(baseFolder, libraryName);
        FileObject jarFolder = FileUtil.createFolder(baseFolder, folderName);
        Map<String, Integer> usedNames = new  HashMap<String, Integer>();
        
        List<URL> contentItem = new ArrayList<URL>();
        content.put(ServerLibraryTypeProvider.VOLUME_CLASSPATH, contentItem); // NOI18N
        copyFiles(usedNames, jarFolder, folderName, files, contentItem);

        return manager.createLibrary(ServerLibraryTypeProvider.LIBRARY_TYPE, libraryName, content); // NOI18N
    }
    
    private static Library createLibrary(File location, String libraryName,
            File[] platformFiles, File[] wsCompileFiles, File[] wsGenerateFiles,
            File[] wsImportFiles, File[] wsInteropFiles, File[] wsJwsdpFiles,
            File[] appClientFiles) throws IOException {
        
        Parameters.notNull("location", location); // NOI18N

        FileObject libraries = FileUtil.toFileObject(FileUtil.normalizeFile(location));
        if (libraries == null) {
            throw new IOException("Library folder does not exist"); // NOI18N
        }

        URL url = URLMapper.findURL(libraries, URLMapper.EXTERNAL);

        LibraryManager manager = LibraryManager.forLocation(url);
        Map<String, List<URL>> content = new HashMap<String, List<URL>>();

        FileObject baseFolder = libraries.getParent();
        String folderName = getFolderName(baseFolder, libraryName);
        FileObject jarFolder = FileUtil.createFolder(baseFolder, folderName);
        Map<String, Integer> usedNames = new  HashMap<String, Integer>();
        
        List<URL> contentItem = new ArrayList<URL>();
        content.put(ServerLibraryTypeProvider.VOLUME_CLASSPATH, contentItem); // NOI18N
        copyFiles(usedNames, jarFolder, folderName, platformFiles, contentItem);
        
        contentItem = new  ArrayList<URL>();
        content.put(ServerLibraryTypeProvider.VOLUME_WS_COMPILE_CLASSPATH, contentItem);
        copyFiles(usedNames, jarFolder, folderName, wsCompileFiles, contentItem);

        contentItem = new  ArrayList<URL>();
        content.put(ServerLibraryTypeProvider.VOLUME_WS_GENERATE_CLASSPATH, contentItem);
        copyFiles(usedNames, jarFolder, folderName, wsGenerateFiles, contentItem);        
        
        contentItem = new  ArrayList<URL>();
        content.put(ServerLibraryTypeProvider.VOLUME_WS_IMPORT_CLASSPATH, contentItem);
        copyFiles(usedNames, jarFolder, folderName, wsImportFiles, contentItem);  
        
        contentItem = new  ArrayList<URL>();
        content.put(ServerLibraryTypeProvider.VOLUME_WS_INTEROP_CLASSPATH, contentItem);
        copyFiles(usedNames, jarFolder, folderName, wsInteropFiles, contentItem); 
        
        contentItem = new  ArrayList<URL>();
        content.put(ServerLibraryTypeProvider.VOLUME_WS_JWSDP_CLASSPATH, contentItem);
        copyFiles(usedNames, jarFolder, folderName, wsJwsdpFiles, contentItem);
        
        contentItem = new  ArrayList<URL>();
        content.put(ServerLibraryTypeProvider.VOLUME_APP_CLIENT_CLASSPATH, contentItem);
        copyFiles(usedNames, jarFolder, folderName, appClientFiles, contentItem);

        return manager.createLibrary(ServerLibraryTypeProvider.LIBRARY_TYPE, libraryName, content); // NOI18N
    }    

    private static void copyFiles(Map<String, Integer> usedNames, FileObject jarFolder, String folderName, File[] files, List<URL> content) throws IOException {
        if (files == null) {
            return;
        }
        
        for (File jarFile : files) {
            FileObject jarObject = FileUtil.toFileObject(FileUtil.normalizeFile(jarFile));
            if (jarObject != null) {
                String name = jarObject.getName() + getEntrySuffix(jarObject.getNameExt(), usedNames);
                if (jarObject.isFolder()) {
                    FileObject folder = FileUtil.createFolder(jarFolder, name);
                    copyFolder(jarObject, folder);
                } else {
                    FileUtil.copyFile(jarObject, jarFolder, name, jarObject.getExt());
                }

                URL u = LibrariesSupport.convertFilePathToURL(folderName
                        + File.separator + jarObject.getNameExt().replace(jarObject.getName(), name));
                content.add(u);                    
            } else {
                LOGGER.log(Level.INFO, "Could not find " + jarFile); // NOI18N
            }
        }
    }
    
    private static void copyFolder(FileObject source, FileObject dest) throws IOException {
        assert source.isFolder() : "Source is not a folder"; // NOI18N
        assert dest.isFolder() : "Source is not a folder"; // NOI18N
        
        for (FileObject child : source.getChildren()) {
            if (child.isFolder()) {
                FileObject created = FileUtil.createFolder(dest, child.getNameExt());
                copyFolder(child, created);
            } else {
                FileUtil.copyFile(child, dest, child.getName(), child.getExt());
            }
        }
    }
    
    private static String getEntrySuffix(String realName, Map<String, Integer> usages) {
        Integer value = usages.get(realName);
        if (value == null) {
            value = Integer.valueOf(0);
        } else {
            value = Integer.valueOf(value.intValue() + 1);
        }

        usages.put(realName, value);
        if (value.intValue() == 0) {
            return ""; // NOI18N
        }
        return "-" + value.toString();
    }

    private static String getFolderName(FileObject baseFolder, String libraryName) {
        int suffix = 2;
        String baseName = libraryName;  //NOI18N

        String name = baseName;
        while (baseFolder.getFileObject(name) != null) {
            name = baseName + "-" + suffix; // NOI18N
            suffix++;
        }
        return name;
    }
}
