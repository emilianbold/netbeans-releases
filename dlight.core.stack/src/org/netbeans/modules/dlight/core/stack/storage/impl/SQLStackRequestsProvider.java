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
package org.netbeans.modules.dlight.core.stack.storage.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.netbeans.modules.dlight.spi.support.SQLRequest;
import org.netbeans.modules.dlight.spi.support.SQLStatementsCache;

/**
 *
 * @author ak119685
 */
final class SQLStackRequestsProvider {

    /**
     * Maximal string length that underlying database can store.
     * Must match the numbers in schema.sql.
     */
    private static final int MAX_STRING_LENGTH = 16384;
    private final SQLStatementsCache cache;
    private final MetricsCache metricsCache;

    public SQLStackRequestsProvider(SQLStatementsCache cache, MetricsCache metricsCache) {
        this.cache = cache;
        this.metricsCache = metricsCache;
    }

    public SQLRequest addSourceFileInfo(long id, CharSequence sourceFile) {
        return new AddSourceFileInfo(id, sourceFile);
    }

    public AddNodeRequest addNode(Long nodeId, long callerId, long funcId, long offset, int lineNumber) {
        return new AddNodeRequest(nodeId, callerId, funcId, offset, lineNumber);
    }

    public AddFunctionRequest addFunction(Long funcId, String funcName, int source_file_index, int line_number, long context_id) {
        return new AddFunctionRequest(funcId, funcName, source_file_index, line_number, context_id);
    }

    public SQLRequest updateNodeMetrics(long id, long bucket) {
        return new UpdateMetricsRequest(true, id, bucket);
    }

    public SQLRequest updateFunctionMetrics(long id, long bucket) {
        return new UpdateMetricsRequest(false, id, bucket);
    }

    public class AddNodeRequest implements SQLRequest {

        public final long id;
        public final long callerId;
        public final long funcId;
        public final long offset;
        public final int lineNumber;

        public AddNodeRequest(long id, long callerId, long funcId, long offset, int lineNumber) {
            this.id = id;
            this.callerId = callerId;
            this.funcId = funcId;
            this.offset = offset;
            this.lineNumber = lineNumber;
        }

        @Override
        public void execute() throws SQLException {
            PreparedStatement stmt = cache.getPreparedStatement(
                    "INSERT INTO Node (node_id, caller_id, func_id, offset, " + // NOI18N
                    "time_incl, time_excl, line_number) " + // NOI18N
                    "VALUES (?, ?, ?, ?, ?, ?, ?)"); // NOI18N

            stmt.setLong(1, id);
            stmt.setLong(2, callerId);
            stmt.setLong(3, funcId);
            stmt.setLong(4, offset);
            stmt.setInt(5, lineNumber);
            stmt.setLong(5, 0);
            stmt.setLong(6, 0);
            stmt.setInt(7, lineNumber);
            stmt.executeUpdate();
        }
    }

    public class AddFunctionRequest implements SQLRequest {

        public final long id;
        public final CharSequence name;
        public final int sourceFileIndex;
        public final int line_number;
        public final long context_id;

        public AddFunctionRequest(long id, CharSequence name, int sourceFileIndex, int line_number, long context_id) {
            this.id = id;
            this.name = name;
            this.sourceFileIndex = sourceFileIndex;
            this.line_number = line_number;
            this.context_id = context_id;
        }

        @Override
        public void execute() throws SQLException {
            PreparedStatement stmt = cache.getPreparedStatement(
                    "INSERT INTO Func " + // NOI18N
                    "(func_id, func_name, func_source_file_id, line_number, context_id) " + // NOI18N
                    "VALUES (?, ?, ?, ?, ?)"); // NOI18N
            stmt.setLong(1, id);
            stmt.setString(2, truncateString(name.toString()));
            stmt.setInt(3, sourceFileIndex);
            stmt.setLong(4, line_number);
            stmt.setLong(5, context_id);
            stmt.executeUpdate();
        }
    }

    public class AddSourceFileInfo implements SQLRequest {

        public final long id;
        public final CharSequence sourceFile;

        public AddSourceFileInfo(long id, CharSequence sourceFile) {
            this.id = id;
            this.sourceFile = sourceFile;
        }

        @Override
        public void execute() throws SQLException {
            PreparedStatement stmt = cache.getPreparedStatement(
                    "INSERT INTO SourceFiles (source_file) VALUES ( ?)"); // NOI18N
            stmt.setString(1, truncateString(sourceFile.toString()));
            stmt.executeUpdate();
        }
    }

    public class UpdateMetricsRequest implements SQLRequest {

        public final boolean funcOrNode; // false => func, true => node
        public final long objId;
        public final long bucketId;

        public UpdateMetricsRequest(boolean funcOrNode, long objId, long bucketId) {
            this.funcOrNode = funcOrNode;
            this.objId = objId;
            this.bucketId = bucketId;
        }

        @Override
        public void execute() throws SQLException {
            if (funcOrNode) {
                executeNode();
            } else {
                executeFunc();
            }
        }

        private void executeFunc() throws SQLException {
            MetricsCache.Metrics metrics = metricsCache.getAndResetFunctionMetrics(objId, bucketId);

            if (metrics == null) {
                return;
            }

            PreparedStatement stmt = cache.getPreparedStatement(
                    "SELECT func_id, bucket_id, time_incl, time_excl " + // NOI18N
                    "FROM FuncMetricAggr " + // NOI18N
                    "WHERE func_id = ? AND bucket_id = ? FOR UPDATE"); // NOI18N
            stmt.setLong(1, objId);
            stmt.setLong(2, bucketId);
            ResultSet rs = stmt.executeQuery();

            try {
                if (rs.next()) {
                    rs.updateLong(3, rs.getLong(3) + metrics.incl);
                    rs.updateLong(4, rs.getLong(4) + metrics.excl);
                    rs.updateRow();
                } else {
                    rs.moveToInsertRow();
                    rs.updateLong(1, objId);
                    rs.updateLong(2, bucketId);
                    rs.updateLong(3, metrics.incl);
                    rs.updateLong(4, metrics.excl);
                    rs.insertRow();
                }
            } finally {
                rs.close();
            }
        }

        private void executeNode() throws SQLException {
            MetricsCache.Metrics metrics = metricsCache.getAndResetNodeMetrics(objId, bucketId);

            if (metrics == null) {
                return;
            }

            PreparedStatement stmt = cache.getPreparedStatement(
                    "UPDATE Node SET time_incl = time_incl + ?, " + // NOI18N
                    "time_excl = time_excl + ? WHERE node_id = ?"); // NOI18N
            stmt.setLong(1, metrics.incl);
            stmt.setLong(2, metrics.excl);
            stmt.setLong(3, objId);
            stmt.executeUpdate();

        }
    }

    private static String truncateString(String str) {
        if (str.length() <= MAX_STRING_LENGTH) {
            return str;
        } else {
            return str.substring(0, MAX_STRING_LENGTH - 3) + "..."; // NOI18N
        }
    }
}
