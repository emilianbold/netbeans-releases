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

package org.netbeans.module.dlight.threads.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import org.netbeans.module.dlight.threads.api.ThreadDump;
import org.netbeans.module.dlight.threads.api.storage.ThreadsDataStorage;
import org.netbeans.modules.dlight.impl.SQLDataStorage;

/**
 *
 * @author mt154047
 */
public abstract class ThreadsSQLDataStorage extends SQLDataStorage implements ThreadsDataStorage{

    protected ThreadsSQLDataStorage() {
        super();
    }

    protected ThreadsSQLDataStorage(String dburl) throws SQLException {
        super(dburl);
    }
    
    public ThreadDump getThreadDump(long timestamp, int threadID, int threadState) {
        ThreadDumpImpl result = null;

        try {
            // First, we need ts of the thread threadID when it was in required state.
            PreparedStatement statement = prepareStatement(
                    "select max(time_stamp) from CallStack where " + // NOI18N
                    "thread_id = ? and time_stamp <= ? and mstate = ?"); // NOI18N

            statement.setInt(1, threadID);
            statement.setLong(2, timestamp);
            statement.setInt(3, threadState);

            ResultSet rs = statement.executeQuery();
            long ts = -1;

            if (rs.next()) {
                ts = rs.getLong(1);
            }

            rs.close();

            if (ts < 0) {
                // Means that no callstack found for this thread in this state
                //System.out.println("No callstack found!!!");
                return null;
            }

            //System.out.println("Nearest callstack found at " + ts);

            result = new ThreadDumpImpl(ts);

            // Next, get all times for all threads for alligned stacks (time <= ts)
            // select threadid, max(ts) from test where ts <= 6 group by threadid;

            statement = prepareStatement(
                    "select thread_id, max(time_stamp) from CallStack where " + // NOI18N
                    "time_stamp <= ? group by thread_id"); // NOI18N

            statement.setLong(1, ts);

            rs = statement.executeQuery();

            HashMap<Integer, Long> idToTime = new HashMap<Integer, Long>();

            while (rs.next()) {
                int callStackThreadId = rs.getInt(1);
                long callStackTimeStamp = rs.getLong(2);
                idToTime.put(callStackThreadId, callStackTimeStamp);
            }


            // Next, get stacks from database having tstamps and thread ids..

        } catch (SQLException ex) {
            System.err.println("ex: " + ex.getSQLState());
        }

        return result;
    }
}
