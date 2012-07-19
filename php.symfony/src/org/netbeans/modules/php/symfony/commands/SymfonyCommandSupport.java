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

package org.netbeans.modules.php.symfony.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommand;
import org.netbeans.modules.php.spi.framework.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.symfony.SymfonyPhpFrameworkProvider;
import org.netbeans.modules.php.symfony.SymfonyScript;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public final class SymfonyCommandSupport extends FrameworkCommandSupport {
    static final Logger LOGGER = Logger.getLogger(SymfonyCommandSupport.class.getName());

    static final Pattern COMMAND_PATTERN = Pattern.compile("^\\:(\\S+)\\s+(.+)$"); // NOI18N
    static final Pattern PREFIX_PATTERN = Pattern.compile("^(\\w+)$"); // NOI18N

    public SymfonyCommandSupport(PhpModule phpModule) {
        super(phpModule);
    }

    @Override
    public String getFrameworkName() {
        return NbBundle.getMessage(SymfonyCommandSupport.class, "MSG_Symfony");
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
        return SymfonyScript.getOptionsPath();
    }

    @Override
    protected ExternalProcessBuilder getProcessBuilder(boolean warnUser) {
        ExternalProcessBuilder externalProcessBuilder = super.getProcessBuilder(warnUser);
        if (externalProcessBuilder == null) {
            return null;
        }
        SymfonyScript symfonyScript;
        try {
            symfonyScript = SymfonyScript.forPhpModule(phpModule, warnUser);
        } catch (InvalidPhpProgramException ex) {
            if (warnUser) {
                UiUtils.invalidScriptProvided(
                        ex.getMessage(),
                        SymfonyScript.getOptionsSubPath());
            }
            return null;
        }
        assert symfonyScript.isValid();

        externalProcessBuilder = externalProcessBuilder
                .workingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()))
                .addArgument(symfonyScript.getProgram());
        for (String param : symfonyScript.getParameters()) {
            externalProcessBuilder = externalProcessBuilder.addArgument(param);
        }
        externalProcessBuilder = externalProcessBuilder
                .addArgument("--color"); // NOI18N
        return externalProcessBuilder;
    }

    @Override
    protected List<FrameworkCommand> getFrameworkCommandsInternal() {
        List<FrameworkCommand> freshCommands = getFrameworkCommandsInternalXml();
        if (freshCommands != null) {
            return freshCommands;
        }

        ExternalProcessBuilder processBuilder = createCommand("list"); // NOI18N
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

        freshCommands = Collections.emptyList();
        ExecutionService service = ExecutionService.newService(processBuilder, executionDescriptor, "help"); // NOI18N
        Future<Integer> task = service.run();
        try {
            if (task.get().intValue() == 0) {
                freshCommands = lineProcessor.getCommands();
            }
            // #180425
            if (freshCommands.isEmpty()) {
                String error = lineProcessor.getError();
                if (StringUtils.hasText(error)) {
                    NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(SymfonyCommandSupport.class, "MSG_NoCommands"),
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

    private List<FrameworkCommand> getFrameworkCommandsInternalXml() {
        // validate
        if (getProcessBuilder(true) == null) {
            return null;
        }
        InputStream output = redirectScriptOutput("list", "--xml"); // NOI18N
        if (output == null) {
            return null;
        }
        List<SymfonyCommandVO> commandsVO = new ArrayList<SymfonyCommandVO>();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(output));
            SymfonyCommandsXmlParser.parse(reader, commandsVO);
        } finally {
            try {
                output.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        if (commandsVO.isEmpty()) {
            // ??? try to read them from output
            LOGGER.info("Symfony commands from XML should be parsed");
            return null;
        }
        List<FrameworkCommand> commands = new ArrayList<FrameworkCommand>(commandsVO.size());
        for (SymfonyCommandVO command : commandsVO) {
            commands.add(new SymfonyCommand(phpModule, command.getCommand(), command.getDescription(), command.getCommand()));
        }
        return commands;
    }

    @Override
    protected File getPluginsDirectory() {
        FileObject plugins = SymfonyPhpFrameworkProvider.locate(phpModule, "plugins", true); // NOI18N
        if (plugins != null && plugins.isFolder()) {
            return FileUtil.toFile(plugins);
        }
        return null;
    }

    //~ Inner classes

    class CommandsLineProcessor implements LineProcessor {
        private final StringBuffer error = new StringBuffer(200);
        private final String newLine = System.getProperty("line.separator"); // NOI18N

        // @GuardedBy(commands)
        private final List<FrameworkCommand> commands = new ArrayList<FrameworkCommand>();
        private String prefix;

        @Override
        public void processLine(String line) {
            if (!StringUtils.hasText(line)) {
                prefix = null;
                return;
            }
            error.append(line);
            error.append(newLine);

            String trimmed = line.trim();
            Matcher prefixMatcher = PREFIX_PATTERN.matcher(trimmed);
            if (prefixMatcher.matches()) {
                prefix = prefixMatcher.group(1);
            }
            Matcher commandMatcher = COMMAND_PATTERN.matcher(trimmed);
            if (commandMatcher.matches()) {
                String command = commandMatcher.group(1);
                if (prefix != null) {
                    command = prefix + ":" + command; // NOI18N
                }
                String description = commandMatcher.group(2);
                synchronized (commands) {
                    commands.add(new SymfonyCommand(phpModule, command, description, command));
                }
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
