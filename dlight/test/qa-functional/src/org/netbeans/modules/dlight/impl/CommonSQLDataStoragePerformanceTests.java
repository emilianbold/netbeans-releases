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
package org.netbeans.modules.dlight.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import static org.junit.Assert.*;

/**
 *
 * @author Alexey Vladykin
 */
public abstract class CommonSQLDataStoragePerformanceTests {

    private SQLDataStorage db;

    protected abstract SQLDataStorage createStorage();

    @Before
    public void setUp() {
        db = createStorage();
        assertNotNull("Unable to create SQLDataStorage DB", db); // NOI18N
    }

    @After
    public void tearDown() {
        boolean shutdownResult = db.shutdown();
        assertTrue(shutdownResult);
    }

    @Test
    public void testAddData_thousands_nonstop() throws Exception {
        doTestAddData("nonstop", 300000, new NoOp(), false);
    }

    @Test
    public void testAddData_thousands_fast() throws Exception {
        doTestAddData("fast", 300000, new MathLoop(10), false);
    }

    @Test
    public void testAddData_millions_fast() throws Exception {
        doTestAddData("fast", 3000000, new MathLoop(10), false);
    }

    @Test
    public void testAddData_thousands_slow() throws Exception {
        doTestAddData("slow", 300000, new MathLoop(100), false);
    }

    @Test
    public void testAddData_millions_slow() throws Exception {
        doTestAddData("slow", 3000000, new MathLoop(100), false);
    }

    @Test
    public void testAddData_thousands_slow_parallel() throws Exception {
        doTestAddData("slow_parallel", 300000, new MathLoop(100), true);
    }

    @Test
    public void testAddData_millions_slow_parallel() throws Exception {
        doTestAddData("slow_parallel", 3000000, new MathLoop(100), true);
    }

    private void doTestAddData(String testName, int dataRowCount, Runnable load, boolean parallel) throws Exception {
        DataTableMetadata table1 = new DataTableMetadata("t1",
                Arrays.asList(new Column("t1c1", Long.class), new Column("t1c2", String.class)), null);

        DataTableMetadata table2 = new DataTableMetadata("t2",
                Arrays.asList(new Column("t2c1", Long.class), new Column("t2c2", String.class)), null);

        DataTableMetadata table3 = new DataTableMetadata("t3",
                Arrays.asList(new Column("t3c1", Long.class), new Column("t3c2", String.class)), null);

        List<DataTableMetadata> tables = Arrays.asList(table1, table2, table3);

        db.createTables(tables);

        long startTime = System.currentTimeMillis();

        if (parallel) {
            doAddDataParallel(tables, dataRowCount, load);
        } else {
            doAddDataSequential(tables, dataRowCount, load);
        }

        long addTime = System.currentTimeMillis();

        db.flush();

        long flushTime = System.currentTimeMillis();
        System.err.printf("%s %d %d %d\n", testName, dataRowCount, addTime - startTime, flushTime - addTime);

        assertTableSize(dataRowCount / 3, table1);
        assertTableSize(dataRowCount / 3, table2);
        assertTableSize(dataRowCount / 3, table3);
    }

    private void doAddDataParallel(List<DataTableMetadata> tables, final int dataRowCount, final Runnable load) throws Exception {
        List<Thread> threads = new ArrayList<Thread>(tables.size());
        for (final DataTableMetadata table : tables) {
            threads.add(new Thread() {
                @Override
                public void run() {
                    for (int i = 0; i < dataRowCount / 3; ++i) {
                        db.addData(table.getName(), Collections.singletonList(
                                new DataRow(table.getColumnNames(), Arrays.asList(Long.valueOf(i), String.valueOf(i)))));
                        load.run();
                    }
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private void doAddDataSequential(List<DataTableMetadata> tables, int dataRowCount, Runnable load) throws Exception {
        for (int i = 0; i < dataRowCount; ++i) {
            addDataRow(tables.get(i % tables.size()), i);
            load.run();
        }
    }

    private void addDataRow(DataTableMetadata table, int i) throws Exception {
        db.addData(table.getName(), Collections.singletonList(
                new DataRow(table.getColumnNames(), Arrays.asList(Long.valueOf(i), String.valueOf(i)))));
    }

    private void assertTableSize(int size, DataTableMetadata table) throws Exception {
        PreparedStatement ps = db.prepareStatement("SELECT COUNT(*) FROM " + table.getName());
        try {
            ResultSet rs = ps.executeQuery();
            try {
                assertTrue(rs.next());
                assertEquals(size, rs.getInt(1));
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
    }

    private static final class NoOp implements Runnable {
        @Override
        public void run() {
            // do nothing
        }
    }

    private static final class MathLoop implements Runnable {
        private final int count;
        public MathLoop(int count) {
            this.count = count;
        }

        @Override
        public void run() {
            for (int j = 0; j < count; ++j) {
                Math.sin(j);
            }
        }
    }
}
