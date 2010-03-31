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

package org.netbeans.modules.php.zend;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpProgram;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.zend.commands.ZendCommand;
import org.netbeans.modules.php.zend.commands.ZendCommandSupport;
import org.netbeans.modules.php.zend.ui.options.ZendOptions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public class ZendScript extends PhpProgram {
    public static final String SCRIPT_NAME;

    public static final String OPTIONS_SUB_PATH = "Zend"; // NOI18N

    public static final String ENV_INCLUDE_PATH_PREPEND = "ZEND_TOOL_INCLUDE_PATH_PREPEND"; // NOI18N

    public static final String CMD_INIT_PROJECT = "create"; // NOI18N
    public static final String[] CMD_INIT_PROJECT_ARGS = new String[] {"project", "."}; // NOI18N
    public static final String[] CMD_INIT_PROJECT_ARGS_TITLE = new String[] {"project"}; // NOI18N

    private static final String[] CMD_CREATE_CONFIG = new String[] {"create", "config"}; // NOI18N
    private static final String[] CMD_ENABLE_CONFIG = new String[] {"enable", "config.provider", "NetBeansCommandsProvider"}; // NOI18N

    static {
        String scriptName = null;
        if (Utilities.isWindows()) {
            scriptName = "zf.bat"; // NOI18N
        } else {
            scriptName = "zf.sh"; // NOI18N
        }
        SCRIPT_NAME = scriptName;
    }

    public ZendScript(String command) {
        super(command);
    }

    /**
     * Get the default, <b>valid only</b> Zend script.
     * @return the default, <b>valid only</b> Zend script.
     * @throws InvalidPhpProgramException if Zend script is not valid.
     */
    public static ZendScript getDefault() throws InvalidPhpProgramException {
        return getCustom(ZendOptions.getInstance().getZend());
    }

    /**
     * Get the default, <b>valid only</b> Zend script.
     * @return the default, <b>valid only</b> Zend script.
     * @throws InvalidPhpProgramException if Zend script is not valid.
     */
    private static ZendScript getCustom(String command) throws InvalidPhpProgramException {
        String error = validate(command);
        if (error != null) {
            throw new InvalidPhpProgramException(error);
        }
        return new ZendScript(command);
    }

    /**
     * @return full IDE options Zend path
     */
    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + getOptionsSubPath(); // NOI18N
    }

    /**
     * @return IDE options Zend subpath
     */
    public static String getOptionsSubPath() {
        return OPTIONS_SUB_PATH;
    }

    public static String getHelp(PhpModule phpModule, ZendCommand zendCommand) {
        assert phpModule != null;
        assert zendCommand != null;

        FrameworkCommandSupport commandSupport = ZendPhpFrameworkProvider.getInstance().getFrameworkCommandSupport(phpModule);
        ExternalProcessBuilder processBuilder = commandSupport.createSilentCommand(zendCommand.getCommands(), "?"); // NOI18N
        assert processBuilder != null;

        final HelpLineProcessor lineProcessor = new HelpLineProcessor();
        ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {
            @Override
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.ansiStripping(InputProcessors.bridge(lineProcessor));
            }
        });
        runService(processBuilder, executionDescriptor, "getting help for: " + zendCommand.getPreview(), true); // NOI18N
        return lineProcessor.getHelp();
    }

    @Override
    public String validate() {
        if (!StringUtils.hasText(getProgram())) {
            return NbBundle.getMessage(ZendScript.class, "MSG_NoZend");
        }

        File file = new File(getProgram());
        if (!file.isAbsolute()) {
            return NbBundle.getMessage(ZendScript.class, "MSG_ZendNotAbsolutePath");
        }
        if (!file.isFile()) {
            return NbBundle.getMessage(ZendScript.class, "MSG_ZendNotFile");
        }
        if (!file.canRead()) {
            return NbBundle.getMessage(ZendScript.class, "MSG_ZendCannotRead");
        }
        return null;
    }

    public static String validate(String command) {
        return new ZendScript(command).validate();
    }

    // 180184, needed for ZF 1.10+
    public static void registerNetBeansProvider() {
        registerNetBeansProvider(null);
    }

    public static void registerNetBeansProvider(String command) {
        try {
            ZendScript zendScript = command != null ? getCustom(command) : getDefault();

            ExecutionDescriptor executionDescriptor = getExecutionDescriptor()
                    .outProcessorFactory(ANSI_STRIPPING_FACTORY)
                    .errProcessorFactory(ANSI_STRIPPING_FACTORY)
                    .optionsPath(getOptionsPath());

            // create config
            ExternalProcessBuilder processBuilder = ZendCommandSupport.registerIncludePathPrepend(zendScript.getProcessBuilder());
            for (String arg : CMD_CREATE_CONFIG) {
                processBuilder = processBuilder.addArgument(arg);
            }
            executeAndWait(processBuilder, executionDescriptor, StringUtils.implode(Arrays.asList(CMD_CREATE_CONFIG), " ")); // NOI18N

            // enable config
            processBuilder = ZendCommandSupport.registerIncludePathPrepend(zendScript.getProcessBuilder());
            for (String arg : CMD_ENABLE_CONFIG) {
                processBuilder = processBuilder.addArgument(arg);
            }
            executeAndWait(processBuilder, executionDescriptor, StringUtils.implode(Arrays.asList(CMD_ENABLE_CONFIG), " ")); // NOI18N

            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                NbBundle.getMessage(ZendScript.class, "MSG_ProviderRegistrationInfo"),
                NotifyDescriptor.INFORMATION_MESSAGE));
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, getOptionsSubPath());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (InvalidPhpProgramException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage(), getOptionsSubPath());
        }
    }

    public boolean initProject(PhpModule phpModule) {
        ZendCommandSupport commandSupport = ZendPhpFrameworkProvider.getInstance().getFrameworkCommandSupport(phpModule);
        ExternalProcessBuilder processBuilder = commandSupport.createSilentCommand(CMD_INIT_PROJECT, CMD_INIT_PROJECT_ARGS);
        if (processBuilder == null) {
            return false;
        }
        ExecutionDescriptor executionDescriptor = commandSupport.getDescriptor();
        runService(processBuilder, executionDescriptor, commandSupport.getOutputTitle(CMD_INIT_PROJECT, CMD_INIT_PROJECT_ARGS_TITLE), false);
        return ZendPhpFrameworkProvider.getInstance().isInPhpModule(phpModule);
    }

    private static void runService(ExternalProcessBuilder processBuilder, ExecutionDescriptor executionDescriptor, String title, boolean warnUser) {
        try {
            executeAndWait(processBuilder, executionDescriptor, title);
        } catch (ExecutionException ex) {
            if (warnUser) {
                UiUtils.processExecutionException(ex, getOptionsSubPath());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    static class HelpLineProcessor implements LineProcessor {
        private final StringBuilder buffer = new StringBuilder(2000);

        @Override
        public void processLine(String line) {
            buffer.append(line);
            buffer.append("\n"); // NOI18N
        }

        @Override
        public void reset() {
        }

        @Override
        public void close() {
        }

        public String getHelp() {
            return buffer.toString().trim() + "\n"; // NOI18N
        }
    }
}
