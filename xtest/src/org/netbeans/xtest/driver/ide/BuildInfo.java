/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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


/** Class for getting information about version and build number
 * from given IDE build. Examples of use:
 * @author breh
 */
public class BuildInfo {

	private URLClassLoader coreClassLoader;
	private Package pkg;
	private Method getBundleMethod;
        private Method setBrandingMethod;
        private String branding = null;
	
        
        
        /** Creates new object
         * @param ideHomeDirName ide home dir file object (usually known as netbeans.home or ide.home)
         * @throws IOException in the case there is any problem with accessing required
         * directories in ide.home dir
         */        
	public BuildInfo(String ideHomeDirName) throws IOException {
                this(new File(ideHomeDirName));
	}
	
        /** Creates new object
         * @param ideHomeDir ide home dir file object (usually known as netbeans.home or ide.home)
         * @throws IOException in the case there is any problem with accessing required
         * directories in ide.home dir
         */        
	public BuildInfo(File ideHomeDir) throws IOException {
		if (ideHomeDir == null) {
			throw new NullPointerException("argument cannot be null");
		}
		if (!ideHomeDir.isDirectory()) {
			throw new IOException("Supplied file is not a directory");
		}
		File ideLibDir = new File(ideHomeDir,"lib");
		if (!ideLibDir.isDirectory()) {
			throw new IOException("Supplied directory is not IDE home dir, because it does not contain lib/ subdirectory");
		}
		// now create a classloader 
		coreClassLoader = BuildInfo.createClassLoaderForCore(ideLibDir);
		// load NbBundle stuff
		try {
			Class nbBundleClass = coreClassLoader.loadClass("org.openide.util.NbBundle");
			getBundleMethod = nbBundleClass.getMethod("getBundle",new Class[] {String.class, Locale.class, ClassLoader.class});
                        setBrandingMethod = nbBundleClass.getMethod("setBranding",new Class[] {String.class});		
			pkg = nbBundleClass.getPackage();
                        branding = getBranding(ideLibDir);
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
                if (branding != null) {
                    // only use this if there is any branding set for this ide
                    setBrandingMethod.invoke(null, new Object[] {branding});
                }
                ResourceBundle rb = (ResourceBundle) getBundleMethod.invoke(null,
                new Object[] {"org.netbeans.core.Bundle",Locale.getDefault(),coreClassLoader});
                //ResourceBundle rb = NbBundle.getBundle("org.netbeans.core.Bundle",Locale.getDefault(),coreClassLoader);
                productVersion =  MessageFormat.format(rb.getString("currentVersion"), new Object[] {getBuildNumber()});;
                
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
		String buildNumber = pkg.getImplementationVersion ();
		return (buildNumber == null) ? "Unknown": buildNumber ;
	}
	
	
	// private stuff
	
        // get branding of this ide (if applicable)
        private static String getBranding(File libDir) throws IOException {
            File brandingFile = new File(libDir,"branding");
            if (!brandingFile.isFile()) {
                // branding is not available - 
                return null;
            }
            // read the branding information            
            FileReader fr = new FileReader(brandingFile);
            try {                
                BufferedReader in =new BufferedReader(fr);
                return in.readLine();
            } finally {
                if (fr!= null) {
                    fr.close();
                }
            }
            
        }
        
	// create URLClassLoader for all core jars (including brandings)
	private static URLClassLoader createClassLoaderForCore(File libDir) throws MalformedURLException {
		Collection urls = new ArrayList();

		// get patches jars
		File[] patches = (new File(libDir,"patches")).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(".jar")) {
					return true;
				} else {
					return false;
				}
			}
		});
                if(patches != null) {
                    for (int i=0; i < patches.length; i++) {
                            urls.add(BuildInfo.getURL(patches[i]));
                    }
                }

		// get locale jars (just the ones starting with core)
		File[] locales = (new File(libDir,"locale")).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.startsWith("core")&name.endsWith(".jar")) {
					return true;
				} else {
					return false;
				}
			}
		});
                
                if (locales != null) {
                    for (int i=0; i < locales.length; i++) {
        			urls.add(BuildInfo.getURL(locales[i]));
        		}
                }
		
		// get openide jar
		urls.add(BuildInfo.getURL(new File(libDir,"openide.jar")));
				
		// get core jar
		urls.add(BuildInfo.getURL(new File(libDir,"core.jar")));

		// get the urls as an array
		// now create the classloader;
		return new URLClassLoader((URL[])(urls.toArray(new URL[0])));
	}
	
	private static URL getURL(File file) throws MalformedURLException {
		return new URL("file",null,file.getAbsolutePath());
	}

}
