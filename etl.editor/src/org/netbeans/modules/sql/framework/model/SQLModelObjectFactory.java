/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.model;

import java.util.List;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.w3c.dom.Element;
import net.java.hulp.i18n.Logger;
import com.sun.etl.exception.BaseException;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * SQL Framework Object factory
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public abstract class SQLModelObjectFactory {

    private static SQLModelObjectFactory instance = null;
    private static final String LOG_CATEGORY = SQLModelObjectFactory.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(SQLModelObjectFactory.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * Returns Singlton-instance of SQL Framework Object factory
     * 
     * @return SQLModelObjectFactory factory instance
     */
    public static SQLModelObjectFactory getInstance() {
        if (instance == null) {
            try {
                Class implClass = Class.forName("org.netbeans.modules.sql.framework.model.impl.SQLModelObjectFactoryImpl");
                instance = (SQLModelObjectFactory) implClass.newInstance();
            } catch (Exception ex) {
                mLogger.errorNoloc(mLoc.t("EDIT135: Can't instantiate factory class{0}", LOG_CATEGORY), ex);
            }
        }
        return instance;
    }

    /**
     * Create a ConditionColumn
     * 
     * @return ConditionColumn
     */
    public abstract ColumnRef createColumnRef();

    /**
     * Create a ConditionColumn for a SQLDBColumn
     * 
     * @param column
     * @return ConditionColumn
     */
    public abstract ColumnRef createColumnRef(SQLDBColumn column);

    /**
     * Create an extended DB Connection Definition
     * 
     * @return SQLDBConnectionDefinition
     */
    public abstract SQLDBConnectionDefinition createDBConnectionDefinition();

    public abstract SQLDBConnectionDefinition createDBConnectionDefinition(DBConnectionDefinition conf);

    public abstract SQLDBConnectionDefinition createDBConnectionDefinition(String name,
            String dbType,
            String driverClass,
            String url,
            String user,
            String password,
            String description);

    /**
     * Create an extended DB Connection Definition from a given XML Element that hold the
     * connection informations.
     * 
     * @param element XML Element
     * @return SQLDBConnectionDefinition
     * @throws BaseException
     */
    public abstract SQLDBConnectionDefinition createDBConnectionDefinition(Element element) throws BaseException;

    /**
     * Create a DBModel Object
     * 
     * @return SQLDBModel
     */
    public abstract SQLDBModel createDBModel(int type);

    /**
     * create a DBModel Object
     * 
     * @param src DatabaseModel
     * @param modelType int
     * @param sqlParent SQLFrameworkParentObject
     * @return SQLDBModel
     */
    public abstract SQLDBModel createDBModel(DatabaseModel src, int modelType, SQLFrameworkParentObject sqlParent);

    /**
     * Create a SQLGroupBy Object
     * 
     * @return SQLGroupBy
     */
    public abstract SQLGroupBy createGroupBy();

    /**
     * Create a SQLGroupBy Object
     * 
     * @param columns List of columns
     * @param parent Table that holds the GroupBy object
     * @return SQLGroupBy
     */
    public abstract SQLGroupBy createGroupBy(List columns, Object parent);

    /**
     * Create a SQLFilter that accepts a single input (e.g., IS NULL, IS NOT NULL).
     * 
     * @return
     */
    public abstract SQLFilter createLeftUnarySQLFilter();

    /**
     * Create a SQLFilter that accepts a single input (e.g., NOT).
     * 
     * @return
     */
    public abstract SQLFilter createRightUnarySQLFilter();

    public abstract RuntimeDatabaseModel createRuntimeDatabaseModel();

    public abstract RuntimeInput createRuntimeInput();

    public abstract RuntimeInput createRuntimeInput(RuntimeInput rinput);

    public abstract RuntimeOutput createRuntimeOutput();

    public abstract RuntimeOutput createRuntimeOutput(RuntimeOutput routput);

    /**
     * Create a Source Column for a given DB Column
     * 
     * @param src DBColumn
     * @return SourceColumn
     */
    public abstract SourceColumn createSourceColumn(DBColumn src);

    /**
     * Create a Source Column
     * 
     * @param colName String
     * @param sqlJdbcType int
     * @param colScale int
     * @param colPrecision int
     * @param isNullable boolean
     * @return SourceColumn
     */
    public abstract SourceColumn createSourceColumn(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isNullable);

    /**
     * Create a Source Column
     * 
     * @param colName String
     * @param sqlJdbcType int
     * @param colScale int
     * @param colPrecision int
     * @param isPrimaryKey boolean
     * @param isForeignKey boolean
     * @param isIndexed boolean
     * @param isNullable boolean
     * @return SourceColumn
     */
    public abstract SourceColumn createSourceColumn(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isPrimaryKey,
            boolean isForeignKey, boolean isIndexed, boolean isNullable);

    /**
     * Create a SourceTable
     * 
     * @param source DBTable
     * @return SourceTable
     */
    public abstract SourceTable createSourceTable(DBTable source);

    /**
     * Create a SourceTable
     * 
     * @param name String
     * @param schema String
     * @param catalog String
     * @return SourceTable
     */
    public abstract SourceTable createSourceTable(String name, String schema, String catalog);

    /**
     * Create a SQLCondition Object
     * 
     * @param conditionName String
     * @return SQLCondition
     */
    public abstract SQLCondition createSQLCondition(String conditionName);

    /**
     * Creates an empty SQL Definition Object
     * 
     * @return SQLDefinition
     */
    public abstract SQLDefinition createSQLDefinition();

    /**
     * Creates a SQL Definition Object for a given XML Element, that hold the definition
     * information
     * 
     * @return SQLDefinition
     */
    public abstract SQLDefinition createSQLDefinition(Element element) throws BaseException;

    /**
     * Creates a SQL Definition Object for a given XML Element, that hold the definition
     * information and links itself to the container/parent
     * 
     * @param element
     * @param parent SQLFrameworkParentObject
     * @return SQLDefinition
     * @throws BaseException
     */
    public abstract SQLDefinition createSQLDefinition(Element element, SQLFrameworkParentObject parent) throws BaseException;

    /**
     * Creates a named empty SQL Definition Object
     * 
     * @return SQLDefinition
     */
    public abstract SQLDefinition createSQLDefinition(String defName);

    /**
     * Create a SQLFilter
     * 
     * @return SQLFilter
     */
    public abstract SQLFilter createSQLFilter();

    /**
     * Create SQLFilter for a given XML Element
     * 
     * @param element
     * @return SQLFilter
     * @throws BaseException
     */
    public abstract SQLFilter createSQLFilter(Element element) throws BaseException;

    /**
     * @return
     */
    public abstract SQLJoinOperator createSQLJoinOperator();

    /**
     * Create a SQLJoinTable
     * 
     * @param source SourceTable
     * @return SQLJoinTable
     */
    public abstract SQLJoinTable createSQLJoinTable(SourceTable source);

    public abstract SQLJoinView createSQLJoinView();

    /**
     * Create a SQLLiteral
     * 
     * @param name String
     * @param value String
     * @param jdbcType int
     * @return SQLLiteral
     * @throws BaseException
     */
    public abstract SQLLiteral createSQLLiteral(String name, String value, int jdbcType) throws BaseException;

    /**
     * Create a SQLPredicate
     * 
     * @return SQLPredicate
     */
    public abstract SQLPredicate createSQLPredicate();

    /**
     * Create a Target Column for a given DB Column
     * 
     * @param src DBColumn
     * @return TargetColumn
     */
    public abstract TargetColumn createTargetColumn(DBColumn src);

    /**
     * Create a Target Column
     * 
     * @param colName String
     * @param sqlJdbcType int
     * @param colScale int
     * @param colPrecision int
     * @param isNullable boolean
     * @return TargetColumn
     */
    public abstract TargetColumn createTargetColumn(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isNullable);

    /**
     * Create a Target Column
     * 
     * @param colName String
     * @param sqlJdbcType int
     * @param colScale int
     * @param colPrecision int
     * @param isPrimaryKey boolean
     * @param isForeignKey boolean
     * @param isIndexed boolean
     * @param isNullable boolean
     * @return TargetColumn
     */
    public abstract TargetColumn createTargetColumn(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isPrimaryKey,
            boolean isForeignKey, boolean isIndexed, boolean isNullable);

    /**
     * Create a TargetTable
     * 
     * @param source DBTable
     * @return TargetTable
     */
    public abstract TargetTable createTargetTable(DBTable source);

    /**
     * Create a TargetTable
     * 
     * @param name String
     * @param schema String
     * @param catalog String
     * @return TargetTable
     */
    public abstract TargetTable createTargetTable(String name, String schema, String catalog);

    public abstract ValidationInfo createValidationInfo(Object obj, String description, int vType);

    /**
     * Create a SQLVisibleLiteral
     * 
     * @param name String
     * @param value String
     * @param jdbcType int
     * @return SQLLiteral
     * @throws BaseException
     */
    public abstract VisibleSQLLiteral createVisibleSQLLiteral(String name, String value, int jdbcType) throws BaseException;

    /**
     * Create a VisibleSQLPredicate
     * 
     * @return VisibleSQLPredicate
     */
    public abstract VisibleSQLPredicate createVisibleSQLPredicate();
}
