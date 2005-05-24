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
/*
 * Installer.java -- synopsis
 *
 */

package org.netbeans.modules.j2ee.sun.ide;


import java.io.File;
import java.net.Authenticator;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;

import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.sun.ide.editors.AdminAuthenticator;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;

public class Installer extends org.openide.modules.ModuleInstall {
    
    private static ExtendedClassLoader pluginLoader =null;
    static FacadeDeploymentFactory facadeDF=null;
    

    
    public static void updatePluginLoader(ExtendedClassLoader loader) throws Exception{
        try {
            
            String installRoot = PluginProperties.getDefault().getInstallRoot().getAbsolutePath(); 
            File f ;
	    InstalledFileLocator fff= InstalledFileLocator.getDefault();

         /*   f = fff.locate("modules/ext/appserv-jsr88.jar", null, true);
            if (f!=null){
		loader.addURL(f);
                loadLocaleSpecificJars(f, loader);
            }
	    else
		System.out.println("cannot locate file modules/ext/appserv-jsr88.jar");
*/
            
            
            f = new File("C:\\acvs\\nb_all\\serverplugins\\sun\\appsrvbridge\\dist\\appsrvbridge.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/appserv-admin.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/appserv-ext.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/appserv-rt.jar");
            loader.addURL(f);
        /*    f = new File(installRoot+"/lib/appserv-cmp.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/appserv-assemblytool.jar");
            loader.addURL(f);*/
            f = new File(installRoot+"/lib/commons-logging.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/admin-cli.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/common-laucher.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/j2ee.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/install/applications/jmsra/imqjmsra.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/admin-cli.jar");
            loader.addURL(f);
            //for AS 8.1: no more endorsed dir!!!
            f = new File(installRoot+"/lib/xercesImpl.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/dom.jar");
            loader.addURL(f);
            f = new File(installRoot+"/lib/xalan.jar");
            loader.addURL(f);        }
        catch (Exception ex2) {
            throw new Exception(ex2.getLocalizedMessage());
        }
    }
    /** Factory method to create DeploymentFactory for s1as.
     */
    public static synchronized Object create() {                
        if (facadeDF != null)
            return facadeDF;
        
        try{
            getPluginLoader();//make sure it's initialized

            //register the panel that will ask username password. Global IDE level...
            Authenticator.setDefault(new AdminAuthenticator());          
            java.net.URL[] myURLs = getPluginLoader().getURLs();
            facadeDF = new FacadeDeploymentFactory();

            return facadeDF;
        }
        catch (Throwable ee){
            System.out.println("-----null deploy factory because lacking app server classes");
        }
        return null;
    }
    
    public static DeploymentFactory getDeploymentFactory() {
        return (DeploymentFactory)create();
    }
    
    public static ExtendedClassLoader getClassLoader(){
        ExtendedClassLoader loader = null;
        try{
            loader = new ExtendedClassLoader();
            updatePluginLoader(loader);
        }catch(Exception ex){
            //Supress Exception. Null value is expected on failure
        }  
        return loader;
    }
    public static void  resetClassLoader(){
        pluginLoader =null;
        try{
            pluginLoader =getPluginLoader();//get a new one.
            facadeDF.init();
            // reset the deployment managers
            String [] instanceURLs = InstanceProperties.getInstanceList();
            for (int i=0;i<instanceURLs.length;i++){
                InstanceProperties ip =  InstanceProperties.getInstanceProperties(instanceURLs[i]);
                if (ip.getDeploymentManager() instanceof SunDeploymentManagerInterface){
                    ip.refreshServerInstance();
                    
                }
        }
        }catch(Exception ex){
            //Supress Exception. Null value is expected on failure
        }  

    }    
    public void close() {
        
    }
    public void uninstalled() {
        
    }

    
    /*
     *used to get the netbeans classload of this class.
     *
     **/
    static class Empty{
    
    }
    static public class FacadeDeploymentFactory implements DeploymentFactory{
        private DeploymentFactory innerDF = null;
        public FacadeDeploymentFactory(){
            init();
            
        }
        public void init(){
            try{
innerDF = new org.netbeans.modules.j2ee.sun.share.SunDeploymentFactory();
 //              innerDF = (DeploymentFactory)Installer.getPluginLoader().loadClass("org.netbeans.modules.j2ee.sun.share.SunDeploymentFactory").newInstance();//NOI18N
            }
            catch (Exception e){
                System.out.println("-----null deploy factory because lacking app server classes");
                
            }
            
        }
        public DeploymentManager getDeploymentManager(String str, String str1, String str2) throws DeploymentManagerCreationException {

            if (!PluginProperties.getDefault().isCurrentAppServerLocationValid()){
                throw new DeploymentManagerCreationException(NbBundle.getMessage(PluginProperties.class, "MSG_WrongInstallDir"));
            }
            Authenticator.setDefault(new AdminAuthenticator());
            
            DeploymentManager dm= innerDF.getDeploymentManager(str,str1,str2);
            //System.out.println("(DeploymentManager in installer"+dm);
            return dm;
        }
        
        public DeploymentManager getDisconnectedDeploymentManager(String str) throws DeploymentManagerCreationException {
            if (!PluginProperties.getDefault().isCurrentAppServerLocationValid()){
                throw new DeploymentManagerCreationException(NbBundle.getMessage(PluginProperties.class, "MSG_WrongInstallDir"));
            }
            return innerDF.getDisconnectedDeploymentManager(str);
        }
        
        public String getDisplayName() {
            return innerDF.getDisplayName();
        }
        
        public String getProductVersion() {
            return innerDF.getProductVersion();
        }
        
        public boolean handlesURI(String str) {
            return innerDF.handlesURI(str);
        }
        
    }

    public synchronized static ExtendedClassLoader getPluginLoader() {
        if(pluginLoader==null)
            try {
                pluginLoader =new ExtendedClassLoader( new Empty().getClass().getClassLoader());
                updatePluginLoader(pluginLoader);
            }
            catch (Exception ex2) {
                org.openide.ErrorManager.getDefault().notify(ex2);
                System.out.println(ex2);
            }
        return pluginLoader;
    }

  private static void loadLocaleSpecificJars(File file, ExtendedClassLoader loader) {
       File parentDir = file.getParentFile();
       //System.out.println("parentDir: " + parentDir);
       File localeDir = new File(parentDir, "locale"); //NOI18N
       if(localeDir.exists()){
            File[] localeFiles = localeDir.listFiles();
            File localeFile = null;
            String localeFileName = null;
            String fileName = file.getName();
            fileName = getFileNameWithoutExt(fileName);
            //System.out.println("fineName: " + fileName);
            assert(fileName.length() > 0);
            for(int i=0; i<localeFiles.length; i++){
                localeFile = localeFiles[i];
                localeFileName = localeFile.getName();
                //System.out.println("localeFileName: " + localeFileName);
                assert(localeFileName.length() > 0);
                if(localeFileName.startsWith(fileName)){
                   try{
                    loader.addURL(localeFile); 
                   }catch (Exception ex2) {
                        System.out.println(ex2.getLocalizedMessage());
                   }
                }
            }
       }
  }
  
  private static String getFileNameWithoutExt(String fileName){
    int index = fileName.lastIndexOf("."); //NOI18N
    if(index != -1){
        fileName = fileName.substring(0, index);
    }
    return fileName;
  }

}
