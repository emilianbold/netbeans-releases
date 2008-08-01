/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.db.sql.loader;

import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import org.netbeans.modules.db.sql.execute.SQLExecutionLogger;
import org.netbeans.modules.db.sql.execute.SQLExecutionResult;
import org.netbeans.modules.db.sql.execute.StatementInfo;
import org.openide.cookies.LineCookie;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Andrei Badea
 */
public class SQLExecutionLoggerImpl implements SQLExecutionLogger {

    private final LineCookie lineCookie;
    private final InputOutput inputOutput;

    private boolean inputOutputSelected = false;
    private int errorCount;

    public SQLExecutionLoggerImpl(String displayName, LineCookie lineCookie) {
        this.lineCookie = lineCookie;

        String ioName = NbBundle.getMessage(SQLEditorSupport.class, "LBL_SQLFileExecution", displayName);
        inputOutput = IOProvider.getDefault().getIO(ioName, true);
    }

    public SQLExecutionLoggerImpl(String displayName) {
        this(displayName, null);
    }

    public void log(SQLExecutionResult result) {
        if (result.hasExceptions()) {
            logException(result);
        } else {
            logSuccess(result);
        }
    }

    public void finish(long executionTime) {
        OutputWriter writer = inputOutput.getOut();
        writer.println(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutionFinished",
                String.valueOf(millisecondsToSeconds(executionTime)),
                String.valueOf(errorCount)));
        writer.println(""); // NOI18N
    }

    public void cancel() {
        OutputWriter writer = inputOutput.getErr();
        writer.println(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutionCancelled"));
        writer.println(""); // NOI18N
    }

    public void close() {
        inputOutput.closeInputOutput();
    }

    private void logException(SQLExecutionResult result) {
        errorCount++;

        if (!inputOutputSelected) {
            inputOutputSelected = true;
            inputOutput.select();
        }

        OutputWriter writer = inputOutput.getErr();

        for(Throwable e: result.getExceptions()) {
            if (e instanceof SQLException) {
                writeSQLException((SQLException)e, writer);
            } else {
                Exceptions.printStackTrace(e);
            }
        }
        
        printLineColumn(writer, result.getStatementInfo(), true);
        writer.println(""); // NOI18N
    }

    private void writeSQLException(SQLException e, OutputWriter writer) {
        while (e != null) {
            writer.println(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ErrorCodeStateMessage",
                    String.valueOf(e.getErrorCode()),
                    e.getSQLState(),
                    e.getMessage()));

            e = e.getNextException();
        }
    }

    private void logSuccess(SQLExecutionResult result) {
        OutputWriter writer = inputOutput.getOut();

        String executionTimeStr = millisecondsToSeconds(result.getExecutionTime());
        String successLine = null;
        if (result.getUpdateCount() >= 0) {
            successLine = NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutedSuccessfullyTimeRows",
                    String.valueOf(executionTimeStr),
                    String.valueOf(result.getUpdateCount()));
        } else {
            successLine = NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutedSuccessfullyTime",
                    String.valueOf(executionTimeStr));
        }
        writer.println(successLine);
        printLineColumn(writer, result.getStatementInfo(), false);
        writer.println(""); // NOI18N
    }

    private void printLineColumn(OutputWriter writer, StatementInfo statementInfo, boolean hyperlink) {
        String lineColumn = NbBundle.getMessage(SQLEditorSupport.class, "LBL_LineColumn",
                String.valueOf(statementInfo.getStartLine() + 1),
                String.valueOf(statementInfo.getStartColumn() + 1));
        try {
            if (hyperlink) {
                writer.println(lineColumn, new Hyperlink(statementInfo.getStartLine(), statementInfo.getStartColumn()));
            } else {
                writer.println(lineColumn);
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private String millisecondsToSeconds(long ms) {
        NumberFormat fmt = NumberFormat.getInstance();
        fmt.setMaximumFractionDigits(3);
        return fmt.format(ms / 1000.0);
    }

    /**
     * Represents a hyperlinked line in an InputOutput.
     */
    private final class Hyperlink implements OutputListener {

        private final int line;
        private final int column;

        public Hyperlink(int line, int column) {
            this.line = line;
            this.column = column;
        }

        public void outputLineSelected(OutputEvent ev) {
            goToLine(false);
        }

        public void outputLineCleared(OutputEvent ev) {
        }

        public void outputLineAction(OutputEvent ev) {
            goToLine(true);
        }

        private void goToLine(boolean focus) {
            Line l = lineCookie.getLineSet().getOriginal(line);
            if (!l.isDeleted()) {
                l.show(focus ? Line.SHOW_GOTO : Line.SHOW_TRY_SHOW, column);
            }
        }
    }
}
