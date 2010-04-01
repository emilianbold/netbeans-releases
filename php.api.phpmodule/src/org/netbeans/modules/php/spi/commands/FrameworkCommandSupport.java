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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
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
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * @author Tomas Mysik
 */
public abstract class FrameworkCommandSupport {

    // @GuardedBy(COMMANDS_CACHE)
    private static final Map<PhpModule, List<FrameworkCommand>> COMMANDS_CACHE = new WeakHashMap<PhpModule, List<FrameworkCommand>>();

    private static final RequestProcessor RP = new RequestProcessor(FrameworkCommandSupport.class.getName());

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
     * Get the plugin directory to which a {@link FileChangeListener} is added
     * (commands are refreshed if any change in this directory happens).
     * @return the plugin directory or <code>null</code> if the framework does not have such directory
     * @since 1.18
     */
    protected abstract File getPluginsDirectory();

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

        return phpInterpreter.getProcessBuilder();
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
     * Get {@link PhpProgram#getExecutionDescriptor() descriptor} with factory for standard output processor.
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

        File plugins = getPluginsDirectory();
        if (plugins != null) {
            // intentionally used isFile() because directory does not need to exist
            assert !plugins.isFile() : "Plugins is expected to be a directory: " + plugins;
            synchronized (this) {
                if (pluginListener == null) {
                    pluginListener = new PluginListener();
                    FileUtil.addFileChangeListener(pluginListener, plugins);
                }
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
        RP.execute(new Runnable() {
            @Override
            public void run() {
                refreshFrameworkCommands();
                if (post != null) {
                    post.run();
                }
            }
        });
    }

    /**
     * Create command which uses {@link #getProcessBuilder(boolean) process builder} and {@link #getDescriptor() descriptor}.
     * Warning should be shown if any error occurs.
     * This method is useful for commands like "create-project" etc.
     * @param command command to create.
     * @param arguments command's arguments.
     * @return command or <code>null</code> if any error occurs.
     * @see #createSilentCommand(String, String[])
     */
    public ExternalProcessBuilder createCommand(final String command, final String... arguments) {
        return createCommand(new String[] {command}, arguments);
    }

    /**
     * Create command which uses {@link #getProcessBuilder(boolean) process builder} and {@link #getDescriptor() descriptor}.
     * Warning should be shown if any error occurs.
     * This method is useful for commands like "create project" etc.
     * @param commands command to create.
     * @param arguments command's arguments.
     * @return command or <code>null</code> if any error occurs.
     * @see #createSilentCommand(String, String[])
     * @since 1.24
     */
    public ExternalProcessBuilder createCommand(final String[] commands, final String... arguments) {
        return createCommandInternal(commands, arguments, true);
    }

    /**
     * Create command which uses {@link #getProcessBuilder(boolean) process builder} and {@link #getDescriptor() descriptor}.
     * No error dialog is displayed if e.g. framework script is invalid.
     * This method is useful for commands like "create-project" etc.
     * @param command command to create.
     * @param arguments command's arguments.
     * @return command or <code>null</code> if any error occurs.
     * @see #createCommand(String, String[])
     */
    public ExternalProcessBuilder createSilentCommand(final String command, final String... arguments) {
        return createSilentCommand(new String[] {command}, arguments);
    }

    /**
     * Create command which uses {@link #getProcessBuilder(boolean) process builder} and {@link #getDescriptor() descriptor}.
     * No error dialog is displayed if e.g. framework script is invalid.
     * This method is useful for commands like "create project" etc.
     * @param commands command to create.
     * @param arguments command's arguments.
     * @return command or <code>null</code> if any error occurs.
     * @see #createCommand(String, String[])
     * @since 1.24
     */
    public ExternalProcessBuilder createSilentCommand(final String[] commands, final String... arguments) {
        return createCommandInternal(commands, arguments, false);
    }

    /**
     * Get the title for the given command descriptor that could be useful for e.g. Output Window title.
     * @param commandDescriptor command descriptor
     * @return title for the given command descriptor.
     */
    public String getOutputTitle(CommandDescriptor commandDescriptor) {
        String command = StringUtils.implode(Arrays.asList(commandDescriptor.getFrameworkCommand().getCommands()), " ");
        return getOutputTitle(command, commandDescriptor.getCommandParams()); // NOI18N
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

    private ExternalProcessBuilder createCommandInternal(final String[] commands, final String[] arguments, boolean warnUser) {
        ExternalProcessBuilder processBuilder = getProcessBuilder(warnUser);
        if (processBuilder == null) {
            return null;
        }
        for (String command : commands) {
            processBuilder = processBuilder.addArgument(command);
        }
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

        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            InputProcessor[] processors = new InputProcessor[factories.size()];
            for (int i = 0; i < processors.length; i++) {
                processors[i] = factories.get(i).newInputProcessor(defaultProcessor);
            }
            return InputProcessors.proxy(processors);
        }
    }

    private class PluginListener implements FileChangeListener {

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
            changed();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            changed();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            changed();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            changed();
        }

        @Override
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
