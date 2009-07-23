/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.spi.indicator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.dlight.util.DLightExecutorService;

/**
 *
 * @author mt154047
 */
final class IndicatorTickerService {

    private static final IndicatorTickerService instance = new IndicatorTickerService();
    private Future tickerService;
    private boolean started = false;
    private static final class Lock{};
    private final Object lock = new Lock();
    private final Object listenersLock = new Object();
    private final List<TickerListener> listeners = new ArrayList<TickerListener>();

    private IndicatorTickerService() {
    }

    static IndicatorTickerService getInstance() {
        synchronized(instance.lock){
            if (!instance.started){
                instance.startIfNeed();
            }
        }
        return instance;
    }

    void notifyListeners() {
        TickerListener[] ll;

        synchronized (listenersLock) {
            ll = listeners.toArray(new TickerListener[0]);
            if (ll.length == 0){
                return;
            }
        }

        final CountDownLatch doneFlag = new CountDownLatch(ll.length);

        // Will do notification in parallel, but wait until all listeners
        // finish processing of event.
        for (final TickerListener l : ll) {
            DLightExecutorService.submit(new Runnable() {

                public void run() {
                    try {
                        l.tick();
                    } finally {
                        doneFlag.countDown();
                    }
                }
            }, "Notifying " + l); // NOI18N
        }

        try {
            doneFlag.await();
        } catch (InterruptedException ex) {
        }
    }

    void startIfNeed() {
        synchronized (lock) {
            if (!started) {
                tickerService = DLightExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        public void run() {
                            notifyListeners() ;
                        }
                    }, 1, TimeUnit.SECONDS, "IndicatorTickerService"); // NOI18N
                    started = true;
            }
        }

    }

    void subsribe(TickerListener l) {
        synchronized (listenersLock) {
            if (!listeners.contains(l)) {
                listeners.add(l);
            }
        }
    }

    void unsubscribe(TickerListener l) {
        synchronized (listenersLock) {
            listeners.remove(l);
            //stop of there are no any listeners
            if (listeners.isEmpty()){
                synchronized(lock){
                    if (started){
                        tickerService.cancel(true);
                        started = false;
                    }
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        synchronized(lock){
            if (started){
                tickerService.cancel(true);
            }
        }
    }


}
