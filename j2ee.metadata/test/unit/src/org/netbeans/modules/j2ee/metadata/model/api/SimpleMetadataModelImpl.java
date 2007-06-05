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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.metadata.model.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;

/**
 *
 * @author Andrei Badea
 */
public class SimpleMetadataModelImpl<T> implements MetadataModelImplementation<T> {

    private final boolean ready;
    
    public SimpleMetadataModelImpl() {
        this(true);
    }

    public SimpleMetadataModelImpl(boolean ready) {
        this.ready = ready;
    }

    public <R> R runReadAction(MetadataModelAction<T, R> action) throws IOException {
        try {
            return action.run(null);
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            } else {
                throw new MetadataModelException(t);
            }
        }
    }

    public boolean isReady() {
        return ready;
    }

    public <R> Future<R> runReadActionWhenReady(MetadataModelAction<T, R> action) throws IOException {
        if (ready) {
            try {
                return new SimpleFuture<R>(action.run(null), null);
            } catch (Throwable t) {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                } else {
                    throw new MetadataModelException(t);
                }
            }
        } else {
            R result = null;
            ExecutionException executionException = null;
            try {
                result = action.run(null);
            } catch (Throwable t) {
                executionException = new ExecutionException(t);
            }
            return new SimpleFuture<R>(result, executionException);
        }
    }

    private static final class SimpleFuture<R> implements Future<R> {

        private final R result;
        private final ExecutionException executionException;

        public SimpleFuture(R result, ExecutionException executionException) {
            this.result = result;
            this.executionException = executionException;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return true;
        }

        public R get() throws InterruptedException, ExecutionException {
            if (executionException != null) {
                throw executionException;
            }
            return result;
        }

        public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }
}
