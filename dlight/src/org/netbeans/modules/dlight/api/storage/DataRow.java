/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.storage;

import java.util.List;
import org.netbeans.modules.dlight.util.DLightLogger;

/**
 * Represents one row of data along with column names
 */
public final class DataRow {

    private List<String> colnames;
    private List<? extends Object> data;

    /**
     * Created new DataRow instance with the <code>colnames</code> column names
     * and <code>data</code>
     * @param colnames column names
     * @param data data
     */
    public DataRow(List<String> colnames, List<? extends Object> data) {
        this.colnames = colnames;
        DLightLogger.assertTrue(data != null, "data parameter should not be null"); //NOI18N
        this.data = data;
    }

    /**
     * Returns value of the row as Long for column with the name <code>columnName</code>
     * @param columnName column name to get Long value for
     * @return value of row for the column as Long
     */
    public Long getLongValue(String columnName) {
        Long result = null;
        int idx = colnames.indexOf(columnName);
        if (idx >= 0) {
            result = (Long) data.get(idx);
        }
        return result;
    }

    /**
     * Returns value of the row as String for column with the name <code>columnName</code>
     * @param columnName column name to get String value for
     * @return value of row for the column as String
     */
    public String getStringValue(String columnName) {
        return getStringValue(colnames.indexOf(columnName));
    }

    public String getStringValue(int idx) {
        String result = null;
        if (idx >= 0) {
            result = String.valueOf(data.get(idx));
        }
        return result;
    }

    public Double getDoubleValue(String columnName) {
        return getDoubleValue(colnames.indexOf(columnName));
    }

    public Double getDoubleValue(int idx) {
        Double result = null;
        if (idx >= 0) {
            result = (Double) data.get(idx);
        }
        return result;
    }

    public Float getFloatValue(String columnName) {
        return getFloatValue(colnames.indexOf(columnName));
    }

    public Float getFloatValue(int idx) {
        Float result = null;
        if (idx >= 0) {
            result = (Float) data.get(idx);
        }
        return result;
    }

    /**
     * Return this row column names
     * @return column names
     */
    public List<String> getColumnNames() {
        return colnames;
    }

    /**
     * Returns data this row contains as Object
     * @return data
     */
    public List<? extends Object> getData() {
        return data;
    }

    /**
     * Returns data for column with the name <code>columnName</code>
     * @param columnName column name
     * @return return value for the column with <code>columnName</code>
     */
    public Object getData(String columnName) {
        int idx = colnames.indexOf(columnName);
        if (idx >= 0) {
            return data.get(idx);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(" *"); //NOI18N
        for (String n : colnames) {
            sb.append(n).append("* | *"); //NOI18N
        }

        sb.append("\n"); //NOI18N

        for (Object v : data) {
            sb.append(v.toString()).append(" | "); //NOI18N
        }

        return sb.toString();
    }
}
