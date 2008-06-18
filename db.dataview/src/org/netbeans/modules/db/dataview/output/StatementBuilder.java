/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.db.dataview.output;

import org.netbeans.modules.db.dataview.util.DataViewUtils;

/**
 *
 * @author Ahimanikya
 */
public class StatementBuilder {

    String[] generateSQL(Object[] insertedRow, DataViewOutputPanel dvParent) {
        StringBuilder insertSql = new StringBuilder();
        StringBuilder rawInsertSql = new StringBuilder();
        insertSql.append("INSERT INTO ");
        rawInsertSql.append(insertSql.toString());

        String colNames = " (";
        String values = "";
        String rawvalues = "";
        boolean comma = false;
        for (int i = 0; i < insertedRow.length; i++) {
            if (insertedRow[i] != null) {

                if (comma) {
                    values += ", ";
                    rawvalues += ", ";
                    colNames += ", ";
                } else {
                    comma = true;
                }
                values += "?";
                if (DataViewUtils.isNumeric(dvParent.getDBTableWrapper().getColumnType(i))) {
                    rawvalues += insertedRow[i];
                } else {
                    rawvalues += "'" + insertedRow[i] + "'";
                }

                colNames += dvParent.getDBTableWrapper().getColumn(i).getQualifiedName();
            }
        }
        colNames += ") ";

        String tableName = dvParent.getDBTableWrapper().getFullyQualifiedName(0);
        insertSql.append(tableName + colNames + " Values(" + values + ")");
        rawInsertSql.append(tableName + "\n\t" + colNames + "\nVALUES\n\t (" + rawvalues + ") ");

        return new String[]{insertSql.toString(), rawInsertSql.toString()};
    }
}
