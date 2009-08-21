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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.dlight.core.stack.api.ThreadData;
import org.netbeans.modules.dlight.core.stack.api.ThreadInfo;
import org.netbeans.modules.dlight.core.stack.api.ThreadState;

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
    private final List<ThreadStateColumnImpl> threadData = new ArrayList<ThreadStateColumnImpl>(); // Per-thread array of points at which thread's state changes
    private boolean threadsMonitoringEnabled = true;
    private long endTime; // Timestamp of threadData end
    private long startTime; // Timestamp of threadData start
    private final Set<DataManagerListener> listeners = new HashSet<DataManagerListener>();
    private int monitoredDataInterval;

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
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes threadData listener.
     *
     * @param listener threadData listener to remove
     */
    public void removeDataListener(DataManagerListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Notifies all listeners about the threadData change.
     */
    private void fireDataChanged() {
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
    public synchronized long getEndTimeStump() {
        return endTime;
    }

    /**
     * Returns the timestamp representing end time of collecting threadData (timestamp of last valid threadData record).
     */
    public synchronized long getEndTime() {
        return ThreadStateColumnImpl.timeStampToMilliSeconds(endTime);
    }

    /**
     * Returns the timestamp representing start time of collecting threadData (timestamp of first threadData record).
     */
    public synchronized long getStartTime() {
        return ThreadStateColumnImpl.timeStampToMilliSeconds(startTime);
    }

    public synchronized int getInterval() {
        return monitoredDataInterval;
    }

    public synchronized ThreadStateColumnImpl getThreadData(int index) {
        return threadData.get(index);
    }

    public synchronized String getThreadName(int index) {
        return threadData.get(index).getName();
    }

    /**
     * Returns the number of currently monitored threads
     */
    public synchronized int getThreadsCount() {
        return threadData.size();
    }

    public synchronized void setThreadsMonitoringEnabled(boolean enabled) {
        if (threadsMonitoringEnabled == enabled) {
            return;
        }
        threadsMonitoringEnabled = enabled;
        if (!threadsMonitoringEnabled) { // clear accumulated data, except thread ids and names
            for (int i = 0; i < threadData.size(); i++) {
                threadData.get(i).clearStates();
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
        mergeData(monitoredData);
        threadSize = threadData.size();
        if (threadSize == 0) {
            return;
        }
        startTime = Long.MAX_VALUE;
        for(int i = 0; i < threadSize; i++){
            startTime = Math.min(startTime, threadData.get(i).getThreadStartTimeStamp());
        }
        endTime = 0;
        if (threadsMonitoringEnabled) {
            for(int i = 0; i < threadSize; i++){
                ThreadStateColumnImpl col = threadData.get(i);
                endTime = Math.max(endTime, col.getThreadStateAt(col.size()-1).getTimeStamp());
            }
            fireDataChanged(); // all listeners are notified about threadData change */
        }
    }

    private void mergeData(MonitoredData monitoredData){
        int updateThreadSize = monitoredData.getThreadsSize();
        if (updateThreadSize == 0) {
            return;
        }
        monitoredDataInterval = monitoredData.getTimeStampInterval();
        Map<Integer, Integer> IdToNumber = new LinkedHashMap<Integer, Integer>();
        for(int i = 0; i < updateThreadSize; i++){
            ThreadInfo info = monitoredData.getThreadInfo(i);
            IdToNumber.put(info.getThreadId(), i);
        }
        // merge old state and increment
        int oldThreadSize = threadData.size();
        for(int i = 0; i < oldThreadSize; i++){
            ThreadStateColumnImpl col = threadData.get(i);
            Integer number = IdToNumber.get(col.getThreadID());
            if (number == null) {
                // this is dead thread
                //if (col.isAlive()){
                //   closeThread(col, monitoredDataInterval);
                //}
            } else {
                ThreadState lastState = col.getThreadStateAt(col.size()-1);
                int newData = number.intValue();
                List<ThreadState> states = monitoredData.getThreadStates(newData);
                long lastTimeStamp = -1;
                for (int j = 0; j < states.size(); j++) {
                    ThreadState newState = states.get(j);
                    if (newState.getTimeStamp() > lastState.getTimeStamp()) {
                        if (lastTimeStamp == -1) {
                            //if (!col.isAlive()) {
                            //    reopenThread(col);
                            //}
                        }
                        col.add(newState);
                        //System.err.println("thread "+number+" state "+newState);
                        lastTimeStamp = newState.getTimeStamp();
                    }
                    col.updateStackProvider(monitoredData.getStackProvider(newData));
                }
                if (lastTimeStamp == -1) {
                    //if (col.isAlive()){
                    //    closeThread(col, monitoredData.getTimeStampInterval());
                    //}
                }
                IdToNumber.remove(col.getThreadID());
            }
        }
        // add new threads
        for (Integer number : IdToNumber.values()) {
            int i = number.intValue();
            List<ThreadState> states = monitoredData.getThreadStates(i);
            int size = states.size();
            if (size > 0) {
                MergedThreadInfo info = new MergedThreadInfo(monitoredData.getThreadInfo(i), monitoredData.getStartTimestamp(i));
                ThreadData stackProvider = monitoredData.getStackProvider(i);
                ThreadStateColumnImpl col = new ThreadStateColumnImpl(info, stackProvider);
                threadData.add(col);
                for (int j = 0; j < size; j++) {
                    col.add(states.get(j));
                }
            }
        }
    }

    private void reopenThread(ThreadStateColumnImpl col){
        System.out.println("Reopen thread line "+col.getName()); // NOI18N
        // remove stop mark
        col.removeStopMark();
    }

    private void closeThread(ThreadStateColumnImpl col, int interval){
        final long endTimeStamp = col.getThreadStateAt(col.size()-1).getTimeStamp() + interval;
        System.out.println("Close thread line "+col.getName()); // NOI18N
        col.add(new ThreadState(){
            public int size() {
                return 1;
            }
            public MSAState getMSAState(int index, boolean full) {
                return ThreadState.MSAState.ThreadFinished;
            }
            public byte getState(int index) {
                return ThreadState.POINTS;
            }
            public long getTimeStamp(int index) {
                return endTimeStamp;
            }
            public long getTimeStamp() {
                return endTimeStamp;
            }

            @Override
            public String toString() {
                return "MSA "+getTimeStamp()+" "+getMSAState(0, false).name(); // NOI18N
            }

            public int getSamplingStateIndex(boolean full) {
                return 0;
            }
        });
    }

    /**
     * Resets the threadData - clears timestamps and threadData store.
     */
    public synchronized void reset() {
        startTime = 0;
        endTime = 0;
        threadData.clear();
        fireDataReset(); // all listeners are notified about threadData change
    }

    static class MergedThreadInfo {
        private final String name;
        private final LinkedList<ProcessID> processes = new LinkedList<ProcessID>();
        private MergedThreadInfo(ThreadInfo info, long startTime){
            this.name = info.getThreadName();
            processes.add(new ProcessID(info.getThreadId(), startTime));
        }
        String getThreadName() {
            return name;
        }
        int getThreadId() {
            return processes.getFirst().getId();
        }
        long getStartTimeStamp() {
            return processes.getFirst().getStartTimeStamp();
        }
    }

    private static final class ProcessID {
        private final int id;
        private final long startTimeStamp;
        private ProcessID(int id, long startTimeStamp) {
            this.id = id;
            this.startTimeStamp = startTimeStamp;
        }
        private int getId() {
            return id;
        }
        private long getStartTimeStamp() {
            return startTimeStamp;
        }
    }
}
