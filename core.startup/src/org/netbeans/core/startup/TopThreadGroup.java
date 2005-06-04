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

package org.netbeans.core.startup;


import org.openide.ErrorManager;

/** The ThreadGroup for catching uncaught exceptions in Corona.
*
* @author   Ian Formanek
*/
final class TopThreadGroup extends ThreadGroup implements Runnable {
    /** The command line args */
    private String[] args;
    /** flag that indicates whether the main thread had finished or not */
    private boolean finished;
        
    /** Constructs a new thread group. The parent of this new group is
    * the thread group of the currently running thread.
    *
    * @param name the name of the new thread group.
    */
    public TopThreadGroup(String name, String[] args) {
        super(name);
        this.args = args;
    }

    /** Creates a new thread group. The parent of this new group is the
    * specified thread group.
    * <p>
    * The <code>checkAccess</code> method of the parent thread group is
    * called with no arguments; this may result in a security exception.
    *
    * @param parent the parent thread group.
    * @param name the name of the new thread group.
    * @exception  NullPointerException  if the thread group argument is
    *             <code>null</code>.
    * @exception  SecurityException  if the current thread cannot create a
    *             thread in the specified thread group.
    * @see java.lang.SecurityException
    * @see java.lang.ThreadGroup#checkAccess()
    */
    public TopThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);
    }

    public void uncaughtException(Thread t, Throwable e) {
        if (!(e instanceof ThreadDeath)
            // XXX(-ttran) in Sun JDK 1.3.1_01 Linux (at least) there is a bug
            // which can cause this exception to be thrown from a helper
            // AWT-Selection thread
            // XXX what bug #? fixed in 1.4 or not? -jglick
            && !(e instanceof IllegalMonitorStateException &&
                 "AWT-Selection".equals(t.getName())) // NOI18N
            ) {
            if (e instanceof VirtualMachineError) {
                // Try as hard as possible to get a stack trace from e.g. StackOverflowError
                e.printStackTrace();
            }
            System.err.flush();
            ErrorManager.getDefault().notify(e);
        }
        else {
            super.uncaughtException(t, e);
        }
    }
    
    public synchronized void start () throws InterruptedException {
        Thread t = new Thread (this, this, "main"); // NOI18N
        t.start ();
        
        while (!finished) {
            wait ();
        }
    }

    public void run() {
        try {
            Main.start (args);
        } catch (Throwable t) {
            // XXX is this not handled by uncaughtException?
            t.printStackTrace();
            ErrorManager.getDefault().notify(t);
        } finally {
            synchronized (this) {
                finished = true;
                notify ();
            }
        }
    }
}
