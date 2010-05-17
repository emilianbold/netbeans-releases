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
import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.awt.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.text.*;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.*;
import org.netbeans.modules.visualweb.gravy.model.deployment.*;
import org.netbeans.modules.visualweb.gravy.designer.*;
import org.netbeans.modules.visualweb.gravy.toolbox.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jemmy.drivers.*;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.modules.visualweb.gravy.properties.*;

public class Utils implements Constants {
    public static PrintStream logStream;

    private static final long WAIT_NEXT_NODE_TIMEOUT = 1500; // milliseconds

    public static void openLog() {
        closeLog();
        String logFilePath = TEST_LOG_FILE_PATH + "/" + TEST_LOG_FILE_NAME;
        try {
            File logFile = new File(logFilePath);
            if (logFile.exists()) {
                logFile.delete();
            }
            FileOutputStream logFileStream = new FileOutputStream(logFile, true);
            logStream = new PrintStream(logFileStream);
        } catch (Exception e) {
            e.printStackTrace();
            logStream = null;
            throw new RuntimeException(e);
        }
    }
    public static void closeLog() {
        if (logStream != null) {
            try {
                logStream.flush();
                logStream.close();
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                logStream = null;
            }
        }
    }
    
    public static void logMsg(String msg) {
        logMsg(msg, true, true);
    }
    public static void logMsg(String msg, boolean spaceBefore, boolean spaceAfter) {
        logMsg(logStream, msg, spaceBefore, spaceAfter);
    }
    public static void logMsg(PrintStream out, String msg, boolean spaceBefore,
        boolean spaceAfter) {
        if (spaceBefore) {
            out.println();
        }
        out.println(msg);
        if (spaceAfter) {
            out.println();
        }
    }
    public static void logMsg(Object... dataArray) {
        debugOutput(logStream, dataArray);
    }
    public static void debugOutput(Object... dataArray) {
        debugOutput(logStream, dataArray);
    }
    public static void debugOutput(PrintStream out, Object... dataArray) {
        out.println();
        for (Object obj : dataArray) {
            out.println(obj.toString());
        }
        out.println();
    }
    
    public static void doSaveAll() {
        Util.getMainMenu().pushMenuNoBlock(MAIN_MENU_ITEM_WINDOW_SAVE_ALL);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        logMsg("+++ [Save All] action has been performed");
    }
    
    public static void doCloseWindow() {
        Util.getMainMenu().pushMenuNoBlock(MAIN_MENU_ITEM_WINDOW_CLOSE_WINDOW);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        logMsg("+++ [Close Window] action has been performed");
    }
    
    public static void doResetWindows() {
        Util.getMainMenu().pushMenuNoBlock(MAIN_MENU_ITEM_WINDOW_RESET_WINDOWS);
        Util.wait(2500);
        new QueueTool().waitEmpty();
        logMsg("+++ [Reset Windows] action has been performed");
    }
    
    public static String setDeploymentTargetProperties(
        DeploymentTargetDescriptor deploymentTargetDescriptor) {
        String errMsg = null;
        
        String appServerType = TestPropertiesHandler.getServerProperty("Application_Server_Type");
        if (appServerType == null) {
            errMsg = "--- Deployment Target (application server) isn't defined ---";
            return errMsg;
        }
        if (ServerExplorerOperator.STR_NAME_APPSERVER.toLowerCase().contains(
            appServerType.toLowerCase())) {
            return setSunAppServerProperties(deploymentTargetDescriptor);
        } else if ((ServerExplorerOperator.STR_NAME_TOMCAT50 + 
            ServerExplorerOperator.STR_NAME_TOMCAT55).toLowerCase().contains(
            appServerType.toLowerCase())) {
            return setTomcatAppServerProperties(deploymentTargetDescriptor);
        } else {
            errMsg = "--- Unknown Deployment Target (application server): [" + 
                appServerType + "] ---";
        }
        return errMsg;
    }
    
