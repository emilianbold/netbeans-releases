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
import org.netbeans.modules.php.api.util.PhpProgram;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.symfony.ui.commands.SymfonyCommandSupport;
import org.netbeans.modules.php.symfony.ui.options.SymfonyOptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public class SymfonyScript extends PhpProgram {
    public static final String SCRIPT_NAME = "symfony"; // NOI18N

    private static final String PARAM_VERSION = "--version"; // NOI18N
    private static final String OPTIONS_SUB_PATH = "Symfony"; // NOI18N


    // unknown version
    static final int[] UNKNOWN_VERSION = new int[0];
    /**
     * volatile is enough because:
     *  - never mind if the version is detected 2x
     *  - we don't change array values but only the array itself (local variable created and then assigned to 'version')
     */
    static volatile int[] version = null;

    public static final String CMD_INIT_PROJECT = "generate:project"; // NOI18N
    public static final String CMD_CLEAR_CACHE = "cache:clear"; // NOI18N

    public SymfonyScript(String command) {
        super(command);
    }

    /**
     * Get the default, <b>valid only</b> Symfony script.
     * @return the default, <b>valid only</b> Symfony script, <code>null</code> otherwise.
     */
    public static SymfonyScript getDefault() {
        String symfony = SymfonyOptions.getInstance().getSymfony();
        if (validate(symfony) == null) {
            return new SymfonyScript(symfony);
        }
        return null;
    }

    public static void resetVersion() {
        version = null;
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
        if (symfonyScript.getVersion() == UNKNOWN_VERSION) {
            return NbBundle.getMessage(SymfonyScript.class, "MSG_SymfonyUnknownVersion");
        }
        return null;
    }

    public void initProject(PhpModule phpModule) {
        String projectName = phpModule.getDisplayName();
        SymfonyCommandSupport commandSupport = SymfonyCommandSupport.forPhpModule(phpModule);
        ExternalProcessBuilder processBuilder = commandSupport.createCommand(CMD_INIT_PROJECT, projectName);
        assert processBuilder != null;
        ExecutionDescriptor executionDescriptor = commandSupport.getDescriptor();
        String tabTitle = String.format("%s %s \"%s\"", getProgram(), CMD_INIT_PROJECT, projectName); // NOI18N
        final ExecutionService service = ExecutionService.newService(
                processBuilder,
                executionDescriptor,
                tabTitle);
        final Future<Integer> result = service.run();
        try {
            result.get();
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, getOptionsSubPath());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private int[] getVersion() {
        if (!super.isValid()) {
            return UNKNOWN_VERSION;
        }
        if (version != null) {
            return version;
        }

        version = UNKNOWN_VERSION;
        ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(getProgram())
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
            // ignored
            LOGGER.log(Level.INFO, null, ex);
        }
        return version;
    }

    static final class OutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {
        //                                                              symfony version 1.2.7 (/usr/share/php/symfony)
        private static final Pattern SYMFONY_VERSION = Pattern.compile("symfony\\s+version\\s+(\\d+)\\.(\\d+)\\.(\\d+)\\s+"); // NOI18N

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new LineProcessor() {
                public void processLine(String line) {
                    int[] match = match(line);
                    if (match != null) {
                        version = match;
                    }
                }
                public void reset() {
                }
                public void close() {
                }
            });
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
    }
}
