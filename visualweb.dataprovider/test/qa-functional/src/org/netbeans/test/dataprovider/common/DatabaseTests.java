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
package org.netbeans.test.dataprovider.common;

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
import org.netbeans.modules.visualweb.gravy.plugins.*;
import org.netbeans.modules.visualweb.gravy.designer.*;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.*;
import org.netbeans.modules.visualweb.gravy.model.deployment.*;
        
public class DatabaseTests implements Constants {
    public String checkDBConnection() {
        String errMsg = null, dbURL = TestPropertiesHandler.getDatabaseProperty("DB_URL");

        Utils.putFocusOnWindowServices();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        TreePath dbTreePath = Utils.findServicesTreeNode(SERVICES_TREE_NODE_DATABASES, true);
        Util.wait(1000);
        new QueueTool().waitEmpty();

        JTreeOperator treeOp = new ServerNavigatorOperator().getTree();
        treeOp.expandPath(dbTreePath);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        dbTreePath = Utils.findDBConnectionTreeNode();
        if (dbTreePath == null) {
            addNewDBDriver();
            addNewDBConnection();
            dbTreePath = Utils.findDBConnectionTreeNode();
            if (dbTreePath == null) return 
                "The tree subnode [" + dbURL + 
                "] isn't found under the tree node [" + SERVICES_TREE_NODE_DATABASES + "]";
        }
        treeOp = new ServerNavigatorOperator().getTree();
        //JPopupMenuOperator popupMenuOp = new JPopupMenuOperator(treeOp.callPopupOnPath(dbTreePath));
        JPopupMenuOperator popupMenuOp = Utils.callPopupOnPath(treeOp, dbTreePath);
        JMenuItem menuItem = TestUtils.findPopupMenuItemByLabel(popupMenuOp, 
            DB_POPUP_MENU_ITEM_LABEL_CONNECT, false, false);
        if (menuItem.isEnabled()) { // no DB connection
            new JMenuItemOperator(menuItem).pushNoBlock();
            Util.wait(500);
            new QueueTool().waitEmpty();
            connectToDB();
        } else { // required DB is already connected
            popupMenuOp.pressKey(KeyEvent.VK_ESCAPE);
        }
        Util.wait(500);
        new QueueTool().waitEmpty();
        if (errMsg == null) {       
            Utils.logMsg("+++ DB connection to [" + dbURL + "] is done");
        }
        return errMsg;
    }
    
    private void connectToDB() {
        JDialogOperator dialogOp = new JDialogOperator(DB_DIALOG_CONNECT_TITLE);
        
        // for DB Derby: now the dialog "Connect" doesn't contain any
        // text fields and user name and password are not required
        if (Utils.isUsedDBDerby()) {
            dialogOp.waitClosed();
            Util.wait(500);
            new QueueTool().waitEmpty();
            return;
        }
        
        new JTextFieldOperator(dialogOp, 0).setText(
            TestPropertiesHandler.getDatabaseProperty("DB_Password"));
        new JTextFieldOperator(dialogOp, 1).setText(
            TestPropertiesHandler.getDatabaseProperty("DB_User"));
        Util.wait(500);
        new JButtonOperator(dialogOp, BUTTON_LABEL_OK).pushNoBlock();
        Util.wait(500);
        new QueueTool().waitEmpty();
    }
    
    private void addNewDBDriver() {
        String jdbcDriverName = getJDBCDriverName();
        TreePath driverPath = Utils.findServicesTreeNode(SERVICES_TREE_NODE_DATABASES + 
            "|" + DB_TREE_NODE_DRIVERS + "|" + jdbcDriverName, false);
        if (driverPath == null) {
            if (Utils.isUsedDBDerby()) {
                defineDBDerbySettings();
                return;
            }
            
            Utils.callPopupMenuOnServicesTreeNode(SERVICES_TREE_NODE_DATABASES + 
                "|" + DB_TREE_NODE_DRIVERS, DB_POPUP_MENU_ITEM_LABEL_NEW_DRIVER);
            
            JDialogOperator dialogOp = new JDialogOperator(DB_DIALOG_NEW_DRIVER_TITLE);
           
            new JTextFieldOperator(dialogOp, 1).setText(jdbcDriverName);
            Util.wait(500);
 
            new JButtonOperator(dialogOp, BUTTON_LABEL_ADD).pushNoBlock();
            Util.wait(500);
            
            JFileChooserOperator fileChooserOp = new JFileChooserOperator();
            String jdbcDriverPath = TestPropertiesHandler.getDatabaseProperty("JDBC_Driver_AbsPath") + 
                "/" + TestPropertiesHandler.getDatabaseProperty("JDBC_Driver_File");
            new JTextFieldOperator(fileChooserOp, 0).setText(jdbcDriverPath);
            Util.wait(500);
            new JButtonOperator(fileChooserOp, BUTTON_LABEL_OPEN).pushNoBlock();
            Util.wait(1000);
            
            new JButtonOperator(dialogOp, BUTTON_LABEL_OK).pushNoBlock();
            Util.wait(500);
            new QueueTool().waitEmpty();
            
            Utils.logMsg("+++ New JDBC driver [" + jdbcDriverName + "] has been added");
        } else {
            Utils.logMsg("+++ JDBC driver [" + jdbcDriverName + "] exists alredy");
        }
    }
    
