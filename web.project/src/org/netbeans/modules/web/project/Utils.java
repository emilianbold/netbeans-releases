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

package org.netbeans.modules.web.project;

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.*;

import org.netbeans.api.j2ee.core.Profile;
import org.openide.filesystems.FileUtil;

import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

public class Utils {

    private static final Logger UI_LOGGER = Logger.getLogger("org.netbeans.ui.web.project"); // NOI18N
    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.web.project"); // NOI18N

    // COPIED FROM TOMCAT
    private static final String JAVA_KEYWORDS[] = {
        "abstract", "assert", "boolean", "break", "byte", "case",
        "catch", "char", "class", "const", "continue",
        "default", "do", "double", "else", "enum", "extends",
        "final", "finally", "float", "for", "goto",
        "if", "implements", "import", "instanceof", "int",
        "interface", "long", "native", "new", "package",
        "private", "protected", "public", "return", "short",
        "static", "strictfp", "super", "switch", "synchronized",
        "this", "throws", "transient", "try", "void",
        "volatile", "while" };

    private static final String JSP_PACKAGE_NAME = "org.apache.jsp";
    
    private static final String PLATFORM_ANT_NAME = "platform.ant.name"; //NOI18N
    public static final String SPECIFICATION_J2SE = "j2se";              //NOI18N

    /** Create a valid default for context path from project name.
     */
    public static String createDefaultContext(String projectName) {
        return "/" + PropertyUtils.getUsablePropertyName(projectName);
    }
    
    /**
     * Updates property file at given location of ant based project
     * @param h helper of the project
     * @param path a relative URI in the project directory
     * @param ep new or updated properties
     */
    public static void updateProperties(AntProjectHelper h, String path, EditableProperties ep) {
        EditableProperties properties = h.getProperties(path);
        properties.putAll(ep);
        h.putProperties(path, properties);
    }

    /**
     * Recursively checks whether the file lies underneath or equals the folder
     * @param folder the root of folders hierarchy to search in 
     * @param file the file to search for
     * @return <code>true</code>, if <code>file</code> lies somewhere underneath or equals the <code>folder</code>,
     * <code>false</code> otherwise
     */
    public static boolean isParentOrEqual(File folder, File file) {
        if(folder != null || file != null) {
            folder = FileUtil.normalizeFile(folder);
            file = FileUtil.normalizeFile(file);
            while(file != null) {
                if(file.equals(folder)) {
                    return true;
                }
                file = file.getParentFile();
            }
        }
        return false;
    }

    /**
     * Searches Java platform according to platform name
     * Specification of the platform has to be J2SE
     * @param platformName
     * @return related JavaPlatform object if found, otherwise null
     */
    public static JavaPlatform findJ2seJavaPlatform(String platformName) {
        return findJavaPlatform(platformName, SPECIFICATION_J2SE);
    }

    /**
     * Searches Java platform according to platform name
     * The platform sepecification does not need to be J2SE
     * @param platformName
     * @return related JavaPlatform object if found, otherwise null
     */
    public static JavaPlatform findJavaPlatform(String platformName) {
        return findJavaPlatform(platformName, null);
    }
    
    /**
     * Get the default value of the <tt>debug.classpath</tt> property.
     * @return the default value of the <tt>debug.classpath</tt> property.
     */
    public static String getDefaultDebugClassPath() {
        return "${" + ProjectProperties.BUILD_CLASSES_DIR + "}:${" + ProjectProperties.JAVAC_CLASSPATH + "}"; // NOI18N
    }
    
    /**
     * Correct given classpath, that means remove obsolete properties, add missing ones etc.
     * If the given parameter is <code>null</code> or empty, the default debug classpath is returned.
     * @return corrected classpath, never <code>null</code>.
     * @see #getDefaultClassPath()
     */
    public static String correctDebugClassPath(String debugClassPath) {

        if (debugClassPath == null || debugClassPath.length() == 0) {
            // should not happen
            return Utils.getDefaultDebugClassPath();
        }
        
        // "invalid" strings
        final String buildEarWebDir = "${build.ear.web.dir}"; // NOI18N
        final String buildEarClassesDir = "${build.ear.classes.dir}"; // NOI18N
        final String buildEarPrefix = "${build.ear."; // NOI18N

        if (!debugClassPath.contains(buildEarPrefix)) {
            return debugClassPath;
        }

        StringBuilder buffer = new StringBuilder(debugClassPath.length());
        for (String token : PropertyUtils.tokenizePath(debugClassPath)) {
            // check NB 5.5.x obsolete properties
            if (!buildEarWebDir.equals(token)
                    && !buildEarClassesDir.equals(token)) {
                if (buffer.length() > 0) {
                    buffer.append(":"); // NOI18N
                }
                buffer.append(token);
            }
        }

        return buffer.toString();
    }

