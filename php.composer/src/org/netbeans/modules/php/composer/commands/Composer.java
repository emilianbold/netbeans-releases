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

import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.php.api.phpmodule.PhpInterpreter;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpProgram;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.composer.options.ComposerOptions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Represents <a href="http://getcomposer.org/">Composer</a> command line tool.
 */
public final class Composer extends PhpProgram {

    public static final String NAME = "composer"; // NOI18N
    public static final String LONG_NAME = NAME + ".phar"; // NOI18N

    private static final String[] DEFAULT_PARAMS = {
        "--ansi", // NOI18N
        "--no-interaction", // NOI18N
    };
    private static final String INIT_COMMAND = "init"; // NOI18N
    private static final String INSTALL_COMMAND = "install"; // NOI18N
    private static final String UPDATE_COMMAND = "update"; // NOI18N
    private static final String VALIDATE_COMMAND = "validate"; // NOI18N
    private static final String SELF_UPDATE_COMMAND = "self-update"; // NOI18N


    public Composer(String command) {
        super(command);
    }

    /**
     * Get the default, <b>valid only</b> Composer.
     * @return the default, <b>valid only</b> Composer.
     * @throws InvalidPhpProgramException if Composer is not valid.
     */
    public static Composer getDefault() throws InvalidPhpProgramException {
        String composerPath = ComposerOptions.getInstance().getComposerPath();
        String error = validate(composerPath);
        if (error != null) {
            throw new InvalidPhpProgramException(error);
        }
        return new Composer(composerPath);
    }

    public static String validate(String command) {
        return new Composer(command).validate();
    }

    @NbBundle.Messages("Composer.script.label=Composer")
    @Override
    public String validate() {
        return FileUtils.validateFile(Bundle.Composer_script_label(), getProgram(), false);
    }

    @NbBundle.Messages("Composer.run.init=Composer init")
    public void init(PhpModule phpModule) {
        runCommand(phpModule, INIT_COMMAND, Bundle.Composer_run_init());
    }

    @NbBundle.Messages("Composer.run.install=Composer install")
    public void install(PhpModule phpModule) {
        runCommand(phpModule, INSTALL_COMMAND, Bundle.Composer_run_install());
    }

    @NbBundle.Messages("Composer.run.update=Composer update")
    public void update(PhpModule phpModule) {
        runCommand(phpModule, UPDATE_COMMAND, Bundle.Composer_run_update());
    }

    @NbBundle.Messages("Composer.run.validate=Composer validate")
    public void validate(PhpModule phpModule) {
        runCommand(phpModule, VALIDATE_COMMAND, Bundle.Composer_run_validate());
    }

    @NbBundle.Messages("Composer.run.selfUpdate=Composer self-update")
    public void selfUpdate(PhpModule phpModule) {
        runCommand(phpModule, SELF_UPDATE_COMMAND, Bundle.Composer_run_selfUpdate());
    }

    private void runCommand(PhpModule phpModule, String command, String title) {
        ExternalProcessBuilder processBuilder = getBuilder(phpModule, command);
        if (processBuilder == null) {
            warnNoSources(phpModule.getDisplayName());
            return;
        }
        PhpProgram.executeLater(processBuilder, getDescriptor(), title);
    }

    private ExternalProcessBuilder getBuilder(PhpModule phpModule, String command) {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            return null;
        }
        // run file via php interpreter
        try {
            ExternalProcessBuilder processBuilder = PhpInterpreter.getDefault()
                    .getProcessBuilder()
                    .workingDirectory(FileUtil.toFile(sourceDirectory))
                    .redirectErrorStream(true)
                    .addArgument(getProgram());
            for (String param : getParameters()) {
                processBuilder = processBuilder.addArgument(param);
            }
            for (String param : DEFAULT_PARAMS) {
                processBuilder = processBuilder.addArgument(param);
            }
            return processBuilder.addArgument(command);
        } catch (InvalidPhpProgramException ex) {
            // ignored
        }
        return super.getProcessBuilder();
    }

    private ExecutionDescriptor getDescriptor() {
        return getExecutionDescriptor()
                .inputVisible(false);
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "Composer.project.noSources=Project {0} has no Source Files."
    })
    public static void warnNoSources(String projectName) {
        DialogDisplayer.getDefault().notifyLater(
                new NotifyDescriptor.Message(Bundle.Composer_project_noSources(projectName), NotifyDescriptor.WARNING_MESSAGE));
    }

}
