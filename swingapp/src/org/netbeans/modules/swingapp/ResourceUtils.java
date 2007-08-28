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

package org.netbeans.modules.swingapp;

import org.jdesktop.application.ResourceMap;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.properties.BundleStructure;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Utility class for working with resources (creating properties files, resource
 * maps, etc).
 * 
 * @author Tomas Pavek
 */
class ResourceUtils {

    // maps source file objects (java files of forms) to corresponding resource maps
    private static Map<FileObject, DesignResourceMap> resources;

    private ResourceUtils() {
    }

    /**
     * Creates a DesignResourceMap for given source file. Creates three
     * instances (in a chain) corresponding to the levels of resource maps as
     * defined by the app framework (class, package, whole app). Returns the
     * class-level instance of the chain. The created resource map is kept
     * registered for the source file (can be obtained via getDesignResourceMap
     * method later).
     * @return new instance of DesignResourceMap for given source file
     */
    static DesignResourceMap createDesignResourceMap(FileObject srcFile, ClassLoader classLoader) {
        ClassPath cp = ClassPath.getClassPath(srcFile, ClassPath.SOURCE);
        if (cp == null)
            return null;

        if (classLoader == null)
            classLoader = cp.getClassLoader(true);

        DesignResourceMap resMap = null;

        String appClassName = AppFrameworkSupport.getApplicationClassName(srcFile);
        String bundleName = getBundleName(appClassName);
        if (bundleName != null) {
            resMap = new DesignResourceMap(getAppDefaultResourceMap(srcFile, appClassName),
                                           classLoader, srcFile,
                                           new String[] { bundleName },
                                           DesignResourceMap.APP_LEVEL);
        }

        String srcClassName = cp.getResourceName(srcFile, '.', false);
        bundleName = getPackageBundleName(srcClassName);
        resMap = new DesignResourceMap(resMap, classLoader, srcFile,
                                       new String[] { bundleName },
                                       DesignResourceMap.PACKAGE_LEVEL);

        bundleName = getBundleName(srcClassName);
        resMap = new DesignResourceMap(resMap, classLoader, srcFile,
                                       new String[] { bundleName },
                                       DesignResourceMap.CLASS_LEVEL);

        if (resources == null) {
            resources = new HashMap<FileObject, DesignResourceMap>();
        }
        resources.put(srcFile, resMap);

        return resMap;
    }

    /**
     * Returns the DesignResourceMap to be used for given source file. Creates
     * and registers a new one if not created yet.
     * @return DesignResourceMap for given source file
     */
    static DesignResourceMap getDesignResourceMap(FileObject srcFile, boolean createNew) {
        DesignResourceMap resMap = (resources != null) ? resources.get(srcFile) : null;
        if (resMap == null && createNew) {
            resMap = createDesignResourceMap(srcFile, null);
        }
        return resMap;
    }

    private static ResourceMap getAppDefaultResourceMap(FileObject srcFile, String appClassName) {
        FileObject appFO = AppFrameworkSupport.getFileForClass(srcFile, appClassName);
        // TBD this is provisional code - we should go through all subclasses of
        // the current user's application
        appClassName = org.jdesktop.application.Application.class.getName();
        String[] bundleNames = new String[] { getBundleName(appClassName) };
        return new ResourceMap(null,
                ClassPath.getClassPath(srcFile, ClassPath.EXECUTE).getClassLoader(true),
                bundleNames);
    }

    static DesignResourceMap getAppDesignResourceMap(Project project) {
        String appClassName = AppFrameworkSupport.getApplicationClassName(project);
        if (appClassName != null) {
            String bundleName = getBundleName(appClassName);
            ClassPath cp = AppFrameworkSupport.getSourcePath(project);
            FileObject fo = cp.findResource(appClassName.replace('.', '/') + ".java"); // NOI18N
            return new DesignResourceMap(null, cp.getClassLoader(true), fo,
                    new String[] { bundleName }, DesignResourceMap.APP_LEVEL);
        }
        return null;
    }

