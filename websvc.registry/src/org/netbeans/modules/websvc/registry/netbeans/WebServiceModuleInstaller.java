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
import org.openide.modules.ModuleInstall;
import org.openide.modules.InstalledFileLocator;
import org.netbeans.modules.j2ee.platform.api.PlatformProvider;


// This is here just to persist the snippets

public class WebServiceModuleInstaller extends ModuleInstall {
    PersistenceManagerInterface pmi;
    static private ExtensionClassLoader specialLoader=null;
    public void restored() {
        restoreds();
        try{
            PersistenceManagerInterface persistenceManager =(PersistenceManagerInterface)specialLoader.loadClass("org.netbeans.modules.websvc.registry.WebServicePersistenceManager").newInstance();//NOI18N
            persistenceManager.load(specialLoader);
        }
        catch (Exception e){
          //  System.out.println("-----null restored because lacking app server classes");
          //  e.printStackTrace();
            
        }
    }
    
    public void close() {
        //Debug.print(this,"close"," called");
        try{
            PersistenceManagerInterface persistenceManager =(PersistenceManagerInterface)specialLoader.loadClass("org.netbeans.modules.websvc.registry.WebServicePersistenceManager").newInstance();//NOI18N
            persistenceManager.save(specialLoader);
        }
        catch (Exception e){
          //  System.out.println("-----null close because lacking app server classes");
            
        }
        finally{
            
        }
        
    }
    static public  ClassLoader getExtensionClassLoader(){
        
        return specialLoader;
    }
    
    public void uninstalled() {
        close();
    }
    
    public static void restoreds() {
        if(specialLoader==null){
            
            try {
                specialLoader = new ExtensionClassLoader( new Empty().getClass().getClassLoader());
                updatesSecialLoader(specialLoader);
            }
            catch (Exception ex2) {
                org.openide.ErrorManager.getDefault().notify(ex2);
                System.out.println(ex2);
            }
        }
    }
    
    public static void updatesSecialLoader(ExtensionClassLoader loader) throws Exception{
        try {
            PlatformProvider pm = PlatformProvider.getDefault();
            File f1 = pm.getLocation();
			if(f1 != null && f1.exists()) {
				String installRoot = f1.getAbsolutePath();
				if (installRoot==null){
					// !PW What will this do on UNIX?
					File temp = new File("c:\\sun\\appserver\\lib\\appserv-admin.jar");
					// need also to check on Unix system the defautl location
					if (temp.exists()){
						installRoot = "c:\\sun\\appserver";
						System.setProperty("com.sun.aas.installRoot", installRoot);
					} else {
						// !PW due to null check on f1, I don't think this entire block
						// is even necessary or useful anymore, but just in case,
						// add a return here so we don't get a NPE later.
						return;
					}
				}
				File f ;
				InstalledFileLocator fff= InstalledFileLocator.getDefault();

				f = fff.locate("modules/ext/websvcregistry.jar", null, true);
				if (f!=null)
					loader.addURL(f);
				else
					System.out.println("cannot locate file modules/ext/websvcregistry.jar");

				f = new File(installRoot+"/lib/jaxrpc-api.jar");
				loader.addURL(f);
				f = new File(installRoot+"/lib/jaxrpc-spi.jar");
				loader.addURL(f);
				f = new File(installRoot+"/lib/j2ee.jar");
				loader.addURL(f);
				f = new File(installRoot+"/lib/jaxrpc-impl.jar");
				loader.addURL(f);
				f = new File(installRoot+"/lib/endorsed/xercesImpl.jar");
				loader.addURL(f);
				f = new File(installRoot+"/lib/endorsed/dom.jar");
				loader.addURL(f);
				f = new File(installRoot+"/lib/endorsed/xalan.jar");
				loader.addURL(f);
			}
        }
        catch (Exception ex2) {
            throw new Exception(ex2.getLocalizedMessage(), ex2);
        }
    }
        /*
         *used to get the netbeans classload of this class.
         *
         **/
    static class Empty{
        
    }
}
