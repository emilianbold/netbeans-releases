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

import org.netbeans.modules.visualweb.gravy.DNDDriver;
import org.netbeans.modules.visualweb.gravy.TestUtils;
import org.netbeans.modules.visualweb.gravy.TopComponentOperator;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.websvc.AddWebServiceOperator;
import org.netbeans.modules.visualweb.gravy.nodes.WebServicesNode;
import org.netbeans.modules.visualweb.gravy.designer.DesignerPaneOperator;
import org.netbeans.modules.visualweb.gravy.actions.ActionNoBlock;
import org.netbeans.modules.visualweb.gravy.model.deployment.DeploymentTargetDescriptor;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.*;
import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.KeyEvent;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import java.util.Hashtable;

/**
 * This class implements test functionality for the window "ServerExplorer".
 */
public class ServerExplorerOperator extends TopComponentOperator {

    public static final String
        STR_NAME_WEBLOGIC = "BEA WebLogic Server",
        STR_NAME_JBOSS = "JBoss Application Server",
        STR_NAME_APPSERVER = "Sun Java System Application Server",
        STR_NAME_GLASSFISH_V1 = "GlassFish V1",
        STR_NAME_GLASSFISH_V2 = "GlassFish V2",
        STR_NAME_TOMCAT50 = "Tomcat 5.0",
        STR_NAME_TOMCAT55 = "Tomcat 5.5",
        STR_NAME_TOMCAT60 = "Tomcat 6.0",
        STR_MENU_ITEM_SERVER_MANAGER = "Tools|Servers",
        STR_DIALOG_TITLE_SERVER_MANAGER = "Servers",
        STR_WIZARD_TITLE_ADD_SERVER = "Add Server Instance",
        STR_BUTTON_TITLE_ADD_SERVER = "Add Server...",
        STR_BUTTON_TITLE_CLOSE = "Close",
        STR_BUTTON_TITLE_CHOOSE = "Choose",
        STR_MENU_ITEM_START = "Start",
        STR_MENU_ITEM_STOP = "Stop",
        STR_SERVERS_PATH = "Servers|",
        STR_SERVER_START = "Starting ",
        STR_SERVER_STOP = "Stopping ";
    
    JTreeOperator tree = null;

    /**
     * Creates a new instance of this class
     * @param parent an object ContainerOperator related to container, which 
     * includes window "Server Explorer".
     */
    public ServerExplorerOperator(ContainerOperator parent) {
        super(parent, "Services");
    }

    /**
     * Creates a new instance of this class
     */
    public ServerExplorerOperator() {
        this(Util.getMainWindow());
    }

    /**
     * Returns a tree, which is contained in the window "Server Explorer".
     * @return an object JTreeOperator
     */
    public JTreeOperator getTree() {
        if (tree == null) {
            makeComponentVisible();
            tree = new JTreeOperator(this);
        }
        return (tree);
    }

    /**
     * Selects a tree node according to a required tree path.
     * @param treePath a path of required tree node
     */
    public void selectPath(String treePath) {
        makeComponentVisible();
        TestUtils.wait(500);
        TreePath path = getTree().findPath(treePath);
        TestUtils.wait(500);
        tree.selectPath(path);
    }

    /**
     * Clicks an item of popup menu, related to required tree node.
     * @param treePath a path of required tree node
     * @param menu a name of menu item
     */
    public void pushPopup(String treePath, String menu) {
        pushPopup(getTree(), treePath, menu);
    }

    /**
     * Clicks an item of popup menu, related to required tree node.
     * @param tree an object JTreeOperator, related to a tree
     * @param treePath a path of required tree node
     * @param menu a name of menu item
     */
    public void pushPopup(JTreeOperator tree, String treePath, String menu) {
        new JPopupMenuOperator(getTree().callPopupOnPath(getTree().findPath(treePath))).pushMenuNoBlock(menu);
        new QueueTool().waitEmpty();
        TestUtils.wait(100);
    }

    /**
     * Clicks an item of popup menu, related to required tree node.
     * @param treePath a string array with path of required tree node 
     * (sequence of node names from the root node to a required node)
     * @param menu a name of menu item
     */
    public void pushPopup(String[] treePath, String menu) {
        new JPopupMenuOperator(getTree().callPopupOnPath(getTree().findPath(treePath))).pushMenuNoBlock(menu);
        new QueueTool().waitEmpty();
        TestUtils.wait(100);
    }