    /**
     * Unregisters resource map for given source file. Called when the source
     * file is closed (@see ResourceServiceImpl.close).
     * @return DesignResourceMap used for the source file so far
     */
    static DesignResourceMap unregisterDesignResourceMap(FileObject srcFile) {
        return resources != null ? resources.remove(srcFile) : null;
    }

    /**
     * Returns source code for obtaining the ResourceMap for given file (class).
     * Supposing the code is only used within the given source file - so the
     * simple class name (not FQN) can be used; and not in a static block - so
     * getClass().getClassLoader() can be used.
     * @param srcFile the source file for which the ResourceMap should be obtained
     */
    static String getResourceMapCode(FileObject srcFile) {
        // The code for getting the resource map is quite long - worth "caching"
        // in a variable. Using special code mark to encode 3 data elements:
        // - the code to replace
        // - the type of variable to declare for the code
        // - suggested variable name
        String appCode = AppFrameworkSupport.getApplicationCode(srcFile);
        if (appCode != null) { // generate ResourceMap getter code for given class using Application
            // org.jdesktop.application.Application.getInstance(pkg.MyApp.class).getContext().getResourceMap(MyGUIClass.class)
            return CODE_MARK_VARIABLE_SUBST
                       + appCode + ".getContext().getResourceMap(" + srcFile.getName() + ".class)" // NOI18N
                   + CODE_MARK_VARIABLE_SUBST + org.jdesktop.application.ResourceMap.class.getName()
                   + CODE_MARK_VARIABLE_SUBST + "resourceMap"; // NOI18N
        } else { // no application - return code creating the ResourceMap directly
            // new org.jdesktop.application.ResourceMap(null, getClass().getClassLoader(), "pkg.resources.MyGUIClass")
            return CODE_MARK_LINE_COMMENT + "NOI18N" // NOI18N
                   + CODE_MARK_VARIABLE_SUBST
                       + "new org.jdesktop.application.ResourceMap(null, getClass().getClassLoader(), \"" // NOI18N
                       + getBundleName(AppFrameworkSupport.getClassNameForFile(srcFile)) +"\")" // NOI18N
                   + CODE_MARK_VARIABLE_SUBST + org.jdesktop.application.ResourceMap.class.getName()
                   + CODE_MARK_VARIABLE_SUBST + "resourceMap"; // NOI18N
        }
    }

    // special code marks recognized by form editor:
    static final String CODE_MARK_VARIABLE_SUBST = "*/\n\\2"; // NOI18N
    static final String CODE_MARK_LINE_COMMENT = "*/\n\\1"; // NOI18N
    static final String CODE_MARK_END = "*/\n\\0"; // NOI18N

    private static String getBundleName(String className) {
        if (className == null)
            return null;

        int i = className.lastIndexOf('.');
        return i > 0 ?
            className.substring(0, i) + ".resources" + className.substring(i) : // NOI18N
            "resources." + className; // NOI18N
    }

    private static String getPackageBundleName(String className) {
        int i = className.lastIndexOf('.');
        return i > 0 ?
            className.substring(0, i) + ".resources.PackageResources" : // NOI18N
            "resources.PackageResources"; // NOI18N
    }

    /**
     * @param bundleFileResName resource name of the properties file
     *        (using / path separator and including file extension),
     *        e.g. com/me/Bundle.properties
     */
    private static FileObject getResourceFile(FileObject srcFile, String bundleFileResName, boolean completeClassPath) {
        // try to find it in sources of the same project
        ClassPath scp = ClassPath.getClassPath(srcFile, ClassPath.SOURCE);
        if (scp != null) {
            FileObject resFO = scp.findResource(bundleFileResName);
            if (resFO != null)
                return resFO;
        }

        if (completeClassPath) { // try to find in sources of execution classpath
            ClassPath ecp = ClassPath.getClassPath(srcFile, ClassPath.EXECUTE);
            Iterator it = ecp.entries().iterator();
            while (it.hasNext()) {
                ClassPath.Entry e = (ClassPath.Entry) it.next();
                SourceForBinaryQuery.Result r = SourceForBinaryQuery.findSourceRoots(e.getURL());
                FileObject[] sourceRoots = r.getRoots();
                for (int i=0; i < sourceRoots.length; i++) {
                    // try to find the bundle under this source root
                    ClassPath cp = ClassPath.getClassPath(sourceRoots[i], ClassPath.SOURCE);
                    if (cp != null) {
                        FileObject resFO = cp.findResource(bundleFileResName);
                        if (resFO != null)
                            return resFO;
                    }
                }
            }
        }

        return null;
    }

