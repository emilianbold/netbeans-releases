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
package org.netbeans.modules.dlight.perfan.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionCallTreeTableNode;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.storage.impl.PerfanDataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class SSStackDataProviderTest {
    private static final List<Column> fcols = Arrays.asList(
            SunStudioDCConfiguration.c_eUser,
            SunStudioDCConfiguration.c_iUser);

    public SSStackDataProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFake() {
        
    }
    /**
     * Test of getCallers method, of class SSStackDataProvider.
     */
//    @Test
    public void testGetCallers() {
        System.out.println("getCallers");
        List<FunctionCallWithMetric> path = null;
        boolean aggregate = false;
        SSStackDataProvider instance = new SSStackDataProvider();
        List<FunctionCallWithMetric> expResult = null;
        List<FunctionCallWithMetric> result = instance.getCallers(path, fcols, null, aggregate);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCallees method, of class SSStackDataProvider.
     */
//    @Test
    public void testGetCallees() {
        System.out.println("getCallees");
        List<FunctionCallWithMetric> path = null;
        boolean aggregate = false;
        SSStackDataProvider instance = new SSStackDataProvider();
        List<FunctionCallWithMetric> expResult = null;
        List<FunctionCallWithMetric> result = instance.getCallees(path, fcols, null, aggregate);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTableView method, of class SSStackDataProvider.
     */
//    @Test
    public void testGetTableView() {
        System.out.println("getTableView");
        List<Column> columns = null;
        List<Column> orderBy = null;
        int limit = 0;
        SSStackDataProvider instance = new SSStackDataProvider();
        List<FunctionCallTreeTableNode> expResult = null;
        List<FunctionCallTreeTableNode> result = instance.getTableView(columns, orderBy, limit);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getChildren method, of class SSStackDataProvider.
     */
//    @Test
    public void testGetChildren() {
        System.out.println("getChildren");
        List<FunctionCallTreeTableNode> path = null;
        SSStackDataProvider instance = new SSStackDataProvider();
        List<FunctionCallTreeTableNode> expResult = null;
        List<FunctionCallTreeTableNode> result = instance.getChildren(path, fcols, null);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getValueAt method, of class SSStackDataProvider.
     */
//    @Test
    public void testGetValueAt() {
        System.out.println("getValueAt");
        int row = 0;
        SSStackDataProvider instance = new SSStackDataProvider();
        FunctionCallTreeTableNode expResult = null;
        FunctionCallTreeTableNode result = instance.getValueAt(row);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTableValueAt method, of class SSStackDataProvider.
     */
//    @Test
    public void testGetTableValueAt() {
        System.out.println("getTableValueAt");
        Column column = null;
        int row = 0;
        SSStackDataProvider instance = new SSStackDataProvider();
        String expResult = "";
        String result = instance.getTableValueAt(column, row);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    static Integer threadCount;
    static final Object lock = new Object();

    /**
     * Test of getHotSpotFunctions method, of class SSStackDataProvider.
     */
//    @Test
    public void testGetHotSpotFunctions() {
        System.out.println("getHotSpotFunctions");
        final List<Column> columns = new ArrayList<Column>();
        final List<Column> orderBy = new ArrayList<Column>();

        columns.add(new Column("name", String.class, "Function Name", null));
        columns.add(new Column("e.user", Double.class, "Exclusive User CPU Time", null));
        columns.add(new Column("i.user", Double.class, "Inclusive User CPU Time", null));

        final int limit = 10;

        final SSStackDataProvider provider = new SSStackDataProvider();
        PerfanDataStorage storage = new PerfanDataStorage();
        storage.init(ExecutionEnvironmentFactory.getLocal(), "/shared/dp/sstrunk/intel-S2", "/var/tmp/dlightExperiment.er", null);
        provider.attachTo(storage);

        int threadLimit = 20;
        threadCount = 1;

        final CountDownLatch latch = new CountDownLatch(threadLimit);

        for (int i = 0; i < threadLimit; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            DLightExecutorService.submit(new Runnable() {
                String prefix;
                
                public void run() {
                    try {
                        synchronized (lock) {
                            prefix = String.valueOf(threadCount++) + ": ";
                        }

                        System.out.println("-------------------- " + prefix + " --------------------");
                        List<FunctionCallWithMetric> result = provider.getHotSpotFunctions(columns, orderBy, limit);
                        int idx = 1;

                        for (FunctionCallWithMetric fc : result) {
                            System.out.println(prefix + (idx++) + " " + fc.toString());
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            }, "getHotSpotFunctions for columns " + columns.toString());


        }

        try {
            latch.await();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println("Done!");

    }

    /**
     * Test of getMetricsList method, of class SSStackDataProvider.
     */
//    @Test
    public void testGetMetricsList() {
        System.out.println("getMetricsList");
        SSStackDataProvider instance = new SSStackDataProvider();
        List<FunctionMetric> expResult = null;
        List<FunctionMetric> result = instance.getMetricsList();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of attachTo method, of class SSStackDataProvider.
     */
//    @Test
    public void testAttachTo() {
        System.out.println("attachTo");
        DataStorage storage = null;
        SSStackDataProvider instance = new SSStackDataProvider();
        instance.attachTo(storage);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}