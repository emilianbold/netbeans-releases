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

package org.netbeans.modules.visualweb.gravy.dataconnectivity;

import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.db.sql.visualeditor.querybuilder.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.Bundle;

/**
 * This class implements test functionality for a window "QueryBuilder".
 */
public class QueryBuilderOperator extends ContainerOperator{
    ComponentOperator desktop = null;
    JTextComponentOperator query = null;
    public static final String SORT_TYPE = "Sort Type";
    public static final String SORT_ORDER = "Sort Order";
    public static final String DESCENDING = "Descending";
    public static final String ASCENDING = "Ascending";
    
    static final String ADD_QUERY_CRITERIA = "Add Query Criteria";
    public static final String CRITERIA = "Criteria";
    private static final String COLUMN = "Column";
    static final String POPUP_MENU_ITEM_PARSE_QUERY = "Parse Query";
    static final String POPUP_MENU_ITEM_RUN_QUERY = "Run Query";
    public static final String POPUP_MENU_ITEM_ADD_TABLE = "Add Table"; // "Add Table...";
    public static final String DIALOG_TITLE_ADD_TABLE = "Select Table"; // "Select Table(s) to Add";
    public final static String CANCEL = "Cancel";
    public final static String GROUP_BY="Group By";
    public static final String POPUP_MENU_ITEM_REMOVE_FROM_QUERY="Remove from Query";
    public final static String EQUALS = "Equals";
    public final static String NOT_EQUALS = "Not Equals";
    public final static String LESS_THAN = "Less Than";
    public final static String LESS_THAN_EQUALS = "Less Than Equals";
    public final static String GREATER_THAN = "Greater Than";
    public final static String GREATER_THAN_EQUALS = "Greater Than Equals";
    /**
     * @param tableName looking for given table to choose correct QueryBuilder instance
     * @return Container instanceof QueryBuilder
     */
    private static Container findQueryBuilder(String tableName) {
        java.awt.Container container = new org.netbeans.jemmy.operators.JTableOperator(
                Util.getMainWindow(), 
                new QueryBuilderInputTableChooser(tableName)).getParent();
        while (!(container instanceof QueryBuilder)) {
            //System.out.println("TRACE: findQueryBuilder: " + container.getClass().getName());
            container = container.getParent();
        }
        return container;
    }
    
    /**
     * Creates new instance of this class.
     * @param parent an object ContainerOperator related to container, which includes created Query Builder
     * @param tabId a name of required tab
     */
    public QueryBuilderOperator(ContainerOperator parent, String tabId) {
        //super(parent, new QueryBuilderChooser(tabId));
        super(findQueryBuilder(tabId));
        this.setComparator(new Operator.DefaultStringComparator(true, true));
    }
    
    /**
     * Creates new instance of this class.
     * @param tabId a name of required tab
     */
    public QueryBuilderOperator(String tabId) {
        this(Util.getMainWindow(), tabId);
    }
    
    /**
     * Creates new instance of this class.
     * @param parent an object ContainerOperator related to container, which includes created Query Builder
     */
    public QueryBuilderOperator(ContainerOperator parent) {
        super(parent, new QueryBuilderChooser());
        this.setComparator(new Operator.DefaultStringComparator(true, true));
    }
    
    /**
     * Creates new instance of this class.
     */
    public QueryBuilderOperator() {
        this(Util.getMainWindow());
    }
    
    /**
     * Closes a window of Query Builder.
     */
    public void close(){
        Util.closeWindow();
    }
    
    /**
     * TODO: Fix it
     * Returns an object QueryBuilderGraphFrameOperator
     * @return QueryBuilderGraphFrameOperator
     */
    public QueryBuilderPaneOperator getGraphFrame() {
        return(new QueryBuilderPaneOperator());
    }
    
