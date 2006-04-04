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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import javax.swing.SwingUtilities;
import org.openide.ErrorManager;
import org.openide.util.Cancellable;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 *
 * @author Tomas Stupka
 * XXX polish
 */
public class SvnClientInvocationHandler implements InvocationHandler {

    private static Set remoteMethods = new HashSet();

    static {
        remoteMethods.add("checkout");  // NOI19N
        remoteMethods.add("commit"); // NOI19N
        remoteMethods.add("commitAcrossWC"); // NOI19N
        remoteMethods.add("getList"); // NOI19N
        remoteMethods.add("getDirEntry"); // NOI19N
        remoteMethods.add("copy"); // NOI19N
        remoteMethods.add("remove"); // NOI19N
        remoteMethods.add("doExport"); // NOI19N
        remoteMethods.add("doImport"); // NOI19N
        remoteMethods.add("mkdir"); // NOI19N
        remoteMethods.add("move"); // NOI19N
        remoteMethods.add("update"); // NOI19N
        remoteMethods.add("getLogMessages"); // NOI19N
        remoteMethods.add("getContent"); // NOI19N
        remoteMethods.add("setRevProperty"); // NOI19N
        remoteMethods.add("diff"); // NOI19N
        remoteMethods.add("annotate"); // NOI19N
        remoteMethods.add("getInfo"); // NOI19N
        remoteMethods.add("switchToUrl"); // NOI19N
        remoteMethods.add("merge"); // NOI19N
        remoteMethods.add("lock"); // NOI19N
        remoteMethods.add("unlock"); // NOI19N
    }

    private final ISVNClientAdapter adapter;
    private final SvnClientDescriptor desc;
    private Cancellable cancellable;
    private SvnProgressSupport support;

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

        assert noRemoteCallinAWT(method, args) : "noRemoteCallinAWT(): " + method.getName();

        try {             
            return invokeMethod(method, args);
        } catch (Exception e) {
            try {
                if(handleException((SvnClient) proxy, e) ) {
                    return invokeMethod(method, args);
                } else {
                    // XXX some action canceled by user message ... wrap the exception ???
                    throw new SVNClientException("Action canceled by user"); // XXX wrap me
                }
            } catch (InvocationTargetException ite) {
                Throwable t = ite.getTargetException();
                if(t instanceof SVNClientException) {
                    throw t;
                }
                throw ite;
            }
        }
    }

    private Object invokeMethod(Method proxyMethod, Object[] args)
    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        Object ret = null;

        // XXX refactor
        Class[] parameters = proxyMethod.getParameterTypes();
        Class clazz = null;
        Class declaringClass = proxyMethod.getDeclaringClass();

        if( ISVNClientAdapter.class.isAssignableFrom(declaringClass) ) {
            if(support != null) {
                support.setCancellableDelegate(cancellable);
            }
            ret = adapter.getClass().getMethod(proxyMethod.getName(), parameters).invoke(adapter, args);
            if(support != null) {
                support.setCancellableDelegate(null);
            }
        } else if( Cancellable.class.isAssignableFrom(declaringClass) ) {
            ret = cancellable.getClass().getMethod(proxyMethod.getName(), parameters).invoke(cancellable, args);
        } else if( SvnClientDescriptor.class.isAssignableFrom(declaringClass) ) {            
            ret = desc.getClass().getMethod(proxyMethod.getName(), parameters).invoke(desc, args);
        } else {
            // try to take care for hashCode, equals & co.
            ret = adapter.getClass().getMethod(proxyMethod.getName(), parameters).invoke(adapter, args);
        }        
        return ret;
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
        
        ExceptionHandler eh = new ExceptionHandler((SVNClientException) t, adapter, client);
        return eh.handleException();
    }
     
}
