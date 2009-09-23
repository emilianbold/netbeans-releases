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
package org.netbeans.modules.dlight.indicators.support;

import org.netbeans.modules.dlight.indicators.DataRowToTimeSeries;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.DataUtil;

/**
 * Default {@link DataRowToTimeSeries} implementation, suitable for simple cases.
 *
 * @author Alexey Vladykin
 */
public final class DefaultDataRowToTimeSeries implements DataRowToTimeSeries {

    private final float[] data;
    private final Map<String, Integer> columnToIndex;

    public DefaultDataRowToTimeSeries(Column[][] columns) {
        this.data = new float[columns.length];
        this.columnToIndex = Collections.unmodifiableMap(buildColumnToIndexMap(columns));
    }

    public synchronized void addDataRow(DataRow row) {
        for (String columnName : row.getColumnNames()) {
            Integer index = columnToIndex.get(columnName);
            if (index != null) {
                data[index] = DataUtil.toFloat(row.getData(columnName));
            }
        }
    }

    public synchronized void tick(float[] data, Map<String, String> details) {
        System.arraycopy(this.data, 0, data, 0, Math.min(this.data.length, data.length));
    }

    private static Map<String, Integer> buildColumnToIndexMap(Column[][] columns) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (int i = 0; i < columns.length; ++i) {
            for (Column column : columns[i]) {
                result.put(column.getColumnName(), Integer.valueOf(i));
            }
        }
        return result;
    }
}
