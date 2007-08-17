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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.manager.actions;

import java.io.IOException;
import java.util.Properties;
import java.io.File;
import org.apache.tools.ant.module.api.support.ActionUtils;

import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.nodes.WebServiceGroupNode;
import org.netbeans.modules.websvc.manager.nodes.WebServicesRootNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.util.*;
import org.openide.modules.InstalledFileLocator;
import org.openide.execution.ExecutorTask;

// import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.websvc.manager.ui.AddWebServiceDlg;
import org.openide.filesystems.FileUtil;

public class AddWebServiceAction extends NodeAction {
    
    
    private static final String wsImportCompileScriptName = "ws_import_compile.xml";
    private static File wsImportCompileScript;
    private static final String USER_HOME_PROP = "user.home";
    private static final String USER_FILE_PROP = "user.properties.file";
    private static final String WSDL_NAME_PROP = "wsdlName";
    private static final String WSDL_FILE_NAME_PROP = "wsdlFileName";
    private static final String CLASSPATH_PROP = "wscompile.classpath";
    
    /*
    private static final String glassfishPath = "${glassfish.home}\\lib\\j2ee.jar:${glassfish.home}\\lib\\saaj-api.jar:${glassfish.home}\\lib\\saaj-impl.jar:${glassfish.home}\\lib\\jaxrpc-api.jar:\\${glassfish.home}\\lib\\jaxrpc-impl.jar:${glassfish.home}\\lib\\endorsed\\jaxp-api.jar:${glassfish.home}\\lib\\appserv-ws.jar:${glassfish.home}\\lib\\webservices-tools.jar:${glassfish.home}\\lib\\webservices-rt.jar";
     */
    //private static final String toolsPath = "${java.home}\\..\\lib\\tools.jar";
    
    private static String wsdlName;  // WSDL file name w/o extension
    // e.g. in TravelWS.wsdl the wsdlName
    // is TravelWS
    
    WebServiceListModel wsListModel = WebServiceListModel.getInstance();
    
    protected boolean enable(org.openide.nodes.Node[] node) {
        return true;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return new HelpCtx(AddWebServiceAction.class);
    }
    
    public String getName() {
        return NbBundle.getMessage(AddWebServiceAction.class, "ADD_WEB_SERVICE_Action");
    }
    
    protected void performAction(Node[] nodes) {
        Node invokingNode = nodes[0];
        String groupId = null;
        
        if (invokingNode instanceof FilterNode) {
            Node root = invokingNode.getCookie(WebServicesRootNode.class);
            Node group = invokingNode.getCookie(WebServiceGroupNode.class);
            if(root != null){
                WebServicesRootNode rootNode = (WebServicesRootNode)root;
                groupId = rootNode.getWebServiceGroup().getId();
            }else if(group != null){
                WebServiceGroupNode groupNode = (WebServiceGroupNode)group;
                groupId = groupNode.getWebServiceGroup().getId();
            }
        }else {
            if(invokingNode instanceof WebServicesRootNode){
                WebServicesRootNode rootNode = (WebServicesRootNode)invokingNode;
                groupId = rootNode.getWebServiceGroup().getId();
            }else if(invokingNode instanceof WebServiceGroupNode){
                WebServiceGroupNode groupNode = (WebServiceGroupNode)invokingNode;
                groupId = groupNode.getWebServiceGroup().getId();
            }
        }
        
        if (groupId != null) {
            AddWebServiceDlg dlg = new AddWebServiceDlg(groupId);
            dlg.displayDialog();
        }else {
            AddWebServiceDlg dlg = new AddWebServiceDlg();
            dlg.displayDialog();
        }
                
        return;
        
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
    
    protected String iconResource() {
        return "org/netbeans/modules/visualweb/websvcmgr/resources/webservice.png"; // NOI18N
    }
    
}
