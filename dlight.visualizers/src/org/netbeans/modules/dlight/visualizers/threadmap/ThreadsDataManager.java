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
package org.netbeans.modules.dlight.visualizers.threadmap;

///**
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.dlight.threadmap.support.spi.ThreadState;

/**
 * A class that holds data about threads history (state changes) during a
 * profiling session. It consumes/processes data obtained from the server via the
 * MonitoredData class, but translates them into data structures more efficient for
 * presentation. A listener is provided for those who want to be notified about
 * newly arrived data.
 *
 * @author Jiri Sedlacek
 * @author Ian Formanek
 * @author Misha Dmitriev
 * @author Alexander Simon (adapted for CND)
 */
public class ThreadsDataManager {
    private ThreadData[] threadData; // Per-thread array of points at which thread's state changes
    private boolean threadsMonitoringEnabled = true;
    private long endTime; // Timestamp of threadData end
    private long startTime; // Timestamp of threadData start
    private final Set<DataManagerListener> listeners = new HashSet<DataManagerListener>();

    /**
     * Creates a new instance of ThreadsDataManager
     */
    public ThreadsDataManager() {
        reset();
    }

    /**
     * Adds new threadData Listener.
     *
     * @param listener threadData listener to add
     */
    public void addDataListener(DataManagerListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes threadData listener.
     *
     * @param listener threadData listener to remove
     */
    public void removeDataListener(DataManagerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners about the threadData change.
     */
    private void fireDataChanged() {
        if (listeners.isEmpty()) {
            return;
        }

        Set<DataManagerListener> toNotify;
        synchronized (listeners) {
            toNotify = new HashSet<DataManagerListener>(listeners);
        }
        Iterator<DataManagerListener> iterator = toNotify.iterator();
        while (iterator.hasNext()) {
            iterator.next().dataChanged();
        }
    }

    /**
     * Notifies all listeners about the reset of threads data.
     */
    private void fireDataReset() {
        if (listeners.isEmpty()) {
            return;
        }
        Set<DataManagerListener> toNotify;
        synchronized (listeners) {
            toNotify = new HashSet<DataManagerListener>(listeners);
        }
        Iterator<DataManagerListener> iterator = toNotify.iterator();
        while (iterator.hasNext()) {
            iterator.next().dataReset();
        }
    }

    /**
     * Returns the timestamp representing end time of collecting threadData (timestamp of last valid threadData record).
     */
    public synchronized long getEndTime() {
        return endTime;
    }

    /**
     * Returns the timestamp representing start time of collecting threadData (timestamp of first threadData record).
     */
    public synchronized long getStartTime() {
        return startTime;
    }

    public synchronized ThreadData getThreadData(int index) {
        return threadData[index];
    }

    public synchronized String getThreadName(int index) {
        return threadData[index].getName();
    }

    /**
     * Returns the number of currently monitored threads
     */
    public synchronized int getThreadsCount() {
        return threadData.length;
    }

    public synchronized void setThreadsMonitoringEnabled(boolean enabled) {
        if (threadsMonitoringEnabled == enabled) {
            return;
        }
        threadsMonitoringEnabled = enabled;
        if (!threadsMonitoringEnabled) { // clear accumulated data, except thread ids and names
            for (int i = 0; i < threadData.length; i++) {
                threadData[i].clearStates();
            }
        }
    }

    /**
     * Returns <CODE>true</CODE> if there are some monitored threads
     */
    public synchronized boolean hasData() {
        return (getThreadsCount() != 0);
    }

    /**
     * Convert the data received from the server on this iteration into the internal compressed format,
     * and notify listeners
     */
    public synchronized void processData(MonitoredData monitoredData) {
        int threadSize = monitoredData.getThreadsSize();
        if (threadSize == 0) {
            return;
        }
        threadData = new ThreadData[threadSize];
        for(int i = 0; i < threadSize; i++){
            threadData[i] = new ThreadData(monitoredData.getThreadInfo(i));
        }
        startTime = monitoredData.getStateTimestamps()[0];

        if (threadsMonitoringEnabled) {
            int nStates = monitoredData.getThreadStatesSize();
            if (nStates == 0) {
                return;
            }
            for(int i = 0; i < threadSize; i++){
                List<ThreadState> states = monitoredData.getThreadStates(i);
                ThreadData tData = threadData[i];
                for (int j = 0; j < states.size(); j++) {
                    tData.add(states.get(j));
                    endTime = states.get(j).getTimeStamp(i);
                }
            }
            fireDataChanged(); // all listeners are notified about threadData change */
        }
    }

    /**
     * Resets the threadData - clears timestamps and threadData store.
     */
    public synchronized void reset() {
        startTime = 0;
        endTime = 0;
        threadData = new ThreadData[0];
        fireDataReset(); // all listeners are notified about threadData change
    }
}
