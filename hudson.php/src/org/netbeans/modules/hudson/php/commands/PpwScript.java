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

import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.hudson.php.options.HudsonOptions;
import org.netbeans.modules.hudson.php.ui.options.HudsonOptionsPanelController;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpProgram;
import org.netbeans.modules.php.api.util.FileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Represents <a href="https://github.com/sebastianbergmann/php-project-wizard">ppw</a> command line tool.
 */
public class PpwScript extends PhpProgram {

    public static final String SCRIPT_NAME = "ppw"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);
    // generated files
    public static final String BUILD_XML = "build.xml";  // NOI18N
    public static final String PHPUNIT_XML = "phpunit.xml.dist";  // NOI18N


    public PpwScript(String command) {
        super(command);
    }

    /**
     * Get the default, <b>valid only</b> PPW script.
     * @return the default, <b>valid only</b> PPW script.
     * @throws InvalidPhpProgramException if PPW script is not valid.
     */
    public static PpwScript getDefault() throws InvalidPhpProgramException {
        String ppw = HudsonOptions.getInstance().getPpw();
        String error = validate(ppw);
        if (error != null) {
            throw new InvalidPhpProgramException(error);
        }
        return new PpwScript(ppw);
    }

    public static String validate(String command) {
        return new PpwScript(command).validate();
    }

    @NbBundle.Messages("LBL_PpwScriptPrefix=PPW script: {0}")
    @Override
    public String validate() {
        String error = FileUtils.validateFile(getProgram(), false);
        if (error == null) {
            return null;
        }
        return Bundle.LBL_PpwScriptPrefix(error);
    }

    @NbBundle.Messages({
        "PpwScript.create.title=Hudson job files for {0}",
        "PpwScript.create.progress=Creating Hudson job files for project {0}..."
    })
    public boolean createProjectFiles(PhpModule phpModule) {
        String name = phpModule.getDisplayName();
        FileObject projectDirectory = phpModule.getProjectDirectory();
        ExternalProcessBuilder processBuilder = getProcessBuilder()
                .addArgument("--name") // NOI18N
                .addArgument(name)
                .addArgument("--source") // NOI18N
                .addArgument(relativizePath(projectDirectory, phpModule.getSourceDirectory()))
                .addArgument("--tests") // NOI18N
                .addArgument(relativizePath(projectDirectory, phpModule.getTestDirectory()))
                .addArgument(FileUtil.toFile(projectDirectory).getAbsolutePath());
        ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                .optionsPath(HudsonOptionsPanelController.getOptionsPath());
        Integer status = execute(processBuilder, executionDescriptor,
                Bundle.PpwScript_create_title(name), Bundle.PpwScript_create_progress(name));
        // refresh fs
        projectDirectory.refresh();
        return status != null && status == 0;
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
