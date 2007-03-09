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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.util.*;
import javax.net.ssl.SSLKeyException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
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
    
    private static Set<String> remoteMethods = new HashSet<String>();    
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
    
    private static Object semaphor = new Object();        

    private final ISVNClientAdapter adapter;
    private final SvnClientDescriptor desc;
    private Cancellable cancellable;
    private SvnProgressSupport support;
    private final int handledExceptions; 
    
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
        
        String methodName = method.getName();
        assert noRemoteCallinAWT(methodName, args) : "noRemoteCallinAWT(): " + methodName; // NOI18N

        try {      
            Object ret = null;        
            if(SwingUtilities.isEventDispatchThread()) {
                ret = invokeMethod(method, args);    
            } else {
                synchronized (semaphor) {
                    ret = invokeMethod(method, args);    
                }
            }            
            Subversion.getInstance().getStatusCache().refreshDirtyFileSystems();
            return ret;
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

        if( ISVNClientAdapter.class.isAssignableFrom(declaringClass) ) {
            // Cliet Adapter
            if(support != null) {
                support.setCancellableDelegate(cancellable);
            }
            if (remoteMethods.contains(proxyMethod.getName())) {
                // save the proxy settings into the svn servers file
                SvnConfigFiles.getInstance().setProxy(desc.getSvnUrl().toString());
            }
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
        SVNClientException svnException = null;
        if( t instanceof InvocationTargetException ) {
            t = ((InvocationTargetException) t).getCause();            
        } 
        if( !(t instanceof SVNClientException) ) {
            throw t;
        }

        SvnClientExceptionHandler eh = new SvnClientExceptionHandler((SVNClientException) t, adapter, client, handledExceptions);        
        return eh.handleException();        
    }
    
   /**
     * @return false for methods that perform calls over network
     */
    protected boolean noRemoteCallinAWT(String methodName, Object[] args) {
        if(!SwingUtilities.isEventDispatchThread()) {
            return true;
        }

        if (remoteMethods.contains(methodName)) {
            return false;
        } 
        return true;
    }
}

