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

package org.netbeans.modules.groovy.grailsproject.actions;

import java.net.URL;
import java.util.concurrent.Callable;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.GrailsServerState;
import org.netbeans.modules.groovy.grailsproject.commands.GrailsCommandChooser;
import org.netbeans.modules.groovy.grailsproject.commands.GrailsCommandSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Petr Hejl
 */
public class GrailsCommandAction extends CallableSystemAction {

    @Override
    public void performAction() {
        final GrailsPlatform runtime = GrailsPlatform.getDefault();
        if (!runtime.isConfigured()) {
            ConfigurationSupport.showConfigurationWarning(runtime);
            return;
        }

        final GrailsProject project = GrailsProject.inferGrailsProject();
        if (project == null) {
            return;
        }

        GrailsCommandChooser.CommandDescriptor commandDescriptor = GrailsCommandChooser.select(project);
        if (commandDescriptor == null) {
            return;
        }

        ProjectInformation inf = project.getLookup().lookup(ProjectInformation.class);
        String displayName = inf.getDisplayName() + " (" + commandDescriptor.getGrailsCommand().getCommand() + ")"; // NOI18N


        final String[] params;
        // FIXME all parameters in one String should we split it ?
        if (commandDescriptor.getCommandParams() != null && !"".equals(commandDescriptor.getCommandParams().trim())) {
            params = new String[] {commandDescriptor.getCommandParams()};
        } else {
            params = new String[] {};
        }


        Callable<Process> callable;
        ExecutionDescriptor descriptor;

        final boolean debug = commandDescriptor.isDebug();

        if (GrailsPlatform.IDE_RUN_COMMAND.equals(commandDescriptor.getGrailsCommand().getCommand())) {
            final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
            Process process = null;
            if (serverState != null && serverState.isRunning()) {
                if (!debug /*|| debug == serverState.isDebug()*/) {
                    URL url = serverState.getRunningUrl();
                    if (url != null) {
                        GrailsCommandSupport.showURL(url, debug, project);
                    }
                    return;
                } else {
                    process = serverState.getProcess();
                    if (process != null) {
                        process.destroy();
                    }
                }
            }

            final Process oldProcess = process;
            callable = new Callable<Process>() {
                public Process call() throws Exception {
                    if (oldProcess != null) {
                        oldProcess.waitFor();
                    }
                    Callable<Process> inner = ExecutionSupport.getInstance().createRunApp(
                            GrailsProjectConfig.forProject(project), debug, params);
                    Process process = inner.call();
                    final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
                    if (serverState != null) {
                        serverState.setProcess(process);
                        serverState.setDebug(debug);
                    }
                    return process;
                }
            };

            descriptor = project.getCommandSupport().getRunDescriptor(debug);
        } else {
            callable = ExecutionSupport.getInstance().createSimpleCommand(
                    commandDescriptor.getGrailsCommand().getCommand(), debug,
                    GrailsProjectConfig.forProject(project), params);
            descriptor = project.getCommandSupport().getDescriptor(
                    commandDescriptor.getGrailsCommand().getCommand(), debug);
        }
        ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
        service.run();
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(GrailsCommandAction.class, "CTL_GrailsCommandAction");
    }

}
