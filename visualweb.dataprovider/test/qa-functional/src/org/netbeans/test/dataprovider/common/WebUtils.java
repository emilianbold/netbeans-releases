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

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.text.*;
import java.util.regex.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.jemmy.*;
import com.meterware.httpunit.*;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
        
public class WebUtils implements Constants {
    public static void runProject(String prjName) {
        Utils.callPopupMenuOnProjectsTreeNode(prjName, POPUP_MENU_ITEM_RUN);
        if (isDeploymentSuccessful(prjName)) {
            Utils.logMsg("+++ Project [" + prjName + "] has been deployed successfully");
        } else {
            throw new RuntimeException("Project [" + prjName + "] wasn't deployed successfully");
        }
    }
    
    private static boolean isDeploymentSuccessful(String prjName) {
        String serverType = TestPropertiesHandler.getServerProperty("Application_Server_Type");
        if (Pattern.matches("Sun" + PATTERN_ANY_CHARS + "Application Server", serverType)) {
            return (isDeploymentSuccessful_SunAppServer(prjName));
        }
        throw new RuntimeException("Method isn't defined for an application server type [" + 
            serverType + "]");
    }

    private static boolean isDeploymentSuccessful_SunAppServer(String prjName) {
        TreePath treePath = null;
        for (int i = 0; i < 30; ++i) {
            try {
                Utils.putFocusOnWindowServices();
                Utils.callPopupMenuOnServicesTreeNode(getWebApplicationsTreeNodeName_SunAppServer(),
                    POPUP_MENU_ITEM_REFRESH);
                //treePath = Utils.findServicesTreeNode(getDeployedApplicationTreeNodeName_SunAppServer(prjName), 
                //    2000, true);
                String appServerName = TestPropertiesHandler.getServerProperty("Application_Server_Name");
                treePath = findDeployedProjectNode(new String[] {
                    appServerName, SERVICES_TREE_NODE_APPLICATIONS, SERVICES_TREE_NODE_WEB_APPLICATIONS}, 
                    prjName);
                if (treePath != null) {
                    Util.wait(1500);
                    new QueueTool().waitEmpty();
                    return true;
                }
            } catch(Exception e) {
                e.printStackTrace(Utils.logStream);
            }
        }
        return false;
    }    

    public static void undeployProject(String prjName) {
        String serverType = TestPropertiesHandler.getServerProperty("Application_Server_Type");
        if (Pattern.matches("Sun" + PATTERN_ANY_CHARS + "Application Server", serverType)) {
            undeployProject_SunAppServer(prjName);
            return;
        }
        throw new RuntimeException("Method isn't defined for an application server type of [" + 
            serverType + "]");
    }
    
    private static void undeployProject_SunAppServer(String prjName) {
        TreePath treePath = null;
        String deployedAppTreeNodeName = getDeployedApplicationTreeNodeName_SunAppServer(prjName);
        boolean isUndeployActionPerformed = false;
        for (int i = 0; i < 30; ++i) {
            try {
                Utils.putFocusOnWindowServices();
                Utils.callPopupMenuOnServicesTreeNode(getWebApplicationsTreeNodeName_SunAppServer(),
                    POPUP_MENU_ITEM_REFRESH);
                
                String appServerName = TestPropertiesHandler.getServerProperty("Application_Server_Name");
                treePath = findDeployedProjectNode(new String[] {
                    appServerName, SERVICES_TREE_NODE_APPLICATIONS, SERVICES_TREE_NODE_WEB_APPLICATIONS}, 
                    prjName);
                if (treePath == null) {
                    if (isUndeployActionPerformed) {
                        Utils.logMsg("+++ Project [" + prjName + "] has been undeployed successfully");
                    }
                    return;
                } else {
                    if (! isUndeployActionPerformed) {
                        Utils.callPopupMenuOnServicesTreeNode(deployedAppTreeNodeName, POPUP_MENU_ITEM_UNDEPLOY);
                        Util.wait(2000);
                        new QueueTool().waitEmpty();
                        isUndeployActionPerformed = true;
                    }
                }
            } catch(Exception e) {
                e.printStackTrace(Utils.logStream);
            }
        }
        throw new RuntimeException("Problem with undeployment of project [" + prjName + "]");
    }
    
