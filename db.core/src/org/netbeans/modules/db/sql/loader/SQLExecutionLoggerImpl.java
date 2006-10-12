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

package org.netbeans.modules.db.sql.loader;

import java.io.IOException;
import java.text.NumberFormat;
import org.netbeans.modules.db.sql.execute.SQLExecutionLogger;
import org.netbeans.modules.db.sql.execute.SQLExecutionResult;
import org.netbeans.modules.db.sql.execute.StatementInfo;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.text.Line;
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

    public void log(SQLExecutionResult result) {
        if (result.getException() != null) {
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

        writer.println(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ErrorCodeStateMessage",
                String.valueOf(result.getException().getErrorCode()),
                result.getException().getSQLState(),
                result.getException().getMessage()));
        printLineColumn(writer, result.getStatementInfo(), true);
        writer.println(""); // NOI18N
    }

    private void logSuccess(SQLExecutionResult result) {
        OutputWriter writer = inputOutput.getOut();

        String executionTimeStr = millisecondsToSeconds(result.getExecutionTime());
        String successLine = null;
        if (result.getRowCount() >= 0) {
            successLine = NbBundle.getMessage(SQLEditorSupport.class, "LBL_ExecutedSuccessfullyTimeRows",
                    String.valueOf(executionTimeStr),
                    String.valueOf(result.getRowCount()));
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
            ErrorManager.getDefault().notify(e);
        }
    }

    private String millisecondsToSeconds(long ms) {
        NumberFormat fmt = NumberFormat.getInstance();
        fmt.setMaximumFractionDigits(3);
        return fmt.format(ms / 1000.0);
    }

    public void logResultSetException(Exception e) {
        inputOutput.select();
        OutputWriter writer = inputOutput.getErr();

        writer.println(NbBundle.getMessage(SQLEditorSupport.class, "LBL_ResultSetErrorDetailed",
                e.getMessage()));
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
