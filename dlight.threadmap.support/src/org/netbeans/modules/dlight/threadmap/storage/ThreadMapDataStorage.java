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
package org.netbeans.modules.dlight.threadmap.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.threadmap.api.ThreadData;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapDataQuery;
import org.netbeans.modules.dlight.threadmap.api.ThreadMapData;
import org.netbeans.modules.dlight.api.storage.types.TimeDuration;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.ProxyDataStorage;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;

public class ThreadMapDataStorage implements ProxyDataStorage {
    // TODO: currently only one

    public static final String THREAD_MAP_STORAGE_TYPE_ID = "ThreadMapDataStorage"; // NOI18N

    private static final List<ThreadMapDataStorage> instances = new ArrayList<ThreadMapDataStorage>();
    private final List<ThreadDataImpl> data;
    private TimeDuration frequency;
    private SQLDataStorage sqlStorage;


    static {
        instances.add(new ThreadMapDataStorage());
    }

    public static final ThreadMapDataStorage getInstance() {
        return instances.get(0);
    }

    /*package*/ ThreadMapDataStorage() {
        data = new LinkedList<ThreadDataImpl>();
    }

    public void addThreadInfo(ThreadInfoImpl threadInfo) {
        data.add(new ThreadDataImpl(threadInfo));
    }

    public void addThreadState(ThreadInfoImpl threadInfo, ThreadStateImpl state) {
        for (ThreadDataImpl td : data) {
            if (td.getThreadInfo() == threadInfo) {
                td.addState(state);
                break;
            }
        }
    }

    /**
     *
     * @param tid - thread ID to get ThreadInfoImpl for.
     * @return null if there is no data associated with this thread ID yet.
     */
    public ThreadInfoImpl getThreadInfo(int tid) {
        for (ThreadDataImpl tmd : data) {
            if (tmd.getThreadInfo().getThreadId() == tid) {
                return tmd.getThreadInfo();
            }
        }

        return null;
    }

    public void clear() {
        data.clear();
    }

    public void init(TimeDuration frequency, DataTableMetadata metadata) {
        clear();
        this.frequency = frequency;
    }

    public ThreadMapData queryThreadMapData(ThreadMapDataQuery query) {
        final List<ThreadData> threadsData = new ArrayList<ThreadData>();

//        if (query.getTimeTo() == Long.MAX_VALUE) {
//            System.out.println("Query: " + TimeUnit.NANOSECONDS.toSeconds(query.getTimeFrom()) + " - till now ("+TimeUnit.NANOSECONDS.toSeconds(System.nanoTime())+")"); // NOI18N
//        } else {
//            System.out.println("Query: " + TimeUnit.NANOSECONDS.toSeconds(query.getTimeFrom()) + " - " + TimeUnit.NANOSECONDS.toSeconds(query.getTimeTo())); // NOI18N
//        }

        for (ThreadDataImpl td : data) {
            threadsData.add(td);
        }

        final ThreadMapData result = new ThreadMapData() {

            public List<ThreadData> getThreadsData() {
                return Collections.unmodifiableList(threadsData);
            }

            public TimeDuration getPrecision() {
                return frequency;
            }

            public boolean isSamplingMode() {
                return false;
            }
        };

        return result;
    }

    public boolean shutdown() {
        clear();
        return true;
    }

    public DataStorageType getBackendDataStorageType() {
        return DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE);
    }

    public List<DataTableMetadata> getBackendTablesMetadata() {
        return Collections.emptyList();
    }

    public void attachTo(DataStorage storage) {
        this.sqlStorage = (SQLDataStorage) storage;
    }

    public boolean hasData(DataTableMetadata data) {
        return false;
    }

    public void addData(String tableName, List<DataRow> data) {
        if (sqlStorage != null) {
            sqlStorage.addData(tableName, data);
        }
    }

    public Collection<DataStorageType> getStorageTypes() {
        return Collections.singletonList(DataStorageTypeFactory.getInstance().getDataStorageType(THREAD_MAP_STORAGE_TYPE_ID));
    }

    public boolean supportsType(DataStorageType storageType) {
        return getStorageTypes().contains(storageType);
    }

    public void createTables(List<DataTableMetadata> tableMetadatas) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
