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

package org.netbeans.modules.php.symfony;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpProgram;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.spi.commands.FrameworkCommand;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport.ProxyInputProcessorFactory;
import org.netbeans.modules.php.symfony.commands.SymfonyCommandSupport;
import org.netbeans.modules.php.symfony.ui.options.SymfonyOptions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public class SymfonyScript extends PhpProgram {
    public static final String SCRIPT_NAME = "symfony"; // NOI18N

    private static final String OPTIONS_SUB_PATH = "Symfony"; // NOI18N

    public static final String CMD_INIT_PROJECT = "generate:project"; // NOI18N
    public static final String CMD_CLEAR_CACHE = "cache:clear"; // NOI18N
    public static final String CMD_INIT_APP = "generate:app"; // NOI18N

    public SymfonyScript(String command) {
        super(command);
    }

    /**
     * Get the default, <b>valid only</b> Symfony script.
     * @return the default, <b>valid only</b> Symfony script.
     * @throws InvalidPhpProgramException if Symfony script is not valid.
     */
    public static SymfonyScript getDefault() throws InvalidPhpProgramException {
        String symfony = SymfonyOptions.getInstance().getSymfony();
        String error = validate(symfony);
        if (error != null) {
            throw new InvalidPhpProgramException(error);
        }
        return new SymfonyScript(symfony);
    }

    /**
     * Get the project specific, <b>valid only</b> Symfony script. If not found, the {@link #getDefault() default} Symfony script is returned.
     * @param phpModule PHP module for which Symfony script is taken
     * @param warn <code>true</code> if user is warned when the {@link #getDefault() default} Symfony script is returned.
     * @return the project specific, <b>valid only</b> Symfony script.
     * @throws InvalidPhpProgramException if Symfony script is not valid. If not found, the {@link #getDefault() default} Symfony script is returned.
     * @see #getDefault()
     */
    public static SymfonyScript forPhpModule(PhpModule phpModule, boolean warn) throws InvalidPhpProgramException {
        String symfony = new File(FileUtil.toFile(phpModule.getSourceDirectory()), SCRIPT_NAME).getAbsolutePath();
        String error = validate(symfony);
        if (error != null) {
            if (warn) {
                Message message = new NotifyDescriptor.Message(
                        NbBundle.getMessage(SymfonyScript.class, "MSG_InvalidProjectSymfonyScript", error),
                        NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(message);
            }
            return getDefault();
        }
        return new SymfonyScript(symfony);
    }

    /**
     * @return full IDE options Symfony path
     */
    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + getOptionsSubPath(); // NOI18N
    }

    /**
     * @return IDE options Symfony subpath
     */
    public static String getOptionsSubPath() {
        return OPTIONS_SUB_PATH;
    }

    @Override
    public String validate() {
        if (!StringUtils.hasText(getProgram())) {
            return NbBundle.getMessage(SymfonyScript.class, "MSG_NoSymfony");
        }

        File file = new File(getProgram());
        if (!file.isAbsolute()) {
            return NbBundle.getMessage(SymfonyScript.class, "MSG_SymfonyNotAbsolutePath");
        }
        if (!file.isFile()) {
            return NbBundle.getMessage(SymfonyScript.class, "MSG_SymfonyNotFile");
        }
        if (!file.canRead()) {
            return NbBundle.getMessage(SymfonyScript.class, "MSG_SymfonyCannotRead");
        }
        return null;
    }

    public static String validate(String command) {
        return new SymfonyScript(command).validate();
    }

    public boolean initProject(PhpModule phpModule) {
        String projectName = phpModule.getDisplayName();
        SymfonyCommandSupport commandSupport = getCommandSupport(phpModule);
        ExternalProcessBuilder processBuilder = commandSupport.createSilentCommand(CMD_INIT_PROJECT, projectName);
        assert processBuilder != null;
        ExecutionDescriptor executionDescriptor = commandSupport.getDescriptor();
        runService(processBuilder, executionDescriptor, commandSupport.getOutputTitle(CMD_INIT_PROJECT, projectName), false);
        return SymfonyPhpFrameworkProvider.getInstance().isInPhpModule(phpModule);
    }

    public void initApp(PhpModule phpModule, String app, String[] params) {
        assert StringUtils.hasText(app);
        assert params != null;

        String[] cmdParams = mergeArrays(params, new String[]{app});
        FrameworkCommandSupport commandSupport = getCommandSupport(phpModule);
        ExternalProcessBuilder processBuilder = commandSupport.createCommand(CMD_INIT_APP, cmdParams);
        assert processBuilder != null;
        ExecutionDescriptor executionDescriptor = commandSupport.getDescriptor();
        runService(processBuilder, executionDescriptor, commandSupport.getOutputTitle(CMD_INIT_APP, cmdParams), true);
    }

    public static String getHelp(PhpModule phpModule, FrameworkCommand command) {
        assert phpModule != null;
        assert command != null;

        FrameworkCommandSupport commandSupport = getCommandSupport(phpModule);
        ExternalProcessBuilder processBuilder = commandSupport.createSilentCommand("help", command.getCommand());
        assert processBuilder != null;

        final HelpLineProcessor lineProcessor = new HelpLineProcessor();
        ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .outProcessorFactory(new ProxyInputProcessorFactory(FrameworkCommandSupport.ANSI_STRIPPING, new ExecutionDescriptor.InputProcessorFactory() {
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.bridge(lineProcessor);
            }
        }));
        runService(processBuilder, executionDescriptor, "getting help for: " + command.getPreview(), true); // NOI18N
        return lineProcessor.getHelp();
    }

    static <T> T[] mergeArrays(T[]... arrays) {
        List<T> list = new LinkedList<T>();
        for (T[] array : arrays) {
            list.addAll(Arrays.asList(array));
        }
        @SuppressWarnings("unchecked")
        T[] merged = (T[]) Array.newInstance(arrays[0].getClass().getComponentType(), list.size());
        return list.toArray(merged);
    }

    private static void runService(ExternalProcessBuilder processBuilder, ExecutionDescriptor executionDescriptor, String title, boolean warnUser) {
        final ExecutionService service = ExecutionService.newService(
                processBuilder,
                executionDescriptor,
                title);
        final Future<Integer> result = service.run();
        try {
            result.get();
        } catch (ExecutionException ex) {
            if (warnUser) {
                UiUtils.processExecutionException(ex, SymfonyScript.getOptionsSubPath());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static SymfonyCommandSupport getCommandSupport(PhpModule phpModule) {
        return (SymfonyCommandSupport) SymfonyPhpFrameworkProvider.getInstance().createFrameworkCommandSupport(phpModule);
    }

    static class HelpLineProcessor implements LineProcessor {
        private final StringBuilder buffer = new StringBuilder();

        public void processLine(String line) {
            buffer.append(line);
            buffer.append("\n"); // NOI18N
        }

        public void reset() {
        }

        public void close() {
        }

        public String getHelp() {
            return buffer.toString().trim() + "\n"; // NOI18N
        }
    }
}
