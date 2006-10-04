/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.netbeans.modules.subversion.Subversion;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;

/**
 * A SvnClient factory
*
 * @author Tomas Stupka
 */
public class SvnClientFactory {

    /** the only existing SvnClientFactory instance */
    private static SvnClientFactory instance;    
            
    /** Creates a new instance of SvnClientFactory */
    private SvnClientFactory() {

    }

    /**
     * Returns the only existing SvnClientFactory instance
     *
     * @return the SvnClientFactory instance
     */
    public static SvnClientFactory getInstance() {
        if(instance == null) {
            instance = new SvnClientFactory();
        }
        return instance;
    }

    /**
     * Returns a SvnClientInvocationHandler instance, which doesn't know anything about the remote repository,
     * has no username, password and SvnProgressSupport<br/>
     * It's not supposed to work when calling svn commands which interact wiht the remote repository.
     *
     * @return the SvnClient
     */
    public SvnClient createSvnClient() {
        ISVNClientAdapter adapter = createSvnClientAdapter();
        return createSvnClient(adapter, null, null, SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS);
    }

    /**
     *
     * Returns a SvnClientInvocationHandler instance which is configured with the given <tt>support</tt>
     * and a <tt>username</tt> and <tt>password</tt> if there is a file in the [SVN_CONFIG_DIR]/auth folder
     * for the given <tt>repositoryUrl</tt>. 
     *
     * @param repositoryUrl
     * @param support    
     *
     * @return the configured SvnClient
     *
     */
    public SvnClient createSvnClient(SVNUrl repositoryUrl, SvnProgressSupport support)
    throws SVNClientException 
    {                                                
        String username = ""; // NOI18N
        String password = ""; // NOI18N
        PasswordFile passwordFile = PasswordFile.findFileForUrl(repositoryUrl);
        if(passwordFile!=null) {
            username = passwordFile.getUsername();
            password = passwordFile.getPassword();            
        }        
        ISVNClientAdapter adapter = createSvnClientAdapter(repositoryUrl, null, username, password);
        return createSvnClient(adapter, support, repositoryUrl, SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS);
    }    

    /**
     *
     * Returns a SvnClientInvocationHandler instance which is configured with the given <tt>username</tt>,
     * <tt>password</tt> and a SvnClientDescriptor for <tt>repository</tt>. In case the proxy given via
     * <tt>pd</tt> is http, an according entry for the <tt>repositoryUrl</tt> will be created in the svn config file.
     *
     *
     * @param repositoryUrl
     * @param pd
     * @param username
     * @param password
     *
     * @return the configured SvnClient
     *
     */
    public SvnClient createSvnClient(SVNUrl repositoryUrl,
                                     ProxyDescriptor pd,
                                     String username, 
                                     String password) 
    {                                                                                   
        return createSvnClient(repositoryUrl, pd, username, password, SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS) ;
    }

    /**
     *
     * Returns a SvnClientInvocationHandler instance which is configured with the given <tt>username</tt>,
     * <tt>password</tt> and a SvnClientDescriptor for <tt>repository</tt>. In case the proxy given via
     * <tt>pd</tt> is http, an according entry for the <tt>repositoryUrl</tt> will be created in the svn config file.
     * The mask <tt>handledExceptions</tt> specifies which exceptions are to be handled.
     *
     * @param repositoryUrl
     * @param pd
     * @param username
     * @param password
     * @param handledExceptions
     *
     * @return the configured SvnClient
     *
     */    
    public SvnClient createSvnClient(SVNUrl repositoryUrl, ProxyDescriptor pd, String username, String password, int handledExceptions) {
        ISVNClientAdapter adapter = createSvnClientAdapter(repositoryUrl, pd, username, password);
        return createSvnClient(adapter, null, repositoryUrl, handledExceptions);
    }
    
    /**
     *
     * Returns a SvnClientInvocationHandler instance which is configured with the given <tt>adapter</tt>,
     * <tt>support</tt> and a SvnClientDescriptor for <tt>repository</tt>. The mask <tt>handledExceptions</tt> 
     * specifies which exceptions are to be handled.
     *
     * @param adapter
     * @param support
     * @param repository
     * @param handledExceptions
     *
     * @return the created SvnClientInvocationHandler instance
     *
     */
    private SvnClient createSvnClient(ISVNClientAdapter adapter, SvnProgressSupport support, final SVNUrl repository, int handledExceptions) {
        
        assert (handledExceptions & SvnClientExceptionHandler.EX_HANDLED_EXCEPTIONS) != 0 : "handledExceptions not in SvnClientExceptionHandler.EX_HANDLED_EXCEPTIONS";
        
        Class proxyClass = Proxy.getProxyClass(SvnClient.class.getClassLoader(), new Class[]{ SvnClient.class } );

        SvnClientInvocationHandler handler;
        SvnClientDescriptor desc = new SvnClientDescriptor() {
            public SVNUrl getSvnUrl() {
                return repository;
            }
        };
        Subversion.getInstance().cleanupFilesystem();
        if(support != null) {
            handler = new SvnClientInvocationHandler(adapter, desc, support, handledExceptions);
        } else {
            handler = new SvnClientInvocationHandler(adapter, desc, handledExceptions);
        } 
        try {
           return (SvnClient) proxyClass.getConstructor( new Class[] { InvocationHandler.class } ).newInstance( new Object[] { handler } );
        } catch (Exception e) {
            org.openide.ErrorManager.getDefault().notify(e);
        }
        return null;
    }    

    /**
     * Creates a new CommandlineClientAdapter instance, configures it with the given <tt>username</tt> and <tt>password</tt>, and
     * in case the proxy given via <tt>pd</tt> is http, an according entry for the <tt>repositoryUrl</tt>
     * will be created in the svn config file.
     *
     * @param repositoryUrl
     * @param pd
     * @param username
     * @param password
     *
     * @return the configured ISVNClientAdapter
     */
    private ISVNClientAdapter createSvnClientAdapter(SVNUrl repositoryUrl,
                                                     ProxyDescriptor pd,
                                                     String username,
                                                     String password)
    {        
        ISVNClientAdapter adapter = createSvnClientAdapter();
        // XXX do we need this?
        if(pd != null && pd.getType() == ProxyDescriptor.TYPE_HTTP) {
            SvnConfigFiles.getInstance().setProxy(pd, SvnUtils.ripUserFromHost(repositoryUrl.getHost()));
        }        
        try {
            File configDir = FileUtil.normalizeFile(new File(SvnConfigFiles.getNBConfigPath()));
            adapter.setConfigDirectory(configDir);
            adapter.setUsername(username);
            adapter.setPassword(password);
        } catch (SVNClientException ex) {
            ErrorManager.getDefault().notify(ex); // should not happen
        }        
        return adapter;
    }        
    
    /**
     * Returns a CommandlineClientAdapter instance.
     *
     * @return a CommandlineClientAdapter instance
     */
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
