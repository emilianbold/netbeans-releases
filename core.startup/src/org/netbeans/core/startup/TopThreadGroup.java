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

package org.netbeans.core.startup;

import org.openide.util.Exceptions;

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
        if (!(e instanceof ThreadDeath)) {
            if (e instanceof VirtualMachineError) {
                // Try as hard as possible to get a stack trace from e.g. StackOverflowError
                e.printStackTrace();
            }
            System.err.flush();
            Exceptions.printStackTrace(e);
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
            Exceptions.printStackTrace(t);
            // System is probably broken, so don't just sit there with the splash screen open.
            try {
                Thread.sleep(10000);
            } catch (InterruptedException x) {
                Exceptions.printStackTrace(x);
            }
            System.exit(2);
        } finally {
            synchronized (this) {
                finished = true;
                notify ();
            }
        }
    }
}
