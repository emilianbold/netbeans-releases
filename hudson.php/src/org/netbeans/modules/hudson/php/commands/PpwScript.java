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
package org.netbeans.modules.hudson.php.commands;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.hudson.php.options.HudsonOptions;
import org.netbeans.modules.hudson.php.ui.options.HudsonOptionsPanelController;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Represents <a href="https://github.com/sebastianbergmann/php-project-wizard">ppw</a> command line tool.
 */
public final class PpwScript {

    private static final Logger LOGGER = Logger.getLogger(PpwScript.class.getName());

    public static final String SCRIPT_NAME = "ppw"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);
    // generated files
    public static final String BUILD_XML = "build.xml";  // NOI18N
    public static final String PHPUNIT_XML = "phpunit.xml.dist";  // NOI18N
    // command params
    public static final String PHPCS_RULESET_PARAM = "--phpcs";  // NOI18N
    public static final List<String> PHPCS_RULESET_OPTIONS = Arrays.asList("PEAR", "Zend", "PHPCS", "Squiz", "MySource"); // NOI18N


    private final String ppwPath;

    private PpwScript(String ppwPath) {
        this.ppwPath = ppwPath;
    }

    /**
     * Get the default, <b>valid only</b> PPW script.
     * @return the default, <b>valid only</b> PPW script.
     * @throws InvalidPhpExecutableException if PPW script is not valid.
     */
    public static PpwScript getDefault() throws InvalidPhpExecutableException {
        String ppw = HudsonOptions.getInstance().getPpw();
        String error = validate(ppw);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new PpwScript(ppw);
    }

    @NbBundle.Messages("PpwScript.script.label=PPW script")
    public static String validate(String command) {
        return PhpExecutableValidator.validateCommand(command, Bundle.PpwScript_script_label());
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "PpwScript.create.title=Hudson job files for {0}",
        "# {0} - project name",
        "PpwScript.create.progress=Creating Hudson job files for project {0}..."
    })
    public boolean createProjectFiles(PhpModule phpModule, Map<String, String> optionalParams) {
        assert !EventQueue.isDispatchThread();

        String name = phpModule.getDisplayName();
        FileObject projectDirectory = phpModule.getProjectDirectory();
        File projectDir = FileUtil.toFile(projectDirectory);
        Map<String, String> params = new LinkedHashMap<String, String>();
        if (optionalParams != null) {
            params.putAll(optionalParams);
        }
        params.put("--name", name); // NOI18N
        params.put("--source", relativizePath(projectDirectory, phpModule.getSourceDirectory())); // NOI18N
        params.put("--tests", relativizePath(projectDirectory, phpModule.getTestDirectory())); // NOI18N

        List<String> allParams = new ArrayList<String>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            allParams.add(entry.getKey());
            allParams.add(entry.getValue());
        }
        allParams.add(projectDir.getAbsolutePath());

        ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                .optionsPath(HudsonOptionsPanelController.getOptionsPath());

        try {
            Integer status = new PhpExecutable(ppwPath)
                    .additionalParameters(allParams)
                    .workDir(projectDir)
                    .runAndWait(executionDescriptor, "Creating project files"); // NOI18N
            if (status != null) {
                // refresh fs
                projectDirectory.refresh();
                return status == 0;
            }
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return false;
    }

    private String relativizePath(FileObject parent, FileObject child) {
        if (parent.equals(child)) {
            return "."; // NOI18N
        }
        String relativePath = FileUtil.getRelativePath(parent, child);
        if (relativePath != null) {
            return relativePath;
        }
        return FileUtil.toFile(child).getAbsolutePath();
    }

}
