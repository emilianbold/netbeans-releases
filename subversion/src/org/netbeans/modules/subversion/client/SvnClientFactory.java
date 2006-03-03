/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.netbeans.modules.subversion.settings.SvnRootSettings;
import org.netbeans.modules.subversion.settings.PasswordFile;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;

/**
 *
 * @author Tomas Stupka
 */
public class SvnClientFactory {
    
    private static SvnClientFactory instance;
    //private static String 
            
    /** Creates a new instance of SvnClientFactory */
    private SvnClientFactory() {
    }

    public static SvnClientFactory getInstance() {
        if(instance == null) {
            instance = new SvnClientFactory();
        }
        return instance;
    }
    
    public SvnClient createSvnClient() {
        ISVNClientAdapter adapter = createSvnClientAdapter();
        return createSvnClient(adapter);          
    }
    
    public SvnClient createSvnClient(SVNUrl repositoryUrl) 
    throws SVNClientException 
    {                                
        ProxyDescriptor pd = SvnRootSettings.getProxyFor(repositoryUrl);       
                
        String username = "";
        String password = "";
        PasswordFile passwordFile = PasswordFile.findFileForUrl(repositoryUrl);
        if(passwordFile!=null) {
            username = passwordFile.getUsername();
            password = passwordFile.getPassword();            
        }        
        ISVNClientAdapter adapter = createSvnClientAdapter(pd, username, password);
        return createSvnClient(adapter);             
    }    

    public SvnClient createSvnClient(ProxyDescriptor pd, 
                                     String username, 
                                     String password) 
    {                                                                                   
        ISVNClientAdapter adapter = createSvnClientAdapter(pd, username, password);
        return createSvnClient(adapter);                     
    }
    
    private SvnClient createSvnClient(ISVNClientAdapter adapter) {
        Class proxyClass = Proxy.getProxyClass(SvnClient.class.getClassLoader(), new Class[]{ SvnClient.class } );        
        SvnClientInvocationHandler handler = new SvnClientInvocationHandler(adapter);         
        try {
           return (SvnClient) proxyClass.getConstructor(new Class[] { InvocationHandler.class }).newInstance(new Object[] { handler });                   
        } catch (Exception e) {
            org.openide.ErrorManager.getDefault().notify(e);
        }
        return null;
    }    

    private ISVNClientAdapter createSvnClientAdapter(ProxyDescriptor pd, 
                                                 String username, 
                                                 String password) 
    {        
        ISVNClientAdapter adapter = createSvnClientAdapter();
        File configDir = createTempConfig(pd);
        try {
            adapter.setConfigDirectory(configDir);
            adapter.setUsername(username);
            adapter.setPassword(password);
        } catch (SVNClientException ex) {
            ex.printStackTrace(); // should not happen
        }        
        return adapter;
    }        

    private ISVNClientAdapter createSvnClientAdapter() {
        ISVNClientAdapter adapter = SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
        if (adapter == null) {
            adapter = new UnsupportedSvnClientAdapter();
        }        
        // TODO add version check, but there is no API                                
        return adapter;
    }  
    
