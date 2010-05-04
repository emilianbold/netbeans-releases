/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.subversion.client;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLKeyException;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.IndexingBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.Cancellable;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 *
 * @author Tomas Stupka 
 */
public class SvnClientInvocationHandler implements InvocationHandler {    

    private static final Logger LOG = Logger.getLogger(SvnClientInvocationHandler.class.getName());
    
    protected static final String GET_SINGLE_STATUS = "getSingleStatus"; // NOI18N
    protected static final String GET_STATUS = "getStatus"; // NOI18N
    protected static final String GET_INFO_FROM_WORKING_COPY = "getInfoFromWorkingCopy"; // NOI18N
    protected static final String CANCEL_OPERATION = "cancel"; //NOI18N
    private static final HashSet<String> PARALLELIZABLE_METHODS = new HashSet<String>(Arrays.asList(new String[] {
        "setConfigDirectory",                                           //NOI18N
        "getSvnUrl",                                                    //NOI18N
        "addNotifyListener",                                            //NOI18N
        "getIgnoredPatterns",                                           //NOI18N
        "removeNotifyListener"                                          //NOI18N
    }));
    
    private static final Object semaphor = new Object();

    private final ISVNClientAdapter adapter;
    private final SvnClientDescriptor desc;
    private Cancellable cancellable;
    private SvnProgressSupport support;
    private final int handledExceptions;
    private static boolean metricsAlreadyLogged = false;
    
   /**
     *
     */
    public SvnClientInvocationHandler (ISVNClientAdapter adapter, SvnClientDescriptor desc, int handledExceptions) {
        
        assert adapter  != null;
        assert desc     != null;
        
        this.adapter = adapter;
        this.desc = desc;
        this.handledExceptions = handledExceptions;
    }

    public SvnClientInvocationHandler (ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support, int handledExceptions) {
        
        assert adapter  != null;
        assert desc     != null;
        
        this.adapter = adapter;
        this.desc = desc;
        this.support = support;
        this.handledExceptions = handledExceptions;
        this.cancellable = new Cancellable() {
            public boolean cancel() {
                try {
                    SvnClientInvocationHandler.this.adapter.cancelOperation();
                } catch (SVNClientException ex) {
                    Subversion.LOG.log(Level.SEVERE, null, ex);
                    return false;
                }
                return true;
            }
        };
    }

    private static String print(Object[] args) {
        if (args == null || args.length == 0) {
            return "no parameters"; //NOI18N
        } else {
            StringBuilder sb = new StringBuilder();
            for(Object a : args) {
                sb.append("\n  "); //NOI18N
                if (a == null) {
                    sb.append("null"); //NOI18N
                } else {
                    sb.append(a.toString());
                    sb.append(" : "); //NOI18N
                    sb.append(a.getClass().getName());
                }
                sb.append("\n"); //NOI18N
            }
            return sb.toString();
        }
    }

    /**
     * @see InvocationHandler#invoke(Object proxy, Method method, Object[] args)
     */
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                
        boolean fsReadOnlyAction = isFSWrittingCommand(method);

        try {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("~~~ SVN: invoking '" + method.getName() + "' with " + print(args)); //NOI18N
                //new Throwable("~~~ SVN: invoking '" + method.getName() + "'").printStackTrace();
            }

