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

import java.awt.Point;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.Bundle;
import javax.swing.tree.TreePath;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.MessageFormat;
import org.netbeans.modules.visualweb.gravy.*;
import junit.framework.*;

/**
 * This class implements test functionality for the window "Server Navigator".
 */
public class ServerNavigatorOperator extends ServerExplorerOperator {
    private static final String pointbaseName = "Bundled Database";
    private static final String[] pbParams = new String[] {"", pointbaseName};
    
    JTreeOperator tree = null;
    
    /**
     * Creates a new instance of this class
     * @param parent an object ContainerOperator related to container, which
     * includes window "Server Navigator".
     */
    public ServerNavigatorOperator(ContainerOperator parent) {
        super(parent);
        getTree();
    }
    
    /**
     * Creates a new instance of this class
     */
    public ServerNavigatorOperator() {
        this(Util.getMainWindow());
    }
    
    /**
     * Makes the window "Server Navigator" visible.
     * @return an object ServerNavigatorOperator
     */
    public static ServerNavigatorOperator showNavigatorOperator() {
        // Util.getMainMenu().pushMenuNoBlock("Window|"+Bundle.getStringTrimmed("com.sun.rave.servernav.Bundle", "LBL_ServerNavigator"));
        Util.getMainMenu().pushMenuNoBlock("Window|Services");
        Util.wait(1000);
        new QueueTool().waitEmpty();
        return (new ServerNavigatorOperator());
    }
    
    /**
     * Adds database table on Design View via popup menu, related to its tree node.
     * @param p_path a path of required tree node as an object TreePath
     */
    public void addTable(TreePath p_path) {
        JPopupMenuOperator popup = new JPopupMenuOperator(getTree().callPopupOnPath(p_path));
        popup.pushMenu(getBundleString("Add_to_Form"));
    }
    
    /**
     * Adds a data source into window "Server Navigator".
     * @param p_dsname a name of data source
     * @param p_ds an object DataSource with properties of data source
     * @return true if data source was added successfully
     */
    public boolean addDataSource(String p_dsname, DataSource p_ds) {
        
        pushPopup(getBundleString("Databases"),getBundleString("New connection..."));
        
        AddDataSourceOperator a_dlg = new AddDataSourceOperator();
        
        JComboBoxOperator a_cbo = a_dlg.cboServerType();
        System.out.println("DBType=" + p_ds.getDbType());
        
        // Following line don't work :( Imitating...
        System.out.println("DBType now=" + a_cbo.getSelectedItem());
        if(p_ds.getDbType()!=null && p_ds.getDbType().length()>0){
            a_cbo.selectItem(p_ds.getDbType());
        }
/*        int ind = -1;
        for (int i = 0; i < a_cbo.getItemCount(); i++) {
            if ((((JdbcDriverInfo) a_cbo.getItemAt(i)).getDisplayName()).equals(p_ds.getDbType())) {
                ind = i;
            }
        }
        a_cbo.selectItem(ind);
 */
        Util.wait(1000);
/*
        a_dlg.txtDSName().clearText();
        a_dlg.txtDSName().typeText(p_dsname);
        Util.wait(500);
 
        if (a_dlg.txtHostName().isEditable()) {
            a_dlg.txtHostName().clearText();
            a_dlg.txtHostName().typeText(p_ds.getDbHost());
        }
        Util.wait(500);
 
        if (a_dlg.txtDBName().isEditable()) {
            a_dlg.txtDBName().clearText();
            a_dlg.txtDBName().typeText(p_ds.getDbName());
        }
        Util.wait(500);
 */
        System.out.println("DB URL=" + p_ds.getDbUrl());
        if(p_ds.getDbUrl()!=null && p_ds.getDbUrl().length()>0){
            a_dlg.cboURL().clearText();
            a_dlg.cboURL().typeText(p_ds.getDbUrl());
        }
        Util.wait(500);
        
        a_dlg.txtUser().clearText();
        a_dlg.txtUser().typeText(p_ds.getUser());
        Util.wait(500);
        
        a_dlg.txtPassword().clearText();
        a_dlg.txtPassword().typeText(p_ds.getPassword());
        Util.wait(500);
/*
        if (p_ds.getValidationTable()!=null){
            a_dlg.txtValidationTable().clearText();
            a_dlg.txtValidationTable().typeText(p_ds.getValidationTable());
        }else try {
            //select first table of schema as validation table
            a_dlg.btSelectTable().push();
            JDialogOperator selectDlg=new JDialogOperator();
            new JListOperator(selectDlg).selectItem(0);
            new JButtonOperator(selectDlg,0).push();
        }catch (org.netbeans.jemmy.TimeoutExpiredException e){
            // exception appears if connection isn't successful at all
        }
        Util.wait(1000);
 
        //a_dlg.txtURL().clearText();
        //a_dlg.txtURL().typeText(p_ds.getDbUrl());
 
        new QueueTool().waitEmpty();
 
        // Test connection
 
        a_dlg.btTestConnection().push();
        JDialogOperator a_dlg1 = new JDialogOperator(a_dlg);
        Util.wait(1000);
 
        boolean success = !(new JTextFieldOperator(a_dlg1).getText().equals("Failed") ||
                            new JTextFieldOperator(a_dlg1,1).getText().equals("Failed")
                          );
 */
        new JButtonOperator(a_dlg, getBundleString("OK")).push();
        
        // Add data source, if connection is OK
        
        Util.wait(10000);
        if (p_ds.getDbType().equalsIgnoreCase("mysql")) {
            new JButtonOperator(a_dlg, getBundleString("OK")).push();
        }
        return true;
    }
    
