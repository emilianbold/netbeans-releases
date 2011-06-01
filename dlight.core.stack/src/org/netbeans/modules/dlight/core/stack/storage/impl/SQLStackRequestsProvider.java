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

    public SQLStackRequestsProvider(SQLStatementsCache cache) {
        this.cache = cache;
        metricsCache = new MetricsCache();
    }

    public SQLRequest addNode(long nodeID, long callerID, long funcID, long offset) {
        return new AddNodeRequest(nodeID, callerID, funcID, offset);
    }

    SQLRequest updateNodeMetrics(long nodeID, long contextID, long bucket, long duration) {
        metricsCache.updateNodeMetrics(nodeID, contextID, bucket, duration, false, true);
        return new UpdateNodeMetricsRequest(nodeID, contextID, bucket);
    }

    SQLRequest updateFunctionMetrics(long funcID, long contextID, long bucket, long duration, boolean addInclusive, boolean addExclusive) {
        metricsCache.updateFunctionMetrics(funcID, contextID, bucket, duration, addInclusive, addExclusive);
        return new UpdateFuncMetricsRequest(funcID, contextID, bucket);
    }

    SQLRequest addFunction(long funcID, CharSequence funcName) {
        return new AddFunctionRequest(funcID, funcName);
    }

    SQLRequest addFile(Long fileID, CharSequence path) {
        return new AddFileRequest(fileID, path);
    }

    SQLRequest addSourceInfo(long nodeID, long contextID, long fileID, int line, int column, long fileOffset) {
        return new AddSourceInfoRequest(nodeID, contextID, fileID, line, column, fileOffset);
    }

    SQLRequest addModule(Long moduleID, CharSequence module) {
        return new AddModuleRequest(moduleID, module);
    }

    SQLRequest addModuleInfo(long nodeID, long contextID, long moduleID, long offsetInModule) {
        return new AddModuleInfoRequest(nodeID, contextID, moduleID, offsetInModule);
    }

    private class AddSourceInfoRequest implements SQLRequest {

        private final long nodeID;
        private final long contextID;
        private final long fileID;
        private final int line;
        private final int column;
        private final long fileOffset;

        private AddSourceInfoRequest(long nodeID, long contextID, long fileID, int line, int column, long fileOffset) {
            this.nodeID = nodeID;
            this.contextID = contextID;
            this.fileID = fileID;
            this.line = line;
            this.column = column;
            this.fileOffset = fileOffset;
        }

        @Override
        public void execute() throws SQLException {
            PreparedStatement stmt = cache.getPreparedStatement("insert into sourceinfo (node_id, context_id, file_id, fline, fcolumn, file_offset) values (?,?,?,?,?,?)"); // NOI18N
            stmt.setLong(1, nodeID);
            stmt.setLong(2, contextID);
            stmt.setLong(3, fileID);
            stmt.setInt(4, line);
            stmt.setInt(5, column);
            stmt.setLong(6, fileOffset);
            stmt.executeUpdate();
        }
    }

    private class AddModuleInfoRequest implements SQLRequest {

        private final long nodeID;
        private final long contextID;
        private final long moduleID;
        private final long offsetInModule;

        private AddModuleInfoRequest(long nodeID, long contextID, long moduleID, long offsetInModule) {
            this.nodeID = nodeID;
            this.contextID = contextID;
            this.moduleID = moduleID;
            this.offsetInModule = offsetInModule;
        }

        @Override
        public void execute() throws SQLException {
            PreparedStatement stmt = cache.getPreparedStatement("insert into moduleinfo (node_id, context_id, module_id, module_offset) values (?,?,?,?)"); // NOI18N
            stmt.setLong(1, nodeID);
            stmt.setLong(2, contextID);
            stmt.setLong(3, moduleID);
            stmt.setLong(4, offsetInModule);
            stmt.executeUpdate();
        }
    }

    private class AddFileRequest implements SQLRequest {

        private final Long fileID;
        private final CharSequence path;

        private AddFileRequest(Long fileID, CharSequence path) {
            this.fileID = fileID;
            this.path = path;
        }

        @Override
        public void execute() throws SQLException {
            PreparedStatement stmt = cache.getPreparedStatement("insert into sourcefiles (id, path) values (?, ?)"); // NOI18N
            stmt.setLong(1, fileID);
            stmt.setString(2, truncate(path).toString());
            stmt.executeUpdate();
        }
    }

    private class AddModuleRequest implements SQLRequest {

        private final Long moduleID;
        private final CharSequence module;

        private AddModuleRequest(Long moduleID, CharSequence module) {
            this.moduleID = moduleID;
            this.module = module;
        }

        @Override
        public void execute() throws SQLException {
            PreparedStatement stmt = cache.getPreparedStatement("insert into modules (id, path) values (?, ?)"); // NOI18N
            stmt.setLong(1, moduleID);
            stmt.setString(2, truncate(module).toString());
            stmt.executeUpdate();
        }
    }

    private class AddNodeRequest implements SQLRequest {

        public final long nodeID;
        public final long callerId;
        public final long funcID;
        public final long offset;

        public AddNodeRequest(long nodeID, long callerId, long funcID, long offset) {
            this.nodeID = nodeID;
            this.callerId = callerId;
            this.funcID = funcID;
            this.offset = offset;
        }

        @Override
        public void execute() throws SQLException {
            PreparedStatement stmt = cache.getPreparedStatement(
                    "insert into StackNode (id, caller_id, func_id, offset) values (?, ?, ?, ?)"); // NOI18N

            stmt.setLong(1, nodeID);
            stmt.setLong(2, callerId);
            stmt.setLong(3, funcID);
            stmt.setLong(4, offset);
            stmt.executeUpdate();
        }
    }

    private class AddFunctionRequest implements SQLRequest {

        public final long funcID;
        public final CharSequence name;

        private AddFunctionRequest(long funcID, CharSequence name) {
            this.funcID = funcID;
            this.name = name;
        }

        @Override
        public void execute() throws SQLException {
            PreparedStatement stmt = cache.getPreparedStatement("insert into funcnames (id, fname) values (?, ?)"); // NOI18N
            stmt.setLong(1, funcID);
            stmt.setString(2, truncate(name).toString());
            stmt.executeUpdate();
        }
    }

    private class UpdateNodeMetricsRequest implements SQLRequest {

        private final long nodeID;
        private final long contextID;
        private final long bucket;

        private UpdateNodeMetricsRequest(long nodeID, long contextID, long bucket) {
            this.nodeID = nodeID;
            this.contextID = contextID;
            this.bucket = bucket;
        }

        @Override
        public void execute() throws SQLException {
            MetricsCache.Metrics metrics = metricsCache.getAndResetNodeMetrics(nodeID, contextID, bucket);

            if (metrics == null || (metrics.incl == 0 && metrics.excl == 0)) {
                return;
            }

            PreparedStatement stmt = cache.getPreparedStatement(
                    "select node_id, context_id, bucket, time_excl " + // NOI18N
                    "from NodeMetrics " + // NOI18N
                    "where node_id = ? and context_id = ? and bucket = ? for update"); // NOI18N

            stmt.setLong(1, nodeID);
            stmt.setLong(2, contextID);
            stmt.setLong(3, bucket);

            ResultSet rs = stmt.executeQuery();

            try {
                if (rs.next()) {
                    rs.updateLong(4, rs.getLong(4) + metrics.excl);
                    rs.updateRow();
                } else {
                    rs.moveToInsertRow();
                    rs.updateLong(1, nodeID);
                    rs.updateLong(2, contextID);
                    rs.updateLong(3, bucket);
                    rs.updateLong(4, metrics.excl);
                    rs.insertRow();
                }
            } finally {
                rs.close();
            }
        }
    }

    private class UpdateFuncMetricsRequest implements SQLRequest {

        private final long funcID;
        private final long contextID;
        private final long bucket;

        private UpdateFuncMetricsRequest(long funcID, long contextID, long bucket) {
            this.funcID = funcID;
            this.contextID = contextID;
            this.bucket = bucket;
        }

        @Override
        public void execute() throws SQLException {
            MetricsCache.Metrics metrics = metricsCache.getAndResetFunctionMetrics(funcID, contextID, bucket);

            if (metrics == null || (metrics.incl == 0 && metrics.excl == 0)) {
                return;
            }

            PreparedStatement stmt = cache.getPreparedStatement(
                    "select func_id, context_id, bucket, time_incl, time_excl " + // NOI18N
                    "from FuncMetrics " + // NOI18N
                    "where func_id = ? and context_id = ? and bucket = ? for update"); // NOI18N

            stmt.setLong(1, funcID);
            stmt.setLong(2, contextID);
            stmt.setLong(3, bucket);

            ResultSet rs = stmt.executeQuery();

            try {
                if (rs.next()) {
                    rs.updateLong(4, rs.getLong(4) + metrics.incl);
                    rs.updateLong(5, rs.getLong(5) + metrics.excl);
                    rs.updateRow();
                } else {
                    rs.moveToInsertRow();
                    rs.updateLong(1, funcID);
                    rs.updateLong(2, contextID);
                    rs.updateLong(3, bucket);
                    rs.updateLong(4, metrics.incl);
                    rs.updateLong(5, metrics.excl);
                    rs.insertRow();
                }
            } finally {
                rs.close();
            }
        }
    }

    private static CharSequence truncate(CharSequence str) {
        if (str.length() <= MAX_STRING_LENGTH) {
            return str;
        } else {
            return str.subSequence(0, MAX_STRING_LENGTH - 3) + "..."; // NOI18N
        }
    }
}
