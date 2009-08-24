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

package org.netbeans.modules.php.symfony.commands;

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
import org.netbeans.modules.php.spi.commands.FrameworkCommand;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpProgram;
import org.netbeans.modules.php.api.phpmodule.PhpProgram.InvalidPhpProgramException;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.symfony.SymfonyScript;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public final class SymfonyCommandSupport extends FrameworkCommandSupport {
    private static final Logger LOGGER = Logger.getLogger(SymfonyCommandSupport.class.getName());

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
        Callable<Process> callable = createCommand(commandDescriptor.getFrameworkCommand().getCommand(), commandDescriptor.getCommandParams());
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
        SymfonyScript symfonyScript = null;
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
        return externalProcessBuilder;
    }

    protected List<FrameworkCommand> getFrameworkCommandsInternal() {
        ExternalProcessBuilder processBuilder = createCommand("list"); // NOI18N
        if (processBuilder == null) {
            return null;
        }
        final CommandsLineProcessor lineProcessor = new CommandsLineProcessor();
        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(InputOutput.NULL)
                .outProcessorFactory(new ProxyInputProcessorFactory(PhpProgram.ANSI_STRIPPING_FACTORY, new ExecutionDescriptor.InputProcessorFactory() {

            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                // we are sure this will be invoked at most once
                return InputProcessors.bridge(lineProcessor);
            }
        }));

        List<FrameworkCommand> freshCommands = Collections.emptyList();
        ExecutionService service = ExecutionService.newService(processBuilder, descriptor, "help"); // NOI18N
        Future<Integer> task = service.run();
        try {
            if (task.get().intValue() == 0) {
                freshCommands = new ArrayList<FrameworkCommand>(lineProcessor.getCommands());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return freshCommands;
    }

    class CommandsLineProcessor implements LineProcessor {

        private List<FrameworkCommand> commands = Collections.synchronizedList(new ArrayList<FrameworkCommand>());
        private String prefix;

        public void processLine(String line) {
            if (!StringUtils.hasText(line)) {
                return;
            }
            String trimmed = line.trim();
            Matcher prefixMatcher = PREFIX_PATTERN.matcher(trimmed);
            if (prefixMatcher.matches()) {
                prefix = prefixMatcher.group(1);
            }
            if (prefix != null) {
                Matcher commandMatcher = COMMAND_PATTERN.matcher(trimmed);
                if (commandMatcher.matches()) {
                    String command = prefix + ":" + commandMatcher.group(1); // NOI18N
                    String description = commandMatcher.group(2);
                    commands.add(new SymfonyCommand(phpModule, command, description, command));
                }
            }
        }

        public List<FrameworkCommand> getCommands() {
            return commands;
        }

        public void close() {
        }

        public void reset() {
        }
    }
}
