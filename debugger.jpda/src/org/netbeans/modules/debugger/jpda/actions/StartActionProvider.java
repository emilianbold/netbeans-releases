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
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;


/**
*
* @author   Jan Jancura
*/
public class StartActionProvider extends ActionsProvider implements Cancellable {
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
    private Thread startingThread;
    
    
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
        
        
        debuggerImpl.setStarting ();
        final AbstractDICookie cookie = (AbstractDICookie) lookupProvider.
            lookupFirst (null, AbstractDICookie.class);
        doStartDebugger(cookie);
        if (startVerbose)
            System.out.println ("\nS StartActionProvider." +
                "doAction () setStarting"
            );
        if (startVerbose)
            System.out.println ("\nS StartActionProvider." +
                "doAction () end"
            );
    }
    
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        if (startVerbose)
            System.out.println ("\nS StartActionProvider.postAction ()");
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        if ( debugger != null && 
             debugger.getVirtualMachine () != null
        ) {
            actionPerformedNotifier.run();
            return;
        }
        
        
        final AbstractDICookie cookie = (AbstractDICookie) lookupProvider.
            lookupFirst (null, AbstractDICookie.class);
        
        if (startVerbose)
            System.out.println ("\nS StartActionProvider." +
                "postAction () setStarting"
            );
        debuggerImpl.setStarting ();  // JS
        if (startVerbose)
            System.out.println ("\nS StartActionProvider." +
                "postAction () setStarting end"
            );
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                //debuggerImpl.setStartingThread(Thread.currentThread());
                synchronized (StartActionProvider.this) {
                    startingThread = Thread.currentThread();
                }
                try {
                    doStartDebugger(cookie);
                //} catch (InterruptedException iex) {
                    // We've interrupted ourselves
                } finally {
                    synchronized (StartActionProvider.this) {
                        startingThread = null;
                    }
                    //debuggerImpl.unsetStartingThread();
                    actionPerformedNotifier.run();
                }
                
            }
        });
        
    }
    
    private void doStartDebugger(AbstractDICookie cookie) {
        if (startVerbose)
            System.out.println ("\nS StartActionProvider." +
                "doAction ().thread"
            );
        Exception exception = null;
        try {
            VirtualMachine virtualMachine = cookie.getVirtualMachine ();
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
//            if (cookie instanceof AttachingDICookie) {
//                synchronized (debuggerImpl.LOCK) {
//                    virtualMachine.resume ();
//                }
//            }
            // PATCHEND Hanz

            if (startVerbose)
                System.out.println ("\nS StartActionProvider." +
                    "doAction ().thread end: success"
                );
        } catch (InterruptedException iex) {
            exception = iex;
        } catch (IOException ioex) {
            exception = ioex;
        } catch (Exception ex) {
            exception = ex;
            // Notify! Otherwise bugs in the code can not be located!!!
            ErrorManager.getDefault().notify(ex);
        }
        if (exception != null) {
            if (startVerbose)
                System.out.println ("\nS StartActionProvider." +
                    "doAction ().thread end: exception " + exception
                );
            debuggerImpl.setException (exception);
            // kill the session that did not start properly
            final Session session = (Session) lookupProvider.lookupFirst(null, Session.class);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    // Kill it in a separate thread so that the startup sequence can be finished.
                    session.kill();
                }
            });
        }
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
            debuggerImpl,
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
                    debuggerImpl.finish();
                }
            },
            debuggerImpl.LOCK
        );
    }

    public boolean cancel() {
        synchronized (StartActionProvider.this) {
            if (startingThread != null) {
                startingThread.interrupt();
            } else {
                return true;
            }
        }
        return true;
    }
}
