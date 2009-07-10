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

import com.sun.jmx.mbeanserver.MetaData;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.storage.ThreadMapMetadata;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.threadmap.support.spi.ThreadInfo;
import org.netbeans.modules.dlight.threadmap.support.spi.ThreadMapData;
import org.netbeans.modules.dlight.threadmap.support.spi.ThreadMapDataProvider;
import org.netbeans.modules.dlight.threadmap.support.spi.ThreadState;

/**
 *
 * @author Alexander Simon
 */
public class MockDataProvider implements ThreadMapDataProvider {
    private final ThreadMapStorage dataBase = new ThreadMapStorage();

    public List<ThreadMapData> queryData(ThreadMapMetadata metadata) {
        return dataBase.getThreadMap(metadata);
    }

    public void attachTo(DataStorage storage) {
    }

    public void attachTo(ServiceInfoDataStorage serviceInfoDataStorage) {
    }

    public void dataFiltersChanged(List<DataFilter> newSet) {
    }

    private static final class ThreadMapStorage {
        private final List<MockThreadMapData> storage = new ArrayList<MockThreadMapData>();
        private Timer timer = new Timer();
        private int currentTime = 0;
        private ThreadMapMetadata metadata;
        private ThreadMapStorage(){
            timer.schedule(new TimerTask(){
                @Override
                public void run() {
                    if (currentTime == 0) {
                        storage.add(new MockThreadMapData("Dispather", 1, 97, 900, 3)); //NOI18N
                    }
                    if (currentTime == 20) {
                        storage.add(new MockThreadMapData("Worker", 2, 980, 0, 20)); //NOI18N
                    }
                    if (currentTime == 30) {
                        storage.add(new MockThreadMapData("Bad Dispather", 3, 800, 200, 0)); //NOI18N
                    }
                    if (currentTime == 50) {
                        storage.add(new MockThreadMapData("Bad Worker", 4, 800, 0, 200)); //NOI18N
                    }
                    for(MockThreadMapData data : storage){
                        data.advance(currentTime);
                    }
                    currentTime++;
                }
            },0, 1000);
        }

        public List<ThreadMapData> getThreadMap(ThreadMapMetadata metadata) {
            List<ThreadMapData> result = new ArrayList<ThreadMapData>();
            assert metadata.getTimeUnit() == TimeUnit.SECONDS;
            assert metadata.getStep() == 1;
            assert !metadata.isFullState();
            this.metadata = metadata;
            for(MockThreadMapData data : storage) {
                ThreadInfo info = data.getThreadInfo();
                List<ThreadState> list = new ArrayList<ThreadState>();
                for(ThreadState state : data.getThreadState()) {
                    int start = (int) state.getTimeStamp();
                    if (start < metadata.getTimeFrom()*1000) {
                        continue;
                    } else if (start > metadata.getTimeTo()*1000) {
                        break;
                    }
                    list.add(state);
                }
                result.add(new ThreadMapDataImpl(info, list));
            }
            return result;
        }
    }

    private static final class ThreadMapDataImpl implements ThreadMapData {
        private final ThreadInfo info;
        private final List<ThreadState> list;
        public ThreadMapDataImpl(ThreadInfo info, List<ThreadState> list){
            this.info = info;
            this.list = list;
        }
        public ThreadInfo getThreadInfo() {
            return info;
        }
        public List<ThreadState> getThreadState() {
            return list;
        }
    }

    private static final class MockThreadMapData implements ThreadMapData {
        private final ThreadInfo info;
        private final List<ThreadState> threadLine = new LinkedList<ThreadState>();
        private final int running;
        private final int waiting;
        private final int blocking;
        private final int sleeping;
        private final Random rand = new Random();

        public MockThreadMapData(final String name, final int id, int running, int waiting, int blocking){
            info = new ThreadInfo(){
                public int getThreadId() {
                    return id;
                }
                public String getThreadName() {
                    return name;
                }
            };
            this.running = running;
            this.waiting = waiting;
            this.blocking = blocking;
            sleeping = 1000 - running - waiting - blocking;
        }
        public ThreadInfo getThreadInfo() {
            return info;
        }
        public List<ThreadState> getThreadState() {
            return threadLine;
        }
        public void advance(int currentTime){
            int runningTime = running > 0 ? rand.nextInt(running) : 0;
            int waitingTime = waiting > 0 ? rand.nextInt(waiting) : 0;
            int blockingTime = blocking > 0 ? rand.nextInt(blocking) : 0;
            int sleepingTime = sleeping > 0 ? rand.nextInt(sleeping) : 0;
            int s = runningTime + waitingTime + blockingTime + sleepingTime;
            int rest = 0;
            int oldRest = 0;
            oldRest = rest;
            rest = (1000 * runningTime + rest)%s;
            runningTime = (1000 * runningTime + oldRest)/s;
            oldRest = rest;
            rest = (1000 * waitingTime + rest)%s;
            waitingTime = (1000 * waitingTime + oldRest)/s;
            oldRest = rest;
            rest = (1000 * blockingTime + rest)%s;
            blockingTime = (1000 * blockingTime + oldRest)/s;
            sleepingTime = 1000 - runningTime - waitingTime - blockingTime;
            MockThreadState state = new MockThreadState(currentTime*1000, runningTime, waitingTime, blockingTime, sleepingTime);
            threadLine.add(state);
        }
    }

    private static final class MockThreadState implements ThreadState {
        private final short runningTime;
        private final short waitingTime;
        private final short blockingTime;
        private final short sleepingTime;
        private final long currentTime;
        public MockThreadState(int currentTime, int runningTime, int waitingTime, int blockingTime, int sleepingTime) {
            this.runningTime = (short) runningTime;
            this.waitingTime = (short) waitingTime;
            this.blockingTime = (short) blockingTime;
            this.sleepingTime = (short) sleepingTime;
            this.currentTime = currentTime;
        }
        public int size() {
            return 4;
        }
        public String getStateName(int index) {
            switch (index) {
                case 0:
                    return ThreadState.ShortThreadState.Blocked.name();
                case 1:
                    return ThreadState.ShortThreadState.Running.name();
                case 2:
                    return ThreadState.ShortThreadState.Sleeping.name();
                case 3:
                    return ThreadState.ShortThreadState.Waiting.name();
            }
            throw new IllegalArgumentException();
        }
        public int getState(int index) {
            switch (index) {
                case 0:
                    return blockingTime;
                case 1:
                    return runningTime;
                case 2:
                    return sleepingTime;
                case 3:
                    return waitingTime;
            }
            throw new IllegalArgumentException();
        }
        public long getTimeStamp(int index) {
            return currentTime;
        }
        public long getTimeStamp() {
            return currentTime;
        }
    }
}
