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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.ErrorManager;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 * Runnable that actually performs a command and stores
 * an exception it may throw and its isFinished state.
 *
 * @author Maros Sandor
 */
class CommandRunnable implements Runnable, Cancellable {

    private final Client        client;
    private final GlobalOptions options;
    private final Command       cmd;
    private Throwable           failure;
    
    private boolean             aborted;
    private Thread              interruptibleThread;
    private ExecutorSupport     support;

    private static boolean      testRetry = Boolean.getBoolean("netbeans.debug.cvs.io.retry");  // NOI18N

    public CommandRunnable(Client client, GlobalOptions options, Command cmd, ExecutorSupport support) {
        this.client = client;
        this.options = options;
        this.cmd = cmd;
        this.support = support;
    }

    public void run() {
        synchronized(this) {
            if (isAborted()) {
                return;
            }
            support.commandStarted(this);
        }
        interruptibleThread = Thread.currentThread();
        Runnable worker = new Runnable() {
            public void run() {
                CounterRunnable counterUpdater = new CounterRunnable();
                RequestProcessor.Task counterTask = RequestProcessor.getDefault().create(counterUpdater);
                counterUpdater.initTask(counterTask);
                try {
                    counterTask.schedule(500);
                    if (testRetry && support.t9yRetryFlag == false) {
                        support.t9yRetryFlag = true;
                        String msg = "Testing retry logic. Retry attempt will be OK. (-Dnetbeans.debug.cvs.io.retry=true)"; // NOI18N
                        throw new AuthenticationException(msg, msg);
                    }
                    client.executeCommand(cmd, options);
                } catch (Throwable e) {
                    failure = e;
                } finally {
                    counterTask.cancel();
                    try {
                        client.getConnection().close();
                    } catch (Throwable e) {
                        ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                    }
                }
            }
        };
        Thread workerThread = new Thread(worker, "CVS I/O Worker ");  // NOI18N
        workerThread.start();
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Passing interrupt to possibly uninterruptible nested thread: " + workerThread + "\nCVS command: " + cmd.getCVSCommand());  // NOI18N
            workerThread.interrupt(); // sometimes not interuptible e.g. while in Socket.connect()
            err.notify(ErrorManager.INFORMATIONAL, e);
        }
    }

    public Throwable getFailure() {
        return failure;
    }
    
    /**
     * Cancelled?
     */
    public synchronized boolean isAborted() {
        return aborted;
    }

    public synchronized boolean cancel() {
        if (aborted) {
            return false;
        }
        aborted = true;
        client.abort();
        if (interruptibleThread != null) {
            interruptibleThread.interrupt();  // waiting in join
        }
        return true;
    }

    public String toString() {
        return "CommandRunnable command=" + cmd.getCVSCommand();  // NOI18N
    }

    /** Periodic task updating transmitted/received data counter. */
    private class CounterRunnable implements Runnable {

        private RequestProcessor.Task task;

        private long counter;

        public void run() {
            long current = client.getCounter();
            long delta = current - counter;
            counter = current;
            support.increaseDataCounter(delta);
            task.schedule(500);
        }

        void initTask(RequestProcessor.Task task) {
            this.task = task;
        }
    }
}
