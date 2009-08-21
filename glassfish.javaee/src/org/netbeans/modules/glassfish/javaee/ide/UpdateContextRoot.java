/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.javaee.ide;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.ServerCommand.GetPropertyCommand;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author vkraemer
 */
public class UpdateContextRoot implements ProgressListener {

    private MonitorProgressObject returnProgress;
    private Hk2TargetModuleID moduleId;
    private ServerInstance si;
    private boolean needToDo;

    public UpdateContextRoot(MonitorProgressObject returnProgress, Hk2TargetModuleID moduleId,
            ServerInstance si, boolean needToDo) {
        this.returnProgress = returnProgress;
        this.moduleId = moduleId;
        this.si = si;
        this.needToDo = needToDo;
    }

    public void handleProgressEvent(ProgressEvent event) {
        if (event.getDeploymentStatus().isCompleted()) {
            if (needToDo) {
                returnProgress.operationStateChanged(OperationState.RUNNING, event.getDeploymentStatus().getMessage());
                // let's update the context-root
                //
                RequestProcessor.getDefault().post(new Runnable() {

                    public void run() {
                        // Maven projects like to embed a '.' into the ModuleID
                        //   that played havoc with the get command, so we started
                        //   to use a different get pattern,
                        GetPropertyCommand gpc = new GetPropertyCommand("applications.application.*.context-root");
                        Future<OperationState> result =
                                si.getBasicNode().getLookup().lookup(GlassfishModule.class).execute(gpc);
                        try {
                            if (result.get(60, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                                Map<String, String> retVal = gpc.getData();
                                String newCR = retVal.get("applications.application." + moduleId.getModuleID() + ".context-root");
                                if (null != newCR) {
                                    moduleId.setPath(newCR); //e.getValue());
                                    returnProgress.operationStateChanged(OperationState.COMPLETED, "updated the moduleid");
                                    return;
                                }
                                returnProgress.operationStateChanged(OperationState.FAILED, "failed updating the moduleid");
                            }
                        } catch (InterruptedException ex) {
                            returnProgress.operationStateChanged(OperationState.FAILED, "failed updating the moduleid");
                            Exceptions.printStackTrace(ex);
                        } catch (ExecutionException ex) {
                            returnProgress.operationStateChanged(OperationState.FAILED, "failed updating the moduleid");
                            Exceptions.printStackTrace(ex);
                        } catch (TimeoutException ex) {
                            returnProgress.operationStateChanged(OperationState.FAILED, "failed updating the moduleid");
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } else {
                returnProgress.operationStateChanged(OperationState.COMPLETED, event.getDeploymentStatus().getMessage());
            }
        }else if (event.getDeploymentStatus().isFailed()) {
            returnProgress.operationStateChanged(OperationState.FAILED, event.getDeploymentStatus().getMessage());
        } else {
            returnProgress.operationStateChanged(OperationState.RUNNING, event.getDeploymentStatus().getMessage());
        }
    }
}
