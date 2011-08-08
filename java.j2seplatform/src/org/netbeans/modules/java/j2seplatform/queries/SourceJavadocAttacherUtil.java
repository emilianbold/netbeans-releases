/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seplatform.queries;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation.Result;

/**
 *
 * @author Tomas Zezula
 */
public final class SourceJavadocAttacherUtil {

    private SourceJavadocAttacherUtil() {}

    @NonNull
    public static final Future<Result> scheduleInEDT(
        @NonNull final Callable<Result> call) {
        assert call != null;
        final Future<Result> res;
        if (SwingUtilities.isEventDispatchThread()) {
            res = new Now(call);
        } else {
            final FutureTask<Result> ft =
                    new FutureTask<Result>(call);
            SwingUtilities.invokeLater(ft);
            res = ft;
        }
        return res;
    }

    @NonNull
    public static final Future<Result> resultAsFuture(
        @NonNull final Result result) {
        assert result != null;
        return new Fixed(result);
    }

    private static class Now implements Future<Result> {

        private final Callable<Result> call;
        private final AtomicReference<Result> result =
                new AtomicReference<Result>();
        private volatile boolean canceled;

        private Now(@NonNull final Callable<Result> call) {
            assert  call != null;
            this.call = call;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            canceled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }

        @Override
        public boolean isDone() {
            return result.get() != null;
        }

        @Override
        public Result get() throws InterruptedException, ExecutionException {
            if (canceled) {
                throw new CancellationException();
            }
            Result res = result.get();
            if (res != null) {
                return res;
            }
            try {
                res = call.call();
                result.compareAndSet(null, res);
                return result.get();
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        }

        @Override
        public Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }

    private static class Fixed implements Future<Result> {

        private final Result result;
        private volatile boolean canceled;

        private Fixed(@NonNull final Result result) {
            assert result != null;
            this.result = result;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            canceled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public Result get() throws InterruptedException, ExecutionException {
            return result;
        }

        @Override
        public Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }

    }
}