    /**
     * Adds database table on Design View via popup menu, related to its tree node.
     * @param treePath a path of required tree node
     */
    public void addTable(String treePath) {
        selectPath(treePath);
        pushPopup(getTree(), treePath, "Add to Form");
    }

    /**
     * Adds database table on Design View via popup menu, related to its tree node.
     * @param treePath a path of required tree node
     * @param designer an object DesignerPaneOperator
     */
    public void addTable(String treePath, DesignerPaneOperator designer) {
        addTable(treePath, designer, new Point(1,1));
    }

    /**
     * Adds database table on Design View via popup menu, related to its tree node.
     * @param treePath a path of required tree node as object TreePath
     * @param designer an object DesignerPaneOperator
     */
    public void addTable(TreePath treePath, DesignerPaneOperator designer) {
        addTable(treePath, designer, new Point(1,1));
    }

    /**
     * Adds database table on Design View via popup menu, related to its tree node.
     * @param treePath a path of required tree node
     * @param designer an object DesignerPaneOperator
     * @param location a point on Design View for mouse clicking
     */
    public void addTable(String treePath, DesignerPaneOperator designer, Point location) {
        addTable(tree.findPath(treePath), designer, location);
    }

    /**
     * Adds database table on Design View via popup menu, related to its tree node.
     * @param treePath a path of required tree node as object TreePath
     * @param designer an object DesignerPaneOperator
     * @param location a point on Design View for mouse clicking
     */
    public void addTable(TreePath treePath, DesignerPaneOperator designer, Point location) {
        makeComponentVisible();
        TestUtils.wait(2000);
        //ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(500);
        tree.selectPath(treePath);
        TestUtils.wait(3000);
        new DNDDriver().dnd(tree, tree.getPointToClick(treePath), designer, location);
        TestUtils.wait(3000);
    }

    /**
     * Undeploys a project from an application server.
     * @param server a name of application server
     * @param prj a name of project
     * @param parse boolean parameter: true - only the first 2 chars of the 
     * project name are considered, false - full project name is valuable
     */
    public static void undeployProject(String server, String prj, boolean parse) {

        String serverPath = null;

        serverPath = "Deployment Server";


        System.out.println("default is " + serverPath);

        // Select the Server Navigator and set the JTreeOperator   
        new QueueTool().waitEmpty(100);
        ServerNavigatorOperator explorer = ServerNavigatorOperator.showNavigatorOperator();
        explorer.makeComponentVisible();
        JTreeOperator tree = explorer.getTree();

        // Sleep 4 secs to make sure Server Navigator is in focus
        Util.wait(4000);

        // Need to refresh J2EE AppServer node
        explorer.pushPopup(tree, serverPath, "Refresh");

        // Increase timeout for tree to redisplay after refresh
        tree.getTimeouts().setTimeout("JTreeOperator.WaitNodeExpandedTimeout", 60000);
        tree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 60000);
        tree.getTimeouts().setTimeout("JTreeOperator.WaitNodeVisibleTimeout", 60000);
        
        TestUtils.wait(1000);
        explorer.selectPath(serverPath+"|Deployed Components");
        explorer.getTree().expandPath(explorer.getTree().findPath(serverPath+"|Deployed Components"));
        System.out.println("TRACE: Path =" + serverPath+"|Deployed Components");
        TestUtils.wait(1000);
        
        //      TODO delete workaround
        //      ************* workaround *************
        explorer.pushPopup(tree, serverPath, "Refresh");
        TestUtils.wait(1000);
        explorer.selectPath(serverPath+"|Deployed Components");
        explorer.getTree().expandPath(explorer.getTree().findPath(serverPath+"|Deployed Components"));
        System.out.println("TRACE: Path =" + serverPath+"|Deployed Components");
        TestUtils.wait(1000);
        //      ************* workaround *************
        