    /**
     * Opens connection with a data source.
     * @param p_dsname a name of data source
     * @param p_ds an object DataSource with properties of data source
     */
    public void connectToDataSource(String p_dsname, DataSource p_ds){
        
        
        String[] a_ss = {getBundleString("Databases"), p_dsname};
        pushPopup(a_ss,getBundleString("Connect..."));
        Util.wait(1000);
        
        JDialogOperator a_dlg = new JDialogOperator();
        Util.wait(1000);
        
        JTextFieldOperator tfUser = new JTextFieldOperator(a_dlg, 0);
        tfUser.clearText();
        tfUser.typeText(p_ds.getUser());
        Util.wait(500);
        
        JTextFieldOperator tfPassword = new JTextFieldOperator(a_dlg, 1);
        tfPassword.clearText();
        tfPassword.typeText(p_ds.getPassword());
        Util.wait(500);
        
        JCheckBoxOperator chkRemember = new JCheckBoxOperator(a_dlg);
        chkRemember.setSelected(true);
        
        new JButtonOperator(a_dlg, getBundleString("OK")).pushNoBlock();
        
        // Add data source, if connection is OK
        
        Util.wait(2000);
        try {
            new org.netbeans.jellytools.TopComponentOperator("Output").close();
        }catch(Exception e) {
            e.printStackTrace();
        }
        //new JButtonOperator(a_dlg, getBundleString("OK")).pushNoBlock();
        
    }
    
    /**
     * Changes parameters of a data source.
     * @param p_dsname a name of data source
     * @param p_ds an object DataSource with properties of data source
     * @return true if data source was modified successfully
     */
    public boolean modifyDataSource(String p_dsname, DataSource p_ds) {
        
        String[] a_ss = {getBundleString("Databases"), p_dsname};
        pushPopup(a_ss,getBundleString("MODIFY_DATASOURCE"));
        
        ModifyDataSourceOperator a_dlg = new ModifyDataSourceOperator();
        
        JTextFieldOperator a_tfo = a_dlg.txtDSName();
        a_tfo.clearText();
        a_tfo.typeText(p_dsname);
        Util.wait(500);
        
        a_tfo = a_dlg.txtUser();
        a_tfo.clearText();
        a_tfo.typeText(p_ds.getUser());
        Util.wait(500);
        
        a_tfo = a_dlg.txtPassword();
        a_tfo.clearText();
        a_tfo.typeText(p_ds.getPassword());
        Util.wait(500);
        
        a_tfo = a_dlg.txtURL();
        a_tfo.clearText();
        a_tfo.typeText(p_ds.getDbUrl());
        Util.wait(2000);
        
        //a_tf=(JTextField)a_dlg.waitSubComponent(new TextFieldChooser(6));
        //a_tf.setText(p_ds.getDbUrl());
        
        new QueueTool().waitEmpty();
        
        // Test connection
        
        a_dlg.btTestConnection().push();
        JDialogOperator a_dlg1 = new JDialogOperator(a_dlg, 0);
        
        boolean success = a_dlg1.getTitle().equals("Information");
        new JButtonOperator(a_dlg1, getBundleString("OK")).push();
        
        // Modify data source, if connection is OK
        
        new JButtonOperator(a_dlg, getBundleString(success ? "MODIFY" : "CANCEL")).push();
        
        return success;
    }
    
