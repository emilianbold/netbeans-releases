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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadData;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadMapDataQuery;
import org.netbeans.modules.dlight.api.storage.types.TimeDuration;
import org.netbeans.modules.dlight.spi.impl.ThreadMapData;

public class ThreadMapDataStorage {
    // TODO: currently only one

    private static final List<ThreadMapDataStorage> instances = new ArrayList<ThreadMapDataStorage>();
    private final List<ThreadDataImpl> data;


    static {
        instances.add(new ThreadMapDataStorage());
    }
    private TimeDuration frequency;

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

    public ThreadInfoImpl getThreadInfo(int tid) {
        for (ThreadDataImpl tmd : data) {
            if (tmd.getThreadInfo().getThreadId() == tid) {
                return tmd.getThreadInfo();
            }
        }

        return null;
    }

    private ThreadMapDataStorage() {
        data = new LinkedList<ThreadDataImpl>();
    }

    public static final ThreadMapDataStorage getInstance() {
        return instances.get(0);
    }

    public void addThreadStatesRecord(ThreadData data) {
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

        for (ThreadDataImpl td : data) {
//            if (td.getThreadState())
            threadsData.add(td);
        }

        final ThreadMapData result = new ThreadMapData() {

            public List<ThreadData> getThreadsData() {
                return Collections.unmodifiableList(threadsData);
            }

            public TimeDuration getPrecision() {
                return frequency;
            }
        };

        return result;
    }

    public boolean shutdown() {
        clear();
        return true;
    }
}
//
//
//    public List<ThreadMapData> queryThreadMapData(ThreadMapDataQuery queryData) {
//        List<ThreadMapData> result = new ArrayList<ThreadMapData>();
//
//        try {
//            String tableName = "MSA";
//            DataTableMetadata tableMetadata = tables.get(tableName); // NOI18N
//            StringBuilder query = new StringBuilder("select "); //NOI18N
//            Collection<? extends Column> columns = tableMetadata.getColumns();
//
//            query.append(new EnumStringConstructor<Column>().constructEnumString(columns,
//                    new Convertor<Column>() {
//
//                        public String toString(Column item) {
//                            return (item.getExpression() == null) ? item.getColumnName() : item.getExpression();
//                        }
//                    }));
//
//            query.append(" from ").append(tableName); //NOI18N
//
//            final ResultSet rs = select("MSA", tableMetadata.getColumns(), query.toString());
//            if (rs == null) {
//                return Collections.emptyList();
//            }
//            List<String> colnames = new ArrayList<String>();
//
//            for (Column c : columns) {
//                colnames.add(c.getColumnName());
//            }
//
//            while (rs.next()) {
//                ArrayList<Object> data = new ArrayList<Object>();
//                for (Column c : columns) {
//                    data.add(rs.getObject(c.getColumnName()));
//                }
//
//                ThreadMapData threadMapData = new ThreadMapData() {
//
//                    public ThreadInfo getThreadInfo() {
//                        return new ThreadInfo() {
//
//                            public int getThreadId() {
//                                int result = 0;
//                                try {
//                                    rs.getInt("THRID");
//                                } catch (SQLException ex) {
//                                    Exceptions.printStackTrace(ex);
//                                }
//                                return result;
//                            }
//
//                            public String getThreadName() {
//                                return "XXX";
//                            }
//                        };
//                    }
//
//                    public List<ThreadState> getThreadState() {
//                        return Arrays.<ThreadState>asList(new ThreadState() {
//
//                            public int size() {
//                                return 5;
//                            }
//
//                            public String getStateName(int index) {
//                                return "ZOPA";
//                            }
//
//                            public int getState(int index) {
//                                if (index == 0) {
//                                    return 30;
//                                }
//                                return 1;
//                            }
//
//                            public long getTimeStamp(int index) {
//                                long result = 0;
//                                try {
//                                    result = rs.getLong("TSTAMP");
//                                } catch (SQLException ex) {
//                                    Exceptions.printStackTrace(ex);
//                                }
//                                return result;
//                            }
//
//                            public long getTimeStamp() {
//                                long result = 0;
//                                try {
//                                    result = rs.getLong("TSTAMP");
//                                } catch (SQLException ex) {
//                                    Exceptions.printStackTrace(ex);
//                                }
//                                return result;
//                            }
//                        });
//                    }
//                };
//
//                result.add(threadMapData);
//            }
//
//        } catch (Throwable th) {
//        }
//
//        return result;
//    }