    private static JavaPlatform findJavaPlatform(String platformName, String specFilter) {
        if(platformName != null) {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
            for(int i = 0; i < platforms.length; i++) {
                JavaPlatform platform = platforms[i];
                String antName = (String)platform.getProperties().get(PLATFORM_ANT_NAME);
                if (antName != null && antName.equals(platformName)) {
                    if(specFilter == null || specFilter.equalsIgnoreCase(platform.getSpecification().getName()))
                    return platform;
                }
            }
        }
        return null;
    }

    // COPIED FROM TOMCAT
    /** Returns a slash-delimited resource path for the servlet generated from 
     * JSP, given a resource path of the original JSP. Uses code copied from Tomcat.
     * Note: does not handle tag files yet, only JSP files.
     */
    static String getGeneratedJavaResource(String jspUri) {
        int iSep = jspUri.lastIndexOf('/');
        String packageName = (iSep > 0) ? makeJavaPackage(jspUri.substring(0,iSep)) : ""; // NOI18N
        if (packageName.length() == 0) {
            packageName = JSP_PACKAGE_NAME;
        }
        else {
            packageName = JSP_PACKAGE_NAME + "." + packageName; // NOI18N
        }
        String className = makeJavaIdentifier(jspUri.substring(iSep + 1));
        return packageName.replace('.', '/') + "/" + className + ".java"; // NOI18N
    }
    
    // COPIED FROM TOMCAT
    /**
     * Converts the given path to a Java package or fully-qualified class name
     *
     * @param path Path to convert
     *
     * @return Java package corresponding to the given path
     */
    private static final String makeJavaPackage(String path) {
        String classNameComponents[] = split(path,"/");
        StringBuffer legalClassNames = new StringBuffer();
        for (int i = 0; i < classNameComponents.length; i++) {
            legalClassNames.append(makeJavaIdentifier(classNameComponents[i]));
            if (i < classNameComponents.length - 1) {
                legalClassNames.append('.');
            }
        }
        return legalClassNames.toString();
    }
    
    // COPIED FROM TOMCAT
    /**
     * Splits a string into it's components.
     * @param path String to split
     * @param pat Pattern to split at
     * @return the components of the path
     */
    private static final String [] split(String path, String pat) {
        Vector<String> comps = new Vector<String>();
        int pos = path.indexOf(pat);
        int start = 0;
        while( pos >= 0 ) {
            if(pos > start ) {
                String comp = path.substring(start,pos);
                comps.add(comp);
            }
            start = pos + pat.length();
            pos = path.indexOf(pat,start);
        }
        if( start < path.length()) {
            comps.add(path.substring(start));
        }
        String [] result = new String[comps.size()];
        for(int i=0; i < comps.size(); i++) {
            result[i] = comps.elementAt(i);
        }
        return result;
    }
            
    // COPIED FROM TOMCAT
    /**
     * Converts the given identifier to a legal Java identifier
     *
     * @param identifier Identifier to convert
     *
     * @return Legal Java identifier corresponding to the given identifier
     */
    private static final String makeJavaIdentifier(String identifier) {
        StringBuffer modifiedIdentifier = 
            new StringBuffer(identifier.length());
        if (!Character.isJavaIdentifierStart(identifier.charAt(0))) {
            modifiedIdentifier.append('_');
        }
        for (int i = 0; i < identifier.length(); i++) {
            char ch = identifier.charAt(i);
            if (Character.isJavaIdentifierPart(ch) && ch != '_') {
                modifiedIdentifier.append(ch);
            } else if (ch == '.') {
                modifiedIdentifier.append('_');
            } else {
                modifiedIdentifier.append(mangleChar(ch));
            }
        }
        if (isJavaKeyword(modifiedIdentifier.toString())) {
            modifiedIdentifier.append('_');
        }
        return modifiedIdentifier.toString();
    }
    
    // COPIED FROM TOMCAT
    /**
     * Mangle the specified character to create a legal Java class name.
     */
    private static final String mangleChar(char ch) {
        char[] result = new char[5];
        result[0] = '_';
        result[1] = Character.forDigit((ch >> 12) & 0xf, 16);
        result[2] = Character.forDigit((ch >> 8) & 0xf, 16);
        result[3] = Character.forDigit((ch >> 4) & 0xf, 16);
        result[4] = Character.forDigit(ch & 0xf, 16);
        return new String(result);
    }

