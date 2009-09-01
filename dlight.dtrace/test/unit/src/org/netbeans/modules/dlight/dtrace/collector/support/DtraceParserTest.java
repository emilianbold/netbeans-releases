/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.dtrace.collector.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.api.ThreadDumpQuery;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshot;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshotQuery;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.dtrace.collector.DtraceParser;

/**
 * @author Alexey Vladykin
 */
public class DtraceParserTest extends NbTestCase {

    public DtraceParserTest(String name) {
        super(name);
    }

    @Test
    public void testTwoColumns() throws IOException {
        DataTableMetadata metadata = new DataTableMetadata(
                "dummy",
                Arrays.asList(
                        new Column("timestamp", Long.class),
                        new Column("cpu", Integer.class)),
                null);
        doTest(getDataFile(), new DtraceParser(metadata));
    }

    @Test
    public void testMemD() throws IOException {
        DataTableMetadata metadata = new DataTableMetadata(
                "mem",
                Arrays.asList(
                        new Column("timestamp", Long.class),
                        new Column("kind", Integer.class),
                        new Column("size", Integer.class),
                        new Column("address", Long.class),
                        new Column("total", Integer.class),
                        new Column("stackid", Long.class)),
                null);
        doTest(getDataFile(), new DtraceDataAndStackParser(metadata, new SDSImpl()));
    }

    protected File getDataFile() {
        String fullClassName = this.getClass().getName();
        String dataFilePath = fullClassName.replace('.', '/') + '/' + getName() + ".txt";
        return new File(getDataDir(), dataFilePath);
    }

    protected void doTest(File dataFile, DtraceParser parser) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(dataFile));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                DataRow row = parser.process(line);
                if (row != null) {
                    ref(row.toString());
                }
            }
        } finally {
            reader.close();
        }
        compareReferenceFiles();
    }


    private class SDSImpl implements StackDataStorage {

        private List<String> knownStacks;

        public SDSImpl() {
            knownStacks = new ArrayList<String>();
        }

        public long putStack(List<CharSequence> stack, long sampleDuration) {
            String stackAsString = stack.toString();
            long id = 0;
            while (id < knownStacks.size()) {
                if (knownStacks.get((int)id).equals(stackAsString)) {
                    break;
                }
                ++id;
            }
            if (knownStacks.size() <= id) {
                knownStacks.add((int)id, stackAsString);
            }
            ++id;
            ref("putStack(" + stack + ", " + sampleDuration + ") = " + id);
            return id;
        }

        public List<FunctionCall> getCallStack(long stackId) {
            fail("Parser is not expected to call this method");
            return null;
        }

        public List<FunctionMetric> getMetricsList() {
            fail("Parser is not expected to call this method");
            return null;
        }

        public List<FunctionCallWithMetric> getCallers(FunctionCallWithMetric[] path, boolean aggregate) {
            fail("Parser is not expected to call this method");
            return null;
        }

        public List<FunctionCallWithMetric> getCallees(FunctionCallWithMetric[] path, boolean aggregate) {
            fail("Parser is not expected to call this method");
            return null;
        }

        public List<FunctionCallWithMetric> getHotSpotFunctions(FunctionMetric metric, int limit) {
            fail("Parser is not expected to call this method");
            return null;
        }

        public List<FunctionCallWithMetric> getFunctionsList(DataTableMetadata metadata, List<Column> metricsColumn, FunctionDatatableDescription functionDescription) {
            fail("Parser is not expected to call this method");
            return null;
        }

        public ThreadDump getThreadDump(ThreadDumpQuery query) {
            fail("Parser is not expected to call this method");
            return null;
        }

        public List<ThreadSnapshot> getThreadSnapshots(ThreadSnapshotQuery query) {
            fail("Parser is not expected to call this method");
            return null;
        }
    }
}
