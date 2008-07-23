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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.dataview.output;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jawed
 */
public class SQLStatementGeneratorTest extends NbTestCase {
    
//    TestCaseContext context;
//    DatabaseConnection dbconn;
//    Connection conn;
//    DBTable table;
    public SQLStatementGeneratorTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(SQLStatementGeneratorTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        context = DbUtil.getContext();
//        dbconn = DbUtil.getDBConnection();
//        conn = DbUtil.getjdbcConnection();
//        DbUtil.createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
//        DbUtil.dropTable();
    }

//        protected void createTable(){
//        try {
//            //Quoter qt = SQLIdentifiers.createQuoter(dbmd);
//            ResultSet rs = conn.createStatement().executeQuery(context.getSqlSelect());
//            ResultSetMetaData rsMeta = rs.getMetaData();
//            String aName = rsMeta.getTableName(1);
//            String aSchema = rsMeta.getSchemaName(1);
//            String aCatalog = rsMeta.getCatalogName(1);
//            table = new DBTable(aName, aSchema, aCatalog);
//            //table.setQuoter(quoter);
//        } catch (SQLException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
        
    /**
     * Test of generateInsertStatement method, of class SQLStatementGenerator.
     */
//    public void testGenerateInsertStatement() throws Exception {
//        Statement stmt = null;
//        String sqlSelect = context.getSqlSelect();
//        int pageSize = 5;
//        //ResultSet rs = conn.createStatement().executeQuery(sqlSelect);
//        DataView dataView = DataView.create(dbconn, sqlSelect, pageSize);
//        
//        String sql = dataView.getSQLString();
//        stmt = conn.createStatement();
//        ResultSet rs = stmt.executeQuery(sql);
//        DataViewDBTable tblMeta = dataView.getDataViewDBTable();
//        List<Object[]> rows = new ArrayList<Object[]>();
//        int colCnt = tblMeta.getColumnCount();
//        Object[] row = new Object[colCnt];
//        for (int i = 0; i < colCnt; i++) {
//            int type = tblMeta.getColumn(i).getJdbcType();
//            row[i] = DBReadWriteHelper.readResultSet(rs, type, i + 1);
//        }
//        rows.add(row);
//        
//        Object[] insertedRow = dataView.getDataViewPageContext().getCurrentRows().get(0);
//        SQLStatementGenerator instance = new SQLStatementGenerator(dataView);
//        String[] expResult = null;
//        String[] result = instance.generateInsertStatement(insertedRow);
//        assertEquals(expResult, result[0]);
//    }
//
//    /**
//     * Test of generateUpdateStatement method, of class SQLStatementGenerator.
//     */
//    public void testGenerateUpdateStatement() throws Exception {
//        System.out.println("generateUpdateStatement");
//        int row = 0;
//        int col = 0;
//        Object value = null;
//        List<Object> values = null;
//        List<Integer> types = null;
//        TableModel tblModel = null;
//        SQLStatementGenerator instance = null;
//        String[] expResult = null;
//        String[] result = instance.generateUpdateStatement(row, col, value, values, types, tblModel);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of generateDeleteStatement method, of class SQLStatementGenerator.
//     */
//    public void testGenerateDeleteStatement() {
//        System.out.println("generateDeleteStatement");
//        List<Integer> types = null;
//        List<Object> values = null;
//        int rowNum = 0;
//        TableModel tblModel = null;
//        SQLStatementGenerator instance = null;
//        String[] expResult = null;
//        String[] result = instance.generateDeleteStatement(types, values, rowNum, tblModel);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of generateCreateStatement method, of class SQLStatementGenerator.
//     */
//    public void testGenerateCreateStatement() throws Exception {
//        System.out.println("generateCreateStatement");
//        DBTable table = null;
//        SQLStatementGenerator instance = null;
//        String expResult = "";
//        String result = instance.generateCreateStatement(table);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCountSQLQuery method, of class SQLStatementGenerator.
//     */
//    public void testGetCountSQLQuery() {
//        System.out.println("getCountSQLQuery");
//        String queryString = "";
//        String expResult = "";
//        String result = SQLStatementGenerator.getCountSQLQuery(queryString);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    public void testToDo(){
        assertTrue("To Do", true);
    }
}
