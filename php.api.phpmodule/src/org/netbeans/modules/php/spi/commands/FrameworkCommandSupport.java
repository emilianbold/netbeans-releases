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

package org.netbeans.modules.php.spi.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.modules.php.api.phpmodule.PhpInterpreter;
import org.netbeans.modules.php.api.phpmodule.PhpProgram.InvalidPhpProgramException;
import org.netbeans.modules.php.api.ui.commands.RefreshPhpModuleRunnable;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpProgram;
import org.netbeans.modules.php.api.ui.commands.FrameworkCommandChooser;
import org.netbeans.modules.php.api.util.UiUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * @author Tomas Mysik
 */
public abstract class FrameworkCommandSupport {

    // @GuardedBy(COMMANDS_CACHE)
    private static final Map<PhpModule, List<FrameworkCommand>> COMMANDS_CACHE = new WeakHashMap<PhpModule, List<FrameworkCommand>>();

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    protected final PhpModule phpModule;

    // @GuardedBy(this)
    private PluginListener pluginListener;

    protected FrameworkCommandSupport(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    /**
     * Get the name of the framework; it's used in the UI for selecting a command.
     * @return the name of the framework.
     * @since 1.8
     */
    public abstract String getFrameworkName();

    /**
     * Run command for the given command descriptor.
     * @param commandDescriptor descriptor for the selected framework command
     * @since 1.8
     * @see #runCommand()
     * @see RunCommand
     * @see CommandDescriptor
     */
    public abstract void runCommand(CommandDescriptor commandDescriptor);

    /**
     * Get options path for {@link ExecutionDescriptor execution descriptor}, can be <code>null</code>.
     * @return options path, can be <code>null</code> if not needed.
     */
    protected abstract String getOptionsPath();

    /**
     * Get the process builder for running framework commands or <code>null</code> if something is wrong.
     * The default implmentation returns {@link ExternalProcessBuilder process builder}
     * with default {@link PhpInterpreter#getDefault() PHP interpreter}
     * with all its parameters (specified in Tools > Options > PHP).
     * @param warnUser <code>true</code> if user should be warned (e.g. the script is incorrect), <code>false</code> otherwise
     * @return {@ExternalProcessBuilder process builder} with default {@link PhpInterpreter#getDefault() PHP interpreter}
     *         or <code>null</code> if something is wrong.
     * @see PhpInterpreter#getDefault()
     */
    protected ExternalProcessBuilder getProcessBuilder(boolean warnUser) {
        PhpInterpreter phpInterpreter;
        try {
            phpInterpreter = PhpInterpreter.getDefault();
        } catch (InvalidPhpProgramException ex) {
            if (warnUser) {
                UiUtils.invalidScriptProvided(ex.getLocalizedMessage());
            }
            return null;
        }
        assert phpInterpreter.isValid() : "php interpreter must be valid";

        ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(phpInterpreter.getProgram());
        for (String param : phpInterpreter.getParameters()) {
            externalProcessBuilder = externalProcessBuilder.addArgument(param);
        }
        return externalProcessBuilder;
    }

    /**
     * Get the framework commands. Typically in this method script is called and its output is parsed,
     * so the list of {@link FrameworkCommand commands} can be returned.
     * @return list of {@link FrameworkCommand commands}, can be <code>null</code> (typically if any error occurs).
     */
    protected abstract List<FrameworkCommand> getFrameworkCommandsInternal();

    /**
     * Get {@link PhpModule PHP module} for which this framework command support is created.
     * @return {@link PhpModule PHP module} for which this framework command support is created, never <code>null</code>.
     * @since 1.8
     */
    public PhpModule getPhpModule() {
        return phpModule;
    }

    /**
     * Get framework commands, can be empty or <code>null</code> if not known already.
     * @return list of {@link FrameworkCommand framework commands} or <code>null</code> if not known already.
     */
    public List<FrameworkCommand> getFrameworkCommands() {
        List<FrameworkCommand> commands;
        synchronized (COMMANDS_CACHE) {
            commands = COMMANDS_CACHE.get(phpModule);
        }
        return commands;
    }

    /**
     * Get {@link ExecutionDescriptor descriptor} with no factory for standard output processor.
     * @return {@link ExecutionDescriptor descriptor} with no factory for standard output processor.
     * @see #getDescriptor(InputProcessorFactory)
     */
    public ExecutionDescriptor getDescriptor() {
        return getDescriptor(null);
    }

    /**
     * Get {@link ExecutionDescriptor descriptor} with factory for standard output processor.
     * This descriptor refreshes PHP module after running a command.
     * @param outFactory factory for standard output processor.
     * @return {@link ExecutionDescriptor descriptor} with factory for standard output processor.
     */
    public ExecutionDescriptor getDescriptor(InputProcessorFactory outFactory) {
        ExecutionDescriptor descriptor = PhpProgram.getExecutionDescriptor().postExecution(new RefreshPhpModuleRunnable(phpModule))
                .errProcessorFactory(PhpProgram.ANSI_STRIPPING_FACTORY);
        String optionsPath = getOptionsPath();
        if (optionsPath != null) {
            descriptor = descriptor.optionsPath(optionsPath);
        }
        if (outFactory != null) {
            descriptor = descriptor.outProcessorFactory(new ProxyInputProcessorFactory(PhpProgram.ANSI_STRIPPING_FACTORY, outFactory));
        } else {
            descriptor = descriptor.outProcessorFactory(PhpProgram.ANSI_STRIPPING_FACTORY);
        }
        return descriptor;
    }

    final void refreshFrameworkCommands() {
        List<FrameworkCommand> freshCommands = getFrameworkCommandsInternal();

        synchronized (this) {
            if (pluginListener == null) {
                pluginListener = new PluginListener();
                File folder = FileUtil.toFile(phpModule.getSourceDirectory());
                // weakly referenced + hardcoded for now
                FileUtil.addFileChangeListener(pluginListener, new File(folder, "plugins")); // NOI18N
            }
        }
        synchronized (COMMANDS_CACHE) {
            COMMANDS_CACHE.put(phpModule, freshCommands);
        }
    }

    /**
     * Refresh framework commands in background.
     * @param post {@link Runnable} that is run afterwards, can be <code>null</code>.
     */
    public final void refreshFrameworkCommandsLater(final Runnable post) {
        EXECUTOR.submit(new Runnable() {
            public void run() {
                refreshFrameworkCommands();
                if (post != null) {
                    post.run();
                }
            }
        });
    }

    /**
     * Create command. Warning should be shown if any error occurs.
     * @param command command to create.
     * @param arguments command's arguments.
     * @return command or <code>null</code> if any error occurs.
     * @see #createSilentCommand(String, String[])
     */
    public ExternalProcessBuilder createCommand(final String command, final String... arguments) {
        return createCommandInternal(command, arguments, true);
    }

    /**
     * Create command. No error dialog is displayed if e.g. framework script is invalid.
     * @param command command to create.
     * @param arguments command's arguments.
     * @return command or <code>null</code> if any error occurs.
     * @see #createCommand(String, String[])
     */
    public ExternalProcessBuilder createSilentCommand(final String command, final String... arguments) {
        return createCommandInternal(command, arguments, false);
    }

    /**
     * Get the title for the given command descriptor that could be useful for e.g. Output Window title.
     * @param commandDescriptor command descriptor
     * @return title for the given command descriptor.
     */
    public String getOutputTitle(CommandDescriptor commandDescriptor) {
        return getOutputTitle(commandDescriptor.getFrameworkCommand().getCommand(), commandDescriptor.getCommandParams());
    }

    /**
     * Get the title for the given command and its arguments that could be useful for e.g. Output Window title.
     * @param command command
     * @param params command's arguments
     * @return the title for the given command and its arguments.
     */
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

    /**
     * Show the panel with framework commands with possibility to run any.
     * @since 1.8
     * @see #runCommand(CommandDescriptor)
     */
    public void runCommand() {
        FrameworkCommandChooser.open(this);
    }

    private ExternalProcessBuilder createCommandInternal(final String command, final String[] arguments, boolean warnUser) {
        ExternalProcessBuilder processBuilder = getProcessBuilder(warnUser);
        if (processBuilder == null) {
            return null;
        }
        processBuilder = processBuilder.addArgument(command);
        for (String arg : arguments) {
            processBuilder = processBuilder.addArgument(arg);
        }
        return processBuilder;
    }

    /**
     * Proxy for factories for standard input processor.
     */
    public static final class ProxyInputProcessorFactory implements InputProcessorFactory {
        private final List<InputProcessorFactory> factories;

        public ProxyInputProcessorFactory(InputProcessorFactory... proxied) {
            Parameters.notNull("proxied", proxied);

            factories = new ArrayList<InputProcessorFactory>(proxied.length);
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
            synchronized (COMMANDS_CACHE) {
                COMMANDS_CACHE.remove(getPhpModule());
            }
        }
    }

    /**
     * Descriptor for the selected framework command.
     * @see FrameworkCommandChooser#open(FrameworkCommandSupport)
     */
    public static final class CommandDescriptor {

        private final FrameworkCommand task;
        private final String[] params;
        private final boolean debug;

        public CommandDescriptor(FrameworkCommand task, String params, boolean debug) {
            Parameters.notNull("task", task);
            Parameters.notNull("params", params);

            this.task = task;
            this.params = Utilities.parseParameters(params.trim());
            this.debug = debug;
        }

        public FrameworkCommand getFrameworkCommand() {
            return task;
        }

        public String[] getCommandParams() {
            return params;
        }

        public boolean isDebug() {
            return debug;
        }
    }

    /**
     * Command that is run when a command is invoked.
     * @since 1.8
     */
    public static interface RunCommand {

        /**
         * Called when a command is to be run.
         * <p>
         * This method is run in AWT thread.
         * @param commandDescriptor {@link CommandDescriptor descriptor} of a command to be run.
         */
        void runCommand(CommandDescriptor commandDescriptor);
    }
}
