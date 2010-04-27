/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.indicators.impl;

import java.awt.Color;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.db.h2.H2DataStorageFactory;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.indicators.DataRowToTimeSeries;
import org.netbeans.modules.dlight.indicators.DetailDescriptor;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import org.netbeans.modules.dlight.indicators.TimeSeriesIndicatorConfiguration;

/**
 * @author Alexey Vladykin
 */
public class TimeSeriesIndicatorTest extends NbTestCase {

    public TimeSeriesIndicatorTest(String name) {
        super(name);
    }

    @Test
    public void testPersistence() throws Exception {
        //Column timeColumn = new Column("timestamp", Long.class);
        Column testColumn = new Column("test", Integer.class);
        IndicatorMetadata metadata = new IndicatorMetadata(Collections.singletonList(testColumn));
        TimeSeriesIndicatorConfiguration conf = new TimeSeriesIndicatorConfiguration(metadata, 0);
        conf.setPersistencePrefix("prefix");
        conf.addTimeSeriesDescriptors(new TimeSeriesDescriptor("t1", "t1 display name", Color.YELLOW, TimeSeriesDescriptor.Kind.LINE));
        conf.addDetailDescriptors(new DetailDescriptor("d1", "d1 display name", "d1 value"));
        conf.setDataRowHandler(new DataRowToTimeSeries() {

            private int rowCount = 0;

            @Override
            public float[] getData(DataRow row) {
                ++rowCount;
                return new float[] {(Integer) row.getData().get(1)};
            }

            @Override
            public Map<String, String> getDetails() {
                return Collections.singletonMap("d1", String.valueOf(rowCount));
            }
        });

        SQLDataStorage sqlStorage = new H2DataStorageFactory().createStorage();

        TimeSeriesIndicator ind1 = (TimeSeriesIndicator) new TimeSeriesIndicatorFactory().create(conf);
        ind1.getComponent(); // for initUI()
        updateIndicator(ind1, 0, 10);
        updateIndicator(ind1, 1, 10);
        updateIndicator(ind1, 2, 10);
        updateIndicator(ind1, 3, 10);
        ind1.tick(); // for details
        File state1 = new File(getWorkDir(), "state1");
        ind1.dumpData(new PrintStream(state1));

        sqlStorage.createTables(ind1.getDataTableMetadata());
        assertTrue(ind1.saveState(sqlStorage));

        TimeSeriesIndicator ind2 = (TimeSeriesIndicator) new TimeSeriesIndicatorFactory().create(conf);
        assertTrue(ind2.loadState(sqlStorage));
        File state2 = new File(getWorkDir(), "state2");
        ind2.dumpData(new PrintStream(state2));

        assertFile(state1, state2);
    }

    private static void updateIndicator(TimeSeriesIndicator ind, long time, int value) {
        ind.updated(Collections.singletonList(
                new DataRow(Arrays.asList("timestamp", "test"), Arrays.asList(time, value))));
    }
}
