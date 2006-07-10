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

package threaddemo.locking;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Worker thread (off-AWT) that can run tasks asynch.
 * Convenience wrapper for {@link ExecutorService}.
 * @author Jesse Glick
 */
public final class Worker {

    private static final ExecutorService POOL = Executors.newCachedThreadPool();

    private Worker() {}

    /**
     * Start a task.
     * It will be run soon.
     * At most one task will be run at a time.
     */
    public static void start(Runnable run) {
        POOL.submit(run);
    }
    
    /**
     * Do something and wait for it to finish.
     */
    public static <T> T block(final LockAction<T> act) {
        Future<T> f = POOL.submit(new Callable<T>() {
            public T call() {
                return act.run();
            }
        });
        try {
            return f.get();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw (RuntimeException) t;
            }
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
    
    /**
     * Do something and wait for it to finish.
     * May throw exceptions.
     */
    public static <T, E extends Exception> T block(final LockExceptionAction<T,E> act) throws E {
        Future<T> f = POOL.submit(new Callable<T>() {
            public T call() throws Exception {
                return act.run();
            }
        });
        try {
            return f.get();
        } catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof Error) {
                throw (Error) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                @SuppressWarnings("unchecked")
                E _e = (E) e;
                throw _e;
            }
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
    
}
