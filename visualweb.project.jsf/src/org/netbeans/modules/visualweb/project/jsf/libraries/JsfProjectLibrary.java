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

package org.netbeans.modules.visualweb.project.jsf.libraries;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
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
    private static final String JAR_HEADER = "nbinst:///";
    private static final String JAR_TAIL = "!/";

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
        Library[] alltimeLibs;
        Library[] designtimeLibs;
        Library[] runtimeLibs;
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

        alltimeLibs = new Library[alltimeList.length + 1];
        for (int i = 0; i < alltimeList.length; i++) {
            alltimeLibs[i] = libMgr.getLibrary(alltimeList[i]);
        }
        alltimeLibs[alltimeList.length] = libMgr.getLibrary(defaultTheme);

        designtimeLibs = new Library[designtimeList.length];
        for (int i = 0; i < designtimeList.length; i++) {
            designtimeLibs[i] = libMgr.getLibrary(designtimeList[i]);
        }

        runtimeLibs = new Library[runtimeList.length];
        for (int i = 0; i < runtimeList.length; i++) {
            runtimeLibs[i] = libMgr.getLibrary(runtimeList[i]);
        }

        JsfProjectUtils.addLibraryReferences(project, alltimeLibs);
        JsfProjectUtils.addLibraryReferences(project, designtimeLibs, ClassPath.COMPILE);
        JsfProjectUtils.addLibraryReferences(project, runtimeLibs, ClassPath.EXECUTE);

        updateLocalizedRoots(project);
    }

    public static void updateLocalizedRoots(Project project) throws IOException {
        // Add the localized JSF support jar files to the project
        String[] alltimeList;
        String[] designtimeList;
        String[] runtimeList;
        String[] locAlltimeList;
        String[] locDesigntimeList;
        String[] locRuntimeList;

        if (JsfProjectUtils.isJavaEE5Project(project)) {
            alltimeList = ALLTIME_LIBS_JSF12;
            designtimeList = DESIGNTIME_LIBS_JSF12;
            runtimeList = RUNTIME_LIBS_JSF12;
        } else {
            alltimeList = ALLTIME_LIBS_JSF11;
            designtimeList = DESIGNTIME_LIBS_JSF11;
            runtimeList = RUNTIME_LIBS_JSF11;
        }

        locAlltimeList = getLocalePaths(alltimeList);
        locDesigntimeList = getLocalePaths(designtimeList);
        locRuntimeList = getLocalePaths(runtimeList);

        JsfProjectUtils.addLocalizedRoots(project, locAlltimeList);
        JsfProjectUtils.addLocalizedRoots(project, locDesigntimeList, ClassPath.COMPILE);
        JsfProjectUtils.addLocalizedRoots(project, locRuntimeList, ClassPath.EXECUTE);

        String defaultTheme = JsfProjectUtils.getProjectProperty(project, JsfProjectConstants.PROP_CURRENT_THEME);
        JsfProjectUtils.addLocalizedTheme(project, defaultTheme);
    }

    public static URL getLocalizedThemeRoot(String themeName) {
        String[] list = getLocalePaths(new String[] { themeName });
        if (list.length == 0) {
            return null;
        }
        
        File file = InstalledFileLocator.getDefault().locate(list[0], null, true);
        if (file == null) {
            return null;
        }

        try {
            return FileUtil.getArchiveRoot(FileUtil.toFileObject(file)).getURL();
        } catch (FileStateInvalidException e) {
            return null;
        }
    }

    private static String[] getLocalePaths(String[] libNames) {
        ArrayList<String> list = new ArrayList();

        for (String libName: libNames) {
            Library lib = LibraryManager.getDefault().getLibrary(libName);
            if (lib == null) {
                continue;
            }

            List<URL> ulist = lib.getContent("classpath"); // NOI18N
            for (URL url: ulist) {
                String path = url.getPath();
                if (!path.startsWith(JAR_HEADER) || !path.endsWith(JAR_TAIL)) {
                    continue;
                }

                String name = path.substring(JAR_HEADER.length(), path.length()-JAR_TAIL.length());
                int index = name.lastIndexOf("/");
                list.add(name.substring(0, index) + "/locale" + name.substring(index)); // NOI18N
            }
        }

        return list.toArray(new String[0]);
    }

    public static boolean isDesigntimeLib(String name) {
        if (name == null) {
            return false;
        }

    	return name.startsWith("${libs.") && name.endsWith("-designtime.classpath}");
    }
}