    // XXX this should be at some another place - maybe at settings?
    // XXX there is also a systemwide config folder /etc/subversion - use it
    //     - find out if there is also something like this on windows ...
    private static File createTempConfig(ProxyDescriptor pd) {
        
        // create the IDE config dir
        File configDir = new File(SvnUtils.getConfigDir());                
        configDir = FileUtil.normalizeFile(configDir);
        configDir.mkdirs();
        
        // create a new svn servers file        
        File configFile = new File(SvnUtils.getConfigDir() + "/servers");       
        configFile = FileUtil.normalizeFile(configFile);
        
        StringBuffer sb = new StringBuffer();
        sb.append("[groups]\n");
        sb.append("group1 = * \n");
        sb.append("[group1]\n");
        if(pd.getHost()!=null) {
            sb.append("http-proxy-host = " + pd.getHost());
            sb.append("\n");   
        }        
        if(pd.getPort() >= 0) {
            sb.append("http-proxy-port = " + pd.getPort());
            sb.append("\n");   
        }        
        if(pd.getUserName()!=null) {
            sb.append("http-proxy-username = " + pd.getUserName());
            sb.append("\n");    
        }
        if(pd.getPassword()!=null) {        
            sb.append("http-proxy-password = " + pd.getPassword());
            sb.append("\n");                        
        }
            
        FileOutputStream fos = null;        
        try {
            fos = new FileOutputStream(configFile);            
            fos.write(sb.toString().getBytes());            
            fos.flush();                        
        } catch (IOException ex) {
            org.openide.ErrorManager.getDefault().notify(ex);
            return null;
        } finally {
            if( fos != null){
                try {
                    fos.close();    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }    
            }
        }       
            
        // copy svn config file
        String userHome = System.getProperty("user.home");               
        if(Utilities.isUnix()) {
            copyToIDEConfigDir(new File(userHome + "/.subversion/config"));  
        } else if (Utilities.isWindows()){
            copyToIDEConfigDir(new File(userHome + "/Application Data/Subversion/config"));  
        } else {
            // skip            
            // XXX what about another platforms?    
        }
        
        // copy svn auth files       
        if(Utilities.isUnix()) {
            copyToIDEAuthDir(new File(userHome + "/.subversion/"));  
        } else if (Utilities.isWindows()){
            copyToIDEAuthDir(new File(userHome + "/Application Data/Subversion/"));  
        } else {
            // skip            
            // XXX what about another platforms?    
        }        
        
        return configDir;
    }
    
    // XXX reafctor
    private static void copyToIDEConfigDir (File file) {
        file = FileUtil.normalizeFile(file);
        File targetConfigFile = new File(SvnUtils.getConfigDir() + "/config");  
        targetConfigFile = FileUtil.normalizeFile(targetConfigFile);
        try {                    
            SvnUtils.copyFile (file, targetConfigFile);
        } catch (IOException ex) {
            ex.printStackTrace(); // should not happen
        }            
    }

    // XXX reafctor    
    private static void copyToIDEAuthDir (File sourceDir) {
        sourceDir = FileUtil.normalizeFile(sourceDir);
        
        File targetAuthDir = new File(SvnUtils.getConfigDir() + "/auth/svn.simple");          
        targetAuthDir = FileUtil.normalizeFile(targetAuthDir);
        targetAuthDir.mkdirs();
        File sourceAuthDir = FileUtil.normalizeFile(new File(sourceDir.getAbsolutePath() + "/auth/svn.simple"));  
        sourceAuthDir.mkdirs();
        copyDirFiles(sourceAuthDir, targetAuthDir);
                
        targetAuthDir = new File(SvnUtils.getConfigDir() + "/auth/svn.username");  
        targetAuthDir = FileUtil.normalizeFile(targetAuthDir);
        targetAuthDir.mkdirs();
        sourceAuthDir = FileUtil.normalizeFile(new File(sourceDir.getAbsolutePath() + "/auth/svn.username"));  
        sourceAuthDir.mkdirs();
        copyDirFiles(sourceAuthDir, targetAuthDir);
                
        targetAuthDir = new File(SvnUtils.getConfigDir() + "/auth/svn.ssl.server");  
        targetAuthDir = FileUtil.normalizeFile(targetAuthDir);
        targetAuthDir.mkdirs();
        sourceAuthDir = FileUtil.normalizeFile(new File(sourceDir.getAbsolutePath() + "/auth/svn.ssl.server"));  
        sourceAuthDir.mkdirs();
        copyDirFiles(sourceAuthDir, targetAuthDir);

    }    
    
    private static void copyDirFiles(File sourceDir, File targetDir) {
        File[] files = sourceDir.listFiles();
        if(files==null || files.length == 0) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            try {                    
                File target = FileUtil.normalizeFile(new File(targetDir.getAbsolutePath() + "/" + files[i].getName()));
                SvnUtils.copyFile (files[i], target);
            } catch (IOException ex) {
                ex.printStackTrace(); // should not happen
            }                
        }                       
    }
    
}
