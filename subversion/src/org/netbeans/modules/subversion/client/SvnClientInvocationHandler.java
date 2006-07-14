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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.util.*;
import javax.net.ssl.SSLKeyException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.client.parser.LocalSubversionException;
import org.netbeans.modules.subversion.client.parser.SvnWcParser;
import org.openide.ErrorManager;
import org.openide.util.Cancellable;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 *
 * @author Tomas Stupka
 */
public class SvnClientInvocationHandler implements InvocationHandler {

    private static final String ISVNSTATUS_IMPL = System.getProperty("ISVNStatus.impl", ""); // NOI18N
    private static final String GET_SINGLE_STATUS = "getSingleStatus"; // NOI18N
    private static final String GET_STATUS = "getStatus"; // NOI18N
    private static final String GET_INFO_FROM_WORKING_COPY = "getInfoFromWorkingCopy"; // NOI18N

    private static Set<String> remoteMethods = new HashSet<String>();

    private static Object semaphor = new Object();
        
    static {
        remoteMethods.add("checkout");  // NOI18N
        remoteMethods.add("commit"); // NOI18N
        remoteMethods.add("commitAcrossWC"); // NOI18N
        remoteMethods.add("getList"); // NOI18N
        remoteMethods.add("getDirEntry"); // NOI18N
        remoteMethods.add("copy");  // NOI18N
        remoteMethods.add("remove"); // NOI18N
        remoteMethods.add("doExport"); // NOI18N
        remoteMethods.add("doImport"); // NOI18N
        remoteMethods.add("mkdir"); // NOI18N
        remoteMethods.add("move"); // NOI18N
        remoteMethods.add("update"); // NOI18N
        remoteMethods.add("getLogMessages"); // NOI18N
        remoteMethods.add("getContent"); // NOI18N
        remoteMethods.add("setRevProperty"); // NOI18N
        remoteMethods.add("diff"); // NOI18N
        remoteMethods.add("annotate"); // NOI18N
        remoteMethods.add("getInfo"); // NOI18N
        remoteMethods.add("switchToUrl"); // NOI18N
        remoteMethods.add("merge"); // NOI18N
        remoteMethods.add("lock"); // NOI18N
        remoteMethods.add("unlock"); // NOI18N        
    }

    private final ISVNClientAdapter adapter;
    private final SvnClientDescriptor desc;
    private Cancellable cancellable;
    private SvnProgressSupport support;
    private SvnWcParser wcParser = new SvnWcParser();

   /**
     *
     */
    public SvnClientInvocationHandler (ISVNClientAdapter adapter, SvnClientDescriptor desc) {
        this.adapter = adapter;
        this.desc = desc;
    }

    public SvnClientInvocationHandler (ISVNClientAdapter adapter, SvnClientDescriptor desc, SvnProgressSupport support) {
        this.adapter = adapter;
        this.desc = desc;
        this.support = support;
        this.cancellable = new Cancellable() {
            public boolean cancel() {
                try {
                    SvnClientInvocationHandler.this.adapter.cancelOperation();
                } catch (SVNClientException ex) {
                    ErrorManager.getDefault().notify(ex);
                    return false;
                }
                return true;
            }
        };
    }

