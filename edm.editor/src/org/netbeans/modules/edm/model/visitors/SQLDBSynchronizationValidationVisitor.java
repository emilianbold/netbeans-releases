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
package org.netbeans.modules.edm.model.visitors;

import org.netbeans.modules.edm.model.EDMException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.edm.model.SQLDBColumn;
import org.netbeans.modules.edm.model.SQLDBModel;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.ValidationInfo;
import org.netbeans.modules.edm.model.impl.ValidationInfoImpl;
import java.sql.Connection;

import java.util.logging.Logger;
import org.netbeans.modules.edm.model.DBMetaDataFactory;
import org.netbeans.modules.edm.editor.utils.DBExplorerUtil;
import org.netbeans.modules.edm.model.DBConnectionDefinition;
import org.netbeans.modules.edm.model.impl.AbstractDBTable;
import org.openide.util.NbBundle;

/**
 * @author Ahimanikya Satapathy
 */
public class SQLDBSynchronizationValidationVisitor {

    private static transient final Logger mLogger = Logger.getLogger(SQLDBSynchronizationValidationVisitor.class.getName());

    private class Table extends AbstractDBTable {

        public Table(String tname, String tcatalog, String tschema) {
            super(tname, tschema, tcatalog);
        }
    }
    private List<ValidationInfo> validationInfoList = new ArrayList<ValidationInfo>();

    public List<ValidationInfo> getValidationInfoList() {
        return this.validationInfoList;
    }

    public void visit(SQLDBModel etlDBModel) throws EDMException, Exception {
        DBMetaDataFactory meta = new DBMetaDataFactory();
        Connection conn = null;
        DBConnectionDefinition connDef = null;
        connDef = etlDBModel.getETLDBConnectionDefinition();

        if (connDef != null) {
            try {
                conn = DBExplorerUtil.createConnection(connDef.getConnectionProperties());
                if (conn == null) {
                    return;
                }
                meta.connectDB(conn);
                List collabTables = etlDBModel.getTables();

                if ((collabTables != null) && (!collabTables.isEmpty())) {
                    Iterator itr = collabTables.iterator();
                    SQLDBTable collabTable = null;
                    while (itr.hasNext()) {
                        collabTable = (SQLDBTable) itr.next();
                        compareCollabTableWithDatabaseTable(collabTable, meta);
                    }
                }
            } finally {
                meta.disconnectDB();
                meta = null;
            }
        }
    }

    public void visit(SQLDefinition definition) {
        List collabDatabases = definition.getAllDatabases();
        if ((collabDatabases != null) && (collabDatabases.size() > 0)) {
            SQLDBModel etlDBModel = null;
            Iterator itr = collabDatabases.iterator();
            while (itr.hasNext()) {
                try {
                    etlDBModel = (SQLDBModel) itr.next();
                    visit(etlDBModel);
                } catch (Exception ex) {
                    ValidationInfo vInfo = new ValidationInfoImpl(etlDBModel, ex.getMessage(), ValidationInfo.VALIDATION_ERROR);
                    this.validationInfoList.add(vInfo);
                }
            }
        }
    }

    private void checkForUpdates(SQLDBColumn collabColumn, List newColumns, SQLDBTable table, boolean ignorePrecision) {
        Iterator itr = newColumns.iterator();
        SQLDBColumn newColumn = null;
        boolean columnMatched = true;

        while (itr.hasNext()) {
            newColumn = (SQLDBColumn) itr.next();
            columnMatched = (compareWith(collabColumn, newColumn, ignorePrecision) == 0);
            if (columnMatched) {
                break;
            }
        }

        if (!columnMatched) {
            // Column not present or is modified in Database
            String desc = NbBundle.getMessage(SQLDBSynchronizationValidationVisitor.class, "MSG_column_not_found",new Object[] {collabColumn.getName(), table.getName()});
            ValidationInfo vInfo = new ValidationInfoImpl(table, desc, ValidationInfo.VALIDATION_ERROR);
            this.validationInfoList.add(vInfo);
        }
    }

