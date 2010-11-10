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
package org.netbeans.modules.dlight.spi.support;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class SQLStatementsCache {

    private static final HashMap<SQLDataStorage, SQLStatementsCache> storageToCache =
            new HashMap<SQLDataStorage, SQLStatementsCache>();
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final SQLDataStorage sqlStorage;
    private AtomicReference<ConcurrentHashMap<String, PreparedStatement>> cache =
            new AtomicReference<ConcurrentHashMap<String, PreparedStatement>>();

    private SQLStatementsCache(SQLDataStorage sqlStorage) {
        this.sqlStorage = sqlStorage;
        cache.set(new ConcurrentHashMap<String, PreparedStatement>());
    }

    public static SQLStatementsCache getFor(SQLDataStorage sqlStorage) {
        synchronized (storageToCache) {
            if (!storageToCache.containsKey(sqlStorage)) {
                storageToCache.put(sqlStorage, new SQLStatementsCache(sqlStorage));
            }
        }

        return storageToCache.get(sqlStorage);
    }

    public PreparedStatement getPreparedStatement(String sql) throws SQLException {
        if (closed.get()) {
            throw new SQLException("Already closed"); // NOI18N
        }

        ConcurrentHashMap<String, PreparedStatement> data = cache.get();

        PreparedStatement stmt = data.get(sql);

        if (stmt == null) {
            stmt = sqlStorage.prepareStatement(sql);
            PreparedStatement oldStmt = data.putIfAbsent(sql, stmt);
            if (oldStmt != null) {
                stmt = oldStmt;
            }
        }

        return stmt;
    }

    public void close() throws SQLException {
        if (closed.compareAndSet(false, true)) {
            ConcurrentHashMap<String, PreparedStatement> data = cache.getAndSet(null);

            for (PreparedStatement stmt : data.values()) {
                stmt.close();
            }
        }
    }
}
