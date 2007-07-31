/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.execute;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;

/**
 * Support class for executing SQL statements.
 *
 * @author Andrei Badea
 */
public final class SQLExecuteHelper {

    private static final Logger LOGGER = Logger.getLogger(SQLExecuteHelper.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    /**
     * Executes a SQL string, possibly containing multiple statements. Returns the execution
     * result, but only if the string contained a single statement.
     *
     * @param sqlScript the SQL script to execute. If it contains multiple lines
     * they have to be delimited by '\n' characters.
     */
    public static SQLExecutionResults execute(String sqlScript, int startOffset, int endOffset, Connection conn, ProgressHandle progressHandle, SQLExecutionLogger executionLogger) {
        
        boolean cancelled = false;
        
        List<StatementInfo> statements = getStatements(sqlScript, startOffset, endOffset);
        boolean computeResults = statements.size() == 1;
        
        List<SQLExecutionResult> resultList = new ArrayList<SQLExecutionResult>();
        long totalExecutionTime = 0;
        
        for (Iterator i = statements.iterator(); i.hasNext();) {
            
            cancelled = Thread.currentThread().isInterrupted();
            if (cancelled) {
                break;
            }
            
            StatementInfo info = (StatementInfo)i.next();
            String sql = info.getSQL();

            if (LOG) {
                LOGGER.log(Level.FINE, "Executing: " + sql);
            }
            
            SQLExecutionResult result = null;
            Statement stmt = null;

            try {
                if (sql.startsWith("{")) { // NOI18N
                    stmt = conn.prepareCall(sql);
                } else {
                    stmt = conn.createStatement();
                }
                
                boolean isResultSet = false;
                long startTime = System.currentTimeMillis();
                if (stmt instanceof PreparedStatement) {
                    isResultSet = ((PreparedStatement)stmt).execute();
                } else {
                    isResultSet = stmt.execute(sql);
                }
                long executionTime = System.currentTimeMillis() - startTime;
                totalExecutionTime += executionTime;

                if (isResultSet) {
                    result = new SQLExecutionResult(info, stmt, stmt.getResultSet(), executionTime);
                } else {
                    result = new SQLExecutionResult(info, stmt, stmt.getUpdateCount(), executionTime);
                }
            } catch (SQLException e) {
                result = new SQLExecutionResult(info, stmt, e);
            }
            assert result != null;
            
            executionLogger.log(result);
            
            if (LOG) {
                LOGGER.log(Level.FINE, "Result: " + result);
            }
            
            if (computeResults || result.getException() != null) {
                resultList.add(result);
            } else {
                try {
                    result.close();
                } catch (SQLException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            }
        }
        
        if (!cancelled) {
            executionLogger.finish(totalExecutionTime);
        } else {
            if (LOG) {
                LOGGER.log(Level.FINE, "Execution cancelled"); // NOI18N
            }
            executionLogger.cancel();
        }
        
        SQLExecutionResults results = new SQLExecutionResults(resultList);
        if (!cancelled) {
            return results;
        } else {
            results.close();
            return null;
        }
    }
    
    private static int[] getSupportedResultSetTypeConcurrency(Connection conn) throws SQLException {
        // XXX some drivers don't implement the DMD.supportsResultSetConcurrency() method
        // for example the MSSQL WebLogic driver 4v70rel510 always throws AbstractMethodError
        
        DatabaseMetaData dmd = conn.getMetaData();
        
        int type = ResultSet.TYPE_SCROLL_INSENSITIVE;
        int concurrency = ResultSet.CONCUR_UPDATABLE;
        if (!dmd.supportsResultSetConcurrency(type, concurrency)) {
            concurrency = ResultSet.CONCUR_READ_ONLY;
            if (!dmd.supportsResultSetConcurrency(type, concurrency)) {
                type = ResultSet.TYPE_FORWARD_ONLY;
            }
        }
        return new int[] { type, concurrency };
    }
    
    private static List<StatementInfo> getStatements(String script, int startOffset, int endOffset) {
        List<StatementInfo> allStatements = split(script);
        if (startOffset == 0 && endOffset == script.length()) {
            return allStatements;
        }
        List<StatementInfo> statements = new ArrayList<StatementInfo>();
        for (Iterator i = allStatements.iterator(); i.hasNext();) {
            StatementInfo stmt = (StatementInfo)i.next();
            if (startOffset == endOffset) {
                // only find the statement at offset startOffset
                if (stmt.getRawStartOffset() <= startOffset && stmt.getRawEndOffset() >= endOffset) {
                    statements.add(stmt);
                }
            } else {
                // find the statements between startOffset and endOffset
                if (stmt.getStartOffset() >= startOffset && stmt.getEndOffset() <= endOffset) {
                    statements.add(stmt);
                }
            }
        }
        return Collections.unmodifiableList(statements);
    }
    
    static List<StatementInfo> split(String script) {
        return new SQLSplitter(script).getStatements();
    }
    
    private static final class SQLSplitter {
        
        private static final int STATE_MEANINGFUL_TEXT = 0;
        private static final int STATE_MAYBE_LINE_COMMENT = 1;
        private static final int STATE_LINE_COMMENT = 2;
        private static final int STATE_MAYBE_BLOCK_COMMENT = 3;
        private static final int STATE_BLOCK_COMMENT = 4;
        private static final int STATE_MAYBE_END_BLOCK_COMMENT = 5;
        private static final int STATE_STRING = 6;
        
        private String sql;
        private int sqlLength;
        
        private StringBuffer statement = new StringBuffer();
        private List<StatementInfo> statements = new ArrayList<StatementInfo>();
        
        private int pos = 0;
        private int line = -1;
        private int column;
        private boolean wasEOL = true;
        
        private int rawStartOffset;
        private int startOffset;
        private int startLine;
        private int startColumn;
        private int endOffset;
        private int rawEndOffset;
        
        private int state = STATE_MEANINGFUL_TEXT;
        
        /**
         * @param sql the SQL string to parse. If it contains multiple lines
         * they have to be delimited by '\n' characters.
         */
        public SQLSplitter(String sql) {
            assert sql != null;
            this.sql = sql;
            sqlLength = sql.length();
            parse();
        }
        
        private void parse() {
            while (pos < sqlLength) {
                char ch = sql.charAt(pos);
                
                if (ch == '\r') { // NOI18N
                    // the string should not contain these
                    if (LOG) {
                        LOGGER.log(Level.FINE, "The SQL string contained non-supported \r characters."); // NOI18N
                    }
                    continue;
                }
                
                if (wasEOL) {
                    line++;
                    column = 0;
                    wasEOL = false;
                } else {
                    column++;
                }
                
                if (ch == '\n') {
                    wasEOL = true;
                }
                
                switch (state) {
                    case STATE_MEANINGFUL_TEXT:
                        if (ch == '-') {
                            state = STATE_MAYBE_LINE_COMMENT;
                        }
                        if (ch == '/') {
                            state = STATE_MAYBE_BLOCK_COMMENT;
                        }
                        if (ch == '\'') {
                            state = STATE_STRING;
                        }
                        break;
                        
                    case STATE_MAYBE_LINE_COMMENT:
                        if (ch == '-') {
                            state = STATE_LINE_COMMENT;
                        } else {
                            state = STATE_MEANINGFUL_TEXT;
                            statement.append('-'); // previous char
                            endOffset = pos;
                        }
                        break;
                        
                    case STATE_LINE_COMMENT:
                        if (ch == '\n') {
                            state = STATE_MEANINGFUL_TEXT;
                            // avoid appending the final \n to the result
                            pos++;
                            continue;
                        } 
                        break;
                        
                    case STATE_MAYBE_BLOCK_COMMENT:
                        if (ch == '*') {
                            state = STATE_BLOCK_COMMENT;
                        } else {
                            statement.append('/'); // previous char
                            endOffset = pos;
                            if (ch != '/') {
                                state = STATE_MEANINGFUL_TEXT;
                            }
                        }
                        break;
                        
                    case STATE_BLOCK_COMMENT:
                        if (ch == '*') {
                            state = STATE_MAYBE_END_BLOCK_COMMENT;
                        }
                        break;
                        
                    case STATE_MAYBE_END_BLOCK_COMMENT:
                        if (ch == '/') {
                            state = STATE_MEANINGFUL_TEXT;
                            // avoid writing the final / to the result
                            pos++;
                            continue;
                        } else if (ch != '*') {
                            state = STATE_BLOCK_COMMENT;
                        }
                        break;
                        
                    case STATE_STRING:
                        if (ch == '\n' || ch == '\'') {
                            state = STATE_MEANINGFUL_TEXT;
                        }
                        break;
                        
                    default:
                        assert false;
                }
                
                if (state == STATE_MEANINGFUL_TEXT && ch == ';') {
                    rawEndOffset = pos;
                    addStatement();
                    statement.setLength(0);
                    rawStartOffset = pos + 1; // skip the semicolon
                } else {
                    if (state == STATE_MEANINGFUL_TEXT || state == STATE_STRING) {
                        // don't append leading whitespace
                        if (statement.length() > 0 || !Character.isWhitespace(ch)) {
                            // remember the position of the first appended char
                            if (statement.length() == 0) {
                                startOffset = pos;
                                endOffset = pos;
                                startLine = line;
                                startColumn = column;
                            }
                            statement.append(ch);
                            // the end offset is the character after the last non-whitespace character
                            if (state == STATE_STRING || !Character.isWhitespace(ch)) {
                                endOffset = pos + 1;
                            }
                        }
                    }
                }
                
                pos++;
            }
            
            rawEndOffset = pos;
            addStatement();
        }
        
        private void addStatement() {
            // PENDING since startOffset is the first non-whitespace char and
            // endOffset is the offset after the last non-whitespace char,
            // the trim() call could be replaced with statement.substring(startOffset, endOffset)
            String sql = statement.toString().trim();
            if (sql.length() <= 0) {
                return;
            }
            
            StatementInfo info = new StatementInfo(sql, rawStartOffset, startOffset, startLine, startColumn, endOffset, rawEndOffset);
            statements.add(info);
        }
        
        public List<StatementInfo> getStatements() {
            return Collections.unmodifiableList(statements);
        }
    }
}
