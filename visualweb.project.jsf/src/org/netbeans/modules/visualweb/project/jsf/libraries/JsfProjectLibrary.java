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

package org.netbeans.modules.visualweb.project.jsf.libraries;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;


/**
 *
 * @author Po-Ting Wu
 */
public class JsfProjectLibrary {

    // JSF 1.1 support libraries for both Compile and Deploy
    public static final String[] ALLTIME_LIBS_JSF11 = {
        "jsf12-support",
        "webui",
    };

    // JSF 1.1 support libraries for Compile only
    public static final String[] DESIGNTIME_LIBS_JSF11 = {
    };

    // JSF 1.1 support libraries for Deploy only
    public static final String[] RUNTIME_LIBS_JSF11 = {
        "exceptionhandler-runtime",
    };

    // JSF 1.2 support libraries for both Compile and Deploy
    public static final String[] ALLTIME_LIBS_JSF12 = {
        "jsf12-support",
        "woodstock-components",
    };

    // JSF 1.2 support libraries for Compile only
    public static final String[] DESIGNTIME_LIBS_JSF12 = {
    };

    // JSF 1.2 support libraries for Deploy only
    public static final String[] RUNTIME_LIBS_JSF12 = {
         "exceptionhandler-runtime",
    };

    public static final String DEFAULT_JSF11_THEME = "theme-default";
    public static final String DEFAULT_JSF12_THEME = "woodstock-theme-default";

    public static void addLibrary(Project project) throws IOException {
        // Add the JSF support libraries to the project
        LibraryManager libMgr = LibraryManager.getDefault();
        String[] alltimeList;
        String[] designtimeList;
        String[] runtimeList;
//        Library[] alltimeLibs;
//        Library[] designtimeLibs;
//        Library[] runtimeLibs;
        String defaultTheme;

        if (JsfProjectUtils.isJavaEE5Project(project)) {
            defaultTheme = DEFAULT_JSF12_THEME;
            alltimeList = ALLTIME_LIBS_JSF12;
            designtimeList = DESIGNTIME_LIBS_JSF12;
            runtimeList = RUNTIME_LIBS_JSF12;
        } else {
            defaultTheme = DEFAULT_JSF11_THEME;
            alltimeList = ALLTIME_LIBS_JSF11;
            designtimeList = DESIGNTIME_LIBS_JSF11;
            runtimeList = RUNTIME_LIBS_JSF11;
        }

        JsfProjectUtils.createProjectProperty(project, JsfProjectConstants.PROP_JSF_PROJECT_LIBRARIES_DIR, JsfProjectConstants.PATH_LIBRARIES);
        JsfProjectUtils.createProjectProperty(project, JsfProjectConstants.PROP_CURRENT_THEME, defaultTheme);

//        alltimeLibs = new Library[alltimeList.length + 1];
        List<Library> allTimeLibs = getLibraryList(alltimeList);
//        alltimeLibs[alltimeList.length] = libMgr.getLibrary(defaultTheme);
        allTimeLibs.addAll(getLibraryList(new String[] {defaultTheme}));

//        designtimeLibs = new Library[designtimeList.length];
        List<Library> designTimeLibs = getLibraryList(designtimeList);

//        runtimeLibs = new Library[runtimeList.length];
        List<Library> runTimeLibs = getLibraryList(runtimeList);

        JsfProjectUtils.addLibraryReferences(project, allTimeLibs.toArray(new Library[allTimeLibs.size()]));
        JsfProjectUtils.addLibraryReferences(project, designTimeLibs.toArray(new Library[designTimeLibs.size()]), ClassPath.COMPILE);
        JsfProjectUtils.addLibraryReferences(project, runTimeLibs.toArray(new Library[runTimeLibs.size()]), ClassPath.EXECUTE);

        updateLocalizedRoots(project);
    }

    private static List<Library> getLibraryList(String[] libraryNames) {
        List<Library> libraries = new ArrayList<Library>();
        for (String libraryName : libraryNames) {
            Library library = LibraryManager.getDefault().getLibrary(libraryName);
            if (library == null) {
                info(new NullPointerException("The library of name was not found, libraryName=" + libraryName)); // NOI18N
            } else {
                libraries.add(library);
            }
        }
        return libraries;
    }

    public static void updateLocalizedRoots(Project project) throws IOException {
        // Add the localized JSF support jar files to the project
        String[] alltimeList;
        String[] designtimeList;
        String[] runtimeList;
        URL[] locAlltimeList;
        URL[] locDesigntimeList;
        URL[] locRuntimeList;

        if (JsfProjectUtils.isJavaEE5Project(project)) {
            alltimeList = ALLTIME_LIBS_JSF12;
            designtimeList = DESIGNTIME_LIBS_JSF12;
            runtimeList = RUNTIME_LIBS_JSF12;
        } else {
            alltimeList = ALLTIME_LIBS_JSF11;
            designtimeList = DESIGNTIME_LIBS_JSF11;
            runtimeList = RUNTIME_LIBS_JSF11;
        }

        locAlltimeList = getLocaleRoots(project, alltimeList);
        locDesigntimeList = getLocaleRoots(project, designtimeList);
        locRuntimeList = getLocaleRoots(project, runtimeList);

        JsfProjectUtils.addLocalizedRoots(project, locAlltimeList, ClassPath.COMPILE);
        JsfProjectUtils.addLocalizedRoots(project, locDesigntimeList, ClassPath.COMPILE);
        JsfProjectUtils.addLocalizedRoots(project, locRuntimeList, ClassPath.EXECUTE);

        String defaultTheme = JsfProjectUtils.getProjectProperty(project, JsfProjectConstants.PROP_CURRENT_THEME);
        JsfProjectUtils.addLocalizedTheme(project, defaultTheme);
    }

    public static URL getLocalizedThemeRoot(Project project, String themeName) {
        try {
            URL[] list = getLocaleRoots(project, new String[] { themeName });
            if (list.length == 0) {
                return null;
            }
            return list[0];
        } catch (IOException e) {
            return null;
        }
    }

    private static URL[] getLocaleRoots(Project project, String[] libNames) throws IOException {
        ArrayList<URL> list = new ArrayList<URL>();

        for (String libName: libNames) {
            Library lib = LibraryManager.getDefault().getLibrary(libName);
            if (lib == null) {
                    continue;
            }

            List<URL> ulist = lib.getContent("classpath"); // NOI18N
            for (URL url: ulist) {
                if (!"jar".equals(url.getProtocol())) {
                    continue;
                }
                url = FileUtil.getArchiveFile(url);
                String name = url.getPath();
                int index = name.lastIndexOf("/");
                // exclude first slash:
                name = name.substring(1, index) + "/locale" + name.substring(index); // NOI18N
                File f = InstalledFileLocator.getDefault().locate(name, null, true);
                if (f != null) {
                    list.add(FileUtil.getArchiveRoot(FileUtil.toFileObject(f)).getURL());
                }
            }
        }

        return list.toArray(new URL[list.size()]);
    }

    public static boolean isDesigntimeLib(String name) {
        if (name == null) {
            return false;
        }

    	return name.startsWith("${libs.") && name.endsWith("-designtime.classpath}");
    }

    private static void info(Exception ex) {
        Logger.getLogger(JsfProjectLibrary.class.getName()).log(Level.INFO, null, ex);
    }
}
