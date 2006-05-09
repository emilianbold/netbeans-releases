/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.execution;

import java.security.*;

import org.openide.execution.NbClassLoader;

/**
 * A security manager for execution.
 * @author Jesse Glick
 */
public class SecMan extends SecurityManager {
    
    public static SecurityManager DEFAULT = new SecMan();
    
    private static Class nbClassLoaderClass = NbClassLoader.class;
    
    /** thread group of executed classes */
    private ThreadGroup base;

    public SecMan() {
        base = ExecutionEngine.base;
    }
    
    public void checkExit(int status) throws SecurityException {
        PrivilegedCheck.checkExit(status, this);
    }
    
    final void checkExitImpl(int status, AccessControlContext acc) throws SecurityException {
        IOPermissionCollection iopc;
        iopc = AccController.getIOPermissionCollection(acc);

        if (iopc != null && iopc.grp != null) {
            ExecutionEngine.getTaskIOs().free(iopc.grp, iopc.getIO()); // closes output
            ExecutionEngine.closeGroup(iopc.grp);
            stopTaskThreadGroup(iopc.grp);
            throw new ExitSecurityException("Exit from within execution engine, normal"); // NOI18N
        }

        ThreadGroup g = Thread.currentThread().getThreadGroup ();
        if (g instanceof TaskThreadGroup) {
            throw new ExitSecurityException("Exit from within execution engine, normal"); // NOI18N
        }
        
        if (isNbClassLoader()) {
            throw new ExitSecurityException("Exit from within user-loaded code"); // NOI18N
        }
    }
    
    private void stopTaskThreadGroup(TaskThreadGroup old) {
        synchronized(old) {
            int count = old.activeCount();
            int icurrent = -1;
            Thread current = Thread.currentThread();
            Thread[] thrs = new Thread[count];
            old.enumerate(thrs, true);
            for (int i = 0; i < thrs.length; i++) {
                if (thrs[i] == null) break;
                if (thrs[i] == current) icurrent = i;
                else thrs[i].stop();
            }
            if (icurrent != -1) thrs[icurrent].stop();
            //throw new ExitSecurityException();
        }
    }
    
    public boolean checkTopLevelWindow(Object window) {
        IOPermissionCollection iopc = AccController.getIOPermissionCollection();
        if (iopc != null && iopc.grp != null && (window instanceof java.awt.Window)) {
            ExecutionEngine.putWindow((java.awt.Window) window, iopc.grp);
        }
        return true;
    }

    /** @return true iff an instance of the NbClassLoader class is on the stack
    */
    protected boolean isNbClassLoader() {
        Class[] ctx = getClassContext();
        ClassLoader cloader;

        for (int i = 0; i < ctx.length; i++) {
            if ((nbClassLoaderClass.isInstance(ctx[i].getClassLoader())) &&
                (ctx[i].getProtectionDomain().getCodeSource() != null)) {
                return true;
            }
        }
        return false;
    }
    
    /** mostly copied from TopSecurityManager */
    private static final class PrivilegedCheck implements PrivilegedExceptionAction<Object> {
        private final int action;
        private final SecMan sm;
        
        // exit
        private int status;
        private AccessControlContext acc;

        public PrivilegedCheck(int action, SecMan sm) {
            this.action = action;
            this.sm = sm;
            
            if (action == 0) {
                acc = AccessController.getContext();
            }
        }
        
        public Object run() throws Exception {
            switch (action) {
                case 0 : 
                    sm.checkExitImpl(status, acc);
                    break;
                default :
            }
            return null;
        }
        
        static void checkExit(int status, SecMan sm) {
            PrivilegedCheck pea = new PrivilegedCheck(0, sm);
            pea.status = status;
            check(pea);
        }
        
        private static void check(PrivilegedCheck action) {
            try {
                AccessController.doPrivileged(action);
            } catch (PrivilegedActionException e) {
                Exception orig = e.getException();
                if (orig instanceof RuntimeException) {
                    throw ((RuntimeException) orig);
                }
                orig.printStackTrace();
            }
        }
    }
    
}
