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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.team.ods.git;

import com.tasktop.c2c.server.scm.domain.ScmRepository;
import com.tasktop.c2c.server.scm.domain.ScmType;
import java.awt.event.ActionEvent;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import javax.swing.AbstractAction;
import org.netbeans.modules.git.api.Git;
import org.netbeans.modules.team.ods.ui.api.CloudUiServer;
import org.netbeans.modules.team.ui.spi.SourceHandle;
import org.netbeans.modules.team.ods.git.GetSourcesFromCloudPanel.GetSourcesInfo;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.netbeans.modules.team.ods.git.SourceAccessorImpl.ProjectAndRepository;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

public final class GetSourcesFromCloudAction extends AbstractAction {

    private ProjectAndRepository prjAndRepository;
    private SourceHandleImpl srcHandle;

    private String dialogTitle = NbBundle.getMessage(GetSourcesFromCloudAction.class, "GetSourcesFromCloudTitle");
    private String getOption = NbBundle.getMessage(GetSourcesFromCloudAction.class, "GetSourcesFromCloudAction.GetFromKenai.option");
    private String cancelOption = NbBundle.getMessage(GetSourcesFromCloudAction.class, "GetSourcesFromCloudAction.Cancel.option");

    public GetSourcesFromCloudAction(ProjectAndRepository prjRepo, SourceHandle src) {
        prjAndRepository = prjRepo;
        this.srcHandle = (SourceHandleImpl) src;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        if (prjAndFeature!=null && KenaiService.Names.EXTERNAL_REPOSITORY.equals(prjAndFeature.feature.getService())) {
//            tryExternalCheckout(prjAndFeature.feature.getLocation());
//            return;
//        }
//
//        if (prjAndFeature!=null && KenaiService.Names.SUBVERSION.equals(prjAndFeature.feature.getService())) {
//            if (!Subversion.isClientAvailable(true)) {
//                return;
//            }
//        }

        Object options[] = new Object[2];
        options[0] = getOption;
        options[1] = cancelOption;

        org.netbeans.modules.team.ui.spi.UIUtils.activateTeamDashboard();

        GetSourcesFromCloudPanel getSourcesPanel = new GetSourcesFromCloudPanel(prjAndRepository);

        DialogDescriptor dialogDesc = new DialogDescriptor(getSourcesPanel, dialogTitle,
            true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);

        Object option = DialogDisplayer.getDefault().notify(dialogDesc);
        
        if (options[0].equals(option)) {
            
            final GetSourcesInfo sourcesInfo = getSourcesPanel.getSelectedSourcesInfo();
            
            if (sourcesInfo == null) {
                return;
            }

                final ScmRepository repository = sourcesInfo.repository;
                final PasswordAuthentication passwdAuth = CloudUiServer.forServer(sourcesInfo.projectHandle.getTeamProject().getServer()).getPasswordAuthentication();

                if (repository.getType() == ScmType.GIT) {
//                 XXX   UIUtils.logKenaiUsage("KENAI_HG_CLONE"); // NOI18N
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            try {
                                
                                if (passwdAuth != null) {
                                    Git.cloneRepository(repository.getUrl(), passwdAuth.getUserName(), passwdAuth.getPassword()); 
                                } else {
                                    Git.cloneRepository(repository.getUrl());
                                }
                                if (srcHandle != null) {
                                    srcHandle.refresh();
                                }
                            } catch (URISyntaxException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                } else {
                    throw new IllegalStateException("Wrong repository type " + repository.getType());
                }
            // XXX store the project in preferrences, it will be shown as first for next Get From Kenai
            // XXX store the project in preferrences, it will be shown as first for next Get From Kenai
            
        }

    }

    // XXX implement external
//    private void tryExternalCheckout(String url) {
//        try {
//            if (KenaiService.Names.SUBVERSION.equals(prjAndFeature.externalScmType)) {
//                Subversion.openCheckoutWizard(url);
//                return;
//            } else if (SourceHandleImpl.SCM_TYPE_CVS.equals(prjAndFeature.externalScmType)) {
//                CVS.openCheckoutWizard(url);
//                return;
//            } else if (KenaiService.Names.MERCURIAL.equals(prjAndFeature.externalScmType))  {
//                Mercurial.openCloneWizard(url);
//                return;
//            }
//        } catch (MalformedURLException malformedURLException) {
//            Logger.getLogger(GetSourcesFromCloudAction.class.getName()).log(Level.INFO, "Cannot checkout external repository " + url, malformedURLException);
//        } catch (IOException ex) {
//            if (Subversion.CLIENT_UNAVAILABLE_ERROR_MESSAGE.equals(ex.getMessage())) {
//                //this should not happen. It is handled in openCheckoutWizard
//                return;
//            }
//        }
//        JOptionPane.showMessageDialog(
//                WindowManager.getDefault().getMainWindow(),
//                NbBundle.getMessage(GetSourcesFromCloudAction.class, "MSG_ScmNotRecognized", url),
//                NbBundle.getMessage(GetSourcesFromCloudAction.class, "MSG_ScmNotRecognizedTitle"),
//                JOptionPane.INFORMATION_MESSAGE);
//    }
    
}
