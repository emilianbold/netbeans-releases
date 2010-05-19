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
package org.netbeans.modules.edm.model;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.edm.editor.utils.SQLDBConnectionDefinition;
import org.w3c.dom.Element;
import org.openide.util.NbBundle;


/**
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public abstract class SQLModelObjectFactory {

    private static SQLModelObjectFactory instance = null;
    private static final String LOG_CATEGORY = SQLModelObjectFactory.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(SQLModelObjectFactory.class.getName());

    public static SQLModelObjectFactory getInstance() {
        if (instance == null) {
            try {
                Class implClass = Class.forName("org.netbeans.modules.edm.model.impl.SQLModelObjectFactoryImpl");
                instance = (SQLModelObjectFactory) implClass.newInstance();
            } catch (Exception ex) {
                mLogger.log(Level.INFO,NbBundle.getMessage(SQLModelObjectFactory.class, "LOG.INFO_Can't_instantiate_factory_class"),ex);
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
     * @throws EDMException
     */
    public abstract SQLDBConnectionDefinition createDBConnectionDefinition(Element element) throws EDMException;

    /**
     * Create a DBModel Object
     * 
     * @return SQLDBModel
     */
    public abstract SQLDBModel createDBModel(int type);

    public abstract SQLDBModel createDBModel(DatabaseModel src, int modelType, EDMParentObject sqlParent);

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
    public abstract SQLDefinition createSQLDefinition(Element element) throws EDMException;

    public abstract SQLDefinition createSQLDefinition(Element element, EDMParentObject parent) throws EDMException;

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
     * @throws EDMException
     */
    public abstract SQLFilter createSQLFilter(Element element) throws EDMException;

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
     * @throws EDMException
     */
    public abstract SQLLiteral createSQLLiteral(String name, String value, int jdbcType) throws EDMException;

    /**
     * Create a SQLPredicate
     * 
     * @return SQLPredicate
     */
    public abstract SQLPredicate createSQLPredicate();


    public abstract ValidationInfo createValidationInfo(Object obj, String description, int vType);

    /**
     * Create a SQLVisibleLiteral
     * 
     * @param name String
     * @param value String
     * @param jdbcType int
     * @return SQLLiteral
     * @throws EDMException
     */
    public abstract VisibleSQLLiteral createVisibleSQLLiteral(String name, String value, int jdbcType) throws EDMException;

    /**
     * Create a VisibleSQLPredicate
     * 
     * @return VisibleSQLPredicate
     */
    public abstract VisibleSQLPredicate createVisibleSQLPredicate();
}
