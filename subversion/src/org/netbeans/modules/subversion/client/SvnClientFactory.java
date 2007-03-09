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
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.Subversion;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapter;

/**
 * A SvnClient factory
*
 * @author Tomas Stupka 
 */
public class SvnClientFactory {

    /** the only existing SvnClientFactory instance */
    private static SvnClientFactory instance;    
    
    private ClientAdapterFactory factory;
        
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
     * Returns a SvnClient, which isn't configured in any way.
     * Knows no username, password, has no SvnProgressSupport<br/>
     * Such an instance isn't supposed to work properly when calling remote svn commands.
     *
     * @return the SvnClient
     */
    public SvnClient createSvnClient() {    
        return factory.createSvnClient();
    }

    /**
     *
     * Returns a SvnClient which is configured with the given <tt>username</tt>,
     * <tt>password</tt>, <tt>repositoryUrl</tt> and the <tt>support</tt>.<br>
     * In case a http proxy was given via <tt>pd</tt> an according entry for the <tt>repositoryUrl</tt> 
     * will be created in the svn config file. 
     * The mask <tt>handledExceptions</tt> specifies which exceptions are to be handled.
     *
     * @param repositoryUrl
     * @param support
     * @param username
     * @param password
     * @param handledExceptions
     *
     * @return the configured SvnClient
     *
     */    
    public SvnClient createSvnClient(SVNUrl repositoryUrl, SvnProgressSupport support, String username, String password, int handledExceptions) {
        return factory.createSvnClient(repositoryUrl, support, username, password, handledExceptions);
    }
    
    /**
     * A SVNClientAdapterFactory will be setup, according to the svnClientAdapterFactory property.<br>
     * The CommandlineClientAdapterFactory is default as long no value is set for svnClientAdapterFactory.
     *
     */ 
    public void setup() throws SVNClientException {
        try {
            String factoryType = System.getProperty("svnClientAdapterFactory");
            
            if(factoryType == null || 
               factoryType.trim().equals("") || 
               factoryType.equals(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT)) 
            {                
                setupCommandline();
            } 
            else if(factoryType.equals(JhlClientAdapterFactory.JAVAHL_CLIENT)) {
                try {                    
                    setupJavaHl();
                } catch (Throwable t) {
                    String jhlErorrs = JhlClientAdapter.getLibraryLoadErrors();
                    // something went wrong - fallback on the commandline                                                            
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, t);                    
                    ErrorManager.getDefault().log(ErrorManager.WARNING, jhlErorrs);                                                            
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "Could not setup JavaHl. Falling back on the commandline client!");                    
                    setupCommandline();
                }
            } /*else if(factoryType.equals(JavaSvnClientAdapterFactory.JAVASVN_CLIENT)) {
                try {                    
                    setupJavaSvn();
                } catch (Throwable t) {                    
                    // something went wrong - fallback on the commandline                                                            
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, t);                                        
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "Could not setup javasvn. Falling back on the commandline client!");                    
                    setupCommandline();
                }
            } */else {                
                throw new SVNClientException("Unknown factory: " + factoryType);
            } 
        } catch (Throwable t) {                                    
            setupUnsupported();            
            if(t instanceof SVNClientException) {
                throw (SVNClientException) t;
            }            
            throw new SVNClientException(t);
        }
    }
    
    private void setupJavaHl () throws SVNClientException {        
        JhlClientAdapterFactory.setup();        
        factory = new ClientAdapterFactory() {            
            protected ISVNClientAdapter createAdapter() {
                return SVNClientAdapterFactory.createSVNClient(JhlClientAdapterFactory.JAVAHL_CLIENT);           
            }
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnClientInvocationHandler(adapter, desc, support, handledExceptions);
            }
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return new SvnClientCallback(repositoryUrl, handledExceptions);
            }
        };
        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "svnClientAdapter running on javahl");
    }
    
    /*
    private void setupJavaSvn () throws SVNClientException {        
        JavaSvnClientAdapterFactory.setup();        
        factory = new ClientAdapterFactory() {            
            protected ISVNClientAdapter createAdapter() {
                return SVNClientAdapterFactory.createSVNClient(JavaSvnClientAdapterFactory.JAVASVN_CLIENT);           
            }
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnClientInvocationHandler(adapter, desc, support, handledExceptions);
            }
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return new SvnClientCallback(repositoryUrl, handledExceptions);
            }
        };
        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "svnClientAdapter running on javasvn");
    }
    */
    
    private void setupUnsupported () throws SVNClientException {                
       factory = new ClientAdapterFactory() {
            protected ISVNClientAdapter createAdapter() {
                return new UnsupportedSvnClientAdapter();
            }
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnCmdLineClientInvocationHandler(adapter, desc, support, handledExceptions);
            }   
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return null;
            }            
        };         
    }
    
    private void setupCommandline () throws SVNClientException {
        String subversionPath = SvnModuleConfig.getDefault().getExecutableBinaryPath();
        CmdLineClientAdapterFactory.setup(subversionPath);
        factory = new ClientAdapterFactory() {
            protected ISVNClientAdapter createAdapter() {
                return SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
            }        
            protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
                return new SvnCmdLineClientInvocationHandler(adapter, desc, support, handledExceptions);
            }
            protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions) {
                return null;
            }            
        };       
        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "svnClientAdapter running on commandline client");        
    }           
               
    private abstract class ClientAdapterFactory {
                
        abstract protected ISVNClientAdapter createAdapter();
        abstract protected SvnClientInvocationHandler getInvocationHandler(ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions);
        abstract protected ISVNPromptUserPassword createCallback(SVNUrl repositoryUrl, int handledExceptions);
        
        SvnClient createSvnClient() {                        
            SvnClientInvocationHandler handler = getInvocationHandler(createAdapter(), createDescriptor(null), null, -1);
            return createSvnClient(handler);  
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
        public SvnClient createSvnClient(SVNUrl repositoryUrl, SvnProgressSupport support, String username, String password, int handledExceptions) {
            ISVNClientAdapter adapter = createAdapter();
            SvnClientInvocationHandler handler = getInvocationHandler(adapter, createDescriptor(repositoryUrl), support, handledExceptions);            
            setupAdapter(adapter, username, password, createCallback(repositoryUrl, handledExceptions));
            return createSvnClient(handler);
        }
        
        private SvnClientDescriptor createDescriptor(final SVNUrl repositoryUrl) {
            return new SvnClientDescriptor() {
                public SVNUrl getSvnUrl() {
                    return repositoryUrl;
                }
            };                 
        }
        
        private SvnClient createSvnClient(SvnClientInvocationHandler handler) {
            Class proxyClass = Proxy.getProxyClass(SvnClient.class.getClassLoader(), new Class[]{ SvnClient.class } );
            Subversion.getInstance().cleanupFilesystem();                       
            try {
               return (SvnClient) proxyClass.getConstructor( new Class[] { InvocationHandler.class } ).newInstance( new Object[] { handler } );
            } catch (Exception e) {
                org.openide.ErrorManager.getDefault().notify(e);
            }
            return null;
        }   
                  
        protected void setupAdapter(ISVNClientAdapter adapter, String username, String password, ISVNPromptUserPassword callback) {                    
            
            if(callback != null) {
                adapter.addPasswordCallback(callback);
            }
            
            try {
                File configDir = FileUtil.normalizeFile(new File(SvnConfigFiles.getNBConfigPath()));
                adapter.setConfigDirectory(configDir);
                adapter.setUsername(username);
                adapter.setPassword(password);
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ex); // should not happen
            }        
            
        }                    
    }
    
}