    /**
     * Returns an object TableFrameOperator
     * Represent Operator for Table with given name in Graph Frame of the QueryBuilder
     * @param tableName
     * @return TableFrameOperator
     * 
     */
    public TableFrameOperator getTableFrame(String tableName) {
        return(new TableFrameOperator(tableName));
    }
    
    /**
     * Returns an object InputTableOperator
     * @return InputTableOperator
     */
    public InputTableOperator getInputTable() {
        return(new InputTableOperator());
    }
    /**
     * TODO: Fix it
     */
    private ComponentOperator getDesktop() {
        if(desktop == null) {
            //desktop = new ComponentOperator(new QueryBuilderOperator().getGraphFrame(),
            //        new Operator.Finder(JDesktopPane.class));
            desktop = new QueryBuilderOperator().getGraphFrame();
        }
        return(desktop);
    }
    
    /**
     * Initializes (if necessary) and returns an object JTextComponentOperator (query text).
     * @return JTextComponentOperator
     */
    public JTextComponentOperator getQueryTextComponent() {
        if(query == null) {
            //query = new JTextComponentOperator(this);
            query = new JEditorPaneOperator(this);
        }
        return(query);
    }
    
    /**
     * Returns an object JTableOperator (result table of a query)
     * @return JTableOperator
     */
    public JTableOperator getResultTable() {
        return(new JTableOperator(this, new Operator.Finder(QueryBuilderResultTable.class)));
    }

    /**
     * Clicks an item of a popup menu.
     * @param menuText a menu item
     */
    public void pushPopup(String menuText) {
        getDesktop().clickForPopup(10, 10);
        new JPopupMenuOperator().pushMenuNoBlock(menuText);
    }
        
