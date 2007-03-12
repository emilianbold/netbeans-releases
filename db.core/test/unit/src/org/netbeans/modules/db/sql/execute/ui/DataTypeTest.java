/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.execute.ui;

import java.awt.Component;
import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import junit.framework.TestSuite;





import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.sql.execute.SQLExecuteHelper;
import org.netbeans.modules.db.sql.execute.SQLExecutionLogger;
import org.netbeans.modules.db.sql.execute.SQLExecutionResult;
import org.netbeans.modules.db.sql.execute.SQLExecutionResults;
import org.netbeans.modules.db.sql.execute.ui.SQLResultPanel.SQLResultTable;
import org.netbeans.modules.db.sql.execute.ui.util.TestCaseContext;
import org.netbeans.modules.db.sql.execute.ui.util.DbUtil;
import org.netbeans.modules.db.sql.execute.ui.util.TestCaseDataFactory;

/**
 *
 * @author luke
 */
public class DataTypeTest extends NbTestCase{
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
        SQLExecutionResults sqlrs=SQLExecuteHelper.execute(sql,0,sql.length(),conn,null,new SQLExecutionLogger() {
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
}