            Callable<Object> c = new Callable<Object>() {
                public Object call() throws Exception {
                    if(parallelizable(method, args)) {
                        return invokeMethod(method, args);
                    } else {
                        synchronized (semaphor) {
                            return invokeMethod(method, args);
                        }
                    }
                }
            };
            if (fsReadOnlyAction) {
                return c.call();
            } else {
                List<File> files = getFileParameters(args);
                if(files.size() > 0) {
                    return IndexingBridge.getInstance().runWithoutIndexing(c, files.toArray(new File[files.size()]));
                } else {
                    // no file arguments - seems there is no need to stop indexing
                    //                     or refresh the FS
                    return c.call();
                }
            }
        } catch (Exception e) {
            try {
                if(handleException((SvnClient) proxy, e) ) {
                    return invoke(proxy, method, args);
                } else {
                    // some action canceled by user message 
                    throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER); 
                }                
            } catch (InvocationTargetException ite) {
                Throwable t = ite.getTargetException();
                if(t instanceof SVNClientException) {
                    throw t;
                }
                throw ite;
            } catch (SSLKeyException ex) {
                if(ex.getCause() instanceof InvalidKeyException) {
                    InvalidKeyException ike = (InvalidKeyException) ex.getCause();
                    if(ike.getMessage().toLowerCase().equals("illegal key size or default parameters")) { // NOI18N
                        SvnClientExceptionHandler.handleInvalidKeyException(ike);
                    }
                    return null; 
                }
                throw ex;
            } catch (Throwable t) {
                if(t instanceof InterruptedException) {
                    throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER);                     
                } 
                if(t instanceof SVNClientException) {
                    Throwable c = t.getCause();
                    if(c instanceof IOException) {
                        c = c.getCause();
                        if(c instanceof InterruptedException) {                    
                            throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER);                     
                        } 
                    }
                }
                Throwable c = t.getCause();
                if(c != null) {
                    // c.getMessage() could return null here, it is a general Throwable object => isOperationCancelled throws a NPE
                    String exMessage = c.getMessage();
                    if(c instanceof InterruptedException || (exMessage != null && SvnClientExceptionHandler.isOperationCancelled(exMessage))) {
                        throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER);
                    }
                }
                if(support != null && support.isCanceled()) {
                    // action has been canceled, level info should be fine
                    Subversion.LOG.log(Level.INFO, null, t);
                    // who knows what might have happened ...
                    throw new SVNClientException(SvnClientExceptionHandler.ACTION_CANCELED_BY_USER);
                }
                throw t;
            }
        } finally {
            // whatever command was invoked, whatever the result is - 
            // call refresh for all files notified by the client adapter
            if (fsReadOnlyAction) {
                Subversion.getInstance().getRefreshHandler().refresh();
            }
        }
    }

    private List<File> getFileParameters(final Object[] args) {
        List<File> files = new LinkedList<File>();
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg instanceof File) {
                    files.add((File) arg);
                } else if (arg instanceof File[]) {
                    File[] fs = (File[]) arg;
                    for (File file : fs) {
                        files.add(file);
                    }
                }
            }
        }
        return files;
    }

    private boolean isFSWrittingCommand(final Method method) {
        // list here all operations that can potentially modify files on the disk
        return !method.getName().equals("update") &&           // NOI18N
               !method.getName().equals("revert") &&           // NOI18N
               !method.getName().equals("switchToUrl") &&      // NOI18N
               !method.getName().equals("remove") &&           // NOI18N
               !method.getName().equals("mkdir") &&            // NOI18N
               !method.getName().equals("checkout") &&         // NOI18N
               !method.getName().equals("copy") &&             // NOI18N
               !method.getName().equals("move") &&             // NOI18N
               !method.getName().equals("merge");              // NOI18N
    }

    private void logClientInvoked() {
        if(metricsAlreadyLogged) {
            return;
        }
        try {
            SvnClientFactory.checkClientAvailable();
        } catch (SVNClientException e) {
            return;
        }
        String client = null;
        if(SvnClientFactory.isCLI()) {
            client = "CLI";
        } else if(SvnClientFactory.isJavaHl()) {
            client = "JAVAHL";
        } else if(SvnClientFactory.isSvnKit()) {
            client = "SVNKIT";
        } else {
            Subversion.LOG.warning("Unknown client type!");            
        }
        if(client != null) {
            Utils.logVCSClientEvent("SVN", client);   
        }
        metricsAlreadyLogged = true;
    }
    
    private boolean parallelizable(Method method, Object[] args) {
        return isLocalReadCommand(method, args) || isCancelCommand(method, args)
                || PARALLELIZABLE_METHODS.contains(method.getName());
    }
    
    protected boolean isLocalReadCommand(Method method, Object[] args) {
        String methodName = method.getName();
        return methodName.equals(GET_SINGLE_STATUS) ||
               methodName.equals(GET_INFO_FROM_WORKING_COPY) ||
               (method.getName().equals(GET_STATUS) && method.getParameterTypes().length == 3);
    }

    protected boolean isCancelCommand(final Method method, Object[] args) {
        String methodName = method.getName();
        return Cancellable.class.isAssignableFrom(method.getDeclaringClass())
                && methodName.equals(CANCEL_OPERATION);
    }
    
    protected Object invokeMethod(Method proxyMethod, Object[] args)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        return handle(proxyMethod, args);    
    }

    protected Object handle(final Method proxyMethod, final Object[] args) 
    throws SecurityException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException 
    {
        Object ret;

        Class[] parameters = proxyMethod.getParameterTypes();
        Class declaringClass = proxyMethod.getDeclaringClass();

        // escaping SVNUrl argument values - #146041
        // for javahl this is the place to escape those args, for cmd see SvnCommand
        if (args != null) {
            for (int i = 0; i < args.length; ++i) {
                Object arg = args[i];
                if (arg != null && arg instanceof SVNUrl) {
                    try {
                        args[i] = SvnUtils.decodeAndEncodeUrl((SVNUrl) arg);
                    } catch (MalformedURLException ex) {
                        Subversion.LOG.log(Level.INFO, "Url: " + arg, ex);
                    }
                }
            }
        }

        if( ISVNClientAdapter.class.isAssignableFrom(declaringClass) ) {
            // Cliet Adapter
            if(support != null) {
                support.setCancellableDelegate(cancellable);
            }            
            // save the proxy settings into the svn servers file                
            if(desc != null && desc.getSvnUrl() != null) {
                SvnConfigFiles.getInstance().storeSvnServersSettings(desc.getSvnUrl());
            }
            logClientInvoked();
            ret = adapter.getClass().getMethod(proxyMethod.getName(), parameters).invoke(adapter, args);
            if(support != null) {
                support.setCancellableDelegate(null);
            }
        } else if( Cancellable.class.isAssignableFrom(declaringClass) ) { 
            // Cancellable
            ret = cancellable.getClass().getMethod(proxyMethod.getName(), parameters).invoke(cancellable, args);
        } else if( SvnClientDescriptor.class.isAssignableFrom(declaringClass) ) {            
            // Client Descriptor
            if(desc != null) {
                ret = desc.getClass().getMethod(proxyMethod.getName(), parameters).invoke(desc, args);    
            } else {
                // when there is no descriptor, then why has the method been called
                throw new NoSuchMethodException(proxyMethod.getName());
            }            
        } else {
            // try to take care for hashCode, equals & co. -> fallback to clientadapter
            ret = adapter.getClass().getMethod(proxyMethod.getName(), parameters).invoke(adapter, args);
        }                
        
        return ret;
    }

    private boolean handleException(SvnClient client, Throwable t) throws Throwable {
        if( t instanceof InvocationTargetException ) {
            t = ((InvocationTargetException) t).getCause();            
        } 
        if( !(t instanceof SVNClientException) ) {
            throw t;
        }

        SvnClientExceptionHandler eh = new SvnClientExceptionHandler((SVNClientException) t, adapter, client, desc, handledExceptions, isCommandLine());
        return eh.handleException();        
    }

    protected boolean isCommandLine () {
        return false;
    }
    
}

