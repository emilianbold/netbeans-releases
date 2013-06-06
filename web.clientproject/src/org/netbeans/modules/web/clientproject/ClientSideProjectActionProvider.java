/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject;

import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class ClientSideProjectActionProvider implements ActionProvider {

    private final ClientSideProject project;

    public ClientSideProjectActionProvider(ClientSideProject project) {
        this.project = project;
    }

    @Override
    public String[] getSupportedActions() {
        return new String[]{
                    COMMAND_RUN_SINGLE,
                    COMMAND_BUILD,
                    COMMAND_REBUILD,
                    COMMAND_CLEAN,
                    COMMAND_RUN,
                    COMMAND_RENAME,
                    COMMAND_MOVE,
                    COMMAND_COPY,
                    COMMAND_DELETE,
                    COMMAND_TEST,
                };
    }

    private ActionProvider getActionProvider() {
        ClientProjectEnhancedBrowserImplementation cfg = project.getEnhancedBrowserImpl();
        if (cfg != null) {
            return cfg.getActionProvider();
        } else {
            return null;
        }
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        LifecycleManager.getDefault().saveAll();
        // XXX sorry no idea how to do this correctly
        if (COMMAND_RENAME.equals(command)) {
            renameProject();
            return;
        }
        // XXX sorry no idea how to do this correctly
        if (COMMAND_MOVE.equals(command)) {
            moveProject();
            return;
        }
        // XXX sorry no idea how to do this correctly
        if (COMMAND_COPY.equals(command)) {
            copyProject();
            return;
        }
        // XXX sorry no idea how to do this correctly
        if (COMMAND_DELETE.equals(command)) {
            deleteProject();
            return;
        }
        ActionProvider ap = getActionProvider();
        if (ap != null) {
            // #217362 and possibly others
            if (!checkSiteRoot()) {
                return;
            }
            ap.invokeAction(command, context);
            return;
        }
        NotifyDescriptor desc = new NotifyDescriptor("Action not supported for this configuration", //NOI18N
                "Action not supported", //NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.INFORMATION_MESSAGE,
                new Object[]{NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(desc);
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        ActionProvider ap = getActionProvider();
        if (ap != null) {
            return ap.isActionEnabled(command, context);
        }
        return true;
    }

    private void renameProject() {
        DefaultProjectOperations.performDefaultRenameOperation(project, null);
    }

    private void moveProject() {
        DefaultProjectOperations.performDefaultMoveOperation(project);
    }

    private void copyProject() {
        DefaultProjectOperations.performDefaultCopyOperation(project);
    }

    private void deleteProject() {
        DefaultProjectOperations.performDefaultDeleteOperation(project);
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "ClientSideProjectActionProvider.error.invalidSiteRoot=<html>Project <b>{0}</b> has invalid Site Root, resolve project problems first."
    })
    private boolean checkSiteRoot() {
        if (project.getSiteRootFolder() == null) {
            // broken project, do not run any action
            NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                    Bundle.ClientSideProjectActionProvider_error_invalidSiteRoot(project.getName()), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(descriptor);
            ProjectProblems.showCustomizer(project);
            return false;
        }
        return true;
    }

}