    /**
     * Removes a data source.
     * @param p_dsname a name of data source
     * @return true if data source was removed successfully
     */
    public boolean removeDataSource(String p_dsname) {
        
        String[] a_ss = {getBundleString("Databases"), p_dsname};
        
        pushPopup(a_ss,getBundleString("REMOVE_CONNECTION"));
        
        Util.wait(1000);
        
        return true;
    }
    
    /**
     * Adds new type of DB server into Server Navigator.
     * @param p_typeName a name of server type
     * @param p_template not used
     * @param p_iniDrivers drivers, needed for this DB server
     */
    public void addNewServerType(String p_typeName, String p_template, String p_iniDrivers) {
        addNewServerType(p_typeName, p_template, p_iniDrivers, "");
    }
    
    /**
     * Adds new type of DB server into Server Navigator.
     * @param p_typeName a name of server type
     * @param p_template not used
     * @param p_iniDrivers drivers, needed for this DB server
     * @param p_addDrivers additional drivers, which should be added
     */
    public void addNewServerType(String p_typeName, String p_template, String p_iniDrivers, String p_addDrivers) {
        
        pushPopup(new String[]{getBundleString("Databases"),getBundleString("Drivers")}, getBundleString("New driver..."));
        
        EditServerTypesOperator a_dlg = new EditServerTypesOperator();
        Util.wait(1000);
        
        a_dlg.btNew().pushNoBlock();
        JDialogOperator a_dlg1 = new JDialogOperator(getBundleString("Select Driver"));
        
        if (System.getProperty("os.name").equals("Mac OS X")) {
            String[] splittedPath = p_iniDrivers.substring(1, p_iniDrivers.length()).split(File.separator);
            TestUtils.wait(1000);
            JComboBoxOperator cbRoot = new JComboBoxOperator(a_dlg1);
            for (int i = 0; i < cbRoot.getItemCount(); i++)
                if (cbRoot.getItemAt(i).toString().equals(File.separator)) {
                    cbRoot.setSelectedIndex(i);
                    break;
                }
            String toCompare = "";
            for (int i = 0; i < splittedPath.length; i++) {
                JTableOperator jtoPath = new JTableOperator(a_dlg1);
                toCompare += "/" + splittedPath[i];
                System.out.println("toComapre="+toCompare);
                Point cell = jtoPath.findCell(toCompare, new Operator.DefaultStringComparator(true, true), 0);
                TestUtils.wait(1000);
                jtoPath.clickOnCell((int) cell.getY(), (int) cell.getX(),2);
                TestUtils.wait(1000);
            }
        } else {
            
            JTextFieldOperator a_tf1 = new JTextFieldOperator(a_dlg1);
            a_tf1.typeText(p_iniDrivers);
            Util.wait(500);
            new JButtonOperator(a_dlg1, "Open").push();
            Util.wait(1000);
        }
/*Case when more then one file commented
        if (!(p_addDrivers == null) && !(p_addDrivers.equals(""))) {
 
            new JButtonOperator(a_dlg, getBundleString("Add...")).pushNoBlock();
            a_dlg1 = new JDialogOperator(getBundleString("Select driver"));
 
            a_tf1 = new JTextFieldOperator(a_dlg1);
            a_tf1.typeText(p_addDrivers);
 
            new JButtonOperator(a_dlg1, "Open").push();
            Util.wait(1000);
        }
 
        a_tf1 = a_dlg.txtName();
        a_tf1.clearText();
        a_tf1.typeText(p_typeName);
        Util.wait(1000);
 */
/*
        a_tf1 = a_dlg.txtTemplate();
        a_tf1.clearText();
        a_tf1.typeText(p_template);
        Util.wait(1000);
 
 
        // try to close window without choosing driver class. Error message should appear
        a_dlg.btOK().push();
        a_dlg1 = new JDialogOperator("Information");
        Util.wait(1000);
        new JButtonOperator(a_dlg1, getBundleString("OK")).push();
 */
        // choose driver class and close window
        //a_dlg.btFind().push();
        //a_dlg.cboDriver().setSelectedIndex(0);
        
        Util.wait(1000);
        a_dlg.btOK().push();
        
    }
    
