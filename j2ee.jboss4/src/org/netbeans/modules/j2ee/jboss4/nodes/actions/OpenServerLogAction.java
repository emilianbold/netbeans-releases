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

package org.netbeans.modules.j2ee.jboss4.nodes.actions;

import java.io.File;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.JBLogWriter;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginProperties;
import org.netbeans.modules.j2ee.jboss4.nodes.JBManagerNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.InputOutput;

/**
 *
 * @author Libor Kotouc
 */
public class OpenServerLogAction extends NodeAction {
    
    public OpenServerLogAction() {
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    protected void performAction(Node[] activatedNodes) {
        for (Node activatedNode : activatedNodes) {
            Object node = activatedNode.getLookup().lookup(JBManagerNode.class);
            
            if (!(node instanceof JBManagerNode)) {
                continue;
            }
            
            JBDeploymentManager dm = ((JBManagerNode)node).getDeploymentManager();
            InputOutput io = UISupport.getServerIO(dm.getUrl());
            if (io != null) {
                io.select();
            }
            
            String instanceName = dm.getInstanceProperties().getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
            JBLogWriter logWriter = JBLogWriter.getInstance(instanceName);
            if (logWriter == null) {
                if (JBLogWriter.VERBOSE) {
                    System.out.println("CREATING LOG WRITER reading from the server.log file");                
                }
                logWriter = JBLogWriter.createInstance(io, instanceName);
                String serverDir = dm.getInstanceProperties().getProperty(JBPluginProperties.PROPERTY_SERVER_DIR);
                String logFileName = serverDir + File.separator + "log" + File.separator + "server.log" ; // NOI18N
                File logFile = new File(logFileName);
                if (logFile.exists()) {
                    logWriter.start(logFile);
                }
            }
            else {
                logWriter.refresh();
            }
        }        
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(OpenServerLogAction.class, "LBL_OpenServerLogAction");
    }

    public boolean asynchronous() {
        return false;
    }
    
}
