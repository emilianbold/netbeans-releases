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

/**
 * Module installer class that persist the
 * @author Winston Prakash
 */
package org.netbeans.modules.websvc.registry.netbeans;

import java.io.*;

import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.modules.InstalledFileLocator;

import org.netbeans.modules.j2ee.platform.api.PlatformProvider;


/** class WebServiceModuleInstaller
 *
 *  ModuleInstall for the web service registry module.  Handles reading
 *  the registry on module startup and saving any changes on module shutdown.
 */
public class WebServiceModuleInstaller extends ModuleInstall {
	
	private static ExtensionClassLoader specialLoader = null;
	private static boolean registryInstalled = false;
	
	private PersistenceManagerInterface pmi = null;
	
	public void restored() {
		restoreds();
		
		if(registryInstalled) {
			try {
				PersistenceManagerInterface persistenceManager = (PersistenceManagerInterface) 
					specialLoader.loadClass("org.netbeans.modules.websvc.registry.WebServicePersistenceManager").newInstance(); //NOI18N
				persistenceManager.load(specialLoader);
			} catch(Exception ex) {
				ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
			}
		}
	}

	public void close() {
		if(registryInstalled) {
			try {
				PersistenceManagerInterface persistenceManager =(PersistenceManagerInterface)
					specialLoader.loadClass("org.netbeans.modules.websvc.registry.WebServicePersistenceManager").newInstance(); //NOI18N
				persistenceManager.save(specialLoader);
			} catch(Exception ex) {
				ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
			} finally {
			}
		}
	}
	
	public void uninstalled() {
		close();
	}

	public static void restoreds() {
		if(specialLoader == null) {
			try {
				specialLoader = new ExtensionClassLoader(new Empty().getClass().getClassLoader());
				updatesSecialLoader(specialLoader);
			} catch(Exception ex) {
				ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
			}
		}
	}

	public static ClassLoader getExtensionClassLoader() {
		return specialLoader;
	}

	public static void updatesSecialLoader(ExtensionClassLoader loader) throws Exception {
		try {
			PlatformProvider pm = PlatformProvider.getDefault();
			File f1 = pm.getLocation();
			if(f1 != null && f1.exists()) {
				String installRoot = f1.getAbsolutePath();
//				if(installRoot == null) {
//					// !PW What will this do on UNIX?
//					File temp = new File("c:\\sun\\appserver\\lib\\appserv-admin.jar"); // NOI18N
//					// need also to check on Unix system the defautl location
//					if(temp.exists()) {
//						installRoot = "c:\\sun\\appserver"; // NOI18N
//						System.setProperty("com.sun.aas.installRoot", installRoot); // NOI18N
//					} else {
//						// !PW due to null check on f1, I don't think this entire block
//						// is even necessary or useful anymore, but just in case,
//						// add a return here so we don't get a NPE later.
//						return;
//					}
//				}
				
				InstalledFileLocator locator = InstalledFileLocator.getDefault();

				File f = locator.locate("modules/ext/websvcregistry.jar", null, true); // NOI18N
				if(f != null) {
					registryInstalled = true;
					loader.addURL(f);
				} else {
					ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot locate file modules/ext/websvcregistry.jar");
				}

				// Add correct jars from the installed application server.
				SJSASVersion appServerVersion = SJSASVersion.getSJSAppServerVersion();
				String [] registryRuntimeJars = appServerVersion.getRegistryRuntimeLibraries();
				
				for(int i = 0; i < registryRuntimeJars.length; i++) {
					loader.addURL(new File(installRoot + registryRuntimeJars[i]));
				}
			}
		} catch(Exception ex) {
			throw new Exception(ex.getLocalizedMessage(), ex);
		}
	}
	
	/*
	 * Used to get the netbeans classloader of this class.
	 *
	 */
	static class Empty {
	}
}
