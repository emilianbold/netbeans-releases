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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db.sql.execute.ui;






import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.sql.execute.ui.util.TestCaseContext;

/**
 *
 * @author luke
 */
public class DataTypeTest extends NbTestCase{
    public DataTypeTest(String s,TestCaseContext c) {
        super(s);
    }
    /**
     * This needs to be rewritten to use a SQL execution mechanism that doesn't rely on the
     * internal implementation of SQLExecuteHelper.  Commenting out for now.
     * 
    TestCaseContext context;
    Connection conn;
    JTable table;
    Locale defaultLocale;
    public DataTypeTest(String s,TestCaseContext c) {
        super(s);
        context=c;
        debug("TestCase: "+context);
    }

    
    public void testData(){
        debug("testData()");
        
        
        int count=table.getColumnCount();
        int row=table.getRowCount();
        if(row==0)
            fail(context+": table exist but there is no data in table probably there is an error in insert sql script in file: "+TestCaseDataFactory.DB_SQLCREATE);
        Map data=context.getData();
        assertEquals(context+": number of entries in file with data and columns in database are different for test case: "+context,data.size(),count);
        for(int i=0;i<count;i++){
           String column=table.getColumnName(i);
           String expected=(String)data.get(column);
           if(expected==null)
                  expected=(String)data.get(column.toLowerCase());
           if(expected==null)
               fail(context+": the file with data doesn't contains entry called: "+column);
           TableCellRenderer renderer=table.getCellRenderer(0,i) ;
           
           Component c=table.prepareRenderer(renderer,0,i);
           String actual=(String)((JLabel)c).getText();  
           assertEquals(context+": values are different for column: "+column,expected,actual);
        }
        
        
    }
    
    private ResultSetTableModel executeSQL(String sql,Connection conn) throws Exception{
        SQLExecutionResults sqlrs=SQLExecuteHelper.execute(sql,0,sql.length(),conn,new SQLExecutionLogger() {
            public void cancel() {
            }
            public void finish(long executionTime) {
            }
            public void log(SQLExecutionResult result) {
            }
        });
        SQLResultPanelModel panelModel=SQLResultPanelModel.create(sqlrs);
        ResultSetTableModel model=panelModel.getResultSetModel();
        
        return model;
    }
    
    protected void setUp() throws Exception {
        debug("setUp()");
        Properties prop=context.getProperties();
        File[] jars=context.getJars();
        conn = DbUtil.createConnection(prop,jars);
        String sql_create=context.getSqlCreate();
        String sql_select=context.getSqlSelect();
        debug("sql_select: "+sql_select);
        executeSQL(sql_create,conn);
        ResultSetTableModel model=executeSQL(sql_select,conn);
        if(model==null)
            throw new RuntimeException(context+": model ResultSetTableModel is null probably there is a error in sql statement");
        table=new SQLResultTable();
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        table.setModel(model);
        
        
        
        
    }

    protected void tearDown() throws Exception {
       debug("tearDown()");
       Locale.setDefault(defaultLocale);
       String sql_del=context.getSqlDel();
       executeSQL(sql_del,conn);
       conn.close();
    }
    
    
    public static TestSuite suite() throws Exception{
        TestSuite suite=new TestSuite();
        TestCaseDataFactory factory=TestCaseDataFactory.getTestCaseFactory();
        Object[] context=factory.getTestCaseContext();
        for(int i=0;i<context.length;i++){
            Class[] args={String.class,TestCaseContext.class};
            Object[] o={"testData",context[i]};
            Constructor con=DataTypeTest.class.getConstructor(args);
            DataTypeTest testcase=(DataTypeTest)con.newInstance(o);
            suite.addTest(testcase);
        }
        return suite;
    }
    
    
    private  void debug(String message){
       
            log(message);
            System.out.println("> " + message);
       
    }
     */
}
