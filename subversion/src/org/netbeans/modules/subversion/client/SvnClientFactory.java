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
import org.netbeans.modules.subversion.settings.SvnConfigFiles;
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
        File configDir = SvnConfigFiles.getInstance().getConfigFile(pd);
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
    

    
}
