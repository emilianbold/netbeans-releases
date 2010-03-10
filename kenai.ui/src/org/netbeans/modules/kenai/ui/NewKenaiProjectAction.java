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
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiManager;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.NewKenaiProjectWizardIterator.CreatedProjectInfo;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public final class NewKenaiProjectAction implements ActionListener {

    private Kenai kenai;

    public NewKenaiProjectAction(Kenai kenai) {
        this.kenai = kenai;
    }

    public NewKenaiProjectAction() {
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        WizardDescriptor wizardDescriptor = new WizardDescriptor(new NewKenaiProjectWizardIterator(new Node[0], kenai));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(NbBundle.getMessage(NewKenaiProjectAction.class,
                "NewKenaiProjectAction.dialogTitle")); // NOI18N

        DialogDisplayer.getDefault().notify(wizardDescriptor);
        
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            Set<CreatedProjectInfo> createdProjects = wizardDescriptor.getInstantiatedObjects();
            // everything should be created, show summary
            showLandingPage(createdProjects);
        }

    }

    private void showLandingPage(Set<CreatedProjectInfo> projects) {

        CreatedProjectInfo cpi = projects.iterator().next();
        KenaiProject kenaiPrj = cpi.project;
        String localPath = cpi.localRepoPath;

        Object options[] = new Object[3];
        options[0] = new JButton(NbBundle.getMessage(NewKenaiProjectAction.class, "NewKenaiProjectAction.goToKenai", kenaiPrj.getKenai()));
        options[1] = new JButton(NbBundle.getMessage(NewKenaiProjectAction.class, "NewKenaiProjectAction.createNewProject"));
        options[2] = new JButton(NbBundle.getMessage(NewKenaiProjectAction.class, "NewKenaiProjectAction.close"));


        DialogDescriptor dialogDesc = new DialogDescriptor(new LandingPagePanel(kenaiPrj.getName(), localPath, kenaiPrj.getKenai().getName()),
                NbBundle.getMessage(NewKenaiProjectAction.class, "NewKenaiProjectAction.dialogTitle"),
                true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);

        Object option = DialogDisplayer.getDefault().notify(dialogDesc);
        
        if (options[0].equals(option)) { // open Kenai project page
            URL projectUrl = kenaiPrj.getWebLocation();
            URLDisplayer.getDefault().showURL(projectUrl);
        } else if (options[1].equals(option)) { // create NB project
            Action newProjectAction = CommonProjectActions.newProjectAction();
            File projPath = null;
            if (localPath != null) {
                projPath = new File(localPath);
            }
            if (newProjectAction != null && projPath != null && projPath.exists()) {
                ProjectChooser.setProjectsFolder(projPath);
                newProjectAction.actionPerformed(new ActionEvent(NewKenaiProjectAction.class,
                        ActionEvent.ACTION_PERFORMED, "command")); // NOI18N
            }
        }

    }

    }
