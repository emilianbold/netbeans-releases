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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.openide.ErrorManager;


/**
 * Support class for executing SQL statements.
 *
 * @author Andrei Badea
 */
public final class SQLExecuteHelper {

    private static final ErrorManager LOGGER = ErrorManager.getDefault().getInstance(SQLExecuteHelper.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(ErrorManager.INFORMATIONAL);
    
    public static SQLExecutionResults execute(String statements[], Connection conn) throws SQLException {
        List/*<SQLExecutionResults>*/ resultList = new ArrayList();
                
        for (int i = 0; i < statements.length; i++) {
            String sql = removeComments(statements[i]).trim();
            if (LOG) {
                LOGGER.log(ErrorManager.INFORMATIONAL, "Executing: " + sql); // NOI18N
            }
            
            SQLExecutionResult result = null;
            String sqlType = sql.substring(0, Math.min(6, sql.length())).toUpperCase();
            
            // XXX detect procedures call better
            // will be fixed when we support the execution of multiple statements
            if (sqlType.startsWith("{")) { // NOI18N
                CallableStatement stmt = conn.prepareCall(sql);
                if (stmt.execute()) {
                    result = new SQLExecutionResult(stmt, stmt.getResultSet());
                } else {
                    result = new SQLExecutionResult(stmt, stmt.getUpdateCount());
                }
            } else {
                Statement stmt = conn.createStatement();
                if ("SELECT".equals(sqlType)) { // NOI18N
                    result = new SQLExecutionResult(stmt, stmt.executeQuery(sql));
                } else {
                    result = new SQLExecutionResult(stmt, stmt.executeUpdate(sql));
                }
            }
            
            assert result != null;
            if (LOG) {
                if (result.getResultSet() != null) {
                    LOGGER.log(ErrorManager.INFORMATIONAL, "Result: " + result.getResultSet()); // NOI18N
                } else {
                    LOGGER.log(ErrorManager.INFORMATIONAL, "Result: " + result.getRowCount() + " rows affected"); // NOI18N
                }
            }
            resultList.add(result);
        }
        
        SQLExecutionResult[] resultArray = (SQLExecutionResult[])resultList.toArray(new SQLExecutionResult[resultList.size()]);
        return new SQLExecutionResults(resultArray);
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
    
    static String removeComments(String sql) {
        return new CommentRemover(sql).getResult();
    }
    
    private static final class CommentRemover {
        
        private static final int STATE_START = 0;
        private static final int STATE_MAYBE_LINE_COMMENT = 1;
        private static final int STATE_LINE_COMMENT = 2;
        private static final int STATE_MAYBE_BLOCK_COMMENT = 3;
        private static final int STATE_BLOCK_COMMENT = 4;
        private static final int STATE_MAYBE_END_BLOCK_COMMENT = 5;
        
        private String sql;
        private StringBuffer result = new StringBuffer();
        private int length;
        private int pos = 0;
        private int state = STATE_START;
        
        public CommentRemover(String sql) {
            assert sql != null;
            this.sql = sql;
            length = sql.length();
            parse();
        }
        
        private void parse() {
            while (pos < length) {
                char ch = sql.charAt(pos);
                
                switch (state) {
                    case STATE_START:
                        if (ch == '-') {
                            state = STATE_MAYBE_LINE_COMMENT;
                        }
                        if (ch == '/') {
                            state = STATE_MAYBE_BLOCK_COMMENT;
                        }
                        break;
                        
                    case STATE_MAYBE_LINE_COMMENT:
                        if (ch == '-') {
                            state = STATE_LINE_COMMENT;
                        } else {
                            state = STATE_START;
                            result.append('-'); // previous char
                        }
                        break;
                        
                    case STATE_LINE_COMMENT:
                        if (ch == '\n') {
                            state = STATE_START;
                            // avoid appending the final \n to the result
                            pos++;
                            continue;
                        } 
                        break;
                        
                    case STATE_MAYBE_BLOCK_COMMENT:
                        if (ch == '*') {
                            state = STATE_BLOCK_COMMENT;
                        } else {
                            result.append('/'); // previous char
                            if (ch != '/') {
                                state = STATE_START;
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
                            state = STATE_START;
                            // avoid writing the final / to the result
                            pos++;
                            continue;
                        } else if (ch != '*') {
                            state = STATE_BLOCK_COMMENT;
                        }
                        break;
                        
                    default:
                        assert false;
                }
                
                if (state == STATE_START) {
                    result.append(ch);
                }
                pos++;
            }
        }
        
        public String getResult() {
            return result.toString();
        }
    }
}