    /**
     * @see InvocationHandler#invoke(Object proxy, Method method, Object[] args)
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {               

        assert noRemoteCallinAWT(method, args) : "noRemoteCallinAWT(): " + method.getName(); // NOI18N

        try {             
            if(SwingUtilities.isEventDispatchThread()) {
                return invokeMethod(method, args);    
            } else {
                synchronized (semaphor) {
                    return invokeMethod(method, args);    
                }
            }
        } catch (Exception e) {
            try {
                if(handleException((SvnClient) proxy, e) ) {
                    return invoke(proxy, method, args);
                } else {
                    // some action canceled by user message 
                    throw new SVNClientException(ExceptionHandler.ACTION_CANCELED_BY_USER); 
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
                        ExceptionHandler.handleInvalidKeyException(ike);
                    }
                    return null; 
                }
                throw ex;
            }
        }
    }
    
    private Object invokeMethod(Method proxyMethod, Object[] args)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        Object ret = null;        
        if (isHandledLocally(proxyMethod, args)) {
            try {
                ret = handleLocally(proxyMethod, args);
            } catch (LocalSubversionException ex) {
                //Exception thrown.  Call out to the default adapter
            }
        } else {
            ret = handleRemotely(proxyMethod, args);    
        }
        return ret;
    }

    private Object handleRemotely(final Method proxyMethod, final Object[] args) 
    throws SecurityException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException 
    {
        Object ret;

        Class[] parameters = proxyMethod.getParameterTypes();
        Class declaringClass = proxyMethod.getDeclaringClass();

        if( ISVNClientAdapter.class.isAssignableFrom(declaringClass) ) {
            // Cliet Adapter
            if(support != null) {
                support.setCancellableDelegate(cancellable);
            }
            ret = adapter.getClass().getMethod(proxyMethod.getName(), parameters).invoke(adapter, args);
            if(support != null) {
                support.setCancellableDelegate(null);
            }
        } else if( Cancellable.class.isAssignableFrom(declaringClass) ) {return 
            // Cancellable
            ret = cancellable.getClass().getMethod(proxyMethod.getName(), parameters).invoke(cancellable, args);
        } else if( SvnClientDescriptor.class.isAssignableFrom(declaringClass) ) {            
            // Client Descriptor
            ret = desc.getClass().getMethod(proxyMethod.getName(), parameters).invoke(desc, args);
        } else {
            // try to take care for hashCode, equals & co. -> fallback to clientadapter
            ret = adapter.getClass().getMethod(proxyMethod.getName(), parameters).invoke(adapter, args);
        }        
        return ret;
    }

    private static boolean isHandledLocally(Method method, Object[] args) {
        boolean exec = ISVNSTATUS_IMPL.equals("exec"); // NOI18N
        if(exec) {
            return false;
        }        
        
        String methodName = method.getName();
        return methodName.equals(GET_SINGLE_STATUS) || 
               methodName.equals(GET_INFO_FROM_WORKING_COPY) ||  
               (method.getName().equals(GET_STATUS) && method.getParameterTypes().length == 3); 
    }

    private Object handleLocally(Method method, Object[] args) throws LocalSubversionException {
        Object returnValue = null;

        if (GET_SINGLE_STATUS.equals(method.getName())) {
            returnValue = wcParser.getSingleStatus((File) args[0]);
        } else if (GET_INFO_FROM_WORKING_COPY.equals(method.getName())) {
            returnValue= wcParser.getInfoFromWorkingCopy((File) args[0]);
        } else if (GET_STATUS.equals(method.getName())) {
            returnValue= wcParser.getStatus(
                    (File) args[0], 
                    ((Boolean) args[1]).booleanValue(), 
                    ((Boolean) args[2]).booleanValue()
            );
        }
        return returnValue;
    }

   /**
     * @return false for methods that perform calls over network
     */
    private static boolean noRemoteCallinAWT(Method method, Object[] args) {
        if(!SwingUtilities.isEventDispatchThread()) {
            return true;
        }

        String name = method.getName();
        if (remoteMethods.contains(name)) {
            return false;
        } else if ("getStatus".equals(name)) { // NOI18N
            return args.length != 4 || (Boolean.TRUE.equals(args[3]) == false);
        }
        return true;
    }

    private boolean handleException(SvnClient client, Throwable t) throws Throwable {
        SVNClientException svnException = null;
        if( t instanceof InvocationTargetException ) {
            t = ((InvocationTargetException) t).getCause();            
        } 
        if( !(t instanceof SVNClientException) ) {
            throw t;
        }

        SvnClientExceptionHandler eh = new SvnClientExceptionHandler((SVNClientException) t, adapter, client);        
        return eh.handleException();        
    }

}

