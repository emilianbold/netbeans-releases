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
package org.netbeans.modules.sql.framework.model;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;

/**
 * @author Girish Patil
 */
public interface JDBCConnectionProvider {
	public static int JDBC_TYPE_1 = 1;

	public static int JDBC_TYPE_2 = 2;

	public static int JDBC_TYPE_3 = 3;

	public static int JDBC_TYPE_4 = 4;

	/**
	 * Returns list of valid Driver names for this Database implementing
	 * java.sql.Driver.
	 */
	public List getJDBCDriverClassNames() throws Exception;

	/**
	 * Returns valid Driver names for this Database implementing java.sql.Driver.
	 */
	public String getJDBCDriverClassName() throws Exception;

	/**
	 * Returns default driver type.
	 */
	public int getJDBCDriverType() throws Exception;

	/**
	 * Returns driver type for the driver name passed.
	 */
	public int getJDBCDriverTypes(String driverClassName) throws Exception;

	/**
	 * Returns Connection object using the properties supplied to while defining
	 * Database.
	 */
	public Connection getJDBCConnection() throws Exception;

	/**
	 * Returns JDBC Connection using the parameter set in the Database wizard.
	 * 
	 * @param cl
	 *            ClassLoader to use to get connection.
	 */
	public Connection getJDBCConnection(ClassLoader cl) throws Exception;

	/**
	 * Gets the connection using the connection properties passed.
	 * 
	 * @param props
	 * 
	 */
	public Connection getJDBCConnection(Properties props) throws Exception;

	/**
	 * Gets the JDBC connection using the properties passed. JDBC URL used is
	 * one entered while creating Database.
	 * 
	 * @param prop
	 * @param cl
	 */
	public Connection getJDBCConnection(Properties prop, ClassLoader cl)
			throws Exception;

	/**
	 * Gets the JDBC connection using the properties passed.
	 * 
	 * @param url
	 * @param uid
	 * @param pswd
	 */
	public Connection getJDBCConnection(String jdbcUrl, String uid, String pswd)
			throws Exception;

	/**
	 * Gets the JDBC connection using the properties passed.
	 * 
	 * @param url
	 * @param uid
	 * @param pswd
	 * @param cl
	 */
	public Connection getJDBCConnection(String jdbcUrl, String uid,
			String pswd, ClassLoader cl) throws Exception;
}
