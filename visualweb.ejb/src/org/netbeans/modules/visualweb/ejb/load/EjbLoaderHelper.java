/*
 * EjbLoaderHelper.java
 *
 * Created on March 9, 2005, 10:10 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.util.JarExploder;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * @author cao
 */
public class EjbLoaderHelper {

    public EjbLoaderHelper() {
    }

    public static URLClassLoader getEjbGroupClassLoader(EjbGroup ejbGroup) {

        try {
            // The class path has the client jar files plus Java EE classes which contain the
            // classes/interfaces for EJBs

            ArrayList<URL> classPathUrls = new ArrayList<URL>();

            // Client jars
            List<String> jarFileNames = ejbGroup.getClientJarFiles();
            for (String fileName : jarFileNames) {
                URL url = new File(fileName).toURI().toURL();
                classPathUrls.add(url);
            }

            List<File> containerJars = getJavaEEClasspathEntries();
            for (File file : containerJars) {
                URL url = file.toURI().toURL();
                classPathUrls.add(url);
            }

            URL[] classPathArray = classPathUrls.toArray(new URL[classPathUrls.size()]);
            URLClassLoader classloader = URLClassLoader.newInstance(classPathArray);
            return classloader;
        }
        // try {
        // // Class loader for loading the classes in the client jar files
        // // and the ejb-2.0.jar (it contains the super classes/interfaces for the EJBs)
        //            
        // URL[] clientJarURLs = new URL[ ejbGroup.getClientJarFiles().size() + 1 ];
        //            
        // // Client jars
        // ArrayList jarFileNames = ejbGroup.getClientJarFiles();
        // for( int i = 0; i < jarFileNames.size(); i ++ ) {
        // String fileName = (String)jarFileNames.get( i );
        // clientJarURLs[i] = new File(fileName).toURL();
        //                
        // }
        //            
        // // ejb-2.0.jar
        // clientJarURLs[jarFileNames.size()] = new File(EjbLoader.ejb20Jar).toURL();
        //            
        // URLClassLoader classloader = URLClassLoader.newInstance( clientJarURLs );
        //            
        // return classloader;
        //            
        // }
        catch (java.net.MalformedURLException e) {
            // Log error
            String logMsg = "Error occurred when trying load classes from "
                    + ejbGroup.getClientJarFiles().toString();

            e.printStackTrace();

            return null;
        }
    }

    /**
     * Looks through all installed servers and looks for one that supports EJBs which typically is a
     * Java EE container. It then returns a list of classpath entries represented by File-s. A File
     * is typically a jar file but could also be a directory.
     * 
     * @return List of classpath entries
     */
    public static List<File> getJavaEEClasspathEntries() {
        for (String serverInstanceID : Deployment.getDefault().getServerInstanceIDs()) {
            String displayName = Deployment.getDefault().getServerInstanceDisplayName(
                    serverInstanceID);
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
            if (displayName != null && j2eePlatform != null
                    && j2eePlatform.getSupportedModuleTypes().contains(J2eeModule.EJB)) {
                File[] classpath = j2eePlatform.getClasspathEntries();
                return Arrays.asList(classpath);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns true iff an action should be enabled if that action depends on access to EJB API
     * classes. In NB6, these classes come from an installed app server that is separate from the
     * IDE itself.
     * 
     * @return
     */
    public static boolean isEnableAction() {
        return !getJavaEEClasspathEntries().isEmpty();
    }

    public static ArrayList getAllClazz(EjbGroup ejbGroup) {

        try {

            ArrayList allClazz = new ArrayList();

            for (Iterator<String> iter = ejbGroup.getClientJarFiles().iterator(); iter.hasNext();) {
                String jarFile = iter.next();
                allClazz.addAll(JarExploder.getAllClasses(jarFile));
            }

            // Remove the invalid ones
            // URLClassLoader classloader = getEjbGroupClassLoader( ejbGroup );
            for (Iterator iter = allClazz.iterator(); iter.hasNext();) {
                String className = (String) iter.next();

                // No internal classes for now
                if (className.indexOf('$') != -1) {
                    iter.remove();
                    continue;
                }

                /*
                 * try { Class.forName( className, false, classloader ); } catch(
                 * java.lang.ClassNotFoundException e ) { iter.remove(); }
                 */
            }

            return allClazz;
        } catch (Exception e) {
            e.printStackTrace();

            return new ArrayList();
        }
    }
}
