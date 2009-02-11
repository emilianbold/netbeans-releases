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

package org.netbeans.modules.groovy.grailsproject.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.GrailsServerState;
import org.netbeans.modules.groovy.grailsproject.actions.RefreshProjectRunnable;
import org.netbeans.modules.groovy.support.api.GroovySettings;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public final class GrailsCommandSupport {

    private static final ExecutionDescriptor GRAILS_DESCRIPTOR = new ExecutionDescriptor()
            .controllable(true).frontWindow(true).inputVisible(true)
                .showProgress(true).optionsPath(GroovySettings.GROOVY_OPTIONS_CATEGORY);

    private static final ExecutionDescriptor RUN_DESCRIPTOR = GRAILS_DESCRIPTOR.showSuspended(true);

    private static final InputProcessorFactory ANSI_STRIPPING = new AnsiStrippingInputProcessorFactory();

    private static final Logger LOGGER = Logger.getLogger(GrailsCommandSupport.class.getName());

    private static final Pattern COMMAND_PATTERN = Pattern.compile("grails\\s(.*)"); // NOI18N

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final GrailsProject project;

    private List<GrailsCommand> commands;

    public GrailsCommandSupport(GrailsProject project) {
        this.project = project;
    }

    public synchronized List<GrailsCommand> getGrailsCommands() {
        return commands;
    }

    public ExecutionDescriptor getRunDescriptor() {
        return getDescriptor("run-app"); // NOI18N
    }

    public ExecutionDescriptor getDescriptor(String command) {
        if ("run-app".equals(command) || "run-app-https".equals(command) || "run-war".equals(command)) { // NOI18N
            Runnable runnable = new Runnable() {
                public void run() {
                    final GrailsServerState serverState = project.getLookup().lookup(GrailsServerState.class);
                    if (serverState != null) {
                        serverState.setProcess(null);
                        serverState.setRunningUrl(null);
                    }
                }
            };
            return RUN_DESCRIPTOR.postExecution(runnable);
        } else if ("shell".equals(command)) { // NOI18N
            return RUN_DESCRIPTOR.postExecution(new RefreshProjectRunnable(project))
                    .outProcessorFactory(ANSI_STRIPPING).errProcessorFactory(ANSI_STRIPPING);
        }

        return GRAILS_DESCRIPTOR.postExecution(new RefreshProjectRunnable(project))
                .outProcessorFactory(ANSI_STRIPPING).errProcessorFactory(ANSI_STRIPPING);
    }

    public void refreshGrailsCommands() {
        Callable<Process> callable = ExecutionSupport.getInstance().createSimpleCommand("help", // NOI18N
                GrailsProjectConfig.forProject(project), new String[]{});
        final HelpLineProcessor lineProcessor = new HelpLineProcessor();

        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(InputOutput.NULL)
                .outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                // we are sure this will be invoked at most once
                return InputProcessors.bridge(lineProcessor);
            }
        });

        ExecutionService service = ExecutionService.newService(callable, descriptor, "help"); // NOI18N
        Future<Integer> task = service.run();
        try {
            if (task.get().intValue() == 0) {
                List<GrailsCommand> freshCommands = new ArrayList<GrailsCommand>();
                for (String command : lineProcessor.getCommands()) {
                    freshCommands.add(new GrailsCommand(command, null, command)); // NOI18N
                }

                synchronized (this) {
                    this.commands = freshCommands;
                }
                return;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

        synchronized (this) {
            this.commands = Collections.emptyList();
        }
    }

    public void refreshGrailsCommandsLater(final Runnable post) {
        EXECUTOR.submit(new Runnable() {

            public void run() {
                refreshGrailsCommands();
                if (post != null) {
                    post.run();
                }
            }
        });
    }

    private static class HelpLineProcessor implements LineProcessor {

        private List<String> commands = Collections.synchronizedList(new ArrayList<String>());

        public void processLine(String line) {
            Matcher matcher = COMMAND_PATTERN.matcher(line);
            if (matcher.matches()) {
                commands.add(matcher.group(1));
            }
        }

        public List<String> getCommands() {
            return commands;
        }

        public void close() {
        }

        public void reset() {
        }
    }

    private static class AnsiStrippingInputProcessorFactory implements InputProcessorFactory {

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.ansiStripping(defaultProcessor);
        }

    }
}
