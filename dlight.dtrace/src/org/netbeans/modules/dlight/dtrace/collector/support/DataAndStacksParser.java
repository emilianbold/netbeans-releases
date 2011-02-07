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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.dtrace.collector.support;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.StandardColumns;
import org.netbeans.modules.dlight.dtrace.collector.DTraceEventData;
import org.netbeans.modules.dlight.dtrace.collector.DTraceOutputParser;
import org.netbeans.modules.dlight.util.DLightLogger;

/**
 * Parser of dtrace(1M) script output that is used if DtraceDataCollector was 
 * configured with enabled stack support 
 * (see {@link org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration}).
 * <p>
 * In addition to <code>DataOnlyParser</code> parser this one can recognize call
 * stacks produced by using <b>ustack()</b> or <b>stack()</b> in a dtrace script.
 * <p>
 * <b>Output format limitations</b>
 * <p>
 * Line is recognized as a callstack entry if and only if following condition 
 * is true:
 * <pre>
 * !line.trim().isEmpty() && line.trim().indexOf(' ') == 0
 * </pre>
 * So lines that represent data have to have at least two fields.
 * <p>
 * Recognized callstack is associated with the data row that comes 
 * <b>above</b> the callstack and there were <b>no</b> empty lines between
 * the data and the callstack.
 * 
 */
final class DataAndStacksParser implements DTraceOutputParser {

    private static final Logger log = DLightLogger.getLogger(DataAndStacksParser.class);
    private final boolean isProfiling;
    private final LineParser lineParser;
    private final List<String> colNames;
    private List<Object> currData;
    private List<CharSequence> currStack;

    public DataAndStacksParser(DataTableMetadata tableMetadata) {
        assert tableMetadata != null;

        // CallStack is a known table that stores CPU profiling
        // data. For this table we know that we need to put stacks with
        // timestamp and duration.

        isProfiling = tableMetadata.getName().equals("CallStack"); // NOI18N

        ArrayList<Column> columns = new ArrayList<Column>(tableMetadata.getColumns());
        colNames = new ArrayList<String>(tableMetadata.getColumnNames());

        if (colNames.contains(StandardColumns.STACK_COLUMN.getColumnName())) {
            // StackID column is not what could come from script itself
            // So pretend that there is no such coulumn at all.
            // Once callstack is identified it will be returned to the
            // DTraceOutputProcessor and it will add it's ID to this coulumn.
            for (Column column : columns) {
                if (column.getColumnName().equals(StandardColumns.STACK_COLUMN.getColumnName())) {
                    columns.remove(column);
                    break;
                }
            }

            colNames.remove(StandardColumns.STACK_COLUMN.getColumnName());
        }

        // Number of fields is a differentiator.
        // There is an assumption that lines that doesn't contain spaces are
        // part of a callstack. Those lines that have spaces are considered to 
        // be a data.

        assert columns.size() > 1;

        lineParser = new LineParser(columns);
    }

    @Override
    public DTraceEventData parse(String line) {
        String l = line.trim();

        // If this line is empty of is a data line, then return previously 
        // collected data (if any)

        if (l.isEmpty() || l.indexOf(' ') > 0) {
            DTraceEventData result = null;

            if (currData != null) {
                if (currStack != null && currStack.size() > 0) {
                    result = new DTraceEventData(new DataRow(colNames, currData), currStack);
                    currStack = null;
                } else {
                    // Empty line after data.. just return data
                    result = new DTraceEventData(new DataRow(colNames, currData));
                }
            }

            currData = l.isEmpty() ? null : lineParser.parse(l, log);

            return result;
        }

        
        // Consider current line as a part of a call stack
        // But if there was no data captured (first line with stack must follow 
        // data line without empty lines between), *skip* the stack
        
        if (currData == null) {
            return null;
        }
        
        if (currStack == null) {
            currStack = new ArrayList<CharSequence>(10);
        }

        if (isProfiling || !l.startsWith("libc.so.")) { // NOI18N
            currStack.add(l);
        }

        return null;
    }
}
