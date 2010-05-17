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
package org.netbeans.test.dataprovider.cachedrowsetdataprovider;

import java.awt.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.*;
import org.netbeans.modules.visualweb.gravy.designer.*;
import org.netbeans.modules.visualweb.gravy.toolbox.*;
import org.netbeans.test.dataprovider.common.*;
        
public class TripTable extends JSFComponent {
    public TripTable() {
        componentName = COMPONENT_TABLE_NAME;
        componentID = "table1"; // default Table id
        dbTableName = DB_TABLE_TRIP;    
    }
    
    public String makeTripTable() {
        String errMsg = null;
        try {
            putVisualComponentOnDesigner(25, 220); 
            // now Table component is selected in Navigator
            
            componentPoint = getComponentPoint(); // component should be selected
            Utils.doSaveAll();

            putDBTableOnComponent();
            Utils.doSaveAll();
            modifyTableLayout();
            modifyTripRowSet();
            checkTableLayout();
            setSQLTimestampConverter();
        } catch (Exception e) {
            e.printStackTrace(Utils.logStream);
            errMsg = (e.getMessage() == null ? e.toString() : e.getMessage());
        }
        return errMsg;
    }
    
    private void modifyTripRowSet() { // modify SQL statement for tripRowSet
        String tripRowSetNode = NAVIGATOR_TREE_NODE_SESSION_PREFIX + Utils.getBaseRowSetName(dbTableName);
        Utils.callPopupMenuOnNavigatorTreeNode(tripRowSetNode, POPUP_MENU_ITEM_EDIT_SQL_STATEMENT);
        QueryBuilderOperator queryBuilderOperator = new QueryBuilderOperator();
        queryBuilderOperator.addParamCriteria(DB_TABLE_TRIP_PERSONID);

        Utils.doSaveAll();
        
        //queryBuilderOperator.close();
        //Util.wait(500);
        //new QueueTool().waitEmpty();
        Utils.doCloseWindow();

        selectModifiedTripRowSet(tripRowSetNode);
        String valuePropertyCommand = Utils.getSelectedComponentProperty(PROPERTY_NAME_COMMAND);
        Utils.logMsg("+++ [" + Utils.getBaseRowSetName(dbTableName) + 
            "]: value of property [" + PROPERTY_NAME_COMMAND + "] = [" + valuePropertyCommand + "]");
        
        String pattern = PATTERN_ANY_CHARS + "WHERE" + PATTERN_ANY_CHARS + DB_TABLE_TRIP_PERSONID + 
            PATTERN_ANY_CHARS + "=" + PATTERN_ANY_CHARS + "\\?";
        String sqlWhereClause = "WHERE " + DB_TABLE_TRIP_PERSONID + " = ?";
        boolean result = Pattern.matches(pattern, sqlWhereClause);
        if (!result) {
            throw new RuntimeException("SQL statement for [" + Utils.getBaseRowSetName(dbTableName) + 
                "] should end with [" + sqlWhereClause + "]");
        }
        Utils.logMsg("+++ Clause [" + sqlWhereClause + 
            "] was appended correctly to SQL statement of [" + tripRowSetNode + "]");
    }

    private void selectModifiedTripRowSet(String tripRowSetNode) {
        /** a tree subnode tripRowSet won't be selected properly in the window Navigator by
         *  using JTreeOperator due to having very long node label "tripRowSet:SELECT ALL ..."
            //Utils.use_JTreeOperator_Verification = false;
            TreePath tripRowSetTreePath = Utils.findNavigatorTreeNode(tripRowSetNode, true);
            //Utils.use_JTreeOperator_Verification = true;
        **/
        JTree jTree = ((JTree) Utils.getNavigatorTreeOperator().getSource());
        TreePath treePath = Utils.selectChildOfTreeNode(jTree, 
             NAVIGATOR_TREE_NODE_SESSION_PREFIX.replace("|",""), 
             Utils.getBaseRowSetName(dbTableName));
        if (treePath == null) {
            throw new RuntimeException("Tree node [" + tripRowSetNode + 
                "] wasn't selected correctly in the window Navigator");
        }
        Utils.logMsg("+++ Selected treePath = [" + jTree.getSelectionPath().toString() + 
            "] ("+ treePath.getClass().getName() + ")");
    }
    
    private void modifyTableLayout() {
        callPopupMenuItem(POPUP_MENU_ITEM_TABLE_LAYOUT);
        JDialogOperator jDialogOp = new JDialogOperator(DIALOG_TITLE_TABLE_LAYOUT);
        hideDBColumns(jDialogOp);

        new JButtonOperator(jDialogOp, BUTTON_LABEL_OK).pushNoBlock();
        Util.wait(500);        
        new QueueTool().waitEmpty();
        Utils.doSaveAll();
    }
    