    private void checkForNewColumns(SQLDBColumn newColumn, List newColumns, SQLDBTable table, boolean ignorePrecision) {
        Iterator itr = newColumns.iterator();
        SQLDBColumn collabColumn = null;
        boolean columnMatched = true;

        while (itr.hasNext()) {
            collabColumn = (SQLDBColumn) itr.next();
            columnMatched = (compareWith(collabColumn, newColumn, ignorePrecision) == 0);
            if (columnMatched) {
                break;
            }
        }

        if (!columnMatched) {
            // new column found
            String desc = NbBundle.getMessage(SQLDBSynchronizationValidationVisitor.class, "MSG_Found_new_column",new Object[] {collabColumn.getName(), table.getParent().getModelName()});
            ValidationInfo vInfo = new ValidationInfoImpl(table, desc, ValidationInfo.VALIDATION_ERROR);
            this.validationInfoList.add(vInfo);
        }
    }

    private int compareWith(SQLDBColumn collabCol, SQLDBColumn newCol, boolean ignorePrecision) {
        // compare primary keys
        if (collabCol.isPrimaryKey() && !newCol.isPrimaryKey()) {
            return -1;
        } else if (!collabCol.isPrimaryKey() && newCol.isPrimaryKey()) {
            return 1;
        }

        // compare foreign keys
        if (collabCol.isForeignKey() && !newCol.isForeignKey()) {
            return -1;
        } else if (!collabCol.isForeignKey() && newCol.isForeignKey()) {
            return 1;
        }

        // compare type
        if (collabCol.getJdbcType() != newCol.getJdbcType()) {
            return -1;
        }

        if (!ignorePrecision) {
            // compare scale
            if (collabCol.getScale() != newCol.getScale()) {
                return -1;
            }

            // compare getPrecision
            if (collabCol.getPrecision() != newCol.getPrecision()) {
                return -1;
            }
        }

        // compare getOrdinalPosition
        if (collabCol.getOrdinalPosition() != newCol.getOrdinalPosition()) {
            return -1;
        }

        // compare isNullable
        if (collabCol.isNullable() != newCol.isNullable()) {
            return -1;
        }

        return 0;
    }

    private void compareCollabTableWithDatabaseTable(SQLDBTable collabTable, DBMetaDataFactory meta) throws Exception {
        if (meta.isTableOrViewExist(AbstractDBTable.getResolvedCatalogName(collabTable), AbstractDBTable.getResolvedSchemaName(collabTable), AbstractDBTable.getResolvedTableName(collabTable))) {
            // Get the table from database
            Table newTable = new Table(AbstractDBTable.getResolvedTableName(collabTable), AbstractDBTable.getResolvedCatalogName(collabTable), AbstractDBTable.getResolvedSchemaName(collabTable));
            meta.populateColumns(newTable);

            List collabColumns = collabTable.getColumnList();
            List newColumns = newTable.getColumnList();

            //Check for update and delete
            for (Iterator itr = collabColumns.iterator(); itr.hasNext();) {
                SQLDBColumn oldCol = (SQLDBColumn) itr.next();
                int sqlTypeCode = oldCol.getJdbcType();
                boolean ignorePrecision = false;
                if ((sqlTypeCode == java.sql.Types.DATE || sqlTypeCode == java.sql.Types.TIME || sqlTypeCode == java.sql.Types.TIMESTAMP || sqlTypeCode == java.sql.Types.NUMERIC) && meta.getDBType().equals(DBMetaDataFactory.AXION)) {
                    ignorePrecision = true;
                }
                checkForUpdates(oldCol, newColumns, collabTable, ignorePrecision);
            }

            // check for new columns
            for (Iterator itr = newColumns.iterator(); itr.hasNext();) {
                SQLDBColumn newCol = (SQLDBColumn) itr.next();
                int sqlTypeCode = newCol.getJdbcType();
                boolean ignorePrecision = false;
                if ((sqlTypeCode == java.sql.Types.DATE || sqlTypeCode == java.sql.Types.TIME || sqlTypeCode == java.sql.Types.TIMESTAMP || sqlTypeCode == java.sql.Types.NUMERIC) && meta.getDBType().equals(DBMetaDataFactory.AXION)) {
                    ignorePrecision = true;
                }
                checkForNewColumns(newCol, collabColumns, collabTable, ignorePrecision);
            }

        // TODO: XXXXX We also need to check PK, FK, Index modifications XXXXX
        } else {
            boolean createIfNotExists = false;
            
            String desc = NbBundle.getMessage(SQLDBSynchronizationValidationVisitor.class, "MSG_table_not_found",new Object[] {collabTable.getName()}) + " " + meta.getDBName();
            ValidationInfo vInfo = new ValidationInfoImpl(collabTable, desc, createIfNotExists ? ValidationInfo.VALIDATION_WARNING : ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(vInfo);
            return;
        }
    }
}