    public static String setSunAppServerProperties(
        DeploymentTargetDescriptor deploymentTargetDescriptor) {
        String errMsg = null, 
               serverType = ServerExplorerOperator.STR_NAME_APPSERVER,
               appServerAbsPath = TestPropertiesHandler.getServerProperty("Application_Server_AbsPath");

        Hashtable deploymentTargetProperties = deploymentTargetDescriptor.getProperties();
        deploymentTargetProperties.put(
            DeploymentTargetDescriptor.SERVER_TYPE_KEY, 
            serverType);
        deploymentTargetProperties.put(
            DeploymentTargetDescriptor.NAME_KEY + serverType, 
            TestPropertiesHandler.getServerProperty("Application_Server_Name"));
        deploymentTargetProperties.put(
            DeploymentTargetDescriptor.PATH_KEY + serverType, appServerAbsPath);
        deploymentTargetProperties.put(
            DeploymentTargetDescriptor.DOMAIN_KEY + serverType, 
            appServerAbsPath + "/" + 
            TestPropertiesHandler.getServerProperty("Application_Server_Domain_Dir"));
        deploymentTargetProperties.put(
            DeploymentTargetDescriptor.LOGIN_KEY + serverType,
            TestPropertiesHandler.getServerProperty("Application_Server_Login"));
        deploymentTargetProperties.put(
            DeploymentTargetDescriptor.PASSWORD_KEY + serverType,
            TestPropertiesHandler.getServerProperty("Application_Server_Password"));

        printDeploymentTargetProperties(deploymentTargetDescriptor);
        
        return errMsg;
    }    
    
    public static String setTomcatAppServerProperties(
        DeploymentTargetDescriptor deploymentTargetDescriptor) {
        String errMsg = "--- Deployment Target [Tomcat] isn't implemented yet ---";
        return errMsg;
    }    
    
    private static void printDeploymentTargetProperties(DeploymentTargetDescriptor deploymentTargetDescriptor) {
        Hashtable deploymentTargetProperties = deploymentTargetDescriptor.getProperties();
        Set entrySet = deploymentTargetProperties.entrySet();
        logMsg("Property of Deployment Target [" + 
            deploymentTargetProperties.get(DeploymentTargetDescriptor.SERVER_TYPE_KEY) + "]", true, false);
        for (Object entryKeyValue : entrySet) {
            Map.Entry mapEntry = (Map.Entry) entryKeyValue;
            logMsg("    Property [" + mapEntry.getKey() + "] = [" + mapEntry.getValue() + "]", false, false);
        }
    }
    
    public static void putFocusOnWindowServices() {
        putFocusOnWindow(MAIN_MENU_ITEM_WINDOW_SERVICES);
    }
    public static void putFocusOnWindowPalette() {
        putFocusOnWindow(MAIN_MENU_ITEM_WINDOW_PALETTE);
    }
    public static void putFocusOnWindowNavigator() {
        putFocusOnWindow(MAIN_MENU_ITEM_WINDOW_NAVIGATOR);
    }
    public static void putFocusOnWindowProperties() {
        putFocusOnWindow(MAIN_MENU_ITEM_WINDOW_PROPERTIES);
    }
    public static void putFocusOnWindowProjects() {
        putFocusOnWindow(MAIN_MENU_ITEM_WINDOW_PROJECTS);
    }
    
    private static void putFocusOnWindow(String windowMenuItem) {
        JMenuBarOperator menuBarOp = Util.getMainMenu();
        Util.wait(1500);
        new QueueTool().waitEmpty();
        menuBarOp.clickMouse(1, 1);
        Util.wait(1000);
        menuBarOp.pressKey(KeyEvent.VK_ESCAPE);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        menuBarOp.pushMenuNoBlock(windowMenuItem);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }

    private static TreePath findTreeNode(JTreeOperator jTreeOp, String treeNodeLabel, 
        long waitTimeout, boolean setSelected) {
        TreePath treePath = null;
        try {
            jTreeOp.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", waitTimeout);
            treePath = jTreeOp.findPath(treeNodeLabel);
            if (setSelected) {
                //jTreeOp.setVerification(use_JTreeOperator_Verification);
                Util.wait(500);
                new QueueTool().waitEmpty();
                jTreeOp.selectPath(treePath);
            }
        } catch(TimeoutExpiredException tee) {
            tee.printStackTrace(Utils.logStream);
            return null;
        } finally {
            Util.wait(500);
            new QueueTool().waitEmpty();
        }
        return treePath;
    }
    
