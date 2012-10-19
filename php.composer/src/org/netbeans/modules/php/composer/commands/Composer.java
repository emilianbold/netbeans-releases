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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.composer.options.ComposerOptions;
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

    private static final String LOCK_FILENAME = "composer.json"; // NOI18N

    // commands
    private static final String INIT_COMMAND = "init"; // NOI18N
    private static final String INSTALL_COMMAND = "install"; // NOI18N
    private static final String UPDATE_COMMAND = "update"; // NOI18N
    private static final String VALIDATE_COMMAND = "validate"; // NOI18N
    private static final String SELF_UPDATE_COMMAND = "self-update"; // NOI18N
    // params
    private static final List<String> DEFAULT_PARAMS = Arrays.asList(
        "--ansi", // NOI18N
        "--no-interaction" // NOI18N
    );
    private static final String NAME_PARAM = "--name=%s"; // NOI18N
    private static final String AUTHOR_PARAM = "--author=%s <%s>"; // NOI18N
    private static final String DESCRIPTION_PARAM = "--description=%s"; // NOI18N

    private final String composerPath;


    public Composer(String composerPath) {
        this.composerPath = composerPath;
    }

    /**
     * Get the default, <b>valid only</b> Composer.
     * @return the default, <b>valid only</b> Composer.
     * @throws InvalidPhpProgramException if Composer is not valid.
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

    @NbBundle.Messages({
        "Composer.run.init=Composer (init)",
        "Composer.lockFile.exists=Composer lock file already exists - overwrite it?",
        "# {0} - project name",
        "Composer.init.description=Description of project {0}."
    })
    public void init(PhpModule phpModule) {
        FileObject lockFile = getLockFile(phpModule);
        if (lockFile != null && lockFile.isValid()) {
            if (!userConfirmation(phpModule.getDisplayName(), Bundle.Composer_lockFile_exists())) {
                return;
            }
        }
        // command params
        ComposerOptions options = ComposerOptions.getInstance();
        List<String> params = Arrays.asList(
                String.format(NAME_PARAM, getInitName(options.getVendor(), phpModule.getName())),
                String.format(AUTHOR_PARAM, options.getAuthorName(), options.getAuthorEmail()),
                String.format(DESCRIPTION_PARAM, Bundle.Composer_init_description(phpModule.getDisplayName())));
        runCommand(phpModule, INIT_COMMAND, Bundle.Composer_run_init(), params);
    }

    private String getInitName(String vendor, String projectName) {
        StringBuilder name = new StringBuilder(50);
        name.append(vendor);
        name.append('/'); // NOI18N
        name.append(StringUtils.webalize(projectName));
        return name.toString();
    }

    @NbBundle.Messages("Composer.run.install=Composer (install)")
    public void install(PhpModule phpModule) {
        runCommand(phpModule, INSTALL_COMMAND, Bundle.Composer_run_install());
    }

    @NbBundle.Messages("Composer.run.update=Composer (update)")
    public void update(PhpModule phpModule) {
        runCommand(phpModule, UPDATE_COMMAND, Bundle.Composer_run_update());
    }

    @NbBundle.Messages("Composer.run.validate=Composer (validate)")
    public void validate(PhpModule phpModule) {
        runCommand(phpModule, VALIDATE_COMMAND, Bundle.Composer_run_validate());
    }

    @NbBundle.Messages("Composer.run.selfUpdate=Composer (self-update)")
    public void selfUpdate(PhpModule phpModule) {
        runCommand(phpModule, SELF_UPDATE_COMMAND, Bundle.Composer_run_selfUpdate());
    }

    private void runCommand(PhpModule phpModule, String command, String title) {
        runCommand(phpModule, command, title, Collections.<String>emptyList());
    }

    private void runCommand(PhpModule phpModule, String command, String title, List<String> commandParams) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            warnNoSources(phpModule.getDisplayName());
            return;
        }
        new PhpExecutable(composerPath)
                .optionsSubcategory(ComposerOptionsPanelController.OPTIONS_SUBPATH)
                .workDir(FileUtil.toFile(sourceDirectory))
                .displayName(title)
                .additionalParameters(getAllParameters(command, commandParams))
                .run(getDescriptor());
    }

    private List<String> getAllParameters(String command, List<String> commandParams) {
        List<String> allParams = new ArrayList<String>(DEFAULT_PARAMS.size() + commandParams.size() + 1);
        allParams.addAll(DEFAULT_PARAMS);
        allParams.add(command);
        allParams.addAll(commandParams);
        return allParams;
    }

    private ExecutionDescriptor getDescriptor() {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(ComposerOptionsPanelController.getOptionsPath())
                .inputVisible(false);
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "Composer.project.noSources=Project {0} has no Source Files."
    })
    private static void warnNoSources(String projectName) {
        DialogDisplayer.getDefault().notifyLater(
                new NotifyDescriptor.Message(Bundle.Composer_project_noSources(projectName), NotifyDescriptor.WARNING_MESSAGE));
    }

    private FileObject getLockFile(PhpModule phpModule) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return null;
        }
        return sourceDirectory.getFileObject(LOCK_FILENAME);
    }

    private boolean userConfirmation(String title, String question) {
        NotifyDescriptor confirmation = new DialogDescriptor.Confirmation(question, title, DialogDescriptor.YES_NO_OPTION);
        return DialogDisplayer.getDefault().notify(confirmation) == DialogDescriptor.YES_OPTION;
    }

}
