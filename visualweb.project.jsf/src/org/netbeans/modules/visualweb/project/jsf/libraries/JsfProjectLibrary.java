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
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectClassPathExtender;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.modules.InstalledFileLocator;


/**
 *
 * @author Po-Ting Wu
 */
public class JsfProjectLibrary {
    private static final String JAR_HEADER = "nbinst:///";
    private static final String JAR_TAIL = "!/";

    public static final String[] DESIGNTIME_LIBS_JSF11 = {
        "jsf-designtime",
        "jsfsupport-designtime",
        "webui-designtime",
        "jdbcsupport-designtime",
    };

    public static final String[] RUNTIME_LIBS_JSF11 = {
        "jsf-runtime",
        "jsfsupport-runtime",
        "webui-runtime",
        "jdbcsupport-runtime",
        "exceptionhandler-runtime",
    };

    public static final String[] DESIGNTIME_LIBS_JSF12 = {
        "jsf12-support",
        "woodstock-components",
        "jdbcsupport-designtime",
    };

    public static final String[] RUNTIME_LIBS_JSF12 = {
         "jsf12-support",
         "woodstock-components",
         "jdbcsupport-runtime",
         "exceptionhandler-runtime",
    };

    public static final String DEFAULT_JSF11_THEME = "theme-default";
    public static final String DEFAULT_JSF12_THEME = "woodstock-theme-default";

    public static String addLibrary(Project project) throws IOException {
        // Add the Creator libraries to the project
        LibraryManager libMgr = LibraryManager.getDefault();
        String[] designtimeList;
        String[] runtimeList;
        Library[] designtimeLibs;
        Library[] runtimeLibs;
        String defaultTheme;

        if (JsfProjectUtils.isJavaEE5Project(project)) {
            defaultTheme = DEFAULT_JSF12_THEME;
            designtimeList = DESIGNTIME_LIBS_JSF12;
            runtimeList = RUNTIME_LIBS_JSF12;
        } else {
            defaultTheme = DEFAULT_JSF11_THEME;
            designtimeList = DESIGNTIME_LIBS_JSF11;
            runtimeList = RUNTIME_LIBS_JSF11;
        }

        designtimeLibs = new Library[designtimeList.length+1];
        for (int i = 0; i < designtimeList.length; i++) {
            designtimeLibs[i] = libMgr.getLibrary(designtimeList[i]);
        }
        designtimeLibs[designtimeList.length] = libMgr.getLibrary(defaultTheme);

        runtimeLibs = new Library[runtimeList.length+1];
        for (int i = 0; i < runtimeList.length; i++) {
            runtimeLibs[i] = libMgr.getLibrary(runtimeList[i]);
        }
        runtimeLibs[runtimeList.length] = libMgr.getLibrary(defaultTheme);

        JsfProjectUtils.addLibraryReferences(project, designtimeLibs, JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN);
        JsfProjectUtils.addLibraryReferences(project, runtimeLibs, JsfProjectClassPathExtender.LIBRARY_ROLE_DEPLOY);
        JsfProjectUtils.addLocalizedThemeArchive(project, defaultTheme);

        return defaultTheme;
    }

    public static void updateLocalizedArchives(Project project) throws IOException {
        String[] designtimeList;
        String[] runtimeList;
        String[] locDesigntimeList;
        String[] locRuntimeList;

        if (JsfProjectUtils.isJavaEE5Project(project)) {
            designtimeList = DESIGNTIME_LIBS_JSF12;
            runtimeList = RUNTIME_LIBS_JSF12;
        } else {
            designtimeList = DESIGNTIME_LIBS_JSF11;
            runtimeList = RUNTIME_LIBS_JSF11;
        }

        locDesigntimeList = getLocalePaths(designtimeList);
        locRuntimeList = getLocalePaths(runtimeList);

        JsfProjectUtils.addLocalizedArchives(project, locDesigntimeList, JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN);
        JsfProjectUtils.addLocalizedArchives(project, locRuntimeList, JsfProjectClassPathExtender.LIBRARY_ROLE_DEPLOY);

        String defaultTheme = JsfProjectUtils.getProjectProperty(project, JsfProjectConstants.PROP_CURRENT_THEME);
        JsfProjectUtils.addLocalizedThemeArchive(project, defaultTheme);
    }

    public static File getLocalizedThemeArchive(String themeName) {
        String[] list = getLocalePaths(new String[] { themeName });
        if (list.length == 0) {
            return null;
        }
        
        return InstalledFileLocator.getDefault().locate(list[0], null, true);
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
