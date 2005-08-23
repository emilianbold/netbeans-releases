/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import java.io.IOException;

import java.util.Collections;

import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.api.debugger.jpda.AttachingDICookie;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AbstractDICookie;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.util.Operator;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;
import org.openide.ErrorManager;


/**
*
* @author   Jan Jancura
*/
public class StartActionProvider extends ActionsProvider {
//    private static transient String []        stopMethodNames = 
//        {"main", "start", "init", "<init>"}; // NOI18N
    
    private static final boolean startVerbose = 
        System.getProperty ("netbeans.debugger.start") != null;
    private static int jdiTrace;
    static { 
        if (System.getProperty ("netbeans.debugger.jditrace") != null) {
            try {
                jdiTrace = Integer.parseInt (
                    System.getProperty ("netbeans.debugger.jditrace")
                );
            } catch (NumberFormatException ex) {
                jdiTrace = VirtualMachine.TRACE_NONE;
            }
        } else
            jdiTrace = VirtualMachine.TRACE_NONE;
    }

    private JPDADebuggerImpl debuggerImpl;
    private ContextProvider lookupProvider;
    
    
    public StartActionProvider (ContextProvider lookupProvider) {
        debuggerImpl = (JPDADebuggerImpl) lookupProvider.lookupFirst
            (null, JPDADebugger.class);
        this.lookupProvider = lookupProvider;
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_START);
    }
    
    public void doAction (Object action) {
        if (startVerbose)
            System.out.println ("\nS StartActionProvider.doAction ()");
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        if ( debugger != null && 
             debugger.getVirtualMachine () != null
        ) return;
        final AbstractDICookie cookie = (AbstractDICookie) lookupProvider.
            lookupFirst (null, AbstractDICookie.class);
        
        if (startVerbose)
            System.out.println ("\nS StartActionProvider." +
                "doAction () setStarting"
            );
        debuggerImpl.setStarting (null);
        // This is a synchronous action, the following code can be
        // asynchronous in postAction, if it's necessary:
        /*
        Thread startingThread = new Thread (
            new Runnable () {
                public void run () {
         */
        if (startVerbose)
            System.out.println ("\nS StartActionProvider." +
                "doAction ().thread"
            );
        try {
            VirtualMachine virtualMachine = cookie.
                getVirtualMachine ();
            virtualMachine.setDebugTraceMode (jdiTrace);

            final Object startLock = new Object();
            Operator o = createOperator (virtualMachine, startLock);
            synchronized (startLock) {
                if (startVerbose) System.out.println (
                        "\nS StartActionProvider.doAction () - " +
                        "starting operator thread"
                    );
                o.start ();
                if (cookie instanceof ListeningDICookie) 
                    startLock.wait(1500);
            }
            debuggerImpl.setRunning (
                virtualMachine,
                o
            );

            // PATCH #46295 JSP breakpoint isn't reached during 
            // second debugging
            if (cookie instanceof AttachingDICookie) {
                synchronized (debuggerImpl.LOCK) {
                    virtualMachine.resume ();
                }
            }
            // PATCHEND Hanz

            if (startVerbose)
                System.out.println ("\nS StartActionProvider." +
                    "doAction ().thread end: success"
                );
        } catch (IOException ioex) {
            if (startVerbose)
                System.out.println ("\nS StartActionProvider." +
                    "doAction ().thread end: exception " + ioex
                );
            debuggerImpl.setException (ioex);
            ((Session) lookupProvider.lookupFirst 
                (null, Session.class)).kill ();
        } catch (Exception ex) {
            if (startVerbose)
                System.out.println ("\nS StartActionProvider." +
                    "doAction ().thread end: exception " + ex
                );
            debuggerImpl.setException (ex);
            // Notify! Otherwise bugs in the code can not be located!!!
            ErrorManager.getDefault().notify(ex);
            ((Session) lookupProvider.lookupFirst 
                (null, Session.class)).kill ();
        }
        /*
                }
            },
            "Debugger start"
        );
         */
        //startingThread.start ();
        if (startVerbose)
            System.out.println ("\nS StartActionProvider." +
                "doAction () end"
            );
    }

    public boolean isEnabled (Object action) {
        return true;
    }

    public void addActionsProviderListener (ActionsProviderListener l) {}
    public void removeActionsProviderListener (ActionsProviderListener l) {}
    
    private Operator createOperator (
        VirtualMachine virtualMachine,
        final Object startLock
    ) {
        return new Operator (
            virtualMachine,
            new Executor () {
                public boolean exec(Event event) {
                    synchronized(startLock) {
                        startLock.notify();
                    }
                    return false;
                }
            },
            new Runnable () {
                public void run () {
                    ((Session) lookupProvider.lookupFirst 
                        (null, Session.class)).kill ();
                }
            },
            debuggerImpl.LOCK
        );
    }
}
