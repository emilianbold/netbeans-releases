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
package org.netbeans.modules.dlight.spi.support.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.util.DLightLogger;

/**
 * This an utility wrapper class around sql.Connection.
 * It 'hides' SQLExceptions in several cases and returns valid empty ResultSet
 * in some cases to make it easier to aviod NPEs in users code...
 *
 * @author ak119685
 */
public final class SQLConnection {

    private static final Logger logger = DLightLogger.getLogger(SQLConnection.class);
    private Connection connection = null;
    private String logPrefix = ""; // NOI18N
    private PreparedStatement emptyResultStatement;

    public synchronized Connection getConnection() {
        return connection;
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                log(ex);
            } finally {
                connection = null;
                emptyResultStatement = null;
            }
        }
    }

    public synchronized void connect(Connection connection) {
        if (this.connection != null) {
            throw new IllegalStateException("Already connected!"); // NOI18N
        }

        this.connection = connection;

        try {
            emptyResultStatement = connection.prepareStatement("select 0 where 1 = 2"); // NOI18N
        } catch (SQLException ex) {
            log("Unable to prepare emptyResultStatement"); // NOI18N
        }

        if (logger.isLoggable(Level.FINE)) {
            try {
                logPrefix = connection.getMetaData().getURL() + ": "; // NOI18N
            } catch (SQLException ex) {
                log(ex);
            }
        }
    }

    /**
     * Executes SQL query. WARNING! Return value differs from what is returned
     * in Statement.execute(). The form of the first result (as returned from
     * Statement.execute()), is assigned to passed resultType AtomicBoolean.
     *
     * This method doesn't throw exceptions.
     *
     * @param sql SQL query to execute
     * @param resultType <code>true</code> if the first result is a <code>ResultSet</code>
     *         object; <code>false</code> if it is an update count or there are
     *         no results
     * @return true if no exception occured during query execution; false otherwise
     */
    public boolean execute(String sql, AtomicBoolean resultType) {
        try {
            Statement stmt = createStatement();
            try {
                boolean result = stmt.execute(sql);
                if (resultType != null) {
                    resultType.set(result);
                }
            } finally {
                stmt.close();
            }
        } catch (SQLException ex) {
            log(ex);
            return false;
        }

        return true;
    }

    /**
     * Executes SQL query. WARNING! Return value differs from what is returned
     * in Statement.execute().
     *
     * If need to get the form of the first result (as returned from
     * Statement.execute()), use execute(String sql, AtomicBoolean resultType)
     *
     * This method doesn't throw exceptions.
     *
     * @param sql SQL query to execute
     * @return true if no exception occured during query execution; false otherwise
     */
    public boolean execute(String sql) {
        return execute(sql, null);
    }

    private synchronized Statement createStatement() throws SQLException {
        if (connection == null) {
            throw new SQLException("Not connected"); // NOI18N
        }

        return connection.createStatement();
    }

    /**
     * Executes query and returns ResultSet.
     * This method doesn't throw SQLException. Instead even if exception occurs
     * during interaction with a DB, empty ResultSet is returned.
     *
     * @param sqlQuery Query to execute
     * @return not NULL result set.
     */
    public ResultSet executeQuery(String sqlQuery) {
        ResultSet rs;

        try {
            rs = createStatement().executeQuery(sqlQuery);
        } catch (SQLException ex) {
            log(ex);
            if (emptyResultStatement != null) {
                try {
                    return emptyResultStatement.executeQuery();
                } catch (SQLException ex1) {
                    logger.log(Level.SEVERE, "Unable to get empty ResultSet", ex1); // NOI18N
                }
            }
            rs = null;
        }

        return rs;
    }

    private void log(SQLException ex) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, logPrefix, ex);
        }
    }

    private void log(String message) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, logPrefix.concat(message));
        }
    }

    public void executeUpdate(String sql) throws SQLException {
        Statement stmt = createStatement();

        try {
            stmt.executeUpdate(sql);
        } finally {
            stmt.close();
        }
    }

    public synchronized PreparedStatement prepareStatement(String sql) {
        if (connection == null) {
            return null;
        }

        try {
            return connection.prepareStatement(sql);
        } catch (SQLException ex) {
            log(ex);
            return null;
        }
    }

    public synchronized PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) {
        if (connection == null) {
            return null;
        }

        try {
            return connection.prepareStatement(sql, autoGeneratedKeys);
        } catch (SQLException ex) {
            log(ex);
            return null;
        }
    }

    public synchronized PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) {
        if (connection == null) {
            return null;
        }

        try {
            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException ex) {
            log(ex);
            return null;
        }
    }
}
