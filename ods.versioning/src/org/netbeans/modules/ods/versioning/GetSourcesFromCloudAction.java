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
package org.netbeans.modules.ods.versioning;

import com.tasktop.c2c.server.scm.domain.ScmLocation;
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.PasswordAuthentication;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.netbeans.modules.ods.ui.api.CloudUiServer;
import org.netbeans.modules.team.ui.spi.SourceHandle;
import org.netbeans.modules.ods.versioning.GetSourcesFromCloudPanel.GetSourcesInfo;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.netbeans.modules.ods.versioning.SourceAccessorImpl.ProjectAndRepository;
import org.netbeans.modules.ods.versioning.spi.ApiProvider;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.ods.versioning.Bundle.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

public final class GetSourcesFromCloudAction extends AbstractAction {

    private ProjectAndRepository prjAndRepository;
    private SourceHandleImpl srcHandle;

    private String dialogTitle = NbBundle.getMessage(GetSourcesFromCloudAction.class, "GetSourcesFromCloudTitle");
    private String getOption = NbBundle.getMessage(GetSourcesFromCloudAction.class, "GetSourcesFromCloudAction.GetFromKenai.option");
    private String cancelOption = NbBundle.getMessage(GetSourcesFromCloudAction.class, "GetSourcesFromCloudAction.Cancel.option");

    public GetSourcesFromCloudAction(ProjectAndRepository prjRepo, SourceHandleImpl src) {
        prjAndRepository = prjRepo;
        this.srcHandle = src;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Object options[] = new Object[2];
        options[0] = getOption;
        options[1] = cancelOption;

        org.netbeans.modules.team.ui.spi.TeamUIUtils.activateTeamDashboard();

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
                final PasswordAuthentication passwdAuth = repository.getScmLocation() == ScmLocation.CODE2CLOUD
                        ? CloudUiServer.forServer(sourcesInfo.projectHandle.getTeamProject().getServer()).getPasswordAuthentication()
                        : null;
                // XXX   UIUtils.logKenaiUsage("KENAI_HG_CLONE"); // NOI18N
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        ApiProvider prov = getProvider(repository);
                        if (prov != null) {
                            File cloneDest = prov.getSources(repository.getUrl(), passwdAuth);
                            if (cloneDest != null && srcHandle != null) {
                                srcHandle.setWorkingDirectory(repository.getUrl(), cloneDest.getAbsolutePath());
                                srcHandle.refresh();
                            }
                        }
                    }
                });
            // XXX store the project in preferrences, it will be shown as first for next Get From Kenai
        }

    }

    @Messages("LBL_SelectProviderPanel.title=Select Repository Type")
    private ApiProvider getProvider (ScmRepository repository) {
        ApiProvider[] providers = SourceAccessorImpl.getProvidersFor(
                repository.getScmLocation() == ScmLocation.CODE2CLOUD
                ? repository.getType()
                : null);
        if (providers.length == 0) {
            return null;
        } else if (providers.length == 1) {
            return providers[0];
        } else {
            SelectProviderPanel panel = new SelectProviderPanel();
            panel.cmbProvider.setModel(new DefaultComboBoxModel(providers));
            panel.txtRepositoryUrl.setText(repository.getUrl());
            Preferences prefs = NbPreferences.forModule(GetSourcesFromCloudAction.class);
            String className = prefs.get("repository.scm.provider." + repository.getUrl(), null); //NOI18N
            if (className != null) {
                for (ApiProvider p : providers) {
                    if (className.equals(p.getClass().getName())) {
                        panel.cmbProvider.setSelectedItem(p);
                    }
                }
            }
            panel.cmbProvider.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if (value instanceof ApiProvider) {
                        value = ((ApiProvider) value).getName();
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
                
            });
            DialogDescriptor dd = new DialogDescriptor(panel,
                    LBL_SelectProviderPanel_title(),
                    true,
                    new Object[] { DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION },
                    DialogDescriptor.OK_OPTION,
                    DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx("org.netbeans.modules.ods.versioning.SelectProviderPanel"), //NOI18N
                    null);
            Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
            dlg.setVisible(true);
            ApiProvider p = null;
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                p = (ApiProvider) panel.cmbProvider.getSelectedItem();
                prefs.put("repository.scm.provider." + repository.getUrl(), p.getClass().getName()); //NOI18N
            }
            return p;
        }
    }
    
}