    /**
     * Deletes a type of DB server from Server Navigator.
     * @param p_typeName a name of server type
     */
    public void deleteServerType(String p_typeName) {
        
        pushPopup(getBundleString("Databases"), getBundleString("CONFIGURE_JDBC_DRIVER"));
        
        EditServerTypesOperator a_dlg = new EditServerTypesOperator();
        Util.wait(1000);
        
        a_dlg.lstServerTypes().setSelectedValue(p_typeName, true);
        a_dlg.btRemove().pushNoBlock();
        
        //        JDialogOperator a_dlg1=new JDialogOperator("Question");
        //        a_dlg1.pressKey(KeyEvent.VK_ENTER);
        
        Util.wait(1000);
        
        a_dlg.closeByButton();
    }
    
    /**
     * Opens a window "View Data" for some database table/view.
     * @param p_path a path of required tree node
     */
    public void viewData(String p_path) {
        pushPopup(p_path, getBundleString("VIEW_DATA"));
    }
    
    /**
     * Opens a window "View Data" for some database table/view.
     * @param p_path a string array with path of required tree node
     * (sequence of node names from the root node to a required node)
     */
    public void viewData(String[] p_path) {
        pushPopup(p_path, getBundleString("VIEW_DATA"));
    }
    
    //    public void viewData(TreePath p_path) {
    //
    //        JPopupMenuOperator popup = new JPopupMenuOperator(tree.callPopupOnPath(p_path));
    //        popup.pushMenu(getBundleString("VIEW_DATA"));
    //    }
    
    /**
     * Closes an opened window "View Data".
     */
    public void closeDataView() {
        Util.closeWindow();
    }
    
    /**
     * Opens a window "View Data" for a schema of some database table/view.
     * @param p_ds a name of required data source
     * @param p_schema not used
     */
    public void showSchema(String p_ds, String[] p_schema) {
        pushPopup(getBundleString("Databases") + "/" + p_ds, getBundleString("VIEW_DATA"));
    }
    
    /**
     * Finds in a bundle file and returns an actual text of control component.
     * @param p_text string-key corresponding to required control component.
     * @return actual text of control component
     */
    public static String getBundleString(String p_text) {
        System.out.println("Getting bundle for " + p_text);
/*        try {
            return Bundle.getStringTrimmed("org.netbeans.modules.visualweb.dataconnectivity.actions.Bundle", p_text);
        } catch (JemmyException e) {}
        try {
            return Bundle.getStringTrimmed("org.netbeans.modules.visualweb.dataconnectivity.ui.Bundle", p_text);
        } catch (JemmyException e) {}
        return null;
 */
        
        // stub
        return p_text;
    }
    