    private void defineDBDerbySettings() {
        Util.getMainMenu().pushMenuNoBlock(MAIN_MENU_ITEM_TOOLS_JAVA_DB_DATABASE_SETTINGS);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        JDialogOperator dialogOp = new JDialogOperator(DB_DIALOG_JAV_DB_SETTINGS_TITLE);

        String dbInstallation = TestPropertiesHandler.getDatabaseProperty("DB_Installation");
        new JTextFieldOperator(dialogOp, 1).setText(dbInstallation);
        Util.wait(500);

        String dbLocation = TestPropertiesHandler.getDatabaseProperty("DB_Location");
        new JTextFieldOperator(dialogOp, 0).setText(dbLocation);
        Util.wait(500);

        new JButtonOperator(dialogOp, BUTTON_LABEL_OK).pushNoBlock();
        Util.wait(500);
        
        Utils.logMsg("+++ Java DB Settings are defined properly.");
    }
    
    private String getJDBCDriverName() {
        String dbType = TestPropertiesHandler.getDatabaseProperty("Database"), 
               jdbcDriverName = null;
        if (dbType.toLowerCase().contains("oracle")) {
            jdbcDriverName = DB_TREE_NODE_ORACLE_JDBC_DRIVER_NAME;
        } else if (dbType.toLowerCase().contains("derby")) {
            jdbcDriverName = DB_TREE_NODE_DERBY_JDBC_DRIVER_NAME;
        } else {
            jdbcDriverName = "Unknown JDBC Driver";
        }
        return jdbcDriverName;
    }
    
    private void addNewDBConnection() {
        TreePath dbTreePath = Utils.findDBConnectionTreeNode();
        if (dbTreePath != null) return;

        String jdbcDriverName = getJDBCDriverName();
        Utils.callPopupMenuOnServicesTreeNode(SERVICES_TREE_NODE_DATABASES, 
            DB_POPUP_MENU_ITEM_LABEL_NEW_CONNECTION);
        JDialogOperator dialogOp = new JDialogOperator(DB_DIALOG_NEW_CONNECTION_TITLE);

        JComboBoxOperator driverComboBoxOp = new JComboBoxOperator(dialogOp);
        driverComboBoxOp.selectItem(jdbcDriverName);
        /* - workaround if the previous line has no effect      
        int itemIndex = -1, itemCount = driverComboBoxOp.getItemCount();        
        for (int i = 0; i < itemCount; ++i) {
            String itemText = driverComboBoxOp.getItemAt(i).toString();
            if (itemText.contains(jdbcDriverName)) {
                itemIndex = i;
                //Utils.logMsg("itemIndex = " + itemIndex);
                break;
            }
        }
        driverComboBoxOp.setSelectedIndex(itemIndex);
        */        
        
        Util.wait(500);
        
        String dbURL = TestPropertiesHandler.getDatabaseProperty("DB_URL"), 
               dbUser = TestPropertiesHandler.getDatabaseProperty("DB_User"), 
               dbPassword = TestPropertiesHandler.getDatabaseProperty("DB_Password");
        
        new JTextFieldOperator(dialogOp, 1).setText(dbURL);
        Util.wait(500);
        
        new JTextFieldOperator(dialogOp, 2).setText(dbUser);
        Util.wait(500);
        
        new JTextFieldOperator(dialogOp, 3).setText(dbPassword);
        Util.wait(500);

        new JButtonOperator(dialogOp, BUTTON_LABEL_OK).pushNoBlock();
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        // wait until DB is connected and dialog is closed
        waitDialogClosed(dialogOp);
    }
    
    private void waitDialogClosed(JDialogOperator dialogOp) {
        ((JDialog) dialogOp.getSource()).addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                synchronized (DatabaseTests.this) {
                    DatabaseTests.this.notifyAll();
                }
            }
        });
        synchronized (this) {
            try {
                wait(60000);
            } catch (InterruptedException e) {
                e.printStackTrace(Utils.logStream);
            }
        }
    }
}