    private static TreePath findDeployedProjectNode(String[] parentNodeNames, String prjName) {
        //String deployedAppTreeNodeName = getDeployedApplicationTreeNodeName_SunAppServer(prjName);
        //treePath = Utils.findServicesTreeNode(deployedAppTreeNodeName, 2000, false);
        JTree jTree = (JTree) new ServerNavigatorOperator().getTree().getSource();
        int startTreeRow = 0;
        TreePath treePath = null;
        for (String parentNodeName : parentNodeNames) {
            treePath = expandTreePath(jTree, parentNodeName, startTreeRow);
            startTreeRow = jTree.getRowForPath(treePath);
        }
        TreeModel treeModel = jTree.getModel();
        int deployedProjectCount = treeModel.getChildCount(treePath.getLastPathComponent());
        for (int i = 0; i < deployedProjectCount; i++) {
            Object nodeChild = treeModel.getChild(treePath.getLastPathComponent(), i);
            TreePath deployedProjectTreePath = treePath.pathByAddingChild(nodeChild);
            Utils.logMsg("+++ Deployed project treePath = " + deployedProjectTreePath.toString());        

            Util.wait(800);
            new QueueTool().waitEmpty();
            if (isTreeNodeLabelEqualsPrjName(deployedProjectTreePath, prjName)) {
                Utils.logMsg("+++ TreePath " + deployedProjectTreePath.toString() + 
                    " contains the project name [" + prjName + "]");        
                return treePath;
            }
        }
        Utils.logMsg("+++ Deployed project with name [" + prjName + "] is not found");        
        return null;
    }
    
    private static TreePath expandTreePath(JTree jTree, String treeNodeLabel, int startTreeRow) {
        TreePath treePath = jTree.getNextMatch(treeNodeLabel, startTreeRow, Position.Bias.Forward);
        Util.wait(800);
        new QueueTool().waitEmpty();

        jTree.expandPath(treePath);
        Util.wait(800);
        new QueueTool().waitEmpty();
        
        Utils.logMsg("+++ Expanded treePath = " + treePath.toString());        
        return treePath;
    }
    
    private static boolean isTreeNodeLabelEqualsPrjName(TreePath treePath, String prjName) {
        // after prjName shouldn't be a word character [a-zA-Z_0-9], spaces, -
        boolean result = Pattern.matches(PATTERN_ANY_CHARS + prjName + "[^\\w\\s\\-]?", 
            treePath.toString());
        //Utils.logMsg("+++ TreePath " + treePath.toString() + 
        //    " contains the project name [" + prjName + "]: " + result);        
        return result;
    }
    
    public static WebResponse getWebResponseAfterDeployment(String webAppURL) throws Throwable {
        //HttpUnitOptions.setExceptionsThrownOnScriptError(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setScriptingEnabled(false);

        WebConversation conversation = new WebConversation();
        WebResponse response = null;
        for (int i = 0; i < 8; ++i) {
            try {
                response = conversation.getResponse(webAppURL);
                Util.wait(2000);
            } catch(HttpNotFoundException e) {
                if (i == 8) {
                    throw new RuntimeException(e);
                }
            }
        }
        return response;
    }
            
    public static boolean isWebResponseOK(WebResponse response) {
        if ((response != null) && (response.getResponseCode() == WEB_RESPONSE_CODE_OK)) {
            return true;
        }
        return false;
    }

    private static String getWebApplicationsTreeNodeName_SunAppServer() {
        String serverName = TestPropertiesHandler.getServerProperty("Application_Server_Name");
        return (SERVICES_TREE_NODE_SERVERS + "|" + serverName + "|" + 
                SERVICES_TREE_NODE_APPLICATIONS + "|" + 
                SERVICES_TREE_NODE_WEB_APPLICATIONS);
    }
    
    private static String getDeployedApplicationTreeNodeName_SunAppServer(String prjName) {
        return (getWebApplicationsTreeNodeName_SunAppServer() + "|" + prjName);
    }
}
