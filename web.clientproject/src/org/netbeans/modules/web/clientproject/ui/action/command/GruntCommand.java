/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.ui.action.command;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.grunt.GruntfileExecutor;
import org.netbeans.modules.web.clientproject.ui.customizer.CustomizerProviderImpl;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class GruntCommand extends Command {

    private static final Logger LOGGER = Logger.getLogger(GruntCommand.class.getName());

    private final String commandId;


    public GruntCommand(ClientSideProject project, String commandId) {
        super(project);
        assert commandId != null;
        this.commandId = commandId;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    @Override
    boolean isActionEnabledInternal(Lookup context) {
        return true;
    }

    @Override
    void invokeActionInternal(Lookup context) {
        tryTask(true, false);
    }

    @NbBundle.Messages({
        "GruntCommand.notFound=No Gruntfile.js found; create it and rerun the action.",
        "GruntCommand.configure=Do you want to configure project actions to call Grunt tasks?",
    })
    public boolean tryTask(boolean showCustomizer, boolean waitFinished) {
        FileObject gruntFile = project.getProjectDirectory().getFileObject("Gruntfile.js"); // NOI18N
        if (gruntFile != null) {
            String gruntBuild = project.getEvaluator().getProperty("grunt.action." + commandId); // NOI18N
            if (gruntBuild != null) {
                try {
                    ExecutorTask execute = new GruntfileExecutor(gruntFile, gruntBuild.split(" ")).execute(); // NOI18N
                    if (waitFinished) {
                        execute.result();
                    }
                    return true;
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            } else if (showCustomizer) {
                NotifyDescriptor desc = new NotifyDescriptor.Confirmation(Bundle.GruntCommand_configure(), NotifyDescriptor.YES_NO_OPTION);
                Object option = DialogDisplayer.getDefault().notify(desc);
                if (option == NotifyDescriptor.YES_OPTION) {
                    project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer("grunt"); // NOI18N
                }
                return true;
            }
        } else if (showCustomizer) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.GruntCommand_notFound()));
        }
        return false;
    }

}
