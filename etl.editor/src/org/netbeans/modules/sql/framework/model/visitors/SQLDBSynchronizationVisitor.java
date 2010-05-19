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
package org.netbeans.modules.sql.framework.model.visitors;

import com.sun.etl.exception.DBSQLException;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.model.impl.SourceColumnImpl;
import org.netbeans.modules.sql.framework.model.impl.TargetColumnImpl;
import org.netbeans.modules.sql.framework.model.impl.ValidationInfoImpl;
import org.netbeans.modules.sql.framework.ui.view.graph.MetaTableModel;
import java.util.ArrayList;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBMetaDataFactory;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.impl.AbstractDBTable;
import org.netbeans.modules.sql.framework.model.impl.PrimaryKeyImpl;



/**
 * @author Ahimanikya Satapathy
 * @author Nithya Radhakrishnan
 * @version $Revision$
 */
public class SQLDBSynchronizationVisitor {

    private static transient final Logger mLogger = Logger.getLogger(SQLDBSynchronizationVisitor.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private class Table extends AbstractDBTable {

        public Table(String tname, String tcatalog, String tschema) {
            super(tname, tschema, tcatalog);
        }
    }
    
    public List<ValidationInfo> infoList = new ArrayList<ValidationInfo>();

    private void mergeUpdates(SQLDBColumn collabColumn, List newColumns, SQLDBTable table, MetaTableModel tableModel, boolean ignorePrecision) {
        SQLDBColumn newColumn = null;
        boolean columnMatched = true;

        for (Iterator itr = newColumns.iterator(); itr.hasNext();) {
            newColumn = (SQLDBColumn) itr.next();
            String newColName = newColumn.getName();
            String collabColName = collabColumn.getName();

            // check whether column name match
            columnMatched = (collabColName.compareTo(newColName) == 0);

            // If column name match
            if (columnMatched) {
                // Check whether the column metadata is not matching
                if (compareWith(collabColumn, newColumn, ignorePrecision) != 0) {
                    // *** UPDATE ***
                    copyFrom(newColumn, collabColumn, ignorePrecision);
                    tableModel.updateColumn(collabColName, collabColumn);
                    String desc = collabColumn.getQualifiedName() + " was updated from Database " + table.getParent().getModelName();
                    ValidationInfo vInfo = new ValidationInfoImpl(collabColumn, desc, ValidationInfo.VALIDATION_WARNING);
                    infoList.add(vInfo);
                }
                break;
            }
        }

        // column does not exist in the new table
        if (!columnMatched) {
            // *** DELETE *** the column
            table.deleteColumn(collabColumn.getName());
            tableModel.removeColumn(collabColumn);
            String desc = collabColumn.getQualifiedName() + " was deleted since it no longer exists in Database " + table.getParent().getModelName();
            ValidationInfo vInfo = new ValidationInfoImpl(collabColumn, desc, ValidationInfo.VALIDATION_WARNING);
            infoList.add(vInfo);
        }
    }

    public void copyFrom(SQLDBColumn source, SQLDBColumn target, boolean ignorePrecision) {
        target.setJdbcType(source.getJdbcType());
        if(!ignorePrecision) {
        target.setScale(source.getScale());
        target.setPrecision(source.getPrecision());
        }
        target.setOrdinalPosition(source.getOrdinalPosition());
        target.setPrimaryKey(source.isPrimaryKey());
        target.setForeignKey(source.isForeignKey());
        target.setNullable(source.isNullable());
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

        if(!ignorePrecision) {
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

    private void mergeNewColumns(SQLDBColumn newColumn, List collabColumns, SQLDBTable table, MetaTableModel tableModel) {
        SQLDBColumn collabColumn = null;
        boolean columnMatched = true;

        for (Iterator itr = collabColumns.iterator(); itr.hasNext();) {
            collabColumn = (SQLDBColumn) itr.next();
            String newColName = newColumn.getName();
            String collabColName = collabColumn.getName();

            // check whether column name match
            columnMatched = (collabColName.compareTo(newColName) == 0);

            // If column name match
            if (columnMatched) {
                break;
            }
        }

        if (!columnMatched) {
            SQLDBColumn col = null;
            if (table instanceof SourceTable) {
                col = new SourceColumnImpl(newColumn);
            } else if (table instanceof TargetTable) {
                col = new TargetColumnImpl(newColumn);
            }
            // *** ADD *** the column
            table.addColumn(col);
            tableModel.addColumn(col);
            String desc1 = "Found new column in Database -> " + table.getParent().getModelName() + col.getQualifiedName() + " was added";
            ValidationInfo vInfo1 = new ValidationInfoImpl(collabColumn, desc1, ValidationInfo.VALIDATION_INFO);
            infoList.add(vInfo1);
        }
    }

    public void mergeCollabTableWithDatabaseTable(SQLDBTable collabTable, MetaTableModel tableModel) throws DBSQLException, Exception {
        DBMetaDataFactory meta = new DBMetaDataFactory();
        DBConnectionDefinition connDef = ((SQLDBModel) collabTable.getParentObject()).getETLDBConnectionDefinition();
        Connection conn = DBExplorerUtil.createConnection(connDef.getConnectionProperties());
        if (conn == null) {
            return;
        }
        
        try {
            meta.connectDB(conn);

            if (meta.isTableOrViewExist(AbstractDBTable.getResolvedCatalogName(collabTable), AbstractDBTable.getResolvedSchemaName(collabTable), AbstractDBTable.getResolvedTableName(collabTable))) {
                // Get the table from database
                Table newTable = new Table(AbstractDBTable.getResolvedTableName(collabTable),AbstractDBTable.getResolvedCatalogName(collabTable),AbstractDBTable.getResolvedSchemaName(collabTable));
                meta.populateColumns(newTable);

                List collabColumns = collabTable.getColumnList();
                List newColumns = newTable.getColumnList();

                for (Iterator itr = collabColumns.iterator(); itr.hasNext();) {
                    SQLDBColumn oldCol = (SQLDBColumn) itr.next();
                    int sqlTypeCode = oldCol.getJdbcType();
                    boolean ignorePrecision = false;
                    if ((sqlTypeCode == java.sql.Types.DATE || sqlTypeCode == java.sql.Types.TIME || sqlTypeCode == java.sql.Types.TIMESTAMP || sqlTypeCode == java.sql.Types.NUMERIC) && connDef.getDBType().equals(DBMetaDataFactory.AXION)) {
                        ignorePrecision = true;
                    }
                    mergeUpdates(oldCol, newColumns, collabTable, tableModel, ignorePrecision);
                }
                for (Iterator itr = newColumns.iterator(); itr.hasNext();) {
                    mergeNewColumns((SQLDBColumn) itr.next(), collabColumns, collabTable, tableModel);
                }
                
                if ((newTable.getPrimaryKey() != null)) {
                    List<String> pkCols = newTable.getPrimaryKey().getColumnNames();
                    for (String col : pkCols) {
                        mLogger.fine("DB PK Column(s):" + col.toString());
                    }
                    mLogger.fine("DB PK : " + newTable.getPrimaryKey().toString());
                } else {
                    mLogger.fine("Database table " + newTable.getName() + " has no PK");
                }
                
                if ((collabTable.getPrimaryKey() != null)) {
                    List<String> pkCols = collabTable.getPrimaryKey().getColumnNames();
                    for (String col : pkCols) {
                        mLogger.fine("Collab Table PK Column(s):" + col.toString());
                    }
                    mLogger.fine("Collab Table PK : " + collabTable.getPrimaryKey().toString());
                }
                else {
                    mLogger.fine("Collab Table " + collabTable.getName() + " has no PK.");
                }
                                
                ((AbstractDBTable) collabTable).setPrimaryKey((PrimaryKeyImpl) newTable.getPrimaryKey());

                if ((collabTable.getPrimaryKey() != null)) {
                    String nbBundle1 = collabTable.getQualifiedName() + " - Primary Key: ("
                            + collabTable.getPrimaryKey().toString() + ")";
                    
                    ValidationInfo vInfo = new ValidationInfoImpl(collabTable, nbBundle1, ValidationInfo.VALIDATION_INFO);
                    infoList.add(vInfo);
                    return;
                } else {
                    String nbBundle1 = collabTable.getQualifiedName() + " has no Primary Key.";
                    ValidationInfo vInfo = new ValidationInfoImpl(collabTable, nbBundle1, ValidationInfo.VALIDATION_INFO);
                    infoList.add(vInfo);
                    return;
                }                

            // TODO: XXXXX We also need to check PK, FK, Index modifications XXXXX
            } else {
                boolean createIfNotExists = false;
                if (collabTable instanceof TargetTable) {
                    createIfNotExists = ((TargetTable) collabTable).isCreateTargetTable();
                }
                String nbBundle1 = mLoc.t("BUND299: Table {0} is removed or renamed in Database", collabTable.getName());
                String desc = nbBundle1.substring(15) + " " + connDef.getConnectionURL();
                ValidationInfo vInfo = new ValidationInfoImpl(collabTable, desc, createIfNotExists ? ValidationInfo.VALIDATION_WARNING : ValidationInfo.VALIDATION_ERROR);
                infoList.add(vInfo);
                return;
            }
        } finally {
           meta.disconnectDB();
        }
    }
}