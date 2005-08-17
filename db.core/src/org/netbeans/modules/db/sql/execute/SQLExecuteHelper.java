/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.execute;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.openide.options.SystemOption;


/**
 * Support class for executing SQL statements.
 *
 * @author Andrei Badea
 */
public final class SQLExecuteHelper {
    
    public static SQLExecutionResult execute(String statements[], Connection conn) throws SQLException {
        List/*<Statement>*/ statementList = new ArrayList();
        List/*<ResultSet>*/ resultSetList = new ArrayList();
                
        int[] typeConcurrency = getSupportedResultSetTypeConcurrency(conn);
        int type = typeConcurrency[0];
        int concurrency = typeConcurrency[1];
        
        for (int i = 0; i < statements.length; i++) {
            Statement stmt = conn.createStatement(type, concurrency);
            statementList.add(stmt);
            
            String sql = removeComments(statements[i]).trim();
            String sqlType = sql.substring(0, Math.min(6, sql.length())).toUpperCase();
            if ("SELECT".equals(sqlType)) { // NOI18N
                ResultSet rs = stmt.executeQuery(sql);
                resultSetList.add(rs);
            } else {
                stmt.executeUpdate(sql);
            }
        }
        
        Statement[] statementArray = (Statement[])statementList.toArray(new Statement[statementList.size()]);
        ResultSet[] resultSetArray = (ResultSet[])resultSetList.toArray(new ResultSet[resultSetList.size()]);
        
        return new SQLExecutionResult(statementArray, resultSetArray);
    }
    
    private static int[] getSupportedResultSetTypeConcurrency(Connection conn) throws SQLException {
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
