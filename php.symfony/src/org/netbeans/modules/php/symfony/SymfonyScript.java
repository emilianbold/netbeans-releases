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
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpOptions;
import org.netbeans.modules.php.api.util.PhpProgram;
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
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public class SymfonyScript extends PhpProgram {
    public static final String SCRIPT_NAME = "symfony"; // NOI18N

    private static final String PARAM_VERSION = "--version"; // NOI18N
    private static final String OPTIONS_SUB_PATH = "Symfony"; // NOI18N

    //                                                              symfony version 1.2.7 (/usr/share/php/symfony)
    //                                                              symfony version 1.2.8-DEV (/usr/share/php/symfony)
    private static final Pattern SYMFONY_VERSION = Pattern.compile("symfony\\s+version\\s+(\\d+)\\.(\\d+)\\.(\\d+)(?:\\-DEV)?\\s+"); // NOI18N

    // @GuardedBy(CACHE) [key "null" => default symfony script]
    private static final Map<PhpModule, SymfonyScript> CACHE = new WeakHashMap<PhpModule, SymfonyScript>();

    // script cannot be run at all
    static final int[] UNDETECTABLE_VERSION = new int[0];
    // script can be run but the output is not expected
    static final int[] UNKNOWN_VERSION = new int[0];
    /**
     * volatile is enough because:
     *  - never mind if the version is detected 2x
     *  - we don't change array values but only the array itself (local variable created and then assigned to 'version')
     */
    volatile int[] version = null;

    public static final String CMD_INIT_PROJECT = "generate:project"; // NOI18N
    public static final String CMD_CLEAR_CACHE = "cache:clear"; // NOI18N

    public SymfonyScript(String command) {
        super(command);
    }

    /**
     * Get the default, <b>valid only</b> Symfony script.
     * @return the default, <b>valid only</b> Symfony script.
     * @throws InvalidSymfonyScriptException if Symfony script is not valid.
     */
    public static SymfonyScript getDefault() throws InvalidSymfonyScriptException {
        SymfonyScript symfonyScript = null;
        synchronized (CACHE) {
            symfonyScript = CACHE.get(null);
        }
        if (symfonyScript != null) {
            return symfonyScript;
        }

        String symfony = SymfonyOptions.getInstance().getSymfony();
        String error = validate(symfony);
        if (error != null) {
            throw new InvalidSymfonyScriptException(error);
        }
        symfonyScript = new SymfonyScript(symfony);
        synchronized (CACHE) {
            CACHE.put(null, symfonyScript);
        }
        return symfonyScript;
    }

    /**
     * Get the project specific, <b>valid only</b> Symfony script. If not found, the {@link #getDefault() default} Symfony script is returned.
     * @param phpModule PHP module for which Symfony script is taken
     * @param warn <code>true</code> if user is warned when the {@link #getDefault() default} Symfony script is returned.
     * @return the project specific, <b>valid only</b> Symfony script.
     * @throws InvalidSymfonyScriptException if Symfony script is not valid. If not found, the {@link #getDefault() default} Symfony script is returned.
     * @see #getDefault()
     */
    public static SymfonyScript forPhpModule(PhpModule phpModule, boolean warn) throws InvalidSymfonyScriptException {
        SymfonyScript symfonyScript = null;
        synchronized (CACHE) {
            symfonyScript = CACHE.get(phpModule);
        }
        if (symfonyScript != null) {
            return symfonyScript;
        }

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
        symfonyScript = new SymfonyScript(symfony);
        synchronized (CACHE) {
            CACHE.put(phpModule, symfonyScript);
        }
        return symfonyScript;
    }

    public static String getOptionsPath() {
        return UiUtils.OPTIONS_PATH + "/" + getOptionsSubPath(); // NOI18N
    }

    public static String getOptionsSubPath() {
        return OPTIONS_SUB_PATH;
    }


    @Override
    public boolean isValid() {
        return validate(getFullCommand()) == null;
    }

    public static String validateDefault() {
        return validate(SymfonyOptions.getInstance().getSymfony());
    }

    public static String validate(String command) {
        SymfonyScript symfonyScript = new SymfonyScript(command);
        if (!StringUtils.hasText(symfonyScript.getProgram())) {
            return NbBundle.getMessage(SymfonyScript.class, "MSG_NoSymfony");
        }

        File file = new File(symfonyScript.getProgram());
        if (!file.isAbsolute()) {
            return NbBundle.getMessage(SymfonyScript.class, "MSG_SymfonyNotAbsolutePath");
        }
        if (!file.isFile()) {
            return NbBundle.getMessage(SymfonyScript.class, "MSG_SymfonyNotFile");
        }
        if (!file.canRead()) {
            return NbBundle.getMessage(SymfonyScript.class, "MSG_SymfonyCannotRead");
        }
        if (symfonyScript.getVersion() == UNDETECTABLE_VERSION) {
            return NbBundle.getMessage(SymfonyScript.class, "MSG_SymfonyUndetectableVersion");
        }
        if (symfonyScript.getVersion() == UNKNOWN_VERSION) {
            return NbBundle.getMessage(SymfonyScript.class, "MSG_SymfonyUnknownVersion");
        }
        return null;
    }

    public void initProject(PhpModule phpModule) {
        String projectName = phpModule.getDisplayName();
        SymfonyCommandSupport commandSupport = new SymfonyCommandSupport(phpModule);
        ExternalProcessBuilder processBuilder = commandSupport.createSilentCommand(CMD_INIT_PROJECT, projectName);
        assert processBuilder != null;
        ExecutionDescriptor executionDescriptor = commandSupport.getDescriptor();
        String tabTitle = String.format("%s %s \"%s\"", getProgram(), CMD_INIT_PROJECT, projectName); // NOI18N
        runService(processBuilder, executionDescriptor, tabTitle);
    }

    public static String getHelp(FrameworkCommand command) {
        try {
            return getHelp(getDefault(), command);
        } catch (InvalidSymfonyScriptException ex) {
            return ex.getMessage();
        }
    }

    public static String getHelp(SymfonyScript symfonyScript, FrameworkCommand command) {
        assert symfonyScript != null;
        assert symfonyScript.isValid();

        String phpInterpreter = Lookup.getDefault().lookup(PhpOptions.class).getPhpInterpreter();
        if (phpInterpreter == null) {
            return NbBundle.getMessage(SymfonyCommandSupport.class, "MSG_InvalidPhpInterpreter");
        }
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(phpInterpreter)
                .addArgument(symfonyScript.getProgram());
        for (String param : symfonyScript.getParameters()) {
            processBuilder = processBuilder.addArgument(param);
        }
        processBuilder = processBuilder.addArgument("help"); // NOI18N
        processBuilder = processBuilder.addArgument(command.getCommand());
        final HelpLineProcessor lineProcessor = new HelpLineProcessor();
        ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .outProcessorFactory(new ProxyInputProcessorFactory(FrameworkCommandSupport.ANSI_STRIPPING, new ExecutionDescriptor.InputProcessorFactory() {
            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.bridge(lineProcessor);
            }
        }));
        runService(processBuilder, executionDescriptor, "getting help for: " + command.getPreview()); // NOI18N
        return lineProcessor.getHelp();
    }

    private static void runService(ExternalProcessBuilder processBuilder, ExecutionDescriptor executionDescriptor, String title) {
        final ExecutionService service = ExecutionService.newService(
                processBuilder,
                executionDescriptor,
                title);
        final Future<Integer> result = service.run();
        try {
            result.get();
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, SymfonyScript.getOptionsSubPath());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private int[] getVersion() {
        if (!super.isValid()) {
            return UNDETECTABLE_VERSION;
        }
        if (version != null) {
            return version;
        }
        String phpInterpreter = Lookup.getDefault().lookup(PhpOptions.class).getPhpInterpreter();
        if (phpInterpreter == null) {
            return UNDETECTABLE_VERSION;
        }

        version = UNKNOWN_VERSION;
        ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(phpInterpreter)
                .addArgument(getProgram())
                .addArgument(PARAM_VERSION);
        ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .outProcessorFactory(new OutputProcessorFactory());
        ExecutionService service = ExecutionService.newService(externalProcessBuilder, executionDescriptor, null);
        Future<Integer> result = service.run();
        try {
            result.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            version = UNDETECTABLE_VERSION;
            LOGGER.log(Level.INFO, null, ex);
        }
        return version;
    }

    static int[] match(String text) {
        assert text != null;
        if (StringUtils.hasText(text)) {
            Matcher matcher = SYMFONY_VERSION.matcher(text);
            if (matcher.find()) {
                int major = Integer.parseInt(matcher.group(1));
                int minor = Integer.parseInt(matcher.group(2));
                int release = Integer.parseInt(matcher.group(3));
                return new int[] {major, minor, release};
            }
        }
        return null;
    }

    final class OutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {

        public InputProcessor newInputProcessor(final InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new LineProcessor() {
                public void processLine(String line) {
                    int[] match = match(line);
                    if (match != null) {
                        version = match;
                    }
                }
                public void reset() {
                    try {
                        defaultProcessor.reset();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                public void close() {
                    try {
                        defaultProcessor.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
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

    public static final class InvalidSymfonyScriptException extends Exception {
        private static final long serialVersionUID = -83198591758428354L;

        public InvalidSymfonyScriptException(String message) {
            super(message);
        }
    }
}
