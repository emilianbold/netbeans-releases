/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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


package org.netbeans.modules.sql.framework.ui.output.dataview;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.exception.DBSQLException;
import com.sun.sql.framework.jdbc.DBConstants;
import java.sql.Connection;
import java.util.List;
import org.axiondb.AxionException;
import org.netbeans.modules.sql.framework.codegen.AbstractGeneratorFactory;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.openide.util.Exceptions;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class DBTableMetadata {
    private SQLDBTable dbTable;
    private DBConnectionDefinition connDef;
    private List<DBColumn> columns;
    private int dbType;
    private AbstractGeneratorFactory genFactory;
    private StatementContext stmtContext = new StatementContext();
    
    public DBTableMetadata (SQLDBTable table) {
        try {
            construct(table);
            DB db = DBFactory.getInstance().getDatabase(dbType);
            genFactory = db.getGeneratorFactory();
        } catch (BaseException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    public void shutdownIfAxion() {
        if (isDBType(DBConstants.AXION)) {
            try {
                String url = DBExplorerUtil.adjustDatabaseURL(connDef.getConnectionURL());
                DBExplorerUtil.getAxionDBFromURL(url).shutdown();
            } catch (AxionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public Connection createConnection() throws DBSQLException {
        return DBExplorerUtil.createConnection(connDef.getConnectionProperties());
    }
    
    public void refresh(SQLDBTable table) throws DBSQLException {
        if (table.getColumnList().size() != getColumnCount()){
            construct(table);
        }
    }
    
    public SQLDBTable getTable(){
        return dbTable;
    }
    
    public boolean isDBType(int type) {
        return type == dbType ? true : false;
    }
    
    public String getQualifiedTableName() {
        try {
            return genFactory.generate(dbTable, stmtContext);
        } catch (BaseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return dbTable.getName();
    }
    
    public DBColumn getColumn(int index){
        return columns.get(index);
    }
    
    public int getColumnType(int index){
        return columns.get(index).getJdbcType();
    }
    
    public String getColumnName(int index){
        return columns.get(index).getName();
    }
    
    public int getColumnCount() {
        return columns.size();
    }
    
    public int getDBType(){
        return dbType;
    }
    
    private void construct(SQLDBTable table) throws DBSQLException {
        try {
            dbTable = table;
            columns = dbTable.getColumnList();
            connDef = ((SQLDBModel) dbTable.getParentObject()).getETLDBConnectionDefinition();
            dbType = SQLUtils.getSupportedDBType(connDef.getDBType());
        } catch (Exception ex) {
            throw new DBSQLException(ex);
        }
    }

}
