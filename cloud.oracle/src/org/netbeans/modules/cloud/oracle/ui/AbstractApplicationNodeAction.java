/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cloud.oracle.ui;

import oracle.cloud.paas.exception.ResourceBusyException;
import oracle.cloud.paas.exception.UnknownResourceException;
import oracle.cloud.paas.model.Application;
import oracle.cloud.paas.model.ApplicationState;
import oracle.cloud.paas.model.Job;
import org.netbeans.modules.cloud.oracle.OracleInstance;
import org.netbeans.modules.cloud.oracle.serverplugin.OracleJ2EEInstance;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 *
 */
public abstract class AbstractApplicationNodeAction extends NodeAction {

    private static final RequestProcessor RP = new RequestProcessor("cloud application node action", 1);
    
    @NbBundle.Messages({"MSG_WrongState=Application is not in state to perform this action.",
        "MSG_WasRemoved=Application does not exist anymore.",
        "MSG_Busy=Action cannot be performed at this moment. Try few minutes later."})
    @Override
    protected void performAction(Node[] activatedNodes) {
        final OracleJ2EEInstance inst = activatedNodes[0].getLookup().lookup(OracleJ2EEInstance.class);
        final OracleJ2EEInstanceNode.ApplicationNode appNode = activatedNodes[0].getLookup().lookup(OracleJ2EEInstanceNode.ApplicationNode.class);
        Application app = appNode.getApp();
        
        // check latest status of app
        try {
            app = inst.getOracleInstance().refreshApplication(app);
        } catch (UnknownResourceException ex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.MSG_WasRemoved()));
            appNode.refreshChildren();
            return;
        }
        if (!isAppInRightState(app)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.MSG_WrongState()));
            appNode.setApp(app);
            return;
        }
        final Job job;
        try {
            job = performActionImpl(inst, app);
        } catch (ResourceBusyException ex) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.MSG_Busy()));
            appNode.setApp(app);
            return;
        }
        app = inst.getOracleInstance().refreshApplication(app);
        appNode.setApp(app);
        final Application app2 = app;
        RP.post(new Runnable() {
            @Override
            public void run() {
                OracleInstance.waitForJobToFinish(inst.getOracleInstance().getApplicationManager(), job);
                try {
                    Application app3 = inst.getOracleInstance().refreshApplication(app2);
                    appNode.setApp(app3);
                } catch (UnknownResourceException ex) {
                    appNode.refreshChildren();
                    return;
                }
            }
        });
    }
    
    abstract protected Job performActionImpl(OracleJ2EEInstance inst, Application app);

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        if (activatedNodes[0].getLookup().lookup(OracleJ2EEInstance.class) == null) {
            return false;
        }
        OracleJ2EEInstanceNode.ApplicationNode appNode = activatedNodes[0].getLookup().lookup(OracleJ2EEInstanceNode.ApplicationNode.class);
        if (appNode == null) {
            return false;
        }
        return isAppInRightState(appNode.getApp());
    }
    
    abstract protected boolean isAppInRightState(Application app);

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }
    
}
