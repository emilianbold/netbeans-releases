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
package org.netbeans.modules.dlight.threadmap.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.storage.types.TimeDuration;
import org.netbeans.modules.dlight.core.stack.api.ThreadInfo;
import org.netbeans.modules.dlight.core.stack.api.ThreadState;
import org.netbeans.modules.dlight.threadmap.api.ThreadData;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.core.stack.api.ThreadDumpQuery;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapDataQuery;
import org.netbeans.modules.dlight.threadmap.api.ThreadMapData;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshot;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshotQuery;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapDataProvider;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.threadmap.storage.ThreadMapDataStorage;

public class ThreadMapDataProviderImpl implements ThreadMapDataProvider {

    private StackDataStorage stackDataStorage;
    private final ThreadMapDataStorage storage = ThreadMapDataStorage.getInstance();

    public void attachTo(ServiceInfoDataStorage serviceInfoDataStorage) {
    }

    public ThreadMapData queryData(final ThreadMapDataQuery query) {
        if (!query.isSampling() && storage == null) {
            throw new NullPointerException("No STORAGE"); // NOI18N
        }
        if (query.isSampling() && stackDataStorage == null && storage != null) {
            return storage.queryThreadMapData(query);
        }
        if (query.isSampling() && stackDataStorage != null) {
            final List<ThreadSnapshot> snapshots =
                    stackDataStorage.getThreadSnapshots(
                    new ThreadSnapshotQuery(false, new ThreadSnapshotQuery.TimeFilter(query.getTimeFrom(), query.getTimeTo(), ThreadSnapshotQuery.TimeFilter.Mode.LAST)));
            //now we should create ThreadMapData
            ThreadMapData result = new ThreadMapData() {

                public List<ThreadData> getThreadsData() {
                    List<ThreadData> result = new ArrayList<ThreadData>();
                    for (final ThreadSnapshot snapshot : snapshots){
                        ThreadData data = new ThreadData() {

                            public ThreadInfo getThreadInfo() {
                                return snapshot.getThreadInfo();
                            }

                            public List<ThreadState> getThreadState() {
                                ThreadState result = new ThreadState() {

                                    public int size() {
                                        return 1;
                                    }

                                    public MSAState getMSAState(int index, boolean full) {
                                        return snapshot.getState();
                                    }

                                    public byte getState(int index) {
                                        return  ThreadState.POINTS;
                                    }

                                    public long getTimeStamp(int index) {
                                        return snapshot.getTimestamp();
                                    }

                                    public long getTimeStamp() {
                                        return snapshot.getTimestamp();
                                    }

                                    public int getSamplingStateIndex(boolean full) {
                                        return 0;
                                    }
                                };
                                return Arrays.asList(result);
                            }

                        };
                        result.add(data);
                    }

                    return result;
                }

                public TimeDuration getPrecision() {
                    return new TimeDuration(TimeUnit.SECONDS, 1);
                }

                public boolean isSamplingMode() {
                    return true;
                }
            };
            return result ;
        }
        if (storage != null) {
            return storage.queryThreadMapData(query);
        }
        return null;

        //   return storage.queryThreadMapData(query);



    }

    public ThreadDump getThreadDump(final ThreadDumpQuery query) {
        if (stackDataStorage == null) {
            return null;
        }
        return stackDataStorage.getThreadDump(query);
//      TODO: try the new getThreadSnapshots() method
//        final long timestamp = query.getThreadState().getTimeStamp();
//        final List<ThreadSnapshot> result = stackDataStorage.getThreadSnapshots(
//                new ThreadSnapshotQuery(query.isFullMode(), new ThreadSnapshotQuery.ThreadFilter(query.getShowThreads()), new ThreadSnapshotQuery.TimeFilter(-1, timestamp, ThreadSnapshotQuery.TimeFilter.Mode.LAST)));
//        return new ThreadDump() {
//
//            public long getTimestamp() {
//                return timestamp;
//            }
//
//            public List<ThreadSnapshot> getThreadStates() {
//                return result;
//            }
//        };
    }

    public void attachTo(DataStorage storage) {
        if (storage instanceof StackDataStorage) {
            stackDataStorage = (StackDataStorage) storage;
        } else {
            stackDataStorage = null;
        }
    }

    public void dataFiltersChanged(List<DataFilter> newSet) {
    }
}
