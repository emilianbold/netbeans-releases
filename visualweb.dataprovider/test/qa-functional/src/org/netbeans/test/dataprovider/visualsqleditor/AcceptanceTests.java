/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
*/
package org.netbeans.test.dataprovider.visualsqleditor;

import java.awt.event.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.actions.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.designer.*;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.*;
import org.netbeans.test.dataprovider.common.*;
        
public class AcceptanceTests implements Constants {
    private static final String 
        DB_SCHEMA_NAME_TRAVEL = "TRAVEL",
        SQL_QUERY_UNSUPPORTED = "SELECT MAX(TRAVEL.PERSON.PERSONID)+1 FROM TRAVEL.PERSON",
        SQL_QUERY_PERSON_SELECT_2_FIELDS = "SELECT ALL PERSON.PERSONID, PERSON.FREQUENTFLYER FROM PERSON",
        SQL_QUERY_PERSON_SELECT_ALL_FIELDS = "SELECT ALL PERSON.PERSONID, PERSON.NAME, PERSON.JOBTITLE, PERSON.FREQUENTFLYER, PERSON.LASTUPDATED FROM PERSON",
        FIELD_PERSONID = "PERSONID",
        QUERY_CRITERIA_GREATER_THAN = "> Greater Than";

    private static int 
        INPUT_TABLE_COLUMN_INDEX_OUTPUT = 3,
        STRUCTURE_TABLE_COLUMN_INDEX_CHECKBOX = 0,
        STRUCTURE_TABLE_COLUMN_INDEX_FIELD = 2;
    
    private BaseTests testCaseInstance;
    
    public AcceptanceTests(BaseTests testCaseInstance) {
        this.testCaseInstance = testCaseInstance;
    }
    