    /**
     * Checks, whether built-in database server PointBase is running or not.
     * @return true - DB server ir running, false - otherwise
     */
    public static boolean isPBRunning() {
        boolean result = true;
        JPopupMenuOperator pointbasePopupMenu = getTreeNodePopupMenu(
                Bundle.getStringTrimmed("org.netbeans.modules.visualweb.dataconnectivity.explorer.ideDb.Bundle",
                "Bundled_DB_ServerNav_NodeName", pbParams));
        if (pointbasePopupMenu != null) {
            result = !(TestUtils.findPopupMenuItemByLabel(pointbasePopupMenu,
                    Bundle.getStringTrimmed("org.netbeans.modules.visualweb.dataconnectivity.explorer.ideDb.Bundle",
                    "Bundled_DB_Start", pbParams)).isEnabled());
        } else {
            Assert.fail("Popup menu for bundled server not found");
        }
        pointbasePopupMenu.pressKey(KeyEvent.VK_ESCAPE);
        new QueueTool().waitEmpty();
        Util.wait(500);
        return result;
    }
    
    /**
     * Shuts a built-in database server PointBase down.
     */
    public static void stopPB(){
        JPopupMenuOperator pointbasePopupMenu = getTreeNodePopupMenu(
                Bundle.getStringTrimmed("org.netbeans.modules.visualweb.dataconnectivity.explorer.ideDb.Bundle",
                "Bundled_DB_ServerNav_NodeName", pbParams));
        if (pointbasePopupMenu != null) {
            pointbasePopupMenu.pushMenuNoBlock(
                    Bundle.getStringTrimmed("org.netbeans.modules.visualweb.dataconnectivity.explorer.ideDb.Bundle",
                    "Bundled_DB_Stop", pbParams));
            new QueueTool().waitEmpty();
            Util.wait(2000);
            System.out.println("Pointbase Database Server has been stopped.");
        }
        new QueueTool().waitEmpty();
        Util.wait(1000);
    }
    
    /**
     * Launches a built-in database server PointBase.
     */
    public static void startPB(){
        JPopupMenuOperator pointbasePopupMenu = getTreeNodePopupMenu(
                Bundle.getStringTrimmed("org.netbeans.modules.visualweb.dataconnectivity.explorer.ideDb.Bundle",
                "Bundled_DB_ServerNav_NodeName", pbParams));
        if (pointbasePopupMenu != null) {
            pointbasePopupMenu.pushMenuNoBlock(
                    Bundle.getStringTrimmed("org.netbeans.modules.visualweb.dataconnectivity.explorer.ideDb.Bundle",
                    "Bundled_DB_Start", pbParams));
            new QueueTool().waitEmpty();
            Util.wait(2000);
            System.out.println("Pointbase Database Server has started.");
        }
        new QueueTool().waitEmpty();
        Util.wait(1000);
    }
    
    private static JTreeOperator selectTreeNode(String nodeName) {
        try {
            // select the Server Navigator and set the JTreeOperator
            QueueTool queueTool = new QueueTool();
            queueTool.waitEmpty(100);
            ServerNavigatorOperator serverExplorer = showNavigatorOperator();
            serverExplorer.makeComponentVisible();
            
            JTreeOperator tree = serverExplorer.getTree();
            
            // Increase timeout for tree to redisplay after refresh
            tree.getTimeouts().setTimeout("JTreeOperator.WaitNodeExpandedTimeout", 60000);
            tree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 60000);
            tree.getTimeouts().setTimeout("JTreeOperator.WaitNodeVisibleTimeout", 60000);
            
            TreePath serverNode = tree.findPath(nodeName);
            serverExplorer.getTree().selectPath(serverNode);
            queueTool.waitEmpty(100);
            
            return tree;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static JPopupMenuOperator getTreeNodePopupMenu(String nodeName) {
        try {
            QueueTool queueTool = new QueueTool();
            JTreeOperator tree = selectTreeNode(nodeName);
            
            TreePath serverNode = tree.findPath(nodeName);
            queueTool.waitEmpty(100);
            
            JPopupMenuOperator popupMenu = new JPopupMenuOperator(
                    tree.callPopupOnPath(serverNode));
            queueTool.waitEmpty(100);
            Util.wait(100);
            
            return popupMenu;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
