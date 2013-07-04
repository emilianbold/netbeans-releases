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
package org.netbeans.modules.odcs.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Set;
import javax.swing.JButton;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.odcs.ui.Bundle.*;
import org.netbeans.modules.odcs.ui.NewProjectWizardIterator.CreatedProjectInfo;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.openide.DialogDescriptor;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

@ActionID(id = "org.netbeans.modules.odcs.ui.NewProjectAction", category = "Team")
@ActionRegistration(displayName = "#CTL_NewProjectAction.name")
@ActionReference(path = "Menu/Versioning/Team/ODCS", position = 200)
@Messages({"CTL_NewProjectAction.name=New Team Project...",
    "NewProjectAction.dialogTitle=New Team Project"
})
public final class NewProjectAction implements ActionListener {

    private ODCSServer server;

    public NewProjectAction(ODCSServer server) {
        this.server = server;
    }

    public NewProjectAction() {
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        createProject(new File[0]);
    }
    
    public void createProject(File[] initialDirs) {

        if (server == null) {
            if ((server = Utilities.getActiveServer(true)) == null) {
                return;
            }
        }

        WizardDescriptor wizardDescriptor = new WizardDescriptor(new NewProjectWizardIterator(initialDirs, server));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(NewProjectAction_dialogTitle());

        DialogDisplayer.getDefault().notify(wizardDescriptor);
        
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            Set<CreatedProjectInfo> createdProjects = wizardDescriptor.getInstantiatedObjects();
            // everything should be created, show summary
            showLandingPage(createdProjects);
        }

    }

    @Messages({"# {0} - server instance name", "NewProjectAction.goToKenai=Go to {0}",
        "NewProjectAction.createNewProject=Create New Project...",
        "NewProjectAction.close=Close"})
    private void showLandingPage(Set<CreatedProjectInfo> projects) {

        CreatedProjectInfo cpi = projects.iterator().next();
        ODCSProject prj = cpi.project;
        String localPath = cpi.localRepoPath;

        Object options[] = new Object[3];
        options[0] = new JButton(NewProjectAction_goToKenai(prj.getServer().getDisplayName()));
        options[1] = new JButton(NewProjectAction_createNewProject());
        options[2] = new JButton(NewProjectAction_close());


        DialogDescriptor dialogDesc = new DialogDescriptor(new LandingPagePanel(prj.getName(), localPath, prj.getServer().getDisplayName()),
                NewProjectAction_dialogTitle(),
                true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);

        Object option = DialogDisplayer.getDefault().notify(dialogDesc);
        
        if (options[0].equals(option)) { // open project page
            try {
                URL projectUrl = new URL(prj.getWebUrl());
                URLDisplayer.getDefault().showURL(projectUrl);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (options[1].equals(option)) { // create NB project
            File projPath = localPath != null ? new File(localPath) : null;
            if (projPath != null && projPath.exists()) {
                Lookup.getDefault().lookup(ProjectServices.class).createNewProject(projPath);
            }
        }
    }

}