    /**
     * Adds several DB tables to Query Editor.
     */
    public void checkQueryEditor_AddDBTables() {
        String dbURL = TestPropertiesHandler.getDatabaseProperty("DB_URL");
        Utils.callPopupMenuForDBTable(dbURL, DB_TABLE_PERSON, POPUP_MENU_ITEM_DESIGN_QUERY);
        
        Utils.logMsg("+++ Popup menu item [" + POPUP_MENU_ITEM_DESIGN_QUERY + 
            "] has been invoked for the DB table [" + DB_TABLE_PERSON + "]");
        Util.wait(2000);
        new QueueTool().waitEmpty();

        QueryBuilderOperator queryBuilder = new QueryBuilderOperator();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        Utils.logMsg("+++ Query Editor has been opened: " + queryBuilder.getSource());
        
        //TestUtils.printComponentList("D:\\zzz.zzz", visualSQLEditor.getSource().getParent().getParent());

        String[] dbTableNames = {DB_TABLE_TRIP, DB_TABLE_VALIDATION_TABLE};
        for (String dbTableName : dbTableNames) {
            queryBuilder.addTable(dbTableName);
            Util.wait(1000);
            new QueueTool().waitEmpty();
            Utils.logMsg("+++ DB table [" + dbTableName + 
                "] has been added on Query Editor Graph Panel");

            JTextComponentOperator textComponentOp = queryBuilder.getQueryTextComponent();
            Util.wait(1000);
            new QueueTool().waitEmpty();

            String sqlStatementText = textComponentOp.getText();
            Utils.logMsg("+++ SQL statement in Query Editor SQL Text Panel: [" + 
                sqlStatementText + "]");

            testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR,
                sqlStatementText, GOLDEN_FILE_LINE_SEPARATOR});
        }
        //Utils.doCloseWindow();
        //Util.wait(1000);
        //new QueueTool().waitEmpty();

        testCaseInstance.compareReferenceFiles();
        Utils.logMsg("+++ SQL statements equal to goldenfile data");
    }
    
    /**
     * Removes DB tables from Query Editor.
     */
    public void checkQueryEditor_RemoveDBTables() {
        // Query Editor is opened and 3 DB tables are placed on Graph Panel
        QueryBuilderOperator queryBuilder = new QueryBuilderOperator();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        Utils.logMsg("+++ Query Editor is opened: " + queryBuilder.getSource());
        
        String[] dbTableNames = {DB_TABLE_VALIDATION_TABLE, DB_TABLE_TRIP};
        for (String dbTableName : dbTableNames) {
            queryBuilder.removeTable(dbTableName);
            Util.wait(1000);
            new QueueTool().waitEmpty();
            Utils.logMsg("+++ DB table [" + dbTableName + 
                "] has been removed from Query Editor Graph Panel");

            JTextComponentOperator textComponentOp = queryBuilder.getQueryTextComponent();
            Util.wait(1000);
            new QueueTool().waitEmpty();

            String sqlStatementText = textComponentOp.getText();
            Utils.logMsg("+++ SQL statement in Query Editor SQL Text Panel: [" + 
                sqlStatementText + "]");

            testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR,
                sqlStatementText, GOLDEN_FILE_LINE_SEPARATOR});
        }
        String gridTableData = gridTableData2String(queryBuilder);
        testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR,
            gridTableData, GOLDEN_FILE_LINE_SEPARATOR});
        
        if (gridTableData.contains(DB_TABLE_VALIDATION_TABLE)) {
            throw new RuntimeException("Columns of DB table [" + 
                DB_TABLE_VALIDATION_TABLE + "] aren't removed completely from Query Editor Table Panel");
        }
        if (gridTableData.contains(DB_TABLE_TRIP)) {
            throw new RuntimeException("Columns of DB table [" + 
                DB_TABLE_TRIP + "] aren't removed completely from Query Editor Table Panel");
        }
        testCaseInstance.compareReferenceFiles();
        Utils.logMsg("+++ Test data equal to goldenfile data");
    }
    
    /**
     * Parses SQL queries in Query Editor.
     */
    public void checkQueryEditor_ParseSQLQuery() {
        QueryBuilderOperator queryBuilder = new QueryBuilderOperator();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        Utils.logMsg("+++ Query Editor is opened: " + queryBuilder.getSource());

        queryBuilder.retypeQuery(SQL_QUERY_PERSON_SELECT_2_FIELDS);
        Utils.logMsg("+++ SQL query [" + SQL_QUERY_PERSON_SELECT_2_FIELDS + "] has been entered");
        queryBuilder.parseQuery();
        Utils.logMsg("+++ SQL query [" + SQL_QUERY_PERSON_SELECT_2_FIELDS + "] has been parsed");

        queryBuilder.retypeQuery(SQL_QUERY_UNSUPPORTED);
        Utils.logMsg("+++ SQL query [" + SQL_QUERY_UNSUPPORTED + "] has been entered");
        String errMsg = null;
        /*
        queryBuilder.parseQuery();

        Exception e = waitErrorDialogUnsupportedSQLStatement(BUTTON_LABEL_CANCEL);
        if (e != null) {
            errMsg = (e instanceof TimeoutExpiredException ? 
                "Dialog [" + DIALOG_TITLE_SQL_PARSING_ERROR + "] hasn't appeared after " +
                    "parsing of unsupported SQL query [" + SQL_QUERY_UNSUPPORTED + "]" :
                e.getMessage());
        } else {
            Utils.logMsg("+++ Dialog [" + DIALOG_TITLE_SQL_PARSING_ERROR + "] has appeared " +
                "after parsing of unsupported SQL query [" + SQL_QUERY_UNSUPPORTED + "]");
        }
        */
        queryBuilder.retypeQuery(SQL_QUERY_PERSON_SELECT_2_FIELDS);
        Utils.logMsg("+++ SQL query [" + SQL_QUERY_PERSON_SELECT_2_FIELDS + "] has been entered");
        queryBuilder.parseQuery();
        Utils.logMsg("+++ SQL query [" + SQL_QUERY_PERSON_SELECT_2_FIELDS + "] has been parsed");

        queryBuilder.parseQuery(); // if first parsing gives error dialog, this parsing will fail to call popup menu
        Utils.logMsg("+++ SQL query [" + SQL_QUERY_PERSON_SELECT_2_FIELDS + "] has been parsed twice");
        
        if (errMsg != null) {
            throw new RuntimeException(errMsg);
        }
    }
    
    /**
     * Runs SQL query in Query Editor.
     */
    public void checkQueryEditor_RunSQLQuery() {
        QueryBuilderOperator queryBuilder = new QueryBuilderOperator();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        Utils.logMsg("+++ Query Editor is opened: " + queryBuilder.getSource());
        
        queryBuilder.addTable(DB_TABLE_PERSON);
                
        testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR});
        printTable(queryBuilder.runQuery());
        testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR});
        Utils.logMsg("+++ SQL statement [" + queryBuilder.getQueryTextComponent().getText() + 
            "] has been run in Query Editor");
        
        testCaseInstance.compareReferenceFiles();
        Utils.logMsg("+++ SQL statements equal to goldenfile data");
    }
    
    /**
     * Runs SQL query with ORDER clause
     */
    public void checkQueryEditor_RunOrderedSQLQuery() {
        QueryBuilderOperator queryBuilder = new QueryBuilderOperator();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        Utils.logMsg("+++ Query Editor is opened: " + queryBuilder.getSource());
        
        queryBuilder.addTable(DB_TABLE_PERSON);
        JTableOperator jTable = queryBuilder.getInputTable();
        
        // Specify the 1st sort type
        setInputTableComboboxValue(jTable, 0, QueryBuilderOperator.SORT_TYPE, 
            QueryBuilderOperator.DESCENDING);
        String sqlStatementText = queryBuilder.getQueryTextComponent().getText();
        testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR, sqlStatementText});
        printTable(queryBuilder.runQuery());
        testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR});
        Utils.logMsg("+++ SQL statement [" + sqlStatementText + "] has been run in Query Editor");

        // Specify the 2nd sort type
        setInputTableComboboxValue(jTable, 3, QueryBuilderOperator.SORT_TYPE, 
            QueryBuilderOperator.ASCENDING);
        sqlStatementText = queryBuilder.getQueryTextComponent().getText();
        testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR, sqlStatementText});
        printTable(queryBuilder.runQuery());
        testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR});
        Utils.logMsg("+++ SQL statement [" + sqlStatementText + "] has been run in Query Editor");
        
        // Specify sort order
        setInputTableComboboxValue(jTable, 3, QueryBuilderOperator.SORT_ORDER, 
            "1");
        sqlStatementText = queryBuilder.getQueryTextComponent().getText();
        testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR, sqlStatementText});
        printTable(queryBuilder.runQuery());
        testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR});
        Utils.logMsg("+++ SQL statement [" + sqlStatementText + "] has been run in Query Editor");
        
        testCaseInstance.compareReferenceFiles();
        Utils.logMsg("+++ SQL statements equal to goldenfile data");
    }
    
    /**
     * Runs SQL query with criteria
     */
    public void checkQueryEditor_RunSQLQueryWithCriteria() {
        QueryBuilderOperator queryBuilder = new QueryBuilderOperator();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        Utils.logMsg("+++ Query Editor is opened: " + queryBuilder.getSource());
        
        queryBuilder.addTable(DB_TABLE_PERSON);
        queryBuilder.addSimpleCriteria(FIELD_PERSONID, "3", "> Greater Than");
        String sqlStatementText = queryBuilder.getQueryTextComponent().getText();
        Utils.logMsg("+++ SQL statement [" + sqlStatementText + "] with criteria in Query Editor");
        
        queryBuilder.parseQuery();
        Utils.logMsg("+++ SQL statement [" + sqlStatementText + "] has been parsed in Query Editor");
        
        testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR, sqlStatementText});
        printTable(queryBuilder.runQuery());
        testCaseInstance.writeRefData(new Object[] {GOLDEN_FILE_LINE_SEPARATOR});
        Utils.logMsg("+++ SQL statement [" + sqlStatementText + "] has been run in Query Editor");

        Utils.doCloseWindow();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        testCaseInstance.compareReferenceFiles();
        Utils.logMsg("+++ SQL statements equal to goldenfile data");
    }
    
    /**
     * Puts DB table on VW project Designer and 
     * opens Query Editor for the created CachedRowSet 
     */
    public void checkQueryEditor_QueryEditorForCachedRowSet() {
        String 
            dbURL = TestPropertiesHandler.getDatabaseProperty("DB_URL"),
            dbTableName = DB_TABLE_PERSON,
            rowSetName = Utils.getBaseRowSetName(dbTableName),
            dataProviderName = Utils.getBaseDataProviderName(dbTableName);

        Utils.putDBTableOnComponent(dbURL, dbTableName, new Point(25, 25));
        //--------------------------------------------------------------------//
        class FakeJSFComponent extends JSFComponent {
            @Override
            protected void checkRowSetAppearance(String rowSetNodeName) {
                super.checkRowSetAppearance(rowSetNodeName);
            }
            @Override
            protected void checkDataProviderAppearance(String dataProviderNodeName) {
                super.checkDataProviderAppearance(dataProviderNodeName);
            }
        }
        FakeJSFComponent fakeJSFComponent = new FakeJSFComponent();
        //--------------------------------------------------------------------//
        Utils.doResetWindows();
        fakeJSFComponent.checkRowSetAppearance(NAVIGATOR_TREE_NODE_SESSION_PREFIX + rowSetName);
        fakeJSFComponent.checkDataProviderAppearance(NAVIGATOR_TREE_NODE_PAGE_PREFIX + dataProviderName);

        openQueryEditorForCachedRowSet(NAVIGATOR_TREE_NODE_SESSION_PREFIX.replace("|", ""), 
            rowSetName);
        Utils.doSaveAll();
        
        QueryBuilderOperator queryBuilder = new QueryBuilderOperator();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        Utils.logMsg("+++ Query Editor is opened: " + queryBuilder.getSource());
        
        boolean isDBTableOpened = queryBuilder.isDBTableOpenedOnGraphPanel(dbTableName);
        if (! isDBTableOpened) {
            throw new RuntimeException("DB table [" + dbTableName + "] isn't found on Graph Panel");
        }
        Utils.logMsg("+++ DB table [" + dbTableName + "] is found on Graph Panel");

        String sqlStatementText = queryBuilder.getQueryTextComponent().getText();
        Utils.logMsg("+++ SQL statement [" + sqlStatementText + "] with criteria in Query Editor");
    }

    private void openQueryEditorForCachedRowSet(String beanNodeName, String rowSetName) {
        JTreeOperator navigatorTreeOp = Utils.getNavigatorTreeOperator();
        TreePath treePath = Utils.selectChildOfTreeNode((JTree) navigatorTreeOp.getSource(), 
            beanNodeName, rowSetName);
        navigatorTreeOp.clickOnPath(treePath, 2); // double click on rowset
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Modifies SQL query and reopens Query Editor for CachedRowSet
     */
    public void checkQueryEditor_ModifyQueryForCachedRowSet() {
        QueryBuilderOperator queryBuilder = new QueryBuilderOperator();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        Utils.logMsg("+++ Query Editor is opened: " + queryBuilder.getSource());

        queryBuilder.retypeQuery(SQL_QUERY_PERSON_SELECT_ALL_FIELDS);
        Utils.logMsg("+++ SQL query [" + SQL_QUERY_PERSON_SELECT_ALL_FIELDS + "] has been entered");
        queryBuilder.parseQuery();
        Utils.logMsg("+++ SQL query [" + SQL_QUERY_PERSON_SELECT_ALL_FIELDS + "] has been parsed");

        JTableOperator structureTable = queryBuilder.getStructureTableOfDBTableOnGraphPanel(DB_TABLE_PERSON);
        if (structureTable == null) {
            throw new RuntimeException("DB table [" + DB_TABLE_PERSON + "] isn't found on Graph Panel " +
                "or its structure table isn't found");
        }
        Utils.logMsg("+++ Structure table of DB table [" + DB_TABLE_PERSON + "] is found");
        
        // unmark the 1st checkbox (exclude the 1st field from SQL query)
        Utils.logMsg("+++ Current value of the 1st checkbox in the structure table: [" + 
            structureTable.getValueAt(0, STRUCTURE_TABLE_COLUMN_INDEX_CHECKBOX) + "]");
        structureTable.clickOnCell(0, STRUCTURE_TABLE_COLUMN_INDEX_CHECKBOX); 
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        structureTable.setValueAt(Boolean.FALSE, 0, STRUCTURE_TABLE_COLUMN_INDEX_CHECKBOX);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        Utils.logMsg("+++ Current value of the 1st DB table checkbox in the structure table: [" + 
            structureTable.getValueAt(0, STRUCTURE_TABLE_COLUMN_INDEX_CHECKBOX) + "]");
        
        Utils.doCloseWindow(); // modified SQL query should be stored in the appropriate CachedRowSet
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        openQueryEditorForCachedRowSet(NAVIGATOR_TREE_NODE_SESSION_PREFIX.replace("|", ""), 
            Utils.getBaseRowSetName(DB_TABLE_PERSON));
        queryBuilder = new QueryBuilderOperator();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        Utils.logMsg("+++ Query Editor is opened: " + queryBuilder.getSource());

        structureTable = queryBuilder.getStructureTableOfDBTableOnGraphPanel(DB_TABLE_PERSON);
        if (structureTable == null) {
            throw new RuntimeException("DB table [" + DB_TABLE_PERSON + "] isn't found on Graph Panel " +
                "or its structure table isn't found");
        }
        Utils.logMsg("+++ Structure table of DB table [" + DB_TABLE_PERSON + "] is found");
        Boolean value = (Boolean) structureTable.getValueAt(0, STRUCTURE_TABLE_COLUMN_INDEX_CHECKBOX);
        Utils.logMsg("+++ Current value of the 1st cell in the structure table: [" + 
            value + "]");
        
        Utils.doSaveAll();
        
        if (! value.equals(Boolean.FALSE)) {
            throw new RuntimeException("SQL query wasn't stored in CachedRowSet properly: " +
            "the 1st checkbox of structure table should be [" + Boolean.FALSE + "], " +
            "but its current value is [" + value + "]");
        }
        checkStructureTableInpuTableSynchronization(structureTable, queryBuilder.getInputTable());
        checkStructureTableSQLQuerySynchronization(structureTable, 
            queryBuilder.getQueryTextComponent().getText(), DB_TABLE_PERSON);
    }
    
    private void checkStructureTableSQLQuerySynchronization(JTableOperator structTable, 
        String controlSQLQuery, String dbTableName) {
        String sqlQuery = "SELECT ALL ";
        int structRowCount = structTable.getRowCount();
        for (int i = 0; i < structRowCount; ++i) {
            Boolean structValue = (Boolean) structTable.getValueAt(i, STRUCTURE_TABLE_COLUMN_INDEX_CHECKBOX);
            if (structValue) {
                sqlQuery += dbTableName + "." + structTable.getValueAt(i, 
                    STRUCTURE_TABLE_COLUMN_INDEX_FIELD) + 
                    (i < (structRowCount - 1) ? ", " : "");
            }
        }
        sqlQuery += " FROM " + dbTableName;
        Utils.logMsg("+++ SQL query from structure table = [" + sqlQuery + "]");
        Utils.logMsg("+++ Control SQL query = [" + controlSQLQuery + "]");
        
        sqlQuery = testCaseInstance.changeExtraWhiteSpaces(sqlQuery);
        String proxyControlSQLQuery = testCaseInstance.changeExtraWhiteSpaces(controlSQLQuery).replace(
            "\"", "").replace(DB_SCHEMA_NAME_TRAVEL + ".", "");
        if (! sqlQuery.equalsIgnoreCase(proxyControlSQLQuery)) {
            throw new RuntimeException("Control SQL query [" + controlSQLQuery + 
                "] isn't equal to SQL query from structure table [" + sqlQuery + "]");
        }
        Utils.logMsg("+++ Control SQL query is equal to SQL query from structure table.");
    }
    
    private void checkStructureTableInpuTableSynchronization(JTableOperator structTable, 
        JTableOperator inputTable) {
        int structRowCount = structTable.getRowCount(), 
            inputRowCount = inputTable.getRowCount();
        if (structRowCount != inputRowCount) {
            throw new RuntimeException("Amount of rows for structure table in Graph Pane [" + structRowCount + "] " +
            "isn't equal to amount of rows for table in Grid Pane [" + inputRowCount + "]");
        }
        Utils.logMsg("+++ Amount of rows for structure table in Graph Pane [" + structRowCount + "] " +
            "is equal to amount of rows for table in Grid Pane [" + inputRowCount + "]");
        for (int i = 0; i < structRowCount; ++i) {
            Boolean structValue = (Boolean) structTable.getValueAt(i, STRUCTURE_TABLE_COLUMN_INDEX_CHECKBOX),
                    inputValue =  (Boolean) inputTable.getValueAt(i, INPUT_TABLE_COLUMN_INDEX_OUTPUT);
            if (! structValue.equals(inputValue)) {
                throw new RuntimeException("Checkbox value [" + structValue + 
                "] of the 1st column for structure table in Graph Pane " +
                "isn't equal to checkbox value [" + inputValue + "] of column [Output] " +
                "for table in Grid Pane: row = [" + (i + 1) + "]");
            }
        }
        Utils.logMsg("+++ Data of structure table in Graph Pane are synchronized with data of table in Grid Pane");
    }
    
    protected void setInputTableComboboxValue(JTableOperator jTable, int rowNumber, 
        String columnName, String comboboxValue) {
        jTable.clickOnCell(rowNumber, jTable.findColumn(columnName));
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        JComboBoxOperator combobox = new JComboBoxOperator(jTable);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        combobox.setVerification(false);
        combobox.selectItem(comboboxValue);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    protected Exception waitErrorDialogUnsupportedSQLStatement(String buttonLabel) {
        return waitDialog(DIALOG_TITLE_SQL_PARSING_ERROR, buttonLabel);
    }
    protected Exception waitDialog(String dialogTitle, String buttonLabel) {
        String timeoutName = "DialogWaiter.WaitDialogTimeout";
        long previousTimeoutValue = JemmyProperties.getCurrentTimeout(timeoutName),
             newTimeoutValue = 5000;
        JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, newTimeoutValue);
        try {
            JDialogOperator errDlg = new JDialogOperator(dialogTitle);
            Util.wait(1000);
            new QueueTool().waitEmpty();
            new JButtonOperator(errDlg, buttonLabel).pushNoBlock();
        } catch(Exception e) {
            return e;
        } finally {
            JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, previousTimeoutValue);
            Util.wait(1000);
            new QueueTool().waitEmpty();
        }
        return null;
    }

    protected String printTable(JTableOperator table) {
        String res = "";
        for (int col = 0; col < table.getColumnCount(); col++) {
            res += table.getColumnName(col) + " ";
        }
        testCaseInstance.writeRefData(new Object[] {res});
        
        for (int row = 0; row < table.getRowCount(); row++) {
            res="";
            for (int col = 0; col < table.getColumnCount(); col++) {
                res += table.getValueAt(row, col).toString() + " ";
            }
            testCaseInstance.writeRefData(new Object[] {res});
        }
        return res;
    }
    
    public static String gridTableData2String(QueryBuilderOperator queryBuilder) {
        String result = "";
        JTableOperator gridTableOp = queryBuilder.getInputTable();
        for (int row = 0; row < queryBuilder.getInputTable().getRowCount(); row++) {
            for (int col = 0; col < queryBuilder.getInputTable().getColumnCount(); col++) {
                result += gridTableOp.getValueAt(row, col).toString() + " ";
            }
            result += "\r\n";
        }
        return result;
    }
}
