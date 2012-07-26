/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * Skeleton of JFX Action Provider
 */
@ProjectServiceProvider(
    service=ActionProvider.class,
    projectTypes={@LookupProvider.Registration.ProjectType(id="org-netbeans-modules-java-j2seproject",position=90)})
public class JFXActionProvider implements ActionProvider {

    private final Project prj;

    private static final Map<String,String> ACTIONS = new HashMap<String,String>(){
        {
            put(COMMAND_RUN,"run"); //NOI18N
            put(COMMAND_DEBUG,"debug"); //NOI18N
            put(COMMAND_PROFILE,"profile"); //NOI18N
        }
    };

    public JFXActionProvider(@NonNull final Project project) {
        Parameters.notNull("project", project);
        this.prj = project;
    }

    @Override
    @NonNull
    public String[] getSupportedActions() {
        return ACTIONS.keySet().toArray(new String[ACTIONS.size()]);
    }

    @Override
    public void invokeAction(@NonNull String command, @NonNull Lookup context) throws IllegalArgumentException {
        if (command != null) {
            if(JFXProjectUtils.isFXPreloaderProject(prj)) {
                NotifyDescriptor d =
                    new NotifyDescriptor.Message(NbBundle.getMessage(JFXActionProvider.class,"WARN_PreloaderExecutionUnsupported", // NOI18N
                        ProjectUtils.getInformation(prj).getDisplayName()), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }
            FileObject buildFo = findBuildXml();
            assert buildFo != null && buildFo.isValid();
            String runAs = JFXProjectUtils.getFXProjectRunAs(prj);
            if(runAs == null) {
                runAs = JFXProjectProperties.RunAsType.STANDALONE.getString();
            }
            final ActionProgress listener = ActionProgress.start(context);
            try {
                String target;
                if(runAs.equalsIgnoreCase(JFXProjectProperties.RunAsType.STANDALONE.getString())) {
                    target = "jfxsa-".concat(command); //NOI18N
                } else {
                    if(runAs.equalsIgnoreCase(JFXProjectProperties.RunAsType.ASWEBSTART.getString())) {
                        target = "jfxws-".concat(command); //NOI18N
                    } else { //JFXProjectProperties.RunAsType.INBROWSER
                        target = "jfxbe-".concat(command); //NOI18N
                    }
                }
                
                Properties props = new Properties();
                collectStartupExtenderArgs(props, command, context);
                
                ActionUtils.runTarget(buildFo, new String[] {target}, props).addTaskListener(new TaskListener() {
                    @Override public void taskFinished(Task task) {
                        listener.finished(((ExecutorTask) task).result() == 0);
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                listener.finished(false);
            }
        } else {
            throw new IllegalArgumentException(command);
        }
    }

    @Override
    public boolean isActionEnabled(@NonNull String command, @NonNull Lookup context) throws IllegalArgumentException {
        if (!JFXProjectUtils.isFXProject(prj)) {
            return false;
        }
        if (findBuildXml() == null) {
            return false;
        }
        return findTarget(command) != null;
    }

    @NonNull
    private static String getBuildXmlName (@NonNull final PropertyEvaluator evaluator) {
        String buildScriptPath = evaluator.getProperty("buildfile");    //NOI18N
        if (buildScriptPath == null) {
            buildScriptPath = GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return buildScriptPath;
    }

    @CheckForNull
    private FileObject findBuildXml () {
        final J2SEPropertyEvaluator ep = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        assert ep != null;
        return prj.getProjectDirectory().getFileObject (getBuildXmlName(ep.evaluator()));
    }

    @CheckForNull
    private static String findTarget(@NonNull final String command) {
        return ACTIONS.get(command);
    }
    
    private void collectStartupExtenderArgs(Map<? super String,? super String> p, String command, Lookup context) {
        StringBuilder b = new StringBuilder();
        for (String arg : runJvmargsIde(command, context)) {
            b.append(' ').append(arg);
        }
        if (b.length() > 0) {
            p.put("run.jvmargs.ide", b.toString());
        }
    }
    
    private List<String> runJvmargsIde(String command, Lookup context) {
        StartupExtender.StartMode mode;
        if (command.equals(COMMAND_RUN) || command.equals(COMMAND_RUN_SINGLE)) {
            mode = StartupExtender.StartMode.NORMAL;
        } else if (command.equals(COMMAND_DEBUG) || command.equals(COMMAND_DEBUG_SINGLE) || command.equals(COMMAND_DEBUG_STEP_INTO)) {
            mode = StartupExtender.StartMode.DEBUG;
        } else if (command.equals(COMMAND_PROFILE) || command.equals(COMMAND_PROFILE_SINGLE)) {
            mode = StartupExtender.StartMode.PROFILE;
        } else if (command.equals(COMMAND_TEST) || command.equals(COMMAND_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_NORMAL;
        } else if (command.equals(COMMAND_DEBUG_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_DEBUG;
        } else if (command.equals(COMMAND_PROFILE_TEST_SINGLE)) {
            mode = StartupExtender.StartMode.TEST_PROFILE;
        } else {
            return Collections.emptyList();
        }
        List<String> args = new ArrayList<String>();

        for (StartupExtender group : StartupExtender.getExtenders(context, mode)) {
            args.addAll(group.getArguments());
        }
        return args;
    }
}