    // COPIED FROM TOMCAT
    /**
     * Test whether the argument is a Java keyword
     */
    private static boolean isJavaKeyword(String key) {
        return Arrays.binarySearch(JAVA_KEYWORDS, key) >= 0;
    }

    public static Color getErrorColor() {
        // inspired by org.openide.WizardDescriptor
        Color c = UIManager.getColor("nb.errorForeground"); //NOI18N
        return c == null ? new Color(89,79,191) : c;
    }
    
    public static String toClasspathString(File[] classpathEntries) {
        if (classpathEntries == null) {
            return "";
        }
        StringBuffer classpath = new StringBuffer();
        for (int i = 0; i < classpathEntries.length; i++) {
            classpath.append(classpathEntries[i].getAbsolutePath());
            if (i + 1 < classpathEntries.length) {
                classpath.append(':'); // NOI18N
            }
        }
        return classpath.toString();
    }

    /**
     * Logs the UI gesture.
     *
     * @param bundle resource bundle to use for message
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUI(ResourceBundle bundle,String message, Object[] params) {
        Parameters.notNull("message", message);
        Parameters.notNull("bundle", bundle);

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(UI_LOGGER.getName());
        logRecord.setResourceBundle(bundle);
        if (params != null) {
            logRecord.setParameters(params);
        }
        UI_LOGGER.log(logRecord);
    }

    /**
     * Logs usage data.
     *
     * @param srcClass source class
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUsage(Class srcClass, String message, Object[] params) {
        Parameters.notNull("message", message); // NOI18N

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER.log(logRecord);
    }
    
    public static String getServletName(FileObject docBase, FileObject jsp) {
        String jspRelativePath = FileUtil.getRelativePath(docBase, jsp);
        return getServletResourcePath(null, jspRelativePath);
    }
    
    public static String getServletResourcePath(String moduleContextPath, String jspResourcePath) {
        return getServletPackageName(jspResourcePath).replace('.', '/') + '/' +
            getServletClassName(jspResourcePath) + ".java";
    }

    private static String getServletPackageName(String jspUri) {
        String dPackageName = getDerivedPackageName(jspUri);
        if (dPackageName.length() == 0) {
            return JSP_PACKAGE_NAME;
        }
        return JSP_PACKAGE_NAME + '.' + getDerivedPackageName(jspUri);
    }
    
    private static String getDerivedPackageName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/');
        return (iSep > 0) ? makeJavaPackage(jspUri.substring(0,iSep)) : "";
    }
    
    private static String getServletClassName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/') + 1;
        return makeJavaIdentifier(jspUri.substring(iSep));
    }
    
    /**
     * Creates an URL of a classpath or sourcepath root
     * For the existing directory it returns the URL obtained from {@link File#toUri()}
     * For archive file it returns an URL of the root of the archive file
     * For non existing directory it fixes the ending '/'
     * @param root the file of a root
     * @param offset a path relative to the root file or null (eg. src/ for jar:file:///lib.jar!/src/)" 
     * @return an URL of the root
     * @throws MalformedURLException if the URL cannot be created
     */
    public static URL getRootURL (File root, String offset) throws MalformedURLException {
        URL url = root.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        } else if (!root.exists()) {
            url = new URL(url.toExternalForm() + "/"); // NOI18N
        }
        if (offset != null) {
            assert offset.endsWith("/");    //NOI18N
            url = new URL(url.toExternalForm() + offset); // NOI18N
        }
        return url;
    }
    
    public static LibraryChooser.Filter getFilter(WebProject p) {
        LibraryChooser.Filter filter = null;
        WebModule wm = WebModule.getWebModule(p.getProjectDirectory());
        if (wm != null && Profile.J2EE_13.equals(wm.getJ2eeProfile())) { // NOI18N
            filter = new LibraryChooser.Filter() {
                public boolean accept(Library library) {
                    if ("javascript".equals(library.getType())) { //NOI18N
                        return false;
                    }
                    try {
                        library.getContent("classpath"); //NOI18N
                    } catch (IllegalArgumentException ex) {
                        return false;
                    }
                    return !library.getName().matches("jstl11|jaxrpc16|toplink|Spring|jaxws20|jaxb20|struts|jsf"); // NOI18N
                }
            };
        }
        return filter;
    }

    
}
