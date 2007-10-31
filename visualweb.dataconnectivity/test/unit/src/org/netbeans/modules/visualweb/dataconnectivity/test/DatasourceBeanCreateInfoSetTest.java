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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.visualweb.dataconnectivity.test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.netbeans.modules.visualweb.dataconnectivity.model.DatasourceTransferManager;
import org.netbeans.modules.visualweb.dataconnectivity.test.utils.SetupProject;

/**
 *
 * @author JohnBaker
 */
public class DatasourceBeanCreateInfoSetTest extends NbTestCase {
    private Project project;
    private RequestedJdbcResource jdbcResource;
    
    public DatasourceBeanCreateInfoSetTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = openProject();
      
        testCreateJdbcResource();
        
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}


    public Project openProject() throws IOException {
        project = SetupProject.setup(getWorkDir());
        return project;
    }

    public void testCreateJdbcResource() {
        jdbcResource = new RequestedJdbcResource("jdbc/VIR",
                "org.apache.derby.jdbc.ClientDriver", "jdbc:derby://localhost:1527/vir", "vir",
                "vir");  

        assertNotNull("jdbcResource should not be null", jdbcResource);        
    }

//    public void testDatasourceBeanCreateInfoSet() {
//        DatabaseConnection dbConnection = DatabaseConnection.create(jdbcDriver, jdbcResource.getUrl(), jdbcResource.getUsername(), jdbcResource.getUsername().toUpperCase(), jdbcResource.getPassword(), true);
//        String schemaName = dbConnection.getSchema();
//        Connection conn = dbConnection.getJDBCConnection();
//        DatabaseMetaData metaData = (conn == null) ? null : conn.getMetaData();
//        String tableName =
//                ((schemaName == null) || (schemaName.equals(""))) ? "TRIP" : schemaName + "." + "TRIP";
//        JDBCDriver jdbcDriver = ;
//        assert (new DatasourceBeanCreateInfoSet(dbConnection, jdbcDriver, viewName, metaData) != null);
//    }
}
