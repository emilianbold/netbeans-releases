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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.netbeans.modules.subversion.config.ProxyDescriptor;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.config.PasswordFile;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
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
        return createSvnClient(adapter, null, null);
    }
    
    public SvnClient createSvnClient(SVNUrl repositoryUrl, SvnProgressSupport support)
    throws SVNClientException 
    {                                                
        String username = "";
        String password = "";
        PasswordFile passwordFile = PasswordFile.findFileForUrl(repositoryUrl);
        if(passwordFile!=null) {
            username = passwordFile.getUsername();
            password = passwordFile.getPassword();            
        }        
        ISVNClientAdapter adapter = createSvnClientAdapter(repositoryUrl, null, username, password);
        return createSvnClient(adapter, support, repositoryUrl);
    }    

    public SvnClient createSvnClient(SVNUrl repositoryUrl,
                                     ProxyDescriptor pd,
                                     String username, 
                                     String password) 
    {                                                                                   
        ISVNClientAdapter adapter = createSvnClientAdapter(repositoryUrl, pd, username, password);
        return createSvnClient(adapter, null, repositoryUrl);
    }
    
    private SvnClient createSvnClient(ISVNClientAdapter adapter, SvnProgressSupport support, final SVNUrl repository) {
        Class proxyClass = Proxy.getProxyClass(SvnClient.class.getClassLoader(), new Class[]{ SvnClient.class } );

        SvnClientInvocationHandler handler;
        SvnClientDescriptor desc = new SvnClientDescriptor() {
            public SVNUrl getSvnUrl() {
                return repository;
            }
        };
        if(support!=null) {
            handler = new SvnClientInvocationHandler(adapter, desc, support);
        } else {
            handler = new SvnClientInvocationHandler(adapter, desc);
        } 
        try {
           return (SvnClient) proxyClass.getConstructor(new Class[] { InvocationHandler.class }).newInstance(new Object[] { handler });                   
        } catch (Exception e) {
            org.openide.ErrorManager.getDefault().notify(e);
        }
        return null;
    }    

    private ISVNClientAdapter createSvnClientAdapter(SVNUrl repositoryUrl,
                                                     ProxyDescriptor pd,
                                                     String username,
                                                     String password)
    {        
        ISVNClientAdapter adapter = createSvnClientAdapter();
        File configDir = null;
        if(pd!=null && pd.getType() == ProxyDescriptor.TYPE_HTTP) {
            SvnConfigFiles.getInstance().setProxy(pd, SvnUtils.ripUserFromHost(repositoryUrl.getHost()));
        }        
        try {
            File file = FileUtil.normalizeFile(new File(SvnConfigFiles.getNBConfigPath()));
            adapter.setConfigDirectory(file);
            adapter.setUsername(username);
            adapter.setPassword(password);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ex); // should not happen
        }        
        return adapter;
    }        

    private ISVNClientAdapter createSvnClientAdapter() {
        ISVNClientAdapter adapter = SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
        if (adapter == null) {
            // TODO ask user for the svn binary path...
            // null can mean that user does not have proper version
            // of svn binary in PATH, so let him provide
            // exact path (and store it for future use)
            adapter = new UnsupportedSvnClientAdapter();
        }                
        return adapter;
    }  
    

    
}
