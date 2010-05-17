/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.visualweb.dataconnectivity.sql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Just in time table meta data.  Cached after retreived.
 *
 * @author John Kline
 */
public class ProcedureMetaData {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.sql.Bundle",
        Locale.getDefault());

    public static final int PROCEDURE_CAT   = 0;
    public static final int PROCEDURE_SCHEM = 1;
    public static final int PROCEDURE_NAME  = 2;
    public static final int REMARKS         = 3;
    public static final int PROCEDURE_TYPE  = 4;
    private static final String[] metaNames = {
        "PROCEDURE_CAT", // NOI18N
        "PROCEDURE_SCHEM", // NOI18N
        "PROCEDURE_NAME", // NOI18N
        "REMARKS", // NOI18N
        "PROCEDURE_TYPE" // NOI18N
    };
    private Object[]            metaValues;
    private ProcedureColumnMetaData[] procedureColumnMetaData;
    private DatabaseMetaData    dbmd;

    ProcedureMetaData(ResultSet resultSet, DatabaseMetaData dbmd) throws SQLException {
        this.dbmd = dbmd;
        int exceptionCount = 0;
        SQLException firstException = null;
        metaValues = new Object[metaNames.length];
        for (int i = 0; i < metaNames.length; i++) {
            try {
                metaValues[i] = resultSet.getObject(metaNames[i]);
            } catch (SQLException e) {
                metaValues[i] = null;
                exceptionCount++;
                if (firstException == null) {
                    firstException = e;
                }
            }
        }
        if (exceptionCount == metaNames.length) {
            throw firstException;
        }
        // Delay getting column metadata until it is needed
        procedureColumnMetaData = null;
    }

    public Object getMetaInfo(String name) throws SQLException {
        for (int i = 0; i < metaNames.length; i++) {
            if (name.equals(metaNames[i])) {
                return metaValues[i];
            }
        }
        throw new SQLException(rb.getString("NAME_NOT_FOUND") + ": " + name); // NOI18N
    }

    public String getMetaInfoAsString(String name) throws SQLException {
        Object o = getMetaInfo(name);
        return (o == null)? null: o.toString();
    }

    public Object getMetaInfo(int index) throws SQLException {
        if (index < 0 || index > metaValues.length) {
            throw new SQLException(rb.getString("NO_SUCH_INDEX") + ": " + index); // NOI18N
        }
        return metaValues[index];
    }

    public String getMetaInfoAsString(int index) throws SQLException {
        Object o = getMetaInfo(index);
        return (o == null)? null: o.toString();
    }

    public ProcedureColumnMetaData[] getProcedureColumnMetaData() throws SQLException {
        if (procedureColumnMetaData == null) {
            ArrayList list = new ArrayList();
            ResultSet colRs = dbmd.getColumns(null, (String)metaValues[PROCEDURE_SCHEM], (String)metaValues[PROCEDURE_NAME], "%"); // NOI18N
            while (colRs.next()) {
                list.add(new ProcedureColumnMetaData(colRs));
            }
            colRs.close();
            procedureColumnMetaData = (ProcedureColumnMetaData[])list.toArray(new ProcedureColumnMetaData[0]);
        }
        return procedureColumnMetaData;
    }

    public ProcedureColumnMetaData getProcedureColumnMetaData(String columnName) throws SQLException {
        for (int i = 0; i < getProcedureColumnMetaData().length; i++) {
            if (getProcedureColumnMetaData()[i].getMetaInfo(ProcedureColumnMetaData.COLUMN_NAME).equals(columnName)) {
                return getProcedureColumnMetaData()[i];
            }
        }
        throw new SQLException(rb.getString("COLUMN_NOT_FOUND") + ": " + columnName); // NOI18N
    }

    public String[] getColumns() throws SQLException {
        ArrayList list = new ArrayList();
        for (int i = 0; i < getProcedureColumnMetaData().length; i++) {
            list.add(getProcedureColumnMetaData()[i].getMetaInfoAsString(ProcedureColumnMetaData.COLUMN_NAME));
        }
        return (String[])list.toArray(new String[0]);
    }
}