        explorer.selectPath(serverPath+"|Deployed Components|/"+prj);
        System.out.println("TRACE: Path =" + serverPath+"|Deployed Components|/"+prj);
        TestUtils.wait(1000);
        System.out.println("TRACE: Push Menu Undeploy...");
        explorer.pushPopup(explorer.getTree(), serverPath+"|Deployed Components|/"+prj, "Undeploy");
    }

    /**
     * Checks undeployment of a project from an application server.
     * @param server a name of application server
     * @param prj a name of project
     * @param parse boolean parameter: true - only the first 2 chars of the 
     * project name are considered, false - full project name is valuable
     */
    public static boolean verifyUndeployment(String server, String prj, boolean parse) {

        System.out.println("Started!");

        String serverPath = null;
        String pePath = "Deployment Server";
        String deploymentPath = null;
        String defaultServer = null;

        serverPath = pePath;
        serverPath = "Deployment Server";


        // Select the Server Navigator and set the JTreeOperator   
        new QueueTool().waitEmpty(100);
        ServerNavigatorOperator explorer = ServerNavigatorOperator.showNavigatorOperator();
        explorer.makeComponentVisible();
        JTreeOperator tree = explorer.getTree();


        // Need to refresh J2EE AppServer node
        explorer.pushPopup(tree, serverPath, "Refresh");

        // Increase timeout for tree to redisplay after refresh
        tree.getTimeouts().setTimeout("JTreeOperator.WaitNodeExpandedTimeout", 60000);
        tree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 60000);
        tree.getTimeouts().setTimeout("JTreeOperator.WaitNodeVisibleTimeout", 60000);

        // Get treepath of the parent node of Deployed components
        //recreate operator (just in case - to avoid caching)
        JTreeOperator oldTree = tree;
        tree = new JTreeOperator((JTree) tree.getSource());
        tree.copyEnvironment(oldTree);
        //do it two times 'cause refresh is delayed.
        TreePath deployedComps;

        try {
            deployedComps = tree.findPath(serverPath + "|Deployed Components");
        } catch (TimeoutExpiredException e) {
            deployedComps = tree.findPath(serverPath + "|Deployed Components");
        }

        // Expand the SN to the Deployed Components node
        explorer.getTree().selectPath(deployedComps);
        explorer.getTree().expandPath(deployedComps);

        Util.wait(1000);
        new QueueTool().waitEmpty(100);

        // Get a TreePath array of the deployed components
        TreePath[] aDeployedComps = tree.getChildPaths(deployedComps);

        boolean undeployed = true;
        // Search for a project matching prj if matches then undeploy
        for (int i = 0; i < aDeployedComps.length; i++) {

            // Get the index (position) of the project in the TreePath
            String prjNode = aDeployedComps[i].getLastPathComponent().toString();
            if (parse) prjNode = prjNode.substring(0, 3);

            JemmyProperties.getCurrentOutput().printLine(aDeployedComps[i].toString());
            JemmyProperties.getCurrentOutput().printLine(aDeployedComps[i].getLastPathComponent().toString());

            if (prjNode.equals("/" + prj)) {
                Util.wait(1000);
                undeployed = false;
                new QueueTool().waitEmpty(100);
            }
        } // end for

        return undeployed;
    }

    /**
     * Removes a resource from an application server.
     * @param resource a name of resource (tree node)
     */
    public static void deleteResource(String resource) {

        System.out.println("Started!");

        String serverPath = null;
        String pePath = "Deployment Server";
        TreePath resourcePath = null;
        String defaultServer = null;

        serverPath = pePath;
        serverPath = "Deployment Server";


        // Select the Server Navigator and set the JTreeOperator   
        new QueueTool().waitEmpty(100);
        ServerNavigatorOperator explorer = ServerNavigatorOperator.showNavigatorOperator();
        explorer.makeComponentVisible();
        JTreeOperator tree = explorer.getTree();


        // Need to refresh J2EE AppServer node
        explorer.pushPopup(tree, serverPath, "Refresh");

        // Increase timeout for tree to redisplay after refresh
        tree.getTimeouts().setTimeout("JTreeOperator.WaitNodeExpandedTimeout", 60000);
        tree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 60000);
        tree.getTimeouts().setTimeout("JTreeOperator.WaitNodeVisibleTimeout", 60000);

        // Get treepath of the parent node of Deployed components
        //recreate operator (just in case - to avoid caching)
        JTreeOperator oldTree = tree;
        tree = new JTreeOperator((JTree) tree.getSource());
        tree.copyEnvironment(oldTree);
        //do it two times 'cause refresh is delayed.
        

        try {
            explorer.makeComponentVisible();
            TestUtils.wait(500);
            resourcePath = tree.findPath(serverPath + "|Resources");
        } catch (TimeoutExpiredException e) {
            explorer.makeComponentVisible();
            TestUtils.wait(500);
            resourcePath = tree.findPath(serverPath + "|Resources");
        }

        TestUtils.wait(500);
        // Expand the SN to the Deployed Components node
        explorer.getTree().selectPath(resourcePath);
        explorer.getTree().expandPath(resourcePath);

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        new QueueTool().waitEmpty(100);

        // Get a TreePath array of the deployed components
        TreePath[] aResources = tree.getChildPaths(resourcePath);

        // Search for a project matching prj if matches then undeploy
        for (int i = 0; i < aResources.length; i++) {
            // Get the index (position) of the project in the TreePath
            String resNode = aResources[i].getLastPathComponent().toString();

            JemmyProperties.getCurrentOutput().printLine(aResources[i].toString());
            JemmyProperties.getCurrentOutput().printLine(aResources[i].getLastPathComponent().toString());

            if (resNode.equals(resource)) {
                Util.wait(1000);
                new QueueTool().waitEmpty(100);
                //Remove resource
                explorer.pushPopup(tree, serverPath + "|Resources|" + resource, "Delete");
                new QueueTool().waitEmpty(100);
            }
        } // end for
    }

    /** 
     * Stops an application server.
     * @param server a name of an application server (name of tree node)
     */
    public static void stopServer(String server) {
        // Select the Runtime and set the JTreeOperator
        new QueueTool().waitEmpty(100);
        ServerNavigatorOperator explorer = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(2000);
        JTreeOperator tree = explorer.getTree();
        TestUtils.wait(1000);
        
        // Select the requested server node
        tree.selectPath(tree.findPath(STR_SERVERS_PATH + server));
        TestUtils.wait(5000);
        // open context menu on the requested server node
        new QueueTool().waitEmpty(100);
        tree.callPopupOnPath(tree.findPath(STR_SERVERS_PATH + server));
        TestUtils.wait(500);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        if (!(new JMenuItemOperator(snpm, STR_MENU_ITEM_STOP).isEnabled()))
            tree.pushKey(KeyEvent.VK_ESCAPE);
        else {
            JLabelOperator jlo = new JLabelOperator(Util.getMainWindow(), 1);
            new JMenuItemOperator(snpm, STR_MENU_ITEM_STOP).push();
            while (jlo.getText() == null || !jlo.getText().equals(STR_SERVER_STOP + server)) {
                jlo = new JLabelOperator(Util.getMainWindow(), 1);
                System.out.println("label in first cycle = " + jlo.getText());
                TestUtils.wait(1000);
            }
            while (jlo.getText() != null && jlo.getText().equals(STR_SERVER_STOP + server)) {
                jlo = new JLabelOperator(Util.getMainWindow(), 1);
                TestUtils.wait(1000);
            }
            TestUtils.wait(1000);
        }
        new QueueTool().waitEmpty(100);
        TestUtils.wait(5000);
    }
    
    /** 
     * Checks if an application server is started or not.
     * @param server a name of an application server (name of tree node)
     * @return true - server is running, false - otherwise.
     */
    public static boolean isServerStarted(String server) throws Exception {
        String serverPath = null;
        String pePath = "Deployment Server";
        String deploymentPath = null;

        // Select the Server Navigator and set the JTreeOperator
        new QueueTool().waitEmpty(100);
        ServerNavigatorOperator explorer = ServerNavigatorOperator.showNavigatorOperator();
        explorer.makeComponentVisible();
        JTreeOperator tree = explorer.getTree();

        // Sleep 4 secs to make sure Server Navigator is in focus
        Util.wait(1000);

        serverPath = pePath;

        // Select the requested server node
        TreePath serverNode = tree.findPath(serverPath);
        explorer.getTree().selectPath(serverNode);
        explorer.getTree().expandPath(serverNode);
        TreePath serverSubNode = tree.findPath(serverPath);
        explorer.getTree().selectPath(serverSubNode);

        // open context menu on the requested server node
        new QueueTool().waitEmpty(100);

        explorer.pushPopup(tree, serverPath,
                Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle",
                "LBL_StartStopServer"));
        JDialogOperator serverStatusDialog = new JDialogOperator("Server Status");
        //  "Server Status - deployer:Sun:AppServer::localhost:14848");
        new JCheckBoxOperator(serverStatusDialog, 
                Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle",
                "LBL_Close_When_Finished")).changeSelection(true);
        final JButtonOperator buttonStart = new JButtonOperator(serverStatusDialog,
                "Start Server");
        final JButtonOperator buttonStop = new JButtonOperator(serverStatusDialog,
                "Stop Server");
        
        new Waiter(new Waitable() {
            public Object actionProduced(Object obj) {
                return (((buttonStart != null && buttonStart.isEnabled()) ||
                        (buttonStop != null && buttonStop.isEnabled())) ? this : null);
            }
            public String getDescription() {
                return "One of Start/Stop buttons to enable";
            }
        }).waitAction(null);

        boolean started = !((buttonStart != null) && (buttonStart.isEnabled()));
        
        serverStatusDialog.close();            
        serverStatusDialog.dispose();
        
        new QueueTool().waitEmpty(100);
        return started;
    }

    /** 
     * Starts an application server.
     * @param server a name of an application server (name of tree node)
     */
    public static void startServer(String server) throws Exception {
        // Select the Runtime and set the JTreeOperator
        new QueueTool().waitEmpty(100);
        ServerNavigatorOperator explorer = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(2000);
        JTreeOperator tree = explorer.getTree();
        TestUtils.wait(1000);
        
        // Select the requested server node
        tree.selectPath(tree.findPath(STR_SERVERS_PATH + server));
        TestUtils.wait(5000);
        // open context menu on the requested server node
        new QueueTool().waitEmpty(100);
        tree.callPopupOnPath(tree.findPath(STR_SERVERS_PATH + server));
        TestUtils.wait(500);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        if (!(new JMenuItemOperator(snpm, STR_MENU_ITEM_START).isEnabled()))
            tree.pushKey(KeyEvent.VK_ESCAPE);
        else {
            JLabelOperator jlo = new JLabelOperator(Util.getMainWindow(), 1);
            new JMenuItemOperator(snpm, STR_MENU_ITEM_START).push();
            while (jlo.getText() == null || !jlo.getText().equals(STR_SERVER_START + server)) {
                jlo = new JLabelOperator(Util.getMainWindow(), 1);
                System.out.println("label in first cycle = " + jlo.getText());
                TestUtils.wait(1000);
            }
            while (jlo.getText() != null && jlo.getText().equals(STR_SERVER_START + server)) {
                jlo = new JLabelOperator(Util.getMainWindow(), 1);
                TestUtils.wait(1000);
            }
            TestUtils.wait(1000);
        }
        new QueueTool().waitEmpty(100);
        TestUtils.wait(5000);
    }

    /**
     * Adds a deployment target.
     * @param DTDescriptor object DeploymentTargetDescriptor, describing
     * properties of deployment target
     */
    public static void addDeploymentTarget(DeploymentTargetDescriptor DTDescriptor) {
        Hashtable properties = (Hashtable) DTDescriptor.getProperties();

        String serverType = properties.get(DeploymentTargetDescriptor.SERVER_TYPE_KEY).toString();
        
        //log("Open Server manager");
        ActionNoBlock srv = new ActionNoBlock(STR_MENU_ITEM_SERVER_MANAGER, "", "");
        srv.performMenu();
        TestUtils.wait(500);
        
        //log("Find Server manager dialog");
        NbDialogOperator serverManager = new NbDialogOperator(STR_DIALOG_TITLE_SERVER_MANAGER);

        // Is server in list?
        // We want to use here public API
        JTreeOperator jto = new JTreeOperator(serverManager);
        Object[] treeNodes = jto.getChildren(jto.getRoot());
        int length = treeNodes.length;
        String addServerName = properties.get(DeploymentTargetDescriptor.NAME_KEY + serverType).toString();
        for (int i = 0; i < length; i++) {
            String currentServerName = treeNodes[i].toString();
            if (currentServerName.equals(addServerName)) {
                //log("press close for server manager");
                JButtonOperator close = new JButtonOperator(serverManager, STR_BUTTON_TITLE_CLOSE);
                close.pushNoBlock();
                TestUtils.wait(500);
                return;
            }
        }

        //log("Press add server button");
        JButtonOperator addServer = new JButtonOperator(serverManager, STR_BUTTON_TITLE_ADD_SERVER);
        addServer.pushNoBlock();
        TestUtils.wait(500);
        
        //log("locate server instance wizard");
        WizardOperator serverInstance = new WizardOperator(STR_WIZARD_TITLE_ADD_SERVER);
        
        //log("Chose Server");
        //JComboBoxOperator cbServerType = new JComboBoxOperator(serverInstance);
        //cbServerType.selectItem(serverType);
        JListOperator jlo = new JListOperator(serverInstance, 1);
        jlo.selectItem(serverType);
        TestUtils.wait(500);

        //log("Enter Name");
        JTextFieldOperator tfName = new JTextFieldOperator(serverInstance);
        tfName.setText(properties.get(DeploymentTargetDescriptor.NAME_KEY + serverType).toString());
        TestUtils.wait(500);

        //log("Go to next wizard page");
        serverInstance.next();
        TestUtils.wait(500);

        //log("Set location");
        String projectPath = properties.get(DeploymentTargetDescriptor.PATH_KEY + serverType).toString();
        JTextFieldOperator fileName;
        if (serverType.equals(STR_NAME_TOMCAT50) || serverType.equals(STR_NAME_TOMCAT55) || serverType.equals(STR_NAME_TOMCAT60))
            fileName = new JTextFieldOperator(serverInstance, 1);
        else
            fileName = new JTextFieldOperator(serverInstance, 0);
        fileName.setText(projectPath);
        
        TestUtils.wait(5000);
        
        if (serverType.equals(STR_NAME_WEBLOGIC)) addWebLogic(serverType, properties, serverInstance);
        if (serverType.equals(STR_NAME_JBOSS)) addJBoss(serverType, properties, serverInstance);
        if (serverType.equals(STR_NAME_APPSERVER) || serverType.equals(STR_NAME_GLASSFISH_V1) || serverType.equals(STR_NAME_GLASSFISH_V2)) addAppServer(serverType, properties, serverInstance);
        if (serverType.equals(STR_NAME_TOMCAT50) || serverType.equals(STR_NAME_TOMCAT55) || serverType.equals(STR_NAME_TOMCAT60)) addTomcat(serverType, properties, serverInstance);

        //log("finish wizard");
        serverInstance.finish();
        TestUtils.wait(500);

        //log("press close for server manager");
        JButtonOperator close = new JButtonOperator(serverManager, STR_BUTTON_TITLE_CLOSE);
        close.pushNoBlock();
        TestUtils.wait(5000);
    }
    
    private static void addWebLogic(String serverType, Hashtable properties, WizardOperator serverInstance) {
        //workaround
        serverInstance.pushKey(KeyEvent.VK_LEFT);
        
        //log("Go to next wizard page");
        serverInstance.next();
        TestUtils.wait(500);
        
        //log("Choose Instance");
        JComboBoxOperator cbInstance = new JComboBoxOperator(serverInstance);
        cbInstance.selectItem(properties.get(DeploymentTargetDescriptor.DOMAIN_KEY + serverType).toString());
        TestUtils.wait(500);

        //log("Enter Login");
        JTextFieldOperator tfLogin = new JTextFieldOperator(serverInstance, 3);
        tfLogin.setText(properties.get(DeploymentTargetDescriptor.LOGIN_KEY + serverType).toString());
        TestUtils.wait(500);
        
        //log("Enter Password");
        JTextFieldOperator tfPassword = new JTextFieldOperator(serverInstance, 4);
        tfPassword.setText(properties.get(DeploymentTargetDescriptor.PASSWORD_KEY + serverType).toString());
        TestUtils.wait(500);
    }
    
    private static void addJBoss(String serverType, Hashtable properties, WizardOperator serverInstance) {
        //log("Go to next wizard page");
        serverInstance.next();
        TestUtils.wait(500);
        //log("Wizard step = " + serverInstance.stepsGetSelectedIndex() + ": " + serverInstance.stepsGetSelectedValue());
        
        //log("Choose Instance");
        JComboBoxOperator cbInstance = new JComboBoxOperator(serverInstance);
        cbInstance.selectItem(properties.get(DeploymentTargetDescriptor.DOMAIN_KEY + serverType).toString());
        TestUtils.wait(500);
    }
    
    private static void addAppServer(String serverType, Hashtable properties, WizardOperator serverInstance) {
        //log("Choose Instance");
        JComboBoxOperator cbInstance = new JComboBoxOperator(serverInstance);
//        cbInstance.selectItem(properties.get(PROP_PREFIX_DOMAIN + serverType).toString());
        TestUtils.wait(500);

        //log("Go to next wizard page");
        serverInstance.next();
        TestUtils.wait(2000);

        //log("Enter Login");
        JTextFieldOperator tfLogin = new JTextFieldOperator(serverInstance, 0);
        tfLogin.setText(properties.get(DeploymentTargetDescriptor.LOGIN_KEY + serverType).toString());
        TestUtils.wait(500);
        
        //log("Enter Password");
        JTextFieldOperator tfPassword = new JTextFieldOperator(serverInstance, 1);
        tfPassword.setText(properties.get(DeploymentTargetDescriptor.PASSWORD_KEY + serverType).toString());
        TestUtils.wait(500);
    }
    
    private static void addTomcat(String serverType, Hashtable properties, WizardOperator serverInstance) {
        //log("Enter Login");
        JTextFieldOperator tfLogin = new JTextFieldOperator(serverInstance, 3);
        tfLogin.setText(properties.get(DeploymentTargetDescriptor.LOGIN_KEY + serverType).toString());
        TestUtils.wait(500);
        
        //log("Enter Password");
        JTextFieldOperator tfPassword = new JTextFieldOperator(serverInstance, 2);
        tfPassword.setText(properties.get(DeploymentTargetDescriptor.PASSWORD_KEY + serverType).toString());
        TestUtils.wait(500);
    }

    /** 
     * Adds an URL of Web Service
     * @param url URL of a Web Service WSDL file
     * @return name of added Web Service
     */
    public String addWebServiceUrl(String url) {
        new WebServicesNode(this.getTree(), "Web Services").addWebService();
        AddWebServiceOperator addWs = new AddWebServiceOperator();
        //if (addWs.isProxySet()) addWs.clearProxy();
        String wsName = addWs.addWebService(url);
        addWs.btAdd().pushNoBlock();
        TestUtils.wait(1000);
        //new JButtonOperator(new JDialogOperator("Configure web servcie methods"), "OK").doClick();
        return wsName;
    }

    /** 
     * Adds an URL of Web Service
     * @param url URL of a Web Service WSDL file
     * @param proxyHost a proxy server host
     * @param proxyPort a proxy server port
     * @return name of added Web Service
     */
    public String addWebServiceUrl(String url, String proxyHost, String proxyPort) {
        new WebServicesNode(this.getTree(), "Web Services").addWebService();
        AddWebServiceOperator addWs = new AddWebServiceOperator();
        addWs.setProxy(proxyHost, proxyPort);
        String wsName = addWs.addWebService(url);
        addWs.btAdd().pushNoBlock();
        TestUtils.wait(1000);
        //new JButtonOperator(new JDialogOperator("Configure web servcie methods"), "OK").doClick();
        return wsName;
    }

    /** 
     * Adds a Web Service by using a local file.
     * @param wsName a local WSDL file
     * @return name of added Web Service
     */
    public String addWebServiceLocal(String wsName) {
        new WebServicesNode(this.getTree(), "Web Services").addWebService();
        AddWebServiceOperator addWs = new AddWebServiceOperator();
        String wsDisplayName = addWs.addLocalWebService(wsName);
        addWs.btAdd().pushNoBlock();
        TestUtils.wait(1000);
        //new JButtonOperator(new JDialogOperator("Configure web servcie methods"), "OK").doClick();
        return wsDisplayName;

    }
}
