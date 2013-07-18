/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.composer.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.composer.options.ComposerOptions;
import org.netbeans.modules.php.composer.output.model.SearchResult;
import org.netbeans.modules.php.composer.output.parsers.Parsers;
import org.netbeans.modules.php.composer.ui.options.ComposerOptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Represents <a href="http://getcomposer.org/">Composer</a> command line tool.
 */
public final class Composer {

    public static final String NAME = "composer"; // NOI18N
    public static final String LONG_NAME = NAME + ".phar"; // NOI18N

    private static final String COMPOSER_FILENAME = "composer.json"; // NOI18N

    // commands
    private static final String INIT_COMMAND = "init"; // NOI18N
    private static final String INSTALL_COMMAND = "install"; // NOI18N
    private static final String UPDATE_COMMAND = "update"; // NOI18N
    private static final String REQUIRE_COMMAND = "require"; // NOI18N
    private static final String VALIDATE_COMMAND = "validate"; // NOI18N
    private static final String SELF_UPDATE_COMMAND = "self-update"; // NOI18N
    private static final String SEARCH_COMMAND = "search"; // NOI18N
    private static final String SHOW_COMMAND = "show"; // NOI18N
    // params
    private static final String ANSI_PARAM = "--ansi"; // NOI18N
    private static final String NO_INTERACTION_PARAM = "--no-interaction"; // NOI18N
    private static final String NAME_PARAM = "--name=%s"; // NOI18N
    private static final String AUTHOR_PARAM = "--author=%s <%s>"; // NOI18N
    private static final String DESCRIPTION_PARAM = "--description=%s"; // NOI18N
    private static final String DEV_PARAM = "--dev"; // NOI18N
    private static final String ONLY_NAME_PARAM = "--only-name"; // NOI18N
    private static final List<String> DEFAULT_PARAMS = Arrays.asList(
        ANSI_PARAM,
        NO_INTERACTION_PARAM
    );

    private final String composerPath;

    private volatile File workDir;


    public Composer(String composerPath) {
        this.composerPath = composerPath;
    }

    /**
     * Get the default, <b>valid only</b> Composer.
     * @return the default, <b>valid only</b> Composer.
     * @throws InvalidPhpExecutableException if Composer is not valid.
     */
    public static Composer getDefault() throws InvalidPhpExecutableException {
        String composerPath = ComposerOptions.getInstance().getComposerPath();
        String error = validate(composerPath);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new Composer(composerPath);
    }

    @NbBundle.Messages("Composer.script.label=Composer")
    public static String validate(String composerPath) {
        return PhpExecutableValidator.validateCommand(composerPath, Bundle.Composer_script_label());
    }

    public static boolean isValidOutput(String output) {
        if (output.startsWith("Warning:") // NOI18N
                || output.startsWith("No composer.json found")) { // NOI18N
            return false;
        }
        return true;
    }

    public Future<Integer> initIfNotPresent(PhpModule phpModule) {
        FileObject configFile = getComposerConfigFile(phpModule);
        if (configFile != null && configFile.isValid()) {
            return null;
        }
        return init(phpModule);
    }

    @NbBundle.Messages({
        "Composer.run.init=Composer (init)",
        "Composer.file.exists=Composer config file already exists - overwrite it?",
        "# {0} - project name",
        "Composer.init.description=Description of project {0}."
    })
    public Future<Integer> init(PhpModule phpModule) {
        FileObject configFile = getComposerConfigFile(phpModule);
        if (configFile != null && configFile.isValid()) {
            if (!userConfirmation(phpModule.getDisplayName(), Bundle.Composer_file_exists())) {
                return null;
            }
        }
        // command params
        ComposerOptions options = ComposerOptions.getInstance();
        List<String> params = Arrays.asList(
                String.format(NAME_PARAM, getInitName(options.getVendor(), phpModule.getName())),
                String.format(AUTHOR_PARAM, options.getAuthorName(), options.getAuthorEmail()),
                String.format(DESCRIPTION_PARAM, Bundle.Composer_init_description(phpModule.getDisplayName())));
        return runCommand(phpModule, INIT_COMMAND, Bundle.Composer_run_init(), params);
    }

    private String getInitName(String vendor, String projectName) {
        StringBuilder name = new StringBuilder(50);
        name.append(vendor);
        name.append('/'); // NOI18N
        name.append(StringUtils.webalize(projectName));
        return name.toString();
    }

