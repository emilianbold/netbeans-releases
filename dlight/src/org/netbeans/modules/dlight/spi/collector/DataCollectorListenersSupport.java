/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.spi.collector;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import org.netbeans.modules.dlight.util.DLightExecutorService;

/**
 * A supporting class for DataCollectorListeners notifications
 * 
 * @author ak119685
 */
public final class DataCollectorListenersSupport {

    private final DataCollector<?> collector;
    private final CopyOnWriteArrayList<DataCollectorListener> listeners =
            new CopyOnWriteArrayList<DataCollectorListener>();
    private final Strategy strategy;

    /**
     * Creates new instance of <code>DataCollectorListenersSupport</code> with a
     * <code>SERIAL</code> notification strategy
     * 
     * @param collector source of notifications
     */
    public DataCollectorListenersSupport(DataCollector<?> collector) {
        this(collector, Strategy.SERIAL);
    }

    /**
     * Creates new instance of <code>DataCollectorListenersSupport</code> with a
     * notification strategy specified by the <code>notificationStrategy</code>.
     *
     * @param collector source of notifications
     * @param notificationStrategy a notification strategy to use
     */
    public DataCollectorListenersSupport(DataCollector<?> collector, Strategy notificationStrategy) {
        this.collector = collector;
        this.strategy = notificationStrategy;
    }

    /**
     * Adds a <code>DataCollectorListener</code> to the listener list. If
     * listener list already contains the listener passed, no action is taken.
     * If <code>listener</code> is null, no exception is thrown and no action
     * is taken.
     *
     * @param listener the <code>DataCollectorListener</code> to be added.
     */
    public void addListener(DataCollectorListener listener) {
        if (listener != null) {
            listeners.addIfAbsent(listener);
        }
    }

    /**
     * Removes a <code>DataCollectorListener</code> from the listener list.
     * If <code>listener</code> is null, or was never added, no exception is
     * thrown and no action is taken.
     *
     * @param listener the <code>DataCollectorListener</code> to be removed.
     */
    public void removeListener(DataCollectorListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Notifies registered listeners with a new state of the collector.
     * Notification is performed in a way defined while this support construction.
     *
     * Not thread safe (if called from different threads simultaniously, the
     * order of notification is not guaranteed)
     * 
     * @param state a new state of DataCollector
     */
    public void notifyListeners(final DataCollector.CollectorState state) {
        switch (strategy) {
            case SERIAL:
                notifyListenersSerially(state);
                break;
            case CONCURRENT:
                notifyListenersConcurrently(state);
                break;
        }
    }

    /**
     * Serial notification strategy implementation
     * @param state a new state of DataCollector
     */
    private synchronized  void notifyListenersSerially(final DataCollector.CollectorState state) {
        for (DataCollectorListener l : listeners) {
            l.collectorStateChanged(collector, state);
        }
    }

    /**
     * Concurrent notification strategy implementation
     * @param state a new state of DataCollector
     */
    private void notifyListenersConcurrently(final DataCollector.CollectorState state) {
        CopyOnWriteArrayList<DataCollectorListener> clone = (CopyOnWriteArrayList<DataCollectorListener>) listeners.clone();

        final CountDownLatch doneFlag = new CountDownLatch(clone.size());

        // Will do notification in parallel, but wait until all listeners
        // finish processing of event.
        for (final DataCollectorListener l : clone) {
            DLightExecutorService.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        l.collectorStateChanged(collector, state);
                    } finally {
                        doneFlag.countDown();
                    }
                }
            }, "Notifying " + l); // NOI18N
        }

        try {
            doneFlag.await();
        } catch (InterruptedException ex) {
            // TODO: notification tasks still exist at this point.
            // Shouldn't they be cancelled in this case?
        }
    }

    /**
     * A strategy of listeners notification.
     */
    public static enum Strategy {

        /**
         * Serial notification is performed in the same thread and listeners
         * are notified in a sequential order
         */
        SERIAL,
        /**
         * Notification is performed simultaniously (each listener will get
         * notification in a different (non UI) separate thread. But
         * <code>notifyListeners</code> method will return only after all events
         * are processed by the listeners.
         *
         * Note: I doubt that this approach may have any performance benefits...
         *       It's a much better idea to have listeners that do not spend time
         *       in notification method
         */
        CONCURRENT
    }
}
