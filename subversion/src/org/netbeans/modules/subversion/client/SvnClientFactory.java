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
//import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
//import org.tigris.subversion.svnclientadapter.javasvn.JavaSvnClientAdapterFactory;

/**
 * A SvnClient factory
*
 * @author Tomas Stupka 
 */
public class SvnClientFactory {

    /** the only existing SvnClientFactory instance */
    private static SvnClientFactory instance;    
    
    private ClientAdapterFactory factory;
        
    private SVNClientException exception;
    
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

    public void setup() throws SVNClientException {
        try {
//            String factoryType = System.getProperty("svnClientAdapterFactory");
            
            setupCommandline();
            
//            if(factoryType == null || 
//               factoryType.trim().equals("") || 
//               factoryType.equals(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT)) 
//            {                
//                setupCommandline();
//            } 
//            else if(factoryType.equals(JhlClientAdapterFactory.JAVAHL_CLIENT)) {
//                try {                    
//                    setupJavaHl();
//                } catch (SVNClientException ex) {
//                    // something went wrong - fallback on the commandline
//                    ErrorManager.getDefault().log(ErrorManager.WARNING, "Could not setup JavaHl. Trying to fallback on the commandline client");                    
//                    setupCommandline();
//                }
//            }
//            else if(factoryType.equals(JavaSvnClientAdapterFactory.JAVASVN_CLIENT)) {
//                try {                    
//                    setupJavaSVN();
//                } catch (SVNClientException ex) {
//                    ErrorManager.getDefault().log(ErrorManager.WARNING, "Could not setup JavaSVN. Trying to fallback on the commandline client");                    
//                    setupCommandline();
//                }
//            } 
//            else {                
//                throw new SVNClientException("Unknown factory: " + factoryType);
//            } 
        } catch (Throwable t) {            
            //ErrorManager.getDefault().notify(ErrorManager.WARNING, "Could not setup for the given ")
            // XXX is this robust enought? - no! if this won't work then you'll get NPEs until the doctor comes
            
           factory = new ClientAdapterFactory() {
                public ISVNClientAdapter createAdapter() {
                    return new UnsupportedSvnClientAdapter();
                }
                public boolean isCommandLine() {
                    return true;
                }                        
                protected ISVNClientAdapter createSvnClientAdapter(SVNUrl repositoryUrl, ProxyDescriptor pd, String username, String password) {
                    return super.createSvnClientAdapter(repositoryUrl, pd, username, password);
                }

                public SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                    return new SvnCmdLineClientInvocationHandler(adapter, desc, support, handledExceptions);
                }            
            }; 
            
            if(t instanceof SVNClientException) throw (SVNClientException) t;
            
            throw exception;
        }
    }
    
//    private void setupJavaHl () throws SVNClientException {        
//        JhlClientAdapterFactory.setup();
//        factory = new ClientAdapterFactory() {
//            public ISVNClientAdapter createAdapter() {
//                ISVNClientAdapter adapter = SVNClientAdapterFactory.createSVNClient(JhlClientAdapterFactory.JAVAHL_CLIENT);               
//                return adapter;
//            }
//            public boolean isCommandLine() {
//                
//                return false;
//            }
//            public SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
//                return new SvnClientInvocationHandler(adapter, desc, support, handledExceptions);
//            }
//        };
//        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "svnClientAdapter running on javahl");
//    }
    