    public static TreePath findDBConnectionTreeNode() {
        String dbURL = TestPropertiesHandler.getDatabaseProperty("DB_URL");
        
        //return findServicesTreeNode(SERVICES_TREE_NODE_DATABASES + "|" + dbURL, false);
        JTree jTree = (JTree) new ServerNavigatorOperator().getTree().getSource();
        return selectChildOfTreeNode(jTree, SERVICES_TREE_NODE_DATABASES, dbURL);
    }
    
    public static TreePath findServicesTreeNode(String treeNodeLabel, boolean setSelected) {
        return (findServicesTreeNode(treeNodeLabel, WAIT_NEXT_NODE_TIMEOUT, setSelected));
    }

    public static TreePath findServicesTreeNode(String treeNodeLabel, long waitTimeout, 
        boolean setSelected) {
        ServerNavigatorOperator serverNavigatorOp = null;
        for (int i = 0; i < 3; i++) {
            try {
                serverNavigatorOp = new ServerNavigatorOperator();
                if (serverNavigatorOp != null) break;
                Util.wait(500);
                new QueueTool().waitEmpty();        
            } catch(TimeoutExpiredException e) {
                e.printStackTrace(logStream);
            }
        }
        JTreeOperator jTreeOp = null;
        for (int i = 0; i < 3; i++) {
            try {
                jTreeOp = serverNavigatorOp.getTree();
                if (jTreeOp != null) break;
                Util.wait(500);
                new QueueTool().waitEmpty();        
            } catch(TimeoutExpiredException e) {
                e.printStackTrace(logStream);
            }
        }
        return (findTreeNode(jTreeOp, treeNodeLabel, waitTimeout, setSelected));
    }
    
    public static void callPopupMenuOnProjectsTreeNode(String treeNodeLabel, 
        String popupMenuItem) {
        callPopupMenuOnTreeNode(new ProjectNavigatorOperator().tree(), 
            treeNodeLabel, popupMenuItem);
    }
    
    public static void callPopupMenuOnServicesTreeNode(String treeNodeLabel, 
        String popupMenuItem) {
        callPopupMenuOnTreeNode(new ServerNavigatorOperator().getTree(), treeNodeLabel, popupMenuItem);
    }
    
    public static void callPopupMenuOnNavigatorTreeNode(String treeNodeLabel, 
        String popupMenuItem) {
        callPopupMenuOnTreeNode(getNavigatorTreeOperator(), treeNodeLabel, popupMenuItem);
    }
    
    public static JPopupMenuOperator invokePopupMenuOnTreeNode(JTreeOperator jTreeOp, 
        String treeNodeLabel) {
        JPopupMenuOperator popupMenuOp = new JPopupMenuOperator(
            jTreeOp.callPopupOnPath(
            findTreeNode(jTreeOp, treeNodeLabel, WAIT_NEXT_NODE_TIMEOUT, true)));
        Util.wait(500);
        new QueueTool().waitEmpty();
        return popupMenuOp;
    }
            
    private static void callPopupMenuOnTreeNode(JTreeOperator jTreeOp, String treeNodeLabel, 
        String popupMenuItem) {
        JPopupMenuOperator popupMenuOp = invokePopupMenuOnTreeNode(jTreeOp, treeNodeLabel);
        popupMenuOp.pushMenuNoBlock(popupMenuItem);
        Util.wait(500);
        new QueueTool().waitEmpty();
        /*        
        JMenuItem menuItem = TestUtils.findPopupMenuItemByLabel(popupMenuOp, 
            strMenuItem, false, false);
        new JMenuItemOperator(menuItem).pushNoBlock();
        Util.wait(500);
        new QueueTool().waitEmpty();
        */
    }
    
