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
package org.netbeans.modules.kenai.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.ui.GetSourcesFromKenaiPanel.GetSourcesInfo;
import org.netbeans.modules.kenai.ui.SourceAccessorImpl.ProjectAndFeature;
import org.netbeans.modules.kenai.ui.spi.SourceHandle;
import org.netbeans.modules.mercurial.api.Mercurial;
import org.netbeans.modules.subversion.api.Subversion;
import org.netbeans.modules.versioning.system.cvss.api.CVS;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

public final class GetSourcesFromKenaiAction extends AbstractAction {

    private ProjectAndFeature prjAndFeature;
    private SourceHandleImpl srcHandle;

    private String dialogTitle = NbBundle.getMessage(GetSourcesFromKenaiAction.class, "GetSourcesFromKenaiTitle");
    private String getOption = NbBundle.getMessage(GetSourcesFromKenaiAction.class, "GetSourcesFromKenaiAction.GetFromKenai.option");
    private String cancelOption = NbBundle.getMessage(GetSourcesFromKenaiAction.class, "GetSourcesFromKenaiAction.Cancel.option");

    public GetSourcesFromKenaiAction(ProjectAndFeature prjFtr, SourceHandle src) {
        prjAndFeature = prjFtr;
        this.srcHandle = (SourceHandleImpl) src;
    }

    public GetSourcesFromKenaiAction() {
        this(null, null);
    }

    public void actionPerformed(ActionEvent e) {
        if (prjAndFeature!=null && KenaiService.Names.EXTERNAL_REPOSITORY.equals(prjAndFeature.feature.getService())) {
            tryExternalCheckout(prjAndFeature.feature.getLocation());
            return;
        }

        if (prjAndFeature!=null && KenaiService.Names.SUBVERSION.equals(prjAndFeature.feature.getService())) {
            if (!Subversion.isClientAvailable(true)) {
                return;
            }
        }

        Object options[] = new Object[2];
        options[0] = getOption;
        options[1] = cancelOption;

        KenaiTopComponent.findInstance().open();
        KenaiTopComponent.findInstance().requestActive();

        GetSourcesFromKenaiPanel getSourcesPanel = new GetSourcesFromKenaiPanel(prjAndFeature);

        DialogDescriptor dialogDesc = new DialogDescriptor(getSourcesPanel, dialogTitle,
            true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);

        Object option = DialogDisplayer.getDefault().notify(dialogDesc);
        
        if (options[0].equals(option)) {
            
            final PasswordAuthentication passwdAuth = Kenai.getDefault().getPasswordAuthentication();
            final GetSourcesInfo sourcesInfo = getSourcesPanel.getSelectedSourcesInfo();
            if (sourcesInfo == null) {
                return;
            }

            final KenaiFeature feature = sourcesInfo.feature;

            if (KenaiService.Names.SUBVERSION.equals(feature.getService())) {
                if (Subversion.isClientAvailable(true)) {
                    RequestProcessor.getDefault().post(new Runnable() {

                        public void run() {
                            try {

                                if (passwdAuth != null) {
                                    Subversion.checkoutRepositoryFolder(feature.getLocation(), sourcesInfo.relativePaths,
                                            new File(sourcesInfo.localFolderPath), passwdAuth.getUserName(), new String(passwdAuth.getPassword()), true);
                                } else {
                                    Subversion.checkoutRepositoryFolder(feature.getLocation(), sourcesInfo.relativePaths,
                                            new File(sourcesInfo.localFolderPath), true);
                                }
                                if (srcHandle!=null) {
                                    srcHandle.refresh();
                                }

                            } catch (MalformedURLException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (IOException ex) {
                                if (Subversion.CLIENT_UNAVAILABLE_ERROR_MESSAGE.equals(ex.getMessage())) {
                                    // DO SOMETHING, svn client is unavailable
                                }
                            }
                        }
                    });
                }
            } else if (KenaiService.Names.MERCURIAL.equals(feature.getService())) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {

                            if (passwdAuth != null) {
                                Mercurial.cloneRepository(feature.getLocation(), new File(sourcesInfo.localFolderPath),
                                    "", "", "", passwdAuth.getUserName(), new String(passwdAuth.getPassword())); // NOI18N
                            } else {
                                Mercurial.cloneRepository(feature.getLocation(), new File(sourcesInfo.localFolderPath),
                                    "", "", ""); // NOI18N
                            }
                            if (srcHandle != null) {
                                srcHandle.refresh();
                            }
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
            // XXX store the project in preferrences, it will be shown as first for next Get From Kenai
        }

    }

    private void tryExternalCheckout(String url) {
        try {
            if (KenaiService.Names.SUBVERSION.equals(prjAndFeature.externalScmType)) {
                Subversion.openCheckoutWizard(url);
                return;
            } else if (SourceHandleImpl.SCM_TYPE_CVS.equals(prjAndFeature.externalScmType)) {
                CVS.openCheckoutWizard(url);
                return;
            } else if (KenaiService.Names.MERCURIAL.equals(prjAndFeature.externalScmType))  {
                Mercurial.openCloneWizard(url);
                return;
            }
        } catch (MalformedURLException malformedURLException) {
            Logger.getLogger(GetSourcesFromKenaiAction.class.getName()).log(Level.INFO, "Cannot checkout external repository " + url, malformedURLException);
        } catch (IOException ex) {
            if (Subversion.CLIENT_UNAVAILABLE_ERROR_MESSAGE.equals(ex.getMessage())) {
                //this should not happen. It is handled in openCheckoutWizard
                return;
            }
        }
        JOptionPane.showMessageDialog(
                WindowManager.getDefault().getMainWindow(),
                NbBundle.getMessage(GetSourcesFromKenaiAction.class, "MSG_ScmNotRecognized", url),
                NbBundle.getMessage(GetSourcesFromKenaiAction.class, "MSG_ScmNotRecognizedTitle"),
                JOptionPane.INFORMATION_MESSAGE);
    }
    
}
