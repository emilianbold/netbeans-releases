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

package org.netbeans.modules.dlight.core.stack.storage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.api.storage.types.Time;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public abstract class CommonStackDataStorageTests {

    private StackDataStorage db;

    protected abstract StackDataStorage createStorage();

    protected abstract boolean shutdownStorage(StackDataStorage db);

    protected abstract void flush(StackDataStorage db);

    @Before
    public void setUp() {
        db = createStorage();
        assertNotNull("Unable to create StackDataStorage DB", db); // NOI18N
    }

    @After
    public void tearDown() {
        assertTrue(shutdownStorage(db));
    }

    @Test
    public void testSimple() {
        db.putSample(Arrays.<CharSequence>asList("func1"), 0, 10);
        db.putSample(Arrays.<CharSequence>asList("func1", "func2"), 1, 10);
        db.putSample(Arrays.<CharSequence>asList("func1", "func2", "func3"), 2, 10);
        db.putSample(Arrays.<CharSequence>asList("func1", "func2", "func3", "func4"), 3, 10);
        flush(db);

        List<FunctionCallWithMetric> hotSpots = db.getHotSpotFunctions(FunctionMetric.CpuTimeInclusiveMetric, Collections.<DataFilter>emptyList(), 10);
        assertEquals(4, hotSpots.size());

        assertEquals("func1", hotSpots.get(0).getFunction().getName());
        assertTimeEquals(40, hotSpots.get(0).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(10, hotSpots.get(0).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func2", hotSpots.get(1).getFunction().getName());
        assertTimeEquals(30, hotSpots.get(1).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(10, hotSpots.get(1).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func3", hotSpots.get(2).getFunction().getName());
        assertTimeEquals(20, hotSpots.get(2).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(10, hotSpots.get(2).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func4", hotSpots.get(3).getFunction().getName());
        assertTimeEquals(10, hotSpots.get(3).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(10, hotSpots.get(3).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        List<FunctionCallWithMetric> callees = db.getCallees(Arrays.asList(hotSpots.get(0)), Collections.<Column>emptyList(), Collections.<Column>emptyList(), true);
        assertEquals(1, callees.size());
        assertEquals("func2", callees.get(0).getFunction().getName());

        List<FunctionCallWithMetric> callers = db.getCallers(Arrays.asList(hotSpots.get(3)), Collections.<Column>emptyList(), Collections.<Column>emptyList(), true);
        assertEquals(1, callers.size());
        assertEquals("func3", callers.get(0).getFunction().getName());
    }

    @Test
    public void testCallersCallees() {
        db.putSample(Arrays.<CharSequence>asList("func1", "func1"), 0, 10);
        db.putSample(Arrays.<CharSequence>asList("func2", "func1"), 1, 10);
        db.putSample(Arrays.<CharSequence>asList("func1", "func2", "func3"), 2, 10);
        db.putSample(Arrays.<CharSequence>asList("func3", "func2", "func1"), 3, 10);
        flush(db);

        List<FunctionCallWithMetric> hotSpots = db.getHotSpotFunctions(FunctionMetric.CpuTimeInclusiveMetric, Collections.<DataFilter>emptyList(), 10);
        assertEquals(3, hotSpots.size());

        assertEquals("func1", hotSpots.get(0).getFunction().getName());
        assertTimeEquals(40, hotSpots.get(0).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(30, hotSpots.get(0).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func2", hotSpots.get(1).getFunction().getName());
        assertTimeEquals(30, hotSpots.get(1).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, hotSpots.get(1).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func3", hotSpots.get(2).getFunction().getName());
        assertTimeEquals(20, hotSpots.get(2).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(10, hotSpots.get(2).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        List<FunctionCallWithMetric> callees = db.getCallees(Arrays.asList(hotSpots.get(0)), Collections.<Column>emptyList(), Collections.<Column>emptyList(), true);
        assertEquals(2, callees.size());
        Collections.sort(callees, new FunctionCallComparator());

        assertEquals("func1", callees.get(0).getFunction().getName());
        assertTimeEquals(10, callees.get(0).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(10, callees.get(0).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func2", callees.get(1).getFunction().getName());
        assertTimeEquals(10, callees.get(1).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, callees.get(1).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        List<FunctionCallWithMetric> callers = db.getCallers(Arrays.asList(hotSpots.get(0)), Collections.<Column>emptyList(), Collections.<Column>emptyList(), true);
        assertEquals(2, callers.size());
        Collections.sort(callers, new FunctionCallComparator());

        assertEquals("func1", callers.get(0).getFunction().getName());
        assertTimeEquals(20, callers.get(0).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, callers.get(0).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("func2", callers.get(1).getFunction().getName());
        assertTimeEquals(20, callers.get(1).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, callers.get(1).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));
    }

    @Test
    public void testDeepCallers() {
        db.putSample(Arrays.<CharSequence>asList("x", "a", "b", "c", "x"), 0, 10);
        db.putSample(Arrays.<CharSequence>asList("x", "a", "b", "x"), 1, 10);
        db.putSample(Arrays.<CharSequence>asList("a", "b", "c"), 2, 10);
        db.putSample(Arrays.<CharSequence>asList("x", "x", "a", "b", "c"), 3, 10);
        db.putSample(Arrays.<CharSequence>asList("x", "b", "c"), 4, 10);
        flush(db);

        List<FunctionCallWithMetric> hotSpots = db.getHotSpotFunctions(FunctionMetric.CpuTimeInclusiveMetric, Collections.<DataFilter>emptyList(), 10);
        assertEquals(4, hotSpots.size());

        FunctionCallWithMetric c = find(hotSpots, "c");
        List<FunctionCallWithMetric> cCallers = db.getCallers(Arrays.asList(c), Collections.<Column>emptyList(), Collections.<Column>emptyList(), true);
        assertEquals(1, cCallers.size());

        FunctionCallWithMetric b = cCallers.get(0);
        assertEquals("b", b.getFunction().getName());
        assertTimeEquals(50, b.getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, b.getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));
        List<FunctionCallWithMetric> bCallers = db.getCallers(Arrays.asList(b, c), Collections.<Column>emptyList(), Collections.<Column>emptyList(), true);
        assertEquals(2, bCallers.size());

        FunctionCallWithMetric a = find(bCallers, "a");
        assertNotNull(a);
        assertTimeEquals(40, a.getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, a.getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));
        List<FunctionCallWithMetric> aCallers = db.getCallers(Arrays.asList(a, b, c), Collections.<Column>emptyList(), Collections.<Column>emptyList(), true);
        assertEquals(1, aCallers.size());

        FunctionCallWithMetric x = aCallers.get(0);
        assertTimeEquals(50, x.getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(0, x.getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));
    }

    @Test
    public void testDeepCallees() {
        db.putSample(Arrays.<CharSequence>asList("a", "b", "c", "d", "e"), 0, 10);
        db.putSample(Arrays.<CharSequence>asList("a", "b", "c", "d", "f"), 1, 10);
        db.putSample(Arrays.<CharSequence>asList("b", "c", "d", "e"), 2, 10);
        db.putSample(Arrays.<CharSequence>asList("b", "c", "d", "f"), 3, 10);
        db.putSample(Arrays.<CharSequence>asList("c", "d", "e"), 4, 10);
        db.putSample(Arrays.<CharSequence>asList("c", "d", "f"), 5, 10);
        flush(db);

        List<FunctionCallWithMetric> hotSpots = db.getHotSpotFunctions(FunctionMetric.CpuTimeInclusiveMetric, Collections.<DataFilter>emptyList(), 10);
        assertEquals(6, hotSpots.size());

        FunctionCallWithMetric b = find(hotSpots, "b");
        assertNotNull(b);
        List<FunctionCallWithMetric> bCallees = db.getCallees(Arrays.asList(b), Collections.<Column>emptyList(), Collections.<Column>emptyList(), true);
        assertEquals(1, bCallees.size());
        assertEquals("c", bCallees.get(0).getFunction().getName());

        FunctionCallWithMetric c = bCallees.get(0);
        List<FunctionCallWithMetric> cCallees = db.getCallees(Arrays.asList(b, c), Collections.<Column>emptyList(), Collections.<Column>emptyList(), true);
        assertEquals(1, cCallees.size());
        assertEquals("d", cCallees.get(0).getFunction().getName());

        FunctionCallWithMetric d = cCallees.get(0);
        List<FunctionCallWithMetric> dCallees = db.getCallees(Arrays.asList(b, c, d), Collections.<Column>emptyList(), Collections.<Column>emptyList(), true);
        assertEquals(2, dCallees.size());
        Collections.sort(dCallees, new FunctionCallComparator());

        assertEquals("e", dCallees.get(0).getFunction().getName());
        assertTimeEquals(20, dCallees.get(0).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(20, dCallees.get(0).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));

        assertEquals("f", dCallees.get(1).getFunction().getName());
        assertTimeEquals(20, dCallees.get(1).getMetricValue(FunctionMetric.CpuTimeInclusiveMetric));
        assertTimeEquals(20, dCallees.get(1).getMetricValue(FunctionMetric.CpuTimeExclusiveMetric));
    }

    @Test
    public void testLongNames() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 256; ++j) {
                longName.append("x");
            }
            db.putSample(Arrays.<CharSequence>asList(longName), i, 10);
        }
        flush(db);

        List<FunctionCallWithMetric> hotSpots = db.getHotSpotFunctions(FunctionMetric.CpuTimeInclusiveMetric, Collections.<DataFilter>emptyList(), 10);
        assertEquals(10, hotSpots.size());
    }

    private static void assertTimeEquals(long nanos, Object obj) {
        assertTrue(obj instanceof Time);
        assertEquals(nanos, ((Time)obj).getNanos());
    }

    private static FunctionCallWithMetric find(List<FunctionCallWithMetric> list, String name) {
        for (FunctionCallWithMetric call : list) {
            if (call.getFunction().getName().equals(name)) {
                return call;
            }
        }
        return null;
    }

    private static class FunctionCallComparator implements Comparator<FunctionCallWithMetric> {
        public int compare(FunctionCallWithMetric c1, FunctionCallWithMetric c2) {
            return c1.getFunction().getName().compareTo(c2.getFunction().getName());
        }
    }

}
