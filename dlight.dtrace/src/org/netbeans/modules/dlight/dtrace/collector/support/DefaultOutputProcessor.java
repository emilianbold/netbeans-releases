/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.dtrace.collector.support;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataUtil;
import org.netbeans.modules.dlight.api.storage.StandardColumns;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.dtrace.collector.DTraceEventData;
import org.netbeans.modules.dlight.dtrace.collector.DTraceOutputParser;
import org.openide.util.Exceptions;

/**
 * Processor of DTrace script output that comes from a _single_ script.
 * Even if several scripts were merged before dtrace(1M) was started, this 
 * processor will get data as if it comes from a single script (de-multiplexed).
 * 
 * @author ak119685
 */
final class DefaultOutputProcessor implements OutputProcessor {

    private static final Tracer tracer = Boolean.getBoolean("dlight.dns.parser.trace") ? new Tracer() : null; // NOI18N
    private final DataTableMetadata tableMetaData;
    private final int durationColumnIdx;
    private final int timestampColumnIdx;
    private final int stackRefColumnIdx;
    private DtraceDataCollector collector;
    private DTraceOutputParser parser;
    private StackDataStorage stackStorage;

    DefaultOutputProcessor(DataTableMetadata tableMetaData) {
        this.tableMetaData = tableMetaData;
        final List<String> columnNames = tableMetaData.getColumnNames();
        durationColumnIdx = columnNames.indexOf(StandardColumns.DURATION_COLUMN.getColumnName());
        timestampColumnIdx = columnNames.indexOf(StandardColumns.TIMESTAMP_COLUMN.getColumnName());
        stackRefColumnIdx = columnNames.indexOf(StandardColumns.STACK_COLUMN.getColumnName());
    }

    @Override
    public void init(DtraceDataCollector collector, StackDataStorage stackStorage) {
        this.collector = collector;
        this.parser = collector.getParser();
        this.stackStorage = stackStorage;
    }

    @Override
    public void processLine(String line) {
        if (tracer != null) {
            tracer.trace(line);
        }
        try {
            DTraceEventData data = parser.parse(line);

            // If no data formed yet - just return...

            if (data == null) {
                return;
            }

            DataRow dataRow = data.getDataRow();

            final List<CharSequence> callStack = data.getEventCallStack();

            // If parser has identified a stack we need to decide how to add it
            // and should it's leaf_id be added to a data or not...

            if (stackStorage != null && callStack != null) {
                Long stackID;

                if (timestampColumnIdx == -1 || durationColumnIdx == -1) {
                    //TODO: define context
                    stackID = stackStorage.putStack(-1, callStack);
                } else {
                    long time = DataUtil.toLong(dataRow.getData().get(timestampColumnIdx));
                    long duration = DataUtil.toLong(dataRow.getData().get(durationColumnIdx));
                    //TODO: define context
                    stackID = stackStorage.putSample(-1, callStack, time, duration);
                }

                if (stackRefColumnIdx >= 0) {
                    List<? extends Object> originalData = dataRow.getData();
                    List<Object> dataWithStackID = new ArrayList<Object>(originalData.size() + 1);
                    dataWithStackID.addAll(originalData);
                    dataWithStackID.add(stackRefColumnIdx, stackID);
                    dataRow = new DataRow(tableMetaData.getColumnNames(), dataWithStackID);
                }
            }
            collector.addDataRow(dataRow);
        } catch (Throwable th) {
            Exceptions.printStackTrace(th);
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void close() {
        processLine(""); // NOI18N
        collector.packageVisibleSuggestIndicatorsRepaint();
    }
}
