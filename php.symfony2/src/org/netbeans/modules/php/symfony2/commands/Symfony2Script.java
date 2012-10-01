/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.symfony2.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.symfony2.preferences.Symfony2Preferences;
import org.netbeans.modules.php.symfony2.ui.options.Symfony2OptionsPanelController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.InputOutput;

/**
 * Represents Symfony2 command line tool.
 */
public final class Symfony2Script {

    private static final Logger LOGGER = Logger.getLogger(Symfony2Script.class.getName());

    private static final List<String> CACHE_CLEAR_COMMAND = Collections.singletonList("cache:clear"); // NOI18N
    private static final List<String> CACHE_WARMUP_COMMAND = Collections.singletonList("cache:warmup"); // NOI18N
    private static final List<String> LIST_COMMANDS_COMMAND = Arrays.asList("list", "--xml"); // NOI18N

    private static final List<String> DEFAULT_PARAMS = Collections.singletonList("--ansi"); // NOI18N

    public static final String SCRIPT_NAME = "console"; // NOI18N

    private final String symfony2Path;


    private Symfony2Script(String symfony2Path) {
        this.symfony2Path = symfony2Path;
    }

    /**
     * @return console script or {@code null} if not valid
     */
    public static FileObject getPath(PhpModule phpModule) {
        return getPath(phpModule, Symfony2Preferences.getAppDir(phpModule));
    }

    /**
     * @return console script or {@code null} if not valid
     */
    public static FileObject getPath(PhpModule phpModule, String relativeAppDir) {
        FileObject appDir = phpModule.getSourceDirectory().getFileObject(relativeAppDir);
        if (appDir == null) {
            // perhaps deleted app dir? fallback to default and let it fail later...
            return null;
        }
        return appDir.getFileObject(SCRIPT_NAME);
    }

    /**
     * Get the project specific, <b>valid only</b> Symfony2 script. If not found, {@code null} is returned.
     * @param phpModule PHP module for which Symfony2 script is taken
     * @param warn <code>true</code> if user is warned when the Symfony2 script is not valid
     * @return Symfony2 console script or {@code null} if the script is not valid
     */
    @Messages({
        "# {0} - error message",
        "MSG_InvalidSymfony2Script=<html>Project''s Symfony2 console script is not valid.<br>({0})"
    })
    public static Symfony2Script forPhpModule(PhpModule phpModule, boolean warn) throws InvalidPhpExecutableException {
        String console = null;
        FileObject script = getPath(phpModule);
        if (script != null) {
            console = FileUtil.toFile(script).getAbsolutePath();
        }
        String error = validate(console);
        if (error == null) {
            return new Symfony2Script(console);
        }
        if (warn) {
            NotifyDescriptor.Message message = new NotifyDescriptor.Message(
                    Bundle.MSG_InvalidSymfony2Script(error),
                    NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(message);
        }
        // in fact should not happen since the console script is used for framework detection
        throw new InvalidPhpExecutableException(error);
    }

    @Messages("Symfony2Script.script.label=Symfony2 console")
    public static String validate(String command) {
        return PhpExecutableValidator.validateCommand(command, Bundle.Symfony2Script_script_label());
    }

    public void clearCache(PhpModule phpModule) {
        runCommand(phpModule, CACHE_CLEAR_COMMAND, null);
    }

    public void cacheWarmUp(PhpModule phpModule) {
        runCommand(phpModule, CACHE_WARMUP_COMMAND, null);
    }

    public void runCommand(PhpModule phpModule, List<String> parameters, Runnable postExecution) {
        createExecutable(phpModule)
                .displayName(getDisplayName(phpModule, parameters.get(0)))
                .additionalParameters(getAllParameters(parameters))
                .run(getDescriptor(postExecution));
    }

    public List<Symfony2CommandVO> getCommands(PhpModule phpModule) {
        File tmpFile;
        try {
            tmpFile = File.createTempFile("nb-symfony2-commands-", ".xml"); // NOI18N
            tmpFile.deleteOnExit();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return null;
        }
        Future<Integer> result = createExecutable(phpModule)
                .fileOutput(tmpFile, true)
                .additionalParameters(LIST_COMMANDS_COMMAND)
                .run(getSilentDescriptor());
        try {
            if (result == null || result.get() != 0) {
                // error => rerun with output window
                runCommand(phpModule, LIST_COMMANDS_COMMAND, null);
                return null;
            }
        } catch (CancellationException ex) {
            // canceled
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, Symfony2OptionsPanelController.OPTIONS_SUBPATH);
            return null;
        }
        List<Symfony2CommandVO> commandsVO = new ArrayList<Symfony2CommandVO>();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile), "UTF-8")); // NOI18N
            Symfony2CommandsXmlParser.parse(reader, commandsVO);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        } finally {
            if (!tmpFile.delete()) {
                LOGGER.info("Cannot delete temporary file");
            }
        }
        if (commandsVO.isEmpty()) {
            // error => rerun with output window
            runCommand(phpModule, LIST_COMMANDS_COMMAND, null);
            return null;
        }
        return commandsVO;
    }

    private PhpExecutable createExecutable(PhpModule phpModule) {
        return new PhpExecutable(symfony2Path)
                .workDir(FileUtil.toFile(phpModule.getSourceDirectory()));
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "# {1} - command",
        "Symfony2Script.command.title={0} ({1})"
    })
    private String getDisplayName(PhpModule phpModule, String command) {
        return Bundle.Symfony2Script_command_title(phpModule.getDisplayName(), command);
    }

    private List<String> getAllParameters(List<String> params) {
        List<String> allParams = new ArrayList<String>(DEFAULT_PARAMS.size() + params.size());
        allParams.addAll(DEFAULT_PARAMS);
        allParams.addAll(params);
        return allParams;
    }

    private ExecutionDescriptor getDescriptor(Runnable postExecution) {
        ExecutionDescriptor executionDescriptor = PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(Symfony2OptionsPanelController.getOptionsPath())
                .inputVisible(false);
        if (postExecution != null) {
            executionDescriptor = executionDescriptor.postExecution(postExecution);
        }
        return executionDescriptor;
    }

    private ExecutionDescriptor getSilentDescriptor() {
        return new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL);
    }

}
