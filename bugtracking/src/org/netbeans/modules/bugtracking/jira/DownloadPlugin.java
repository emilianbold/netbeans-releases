/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.jira;

import java.util.List;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
class DownloadPlugin {

    private static final String JIRA_CNB = "org.netbeans.modules.jira";         // NOI18N

    public DownloadPlugin() {
    }
    void startDownload() {
        final ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(DownloadPlugin.class, "MSG_LookingForJira"));
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                ph.start();
                final UpdateElement[] updateElement = new UpdateElement[1];
                try {
                    List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
                    
                    boolean installed = false;
                    
                    for (UpdateUnit u : units) {
                        if(u.getCodeName().equals(JIRA_CNB)) {       
                            List<UpdateElement> elements = u.getAvailableUpdates();
                            if(elements.isEmpty()) {
                                installed = true;
                            } else {
                                updateElement[0] = elements.get(0);
                            }
                            break;
                        }
                    }
                    if(installed) {
                        notifyError(NbBundle.getMessage(DownloadPlugin.class, "MSG_AlreadyInstalled"),  // NOI18N
                                    NbBundle.getMessage(DownloadPlugin.class, "LBL_Error"));            // NOI18N
                        return;
                    }
                } finally {
                    ph.finish();
                }
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        OperationContainer<InstallSupport> oc = OperationContainer.createForInstall();
                        oc.add(updateElement[0]);
                        PluginManager.openInstallWizard(oc);
                    }
                });
            }
        });
    }

    private static void notifyError (final String message, final String title) {
        notifyInDialog(message, title, NotifyDescriptor.ERROR_MESSAGE, true);
    }

    private static void notifyInDialog (final String message, final String title, int messageType, boolean cancelVisible) {
        NotifyDescriptor nd = new NotifyDescriptor(message, title, NotifyDescriptor.DEFAULT_OPTION, messageType,
                cancelVisible ? new Object[] {NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION} : new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notifyLater(nd);
    }

}