    /**
     * @param bundleName class-like name of the bundle (using . as separator
     *        and not including the file extension), e.g. com.me.Bundle
     */
    static PropertiesDataObject getPropertiesDataObject(FileObject srcFile, String bundleName, boolean completeClassPath) {
        if (bundleName == null)
            return null;

        if (bundleName.startsWith("/")) // NOI18N
            bundleName = bundleName.substring(1);
        else if (bundleName.contains(".")) // NOI18N
            bundleName = bundleName.replace('.', '/');
        if (!bundleName.toLowerCase().endsWith(".properties")) // NOI18N
            bundleName = bundleName + ".properties"; // NOI18N
        FileObject bundleFile = getResourceFile(srcFile, bundleName, completeClassPath);
        if (bundleFile != null) {
            try {
                DataObject dobj = DataObject.find(bundleFile);
                if (dobj instanceof PropertiesDataObject)
                    return (PropertiesDataObject) dobj;
            }
            catch (DataObjectNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return null;
    }

    static PropertiesDataObject getPropertiesDataObject(FileObject srcFile) {
        ClassPath cp = ClassPath.getClassPath(srcFile, ClassPath.SOURCE);
        if (cp == null) {
            return null;
        }
        String srcClassName = cp.getResourceName(srcFile, '.', false);
        String bundleName = getBundleName(srcClassName);
        return getPropertiesDataObject(srcFile, bundleName, false);
    }

    /**
     * @param bundleName class-like name of the bundle (using . as separator
     *        and not including the file extension), e.g. com.me.Bundle
     */
    static BundleStructure getBundleStructure(FileObject srcFile, String bundleName) {
        PropertiesDataObject pdo = getPropertiesDataObject(srcFile, bundleName, false);
        return pdo != null ? pdo.getBundleStructure() : null;
    }

    static FileObject createResourcesFolder(FileObject srcFile) throws IOException {
        return FileUtil.createFolder(srcFile.getParent(), "resources"); // NOI18N
    }

    /**
     * @param bundleName class-like name of the bundle (using . as separator
     *        and not including the file extension), e.g. com.me.Bundle
     */
    static PropertiesDataObject createPropertiesDataObject(FileObject srcFile,
                                                           String bundleName)
        throws IOException
    {
        if (bundleName == null)
            return null;
        if (bundleName.toLowerCase().endsWith(".properties")) // NOI18N
            bundleName = bundleName.substring(0, bundleName.length()-".properties".length()); // NOI18N
        if (bundleName.contains(".")) // NOI18N
            bundleName = bundleName.replace('.', '/');
        FileObject folder = ClassPath.getClassPath(srcFile, ClassPath.SOURCE).getRoots()[0];
        
        return org.netbeans.modules.properties.Util.createPropertiesDataObject(folder, bundleName);
    }

    /**
     * Returns a string representation of a value to be stored in the properties
     * file. Used when an invalid string value is provided (usually come from
     * PropertyEditor.getAsText()). In some cases it is possible to determine
     * the string from the value object.
     * @return String representation of a value for ResourceMap
     */
    static String getValueAsString(Object value) {
        if (value instanceof String)
            return (String) value;

        if (value instanceof Color) {
            Color c = (Color) value;
            return "" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue(); // NOI18N
        }

        if (value instanceof Font) {
            Font f = (Font) value;
            String style = f.isBold() ?
                (f.isItalic() ? "BoldItalic" : "Bold") : // NOI18N
                (f.isItalic() ? "Italic" : "Plain"); // NOI18N
            return f.getName() + "-" + style + "-" + f.getSize(); // NOI18N
        }

        return null;
    }
}