//    private void setupJavaSVN () throws SVNClientException {
//        // XXX error handling if the library isn't there
//        JavaSvnClientAdapterFactory.setup();
//        factory = new ClientAdapterFactory() {
//            public ISVNClientAdapter createAdapter() {
//                return SVNClientAdapterFactory.createSVNClient(JavaSvnClientAdapterFactory.JAVASVN_CLIENT);
//            }
//            public boolean isCommandLine() {
//                return false;
//            }            
//            public SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
//                return new SvnClientInvocationHandler(adapter, desc, support, handledExceptions);
//            }            
//        };                        
//        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "svnClientAdapter running on javasvn");        
//    }

    private void setupCommandline () throws SVNClientException {
        CmdLineClientAdapterFactory.setup();
        factory = new ClientAdapterFactory() {
            public ISVNClientAdapter createAdapter() {
                return SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
            }
            public boolean isCommandLine() {
                return true;
            }                        
            protected ISVNClientAdapter createSvnClientAdapter(SVNUrl repositoryUrl, ProxyDescriptor pd, String username, String password) {
                ISVNClientAdapter adapter = super.createSvnClientAdapter(repositoryUrl, pd, username, password);
                adapter.addPasswordCallback(new SvnClientCallback(repositoryUrl));
                return adapter;
            }
        
            public SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnCmdLineClientInvocationHandler(adapter, desc, support, handledExceptions);
            }            
        };       
        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "svnClientAdapter running on commandline client");        
    }   
        
    /**
     * Returns a SvnClientInvocationHandler instance, which doesn't know anything about the remote repository,
     * has no username, password and SvnProgressSupport<br/>
     * It's not supposed to work when calling svn commands which interact wiht the remote repository.
     *
     * @return the SvnClient
     */
    public SvnClient createSvnClient() {    
        return factory.createSvnClient();
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
    public SvnClient createSvnClient(SVNUrl repositoryUrl, SvnProgressSupport support) {
        return factory.createSvnClient(repositoryUrl, support);
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
    public SvnClient createSvnClient(SVNUrl repositoryUrl, ProxyDescriptor pd, String username, String password) {
        return factory.createSvnClient(repositoryUrl, pd, username, password);
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
        return factory.createSvnClient(repositoryUrl, pd, username, password, handledExceptions);
    }
               
    private abstract class ClientAdapterFactory {
                
        abstract public ISVNClientAdapter createAdapter();
        abstract public boolean isCommandLine();
        abstract public SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions);


    
        SvnClient createSvnClient() {            
            ISVNClientAdapter adapter = createAdapter();
            return createSvnClient(adapter, null, null, SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS);
        }
        
        public SvnClient createSvnClient(SVNUrl repositoryUrl, SvnProgressSupport support) {                                                
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
        
        public SvnClient createSvnClient(SVNUrl repositoryUrl,
                                         ProxyDescriptor pd,
                                         String username, 
                                         String password) 
        {                                                                                   
            return createSvnClient(repositoryUrl, pd, username, password, SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS) ;
        }
        
        public SvnClient createSvnClient(SVNUrl repositoryUrl, ProxyDescriptor pd, String username, String password, int handledExceptions) {
            ISVNClientAdapter adapter = createSvnClientAdapter(repositoryUrl, pd, username, password);
            return createSvnClient(adapter, null, repositoryUrl, handledExceptions);
        }
        
        /**
         *
         * Returns a SvnClientInvocationHandler instance which is configured with the given <tt>adapter</tt>,
         * <tt>support</tt> and a SvnClientDescriptor for <tt>repository</tt>.
         *
         * @param adapter
         * @param support
         * @param repository
         *
         * @return the created SvnClientInvocationHandler instance
         *
         */
        private SvnClient createSvnClient(ISVNClientAdapter adapter, SvnProgressSupport support, final SVNUrl repository, int handledExceptions) {
            Class proxyClass = Proxy.getProxyClass(SvnClient.class.getClassLoader(), new Class[]{ SvnClient.class } );

            SvnClientInvocationHandler handler;
            Subversion.getInstance().cleanupFilesystem();

            SvnClientDescriptor desc = new SvnClientDescriptor() {
                public SVNUrl getSvnUrl() {
                    return repository;
                }
            };     

            handler = getInvocationHandler(adapter, desc, support, handledExceptions);
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
        protected ISVNClientAdapter createSvnClientAdapter(SVNUrl repositoryUrl,
                                                         ProxyDescriptor pd,
                                                         String username,
                                                         String password)
        {        
            ISVNClientAdapter adapter = createAdapter();
            if(pd != null && pd.getType() == ProxyDescriptor.TYPE_HTTP) {
                SvnConfigFiles.getInstance().setProxy(pd, SvnUtils.ripUserFromHost(repositoryUrl.getHost()));
            }        
            try {
                File configDir = FileUtil.normalizeFile(new File(SvnConfigFiles.getNBConfigPath()));
                adapter.setConfigDirectory(configDir);
                // XXX do we need this for javahL ???
                adapter.setUsername(username);
                adapter.setPassword(password);
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ex); // should not happen
            }        
            return adapter;
        }                    
    }
    
}
