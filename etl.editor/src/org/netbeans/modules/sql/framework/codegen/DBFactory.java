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
package org.netbeans.modules.sql.framework.codegen;

import java.util.HashMap;
import java.util.Map;
import com.sun.sql.framework.exception.BaseException;

/**
 * DBFactory is a factory to get instance of a specific DB implementation.
 *
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class DBFactory {

    private static DBFactory instance = null;
    private final Map<Integer, String> dbTypeToDbClassMap = new HashMap<Integer, String>();
    private Map<Integer, DB> dbMap = new HashMap<Integer, DB>();

    public static DBFactory getInstance() {
        if (instance == null) {
            instance = new DBFactory();
        }
        return instance;
    }

    private DBFactory() {
        dbTypeToDbClassMap.put(new Integer(DB.BASEDB), "org.netbeans.modules.sql.framework.codegen.base.BaseDB");
        dbTypeToDbClassMap.put(new Integer(DB.ORACLE8DB), "org.netbeans.modules.sql.framework.codegen.oracle8.Oracle8DB");
        dbTypeToDbClassMap.put(new Integer(DB.ORACLE9DB), "org.netbeans.modules.sql.framework.codegen.oracle9.Oracle9DB");
        dbTypeToDbClassMap.put(new Integer(DB.SQLSERVERDB), "org.netbeans.modules.sql.framework.codegen.sqlserver.SqlServerDB");
        dbTypeToDbClassMap.put(new Integer(DB.SYBASEDB), "org.netbeans.modules.sql.framework.codegen.sybase.SybaseDB");
        dbTypeToDbClassMap.put(new Integer(DB.DB2V5DB), "org.netbeans.modules.sql.framework.codegen.db2v5.DB2V5DB");
        dbTypeToDbClassMap.put(new Integer(DB.DB2V7DB), "org.netbeans.modules.sql.framework.codegen.db2v7.DB2V7DB");
        dbTypeToDbClassMap.put(new Integer(DB.DB2V8DB), "org.netbeans.modules.sql.framework.codegen.db2v8.DB2V8DB");
        dbTypeToDbClassMap.put(new Integer(DB.AXIONDB), "org.netbeans.modules.sql.framework.codegen.axion.AxionDB");
        dbTypeToDbClassMap.put(new Integer(DB.DERBYDB), "org.netbeans.modules.sql.framework.codegen.derby.DerbyDB");
        dbTypeToDbClassMap.put(new Integer(DB.JDBCDB), "org.netbeans.modules.sql.framework.codegen.jdbc.JdbcDB");
        dbTypeToDbClassMap.put(new Integer(DB.PostgreSQL), "org.netbeans.modules.sql.framework.codegen.postgreSQL.PostgreSQLDB");
        dbTypeToDbClassMap.put(new Integer(DB.MYSQLDB), "org.netbeans.modules.sql.framework.codegen.mysql.MySQLDB");
    }

    public DB getDatabase(int dbType) throws BaseException {
        Integer intDbType = new Integer(dbType);
        DB db = dbMap.get(intDbType);

        if (db == null) {
            String dbClass = dbTypeToDbClassMap.get(new Integer(dbType));
            if (dbClass == null) {
                throw new BaseException("Cannot find a DB for type: " + dbType);
            }

            try {
                Class cls = Class.forName(dbClass);
                db = (DB) cls.newInstance();
                dbMap.put(intDbType, db);
            } catch (ClassNotFoundException ex1) {
                throw new BaseException("Cannnot create an instance of DB of class " + dbClass, ex1);
            } catch (InstantiationException ex2) {
                throw new BaseException("Cannnot create an instance of DB of class " + dbClass, ex2);
            } catch (IllegalAccessException ex3) {
                throw new BaseException("Cannot create an instance of DB of class " + dbClass, ex3);
            }
        }

        return db;
    }
}
