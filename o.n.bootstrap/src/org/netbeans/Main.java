/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;

import java.io.File;
import java.util.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;
import java.security.*;

/** Bootstrap main class.
 * @author Jaroslav Tulach, Jesse Glick
 */
public class Main extends Object {
    /** Starts the IDE.
    * @param args the command line arguments
    */
    public static void main (String args[]) throws Exception {
        ArrayList list = new ArrayList ();

        String home = System.getProperty ("netbeans.home"); // NOI18N
        if (home != null) {
            build_cp (new File (home), list);
        }
        
        //
        // prepend classpath
        //
        String prepend = System.getProperty("netbeans.classpath"); // NOI18N
        if (prepend != null) {
            StringTokenizer tok = new StringTokenizer (prepend, File.pathSeparator);
            while (tok.hasMoreElements()) {
                list.add (0, new File (tok.nextToken()));
            }
        }

        // Compute effective dynamic classpath (mostly lib/*.jar) for TopLogging, NbInstaller:
        StringBuffer buf = new StringBuffer(1000);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            if (buf.length() > 0) {
                buf.append(File.pathSeparatorChar);
            }
            buf.append(((File)it.next()).getAbsolutePath());
        }
        System.setProperty("netbeans.dynamic.classpath", buf.toString());
        
        // JarClassLoader treats a File as a dir; for a ZIP/JAR, needs JarFile
        ListIterator it2 = list.listIterator();
        while (it2.hasNext()) {
            File f = (File)it2.next();
            if (f.isFile()) {
                it2.set(new JarFile (f, false));
            }
        }
        
        // XXX separate openide.jar and core*.jar into different classloaders
        ClassLoader loader = new BootClassLoader(list, new ClassLoader[] {
            Main.class.getClassLoader()
        });
        
        String className = System.getProperty(
            "netbeans.mainclass", "org.netbeans.core.Main" // NOI18N
        );
        
	Class c = loader.loadClass(className);
        Method m = c.getMethod ("main", new Class[] { String[].class }); // NOI18N
        
        // Needed e.g. for JAXP to find NbSAXParserFactoryImpl.
        // Also for Lookup.getDefault to find NbTopManager.Lkp.
        // Note that ModuleManager.updateContextClassLoaders will later change
        // the loader on this and other threads to be MM.SystemClassLoader anyway.
        Thread.currentThread().setContextClassLoader (loader);

        m.invoke (null, new Object[] { args });
    }
    
    private static final class BootClassLoader extends JarClassLoader {
        public BootClassLoader(List cp, ClassLoader[] parents) {
            super(cp, parents);
        }
        protected URL fileToURL(File f) throws MalformedURLException {
            // #27330: installation in a dir containing hash marks etc.
            if (convertor != null) {
                return convertor.toURL(f);
            } else {
                // This will be called early and used for setting permissions.
                // If there are hashes in the install path, this must be made
                // to work, or everything will get security exceptions.
                // So special-case that part and hope for the best.
                try {
                    Method m = File.class.getMethod("toURI", null); // NOI18N
                    Object o = m.invoke(f, null);
                    m = o.getClass().getMethod("toURL", null); // NOI18N
                    return (URL)m.invoke(o, null);
                } catch (Throwable t) {
                    // No such luck, JDK 1.3.
                }
                // Copied from Utilities.FileURLConvertor13.toURL.
                URL u = f.toURL();
                String u2 = u.toExternalForm();
                if (u2.indexOf('#') != -1) {
                    int i;
                    while ((i = u2.indexOf('#')) != -1) {
                        u2 = u2.substring(0, i) + "%23" + u2.substring(i + 1); // NOI18N
                    }
                    u = new URL(u2);
                }
                return u;
            }
        }
        /** Startup optimalization. See issue 27226. */
        protected PermissionCollection getPermissions(CodeSource cs) {
            return getAllPermission();
        }
        /** Startup optimalization. See issue 27226. */
        private static PermissionCollection modulePermissions;
        /** Startup optimalization. See issue 27226. */
        private static synchronized PermissionCollection getAllPermission() {
            if (modulePermissions == null) {
                modulePermissions = new Permissions();
                modulePermissions.add(new AllPermission());
                modulePermissions.setReadOnly();
            }
            return modulePermissions;
        }
    }
    
    /**
     * File to URL convertor.
     * @see #27330
     */
    public interface URLConvertor {
        URL toURL(File f) throws MalformedURLException;
    }
    private static URLConvertor convertor = null;
    /**
     * Someone please call this!
     * @see org.openide.util.Utilities#toURL
     */
    public static void setURLConvertor(URLConvertor c) {
        convertor = c;
    }
    
    
    private static void append_jars_to_cp (File dir, Collection toAdd) {
        if (!dir.isDirectory()) return;
        
        File[] arr = dir.listFiles();
        for (int i = 0; i < arr.length; i++) {
            String n = arr[i].getName ();
            if (n.equals("updater.jar") || // NOI18N
                (dir.getName().equals("locale") && n.startsWith("updater_") && n.endsWith(".jar"))) { // NOI18N
                // Used by launcher, not by us.
                continue;
            }
            if (n.endsWith("jar") || n.endsWith ("zip")) { // NOI18N
                toAdd.add (arr[i]);
            }
        }
    }
        
    
    private static void build_cp(File base, Collection toAdd) {
        append_jars_to_cp (new File (base, "lib/patches"), toAdd);
        append_jars_to_cp (new File (base, "lib"), toAdd);
        // XXX a minor optimization: exclude any unused locale JARs
        // For example, lib/locale/ might contain:
        // core_ja.jar
        // core_f4j.jar
        // core_f4j_ja.jar
        // core_f4j_ce.jar
        // core_f4j_ce_ja.jar
        // core_ru.jar
        // core_fr.jar
        // [etc.]
        // Only some of these will apply to the current session, based on the
        // current values of Locale.default and NbBundle.branding.
        append_jars_to_cp (new File (base, "lib/locale"), toAdd);
    }
}