    public static JPopupMenuOperator callPopupOnPath(JTreeOperator treeOp, TreePath treePath) {
        int row = treeOp.getRowForPath(treePath);
        Rectangle rowRect = treeOp.getRowBounds(row);
        int x = rowRect.x + rowRect.width / 2, 
            y = rowRect.y + rowRect.height / 2;
        treeOp.clickMouse(x, y, 1, InputEvent.BUTTON3_MASK);        
        Util.wait(500);
        new QueueTool().waitEmpty();
        return (new JPopupMenuOperator());
    }

    public static TreePath findNavigatorTreeNode(String treeNodeLabel, boolean setSelected) {
        return (findNavigatorTreeNode(treeNodeLabel, WAIT_NEXT_NODE_TIMEOUT, setSelected));
    }

    public static TreePath findNavigatorTreeNode(String treeNodeLabel, long waitTimeout, 
        boolean setSelected) {
        JTreeOperator jTreeOp = getNavigatorTreeOperator();
        return (findTreeNode(jTreeOp, treeNodeLabel, waitTimeout, setSelected));
    }

    public static TopComponentOperator getNavigatorOperator() {
        DesignerPaneOperator designPaneOp = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        DesignerPaneOperator.switchToDesignerPane();
        
        putFocusOnWindowNavigator();
        TopComponentOperator topComponentOp = new TopComponentOperator(WINDOW_NAVIGATOR_TITLE);
        return topComponentOp;
    }    
    
    public static JTreeOperator getNavigatorTreeOperator() {
        TopComponentOperator topComponentOp = getNavigatorOperator();
        /*
        topComponentOp.getTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", 1000);
        try {
            JComboBoxOperator comboBoxOp = new JComboBoxOperator(topComponentOp);
            comboBoxOp.selectItem(NAVIGATOR_COMBOBOX_ITEM_NAVIGATOR);
        } catch (TimeoutExpiredException e) {
            e.printStackTrace(logStream);
        }
        */
        JTreeOperator treeOp = new JTreeOperator(topComponentOp);
        return treeOp;
    }

    public static JTreeOperator getServerNavigatorTreeOperator() {
        putFocusOnWindowServices();
        JTreeOperator jTreeOp = new ServerNavigatorOperator().getTree();
        return jTreeOp;
    }
    
    public static TreePath selectChildOfTreeNode(JTree jTree, String treeNodeName, String childNodeName) {
        TreePath treePath = jTree.getNextMatch(treeNodeName, 0, Position.Bias.Forward);
        return selectChildOfTreeNode(jTree, treePath, childNodeName);
    }
    public static TreePath selectChildOfTreeNode(JTree jTree, TreePath startTreePath, String childNodeName) {
        if (startTreePath == null) throw new RuntimeException("Start tree path is null");

        jTree.expandPath(startTreePath);
        Util.wait(500);        
        new QueueTool().waitEmpty();
        logMsg("+++ Expanded treePath = [" + startTreePath.toString() + "]");
        
        int row = jTree.getRowForPath(startTreePath);
            
        TreePath treePath = jTree.getNextMatch(childNodeName, row, Position.Bias.Forward);
        if (treePath == null) return null;

        jTree.setSelectionPath(treePath);
        logMsg("+++ Selected treePath = [" + treePath.toString() + "]");
        Util.wait(500);        
        new QueueTool().waitEmpty();
        
        treePath = jTree.getSelectionPath();
        return treePath;
    }

    public static TreePath selectChildOfTreeNode(JTree jTree, TreePath parentTreePath, int childNumber) {
        jTree.expandPath(parentTreePath);
        Util.wait(500);        
        new QueueTool().waitEmpty();
        logMsg("+++ Expanded treePath = [" + parentTreePath.toString() + "]");

        TreeModel treeModel = jTree.getModel();
        Object objChild = treeModel.getChild(parentTreePath.getLastPathComponent(), childNumber);
        // String childName = objChild.toString();
        TreePath childTreePath = parentTreePath.pathByAddingChild(objChild);
        
        jTree.setSelectionPath(childTreePath);
        logMsg("+++ Selected treePath = [" + childTreePath.toString() + "]");
        
        Util.wait(500);        
        new QueueTool().waitEmpty();
        return childTreePath;
    }
    
