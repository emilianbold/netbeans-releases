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
package org.netbeans.modules.dlight.core.stack.ui;

import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.dataprovider.SourceFileInfoDataProvider;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.spi.SourceSupportProvider;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
class GoToSourceAction extends AbstractAction {

    private final FunctionCall functionCall;
    private final Future<SourceFileInfo> sourceFileInfoTask;
    private boolean isEnabled = true;
    private boolean gotTheInfo = false;
    private final GoToSourceActionReadnessListener listener;

     GoToSourceAction(final SourceFileInfoDataProvider dataProvider, FunctionCall funcCall, GoToSourceActionReadnessListener listener) {
        super(NbBundle.getMessage(GoToSourceAction.class, "GoToSourceActionName"));//NOI18N
        this.functionCall = funcCall;
        this.listener = listener;
        sourceFileInfoTask = DLightExecutorService.submit(new Callable<SourceFileInfo>() {

            public SourceFileInfo call() {
                if (dataProvider == null){
                    return null;
                }
                return dataProvider.getSourceFileInfo(functionCall);
            }
        }, "SourceFileInfo getting info from Call Stack UI"); // NOI18N
        waitForSourceFileInfo();
    }

    private void waitForSourceFileInfo() {
        DLightExecutorService.submit(new Runnable() {

            public void run() {
                try {
                    SourceFileInfo sourceFileInfo = sourceFileInfoTask.get();
                    isEnabled = sourceFileInfo != null && sourceFileInfo.isSourceKnown();
                } catch (InterruptedException ex) {
                    isEnabled = false;
                } catch (ExecutionException ex) {
                    isEnabled = false;
                } finally {
                    synchronized (GoToSourceAction.this) {
                        gotTheInfo = true;
                    }
                    setEnabled(isEnabled);
                    listener.ready();

                }

            }
        }, "Wait For the SourceFileInfo in Call Stack UI");//NOI18N
        }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public void actionPerformed(ActionEvent e) {
        DLightExecutorService.submit(new Runnable() {

            public void run() {
                SourceFileInfo sourceFileInfo = null;
                try {
                    sourceFileInfo = sourceFileInfoTask.get();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (sourceFileInfo == null) {// TODO: what should I do here if there is no source file info
                    return;
                }

                SourceSupportProvider sourceSupportProvider = Lookup.getDefault().lookup(SourceSupportProvider.class);
                sourceSupportProvider.showSource(sourceFileInfo);
            }
        }, "GoToSource from Call Stack UI"); // NOI18N
        }

    interface GoToSourceActionReadnessListener{
        void ready();
    }
}
