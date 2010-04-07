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

package org.netbeans.modules.php.zend.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpProgram.InvalidPhpProgramException;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.spi.commands.FrameworkCommand;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.zend.ZendScript;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public class ZendCommandSupport extends FrameworkCommandSupport {
    static final Logger LOGGER = Logger.getLogger(ZendCommandSupport.class.getName());

    private static final String SEPARATOR = ":NB:"; // NOI18N

    private static final String COMMANDS_PROVIDER_REL_PATH = "zend/NetBeansCommandsProvider.php"; // NOI18N
    private static final File COMMANDS_PROVIDER;

    static {
        File commandsProvider = null;
        try {
            commandsProvider = FileUtil.normalizeFile(
                    InstalledFileLocator.getDefault().locate(COMMANDS_PROVIDER_REL_PATH, "org.netbeans.modules.php.zend", false).getCanonicalFile()); // NOI18N
            if (commandsProvider == null || !commandsProvider.isFile()) {
                throw new IllegalStateException("Could not locate file " + COMMANDS_PROVIDER_REL_PATH);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not locate file " + COMMANDS_PROVIDER_REL_PATH, ex);
        }
        COMMANDS_PROVIDER = commandsProvider;
    }

    public ZendCommandSupport(PhpModule phpModule) {
        super(phpModule);
    }

    @Override
    public String getFrameworkName() {
        return NbBundle.getMessage(ZendCommandSupport.class, "MSG_Zend");
    }

    @Override
    public void runCommand(CommandDescriptor commandDescriptor) {
        Callable<Process> callable = createCommand(commandDescriptor.getFrameworkCommand().getCommands(), commandDescriptor.getCommandParams());
        ExecutionDescriptor descriptor = getDescriptor();
        String displayName = getOutputTitle(commandDescriptor);
        ExecutionService service = ExecutionService.newService(callable, descriptor, displayName);
        service.run();
    }

    @Override
    protected String getOptionsPath() {
        return ZendScript.getOptionsPath();
    }

    @Override
    protected File getPluginsDirectory() {
        return null;
    }

    @Override
    protected ExternalProcessBuilder getProcessBuilder(boolean warnUser) {
        ZendScript zendScript = null;
        try {
            zendScript = ZendScript.getDefault();
        } catch (InvalidPhpProgramException ex) {
            if (warnUser) {
                UiUtils.invalidScriptProvided(
                        ex.getMessage(),
                        ZendScript.getOptionsSubPath());
            }
            return null;
        }
        assert zendScript.isValid();

        ExternalProcessBuilder externalProcessBuilder = zendScript.getProcessBuilder()
                .workingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()));
        for (String param : zendScript.getParameters()) {
            externalProcessBuilder = externalProcessBuilder.addArgument(param);
        }
        externalProcessBuilder = registerIncludePathPrepend(externalProcessBuilder);

        return externalProcessBuilder;
    }

    public static ExternalProcessBuilder registerIncludePathPrepend(ExternalProcessBuilder processBuilder) {
        return processBuilder.addEnvironmentVariable(
                ZendScript.ENV_INCLUDE_PATH_PREPEND, COMMANDS_PROVIDER.getParentFile().getAbsolutePath());
    }

    @Override
    protected List<FrameworkCommand> getFrameworkCommandsInternal() {
        ExternalProcessBuilder processBuilder = createCommand("show", "nb-commands", SEPARATOR); // NOI18N
        if (processBuilder == null) {
            return null;
        }

        processBuilder = processBuilder.redirectErrorStream(true);
        final CommandsLineProcessor lineProcessor = new CommandsLineProcessor();
        ExecutionDescriptor executionDescriptor = new ExecutionDescriptor().inputOutput(InputOutput.NULL)
                .outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {
            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.ansiStripping(InputProcessors.bridge(lineProcessor));
            }
        });

        List<FrameworkCommand> freshCommands = Collections.emptyList();
        ExecutionService service = ExecutionService.newService(processBuilder, executionDescriptor, "help"); // NOI18N
        Future<Integer> task = service.run();
        try {
            if (task.get().intValue() == 0) {
                freshCommands = lineProcessor.getCommands();
            }
            NotifyDescriptor descriptor = null;
            if (freshCommands.isEmpty()) {
                descriptor = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(ZendCommandSupport.class, "MSG_NoCommandsRegisterProvider"),
                        NotifyDescriptor.YES_NO_OPTION);
                if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION) {
                    ZendScript.registerNetBeansProvider();
                }
                // #180425
                String error = lineProcessor.getError();
                if (StringUtils.hasText(error)) {
                    descriptor = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(ZendCommandSupport.class, "MSG_DisplayOutput"),
                            NotifyDescriptor.YES_NO_OPTION);
                    if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(error));
                    }
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(error);
                    }
                }
            }

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return freshCommands;
    }

    class CommandsLineProcessor implements LineProcessor {
        private final StringBuffer error = new StringBuffer(1000);
        private final String newLine = System.getProperty("line.separator"); // NOI18N

        // @GuardedBy(commands)
        private final List<FrameworkCommand> commands = new LinkedList<FrameworkCommand>();

        @Override
        public void processLine(String line) {
            if (!StringUtils.hasText(line)) {
                return;
            }
            // # 179255
            if (!line.contains(SEPARATOR)) {
                // error occured
                error.append(line);
                error.append(newLine);
                return;
            }
            String trimmed = line.trim();
            List<String> exploded = StringUtils.explode(trimmed, SEPARATOR);
            assert exploded.size() > 0;
            String command = exploded.get(0);
            String description = ""; // NOI18N
            if (exploded.size() > 1) {
                description = exploded.get(1);
            }
            synchronized (commands) {
                commands.add(new ZendCommand(phpModule, StringUtils.explode(command, " ").toArray(new String[0]), description, command)); // NOI18N
            }
        }

        public List<FrameworkCommand> getCommands() {
            List<FrameworkCommand> copy = null;
            synchronized (commands) {
                copy = new ArrayList<FrameworkCommand>(commands);
            }
            return copy;
        }

        public String getError() {
            return error.toString();
        }

        @Override
        public void close() {
        }

        @Override
        public void reset() {
        }
    }
}