    public static void putComponentOnDesigner(String paletteName, String componentName, 
        String componentID, int x, int y, String navigatorTreeNodePrefix) {
        Utils.putFocusOnWindowPalette();
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        DesignerPaneOperator designPaneOp = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());

        PaletteContainerOperator paletteContainerOp = new PaletteContainerOperator(paletteName);

        paletteContainerOp.addComponent(componentName, designPaneOp, new Point(x, y));
        Util.wait(2000);
        new QueueTool().waitEmpty();

        logMsg("+++ Component [" + componentName + "] has been placed on Design View");
  
        String treeNodeLabel = navigatorTreeNodePrefix + componentID;
        TreePath treePath = Utils.findNavigatorTreeNode(treeNodeLabel, true);
        Util.wait(500);
        new QueueTool().waitEmpty();
        if (treePath == null) {
            throw new RuntimeException("The tree node [" + treeNodeLabel + 
                "] isn't found in the window [Navigator]");
        }
        logMsg("The tree node [" + treeNodeLabel + "] is found in the window [Navigator]");
    }
    
    public static void setTextPropertyValue(String propertyName, String propertyValue) {
        putFocusOnWindowProperties();
        SheetTableOperator propTableOp = new SheetTableOperator();
        propTableOp.setTextValue(propertyName, propertyValue);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    public static void setComboboxPropertyValue(String propertyName, String propertyValue) {
        putFocusOnWindowProperties();
        SheetTableOperator propTableOp = new SheetTableOperator();
        int rowNumber = propTableOp.findCellRow(propertyName);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        propTableOp.clickOnCell(rowNumber, 1);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        JComboBoxOperator comboboxOp = new JComboBoxOperator(propTableOp);
        comboboxOp.selectItem(propertyValue);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    public static Point getSelectedComponentLeftUpperPoint() {
        Point componentPoint = null;
        int x = -1, y = -1;
        String valuePropertyStyle = getSelectedComponentProperty(PROPERTY_NAME_STYLE);
        
        // style="position: absolute; left: 48px; top: 96px"
        x = extractComponentCoordinate(valuePropertyStyle, "left"); // get coordinate X
        y = extractComponentCoordinate(valuePropertyStyle, "top"); // get coordinate Y
        if ((x > -1) && (y > -1)) {
            componentPoint = new Point(x, y);
        }
        return componentPoint;
    }
    
    public static String getSelectedComponentProperty(String propertyName) {
        putFocusOnWindowProperties();
        SheetTableOperator propTableOp = new SheetTableOperator();
        Util.wait(500);
        new QueueTool().waitEmpty();
        String propertyValue = propTableOp.getValue(propertyName);
        logMsg("+++ Value of property [" + propertyName + 
            "] for selected component = [" + propertyValue + "]");
        return propertyValue;
    }
    
    private static int extractComponentCoordinate(String valuePropertyStyle, String coordPosition) {
        int coordValue = -1;
                
        // style="position: absolute; left: 48px; top: 96px"
        String patternPrefix = "[:;\\w\\s]*",
               patternSuffix = "[\\s]*:[\\s]*(\\d*)[:;\\w\\s]*";
        
        Pattern pattern = Pattern.compile(patternPrefix + coordPosition + patternSuffix);
        Matcher matcher = pattern.matcher(valuePropertyStyle);
        try {
            matcher.matches();
            coordValue = Integer.parseInt(matcher.group(1));
        } catch (Exception e) {
            logMsg("+++ Problem with getting [" + coordPosition + "] coordinate of selected component: " + 
                e.getMessage());
            e.printStackTrace(logStream);
        }
        return coordValue;
    }

    public static void createSessionBeanDataProvider(String dbURL, String dbTableName) { 
        createSessionBeanDataProvider(dbURL, dbTableName, null, -1, -1);
    }
    public static void createSessionBeanDataProvider(String dbURL, String dbTableName,
        String rowSetName, int radioButtonIndex, int textFieldIndex) {
        createDataProvider(dbURL, dbTableName, NAVIGATOR_TREE_NODE_SESSION_PREFIX.replace("|", ""),
             rowSetName, radioButtonIndex, textFieldIndex);
    }
    public static void createDataProvider(String dbURL, String dbTableName, String beanNodeName, 
        String rowSetName, int radioButtonIndex, int textFieldIndex) {
        //dndDBTableOnManagedBean(dbURL, dbTableName, beanNodeName);
        copyPasteDBTableOnManagedBean(dbURL, dbTableName, beanNodeName);
        if (rowSetName != null) {
            Utils.defineSessionRowSetNameInDialog(rowSetName, radioButtonIndex, textFieldIndex);
        }
        Util.wait(2000);
        new QueueTool().waitEmpty();
        Utils.doSaveAll();
    }

    public static void copyPasteDBTableOnManagedBean(String dbURL, 
        final String dbTableName, final String beanNodeName) {
        selectDBTable(dbURL, dbTableName);
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        Util.getMainMenu().pushMenuNoBlock(MAIN_MENU_ITEM_EDIT_COPY);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        Thread pasteThread = new Thread(new Runnable() {
            public void run() {
                callPopupMenuOnNavigatorTreeNode(beanNodeName, POPUP_MENU_ITEM_PASTE);
                    logMsg("+++ DB table [" + dbTableName + 
                        "] is copied on managed bean [" + beanNodeName + "]");
            }
        });
        pasteThread.start();
        try {
            logMsg("+++ Wait until paste thread is died...");
            pasteThread.join(3000);
        } catch(Exception e) {
            e.printStackTrace(logStream);
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }

    public static void dndDBTableOnManagedBean(String dbURL, final String dbTableName, 
        final String beanNodeName) {
        final JTreeOperator navigatorTreeOp = getNavigatorTreeOperator();
        TreePath beanTreePath = findNavigatorTreeNode(beanNodeName, true);
        if (beanTreePath == null) {
            throw new RuntimeException("The tree node [" + beanNodeName + 
                "] isn't found in the window [Navigator]");
        }
        final Point beanNodePoint = navigatorTreeOp.getPointToClick(beanTreePath);
        logMsg("+++ Point of bean node [" + beanNodeName + 
            "] in the window [Navigator] = [" + beanNodePoint + "]");
        
        final JTreeOperator serverNavigatorTreeOp = getServerNavigatorTreeOperator();
        TreePath dbTableTreePath = selectDBTable(dbURL, dbTableName);
        if (dbTableTreePath == null) {
            throw new RuntimeException("The tree node [" + dbTableName + 
                "] isn't found in the window [Services]");
        }
        final Point dbTableNodePoint = serverNavigatorTreeOp.getPointToClick(dbTableTreePath);
        logMsg("+++ Point of DB table node [" + dbTableName + 
            "] in the window [Services] = [" + dbTableNodePoint + "]");

        Thread dndThread = new Thread(new Runnable() {
            public void run() {
                dndFromTo(serverNavigatorTreeOp, dbTableNodePoint, 
                    navigatorTreeOp, beanNodePoint);
                    logMsg("+++ DnD is performed: DB table [" + dbTableName + 
                        "] => on managed bean [" + beanNodeName + "]");
            }
        });
        dndThread.start();
        try {
            logMsg("+++ Wait until DnD mouse thread is died...");
            dndThread.join(3000);
        } catch(Exception e) {
            e.printStackTrace(logStream);
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    public static void dndFromTo(ComponentOperator sourceComponent, Point sourcePoint, 
        ComponentOperator targetComponent, Point targetPoint) {
        dndFromTo(sourceComponent, sourcePoint, targetComponent, targetPoint, 
            InputEvent.BUTTON1_MASK, 0);
    }
    public static void dndFromTo(ComponentOperator sourceComponent, Point sourcePoint, 
        ComponentOperator targetComponent, Point targetPoint, int mouseButton, int keyModifiers) {
        Timeout dndTimeout = new Timeout("MouseRobotDriver timeout for Drag-n-Drop", 1000);
        MouseRobotDriver mouseRobotDriver = new MouseRobotDriver(dndTimeout);
        int startX = sourceComponent.getSource().getLocationOnScreen().x + sourcePoint.x + 1,
            startY = sourceComponent.getSource().getLocationOnScreen().y + sourcePoint.y + 1,
            endX = targetComponent.getSource().getLocationOnScreen().x + targetPoint.x + 1,
            endY = targetComponent.getSource().getLocationOnScreen().y + targetPoint.y + 1;
        mouseRobotDriver.dragNDrop(startX, startY, endX, endY, mouseButton, 
            keyModifiers, dndTimeout, dndTimeout);
        
        Util.wait(500);
        new QueueTool().waitEmpty();
    }
    
    public static TreePath selectDBTable(String dbURL, String dbTableName) {
        //TreePath treePath = findServicesTreeNode(SERVICES_TREE_NODE_DATABASES + "|" + dbURL + "|" + 
        //    DB_TREE_NODE_TABLES + "|" + dbTableName, true);
        JTree jTree = (JTree) getServerNavigatorTreeOperator().getSource();
        TreePath treePath = selectChildOfTreeNode(jTree, dbURL, DB_TREE_NODE_TABLES);
        jTree.expandPath(treePath);
        Util.wait(500);
        new QueueTool().waitEmpty();
        
        TreeModel treeModel = jTree.getModel();
        int childCount = treeModel.getChildCount(treePath.getLastPathComponent());
        // wait until childCount (amount of available DB tables) is greater than 1 because
        // several seconds only one child exists - the subnode with text "Please, wait..."
        for (int i = 0; i < 10; i++) {
            if (childCount > 1) break;
            childCount = treeModel.getChildCount(treePath.getLastPathComponent());
            Util.wait(1000);
            new QueueTool().waitEmpty();
        }
        TreePath dbTableTreePath = null;
        for (int i = 0; i < childCount; i++) {
            Object objDBTable = treeModel.getChild(treePath.getLastPathComponent(), i);
            if (objDBTable.toString().equalsIgnoreCase(dbTableName)) {
                dbTableTreePath = treePath.pathByAddingChild(objDBTable);
                jTree.setSelectionPath(dbTableTreePath);
                break;
            }
        }
        return dbTableTreePath;
    }    
    
    public static void callPopupMenuForDBTable(String dbURL, String dbTableName, 
        String popupMenuItem) {
        TreePath treePath = selectDBTable(dbURL, dbTableName);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        JPopupMenuOperator popupMenuOp = callPopupOnPath(getServerNavigatorTreeOperator(), 
            treePath);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        popupMenuOp.pushMenuNoBlock(popupMenuItem);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    public static void putDBTableOnComponent(String dbURL, String dbTableName, 
        Point componentPoint) {
        putDBTableOnComponent(dbURL, dbTableName, componentPoint, null, -1, -1);
    }
    public static void putDBTableOnComponent(String dbURL, String dbTableName, 
        final Point componentPoint, String rowSetName, int radioButtonIndex, int textFieldIndex) {
        final DesignerPaneOperator designPaneOp = new DesignerPaneOperator(RaveWindowOperator.getDefaultRave());
        DesignerPaneOperator.switchToDesignerPane();
        TreePath dbTableTreePath = selectDBTable(dbURL, dbTableName);
        if (dbTableTreePath != null) {
            Thread mouseThread = new Thread(new Runnable() {
                public void run() {
                    designPaneOp.clickMouse(componentPoint.x + 5, componentPoint.y + 5, 1);
                    logMsg("+++ Mouse thread has been finished.");
                }
            });
            mouseThread.start();
            try {
                logMsg("+++ Wait until mouse thread is died...");
                mouseThread.join(6000);
            } catch(Exception e) {
                e.printStackTrace(logStream);
            }
            if (rowSetName != null) {
                defineSessionRowSetNameInDialog(rowSetName, radioButtonIndex, textFieldIndex);
            }
            Util.wait(2000);
            new QueueTool().waitEmpty();
            logMsg("+++ DB table [" + dbTableName + "] is bound to component with point [" +
                componentPoint + "]");
        }
    }
 
    public static void defineSessionRowSetNameInDialog(String rowSetName, 
        int radioButtonIndex, int textFieldIndex) {
        String timeoutName = "DialogWaiter.WaitDialogTimeout";
        long previousTimeoutValue = JemmyProperties.getCurrentTimeout(timeoutName),
             newTimeoutValue = 10000;
        JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, newTimeoutValue);
        try {
            JDialogOperator dialogOp = new JDialogOperator(
                DIALOG_TITLE_ADD_NEW_DATAPROVIDER_WITH_ROWSET);
            if (dialogOp != null) {
                logMsg("+++ Dialog [" + DIALOG_TITLE_ADD_NEW_DATAPROVIDER_WITH_ROWSET + 
                    "] is found (RowSet name: [" + rowSetName + "])");
                
                new JRadioButtonOperator(dialogOp, radioButtonIndex).doClick();
                Util.wait(1000);
                new QueueTool().waitEmpty();

                new JTextFieldOperator(dialogOp, textFieldIndex).setText(rowSetName);
                Util.wait(1000);
                new QueueTool().waitEmpty();

                new JButtonOperator(dialogOp, BUTTON_LABEL_OK).pushNoBlock();
                Util.wait(1000);
                new QueueTool().waitEmpty();
            }
        } catch(TimeoutExpiredException tee) {
            Util.wait(500);
            new QueueTool().waitEmpty();
            throw new RuntimeException("Dialog [" + 
                DIALOG_TITLE_ADD_NEW_DATAPROVIDER_WITH_ROWSET + "] isn't found " +
                "(RowSet name: [" + rowSetName + "])");
        } catch(Exception e) {
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        } finally {
            JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, previousTimeoutValue);
        }
    }
    
    private static void selectComponentOnDesigner(DesignerPaneOperator designPaneOp, Point componentPoint) {
        designPaneOp.clickMouse(componentPoint.x + 3, componentPoint.y + 3, 1);
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
    
    public static String getBaseRowSetName(String dbTableName) {
        return (dbTableName.toLowerCase() + ROW_SET_SUFFIX);
    }
    
    public static String getBaseDataProviderName(String dbTableName) {
        return (dbTableName.toLowerCase() + DATA_PROVIDER_SUFFIX);
    }

    public static EditorOperator getJavaEditor() {
        EditorOperator editor = new EditorOperator(Util.getMainWindow(),JAVA_EDITOR_TITLE);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        logMsg("+++ Source editor for [" + JAVA_EDITOR_TITLE + "] found");
        return editor;
    }
    
    public static boolean isUsedJ2EELevel_14() {
        String j2EE_Level = TestPropertiesHandler.getServerProperty("J2EE_Level");
        return (j2EE_Level.equalsIgnoreCase(J2EE_LEVEL_14));
    }
    
    public static boolean isUsedJ2EELevel_5() {
        String j2EE_Level = TestPropertiesHandler.getServerProperty("J2EE_Level");
        return (j2EE_Level.equalsIgnoreCase(J2EE_LEVEL_5));
    }
    
    public static boolean isUsedDBDerby() {
        String database = TestPropertiesHandler.getDatabaseProperty("Database").trim();
        return (database.equalsIgnoreCase(DB_NAME_DERBY));
    }
    
    public static boolean isUsedDBOracle() {
        String database = TestPropertiesHandler.getDatabaseProperty("Database").trim();
        return (database.equalsIgnoreCase(DB_NAME_ORACLE));
    }
    
    public static boolean isUsedDBMySQL() {
        String database = TestPropertiesHandler.getDatabaseProperty("Database").trim();
        return (database.equalsIgnoreCase(DB_NAME_MYSQL));
    }
    
    public static boolean isUsedDBPostgres() {
        String database = TestPropertiesHandler.getDatabaseProperty("Database").trim();
        return (database.equalsIgnoreCase(DB_NAME_POSTGRES));
    }
    
    public static boolean isStringEmpty(String s) {
        return ((s == null) || (s.trim().length() == 0));
    }
}