    @NbBundle.Messages("Composer.run.install=Composer (install)")
    public Future<Integer> install(PhpModule phpModule) {
        return runCommand(phpModule, INSTALL_COMMAND, Bundle.Composer_run_install());
    }

    @NbBundle.Messages("Composer.run.installDev=Composer (install dev)")
    public Future<Integer> installDev(PhpModule phpModule) {
        return runCommand(phpModule, INSTALL_COMMAND, Bundle.Composer_run_installDev(), Collections.singletonList(DEV_PARAM));
    }

    @NbBundle.Messages("Composer.run.update=Composer (update)")
    public Future<Integer> update(PhpModule phpModule) {
        return runCommand(phpModule, UPDATE_COMMAND, Bundle.Composer_run_update());
    }

    @NbBundle.Messages("Composer.run.updateDev=Composer (update dev)")
    public Future<Integer> updateDev(PhpModule phpModule) {
        return runCommand(phpModule, UPDATE_COMMAND, Bundle.Composer_run_updateDev(), Collections.singletonList(DEV_PARAM));
    }

    @NbBundle.Messages("Composer.run.require=Composer (require)")
    public Future<Integer> require(PhpModule phpModule, String... packages) {
        return runCommand(phpModule, REQUIRE_COMMAND, Bundle.Composer_run_require(), Arrays.asList(packages));
    }

    @NbBundle.Messages("Composer.run.requireDev=Composer (require dev)")
    public Future<Integer> requireDev(PhpModule phpModule, String... packages) {
        List<String> params = new ArrayList<>(packages.length + 1);
        params.add(DEV_PARAM);
        params.addAll(Arrays.asList(packages));
        return runCommand(phpModule, REQUIRE_COMMAND, Bundle.Composer_run_requireDev(), params);
    }

    @NbBundle.Messages("Composer.run.validate=Composer (validate)")
    public Future<Integer> validate(PhpModule phpModule) {
        return runCommand(phpModule, VALIDATE_COMMAND, Bundle.Composer_run_validate());
    }

    @NbBundle.Messages("Composer.run.selfUpdate=Composer (self-update)")
    public Future<Integer> selfUpdate() {
        return runCommand(null, SELF_UPDATE_COMMAND, Bundle.Composer_run_selfUpdate());
    }

