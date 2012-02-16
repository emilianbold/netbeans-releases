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
package org.netbeans.modules.parsing.impl.indexing;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Tomas Zezula
 */
final class SuspendSupport implements SuspendStatus {

    private static final Logger LOG = Logger.getLogger(SuspendSupport.class.getName());
    private static final boolean NO_SUSPEND = Boolean.getBoolean("SuspendSupport.disabled");    //NOI18N

    private final RequestProcessor worker;
    private final Object lock = new Object();
    private final ThreadLocal<Boolean> ignoreSuspend = new ThreadLocal<Boolean>();
    //@GuardedBy("lock")
    private int suspedDepth;

    SuspendSupport(@NonNull final RequestProcessor rp) {
        Parameters.notNull("rp", rp);   //NOI18N
        this.worker = rp;
    }

    public void suspend() {
        if (NO_SUSPEND) {
            return;
        }
        if (worker.isRequestProcessorThread()) {
            return;
        }
        synchronized(lock) {
            suspedDepth++;
            if (LOG.isLoggable(Level.FINE) && suspedDepth == 1) {
                LOG.log(
                    Level.FINE,
                    "SUSPEND: {0}", //NOI18N
                    Arrays.toString(Thread.currentThread().getStackTrace()));
            }
        }
    }

    public void resume() {
        if (NO_SUSPEND) {
            return;
        }
        if (worker.isRequestProcessorThread()) {
            return;
        }
        synchronized(lock) {
            assert suspedDepth > 0;
            suspedDepth--;
            if (suspedDepth == 0) {
                lock.notifyAll();
                LOG.fine("RESUME"); //NOI18N
            }
        }
    }
    
    public void runWithNoSuspend(final Runnable work) {
        ignoreSuspend.set(Boolean.TRUE);
        try {
            work.run();
        } finally {
            ignoreSuspend.remove();
        }
    }

    @Override
    public boolean isSuspended() {
        if (ignoreSuspend.get() == Boolean.TRUE) {
            return false;
        }
        synchronized(lock) {
            return suspedDepth > 0;
        }
    }

    @Override
    public void parkWhileSuspended() throws InterruptedException {
        if (ignoreSuspend.get() == Boolean.TRUE) {
            return;
        }
        synchronized(lock) {
            boolean parked = false;
            while (suspedDepth > 0) {
                LOG.fine("PARK");   //NOI18N
                lock.wait();
                parked = true;
            }
            if (LOG.isLoggable(Level.FINE) && parked) {
                LOG.fine("UNPARK");   //NOI18N
            }
        }
    }
    
    static final SuspendStatus NOP = new SuspendStatus () {
        @Override
        public boolean isSuspended() {
            return false;
        }
        @Override
        public void parkWhileSuspended() throws InterruptedException {
        }
    };

}
