/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.lang.reflect.Method;
import java.util.Collection;

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
        
        java.util.ListIterator it = list.listIterator();
        while (it.hasNext()) {
            File f = (File)it.next();
            it.set(new java.util.jar.JarFile (f));
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
        
        
        // XXX separate openide.jar and core*.jar into different classloaders
        ClassLoader loader = new JarClassLoader (list, new ClassLoader[] {
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
        // --> IMPORTANT! <--
        // Please keep this logic in synch with impl of NbInstaller.getEffectiveClasspath.
        // Otherwise the "effective classpath" will be displayed inaccurately.
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
