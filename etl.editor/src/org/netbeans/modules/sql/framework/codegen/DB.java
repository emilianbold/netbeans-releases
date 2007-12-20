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
package org.netbeans.modules.sql.framework.codegen;

import com.sun.sql.framework.jdbc.DBConstants;


/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface DB {

    public static final int ORACLE8DB = DBConstants.ORACLE8;
    public static final int ORACLE9DB = DBConstants.ORACLE9;
    public static final int SQLSERVERDB = DBConstants.MSSQLSERVER;
    public static final int SYBASEDB = DBConstants.SYBASE;
    public static final int DB2V5DB = DBConstants.DB2V5;
    public static final int DB2V7DB = DBConstants.DB2V7;
    public static final int DB2V8DB = DBConstants.DB2V8;
    public static final int AXIONDB = DBConstants.AXION;
    public static final int DERBYDB = DBConstants.DERBY;
    public static final int PostgreSQL = DBConstants.POSTGRESQL;
    public static final int BASEDB = DBConstants.ANSI92;
    public static final int JDBCDB = DBConstants.JDBC;
    public static final int MYSQLDB = DBConstants.MYSQL;

    /**
     * get the Statements which are supported by this DB.
     *
     * @return Statements.
     */
    public Statements getStatements();

    /**
     * get the SQLObject Generator factory for this DB.
     *
     * @return AbstractGeneratorFactory
     */
    public AbstractGeneratorFactory getGeneratorFactory();

    /**
     * get the type Generator for this DB.
     *
     * @return TypeGenerator.
     */
    public TypeGenerator getTypeGenerator();

    /**
     * get the name after applying DB specfic escaping.
     *
     * @param name name which needs to be escaped.
     * @return name after escaping it.
     */
    public String getEscapedName(String name);

    /**
     * Get the name after removing any DB specific escaping.
     *
     * @param name name which needs to be unescaped.
     * @return name after unescaping it.
     */
    public String getUnescapedName(String name);

    /**
     * get the name after applying DB specfic escaping.
     *
     * @param name name which needs to be escaped.
     * @return name after escaping it.
     */
    public String getEscapedCatalogName(String name);

    /**
     * get the name after applying DB specfic escaping.
     *
     * @param name name which needs to be escaped.
     * @return name after escaping it.
     */
    public String getEscapedSchemaName(String name);

    /**
     * get the maximum table name length allowed for the name of the tables in this DB.
     *
     * @return max table length.
     */
    public int getMaxTableNameLength();

    /**
     * get the default date format string of this DB.
     *
     * @return default date format string.
     */
    public String getDefaultDateFormat();

    /**
     * return true if this DB supports ANSI style join.
     *
     * @return true if ANSI style join is supported.
     */
    public boolean isAnsiJoinSyntaxSupported();

    /**
     * get the data type casting rule for this DB. provides caller to check if a source
     * data type can be converted to a target data type by this DB.
     *
     * @param sourceType source data type.
     * @param targetType target data type.
     * @return
     */
    public int getCastingRule(int sourceType, int targetType);

    /**
     * get the operator factory for this data base
     *
     * @return operator factory
     */
    public SQLOperatorFactory getOperatorFactory();

    /**
     * Returns DBType
     *
     * @return operator factory
     */
    public int getDBType();
}