    private void hideDBColumns(JDialogOperator jDialogOp) {
        JListOperator jListOp = new JListOperator(jDialogOp, 1);
        String[] hiddenColumns = getHiddenDBColumnNames();
        for (String hiddenColumnName : hiddenColumns) {
            selectVisibleDBColumn(jListOp, hiddenColumnName);
            Util.wait(500);        
            new QueueTool().waitEmpty();
            
            new JButtonOperator(jDialogOp, BUTTON_LABEL_TO_LEFT).pushNoBlock();
            Util.wait(500);        
            new QueueTool().waitEmpty();
        }
    }
    
    private void selectVisibleDBColumn(JListOperator jListOp, String columnName) {
        int listSize = jListOp.getModel().getSize();
        for (int i = 0; i < listSize; ++i) {
            String listItem = jListOp.getModel().getElementAt(i).toString();
            if (listItem.toUpperCase().endsWith(columnName.toUpperCase())) {
                jListOp.setSelectedIndex(i);
                Utils.logMsg("+++ DB column [" + listItem + "] was removed from list of Selected Table Columns");
                break;
            }
        }
    }
    
    private String[] getHiddenDBColumnNames() {
        return (new String[] {
            DB_TABLE_TRIP_TRIPID,
            DB_TABLE_TRIP_PERSONID,
            DB_TABLE_TRIP_LASTUPDATED
        });
    }

    private void checkTableLayout() {
        callPopupMenuItem(POPUP_MENU_ITEM_TABLE_LAYOUT);
        JDialogOperator jDialogOp = new JDialogOperator(DIALOG_TITLE_TABLE_LAYOUT);
        java.util.List<String> 
            tableColumnList = makeVisibleColumnList(jDialogOp),
            controlColumnList = Arrays.asList(getVisibleDBColumnNames());

        new JButtonOperator(jDialogOp, BUTTON_LABEL_CANCEL).pushNoBlock();
        Util.wait(500);        
        new QueueTool().waitEmpty();
        
        for (int i = 0; i < tableColumnList.size(); ++i) {
            String columnName = tableColumnList.get(i),
                   controlName = controlColumnList.get(i);
            if (! columnName.toUpperCase().endsWith(controlName.toUpperCase())) {
                throw new RuntimeException("List of table columns " + tableColumnList + 
                " doesn't correspond to control list: " + controlColumnList);
            }
        }
        Utils.logMsg("+++ List of table columns is correct: " + tableColumnList);
    }
        
    private java.util.List<String> makeVisibleColumnList(JDialogOperator jDialogOp) {
        JListOperator jListOp = new JListOperator(jDialogOp, 1);
        int listSize = jListOp.getModel().getSize();
        java.util.List<String> visibleColumnList = new ArrayList<String>();
        for (int i = 0; i < listSize; ++i) {
            String listItem = jListOp.getModel().getElementAt(i).toString();
            visibleColumnList.add(listItem);
        }
        return visibleColumnList;
    }
    
    private String[] getVisibleDBColumnNames() {
        return (new String[] {
            DB_TABLE_TRIP_DEPDATE,
            DB_TABLE_TRIP_DEPCITY,
            DB_TABLE_TRIP_DESTCITY,
            DB_TABLE_TRIP_TRIPTYPEID
        });
    }
    
    private void setSQLTimestampConverter() {
        Utils.putComponentOnDesigner(PALETTE_NAME_CONVERTERS, COMPONENT_SQL_TIMESTAMP_CONVERTER_NAME,
            SQL_TIMESTAMP_CONVERTER_ID, 5, 5, NAVIGATOR_TREE_NODE_PAGE_PREFIX);
        Utils.findNavigatorTreeNode(NAVIGATOR_TREE_NODE_PAGE_PREFIX + SQL_TIMESTAMP_CONVERTER_ID, true);
        Utils.setTextPropertyValue(PROPERTY_NAME_PATTERN, SQL_TIMESTAMP_CONVERTER_PATTERN);
        Utils.doSaveAll();
        
        JTree jTree = ((JTree) Utils.getNavigatorTreeOperator().getSource());
        TreePath parentTreePath = Utils.findNavigatorTreeNode(
            NAVIGATOR_TREE_NODE_TABLE1_ROWGROUP_PREFIX, true);
        TreePath treePath = Utils.selectChildOfTreeNode(jTree, parentTreePath, 0);
        treePath = Utils.selectChildOfTreeNode(jTree, treePath, 0);
        if (treePath == null) {
            throw new RuntimeException("1st child of tree node [" + parentTreePath.toString() + 
                "] wasn't selected correctly in the window Navigator");
        }
        Utils.logMsg("+++ Selected treePath = [" + jTree.getSelectionPath().toString() + 
            "] ("+ treePath.getClass().getName() + ")");
        
        Utils.setComboboxPropertyValue(PROPERTY_NAME_CONVERTER, SQL_TIMESTAMP_CONVERTER_ID);
        Utils.doSaveAll();
        Utils.logMsg("+++ SQL Timestamp converter was added.");
    }
}
