/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.test.editor.app.util;

import java.util.ArrayList;
import javax.swing.SwingUtilities;
/**
 *
 * @author  jlahoda
 * @version
 */
public class Scheduler extends Thread {
    
    private static Object synchronizeTo = new Object();
    private static boolean allowedToCreate = true;
    private ArrayList runnables; /*Of Runable's*/
    private ArrayList run; /*Of Runable's*/    
    private static Scheduler scheduler = null;
    private boolean shouldFinish = false;
    
    private static boolean superSafe = false;
    private static boolean superSafeChanged = false;
    
    public static boolean getSuperSafe() {
        return superSafe;
    }
    
    public static void setSuperSafe(boolean newState) {
        if (superSafeChanged)
            return;
        superSafe = newState;
        superSafeChanged = true;
    }
    
    public static synchronized Scheduler getDefault() {
        if (scheduler == null /*&& allowedToCreate*/) {
            new Scheduler();
            scheduler.start();
        }
        return scheduler;
    }
    
    public static void finishScheduler() {
        if (scheduler == null) {
/*	    synchronized (synchronizeTo) {
                if (scheduler == null) {
                    allowedToCreate = false;
                }
            }*/
        } else {
            getDefault().finish();
        }
    }
    
    /** Creates new Scheduler */
    protected Scheduler() {
        runnables = new ArrayList();
        run = new ArrayList();
        scheduler = this;
    }
    
    public void addTask(Runnable what) {
        synchronized (runnables) {
            runnables.add(what);
        }
    }
    
    public void finish() {
        shouldFinish = true;
        //	allowedToCreate = false;
    }
    
    public void run() {
        boolean finish = false;
        
        while (!finish) {
            while (!shouldFinish || (runnables.size() > 0)) {
                if (runnables.size() > 0) {
                    Runnable toRun = null;
                    synchronized (runnables) {
                        toRun = (Runnable)runnables.get(0);
                        runnables.remove(0);
                    }
                    if (superSafe) {
                        SwingUtilities.invokeLater(toRun);
                    } else {
                        try {
                            SwingUtilities.invokeAndWait(toRun);
                        } catch (java.lang.InterruptedException e) {
                            System.err.println("Unexpected interrupt of invokeAndWait(): " + e);
                            e.printStackTrace(System.err);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            System.err.println("Unexpected exception in invokeAndWait: " + e);
                            e.printStackTrace(System.err);
                        }
                    }
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        System.err.println("Interrupted.");
                    }
                } else {
                    //yield();
                    try {
                    sleep(200);
                    } catch (Exception ex) {
                    }
                }
            }
            synchronized (synchronizeTo) {
                if (runnables.size() > 0) {
                    finish = false;
                } else {
                    finish = true;
                    scheduler = null;
                }
            }
        }
    }    
}
