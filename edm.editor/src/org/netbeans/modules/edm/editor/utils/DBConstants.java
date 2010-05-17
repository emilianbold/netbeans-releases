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
package org.netbeans.modules.edm.editor.utils;

/**
 * Defines constant values that are used by all other SQL DB classes.
 * 
 * @author Ahimanikya Satapathy
 */
public interface DBConstants {

    public static final int ANSI92 = 10;
    public static final int JDBC = 15;
    public static final int AXION = 50;
    public static final int DERBY = 80;
    public static final int POSTGRESQL = 85;
    public static final int MYSQL = 90;
    public static final int DB2V5 = 42;
    public static final int DB2V7 = 40;
    public static final int DB2V8 = 45;
    public static final int MSSQLSERVER = 60;
    public static final int ORACLE8 = 20;
    public static final int ORACLE9 = 30;
    public static final int SYBASE = 70;
    public static final String ANSI92_STR = "ANSI92";
    public static final String JDBC_STR = "JDBCDB";
    public static final String AXION_STR = "INTERNAL";
    public static final String DERBY_STR = "DERBY";
    public static final String POSTGRES_STR = "POSTGRES";
    public static final String MYSQL_STR = "MYSQL";
    public static final String DB2V5_STR = "DB2V5";
    public static final String DB2V7_STR = "DB2V7";
    public static final String DB2V8_STR = "DB2V8";
    public static final String DB2_STR = "DB2";
    public static final String MSSQLSERVER_STR = "MSSQLSERVER";
    public static final String SQLSERVER_STR = "SQLSERVER";
    public static final String ORACLE8_STR = "ORACLE8";
    public static final String ORACLE9_STR = "ORACLE9";
    public static final String ORACLE_STR = "ORACLE";
    public static final String SYBASE_STR = "SYBASE";
    public static final String[] SUPPORTED_DB_TYPE_STRINGS = {ORACLE_STR, DB2_STR,
        SQLSERVER_STR, SYBASE_STR, DERBY_STR, POSTGRES_STR, MYSQL_STR
    };
    public static final String JDBC_URL_PREFIX_ORACLE = "jdbc:oracle:";
    public static final String JDBC_URL_PREFIX_POSTGRES = "jdbc:postgres:";
    public static final String JDBC_URL_PREFIX_AXION = "jdbc:axiondb:";
    public static final String JDBC_URL_PREFIX_DERBY = "jdbc:derby:";
    public static final String JDBC_URL_PREFIX_DB2 = ":db2:";
    public static final String JDBC_URL_PREFIX_SQLSERVER = ":sqlserver:";
    public static final String JDBC_URL_PREFIX_SYBASE = ":sybase:";
    public static final String JDBC_URL_PREFIX_MYSQL = "jdbc:mysql:";
    public static final String[] SUPPORTED_DB_URL_PREFIXES = {JDBC_URL_PREFIX_ORACLE,
        JDBC_URL_PREFIX_DB2,
        JDBC_URL_PREFIX_AXION,
        JDBC_URL_PREFIX_SQLSERVER,
        JDBC_URL_PREFIX_SYBASE,
        JDBC_URL_PREFIX_POSTGRES,
        JDBC_URL_PREFIX_DERBY,
        JDBC_URL_PREFIX_MYSQL
    };
}
