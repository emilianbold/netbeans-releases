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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.modules.php.api.phpmodule.PhpFrameworks;
import org.netbeans.modules.php.api.ui.commands.RefreshPhpModuleRunnable;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.ui.commands.FrameworkCommandChooser;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * @author Tomas Mysik
 */
public abstract class FrameworkCommandSupport {

    public static final InputProcessorFactory ANSI_STRIPPING = new AnsiStrippingInputProcessorFactory();

    protected static final Logger LOGGER = Logger.getLogger(FrameworkCommandSupport.class.getName());

    // @GuardedBy(CACHE)
    private static final Map<PhpModule, FrameworkCommandSupport> CACHE = new WeakHashMap<PhpModule, FrameworkCommandSupport>();
    private static final ExecutionDescriptor COMMAND_DESCRIPTOR = new ExecutionDescriptor()
            .controllable(true)
            .frontWindow(true)
            .inputVisible(true)
            .showProgress(true);

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    protected final PhpModule phpModule;

    // @GuardedBy(this)
    private PluginListener pluginListener;
    // @GuardedBy(this)
    private List<FrameworkCommand> commands;

    /**
     * Get the name of the framework; it's used in the UI for selecting a command.
     * @return the name of the framework.
     */
    protected abstract String getFrameworkName();

    /**
     * Get options path for {@link ExecutionDescriptor execution descriptor}, can be <code>null</code>.
     * @return options path, can be <code>null</code> if not needed.
     */
    protected abstract String getOptionsPath();

    /**
     * Get the process builder for running framework commands or <code>null</code> if something is wrong.
     * @param warnUser <code>true</code> if user should be warned (e.g. the script is incorrect), <code>false</code> otherwise
     * @return {@ExternalProcessBuilder process builder} or <code>null</code> if something is wrong.
     */
    protected abstract ExternalProcessBuilder getProcessBuilder(boolean warnUser);

    /**
     * Get the framework commands. Typically in this method script is called and its output is parsed,
     * so the list of {@link FrameworkCommand commands} can be returned.
     * @return list of {@link FrameworkCommand commands}, can be <code>null</code> (typically if any error occurs).
     * @throws InterruptedException if any error occurs.
     * @throws ExecutionException if any error occurs.
     */
    protected abstract List<FrameworkCommand> getFrameworkCommandsInternal() throws InterruptedException, ExecutionException;

    /**
     * Get {@link FrameworkCommandSupport} for the given PHP module. Only one instance per PHP module
     * is created and returned, can return <code>null</code> if there's no {@link Factory} for the given PHP module.
     * @param phpModule PHP module for which the command support is needed
     * @return {@link FrameworkCommandSupport} or <code>null</code> if there's no {@link Factory} for the given PHP module.
     * @see Factory
     */
    public static FrameworkCommandSupport forPhpModule(PhpModule phpModule) {
        Parameters.notNull("phpModule", phpModule);

        FrameworkCommandSupport commandSupport = null;
        synchronized (CACHE) {
            commandSupport = CACHE.get(phpModule);
        }
        if (commandSupport == null) {
            for (FrameworkCommandSupport.Factory factory : getFrameworkCommandSupportFactories()) {
                commandSupport = factory.create(phpModule);
                if (commandSupport != null) {
                    synchronized (CACHE) {
                        CACHE.put(phpModule, commandSupport);
                    }
                    break;
                }
            }
        }
        return commandSupport;
    }

    private static List<FrameworkCommandSupport.Factory> getFrameworkCommandSupportFactories() {
        return new ArrayList<FrameworkCommandSupport.Factory>(Lookups.forPath(PhpFrameworks.FRAMEWORK_PATH).lookupAll(FrameworkCommandSupport.Factory.class));
    }

    protected FrameworkCommandSupport(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    /**
     * Get framework commands, can be empty but never <code>null</code>.
     * @return list of {@link FrameworkCommand framework commands}.
     */
    public synchronized List<FrameworkCommand> getFrameworkCommands() {
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
        ExecutionDescriptor descriptor = COMMAND_DESCRIPTOR.postExecution(new RefreshPhpModuleRunnable(phpModule))
                .errProcessorFactory(ANSI_STRIPPING);
        String optionsPath = getOptionsPath();
        if (optionsPath != null) {
            descriptor = descriptor.optionsPath(optionsPath);
        }
        if (outFactory != null) {
            descriptor = descriptor.outProcessorFactory(new ProxyInputProcessorFactory(ANSI_STRIPPING, outFactory));
        } else {
            descriptor = descriptor.outProcessorFactory(ANSI_STRIPPING);
        }
        return descriptor;
    }

    final void refreshFrameworkCommands() {
        List<FrameworkCommand> freshCommands = null;
        try {
            freshCommands = getFrameworkCommandsInternal();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

        synchronized (this) {
            if (pluginListener == null) {
                pluginListener = new PluginListener();
                File folder = FileUtil.toFile(phpModule.getSourceDirectory());
                // weakly referenced + hardcoded for now
                FileUtil.addFileChangeListener(pluginListener, new File(folder, "plugins")); // NOI18N
            }
            commands = freshCommands;
        }
    }

    /**
     * Refresh framework commands in background.
     * @param post {@link Runnable} taht is run afterwards.
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
     * @param runCommandListener {@link RunCommandListener listener} that is run when command is invoked.
     * @since 1.5
     */
    public void runCommand(RunCommandListener runCommandListener) {
        Parameters.notNull("runCommandListener", runCommandListener);
        FrameworkCommandChooser.open(phpModule, getFrameworkName(), runCommandListener);
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

    private static final class AnsiStrippingInputProcessorFactory implements InputProcessorFactory {

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.ansiStripping(defaultProcessor);
        }
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
            synchronized (FrameworkCommandSupport.this) {
                commands = null;
            }
        }
    }

    /**
     * Factory for creating {@link FrameworkCommandSupport}.
     */
    protected interface Factory {
        /**
         * Create {@link FrameworkCommandSupport} for the provided PHP module; can return <code>null</code>
         * if this the particular implementation is not interested in it (typically only project's framework implementation
         * will return an instance).
         * @param phpModule PHP module
         * @return {@link FrameworkCommandSupport} for the provided PHP module or <code>null</code>.
         */
        FrameworkCommandSupport create(PhpModule phpModule);
    }

    /**
     * Descriptor for the selected framework command.
     * @see FrameworkCommandChooser#open(PhpModule, String, FrameworkCommandSupport.RunCommandListener)
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
     * Listener that is run when a command is invoked.
     * @since 1.5
     */
    public static interface RunCommandListener {

        /**
         * Called when a command is to be run.
         * <p>
         * This method is run in AWT thread.
         * @param commandDescriptor {@link CommandDescriptor descriptor} of a command to be run.
         */
        void runCommand(CommandDescriptor commandDescriptor);
    }
}
