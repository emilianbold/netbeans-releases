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

package org.netbeans.modules.php.symfony.ui.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
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
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.symfony.SymfonyScript;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Petr Hejl, Tomas Mysik
 */
public final class SymfonyCommandSupport {

    private static final Logger LOGGER = Logger.getLogger(SymfonyCommandSupport.class.getName());
    // @GuardedBy(this)
    private static final Map<PhpModule, SymfonyCommandSupport> CACHE = new WeakHashMap<PhpModule, SymfonyCommandSupport>();
    private static final ExecutionDescriptor SYMFONY_DESCRIPTOR = new ExecutionDescriptor()
            .controllable(true)
            .frontWindow(true)
            .inputVisible(true)
            .showProgress(true)
            .optionsPath(SymfonyScript.getOptionsPath());

    private static final InputProcessorFactory ANSI_STRIPPING = new AnsiStrippingInputProcessorFactory();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final PhpModule phpModule;

    // @GuardedBy(this)
    private PluginListener pluginListener;
    // @GuardedBy(this)
    private List<SymfonyCommand> commands;

    public static synchronized SymfonyCommandSupport forPhpModule(PhpModule phpModule) {
        assert phpModule != null;
        SymfonyCommandSupport commandSupport = CACHE.get(phpModule);
        if (commandSupport == null) {
            commandSupport = new SymfonyCommandSupport(phpModule);
            CACHE.put(phpModule, commandSupport);
        }
        return commandSupport;
    }

