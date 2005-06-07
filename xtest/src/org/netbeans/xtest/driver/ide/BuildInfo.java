/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

 
package org.netbeans.xtest.driver.ide;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.jar.Manifest;


/** Class for getting information about version and build number
 * from given IDE build. Examples of use:
 *
 * new BuildInfo("/tmp/netbeans/platform5").getBuildNumber()
 *
 * @author breh
 */
public class BuildInfo {

	private URLClassLoader coreClassLoader;
	private Method getBundleMethod;
      
        /** Creates new object
         * @param idePlatformDirPath path to ide lib directory with openide.jar, core.jar and other libraries
         * @throws IOException in the case there is any problem with accessing required
         * directories in given path
         */        
	public BuildInfo(String idePlatformDirPath) throws IOException {
                this(new File(idePlatformDirPath));
	}
	
        /** Creates new object
         * @param idePlatformDir ide platform dir file object point to directory with 
         * lib/org-openide-util.jar, core/core.jar and other libraries
         * @throws IOException in the case there is any problem with accessing required
         * directories in given directory
         */        
	public BuildInfo(File idePlatformDir) throws IOException {
		if (idePlatformDir == null) {
			throw new NullPointerException("argument cannot be null");
		}
		if (!idePlatformDir.isDirectory()) {
			throw new IOException("Supplied file is not a directory");
		}
		if (!new File(idePlatformDir,"lib/org-openide-util.jar").exists()) {
			throw new IOException("Supplied directory is not IDE platform dir, because it does not contain lib/org-openide-util.jar.");
		}
		// now create a classloader 
		coreClassLoader = BuildInfo.createClassLoaderForCore(idePlatformDir);
		// load NbBundle stuff
		try {
			Class nbBundleClass = coreClassLoader.loadClass("org.openide.util.NbBundle");
			getBundleMethod = nbBundleClass.getMethod("getBundle",new Class[] {String.class, Locale.class, ClassLoader.class});
		} catch (ClassNotFoundException cnfe) {
			throw new IOException("Cannot find org.openide.util.NbBundle class in this IDE");
		} catch (NoSuchMethodException nsme) {
			throw new IOException("Cannot find compatible org.openide.util.NbBundle class in this IDE");
		}
	}
	
        /** get product version string from ide
         * @return the version string
         */        
        public String getProductVersion() {
            String productVersion = "Unknown";
            try {                
                ResourceBundle rb = (ResourceBundle) getBundleMethod.invoke(
                                null,
                                new Object[] {"org.netbeans.core.startup.Bundle", // NOI18N
                                              Locale.getDefault(), 
                                              coreClassLoader}
                );
                productVersion =  MessageFormat.format(rb.getString("currentVersion"), //NOI18N
                                                       new Object[] {getBuildNumber()});
                
            } catch (Exception e) {
                // something bad happened - ingore it for now
            }
            return productVersion;
        }
	
        /** get product name (version without build number)
         * @return product number
         */        
	public String getProductName() {
		String productVersion = getProductVersion();
                int index = productVersion.lastIndexOf('(');
                if (index > 0) {
                    productVersion = productVersion.substring(0,index).trim();
                }
		return productVersion;
	}
	
        /** get build number of this ide
         * @return build number
         */        
	public String getBuildNumber() {
            String buildNumber = null;
            try {
                URL u = coreClassLoader.findResource ("META-INF/MANIFEST.MF"); // NOI18N
                InputStream is = u.openStream();
                Manifest mf = new Manifest(is);
                is.close();

                buildNumber = mf.getMainAttributes().getValue("OpenIDE-Module-Implementation-Version"); // NOI18N
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return (buildNumber == null) ? "Unknown": buildNumber ; // NOI18N
	}
        
	// create URLClassLoader for all core jars
	private static URLClassLoader createClassLoaderForCore(File libDir) throws MalformedURLException {
		Collection urls = new ArrayList();

		// get patches jars
		File[] patches = (new File(libDir,"core/patches")).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
                if(patches != null) {
                    for (int i=0; i < patches.length; i++) {
                            urls.add(patches[i].toURI().toURL());
                    }
                }
                patches = (new File(libDir,"lib/patches")).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
                if(patches != null) {
                    for (int i=0; i < patches.length; i++) {
                            urls.add(patches[i].toURI().toURL());
                    }
                }

		// get locale jars (just the ones starting with core)
		File[] locales = (new File(libDir,"core/locale")).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return  name.startsWith("core") & name.endsWith(".jar");
			}
		});
                
                if (locales != null) {
                    for (int i=0; i < locales.length; i++) {
        			urls.add(locales[i].toURI().toURL());
        		}
                }
		
		// get openide jar
                urls.add(new File(libDir,"lib/org-openide-util.jar").toURI().toURL());
				
		// get core jar
		urls.add(new File(libDir,"core/core.jar").toURI().toURL());

		// get the urls as an array
		// now create the classloader;
		return new URLClassLoader((URL[])(urls.toArray(new URL[0])));
	}
}