    /**
     * Returns JComponentOperator, which wraps component, presenting
     * Graph Panel of Query Editor
     */
    public JComponentOperator getGraphComponent() {
        JComponent jComponent = (JComponent) findComponent((Container) this.getSource(), 
            new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    return (comp.getClass().getName().endsWith("SceneComponent") ? 
                        true : false);
                }
                public String getDescription() {
                    return ("Graph component [SceneComponent] is being found...");
                }
        });
        return (new JComponentOperator(jComponent));
    }

    /**
     * Returns JScrollPaneOperator, which wraps a scroll pane
     * of Graph Panel of Query Editor
     */
    public JScrollPaneOperator getGraphScrollPane() {
        Component component = getGraphComponent().getSource();
        do {
            component = component.getParent();
        } while (!(component instanceof JScrollPane));
        return (new JScrollPaneOperator((JScrollPane) component));
    }
    
    //========================================================================//
    /**
     * Class for component, presenting DB Table on Graph Panel of Query Editor
     */
    private static class DBTableGraphComponent {
        private QueryBuilderOperator queryBuilder;
        private Container dbTableGraphObject;
        private JTable structureTable;

        private DBTableGraphComponent(QueryBuilderOperator queryBuilder, 
            Container dbTableGraphObject) {
            this.queryBuilder = queryBuilder;
            this.dbTableGraphObject = dbTableGraphObject;
            structureTable = (JTable) ComponentOperator.findComponent(dbTableGraphObject, 
                new ComponentChooser() {
                    public boolean checkComponent(Component comp) {
                        return (comp instanceof JTable ? true : false);
                    }
                    public String getDescription() {
                        return "DB table component on Query Editor Graph Panel";
                    }
                });
        }
        
        private String getDBTableName() {
            QueryBuilderTableModel tableModel = (QueryBuilderTableModel) structureTable.getModel();
            return tableModel.getTableName();
        }

        public JTable getStructureTable() {
            return structureTable;
        }
        
        private void callPopupMenu(String popupMenuItem) {
            new ComponentOperator(dbTableGraphObject).makeComponentVisible();
            Util.wait(1000);
            new QueueTool().waitEmpty();
            
            JComponentOperator graphComponentOp = queryBuilder.getGraphComponent();
            Point pointGraphComponent = graphComponentOp.getLocationOnScreen(),
                  pointDBTableObj = dbTableGraphObject.getLocationOnScreen();
            int dx = pointDBTableObj.x - pointGraphComponent.x + 3, 
                dy = pointDBTableObj.y - pointGraphComponent.y - 3;
            
            graphComponentOp.clickForPopup(dx, dy);
            Util.wait(1000);
            new QueueTool().waitEmpty();

            new JPopupMenuOperator().pushMenuNoBlock(popupMenuItem);
            Util.wait(1000);
            new QueueTool().waitEmpty();
        }

        @Override
        public String toString() {
            return getDBTableName();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj instanceof String) return (getDBTableName().equals(obj.toString()));
            
            if (getClass() != obj.getClass()) return false;
            
            DBTableGraphComponent other = (DBTableGraphComponent) obj;
            return (getDBTableName().equals(other.getDBTableName()));
        }

        @Override
        public int hashCode() {
            return getDBTableName().hashCode();
        }
        
        private static java.util.List<DBTableGraphComponent> getDBTableGraphComponentList(
            QueryBuilderOperator queryBuilder) {
            java.util.List<DBTableGraphComponent> componentList = new ArrayList<DBTableGraphComponent>();
            Container container = (Container) queryBuilder.getGraphComponent().getSource();        
            Component[] components = container.getComponents();
            for (Component component : components) {
                if (component.getClass().getName().endsWith("QBNodeComponent")) {
                    componentList.add(new DBTableGraphComponent(queryBuilder, 
                        (Container) component));
                }
            }
            return componentList;
        }

        private static DBTableGraphComponent findDBTable(QueryBuilderOperator queryBuilder,
            String dbTableName) {
            return findDBTable(getDBTableGraphComponentList(queryBuilder), dbTableName);
        }
        private static DBTableGraphComponent findDBTable(
            java.util.List<DBTableGraphComponent> dbTableList, String dbTableName) {
            for (DBTableGraphComponent dbTable : dbTableList) {
                if (dbTable.equals(dbTableName)) return dbTable;
            }
            return null;
        }
    }
    //========================================================================//

    public JTableOperator getStructureTableOfDBTableOnGraphPanel(String dbTableName) {
        DBTableGraphComponent graphComponent = DBTableGraphComponent.findDBTable(this, dbTableName);
        if (graphComponent == null) return null;
        return (new JTableOperator(graphComponent.getStructureTable()));
    }
    
    public boolean isDBTableOpenedOnGraphPanel(String dbTableName) {
        return (DBTableGraphComponent.findDBTable(this, dbTableName) != null);
    }
    
    /**
     * Adds a database table into a query.
     * @param dbTableName name of a database table
     */
    public void addTable(String dbTableName) {
        DBTableGraphComponent dbTable = DBTableGraphComponent.findDBTable(this, dbTableName);
        if (dbTable != null) return; // DB table is already opened in Graph Pane of Query Editor
            
        JComponentOperator jCompOp = getGraphComponent();
        Util.wait(1000);
        new QueueTool().waitEmpty();

        //pushPopup(getBundleString("Add_Table"));
        jCompOp.clickForPopup(2, 2);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        new JPopupMenuOperator().pushMenuNoBlock(POPUP_MENU_ITEM_ADD_TABLE);
        Util.wait(1000);
        new QueueTool().waitEmpty();

        //JDialogOperator addTableDialog = new JDialogOperator(getBundleString("Add_Table_Title"));
        JDialogOperator addTableDialog = new JDialogOperator(DIALOG_TITLE_ADD_TABLE);
        Util.wait(1000);
        new QueueTool().waitEmpty();

        new JListOperator(addTableDialog).selectItem(dbTableName);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        new JButtonOperator(addTableDialog, "OK").pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Removes a database table from a query.
     * @param dbTableName name of a database table
     */
    public void removeTable(String dbTableName) {
        DBTableGraphComponent dbTable = DBTableGraphComponent.findDBTable(this, dbTableName);
        dbTable.callPopupMenu(POPUP_MENU_ITEM_REMOVE_FROM_QUERY);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Updates text of a query.
     * @param newQuery text of query
     */
    public void retypeQuery(String newQuery) {
        //        this.getQueryTextComponent().clearText();
        //        Util.wait(500);
        //        this.getQueryTextComponent().typeText(newQuery);
        this.getQueryTextComponent().setText(newQuery);
        Util.wait(1500);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Changes text of a query.
     * @param substring a part of query text, which should be changed
     * @param newSubstring new text, which will replace a part of query text
     */
    public void changeQuery(String substring, String newSubstring) {
        getQueryTextComponent().selectText(substring);
        getQueryTextComponent().replaceSelection(newSubstring);
    }
    
    /**
     * Performs a query.
     * @return object JTableOperator
     */
    public JTableOperator runQuery() {
        getQueryTextComponent().clickForPopup(2, 2);
        Util.wait(1000);
        new QueueTool().waitEmpty();

        new JPopupMenuOperator().pushMenuNoBlock(POPUP_MENU_ITEM_RUN_QUERY);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        return(getResultTable());
    }
    
    /**
     * Parses a query.
     */
    public void parseQuery() {
        getQueryTextComponent().clickForPopup(2, 2);
        Util.wait(1000);
        new QueueTool().waitEmpty();

        new JPopupMenuOperator().pushMenuNoBlock(POPUP_MENU_ITEM_PARSE_QUERY);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Invokes a dialog "Add query criteria".
     * @param column a name of a required column
     * @return an object AddQueryCriteriaOperator
     */
    public AddQueryCriteriaOperator invokeAddCriteria(String column) {
        return invokeAddCriteria(getInputTable().findCellRow(column));
    }
    
    /**
     * Invokes a dialog "Add query criteria".
     * @param column a number of a required column
     * @return an object AddQueryCriteriaOperator
     */
    public AddQueryCriteriaOperator invokeAddCriteria(int column) {
        JTableOperator inputTable = getInputTable();
        //Point p = inputTable.getPointToClick(column, inputTable.findColumn(getBundleString("CRITERIA")));
        //Point p = inputTable.getPointToClick(column, inputTable.findColumn(CRITERIA));
        Point p = inputTable.getPointToClick(column, inputTable.findColumn(COLUMN));
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        inputTable.clickForPopup(p.x, p.y);
        JPopupMenuOperator popup = new JPopupMenuOperator();
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        //popup.pushMenuNoBlock(getBundleString("ADD_QUERY_CRITERIA"));
        popup.pushMenuNoBlock(ADD_QUERY_CRITERIA);
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        return new AddQueryCriteriaOperator();
    }
    
    /**
     * Adds query criteria to a column of a database table.
     * @param column a name of a required column
     * @param value text of criteria value
     * @param operatorValue string with a comparison operator or a statement,
     * which is used in criteria (for example, "=")
     */
    public void addSimpleCriteria(String column, String value, String operatorValue) {
        // invoke add query criteria dialog
        AddQueryCriteriaOperator dialog = invokeAddCriteria(column);
        
        if (operatorValue != null)
            dialog.cboCompareType().selectItem(operatorValue);
        
        dialog.txtValue().setText(value);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        dialog.btOK().pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Adds a new parameter to a query criteria.
     * @param column a name of a required column
     */
    public void addParamCriteria(String column) {
        AddQueryCriteriaOperator dialog = invokeAddCriteria(column);
        
        dialog.rbtParameter().setSelected(true);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        dialog.btOK().pushNoBlock();
        Util.wait(1500);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Adds a new parameter to a query criteria.
     * @param column a number of a required column
     */
    public void addParamCriteria(int column) {
        AddQueryCriteriaOperator dialog = invokeAddCriteria(column);
        
        dialog.rbtParameter().setSelected(true);
        Util.wait(1000);
        new QueueTool().waitEmpty();

        dialog.btOK().pushNoBlock();
        Util.wait(1500);
        new QueueTool().waitEmpty();
    }
    
    /**
     * Selects "graphical join" of database tables (an arrow between 2 tables)
     * on graphical diagram.
     * @param table1 the 1st database table
     * @param table2 the 2nd database table
     * @deprecated Uses old graph library
     */
    public void selectJoin(String table1, String table2) {
        QueryBuilderGraphFrame gframe = (QueryBuilderGraphFrame) getGraphFrame().getSource();
        
        QueryBuilderInternalFrame fr1 = (QueryBuilderInternalFrame) getTableFrame(table1).getSource();
        QueryBuilderInternalFrame fr2 = (QueryBuilderInternalFrame) getTableFrame(table2).getSource();
/* TODO: jgraph remnoed
        DefaultGraphCell cell1 = fr1.getGraphCell();
        DefaultGraphCell cell2 = fr2.getGraphCell();
 
        for(int i=0;i<gframe.getGraph().getModel().getRootCount();i++) {
            Object o = gframe.getGraph().getModel().getRootAt(i);
 
            if(gframe.getGraph().getModel().isEdge(o)) {
 
                DefaultEdge e = (DefaultEdge) o;
 
                DefaultPort sourcePort = (DefaultPort)e.getSource();
                DefaultPort targetPort = (DefaultPort)e.getTarget();
 
                DefaultGraphCell sourceCell = (DefaultGraphCell) sourcePort.getParent();
                DefaultGraphCell targetCell = (DefaultGraphCell) targetPort.getParent();
 
                if(sourceCell == cell2 && targetCell == cell1 ||
                   sourceCell == cell1 && targetCell == cell2) {
                    Rectangle rect = gframe.getGraph().getCellBounds(o);
                    ComponentOperator desktop = ComponentOperator.createOperator(gframe.getGraph());
                    desktop.clickMouse((int) rect.getCenterX(), (int) rect.getCenterY(), 1);
                    Util.wait(2000);
                    break;
                }
            }
 
        }
 */
    }
    
    /**
     * Find Container consist Table with given tableName
     */
    private Container findTableFrame(String tableName) {
        java.awt.Container container = new JTableOperator(this,
                new QueryBuilderTableChooser(tableName)).getParent();
        while (!(container instanceof QBNodeComponent)) {
            container = container.getParent();
            System.out.println("TRACE: findTableFrame: "+ container.getClass().getName());
        }
        return container;
    }
    
    /**
     * This internal class handles a frame with a "graphical table"
     * on graphical diagram.
     */
    public class TableFrameOperator extends /*JComponentOperator*/ ContainerOperator {
        JTableOperator table = null;
        String tableName;
        
        /**
         * Creates new instance of this class.
         * @param tableName a name of database table
         */
        public TableFrameOperator(String tableName) {
            super(findTableFrame(tableName));
            //super(QueryBuilderOperator.this, new TableFrameChooser(tableName));
            this.tableName = tableName;
            //super(QueryBuilderOperator.this, tableName);
        }
        /*
        public TableFrameOperator(ContainerOperator cont) {
            super(cont, new TableFrameChooser());
        }
         */
        
        public String getTitle() {
            return tableName;
        }
        
        public ContainerOperator getTitleOperator() {
            Container cont = this.getTable().getParent();
            cont = cont.getParent();
            //cont = cont.getParent();
            return new ContainerOperator(cont);
        }
        
        /**
         * Returns an object JTableOperator, related to a database table,
         * included into graphical frame.
         * @return an object JTableOperator
         */
        public JTableOperator getTable() {
            if(table == null) {
                table = new JTableOperator(this, new QueryBuilderTableChooser());
                this.getQueueTool().waitEmpty(100);
            }
            return(table);
        }
        
        /**
         * Reversible selection changing of a single field of "graphical table".
         * @param fieldName a name of table field
         */
        public void changeFieldSelection(String fieldName) {
            System.out.println(fieldName);
            System.out.println(getTable().findCell(fieldName, 0));
            int row = getTable().findCell(fieldName, 0).y;
            if(row == -1) {
                throw(new JemmyException("Can not find \"" + fieldName +
                        "\" in \"\" table"));
            }
            getTable().clickOnCell(row, 0);
        }
        /**
         *   Chooser for Table in Graph View Frame
         *
         */
        public class QueryBuilderTableChooser implements ComponentChooser {
            
            public boolean checkComponent(Component comp) {
                boolean res = comp instanceof QueryBuilderTable;
                if (res) {
                    String firstValue = ((QueryBuilderTable)comp).getValueAt(0, 2).toString();
                    String secondValue = ((QueryBuilderTable)comp).getValueAt(1, 2).toString();
                    //System.out.println("TRACE: QueryBuilderTableChooser: firstValue = "+
                    //        ((QueryBuilderTable)comp).getValueAt(0, 2));
                    //System.out.println("TRACE: QueryBuilderTableChooser: secondValue = " +secondValue);
                    boolean firstFound = false;
                    boolean secondFound = false;
                    JTableOperator inputTable = QueryBuilderOperator.this.getInputTable();
                    int rowCout = inputTable.getRowCount();
                    for (int i=0;i<rowCout;i++) {
                        if ((inputTable.getValueAt(i, 0).toString().equals(firstValue)) &&
                                (inputTable.getValueAt(i, 2).toString().indexOf(TableFrameOperator.this.tableName)!=-1)) {
                            firstFound = true;
                            break;
                        }
                    }
                    for (int i=0;i<rowCout;i++) {
                        if ((inputTable.getValueAt(i, 0).toString().equals(secondValue)) &&
                                (inputTable.getValueAt(i, 2).toString().indexOf(TableFrameOperator.this.tableName)!=-1)) {
                            secondFound = true;
                            break;
                        }
                    }
                    return firstFound && secondFound;
                }
                return false;
            }
            
            public String getDescription() {
                return "Chooser for QueryBuilderTable";
            }
            
        }
    }
    
    public class QueryBuilderTableChooser implements ComponentChooser {
        String tableName;
        
        public QueryBuilderTableChooser(String tableName) {
            this.tableName = tableName;
        }
        
        public boolean checkComponent(Component comp) {
            boolean res = comp instanceof QueryBuilderTable;
            if (res) {
                String firstValue = ((QueryBuilderTable)comp).getValueAt(0, 2).toString();
                String secondValue = ((QueryBuilderTable)comp).getValueAt(1, 2).toString();
                //System.out.println("TRACE: QueryBuilderTableChooser: firstValue = "+
                //        ((QueryBuilderTable)comp).getValueAt(0, 2));
                //System.out.println("TRACE: QueryBuilderTableChooser: secondValue = " +secondValue);
                boolean firstFound = false;
                boolean secondFound = false;
                JTableOperator inputTable = QueryBuilderOperator.this.getInputTable();
                int rowCout = inputTable.getRowCount();
                for (int i=0;i<rowCout;i++) {
                    if ((inputTable.getValueAt(i, 0).toString().equals(firstValue)) &&
                            (inputTable.getValueAt(i, 2).toString().indexOf(tableName)!=-1)) {
                        firstFound = true;
                        break;
                    }
                }
                for (int i=0;i<rowCout;i++) {
                    if ((inputTable.getValueAt(i, 0).toString().equals(secondValue)) &&
                            (inputTable.getValueAt(i, 2).toString().indexOf(tableName)!=-1)) {
                        secondFound = true;
                        break;
                    }
                }
                return firstFound && secondFound;
            }
            return false;
        }
        
        public String getDescription() {
            return "Chooser for QueryBuilderTable";
        }
        
    }
    /**
     * TableFrameChooser
     * @deprecated TODO: Need to choose correct table name
     */
    public class TableFrameChooser implements ComponentChooser {
        String tableName;
        public TableFrameChooser(String tableName) {
            this.tableName = tableName;
        }
        public boolean checkComponent(Component comp) {
            if (comp instanceof QueryBuilderPane) {
                        return true;
            }
            return false;
        }
        
        public String getDescription() {
            return "TableFrameChooser";
        }
        
    }
    
    
    /**
     *
     *
     */
    public class QueryBuilderPaneOperator extends ContainerOperator {
        public QueryBuilderPaneOperator() {
            super(QueryBuilderOperator.this, new QueryBuilderPaneChooser());
        }
    }
    
    public class QueryBuilderPaneChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp instanceof QueryBuilderPane;
        }
        public String getDescription() {
            return "QueryBuilderPane Component";
        }
    }
    
    /**
     * @deprecated
     */
    public class QueryBuilderGraphFrameOperator extends ContainerOperator {
        public QueryBuilderGraphFrameOperator() {
            super(QueryBuilderOperator.this, new QueryBuilderGraphFrameChooser());
        }
    }
    /**
     *
     */
    public class QueryBuilderGraphFrameChooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return(comp instanceof QueryBuilderGraphFrame);
        }
        public String getDescription() {
            return(QueryBuilderGraphFrame.class.getName());
        }
    }
    
    public class InputTableOperator extends JTableOperator {
        public InputTableOperator() {
            super(QueryBuilderOperator.this, new Operator.Finder(QueryBuilderInputTable.class));
        }
        
    }
    
    /**
     * Looking for QueryBuilderInputTable by searching its instances and choosing one
     * which contains tableName Tabke
     */
    public static class QueryBuilderInputTableChooser implements ComponentChooser {
        String tableName;
        
        /**
         *
         * @param tableName Name of the Table in QueryBuilderInputTable
         */
        public QueryBuilderInputTableChooser(String tableName) {
            this.tableName = tableName;
        }
        
        
        public boolean checkComponent(Component comp) {
            if (comp instanceof QueryBuilderInputTable) {
                JTableOperator table = new JTableOperator((QueryBuilderInputTable)comp);
                //System.out.println("TRACE: QueryBuilderInputTableChooser table = "+ table.getValueAt(0, 2).toString());
                if (table.getValueAt(0, 2).toString().indexOf(tableName)!=-1)
                    return true;
            }
            return false;
        }
        
        public String getDescription() {
            return "QueryInputTable with name: " + tableName;
        }
        
    }
    
    /**
     * TODO: Doesn't work properly, not used anymore
     * @deprecated
     */
    public static class QueryBuilderChooser implements ComponentChooser {
        private String tabId = null;
        
        public QueryBuilderChooser() {
        }
        
        public QueryBuilderChooser(String _tabId) {
            tabId = _tabId;
        }
        
        public boolean checkComponent(Component comp) {
            boolean res = comp instanceof QueryBuilder;
            if(tabId != null && res) {
                String name = new JTableOperator(new ContainerOperator(ContainerOperator.findContainerUnder(comp)),
                        new QueryBuilderInputTableChooser(tabId)).getValueAt(0, 2).toString();
                if (name.equalsIgnoreCase(tabId)) {
                    return true;
                }
                return false;
            } else {
                return res;
            }
        }
        
        public String getDescription() {
            return(QueryBuilder.class.getName());
        }
    }
    
    /**
     * Finds in a bundle file and returns an actual text of control component.
     * @param p_text string-key corresponding to required control component.
     * @return actual text of control component
     * @deprecated No Bundle present
     */
    public static String getBundleString(String p_text) {
        System.out.println("Getting bundle for " + p_text);
        try {
            return Bundle.getStringTrimmed("com.sun.rave.dataconnectivity.querybuilder.Bundle", p_text);
        } catch (JemmyException e) {}
        return null;
    }
}
