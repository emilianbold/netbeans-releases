/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.railsprojects.database;

import junit.framework.TestCase;

/**
 *
 * @author Erno Mononen
 */
public class RailsJdbcAsAdapterConnectionTest extends TestCase {

    private String derby = "jdbc:derby://localhost:1527/sample";
    private String mysql = "jdbc:mysql://99.11.22.33:3306/db";
    private String postgresql = "jdbc:postgresql://localhost:5432/depot";

    public RailsJdbcAsAdapterConnectionTest(String testName) {
        super(testName);
    }

    public void testResolveAdapterParams() {
        RailsJdbcAsAdapterConnection.AdapterParameters derbyParams = RailsJdbcAsAdapterConnection.resolveAdapterParams(derby);
        assertEquals("derby", derbyParams.getAdapterName());
        assertEquals("localhost", derbyParams.getHostName());
        assertEquals("1527", derbyParams.getPort());
        assertEquals("sample", derbyParams.getDatabase());

        RailsJdbcAsAdapterConnection.AdapterParameters mysqlParams = RailsJdbcAsAdapterConnection.resolveAdapterParams(mysql);
        assertEquals("mysql", mysqlParams.getAdapterName());
        assertEquals("99.11.22.33", mysqlParams.getHostName());
        assertEquals("3306", mysqlParams.getPort());
        assertEquals("db", mysqlParams.getDatabase());

        RailsJdbcAsAdapterConnection.AdapterParameters postgresqlParams = RailsJdbcAsAdapterConnection.resolveAdapterParams(postgresql);
        assertEquals("postgresql", postgresqlParams.getAdapterName());
        assertEquals("localhost", postgresqlParams.getHostName());
        assertEquals("5432", postgresqlParams.getPort());
        assertEquals("depot", postgresqlParams.getDatabase());

    }
}
