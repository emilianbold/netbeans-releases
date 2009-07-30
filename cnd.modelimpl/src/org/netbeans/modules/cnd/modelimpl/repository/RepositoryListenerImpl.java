/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.modelimpl.repository;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.Timer;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.repository.api.*;
import org.netbeans.modules.cnd.repository.spi.*;

/**
 * RepositoryListener implementation.
 * Watches implicit and explicit opening of units;
 * ensures that implicitly opened units are closed 
 * after the specified interval has passed
 * @author Vladimir Kvashin
 */
public class RepositoryListenerImpl implements RepositoryListener {

    /** Singleton's instance */
    private static final RepositoryListenerImpl instance = new RepositoryListenerImpl();
    /** Interval, in seconds, after which implicitely opened unit should be closed */
    private static final int IMPLICIT_CLOSE_INTERVAL = Integer.getInteger("cnd.implicit.close.interval", 20); // NOI18N
    private static final String TRACE_PROJECT_NAME = System.getProperty("cnd.repository.trace.project"); //NOI18N    
    private static final boolean TRACE_PROJECT = (TRACE_PROJECT_NAME != null && TRACE_PROJECT_NAME.length() > 0);

    /** A shutdown hook to guarantee that repository is shutted down */
    private static class RepositoryShutdownHook extends Thread {

        public RepositoryShutdownHook() {
            setName("Repository Shutdown Hook Thread"); // NOI18N
        }

        @Override
        public void run() {
            RepositoryUtils.shutdown();
        }
    }

    /** 
     * A pair of (unit name, timer) 
     * used to track implicitely opened units
     */
    private class UnitTimer implements ActionListener {

        private final String unitName;
        private final Timer timer;

        public UnitTimer(String unitName, int interval) {
            this.unitName = unitName;
            timer = new Timer(interval, this);
            timer.start();
        }

        public void actionPerformed(ActionEvent e) {
            timeoutElapsed(unitName);
        }

        public void cancel() {
            timer.stop();
        }
    }
    /** Access both unitTimers and explicitelyOpened only under this lock! */
    private static final class Lock {}
    private final Object lock = new Lock();
    /** 
     * Implicitly opened units.
     * Access only under the lock!
     */
    private Map<String, UnitTimer> unitTimers = new HashMap<String, UnitTimer>();
    /** 
     * Explicitly opened units.
     * Access only under the lock!
     */
    private Set<String> explicitelyOpened = new HashSet<String>();

    private RepositoryListenerImpl() {
        Runtime.getRuntime().addShutdownHook(new RepositoryShutdownHook());
    }

    /** Singleton's getter */
    public static RepositoryListenerImpl instance() {
        return instance;
    }

    /** RepositoryListener implementation */
    public boolean unitOpened(final String unitName) {
        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
            trace("RepositoryListener: unitOpened %s\n", unitName); // NOI18N
        }
        if (TRACE_PROJECT && TRACE_PROJECT_NAME.equals(unitName)) {
            trace("Watched project %s is opening\n", unitName); // NOI18N
        }
        synchronized (lock) {
            if (!explicitelyOpened.contains(unitName)) {
                if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
                    trace("RepositoryListener: implicit open !!! %s\n", unitName); // NOI18N
                }
                unitTimers.put(unitName, new UnitTimer(unitName, IMPLICIT_CLOSE_INTERVAL * 1000));
            }
        }
        return true;
    }

    /** RepositoryListener implementation */
    public void unitClosed(final String unitName) {
        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
            trace("RepositoryListener: unitClosed %s\n", unitName); // NOI18N
        }
        if (TRACE_PROJECT && TRACE_PROJECT_NAME.equals(unitName)) {
            trace("Watched project %s is explicitly closing\n", unitName); // NOI18N
        }
        synchronized (lock) {
            killTimer(unitName);
            explicitelyOpened.remove(unitName);
        }
    }

    /** RepositoryListener implementation */
    public void anExceptionHappened(final String unitName, RepositoryException exc) {
        assert exc != null;
        if (exc.getCause() != null) {
            exc.getCause().printStackTrace(System.err);
        }
        DiagnosticExceptoins.register(exc.getCause());
    }

    // NB: un-synchronized!
    private void killTimer(String unitName) {
        UnitTimer unitTimer = unitTimers.remove(unitName);
        if (unitTimer != null) {
            if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
                trace("RepositoryListener: killing timer for %s\n", unitName); // NOI18N
            }
            unitTimer.cancel();
        }
    }

    public void onExplicitOpen(String unitName) {
        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
            trace("RepositoryListener: onExplicitOpen %s\n", unitName); // NOI18N
        }
        synchronized (lock) {
            killTimer(unitName);
            explicitelyOpened.add(unitName);
        }
    }

    public void onExplicitClose(String unitName) {
        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
            trace("RepositoryListener: onExplicitClose %s\n", unitName); // NOI18N
        }
    }

    private void timeoutElapsed(String unitName) {
        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
            trace("RepositoryListener: timeout elapsed for %s\n", unitName); // NOI18N
        }
        synchronized (lock) {
            UnitTimer unitTimer = unitTimers.remove(unitName);
            if (unitTimer != null) {
                if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
                    trace("RepositoryListener: scheduling closure for %s\n", unitName); // NOI18N
                }
                unitTimer.cancel();
                scheduleClosing(unitName, Collections.<String>emptySet());
            }
        }
    }

    private void scheduleClosing(final String unitName, final Set<String> requiredUnits) {
        if (explicitelyOpened.contains(unitName)) {
            if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
                trace("Cancelling closure (A) for implicitely opened unit %s\n", unitName); // NOI18N
            }
            return;
        }
        ModelImpl.instance().enqueueModelTask(new Runnable() {

            public void run() {
                synchronized (lock) {
                    if (explicitelyOpened.contains(unitName)) {
                        if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
                            trace("Cancelling closure (B) for implicitely opened unit %s\n", unitName); // NOI18N
                        }
                        return;
                    }
                }
                if (TraceFlags.TRACE_REPOSITORY_LISTENER) {
                    trace("RepositoryListener: closing implicitely opened unit %s\n", unitName); // NOI18N
                }
                if (TRACE_PROJECT && TRACE_PROJECT_NAME.equals(unitName)) {
                    trace("Watched project %s is implicitely closing\n", unitName); // NOI18N
                }
                RepositoryUtils.closeUnit(unitName, null, !TraceFlags.PERSISTENT_REPOSITORY); // null means the list of required units stays unchanged
            }
        }, "Closing implicitly opened project"); // NOI18N
    }

    private void trace(String format, Object... args) {
        Object[] newArgs = new Object[args.length + 1];
        newArgs[0] = Long.valueOf(System.currentTimeMillis());
        for (int i = 0; i < args.length; i++) {
            newArgs[i + 1] = args[i];
        }
        System.err.printf("RepositoryListener [%d] " + format, newArgs); // NOI18N
    }
}