    @NbBundle.Messages("Composer.run.search=Composer (search)")
    public Future<Integer> search(PhpModule phpModule, String token, boolean onlyName, final OutputProcessor<SearchResult> outputProcessor) {
        PhpExecutable composer = getComposerExecutable(phpModule, Bundle.Composer_run_search());
        if (composer == null) {
            return null;
        }
        // params
        List<String> defaultParams = new ArrayList<>(DEFAULT_PARAMS);
        defaultParams.remove(ANSI_PARAM);
        List<String> params = new ArrayList<>(2);
        if (onlyName) {
            params.add(ONLY_NAME_PARAM);
        }
        params.add(token);
        composer = composer
                .additionalParameters(mergeParameters(SEARCH_COMMAND, defaultParams, params))
                // avoid parser confusion
                .redirectErrorStream(false);
        // descriptor
        ExecutionDescriptor descriptor = getDescriptor(phpModule)
                .frontWindow(false);
        // run
        return composer
                .run(descriptor, new ExecutionDescriptor.InputProcessorFactory() {
                    @Override
                    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                        return new OutputProcessorImpl(new OutputParser() {
                            @Override
                            public void parse(char[] chars) {
                                for (SearchResult result : Parsers.parseSearch(new String(chars))) {
                                    outputProcessor.process(result);
                                }
                            }
                        });
                    }
                });
    }

    @NbBundle.Messages("Composer.run.show=Composer (show)")
    public Future<Integer> show(PhpModule phpModule, String name, final OutputProcessor<String> outputProcessor) {
        PhpExecutable composer = getComposerExecutable(phpModule, Bundle.Composer_run_show());
        if (composer == null) {
            return null;
        }
        // params
        List<String> defaultParams = new ArrayList<>(DEFAULT_PARAMS);
        defaultParams.remove(ANSI_PARAM);
        composer = composer
                .additionalParameters(mergeParameters(SHOW_COMMAND, defaultParams, Collections.singletonList(name)))
                // avoid parser confusion
                .redirectErrorStream(false);
        // descriptor
        ExecutionDescriptor descriptor = getDescriptor(phpModule)
                .frontWindow(false);
        // run
        return composer
                .run(descriptor, new ExecutionDescriptor.InputProcessorFactory() {
                    @Override
                    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                        return new OutputProcessorImpl(new OutputParser() {
                            @Override
                            public void parse(char[] chars) {
                                String chunk = new String(chars);
                                if (!isValidOutput(chunk)) {
                                    return;
                                }
                                outputProcessor.process(chunk);
                            }
                        });
                    }
                });
    }

    private Future<Integer> runCommand(PhpModule phpModule, String command, String title) {
        return runCommand(phpModule, command, title, Collections.<String>emptyList());
    }

    private Future<Integer> runCommand(PhpModule phpModule, String command, String title, List<String> commandParams) {
        PhpExecutable composer = getComposerExecutable(phpModule, title);
        if (composer == null) {
            return null;
        }
        return composer
                .additionalParameters(mergeParameters(command, DEFAULT_PARAMS, commandParams))
                .run(getDescriptor(phpModule));
    }

    @CheckForNull
    private PhpExecutable getComposerExecutable(PhpModule phpModule, String title) {
        File dir = resolveWorkDir(phpModule);
        if (dir == null
                && phpModule != null) {
            warnNoSources(phpModule.getDisplayName());
            return null;
        }
        PhpExecutable composer = new PhpExecutable(composerPath)
                .optionsSubcategory(ComposerOptionsPanelController.OPTIONS_SUBPATH)
                .displayName(title);
        if (dir != null) {
            composer.workDir(dir);
        }
        return composer;
    }

    private List<String> mergeParameters(String command, List<String> defaultParams, List<String> commandParams) {
        List<String> allParams = new ArrayList<>(defaultParams.size() + commandParams.size() + 1);
        allParams.addAll(defaultParams);
        allParams.add(command);
        allParams.addAll(commandParams);
        return allParams;
    }

    private ExecutionDescriptor getDescriptor(@NullAllowed PhpModule phpModule) {
        ExecutionDescriptor descriptor = PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(ComposerOptionsPanelController.getOptionsPath())
                .inputVisible(false);
        if (phpModule != null) {
            final FileObject sourceDirectory = phpModule.getSourceDirectory();
            if (sourceDirectory != null) {
                descriptor = descriptor
                        .postExecution(new Runnable() {
                            @Override
                            public void run() {
                                // refresh sources after running command
                                sourceDirectory.refresh();
                            }
                        });
            }
        }
        return descriptor;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "Composer.project.noSources=Project {0} has no Source Files."
    })
    private static void warnNoSources(String projectName) {
        DialogDisplayer.getDefault().notifyLater(
                new NotifyDescriptor.Message(Bundle.Composer_project_noSources(projectName), NotifyDescriptor.WARNING_MESSAGE));
    }

    private FileObject getComposerConfigFile(PhpModule phpModule) {
        File dir = resolveWorkDir(phpModule);
        if (dir == null) {
            return null;
        }
        FileObject fo = FileUtil.toFileObject(dir);
        if (fo == null) {
            assert false : "FileObject should be found for file: " + dir;
            return null;
        }
        return fo.getFileObject(COMPOSER_FILENAME);
    }

    private boolean userConfirmation(String title, String question) {
        NotifyDescriptor confirmation = new DialogDescriptor.Confirmation(question, title, DialogDescriptor.YES_NO_OPTION);
        return DialogDisplayer.getDefault().notify(confirmation) == DialogDescriptor.YES_OPTION;
    }

    @CheckForNull
    private File resolveWorkDir(PhpModule phpModule) {
        if (workDir != null) {
            return workDir;
        }
        if (phpModule == null) {
            return null;
        }
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return null;
        }
        return FileUtil.toFile(sourceDirectory);
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(File workDir) {
        assert workDir == null || workDir.isDirectory() : "Existing directory or null expected: " + workDir;
        this.workDir = workDir;
    }

    //~ Inner classes

    public interface OutputProcessor<T> {
        void process(T item);
    }

    private interface OutputParser {
        void parse(char[] chars);
    }

    private static final class OutputProcessorImpl implements InputProcessor {

        private final OutputParser outputParser;


        public OutputProcessorImpl(OutputParser outputParser) {
            this.outputParser = outputParser;
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            outputParser.parse(chars);
        }

        @Override
        public void reset() throws IOException {
            // noop
        }

        @Override
        public void close() throws IOException {
            // noop
        }

    }

}