    private SymfonyCommandSupport(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    public synchronized List<SymfonyCommand> getSymfonyCommands() {
        return commands;
    }

    public ExecutionDescriptor getDescriptor() {
        return getDescriptor(null);
    }

    public ExecutionDescriptor getDescriptor(InputProcessorFactory outFactory) {
        ExecutionDescriptor descriptor = SYMFONY_DESCRIPTOR.postExecution(new RefreshPhpModuleRunnable(phpModule))
                .errProcessorFactory(ANSI_STRIPPING);
        if (outFactory != null) {
            descriptor = descriptor.outProcessorFactory(new ProxyInputProcessorFactory(ANSI_STRIPPING, outFactory));
        } else {
            descriptor = descriptor.outProcessorFactory(ANSI_STRIPPING);
        }
        return descriptor;
    }

    public void refreshSymfonyCommands() {
        ExternalProcessBuilder processBuilder = createSilentCommand("list"); // NOI18N
        if (processBuilder == null) {
            return;
        }
        final HelpLineProcessor lineProcessor = new HelpLineProcessor();
        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(InputOutput.NULL)
                .outProcessorFactory(new ProxyInputProcessorFactory(ANSI_STRIPPING, new ExecutionDescriptor.InputProcessorFactory() {

            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                // we are sure this will be invoked at most once
                return InputProcessors.bridge(lineProcessor);
            }
        }));

        List<SymfonyCommand> freshCommands = Collections.emptyList();
        ExecutionService service = ExecutionService.newService(processBuilder, descriptor, "help"); // NOI18N
        Future<Integer> task = service.run();
        try {
            if (task.get().intValue() == 0) {
                freshCommands = new ArrayList<SymfonyCommand>(lineProcessor.getCommands());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

        synchronized (this) {
            if (pluginListener == null) {
                pluginListener = new PluginListener();
                File folder = FileUtil.toFile(phpModule.getSourceDirectory());
                // weakly referenced
                FileUtil.addFileChangeListener(pluginListener, new File(folder, "plugins")); // NOI18N
            }
            commands = freshCommands;
        }
    }

    public void refreshSymfonyCommandsLater(final Runnable post) {
        EXECUTOR.submit(new Runnable() {
            public void run() {
                refreshSymfonyCommands();
                if (post != null) {
                    post.run();
                }
            }
        });
    }

    public ExternalProcessBuilder createCommand(final String command, final String... arguments) {
        return createCommandInternal(command, arguments, true);
    }

    /**
     * No error dialog is displayed if Symfony script is invalid.
     */
    public ExternalProcessBuilder createSilentCommand(final String command, final String... arguments) {
        return createCommandInternal(command, arguments, false);
    }

    public String getOutputTitle(SymfonyCommandChooser.CommandDescriptor commandDescriptor) {
        return getOutputTitle(commandDescriptor.getSymfonyCommand().getCommand(), commandDescriptor.getCommandParams());
    }

    public String getOutputTitle(String command, String... params) {
        StringBuilder title = new StringBuilder(200);
        title.append(phpModule.getDisplayName());
        title.append(" ("); // NOI18N
        title.append(command);
        for (String param : params) {
            title.append(" "); // NOI18N
            title.append(param);
        }
        title.append(")"); // NOI18N
        return title.toString();
    }

    private ExternalProcessBuilder createCommandInternal(final String command, final String[] arguments, boolean warnUser) {
        ExternalProcessBuilder processBuilder = getProcessBuilder(warnUser).addArgument(command);
        for (String arg : arguments) {
            processBuilder = processBuilder.addArgument(arg);
        }
        return processBuilder;
    }

    private ExternalProcessBuilder getProcessBuilder(boolean warnUser) {
        SymfonyScript symfonyScript = SymfonyScript.getDefault();
        if (!symfonyScript.isValid()) {
            if (warnUser) {
                UiUtils.invalidScriptProvided(
                        NbBundle.getMessage(SymfonyCommandSupport.class, "MSG_InvalidSymfonyScript"),
                        SymfonyScript.getOptionsSubPath());
            }
            return null;
        }
        ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(symfonyScript.getProgram())
                .workingDirectory(FileUtil.toFile(phpModule.getSourceDirectory()));
        for (String param : symfonyScript.getParameters()) {
            externalProcessBuilder = externalProcessBuilder.addArgument(param);
        }
        return externalProcessBuilder;
    }

    private static final class AnsiStrippingInputProcessorFactory implements InputProcessorFactory {

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.ansiStripping(defaultProcessor);
        }
    }

    static class HelpLineProcessor implements LineProcessor {

        private static final Pattern COMMAND_PATTERN = Pattern.compile("^\\:(\\S+)\\s+(.+)$"); // NOI18N
        private static final Pattern PREFIX_PATTERN = Pattern.compile("^(\\w+)$"); // NOI18N
        private List<SymfonyCommand> commands = Collections.synchronizedList(new ArrayList<SymfonyCommand>());
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
                    commands.add(new SymfonyCommand(command, description, command));
                }
            }
        }

        public List<SymfonyCommand> getCommands() {
            return commands;
        }

        public void close() {
        }

        public void reset() {
        }
    }

    private static class ProxyInputProcessorFactory implements InputProcessorFactory {

        private final List<InputProcessorFactory> factories = new ArrayList<InputProcessorFactory>();

        public ProxyInputProcessorFactory(InputProcessorFactory... proxied) {
            for (InputProcessorFactory factory : proxied) {
                if (factory != null) {
                    factories.add(factory);
                }
            }
        }

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            InputProcessor[] processors = new InputProcessor[factories.size()];
            for (int i = 0; i < processors.length; i++) {
                processors[i] = factories.get(i).newInputProcessor(defaultProcessor);
            }
            return InputProcessors.proxy(processors);
        }
    }

    private class PluginListener implements FileChangeListener {

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        public void fileChanged(FileEvent fe) {
            changed();
        }

        public void fileDataCreated(FileEvent fe) {
            changed();
        }

        public void fileDeleted(FileEvent fe) {
            changed();
        }

        public void fileFolderCreated(FileEvent fe) {
            changed();
        }

        public void fileRenamed(FileRenameEvent fe) {
            changed();
        }

        private void changed() {
            synchronized (SymfonyCommandSupport.this) {
                commands = null;
            }
        }
    }
}
