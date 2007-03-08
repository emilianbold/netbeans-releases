/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.dataconnectivity.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * helper class used by ColumnMetaData and ProcedureColumnMetaData
 *
 * @author John Kline
 */
class ColumnMetaDataHelper {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.sql.Bundle",
        Locale.getDefault());

    public static interface MetaIndex {
        String getName();
        int    getIndex();
    }

    private Object[] metaValues;

    ColumnMetaDataHelper(MetaIndex[] metaIndicies, ResultSet resultSet) throws SQLException {
        int exceptionCount = 0;
        SQLException firstException = null;
        metaValues = new Object[metaIndicies.length];
        for (int i = 0; i < metaIndicies.length; i++) {
            try {
                metaValues[i] = resultSet.getObject(metaIndicies[i].getName());
            } catch (SQLException e) {
                metaValues[i] = null;
                exceptionCount++;
                if (firstException == null) {
                    firstException = e;
                }
            }
        }
        if (exceptionCount == metaIndicies.length) {
            throw firstException;
        }
    }

    Object getMetaInfo(MetaIndex metaIndex) throws SQLException {
        int index = metaIndex.getIndex();
        /* the following shouldn't happen with out type safety */
        if (index < 0 || index > metaValues.length) {
            throw new SQLException(rb.getString("NO_SUCH_INDEX") + ": " + index); // NOI18N
        }
        return metaValues[index];
    }

    String getMetaInfoAsString(MetaIndex metaIndex) throws SQLException {
        Object o = getMetaInfo(metaIndex);
        return (o == null)? null: o.toString();
    }
}
